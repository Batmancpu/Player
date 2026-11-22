package com.mangoloads.player.ui.library

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Only image documents beside a title inside the one granted SAF tree. */
internal data class LocalArtworkCandidate(
    val documentId: String,
    val displayName: String,
    val mimeType: String,
)

/** Filename-only association. No media bytes, network lookup or fuzzy remote match. */
internal object LocalArtworkMatcher {
    private val imageExtensions = setOf("jpg", "jpeg", "png", "webp")
    private val suffixes = listOf("poster", "cover", "thumb", "thumbnail")
    private val genericNames = setOf("poster", "cover", "folder", "movie", "series", "season")

    fun choose(title: LocalTitle, video: DiscoveredLocalMedia, siblings: List<LocalArtworkCandidate>): String? {
        val videoStem = normalized(video.displayName)
        val titleStem = normalized(title.title + (title.year?.toString().orEmpty()))
        val videoCount = siblings.count { isVideo(it.displayName, it.mimeType) }
        return siblings.asSequence()
            .filter(::isImage)
            .mapNotNull { image ->
                val imageStem = normalized(image.displayName)
                val stripped = suffixes.firstOrNull(imageStem::endsWith)?.let(imageStem::removeSuffix) ?: imageStem
                val score = when {
                    imageStem == videoStem -> 500
                    stripped == videoStem -> 450
                    imageStem == titleStem -> 400
                    stripped == titleStem -> 350
                    title.isSeries && stripped == normalized(title.title) -> 300
                    title.isSeries && imageStem in genericNames -> 200
                    videoCount == 1 && imageStem in genericNames -> 150
                    else -> return@mapNotNull null
                }
                score to image
            }
            .sortedWith(compareByDescending<Pair<Int, LocalArtworkCandidate>> { it.first }
                .thenBy { it.second.displayName.lowercase() })
            .firstOrNull()?.second?.documentId
    }

    private fun isImage(candidate: LocalArtworkCandidate): Boolean =
        candidate.mimeType.startsWith("image/", ignoreCase = true) &&
            candidate.displayName.substringAfterLast('.', "").lowercase() in imageExtensions

    private fun isVideo(name: String, mime: String): Boolean =
        mime.startsWith("video/", ignoreCase = true) ||
            name.substringAfterLast('.', "").lowercase() in setOf("mp4", "m4v", "mkv", "avi", "mov", "webm", "ts", "m2ts")

    private fun normalized(name: String): String = name.substringBeforeLast('.', name)
        .lowercase().replace(Regex("[^\\p{L}\\p{N}]"), "")
}

/**
 * Resolves adjacent artwork lazily for visible cards, never scanning image
 * bytes or adding images to the persistent media index. Directory listings are
 * bounded and cached briefly for this screen instance only.
 */
internal class LocalArtworkResolver private constructor(
    context: Context,
    private val treeUri: Uri,
) {
    private val appContext = context.applicationContext
    private val resolver = appContext.contentResolver
    private val rootId = DocumentsContract.getTreeDocumentId(treeUri)
    private val rootUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, rootId)
    private val gate = Mutex()
    private val siblingCache = LinkedHashMap<String, List<LocalArtworkCandidate>>(16, .75f, true)

    companion object {
        fun create(context: Context, tree: String): LocalArtworkResolver? = runCatching {
            val uri = Uri.parse(tree)
            if (!DocumentsContract.isTreeUri(uri) ||
                uri.authority != "com.android.externalstorage.documents") return null
            LocalArtworkResolver(context, uri)
        }.getOrNull()
    }

    suspend fun find(title: LocalTitle): Uri? {
        if (!DocumentsContract.isTreeUri(treeUri) ||
            resolver.persistedUriPermissions.none { it.uri == treeUri && it.isReadPermission }) return null

        for (video in title.files.filter { it.kind == LocalMediaKind.VIDEO }) {
            val parentId = video.parentDocumentId.takeIf(String::isNotBlank) ?: rootId
            if (!isGrantedParent(parentId)) continue
            try {
                val siblings = siblings(parentId)
                val chosenId = LocalArtworkMatcher.choose(title, video, siblings) ?: continue
                val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, chosenId)
                if (DocumentsContract.isChildDocument(resolver, rootUri, uri)) return uri
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Unavailable image providers fall back to the library's local artwork placeholder.
            }
        }
        return null
    }

    private suspend fun isGrantedParent(parentId: String): Boolean {
        if (parentId == rootId) return true
        val parent = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentId)
        return DocumentsContract.isChildDocument(resolver, rootUri, parent)
    }

    private suspend fun siblings(parentId: String): List<LocalArtworkCandidate> = gate.withLock {
        siblingCache[parentId]?.let { return@withLock it }
        val uri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        )
        val children = resolver.readSafCursor(uri, projection) { cursor, checkActive ->
            val idColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val found = ArrayList<LocalArtworkCandidate>()
            while (cursor.moveToNext()) {
                checkActive()
                if (found.size >= MAX_SIBLING_FILES) throw java.io.IOException("Local artwork directory is too large")
                val id = cursor.getString(idColumn)?.takeIf(String::isNotBlank) ?: continue
                val name = cursor.getString(nameColumn)?.takeIf(String::isNotBlank) ?: continue
                val mime = cursor.getString(mimeColumn)?.takeIf(String::isNotBlank) ?: continue
                if (!name.startsWith(".") && mime != DocumentsContract.Document.MIME_TYPE_DIR) {
                    found += LocalArtworkCandidate(id, name, mime)
                }
            }
            found
        }
        siblingCache[parentId] = children
        if (siblingCache.size > MAX_CACHED_DIRECTORIES) siblingCache.remove(siblingCache.keys.first())
        children
    }

    private companion object {
        const val MAX_SIBLING_FILES = 2_048
        const val MAX_CACHED_DIRECTORIES = 32
    }
}
