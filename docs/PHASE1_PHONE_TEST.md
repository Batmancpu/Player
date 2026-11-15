Current larger offline preview checklist (search/details/playback/Glass): [OFFLINE_PREVIEW_TEST.md](OFFLINE_PREVIEW_TEST.md). v0.1.79 core phone successes are recorded below; remaining physical checks do not pause independent implementation per D-026.

# Phase 1 — OPPO phone checks (partially performed)

**Target:** OPPO Reno10 5G CPH2531, Android 15. Use a **signed** Player APK from GitHub Releases, never the CI-only debug artifact. Current release: [v0.1.91](https://github.com/Batmancpu/Player/releases/tag/v0.1.91), versionCode 91, APK SHA-256 `7c9ff8287405be9b1f607259608d84a256e5aed1fcc4cea24e3f9b882dd05e17`. CI91 passed 15 Android 15 integration cases plus JVM/build/privacy/identity/permanent-certificate gates. Update the existing installation through Obtainium without uninstalling. The user reports that v0.1.79 correctly recognized four video titles/years, retained the library after airplane-mode restart and switched folders. Cancellation feedback was unclear and the old Glass Lab dock looked broken. Search/details/local playback and the repaired dock are new in v0.1.91 and still need OPPO evidence. Phase 1 is not accepted; performance is unmeasured.

## Preparing a safe test folder
Use Android's file manager to create a small test folder of media you already own or are permitted to test, including:
- Two MP4/MKV movies with title/year filenames, e.g. "Example.Movie.2020.mkv".
- One TV subfolder with "Example.Show.S01E01.mp4", "Example.Show.S01E02.mp4".
- An SRT subtitle file and a non-media text file.
- A separate unrelated video **outside** the selected folder (to verify isolation).
Never share private media paths or file contents in GitHub issues or logs.

## Install / update
1. The agent verifies tests/signing and publishes the Phase 1 candidate. No further signing-secret setup is needed.
2. Open https://github.com/Batmancpu/Player/releases, select the newly created version and install its single Player-release-*.apk. Record tag, APK SHA-256, build/versionCode and Android installer result.
3. Add https://github.com/Batmancpu/Player to Obtainium as a GitHub source. Later releases should install over the existing app with no uninstall or data loss, provided the package ID and signing certificate remain unchanged.

## Short v0.1.91 preview check

1. Update through Obtainium without uninstalling; confirm the four existing videos still appear.
2. Search, open a title, tap **Play video**, pause/seek, go back and reopen to check resume.
3. Turn on airplane mode and confirm the library remains.
4. Open **Glass preview**, tap each tab and compare the effects-on/effects-off layouts. Report clipping or unreadable labels; no file deletion is required.

## Phase 1 acceptance test
- Launch Player and choose the test folder using Android's **system folder picker**. Decline all other providers, and confirm Player never requests "all files" or access to photos/media outside that folder.
- Tap **Scan this folder**; confirm nested video names are shown and the unrelated outside file is absent. Subtitle count should include SRT; non-media text files should be ignored.
- Close Player completely and reopen it, including with airplane mode on. The previously scanned library should still appear without a rescan.
- Optional rescan check: tap Rescan when useful; no file addition or removal is needed. Player intentionally has no delete control and must not modify originals. Folder switching can be checked independently.
- Change to another local folder. Previously chosen folder's entries must not mix with the new library. Observe whether the OEM picker supports Android external-storage DocumentsProvider.
- Start a scan of a reasonably large nested folder and cancel it. No app crash; the prior complete library remains.
- Revoke the selected folder permission through Android settings if available; app should request folder reconnection instead of crashing or showing files from ungranted sources.
- During a scan, rotate the phone if supported; return from Glass Lab and verify the same scan/library state. Switch A→B→A and confirm only the current folder’s results appear; cancel/rescan and confirm old work cannot replace newer results.
- If possible test 100+ mixed files for reasonable scrolling and restart behavior. Check filenames for TV episode identifiers, Unicode and year extraction.

## Report
Please report **pass/fail** for each scenario, the GitHub release tag, the phone OS build and screenshot(s) of any wrong UI. For a crash, share sanitized error/logcat only if available. The AI agent must record findings in docs/DEVICE_TESTING.md, DEVELOPMENT_LOG.md, CHAT_HISTORY.md and ISSUES_AND_FIXES.md. Do not claim this checklist passed until the actual user submits evidence.

