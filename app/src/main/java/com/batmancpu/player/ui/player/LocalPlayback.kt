package com.batmancpu.player.ui.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import androidx.media3.datasource.TransferListener
import android.provider.DocumentsContract
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Tracks
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ContentDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.BandwidthMeter
import java.security.MessageDigest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class LocalPlaybackTarget(
    val tree: String,
    val documentId: String,
    val uri: String,
    val label: String,
    val positionMs: Long = 0,
    val playWhenReady: Boolean = true,
    val sidecarSubtitles: List<LocalPlaybackSubtitle> = emptyList(),
)
internal data class LocalPlaybackSubtitle(
    val documentId: String,
    val parentDocumentId: String,
    val displayName: String,
    val mimeType: String,
    val uri: String,
)
internal data class LocalPlaybackState(
    val target: LocalPlaybackTarget? = null,
    val loading: Boolean = false,
    val failed: Boolean = false,
)

internal object LocalPlaybackAccess {
    suspend fun resolve(
        context: Context,
        tree: String,
        documentId: String,
        label: String,
        parentDocumentId: String = "",
        sidecarCandidates: List<LocalPlaybackSubtitle> = emptyList(),
    ): LocalPlaybackTarget =
        withContext(Dispatchers.IO) {
            val root = Uri.parse(tree)
            require(root.scheme == "content" && root.authority == "com.android.externalstorage.documents" && DocumentsContract.isTreeUri(root))
            require(documentId.isNotBlank() && documentId != DocumentsContract.getTreeDocumentId(root))
            if (context.contentResolver.persistedUriPermissions.none { it.uri == root && it.isReadPermission }) {
                throw SecurityException("Selected folder access is unavailable")
            }
            val parent = DocumentsContract.buildDocumentUriUsingTree(root, DocumentsContract.getTreeDocumentId(root))
            val document = DocumentsContract.buildDocumentUriUsingTree(root, documentId)
            if (!DocumentsContract.isChildDocument(context.contentResolver, parent, document)) {
                throw SecurityException("Document is outside the selected folder")
            }
            val safeSidecars = if (parentDocumentId.isBlank()) emptyList() else try {
                resolveSidecars(context, root, parentDocumentId, documentId, sidecarCandidates)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                emptyList()
            }
            LocalPlaybackTarget(
                tree, documentId, document.toString(), label,
                ResumePositions.load(context, tree, documentId),
                sidecarSubtitles = safeSidecars,
            )
        }

    /** Revalidate candidates against a fresh immediate-child listing; stale items never block video playback. */
    private fun resolveSidecars(
        context: Context,
        tree: Uri,
        parentDocumentId: String,
        videoDocumentId: String,
        candidates: List<LocalPlaybackSubtitle>,
    ): List<LocalPlaybackSubtitle> {
        if (candidates.isEmpty() || candidates.size > MAX_SIDECARS) return emptyList()
        val resolver = context.contentResolver
        val rootId = DocumentsContract.getTreeDocumentId(tree)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(tree, parentDocumentId)
        val rootUri = DocumentsContract.buildDocumentUriUsingTree(tree, rootId)
        if (parentDocumentId != rootId && !DocumentsContract.isChildDocument(resolver, rootUri, parentUri)) return emptyList()
        val expected = candidates
            .filter { it.parentDocumentId == parentDocumentId }
            .mapNotNull { candidate ->
                subtitleMimeType(candidate.displayName)?.let { mime -> candidate.documentId to (candidate to mime) }
            }.toMap()
        if (expected.isEmpty()) return emptyList()
        val found = LinkedHashMap<String, LocalPlaybackSubtitle>()
        var videoIsImmediateChild = false
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, parentDocumentId)
        resolver.query(
            children,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE),
            null, null, null,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
            if (idColumn < 0 || nameColumn < 0 || mimeColumn < 0) return emptyList()
            var visited = 0
            while (cursor.moveToNext() && visited++ < MAX_PARENT_CHILDREN &&
                (found.size < expected.size || !videoIsImmediateChild)) {
                val id = cursor.getString(idColumn) ?: continue
                if (id == videoDocumentId) videoIsImmediateChild = true
                val (candidate, expectedMime) = expected[id] ?: continue
                val name = cursor.getString(nameColumn) ?: continue
                val actualType = cursor.getString(mimeColumn) ?: continue
                if (actualType == DocumentsContract.Document.MIME_TYPE_DIR ||
                    name != candidate.displayName || subtitleMimeType(name) != expectedMime) continue
                val uri = DocumentsContract.buildDocumentUriUsingTree(tree, id)
                if (!DocumentsContract.isChildDocument(resolver, parentUri, uri)) continue
                found[id] = candidate.copy(mimeType = expectedMime, uri = uri.toString())
            }
            if (visited >= MAX_PARENT_CHILDREN && (found.size < expected.size || !videoIsImmediateChild)) return emptyList()
        } ?: return emptyList()
        if (!videoIsImmediateChild) return emptyList()
        return candidates.mapNotNull { found[it.documentId] }.distinctBy { it.documentId }.take(MAX_SIDECARS)
    }

    private const val MAX_SIDECARS = 8
    private const val MAX_PARENT_CHILDREN = 10_000
}

private fun subtitleMimeType(displayName: String): String? = when (displayName.substringAfterLast('.', "").lowercase()) {
    "srt" -> "application/x-subrip"
    "vtt" -> "text/vtt"
    "ass", "ssa" -> "text/x-ssa"
    "ttml" -> "application/ttml+xml"
    else -> null
}

@androidx.annotation.OptIn(UnstableApi::class)
internal fun createLocalMediaItem(target: LocalPlaybackTarget): MediaItem =
    MediaItem.Builder()
        .setUri(target.uri)
        .setSubtitleConfigurations(target.sidecarSubtitles.map { subtitle ->
            MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitle.uri))
                .setMimeType(subtitle.mimeType)
                .setLabel(subtitle.displayName)
                .build()
        })
        .build()

internal object ResumePositions {
    private fun key(tree: String, id: String): String = MessageDigest.getInstance("SHA-256")
        .digest((tree + "\u0000" + id).toByteArray()).joinToString("") { "%02x".format(it) }
    suspend fun load(context: Context, tree: String, id: String): Long = withContext(Dispatchers.IO) {
        context.getSharedPreferences("player_resume_positions", Context.MODE_PRIVATE).getLong(key(tree, id), 0).coerceAtLeast(0)
    }
    suspend fun save(context: Context, target: LocalPlaybackTarget, position: Long) = withContext(Dispatchers.IO) {
        context.getSharedPreferences("player_resume_positions", Context.MODE_PRIVATE).edit()
            .putLong(key(target.tree, target.documentId), position.coerceAtLeast(0)).apply()
    }

    /** The selected tree is the app's only library, so forgetting it clears all old checkpoints too. */
    suspend fun clearAll(context: Context) = withContext(Dispatchers.IO) {
        check(context.getSharedPreferences("player_resume_positions", Context.MODE_PRIVATE).edit().clear().commit()) {
            "Playback resume state could not be cleared"
        }
    }
}

/** No HTTP data source, connectivity observer, wake lock or platform metrics listener. */
@androidx.annotation.OptIn(UnstableApi::class)
internal fun createLocalPlayer(context: Context): ExoPlayer = ExoPlayer.Builder(context)
    .setMediaSourceFactory(DefaultMediaSourceFactory(DataSource.Factory { ContentDataSource(context) }))
    .setBandwidthMeter(LocalBandwidthEstimate)
    .setUsePlatformDiagnostics(false)
    .setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true)
    .setHandleAudioBecomingNoisy(true)
    .build()

/** A fixed positive estimate for local tracks; never observes connectivity or transfers. */
@androidx.annotation.OptIn(UnstableApi::class)
private object LocalBandwidthEstimate : BandwidthMeter {
    override fun getBitrateEstimate(): Long = 10_000_000L
    override fun getTransferListener(): TransferListener? = null
    override fun addEventListener(handler: Handler, listener: BandwidthMeter.EventListener) = Unit
    override fun removeEventListener(listener: BandwidthMeter.EventListener) = Unit
}


/** True only when Media3 found at least one decoder-supported track of this type. */
internal fun hasSupportedTrackType(tracks: Tracks, trackType: Int): Boolean =
    tracks.groups.any { it.type == trackType && it.isSupported }
