## Current implementation status — 2026-11-15
The user-reported v0.1.79 checks on OPPO CPH2531 confirm four movie/video titles with years, folder selection, library persistence after airplane-mode restart and folder switching. Cancellation feedback was unclear; Glass Lab looked broken in that version. Main now contains an offline searchable catalogue/details, local Media3 playback/resume preview, scan feedback and a single-surface Glass Lab dock. Functional commit e4cc4b1 passed CI90 / 43678768709, and signed v0.1.91 was published from release-trigger commit 36e6c68 after all CI91 tests, signed privacy/identity and certificate gates passed. New preview behavior and performance still need OPPO evidence; Phase 1 is not accepted.

Per D-026, continue independent implementation while handset testing is unavailable, without inventing device acceptance or frame-rate results. After publishing this offline preview, start with Room v1 schema export and normalized metadata-request/explicit-opt-in contracts. No HTTP client or new permission is part of the current offline preview. Work directly on `main`; no task branches/PRs.

# Player — six phone-testable release phases

Status: approved user workflow, 2026-11-14. This document supersedes the older ten-phase breakdown and is the **active implementation plan**. Historical details and technical rationale remain in PROJECT_CONTEXT.md, CHAT_HISTORY.md, docs/ARCHITECTURE.md and docs/DECISIONS.md.

## Definition of done for EVERY phase

A phase is not complete merely because code builds. Before advancing:
0. Apply the original premium UI/performance specification in `docs/DESIGN_SYSTEM_AND_PERFORMANCE.md` and upstream-reuse/privacy specification in `docs/UPSTREAM_AND_PRIVACY.md` to each applicable slice. Every small/large change refreshes AGENTS.md recent ledger AND DEVELOPMENT_LOG.md; preserve the working signed Obtainium update chain.
1. Commit small, testable changes directly to main, each with a dated DEVELOPMENT_LOG.md entry; do not proceed on a failing main.
2. Automated tests pass; GitHub Actions builds and verifies the signed Release APK with the **existing** permanent signing certificate; no debug APK is recommended for installing/updating.
3. Publish ONE named GitHub Release with the signed APK and human-readable changes/testing instructions; this release is the Obtainium update source.
4. User installs/updates on OPPO Reno10 5G CPH2531 (Android 15), tests phase acceptance checklist and reports findings (including crash/log evidence if necessary). Record verified device results separately from CI.
5. Fix critical failures in focused commits and publish a higher-version signed APK; only mark acceptance complete when user confirms real-device behavior. Do not claim acceptance without evidence.
6. Update docs/WORK_HANDOFF.md, CHAT_HISTORY.md, PROJECT_CONTEXT.md and the issue register appropriately. Record exact commit, CI run, Release URL/tag, SHA-256, signing certificate fingerprint, app versionCode and next step.

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
7. [ ] Unit tests and instrumentation/device checks for nested folders, sibling folder exclusion, duplicates, permission revoked/regranted, cancellation, partial scan, reboots, empty folder, unsupported media, incremental rescan and source-file immutability. No MANAGE_EXTERNAL_STORAGE/READ_MEDIA_VIDEO permission.

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
- Current Phase 2 foundation: KSP-based Room v1 export is being added; CI will validate and upload the generated JSON, which must be inspected and committed before metadata columns/migrations. Then add normalized title/year/type value models and consent-bound request tests. Network, credentials and destination allowlisting remain off until separately implemented and reviewed.
- No marketing/120-FPS claim: actual OPPO CPH2531 profiling (60-Hz FrameTimingMetric/Perfetto baseline, then optional120-Hz and thermal/memory checks) remains future evidence.
- Release policy: increase versionCode from GitHub Actions run number, preserve `com.batmancpu.player` and the permanent certificate, install only Release APKs, never debug artifacts; maintain `allowBackup=false`, one read-only local SAF folder and no original-media writes.
- Estimate of whole product completion is intentionally not refreshed here; the six phases have unequal work and no phase has full phone acceptance yet.
