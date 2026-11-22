"""Regression checks for the intentionally separate brrPlayer package identity."""
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]

class AppIdentityTest(unittest.TestCase):
    def test_namespace_and_application_id_are_brrplayer(self):
        source = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
        self.assertRegex(source, r'namespace\s*=\s*"com\.mangoloads\.player"')
        self.assertRegex(source, r'applicationId\s*=\s*"com\.mangoloads\.player"')

    def test_manifest_uses_adaptive_brand_icon_and_no_backup(self):
        root = ET.parse(ROOT / "app/src/main/AndroidManifest.xml").getroot()
        app = root.find("application")
        self.assertIsNotNone(app)
        self.assertEqual("false", app.get("{http://schemas.android.com/apk/res/android}allowBackup"))
        self.assertEqual("@mipmap/ic_launcher", app.get("{http://schemas.android.com/apk/res/android}icon"))
        self.assertTrue((ROOT / "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml").is_file())
        self.assertTrue((ROOT / "app/src/main/res/drawable-nodpi/brrplayer_launcher_foreground.png").is_file())

    def test_android_12_startup_uses_brrplayer_brand_treatment(self):
        style = ET.parse(ROOT / "app/src/main/res/values-v31/styles.xml").getroot()
        theme = next(node for node in style.findall("style") if node.get("name") == "Theme.brrPlayer")
        items = {node.get("name").split(":")[-1]: node.text for node in theme.findall("item")}
        self.assertEqual("@drawable/brrplayer_splash_mark", items["windowSplashScreenAnimatedIcon"])
        self.assertTrue((ROOT / "app/src/main/res/drawable/brrplayer_splash_mark.xml").is_file())

    def test_display_name_and_room_schema_snapshots_are_migrated(self):
        strings = ET.parse(ROOT / "app/src/main/res/values/strings.xml").getroot()
        app_name = next(node for node in strings.findall("string") if node.get("name") == "app_name")
        self.assertEqual("brrPlayer", app_name.text)
        schema_dir = ROOT / "app/schemas/com.mangoloads.player.ui.library.PlayerLibraryDatabase"
        self.assertEqual({"1.json", "2.json", "3.json", "4.json"}, {p.name for p in schema_dir.glob("*.json")})

    def test_app_source_no_longer_declares_legacy_package(self):
        for root in (ROOT / "app/src/main", ROOT / "app/src/debug", ROOT / "app/src/test", ROOT / "app/src/androidTest"):
            for source in root.rglob("*.kt"):
                self.assertNotIn("com.batmancpu.player", source.read_text(encoding="utf-8"), str(source))

    def test_visible_app_copy_uses_brrplayer_brand(self):
        for path in (ROOT / "app/src/main/res/values/strings.xml", ROOT / "app/src/main/res/values/metadata_strings.xml"):
            root = ET.parse(path).getroot()
            for node in root.iter("string"):
                self.assertNotRegex(node.text or "", r"\bPlayer\b", f"stale product name in {node.get('name')}")

if __name__ == "__main__":
    unittest.main()
