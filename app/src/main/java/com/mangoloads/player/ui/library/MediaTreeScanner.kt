package com.mangoloads.player.ui.library

import java.util.ArrayDeque
import java.io.IOException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * A single entry returned by a folder provider. Document IDs are opaque: callers
 * must not treat them as filesystem paths or use names to infer ancestry.
 */
internal data class TreeEntry(
    val documentId: String,
    val displayName: String,
    val mimeType: String?,
    val isDirectory: Boolean,
)

/** A provider may return rows before its directory listing is complete. */
internal data class TreeListing(
    val entries: List<TreeEntry>,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
)

internal data class ScanLimits(
    val entries: Int = 50_000,
    val directories: Int = 10_000,
    val depth: Int = 128,
    val childrenPerDirectory: Int = 10_000,
) {
    init { require(entries > 0 && directories > 0 && depth > 0 && childrenPerDirectory > 0) }
}

internal enum class LocalMediaKind { VIDEO, SUBTITLE }

internal data class DiscoveredLocalMedia(
    val documentId: String,
    val displayName: String,
    val kind: LocalMediaKind,
    /** Opaque SAF parent ID captured during immediate-child enumeration. */
    val parentDocumentId: String = "",
)

/**
 * Sequential, cancellable, read-only traversal of one chosen document tree.
 *
 * The platform adapter must query only immediate children using the selected
 * SAF tree URI, and must validate each returned ID against that root. A failed
 * query or validation fails the entire scan instead of returning a misleading
 * partial result that could later cause an incremental index to delete entries.
 */
internal class MediaTreeScanner(
    private val listChildren: suspend (parentDocumentId: String) -> TreeListing,
    private val isWithinSelectedTree: suspend (rootDocumentId: String, documentId: String) -> Boolean,
    private val limits: ScanLimits = ScanLimits(),
) {
    suspend fun scan(rootDocumentId: String): List<DiscoveredLocalMedia> {
        require(rootDocumentId.isNotBlank()) { "The selected root must have a document ID" }

        val pendingDirectories = ArrayDeque<Pair<String, Int>>()
        val visitedIds = mutableSetOf(rootDocumentId)
        val media = mutableListOf<DiscoveredLocalMedia>()
        pendingDirectories.add(rootDocumentId to 0)
        var entriesSeen = 0
        var directoriesSeen = 1

        while (pendingDirectories.isNotEmpty()) {
            currentCoroutineContext().ensureActive()
            val (parentId, parentDepth) = pendingDirectories.removeFirst()
            val listing = listChildren(parentId)
            if (listing.isLoading || listing.hasError || listing.entries.size > limits.childrenPerDirectory) {
                // Never reconcile cached rows against a partial provider snapshot.
                throw IOException("Local folder listing is incomplete; retry the scan")
            }
            for (entry in listing.entries) {
                currentCoroutineContext().ensureActive()
                if (++entriesSeen > limits.entries) throw IOException("Local scan entry limit reached")
                if (entry.documentId.isBlank() || entry.displayName.isBlank()) {
                    throw IOException("Local provider returned an invalid entry")
                }
                if (entry.displayName.startsWith(".")) continue
                if (!isWithinSelectedTree(rootDocumentId, entry.documentId)) {
                    throw IOException("Local provider returned a document outside the selected tree")
                }
                if (!visitedIds.add(entry.documentId)) continue

                if (entry.isDirectory) {
                    if (++directoriesSeen > limits.directories || parentDepth + 1 > limits.depth) {
                        throw IOException("Local scan directory limit reached")
                    }
                    pendingDirectories.addLast(entry.documentId to parentDepth + 1)
                } else {
                    val kind = classifyMedia(entry) ?: continue
                    media.add(
                        DiscoveredLocalMedia(
                            documentId = entry.documentId,
                            displayName = entry.displayName,
                            kind = kind,
                            parentDocumentId = parentId,
                        ),
                    )
                }
            }
        }
        return media
    }
}

private val VIDEO_EXTENSIONS = setOf(
    "mp4", "m4v", "mkv", "avi", "mov", "webm", "ts", "m2ts",
    "mpg", "mpeg", "wmv", "flv", "3gp", "3g2",
)
private val VIDEO_MIME_TYPES = setOf(
    "video/mp4", "video/x-matroska", "video/webm", "video/quicktime",
    "video/x-msvideo", "video/mpeg", "video/mp2t", "video/3gpp",
    "video/3gpp2", "video/x-ms-wmv", "video/x-flv",
)
private val SUBTITLE_EXTENSIONS = setOf("srt", "vtt", "ass", "ssa", "ttml", "sub")

private fun classifyMedia(entry: TreeEntry): LocalMediaKind? {
    val extension = entry.displayName.substringAfterLast('.', "").lowercase()
    val mimeType = entry.mimeType?.lowercase()
    return when {
        extension in SUBTITLE_EXTENSIONS -> LocalMediaKind.SUBTITLE
        extension in VIDEO_EXTENSIONS || mimeType in VIDEO_MIME_TYPES -> LocalMediaKind.VIDEO
        else -> null
    }
}

