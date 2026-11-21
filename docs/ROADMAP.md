## Current implementation update — 2026-11-21

Room v3 offline metadata storage, encrypted user-managed TMDB settings, official TMDB Credits attribution, bounded fixed-origin HTTPS search client and manual title → candidate → separate confirmation flow are integrated. Confirmed data is cached in Room and shown offline; the only match operation is explicit and scoped to the current indexed folder. CI #115 / 44896086296 passed Android 15 tests plus unit/build/schema/privacy gates for transport and Credits. The app has the INTERNET capability for this opt-in stage, but no request occurs from scan, startup, scrolling or playback. Confirmed artwork cache/display is integrated after manual match; CI #122 passed. CI #123 attempt 2 passed offline/disabled-settings cache tests and all build/schema/privacy gates after attempt 1's transient UI timeout. Catalog poster tiles use local Files only. Live provider behavior, OPPO acceptance and performance remain unfinished. No installable APK/Release between milestones per user's request.

**Latest sidecar work (2026-11-21):** Room v4 persists opaque immediate-parent IDs (CI #130/#131 green). Playback now attaches only freshly revalidated same-parent local SRT/VTT/ASS/SSA/TTML children, with synthetic Media3 SRT decode coverage; CI #132 / 44911456180 passed Android 15 41/41, unit/build, Room v1-v4 schema and privacy gates. No installable APK/Release. Online subtitles remain unreviewed/unimplemented; subtitle delay/reset remains next. Do not publish an APK until the integrated app is ready. A focused Glass Lab dock readability/placement refinement and selected semantics regression passed in CI #136 / 44913968127 (41/41 Android 15 tests). CI #134's unrelated Pixel Launcher ANR and #135's test-query defect are documented. This remains a laboratory screen, not finished production Glass. Current playback follow-up adds a reversible immersive system-bar toggle; CI #138 passed the enter/exit accessibility test and all 42 Android 15 cases. The Compose title/track chrome now follows Media3 controller auto-hide; CI #144 passed 43/43 Android 15 tests, including tap restore and accessible exit. OPPO behavior remains unverified.

---

## Current implementation status — 2026-11-15

The user-reported v0.1.79 checks on OPPO CPH2531 confirm four movie/video titles with years, selected-folder use, library persistence after airplane-mode restart and folder switching. Cancellation feedback was unclear; Glass Lab looked broken in that version. The newer offline catalogue/details, local Media3 playback/resume, scan feedback and Glass preview are in signed v0.1.91, but that preview and its performance remain untested on OPPO. Phase 1 is not accepted.

Main now has exact Room v1/v2 schema history, the default-off normalized metadata query contract and a separately persisted manual-match foundation. CI #97 and #98 passed 17 Android 15 cases and all build/schema/manifest/signing/certificate gates. The current change adds explicit legacy and Android 12+ cloud/device-transfer backup exclusions while retaining `allowBackup=false`; focused Python tests pass 7/7 and CI #99 / 43740997631 passed Android15 17/17, builds/schema, compiled manifest/privacy and permanent signing-certificate gates. Its versionCode99 signed APK is CI-only; no Release was published. No HTTP, credential, network permission or release is part of this slice. Android 16 QPR2 cross-platform iOS transfer remains unconfigured because no iOS counterpart identifiers exist. Continue on `main`; handset acceptance and performance measurements require actual OPPO evidence.

# Player — six implementation phases

Status: implementation order approved 2026-11-14 and testing cadence updated 2026-11-21. This document supersedes the older ten-phase breakdown and remains the engineering sequence. The user now wants one integrated APK after the complete app is ready, not separate test releases or phone pauses after each phase. Historical details and technical rationale remain in PROJECT_CONTEXT.md, CHAT_HISTORY.md, docs/ARCHITECTURE.md and docs/DECISIONS.md.

## Completion and phone-test rule

Use the six phases below as a staged engineering plan, with automated CI after focused main commits. Do not stop for a phone install or publish an installable APK after each phase. Continue integration independently while the user is away. Once the complete catalogue/player/Glass experience is implemented, tested and documented as one coherent app, produce one signed Release APK with the permanent certificate and a complete phone-test checklist. Fix findings from that final test with higher-version updates. Never claim OPPO acceptance, FPS, thermal or battery results without device evidence. Keep AGENTS.md and DEVELOPMENT_LOG.md current on every change, preserve small main-only commits, and keep the Obtainium update chain intact.

**Release rule:** Use the NEW keystore explicitly approved and created on 2026-11-14 (see docs/ANDROID_SIGNING.md), alias and app identity for every APK. The older key and old fingerprint in historical notes were superseded BEFORE the first signed release. Keep applicationId com.batmancpu.player stable for updates until a user-approved migration plan. Do not regenerate or rotate the NEW key after the first signed release install. Increment versionCode on every new installable APK. Never publish unsigned/debug APKs as an Obtainium update. Manual milestone publishing is preferred to publishing every small commit.

## Phase 1 — Offline media library and one-folder privacy (ACTIVE)

**User-visible outcome:** choose one Android local folder; Player recursively finds movies/episodes/subtitle sidecars in that folder and subfolders; shows a plain, navigable offline list and remembers it after restart. No TMDB/API requirement, metadata/network request, Glass dependency in production, or playback needed.

Implementation steps in small commits:
1. [x] Android shell and system OpenDocumentTree selector; allowlist local external-storage DocumentsProvider only; persist **read-only** grant, revalidate on startup, recover from permission loss. Source implemented, device unverified.
2. [x] Pure cancellable, sequential scanner with video/sidecar classification, duplicate handling, containment callback and JVM tests. GitHub Actions run 36 passed. Android SAF adapter is wired; behavior on actual OPPO provider remains unverified.
3. [partial] SAF provider adapter: enumerate immediate child documents via the single granted tree URI; inspect file metadata without opening video bytes; reject nonlocal authority and out-of-root document IDs. Bound scanning and handle partial/provider errors as errors, not successful empty scans.
4. [partial] Present simple scan/progress/error/cancel/results screens; show movies/episodes (and optionally sidecar counts) from **real documents**, not mock artwork. Show clear permission recovery.
5. [partial] Deterministic filename parser for movie title/year and TV S01E02 / 1x02; handle common noise, Unicode and ambiguous matches; group episodes under series. No external matching yet.
6. [partial] Room persistence of selected-folder document identities, media metadata, parsed titles and scan status. Persist results offline; rescans update changed files and remove stale entries **only after a fully successful scan**; switching the selected folder must not mix libraries. Keep manual corrections prepared for later metadata stage; avoid destructive migrations.
7. [ ] Unit tests and instrumentation/device checks for nested folders, sibling folder exclusion, duplicates, permission revoked/regranted, cancellation, partial scan, reboots, empty folder, unsupported media, incremental rescan and source-file immutability.
8. [x] Add confirmed “Remove folder from Player”: clear Room index and persisted read-only grant with a retry path; original files are never deleted. CI #104 passed grant manager tests; CI #105 is checking resume-position cleanup and confirmation/cancel UI. Real provider behavior remains for the final phone checklist. No MANAGE_EXTERNAL_STORAGE/READ_MEDIA_VIDEO permission.

**Phone test gate:** choose one local Movies folder with nested show folders; scan shows only selected descendants; close/reopen app offline and still see library; rescan reflects additions/removals after success; permission loss recovers; switching folders replaces library without scanning the old tree. Install signed APK from GitHub Release; track subsequent releases in Obtainium. Until the user confirms, phase 1 remains in progress.

## Phase 2 — TMDB identification, artwork and offline metadata

**User-visible outcome:** recognizable posters/backdrops/details for local titles; pending/ambiguous matching states and manual correction; previously indexed media/metadata/artwork browsable offline.
- Confirm privacy-safe direct TMDB API credential model. No shared extractable APK secret, Player server or proxy. Credential optional; no metadata when absent.
- Match normalized titles/year/type, never local path, document URI, raw filename or video bytes. Match series once; resolve season/episode identity separately.
- Rate limits, timeouts, conservative candidate ranking, retries/cancellation, no ambiguous automatic overwrite, persistent user overrides.
- Download validated TMDB image URLs to bounded app-private cache, use maintained image loader and preserve downloaded offline metadata.
- Verify TMDB credits/approved logo, privacy requests and all failure/offline modes.
**Phone gate:** correct 10 mixed movies/series including remakes and Unicode; wrong matches manually fixed and retained; offline detail/posters remain.

## Phase 3 — Cinematic catalogue and navigation

The Apple-inspired original design system, quality states and high-contrast/fallback screen layouts begin here **without** production Glass. Performance profile on the real OPPO at 60 Hz. Later Phase 6 adds bounded live Glass only after features work.

**User-visible outcome:** complete real home/library with poster shelves, films/series, details, seasons/episodes, local search, empty/offline states and settings.
- Reuse Room state, accessible Compose navigation and image loading. Keep content crisp and functional without Glass.
- Stress with 100/500/1000 files, long titles, no artwork, large text, TalkBack and scroll restore.
**Phone gate:** browse a real mixed library end-to-end, search, open details, switch offline and rotate without lost selection or crashes.

## Phase 4 — Built-in local playback

**User-visible outcome:** open a video from the catalogue, play/pause/seek, resume position, full-screen/orientation and audio focus without uploading/streaming any media.
- Evaluate AndroidX Media3 first on SAF content URI and real device format corpus; choose LibVLC only if measured gaps justify licence/ABI/thermal costs.
- Keep engine behind an interface; handle unsupported containers, codecs or permission failures safely.
- Persist resume points in local app data. No source media edits.
**Phone gate:** test mixed MP4/MKV/WebM assets on CPH2531 with real decoder, performance and failure evidence.

## Phase 5 — Audio and subtitle tools

**User-visible outcome:** embedded audio/subtitle selection, sidecar/local subtitle import, adjustable subtitle delay/reset, reviewed optional online subtitle search/download.
- Surface language/codec/channel labels as available; compact, uncluttered controls.
- Review provider API, terms, privacy, credentials and attribution **before** networking; send only normalized identifiers, not local paths, subtitle/media bytes.
- Downloaded subtitles stored in app-private cache; original media/subtitles untouched.
**Phone gate:** switch embedded tracks, load sidecar, adjust/reset subtitle delay, and test online retrieval only if approved provider exists.

## Phase 6 — Liquid Glass, accessibility, performance and launch

Apply the exact screen-by-screen Glass surfaces and interactive seek/volume/subtitle slider thumb spec in `docs/DESIGN_SYSTEM_AND_PERFORMANCE.md`. Benchmark Haze source-backed chrome against fallback on CPH2531, including frame overruns and thermal behavior. Do not promise 60/120 FPS until physically measured.

**User-visible outcome:** polished Apple TV-inspired cinematic presentation with restrained iOS-inspired Liquid Glass on functional controls; reliable fallbacks and verified stable release.
- Validate isolated Haze 2.1.0 Glass Lab first; no experimental native window backdrop on Android 15.
- Glass limited to navigation, sheets and controls, not posters/video; reduced-motion/transparency and unsupported GPU fallbacks.
- Accessibility, battery, cold start, scrolling, offline, caches, source privacy and licences verified on handset.
**Phone gate:** all earlier journeys function with Glass on/off; 60-Hz target and no confirmed instability; final signed APK update and licence/privacy checks pass.

## Current release and implementation state — 2026-11-15

- Latest public installable APK: signed [v0.1.91](https://github.com/Batmancpu/Player/releases/tag/v0.1.91), versionCode 91, applicationId `com.batmancpu.player`, APK SHA-256 `7c9ff8287405be9b1f607259608d84a256e5aed1fcc4cea24e3f9b882dd05e17` and unchanged permanent certificate. CI91 published it from current offline preview source e4cc4b1.
- CI91 has 15 Android15 synthetic integration cases for Room/SAF/privacy, launcher state, local content-only first-frame/seek and denial/resume isolation, catalogue search/details/play identity, dock accessibility/fallback, plus JVM/build and signed production manifest/certificate checks. Five synthetic screenshots were inspected. This evidence is not target-handset acceptance, codec coverage beyond the fixture or a performance benchmark.
- User has confirmed on v0.1.79: four videos recognized with names/years, selected-folder use, offline restart retention and folder switching. Full Phase 1 permission recovery/cancellation/change scenarios remain unverified. None of the six phone-testable phases may be called accepted without user/device evidence.
- Current Phase 2 implementation: Room v3 provider cache, user-confirmed per-document matches, encrypted default-off settings, required About/Credits attribution, fixed-origin bounded HTTPS search, explicit detail-sheet candidate selection/confirmation and cached offline details. CI #115 verified transport and Credits; the larger UI integration is in CI. No artwork downloading/display is integrated yet. Next: bounded app-private artwork cache and local-only rendering with cancellation, memory/performance and offline tests.
- No marketing/120-FPS claim: actual OPPO CPH2531 profiling (60-Hz FrameTimingMetric/Perfetto baseline, then optional120-Hz and thermal/memory checks) remains future evidence.
- Release policy: increase versionCode from GitHub Actions run number, preserve `com.batmancpu.player` and the permanent certificate, install only Release APKs, never debug artifacts; maintain `allowBackup=false`, one read-only local SAF folder and no original-media writes.
- Estimate of whole product completion is intentionally not refreshed here; the six phases have unequal work and no phase has full phone acceptance yet.


## 2026-11-21 — artwork implementation
Coil Compose 3.6.3 Apache-2.0 decodes only app-private cached Files; no image-network module. Fixed unauthenticated CDN transport enforces host/path/redirect/time/byte/pixel bounds and cancellation. Atomic no-backup cache is capped at 96 MiB. Metadata is saved first; poster download follows only the separate confirmation action. Instrumentation uses fake API/artwork and synthetic PNG. CI pending. Do not produce installable APK until the integrated app is ready.


## 2026-11-21 — artwork integration verification
CI #121 attempt 2 passed the corrected manual confirmation/artwork test, Android 15 integration, JVM/build, Room schema and privacy checks. Attempt 1's UI failures all reported Pixel Launcher not responding; rerun passed. No user APK/Release. A new slice displays cached confirmed posters in lazy catalogue cards, with crisp artwork, contrast scrim and original placeholder when no poster exists. It reads only app-private Files; rendering/scroll does not request artwork. Verification pending. Device rendering/performance remain open.


## 2026-11-21 — grid posters and reference license
CI #122 passed the poster card change and all existing Android 15/UI, unit, schema and privacy gates. Cards render only existing app-private confirmed artwork locally; no CDN request while browsing. A new offline/disabled-setting safety regression is in CI. Licensing research recorded below in UPSTREAM_AND_PRIVACY: user's FCZenith project and immediate Zenith use GPL-3.0; Player takes visual/design inspiration only and copies no source or assets. No user APK/Release; continue to one complete final candidate.


## 2026-11-21 — accessibility copy and bounded test waits
Removed unbounded instrumentation waitForIdleSync calls that caused transient CI #123 preview timeout; the exact previous source passed attempt 2. Tests now poll accessibility and callbacks with finite deadlines. Corrected library note to state TMDB artwork/matching are optional and local use remains offline. Follow-up CI pending.


## 2026-11-21 — player controls progress
The local Media3 player now exposes explicit selectors for supported embedded audio and subtitle tracks; the subtitle selector can disable text rendering. This is source work pending CI and is not a completed phase or device result. Continue with safely associated local sidecar subtitles, subtitle-delay controls and playback polish, then finish cinematic navigation/Glass and prepare one integrated signed APK for phone testing. User requested no interim installable APKs. Actual OPPO acceptance and measured frame/thermal/battery evidence remain pending.


## 2026-11-21 — player selector correction
A live source audit found the prior selector commit was documentation-only; its CI did not test selector code. Corrective source now adds Media3 embedded audio/subtitle selectors and an Android test for the synthetic audio selector; this is unverified pending CI. Continue sidecar subtitle discovery, delay controls and later cinematic/Glass work before creating one integrated installable APK. Phase gates and OPPO acceptance remain open.


## 2026-11-21 — Phase 5 sidecar foundation
CI #126 verified embedded Media3 audio selector integration. New source propagates opaque SAF parent identity from immediate-child enumeration and matches only supported same-parent subtitle names with deterministic bounds; scanner and matcher tests are pending CI. This is foundation only: Room still needs a non-destructive migration to persist parent IDs, and playback still needs validated local subtitle URIs. Continue both before calling sidecar support integrated. No interim APK.


## 2026-11-21 — Room v4 parent persistence in progress
CI #129 / 44908615223 verified subtitle-matcher fixes. Current work adds an additive v4 `parentDocumentId` migration, stores scanner-captured opaque IDs in Room and restores them offline. It verifies old rows receive empty parent IDs and historical schemas remain unchanged. Pending CI/schema artifact. Next wire only validated same-parent supported subtitle URIs into Media3; no source file writes.


## 2026-11-21 — Room v4 parent persistence verified
CI #130 / 44909463881 passed Android 15 migration/offline persistence, JVM/build/schema/privacy. The exact KSP-generated Room v4 snapshot is committed, preserving v1-v3. Parent IDs survive database close/reopen; legacy records receive an empty parent value and do not acquire guessed sidecar relationships. Next implement local sidecar URI validation/playback and tests. No installable APK at this intermediate step.


**Catalogue detail update (2026-11-21):** Series details now group local parsed episodes by season and present compact episode numbers; the underlying document IDs remain the playback identity. This is a filename-derived grouping only. Episode titles and artwork still require explicit, confirmed metadata matching. CI pending.


**Playback chrome correction (2026-11-21):** CI #151 reproduced the header staying visible after Media3 reported active playback. An explicit Compose timeout now hides the PlayerView controller and header together while playing, retains controls when paused and restarts on reveal. Verification pending.


**Playback format research (2026-11-21):** DEC-021 keeps Media3 primary while target-device local corpus coverage is unmeasured. Official docs support common containers but device decoder availability determines codecs. LibVLC (LGPLv2 engine) is a candidate fallback only after measured failures and package/performance/license review. No universal format promise.
