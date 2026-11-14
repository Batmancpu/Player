# Development log

This is the chronological record of work actually performed. Planned work belongs in `docs/ROADMAP.md`; never record a plan as completed implementation.

## 2026-11-14 — Project bootstrap and source reconnaissance

### User goal captured
- User wants a very simple local media-library app inspired by AfuseKt/Infuse-style poster scraping.
- User selects one folder only; the app finds movie/series files, retrieves matching posters and metadata, and displays a high-fidelity Apple TV / iOS 27-inspired Liquid Glass interface.
- No streaming through Player's servers or any other media-hosting service. No media upload.
- User expects AI agents to act as technical owners, make small GitHub commits, inspect workflow outcomes, correct defects, and keep a durable handoff so development can continue after chat context is lost.
- Repository name `Player` is temporary; user may rename later.

### Repository facts verified
- `Batmancpu/InfuseKt-release` is a release-only fork of `AttemptD/AfuseKt-release`; its README states it does not contain app source code. Preserve it unchanged.
- User created `Batmancpu/Player` for the new source project. At inspection it was a public repository on `main`, with only an initial README.
- `Batmancpu/FCZenith` contains the user's preferred Liquid Glass reference, including a custom Backdrop engine integration and performance/accessibility guidelines.
- `Batmancpu/CurrentLens` includes local crash diagnostics/recovery and defensive local-store handling in commit `2475970123a2a2fb0e05be25d1d9a45061186c73`. It also removed recurring GPU luminance readback from the glass contrast path to reduce synchronisation/jank.
- `Batmancpu/brrrBoard` documents Android OEM/device limitations for live blur and the need for fallbacks.
- `the-mskd-dev/Flux` is a promising local-media app reference with SAF scanning, TMDB metadata, Room persistence and Compose. It is GPL-3.0-or-later; no code has been imported.

### Technical research verified
- Android's official Storage Access Framework uses `ACTION_OPEN_DOCUMENT_TREE` to grant access to a selected folder tree and its descendants; it does not grant unrestricted access to the rest of storage. Some locations/providers have Android-version-specific restrictions.
- TMDB's official FAQ says free API access is for non-commercial use with attribution and requires the notice: “This product uses the TMDB API but is not endorsed or certified by TMDB.” Commercial use requires a separate licence.
- Haze 2.1.0 was released 2026-11-02. Glass is stable and supports merging/morphing nearby surfaces via `hazeGlassContainer`; it updates Kotlin to 2.4.21 and Compose Multiplatform to 1.12.2. It remains a candidate pending toolchain compatibility build and handset testing.
- Apple's public Human Interface Guidelines specify that Liquid Glass belongs in the functional layer (controls/navigation), not over the content layer. This informs the decision to keep posters crisp and make controls glass.

### Known device incident captured (not fully root-caused)
The user previously reported CurrentLens 0.2.2-stability crashing on OPPO CPH2531 / Android 15 with native `SIGSEGV` after “BrrrNewsApp composition started”. The current source review did not provide the exact tombstone/root cause. A Kotlin exception handler is not a reliable catcher for native SIGSEGV. Player must avoid native/JNI/NDK features initially and validate all Glass effects on the real device.

### State at end of this entry
- Documentation/requirements captured.
- No Android application code or build workflow exists yet.
- No APK has been built or installed.
- No device-side tests have been run.
- Next: commit architecture/decisions/roadmap/device-test instructions, then bootstrap a minimal Android/Compose app and CI in small commits.

## Logging rules for future entries

For each completed commit, append:
- Date and commit SHA/link.
- Exact paths changed.
- Why the change was necessary.
- Behaviour before and after.
- Tests/build commands and actual outcomes.
- GitHub Actions run URL and artifact where applicable.
- Device tested or explicitly “not yet device-tested”.
- Risks, regressions, and next action.

For every failure, record the first failing run, useful error excerpt, root-cause confidence, correction commit and regression verification. Do not delete failed attempts from this record.


## 2026-11-14 — Architecture, roadmap and device playbook committed

Documentation commits:
- 2b3a44b — docs/ARCHITECTURE.md: native Android layer boundaries, SAF single-folder boundary, metadata/network boundary, stable document identity, scanning rules and Glass architecture.
- 51019ab — docs/DECISIONS.md: accepted product constraints, proposed Haze/Android choices, open API-credential/provider/package decisions and rejected approaches.
- 5a0ec0e — docs/ROADMAP.md: 73 small planned commits across build foundation, design system, Glass proof, SAF, metadata, persistence, screens, hardening and release. These are planned, not completed.
- ef43767 — docs/DEVICE_TESTING.md: CPH2531 test matrix and native crash triage playbook.
- 3990cd4 — ISSUES_AND_FIXES.md: historical CurrentLens SIGSEGV and live-blur OEM risk; root cause explicitly marked unverified.
- 340b9d5 — root project handoff and agent instructions established.

Tests: documentation review only. No application build exists yet; no APK or device test. Next: create a minimal Android/Compose project in small commits, then add GitHub Actions and obtain the first passing debug APK.
