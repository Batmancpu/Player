# Current work handoff — 2026-11-22

## Product and release constraints

- Player is a local-first Android catalogue and integrated Media3 player. The repository is the source of truth; read `AGENTS.md` and its canonical documents before edits.
- Preserve `com.batmancpu.player`, existing permanent release certificate and monotonically increasing run-number `versionCode`. User wants one integrated signed test APK only after planned app features and a final efficiency review; do not publish interim APKs or wait for routine phone checks.
- Preserve exactly one user-selected local SAF tree with a persisted read-only grant. No source-media writes/uploads, broad storage, backup, telemetry, ads or Player backend. Metadata is default-off, explicitly initiated and limited to approved TMDB fields/hosts; local browsing/playback works offline.
- Work directly on `main` in small commits; no task branches or PRs. Every change updates `AGENTS.md` and `DEVELOPMENT_LOG.md`, and keeps other context documents accurate.

## Verified repository state

- Latest committed main SHA: `e655d2c` fixes the missing hero test helper. CI #231 / `44972910275` ran 53 Android tests: 51 passed, 2 failed. Compact Home/series/metadata tests now pass. The track picker still lacked a clickable label node; source restores explicit radio/label/selected/onClick semantics with the required `role` extension import. Poster ratio was measured while the card was partly obscured by the bottom dock; test now scrolls Home before measuring. CI #232 pending. CI #226 / `44969253993` passed 53 Android 15 tests, JVM/build, Room schema 1–4 and privacy checks. Synthetic video screenshots do not prove decoder or device performance. README concept art is explicitly not an app screenshot. CI #226 / `44969253993` passed 53 Android 15 tests, JVM/build, Room schema 1–4 and privacy checks. Synthetic video screenshots do not prove decoder or device performance. README concept art is explicitly not an app screenshot.
- User reported OPPO v0.1.79 recognized four videos with years, retained the catalogue after airplane-mode restart, and switched folders successfully. That report is not validation of the newer catalogue/player/Glass implementation.
- Latest release recorded in project history is signed v0.1.174. It is an older progress checkpoint, not the integrated candidate. Current signing identity and package must remain intact.

## Current integrated feature batch

A Compose-owned transport in `LocalPlayerScreen.kt` replaces generic PlayerView controls with play/pause, ±10-second seek and an accessible seek slider with elapsed/total timestamps. Native PlayerView controls are disabled to avoid duplication. The seek/time polling is isolated inside the visible transport and updates at 4 Hz; auto-hide pauses while scrubbing. Its nine Android playback tests passed in #216/#218. The production Glass dock persists its setting and Manage opens full-height. The accessibility regression had three diagnoses: initially only top actions after activity recreation, then a #218 sheet-entry race, then #219's unnecessary swipe assertion. The direct accessible-node correction awaits CI.

## Exact next actions

1. Commit the picker semantics/poster test correction with records; inspect CI #232, screenshots and all build/schema/privacy gates.
2. Continue the integrated backlog in coherent batches: complete catalogue/detail behavior, player interaction/accessibility, production Glass/navigation, Macrobenchmark/Perfetto and final license/privacy/size/efficiency review. Do not make interim APKs.
3. Publish one higher-version signed Release after integrated implementation and automated efficiency checks. Provide the concise OPPO checklist then. Real handset acceptance and measured Reno10 frame-time/thermal/battery remain pending; CI cannot establish them.

The detailed chronology and failed attempts are in `DEVELOPMENT_LOG.md`, `CHAT_HISTORY.md` and `ISSUES_AND_FIXES.md`; approved phase scope is in `docs/ROADMAP.md`.
