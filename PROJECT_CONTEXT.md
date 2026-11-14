# Canonical project context and handoff

Last reviewed: 2026-11-14 (Asia/Kolkata)
Repository: [Batmancpu/Player](https://github.com/Batmancpu/Player)
Working title: **Player** (temporary; the user will rename it later)
Current branch: `main`
Implementation state at this document's creation: **documentation bootstrap only; no Android source/build has been imported or implemented yet.**

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
