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

## Workflow and release blockers

- **Current state (2026-11-14):** Signed GitHub test Releases [v0.1.51](https://github.com/Batmancpu/Player/releases/tag/v0.1.51) and [v0.1.53](https://github.com/Batmancpu/Player/releases/tag/v0.1.53) were published by passing workflows #51 and #53; matching permanent signing certificate, applicationId `com.batmancpu.player`, and increasing versionCode were verified. The user **confirmed an in-place update in Obtainium** from v0.1.51 to v0.1.53, without uninstalling.
- GitHub Actions signing credentials are configured and validated, and no further manual signing setup is needed. NEVER ask for private key or password in a chat, commit or log. `docs/ANDROID_SIGNING.md` contains the CURRENT authoritative public certificate fingerprint after a pre-release reset; earlier keys were superseded.
- The in-place update test is **passed**, but unattended/silent background auto-install and persistence of a previously populated Room library across an update were not separately tested. Phase 1 local-media functionality and device acceptance remain incomplete.
- The next owner is Codex: prioritize finishing Phase 1 and releasing a signed APK for phone testing, not broad feature development. Keep commits and verification focused; use `docs/PHASE1_PHONE_TEST.md`.
- CI debug artifacts are for build investigation only; only signed GitHub Release APKs are part of the Obtainium update channel. Never change package ID/key or decrease versionCode once released.


## Brief progress snapshot — 2026-11-14
Planning estimate only: **10–15% of the whole product**, roughly **half of Phase 1 implemented**; **0/6 phases accepted on the target phone**. Scope/effort is uneven, so phases are not equal percentages. Implemented foundations and recent fixes still need provider/Room/lifecycle/phone verification. Next: grant/root recovery → query cancellation/bounds → persistence/parser/UI checks → signed Phase 1 candidate and user acceptance. Later: metadata/artwork → cinematic catalogue → playback → audio/subtitles → Glass/accessibility/measured performance.

## Phase 1 candidate readiness — 2026-11-14
- Source/JVM/build verified: retained library state, grant/root recovery, read-only/orphan reconciliation, bounded/cancellable provider query helper, safe completed-scan reconciliation, grouped series and conservative filename edge cases.
- Android 15 emulator: 9 integration cases passed (CI 43615935921) for Room reopen/empty/reconcile/isolation/rollback, cursor lifetime/cancellation, merged privacy and launcher recreation. These use synthetic data/provider; real OPPO SAF/provider and update-data persistence still require phone acceptance.
- Pending before publication: PR #8 final APK manifest/identity/attribution gate on signed main; explicit signed candidate Release. Pending after publication: actual OPPO checklist. Phase 1 remains unaccepted; no later phase started.
- Known non-blocking follow-ups: export Room v1 schema before any migration; persist scan-history/size/date metadata in a later schema-tested slice; broader filename corpus and handset accessibility/performance evidence. Core Phase 1 goal is offline discovery/list persistence, not playback/metadata/production Glass.

## Current delivery gate — 2026-11-14
Signed Phase 1 candidate [v0.1.79](https://github.com/Batmancpu/Player/releases/tag/v0.1.79) is published; CI 43618442469 passed automated, built-APK privacy/identity and permanent certificate gates. Next is the actual OPPO phone checklist, not Phase 2 production. Zero phases phone-accepted until user reports. Latest branch rule: direct main-only small tested commits, at most one optional backup if genuinely needed; older task-PR exception superseded.
