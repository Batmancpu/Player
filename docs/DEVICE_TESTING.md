# Device validation and crash playbook

This file separates automated build evidence from real-device evidence.

## Target device
- OPPO Reno10 5G, model CPH2531.
- Android 15 / ColorOS 15.
- MediaTek Dimensity 7050.
- User priority: stable 60-Hz interaction. Visual polish must not create jank, overheating or instability.

Reconfirm device and OS before interpreting a later incident.

## Install and launch
- Install the exact APK from a named GitHub Actions run.
- Record app version, APK SHA-256, package ID, build variant and workflow URL.
- Test cold/warm launch and launch after device restart.
- No login, cloud setup, telemetry prompt or unnecessary permission.

## Folder boundary and persistence
- Select one local folder containing test movies and nested series.
- Confirm the selection persists after process death/reboot.
- Verify no item from sibling folders or other providers appears.
- Revoke the grant in Android settings; app must show recovery, not crash.
- Reselect and confirm no duplicates.
- Test empty folder, unsupported files, large folders and provider errors.
- Confirm source files are unchanged.

## Metadata and privacy
- Test known movies, same-title remakes, years, anime, Unicode and series episode patterns.
- Test offline, slow network, timeout, credential errors, 404, rate limiting and API response changes.
- Cached details/posters must remain available offline.
- Inspect requests: normalized title/year/type and TMDB image fetch only. No file URI, full path, directory name or media bytes.
- Verify no Firebase, ACRA upload, analytics, telemetry, crash endpoint or unrelated host.

## Visual/performance
- Capture cold start, home, poster grid, detail, sheet and navigation.
- Scroll large library while images load; check layout shifts, blanks, stale blur and dropped frames.
- Test full Glass over bright/dark artwork and reduced-transparency/reduced-motion fallback.
- Open/close Glass lab repeatedly, resize/rotate if supported, background during transitions.
- Compare full Glass, frosted and no-glass paths.
- Measure frame timing, memory and thermal behaviour where possible. CI emulator tests do not replace handset tests.

## Playback format matrix and subtitle checks

Do not infer codec support from filename extension. For each local test asset record container, video codec/profile/level, audio codec, subtitle type, resolution, HDR mode, file size, engine version, decoder selected and pass/fail. Include common MP4/H.264/AAC and Matroska/MKV with H.264 or HEVC, WebM/VP9 and AV1 only where the target device reports support, multiple audio streams, embedded subtitles and sidecar SRT/ASS/VTT/TTML.

Exercise play/pause, seeking, progress, resume after process death, full-screen/orientation, audio selection, embedded subtitle selection, local external subtitle import, online subtitle search/download, subtitle delay adjustment/reset, and unsupported-track recovery. Verify no crash on malformed/unsupported media, no write/move/delete of source media, and app-private subtitle cache behaviour. Capture device logs for native decoder failures; CI compilation is not proof of codec compatibility.

## CurrentLens historical incident

User-reported native SIGSEGV on CPH2531 / Android 15, package com.mangoloads.currentlens.stability, build 0.2.2-stability, after “BrrrNewsApp composition started”. Exact tombstone/root cause is not present in the repository audit. Do not attribute it to a particular file without evidence.

CurrentLens later added a local JVM crash report/recovery screen. That catches ordinary Java/Kotlin exceptions, not necessarily native SIGSEGV. Capture native tombstone/logcat evidence if available.

## Crash triage
1. Record exact app version, package ID, APK SHA, workflow, device/OS and reproduction steps.
2. Distinguish Kotlin exception, ANR, SIGSEGV, OOM, GPU failure, system process death and installer/signature failure.
3. Preserve complete stack/tombstone. Keep FATAL EXCEPTION, signal 11, backtrace, Abort message and native frames.
4. Compare against last known good APK/commit.
5. Disable only one suspect subsystem (Glass, lens, backdrop, poster animation, scanner); do not change unrelated systems simultaneously.
6. Reproduce with minimal screen/effect and plain Compose as control.
7. Fix root cause or keep unstable path disabled; add regression test where possible.
8. Record evidence/confidence in ISSUES_AND_FIXES.md and fix/verification in DEVELOPMENT_LOG.md.

Never claim fixed because the app launched once or CI is green. Sanitize user-supplied logs before committing; never include private media paths, tokens, emails or unrelated data.
## 2026-11-14 — Obtainium in-place update confirmed by user

- User reports Player v0.1.51 was installed with Obtainium and then updated successfully to v0.1.53 **over the existing app**, confirming the install/update pipeline in practice. This is user-reported physical-device verification; GitHub CI also verified both signed Releases and the same pinned certificate with increased versionCode (51 → 53).
- **Confirmed:** in-place update worked without an uninstall. **Not separately confirmed:** persistence of a previously scanned library across the update, fully unattended background installation, or acceptance of Phase 1 media-library features. Do not conflate Obtainium's ability to check for updates with silent unattended installation.
- User intends to hand repository implementation to **Codex** now that manual setup works. The project remains in this GitHub repository; Codex should read AGENTS.md, docs/WORK_HANDOFF.md, docs/ROADMAP.md, docs/ARCHITECTURE.md, docs/DECISIONS.md, docs/ANDROID_SIGNING.md and docs/PHASE1_PHONE_TEST.md first.
- **Next engineering task:** complete and verify Phase 1 only: single local SAF folder read-only; actual recursive folder discovery; filename parser, Room index and rescans; safe cancellation/permission recovery; automated tests. Produce a permanently signed APK to GitHub Releases, then request actual OPPO phone acceptance. Avoid premature TMDB/playback/Glass work.
- No additional manual GitHub secrets/signing setup should be requested. Preserve `com.batmancpu.player`, the current pinned certificate, and strictly increasing `versionCode`. No debug APKs in the Obtainium release channel.


## v0.1.79 candidate — automated evidence only, 2026-11-14
Published signed APK: https://github.com/Batmancpu/Player/releases/tag/v0.1.79 (source f1d7f27, versionCode 79, unchanged certificate). CI 43618442469 passed nine Android 15 emulator integration cases plus unit/build/privacy/signing gates. This is not an OPPO test, file-provider acceptance or GPU benchmark. Await user update without uninstalling, nested-folder scan, offline reopen, rescan/cancel/switch/reconnect results under docs/PHASE1_PHONE_TEST.md.
