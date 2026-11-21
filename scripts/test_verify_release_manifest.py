import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


class ReleaseManifestTest(unittest.TestCase):
    def check_apk(self, backup, permission=None, provider=False, version=5, backup_rules=True):
        xmltree = "A: android:allowBackup(0x01010280)=" + backup
        if backup_rules:
            xmltree += "\nA: android:fullBackupContent(0x0101027c)=@0x7f120001\nA: android:dataExtractionRules(0x010106f4)=@0x7f120002"
        if provider:
            xmltree += "\nSyntheticSafProvider"
        responses = {
            "badging": f"package: name='com.batmancpu.player' versionCode='{version}'",
            "permissions": "" if permission is None else f"uses-permission: name='{permission}'",
            "xmltree": xmltree,
        }
        with tempfile.TemporaryDirectory() as directory:
            aapt = Path(directory) / "fixture-aapt"
            aapt.write_text("#!/usr/bin/env python3\nimport json,sys\nresponses=json.loads(" + repr(json.dumps(responses)) + ")\nprint(responses[sys.argv[2]])\n")
            aapt.chmod(0o700)
            return subprocess.run(
                [sys.executable, str(Path(__file__).with_name("verify_release_manifest.py")), "synthetic.apk", str(aapt)],
                env={**os.environ, "GITHUB_RUN_NUMBER": "5"}, text=True, capture_output=True,
            )

    def test_accepts_false_boolean_dump_formats(self):
        for value in ["false", "0x00000000", "(type 0x12)0x0", '(type 0x12)0x00000000 (Raw: "false")']:
            with self.subTest(value=value):
                result = self.check_apk(value)
                self.assertEqual(0, result.returncode, result.stderr)

    def test_rejects_true_or_unknown_backup_values(self):
        for value in ["true", "0xffffffff", "(type 0x12)0x01", "@bool/unknown", ""]:
            with self.subTest(value=value):
                self.assertNotEqual(0, self.check_apk(value).returncode)

    def test_rejects_missing_explicit_backup_rule_resources(self):
        self.assertNotEqual(0, self.check_apk("false", backup_rules=False).returncode)

    def test_allows_only_consent_gated_internet_and_rejects_other_permissions(self):
        self.assertEqual(0, self.check_apk("false", permission="android.permission.INTERNET").returncode)
        self.assertNotEqual(0, self.check_apk("false", permission="android.permission.ACCESS_NETWORK_STATE").returncode)
        self.assertNotEqual(0, self.check_apk("false", provider=True).returncode)
        self.assertNotEqual(0, self.check_apk("false", version=4).returncode)
        self.assertEqual(0, self.check_apk("false", permission="com.batmancpu.player.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION").returncode)


if __name__ == "__main__":
    unittest.main()
