import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]


class GlassIntegrationTest(unittest.TestCase):
    def test_haze_path_is_removed_and_one_renderer_is_used(self):
        gradle = (ROOT / "app/build.gradle.kts").read_text()
        sources = "\n".join(p.read_text() for p in (ROOT / "app/src/main/java/com/mangoloads/player/ui/glass").rglob("*.kt"))
        self.assertNotIn("dev.chrisbanes.haze", gradle + sources)
        self.assertIn("drawBackdrop(", sources)
        self.assertIn("LocalAppBackdrop", sources)

    def test_music_reference_is_limited_to_glass_dock_and_licenses_are_visible(self):
        dock = (ROOT / "app/src/main/java/com/mangoloads/player/ui/glass/PlayerGlassDock.kt").read_text()
        notices = (ROOT / "THIRD_PARTY_NOTICES.md").read_text()
        self.assertIn("R.string.nav_movies", dock)
        self.assertIn("R.string.nav_shows", dock)
        self.assertNotIn("Song", dock)
        self.assertIn("654aecdb7b2777363db3585d1f59f3952aabb603", notices)
        self.assertIn("Keyboard-app reference is limited", notices)
        self.assertTrue((ROOT / "LICENSE").is_file())
        self.assertTrue((ROOT / "app/src/main/assets/licenses/GPL-3.0.txt").is_file())
        self.assertTrue((ROOT / "app/src/main/assets/licenses/MIT_LIQUID_GLASS_EASY.txt").is_file())

    def test_glass_keeps_effect_off_and_system_motion_paths(self):
        renderer = (ROOT / "app/src/main/java/com/mangoloads/player/ui/glass/PlayerLiquidGlass.kt").read_text()
        motion = (ROOT / "app/src/main/java/com/mangoloads/player/ui/glass/SystemMotionPreference.kt").read_text()
        self.assertIn("!LocalLiquidGlassEnabled.current", renderer)
        self.assertIn("LocalReduceMotion", renderer)
        self.assertIn("ANIMATOR_DURATION_SCALE", motion)
        self.assertIn("unregisterContentObserver", motion)

    def test_scroll_collapses_dock_and_instrumentation_covers_it(self):
        source = (ROOT / "app/src/main/java/com/mangoloads/player/ui/library/LocalLibraryScreen.kt").read_text()
        lab = (ROOT / "app/src/main/java/com/mangoloads/player/ui/glass/GlassLabScreen.kt").read_text()
        test = (ROOT / "app/src/androidTest/java/com/mangoloads/player/ui/library/PreviewUiAndroidTest.kt").read_text()
        self.assertIn("rememberFloatingTabBarScrollConnection", source)
        self.assertIn("scrollConnection = dockScroll", source)
        self.assertIn("glassEnabled && isGlassSupported()", source)
        self.assertIn("glassEnabled && isGlassSupported()", lab)
        self.assertIn("Scrolling down collapses the accessible dock tab", test)


if __name__ == "__main__":
    unittest.main()
