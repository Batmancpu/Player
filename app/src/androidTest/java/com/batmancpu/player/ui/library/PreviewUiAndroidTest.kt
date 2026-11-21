package com.batmancpu.player.ui.library

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.test.platform.app.InstrumentationRegistry
import com.batmancpu.player.MainActivity
import com.batmancpu.player.ui.glass.GlassLabScreen
import com.batmancpu.player.ui.metadata.MetadataLookupUiState
import com.batmancpu.player.ui.metadata.TmdbCandidate
import com.batmancpu.player.ui.library.TmdbMediaType
import com.batmancpu.player.ui.theme.PlayerTheme
import android.os.ParcelFileDescriptor
import java.util.concurrent.atomic.AtomicInteger
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

    private fun awaitCondition(timeoutMs: Long, predicate: () -> Boolean): Boolean {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            if (predicate()) return true
            SystemClock.sleep(50)
        }
        return predicate()
    }

    private fun awaitNode(predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            find(predicate)?.let { return it }
            SystemClock.sleep(100)
        }
        error("Synthetic interface node did not appear; active window: ${activeWindowSummary()}")
    }

    /**
     * Include a short synthetic accessibility snapshot on failure so a missing label can be
     * distinguished from a window/lifecycle problem in CI without uploading user media.
     */
    private fun activeWindowSummary(): String {
        val root = automation.rootInActiveWindow ?: return "none"
        val entries = mutableListOf<String>()
        fun visit(node: AccessibilityNodeInfo, depth: Int) {
            if (entries.size >= 60 || depth > 14) return
            val label = node.text?.toString()?.take(80)
            val description = node.contentDescription?.toString()?.take(80)
            if (!label.isNullOrBlank() || !description.isNullOrBlank()) {
                entries += "${node.className}[text=$label, description=$description]"
            }
            for (index in 0 until node.childCount) {
                val child = node.getChild(index) ?: continue
                visit(child, depth + 1)
                if (entries.size >= 60) break
            }
        }
        visit(root, 0)
        return "package=${root.packageName}, class=${root.className}, labels=${entries.joinToString(limit = 60)}"
    }

    private fun text(value: String) = awaitNode { it.text?.toString() == value }

    private fun hasSelectedSemanticsOnLabelOrParent(value: String): Boolean {
        var node: AccessibilityNodeInfo = find { it.text?.toString() == value } ?: return false
        repeat(8) {
            if (node.isSelected) return true
            node = node.parent ?: return false
        }
        return false
    }

    private fun click(node: AccessibilityNodeInfo) {
        var target: AccessibilityNodeInfo? = node
        while (target != null && !target.isClickable) target = target.parent
        assertTrue("No clickable ancestor", target?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true)
    }

    private fun swipeUp() {
        val metrics = context.resources.displayMetrics
        val centerX = metrics.widthPixels / 2
        val fromY = metrics.heightPixels * 7 / 10
        val toY = metrics.heightPixels * 6 / 10
        ParcelFileDescriptor.AutoCloseInputStream(
            automation.executeShellCommand("input swipe $centerX $fromY $centerX $toY 300"),
        ).use { it.readBytes() }
    }

    private fun revealBySwiping(value: String): AccessibilityNodeInfo {
        repeat(8) {
            find { it.text?.toString() == value }?.let { return it }
            swipeUp()
        }
        return awaitNode { it.text?.toString() == value }
    }

    private fun screenshot(name: String) {
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
            assertTrue(
                "Nonmatching title must disappear after the asynchronous edit",
                awaitCondition(10_000) { find { it.text?.toString() == "Blue Harbour" } == null },
            )
            assertNull(find { it.text?.toString() == "Blue Harbour" })
            screenshot("search")
            click(text("Aurora"))
            text("Aurora.2022.mp4")
            screenshot("details")
            click(text("Play video"))
            assertTrue("Play action reaches the selected indexed file", awaitCondition(3_000) { played.get()?.documentId == "movie-A" })
            assertEquals("movie-A", played.get()?.documentId)
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test(timeout = 25_000) fun removeFolderRequiresClearConfirmationAndDoesNotCallDeleteWhenCancelled() {
        val activity = launch()
        val files = listOf(DiscoveredLocalMedia("movie-A", "Aurora.2022.mp4", LocalMediaKind.VIDEO))
        val forgotten = AtomicInteger(0)
        try {
            instrumentation.runOnMainSync {
                activity.setContent {
                    PlayerTheme {
                        LocalLibraryContent(
                            LocalFolder("synthetic-tree", "Synthetic local folder"),
                            LibrarySessionState(tree = "synthetic-tree", media = files, hasCompletedScan = true),
                            CatalogueSnapshot("synthetic-tree", LocalCatalogue.build(files)),
                            onScan = {}, onCancel = {}, scanStillRunning = { false }, onChooseFolder = {},
                            onPlay = {}, onOpenGlassLab = {}, onForgetFolder = { forgotten.incrementAndGet() },
                        )
                    }
                }
            }
            click(text("Manage"))
            click(text("Remove folder from Player"))
            text("Remove this library?")
            text("Player will remove its saved library index, playback resume points and folder access. Your original videos and subtitles stay in the folder and will not be deleted.")
            click(text("Keep library"))
            assertEquals("Cancel must leave the library connected", 0, forgotten.get())
            click(text("Remove folder from Player"))
            click(text("Remove from Player"))
            assertEquals("Only explicit confirmation starts removal", 1, forgotten.get())
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test(timeout = 30_000) fun titleMetadataSearchIsManualAndMatchRequiresASecondConfirmation() {
        val activity = launch()
        val file = DiscoveredLocalMedia("movie-A", "Aurora.2022.mp4", LocalMediaKind.VIDEO)
        val lookup = mutableStateOf(
            MetadataLookupUiState(
                tree = "synthetic-tree",
                titleKey = "file:movie-A",
                settingsEnabled = true,
                credentialConfigured = true,
                searchable = true,
            ),
        )
        val searches = AtomicInteger()
        val confirmations = AtomicInteger()
        try {
            instrumentation.runOnMainSync {
                activity.setContent {
                    PlayerTheme {
                        LocalLibraryContent(
                            LocalFolder("synthetic-tree", "Synthetic local folder"),
                            LibrarySessionState(tree = "synthetic-tree", media = listOf(file), hasCompletedScan = true),
                            CatalogueSnapshot("synthetic-tree", LocalCatalogue.build(listOf(file))),
                            onScan = {}, onCancel = {}, scanStillRunning = { false }, onChooseFolder = {},
                            onPlay = {}, onOpenGlassLab = {}, metadataState = lookup.value,
                            onPrepareMetadata = {},
                            onSearchMetadata = {
                                searches.incrementAndGet()
                                lookup.value = lookup.value.copy(
                                    candidates = listOf(
                                        TmdbCandidate(42, TmdbMediaType.MOVIE, "Aurora", 2022, "Synthetic result description", "/poster.jpg", null),
                                    ),
                                )
                            },
                            onConfirmMetadata = { _, _ -> confirmations.incrementAndGet() },
                        )
                    }
                }
            }
            click(text("Aurora"))
            assertEquals("Opening details must not initiate an external lookup", 0, searches.get())
            click(revealBySwiping("Search TMDB"))
            assertEquals(1, searches.get())
            click(revealBySwiping("Synthetic result description"))
            assertEquals("Selecting a candidate is not confirmation", 0, confirmations.get())
            click(revealBySwiping("Confirm movie match"))
            assertEquals("The selected provider result needs a separate confirmation tap", 1, confirmations.get())
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test(timeout = 25_000) fun metadataSettingsOpenCreditsAndShowTheRequiredAttribution() {
        val activity = launch()
        val opened = AtomicInteger(0)
        try {
            instrumentation.runOnMainSync {
                activity.setContent {
                    PlayerTheme {
                        com.batmancpu.player.ui.metadata.MetadataSettingsContent(
                            state = com.batmancpu.player.ui.metadata.MetadataSettingsUiState(
                                settings = com.batmancpu.player.ui.metadata.MetadataSettingsState(),
                                loading = false,
                            ),
                            onSave = { _, _ -> },
                            onDisable = {},
                            onClear = {},
                            onBack = {},
                            onOpenCredits = { opened.incrementAndGet() },
                        )
                    }
                }
            }
            click(text("About and credits"))
            assertEquals(1, opened.get())
            instrumentation.runOnMainSync {
                activity.setContent { PlayerTheme { com.batmancpu.player.ui.metadata.MetadataCreditsScreen(onBack = {}) } }
            }
            text("Player")
            text("This product uses the TMDB API but is not endorsed or certified by TMDB.")
            assertNotNull(awaitNode { it.contentDescription?.toString() == "The Movie Database (TMDB) logo" })
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
            val displayHeight = context.resources.displayMetrics.heightPixels
            val density = context.resources.displayMetrics.density
            bounds.forEach { assertTrue(it.width() > 0 && it.left >= 0 && it.right <= displayWidth) }
            bounds.zipWithNext().forEach { (a, b) -> assertTrue(a.right <= b.left) }
            val lowestLabelGapDp = (displayHeight - bounds.maxOf { it.bottom }) / density
            assertTrue("Dock labels should sit near the bottom safe area, gap=$lowestLabelGapDp dp", lowestLabelGapDp in 0f..120f)
            screenshot("glass-on")
            click(text("Movies"))
            assertTrue(
                "Selected tab semantics did not update",
                awaitCondition(3_000) { hasSelectedSemanticsOnLabelOrParent("Movies") },
            )
            click(awaitNode { it.contentDescription?.toString() == "Glass effects" })
            labels.forEach { text(it) }
            screenshot("glass-off")
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }
}
