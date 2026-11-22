## Current implementation update — 2026-11-22

User clarified one signed APK at each substantial batch boundary, not per edit; ordinary push CI produces unpublished test/debug artifacts only. CI #240 / 44978138086 passed 53/53 Android 15 tests and unit/build/Room/privacy gates, verifying the Manage navigation inset, modal wait and track selection. Latest committed main is `b8b1639`; working tree starts caption size/position controls in the separate SubtitleView with private persistent settings and accessibility/reset coverage. Local diff hygiene, 15 XML parses and 7 Python privacy/release tests pass; Android compile/test pending. User also wants automatic title-only TMDB posters without per-user setup, is currently non-commercial and prefers app-wide authentication. Official API auth requires a credential; no app key exists yet. Detailed limits and next actions are in D-037 and the work handoff. No signed APK, Reno10 acceptance or FPS claim.

## Current implementation update — 2026-11-21

CI #214 / 44961886974 passed 51/51 Android 15 tests and unit/build/Room v1-v4/privacy gates; Details and Glass-on emulator captures were inspected. The Compose transport compiles on main and all nine playback cases passed in #216/#218. The full-height Manage sheet remains under test: #216 reproduced a hidden Glass control after recreation; #218 began scroll gestures before the sheet heading appeared. The regression helper now waits for that heading before scrolling; next Actions run will verify. No OPPO performance evidence. User wants one signed test APK after all remaining features and a final efficiency review, with no interim APK.

Production navigation now uses a bounded Haze dock with a persisted effects-off fallback; the offline title hero can display only previously confirmed, app-private cached artwork. CI #214 validated all 51 Android 15 integration cases and build/schema/privacy gates. The current unverified change replaces native playback chrome with the Compose-owned accessible seek/play transport. No APK from this change, and no OPPO performance/thermal evidence. One integrated signed candidate remains deferred until all app features and the final efficiency review.

## Current implementation update — 2026-11-21

CI #203 / 44952898549 passed the navigation and playback-speed baseline: 50/50 Android 15 instrumentation tests, JVM/unit checks, debug app build and Room schemas v1-v4. Main has a bounded Haze Glass dock in production navigation with a persisted effects-off fallback. CI #210 (51 Android 15 cases) passed 46 and failed five new accessibility/scroll assertions. The current working tree corrects decorative poster semantics, explicit tab selection/click semantics, Glass switch On/Off announcement, and modal scrolling; it also adds a cinematic details hero with a cache-only offline fallback and a backdrop fetched only after explicit TMDB match confirmation. CI #211 compiled and ran 51 Android 15 tests, with 47 passing and four UI failures. Dock selection and series details now pass; card labels and the Glass switch-state test are being corrected. The latest local fix provides a localized title/type/year label on each card and checks switch announcement/persistence. CI #212 stopped at compilation because the Compose `role` extension was not imported; the import is corrected. CI #213 then compiled but 4 UI tests failed: switch semantics, description-based labels, selected-state timing, and metadata confirmation visibility. The current correction is pending CI #214. This is not full-screen Glass or OPPO performance acceptance. User requested one integrated APK after the app and efficiency review; v0.1.174 is only an older checkpoint.

## Current development status — 2026-11-21

Main includes an opt-in manual TMDB search and match flow: encrypted user token, fixed-origin bounded API client, explicit detail-sheet search, separate candidate confirmation, and offline Room cache. CI #115 / 44896086296 passed Android 15 UI/client tests and verification gates; no real provider request, signed APK or Release was produced. Room v4 parent persistence and local sidecar subtitle playback are now integrated; CI #132 / 44911456180 passed Android 15 41/41, JVM/build, Room schema and privacy gates. This was synthetic emulator evidence, not OPPO acceptance. The Glass Lab repair, subtitle delay, complete player polishing and target-device testing remain in progress. No new installable APK/Release was published. INTERNET permission is used only for the explicit opted-in flow; library scans/playback remain local. The latest user-installable Release remains signed v0.1.91. User's v0.1.79 folder/offline checks are confirmed; later UI/playback and target-device performance are unverified.

## Historical offline preview status — published

Latest signed Release remains [v0.1.91](https://github.com/Batmancpu/Player/releases/tag/v0.1.91), versionCode 91, APK SHA-256 `7c9ff8287405be9b1f607259608d84a256e5aed1fcc4cea24e3f9b882dd05e17`. The user confirmed v0.1.79 scanned four correctly named/year-parsed videos, retained the library offline and switched folders. Newer catalogue, playback and Glass preview remain untested on OPPO. CI #97, #98 and #99 passed 17/17 Android 15 tests and all build/schema/manifest/signing/certificate gates. Room v1/v2 are committed; v2 SHA-256 is `6d2a1dd422834f5cadf3e6c4292aed393bf2a784adc3cef1108ba9975c0bd4fe`.

Current main adds explicit Android backup exclusions for the legacy mode and Android 12+ cloud/device-transfer modes while retaining `allowBackup=false`. The focused Python suite passes 7/7; CI #99 / 43740997631 passed all 17 Android 15 tests, the complete Python suite, build/schema/manifest privacy, APK signature and permanent-certificate checks. VersionCode99 signed APK SHA-256 `29f3945c4c8aa5e1501e90a3f05ad5de7685d7acc543821bb3fc74e57f9296fb` is CI-only; no Release was published. No HTTP, credential or INTERNET permission was added. Android 16 QPR2 cross-platform iOS transfer is not claimed as excluded because Player has no real iOS counterpart identifiers. Phase 1 acceptance and target-device performance remain pending.

# Canonical project context and handoff

Last reviewed: 2026-11-21 (UTC)
Repository: [Batmancpu/Player](https://github.com/Batmancpu/Player)
Working title: **Player** (temporary; the user will rename it later)
Current branch: `main` (only branch; no open PRs)
Current state (2026-11-15): signed [v0.1.91](https://github.com/Batmancpu/Player/releases/tag/v0.1.91) is latest, versionCode 91, APK SHA-256 `7c9ff8287405be9b1f607259608d84a256e5aed1fcc4cea24e3f9b882dd05e17`. User reports v0.1.79 showed four correctly parsed video titles/years, retained the library offline and switched folders. Newer catalogue/playback/Glass preview remains untested on OPPO; no FPS result or phase acceptance. CI #99 / 43740997631 passed Android 15 instrumentation 17/17, full Python suite, build/schema/compiled manifest privacy, APK signature and permanent certificate gates. The Room v1/v2 snapshots remain committed. Signed versionCode 99 artifact SHA-256 `29f3945c4c8aa5e1501e90a3f05ad5de7685d7acc543821bb3fc74e57f9296fb` is CI-only, not a Release. Android legacy/cloud/device-transfer backup exclusions are verified for configured modes; no HTTP client exists. Historical snapshots below are superseded by this status and newest entries.

This is the durable handoff for future ChatGPT/Codex/Claude/other AI agents. It summarizes the project origin, requirements, decisions, references, user context relevant to engineering, and current state. Keep it accurate as the project evolves.

## 1. Origin and intended outcome

The user has been experimenting with AfuseKt (the media scraper/player application) and asked whether a drastically simpler application could be built. The desired app is not a streaming service or media server. It is a private, local media-library catalogue.

The original release repository the user had made available is [Batmancpu/InfuseKt-release](https://github.com/Batmancpu/InfuseKt-release). Inspection confirmed that it is a fork of [AttemptD/AfuseKt-release](https://github.com/AttemptD/AfuseKt-release), and its README explicitly says the repository publishes release builds and contains no application code. It must be preserved unchanged; do not attempt to implement the app there.

The user then created [Batmancpu/Player](https://github.com/Batmancpu/Player) as the new source repository. Its initial state contained only a short README. The name is temporary and must not be treated as the final brand or package identity.

The initial exploration identified [the-mskd-dev/Flux](https://github.com/the-mskd-dev/Flux) as a strong architectural/reference project: an Android local media library with Kotlin, Compose, Room, TMDB metadata, SAF folder scanning, artwork caching, and Media3. Flux is GPL-3.0-or-later. Its code has not been imported into Player. Any future copying/forking must respect the GPL and preserve notices. Flux should be audited before reuse; its current source supports broader/multiple sources and has more functionality than this app needs.

## 2. Non-negotiable product contract

**One folder, one local library, automatic matching, beautiful presentation, no media server.**

The user will select exactly one folder through Android's system folder picker. The app may recursively inspect that folder and its descendants only. It should find local movie and TV episode files, normalize filenames, identify content, retrieve posters/backdrops/metadata and show a browsable library.

The app must never:
- Stream or host media through our infrastructure.
- Upload video files, video contents, screenshots or complete folder listings.
- Send local file paths, folder paths, raw SAF URIs or full personal filenames to metadata services.
- Scan outside the folder the user selected.
- Modify, move, rename or delete source media.
- Add user accounts, cloud drives, Emby, Jellyfin, Plex, SMB, WebDAV, ads, subscriptions, analytics, telemetry or a Player backend.
- Local playback is now explicitly approved by the user (2026-11-14). It must use files inside the selected local folder and must not turn Player into a streaming service or server.

Metadata discovery does require an external provider. The initial provider is The Movie Database (TMDB). The app will send normalized search terms such as title, year and media type directly to TMDB over HTTPS and retrieve images from its CDN. This is not media streaming. Downloaded details/artwork are cached locally and remain visible offline. No custom proxy or server is wanted.

When internet is unavailable, existing content remains browsable. New titles can show an unmatched/pending-artwork state until the next online refresh.

## 3. Visual/product ambition

The user clarified that “Apple TV aesthetics” means much more than a dark theme or translucent bottom bar. The desired result is a fully art-directed, Apple-inspired interface:

- Cinematic, dark, content-first home/library with large and carefully composed movie posters and artwork.
- Artwork-led detail pages, blended hero backdrops, soft gradients/scrims, carefully controlled hierarchy and beautifully typeset metadata.
- iOS 27-inspired Liquid Glass for functional chrome: navigation, floating buttons, controls, transient menus, sheets and selected interactive elements. The video player's audio/subtitle selectors and subtitle-delay controls should use restrained Glass later, with a clean, uncluttered layout.
- Clear and regular glass treatments, source-backed blur, optical refraction, soft highlights, reflective hairlines, subtle shadows, and animated joining/morphing where it makes sense.
- Coherent Apple-inspired icon family, consistent glyph weights/optical sizes, and native-feeling typography/alignment/motion.
- Smooth spring transitions, interactive morphing and polished loading/empty/error/offline states.
- Poster artwork itself remains crisp and is not covered in unnecessary glass.

Apple's own HIG places Liquid Glass in the functional layer (navigation and controls), separate from the content layer. This is important: do not coat every movie card/poster in glass. Use standard material treatment for content surfaces and reserve glass for controls.

“Pixel-perfect” is a quality target, not permission to claim a literal pixel copy of Apple's proprietary implementation. Use publicly available references and original Android implementations; do not copy proprietary source, Apple logos, SF font files, or licensed artwork.

## 4. User's development environment and constraints

The user has no PC and develops using an Android phone and iPad. GitHub and GitHub Actions must be the build/test workstation; provide installable APK artifacts and release links only after verified builds.

Primary handset for real-device validation:
- OPPO Reno10 5G
- Device model CPH2531
- Android 15 / ColorOS 15
- MediaTek Dimensity 7050
- User targets stable 60-Hz behaviour and is sensitive to UI stutter, native crashes, battery drain and privacy.
- Do not presume we can control or inspect the handset directly. CI build success is not proof of device success. Ask the user to install a named artifact and supply logs/screenshots only when a real-device observation is needed.

The user strongly prefers small commits, one main branch, practical verification, no giant patches, no broad refactors, and no moving on while a build is failing. They expect the AI to maintain continuity and act as a technical owner: proactively monitor workflow runs, examine failures, fix root causes, record what happened and verify the fix.

## 5. Relevant existing user repositories inspected

### AfuseKt release fork
- [Batmancpu/InfuseKt-release](https://github.com/Batmancpu/InfuseKt-release)
- Parent: [AttemptD/AfuseKt-release](https://github.com/AttemptD/AfuseKt-release)
- Release-only repository; README says it does not contain application code.
- Preserve it. Player is the source project.

### FC Zenith
- [Batmancpu/FCZenith](https://github.com/Batmancpu/FCZenith)
- Kotlin/Compose music-player fork with a sophisticated Liquid Glass implementation and a performance-focused mobile UI.
- Inspected `app/src/main/java/com/music/zenith/ui/components/LiquidGlass.kt`, `LiquidGlassThumb.kt`, `GlassNavBar.kt`, `GlassSegmentedControl.kt`, `FrostedTopBar.kt`, the vendored `backdrop/` package, `SfIcons.kt`, and `GUIDELINES.md`.
- Its custom glass integration is tied to Zenith-specific application settings, backdrop locals, player/navigation state and vendored rendering code. Do not copy it wholesale.
- Useful observations: 0.33 backdrop resolution in the main glass modifier; full glass gated by platform support; lens path gated to API 33+; fallback paths for reduced dynamic blur/unsupported devices; reduced recomposition by reading animation state at draw time; no expensive per-frame state reads; component reuse; resource-backed UI strings; one icon registry; reduced-blur/reduced-animation/high-performance/low-RAM fallbacks.
- FC Zenith is the visual target/reference for material quality, not the media app source.

### CurrentLens / brrrNews
- [Batmancpu/CurrentLens](https://github.com/Batmancpu/CurrentLens)
- Inspected its README, current source tree, `LaunchCrashDiagnostics.kt`, startup path in `MainActivity.kt`, build workflow and commit `2475970123a2a2fb0e05be25d1d9a45061186c73` (“fix: harden launch, reduce feed jank and finish glass polish”).
- That hardening commit introduced on-device crash diagnostics and a recovery screen, added defensive reads around local preference/cache state, and removed recurring full-screen/bottom-band GPU luminance sampling that could synchronise GPU work during scrolling. Later small commits corrected product-name spelling. These are repository-verified changes.
- Separate historical user-reported incident: the user previously reported a native `SIGSEGV` on OPPO CPH2531 / Android 15, package `com.mangoloads.currentlens.stability`, version `0.2.2-stability`, after the log line “BrrrNewsApp composition started”. The complete native tombstone/root cause is not present in the current repository review. Treat it as a warning and an unverified incident, not as proof that one specific Compose or glass component caused it.
- Important technical distinction: a Kotlin uncaught-exception handler can persist ordinary Java/Kotlin crashes, but it cannot reliably catch a native SIGSEGV that terminates the process. Native crashes need tombstones/logcat/native crash evidence. Therefore Player should avoid JNI/NDK/native graphics in its initial implementation and prove GPU effects independently on the physical handset.

### brrrBoard / FrostKeys
- [Batmancpu/brrrBoard](https://github.com/Batmancpu/brrrBoard)
- Its README documents OEM-specific live-blur limitations, particularly on lower/mid-range Android devices and older APIs. It recommends fallback behaviour when blur is unsupported and warns that some devices may crash or render incorrectly.
- Reuse that lesson: no mandatory live blur, no hard dependency on native window blur, and no glass effect may block basic app use.

## 6. Candidate technical foundation

Flux is an architectural reference, not yet an imported codebase. The simpler Player application can use a conventional native Android app rather than inheriting Flux's KMP and multiple-source complexity.

Candidate components (versions must be pinned and verified in CI before adoption):
- Kotlin + Android Gradle Plugin + Jetpack Compose.
- Haze 2.1.0 as the preferred Glass proof of concept. Its official release (2 November 2026) stabilizes Glass, introduces `hazeGlassContainer` for morphing/joining nearby surfaces, improves fallback/accessibility and reduces some rendering overhead. It updates Kotlin to 2.4.21 and Compose Multiplatform to 1.12.2. Upgrade toolchain components in isolated commits.
- Coil 3 or equivalent for poster/backdrop loading and cache.
- Room for local media/metadata index; app-private storage for artwork cache.
- Android SAF / `ACTION_OPEN_DOCUMENT_TREE` with persisted read permission.
- Direct HTTPS TMDB API client; no proxy/server.
- Coroutines/Flow and WorkManager only where justified for incremental scanning. Do not introduce permanent background services.
- AndroidX Media3 is the first playback-engine candidate to evaluate; LibVLC is a fallback candidate if practical format coverage is insufficient and its native-library/licensing costs are acceptable. No engine has been selected or added yet.

Exact dependency versions, minSdk/targetSdk, Gradle/JDK versions, package ID, app branding, signing strategy and final licence must be confirmed in dedicated, build-tested commits. Do not guess these into a giant initial patch.

## 7. Metadata matching contract

For movies, normalize title and year from filenames; remove release-group, resolution, codec and container noise. For series, parse series title, season and episode from common patterns such as `S01E02`, `S01.E02`, `1x02`, `Season 1/02`.

Identify a series once and group episodes beneath the series/seasons. Do not issue a separate series search for every episode. Retrieve episode-specific details only after matching the parent series.

Use result confidence and allow manual correction. Store the user's chosen TMDB identity so later scans do not overwrite it with a different guess. Be conservative with ambiguous titles and duplicate remakes. Use bounded concurrency, rate limits, cancellation, retry/backoff and incremental reconciliation.

Only normalized title/year/content-type search information may be sent to TMDB. Never transmit full storage paths, URIs, directory names or media content.

TMDB's free developer API is for non-commercial use with required attribution; commercial use requires a separate licence. The app must include the required notice and approved TMDB logo within an About/Credits section, subject to current TMDB terms.

## 8. Roadmap and commit discipline

The detailed roadmap lives in `docs/ROADMAP.md`. Work in small, sequential commits on `main`. Each commit must be independently understood and verified. Keep changes narrow, with a target of 1–3 related files per functional commit. A dependency upgrade is its own commit. A test or docs change may be separate if the code change would otherwise become too large.

Do not report that a stage is done until:
- Its relevant unit/instrumentation tests pass.
- GitHub Actions completes successfully.
- The build artifact exists when the stage produces an APK.
- Real-device behaviour is marked separately and not inferred from CI.

The agent must update the canonical log and issue register as work proceeds. Preserve all failed builds and lessons; do not erase history after a fix.

## 9. Current implementation state and next action

At this document's creation:
- `Player` is the new source repository.
- The only initial project content was `README.md`.
- No Gradle project, Android source, dependency configuration, workflow, APK or app implementation has been committed.
- No code has been copied from Flux or FC Zenith.
- The next step is the repository bootstrap commit set, followed by a minimal Android/Compose build workflow. Establish a passing empty-shell APK before storage, metadata, or Glass work begins.
- The user's first acceptance target is not a mockup alone: a working installable app that demonstrates a real Liquid Glass system and then progressively gains local-folder scanning and metadata.

If the source state changes, update this section immediately. Never leave the next agent guessing whether a feature is planned, implemented, built, installed, or tested.


## 10. Additional visual-system licensing decision
The SF Symbols Compose library used by FC Zenith was reviewed. Its source/generator carries an MIT license, but its README says the symbol artwork and data remain Apple's property and points to Apple's SF Symbols license, which restricts use to Apple platforms. Player will not bundle those glyphs in its Android APK without explicit permission. The visual direction remains Apple-inspired: use a consistent, appropriately licensed symbol family or original vectors, then tune optical size, stroke, alignment, labels and animation. Never use Apple logos, SF font files or unlicensed symbol artwork.

## 12. Current verified implementation state — 2026-11-14

This section supersedes the original repository-bootstrap status above.

### Implemented in main
- Android Gradle app module: namespace/application ID com.batmancpu.player (provisional), compile/target SDK 37, min SDK 29, AGP 9.3.1 built-in Kotlin, Compose compiler plugin 2.4.21, Jetpack Compose BOM 2026.09.00.
- Edge-to-edge Compose launcher with transparent system bars.
- PlayerTheme.kt: dark cinematic palette, semantic text/surface tokens, system sans-serif type scale, spacing and corner tokens.
- PlayerGlyphs.kt plus four curated Lucide vector assets (home, clapperboard/movies, TV/shows, search). License is ISC with MIT-origin portions; complete notice is included in app assets and THIRD_PARTY_NOTICES.md. No Apple SF Symbols/SF fonts/logos shipped.
- Three JVM palette/contrast tests in PlayerThemeTest.kt.
- CI uses Node24-compatible action majors, JDK21, Android SDK 37.0, Gradle9.5.1, testDebugUnitTest, assembleDebug and APK upload.

### Verified CI
- Runs 2, 3, 5, 6, 7 and 8 passed. Run1 failed from the incorrect SDK package name and was corrected; see ISSUES_AND_FIXES.md.
- Run8: https://github.com/Batmancpu/Player/actions/runs/43562170409. It passed the theme tests, APK assembly and artifact upload. APK artifact ID 14100846293, size 10,386,150 bytes, download URL https://github.com/Batmancpu/Player/actions/runs/43562170409/artifacts/14100846293. ZIP digest is recorded in DEVELOPMENT_LOG.md.
- No APK has yet been installed/visually tested on the user's OPPO CPH2531. CI success is not device validation.

### Not implemented
Haze/Glass, folder selection, SAF scanning, filename parser, TMDB client/credential UI, Room database, image cache, library/detail/seasons screens, offline cache behavior and release signing are all planned, not implemented.

### Next agent action
Read AGENTS.md, inspect latest main and workflow runs, then add Haze 2.1.0 dependencies only (core, Glass and optional Material3 integration) in one focused commit. Run CI. Only then implement an isolated source-backed Glass lab using HazeInput.Sources and hazeSource. Do not enable HazeFeatureFlags.isPlatformBackdropEnabled; native window-backdrop rendering is experimental and not required for v1.

## 13. Haze integration decision
Haze 2.1.0 is now included as dependencies only (core, Glass, optional Material 3 integration). No Glass modifier or backdrop source has yet been implemented. Use the same version across all three artifacts. The first proof must use explicit HazeInput.Sources and hazeSource. Never enable the experimental native Android window-backdrop feature on the target Android 15 device. Haze docs specify that native backdrop is only eligible on Android 17/API 37.2+ and is experimental; source-backed rendering remains the selected route.

## 14. Haze dependency baseline verified — 2026-11-14
The app/build.gradle.kts now includes Haze 2.1.0 core, Glass and Material3 integration. CI run 10 passed with Gradle resolving the dependencies and producing an APK (artifact ID 14101256278). This proves dependency resolution and compilation only; no glass modifiers are yet in app code.
For the first Glass screen, use the Haze 2.1.0 stable API in package dev.chrisbanes.haze.glass: Modifier.hazeGlass and Modifier.hazeGlassContainer. The background content must register with dev.chrisbanes.haze.hazeSource(state), and Glass controls explicitly use HazeInput.Sources(state). Use GlassStyle.clear or GlassStyle.regular, begin with HazePerformanceMode.Default/Balanced, and remember styles rather than constructing new ones on every recomposition.
Keep HazeFeatureFlags.isPlatformBackdropEnabled false. Haze's native Android Backdrop renderer is experimental and only eligible on Android 17/API 37.2+, whereas the user's handset is Android 15. Source-backed rendering is the chosen safe path. Keep the first cluster small; do not put Glass on posters.

## 15. Shared Glass helper compile gate passed
The shared PlayerGlass.kt helper now compiles against Haze 2.1.0; CI run 13 passed and uploaded APK artifact 14101425761. The helper uses a RoundedCornerShape-compatible API, explicit HazeInput.Sources, remembered Clear style, Default performance, shared interaction source support and a tinted/outlined disabled fallback. It has not yet been used by a screen. Next is the isolated Glass Lab screen, followed by a separate launcher wiring commit. No phone/device test has occurred.


## 16. Glass laboratory created and verified — 2026-11-14
- Added ui/glass/GlassLabScreen.kt: local gradient hero and poster-like tiles, four connected Glass navigation items, selected state feedback and Glass-on/off fallback switch.
- Glass background source is local and explicit. No internet, storage permission, folder picker, TMDB or media file access is involved.
- CI run 15 passed and artifact 14101132751 was uploaded. The lab compiled but was not yet connected to MainActivity at that checkpoint.
- MainActivity is now being wired to launch this temporary lab. It is a development verification surface, not the final library/home UI.
- The user must install the next successful APK on OPPO Reno10 CPH2531 and provide screenshots/observations for optical and performance tuning. CI does not prove handset rendering.


## 17. Current launcher state — Glass Lab is now the temporary app entry

CI run 16 passed: https://github.com/Batmancpu/Player/actions/runs/43563620311. The app's MainActivity now launches GlassLabScreen rather than the introductory placeholder. APK artifact ID 14101590882 (11,025,438 bytes), download: https://github.com/Batmancpu/Player/actions/runs/43563620311/artifacts/14101590882. Artifact ZIP digest is in DEVELOPMENT_LOG.md.

The lab is development-only. It uses a local generated gradient backdrop and no user media, permissions or network. It has a 4-item glass navigation group, selected-state feedback and a glass enabled/disabled switch. The app has not been installed or visually validated on the user's OPPO Reno10 CPH2531. Physical-device Glass validation remains required before shipping or tuning production effects, but it no longer blocks the user-approved catalogue implementation. The immediate product work is now the plain, functional library foundation described in section 18.


## 18. User-approved scope expansion — local playback and subtitles (2026-11-14)

This section supersedes the earlier playback-deferred decision (D-010), the original catalogue-only product boundary and the previous immediate Glass-Lab-first next step.

### Product outcome

Player remains a local-first catalogue, but now also includes a built-in player. The intended flow is: select one local folder → scan its descendants → identify movies and TV episodes → fetch high-quality TMDB posters/backdrops/details → browse a polished Apple TV-like library → tap Play → play that same local file.

The player must aim for broad practical format compatibility similar to a VLC-class local player. “Every format” is not a testable or universally guaranteeable promise: container, codec/profile, subtitles, decoder installation and handset hardware all affect compatibility. A documented test matrix on the OPPO CPH2531 is required.

### Player requirements

- Local-file playback only, using the selected SAF tree. No media server, streaming, upload, remote protocol or cloud playback.
- Basic reliable play/pause, seek/progress, full-screen/orientation handling, audio focus and remembered playback position.
- Discover and select embedded audio tracks and embedded subtitle tracks.
- Load a local external subtitle sidecar/file.
- Search for and download an external subtitle from a provider while playback is open; provider selection and API/terms/privacy audit remain open.
- Adjust subtitle delay/synchronization in small and larger increments, show the current offset and reset to zero.
- Store downloaded subtitle files in app-private storage by default; do not write to or alter the selected media folder.
- Keep player chrome minimal. Audio/subtitle choices and delay controls should be compact sheets/menus; production Liquid Glass is applied later to functional controls only, never over the actual video or posters.

### Metadata and artwork requirements

The catalogue must use normalized filename parsing and direct TMDB lookup for title, year, media type, series/season/episode identity, posters, backdrops, synopsis and relevant metadata. It must cache results/artwork locally and remain browsable offline. TMDB credentials must not be hardcoded into a public APK; the credential UX remains an open decision. No file paths, full filenames, folder labels, SAF URIs or media bytes go to TMDB.

### Engine and subtitle-provider decisions

No playback engine or online subtitle provider has been selected or added. Evaluate AndroidX Media3 first against real local files and SAF URIs. Official Media3 documentation lists supported containers and standalone subtitle types, but sample-format support and actual decoding depend on the device/decoder. Evaluate LibVLC only if necessary and after native ABI/APK-size, stability and GPL/LGPL review. Do not claim universal format support.

### Reprioritized implementation sequence

1. Replace the Glass Lab as the default launcher with a plain functional app shell; preserve the lab as a development experiment.
2. Implement one-folder SAF selection, local-provider validation, persisted read permission and recursive read-only media/sidecar discovery.
3. Implement filename normalization/parsing and series/episode grouping.
4. Add Room persistence and the direct TMDB metadata/artwork pipeline with manual matching and offline cache.
5. Build the real poster library, detail pages, shelves and search.
6. Add a broad-format local playback engine and connect Play.
7. Add embedded/external subtitle and audio selection, on-demand subtitle download and subtitle-delay controls.
8. Integrate production Liquid Glass into navigation and player controls after functional and device gates pass.

**Immediate next implementation:** a plain, non-Glass onboarding/library shell that starts the single-folder SAF flow, persists the user's read grant and displays the selected folder/recovery state. Then add recursive scanning in a separate focused commit. The current GlassLabScreen remains available for later isolated testing but must not be treated as the catalogue.

## 19. Current source-verified state after the scope decision

- Repository: Batmancpu/Player, branch main; no open PRs or issues were returned during this review.
- Latest source implementation commit: 20e13b928b9c5ce75b64ecb219f795f7dd8bc67f (Glass Lab is wired to MainActivity). The latest repository commit at review time is the documentation-only state record 6a6875f333465daa9bd276e6fc9c6e9f621a5097.
- Latest source CI run 16 passed: https://github.com/Batmancpu/Player/actions/runs/43563620311; APK artifact ID 14101590882. The subsequent docs-only run 17 also passed: https://github.com/Batmancpu/Player/actions/runs/43563846274.
- The manifest currently has no Internet, media or broad-storage permission. MainActivity opens GlassLabScreen. App code contains no folder picker, scanner, TMDB client, Room database, poster loader, library/detail screen, Media3/LibVLC dependency or playback flow.
- The existing Haze 2.1.0 dependency and PlayerGlass helper are present; the isolated lab compiles. No physical-device installation, Glass performance or playback-format test is recorded.
- The user's prompt on 2026-11-14 is the authoritative approval to add local playback and subtitle/audio features. No online subtitle provider is yet approved.


## 20. Current implementation state after folder setup — 2026-11-14

- Latest source commit: 59216311c92b0619a9550651e9fc1539f749931d. MainActivity now opens FolderSetupScreen; GlassLabScreen remains a developer preview.
- Implemented in source: plain dark setup screen, Android system OpenDocumentTree picker, explicit allowlist for com.android.externalstorage.documents, persisted read-only tree grant, app-private URI preference, startup grant revalidation and stale-grant recovery. Replacing the selected tree releases the old grant after the new grant is saved.
- No manifest storage/media/Internet permission was added. This stage does not enumerate or read media files.
- Not implemented: recursive scan, sidecar subtitle pairing, filename parsing, Room, TMDB, artwork cache, catalogue/detail screens, playback engine or subtitle UI.
- Initial CI run 43569596301 failed because MainActivity imported rememberSaveable from the wrong package. The correction is committed in e20d8aa6bb193acac41414bd9e1f6b6b034ba13f; corrective CI run 43569710969 is pending. Do not call folder setup build-verified until the corrected run succeeds.
- OPPO CPH2531 real-device picker/provider and permission-revocation validation remains pending.
- Exact next task: implement and test a cancellable, read-only recursive scanner bounded to the persisted tree; discover video and sidecar subtitle documents, but do not read/transmit subtitle contents.


## 21. Folder setup CI correction handoff — 2026-11-14

The first folder-setup source commit (59216311c92b0619a9550651e9fc1539f749931d) did not pass Kotlin compilation. Workflow run 43569596301 reported only an unresolved import for rememberSaveable in MainActivity.kt. The import was corrected to androidx.compose.runtime.saveable.rememberSaveable in commit e20d8aa6bb193acac41414bd9e1f6b6b034ba13f. The corrected workflow run 43569710969 completed successfully; the subsequent context/roadmap handoff run 43569752178 also completed successfully. The source folder-setup compile gate is green. OPPO CPH2531 provider behaviour and permission-revocation tests remain pending. If successful, proceed to real-device validation of the accepted external-storage provider and folder permission recovery; only then implement the recursive scanner. The failure and correction are preserved in DEVELOPMENT_LOG.md and ISSUES_AND_FIXES.md.


## 22–23. Historical signing setup snapshots — superseded by section 24

These sections recorded the initial signing-scaffold state and a Termux-based key-generation proposal before the user asked the agent to create the key directly. They are retained in the earlier commit history but are no longer the current instructions. The Termux helper has been deleted. The permanent JKS now exists and has been delivered to the user as private files. For current key identity, secure backups, exact Actions secrets and package-ID implications, follow **section 24** and `docs/ANDROID_SIGNING.md`. Do not follow the old Termux steps or regenerate the key.

## 24. Permanent signing key generated; no Termux required — 2026-11-14

This section supersedes the earlier Termux-based proposal in sections 22–23 and in the first version of `docs/ANDROID_SIGNING.md`.

- The user rejected a Termux workflow and asked the AI agent to create the permanent signing key directly. This was explicitly done in the current ChatGPT session.
- A JKS keystore was generated with `keytool` in the session's file workspace, reopened and verified. It was delivered as a private downloadable file, with a separate base64 secret file, credentials note and public fingerprint file. None of those private files or password values are stored in this GitHub repository.
- Key alias: `player-release`; RSA-2048 / SHA256withRSA; 10,000-day validity. Public SHA-256 fingerprint: `62:A0:97:B7:BE:7D:22:A9:88:3D:9E:09:01:5C:04:17:72:72:16:D7:3E:25:06:ED:F4:70:B2:4A:61:CD:AD:C0`.
- User action still required: download and securely back up `player-release.jks`; save the password from `PLAYER_RELEASE_CREDENTIALS.txt` in Proton Pass; create the four Actions secrets documented in `docs/ANDROID_SIGNING.md`. The GitHub connector cannot write repository secrets. No signed Release APK exists until those secrets are configured and CI verifies the signature.
- Actions secrets: `PLAYER_RELEASE_KEYSTORE_BASE64`, `PLAYER_RELEASE_KEYSTORE_PASSWORD`, `PLAYER_RELEASE_KEY_ALIAS`, `PLAYER_RELEASE_KEY_PASSWORD`.
- The source applicationId remains `com.batmancpu.player`, intentionally provisional. User wants branding/package cleanup deferred until core functionality works. The existing signing key can sign a future applicationId without regeneration; a changed applicationId is a new Android package and will not update the old package in place.
- The initial Termux helper was deleted from the repository and its CI syntax step removed. Do not ask the user to install Termux or regenerate this key. Do not create a replacement. Refer to the already-delivered files and the user's Proton Pass/secure backup.
- Signing CI: scaffold run 25 (`43570229274`) passed while correctly skipping signed Release because secrets were absent. Run 27 (`43573474031`) passed after the helper-shell syntax fix. A later cleanup commit removes the obsolete helper/check; verify that cleanup's CI separately.
- Current exact next step: user saves the delivered files/credential securely, adds the four GitHub repository secrets, then trigger Android CI. On success, verify the CI-reported certificate fingerprint equals the fingerprint above. Continue functional development; do not block scanner implementation on a final brand or package-name choice.

## 25. User workflow and continuity preference — 2026-11-14

The user wants the AI to act as the sole implementation worker where connected tools allow, and explicitly prefers a direct, low-friction process over unnecessary setup scripts, Termux steps, device-side terminal work or over-engineered procedures. The user uses an iPad/Android setup and Proton Pass for password storage. Ask for user action only where account permissions or physical-device access actually require it (for example, entering GitHub Actions secrets).

The user also explicitly asked that the project retain a chronological Markdown conversation/handoff record so a new AI agent can resume with the decision history. `CHAT_HISTORY.md` is the accumulating project session log. Keep it updated at every session end, alongside `PROJECT_CONTEXT.md`, `DEVELOPMENT_LOG.md` and `ISSUES_AND_FIXES.md`. Record decisions and actions, never secrets.

## 2026-11-14 — User-approved six phone-verified phases and Obtainium release handoff

- User requested that one-folder access and offline file discovery/filename parsing/Room persistence be merged into Phase 1 rather than separate milestones.
- Adopted six deliverable phases recorded in docs/ROADMAP.md: (1) one-folder offline library; (2) TMDB identification and cached artwork; (3) cinematic real catalogue; (4) local video playback; (5) audio and subtitles; (6) polished Glass, QA and launch.
- At the end of each phase: pass CI and signing checks; publish a distinct Release APK on GitHub Releases with changelog; user tests on OPPO CPH2531; record evidence and gate acceptance before next phase.
- User intends to track released APK updates through Obtainium. Preserve existing permanent signing key and stable package identity and monotonically increase versionCode. Never use a debug APK as install/update channel.
- The canonical instructions for a future ChatGPT Work agent are docs/WORK_HANDOFF.md and AGENTS.md; all product history remains in existing repo Markdown.
- Checked main 95881e4 and CI #36 passed. GitHub Releases was empty; signed release steps were skipped because Actions signing secrets were not configured. No signed installable APK can be claimed now.
- Next: implement Android SAF adapter + scanner list UI and build gate, then parser, Room, incremental reconciler. Add manual signed release publication workflow; user must configure four signing secrets before publishing.

## 2026-11-14 — Six-phase Work handoff and Phase 1 code progress

- User explicitly requests that each phase culminate in an installable, permanently signed APK in GitHub Releases for Obtainium, and that the phase is not marked complete until the OPPO handset installation/test is verified.
- Phase 1 merges one-folder local SAF access, nested video scanning, filename parsing, a Room offline index and incremental rescans. Later phases cover TMDB artwork/metadata, cinematic catalogue, local playback, audio/subtitles and production Liquid Glass/performance.
- New canonical docs/ROADMAP.md replaces ten-phase delivery order; docs/WORK_HANDOFF.md contains session continuation and operational rules for ChatGPT Work.
- Implemented after old main 95881e4: scanner Android SAF adapter, manual scan/cancel UI, local filename parser with tests, Room/KSP dependency setup and offline Room index, lazy grouped video list. Room only deletes stale rows in an atomic transaction following a completed scan; reads/writes are app-private only.
- CI: #37 docs plan passed; #38 Android SAF UI passed; #39 manual publication workflow passed normal CI; #40 parser passed; #41 Room deps passed; #42 offline DB passed; #43 lazy grouped list passed. Latest tested code commit: d3ecc17c0036f4ef2f5db036c970d9c810b2f206. CI evidence https://github.com/Batmancpu/Player/actions/runs/43582079994
- NOT verified: real device, provider compatibility, large-library scrolling, selected folder restricted access, permission-loss handling, manual release publication signing branch, full parser corpus or Room migrations.
- GitHub Releases was empty and no signed Release APK existed at review; user still needs to provide existing key through GitHub Actions secrets. Do not create a new signing identity, do not publish debug APK and do not claim a phase test happened.
- Next action for AI: strengthen Phase 1 acceptance tests and review actual provider behavior when the user has an installed signed APK. Next action for user: add the four existing release secrets as described in docs/ANDROID_SIGNING.md; no key/password material in chat.

## 2026-11-14 — Explicit signing reset before the first published release
- The user explicitly requested: discard the old unreleased key identity, generate a new key, and begin adding repository secrets without extra terminal steps. GitHub Releases was confirmed empty at that moment. No signed-release APK existed in the repository; no claim is made about unverified sideloaded APKs.
- A new JKS release keystore and private Base64/credentials files were generated in the current ChatGPT session and delivered privately for download. Current public fingerprint: `4D:2F:17:89:ED:27:D3:96:13:35:FE:36:45:64:55:BD:9A:31:0B:0D:CD:F4:1E:8C:CC:92:53:98:33:9E:80:BF`, alias `player-release`, RSA-3072. No secret content was committed.
- This new identity supersedes the old fingerprint and instructions in historical signing notes. CI and docs/ANDROID_SIGNING.md now pin the NEW fingerprint. After the first signed install, retain this key for updates. Only the owner can configure the four GitHub Actions secrets; never paste their values in chat.
- Next user action: save the new files and copy the Base64 file content into `PLAYER_RELEASE_KEYSTORE_BASE64` in GitHub Settings. Then add the other three secrets (password, alias and key password). Signed publishing must not proceed until all secrets are saved and CI validates the new certificate.

## 2026-11-14 — FIRST signed GitHub Release: v0.1.51
- **Verified public release:** https://github.com/Batmancpu/Player/releases/tag/v0.1.51
- **Direct APK asset:** https://github.com/Batmancpu/Player/releases/download/v0.1.51/Player-release-51.apk (24,869,875 bytes).
- **GitHub Actions run #51:** https://github.com/Batmancpu/Player/actions/runs/43584487865 — completed SUCCESS. Automated tests/debug build, signed Release assembly, APK signature verification, pinned certificate SHA-256 validation and GitHub Release publication all succeeded.
- Release commit: b2d766132438f94a2e4bbe3add9bff6aad53622d. Android package `com.batmancpu.player`, versionCode 51, versionName `0.1.51`. Certificate fingerprint is in docs/ANDROID_SIGNING.md.
- This is a **test release of incomplete Phase 1**. No OPPO CPH2531 installation, folder-permission or actual Obtainium update has been verified. Do NOT mark Phase 1 accepted.
- **Next user action:** add https://github.com/Batmancpu/Player to Obtainium, install signed v0.1.51, and report that it installs and launches. Do NOT publish a second test release until first install is confirmed; then publish a higher run-number signed release with same app ID/certificate to test an in-place update without uninstalling.
- Secret diagnostic history: run #47 failed from missing separate key-pass secret; workflow now derives it from store pass for this JKS. Run #49 identified an incorrect non-secret alias and CI now pins `player-release`. Run #50 successfully signed APK but custom certificate comparison expected an obsolete apksigner output prefix. Run #51 changed the parser to match current output and completed full publication. All secret files remain private, never in source control.

## 2026-11-14 — SECOND signed Obtainium test release published (v0.1.53)
- User reported that the first signed Player v0.1.51 was installed through Obtainium. This report confirms first installation, **not** yet an in-place update.
- User explicitly requested a second release to verify update continuity. A focused `release(test):` commit `52d9a579a8bd7121ff85926ca0424e0c926dd1a7` triggered GitHub Actions #53.
- **Verified CI #53 success:** https://github.com/Batmancpu/Player/actions/runs/43585394549 — JVM/debug verification, signed release APK assembly, signature validation, pinned certificate fingerprint validation and GitHub Release publication all passed.
- **Verified second release:** https://github.com/Batmancpu/Player/releases/tag/v0.1.53
- **Verified APK asset:** https://github.com/Batmancpu/Player/releases/download/v0.1.53/Player-release-53.apk (24,869,879 bytes).
- versionName 0.1.53, versionCode 53, applicationId `com.batmancpu.player`, same pinned release certificate as v0.1.51; versionCode 51 -> 53 strictly increases. Only release documentation changed between test builds; no application feature/permission change.
- **Next exact user action:** In Obtainium refresh/check Player updates and select Update to v0.1.53 without uninstalling v0.1.51; confirm installed version changed and any previously selected folder/app data persisted. Obtainium may install silently where allowed, or Android may ask for user confirmation. Enable background update checks as desired.
- **Unverified until user reports:** actual Android package update in-place (rather than reinstall), app-data preservation, background automatic installation and the broader Phase 1 feature acceptance. Do not claim these tests are done yet.
- Codex next: after user confirms update, continue functional Phase 1 work from docs/ROADMAP.md, docs/PHASE1_PHONE_TEST.md and this handoff; ensure all future signed APK releases keep the same package/cert and increasing versionCode.

## 2026-11-14 — Final design/performance/privacy Codex handoff
- User requests exceptional original premium Apple Liquid Glass-inspired UI, accessible interactive glass sliders/controls, real measurable smoothness on OPPO Reno10 5G CPH2531 (Dimensity 7050/Mali-G68 MC4, up to 120 Hz), internet research of Apple official HIG/GitHub code and maximal on-device privacy.
- Added `docs/DESIGN_SYSTEM_AND_PERFORMANCE.md` (60-Hz ~16.67ms goal, optional 120-Hz ~8.33ms, benchmarks/fallbacks, explicit per-screen Glass surfaces). Added `docs/UPSTREAM_AND_PRIVACY.md` (licensed/pinned GitHub code reuse, telemetry/security audit, one-tree read-only SAF, offline Phase 1, opt-in normalized TMDB/subtitles, current allowBackup=false).
- Reconciled `docs/ROADMAP.md`, `docs/ARCHITECTURE.md`, `docs/DECISIONS.md` and `docs/WORK_HANDOFF.md`; `AGENTS.md` is mandatory entry point and must receive a dated concise context update **for every subsequent change**, with detailed `DEVELOPMENT_LOG.md` and additional file updates as needed. Only Codex tasks/short-lived PRs should change code; main is release source.
- Current app code is unchanged by this documentation handoff; no new performance benchmarks/real device UI tests were run. The confirmed v0.1.51 → v0.1.53 Obtainium update/signing identity remains in place. Finish Phase 1 and phone-test before subsequent features.
- Research references and implementation requirements are in the two new documents; do not reinvent mature code or blindly copy from unlicensed GitHub repositories. Never add Apple proprietary assets, excessive Glass, secret exposure, outbound media, unreviewed network hosts or broad storage permissions.
- Docs commits include a4317c8 (new specifications), 3649e7f (agent and Work handoff), 07e1dcc (roadmap, architecture and decisions). CI validation of latest documentation updates pending when recorded. Next engineer action: read docs and implement next focused Phase 1 regression test/change with AGENTS ledger update.


## 2026-11-14 — Phase 1 lifecycle reliability
LibrarySession/LibraryViewModel now retain and serialize cache/scan/save state across screen recreation. Stale operations cannot overwrite newer state; folder cache activation is transactional. Six deterministic JVM scenarios added, with PR #2 unit/debug CI passing. Full OPPO recreation/Room/provider checks remain unverified; next H-010 grant/root recovery and H-011 query cancellation/bounds. Rough planning estimate remains 10–15% overall, Phase 1 about half implemented; zero phases have physical-device acceptance. These estimates are not measured completion or acceptance scores.

## 2026-11-14 — Phase 1 signed candidate preparation
H-010 grant/root recovery, H-011 cancellable/bounded scanning and conservative parser/plain library states/grouping are now integrated after passing PR gates. Android 15 emulator suite passed 9 cases covering Room, cursor helper cancellation/cleanup, manifest privacy and launcher recreation. Release preparation adds actual APK privacy/identity/debug-provider assertions and runtime attribution. No new Release yet at authoring; next verify signed gate and trigger candidate publication. Phase 1 acceptance awaits user's real OPPO report; no FPS claim. Existing signed Obtainium update chain is unchanged. User authorized sustained work and two scheduled resumptions from latest GitHub state.

## 2026-11-14 — Published candidate and main-only rule (current)
Signed Phase 1 candidate v0.1.79 is published at https://github.com/Batmancpu/Player/releases/tag/v0.1.79 (source f1d7f27, versionCode 79); CI 43618442469 passed unit/Android integration/build/privacy/signing/publication. Actual OPPO acceptance remains pending. User explicitly requires main-only normal work (at most one genuinely needed backup), superseding earlier task branches. All eight previous task heads are fully merged; safe deletion via temporary Actions cleanup is pending. Both scheduled continuation prompts updated to main-only; second run remains 02:14 UTC Nov 15. No production Phase 2, actual device test or FPS claim.

Branch cleanup completed: CI 43642157896 succeeded, GitHub API confirms only main. One-time workflow removed. All work/releases preserved; no manual deletion needed. Latest README/handoff now identify v0.1.79 as the signed phone-test candidate; Phase 1 acceptance still awaits actual OPPO report.

## 2026-11-15 — Unreleased Phase 1 thread-boundary maintenance
Main now prepares H-014: whole SAF traversal/index entry points dispatch to IO, record mapping cooperates with cancellation, and an Android main-caller 1,000→100 reconciliation case is added. CI pending at authoring. This is not in released v0.1.79 and does not establish FPS; that signed candidate still awaits actual OPPO acceptance. Main-only rule and release identity unchanged.

H-014 main verification completed: 57e4539 passed CI 43667022100 (#82), 10 Android integration cases including 1,000→100 main-caller reconciliation plus unit/build/signing/privacy. No new Release; v0.1.79 remains the physical-phone candidate and actual OPPO acceptance is pending. Both scheduled one-time continuations have fired; main-only workflow remains in force.

## Latest phone report and broader preview — 2026-11-15
User confirms v0.1.79 selection/offline cache/folder switching; cancellation feedback unclear and Glass Lab nav visually broken. User requests larger useful upgrade and explicitly points to Batmancpu/FCZenith/immediate rezapranata089-bit/Zenith upstream. Research identifies Kyant0/backdrop Apache renderer and GPL app glue, not copied. Proceed with larger local-only catalogue/search/details/basic foreground Media3 playback and bounded lab dock; no online metadata/internet permission. Physical acceptance and real performance remain incomplete; do not describe later phases as complete.


## 2026-11-15 — Explicit Android backup exclusions
The current source retains `allowBackup=false` and now defines legacy Android 11-and-lower exclusions plus Android 12+ cloud/device-transfer exclusions for every documented app-data domain. Focused Python regression tests pass 7/7. CI #99 passed 17/17 Android15 tests, source-rule tests, release build/compiled manifest privacy, signature and permanent certificate. Its signed versionCode99 APK is CI-only; no Release was published. No runtime behavior, permission, network access, Room schema or Release changed. Android 16 QPR2 cross-platform transfer needs a real iOS counterpart and identifiers; none are invented. Next continue credential-free opt-in/destination-policy tests; keep HTTP/credential storage separately gated. Phase 1 still awaits OPPO acceptance.


## 2026-11-15 — CI #99 backup/privacy verification
CI #99 / 43740997631 completed successfully. Android 15 instrumentation reports 17 tests, zero failures/errors/skips. Full Python tests, unit/debug/release builds, Room v1/v2 schema validation, compiled manifest checks for backup disabled/both rule references/no unexpected permissions, APK signing and the permanent certificate check passed. Signed versionCode 99 APK SHA-256 is `29f3945c4c8aa5e1501e90a3f05ad5de7685d7acc543821bb3fc74e57f9296fb`; it is a CI artifact only, not a GitHub Release. H-023 is resolved for configured Android modes; real OEM transfer behavior remains untested. Next: continue credential-free opt-in/destination-policy tests before any HTTP or persisted credential. OPPO acceptance remains pending.


## 2026-11-21 — Final APK cadence
The user wants a single integrated signed APK only after the complete app is ready, with no routine phone install pauses during development. CI remains the verification gate; normal development no longer uploads debug APK artifacts or prepares release signing. Explicit candidate builds use the permanent certificate and publish only at the final phone-test milestone. No OPPO acceptance or performance claim is implied.

## 2026-11-21 — Room v3 provider metadata foundation
A focused Phase 2 database slice is on main: cached TMDB metadata is keyed separately from SAF identity and is exposed only after a user-confirmed mapping. CI #102 / 44888106152 passed the explicit v1-to-v3 migration, cache tests, Android 15 suite 20/20 and generated-schema validation; the exact v3 JSON is committed (SHA-256 a48bab777b7dcad1f02f0e61a1a936b95f91eca61265a1ede03d7cccbc4450db). This is not a network-enabled or user-facing metadata feature yet. No candidate APK was produced; publish only when one complete integrated app is ready.


## 2026-11-21 — Explicit library removal
Added “Remove folder from Player” with confirmation, Room index/cache cleanup and persisted SAF grant release; original files remain untouched. CI #104 / 44890135563 passed Android 15 20/20 and unit/build/schema gates; unit tests cover grant release ordering and refused release. A follow-up also clears resume points and adds confirmation/cancel UI coverage; CI #105 is pending. No intermediate APK is produced. CI #103 has verified the Room v3 snapshot follow-up (20/20 Android 15 tests, build/schema checks).


## Current continuation state — 2026-11-21
CI #115 / 44896086296 passed the metadata client and Credits screen. Main now includes the fixed-host client plus explicit manual search/candidate confirmation and offline Room cache; the integrated UI/permission/test change is pending CI. The token remains Keystore-encrypted in no-backup app-private storage, with consent off by default and clear/disable fail-closed. No APK/Release was published. Next: complete bounded artwork cache/rendering. The six-phase app, OPPO acceptance and device performance remain unfinished.


### TMDB attribution milestone — 2026-11-21
The About/Credits screen displays TMDB's approved logo and exact required non-endorsement notice. The bounded client and explicit lookup are now integrated; automated transport tests use synthetic data only. No live provider request, APK or Release has been made. Artwork cache/display and actual OPPO behavior remain pending.


### Credits compile follow-up — 2026-11-21
CI #110 / 44894615964 reported a malformed quote in the Credits button callback and an unresolved verticalScroll import. Both source fixes are on main; CI #111/#112 are verifying. No user-installable APK or Release was produced.


## 2026-11-21 — reference and release cadence update
User requested a single installable APK after the integrated app is ready, not per-slice. CI #122 passed cached poster cards; no Release. The user's FCZenith URL now redirects to Batmancpu/brrMusic and both it and immediate upstream rezapranata089-bit/Zenith have GPL-3.0 licenses. Player uses them as visual reference only, no code/assets reuse. See docs/UPSTREAM_AND_PRIVACY.md. Current follow-up validates disabled/offline artwork cache and unsafe-route rejection; CI pending.


## 2026-11-21 — CI #123 and UI test stability
Artwork behavior is verified by CI #122 and #123 attempt 2. CI #123 attempt 1 hit a transient Compose test timeout in unbounded waitForIdleSync; attempt 2 passed unchanged. This change replaces global idle waits with bounded accessibility polling and corrects obsolete catalogue copy. Follow-up CI pending; no installable APK/Release.

## 2026-11-21 — Playback completion starts cleanly on reopen
- Fixed stale EOF resume state: natural completion checkpoints position 0 and false play intent, and restores player controls.
- Added completedPlaybackResetsResumeCheckpointToBeginning Android instrumentation coverage with the owned synthetic fixture. CI result pending.
- Next: validate the main CI run and continue Phase 5 subtitle controls; Reno10 playback evidence remains uncollected.

## 2026-11-21 — Completion checkpoint follow-up
- Guarded against the periodic timer overwriting the end checkpoint: all save paths return position zero and false intent while Media3 is ended.
- The synthetic instrumentation test now spans the five-second timer interval; helper assertions cover end, ready and buffering state. Await CI before continuing.

## 2026-11-22 — Glass settings regression test diagnosis
CI #219 ran 52 Android 15 tests with 51 passes. Its sole failure was test-helper logic: the snapshot showed the production Glass toggle on-screen and checked, but an unnecessary generic swipe returned false. The helper now awaits the accessible switch and asserts visible bounds/state directly, while retaining localized state and preference-persistence checks. Full CI is pending. Continue to one integrated signed APK after feature completion and final efficiency review; do not infer OPPO performance or acceptance from CI.

## 2026-11-22 — Glass regression resolved; transport visual QA added
CI #220 / 44965653928 passed 52/52 Android 15 tests, JVM/build and Room/privacy checks. The Glass Manage regression is resolved. Synthetic CI captures still lack the production player transport, so the active synthetic playback test now captures `player-controls.png`; inspect the next artifact before considering player presentation visually reviewed. README now matches current main capabilities and Glass scope. No signed release or handset performance data was produced.

## 2026-11-22 — Player screenshot review and follow-up
CI #221 / 44966329372 passed 52/52 Android 15 tests, JVM/build/schema/privacy gates and uploaded `player-controls.png`; this is synthetic emulator UI only. Inspection found cramped/plain controls and a black video surface in that capture; keep presentation and actual decoder evidence open. The prior `glass-on.png` showed lab artwork obscured by the dock. Source now adds a caption-cue delivery assertion and reserves viewport area above that dock, with scroll regression; CI #222 pending. Subtitle delay remains deferred: stable Media3 1.11.1 is pinned and current docs mark the 1.12 offset builder unstable.

CI #222 / 44967004412 ran 53 Android 15 tests; the only failure was the new SRT cue callback test, which had not explicitly selected the discovered sidecar track. The Glass Lab dock-clear scroll regression passed. The test now explicitly selects the supported track, returns playback to cue start, then awaits the callback. Unit/build/schema verification was skipped after the UI test failure; CI #223 will run the full suite.

CI #223 / 44967664631 passed the explicit local subtitle selection/cue test; its one failure was a shell-injected video tap that did not restore auto-hidden controls. The video surface now exposes an accessible activation, and verbose player header labels are replaced with compact custom icons and accessible state descriptions. CI #224 pending.

Commit `1c19a08` pushed the player surface accessibility/header change to main. Android resource XML parses and `git diff --check` passes locally. CI #224 / 44968650641 is currently running. README's original concept artwork now shows a generic library/player presentation, with an explicit note that it is a visual direction image and not a literal shipped screenshot.

CI #224 completed with failure before instrumentation: Android test compilation reports `awaitNode` unresolved in `LocalPlaybackAndroidTest`; production code compilation passed. Added the helper in source. README/artwork-only commit `8b3eeca` triggered #225 against the same code, so #226 is the first run that can validate the correction.

Commit `91aabf2` replaces platform track dialogs with a Compose audio/subtitle sheet. It exposes decoder-supported track labels, selected radio semantics, Automatic restore and Subtitles off. The generated SRT/AAC test switches between subtitle Off, Automatic, local SRT and supported audio selection. CI #227 is pending; online subtitle retrieval and delay remain out of scope until reviewed/stable.

CI #227 / 44970509938 compiled product and test sources, then ran 53 Android 15 tests with one failure: the subtitle-off accessible node was not clickable. The issue was a semantics/action-node split. Source now combines radio role, label, checked state and activation in one `clearAndSetSemantics` node; CI #228 is required to verify all Off/Automatic/SRT/AAC interactions.

CI #226 / 44969253993 passed the clickable-surface correction: 53 Android 15 instrumentation tests, unit/build, Room v1-v4 schema and privacy checks all passed. The `player-controls.png` capture shows the movie title no longer truncated after the compact icon row; its black video area is synthetic and does not prove device codec output.

## 2026-11-21 — Playback completion test correction
- CI #180 compiled and ran 47 tests; the only failure was a 20-second wait against a 30-second video fixture.
- The synthetic test now seeks to 29 seconds before playback and continues through the five-second resume timer. Await CI.

## 2026-11-21 — Completed playback checkpoint verification
The synthetic near-end playback regression test and delayed periodic-save assertion passed in Android 15 CI #182 / 44949435610 (48 instrumentation tests total). Unit tests, build and Room v1-v4 validation passed. Earlier CI #180/#181 failures were due to the 30-second fixture exceeding the 20-second wait; #181 additionally hit a Pixel Launcher ANR. Release-only privacy/signing steps were skipped for this development push.

## 2026-11-21 — Functional catalogue destinations
Production navigation now switches among Home shelves, movie results, series results and focused local Search, all backed by the indexed local catalogue. Android 15 tests are pending; Glass remains lab-only pending final production design/performance integration.

## 2026-11-21 — Navigation test helper
CI #184 / 44950921953 ran 49 tests with one assertion failure: duplicated "Movies" text made the helper inspect the shelf heading, not the bottom tab. The actual category filter assertion passed. The helper now scans all matching semantic labels and ancestors. Rerun pending.

## 2026-11-22 continuation checkpoint
CI #228 / 44971068059 failed at Kotlin compilation: `role` is not writable in the pinned Compose `clearAndSetSemantics` receiver. The current uncommitted correction preserves the radio role via `selectable` and uses merged semantics for label, selected state and accessibility click. A new Home catalogue feature hero is also in progress; it renders only cached app-private artwork or a local deterministic gradient, and its Play/Details callbacks use existing flows. Android coverage is added but not yet validated. User requests a single integrated APK after all planned features and final efficiency review; do not publish incremental APKs or infer handset performance from CI.

CI #229 / 44971780524 verified app-source compilation but failed Android test compilation on undefined `awaitText` in new hero coverage. The source now calls the local bounded `text()` helper. Full automated validation is still pending; no interim APK.

## 2026-11-22 — CI #230 follow-up
CI #230 / 44972167368: production sources compiled, 53 Android tests ran, 48 passed and 5 failed. Failures revealed Home hero viewport crowding/duplicate CTA text and a picker label node not bound to the radio action. Current source reduces hero min-height to 188dp, gives the featured CTA “Play”, fixes test click semantics, and reorders picker modifiers so selectable owns merged label/state. Revalidation pending.

## 2026-11-22 — CI #231 follow-up
CI #231 ran 53 Android tests: 51 passed, 2 failed. Compact hero fixed prior Home/series/metadata failures. Remaining picker error was non-clickable matched label semantics; use explicit clearAndSet semantics and import `androidx.compose.ui.semantics.role`. Poster-ratio assertion saw a card clipped by the floating dock; test now scrolls Home first. Full CI pending.

## 2026-11-22 — CI #232 follow-up
CI #232 / 44973573949: 51/53 Android tests pass. Track option click exists but selected semantics are stale; implementation now mirrors `Player.Listener.onTrackSelectionParametersChanged`. Poster ratio remains clipped after a swipe; test now scrolls the largest accessible container, captures the scrolled home and reports measured bounds. Verification pending CI #233.

## 2026-11-22 — CI #233 compile correction
CI #233 / 44974218075 stopped at app compile: `onDispose` is a `DisposableEffectScope` member, not importable as a top-level symbol. Removed the invalid import; no Android tests ran. CI #234 pending.

## 2026-11-22 — CI #234 follow-up
CI #234 / 44974536206: app compiled, 51/53 Android tests passed. Picker click succeeds but selected node is absent; test now first asserts Media3's text-track-disabled state. Poster bounds remained at dock-clipped Home coordinates, so portrait check moves to the full Movies destination instead of relying on a no-op scroll action. CI #235 pending.

## 2026-11-22 — CI #235 follow-up
CI #235 / 44975039271 compiled; 51/53 tests passed. The new diagnostic Player read was on the wrong thread; test corrected to main. Movies poster geometry passes. Glass preference helper now scrolls to the fully visible switch rather than finding its offscreen semantic node. Revalidation pending CI #236.
