"""Regression tests for Player's explicit Android backup exclusions."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
EXPECTED_DOMAINS = {
    "root", "file", "database", "sharedpref", "external",
    "device_root", "device_file", "device_database", "device_sharedpref",
}


class BackupRulesTest(unittest.TestCase):
    def assert_all_data_excluded(self, section):
        excludes = section.findall("exclude")
        self.assertEqual(
            {(item.attrib.get("domain"), item.attrib.get("path")) for item in excludes},
            {(domain, ".") for domain in EXPECTED_DOMAINS},
        )
        self.assertEqual(len(excludes), len(EXPECTED_DOMAINS))
        self.assertEqual([], section.findall("include"))

    def test_manifest_keeps_backup_disabled_and_references_both_rule_sets(self):
        manifest = ET.parse(ROOT / "app/src/main/AndroidManifest.xml").getroot()
        application = manifest.find("application")
        android = "{http://schemas.android.com/apk/res/android}"
        self.assertEqual(application.attrib.get(android + "allowBackup"), "false")
        self.assertEqual(application.attrib.get(android + "fullBackupContent"), "@xml/backup_rules")
        self.assertEqual(application.attrib.get(android + "dataExtractionRules"), "@xml/data_extraction_rules")

    def test_legacy_android_11_and_lower_excludes_every_backup_domain(self):
        rules = ET.parse(ROOT / "app/src/main/res/xml/backup_rules.xml").getroot()
        self.assertEqual(rules.tag, "full-backup-content")
        self.assert_all_data_excluded(rules)

    def test_android_12_plus_excludes_cloud_and_device_transfer(self):
        rules = ET.parse(ROOT / "app/src/main/res/xml/data_extraction_rules.xml").getroot()
        self.assertEqual(rules.tag, "data-extraction-rules")
        sections = {child.tag: child for child in rules}
        self.assertEqual(set(sections), {"cloud-backup", "device-transfer"})
        for section in sections.values():
            with self.subTest(section=section.tag):
                self.assert_all_data_excluded(section)


if __name__ == "__main__":
    unittest.main()
