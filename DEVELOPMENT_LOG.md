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
