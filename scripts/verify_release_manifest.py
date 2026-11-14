"""Verify the built APK boundary; no private media or signing material is read."""
import os
import re
import subprocess
import sys

apk, aapt = sys.argv[1:]

def dump(*args):
    return subprocess.run([aapt, "dump", *args, apk], check=True, capture_output=True, text=True).stdout

badging = dump("badging")
package = re.search(r"package: name='([^']+)' versionCode='(\d+)'", badging)
assert package and package[1] == "com.batmancpu.player", "Application identity changed"
assert int(package[2]) == int(os.environ["GITHUB_RUN_NUMBER"]), "Unexpected versionCode"
permissions = dump("permissions")
allowed = {"com.batmancpu.player.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"}
for line in permissions.splitlines():
    if line.startswith("uses-permission"):
        permission = re.search(r"name='([^']+)'", line)
        assert permission and permission[1] in allowed, "Unexpected production permission"
xml = dump("xmltree", "--file", "AndroidManifest.xml")
backup = re.search(r"(?:android:)?allowBackup(?:\(0x[0-9a-fA-F]+\))?\s*=\s*([^\n]+)", xml)
assert backup and re.fullmatch(
    r"(?:\(type 0x12\))?(?:0x0+|false)(?:\s+\(Raw: \"false\"\))?", backup[1].strip()
), "Backup must remain disabled; observed " + (backup[1].strip() if backup else "missing attribute")
print("Verified allowBackup representation:", backup[1].strip())
assert "SyntheticSafProvider" not in xml and "com.batmancpu.player.synthetic" not in xml, "Debug provider entered release"
print("Release manifest verified: stable identity/version, offline permissions, backup disabled, no debug provider.")
