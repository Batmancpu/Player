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

## 2026-11-14 — Connect scanner to actual Android local folder (phase 1 substep)
- Base commit 6c1c616442b335d9d999dcb96608f7842702db2b. Added SafLocalMediaSource reading DocumentsContract immediate-child queries using the one persisted read-only tree URI and Android external-storage provider allowlist. No broad permissions, media reads/writes or network calls.
- DocumentsContract.isChildDocument verifies each returned opaque ID remains inside the chosen root; unsupported/failed checks fail closed. The scanner itself handles deduplication, filtering and cancellation between queries.
- FolderSetupScreen now exposes manual Scan, Cancel, counts and a basic display of up to 100 discovered video filenames; errors do not silently masquerade as successful empty scans. This is a phase 1 prototype, **not** a persisted Room library yet. Device provider support remains unverified.
- Validation: CI pending; no release APK or OPPO handset test claimed. Next add filename parser, Room persistence and a scalable results screen, then signed milestone release.

## 2026-11-14 — Manual GitHub Release publication workflow for Obtainium
- Base commit 6ce3329ba44058d0e8102dca83f760307f5a7cee. CI workflow gains a manual publish_release input. Ordinary main pushes still compile/test and upload artifacts but do not publish every small change.
- A requested publish without permanent signing secrets fails. Signed APK certificate SHA-256 is checked against the project's public fingerprint.
- A successful manual publish will create GitHub Release v0.1.<run number> with a single signed APK asset, SHA-256 and package/signature/update instructions. The run number is APK versionCode.
- Verification via CI pending; no signed APK produced or claimed. User secret setup required.

## 2026-11-14 — Deterministic filename parser for offline Phase 1
- Base commit 6b77a1b6aafad8ded973fe882dab97ab4b48e728. Added JVM-only MediaNameParser (movies with optional year; SxxExx, 1x02, Season 2/05 episodes; Unicode names; release-tag trimming) and dedicated tests.
- Parser is local-only and never calls TMDB. Ambiguous cases remain unrecognized instead of inventing identities.
- This initial corpus is intentionally modest; later phase 1 work must add multi-episode, anime, duplicate remake and alternate naming tests before declaring parsing complete.
- CI pending. Next persist and present parsed library through Room rather than displaying only raw filenames.

## 2026-11-14 — Room persistence dependency gate
- Base commit 79150479be412fb76e1f429dfc3dda6f02eada22. Added KSP Gradle plugin 2.3.12 and AndroidX Room runtime/ktx/compiler 2.8.5 as dependencies only, in an isolated build commit before adding entities/DAOs.
- Version choice follows current Kotlin KSP quickstart (2.3.12) and AndroidX Room stable documentation (2.8.5) on 2026-11-14.
- CI pending; no database generated or shipped yet. Do not add database source before this dependency compile gate passes.

## 2026-11-14 — Phase 1 Room offline index and successful-scan reconciliation
- Base commit 476ac20a2feeea133fafe4df971f2dd5f53d618d. Added initial Room database IndexedMedia with per-tree/document identity, classified media and parsed name fields, plus DAO.
- Only after SafLocalMediaSource.scan completes, OfflineLibraryIndex atomically upserts discovered entries and removes records missing from the completed scan. Failed/cancelled provider enumerations leave previous Room rows untouched.
- UI loads the cached index at startup for the selected tree, saves scan results, shows parsed movie/episode labels; switching folders removes cached rows from the old tree. No original media files are written.
- Initial DB schema version 1; explicit migrations are mandatory before any future schema version change. Exported schema setup and instrumentation migration test still pending.
- CI verification pending and CPH2531 testing unverified. Do not declare phase 1 complete until the release APK and on-device acceptance gate.

## 2026-11-14 — Scalable Phase 1 offline library list
- Base commit 7779b701761a697500f72531519158d59a52446e. Replaced the first scan UI's 100-item truncation and eager text list with a height-bounded Compose LazyColumn. It displays all indexed video entries lazily in separate movie/unmatched and TV-episode groups with normalized labels.
- Updated the folder-grant instruction from future-tense scanning to the now-implemented Scan command. The list can read from offline Room rows after restart.
- Verification: CI pending; local handset scrolling and nested-scroll behavior are not yet verified.

## 2026-11-14 — Six-phase Work handoff and Phase 1 code progress

- User explicitly requests that each phase culminate in an installable, permanently signed APK in GitHub Releases for Obtainium, and that the phase is not marked complete until the OPPO handset installation/test is verified.
- Phase 1 merges one-folder local SAF access, nested video scanning, filename parsing, a Room offline index and incremental rescans. Later phases cover TMDB artwork/metadata, cinematic catalogue, local playback, audio/subtitles and production Liquid Glass/performance.
- New canonical docs/ROADMAP.md replaces ten-phase delivery order; docs/WORK_HANDOFF.md contains session continuation and operational rules for ChatGPT Work.
- Implemented after old main 95881e4: scanner Android SAF adapter, manual scan/cancel UI, local filename parser with tests, Room/KSP dependency setup and offline Room index, lazy grouped video list. Room only deletes stale rows in an atomic transaction following a completed scan; reads/writes are app-private only.
- CI: #37 docs plan passed; #38 Android SAF UI passed; #39 manual publication workflow passed normal CI; #40 parser passed; #41 Room deps passed; #42 offline DB passed; #43 lazy grouped list passed. Latest tested code commit: d3ecc17c0036f4ef2f5db036c970d9c810b2f206. CI evidence https://github.com/Batmancpu/Player/actions/runs/43582079994
- NOT verified: real device, provider compatibility, large-library scrolling, selected folder restricted access, permission-loss handling, manual release publication signing branch, full parser corpus or Room migrations.
- GitHub Releases was empty and no signed Release APK existed at review; user still needs to provide existing key through GitHub Actions secrets. Do not create a new signing identity, do not publish debug APK and do not claim a phase test happened.
- Next action for AI: strengthen Phase 1 acceptance tests and review actual provider behavior when the user has an installed signed APK. Next action for user: add the four existing release secrets as described in docs/ANDROID_SIGNING.md; no key/password material in chat.

## 2026-11-14 — User-authorized signing reset before first release
- At the user's explicit request, replaced the former **unreleased** signing identity with a newly generated JKS/RSA-3072 key. No GitHub Release previously existed (public Releases API empty), and no published signed APK had been produced. Do not claim a previously installed package was checked on-device.
- A new key was generated and re-opened with keytool. Public certificate SHA-256: 4D:2F:17:89:ED:27:D3:96:13:35:FE:36:45:64:55:BD:9A:31:0B:0D:CD:F4:1E:8C:CC:92:53:98:33:9E:80:BF. Private key and password are distributed as private ChatGPT conversation files, NOT GitHub commits.
- Updated the release workflow's certificate pin (lowercase hex) and release-note fingerprint; docs/ANDROID_SIGNING.md is the current source of truth. Old key/fingerprint historical records remain for audit but are superseded.
- The user must back up the new JKS, store password in Proton Pass and enter four repository Actions secrets manually. No signed APK has been produced with this new key yet; CI pending for documentation/workflow update.

## 2026-11-14 — Add safe commit-triggered first signed Obtainium test publication
- User reported adding four Actions secrets and requested a real installable GitHub Release APK to test Obtainium in-place updates. The GitHub connector cannot directly trigger Actions manual workflows.
- Added a narrowly scoped signed Release publication trigger for `main` commit subject prefix `release(test):`, in addition to the existing manual workflow_dispatch option. No ordinary push publishes a GitHub Release.
- Signing secret validation, tests, certificate verification, versionCode, signed APK hash and GitHub Release publication remain required; release notes identify Phase 1 as incomplete.
- This workflow edit must pass its own CI before a separate explicit test-publish commit is made.

## 2026-11-14 — Signing gate fixed for identical store/key password
- User reported four secrets configured. Test workflow run 47 (43583692161) failed before Gradle: the Base64 key, store password and alias were present, but GitHub passed an empty PLAYER_RELEASE_KEY_PASSWORD. No private values were printed.
- The current generated permanent JKS is documented with identical store and key passwords. Updated CI to reuse the store password if the optional separate key password secret is empty. The first three secrets remain mandatory; no key regeneration or manual replacement needed.
- This does not bypass key verification: keytool, signed Release build, apksigner certificate verification and SHA-256 pin still gate publication. CI re-verification pending. Do not mark an APK released until a real GitHub Release asset exists.

## 2026-11-14 — Signing diagnostics for test-APK pipeline
- GitHub Actions run 48 (43583815426) failed at the secret-loading step despite three populated variables and an optional empty private-key password; the prior keytool check suppressed its own error output. No APK built.
- Privately checked the ORIGINAL generated JKS, Base64 file and password in the local session: Base64 decodes byte-for-byte to the JKS and keytool can open the private JKS with the generated credentials.
- Added safe, distinct diagnostics in CI: JKS SHA-256 match (not secret content), keystore password verification and alias verification. None of these diagnostic steps prints keys or passwords; the exact failed checkpoint can tell user which GitHub value to correct.
- Next: verify new workflow result; do not publish until all signing and cert checks pass.

## 2026-11-14 — Remove incorrectly entered public alias from GitHub secret dependency
- GitHub Actions diagnostic run #49 (43584051484) verified the exact generated JKS contents and correct keystore password, but the value passed as PLAYER_RELEASE_KEY_ALIAS did not match public alias 'player-release'. No APK or GitHub Release was created.
- Alias is not secret, and this application's generated JKS is known to use 'player-release'. Switched workflow to set fixed alias directly rather than trusting an incorrectly populated repository secret; no user action required.
- Preserve the exact key and password; next CI must pass signed assembly and certificate verification before publishing first test release.

## 2026-11-14 — First Obtainium test APK: fix apksigner output matching and publish
- CI run #50 (43584184863): generated key SHA-256, keystore password and fixed alias verified; tests and debug build passed; **assembleRelease, apksigner verify and signed APK artifact upload all passed**. The final custom certificate check failed because its awk pattern expected "Signer #1 certificate SHA-256 digest" while the current build tools print "V2 Signer: certificate SHA-256 digest". The actual V2 certificate SHA-256 in the job log exactly matched the new published project fingerprint 4d2f1789ed27d3961335fe36456455bd9a310b0dcdf41e8ccc925398339e80bf.
- Fixed only the overly specific awk marker; retained strict expected SHA-256 certificate comparison. A special user-approved release(test): commit triggers the existing GitHub Release publication gate after CI tests and signing succeed.
- The resulting APK is a preview/testing build of incomplete Phase 1, not a fully verified application. Next: confirm the GitHub Release APK asset exists, user installs from Obtainium, and then publish an incremented second release for update-in-place testing.

## 2026-11-14 — FIRST signed GitHub Release: v0.1.51
- **Verified public release:** https://github.com/Batmancpu/Player/releases/tag/v0.1.51
- **Direct APK asset:** https://github.com/Batmancpu/Player/releases/download/v0.1.51/Player-release-51.apk (24,869,875 bytes).
- **GitHub Actions run #51:** https://github.com/Batmancpu/Player/actions/runs/43584487865 — completed SUCCESS. Automated tests/debug build, signed Release assembly, APK signature verification, pinned certificate SHA-256 validation and GitHub Release publication all succeeded.
- Release commit: b2d766132438f94a2e4bbe3add9bff6aad53622d. Android package `com.batmancpu.player`, versionCode 51, versionName `0.1.51`. Certificate fingerprint is in docs/ANDROID_SIGNING.md.
- This is a **test release of incomplete Phase 1**. No OPPO CPH2531 installation, folder-permission or actual Obtainium update has been verified. Do NOT mark Phase 1 accepted.
- **Next user action:** add https://github.com/Batmancpu/Player to Obtainium, install signed v0.1.51, and report that it installs and launches. Do NOT publish a second test release until first install is confirmed; then publish a higher run-number signed release with same app ID/certificate to test an in-place update without uninstalling.
- Secret diagnostic history: run #47 failed from missing separate key-pass secret; workflow now derives it from store pass for this JKS. Run #49 identified an incorrect non-secret alias and CI now pins `player-release`. Run #50 successfully signed APK but custom certificate comparison expected an obsolete apksigner output prefix. Run #51 changed the parser to match current output and completed full publication. All secret files remain private, never in source control.

## 2026-11-14 — Second signed test build for Obtainium in-place update
- The user reports **v0.1.51 installed successfully via Obtainium** and explicitly requests a SECOND signed GitHub Release to test updating it in place, retaining the package and app data.
- This commit's special `release(test):` subject intentionally triggers CI signed APK publication to GitHub Releases. The APK uses the exact same applicationId `com.batmancpu.player` and fixed signing key; Gradle takes a NEW higher versionCode/versionName from the GitHub Actions run number.
- No app feature or storage-permission changes are required for this update-continuity test. The updated APK should be installed through Obtainium's **Update** action, not by uninstalling Player.
- The successful user report applies only to the FIRST installation; in-place update success remains unverified until the user confirms app data remains after the update.
- CI and actual Release asset link must be checked before giving installation instructions. Phase 1 app functionality remains under development.

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

## 2026-11-14 — Obtainium in-place update confirmed by user

- User reports Player v0.1.51 was installed with Obtainium and then updated successfully to v0.1.53 **over the existing app**, confirming the install/update pipeline in practice. This is user-reported physical-device verification; GitHub CI also verified both signed Releases and the same pinned certificate with increased versionCode (51 → 53).
- **Confirmed:** in-place update worked without an uninstall. **Not separately confirmed:** persistence of a previously scanned library across the update, fully unattended background installation, or acceptance of Phase 1 media-library features. Do not conflate Obtainium's ability to check for updates with silent unattended installation.
- User intends to hand repository implementation to **Codex** now that manual setup works. The project remains in this GitHub repository; Codex should read AGENTS.md, docs/WORK_HANDOFF.md, docs/ROADMAP.md, docs/ARCHITECTURE.md, docs/DECISIONS.md, docs/ANDROID_SIGNING.md and docs/PHASE1_PHONE_TEST.md first.
- **Next engineering task:** complete and verify Phase 1 only: single local SAF folder read-only; actual recursive folder discovery; filename parser, Room index and rescans; safe cancellation/permission recovery; automated tests. Produce a permanently signed APK to GitHub Releases, then request actual OPPO phone acceptance. Avoid premature TMDB/playback/Glass work.
- No additional manual GitHub secrets/signing setup should be requested. Preserve `com.batmancpu.player`, the current pinned certificate, and strictly increasing `versionCode`. No debug APKs in the Obtainium release channel.

## 2026-11-14 — Final design/performance/privacy Codex handoff
- User requests exceptional original premium Apple Liquid Glass-inspired UI, accessible interactive glass sliders/controls, real measurable smoothness on OPPO Reno10 5G CPH2531 (Dimensity 7050/Mali-G68 MC4, up to 120 Hz), internet research of Apple official HIG/GitHub code and maximal on-device privacy.
- Added `docs/DESIGN_SYSTEM_AND_PERFORMANCE.md` (60-Hz ~16.67ms goal, optional 120-Hz ~8.33ms, benchmarks/fallbacks, explicit per-screen Glass surfaces). Added `docs/UPSTREAM_AND_PRIVACY.md` (licensed/pinned GitHub code reuse, telemetry/security audit, one-tree read-only SAF, offline Phase 1, opt-in normalized TMDB/subtitles, current allowBackup=false).
- Reconciled `docs/ROADMAP.md`, `docs/ARCHITECTURE.md`, `docs/DECISIONS.md` and `docs/WORK_HANDOFF.md`; `AGENTS.md` is mandatory entry point and must receive a dated concise context update **for every subsequent change**, with detailed `DEVELOPMENT_LOG.md` and additional file updates as needed. Only Codex tasks/short-lived PRs should change code; main is release source.
- Current app code is unchanged by this documentation handoff; no new performance benchmarks/real device UI tests were run. The confirmed v0.1.51 → v0.1.53 Obtainium update/signing identity remains in place. Finish Phase 1 and phone-test before subsequent features.
- Research references and implementation requirements are in the two new documents; do not reinvent mature code or blindly copy from unlicensed GitHub repositories. Never add Apple proprietary assets, excessive Glass, secret exposure, outbound media, unreviewed network hosts or broad storage permissions.
- Docs commits include a4317c8 (new specifications), 3649e7f (agent and Work handoff), 07e1dcc (roadmap, architecture and decisions). CI validation of latest documentation updates pending when recorded. Next engineer action: read docs and implement next focused Phase 1 regression test/change with AGENTS ledger update.

## 2026-11-14 — Codex Phase 1 audit and incomplete directory protection
- Baseline main acb9829daf4720b6a81adaeef7f75def3aad8f00; baseline CI [43588156049](https://github.com/Batmancpu/Player/actions/runs/43588156049) succeeded. Recent commits are documentation; releases v0.1.51 and v0.1.53 share the pinned certificate/package. No open GitHub issues at audit. Source inspected: all library implementation/tests, launcher, dependencies, manifest, workflow and canonical records.
- Defect H-008: SAF cursors with EXTRA_LOADING/EXTRA_ERROR were treated as complete. Null ID/name rows were silently skipped; null MIME could hide a directory. Successful-scan reconciliation could remove cached rows after an incomplete listing.
- Changed MediaTreeScanner.kt to accept TreeListing (rows + loading/error flags) and throw sanitized IOException on incomplete status before processing rows. SafLocalMediaSource.kt reads cursor extras before close and fails invalid mandatory fields rather than returning an incomplete success. No provider error text, IDs or filenames enter diagnostics.
- MediaTreeScannerTest.kt retains traversal/deduplication/permission/cancellation coverage and adds loading/error rejection after earlier valid results, no deeper traversal/no reconciliation, and genuine empty-directory success. No new dependency, permission, schema, signing or release changes.
- docs/PHASE1_AUDIT.md records prioritized remaining risks/tests and official design/upstream research. Updated current handoff/phone/signing/context statements; historical log entries remain historical.
- Local toolchain: Java 21 exists; Gradle, Kotlin CLI, Android SDK and adb absent. Shell GitHub access fails (proxy/DNS); source fetched via connected GitHub at pinned SHA. Android compilation/tests must run on PR CI; currently pending. Cursor adapter instrumentation and real OPPO acceptance remain unverified. Do not publish a Phase 1-complete APK for this slice.
- Next exact gate: PR CI `gradle --no-daemon --stacktrace :app:testDebugUnitTest :app:assembleDebug`; inspect outcome, merge only if green, then test lifecycle/grant races. Commit SHA/evidence to be added in next ledger update.



## 2026-11-14 — First Phase 1 fix verified and integrated
- Functional commit: 95577232cbc120f409910ccab4724e2f836e739f; PR https://github.com/Batmancpu/Player/pull/1 merged as 79ee09d7669f90ff14a9592a801a84170006c062.
- PR CI https://github.com/Batmancpu/Player/actions/runs/43612121975 succeeded. Job 132499215888 logs show testDebugUnitTest and assembleDebug executed; BUILD SUCCESSFUL in 41s. CI-only debug artifact uploaded; no install recommendation. No extracted per-case XML available, so no per-test runtime/count claim.
- Local source-manifest check confirms no uses-permission and allowBackup=false; adapter has no source-write API. This is source inspection only, not merged-manifest or handset verification.
- docs/PHASE1_AUDIT.md now includes exact inspected upstream revision, commit date and root Apache-2.0 license for Now in Android, Media3, Coil and Haze. Full dependency/security/permission/NOTICE/APK/performance evaluation remains required before new adoption; no copied code/new library.
- ISSUES_AND_FIXES.md cross-references H-009/010/011 source-confirmed gaps and next regression scenarios. H-008 JVM gate passed; Android cursor/Room/device reproduction still pending.
- Main signed CI pending at this documentation commit. No new GitHub Release; latest published version remains v0.1.53. Phase 1 unaccepted and no FPS measurements. Next: verify main CI and implement H-009 with deterministic cached-load/scan/selection ordering tests.

## 2026-11-14 — Main signed verification after storage merge
- Main merge 79ee09d7669f90ff14a9592a801a84170006c062 passed workflow #61: https://github.com/Batmancpu/Player/actions/runs/43612318514. Tests/debug assembly, signed Release assembly, apksigner validation and pinned permanent certificate comparison all succeeded (job 132499811851).
- Signed CI artifact: Player-release-v61-79ee09d7669f90ff14a9592a801a84170006c062; artifact ID 14120517799; ZIP archive digest 1f51fafd35a2168020653b7446ee6e2f5818bd6af26fc87b1852cff432faab05 (not raw APK digest). Application identity/signing unchanged; versionCode 61 is greater than published 53.
- Publication was skipped by design; no new GitHub Release or phone acceptance claimed. Latest published Obtainium release remains v0.1.53. No device frame measurements.
- This entry supersedes prior main-pending notes, which describe status when written. Documentation follow-up 44e3d09 records PR proof/provenance. First next coding action: deterministic tests reproducing H-009 delayed cached load versus newer scan/folder selection, then serialize lifecycle state without broad feature expansion.

## 2026-11-14 — H-009 retained library session and serialized operations
- Baseline 8593ea62fefa6672da39bfacca88bd03a8c9574a; latest source signing gate 43612318514 passed; documentation CI checked before editing. User requested proceeding to next goal and very brief progress plan. Planning estimate: 10–15% overall, about half of Phase 1 implemented; these are rough scope estimates, not verified completion or accepted phases.
- Added pure LibrarySession.kt: immutable StateFlow; main-thread commands; generation tokens reject obsolete results/errors/finalizers; a Mutex serializes load/scan/save and waits for cancelled work to unwind. Scan is disabled during cache restoration to preserve the previous complete snapshot on subsequent failure/cancel; cancelled scans never reach save after returning. No arbitrary time delay or retry loop.
- Added LibraryViewModel.kt using existing Activity/Jetpack lifecycle APIs and application context only; MainActivity supplies its retained session. FolderSetupScreen observes state instead of launching its own scans/loads; reattaching same tree preserves ongoing work, initial restoration does not clear retained state, and changed selection cannot display previous-tree rows. Screen still owns folder-picker grant/preferences flow (H-010 remains).
- OfflineLibraryIndex.kt adds transactional activateAndLoad/removeOtherTrees (query only, no schema change), called inside serialized session. Removed picker-local asynchronous forgetTree call: a late old cleanup could erase a quickly reselected tree. Other cached trees retire atomically before loading the active tree after obsolete operations finish. Room cancellation/rollback integration remains unverified pending Android tests.
- Added LibrarySessionTest.kt with six deterministic coroutine cases: blocked cache restore vs scan; failure retains cache; cancelled noncooperative old scan cannot save or clear newer scan flags; A→B→A rejects old identity-matching results; switch waits for old save to unwind; reattachment preserves work and owner cancellation prevents save. Deferred barriers, no sleeps, JUnit timeout limits; tests do not claim actual rotation/Room/device coverage.
- No dependency/version/permission/signing/applicationId/release changes. Local Android toolchain unavailable; PR unit/debug gate pending. Next: inspect full CI, merge only green, inspect signed main gate; then H-010 grant/root recovery plus query cancellation/bounds H-011.

## 2026-11-14 — H-009 verification and progress context
- Functional commit 17e46a8c070130d09fa46422fddcd1d3832ba576; PR https://github.com/Batmancpu/Player/pull/2. CI https://github.com/Batmancpu/Player/actions/runs/43613459732 passed testDebugUnitTest and assembleDebug, BUILD SUCCESSFUL in 42s (job 132503319693). Merged as 10dab087d1b79cfd6402e3ac9a0158752b78c49d.
- Existing dependency graph compiled ViewModelProvider/AndroidViewModel/viewModelScope without adding an external dependency. Source tests contain six new deterministic cases; no per-case XML or Android recreation test extracted.
- docs/ARCHITECTURE.md and PROJECT_CONTEXT.md record implemented retained-session boundaries. docs/ROADMAP.md records user-requested very brief progress context: rough 10–15% whole product, roughly half Phase 1 implemented, zero phone-accepted phases. Estimates are not measured effort/acceptance.
- docs/PHASE1_PHONE_TEST.md adds future rotation/lab return/A→B→A/cancel-rescan checks. No request for device work until the Phase 1 candidate is ready.
- Signed main gate 43613651152 pending at authoring; verify before closing this task. Next code goal H-010: explicit root availability/reconnect and single read-only grant ownership; then H-011 query cancellation/bounds. No new Release/device FPS claim.

### H-009 main signing gate completed
- https://github.com/Batmancpu/Player/actions/runs/43613651152 (#65) completed SUCCESS at merge 10dab087d1b79cfd6402e3ac9a0158752b78c49d. Unit/debug build, signed release assembly, APK signature and permanent certificate comparison passed; signed CI artifact v65 uploaded. No GitHub Release published; v0.1.53 remains the Obtainium release.
- This supersedes main-pending status at authoring above. No handset recreation, grant, Room integration or performance result is inferred. Documentation records updated in the same follow-up commit; next goal H-010 grant/root recovery.

## 2026-11-14 — H-010 folder-grant ownership and recovery
- Baseline 30ea20f; prior lifecycle/signing gates passed. Added FolderAccessManager.kt with serialized restore/connect, durable selection before old-grant release, fail-closed cleanup verification, abandoned-acquisition recovery and write-grant removal. FolderAccessStore is fakeable; nine JVM cases cover root unavailable, missing read grant, unknown provider, failed cleanup/new root, ordering, orphans and concurrent restore.
- AndroidFolderAccessStore.kt performs grant/preferences/root query on IO; allowlists content:// external-storage tree only, verifies root directory/availability and commits selected URI durably. No media bytes or write grant requested. Platform query cancellation arrives in next H-011 slice.
- LibraryViewModel now owns folder restoration/connection across recreation; generation rejects obsolete completion; FolderSetupScreen only launches picker/observes state. Root/permission failure shows reconnection flow without dropping the indexed library. No raw provider error text is shown/logged.
- No dependency/schema/signing/manifest change. PR CI pending; fake tests do not verify OEM provider/cursor behavior. Next H-011 cancellation/bounds, then persistence/parser/UI/release candidate.
- User asked to continue until a Phase 1 signed Release candidate and schedule resumptions. Created two one-time cloud continuations for +5h and +10h in this thread; runs are finite/usage-dependent, preserve GitHub handoff, avoid duplicate work and do not invent physical acceptance.

## 2026-11-14 — H-011 bounded/cancellable SAF enumeration
- Added SafQuery.kt: queries run on IO with CancellationSignal connected to coroutine cancellation; cursor is consumed/closed within the helper on success/error/cancel. Both root probe and child enumeration use it; row loop checks coroutine cancellation. isChildDocument has no cancellable platform overload and remains an IO binder call; never promise forced cancellation of a broken OEM provider.
- MediaTreeScanner adds explicit configurable budgets (50k visited rows incl duplicates/hidden, 10k directories, depth 128, 10k rows per directory). Limit/invalid/containment failure throws sanitized IOException, never partial success. Adapter bounds cursor materialization before allocating beyond per-directory cap. No paths inferred from opaque IDs.
- Tests add total-row/depth/directory/cycle cases and change out-of-root expectation to fail whole scan before collecting any results. Existing incomplete/cancellation cases retained. No dependency/manifest/schema/signing change. Verification pending separate PR gate; Android cancellation/cursor tests still required.

- H-010 verification: PR #3 commit 57ddfbc passed unit/debug CI 43614796902 and was merged. Android/device checks still pending; signed main gate to be inspected before release.

## 2026-11-14 — Android integration test dependency gate
- H-010 PR #3 (57ddfbc) passed CI 43614796902; H-011 PR #4 (8a46a33) passed CI 43615085618 and merged. No phone evidence inferred.
- Add only androidTestImplementation runner 1.7.0 and existing JUnit 4.13.2; add assembleDebugAndroidTest to verification command. Dependency-only gate before writing Android test sources. No new production SDK/permission/network/size impact expected; merged production manifest still needs assertion.
- Official stable version verified from https://developer.android.com/jetpack/androidx/releases/test (runner stable 1.7.0); https://github.com/android/android-test/blob/main/LICENSE is Apache-2.0. Retain applicable notices for redistributed test binaries; no upstream source copied.
- CI pending. Next: real Room reconciliation/reopen/tree-isolation/rollback and synthetic cursor/cancellation tests on Android 15 emulator. Emulator results are not OPPO acceptance or measured FPS.

## 2026-11-14 — Android Room/cursor integration coverage
- Added OfflineLibraryAndroidTest for complete/empty reconciliation, disk reopen, tree isolation/activation, incomplete provider snapshot preservation, Room cancellation rollback and merged debug-manifest privacy. Allows only AndroidX internal DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION, forbids network/storage/other permissions; backup must be disabled.
- Added synthetic nonexported debug-only provider plus SafQueryAndroidTest for cursor closure on success/failure and cancellation reaching a blocked provider. No real filenames/media, URI grants or uploads. Debug provider is excluded from release source set.
- CI gains separate read-only-permission Android 15/API35 emulator job using standard SDK tools (compileSDK37 unchanged); no signing credentials in that job. Existing verification/signing job needs instrumentation success, so publication is gated. Reports uploaded with synthetic data only. Emulator tests are not OPPO or GPU measurements.
- No production dependency/permission/schema change. Test harness dependency gate/compilation and emulator execution pending; inspect failures before continuing to publication.

## 2026-11-14 — Conservative filename parser edge cases
- MediaNameParser/MediaNameParserTest: leading year-like numbers no longer erase numeric titles; select final nonleading year token. Multi-episode SxxExxExx/range and 1xNN ranges remain Unrecognized rather than claiming one episode/movie. Added synthetic fixtures; no online matching, paths, dependency or schema change. Verification pending parser PR; not a promise of universal filename interpretation.

## 2026-11-14 — Plain offline library states and series grouping
- LibrarySession adds explicit completed-scan/cancelled flags; cache restoration alone does not claim a completed empty scan. FolderSetupScreen adds saved-cache loading and empty/cancel status text, groups episodes by parsed series then season/episode, memoizes parsing/grouping, and respects safe system insets. Strings are resource-backed; no Glass or network.
- This slice remains unverified until its own PR gate; actual TalkBack/large-font/OPPO rendering checks belong to the phone checklist. No phase acceptance/FPS claim.

## 2026-11-14 — Phase 1 release boundary and candidate notes
- scripts/verify_release_manifest.py uses standard aapt2 on the built signed APK: fixed app ID, expected run-number versionCode, only AndroidX internal signature permission, allowBackup=false, no debug synthetic provider. Signing secrets/media contents are never read. CI runs this after release assembly before publication.
- Release title/notes now describe the functional Phase 1 phone-test candidate and short/full checklist instead of update-pipeline-only testing. Still explicitly unaccepted without OPPO report; no FPS claim. No signature/key/versioning design change; explicit release(test) main push or manual dispatch remains required. Verification pending signed build; no Release published yet.

- Release attribution follow-up: THIRD_PARTY_NOTICES.md plus packaged APACHE_2_0.txt/DEPENDENCIES.md cover existing AndroidX/Kotlin/coroutines/Haze runtime licenses alongside Lucide ISC/MIT. Haze root license read at tag 2.1.0, blob 261eeb9e9f8b2b4b0d119366dda99c6fd7d35c64; no upstream app code/assets copied. `python -m py_compile scripts/verify_release_manifest.py` passed locally; real APK assertion pending signed gate.

## 2026-11-14 — Android and plain-library verification completed
- Android PR #6 be24e38, CI https://github.com/Batmancpu/Player/actions/runs/43615935921 succeeded. Android job 132510883667 logs: Starting 9 tests on Android 15 AVD; BUILD SUCCESSFUL in 4m 11s, no failed tests. Tests cover Room/SAF helper and launcher recreation; synthetic provider, not actual OPPO external-storage DocumentsProvider or measured frame timing.
- Parser/UI PR #7 fec88a9 passed unit/debug/test-APK CI 43616107190, merged as 3d5d6f4. No source/automated failures observed in those gates. Main combined test/signing gate plus actual release-manifest assertion still required before publication.
- Next: merge release notes/manifest/attribution preparation, inspect signed main privacy gate; only then explicit release(test) publication. Phase 1 remains awaiting real-device acceptance.

- Candidate context reconciliation: updated docs/WORK_HANDOFF.md, ARCHITECTURE.md, ROADMAP.md, PHASE1_PHONE_TEST.md, PROJECT_CONTEXT.md, CHAT_HISTORY.md and ISSUES_AND_FIXES.md with integrated grant/scan/UI fixes, 9 Android emulator tests, remaining actual OPPO/provider gates and nonblocking schema-export/metadata follow-ups. Prepared for explicit publication commit after PR #8 signed gate; no Release or device acceptance yet.

- Publication trigger: release-preparation PR #8 passed CI 43616779144; combined main 7684359 passed CI 43616714927. Merged #8 with explicit user-authorized release(test): title to trigger the existing publication path. The same pipeline gates manifest/signature/tests before publishing; no unverified artifact is distributed. Await publication result and exact tag/hash before announcing APK.

## 2026-11-14 — H-013 publication verifier format failure and correction
- Main publication run https://github.com/Batmancpu/Player/actions/runs/43617409651 (#78, eb66dc9) FAILED at production manifest backup assertion. Android 15 integration, JVM/debug and signed release assembly all passed. No Release was created; latest remains v0.1.53.
- Exact error: AssertionError Backup must remain disabled at verify_release_manifest.py line 23. Source/debug Android test show disabled backup; release checker required the single text form `(type 0x12)0x0`, which is too narrow for aapt2 boolean renderings. Actual release representation will now be logged as a sanitized attribute value to establish exact evidence.
- Corrective scope: verifier accepts only false/literal zero-hex/typed zero forms; rejects true, nonzero, unknown or missing backup. New test_verify_release_manifest.py drives the complete script against synthetic aapt output with accepted false forms and rejected permission/debug-provider/stale-version cases. Three test groups passed locally using `python -m unittest discover -s scripts -p 'test_*.py'`; added same gate to CI. No app behavior/schema/dependency/signature change.
- Next: explicit corrected release(test): main commit; all existing gates still required before publication. Do not claim backup-on defect or a signed Release until actual output/result is verified.

## AGENTS ledger archive — 2026-11-14
The following prior Recent agent changes ledger is preserved verbatim when shortening AGENTS.md. Pending statuses are historical, not current.

- 2026-11-14 — Roadmap/architecture/decisions aligned with the binding premium design, phone benchmarks, source-reuse/privacy rules and Codex task-PR workflow (commit 07e1dcc). Docs only; CI status pending. Next: Codex Phase 1 code/tests.
- 2026-11-14 — Premium Codex handoff: added design/performance spec and upstream/private-data rules; linked binding UI surfaces, measured GPU frame gates, licensed GitHub research, per-change AGENTS ledger and retained Obtainium signing invariants. No GPU/on-device UI benchmark performed; doc-only CI pending. **Next:** Codex finishes Phase 1 with tests and a signed user-tested APK.

- 2026-11-14 — Phase 1 audit and incomplete-provider fix: SAF cursor loading/error status now travels with directory rows; incomplete scans fail before Room reconciliation; malformed mandatory cursor fields fail closed. Added scanner regression tests and docs/PHASE1_AUDIT.md; corrected stale release handoff. Verification: source reviewed, CI pending; no device measurements. Next: run PR unit/debug gate, merge if green, then address scan/folder lifecycle races.



- 2026-11-14 — Verified storage fix 9557723 via PR #1 CI (https://github.com/Batmancpu/Player/actions/runs/43612121975): unit tests/debug assembly passed, merged as 79ee09d. Added pinned upstream license/activity provenance and tracked remaining lifecycle/grant/bounds defects. Main signed CI pending at recording; no device test/release. Next: inspect main signing gate, then implement H-009 lifecycle tests/fix.

- 2026-11-14 — Main verification for 79ee09d passed (https://github.com/Batmancpu/Player/actions/runs/43612318514): tests/debug and signed release assembly/certificate pin all succeeded; signed CI artifact v61 retained, no Release publication. Updated current audit/handoff evidence. No OPPO functional/performance test. Next: implement H-009 cached-load/scan/selection regression and serialized lifecycle owner.

- 2026-11-14 — H-009 lifecycle fix: LibrarySession serializes cache/scan/save with cancellation generations; retained LibraryViewModel owns the session across rotation/lab routing. Scan waits for cache restore; tree activation retires other cached trees transactionally after obsolete work unwinds, replacing picker-local cleanup races. Six deterministic coroutine tests added. Verification pending PR CI; no phone/performance claim. Next: unit/debug gate, then signed main gate; H-010 grant recovery remains next.

- 2026-11-14 — H-009 verified in PR #2: 17e46a8 passed unit/debug CI 43613459732 and merged as 10dab08. Updated architecture/context/roadmap/phone checklist with retained-session behavior, remaining device gaps and rough progress (10–15% overall, zero accepted phases). Main signing gate pending at authoring. Next: inspect gate, then H-010 grant/root recovery.

- 2026-11-14 — H-009 main verification complete: CI 43613651152 passed unit/debug, signed release assembly and permanent certificate check for 10dab08. Recorded evidence in handoff/audit/issue/history; no new Release or OPPO acceptance. Next coding action: H-010 serialized grant/root recovery tests and implementation.

- 2026-11-14 — H-010: serialized durable folder-grant manager, explicit root probe, read-only/orphan-grant reconciliation and retained ViewModel picker flow replace screen-owned grant races. Nine fake-store regressions added. PR verification pending; no phone acceptance. User authorized continuous Phase 1 work through signed candidate; two finite continuations scheduled for +5h/+10h, respecting real-phone gates. Next: CI then H-011 query cancellation/bounds.

- 2026-11-14 — H-011 pending slice: SAF queries now attach CancellationSignal, close cursors before returning and check cancellation per row; scanner budgets bound entries/directories/depth/per-directory rows, reject invalid/out-of-tree listings and preserve cached rows on failure. Added budget/cycle regressions. Unverified pending separate CI after H-010 merge. Next: verify both slices, then Android persistence/provider tests and candidate UI/parser completion.

- 2026-11-14 — H-010/H-011 PR gates passed (43614796902/43615085618), both merged. Added AndroidX Test runner 1.7.0/JUnit instrumentation-only dependencies and test-APK assembly gate, isolated before Android test source. Official stable/Apache-2.0 reviewed; release dependency graph unchanged. Verification pending CI. Next: Room/cursor emulator tests, then candidate parser/UI completion.

- 2026-11-14 — Android integration slice prepared: Room rescan/empty/reopen/tree activation/rollback tests and synthetic debug-only cursor provider/cancellation tests; CI Android 15 emulator job gates signing. Production permissions/backup asserted, allowing only AndroidX internal signature permission. No release/provider authority changes. Verification pending harness and emulator CI. Next: inspect results, finish parser/UI then publish candidate.

- 2026-11-14 — Parser corpus slice: preserve leading numeric movie titles/use trailing release year and keep multi-episode ambiguity under original filenames. Regression fixtures added; CI pending a focused parser PR after Android harness gate. Next: verify corpus and finish plain library states/grouping before candidate release.

- 2026-11-14 — Plain-library completion slice: cached loading, initial empty, completed-empty and cancelled states; series-grouped episode list with memoized parsing and safe system insets. LibrarySession exposes completion/cancel flags. Unverified pending focused CI; no Glass/network addition. Next: validate Android/filename/UI gates and publish a signed Phase 1 candidate.

- 2026-11-14 — Release-candidate preparation: built-APK manifest gate checks fixed identity/run version, offline permission allowlist, backup disabled and debug-provider exclusion. Release notes now give Phase 1 feature/checklist and retain unaccepted status. Unverified until signed main gate; publication remains explicit release(test) trigger only. Next: finish CI and publish verified candidate.

- 2026-11-14 — Candidate attribution: packaged Apache-2.0 text and runtime-library source/author notices alongside existing Lucide license; no new runtime dependency/source copied. Verified Haze license at shipped tag 2.1.0. Syntax check passed for release-manifest script; signed APK check pending. Next: finish Android suite and manifest gate, publish candidate.

- 2026-11-14 — Android integration PR #6 passed CI 43615935921: all nine Android 15 emulator tests passed (Room/cursor/privacy/launcher recreation). Parser/plain UI PR #7 passed CI 43616107190 and merged. OPPO/actual SAF provider acceptance remains pending. Next: final release manifest gate then explicitly publish signed Phase 1 candidate.

- 2026-11-14 — Candidate handoff reconciliation: architecture/context/roadmap/history/issues/phone checklist now distinguish integrated source and 9 passing Android tests from pending signed-APK/real-OPPO gates. Recorded schema/metadata/corpus follow-ups without claiming phase acceptance. Docs prepared for explicit publication commit after PR #8 signed main gate. Next: inspect CI and publish only green candidate.

- 2026-11-14 — Publication authorized/triggered via explicit release(test) PR #8 merge after CI 43616779144 and combined main CI 43616714927 succeeded. Publication pipeline must pass all tests, signing and actual release-manifest assertions before Release creation; no publication/OPPO claim yet. Next: inspect publication run/release asset then record exact tag/hash/checklist.

- 2026-11-14 — H-013 release-checker correction after failed publication CI 43617409651: backup assertion assumed one aapt2 false format. Accept only semantic false forms (literal false/zero hex/typed zero), still reject true/unknown; log sanitized attribute value. Three full-script regression groups pass locally (false variants, true rejection, permission/provider/version guards); wired into CI. No APK published by failed run, no app/signing change. Next: corrected gated publication.

## 2026-11-14 — Published Phase 1 candidate and latest branch policy
- Confirmed https://github.com/Batmancpu/Player/releases/tag/v0.1.79 at source f1d7f277ad8a3534715803f098f3a33336cba0c4, versionCode 79, applicationId com.batmancpu.player. Signed asset Player-release-79.apk, 24,925,596 bytes; raw APK SHA-256 3f889523752df27fd5c025825af828304c2436c34b217021f2439cdd83fb5e95. Permanent certificate remains 4D:2F:17:89:ED:27:D3:96:13:35:FE:36:45:64:55:BD:9A:31:0B:0D:CD:F4:1E:8C:CC:92:53:98:33:9E:80:BF.
- Publication workflow https://github.com/Batmancpu/Player/actions/runs/43618442469 (#79) completed SUCCESS: Android 15 integration, Python verifier fixtures, JVM/debug/test-APK, signed release, actual manifest privacy/identity and certificate pin, GitHub Release publication. Job 132519521581 confirms `Verified allowBackup representation: false`; H-013 was a verifier format mismatch, not enabled app backup. Run #78 failure remains documented.
- Phase 1 candidate is ready for actual OPPO checklist; not accepted and no handset frame measurements. Current production code remains offline/read-only/no source writes.
- Latest user explicitly rejected branch proliferation: main only for normal commits, maximum one optional backup branch if genuinely needed. Supersedes earlier task-PR exception. Checked all eight task refs against main: each behind_by=0; no open PRs. Current CLI GH_TOKEN is invalid and connector lacks delete-ref tool; no secrets requested. Added temporary cleanup workflow using existing Actions contents-write token to delete only exact allowlisted unchanged, fully merged refs. Workflow will be removed after verification. No app/release/signing change.
- Updated both existing scheduled task prompts to main-only; first one-time run has fired, second remains scheduled 2026-11-15 02:14:13 UTC. Schedule times were not changed, no new task created.
- Updated canonical handoff/current-state and shortened AGENTS ledger with verbatim archive above. Next: verify cleanup, remove temporary workflow, report release/checklist; actual phone acceptance remains the phase gate.

## 2026-11-14 — Branch cleanup verified; temporary workflow removed
- Cleanup commit bb189a184afeb6f07784d4f85e5540e18c4a9128; dedicated workflow https://github.com/Batmancpu/Player/actions/runs/43642157896 completed SUCCESS. All eight allowlisted task heads were checked unchanged and fully merged before deletion. Fresh GitHub branches API returns only main. Main/history/releases unchanged; no backup branch needed and no user manual deletion required.
- Removed .github/workflows/cleanup-merged-branches.yml immediately after verification. No permanent branch-management automation or new branches remain. Both scheduled prompts respect latest main-only policy; second run still 02:14 UTC Nov 15.
- README updated with actual signed v0.1.79/CI #79, offline Phase 1 scope, future features, real-device gate and correct canonical links; removed contradictory old signing-setup statement and literal escaped newlines/duplicate links.
- AGENTS/handoff/history/context/decision state updated for completed cleanup. No application/dependency/signing/release update. Release artifact and CI #79 remain verified; this documentation-only follow-up CI is pending at authoring.
- Exact next action: user updates signed v0.1.79 through Obtainium without uninstalling and reports docs/PHASE1_PHONE_TEST.md outcomes. Agent triages failures on main. Until report, Phase 1 is unaccepted; no gated Phase 2 production or actual FPS claim. Safe Phase 1 tests/research are authorized, no duplicate candidate release needed.

## 2026-11-15 — Scheduled continuation: main-thread scan/index work
- Live baseline fb7865915729535fd8525651e686a000cf1045e5 is green (CI 43642359036). Only main exists, no open PRs, latest signed release remains v0.1.79 at f1d7f27. No actual OPPO acceptance received; do not repeat publication or advance gated Phase 2.
- H-014 source audit: LibrarySession invokes callbacks on ViewModel main scope; SAF per-query calls dispatched to IO, but traversal/grant preflight resumed on main. OfflineLibraryIndex.saveSuccessfulScan maps/parses records before Room transaction without a dispatcher boundary; load maps after DAO suspension on caller context. Large libraries could block UI, cancellation could not interrupt synchronous record mapping. No measured phone hitch/ANR reported.
- SafLocalMediaSource.scan now wraps traversal/preflight in Dispatchers.IO. OfflineLibraryIndex public entry points wrap database creation, parsing/mapping and transaction invocation in IO; no source writes or changed reconciliation semantics. Mapping checks the coroutine context per row. Room transaction mapping stays inside its transaction context; no destructive migration/schema/dependency/permission/signing change.
- Added Android integration test invoking save/activate/load from Dispatchers.Main for 1,000 synthetic items then a 100-item rescan; checks full expected identity sets. This verifies functional bulk reconciliation/main-caller compatibility, not measured frame time or proof of a handset FPS target. Existing rollback/isolation/cursor/launcher tests retained.
- Local SDK/Gradle absent; full Android/unit/build/signing/privacy CI pending. No new GitHub Release requested; this fix is unreleased main work while v0.1.79 awaits phone testing. Next: inspect CI and record actual result, then actual user OPPO report/bug triage.

## 2026-11-15 — H-014 verification and scheduled-run handoff
- Functional commit 57e453980e51b676aa4f4e81ef88dd8f65aea906, directly on main. Full workflow https://github.com/Batmancpu/Player/actions/runs/43667022100 (#82) completed SUCCESS. Android job 132668563815 logs show Starting 10 tests on Android 15 AVD, BUILD SUCCESSFUL in 1m 39s; new 1,000→100 main-caller regression passed with existing isolation/rollback/cursor/lifecycle cases. Verification job 132669426157 passed Python verifier fixtures, JVM/debug/test-APK, signed release assembly, actual manifest/privacy/identity and permanent certificate pin. No per-frame/thermal/OPPO performance evidence.
- Existing platform guidance reviewed: https://developer.android.com/kotlin/coroutines/coroutines-best-practices#main-safe — suspend data operations own thread dispatch; suspension alone does not move subsequent processing off caller. No upstream source copied/dependency adopted. Dispatcher injection remains a potential later testability refinement, not part of this focused fix.
- Existing v0.1.79 Release/asset unchanged; #82 publication correctly skipped. Signed CI v82 is not an announced installation/update channel. H-014 is unreleased main work, so it must not be attributed to v0.1.79 or to a successful real handset test.
- Confirmed no open issues/PRs and main-only branch policy. Both requested one-time continuations have fired; no new task created. No actual phone report received, no gated Phase 2 production added. Next action is the real OPPO checklist in docs/PHASE1_PHONE_TEST.md and direct-main bug triage when results arrive. Documentation follow-up carries this exact verification; its own CI is pending at authoring.

## 2026-11-15 — User report, Zenith research and offline playback dependency gate
- User installed signed v0.1.79 through Obtainium, selected folder, reopened in airplane mode with retained library and switched folders successfully. User called media audio files; asked optional clarification before assuming MP3 indexing or verified video classification. Cancellation effect unclear; user-reported broken floating Glass Lab navigation. No screenshot/native trace/FPS evidence; do not invent exact renderer root cause. Clarified removal test meant user-managed test copies, never an app delete feature.
- User explicitly requests a larger useful upgrade and progress beyond unattended micro-fixes. Implement a broader offline preview (search/grouped browsing/details/local playback), fix visible feedback and lab dock while retaining incomplete phase/device status. No online metadata/permission approval implied.
- Researched Batmancpu/FCZenith and its immediate parent rezapranata089-bit/Zenith (parent head 26c5ed8496b7beb5f248ee93f26fbe73e3df47dd). FC Zenith rendering headers identify vendored Kyant0/backdrop v2.0.0 Apache-2.0; LiquidGlass/GlassNavBar glue explicitly adapted from Echo Music under GPL-3.0. Rendering uses downsampled source, blur/lens/tint/highlight; not evidence of OPPO speed. No GPL glue/Apple assets copied. Haze 2.1.0 performance docs recommend Regular/Default, bounded shared effects and physical-device measurement.
- Isolated dependencies: Media3 ExoPlayer + UI pinned 1.11.1 (official stable Nov 11, https://developer.android.com/jetpack/androidx/releases/media3), Apache-2.0 LICENSE inspected at exact upstream tag. ExoPlayer manifest requests ACCESS_NETWORK_STATE/WAKE_LOCK; app explicitly removes both in merge, no INTERNET added. Selected modules only, no DASH/HLS/RTSP/ads/Cast/native decoder extensions. Actual transitive manifest/build gate must pass before player integration. Local ContentDataSource factory, no connectivity observer and disabled platform diagnostics planned; no generic HTTP factory. Real codec coverage/APK-size/device efficiency still need evidence.
- Baseline c12929b green CI 43667702441; no branch/PR created, identity/key unchanged. CI pending dependency gate before source player integration.

## 2026-11-15 — Useful offline catalogue and local-player preview
- Added pure LocalCatalogue grouping/sorting/search/filter over actual indexed videos; series grouped once, unknown/multi-episode names retain original identity. ViewModel computes snapshots on Default with tree identity; UI refuses previous-tree snapshots. New LocalLibraryScreen shows adaptive title cards (honest local placeholders), search, All/Movies/Series filters, movie/episode sheets and Play. Manage sheet contains rescan/change-folder/optional Glass preview. Rescan/cancel show explicit snackbar feedback; fast completed scans distinguished from cancellation. No app source deletion action.
- LocalPlaybackAccess rebuilds only selected external-storage tree document URIs after persisted read/containment validation. ViewModel checks current indexed video membership and generation/root after asynchronous validation. No externally supplied URL intent or generic network media source.
- Media3 local factory uses ContentDataSource only, BandwidthMeter.NO_OP and setUsePlatformDiagnostics(false); audio focus/noisy handling, foreground lifecycle pause/release, window keep-screen-on (no WAKE_LOCK), subtitle button and default seek controls. Private hashed document resume keys, background checkpoints; original media unchanged. Device codec/format coverage not promised; hardware/software decode still needs corpus/OPPO evidence.
- MainActivity routes playback and error/loading states, retaining VM selection across rotation. New interface is original Compose with current licensed Lucide assets and resource-backed strings; no GPL Zenith glue, Apple assets, network, ads or metadata lookup. Preview advances useful local parts without declaring all six phases complete. Source prepared while isolated dependency CI runs; integration/test gates pending.

## 2026-11-15 — Actual video confirmation and coherent Glass preview
- User clarified audio was speech misinterpretation: all four movies recognized correctly with year in brackets; selection/offline retention/switching work. Record positive physical evidence; do not introduce music scanning. File-change/permission-revocation tests and measured Glass performance still not supplied.
- GlassPreviewDock is original Compose, one bounded shared Haze Regular surface (max420dp), four equal accessible selectable tabs and a spring-driven translucent selection pill. Removed the old four independent Clear surfaces plus merging container from the screen. Preview content scrolls and reserves dock space; switch retains same geometry in fallback. No GPL Zenith glue or Apple assets copied, no custom shader/JNI/native backdrop. Geometry/source integration changed; exact OPPO pixel defect and frame timings remain unverified until new build/user evidence.
- This repairs the preview implementation approach without declaring Haze superior to Zenith/Kyant; benchmark engine comparison remains later measured work. Tests/screenshot and integration CI pending.

- 2026-11-15 — Preview verification: added owned H.264/AAC content-provider decode/seek, deny remote/ungranted playback and private resume-isolation Android cases; catalogue search/details/play identity and dock bounds/effects-off UI tests with synthetic screenshots. CI generates the fixture (test assets only), uploads screenshots; no device files/fixture provider in release. Media3 Apache-2.0 notices packaged. Dependency gate #84 passed; integration CI pending. Next: inspect rendered screenshots and full signed privacy gate before publication.

- 2026-11-15 — Latest user authorizes next planned phase implementation without routine APK-install pauses while absent. One requested continuation confirmed for today 13:30 Asia/Kolkata (08:00 UTC); inherit latest GitHub state, no task branches. Continue independent implementation/tests and record phone acceptance/performance as pending; preserve privacy/provider controls. Current preview source 3c62a71, CI #85 (43676678448) in progress. Next: finish preview gate, inspect screenshots, publish, then next roadmap work without inventing acceptance.

- 2026-11-15 — CI #85 (43676678448) stopped before compilation/tests: ubuntu-latest 26.04 lacks ffmpeg. Generate-fixture step now explicitly installs the distro package when absent; app dependencies/permissions unchanged. No APK published and no decode/UI pass claimed. Reconciled partial phone-check wording/preview architecture and recorded the explicitly requested 13:30 IST continuation. Next: rerun full gate from this corrective commit, inspect failures/screenshots.

- 2026-11-15 — CI #86 / 43676928900 compiled app/test sources and ran 15 Android cases; first-frame test exposed ExoPlayer.Builder 1.11.1 explicitly rejecting BandwidthMeter.NO_OP. Replaced it with an original fixed positive local-track estimate (four API methods, no transfers/connectivity observer); preserves content-only source and removed network/wake permissions. Other 14 cases passed, full playback/signing gate pending. No APK from failing run. Next: rerun actual decode/seek plus full gate and inspect screenshots.

- 2026-11-15 — Screenshot artifact audit: two UI scenarios passed in #86 but post-test adb pull found no app external directory; connected-test teardown removes package-owned output. Test-only UI automation now captures synthetic screens under shell-owned Download/player-preview-screens so CI can retrieve after teardown, without adding app storage permission. Release notes/checklist describe substantial offline preview honestly; current decode fix gate pending. Next: inspect actual captured images, then signed publication.

- 2026-11-15 — Local playback correction 8d315b3 verified by CI #87 (43677324287): all15 Android15 cases including first decoded frame/seek; JVM/debug/test APK; signed release and actual manifest offline permissions/backup/provider/identity; permanent certificate all passed. Source permission boundary unchanged; publication skipped. Screenshot retention follow-up ce3dbd2 / CI88 running, exact OPPO appearance/performance still pending. Next: inspect retained synthetic screenshots, publish larger preview via existing release trigger, continue next-phase work per D-026.

- 2026-11-15 — Inspected CI88 five retained synthetic PNGs: dock is one bounded surface but bright material washes out white labels; 320dp catalogue has one oversized column and clipped Shows chip. Added deterministic dark content scrim/softer pill over real bounded Glass, 132dp adaptive cells, moved Rescan beside counts to free filters, scrollable long sheets and weighted preview heading. UI regression now requires both movie cards visible in separate columns. Exact OPPO/performance still pending. Next: rerun screenshots/full gate before publication; no aesthetic pass inferred from bounds tests.

- 2026-11-15 — CI89 compiled and confirmed the two-column layout, but search UI test asserted absence immediately after asynchronous accessibility text edit; now-visible second card exposed that synchronization gap (old one-column test could pass because the card was offscreen). Wait for the nonmatching accessible title to disappear with a bounded10s assertion, retaining correct-result/detail/play checks. No app behavior or assertion removed. Next: full gate and inspect revised synthetic images before release.

- 2026-11-15 — User explicitly requested stopping until the next automation; development paused. Latest functional source e4cc4b1 (main), CI90 / 43678768709 in progress at checkpoint; CI87/88 passed full automated/signing/privacy gates, CI89 screenshots visually confirmed corrected two-column catalogue/filter allocation and readable bounded dock, but search test required asynchronous-result synchronization (fixed in e4cc4b1). No larger preview Release published; v0.1.79 remains latest installed candidate. Confirmed one-time continuation today13:30 Asia/Kolkata (08:00 UTC). Next: inspect CI90 and retained images; repair only actual failures, publish verified larger signed preview through release(test) gate, then next-phase schema/metadata work per D-026 without routine phone-install pauses. This shutdown checkpoint is docs-only; skip redundant CI.


## 2026-11-15 — Preview CI90 verified and release gate resumed

- Live main review: latest functional source `e4cc4b13acbd3ffa387a0dfc55f56d8fae34fcf2`, followed by a docs-only checkpoint. GitHub reports one branch (`main`), no open PRs and latest Release v0.1.79.
- CI #90 / [43678768709](https://github.com/Batmancpu/Player/actions/runs/43678768709) completed green. Both jobs passed: 15 Android 15 integration cases, JVM tests/debug assembly, release manifest privacy/backup/app ID checks, signed APK assembly and pinned permanent certificate verification. Release publication was skipped by design for the ordinary push. The run retained `catalogue.png`, `search.png`, `details.png`, `glass-on.png` and `glass-off.png`; all five were downloaded and inspected. The Glass dock fits and its enabled/disabled states render; these synthetic emulator images do not establish OPPO appearance/FPS/thermal behavior.
- H-020 (search test's asynchronous accessibility update) is resolved by a bounded result wait with assertions preserved; full CI90 is green. Source implementation has not changed since e4cc4b1.
- Current output before this docs checkpoint remains a signed artifact from run90, not a GitHub Release. No preview APK has been published; v0.1.79 is still the current Obtainium Release. Next action: use the repository's existing `release(test):` push trigger to run all gates and publish the next signed version, then verify the public tag, asset digest, app ID and certificate before sharing the short phone checklist.
- After release verification, proceed with D-026 Phase 2 groundwork (Room schema export and offline normalized metadata-request/opt-in tests); do not add HTTP, a secret or network permission in this slice. Actual device acceptance and frame-time results remain pending.


## 2026-11-15 — Signed offline preview published

- Release-trigger commit `36e6c684f8191037c183603139de2e0891b832c8` passed [CI #91](https://github.com/Batmancpu/Player/actions/runs/43726886945): Android 15 emulator integration (15 tests, 0 failures), JVM/debug build, signed release APK, production manifest privacy/identity, and permanent signing certificate. Both CI jobs and the release publication step succeeded.
- Published [Player v0.1.91](https://github.com/Batmancpu/Player/releases/tag/v0.1.91), app ID `com.batmancpu.player`, versionCode 91 (up from 79). APK SHA-256 is `7c9ff8287405be9b1f607259608d84a256e5aed1fcc4cea24e3f9b882dd05e17`; Release asset digest matches. Permanent certificate remains `4D:2F:17:89:ED:27:D3:96:13:35:FE:36:45:64:55:BD:9A:31:0B:0D:CD:F4:1E:8C:CC:92:53:98:33:9E:80:BF`. No signing secret or key material changed.
- CI test XML confirms 15/15 Android15 tests pass, including a synthetic H.264/AAC content-only first-frame/seek case, provider isolation, resume isolation, catalogue/search/details/play identity, dock bounds/fallback and scan/index regression cases. The five synthetic screenshots were reviewed. These do not establish OPPO codec coverage, Glass quality/performance or phase acceptance.
- User's v0.1.79 OPPO report remains: four videos/names/years, selected folder, airplane-mode persistence and folder switching passed. The newer preview remains untested on handset; cancellation/recovery and measured FPS remain open evidence.
- Next: preserve the short v0.1.91 OPPO checklist, then capture Room v1 JSON schema through pinned KSP/Room tooling before adding metadata columns or any migration. Android's official Room guidance says to commit exported schemas and use Gradle argument providers for incremental correctness: https://developer.android.com/training/data-storage/room/migrating-db-versions.html . No network permission/API integration is part of this database slice.


## 2026-11-15 — Begin Room v1 schema history

- Added a Gradle `CommandLineArgumentProvider` for `room.schemaLocation`, marked its schema directory as a relative-path task input, and enabled `@Database(exportSchema=true)` without changing database version1 or entity fields. CI checks that the generated JSON contains exactly the known v1 `indexed_media` entity, then uploads it as `Player-Room-Schema-${GITHUB_SHA}` for review before committing the snapshot.
- This is schema/export plumbing only: no migration, data mutation, app permission, network/HTTP feature, new dependency or released-app runtime behavior. No generated schema was manually fabricated; it must come from the pinned Room KSP build.
- Source and YAML structure were inspected locally; this environment lacks Gradle/Android SDK. Full CI #92, Room KSP output and artifact verification are pending. Next: inspect the exact CI schema JSON, commit it under `app/schemas/`, then define normalized, consent-gated metadata query value types with offline tests before any HTTP integration.
- Research: Android's migration guide documents `room.schemaLocation`, recommends versioned schema files in source control, and provides an input-tracked `CommandLineArgumentProvider` pattern for KSP: https://developer.android.com/training/data-storage/room/migrating-db-versions.html .


## 2026-11-15 — Preserve the exact Room v1 schema baseline

- CI #92 attempt 1 failed before compilation because Gradle could not resolve pinned Compose plugin 2.4.21. The same plugin/build had passed CI #91; inspected the failure, then reran only the failed jobs. Attempt 2 passed both jobs: https://github.com/Batmancpu/Player/actions/runs/43728517965 . This was transient dependency resolution, not a code/test failure.
- The Android 15 XML report was inspected: 15 tests, 0 failures/errors/skips, including SAF isolation, Room reopen/reconciliation, playback decode/seek and UI-preview scenarios. The verification job passed JVM, debug/release assembly, privacy manifest and permanent signing-certificate checks. This non-release commit did not publish another APK.
- Downloaded `Player-Room-Schema-1e4e63a473bb20f90119412acb1ed1a69f3fbab9` artifact and preserved its exact generated `com.batmancpu.player.ui.library.PlayerLibraryDatabase/1.json` at `app/schemas/`. Reviewed format/database versions, all ten columns, composite `(treeUri, documentId)` primary key and single `indexed_media` table; schema SHA-256 is `820f2e814c9fcc3780be7c4bf6a1cbcb2498d52d1f92a0439b58aef11c9d6d04`. No user rows are present.
- This stores build-time migration history only; DB version/entity/runtime, permissions, source-media behavior, network, app ID and signing key are unchanged. Next: implement the offline normalized title/year/type query contract with opt-in default off and tests, without an HTTP client. v0.1.91 preview and performance remain unverified on the OPPO.


## 2026-11-15 — Offline normalized TMDB query and consent contract

- Added a pure Kotlin value model that can contain only normalized title, optional year and typed movie/TV kind. Consent defaults to disabled; only recognized parser output yields query terms after an explicit enabled state. TV episode season/number, local filename, SAF URI/document identity and media bytes are absent from the type.
- Normalize Unicode to NFC and whitespace to one space; reject controls, slash/backslash/path/URI prefixes, media/subtitle/document suffixes, blank titles and titles above 120 Unicode code points. Invalid years are omitted. No data is sent; there is no client, credential storage, network dependency, permission or settings UI in this slice.
- Added seven JVM cases for the default-off gate, movie fields, series-only lookup, ambiguous names, path/URI/filename/control/length rejection, Unicode and invalid-year omission. CI #94 / 43731972463 passed; all seven cases ran under `:app:testDebugUnitTest`.
- Official TMDB references reviewed: https://developer.themoviedb.org/reference/search-movie and https://developer.themoviedb.org/reference/search-tv expose separate search paths and optional year fields; the official FAQ (https://developer.themoviedb.org/docs/faq) requires attribution and the notice “This product uses the TMDB API but is not endorsed or certified by TMDB.” plus an approved logo within About/Credits before using the API/data. No TMDB API use is implemented by this commit.
- Next: CI verification. If green, proceed to the user-controlled credential and endpoint allowlist design; keep network access absent until that review is complete. Actual OPPO acceptance/performance remain pending.


## 2026-11-15 — CI verification for offline metadata query contract

- CI #94 / https://github.com/Batmancpu/Player/actions/runs/43731972463 completed successfully. Android 15 report: 15 tests, 0 failures/errors/skips. Unit-test/build task passed including the seven new query-contract tests. The Room v1 schema validator, signed APK assembly, production privacy manifest, package identity and permanent release-certificate check all passed.
- Retained CI artifacts: `Player-Android15-tests-935601313171e1f43bb6632bb693787b1f84720f`, `Player-Room-Schema-935601313171e1f43bb6632bb693787b1f84720f`, and CI-only signed release artifact `Player-release-v94-935601313171e1f43bb6632bb693787b1f84720f`. No GitHub Release was published; versionCode 94 was built only for verification.
- No network/API client, credential, setting UI, `INTERNET` permission, runtime query, migration or source-media change. No handset acceptance/FPS evidence. Next: evaluate a privacy-safe explicit opt-in and encrypted user-managed credential design plus exact TMDB API/image-host allowlist before considering HTTP code.


## 2026-11-15 — Room v1→v2 manual override persistence foundation

- Added a separate `manual_tmdb_matches` entity keyed by selected tree/document ID; stores only provider ID, media kind and local confirmation timestamp. `indexed_media` v1 fields remain unchanged.
- Added explicit `MIGRATION_1_2` and registered it on the production Room builder. The first Android test used `MigrationTestHelper`, but CI #95 failed before migration execution with an `AbstractMethodError` while Room's serializer decoded the schema JSON. The corrective test now builds a version-1 SQLite database directly from the committed v1 schema JSON asset and opens it through the production Room builder; this exercises the actual migration and Room's v2 validation without that broken helper path. Verification of the corrective harness is pending CI #96.
- Successful scan reconciliation removes overrides for absent documents in the same transaction; selected-tree activation and `forgetTree` remove matching overrides transactionally. No cascading FK is used because media indexing uses `INSERT OR REPLACE`.
- CI schema check now requires the unchanged v1 snapshot plus exactly the generated v2 schema and verifies the manual-match columns/composite identity. Room testing is test-only; no release runtime dependency or permission change. CI and exported v2 JSON pending.
- Official migration reference: https://developer.android.com/training/data-storage/room/migrating-db-versions.html . MigrationTestHelper failure evidence: run 43734224640, `SchemaBundle.deserialize` threw `AbstractMethodError` for `GeneratedSerializer.typeParametersSerializers()` before migration ran. No network/client/settings/manual matching UI is implemented.

## 2026-11-15 — Correct Room migration integration-test harness

- Corrective source commit SHA and CI #96 are pending. Removed the Room testing helper dependency that failed while decoding the schema bundle in Android CI #95. The Android test now constructs a genuine v1 SQLite DB from the exact committed Room v1 JSON, seeds one synthetic library record, sets Room's v1 identity hash/user version, then opens it with the production v1→v2 migration and verifies preserved rows plus new override DAO operations.
- The production migration and schema are unchanged; no destructive fallback, permission, network, or media-file access was added. CI #95 logs confirm the earlier error occurred before migration execution, so do not count its attempted migration case as verified.
- Next: inspect CI #96 Android test report, generated v2 schema artifact and signed privacy/identity gates. No Release or actual-device evidence is associated with this change.



## 2026-11-15 — CI #96 migration pass and cache-sensitive schema export

- CI #96 / 43735914768 Android 15 XML contains 17 tests, 0 failures/errors/skips. The replacement fixture test built a v1 SQLite DB from the exact committed Room v1 JSON and verified the production migration retained its row and supports the new DAO.
- Verify job 132880042081 passed Python fixtures and unit/build/test-APK tasks, then failed at the schema-file presence check. Logs show `:app:kspDebugKotlin FROM-CACHE`; KSP's Room schema JSON side effect was not restored. The schema gate stopped downstream signed APK, privacy and permanent-certificate checks; those must not be inferred as passed.
- H-021 is resolved by the passing Android migration case. H-022 records the separate build-cache omission. No app behavior/release permission/schema/runtime changed; no APK release or device acceptance.

## 2026-11-15 — Force Room schema export before validation

- CI now runs `gradle --no-daemon --rerun-tasks :app:kspDebugKotlin` immediately before validating Room schema snapshots. This explicitly regenerates the KSP schema JSON instead of relying on a cached task result.
- No app runtime or production DB behavior changed. CI #97 is pending to verify exact v1/v2 JSON generation, upload the artifact and continue signed identity/privacy/certificate checks.
- Next: inspect CI #97; confirm v1 is byte-identical and v2 matches the entity/migration, then commit the exact generated v2 JSON. No new Release, OPPO acceptance or performance claim.


## 2026-11-15 — Verify and preserve Room v2 snapshot

- CI #97 / 43737778787 completed successfully on main commit 241e08427c1a35ec9fa1b2b2ecc4b3677fe06c0b. Android 15 instrumentation XML reports 17 tests, 0 failures/errors/skips, including v1 row preservation and manual-match DAO operations.
- The cache-safe KSP export step generated both JSON snapshots. v1 exactly matches the committed baseline (SHA-256 `820f2e814c9fcc3780be7c4bf6a1cbcb2498d52d1f92a0439b58aef11c9d6d04`). Reviewed v2 output: version 2, unchanged `indexed_media`, plus only `manual_tmdb_matches` with composite primary key `(treeUri, documentId)` and fields `tmdbId`, `mediaType`, `confirmedAtEpochMs`; exact artifact SHA-256 `6d2a1dd422834f5cadf3e6c4292aed393bf2a784adc3cef1108ba9975c0bd4fe`. The generated file is committed verbatim.
- CI also passed JVM/unit/debug build, release assembly, exported schema validation, production manifest privacy/identity (no unexpected permissions; backup remains false), APK signature and the pinned permanent certificate. VersionCode 97 artifact is CI-only and not a GitHub Release; no phone acceptance/performance result.
- H-021 and H-022 are resolved. No network/client/permission, runtime app behavior or signing identity changed.
- Next: add explicit Android 12+ cloud/device-transfer backup exclusions and Android 11-and-lower legacy exclusions; validate source and packaged rules. Only then store any credential or add opt-in network integration.


## 2026-11-15 — Explicit Android backup exclusions

- Retained `android:allowBackup="false"` and attached `@xml/backup_rules` (`fullBackupContent`) for Android 11 and lower plus `@xml/data_extraction_rules` for Android 12+ cloud backup and device-to-device transfer. Both configurations exclude all nine documented app-data domains at path `.`; cache/no-backup remain platform excluded.
- Added tests that verify the manifest references both rules, each mode excludes every domain with no include rules, and the release manifest checker rejects absent rule resources. The packaged-APK check still verifies the stable application ID/versionCode, no unexpected production permission, no debug provider and disabled `allowBackup`.
- Why: Android's official backup documentation warns some Android 12+ manufacturers may still allow device-to-device transfer when `allowBackup=false`; a missing extraction-rule mode defaults to all app data. Official reference: https://developer.android.com/identity/data/autobackup .
- Local focused regression run: `python3 -m unittest discover -s scripts -p 'test_*.py' -v` in an isolated tree containing the exact patch — 7 tests passed. CI #99 / run 43740997631 passed: Android 15 instrumentation 17/17 with 0 failures/errors/skips; the full Python script suite; unit/debug/release builds; both Room schema validation; compiled release manifest showing `allowBackup=false` plus both backup-rule references and no unexpected permission/debug provider; APK signature; and the pinned permanent certificate. Signed versionCode 99 artifact SHA-256 `29f3945c4c8aa5e1501e90a3f05ad5de7685d7acc543821bb3fc74e57f9296fb` is CI-only. GitHub Release publication was skipped. No app runtime, permission, network, Room schema or signing changed.
- Cross-platform transfer: Android 16 QPR2/API 36.1 adds optional Android↔iOS transfer rules requiring a real iOS bundle ID, Team ID and content version. Player has no iOS counterpart; these values are not invented and cross-platform transfer is not claimed as excluded. Revisit only if a real iOS app and identifiers are approved.
- H-023 is resolved for configured Android backup modes by CI #99. Do not store credentials or add HTTP until separate user-opt-in, destination and credential protections are reviewed. OPPO acceptance/performance and Release v0.1.91 handset verification remain pending.


## 2026-11-21 — Defer user-facing APK to the complete app candidate

- User clarified that they want one APK to test after the full app is integrated, rather than an APK at every development step. This supersedes per-phase phone-install pauses; the six phases remain the engineering order and automated CI gate.
- CI still compiles/runs debug instrumentation binaries for regression coverage, but no longer uploads the intermediate debug APK artifact. Release signing and the installable signed APK artifact are limited to an explicit release-candidate request (workflow dispatch with publish enabled or the controlled `release(test):` commit). Only publish that candidate after complete-app readiness.
- The existing `com.batmancpu.player` identity, permanent certificate, versionCode policy, privacy boundaries and main-only workflow are preserved. Current latest published Release remains v0.1.91; no new user test APK is produced by this policy commit.
- Verification: pending CI on this workflow change. Next continue the opt-in TMDB/artwork integration and later premium UI/player phases without phone-install pauses; request the user’s one test only after the integrated app candidate is ready.


## 2026-11-21 — Offline provider metadata cache foundation

- Extended the Room database from v2 to v3 with `cached_tmdb_metadata`, keyed only by provider TMDB ID and media type. Added an explicit 2→3 migration; v1→2 remains unchanged.
- Metadata is exposed to a selected library only through an explicit per-document match. Confirmation validates provider identity, bounded text/image paths, and indexed video identities inside a transaction. Successful full rescans remove stale bindings and orphan provider metadata; failed/cancelled scans retain the existing index.
- Added Android instrumentation for the v1→v3 production migration, visibility only after explicit confirmation, rejection of unknown document IDs/unsafe artwork paths, and stale-cache cleanup. CI now validates and uploads exact v1/v2/v3 schema snapshots.
- No network request, INTERNET permission, credential flow, source-media write or downloadable APK is part of this change. CI #102 / 44888106152 passed Android 15 instrumentation 20/20 (0 failures/errors/skips), JVM/build/regression gates and exported v1/v2/v3 schema validation. The exact generated v3 snapshot was inspected and committed with SHA-256 a48bab777b7dcad1f02f0e61a1a936b95f91eca61265a1ede03d7cccbc4450db. Release signing, manifest/signature checks and publication were skipped because this was not a candidate request.
- Next: continue explicit opt-in lookup and artwork integration, including user-facing destination disclosure, attribution, cancellation, and offline presentation before preparing one complete phone-test APK.


## 2026-11-21 — Add explicit folder removal without touching media

- Library settings now offer “Remove folder from Player” behind a clear confirmation. It deletes only Player's indexed Room rows/match cache, clears the saved tree URI and releases persisted SAF grants; no ContentResolver delete/write is used, so the original videos/subtitles remain.
- Folder access cleanup clears the saved selection and reconciles all persisted grants under the existing mutex. The ViewModel cancels playback/scan state, retires the library and provides a retry action if Room cleanup or grant release fails.
- Added resource strings, Android phone checklist coverage and fake-store tests for grant release order and failure recovery. CI #103 / 44888950131 passed 20/20 Android 15 and schema/build gates before this UI/service change; CI #104 is pending.
- No versionCode change, signing or installable APK. Next: verify CI #104 and fix failures; then continue the final integrated experience.


## 2026-11-21 — Complete folder removal cleanup

- CI #104 / 44890135563 passed: Android 15 instrumentation 20/20, unit/build, and Room v1/v2/v3 schema validation. The new folder-access tests passed for grant release order and failed-release recovery.
- Tightened removal: once confirmed, clear the app-private resume-position preferences as well as the Room library/match cache. Persisted grant reconciliation now runs in a non-cancellable section so lifecycle cancellation cannot leave stale SAF access. Original videos/subtitles remain untouched.
- Added a UI integration test that checks the explanation, verifies cancel makes no call, and verifies confirmation invokes removal once; added resume-state cleanup coverage. CI #105 pending. No Release APK or versionCode change.


## 2026-11-21 — Diagnose synthetic UI accessibility failures

- CI #105 / 44890893488 passed Room, SAF, playback, metadata-cache and unit/build/schema checks, but all three PreviewUiAndroidTest cases timed out waiting for text nodes (Shows, Home, and Manage). The new removal/resume storage behavior itself passed its Android checks.
- Added a bounded accessibility-tree snapshot to the synthetic UI test timeout message. This will reveal whether CI is seeing the expected Compose window with changed labels, a different active window, or an empty tree. The test fixture uses only synthetic media names; no user files are read or logged.
- Diagnostic instrumentation only; no product UI, versionCode, APK signing or Release change. CI #106 pending.
- Next: inspect the active-window snapshot and fix the underlying UI or query before proceeding with metadata integration.


## 2026-11-21 — CI verification of folder-removal follow-up

- CI #105 / 44890893488 reported three synthetic UI lookup timeouts, while Room, playback, SAF and unit/build/schema checks passed. The root cause was not captured in that run.
- CI #106 / 44891844084 passed: Android 15 instrumentation 22/22, all synthetic UI, folder removal/resume cleanup, Room migration/schema, unit/build/privacy gates. The timeout diagnostic remained unused because the failure did not reproduce. Treat #105 as an intermittent UI-test failure with no confirmed product regression, not as a resolved root-cause finding.
- Release signing/publication were skipped; no user-installable APK was produced. Actual OPPO behavior and frame-time remain unverified.
- Next: integrate the opt-in manual TMDB lookup and confirmation path against the offline provider cache, with explicit title/year/type disclosure, strict host/redirect/size controls, cancellation, no automatic queries, and tests. Do not enable networking until that full flow and attribution pass review.


## 2026-11-21 — Encrypted metadata connection settings

- Added a Library Manage entry for the metadata connection screen, backed by Android Keystore AES-256-GCM encryption of the user-provided TMDB Read Access Token in no-backup app-private storage.
- Consent remains default-off. Invalid tokens are rejected without replacing the saved credential; disabling revokes use across store instances; clearing deletes ciphertext and key even if the screen coroutine is cancelled. The token stays out of saved Compose state and settings UI state. Added Android tests for disabled first-run state, key absence, encrypted persistence/reopen, opt-in, disable/clear cross-instance revocation, tampering/key loss, malformed input, nonce uniqueness and concurrent revocation.
- This slice adds no INTERNET permission, transport, TMDB request or artwork download. UI explicitly says saving a connection does not start a library lookup in this build. No schema/versionCode/signing/Release change.
- CI #107 for the preceding documentation update is still running; this commit’s Android 15 tests and credential-store verification are pending CI #108. Next: verify the settings lifecycle and proceed only with explicit manual search/match plus official attribution.


## 2026-11-21 — Verify encrypted metadata settings

- CI #108 / 44893247270 passed Android 15 instrumentation 30/30, including the eight new Android Keystore/AtomicFile credential lifecycle cases and existing SAF, playback, metadata-cache, and synthetic UI coverage. Unit tests, debug/release build and Room v1/v2/v3 schema/privacy checks passed.
- CI produced Android test results and the Room schema review artifact only. Signing/publication steps were not requested; no user-installable APK or Release was produced and versionCode/signing identity did not change.
- Fetched the current approved TMDB blue gradient logo asset directly from the URL linked by TMDB's official branding documentation; the SVG SHA-256 is d537fb228cf3ded904ef09b136fe3fec72548ebc1fea3fbbd1ad9e36364db38b. A local PNG conversion was inspected at original aspect ratio; logo still needs to be added to the app's Credits screen before any TMDB API/data use.
- Next: implement user-triggered TMDB search, manual match confirmation to Room, About/Credits with the approved logo and notice, allowlisted/cancellable transport, then separately bounded artwork caching/display. Keep network calls disabled until all pieces are integrated and tested.


## 2026-11-21 — Add provider credits before TMDB data use

- Added an About/Credits route reachable from metadata settings. It uses TMDB's approved blue gradient logo asset from the official logo link, converted SVG→PNG without changing aspect ratio or colors (source SVG SHA-256 d537fb228cf3ded904ef09b136fe3fec72548ebc1fea3fbbd1ad9e36364db38b; PNG SHA-256 80ed9c889db93fdb384b3cc208d59e5697b2a5015a95ff7416e191c83248094b).
- Credits show Player more prominently and display the exact official notice: “This product uses the TMDB API but is not endorsed or certified by TMDB.” The only outbound action is a user-tapped link to themoviedb.org. No request/client/INTERNET permission exists yet.
- Added Android UI coverage for the Credits button, logo accessibility label and notice. No versionCode, database, signing, Release or user-installable APK change. CI #110 pending.
- Next: complete the explicit search→candidate→user-confirmed Room match interaction with fixed TMDB destinations, cancellation/size bounds and no automatic requests; add INTERNET only with that integrated feature.


## 2026-11-21 — Correct Credits callback syntax

- Source review caught an extra escaped quote in the MetadataSettingsScreen Credits click lambda after commit b102860; corrected it to a valid Kotlin string literal before depending on CI output. The app behavior remains: clear the transient token, clear focus, open local Credits.
- CI #110 may report the original commit; verify CI #111 for the corrected head. No installable APK or Release. Next: verify new credits UI tests and continue manual search/match integration.


## 2026-11-21 — Fix Credits screen compile errors

- CI #110 / 44894615964 failed at Kotlin compilation with an escaped quote in the settings Credits callback and an unresolved verticalScroll import from androidx.compose.foundation.layout. The callback was corrected in commit a9e9196; this commit imports verticalScroll from androidx.compose.foundation. Other diagnostics were parser fallout from the quote error.
- CI #111 covers the callback correction; CI #112 will verify both fixes and the Credits UI assertions. No installable APK or Release. Next: inspect #112 and continue the manual search/match feature after green verification.


## 2026-11-21 — Add bounded TMDB request/client foundation

- Added a fixed-origin request policy for normalized movie/series title searches, positive-ID details calls, and fixed-size TMDB artwork destinations. The API client uses only the stored user token in an Authorization header; it never puts credentials in URLs or UI state.
- Added a bounded, redirect-disabled HTTPS transport with 10-second connect and 15-second read limits, 1 MiB JSON cap, at most 20 search results, typed failures, generic headers, and cancellation-triggered disconnect. Search terms carry normalized title/year/type only; no filename, folder URI, episode number, subtitle, or media bytes are sent.
- Added JVM request-policy cases and synthetic Android transport/client tests for consent gates, origin/path constraints, response bounds, credential revocation, typed errors, cancellation and connection cleanup. No real network access is used in tests.
- INTERNET permission and UI dispatch remain absent, so the client cannot be invoked by app users yet. No APK artifact or Release was produced intentionally. CI #113 pending; #112 still verifies the prior Credits repair. Next: integrate the manual search → candidate → explicit match confirmation path into title details and read the cache offline, then add INTERNET permission only with that complete flow and passing privacy gates.


## 2026-11-21 — Make Credits UI test scroll to its action

- CI #112 / 44895129158 compiled successfully and ran the Android 15 suite, where one Credits UI test failed because “About and credits” sits below the initial viewport in the scrollable settings screen. The failure was a test setup issue, not a missing app control.
- Updated the synthetic UI helper to scroll the active screen until the requested label is exposed, then tap the Credits action. This models the actual user path and preserves assertions for the TMDB logo and notice.
- CI #113 contains the bounded TMDB transport groundwork and will also exercise the previous UI test; this fix triggers a follow-up CI run. No APK/Release was produced. Next: verify green UI tests and continue the full manual metadata search/match flow.


## 2026-11-21 — Surface About and credits in the settings header

- CI #113 / 44895567876 passed compilation but repeated the one Android UI failure. The attempted test-only scroll helper could not locate an accessibility scroll action in the Compose settings surface, so no scroll occurred.
- Moved the About and credits action directly below the connection-settings introduction. It is now immediately visible, easier to discover and still opens the local attribution screen. Removed the ineffective helper and restored a direct synthetic tap/assertion.
- CI #114 covers the earlier test fix; this source change triggers a follow-up run. No installable APK or Release. Next: verify the accessibility/attribution screen and continue the explicit TMDB search, candidate selection, confirmation and offline-display flow.


## 2026-11-21 — Integrate manual metadata search and confirmed offline match

- CI #115 / 44896086296 passed after commit 5a467f2: Android 15 instrumentation and UI tests, JVM unit tests, app build, Room schemas and privacy checks. No signed/user-installable APK or Release was created.
- Added a lookup controller scoped to the currently selected tree/title. Opening title details reads only local settings/cache; it never searches. A visible Search TMDB button is available only for safe parsed movie/series names when the user enabled metadata and supplied a token. The request uses normalized title/year/type only, the fixed TMDB API host, no redirects and bounded/cancellable transport.
- Search results require selecting one candidate and tapping a second confirmation. Confirm validates the current title/tree and still-enabled credential before writing provider metadata and matches for the selected movie or all episodes in that series to Room. Users can remove the match; cached details work offline.
- Added synthetic Android controller and UI tests for no request on opening details, manual dispatch, separate confirmation, and cached offline persistence. Expanded the release manifest allowlist and unit regression to permit only the required INTERNET permission plus the AndroidX dynamic receiver permission; no broad media/storage permission was added. No API secret is bundled. Scan, rescan, startup, scroll and local playback do not call the provider.
- Updated privacy, architecture, roadmap, project context and handoff. CI pending. Artwork downloading/rendering, real OPPO provider traffic, Glass polish and performance remain outstanding; no user APK until the integrated app is ready.


## 2026-11-21 — Fix metadata controller test import

- CI #116 / 44898155397 compiled production code but failed before Android instrumentation because the new controller Android test omitted the import for OfflineMetadataCache; its subsequent unresolved title-property diagnostic was a consequence of that missing symbol.
- Added the explicit library-package import. No application behavior or permission changed. No signed/user-installable APK or Release was produced. Follow-up CI must run the controller/UI and privacy-manifest tests before this integrated slice is verified.


## 2026-11-21 — Preserve playback visibility and adapt privacy/UI tests

- CI #117 / 44898595021 compiled production and test sources, then Android 15 instrumentation exposed three issues. The existing privacy test still expected zero INTERNET permission; the new detail panel displaced Play video below the initial viewport; the explicit candidate-selection test needed to scroll inside the sheet.
- Changed the merged-manifest assertion to allow only INTERNET and the AndroidX dynamic receiver permission, while rejecting all other permissions. Moved metadata beneath the existing play/episode controls so local playback remains immediately visible. Added a small real swipe helper for the synthetic candidate UI test.
- No request, provider credential, or release behavior changed. No APK/Release was produced. Follow-up CI must verify controller persistence, UI selection/confirmation and permission checks.


## 2026-11-21 — confirmed offline artwork implementation (verification pending)
Added Coil Compose 3.6.3 Apache-2.0 for app-private local Files only; no Coil networking module. Artwork transport uses fixed image.tmdb.org HTTPS paths, no credentials, redirects off, bounded timeout/bytes/content-type/pixels, cancellable worker and atomic no-backup cache with 96 MiB LRU. Rechecks enabled settings before save. Fixed completion race with tryResume and makes trim errors fail closed. Details only read cached files offline; image fetch begins after explicit match confirmation. Synthetic Android controller test asserts zero CDN requests before confirmation and one after. CI pending; no installable APK/Release.


## 2026-11-21 — poster accessibility resource follow-up
Added the missing localized TalkBack content description required by the confirmed artwork UI. CI #119 is building the feature commit; this follow-up will receive its own build. No release APK.


## 2026-11-21 — synthetic artwork fixture
Static PNG chunk/CRC validation found the first test image fixture was malformed; replaced it with a valid 1×1 RGBA image. This is test-only; no app behavior change. CI runs #119/#120 pending. No release APK.


## 2026-11-21 — artwork cache verification and catalogue poster rendering
CI #119 failed only because Compose referenced a missing poster content-description string. Added the localized TalkBack string. CI #120 was superseded/cancelled when the synthetic test PNG was corrected (CRC check had found the fixture invalid). CI #121 attempt 1 compiled and ran 41 Android 15 tests but five existing Preview UI tests hit a Pixel Launcher ANR; the new metadata artwork test passed. Rerunning the same job as attempt 2 passed Android 15 instrumentation, JVM unit/debug build, Room schema and privacy verification (https://github.com/Batmancpu/Player/actions/runs/44901117586). No signing/release step ran and no user APK was generated. Follow-up displays a cached confirmed poster in visible catalogue tiles: each lazy tile looks up only its already-confirmed metadata and reads local app-private artwork; no card browsing network. It keeps a high contrast dark scrim and fallback gradient/glyph. CI pending; device frame/battery results absent.


## 2026-11-21 — grid artwork CI and Zenith reference review
CI #122 / 44902258363 passed both Android 15 UI/instrumentation plus synthetic screenshots and unit/debug-build/schema/privacy jobs. No signing/release stage or user-installable APK. Lazy catalogue cards use local-only Coil decoding and user-confirmed artwork from app-private cache; missing art uses local gradient/glyph. Test follow-up asserts unsafe routes never call transport and cached image survives disabled TMDB/offline reads. Public-source review: FCZenith currently redirects to Batmancpu/brrMusic, whose license is GPL-3.0; immediate upstream rezapranata089-bit/Zenith also has GPL-3.0. Zenith's README names a further upstream, but Player imports none of their code/assets. This repo has no LICENSE file, so do not copy GPL/proprietary implementations; translate broad visual principles into original Compose. See docs/UPSTREAM_AND_PRIVACY.md. Test follow-up CI pending.


## 2026-11-21 — CI #123 retry and bounded synthetic UI waits
CI #123 / 44902997094 attempt 1: Android tests showed PreviewUi catalogue detail timeout inside Instrumentation.waitForIdleSync at AccessibilityNodeInfo polling, cascading to the next test; MetadataLookupController artwork offline/disable checks passed. Attempt 2 with identical source passed both Android 15 test job and separate unit/debug build, Room schema, privacy gates. No release signing/Release. To remove indefinite global-idle waits, instrumentation now polls accessibility snapshots every 50–100 ms with finite deadlines and polls callback state; swipe/screenshots retain explicit bounded delays. Also revised old library note to reflect optional TMDB artwork/matching while affirming local offline use. CI pending. No phone FPS/performance claim.


## 2026-11-21 — Embedded player track selection

**Change:** Added audio and subtitle track actions to the local player chrome. Actions are shown only after Media3 reports at least one supported track of that type. The official Media3 `TrackSelectionDialogBuilder` presents track names; text tracks include an explicit disable option, while audio cannot be disabled accidentally. Removed the duplicate PlayerView subtitle toggle. No source media is changed and no network access is used. Added localized string resources.

**Reason:** The current player exposed a subtitle toggle but no clear audio-track selector. Embedded track selection is part of the approved local-player scope and is needed before sidecar subtitles and delay controls can be integrated.

**Research:** [AndroidX Media3 TrackSelectionDialogBuilder](https://developer.android.com/reference/androidx/media3/ui/TrackSelectionDialogBuilder), checked 2026-11-21. The builder accepts a Player and a Media3 track type and supports the disable option.

**Verification:** Prior main commit 800d88e1bb512ba8b08729d77d887496dd6131f6 passed CI #124 / 44904367611 (Android 15 integration, unit tests/build, Room schema and privacy gates). This new player UI change has not yet run through CI. No release build or user APK was requested or produced.

**Next:** Verify CI for the track selector, then implement/test local sidecar subtitle association and user-adjustable subtitle delay without modifying source files.


## 2026-11-21 — Corrected player selector integration

A follow-up inspection of the live commit tree showed that commit 9200aaeed66c08eb30f29b27d9d8713f8afe5c67 recorded selector notes but did not contain the intended Kotlin/resource diffs; its CI Android test exercised the previous app source. This was an implementation workflow error, not a product defect. The correction now adds supported-track detection, Media3 audio/text selection dialogs, localized labels, and instrumentation that checks the synthetic file's audio track and opens/dismisses the audio dialog. Track text can be disabled; audio cannot.

Verification: change is unverified until its own CI completes. CI #125 Android job passed but does not verify selector source because it was absent. No user APK or Release was built/published. Next: verify fetched Git blobs match expected source changes, inspect CI, then continue sidecar subtitles.


## 2026-11-21 — SAF parent identity for sidecar subtitles

CI #126 / 44906299456 passed against commit fdbcd895dfa7f25e5f3b3843c3d58020f8f14b48: Android 15 integration (including Media3 audio selector open/dismiss), unit/build, Room schema and privacy gates. The earlier selector correction is verified.

New work captures the opaque immediate parent document ID at the point the scanner enumerates a child; it never decodes IDs or treats a display name as a path. A pure local matcher selects only supported subtitle suffixes next to the video under the same parent, supports exact stems plus constrained language/forced/SDH suffixes, returns deterministic capped results, and refuses candidates without known parent identity. Tests cover folder isolation, filename near-misses, unsupported .sub sidecars, opaque ID behavior and bounds.

Verification: pending CI for this scanner/matcher change. Parent ID is not yet stored in Room and sidecars are not yet attached to Media3; that is the next schema migration/integration slice. No user APK or Release.


## 2026-11-21 — Sidecar matcher test fixture correction

CI #127 / 44907206092: Android 15 instrumentation passed, including scanner traversal. Unit verification failed only in `LocalSubtitleMatcherTest.capsSubtitleCandidates`: the test constructed files named `Film.1.srt` through `Film.12.srt`, none of which satisfy the matcher's exact/language suffix rules for `Film.mp4`. The assertion expected eight matches from unrelated synthetic names. Corrected the fixture to use twelve distinct document identities with a valid `Film.en.srt` stem so the result cap is actually exercised. No production code change.

Next: run full CI on the correction, then continue with the Room parent-ID migration. No APK/Release.


## 2026-11-21 — Subtitle matcher root-locale regression

CI #127 / 44907206092 Android 15 integration passed. First JVM attempt failed only because the cap test's generated names were unrelated to the target movie. CI #128 / 44907892171 Android 15 integration passed with the corrected valid fixture but the cap test still failed. Inspection found a production defect: the root Locale can have a blank ISO3 value; after removing the `.en` suffix from simple `Film.en`, a later pass could treat the blank suffix as a language and strip the base to empty.

The matcher now filters blank locale codes and refuses blank suffix tokens. A direct `Film.en.srt` regression test was added; the cap test retains twelve distinct matching identities. H-024 records the reproduction and root cause. Verification pending a new full CI run. No APK/Release.


## 2026-11-21 — Persist SAF parent identity (Room v4)

CI #129 / 44908615223 passed after excluding blank root-locale values from subtitle suffix matching; the direct simple-title regression, matcher cap, Android 15 integration, unit/build and v1-v3 schema/privacy gates all passed.

This change adds a non-null `parentDocumentId` to indexed media with SQL default `''`, Room migration 3→4 using `ALTER TABLE`, and round-trip mapping for successful scans/offline loads. Existing v1 rows migrate without loss and receive the explicit empty value; they gain sidecar associations after a successful rescan. Migration instrumentation now opens v1 through v4 and verifies old media, metadata and defaulted parent identity. CI schema validation preserves exact v1-v3 and checks generated v4 column order/default; generated schema artifact is retained for committing the canonical v4 snapshot.

Verification pending. Playback has not been attached to sidecars yet. No Release/user APK.


## 2026-11-21 — Room v4 schema artifact recorded

CI #130 / 44909463881 passed the Android 15 migration and offline persistence tests, JVM/build, Room schema and privacy gates. The v1→v4 migration preserves the old media row and supplies the empty parent default; the offline index round-trip test retains the scanned parent ID. Retrieved the generated Room schema artifact (14709327372) and committed exact v4 JSON snapshot; historical v1-v3 snapshots are unchanged. The workflow validates field order and default and now includes v4 in the uploaded schema artifact.

Next: validate same-tree sidecar candidates into bounded local subtitle configurations and test Media3 playback using the synthetic read-only provider. No Release APK or device-performance claim.


## 2026-11-21 — SAF sidecar subtitles attached to local Media3 playback

- Added a playback subtitle descriptor and MediaItem builder for bounded SRT/VTT/ASS/SSA/TTML sidecars. Candidates come only from the offline index's same-parent filename matcher.
- At play time, the app revalidates the persisted read-only tree grant, verifies the video is still an immediate child of its recorded parent, then queries a fresh bounded listing (10,000 rows maximum) and attaches only exact candidate document IDs/names still present as files in that parent. A stale/unavailable sidecar/provider failure is skipped without blocking video playback. No paths, bytes, upload, or writes are introduced.
- Synthetic Android integration fixture now serves a local SRT beside its owned MP4 fixture, asserts Media3 exposes a text track and opens/dismisses both audio and subtitle selectors. The debug-only provider remains unexported and read-only.
- Verification: source/privacy reviewed; local Gradle verification unavailable because this checkout has no Gradle wrapper and no system Gradle. CI pending after direct-main commit. No signed APK/Release. Online retrieval and subtitle delay/reset are not yet implemented.
- Next: run full CI, fix actual compiler/instrumentation failures, then implement local subtitle delay/reset and complete integrated player/navigation/Glass/accessibility work. Keep final APK deferred until the integrated app is ready.


## 2026-11-21 — Local sidecar playback CI verification

- Commit e43b779467f25d08f84c8f0a971c31ef16002a22 integrates Room v4 parent IDs with playback-time verification and content-only Media3 SRT/VTT/ASS/SSA/TTML subtitle configurations.
- CI #132 / 44911456180 passed: Android 15 instrumentation 41/41 with zero failures, JVM unit tests, debug/test builds, exported Room v1-v4 schema validation, release-manifest/privacy checks. The synthetic local SRT track was accepted by Media3, and the selector UI opened/dismissed. Android 15 emulator evidence is not OPPO handset acceptance.
- Workflow artifacts contain Room schema and Android test results only; no installable APK or GitHub Release was produced. The latest public signed APK remains v0.1.91.
- Next: implement and test local subtitle delay/reset, then improve the integrated playback experience and original Glass/navigation/accessibility. Keep all Release output deferred until the app is complete enough for one comprehensive phone test.


## 2026-11-21 — Handoff reconciled after CI #132

Recorded that local Media3 sidecar subtitle integration passed CI #132 / 44911456180: 41/41 Android 15 tests, JVM/build, Room v1-v4 schema and privacy checks. Reconciled the current handoff and agent ledger to remove pending-status text. Only Android test and Room schema artifacts exist for this run; no user APK/Release. Next: local subtitle delay/reset and full player/Glass/accessibility integration.


## 2026-11-21 — Glass Lab floating dock refinement

- Increased separation between the Haze material and the dark content with a restrained shadow, thin vertical specular edge and a brighter selected pill. Preserved the black contrast tint and the effects-disabled fallback.
- Expanded the synthetic Android UI regression to assert that all four tab labels remain in bounds, the dock stays within 120 dp of the bottom safe region, selected state reaches accessibility semantics, and the Glass toggle still renders both modes.
- Motivation: the user reported that Glass Lab's bottom dock looked broken/floating in the older installed version. That handset report is not evidence that the current source failed on OPPO; this change targets clarity and adds coverage. No Apple assets/upstream code copied.
- Verification: source diff checks pass; CI pending. Local Gradle unavailable. No installable APK/Release; the current release remains v0.1.91.
- Next: inspect Android 15 instrumentation/synthetic screenshots and full build/privacy CI; correct failures, then continue integrated player and production Glass/accessibility work before one final APK.


## 2026-11-21 — Glass dock CI findings and accessibility assertion correction

- CI #134 / 44912378872 failed because the Android emulator's Pixel Launcher entered a system "isn't responding" dialog; five unrelated UI tests then could not see their expected app nodes. CI #133 had passed the same application source. No app assertion or code change was implicated; #135 is the source-changing dock gate.
- CI #135 / 44912842899 ran 41 Android 15 tests; 40 passed. The dock safe-area/fit and Glass mode assertions passed, but the new selected-state assertion read only the child Text node. Compose publishes selectable state on its parent semantics node, so this was a test-query defect rather than evidence of broken product selection behavior.
- Corrected the synthetic assertion to walk from the visible label through its accessibility parents and find the selected tab semantics. CI #136 / 44913968127 passed all 41 Android 15 tests, JVM/unit tests, CI-only debug assembly, Room v1-v4 schema regeneration/validation and privacy checks. Signing and Release publication were skipped; no installable APK was produced. Latest public installable remains v0.1.91. Next: continue integrated player controls, navigation, production Glass and accessibility before one final signed APK.


## 2026-11-21 — Glass dock correction verified

- CI #136 / 44913968127 completed successfully on commit 2ba9350b1bb2b58eebc02b5382f199d920319e19. Android 15 instrumentation ran all 41 tests with zero failures; the selected-tab assertion now locates semantics on the selectable parent. The earlier #135 failure was a test query issue.
- The full verify job passed JVM tests, CI-only debug build, exact Room v1-v4 schema export/validation and production privacy checks. Release signing and publication did not run. No installable APK or Release was created.
- Current installable release remains v0.1.91. User asked for a single APK after the integrated app is ready; do not publish milestone APKs.
- Next: evaluate stable Media3 options for subtitle offset, then complete player controls/full-screen/resume, catalogue navigation, production Glass surfaces, accessibility and fallback coverage. Actual OPPO visual, codec, battery, thermal and frame-time evidence remains pending.


## 2026-11-21 — Immersive playback toggle

- Added a player top-bar action to hide Android system bars during local playback and restore them on exit. The mode is transient-swipe-reveal, scoped to an active video target, and reset when the target changes; composition disposal restores system bars. Existing pause/checkpoint/release behavior is unchanged.
- Added accessible state labels for entering and exiting full screen plus an Android Compose interaction test that toggles both ways.
- Verification: CI #138 / 44915530791 passed all 42 Android 15 instrumentation tests, JVM/unit tests, CI-only debug assembly, Room v1-v4 schema export/validation and privacy checks. Signing/publication were skipped. No installable APK/Release; keep to the user's single final integrated APK request.
- Actual OEM system-bar behavior on OPPO remains unmeasured. Continue full player, navigation, Glass and accessibility work.


## 2026-11-21 — Immersive playback CI verification

- CI #138 / 44915530791 passed the new reversible enter/exit player-toggle Compose test; all 42 Android 15 instrumentation tests completed with zero failures. JVM/unit, CI-only debug build, Room v1-v4 schema export/validation and production privacy gates passed.
- Signing and Release publication were skipped. No user-installable APK was created; latest public signed Release remains v0.1.91.
- The Android emulator does not establish OPPO behavior or real-device performance. Continue with player completeness, navigation, production Glass, accessibility/fallback, stable subtitle-offset decision, and offline/thermal/performance review.


## 2026-11-21 — Player chrome visibility and transport integration

- Connected PlayerView's visibility callback to the Compose title/track overlay; set a 2.5-second controller timeout. The overlay hides with the transport, preserving video area, and reappears when the video is tapped. Playback controls themselves remain Media3-owned.
- Extended the test-only synthetic H.264/AAC fixture from 3s to 10s so the auto-hide/tap regression has time to observe the hidden state before playback ends. Added an Android 15 integration that renders LocalPlayerScreen, waits for auto-hide, taps the synthetic video, and invokes the full-screen action by accessibility. Test-only video remains generated at CI time.
- Verification: `git diff --check` passes; local Gradle unavailable. CI #140 / run 44916989309 failed at main Kotlin compilation because Media3 provides two same-named listener overloads. Corrected the call with the explicit PlayerView.ControllerVisibilityListener type; Android CI rerun pending. No installable APK/Release.
- Next: verify compilation and the synthetic controller visibility flow, then continue polished playback, catalogue navigation, production Glass, accessibility, and offline/device review.


## 2026-11-21 — Media3 listener overload correction

- CI #140 / 44916989309 found Kotlin overload ambiguity at `PlayerView.setControllerVisibilityListener`: Media3 also exposes a deprecated `PlayerControlView.VisibilityListener` overload.
- Specified `PlayerView.ControllerVisibilityListener` explicitly. CI #141 compiled the app source and then identified that the test activity was inferred as generic `Activity`; cast the launched test target to `MainActivity` so Compose `setContent` and lifecycle are available.
- No APK or Release. Next: rerun full Android 15 + JVM/build/schema/privacy CI and inspect the synthetic auto-hide/tap test result.


## 2026-11-21 — Playback UI test activity type correction

- CI #141 / 44917670837 compiled app source after the Media3 listener fix, then failed compiling the new instrumentation test because `startActivitySync` was typed as generic `Activity`.
- Cast the result to the launched `MainActivity`, matching the established UI test harness, so `setContent` and `lifecycle` resolve. Android integration has not run yet; rerun pending. No installable APK.


## 2026-11-21 — Keep exit control visible during full-screen toggle

- CI #142 / 44918375043 ran 43 Android tests; the new player overlay integration passed initial display, auto-hide, tap-to-restore and enter action, then failed because the exit label was checked as the controller timeout elapsed. Other 42 tests passed.
- The full-screen button calls `PlayerView.showController()` before switching mode. Because controls are designed to auto-hide, the integration test now taps the video again if needed before verifying the accessible exit label. The button also exposes a localized content description. Player view reference is scoped to the media target and cleared during disposal.
- CI #144 / 44920009659 passed all 43 Android 15 tests. Full verification passed JVM/unit, CI-only debug build, Room v1-v4 schema and privacy checks. Signing/publication skipped; no installable APK or Release.


## 2026-11-21 — Verify exit action after reveal

- CI #143 / 44919172485 confirmed the earlier 42/43 result: header hiding, tap restore and fullscreen toggle all worked, but the exit-label assertion occurred after the controller auto-hide timer.
- Updated the integration test to tap the synthetic video if the toolbar has hidden before checking the exit action; this tests the intended reveal flow. Added an explicit localized content description to the button.
- Rerun Android 15 plus unit/build/schema/privacy gates. No APK or Release.


## 2026-11-21 — Playback chrome integration CI success

- Commit f0ce46f984767effe778be2dd6928ee2160ce0dd and CI #144 / 44920009659 passed 43/43 Android 15 tests, including the local Media3/Compose controller visibility path: toolbar appears, auto-hides, returns on tap, and exposes the full-screen exit action after reveal.
- JVM/unit, CI-only debug build, Room v1-v4 schema and production privacy gates passed. Signing/release steps skipped. No user-installable APK/Release. Latest public signed version remains v0.1.91.
- Android emulator success does not verify OPPO UI or performance. Continue playback completeness, subtitle offset decision, production catalogue/navigation/Glass, accessibility/fallback, then final-device and performance review before one signed integrated APK.


## 2026-11-21 — Playback chrome test timing correction

- CI #145 / 44920952692 failed only `LocalPlaybackAndroidTest.playerOverlayFollowsTransportControllerVisibility`; 42 other tests passed. Android build and test execution completed, and test artifacts were collected. No signed APK or Release was produced.
- Root cause: after entering full screen, the test conditionally tapped only when the exit label was already absent. When the label was present, it skipped the tap and then asserted for up to three seconds, longer than the 2.5-second controller timeout, so the correctly auto-hidden overlay disappeared before the assertion. The test now waits until the title/header auto-hides, taps the video, and verifies the exit label reappears.
- Product playback code is unchanged. This corrects the regression test to model the user's actual reveal interaction.
- Verification: diff reviewed; CI rerun pending. Local Gradle/Android emulator is unavailable here. No installable APK/Release.
- Next: require green Android 15 instrumentation and privacy/schema/build gates, then continue the integrated catalogue/player/Glass backlog. Maintain one final APK request.


## 2026-11-21 — Player overlay test stabilization follow-up

- CI #146 / 44922204439 ran 43 Android 15 tests and failed 2. The player test's new wait for a second controller auto-hide cycle after toggling fullscreen did not complete within four seconds in its isolated test setup. This expectation was beyond the original coverage; revised the test to assert the accessible exit action immediately while controls are shown. Existing tests already verify initial auto-hide, video-tap restore, and reversible button state.
- The same run reported `PreviewUiAndroidTest.titleMetadataSearchIsManualAndMatchRequiresASecondConfirmation` could not find the synthetic detail sheet; the active window showed the app's connected one-video library. This may be an emulator/activity timing issue or a real test isolation defect. Keep it open and use the next run to determine reproducibility before changing product metadata behavior.
- Product code unchanged. No signed APK/Release.
- Verification: change reviewed; next Android 15 CI run pending. Local Gradle/emulator is unavailable. Continue only after recording that run's results.


## 2026-11-21 — Subtitle viewport separated from video scaling

- User specifically asked for subtitles to remain separate when a movie is zoomed. The pinned Media3 1.11.1 default player layout nests SubtitleView inside the resize-managed AspectRatioFrameLayout; a Zoom resize can therefore alter the caption viewport along with video framing.
- Added accessible Fit → Fill → Zoom controls and an app layout override derived from Media3's Apache-2.0 layout. SubtitleView is a sibling overlay at PlayerView bounds; video/artwork continue using the aspect-ratio content frame. Source URL and license are in the XML header and third-party notices.
- Added synthetic instrumentation for sibling viewport placement and for cycling the three modes. No VLC/LibVLC dependency was added; this resolves the identified behavior without native engine size/battery/licensing changes. LibVLC remains an option only if actual local codec corpus testing demonstrates material Media3 gaps.
- Verification: source/API reviewed; CI pending. Local Gradle/Android emulator unavailable. No signed APK/Release.
- Next: inspect CI and screenshots, fix failures, then proceed with subtitle delay after Media3 1.12 stabilizes and continue production Glass/accessibility.


## 2026-11-21 — Fullscreen test no longer couples to controller timeout

- CI #147 / 44923019327 ran 43 Android 15 tests; 42 passed. The sole failure was the exit-label tree assertion immediately following an accessibility click on the fullscreen button. Previous variants either waited past the controller timeout or raced the visibility listener.
- Retained integration assertions for initial toolbar, auto-hide, tap-to-restore, entry-button reachability, successful activation and changed app state. The dedicated `PreviewUiAndroidTest.playerFullscreenButtonHasReversibleAccessibleState` exercises both enter/exit labels and state. Removed the redundant accessibility-tree read after the controller and Compose visibility callbacks race.
- CI #146's one synthetic metadata timeout did not recur in #147; treat it as transient unless repeated.
- Product code unchanged. No installable APK/Release. CI #148 on source change is still pending.
- Next: inspect #148 source/build/test results, then rerun the corrected full suite. Continue subtitle/zoom only with test evidence.


## 2026-11-21 — Playback fixture stability and Media3 layout completeness

- CI #148 / 44923450336 built the caption-safe Fit/Fill/Zoom implementation and ran 45 tests; 44 passed. The subtitle/video sibling hierarchy and resize control cycle passed. The player auto-hide case did not observe the header hide within its timeout.
- The integration fixture was only ten seconds; if setup/controller interaction reaches the end, Media3 can keep playback controls shown. Extended the generated synthetic clip to 30 seconds so its playback state remains active during slow CI/emulator UI setup. This is a test-only media fixture, not bundled in production.
- Restored the standard Media3 buffering and error placeholder views in the app's adapted layout; keeping the caption view as a sibling does not require losing these status surfaces.
- Verification: CI rerun pending. No installable APK/Release. Next: inspect run #149 and the new full build/tests.


## 2026-11-21 — Auto-hide regression waits for playback state

- CI #150 / 44924204549 compiled the 30-second synthetic fixture and full Media3 layout; 44/45 Android tests passed. Subtitle hierarchy, mode cycling, and all other tests passed. Auto-hide failed again; #149 had passed 45/45 on the same app source and integration assertions.
- A longer clip did not resolve the timing variability. Media3 intentionally keeps controls visible while idle, ended, or not playWhenReady. The test now waits for the actual `Player.Listener.onIsPlayingChanged(true)` event before measuring the controller hide behavior, distinguishing player startup from timeout behavior.
- Added an optional internal player-state callback to LocalPlayerScreen; normal production calls use the default no-op. No user-facing behavior change. No signed APK/Release.
- Verification: CI pending. Next: run complete Android/unit/schema/privacy gates; inspect logs for any separate failures before changing production behavior.


## 2026-11-21 — Catalogue series season and episode rows

- Replaced the flat raw-filename list in series detail sheets with season groups and localized `S01 · E01` style labels from the existing deterministic parser. Episodes are grouped/sorted by parsed season and episode; selection still calls `onPlay` with the original indexed `DiscoveredLocalMedia` document, so no filename lookup or filesystem mutation was introduced.
- Added synthetic Android UI coverage for intentionally unsorted Season 1/2 items, visible labels and tapping Season 2 Episode 1 to verify the exact document ID.
- Privacy/offline scope unchanged. CI pending; no installable APK/Release.
- Next: inspect CI results and synthetic screenshots; then continue playback/subtitle controls, accessibility and Glass integration.


## 2026-11-21 — Playback chrome auto-hide made explicit

- CI #151 / 44931701605 failed the auto-hide test after it had already received Media3 `onIsPlayingChanged(true)`; the 30-second clip and startup latch eliminated those hypotheses. The Compose header stayed visible past the controller timeout, establishing a product behavior defect rather than only a test race.
- Added a Compose-owned 2.5-second timeout keyed by player, header visibility and active playback. When it expires, the app explicitly calls `PlayerView.hideController()` and hides its Compose title/track overlay. A pause cancels the timeout, and a video tap/controller reveal or fullscreen action makes controls visible and restarts it.
- Media3's own controller timeout remains aligned at 2.5 seconds. Verification pending CI; no APK/Release.
- Next: verify active-playback hide, tap restore, pause retention, fullscreen activation, and all library tests. Then inspect season-grouped series UI run.


## 2026-11-21 — Playback chrome interaction timer

- CI #152 / 44931933805 passed all 46 Android 15 instrumentation tests, including season grouping, exact episode selection, local subtitle viewport and resize mode cycle. Full unit/build/schema/privacy job is still completing.
- The explicit Compose-owned hide timer now restarts on seeks/position discontinuity, track changes, resize mode selection and fullscreen action. It pauses while playback is paused and starts again when playback resumes. This avoids hiding the overlay mid-interaction.
- Verification of the new timer is pending CI #153; no signed APK/Release.
- Next: inspect #153/#154 results, then continue player controls, subtitle delay decision and production Glass.


## 2026-11-21 — Media3 and LibVLC source review

- Reviewed Android's official Media3 format table: common containers such as MP4, Matroska, WebM and MPEG-TS are supported, but sample-format availability depends on platform decoders; optional software extensions must be built and included. [Source](https://developer.android.com/media/media3/exoplayer/supported-formats).
- Reviewed VideoLAN's official Android README: LibVLC offers broader codec/container coverage; its Android engine is LGPLv2 while the VLC Android app is GPLv2-or-later (de facto GPLv3). [Source](https://github.com/videolan/vlc-android/blob/master/README.md).
- Decision DEC-021: retain Media3 as primary to preserve AndroidX integration and avoid adding native ABI/APK/memory/thermal/licensing cost without evidence. User requested broad formats; compatibility must be measured on a representative local-file corpus, not inferred from marketing lists.
- Next: include codec/container diagnostics in final OPPO checklist; only compare LibVLC if a real local media sample fails on Media3. No dependency or permission change.


## 2026-11-21 — Cinematic home shelves

- Replaced the default all-library grid with separate horizontal Movies and TV Shows poster shelves. Search results and explicit Movies/Shows filters continue to use the adaptive grid for dense browsing.
- Reuses the same indexed LocalTitleCard and private offline artwork cache; card selection retains the same detail sheet and playback identity. No network, image or manifest changes.
- Existing synthetic catalogue UI checks still assert side-by-side movie cards, search, details, and playback. CI pending; no signed APK/Release.
- Next: inspect screenshot and instrumentation on phone-sized Android; tune spacing and card crop within the documented performance budget.


## 2026-11-21 — Isolate Compose instrumentation host after #155
- Base: `11faca5e43f55909b5911e4201f9707d1d4e31fa`; current CI #156 is still running at the time of this change.
- `PreviewUiAndroidTest` and the fullscreen integration test now launch a debug-only empty `PreviewTestActivity` rather than MainActivity. This prevents production startup, persisted folders and lifecycle work from racing test-owned synthetic Compose content; the activity is in the debug source set and is not packaged in release.
- The season-detail regression now scrolls the exact `S02 · E01` accessible episode node into view before clicking it; previously it scrolled the season heading and then assumed the episode remained on screen.
- No user-facing release build or phone test is produced by this change. Verification: pending CI run after commit. Next: inspect the current #156 result, then verify test isolation and all gates in the new run.


## 2026-11-21 — Correct remaining instrumentation host reference
- CI #157 / https://github.com/Batmancpu/Player/actions/runs/44934245938 failed before tests at Android-test compilation: `LocalPlaybackAndroidTest.kt:136` still referenced `MainActivity::class.java` after its import was removed during the synthetic-host conversion.
- Corrected that fullscreen controller test to launch `PreviewTestActivity`, matching the rest of the synthetic UI suite. This was a compile-time omission, not an app/runtime defect.
- No tests ran in #157 and no APK/release was produced. Next: rerun CI #158; inspect test XML and full gates before continuing.


## 2026-11-21 — README and visual identity batch started
- Replaced the stale README overview with an accurate product description, privacy boundaries, current v0.1.91 baseline, known Glass preview limits, current engineering status and four continuing work batches.
- Added one original generated concept banner and two Android 15 CI screenshots from synthetic data. The README explicitly labels concept art and CI captures and disclaims OPPO performance evidence.
- Reviewed the screenshot artifact from CI #156 (46 Android 15 tests, zero failures) before selecting images. No app source, dependency, permission, APK or Release changed in this documentation/artwork slice. CI #158 for test-host repair is in progress.
- Next: verify #158; complete README batch checkpoint, then create the signed APK the user requested while continuing the remaining product work.


## 2026-11-21 — Restore debug SAF test provider and safe season-sheet scrolling
- CI #158 / https://github.com/Batmancpu/Player/actions/runs/44934756101 compiled, then failed 6 of 46 Android tests. Five failures were caused by the test-host manifest change overwriting the existing debug-only `SyntheticSafProvider` declaration. This produced Media3 `ERROR_CODE_IO_FILE_NOT_FOUND` and null cursor results in the SAF tests. Restored that provider beside the new debug-only Activity; neither is included in release.
- The series-detail case's diagnostics showed the sheet was gone after repeated swipe gestures. Replaced gesture-coordinate scrolling with an accessibility `ACTION_SCROLL_FORWARD` against the smallest scrollable container; failures now report scrollable nodes for diagnosis.
- The README-only CI #159 may still run the broken intermediate source; CI #160 will verify the provider and accessibility scroll correction. No APK has been published from the failing runs. Next: inspect #159 and #160, repair any reproducible issues, then create the requested signed checkpoint only after green gates.


## 2026-11-21 — Queue user-requested signed APK checkpoint
- The user explicitly requested a signed APK now, then continued development without a phone-install pause. Queued the existing `release(test):` workflow on main, expected run #161 / versionCode 161.
- Release workflow dependencies require its own Android 15 instrumentation suite plus the repository's manifest/privacy, app identity, signing-certificate and APK signature/hash gates before GitHub Releases publication. The current main code is the restored-provider/accessibility-scroll correction; CI #160 is running and #161 will recheck before signing. The failed #158/#159 runs cannot produce a release.
- Updated README to link to Releases/latest instead of hard-coding the previous v0.1.91. Final APK hash, certificate and release URL must be added after the workflow reports success. This is a preview checkpoint only; no phone acceptance or Reno10 performance claim.


## 2026-11-21 — Use visible sheet content as scroll gesture origin
- CI #160 / https://github.com/Batmancpu/Player/actions/runs/44936109342 passed Kotlin/build, with 44/46 Android tests passing. The restored debug provider fixed the five playback and SAF failures. The remaining two failures were series season 2 and metadata detail controls: Compose's nested sheet content reported no `AccessibilityNodeInfo.ACTION_SCROLL_FORWARD` container.
- Updated the test helper to try accessibility scrolling first, then start an upward swipe on the lowest visible text node in the active sheet. This avoids fixed screen coordinates that can hit the dismiss area; failure summaries include scrollable state.
- CI #161 is queued for the preceding code and will not be used as APK evidence if tests fail. This correction is on main; CI #162 must verify it. No release APK from #158–#160.


## 2026-11-21 — Unify movie/show details into one lazy scroll surface
- CI #162 / https://github.com/Batmancpu/Player/actions/runs/44936962977 failed the two long-sheet UI cases again. Logs showed the active window had returned to Pixel Launcher after in-sheet swipes. The outer vertical-scroll Column plus inner height-capped episode LazyColumn allowed ModalBottomSheet to consume the gesture.
- Rebuilt details as one LazyColumn for title/year, movie action or lazily composed season/episode rows, and the metadata card. This removes nested vertical scroll ownership and retains stable episode keys.
- Synthetic season-episode selection and manual metadata-confirmation tests exercise the unified scroll container. CI #163 pending. Release workflow #161 correctly skipped signing after failed tests; no APK was published.


## 2026-11-21 — Correct detail-sheet refactor closure structure
- Source review after commit `0f8b514` found that the extracted metadata block still carried the old outer Column/Modal closures, duplicating the new LazyColumn/Modal closures. This would fail Kotlin compilation; CI #163 was queued for the intermediate source.
- Removed only those stale braces, retaining the metadata-card and new one-LazyColumn hierarchy. The correction is in this commit. Verification is pending CI #164.
- No APK was produced. Next: confirm source compilation, then run season/metadata UI interactions and complete gates before release.

## 2026-11-21 — Details sheet scroll recovery (verification pending)
- Android 15 CI #164 passed 45/46 instrumentation tests; the manual TMDB search UI test could not reach the button because a short upward scroll dismissed the modal. Changed the details sheet to ignore swipe dismissal and added an accessible Close button while retaining system back/outside dismissal. The single LazyColumn still owns long episode and metadata scrolling. Added the English string resource. CI verification is pending; no APK is published from this change yet.

## 2026-11-21 — Android test swipe correction
- CI #165 identified that the fallback instrumented swipe began near Android's bottom home-gesture area, returning the test to Launcher before sheet content could be checked. Constrain synthetic swipes to visible sheet text above the system gesture region. Restore Material3's normal sheet drag/nested-scroll handling; keep the explicit accessible Close action. CI verification is pending; next rerun Android 15 instrumentation and release gates.

## 2026-11-21 — Screenshot test now checks card bounds
- CI #166 passed both long-details scroll flows after the harness stopped swiping from Android's Home gesture zone. It then exposed a brittle assertion comparing wrapped title-text y coordinates, which differ when one title wraps. Updated the catalogue check to compare clickable card bounds, preserving the actual two-column layout requirement. CI verification pending.

## 2026-11-21 — Instrumentation now requires visible targets
- CI #167 passed the two-column card-bound check and all episode/scroll cases, then exposed that the test helper accepted a composed but clipped button as revealed. Constrained both text-target discovery and swipe origins to accessibility nodes visible above the system gesture region. Next run verifies the manual match action is actually onscreen before a test tap.

## 2026-11-21 — Keep long details content scrollable
- CI #168 showed that restoring ModalBottomSheet drag gestures closes the details sheet during the vertical gesture needed to reveal the metadata action. In CI #166, the same movie metadata and multi-season episode interactions passed with sheet gestures disabled. The details sheet therefore keeps drag-to-dismiss disabled and provides an explicit labeled Close action; system Back and outside tap remain supported. Instrumentation now waits for visible targets and keeps swipe starts out of Android's Home gesture zone. CI verification pending.

## 2026-11-21 — Constrain gestures to the sheet content
- CI #169: disabling Material3 sheet gestures left the modal open but prevented the test from advancing the inner LazyColumn to the second season; this setting is not suitable for the product. Restored default sheet behavior. Instrumentation now anchors fallback swipes below the actual sheet drag handle and above the Android Home gesture region, using only visible targets. The explicit Close action remains. CI verification pending.

## 2026-11-21 — Expand details sheet for reliable inner scrolling
- CI #170 showed the short partially expanded sheet gave competing gestures to the parent and its long LazyColumn. Open details with skipPartiallyExpanded = true so the sheet is full-height and the list owns its vertical content scrolling; retain default drag dismissal and the visible Close control. The UI test now verifies Close returns to the catalogue and checks horizontal card bounds without requiring wrapped titles to share the same text baseline. CI verification pending.

## 2026-11-21 — Capture the metadata sheet before interaction
- CI #171 passed the card separation and long-series episode flows after the details sheet opened expanded. The manual metadata test still exposed only the sheet Close action to accessibility. Added an owned synthetic screenshot immediately after opening that sheet (with the existing finite 400 ms transition settle) to distinguish initial state/render timing from metadata UI state in the next CI artifact. No production behavior changed in this diagnostic commit.

## 2026-11-21 — Use screen bounds for popup visibility in CI
- CI #172 retained a synthetic screenshot where Search TMDB was clearly visible in the full-height sheet, while Android's `AccessibilityNodeInfo.isVisibleToUser` returned false for the popup node. The test helper now uses actual screen bounds and a 64 dp gesture-area reserve to identify visible targets; fallback swipe origins remain anchored within sheet content. This is a CI semantics correction; no product code changed.

## 2026-11-21 — First signed preview checkpoint (release workflow pending)
- User asked for a signed APK now as a progress checkpoint, then continuous app completion without waiting for phone checks between batches. CI #173 passed 46/46 Android 15 instrumentation, unit tests, schema, privacy and app identity gates on `7cbff28881332f7da8de418b8b348166885ff857`. Details now open expanded, preserve inner scrolling, expose an accessible Close action, and have a tested manual metadata flow; the CI screenshot shows the search action. A `release(test):` main commit now triggers the existing permanent-certificate release workflow. No APK is claimed until GitHub Release assets/signature checks confirm it. Continue Glass/player and final optimization work afterward.

## 2026-11-21 — Glass Lab navigation and dock clarity (verification pending)
- Added a visible Back to library action wired through MainActivity, labeled the dock as a selection-only preview, added a bottom fade to protect label contrast over the scrollable demo, and increased final content padding so it can clear the dock. Added an Android UI assertion for the return callback. The change is lab-only; production catalogue is unaffected. CI run pending and physical OPPO visuals remain unverified.

## 2026-11-21 — Signed APK v0.1.174 verified
- GitHub Release: https://github.com/Batmancpu/Player/releases/tag/v0.1.174; asset `Player-release-174.apk`, 32,313,420 bytes, versionCode 174, package `com.batmancpu.player`.
- APK SHA-256: `87d6af82c695ce61a8ca14dd117340086bd085cfe7f2622325152f5e9a1f2eb2`. Permanent certificate SHA-256: `4D:2F:17:89:ED:27:D3:96:13:35:FE:36:45:64:55:BD:9A:31:0B:0D:CD:F4:1E:8C:CC:92:53:98:33:9E:80:BF`. CI #174 passed Android 15 integration and signing/privacy/identity checks. This is a progress checkpoint, not final app acceptance; user does not need to test before the next batch.
- Current main has a Glass Lab navigation/overlay repair committed as `879b19902672d5e2686c628029800f1df6b03c00`; CI #175 is verifying it. Next action: inspect its UI test/screenshot result, then continue player format/subtitle, production Glass, and measured performance work.
## 2026-11-21 — Glass Lab compile correction
- CI #175 failed Kotlin compilation because the new explicit return action used Material3 `TextButton` without importing it. Added the missing import. The signed v0.1.174 release is unaffected; next action is rerun the full CI suite for the Glass Lab patch and inspect its screenshot.

## 2026-11-21 — Haze press response connected to dock controls
- Shared a single dock interaction source between selectable tabs and the bounded Haze material surface. This activates the already-configured Haze pressed lens/highlight/compression response during touch, while the original spring selection pill remains separate and system reduced-motion policy stays active. The effects-off opaque fallback is unchanged. CI and real-device appearance/performance are pending.

## 2026-11-21 — Glass Lab batch verified on Android 15 CI
- CI #177 passed the Glass Lab return, dock bounds, and effects-off fallback UI checks; its synthetic on/off screenshots were inspected. CI #178 passed the full 46-test Android 15 integration suite plus unit/schema/privacy/identity gates after connecting one shared tab interaction source to the bounded Haze surface.
- Haze's existing pressed style now animates lens lighting/refraction and subtle scale; `GlassReducedMotionPolicy.System` is retained. The spring selection pill remains original Compose code. Haze is still lab-only; the screenshots are emulator evidence, not OPPO frame-time, thermal, or premium acceptance evidence.
- Next: apply measured design principles to production navigation and finish player/format/subtitle workflows; then build reproducible Macrobenchmark/Perfetto coverage and do the final release-size/performance audit.

## 2026-11-21 — Completed-video resume recovery
- Added a Media3 STATE_ENDED listener path that writes a zero resume position and false play intent. It also reveals the controls on completion; this prevents an ended file reopening at EOF with no clear way to restart.
- Added completedPlaybackResetsResumeCheckpointToBeginning, an Android 15 instrumentation test driven by the CI-owned synthetic H.264 fixture. The test asserts the callback receives (0, false) on natural completion; no physical phone evidence is claimed.
- CI is pending. Next inspect the test result, then continue the local subtitle workflow (including upstream-supported offset API when stable), production Glass integration and benchmark coverage.

## 2026-11-21 — Prevent EOF checkpoint from being restored by timer
- Code review caught that the periodic checkpoint could re-save the final duration after the first end event. A shared playbackResumeCheckpoint normalizer now maps every checkpoint captured while Media3 is STATE_ENDED to zero/paused, and clears the remembered play intent so background/foreground state cannot restart the ended item unexpectedly.
- Added direct state normalization assertions and extended the synthetic playback test past the five-second checkpoint interval. CI pending.

## 2026-11-21 — Synthetic completion test timeout correction
- CI #180 / 44948650139 passed compilation and ran 47 Android tests; the new natural-end case timed out because its owned synthetic H.264 fixture is deliberately 30 seconds long and the test started at zero with a 20-second wait.
- Updated the test target to start at 29,000 ms, leaving enough time for the player to prepare and reach natural completion, then wait over the five-second checkpoint interval. This is a test timing correction; CI #181 verifies source checkpoint normalization, and the updated test awaits the next run.

## 2026-11-21 — Completed-video resume checkpoint verified
- CI #180 / 44948650139 exposed a test setup timeout (20-second wait versus the generated 30-second video). CI #181 / 44949160303 still had that old fixture position and also encountered a Pixel Launcher ANR affecting unrelated UI cases.
- CI #182 / 44949435610 passed all 48 Android 15 instrumentation tests after seeking the fixture to 29 seconds; unit tests, application build and Room schema v1-v4 validation also passed. Signed-release-only checks were not run.
- The STATE_ENDED checkpoint is now zero/paused across natural-end, periodic, Activity-pause and disposal saves. Continue with subtitle control work and production presentation; real OPPO acceptance/performance is still pending.

## 2026-11-21 — Functional library destinations
- Added a persistent bottom `NavigationBar` with Home/Movies/Shows/Search. Home retains poster shelves; Movies and Shows filter the existing Room-derived catalogue; Search selects the search destination, focuses the local field and shows local-only prompt/results.
- Replaced duplicate type chips with the four destinations. Changing destinations dismisses the details sheet; leaving Search clears its query; the accessible selected-tab semantics are provided by Material3.
- Added an Android 15 synthetic-library test for all destinations, title filtering and Search field focus. CI pending.
- This intentionally establishes navigation behavior before bounded Haze is placed in production; Glass remains a later rendering phase. Next: validate CI and fix failures before continuing player/subtitle polish.

## 2026-11-21 — Correct navigation selected-state test
- CI #184 passed compilation and 48 of 49 Android 15 tests. The navigation test's movie filter assertion passed, then its generic helper matched the earlier Movies shelf heading instead of the selected tab label.
- Changed the test helper to examine all matching text/content-description nodes and their selected ancestors. Product navigation code is unchanged. Re-run CI before treating navigation as verified.


## 2026-11-21 — Navigation selected state made explicit
- CI #185 / 44951546792 compiled the navigation and ran all 49 Android 15 instrumentation tests; only the selected-tab assertion failed. The screenshot shows the bottom bar is laid out correctly. Movie/Show filtering worked; Android accessibility did not expose Material3's selected value via `isSelected` as the test expected.
- Added explicit localized selected/not-selected state descriptions on each production NavigationBarItem and changed the synthetic test to accept that state description or the platform selected flag. This improves the spoken destination state and tests it directly.
- Verification: source/test change is committed on main; new full CI run is pending. No APK produced and no Reno10 behavior/performance claimed.
- Next: inspect latest workflow, resolve any test or UI issue, then continue player captions/controls and Glass/performance batches.


## 2026-11-21 — Playback speed control and navigation accessibility
- Navigation follow-up: CI #185 showed correct Home layout/category filtering but no Android `isSelected` flag on the Material3 navigation item. Added localized `Selected` / `Not selected` state descriptions and updated synthetic UI coverage to inspect stateDescription.
- Added a playback speed menu to the in-player top controls with 0.75×, 1×, 1.25×, 1.5× and 2× choices. It updates Media3 `PlaybackParameters`, tracks speed changes from the player listener, and labels the button for screen readers. Added Android UI coverage that opens the menu, selects 1.5× and checks the callback.
- Verification: changes committed on main; CI for the current tree is pending. No local Gradle environment was available in this workspace and no physical device test was run. This is not a release APK.
- Next: inspect CI compile/test results; fix any failures, then continue subtitle usability and production glass/performance work.


## 2026-11-21 — Navigation accessibility and playback speed verified
- CI #203 / 44952898549 completed successfully on current main. All 50 Android 15 instrumentation tests passed, including the selected navigation state assertion and playback-speed menu selection. Unit tests, debug build and Room schemas v1-v4 verification also passed.
- Release signing, production-manifest and release-certificate checks were correctly skipped because this was a regular development push; no APK or release was created. This does not represent OPPO device or performance verification.
- Next: investigate and refine the Glass Lab dock based on the synthetic screenshot and user's report, then continue subtitle/player work and final efficiency review.


## 2026-11-21 — First production Glass navigation dock (CI pending)
- Starting from CI #203 / 44952898549 (50/50 Android 15 tests, unit/build and Room v1-v4 schemas green), moved the bounded Haze dock into the real Home/Movies/Shows/Search scaffold. The library content is registered as a Haze source only while effects are enabled; only the dock is blurred. Turning Glass off removes the source as well as the Glass effect, so the fallback avoids capture work. Poster and video content are not glass layers.
- Added a user setting in Manage to switch Glass off. Preference is stored locally in private SharedPreferences and survives activity recreation; the off mode uses the existing opaque PlayerGlassSurface fallback. The dock provides explicit selected/not-selected semantics and retains real catalogue navigation. Renamed the component to PlayerGlassDock and encapsulated persistence in PlayerUiSettingsStore.
- Added Android 15 synthetic coverage that disables the effect, verifies local persistence through activity recreation, and cleans up its test preference. Existing production navigation test covers tab behavior and selection.
- Verification: CI pending; no Android SDK/Gradle wrapper is available in this workspace. No OPPO rendering, frame-time, memory, thermal or battery evidence.
- Next: inspect CI and generated catalogue screenshots for bottom inset/overlap and Haze behavior; correct any issues before moving to subtitle controls and the final performance/release pass.


## 2026-11-21 — Glass dock compile failure and correction
- CI #205 / 44955249002 reached `:app:compileDebugKotlin` and failed at LocalLibraryScreen.kt:517 with unresolved reference `contentDescription`; the switch's Compose semantics modifier lacked its import. No instrumentation tests ran, and the release verification job was skipped after the failed test/build job.
- Added the missing `androidx.compose.ui.semantics.contentDescription` import. This changes no behavior; the production Glass dock, local preference and persistence test remain the intended change. Fresh CI is pending.
- No APK/Release or handset evidence. Next: verify compile + 51 Android 15 tests, then inspect the generated library screenshot and full build/schema checks.


## 2026-11-21 — Repair production Glass accessibility and settings reachability

CI #206 / 44955812963 compiled the production dock and exercised 51 Android 15 tests. Three failed: the dock's selected state was not found in the platform accessibility hierarchy, the Switch node did not report the enabled state through `isChecked`, and moving a Glass option above existing Manage actions hid the folder-removal action in the first sheet viewport. Updated `PlayerGlassDock` to expose an explicit selected semantic alongside its tab state description; made the Glass preference announce localized On/Off state; placed its row after the existing scan, folder, metadata and Glass Lab actions; and extended instrumentation to scroll to the setting, verify its announced/persisted state across activity recreation, and report selected/state descriptions in failures. `git diff --check` passes. Android compile/instrumentation verification is pending; no APK or physical-device performance evidence. Next: inspect fresh CI failures/screenshots and correct until green, then continue subtitle/player and Glass performance work.


## 2026-11-21 — CI #207 accessibility diagnostics and sheet-scroll fallback

CI #207 / 44957235967 compiled the app and completed all 51 Android 15 instrumentation tests; two UI tests failed. The Manage-sheet glass setting was offscreen, but Android did not expose the Compose vertical-scroll container as an accessibility scroll action. The test now uses the repository’s safe synthetic upward gesture helper when accessibility scrolling is unavailable. Dock selected state still was not associated with the accessible item label; the tab now has an explicit merged content description and the test click helper accepts it. Navigation assertion failures now capture the full active accessibility snapshot. CI #208 pending; no APK or device evidence. Next: inspect #208 and fix any actual product/accessibility defect until green.


## 2026-11-21 — Correct tab-label scope and refine offline poster proportions

CI #208 / 44957854614 stopped at `PlayerGlassDock.kt:85`: `stringResource` was invoked inside a non-composable semantics lambda, so no Android tests ran. Compute each localized tab label in the surrounding composable scope and reuse it for label text and merged accessibility description. In the same pending batch, replace square offline poster placeholders with portrait 2:3 layouts, original title-initial artwork and a screen-reader description; add an Android bounds regression. Local `git diff --check` and strings XML parsing pass; CI #209 is pending. No release APK or real-device visual/performance evidence. Next: verify the previous accessibility fixes, new poster bounds, and all existing Android/JVM/schema/privacy gates; inspect generated screenshots and continue player/subtitle work.


## 2026-11-21 — Move poster accessibility label into composable scope

CI #209 / 44958254084 failed Kotlin compilation at `LocalLibraryScreen.kt:601`: the new title-specific poster `contentDescription` called `stringResource` inside a non-composable `semantics` lambda. `LocalTitleCard` now resolves the localized string in composable scope and only assigns the value inside the semantics block. Local `git diff --check` passed. The Android suite never ran and no APK was generated. CI #210 pending, including the prior dock and scrolling fixes plus portrait poster bounds. Next: inspect the full Android/JVM/schema/privacy result and screenshots; continue integrated player controls.


## 2026-11-21 — CI #210 accessibility repair and cinematic details backdrop
- CI #210 / 44958625515 compiled and executed 51 Android 15 instrumentation cases; 46 passed and 5 UI assertions failed. Source review plus the uploaded synthetic report identified that decorative card art semantics replaced the title accessible name, tab selected state was not present on the Android node, Switch custom stateDescription was not mapped to `AccessibilityNodeInfo.stateDescription`, and the detail-sheet scroll helper did not recover a safely visible gesture anchor.
- Make title-card posters decorative so the clickable card exposes its title/year and portrait geometry. Dock tabs now use an explicit semantics node with tab role, label, selected state and click action. Glass settings retain the native checked state and include localized On/Off text in the accessible label. The test helper records node bounds and uses a centered gesture only inside a recognized bottom sheet if Compose exposes no useful scroll target.
- The details surface now has an original cinematic landscape hero. A generated color field is immediate/offline; cached TMDB backdrop files are displayed without network. Artwork fetch remains behind the separate confirmed-match action, and now fetches one approved bounded backdrop alongside the poster; integration coverage checks both cached files and offline use after metadata is disabled. No media path/content is sent, no new host/permission is added.
- Local checks: `git diff --check` passed and Android string XML parses; no local Android SDK/Gradle is installed. CI #211 is required before considering the accessibility/backdrop change verified. No APK produced and no actual-phone/performance claim.
- Next: inspect CI #211 result/screenshots, correct failures before adding more player controls; after the integrated features, add Macrobenchmark/Perfetto workflow and do one release-like size/performance audit before the single signed candidate.


## 2026-11-21 — CI #211 semantics finding and explicit catalogue card labels
- CI #211 / 44959819745 compiled and ran all 51 Android 15 instrumentation cases; 47 passed and 4 UI checks failed. The production dock's explicit tab state/click behavior now passes, and the series-details flow passes. Failures traced to Compose clear semantics on the decorative poster initial removing merged child title text, and Android 15 returning false for `AccessibilityNodeInfo.isChecked` on the Material Switch even though the application preference defaults enabled. The metadata sheet test could not open the card because its title was absent.
- Follow-up assigns each clickable card a single localized accessible label with title, media type and year/device fallback; decorative image/initial remain visual. UI tests address titles through that card label. Switch explicitly exposes Role.Switch and ToggleableState.On/Off in addition to a localized On/Off label; tests verify Android's checked flag and preference after toggling/recreation.
- Local checks: `git diff --check` and resource XML parse pass; Android build is known to compile from #211. No local Android SDK/Gradle, no APK and no handset evidence. CI #212 must verify the regression correction and confirm the metadata action scroll helper.
- Next: commit the documented correction, then inspect CI #212's full Android/JVM/schema/privacy result and screenshots. Continue player subtitle/controls and production Glass; only after all functionality, add Macrobenchmark/Perfetto and run the final efficiency/size review for the single signed APK.


## 2026-11-21 — CI #212 missing semantics import
- CI #212 / 44960830044 reached Kotlin compilation and failed at `LocalLibraryScreen.kt:576` because the Compose `role` semantics extension used for `Role.Switch` was not imported. No Android tests/screenshots ran, no APK was produced, and no product assertion was reached.
- Added `androidx.compose.ui.semantics.role` and clarified that the setting provides both explicit native `Role.Switch`/`ToggleableState` and a localized On/Off announcement. Local `git diff --check` passes; no Android SDK/Gradle is installed locally.
- Next: commit and inspect CI #213. It must compile and execute all 51 Android 15 cases, including explicit card semantics, switch state/persistence and modal navigation, before further feature commits.


## 2026-11-21 — CI #213 UI tree and state follow-up
- CI #213 / 44961265322 compiled and ran 51 Android 15 tests: 47 passed, 4 failed. The explicit catalogue card descriptions are now exposed, the series-detail workflow passes, the Glass dock tests pass in isolation, and the real production navigation clicks filter content. Remaining reports: Switch `isChecked`/`isCheckable` still false because its internal semantics merged the explicit role; catalogue's exact `Shows` lookup no longer accepted the dock's contentDescription; the navigation assertion ran before selected state recomposed; and the metadata confirmation button was inside safe screen bounds but the scrolling helper reported no gesture anchor.
- Current correction puts Switch accessibility state, role and click action in a clear semantics node; test label lookup recognizes exact contentDescriptions; selected-state checks wait for the accessibility tree update; metadata test asserts the confirm action is within safe app bounds then activates it directly. This preserves separate user confirmation and does not skip product actions.
- Android compilation passed in #213. Local `git diff --check` and string XML parse pass; local Android SDK unavailable. CI #214 is required for 51 Android 15 tests, privacy/schema/build gates, and synthetic screenshot review. No APK/phone evidence.
- Next: commit with docs, inspect CI #214 and fix until green, then continue player control/subtitle and Glass features. Add Macrobenchmark/Perfetto and final size/efficiency review only after feature work, then produce one signed integrated APK.
## 2026-11-21 — Compose-owned player transport batch

- Verified CI run #214 (run ID `44961886974`, source `4aa127595ec164dbfdd9ba71d2a2e08169ca27eb`): Android 15 integration passed 51/51; JVM/unit tests, app build and Room v1-v4 schema validation passed. Privacy/release checker fixtures passed. Release signing/manifest checks correctly skipped because this was not a requested publication. Synthetic screenshots were generated; the Details and Glass On screenshots were inspected. These are emulator-only results.
- Replaced PlayerView's duplicate native controls with a Compose-owned transport: accessible play/pause, seek slider with current/total timestamps, and ±10-second jumps. Slider scrubbing pauses auto-hide until release; player position updates at 4 Hz only while the controls are visible, and seeking stays within the known media duration. Added localized labels/state descriptions and time-format assertions to local-playback instrumentation.
- Why: native Media3 controller style was generic and did not provide the product's custom player direction. The seek control now lives in the Compose interaction layer while Media3 remains the decoder/transport engine. Captions remain independent from resize-managed video.
- Verification for this new batch: `git diff --check` passed; no local Android SDK/Gradle wrapper is available. New Android 15 verification is pending. No APK produced. Risks to inspect in CI: slider accessibility semantics/touch reachability, auto-hide behavior, seek updates, and regressions to fullscreen/track selection.
- Next: commit the source and context updates on `main`, wait for the complete CI gate, fix any failures, then continue subtitle visibility/delay and the production Glass/player presentation backlog. Defer the one signed integrated APK until implementation, automated checks and release-like efficiency review are complete. Reno10 frame timing/thermal/battery and acceptance remain unavailable.
## 2026-11-21 — CI #215 custom player role import correction

- CI #215 / `44962980148` reached app Kotlin compilation and failed because `LocalPlayerScreen.kt:298` referenced the Compose semantics `role` extension without importing it. Android instrumentation/screenshots did not run; release-only job was skipped.
- Added `androidx.compose.ui.semantics.role`. Local `git diff --check` and strings XML parsing pass; no local Android SDK/Gradle wrapper. This correction must be compiled and fully tested by CI #216 before additional product code.
- Corrective commit `684c35f` is pushed to main; CI #216 is running. A documentation reconciliation records its exact source SHA and current test state. Next: inspect CI #216 and the player interaction screenshot/artifact, then fix any subsequent real compile/test defects.
