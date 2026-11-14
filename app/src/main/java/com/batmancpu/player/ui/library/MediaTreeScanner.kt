package com.batmancpu.player.ui.library

import java.util.ArrayDeque
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

internal enum class LocalMediaKind { VIDEO, SUBTITLE }

internal data class DiscoveredLocalMedia(
    val documentId: String,
    val displayName: String,
    val kind: LocalMediaKind,
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
    private val listChildren: suspend (parentDocumentId: String) -> List<TreeEntry>,
    private val isWithinSelectedTree: suspend (rootDocumentId: String, documentId: String) -> Boolean,
) {
    suspend fun scan(rootDocumentId: String): List<DiscoveredLocalMedia> {
        require(rootDocumentId.isNotBlank()) { "The selected root must have a document ID" }

        val pendingDirectories = ArrayDeque<String>()
        val visitedIds = mutableSetOf(rootDocumentId)
        val media = mutableListOf<DiscoveredLocalMedia>()
        pendingDirectories.add(rootDocumentId)

        while (pendingDirectories.isNotEmpty()) {
            currentCoroutineContext().ensureActive()
            val parentId = pendingDirectories.removeFirst()
            for (entry in listChildren(parentId)) {
                currentCoroutineContext().ensureActive()
                if (entry.documentId.isBlank() ||
                    entry.displayName.isBlank() ||
                    entry.displayName.startsWith(".")
                ) {
                    continue
                }
                if (!isWithinSelectedTree(rootDocumentId, entry.documentId)) continue
                if (!visitedIds.add(entry.documentId)) continue

                if (entry.isDirectory) {
                    pendingDirectories.addLast(entry.documentId)
                } else {
                    val kind = classifyMedia(entry) ?: continue
                    media.add(
                        DiscoveredLocalMedia(
                            documentId = entry.documentId,
                            displayName = entry.displayName,
                            kind = kind,
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
