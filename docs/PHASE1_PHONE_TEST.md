# Phase 1 — OPPO phone acceptance checklist (not yet performed)

**Target:** OPPO Reno10 5G CPH2531, Android 15. Use a **signed** Player APK from the GitHub Releases page, never the CI-only debug artifact. As of the 2026-11-14 review, a signed Release has not yet been published because GitHub Actions signing secrets were missing.

## Preparing a safe test folder
Use Android's file manager to create a small test folder of media you already own or are permitted to test, including:
- Two MP4/MKV movies with title/year filenames, e.g. "Example.Movie.2020.mkv".
- One TV subfolder with "Example.Show.S01E01.mp4", "Example.Show.S01E02.mp4".
- An SRT subtitle file and a non-media text file.
- A separate unrelated video **outside** the selected folder (to verify isolation).
Never share private media paths or file contents in GitHub issues or logs.

## Install / update
1. After repository owner sets four encrypted signing secrets, open Actions -> Android CI -> Run workflow -> enable "Publish verified signed APK to GitHub Releases for Obtainium". Verify run success.
2. Open https://github.com/Batmancpu/Player/releases, select the newly created version and install its single Player-release-*.apk. Record tag, APK SHA-256, build/versionCode and Android installer result.
3. Add https://github.com/Batmancpu/Player to Obtainium as a GitHub source. Later releases should install over the existing app with no uninstall or data loss, provided the package ID and signing certificate remain unchanged.

## Phase 1 acceptance test
- Launch Player and choose the test folder using Android's **system folder picker**. Decline all other providers, and confirm Player never requests "all files" or access to photos/media outside that folder.
- Tap **Scan this folder**; confirm nested video names are shown and the unrelated outside file is absent. Subtitle count should include SRT; non-media text files should be ignored.
- Close Player completely and reopen it, including with airplane mode on. The previously scanned library should still appear without a rescan.
- Add one test video inside the selected folder; rescan; new file appears. Remove a test video; rescan; missing file disappears, but the source file was not modified by Player.
- Change to another local folder. Previously chosen folder's entries must not mix with the new library. Observe whether the OEM picker supports Android external-storage DocumentsProvider.
- Start a scan of a reasonably large nested folder and cancel it. No app crash; the prior complete library remains.
- Revoke the selected folder permission through Android settings if available; app should request folder reconnection instead of crashing or showing files from ungranted sources.
- If possible test 100+ mixed files for reasonable scrolling and restart behavior. Check filenames for TV episode identifiers, Unicode and year extraction.

## Report
Please report **pass/fail** for each scenario, the GitHub release tag, the phone OS build and screenshot(s) of any wrong UI. For a crash, share sanitized error/logcat only if available. The AI agent must record findings in docs/DEVICE_TESTING.md, DEVELOPMENT_LOG.md, CHAT_HISTORY.md and ISSUES_AND_FIXES.md. Do not claim this checklist passed until the actual user submits evidence.
