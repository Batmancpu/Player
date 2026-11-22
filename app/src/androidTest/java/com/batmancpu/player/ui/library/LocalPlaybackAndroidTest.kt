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
import androidx.media3.common.TrackSelectionOverride
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

    private fun awaitNode(predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            fun visit(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
                if (predicate(node)) return node
                for (index in 0 until node.childCount) {
                    val child = node.getChild(index) ?: continue
                    visit(child)?.let { return it }
                }
                return null
            }
            automation.rootInActiveWindow?.let(::visit)?.let { return it }
            SystemClock.sleep(100)
        }
        error("Expected accessibility node was not visible")
    }

    private fun awaitText(value: String, expected: Boolean, timeoutMs: Long = 8_000): Boolean {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            if (containsText(value) == expected) return true
            SystemClock.sleep(50)
        }
        return containsText(value) == expected
    }

    private fun captureSyntheticScreenshot(name: String) {
        SystemClock.sleep(350)
        fun shell(command: String) {
            ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use { it.readBytes() }
        }
        shell("mkdir -p /sdcard/Download/player-preview-screens")
        shell("screencap -p /sdcard/Download/player-preview-screens/$name.png")
    }

    @Test(timeout = 30_000) fun contentOnlyEngineRendersOwnedH264Fixture() {
        val fixture = File(context.cacheDir, "synthetic-video.mp4")
        instrumentation.context.assets.open("synthetic-video.mp4").use { input -> fixture.outputStream().use { output -> input.copyTo(output) } }
        SyntheticSafProvider.reset()
        SyntheticSafProvider.playbackFile = fixture
        SyntheticSafProvider.subtitleContents = "1\n00:00:00,000 --> 00:00:02,000\nSynthetic local subtitle\n".toByteArray()
        val frame = CountDownLatch(1)
        val subtitleCue = CountDownLatch(1)
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
                    override fun onCues(cueGroup: androidx.media3.common.text.CueGroup) {
                        if (cueGroup.cues.any { it.text?.contains("Synthetic local subtitle") == true }) subtitleCue.countDown()
                    }
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
                val subtitleGroup = player.currentTracks.groups.firstOrNull {
                    it.type == C.TRACK_TYPE_TEXT && it.isSupported
                }
                assertNotNull("Synthetic SRT sidecar is exposed as a supported track", subtitleGroup)
                player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                    .setOverrideForType(TrackSelectionOverride(checkNotNull(subtitleGroup).mediaTrackGroup, listOf(0)))
                    .build()
                player.seekTo(0L)
                player.play()
            }
            assertTrue("Local sidecar cue reaches Media3's subtitle renderer", subtitleCue.await(5, TimeUnit.SECONDS))
            instrumentation.runOnMainSync {
                val player = reference.get()
                assertTrue(player.duration > 0)
                assertTrue("Synthetic MP4 must expose embedded audio", hasSupportedTrackType(player.currentTracks, C.TRACK_TYPE_AUDIO))
                assertTrue("Synthetic local SRT sidecar was not decoded", hasSupportedTrackType(player.currentTracks, C.TRACK_TYPE_TEXT))
                player.pause()
                player.seekTo(1000)
                assertEquals(1000L, player.currentPosition)
            }

            val player = checkNotNull(reference.get())
            val trackSnapshot = AtomicReference(mutableStateOf(androidx.media3.common.Tracks.EMPTY))
            val selectedTrackType = mutableStateOf(C.TRACK_TYPE_TEXT)
            val trackListener = object : Player.Listener {
                override fun onTracksChanged(tracks: androidx.media3.common.Tracks) { trackSnapshot.get().value = tracks }
            }
            instrumentation.runOnMainSync {
                trackSnapshot.set(mutableStateOf(player.currentTracks))
                player.addListener(trackListener)
                (activity as PreviewTestActivity).setContent {
                    PlayerTheme {
                        PlayerTrackSelectionSheet(
                            player = player,
                            tracks = trackSnapshot.get().value,
                            trackType = selectedTrackType.value,
                            onDismiss = {},
                            onSelectionApplied = { trackSnapshot.get().value = player.currentTracks },
                        )
                    }
                }
            }
            assertTrue("Custom subtitle picker opens", awaitText("Subtitles", true))
            val offOption = awaitNode { it.contentDescription?.toString() == context.getString(com.batmancpu.player.R.string.player_subtitles_off) }
            assertTrue("Subtitles-off row is accessible", offOption.isClickable)
            assertTrue("Subtitles-off row activates", offOption.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            assertTrue(
                "Subtitles-off action updates Media3 track parameters",
                C.TRACK_TYPE_TEXT in player.trackSelectionParameters.disabledTrackTypes,
            )
            assertTrue("Selecting subtitles off marks the accessible row selected", awaitNode {
                it.contentDescription?.toString() == context.getString(com.batmancpu.player.R.string.player_subtitles_off) && it.isSelected
            }.isSelected)
            val automaticOption = awaitNode { it.contentDescription?.toString() == context.getString(com.batmancpu.player.R.string.player_tracks_automatic) }
            assertTrue("Automatic track selection is accessible", automaticOption.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            assertTrue("Automatic selection restores the default track policy", awaitNode {
                it.contentDescription?.toString() == context.getString(com.batmancpu.player.R.string.player_tracks_automatic) && it.isSelected
            }.isSelected)
            val sidecarOption = awaitNode { it.contentDescription?.toString()?.startsWith("Synthetic.en.srt") == true }
            assertTrue("Selecting the local sidecar is accessible", sidecarOption.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            assertTrue("Selecting a supported sidecar marks its row selected", awaitNode {
                it.contentDescription?.toString()?.startsWith("Synthetic.en.srt") == true && it.isSelected
            }.isSelected)
            instrumentation.runOnMainSync { selectedTrackType.value = C.TRACK_TYPE_AUDIO }
            assertTrue("Audio picker replaces the subtitle picker", awaitText("Audio", true))
            val audioOption = awaitNode {
                it.isCheckable && it.contentDescription?.toString() != context.getString(com.batmancpu.player.R.string.player_tracks_automatic)
            }
            assertTrue("Supported audio track can be selected", audioOption.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            assertTrue("Audio selection exposes its selected state", awaitNode {
                it.isCheckable && it.contentDescription?.toString() != context.getString(com.batmancpu.player.R.string.player_tracks_automatic) && it.isSelected
            }.isSelected)
            instrumentation.runOnMainSync { player.removeListener(trackListener) }
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
            assertTrue("Compose transport should expose play/pause", awaitText("Pause", true))
            assertTrue("Compose transport should expose a seek slider", awaitText("Playback position", true))
            assertTrue("Compose transport should expose a forward seek", awaitText("Seek forward 10 seconds", true))
            assertTrue("Synthetic video should begin before the auto-hide check", playbackStarted.await(15, TimeUnit.SECONDS))
            captureSyntheticScreenshot("player-controls")
            assertTrue("Header should auto-hide during active playback", awaitText("Synthetic feature", false, 8_000))
            val videoSurface = awaitNode {
                it.contentDescription?.toString() == context.getString(com.batmancpu.player.R.string.player_video_surface)
            }
            assertTrue("Video surface exposes an accessible tap action", videoSurface.isClickable)
            assertTrue("Activating the video surface restores player controls", videoSurface.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            assertTrue("Touching the video restores the header", awaitText("Synthetic feature", true, 4_000))
            assertTrue("Full-screen action is reachable", awaitText("Full screen", true, 2_000))
            fun findLabel(current: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
                if (current == null) return null
                if (current.text?.toString() == "Full screen" || current.contentDescription?.toString() == "Full screen") return current
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

    @Test fun playbackTimeFormattingHandlesLongMediaAndNegativePositions() {
        assertEquals("0:00", formatPlaybackTime(-10L))
        assertEquals("1:05", formatPlaybackTime(65_999L))
        assertEquals("1:02:03", formatPlaybackTime(3_723_000L))
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
                                positionMs = 29_000L,
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
