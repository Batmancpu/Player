# Current work handoff — 2026-11-22

## Product and release constraints

- Player is a local-first Android catalogue and integrated Media3 player. The repository is the source of truth; read `AGENTS.md` and its canonical documents before edits.
- Preserve `com.batmancpu.player`, existing permanent release certificate and monotonically increasing run-number `versionCode`. User wants one integrated signed test APK only after planned app features and a final efficiency review; do not publish interim APKs or wait for routine phone checks.
- Preserve exactly one user-selected local SAF tree with a persisted read-only grant. No source-media writes/uploads, broad storage, backup, telemetry, ads or Player backend. Metadata is default-off, explicitly initiated and limited to approved TMDB fields/hosts; local browsing/playback works offline.
- Work directly on `main` in small commits; no task branches or PRs. Every change updates `AGENTS.md` and `DEVELOPMENT_LOG.md`, and keeps other context documents accurate.

## Verified repository state

- Latest committed main SHA before current edits: `dee49e5cfecc521967da3f13738c8cd8efdb5c6f` (`test(ui): wait for settings sheet before scrolling`). CI #219 / `44964806410` compiled and ran 52 Android 15 tests, 51 passing. The sole failure was a swipe helper even though the accessibility snapshot showed the Glass switch visible, in bounds and checked. Current test waits for the accessible switch and asserts visible bounds, localized On/Off label, checked state and persisted preference. Next CI run pending.
- User reported OPPO v0.1.79 recognized four videos with years, retained the catalogue after airplane-mode restart, and switched folders successfully. That report is not validation of the newer catalogue/player/Glass implementation.
- Latest release recorded in project history is signed v0.1.174. It is an older progress checkpoint, not the integrated candidate. Current signing identity and package must remain intact.

## Current integrated feature batch

A Compose-owned transport in `LocalPlayerScreen.kt` replaces generic PlayerView controls with play/pause, ±10-second seek and an accessible seek slider with elapsed/total timestamps. Native PlayerView controls are disabled to avoid duplication. The seek/time polling is isolated inside the visible transport and updates at 4 Hz; auto-hide pauses while scrubbing. Its nine Android playback tests passed in #216/#218. The production Glass dock persists its setting and Manage opens full-height. The accessibility regression had three diagnoses: initially only top actions after activity recreation, then a #218 sheet-entry race, then #219's unnecessary swipe assertion. The direct accessible-node correction awaits CI.

## Exact next actions

1. Commit the direct accessible-switch correction with AGENTS, DEVELOPMENT_LOG, CHAT_HISTORY and R-036; verify the full CI suite and inspect screenshots.
2. Continue the integrated app backlog in coherent batches: caption selection/visibility and local sidecars, player interaction/accessibility polish, Glass/navigation refinement, Macrobenchmark/Perfetto workflow, then release-size/security/dependency/efficiency review. User wants one signed APK after features and final efficiency review, not an interim APK.
3. Publish one higher-version signed Release when implementation and automated gates are ready. Provide the concise OPPO checklist then. Real handset acceptance and measured Reno10 FPS/thermal/battery remain pending; CI cannot establish them.

The detailed chronology and failed attempts are in `DEVELOPMENT_LOG.md`, `CHAT_HISTORY.md` and `ISSUES_AND_FIXES.md`; approved phase scope is in `docs/ROADMAP.md`.
