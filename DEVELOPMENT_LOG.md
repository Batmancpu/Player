# Development log

This is the chronological record of work actually performed. Planned work belongs in `docs/ROADMAP.md`; never record a plan as completed implementation.

## 2026-11-14 — Project bootstrap and source reconnaissance

### User goal captured
- User wants a very simple local media-library app inspired by AfuseKt/Infuse-style poster scraping.
- User selects one folder only; the app finds movie/series files, retrieves matching posters and metadata, and displays a high-fidelity Apple TV / iOS 27-inspired Liquid Glass interface.
- No streaming through Player's servers or any other media-hosting service. No media upload.
- User expects AI agents to act as technical owners, make small GitHub commits, inspect workflow outcomes, correct defects, and keep a durable handoff so development can continue after chat context is lost.
- Repository name `Player` is temporary; user may rename later.

### Repository facts verified
- `Batmancpu/InfuseKt-release` is a release-only fork of `AttemptD/AfuseKt-release`; its README states it does not contain app source code. Preserve it unchanged.
- User created `Batmancpu/Player` for the new source project. At inspection it was a public repository on `main`, with only an initial README.
- `Batmancpu/FCZenith` contains the user's preferred Liquid Glass reference, including a custom Backdrop engine integration and performance/accessibility guidelines.
- `Batmancpu/CurrentLens` includes local crash diagnostics/recovery and defensive local-store handling in commit `2475970123a2a2fb0e05be25d1d9a45061186c73`. It also removed recurring GPU luminance readback from the glass contrast path to reduce synchronisation/jank.
- `Batmancpu/brrrBoard` documents Android OEM/device limitations for live blur and the need for fallbacks.
- `the-mskd-dev/Flux` is a promising local-media app reference with SAF scanning, TMDB metadata, Room persistence and Compose. It is GPL-3.0-or-later; no code has been imported.

### Technical research verified
- Android's official Storage Access Framework uses `ACTION_OPEN_DOCUMENT_TREE` to grant access to a selected folder tree and its descendants; it does not grant unrestricted access to the rest of storage. Some locations/providers have Android-version-specific restrictions.
- TMDB's official FAQ says free API access is for non-commercial use with attribution and requires the notice: “This product uses the TMDB API but is not endorsed or certified by TMDB.” Commercial use requires a separate licence.
- Haze 2.1.0 was released 2026-11-02. Glass is stable and supports merging/morphing nearby surfaces via `hazeGlassContainer`; it updates Kotlin to 2.4.21 and Compose Multiplatform to 1.12.2. It remains a candidate pending toolchain compatibility build and handset testing.
- Apple's public Human Interface Guidelines specify that Liquid Glass belongs in the functional layer (controls/navigation), not over the content layer. This informs the decision to keep posters crisp and make controls glass.

### Known device incident captured (not fully root-caused)
The user previously reported CurrentLens 0.2.2-stability crashing on OPPO CPH2531 / Android 15 with native `SIGSEGV` after “BrrrNewsApp composition started”. The current source review did not provide the exact tombstone/root cause. A Kotlin exception handler is not a reliable catcher for native SIGSEGV. Player must avoid native/JNI/NDK features initially and validate all Glass effects on the real device.

### State at end of this entry
- Documentation/requirements captured.
- No Android application code or build workflow exists yet.
- No APK has been built or installed.
- No device-side tests have been run.
- Next: commit architecture/decisions/roadmap/device-test instructions, then bootstrap a minimal Android/Compose app and CI in small commits.

## Logging rules for future entries

For each completed commit, append:
- Date and commit SHA/link.
- Exact paths changed.
- Why the change was necessary.
- Behaviour before and after.
- Tests/build commands and actual outcomes.
- GitHub Actions run URL and artifact where applicable.
- Device tested or explicitly “not yet device-tested”.
- Risks, regressions, and next action.

For every failure, record the first failing run, useful error excerpt, root-cause confidence, correction commit and regression verification. Do not delete failed attempts from this record.


## 2026-11-14 — Architecture, roadmap and device playbook committed

Documentation commits:
- 2b3a44b — docs/ARCHITECTURE.md: native Android layer boundaries, SAF single-folder boundary, metadata/network boundary, stable document identity, scanning rules and Glass architecture.
- 51019ab — docs/DECISIONS.md: accepted product constraints, proposed Haze/Android choices, open API-credential/provider/package decisions and rejected approaches.
- 5a0ec0e — docs/ROADMAP.md: 73 small planned commits across build foundation, design system, Glass proof, SAF, metadata, persistence, screens, hardening and release. These are planned, not completed.
- ef43767 — docs/DEVICE_TESTING.md: CPH2531 test matrix and native crash triage playbook.
- 3990cd4 — ISSUES_AND_FIXES.md: historical CurrentLens SIGSEGV and live-blur OEM risk; root cause explicitly marked unverified.
- 340b9d5 — root project handoff and agent instructions established.

Tests: documentation review only. No application build exists yet; no APK or device test. Next: create a minimal Android/Compose project in small commits, then add GitHub Actions and obtain the first passing debug APK.


## 2026-11-14 — Architecture and device records completed

- 2b3a44b: added docs/ARCHITECTURE.md.
- 51019ab: added docs/DECISIONS.md.
- 5a0ec0e: added docs/ROADMAP.md (73 planned commit-sized tasks; not completion claims).
- ef43767: added docs/DEVICE_TESTING.md.
- 3990cd4: added ISSUES_AND_FIXES.md.
- These are documentation-only commits. No app build, APK, install or device test exists yet.
- Next: create the minimal Android/Compose build on main, then add a CI workflow that verifies clean builds and unit tests.

## 2026-11-14 — Build foundation begins
- Added the root Gradle settings file and app module include.
- No Android application source or CI workflow exists yet.
- No build/APK/device validation has been run at this step.


## 2026-11-14 — Build toolchain pinned
- Added the root build plugin declarations: AGP 9.3.1 and Kotlin 2.4.21 (Android + Compose compiler).
- Added Gradle project settings and AndroidX/Kotlin conventions.
- Gradle distribution target for CI: 9.5.1; CI JDK target: 21.
- Compatibility basis: Kotlin 2.4.21 supports AGP up to 9.3.1 and Gradle up to 9.7.0. Haze 2.1.0 also expects Kotlin 2.4.21 / Compose Multiplatform 1.12.2.
- No app module has been added yet; this commit cannot produce an APK.


## 2026-11-14 — AGP 9 built-in Kotlin correction
- Removed the redundant org.jetbrains.kotlin.android root plugin declaration. AGP 9.x provides built-in Kotlin support; the Compose compiler plugin remains version-matched to Kotlin 2.4.21.
- This is a pre-module compatibility correction, made before any app code was added.
- Verification: documentation/toolchain compatibility check only; no app build exists yet.


## 2026-11-14 — Minimal Android application module scaffold
- Added app/build.gradle.kts with AGP 9.3.1 built-in Kotlin, Kotlin-matched Compose compiler 2.4.21, compileSdk/targetSdk 37, minSdk 29 and Compose BOM 2026.09.00.
- Added a minimal launcher manifest and resource-backed app label/theme; no runtime permissions or network permission are declared.
- No screen implementation, storage access, metadata API or Glass engine is added in this commit.
- Verification: source/configuration review only; the GitHub Actions build workflow is the next gate.


## 2026-11-14 — Minimal Compose launcher surface
- Added app/src/main/java/com/batmancpu/player/MainActivity.kt.
- Added an edge-to-edge launcher surface with a restrained dark cinematic base and system typography. This is a buildable foundation placeholder, not the final screen design.
- System bars are transparent with light glyphs; no folder permissions, network, metadata or Glass dependencies are active.
- Verification: source review only; CI workflow not yet committed.


## 2026-11-14 — First Android CI workflow added
- Added .github/workflows/ci.yml triggered by pushes/PRs to main and manual dispatch.
- CI uses JDK 21, Gradle 9.5.1, Android SDK platform 37/build tools 37.0.0, runs :app:testDebugUnitTest and :app:assembleDebug, then uploads the APK for 14 days.
- Workflow intentionally has read-only contents permission and no secrets, release publishing, telemetry or signing configuration.
- The first run is the initial toolchain proof. Its result must be inspected before proceeding to Glass or storage work.


## 2026-11-14 — CI run 1: SDK package ID correction
- Workflow run: https://github.com/Batmancpu/Player/actions/runs/43560806966
- Outcome: failed before Gradle setup/build.
- Completed: checkout and JDK 21 setup.
- Failure: sdkmanager reported "Failed to find package 'platforms;android-37'"; the package identifier used by the Android 37 SDK channel is `platforms;android-37.0`.
- Correction: change only the CI SDK package identifier to `platforms;android-37.0`; keep build-tools 37.0.0.
- Root-cause confidence: high; the log explicitly identifies the missing package.
- Regression verification: new push will trigger CI run 2. Do not proceed to design/Glass until the baseline debug build passes.


## 2026-11-14 — CI run 2: first successful debug APK
- Workflow run: https://github.com/Batmancpu/Player/actions/runs/43560868703 — final conclusion: success.
- Gradle output: `BUILD SUCCESSFUL in 1m 36s`; tasks included `:app:testDebugUnitTest` and `:app:assembleDebug`.
- Note: there are no project-authored unit-test cases yet; the unit-test task passed with the current empty test source set. This is not evidence of functional parser/storage tests.
- APK artifact: `Player-debug-6c41cbc8eda342de5f78c888bd717bbc14276b71`, 10,371,251 bytes, artifact ID 14100049909, 14-day retention.
- Artifact archive SHA-256: `6211bf197a7edc9b529603e70c8eed4c3ddb669bfb86e9acc77b0150cd05f677`. This digest is for GitHub's uploaded artifact archive, not separately computed from the APK binary.
- Android platform 37.0 installation, JDK 21, Gradle 9.5.1, compile/package and artifact upload all passed.
- Device installation/launch/visual test: not yet performed by the user on CPH2531.
- CI maintenance: next commit upgrades actions that emitted Node 20 deprecation warnings to Node 24-compatible current major versions and uses the open-source Gradle basic cache provider.


## 2026-11-14 — CI run 3: updated workflow verified
- Workflow run: https://github.com/Batmancpu/Player/actions/runs/43561164125 — final conclusion: success.
- Checkout v6, JDK 21, setup-android v4, Android 37.0/build-tools 37.0.0, Gradle setup v6 with basic cache, Gradle compile/test/package, upload-artifact v7 all completed successfully.
- APK artifact: `Player-debug-aef6cd0a2058d816ba3da83a098a1a21ee7f916f`, 10,371,249 bytes, artifact ID 14100800741, URL https://github.com/Batmancpu/Player/actions/runs/43561164125/artifacts/14100800741, 14-day retention.
- Artifact archive SHA-256: `3b8efdcb8fcf9842ecb11510b0028a536754897bf0906440f1b3d748c6ceee5e`. This is GitHub's uploaded artifact ZIP digest, not separately calculated raw APK digest.
- No Node 20 deprecation warning appeared in the final log tail. Basic open-source Gradle cache provider was enabled.
- The only build-system notice is Gradle 9.5.1 being labelled out of date by the current Gradle action. Keep the verified version pinned until a deliberate upgrade is needed.
- CI still has no project-authored unit tests; the unit-test task executes successfully with an empty test source set. A real unit-test harness and cases remain open.
- Device installation/launch/visual validation: not yet performed on CPH2531.

## 2026-11-14 — Icon glyph licensing review
- Reviewed FC Zenith's SF Symbols Compose library dependency and upstream README/LICENSE.
- The library's generator/source is MIT-licensed, but the README explicitly says SF glyph artwork and symbol data remain Apple's property and points to Apple's SF Symbols license restricting usage to Apple platforms.
- Decision D-013: do not ship SF Symbols glyph assets in Player's Android APK without express permission. Preserve Apple-like optical design through original/permissively licensed icons, consistent weight/spacing and animation instead.
- Next: add design tokens/typography and evaluate a compatible icon library; do not add SF Symbol glyph packs.


## 2026-11-14 — Shared cinematic design tokens
- Added `app/src/main/java/com/batmancpu/player/ui/theme/PlayerTheme.kt`.
- Added a single dark palette, semantic text/surface colors, typography scale using the Android system sans-serif family, spacing tokens, corner geometry and Material 3 shape mapping.
- Rewired the launcher surface to use PlayerTheme and shared typography/spacing instead of local hardcoded styling.
- No Apple font, SF Symbols glyphs, glass engine, network, storage or media features were added.
- Verification: pending CI run triggered by this commit; no device screenshots/installation yet.


## 2026-11-14 — Curated navigation icon assets added
- Added four selected Lucide 2.2.1 vectors (house, clapperboard, TV, search) with 1.8 stroke width and a single PlayerGlyph registry.
- Packaged the complete upstream ISC/MIT license notice under app/src/main/assets/licenses/ and added THIRD_PARTY_NOTICES.md.
- No entire icon pack dependency was added; only the four vector assets required for the initial navigation vocabulary are bundled.
- CI verification for this commit is pending.


## 2026-11-14 — Theme contrast regression tests
- Added three JVM tests for primary text contrast, secondary/tertiary text contrast, and distinct base/elevated/pressed surface tokens.
- Tests use the WCAG contrast ratio calculation on the exact PlayerPalette values.
- Verification: pending CI run.
- Next: resolve any failing threshold from measured values before integrating the Glass engine.

## 2026-11-14 — CI run 8: theme regression tests passed
- Workflow run: https://github.com/Batmancpu/Player/actions/runs/43562170409 — final conclusion: success.
- The job completed :app:testDebugUnitTest, :app:assembleDebug, and APK artifact upload successfully.
- Added PlayerThemeTest with three JVM checks: primary text contrast on background/elevated surface, secondary/tertiary text contrast on dark surfaces, and visible separation among background/surface/elevated/pressed tokens.
- All test/build tasks passed. GitHub job output confirmed BUILD SUCCESSFUL in 30s; this workflow does not yet upload JUnit XML, so an extracted per-test report is not available.
- APK artifact: Player-debug-4d544d5886146188e65cb48fd9a99bf7c8786b57, artifact ID 14100846293, size 10,386,150 bytes, URL https://github.com/Batmancpu/Player/actions/runs/43562170409/artifacts/14100846293, expiration 2026-11-28.
- Artifact ZIP SHA-256: d5346bd4060975b0d2bf5133ce04f388ccfc7f77ec9a5c4fe3831cb1aa98ba44 (not raw APK digest).
- Device install and screenshot validation on CPH2531: not yet performed.

## Current state after CI run 8
- Buildable Android/Compose shell and CI are established.
- Shared cinematic dark theme, semantic palette, system sans-serif typography, spacing and corner tokens are implemented.
- Four licensed Lucide navigation vectors are packaged with their ISC/MIT notice and exposed through PlayerGlyph.
- Three theme contrast/palette JVM tests are now active and pass CI.
- Glass engine, media-folder access/scanning, TMDB, Room, image cache and library screens remain unimplemented.
- Next: commit Haze 2.1.0 dependencies only, then build an isolated source-backed Glass laboratory. Do not enable Haze's experimental Android native window-backdrop flag.

## 2026-11-14 — Haze 2.1.0 dependencies added (no UI integration)
- Added core Haze, Glass and optional Material 3 integration artifacts, all pinned to 2.1.0, in app/build.gradle.kts.
- This is a dependency-only commit. No Haze APIs, backdrop source, native renderer flag, screen, permission or runtime behavior is changed.
- Rationale: the official Haze docs require the same version across core and Glass artifacts; Material 3 integration is optional and included because Player uses Material 3 colors.
- Device safety: Haze's native Android window-backdrop path is experimental and intended for Android 17/API 37.2+; Player will not enable it on its Android 15 target. The Glass lab will use HazeInput.Sources + hazeSource.
- Verification: pending CI dependency resolution and debug build.

## 2026-11-14 — Haze 2.1.0 dependency baseline verified
- Workflow run 9 (handoff/status documentation): https://github.com/Batmancpu/Player/actions/runs/43562507535 — success. Artifact ID 14100791906, size 10,386,150 bytes; archive digest 0c1ae1f2904466183fe6883ba6f7dd759f3127f4dcaf5fba5de731ac46e4d1d2.
- Workflow run 10 (Haze dependency-only commit): https://github.com/Batmancpu/Player/actions/runs/43562542076 — success. Gradle resolved Haze core 2.1.0, Glass 2.1.0 and Glass Material3 2.1.0; testDebugUnitTest and assembleDebug passed; artifact upload passed.
- Haze dependency APK artifact: Player-debug-12a6329d48de4edd9f5d1c16f64e1dc6609cb6f9, artifact ID 14101256278, size 10,991,171 bytes, URL https://github.com/Batmancpu/Player/actions/runs/43562542076/artifacts/14101256278, expiration 2026-11-28.
- Artifact ZIP SHA-256: 1ff98f60760263778f45c453b9b065416077e7b2852c2b6ce7c3921a602929cb (not raw APK digest).
- No Haze composables, native renderer, permissions or product screen were added. Dependency compatibility only is verified.
- Haze API decision: source-backed HazeInput.Sources + hazeSource for Player; do not enable experimental HazeInput.Backdrop native Android window-backdrop. The Glass lab will use Default/Balanced performance first and only a small control cluster.
- Device installation / 60-Hz / GPU validation on CPH2531: not yet performed.

## Current next step
Implement one isolated Glass laboratory screen with a fixed local gradient/artwork test source and a small four-control morphing Glass group. Keep the existing library/product scope unchanged. Build and CI-check the lab before asking the user to install it on CPH2531.

## 2026-11-14 — Shared source-backed Glass primitive added
- Added app/src/main/java/com/batmancpu/player/ui/glass/PlayerGlass.kt.
- Added PlayerGlassSurface: explicit HazeInput.Sources input, remembered Clear style, balanced/default performance profile, interaction response, pointer pivot and system reduced-motion policy.
- Added PlayerGlassContainer: a small configurable wrapper around hazeGlassContainer for morphing groups.
- When the caller disables Glass, the modifier returns a consistent tinted/outlined fallback. Haze's own fallback remains available when its full renderer is unsupported.
- Explicitly does not use HazeInput.Backdrop, HazeFeatureFlags.isPlatformBackdropEnabled, Android window blur or JNI/NDK.
- Verification: pending CI. The helper is not wired into any screen yet.


## 2026-11-14 — CI run 12: Glass shape parameter mismatch
- Workflow: https://github.com/Batmancpu/Player/actions/runs/43563048500 — failed during Kotlin compilation; no APK artifact was produced.
- Exact compiler error: PlayerGlass.kt:47:19 — actual type CornerBasedShape, but Haze GlassStyle.shape expected RoundedCornerShape.
- Root cause confidence: high; the Haze 2.1.0 generated API's GlassStyle shape writer is typed to RoundedCornerShape, while the helper accepted the broader CornerBasedShape.
- Correction: narrow the shared helper's shape parameter to RoundedCornerShape; keep the same default silhouette and fallback behavior.
- This is a type-surface correction only; no rendering behavior or dependency changes.
- Regression verification: new CI run will check compilation and package.


## 2026-11-14 — CI run 13: Haze Glass helper type fix verified
- Workflow: https://github.com/Batmancpu/Player/actions/runs/43563224306 — success.
- The helper's shape argument was narrowed from CornerBasedShape to RoundedCornerShape to match Haze 2.1.0's GlassStyle.shape API.
- Gradle output confirmed BUILD SUCCESSFUL in 18s; testDebugUnitTest and assembleDebug passed, followed by artifact upload.
- APK artifact: Player-debug-9e56e60126f71eb5d890463229d322bb5f0e604b, artifact ID 14101425761, size 10,994,499 bytes, URL https://github.com/Batmancpu/Player/actions/runs/43563224306/artifacts/14101425761, expiry 2026-11-28.
- Artifact ZIP SHA-256: c4ac0ef666b25460762b9b771041ccc20d7270a2d8b2e0176ed610b3551ab242 (not raw APK digest).
- The shared Glass helper compiles but is not yet connected to any screen. Real-device visual/GPU validation is still pending.
- Next: add a single local-source Glass laboratory screen and wire it to the launcher in a separate commit; keep the lab clearly marked as a development test surface.


## 2026-11-14 — Glass laboratory screen implementation (not yet wired to launcher)
- Added GlassLabScreen.kt with a locally generated gradient/backdrop, featured content card, three poster-like artwork tiles and a connected four-control navigation group.
- All visual background content is local; no folder access, metadata request, internet permission or media upload.
- Added a Glass on/off switch to compare the Haze material against the disabled frosted fallback.
- Added UI strings to values/strings.xml.
- The existing shared PlayerGlassSurface/Container helpers provide source-backed effects and the reduced-motion system policy.
- This screen is intentionally not connected to MainActivity in this commit. Launcher wiring will be a separate commit after this screen compiles.
- Verification: pending CI.


## 2026-11-14 — CI run 15: isolated Glass laboratory compiled
- Workflow: https://github.com/Batmancpu/Player/actions/runs/43563459080 — success.
- GlassLabScreen.kt compiled with local gradient/artwork source, four Glass navigation members, selected-state animation, Glass enable/disable comparison, and resource-backed labels.
- Gradle output: BUILD SUCCESSFUL in 31s; existing JVM tests, debug APK assembly and artifact upload passed.
- APK artifact: Player-debug-31d72ec9e48baad1ac87a2c72c5cd300a5e4d62f, artifact ID 14101132751, size 11,028,372 bytes, URL https://github.com/Batmancpu/Player/actions/runs/43563459080/artifacts/14101132751, expiration 2026-11-28.
- Artifact ZIP SHA-256: 4ef45bf94360ee34c6503a6ab07e98fce97d6c34058e7c9f48beeec2bc9bff13 (not raw APK digest).
- This lab is compiled but still not visible on app launch until the separate launcher wiring commit.
- Device visual/GPU validation: pending user installation on CPH2531.


## 2026-11-14 — CI run 16: Glass laboratory launcher verified
- Workflow: https://github.com/Batmancpu/Player/actions/runs/43563620311 — final conclusion: success.
- MainActivity now launches GlassLabScreen as a temporary developer test surface. Edge-to-edge system bars remain configured.
- Gradle output: BUILD SUCCESSFUL in 31s; :app:testDebugUnitTest, :app:assembleDebug and artifact upload all passed.
- APK artifact: Player-debug-20e13b928b9c5ce75b64ecb219f795f7dd8bc67f, artifact ID 14101590882, size 11,025,438 bytes, URL https://github.com/Batmancpu/Player/actions/runs/43563620311/artifacts/14101590882, expiration 2026-11-28.
- Artifact ZIP SHA-256: 7e132dddc037598a3579ea051e124e9e7239477fc28a360a68ce295f02f86b70 (not raw APK digest).
- The app now opens directly into the local-only Glass laboratory. It displays generated gradient/backdrop content, three poster-like tiles, a four-item floating glass navigation group and an on/off comparison switch.
- Device validation on OPPO CPH2531 / Android 15: not yet performed. The next gate is user installation and screenshot/performance observations before tuning blur, tint, refraction, light, motion or layer count.
- The final library UI is not yet implemented. Do not treat the lab as the product UI.


## 2026-11-14 — User-approved local playback scope and functional-first reprioritization
- User explicitly expanded the product from catalogue-only to a local media catalogue with built-in local playback.
- Newly approved player requirements: tap a movie/episode to play its local file; broad practical container/codec support; embedded audio/subtitle track selection; local external subtitle import; online subtitle search/download while playback is open; adjustable subtitle delay with reset; minimal, uncluttered player chrome.
- Downloaded subtitles default to app-private storage. The one-folder/read-only boundary remains. No media upload, streaming, server or remote playback is allowed. Subtitle-provider network calls require a separate API/terms/privacy review and may send normalized title/year/season/episode identifiers only.
- The earlier D-010 playback-deferred decision is superseded by D-017. The production implementation order is now functional shell → SAF folder/scanner → filename parser → Room/offline data → TMDB/artwork → catalogue/detail screens → playback engine → subtitle/audio controls → production Glass.
- Media engine remains undecided. Evaluate AndroidX Media3 first; use LibVLC only if measured format gaps justify its native footprint and GPL/LGPL review. “Every format” is a compatibility goal, not a guarantee. No Media3 or LibVLC dependency has been added.
- The existing GlassLab remains a development experiment. It is not the production home screen and is no longer on the critical path for catalogue implementation. Real-device Glass validation remains mandatory before shipping/tuning Glass.
- Documentation updated: AGENTS.md, PROJECT_CONTEXT.md, README.md, docs/ARCHITECTURE.md, docs/DECISIONS.md, docs/ROADMAP.md, docs/DEVICE_TESTING.md and ISSUES_AND_FIXES.md.
- No application source, dependency, manifest permission or runtime behaviour was changed in this documentation update.
- **Next exact implementation:** replace the Glass Lab launcher with a plain setup/library shell and implement one-folder SAF selection with persisted read grant and permission-recovery state. Keep the scanner in the next focused commit.
- Current code evidence: source CI run 16 passed (https://github.com/Batmancpu/Player/actions/runs/43563620311), APK artifact ID 14101590882. Docs-only CI run 17 passed (https://github.com/Batmancpu/Player/actions/runs/43563846274). No handset install or media playback test is recorded.


## 2026-11-14 — Plain folder-setup shell and persisted SAF access
- Replaced the default Glass Lab launcher with a plain cinematic folder-setup screen; the Glass Lab remains available through a developer-preview entry and Android Back returns to setup.
- Added one-folder selection via the Android system OpenDocumentTree picker.
- Current provider policy explicitly accepts only Android's external-storage DocumentsProvider authority. Unknown, cloud and network providers are rejected by default pending device verification.
- Persisted only the read grant and selected tree URI in app-private preferences. Revalidates the persisted read grant at startup, removes stale selections and provides a reselect path if permission was revoked.
- Changing folders releases the previous persisted read grant after the replacement grant is saved. No broad storage permission or filesystem path access was added.
- This milestone stores the folder grant only. Recursive scanning, filename parsing, metadata requests, poster retrieval, database and playback are not implemented yet.
- Validation: CI pending for this commit; real OPPO CPH2531 provider selection/revocation validation remains pending. No media files are read or modified by this screen.
- Next: implement a cancellable read-only recursive scanner bounded to the persisted tree; include video files and identify sidecar subtitle files without reading or transmitting subtitle contents.


## 2026-11-14 — CI run 18: folder-setup Compose import correction
- Initial folder-setup implementation CI run 18 failed at Kotlin compilation: MainActivity.kt could not resolve rememberSaveable because it was imported from androidx.compose.runtime instead of androidx.compose.runtime.saveable.
- Failure run: https://github.com/Batmancpu/Player/actions/runs/43569596301. No APK artifact was produced.
- Fix: import rememberSaveable from androidx.compose.runtime.saveable; no UI state or runtime behaviour changed.
- Regression gate: rerun testDebugUnitTest and assembleDebug; do not mark folder setup complete until CI passes. Device/provider verification remains separate.


## 2026-11-14 — Folder setup CI verified
- Corrected folder-setup commit e20d8aa6bb193acac41414bd9e1f6b6b034ba13f passed workflow 43569710969: https://github.com/Batmancpu/Player/actions/runs/43569710969.
- The subsequent documentation handoff commit 47ce1fbfaa71c6613f5e2d210026bcf2eef8ffad passed workflow 43569752178: https://github.com/Batmancpu/Player/actions/runs/43569752178.
- Folder setup is compile/build verified in CI. OPPO local DocumentsProvider behaviour and actual permission revocation/regrant are still not device-verified.

## 2026-11-14 — Stable release-signing infrastructure prepared
- User requested one permanent signing identity so a newly built APK can update the previous installed version in place.
- Added Gradle release signing configuration sourced exclusively from runtime environment variables; no credentials or private key are stored in source.
- CI keeps debug APKs explicitly labelled CI-only, conditionally assembles a Release APK when all signing secrets exist, validates the keystore alias/password, checks the APK signature with apksigner, prints only public certificate/hash information and uploads a separately named release artifact.
- Version code is derived from GitHub Actions GITHUB_RUN_NUMBER; each new workflow run has a higher version code.
- Required repository secrets are documented in docs/ANDROID_SIGNING.md. User-side generation and configuration remain outstanding: private keystore, secure backup, base64 secret and three password/alias secrets. No secrets or key material were transmitted in this session.
- applicationId=com.batmancpu.player is unchanged. Keep it stable after the first signed install.
- Verification: pending CI for the signing scaffold. Signed APK production remains blocked until the user configures the four repository secrets.


## 2026-11-14 — Stable release-signing scaffold CI verified; device-side key helper added
- Rechecked current main and signing workflow before proceeding. Current branch head at the start of this work: `f11f2a3eb0ce300644baaecffdd760a41e45f298`.
- Signing scaffold commit `37f84dda2bffb106f50dc7ccc768808cdecb99d8`, workflow run 25 (`43570229274`): success. Its signing-preparation step confirmed that no secrets were configured; signed Release assembly, apksigner verification and Release artifact upload were correctly skipped. Do not treat this as a signed build.
- Latest documentation/handoff workflow run 26 (`43570272899`): success.
- Added `scripts/generate-release-keystore.sh`. It is secret-free, runs on the user's own Android device in Termux, creates/reuses the private JKS outside the repository, sets restrictive file permissions, verifies the alias/certificate, and prepares the base64 Actions-secret value in the same private directory.
- Updated `docs/ANDROID_SIGNING.md`, `PROJECT_CONTEXT.md`, `docs/ROADMAP.md`, `README.md` and `AGENTS.md` with the safe device-side key process, user-only GitHub secret step, first-install package-identity gate and current status.
- Added an explicit agent rule: do not generate the user's private release key in an assistant/cloud workspace or CI runner; keep it under the user's control. Never ask for its bytes/password in chat.
- Validation: `bash -n scripts/generate-release-keystore.sh` passed in the authoring environment. The helper has **not** been run on the user's phone; no real keystore or certificate fingerprint exists yet.
- Remaining user action: run the script in Termux, make at least one additional secure backup of the JKS and password, and add the four repository Actions secrets. The GitHub connector does not expose repository secret-write APIs, so this final settings operation must be done by the repository owner.
- `applicationId` remains `com.batmancpu.player`; it is still provisional until the first signed APK is installed. Decide whether to retain it before that installation.
- **Next engineering milestone after the signing setup is in place:** validate the folder picker on CPH2531, then implement the cancellable read-only recursive scanner bounded to the saved SAF tree. Do not jump to TMDB/database/player before scanner privacy and boundary tests.


## 2026-11-14 — Corrected signing-helper shell escaping before device use
- Post-commit review of `df9d62c2ea03fa9fb8da731fc33a9508a7028c94` found that the helper's shell line-continuation and printf escapes had been doubled while creating the GitHub blob.
- This would have split the keytool command and printed literal escape sequences. It was caught before the helper was run; no keystore or password was created or exposed.
- Replaced the helper with correct Bash line continuations and printf formats.
- Added a CI step to run `bash -n scripts/generate-release-keystore.sh` on every build.
- Validation: `bash -n` on the exact corrected source passed locally. GitHub Actions regression run pending.
- The key remains a user-side operation on the OPPO phone through Termux; do not use the earlier script revision.


## 2026-11-14 — Permanent signing key generated directly; Termux approach retired
- The user rejected a Termux-based setup and explicitly asked the agent to create the key directly. A permanent JKS keystore was generated using `keytool` in the current session workspace.
- Verified by reopening the JKS with `keytool -list -v`, confirming alias `player-release`, password, certificate and SHA-256 fingerprint.
- Key parameters: RSA-2048, SHA256withRSA, 10,000-day validity. Public certificate SHA-256 fingerprint: `62:A0:97:B7:BE:7D:22:A9:88:3D:9E:09:01:5C:04:17:72:72:16:D7:3E:25:06:ED:F4:70:B2:4A:61:CD:AD:C0`.
- Delivered private files separately in this conversation: `player-release.jks`, `PLAYER_RELEASE_KEYSTORE_BASE64.txt`, `PLAYER_RELEASE_CREDENTIALS.txt`, and the public `PLAYER_RELEASE_CERTIFICATE_FINGERPRINT.txt`. No keystore bytes, base64 or password were committed to GitHub.
- User should save the JKS in at least two secure locations and store the password in Proton Pass. The four repository Actions secrets remain to be created manually because the GitHub connector has no repository-secret write action.
- Explained that changing the Android applicationId later does not require a new signing key; it creates a distinct installed app. User wants branding/package cleanup deferred while the app's core functionality is built.
- Removed the no-longer-needed Termux script and its CI syntax step from the repository. Earlier helper CI workflow run 28 (`43573474031`) passed; that check was for the now-retired script. The cleanup commit must be CI-verified independently.
- Next: user downloads/backups the key, stores password in Proton Pass and creates the four GitHub repository secrets. Then trigger Android CI to verify Release APK signature. Continue the functional foundation; do not wait on branding or app-ID finalization.



## 2026-11-14 — CI handoff after Termux removal
- The first cleanup commit removed `scripts/generate-release-keystore.sh` before removing the workflow's syntax-check step. Its run (workflow run 29, ID `43574064538`) failed exactly at `bash -n scripts/generate-release-keystore.sh`: file not found. This was a sequencing error, not an Android/Kotlin failure.
- The following focused workflow update removed the obsolete syntax-check step and completed successfully: run 30, ID `43574233803`, https://github.com/Batmancpu/Player/actions/runs/43574233803. Unit tests and CI-only debug APK assembly/artifact upload passed; Release signing was correctly skipped because repository secrets are not configured.
- The later chat-history/handoff commit triggered run 31, ID `43574286284`; its status must be checked before proceeding.
- The helper and Termux flow are fully removed. Do not reintroduce them.
- Added `CHAT_HISTORY.md` as the cumulative chronological session record. Future agents must read and append to it, while keeping all credentials out of the log.


## 2026-11-14 — Signing and handoff documentation CI verified through run 34
- Corrected run-number references in the handoff docs. Actual CI run mapping: run 28 = `43573474031` (helper syntax passed); run 29 = `43574064538` (temporary missing-helper failure); run 30 = `43574233803` (workflow fix passed); run 31 = `43574286284` (chat-history handoff passed); run 32 = `43574384432` (agent/history corrections passed); run 33 = `43574452938` (mandatory chat-log handoff passed); run 34 = `43574546390` (historical signing context clarification passed).
- Runs 30–34 completed successfully with JVM tests, debug APK assembly and artifact upload. Release assembly, signature verification and Release artifact upload were correctly skipped because the four repository secrets had not yet been configured.
- The only failed run in this cleanup sequence was run 29, caused by the deleted Termux helper still being referenced for one intermediate commit. The stale workflow step was removed in run 30 and all subsequent runs passed.
- Current main before this log update: `260adf70469f36f8dbed4a797c0899a5529e4aba`; latest verified workflow run 34: https://github.com/Batmancpu/Player/actions/runs/43574546390.
- Permanent key is generated/verified and delivered. User-side outstanding tasks: save the JKS and backup, store its password in Proton Pass, add the four repository Actions secrets. No signed Release APK exists yet.
- Exact next engineering task after secret setup is complete: validate the selected-folder provider on CPH2531, then implement a cancellable read-only recursive scanner and tests. Avoid branding/applicationId work until core app functionality is ready.

## 2026-11-14 — Start of recursive scanner (pure traversal and JVM tests)
- Starting main commit: 2f37020d8923f9f8e1d76c45270210cd7b25d5a3. Previous GitHub Actions run 35 (43574688992) passed.
- Added a small sequential, cancellable, read-only traversal engine that takes an explicit selected-root containment validator and provider child-query function. It discovers common videos and subtitle sidecars by extension/MIME, skips hidden/unsupported entries and de-duplicates by document ID.
- The engine deliberately propagates permission and provider errors rather than returning a partial scan that could be treated as authoritative by a future index.
- Added JVM tests for nested discovery, duplicates, root containment, provider failure and cancellation.
- This commit does not access Android storage yet; the next focused step is to wire a SAF DocumentsContract adapter and then expose scan results on the setup screen. No TMDB, playback engine or additional permission is introduced.
- Verification: GitHub Actions CI is pending for this commit; target OPPO device testing has not occurred.

## 2026-11-14 — User-approved six phone-verified phases and Obtainium release handoff

- User requested that one-folder access and offline file discovery/filename parsing/Room persistence be merged into Phase 1 rather than separate milestones.
- Adopted six deliverable phases recorded in docs/ROADMAP.md: (1) one-folder offline library; (2) TMDB identification and cached artwork; (3) cinematic real catalogue; (4) local video playback; (5) audio and subtitles; (6) polished Glass, QA and launch.
- At the end of each phase: pass CI and signing checks; publish a distinct Release APK on GitHub Releases with changelog; user tests on OPPO CPH2531; record evidence and gate acceptance before next phase.
- User intends to track released APK updates through Obtainium. Preserve existing permanent signing key and stable package identity and monotonically increase versionCode. Never use a debug APK as install/update channel.
- The canonical instructions for a future ChatGPT Work agent are docs/WORK_HANDOFF.md and AGENTS.md; all product history remains in existing repo Markdown.
- Checked main 95881e4 and CI #36 passed. GitHub Releases was empty; signed release steps were skipped because Actions signing secrets were not configured. No signed installable APK can be claimed now.
- Next: implement Android SAF adapter + scanner list UI and build gate, then parser, Room, incremental reconciler. Add manual signed release publication workflow; user must configure four signing secrets before publishing.
