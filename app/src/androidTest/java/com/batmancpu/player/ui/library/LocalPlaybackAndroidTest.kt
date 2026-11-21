package com.batmancpu.player.ui.library

import android.content.Intent
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.Player
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.SubtitleView
import androidx.test.platform.app.InstrumentationRegistry
import com.batmancpu.player.ui.player.*
import com.batmancpu.player.ui.theme.PlayerTheme
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
    private val automation get() = instrumentation.uiAutomation

    private fun containsText(value: String): Boolean {
        fun visit(node: AccessibilityNodeInfo): Boolean {
            if (node.text?.toString() == value || node.contentDescription?.toString() == value) return true
            for (index in 0 until node.childCount) {
                val child = node.getChild(index) ?: continue
                if (visit(child)) return true
            }
            return false
        }
        return automation.rootInActiveWindow?.let(::visit) == true
    }

    private fun awaitText(value: String, expected: Boolean, timeoutMs: Long = 8_000): Boolean {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            if (containsText(value) == expected) return true
            SystemClock.sleep(50)
        }
        return containsText(value) == expected
    }

    @Test(timeout = 30_000) fun contentOnlyEngineRendersOwnedH264Fixture() {
        val fixture = File(context.cacheDir, "synthetic-video.mp4")
        instrumentation.context.assets.open("synthetic-video.mp4").use { input -> fixture.outputStream().use { output -> input.copyTo(output) } }
        SyntheticSafProvider.reset()
        SyntheticSafProvider.playbackFile = fixture
        SyntheticSafProvider.subtitleContents = "1\n00:00:00,000 --> 00:00:02,000\nSynthetic local subtitle\n".toByteArray()
        val frame = CountDownLatch(1)
        val failure = AtomicReference<String?>()
        val reference = AtomicReference<ExoPlayer>()
        val activity = instrumentation.startActivitySync(Intent(context, PreviewTestActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
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

    @Test fun subtitleViewportIsIndependentFromVideoResizeFrame() {
        val reference = AtomicReference<PlayerView>()
        instrumentation.runOnMainSync { reference.set(PlayerView(context)) }
        val view = reference.get()
        val videoFrame = view.findViewById<AspectRatioFrameLayout>(androidx.media3.ui.R.id.exo_content_frame)
        val subtitles = view.findViewById<SubtitleView>(androidx.media3.ui.R.id.exo_subtitles)
        assertNotNull(videoFrame)
        assertNotNull(subtitles)
        assertSame("Video keeps its resize-managed content frame", view, videoFrame.parent)
        assertSame("Captions use the independent player viewport", view, subtitles.parent)
        assertNotSame("Caption layout is never nested in the zoomable video frame", videoFrame, subtitles.parent)
        view.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
        assertEquals(1f, subtitles.scaleX)
        assertEquals(1f, subtitles.scaleY)
    }

    @Test(timeout = 45_000) fun playerOverlayFollowsTransportControllerVisibility() {
        val fixture = File(context.cacheDir, "synthetic-player-overlay.mp4")
        instrumentation.context.assets.open("synthetic-video.mp4").use { input -> fixture.outputStream().use { output -> input.copyTo(output) } }
        SyntheticSafProvider.reset()
        SyntheticSafProvider.playbackFile = fixture
        val fullScreen = mutableStateOf(false)
        val playbackStarted = CountDownLatch(1)
        val activity = instrumentation.startActivitySync(Intent(context, PreviewTestActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)) as PreviewTestActivity
        try {
            instrumentation.runOnMainSync {
                activity.setContent {
                    PlayerTheme {
                        LocalPlayerScreen(
                            LocalPlaybackTarget(
                                tree = "synthetic-tree", documentId = "synthetic-video",
                                uri = "content://com.batmancpu.player.synthetic/video", label = "Synthetic feature",
                            ),
                            activity.lifecycle,
                            onCheckpoint = { _, _ -> },
                            onClose = {},
                            isFullScreen = fullScreen.value,
                            onToggleFullScreen = { fullScreen.value = !fullScreen.value },
                            onIsPlayingChanged = { if (it) playbackStarted.countDown() },
                        )
                    }
                }
            }
            assertTrue("Player header should appear with transport controls", awaitText("Synthetic feature", true))
            assertTrue("Synthetic video should begin before the auto-hide check", playbackStarted.await(15, TimeUnit.SECONDS))
            assertTrue("Header should auto-hide during active playback", awaitText("Synthetic feature", false, 8_000))
            val metrics = context.resources.displayMetrics
            ParcelFileDescriptor.AutoCloseInputStream(
                automation.executeShellCommand("input tap ${metrics.widthPixels / 2} ${metrics.heightPixels / 2}"),
            ).use { it.readBytes() }
            assertTrue("Touching the video restores the header", awaitText("Synthetic feature", true, 4_000))
            assertTrue("Full-screen action is reachable", awaitText("Full screen", true, 2_000))
            fun findLabel(current: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
                if (current == null) return null
                if (current.text?.toString() == "Full screen") return current
                for (index in 0 until current.childCount) findLabel(current.getChild(index))?.let { return it }
                return null
            }
            var button = findLabel(automation.rootInActiveWindow)
            while (button != null && !button.isClickable) button = button.parent
            assertTrue("Full-screen control can be activated", button?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true)
            assertTrue("Toggle enters full-screen state", fullScreen.value)
            assertTrue("Full-screen state changes after activating the player control", fullScreen.value)
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
            SyntheticSafProvider.playbackFile = null
            fixture.delete()
        }
    }

    @Test fun rejectsRemoteAndUnselectedProvidersBeforeOpeningDocuments() {
        assertThrows(IllegalArgumentException::class.java) { runBlocking { LocalPlaybackAccess.resolve(context, "https://invalid.example/video", "id", "Synthetic") } }
        assertThrows(IllegalArgumentException::class.java) { runBlocking { LocalPlaybackAccess.resolve(context, "content://unknown/tree/root", "id", "Synthetic") } }
        assertThrows(SecurityException::class.java) { runBlocking { LocalPlaybackAccess.resolve(context, "content://com.android.externalstorage.documents/tree/synthetic-root", "synthetic-child", "Synthetic") } }
    }

    @Test fun completedPlaybackCheckpointStaysAtBeginningAcrossLaterSaves() {
        assertEquals(0L to false, playbackResumeCheckpoint(Player.STATE_ENDED, 98_765L, true))
        assertEquals(4321L to true, playbackResumeCheckpoint(Player.STATE_READY, 4321L, true))
        assertEquals(0L to false, playbackResumeCheckpoint(Player.STATE_BUFFERING, -12L, false))
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

    @Test(timeout = 35_000) fun completedPlaybackResetsResumeCheckpointToBeginning() {
        val fixture = File(context.cacheDir, "synthetic-completion.mp4")
        instrumentation.context.assets.open("synthetic-video.mp4").use { input ->
            fixture.outputStream().use { output -> input.copyTo(output) }
        }
        SyntheticSafProvider.reset()
        SyntheticSafProvider.playbackFile = fixture
        val finished = CountDownLatch(1)
        val checkpoint = AtomicReference<Pair<Long, Boolean>?>()
        val activity = instrumentation.startActivitySync(
            Intent(context, PreviewTestActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
        ) as PreviewTestActivity
        try {
            instrumentation.runOnMainSync {
                activity.setContent {
                    PlayerTheme {
                        LocalPlayerScreen(
                            LocalPlaybackTarget(
                                tree = "synthetic-tree",
                                documentId = "synthetic-video",
                                uri = "content://com.batmancpu.player.synthetic/video",
                                label = "Synthetic completion",
                                playWhenReady = true,
                            ),
                            activity.lifecycle,
                            onCheckpoint = { position, playing ->
                                if (!playing && position == 0L) {
                                    checkpoint.set(position to playing)
                                    finished.countDown()
                                }
                            },
                            onClose = {},
                            isFullScreen = false,
                            onToggleFullScreen = {},
                        )
                    }
                }
            }
            assertTrue("Playback should reach its natural end", finished.await(20, TimeUnit.SECONDS))
            assertEquals(0L to false, checkpoint.get())
            // Let the five-second checkpoint timer run once after completion.
            SystemClock.sleep(5_500)
            assertEquals("Later lifecycle checkpoints must not restore the EOF position", 0L to false, checkpoint.get())
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
            SyntheticSafProvider.playbackFile = null
            fixture.delete()
        }
    }
}
