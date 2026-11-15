package com.batmancpu.player.ui.player

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ContentDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.BandwidthMeter
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class LocalPlaybackTarget(
    val tree: String,
    val documentId: String,
    val uri: String,
    val label: String,
    val positionMs: Long = 0,
    val playWhenReady: Boolean = true,
)
internal data class LocalPlaybackState(
    val target: LocalPlaybackTarget? = null,
    val loading: Boolean = false,
    val failed: Boolean = false,
)

internal object LocalPlaybackAccess {
    suspend fun resolve(context: Context, tree: String, documentId: String, label: String): LocalPlaybackTarget =
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
            LocalPlaybackTarget(tree, documentId, document.toString(), label,
                ResumePositions.load(context, tree, documentId))
        }
}

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
}

/** No HTTP data source, connectivity observer, wake lock or platform metrics listener. */
@androidx.annotation.OptIn(UnstableApi::class)
internal fun createLocalPlayer(context: Context): ExoPlayer = ExoPlayer.Builder(context)
    .setMediaSourceFactory(DefaultMediaSourceFactory(DataSource.Factory { ContentDataSource(context) }))
    .setBandwidthMeter(BandwidthMeter.NO_OP)
    .setUsePlatformDiagnostics(false)
    .setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true)
    .setHandleAudioBecomingNoisy(true)
    .build()
