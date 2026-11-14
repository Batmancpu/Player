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