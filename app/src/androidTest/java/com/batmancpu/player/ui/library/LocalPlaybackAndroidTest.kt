package com.batmancpu.player.ui.library

import android.content.Intent
import android.net.Uri
import androidx.media3.common.Player
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.test.platform.app.InstrumentationRegistry
import com.batmancpu.player.MainActivity
import com.batmancpu.player.ui.player.*
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class LocalPlaybackAndroidTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext

    @Test(timeout = 30_000) fun contentOnlyEngineRendersOwnedH264Fixture() {
        val fixture = File(context.cacheDir, "synthetic-video.mp4")
        instrumentation.context.assets.open("synthetic-video.mp4").use { input -> fixture.outputStream().use { output -> input.copyTo(output) } }
        SyntheticSafProvider.reset()
        SyntheticSafProvider.playbackFile = fixture
        SyntheticSafProvider.subtitleContents = "1\n00:00:00,000 --> 00:00:02,000\nSynthetic local subtitle\n".toByteArray()
        val frame = CountDownLatch(1)
        val failure = AtomicReference<String?>()
        val reference = AtomicReference<ExoPlayer>()
        val activity = instrumentation.startActivitySync(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        try {
            instrumentation.runOnMainSync {
                val player = createLocalPlayer(context)
                reference.set(player)
                player.volume = 0f
                activity.setContentView(PlayerView(activity).apply { this.player = player })
                player.addListener(object : Player.Listener {
                    override fun onRenderedFirstFrame() { frame.countDown() }
                    override fun onPlayerError(error: PlaybackException) { failure.set(error.errorCodeName); frame.countDown() }
                })
                player.setMediaItem(createLocalMediaItem(LocalPlaybackTarget(
                    tree = "synthetic-tree", documentId = "synthetic-video",
                    uri = "content://com.batmancpu.player.synthetic/video", label = "Synthetic",
                    sidecarSubtitles = listOf(LocalPlaybackSubtitle(
                        documentId = "synthetic-subtitle", parentDocumentId = "synthetic-parent",
                        displayName = "Synthetic.en.srt", mimeType = "application/x-subrip",
                        uri = "content://com.batmancpu.player.synthetic/subtitle",
                    )),
                )))
                player.prepare(); player.play()
            }
            assertTrue("No synthetic first frame", frame.await(20, TimeUnit.SECONDS))
            assertNull(failure.get())
            instrumentation.runOnMainSync {
                val player = reference.get()
                assertTrue(player.duration > 0)
                assertTrue("Synthetic MP4 must expose embedded audio", hasSupportedTrackType(player.currentTracks, C.TRACK_TYPE_AUDIO))
                assertTrue("Synthetic local SRT sidecar was not decoded", hasSupportedTrackType(player.currentTracks, C.TRACK_TYPE_TEXT))
                val selector = createTrackSelectionDialog(activity, "Audio", player, C.TRACK_TYPE_AUDIO)
                selector.show()
                assertTrue("Media3 audio selector did not open", selector.isShowing)
                selector.dismiss()
                val subtitleSelector = createTrackSelectionDialog(activity, "Subtitles", player, C.TRACK_TYPE_TEXT)
                subtitleSelector.show()
                assertTrue("Media3 subtitle selector did not open", subtitleSelector.isShowing)
                subtitleSelector.dismiss()
                player.pause()
                player.seekTo(1000)
                assertEquals(1000L, player.currentPosition)
            }
        } finally {
            instrumentation.runOnMainSync { reference.get()?.release(); activity.finish() }
            SyntheticSafProvider.playbackFile = null
            SyntheticSafProvider.subtitleContents = null
            fixture.delete()
        }
    }

    @Test fun rejectsRemoteAndUnselectedProvidersBeforeOpeningDocuments() {
        assertThrows(IllegalArgumentException::class.java) { runBlocking { LocalPlaybackAccess.resolve(context, "https://invalid.example/video", "id", "Synthetic") } }
        assertThrows(IllegalArgumentException::class.java) { runBlocking { LocalPlaybackAccess.resolve(context, "content://unknown/tree/root", "id", "Synthetic") } }
        assertThrows(SecurityException::class.java) { runBlocking { LocalPlaybackAccess.resolve(context, "content://com.android.externalstorage.documents/tree/synthetic-root", "synthetic-child", "Synthetic") } }
    }

    @Test fun resumePositionsAreScopedToFolderAndDocument() = runBlocking {
        val target = LocalPlaybackTarget("synthetic-A", "document", "content://synthetic/video", "Synthetic")
        ResumePositions.save(context, target, 1234)
        assertEquals(1234L, ResumePositions.load(context, target.tree, target.documentId))
        assertEquals(0L, ResumePositions.load(context, "synthetic-B", target.documentId))
        assertEquals(0L, ResumePositions.load(context, target.tree, "different"))
    }

    @Test fun forgettingLibraryClearsAppPrivateResumeCheckpoints() = runBlocking {
        val first = LocalPlaybackTarget("synthetic-A", "document-A", "content://synthetic/video-a", "Synthetic")
        val second = LocalPlaybackTarget("synthetic-old-tree", "document-B", "content://synthetic/video-b", "Synthetic")
        ResumePositions.save(context, first, 1234)
        ResumePositions.save(context, second, 5678)
        ResumePositions.clearAll(context)
        assertEquals(0L, ResumePositions.load(context, first.tree, first.documentId))
        assertEquals(0L, ResumePositions.load(context, second.tree, second.documentId))
    }
}
