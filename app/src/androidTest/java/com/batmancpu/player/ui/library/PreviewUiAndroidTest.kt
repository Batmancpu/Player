package com.batmancpu.player.ui.library

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.test.platform.app.InstrumentationRegistry
import com.batmancpu.player.MainActivity
import com.batmancpu.player.ui.glass.GlassLabScreen
import com.batmancpu.player.ui.theme.PlayerTheme
import android.os.ParcelFileDescriptor
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.*
import org.junit.Test

/** All screenshots and interactions use owned synthetic names, never device media. */
class PreviewUiAndroidTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val automation get() = instrumentation.uiAutomation

    private fun launch() = instrumentation.startActivitySync(
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
    ) as MainActivity

    private fun find(predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        fun visit(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
            if (predicate(node)) return node
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                visit(child)?.let { return it }
            }
            return null
        }
        return automation.rootInActiveWindow?.let(::visit)
    }

    private fun awaitNode(predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            instrumentation.waitForIdleSync()
            find(predicate)?.let { return it }
            SystemClock.sleep(100)
        }
        error("Synthetic interface node did not appear")
    }

    private fun text(value: String) = awaitNode { it.text?.toString() == value }

    private fun click(node: AccessibilityNodeInfo) {
        var target: AccessibilityNodeInfo? = node
        while (target != null && !target.isClickable) target = target.parent
        assertTrue("No clickable ancestor", target?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true)
    }

    private fun screenshot(name: String) {
        instrumentation.waitForIdleSync()
        SystemClock.sleep(400) // Let the finite sheet/selection transition finish.
        // AGP uninstalls tested packages after connected tests and removes their external dirs.
        // Shell-owned, synthetic screenshots survive that cleanup without app storage permissions.
        fun shell(command: String) {
            ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use { it.readBytes() }
        }
        shell("mkdir -p /sdcard/Download/player-preview-screens")
        shell("screencap -p /sdcard/Download/player-preview-screens/$name.png")
    }

    @Test(timeout = 45_000) fun catalogueSearchDetailsAndPlayUseIndexedIdentity() {
        val activity = launch()
        val files = listOf(
            DiscoveredLocalMedia("movie-A", "Aurora.2022.mp4", LocalMediaKind.VIDEO),
            DiscoveredLocalMedia("movie-B", "Blue.Harbour.2020.mkv", LocalMediaKind.VIDEO),
            DiscoveredLocalMedia("episode", "North.Star.S01E01.mkv", LocalMediaKind.VIDEO),
        )
        val played = AtomicReference<DiscoveredLocalMedia?>()
        try {
            instrumentation.runOnMainSync {
                activity.setContent {
                    PlayerTheme {
                        LocalLibraryContent(
                            LocalFolder("synthetic-tree", "Synthetic local folder"),
                            LibrarySessionState(tree = "synthetic-tree", media = files, hasCompletedScan = true),
                            CatalogueSnapshot("synthetic-tree", LocalCatalogue.build(files)),
                            onScan = {}, onCancel = {}, scanStillRunning = { false }, onChooseFolder = {},
                            onPlay = { played.set(it) }, onOpenGlassLab = {},
                        )
                    }
                }
            }
            val first = Rect().also { text("Aurora").getBoundsInScreen(it) }
            val second = Rect().also { text("Blue Harbour").getBoundsInScreen(it) }
            assertEquals("Two visible columns on the narrow synthetic phone", first.top, second.top)
            assertTrue(first.right <= second.left)
            text("Shows")
            screenshot("catalogue")
            val input = awaitNode { it.className?.toString()?.contains("EditText") == true }
            assertTrue(input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "2022")
            }))
            text("Aurora")
            instrumentation.waitForIdleSync()
            val filteredBy = SystemClock.uptimeMillis() + 10_000
            while (find { it.text?.toString() == "Blue Harbour" } != null && SystemClock.uptimeMillis() < filteredBy) {
                instrumentation.waitForIdleSync()
                SystemClock.sleep(100)
            }
            assertNull("Nonmatching title must disappear after the asynchronous edit", find { it.text?.toString() == "Blue Harbour" })
            screenshot("search")
            click(text("Aurora"))
            text("Aurora.2022.mp4")
            screenshot("details")
            click(text("Play video"))
            instrumentation.waitForIdleSync()
            assertEquals("movie-A", played.get()?.documentId)
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test(timeout = 40_000) fun dockTabsFitAndRemainAccessibleWithEffectsDisabled() {
        val activity = launch()
        try {
            instrumentation.runOnMainSync { activity.setContent { PlayerTheme { GlassLabScreen() } } }
            val labels = listOf("Home", "Movies", "Shows", "Search")
            val bounds = labels.map { label -> Rect().also { text(label).getBoundsInScreen(it) } }
            val displayWidth = context.resources.displayMetrics.widthPixels
            bounds.forEach { assertTrue(it.width() > 0 && it.left >= 0 && it.right <= displayWidth) }
            bounds.zipWithNext().forEach { (a, b) -> assertTrue(a.right <= b.left) }
            screenshot("glass-on")
            click(text("Movies"))
            click(awaitNode { it.contentDescription?.toString() == "Glass effects" })
            labels.forEach { text(it) }
            screenshot("glass-off")
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }
}
