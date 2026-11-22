# Current work handoff — 2026-11-22

## Product and release constraints

- Player is a local-first Android catalogue and integrated Media3 player. The repository is the source of truth; read `AGENTS.md` and its canonical documents before edits.
- Preserve `com.batmancpu.player`, existing permanent release certificate and monotonically increasing run-number `versionCode`. Latest instruction: produce one signed APK per substantial completed batch (not per small edit), then continue without waiting for routine phone checks. Normal push CI's debug/test assemblies are unpublished, not Obtainium Releases.
- Preserve exactly one user-selected local SAF tree with a persisted read-only grant. No source-media writes/uploads, broad storage, backup, telemetry, ads or Player backend. Local browsing/playback must work offline. User now wants automatic TMDB lookup with no per-user connection and only normalized title/year/type in app request fields. Official auth requires an app/user credential; the user says non-commercial and prefers a Player-wide application key, which is extractable from an APK and must not be treated as a secret. No key is present yet; do not silently add a proxy/provider.
- Work directly on `main` in small commits; no task branches or PRs. Every change updates `AGENTS.md` and `DEVELOPMENT_LOG.md`, and keeps other context documents accurate.

## Verified repository state

- Latest pushed main SHA: `4e69b38`. CI #243 / `44980944579` compiled source and ran 55 Android 15 tests; 54 passed, with the caption size accessibility range as the sole failure. CI #244 / `44981445066` stopped at app compilation due to a missing Compose semantics extension import. CI #245 / `44981754235` compiled and ran 55 tests; 53 passed; its screenshot exposed Reset below the half-expanded settings sheet and the metadata confirmation test did not scroll to its action. CI #246 / `44982372098` ran 55 tests; 54 passed. The custom size/position accessibility range and set-progress controls now pass; metadata confirmation also passes. Reset still was missing from the tree because the sheet remained half-expanded on a 320×640 display. The working tree now makes the caption sheet skip partial expansion, retain internal scroll for larger text, and asserts Reset's visible safe bounds. This correction is uncommitted and pending CI #247. CI #240 / `44978138086` passed 53/53 Android 15 tests and unit/build/Room v1–v4/privacy checks. No signed APK has been made during this caption batch. Synthetic tests do not prove Reno10 playback/performance. README concept art is not a product screenshot.
- User reported OPPO v0.1.79 recognized four videos with years, retained the catalogue after airplane-mode restart, and switched folders successfully. That report is not validation of the newer catalogue/player/Glass implementation.
- Latest release recorded in project history is signed v0.1.174. It is an older progress checkpoint, not the integrated candidate. Current signing identity and package must remain intact.

## Current integrated feature batch

A Compose-owned transport in `LocalPlayerScreen.kt` replaces generic PlayerView controls with play/pause, ±10-second seek and an accessible seek slider with elapsed/total timestamps. Native PlayerView controls are disabled to avoid duplication. The seek/time polling is isolated inside the visible transport and updates at 4 Hz; auto-hide pauses while scrubbing. Its nine Android playback tests passed in #216/#218. The production Glass dock persists its setting and Manage opens full-height. The accessibility regression had three diagnoses: initially only top actions after activity recreation, then a #218 sheet-entry race, then #219's unnecessary swipe assertion. The direct accessible-node correction awaits CI.

## Exact next actions

1. Commit the fully expanded compact caption sheet correction, inspect CI #247 screenshots/results and all build/schema/privacy gates, fixing any errors before the single signed caption-batch APK.
2. Implement the user-requested automatic normalized-title metadata/artwork flow using only a Player-wide application credential and direct approved TMDB hosts. No credential currently exists in the repo; ask for its one-time setup only after other implementation prep. Keep user paths/media local and preserve an off switch/cache/offline use.
3. Continue in no more than five substantial batches: player/captions, metadata/artwork, premium Glass/navigation, performance/reliability, final security/size/efficiency. Produce one signed higher-version APK at each completed batch boundary; do not wait for phone acceptance between batches. Actual Reno10 performance and phase acceptance remain evidence only the handset/user can supply.

The detailed chronology and failed attempts are in `DEVELOPMENT_LOG.md`, `CHAT_HISTORY.md` and `ISSUES_AND_FIXES.md`; approved phase scope is in `docs/ROADMAP.md`.
