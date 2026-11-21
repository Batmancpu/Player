## 2026-11-21 progress update — production Glass and title details
- CI #211 compiled and ran all 51 Android 15 tests: 47 passed, 4 UI accessibility checks failed. Bounded dock selection and series details now pass; poster label merging and Switch state reporting still need correction. Current working tree exposes a localized title/type/year label on each clickable card and checks the Glass On/Off announcement plus persisted preference. CI #211 passed compilation but 4 UI checks failed; CI #212 then failed compilation on a missing Compose `role` import. That import is corrected and CI #213 is the verification gate.
- Production library navigation has one bounded Haze dock with an effects-off fallback. The title page has a landscape hero that renders a deterministic offline gradient and can use app-private backdrop artwork only after a separately confirmed TMDB match. Artwork remains unblurred.
- Remaining work: verify CI/screenshots, finish subtitle selection/delay and custom player controls/accessibility, refine production Glass, add Macrobenchmark/Perfetto and release-size checks, then do the efficiency review and publish one integrated signed APK. No interim APK. Reno10 frame-time/thermal/battery and acceptance evidence remain unavailable.

## Current implementation update — 2026-11-21

Room v3 offline metadata storage, encrypted user-managed TMDB settings, official TMDB Credits attribution, bounded fixed-origin HTTPS search client and manual title → candidate → separate confirmation flow are integrated. Confirmed data is cached in Room and shown offline; the only match operation is explicit and scoped to the current indexed folder. CI #115 / 44896086296 passed Android 15 tests plus unit/build/schema/privacy gates for transport and Credits. The app has the INTERNET capability for this opt-in stage, but no request occurs from scan, startup, scrolling or playback. Confirmed artwork cache/display is integrated after manual match; CI #122 passed. CI #123 attempt 2 passed offline/disabled-settings cache tests and all build/schema/privacy gates after attempt 1's transient UI timeout. Catalog poster tiles use local Files only. Live provider behavior, OPPO acceptance and performance remain unfinished. No installable APK/Release between milestones per user's request.

**Latest sidecar work (2026-11-21):** Room v4 persists opaque immediate-parent IDs (CI #130/#131 green). Playback now attaches only freshly revalidated same-parent local SRT/VTT/ASS/SSA/TTML children, with synthetic Media3 SRT decode coverage; CI #132 / 44911456180 passed Android 15 41/41, unit/build, Room v1-v4 schema and privacy gates. No installable APK/Release. Online subtitles remain unreviewed/unimplemented; subtitle delay/reset remains next. Do not publish an APK until the integrated app is ready. A focused Glass Lab dock readability/placement refinement and selected semantics regression passed in CI #136 / 44913968127 (41/41 Android 15 tests). CI #134's unrelated Pixel Launcher ANR and #135's test-query defect are documented. This remains a laboratory screen, not finished production Glass. Current playback follow-up adds a reversible immersive system-bar toggle; CI #138 passed the enter/exit accessibility test and all 42 Android 15 cases. The Compose title/track chrome now follows Media3 controller auto-hide; CI #144 passed 43/43 Android 15 tests, including tap restore and accessible exit. OPPO behavior remains unverified.

---

## Current implementation status — 2026-11-15

The user-reported v0.1.79 checks on OPPO CPH2531 confirm four movie/video titles with years, selected-folder use, library persistence after airplane-mode restart and folder switching. Cancellation feedback was unclear; Glass Lab looked broken in that version. The newer offline catalogue/details, local Media3 playback/resume, scan feedback and Glass preview are in signed v0.1.91, but that preview and its performance remain untested on OPPO. Phase 1 is not accepted.

Main now has exact Room v1/v2 schema history, the default-off normalized metadata query contract and a separately persisted manual-match foundation. CI #97 and #98 passed 17 Android 15 cases and all build/schema/manifest/signing/certificate gates. The current change adds explicit legacy and Android 12+ cloud/device-transfer backup exclusions while retaining `allowBackup=false`; focused Python tests pass 7/7 and CI #99 / 43740997631 passed Android15 17/17, builds/schema, compiled manifest/privacy and permanent signing-certificate gates. Its versionCode99 signed APK is CI-only; no Release was published. No HTTP, credential, network permission or release is part of this slice. Android 16 QPR2 cross-platform iOS transfer remains unconfigured because no iOS counterpart identifiers exist. Continue on `main`; handset acceptance and performance measurements require actual OPPO evidence.

# Player — six implementation phases

Status: implementation order approved 2026-11-14 and testing cadence updated 2026-11-21. This document supersedes the older ten-phase breakdown and remains the engineering sequence. The user now wants one integrated APK after the complete app is ready, not separate test releases or phone pauses after each phase. Historical details and technical rationale remain in PROJECT_CONTEXT.md, CHAT_HISTORY.md, docs/ARCHITECTURE.md and docs/DECISIONS.md.

## Current progress — 2026-11-21

The player has local Media3 playback, title and episode navigation, track selection, sidecar caption rendering outside the video zoom frame, resize modes, fullscreen/auto-hide, resume handling and playback speed. Production library navigation is connected to one bounded Haze dock with an effects-off fallback. CI #206/#207 exposed accessibility/layout regressions; CI #208 stopped before tests due to a composable string lookup inside a semantics lambda. The dock-label scope fix compiled, but CI #209 found a second composable-scope issue in the new poster description before tests ran. Both are corrected; portrait offline poster art and bounds coverage are queued for CI #210. The remaining work includes robust subtitle selection/delay, production player control-surface design, accessibility review, release-like size/performance analysis and a single integrated signed test APK. Reno10 frame timing/thermal/battery results and phone acceptance must remain unknown until measured on-device.

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


**Catalogue presentation (2026-11-21):** The default library now groups movies and TV series into horizontal poster shelves; search and type filters use the adaptive grid. This is still the local catalogue milestone, not final Apple TV-inspired polish. CI pending; OPPO scrolling and performance unmeasured.


## Updated delivery directive — 2026-11-21
The user requested a signed APK checkpoint now and wants implementation to continue through the remaining batches without pausing for interim phone installs. This supersedes earlier historical statements that no APK should be published between milestones. CI #173 passed Android 15 instrumentation (46/46), unit, schema, privacy and identity gates on `7cbff28881332f7da8de418b8b348166885ff857`; the next release-triggered run must pass the existing permanent-key signing and asset checks before the APK is called published. Keep actual Reno10 acceptance/performance as final evidence; never infer it from CI.

## 2026-11-21 — Signed APK v0.1.174 verified
- GitHub Release: https://github.com/Batmancpu/Player/releases/tag/v0.1.174; asset `Player-release-174.apk`, 32,313,420 bytes, versionCode 174, package `com.batmancpu.player`.
- APK SHA-256: `87d6af82c695ce61a8ca14dd117340086bd085cfe7f2622325152f5e9a1f2eb2`. Permanent certificate SHA-256: `4D:2F:17:89:ED:27:D3:96:13:35:FE:36:45:64:55:BD:9A:31:0B:0D:CD:F4:1E:8C:CC:92:53:98:33:9E:80:BF`. CI #174 passed Android 15 integration and signing/privacy/identity checks. This is a progress checkpoint, not final app acceptance; user does not need to test before the next batch.
- Current main has a Glass Lab navigation/overlay repair committed as `879b19902672d5e2686c628029800f1df6b03c00`; CI #175 is verifying it. Next action: inspect its UI test/screenshot result, then continue player format/subtitle, production Glass, and measured performance work.
## 2026-11-21 — Glass Lab batch verified on Android 15 CI
- CI #177 passed the Glass Lab return, dock bounds, and effects-off fallback UI checks; its synthetic on/off screenshots were inspected. CI #178 passed the full 46-test Android 15 integration suite plus unit/schema/privacy/identity gates after connecting one shared tab interaction source to the bounded Haze surface.
- Haze's existing pressed style now animates lens lighting/refraction and subtle scale; `GlassReducedMotionPolicy.System` is retained. The spring selection pill remains original Compose code. Haze is still lab-only; the screenshots are emulator evidence, not OPPO frame-time, thermal, or premium acceptance evidence.
- Next: apply measured design principles to production navigation and finish player/format/subtitle workflows; then build reproducible Macrobenchmark/Perfetto coverage and do the final release-size/performance audit.

## 2026-11-21 — Completed local playback resume behavior (verification pending)
- The Phase 4 implementation now resets its checkpoint to the beginning when Media3 naturally reaches STATE_ENDED; controls are shown again at completion.
- The new Android integration test uses only the generated synthetic fixture and asserts zero/paused checkpoint output. Await CI. Phone validation still requires OPPO.

## 2026-11-21 — Guarded completed-video resume state (verification pending)
- A shared checkpoint normalizer keeps zero/paused resume state through periodic and lifecycle saves after STATE_ENDED. The saved play intent is also cleared.
- CI test now waits across a periodic checkpoint interval; device result remains pending.

## 2026-11-21 — CI playback test timing correction
- CI #180's synthetic video is 30 seconds, longer than the test's 20-second wait from position zero. Start at 29 seconds; retain the post-completion timer assertion. No OPPO evidence is implied.

## 2026-11-21 — End-of-playback resume handling verified
- CI #182 / 44949435610 passed all 48 Android 15 instrumentation tests, unit tests, build and Room v1-v4 schema verification, including the near-end synthetic playback test and delayed checkpoint assertion.
- CI #180/#181 failures were test timing issues against a 30-second synthetic fixture; #181 also had a Pixel Launcher ANR. Release-only checks were skipped in #182.
- Continue Phase 5 subtitle tools and the planned production navigation/Glass work. No OPPO acceptance or FPS evidence is claimed.

## 2026-11-21 — Functional library navigation implementation
- Added Home, Movies, Shows and Search destinations for the local catalogue. Movie and series destinations use the same real parsed Room catalogue; Home keeps both shelves and Search focuses local title search.
- Replaced redundant type chips. Search has an empty prompt, and tab selection uses Material3 semantics. CI pending; OPPO layout/accessibility still needs end-of-app validation.
- The production bar is currently a solid fallback; bounded Glass is still scheduled for Phase 6 after navigation works.

## 2026-11-21 — Navigation selected semantics test correction
- The first navigation test run found the Movies shelf heading before the bottom-tab label, so the helper reported a false failure after movie-only filtering passed.
- Search all matching nodes and their ancestors for selected semantics. Build and remaining Android tests passed in CI #184; rerun the corrected assertion.


## 2026-11-21 progress update — production navigation
- Home/Movies/Shows/Search navigation is implemented and filters the local catalogue. Android 15 CI exposed a selected-tab accessibility state reporting gap; explicit selected/not-selected state descriptions and an assertion are now committed, pending CI.
- Sequence: verify navigation → complete player caption controls and playback interaction polish → apply bounded control-surface Glass and accessibility/reduced-motion fallbacks → run release-like size/performance checks and prepare one integrated test candidate. Physical Reno10 FPS/thermal evidence remains a required user-device measurement.


## 2026-11-21 progress update — playback controls
- Production library navigation now announces selected destination state for accessibility. CI #185 surfaced the issue; explicit state descriptions and regression coverage are committed, awaiting a new CI result.
- Added local Media3 playback rate choices (0.75×–2×) and synthetic UI coverage. Next: CI verification, remaining subtitle/player control polish, bounded Glass and release-like performance/size checks. Final user-device acceptance and Reno10 performance evidence remain separate gates.


## 2026-11-21 verification update — navigation and playback controls
- CI #203 / 44952898549 passed 50/50 Android 15 instrumentation tests, unit tests, app build and Room v1-v4 schema export. This verifies the production navigation selected-state announcements and local playback-speed menu selection.
- Release-specific signing, manifest and certificate jobs were skipped on the development push. No APK or release was produced. OPPO device experience and performance remain unverified.
- Next engineering slice: address the user's Glass Lab dock report, continue subtitle/player controls, then production Glass and the final efficiency/performance review before one integrated signed APK.


## 2026-11-21 build correction
CI #205 / 44955249002 failed before instrumentation at LocalLibraryScreen.kt:517 because the new Glass toggle used Compose `contentDescription` without importing it. The import is corrected; no tests ran on that commit. Await the new full CI before continuing visual tuning.
