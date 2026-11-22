# Current work handoff — 2026-11-21

## Product and release constraints

- Player is a local-first Android catalogue and integrated Media3 player. The repository is the source of truth; read `AGENTS.md` and its canonical documents before edits.
- Preserve `com.batmancpu.player`, existing permanent release certificate and monotonically increasing run-number `versionCode`. User wants one integrated signed test APK only after planned app features and a final efficiency review; do not publish interim APKs or wait for routine phone checks.
- Preserve exactly one user-selected local SAF tree with a persisted read-only grant. No source-media writes/uploads, broad storage, backup, telemetry, ads or Player backend. Metadata is default-off, explicitly initiated and limited to approved TMDB fields/hosts; local browsing/playback works offline.
- Work directly on `main` in small commits; no task branches or PRs. Every change updates `AGENTS.md` and `DEVELOPMENT_LOG.md`, and keeps other context documents accurate.

## Verified repository state

- Latest committed main SHA: `066cf871464f95d296f2b26dbcf39f954fe2361b` (`fix(ui): reveal full library settings sheet`).
- CI run #214 / `44961886974` passed 51/51 Android 15 tests plus unit/build/Room v1-v4/privacy gates. Details and Glass On emulator captures were inspected; see `https://github.com/Batmancpu/Player/actions/runs/44961886974`.
- User reported OPPO v0.1.79 recognized four videos with years, retained the catalogue after airplane-mode restart, and switched folders successfully. That report is not validation of the newer catalogue/player/Glass implementation.
- Latest release recorded in project history is signed v0.1.174. It is an older progress checkpoint, not the integrated candidate. Current signing identity and package must remain intact.

## Current uncommitted batch

A Compose-owned transport in `LocalPlayerScreen.kt` replaces generic PlayerView controls with play/pause, ±10-second seek and an accessible seek slider with elapsed/total timestamps. Native PlayerView controls are disabled to avoid duplication. The seek/time polling is isolated inside the visible transport and updates at 4 Hz; auto-hide pauses while scrubbing. Its nine Android playback tests passed in #216/#218. Full-height Manage sheet is under verification: #216 failed after recreation, while #218 showed a test gesture during sheet entry (only close node existed). Current working test waits for the visible settings heading before scrolling. No local Gradle wrapper/Android SDK.

## Exact next actions

1. Commit the sheet-entry wait with required context and issue records; run the full suite and verify all 52 tests/screenshots before proceeding.
2. Continue the remaining approved roadmap: functional caption visibility/delay and local subtitle workflow; cohesive production player/Glass surfaces, navigation and accessibility states; then Macrobenchmark/Perfetto coverage and a release-like APK size/permission/dependency/performance efficiency review.
3. Produce the single higher-version, permanently signed Release APK only when integrated implementation and automated gates are ready. Give the user a concise OPPO checklist then. Do not claim Reno10 acceptance, FPS, battery or thermal results until actual-device evidence is available.

The detailed chronology and failed attempts are in `DEVELOPMENT_LOG.md`, `CHAT_HISTORY.md` and `ISSUES_AND_FIXES.md`; approved phase scope is in `docs/ROADMAP.md`.
