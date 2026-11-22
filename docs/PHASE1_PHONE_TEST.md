Current offline-preview checklist: [OFFLINE_PREVIEW_TEST.md](OFFLINE_PREVIEW_TEST.md). The user's v0.1.79 legacy Player successes are recorded below; independent implementation continues without routine phone-test pauses.

# brrPlayer — OPPO phone checks

**Target:** OPPO Reno10 5G CPH2531, Android 15. Use a **signed brrPlayer APK from GitHub Releases**, never the CI-only debug artifact. The first published brrPlayer build is [v0.1.262](https://github.com/Batmancpu/Player/releases/tag/v0.1.262), package `com.mangoloads.player`; it is separate from legacy package `com.batmancpu.player`. Install it alongside the old app and select the media folder again. v0.1.262 is the identity/offline preview batch and predates the brrMusic-backed dock now in development; test the later release for that Glass change. Android does not transfer the legacy package's app-private database or persisted SAF grant. Preserve the signing certificate and higher versionCode for future brrPlayer updates. The user reports legacy v0.1.79 correctly recognized four video titles/years, retained the library after airplane-mode restart and switched folders. Newer catalogue/player/Glass/caption behavior still needs OPPO evidence. Phase acceptance and performance remain pending. No original-file deletion is required.

## Preparing a safe test folder
Use Android's file manager to create a small test folder of media you already own or are permitted to test, including:
- Two MP4/MKV movies with title/year filenames, e.g. "Example.Movie.2020.mkv".
- One TV subfolder with "Example.Show.S01E01.mp4", "Example.Show.S01E02.mp4".
- An SRT subtitle file and a non-media text file.
- A separate unrelated video **outside** the selected folder (to verify isolation).
Never share private media paths or file contents in GitHub issues or logs.

## Install brrPlayer
1. Verify the release tag, APK SHA-256, versionCode, package `com.mangoloads.player`, signing certificate and passing release checks.
2. Download the one signed brrPlayer APK from the release. Install it without uninstalling legacy Player; Android will show them as separate apps.
3. Add the GitHub Releases source in Obtainium. Later brrPlayer releases should update in place while package, certificate and versionCode progression remain valid.
4. Open brrPlayer, choose the local folder using Android's system folder picker and scan. Do not grant all-files access or another provider.

## Core offline-library acceptance
- Tap **Scan this folder**; confirm nested video names are shown and the unrelated outside file is absent. Subtitle count should include SRT; non-media text files should be ignored.
- Close brrPlayer completely and reopen it, including with airplane mode on. The scanned library should remain without a rescan.
- Search, open a title, tap **Play video**, pause/seek, return and reopen to check resume. If **Caption appearance** is available, adjust and reset text size/position; verify captions stay independent from video zoom.
- Check Rescan and cancellation if useful; the last complete library must remain intact. No files need to be added or deleted.
- Use **Manage → Remove folder from brrPlayer** only if you want to test the explicit library-removal option. Confirm original videos/subtitles remain unchanged. Select and scan the folder again.
- Change to another local folder. Confirm results do not mix. Revoke the selected folder permission through Android settings if available; brrPlayer should request reconnection instead of crashing or showing ungranted files.
- During a scan, rotate the phone if supported; return from Glass and verify the same scan/library state. Switch A→B→A and confirm stale scans cannot replace newer results.
- If convenient, test 100+ mixed files and report filename parsing, scrolling, restart behavior and any performance/heat symptoms. No FPS goal is claimed without device traces.

## Report
Please report pass/fail by scenario, release tag, phone OS build, and screenshot(s) of any wrong UI. For a crash, share sanitized error/logcat only if available. Record actual findings in `docs/DEVICE_TESTING.md`, `DEVELOPMENT_LOG.md`, `CHAT_HISTORY.md` and `ISSUES_AND_FIXES.md`; do not mark phase acceptance until the user submits real handset evidence.
