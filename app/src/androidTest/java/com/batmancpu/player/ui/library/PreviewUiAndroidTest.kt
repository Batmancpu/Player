package com.batmancpu.player.ui.library

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.test.platform.app.InstrumentationRegistry
import com.batmancpu.player.ui.glass.GlassLabScreen
import com.batmancpu.player.ui.metadata.MetadataLookupUiState
import com.batmancpu.player.ui.player.PlayerFullscreenButton
import com.batmancpu.player.ui.player.PlayerResizeButton
import com.batmancpu.player.ui.player.PlayerSpeedButton
import com.batmancpu.player.ui.player.nextPlayerResizeMode
import androidx.media3.ui.AspectRatioFrameLayout
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
        Intent(context, PreviewTestActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
    ) as PreviewTestActivity

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
                val bounds = Rect().also(node::getBoundsInScreen)
                entries += "${node.className}[text=$label, description=$description, bounds=$bounds, selected=${node.isSelected}, checkable=${node.isCheckable}, checked=${node.isChecked}, state=${node.stateDescription}, scrollable=${node.isScrollable}]"
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

    private fun isInsideVisibleAppBounds(node: AccessibilityNodeInfo): Boolean {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val safeBottom = context.resources.displayMetrics.heightPixels -
            (32 * context.resources.displayMetrics.density).toInt()
        return bounds.width() > 0 && bounds.height() > 0 && bounds.top >= 0 && bounds.bottom <= safeBottom
    }

    private fun text(value: String) = awaitNode {
        it.text?.toString() == value || it.contentDescription?.toString() == value
    }
    private fun titleCard(value: String) = awaitNode {
        it.contentDescription?.toString()?.startsWith("$value,") == true
    }
    private fun clickTitleCard(value: String) = click(titleCard(value))

    private fun hasSelectedSemanticsOnLabelOrParent(value: String): Boolean {
        var selected = false
        fun visit(node: AccessibilityNodeInfo) {
            if (node.text?.toString() == value || node.contentDescription?.toString() == value) {
                var ancestor: AccessibilityNodeInfo? = node
                repeat(8) {
                    if (ancestor?.isSelected == true || ancestor?.stateDescription?.toString() == "Selected") selected = true
                    ancestor = ancestor?.parent
                }
            }
            for (index in 0 until node.childCount) node.getChild(index)?.let(::visit)
        }
        automation.rootInActiveWindow?.let(::visit)
        return selected
    }

    private fun click(node: AccessibilityNodeInfo) {
        var target: AccessibilityNodeInfo? = node
        while (target != null && !target.isClickable) target = target.parent
        assertTrue("No clickable ancestor", target?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true)
    }


    private fun clickLastClickableLabel(label: String) {
        var target: AccessibilityNodeInfo? = null
        fun visit(node: AccessibilityNodeInfo) {
            if (node.text?.toString() == label || node.contentDescription?.toString() == label) {
                var clickable: AccessibilityNodeInfo? = node
                while (clickable != null && !clickable.isClickable) clickable = clickable.parent
                if (clickable != null) target = clickable
            }
            for (index in 0 until node.childCount) node.getChild(index)?.let(::visit)
        }
        automation.rootInActiveWindow?.let(::visit)
        assertTrue("No clickable navigation item for $label", target?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true)
    }

    private fun revealGlassSetting() {
        val settingsHeading = context.getString(com.batmancpu.player.R.string.catalogue_settings)
        awaitNode {
            it.text?.toString() == settingsHeading && isInsideVisibleAppBounds(it)
        }
        val visibleSwitch = {
            find {
                it.contentDescription?.toString()?.startsWith("Liquid Glass effects,") == true &&
                    isInsideVisibleAppBounds(it)
            }
        }
        repeat(12) { attempt ->
            if (visibleSwitch() != null) return
            val advanced = scrollForwardOnSmallestScrollable()
            if (!advanced || attempt % 3 == 2) {
                assertTrue(
                    "The settings sheet can scroll to its Glass control; ${activeWindowSummary()}",
                    swipeUpFromVisibleSheetText(),
                )
            }
            SystemClock.sleep(150)
        }
        assertNotNull("Glass setting is visibly revealed; ${activeWindowSummary()}", visibleSwitch())
    }

    private fun scrollForwardOnSmallestScrollable(): Boolean {
        val root = automation.rootInActiveWindow ?: return false
        val scrollableNodes = mutableListOf<Pair<Int, AccessibilityNodeInfo>>()
        fun visit(node: AccessibilityNodeInfo) {
            if (node.isScrollable && node.actionList.any {
                    it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id
                }) {
                val bounds = Rect()
                node.getBoundsInScreen(bounds)
                scrollableNodes += (bounds.width() * bounds.height()) to node
            }
            for (index in 0 until node.childCount) node.getChild(index)?.let(::visit)
        }
        visit(root)
        // Prefer the innermost list/sheet so test scrolling cannot move the background catalogue.
        return scrollableNodes.minByOrNull { it.first }?.second
            ?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) == true
    }

    private fun swipeUpFromVisibleSheetText(): Boolean {
        val root = automation.rootInActiveWindow ?: return false
        val metrics = context.resources.displayMetrics
        val safeTop = metrics.heightPixels / 3
        // Stay clear of Android's bottom gesture/navigation region: an upward swipe there can
        // leave the app and make a sheet-scroll test look like a product dismissal bug.
        val safeBottom = metrics.heightPixels - (64 * metrics.density).toInt()
        val candidates = mutableListOf<Pair<Rect, AccessibilityNodeInfo>>()
        fun visit(node: AccessibilityNodeInfo) {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            if ((!node.text.isNullOrBlank() || !node.contentDescription.isNullOrBlank()) &&
                bounds.width() > 0 && bounds.height() > 0 && bounds.bottom <= safeBottom
            ) candidates += bounds to node
            for (index in 0 until node.childCount) node.getChild(index)?.let(::visit)
        }
        visit(root)
        // Anchor the gesture to the actual modal sheet, then stay clear of both its drag
        // handle and Android's bottom Home gesture area.
        val handleBounds = Rect()
        find { it.contentDescription?.toString() == "Close sheet" }?.getBoundsInScreen(handleBounds)
        val sheetContentTop = maxOf(safeTop, handleBounds.bottom + (24 * metrics.density).toInt())
        val target = candidates
            .filter { it.first.centerY() in sheetContentTop..safeBottom }
            .maxByOrNull { it.first.bottom }
        if (target == null) {
            // LazyColumn may keep offscreen virtual descendants in the accessibility tree. If
            // none are currently safe to anchor on, swipe inside the known bottom-sheet body.
            if (handleBounds.isEmpty || handleBounds.bottom >= safeBottom) return false
            val startY = (safeBottom - (36 * metrics.density).toInt()).coerceAtLeast(sheetContentTop + 1)
            val endY = (startY - (260 * metrics.density).toInt()).coerceAtLeast(sheetContentTop + (48 * metrics.density).toInt())
            if (endY >= startY) return false
            val centerX = metrics.widthPixels / 2
            ParcelFileDescriptor.AutoCloseInputStream(
                automation.executeShellCommand("input swipe $centerX $startY $centerX $endY 300"),
            ).use { it.readBytes() }
            return true
        }
        val fromX = target.first.centerX()
        val fromY = target.first.centerY().coerceAtMost(safeBottom)
        val distance = (180 * metrics.density).toInt()
        val toY = (fromY - distance).coerceAtLeast(safeTop)
        if (toY >= fromY) return false
        ParcelFileDescriptor.AutoCloseInputStream(
            automation.executeShellCommand("input swipe $fromX $fromY $fromX $toY 300"),
        ).use { it.readBytes() }
        return true
    }

    private fun revealByScrolling(value: String): AccessibilityNodeInfo {
        repeat(12) {
            find { it.text?.toString() == value && isInsideVisibleAppBounds(it) }?.let { return it }
            if (!scrollForwardOnSmallestScrollable()) {
                assertTrue(
                    "No visible sheet content could reveal $value; ${activeWindowSummary()}",
                    swipeUpFromVisibleSheetText(),
                )
            }
            SystemClock.sleep(150)
        }
        return awaitNode { it.text?.toString() == value && isInsideVisibleAppBounds(it) }
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

    @Test(timeout = 20_000) fun playerFullscreenButtonHasReversibleAccessibleState() {
        val activity = launch()
        val fullScreen = mutableStateOf(false)
        try {
            instrumentation.runOnMainSync {
                activity.setContent {
                    PlayerTheme {
                        PlayerFullscreenButton(fullScreen.value) { fullScreen.value = !fullScreen.value }
                    }
                }
            }
            click(text("Full screen"))
            assertTrue("Toggle enters immersive mode", fullScreen.value)
            click(text("Exit full screen"))
            assertFalse("Toggle restores normal mode", fullScreen.value)
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test(timeout = 20_000) fun videoResizeModesCycleAccessibly() {
        val activity = launch()
        val mode = mutableStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT)
        try {
            instrumentation.runOnMainSync {
                activity.setContent {
                    PlayerTheme {
                        PlayerResizeButton(mode.value) { mode.value = nextPlayerResizeMode(mode.value) }
                    }
                }
            }
            click(text("Fit"))
            assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FILL, mode.value)
            click(text("Fill"))
            assertEquals(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, mode.value)
            click(text("Zoom"))
            assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FIT, mode.value)
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test(timeout = 30_000) fun playbackSpeedMenuOffersAccessibleCommonRates() {
        val activity = launch()
        val speed = mutableStateOf(1f)
        try {
            instrumentation.runOnMainSync {
                activity.setContent {
                    PlayerTheme { PlayerSpeedButton(speed.value) { speed.value = it } }
                }
            }
            click(text("1×"))
            assertNotNull("Speed options appear after opening the control", awaitNode { it.text?.toString() == "1.25×" })
            click(text("1.5×"))
            assertEquals(1.5f, speed.value)
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test(timeout = 40_000) fun productionGlassCanBeDisabledAndPreferenceSurvivesReopen() {
        val preferences = context.getSharedPreferences(
            com.batmancpu.player.ui.glass.PlayerUiSettingsStore.PREFERENCES_NAME,
            android.content.Context.MODE_PRIVATE,
        )
        preferences.edit().clear().commit()
        val files = listOf(DiscoveredLocalMedia("movie-A", "Aurora.2022.mp4", LocalMediaKind.VIDEO))
        fun install(activity: PreviewTestActivity) {
            instrumentation.runOnMainSync {
                activity.setContent {
                    PlayerTheme {
                        LocalLibraryContent(
                            LocalFolder("synthetic-tree", "Synthetic local folder"),
                            LibrarySessionState(tree = "synthetic-tree", media = files, hasCompletedScan = true),
                            CatalogueSnapshot("synthetic-tree", LocalCatalogue.build(files)),
                            onScan = {}, onCancel = {}, scanStillRunning = { false }, onChooseFolder = {},
                            onPlay = {}, onOpenGlassLab = {},
                        )
                    }
                }
            }
        }
        var activity = launch()
        try {
            install(activity)
            click(text("Manage"))
            revealGlassSetting()
            val announcedOn = context.getString(com.batmancpu.player.R.string.settings_glass_on)
            val announcedOff = context.getString(com.batmancpu.player.R.string.settings_glass_off)
            val glassSwitch = awaitNode {
                it.contentDescription?.toString()?.let { label ->
                    label.startsWith("Liquid Glass effects,") && label.endsWith(", $announcedOn")
                } == true
            }
            assertTrue("Glass starts enabled and announced; ${activeWindowSummary()}", glassSwitch.isCheckable && glassSwitch.isChecked)
            click(glassSwitch)
            assertTrue("Glass off preference is saved", awaitCondition(3_000) {
                !preferences.getBoolean(com.batmancpu.player.ui.glass.PlayerUiSettingsStore.GLASS_ENABLED_KEY, true)
            })
            assertTrue("Glass off state is announced", awaitCondition(3_000) {
                find {
                    it.contentDescription?.toString()?.let { label ->
                        label.startsWith("Liquid Glass effects,") && label.endsWith(", $announcedOff")
                    } == true
                }?.let { it.isCheckable && !it.isChecked } == true
            })
            instrumentation.runOnMainSync { activity.finish() }
            activity = launch()
            install(activity)
            click(text("Manage"))
            revealGlassSetting()
            val restoredSwitch = awaitNode {
                it.contentDescription?.toString()?.let { label ->
                    label.startsWith("Liquid Glass effects,") && label.endsWith(", $announcedOff")
                } == true
            }
            assertFalse("Glass off preference survives screen/activity recreation", preferences.getBoolean(
                com.batmancpu.player.ui.glass.PlayerUiSettingsStore.GLASS_ENABLED_KEY, true,
            ))
            assertTrue("Restored Glass switch exposes its off state; ${activeWindowSummary()}", restoredSwitch.isCheckable && !restoredSwitch.isChecked)
            assertTrue(restoredSwitch.isClickable)
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
            preferences.edit().clear().commit()
        }
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
            fun cardBounds(title: String): Rect {
                var target: AccessibilityNodeInfo? = titleCard(title)
                while (target != null && !target.isClickable) target = target.parent
                val card = checkNotNull(target) { "No clickable catalogue card for $title" }
                return Rect().also { card.getBoundsInScreen(it) }
            }
            val first = cardBounds("Aurora")
            val second = cardBounds("Blue Harbour")
            assertTrue("The cards are distinct side-by-side columns", first.right <= second.left)
            text("Shows")
            screenshot("catalogue")
            val input = awaitNode { it.className?.toString()?.contains("EditText") == true }
            assertTrue(input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "2022")
            }))
            titleCard("Aurora")
            assertTrue(
                "Nonmatching title must disappear after the asynchronous edit",
                awaitCondition(10_000) { find { it.contentDescription?.toString()?.startsWith("Blue Harbour,") == true } == null },
            )
            assertNull(find { it.contentDescription?.toString()?.startsWith("Blue Harbour,") == true })
            screenshot("search")
            clickTitleCard("Aurora")
            text("Aurora.2022.mp4")
            val detailArtwork = awaitNode { it.contentDescription?.toString() == "Title artwork for Aurora" }
            val detailArtworkBounds = Rect()
            detailArtwork.getBoundsInScreen(detailArtworkBounds)
            assertTrue("Title details use a cinematic landscape hero", detailArtworkBounds.width() > detailArtworkBounds.height())
            screenshot("details")
            click(text("Close"))
            assertTrue("Close returns to the library without removing its item", awaitCondition(2_000) {
                find { it.text?.toString() == "Play video" } == null
            })
            clickTitleCard("Aurora")
            text("Aurora.2022.mp4")
            click(text("Play video"))
            assertTrue("Play action reaches the selected indexed file", awaitCondition(3_000) { played.get()?.documentId == "movie-A" })
            assertEquals("movie-A", played.get()?.documentId)
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test(timeout = 35_000) fun seriesDetailsGroupSeasonsAndPlaySelectedIndexedEpisode() {
        val activity = launch()
        val files = listOf(
            DiscoveredLocalMedia("episode-s2e1", "North.Star.S02E01.mkv", LocalMediaKind.VIDEO),
            DiscoveredLocalMedia("episode-s1e2", "North.Star.S01E02.mkv", LocalMediaKind.VIDEO),
            DiscoveredLocalMedia("episode-s1e1", "North.Star.S01E01.mkv", LocalMediaKind.VIDEO),
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
            clickTitleCard("North Star")
            text("Season 1")
            text("S01 · E01")
            text("S01 · E02")
            revealByScrolling("Season 2")
            click(revealByScrolling("S02 · E01"))
            assertTrue(
                "Episode button must play its indexed document",
                awaitCondition(3_000) { played.get()?.documentId == "episode-s2e1" },
            )
            assertEquals("episode-s2e1", played.get()?.documentId)
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
            clickTitleCard("Aurora")
            // Capture also provides a finite transition settle before inspecting sheet actions.
            screenshot("metadata-details")
            assertEquals("Opening details must not initiate an external lookup", 0, searches.get())
            click(revealByScrolling("Search TMDB"))
            assertEquals(1, searches.get())
            click(revealByScrolling("Synthetic result description"))
            assertEquals("Selecting a candidate is not confirmation", 0, confirmations.get())
            val confirmMatch = text("Confirm movie match")
            assertTrue("Confirmation is inside the visible detail sheet", isInsideVisibleAppBounds(confirmMatch))
            click(confirmMatch)
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
        val backCount = AtomicInteger(0)
        try {
            instrumentation.runOnMainSync {
                activity.setContent { PlayerTheme { GlassLabScreen(onBack = { backCount.incrementAndGet() }) } }
            }
            val labels = listOf("Home", "Movies", "Shows", "Search")
            val bounds = labels.map { label -> Rect().also { awaitNode { it.contentDescription?.toString() == label }.getBoundsInScreen(it) } }
            val displayWidth = context.resources.displayMetrics.widthPixels
            val displayHeight = context.resources.displayMetrics.heightPixels
            val density = context.resources.displayMetrics.density
            bounds.forEach { assertTrue(it.width() > 0 && it.left >= 0 && it.right <= displayWidth) }
            bounds.zipWithNext().forEach { (a, b) -> assertTrue(a.right <= b.left) }
            val lowestLabelGapDp = (displayHeight - bounds.maxOf { it.bottom }) / density
            assertTrue("Dock labels should sit near the bottom safe area, gap=$lowestLabelGapDp dp", lowestLabelGapDp in 0f..120f)
            screenshot("glass-on")
            clickLastClickableLabel("Movies")
            assertTrue(
                "Selected tab semantics did not update",
                awaitCondition(3_000) { hasSelectedSemanticsOnLabelOrParent("Movies") },
            )
            click(awaitNode { it.contentDescription?.toString() == "Glass effects" })
            labels.forEach { label -> awaitNode { it.contentDescription?.toString() == label } }
            screenshot("glass-off")
            text("Back to library")
            click(text("Back to library"))
            assertEquals("Preview provides an explicit route back to the library", 1, backCount.get())
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test(timeout = 35_000) fun productionNavigationOpensHomeMoviesShowsAndSearch() {
        val activity = launch()
        val files = listOf(
            DiscoveredLocalMedia("movie-A", "Aurora.2022.mp4", LocalMediaKind.VIDEO),
            DiscoveredLocalMedia("episode-A", "North.Star.S01E01.mkv", LocalMediaKind.VIDEO),
        )
        try {
            instrumentation.runOnMainSync {
                activity.setContent {
                    PlayerTheme {
                        LocalLibraryContent(
                            LocalFolder("synthetic-tree", "Synthetic local folder"),
                            LibrarySessionState(tree = "synthetic-tree", media = files, hasCompletedScan = true),
                            CatalogueSnapshot("synthetic-tree", LocalCatalogue.build(files)),
                            onScan = {}, onCancel = {}, scanStillRunning = { false }, onChooseFolder = {},
                            onPlay = {}, onOpenGlassLab = {},
                        )
                    }
                }
            }
            assertTrue(awaitCondition(3_000) { find { it.contentDescription?.toString()?.startsWith("Aurora,") == true } != null })
            val titleNode = titleCard("Aurora")
            var cardNode: AccessibilityNodeInfo? = titleNode
            while (cardNode != null && !cardNode.isClickable) cardNode = cardNode.parent
            val artworkBounds = Rect()
            checkNotNull(cardNode) { "The title must remain accessible on its clickable card" }.getBoundsInScreen(artworkBounds)
            assertTrue("Offline poster artwork uses a portrait composition", artworkBounds.height() >= artworkBounds.width() * 1.4f)
            assertTrue("Home is selected initially", hasSelectedSemanticsOnLabelOrParent("Home"))

            clickLastClickableLabel("Movies")
            assertTrue("Movies selection filters the catalogue", awaitCondition(3_000) {
                find { it.contentDescription?.toString()?.startsWith("Aurora,") == true } != null &&
                    find { it.contentDescription?.toString()?.startsWith("North Star,") == true } == null
            })
            assertTrue("Movies tab exposes selected semantics; ${activeWindowSummary()}", awaitCondition(3_000) {
                hasSelectedSemanticsOnLabelOrParent("Movies")
            })

            clickLastClickableLabel("Shows")
            assertTrue("Shows selection filters the catalogue", awaitCondition(3_000) {
                find { it.contentDescription?.toString()?.startsWith("North Star,") == true } != null &&
                    find { it.contentDescription?.toString()?.startsWith("Aurora,") == true } == null
            })
            assertTrue("Shows tab exposes selected semantics", awaitCondition(3_000) {
                hasSelectedSemanticsOnLabelOrParent("Shows")
            })

            clickLastClickableLabel("Search")
            assertTrue("Search destination focuses the accessible title field", awaitCondition(3_000) {
                find { it.className?.toString()?.contains("EditText") == true }?.isFocused == true
            })
            assertTrue("Search tab exposes selected semantics", hasSelectedSemanticsOnLabelOrParent("Search"))
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }
}
