# Issues, risks and fixes

A risk is not a confirmed defect. Record evidence, confidence and status precisely.

## Historical device incidents

### H-001 — CurrentLens native SIGSEGV on OPPO Reno10
- **Status:** Historical warning; root cause not verified in Player.
- **User-reported profile:** OPPO CPH2531, Android 15, package com.mangoloads.currentlens.stability, version 0.2.2-stability.
- **Observed marker:** native SIGSEGV after “BrrrNewsApp composition started”.
- **Source-verified later hardening:** CurrentLens main added local JVM crash diagnostics/recovery in commit 2475970123a2a2fb0e05be25d1d9a45061186c73; defensive preference/cache reads; and reduced repeated GPU luminance sampling.
- **Evidence missing:** exact native tombstone/backtrace and confirmed causal component.
- **Lesson:** avoid JNI/NDK and experimental native rendering initially; isolate Glass; retain solid fallback; test on handset. Kotlin uncaught-exception handler does not reliably catch native SIGSEGV.
- **Player fix:** not applicable; no application code yet.

### H-002 — Live blur varies by Android OEM/device
- **Status:** Known cross-device risk, not yet tested in Player.
- **Source:** user's brrrBoard/FrostKeys README documents OEM/hardware limits and crash/rendering risks on unsupported blur paths.
- **Lesson:** opaque/frosted fallback, no required cross-window blur, handset validation before release.
- **Player fix:** not applicable; no Glass code yet.

## Design-time risks

### R-011 — Glass Lab dock can appear detached or visually weak
- **Status:** User-reported on the older installed preview; current-source dock refinement and semantics checks pass Android 15 CI. Actual OPPO visual quality remains unverified.
- **Evidence:** User said the Glass Lab bottom navigation looked broken/floating. No current v0.1.91 screenshot/device reproduction supplied.
- **Mitigation in current source:** A restrained source-backed surface edge, selected pill and shadow are added. CI #136 passed all 41 Android 15 tests including dock bounds/safe-area, both Glass modes and selected semantics through the accessibility parent chain. This does not establish target-device visual quality or performance.
- **Next:** review the CI screenshot and obtain actual OPPO visual feedback with the eventual integrated APK.


### R-010 — Persisted subtitle sibling identity can become stale
- **Status:** Mitigated; CI #132 / 44911456180 passed the external subtitle integration test.
- **Risk:** A document may be moved, renamed or replaced after the offline scan, and stale parent IDs alone must not make it eligible for playback.
- **Mitigation:** Playback revalidates the video and each exact subtitle ID/name through a fresh bounded immediate-child SAF listing under the recorded parent. Missing provider access drops sidecars while preserving video playback. Synthetic Android tests exercise external SRT decoding; actual OEM behavior remains unverified.


### R-001 — Haze 2.1.0 toolchain alignment
- **Status:** Open.
- **Risk:** Haze 2.1.0 updates Kotlin to 2.4.21 and Compose Multiplatform to 1.12.2.
- **Mitigation:** prove a minimal build; upgrade Kotlin and Compose in separate commits; add Haze in a dependency-only commit; rerun full build.

### R-002 — SAF may expose cloud/network providers
- **Status:** Open.
- **Risk:** ACTION_OPEN_DOCUMENT_TREE can show more than local storage providers; selected URI alone does not prove local storage.
- **Mitigation:** default-deny unknown authorities, test OPPO provider, reject known cloud/network providers, never transmit URI or source content.

### R-003 — TMDB client credential in a public APK
- **Status:** Open.
- **Risk:** shared credential embedded in source/APK is extractable. A no-backend design cannot keep a shared static client secret confidential.
- **Mitigation:** user-managed credential, encrypted local storage, no logging, optional metadata mode when absent. Verify current TMDB auth requirements.

### R-004 — Metadata false positives/remakes
- **Status:** Open.
- **Risk:** same-title remakes, languages, alternate names, anime and poorly named episodes.
- **Mitigation:** confidence scoring, year/type context, user confirmation and persistent manual override.

### R-005 — Glass GPU cost and scroll jank
- **Status:** Open.
- **Risk:** backdrop capture, blur, lens and many concurrent surfaces can cause jank/memory spikes.
- **Mitigation:** Glass only on functional chrome; benchmark; avoid per-frame pixel sampling; retain fallback.

### R-006 — Flux GPL-3.0-or-later obligations
- **Status:** Open until source reuse decision.
- **Risk:** copying Flux app code creates copyleft obligations for distributed derivatives.
- **Mitigation:** build independently using compatible libraries/architectural ideas, or adopt Flux with full GPL compliance/notices. Do not copy code before reviewing licences.

### R-008 — Local playback engine compatibility and licensing
- **Status:** Open; evaluation required before adding a playback dependency.
- **Risk:** no Android engine can universally guarantee every container/codec combination; decoder availability varies by handset and OS. A native engine changes APK/ABI size, performance, crash surface and licence obligations.
- **Mitigation:** evaluate AndroidX Media3 against a representative local test corpus first; evaluate LibVLC only if Media3 gaps are material. Record exact container/codec/audio/subtitle results and target-device outcomes. Never infer support from extension or claim universal compatibility.

### R-009 — External subtitle provider privacy and availability
- **Status:** Open; provider not selected.
- **Risk:** subtitle APIs may require an account/key, impose language/rate/availability limits, change availability or transmit identifiers; automatic matches can be wrong.
- **Mitigation:** audit terms, privacy, authentication and limits; send only normalized title/year/season/episode identifiers; store subtitle downloads in app-private storage; retain local sidecar import and manual selection; never modify source media.

### H-005 — Folder setup launcher imported rememberSaveable from the wrong package
- **Status:** Fixed and regression verified.
- **First observed:** 2026-11-14, workflow run 43569596301.
- **Affected file:** app/src/main/java/com/batmancpu/player/MainActivity.kt.
- **Expected:** launcher route state compiles and preserves the developer Glass Lab route across configuration changes.
- **Actual:** Kotlin compiler reported unresolved reference rememberSaveable.
- **Root cause confidence:** high; it is declared by androidx.compose.runtime.saveable, not androidx.compose.runtime.
- **Fix:** correct the import; no dependency or runtime behaviour change.
- **Regression verification:** workflow run 43569710969 succeeded: https://github.com/Batmancpu/Player/actions/runs/43569710969. The subsequent documentation handoff run 43569752178 also succeeded.

## New issue template
- **ID / title:**
- **Status:** Open / Investigating / Fixed / Won't fix
- **First observed:**
- **App version / commit / workflow:**
- **Device / OS:**
- **Severity and impact:**
- **Expected behaviour:**
- **Actual behaviour:**
- **Reproduction steps:**
- **Logs/evidence:**
- **Root cause and confidence:**
- **Fix commit:**
- **Regression test:**
- **Verification result:**
- **Remaining risk:**

Do not erase a failure after a fix. Mark fixed and link the corrective commit.

### H-003 — CI SDK platform package identifier was invalid
- **Status:** Fixed and regression verified.
- **First observed:** 2026-11-14, CI run 1 (https://github.com/Batmancpu/Player/actions/runs/43560806966).
- **Regression verification:** CI run 2 succeeded (https://github.com/Batmancpu/Player/actions/runs/43560868703); Android 37.0 SDK installation and Gradle APK assembly both passed.
- **Affected component:** .github/workflows/ci.yml, Android SDK installation.
- **Expected:** SDK manager installs Android API 37 platform.
- **Actual:** sdkmanager failed with "Failed to find package 'platforms;android-37'" before Gradle ran.
- **Root cause:** CI used the platform package name without the channel's `.0` suffix.
- **Fix:** use `platforms;android-37.0`; retain `build-tools;37.0.0`.
- **Regression test:** following CI run must install the platform and proceed to Gradle; record result in DEVELOPMENT_LOG.md.


### R-007 — Apple SF Symbols glyph licence
- **Status:** Closed by design decision.
- **Finding:** The SF Symbols Compose repository uses MIT for its generator/source, but explicitly says the actual SF glyph artwork/data are Apple's property and subject to Apple's SF Symbols license. That license restricts use to Apple platforms.
- **Mitigation:** Do not bundle Apple glyph artwork or SF font files in Android. Use an original or permissively licensed icon set with consistent optical sizing, weight, and animation. The app may be Apple-inspired without reproducing proprietary glyphs.


### H-004 — Haze Glass shape API requires RoundedCornerShape
- **Status:** Fixed and regression verified.
- **First observed:** 2026-11-14, CI run 12 (https://github.com/Batmancpu/Player/actions/runs/43563048500).
- **Regression verification:** CI run 13 succeeded (https://github.com/Batmancpu/Player/actions/runs/43563224306); Haze helper compiled and APK upload passed.
- **Affected file:** app/src/main/java/com/batmancpu/player/ui/glass/PlayerGlass.kt.
- **Expected:** shared helper accepts a compatible shape and compiles.
- **Actual:** Kotlin error at line 47: Haze GlassStyle.shape expected RoundedCornerShape, but helper parameter was CornerBasedShape.
- **Root cause confidence:** high; compiler output explicitly reports the type mismatch.
- **Fix:** narrow the helper argument to RoundedCornerShape. More complex/custom shapes can be added only if the Haze public API supports them.
- **Regression test:** next CI run must compile PlayerGlass and complete the debug APK task.


### H-006 — First committed Termux signing helper had over-escaped shell sequences
- **Status:** Closed; helper removed because the user requested a no-Termux workflow. The corrected helper passed CI run 27 before removal.
- **First observed:** 2026-11-14, during post-commit source review of `df9d62c2ea03fa9fb8da731fc33a9508a7028c94`.
- **Affected file:** `scripts/generate-release-keystore.sh`.
- **Expected:** Bash line continuations keep keytool options in one command; printf format strings produce normal newlines.
- **Actual:** The initial committed text contained doubled backslashes before line breaks and doubled escapes in printf formats. That would split keytool options into separate shell commands and print literal escape sequences.
- **Root cause confidence:** high; direct inspection of the committed file exposed the exact over-escaping.
- **Exposure:** The helper was not run on a device; no keystore was generated and no secret was exposed. The defect was caught before use.
- **Fix:** Replace the script with correctly escaped shell source, add a CI `bash -n` step, and run `bash -n` against the exact corrected file.
- **Regression verification:** local `bash -n` and GitHub Actions run 28 (`43573474031`) passed. Helper was not run on the user's phone and is no longer part of the project.


### H-007 — CI referenced Termux helper after its deletion
- **Status:** Fixed and regression verified.
- **First observed:** 2026-11-14, workflow run 29, ID `43574064538`.
- **Affected file:** `.github/workflows/ci.yml` during the intermediate commit where `scripts/generate-release-keystore.sh` had already been deleted.
- **Expected:** Removing the unused Termux helper should leave CI independent of that script.
- **Actual:** Workflow still invoked `bash -n scripts/generate-release-keystore.sh`; GitHub Actions reported `No such file or directory` and exited 127 before Gradle setup.
- **Root cause confidence:** high; job log confirms the missing path.
- **Fix:** Remove the obsolete syntax-validation step from the workflow in the next commit.
- **Regression verification:** workflow run 30 (`43574233803`) passed unit tests, debug APK assembly and artifact upload. Release signing remained skipped because secrets were absent.

### H-008 — Incomplete SAF cursor could replace a complete offline library
- First observed: source audit 2026-11-14 at acb9829; affects signed v0.1.51/v0.1.53 source. No actual OPPO data-loss report.
- Severity: high cached-library correctness risk; originals are not modified.
- Reproduction: a child-directory cursor returns some rows plus EXTRA_LOADING=true or EXTRA_ERROR, or omits mandatory ID/name/MIME. Previously scan could return success and Room removeStale could drop omitted records.
- Root cause confidence: high from source; Android cursor reproduction still requires instrumentation. Existing tests exercised thrown exceptions, not successful partial cursors.
- Fix: explicit TreeListing completion flags; throw sanitized IOException for incomplete status/invalid mandatory cursor fields. Existing saveSuccessfulScan call remains reachable only on success.
- Regression: JVM tests reject loading/error listings after earlier valid rows, avoid further descent/reconciliation and accept a genuinely empty complete directory. Adapter cursor/Room integration tests remain pending.
- Status: implemented, PR CI pending; not device-verified. See docs/PHASE1_AUDIT.md for remaining independent defects.



### H-009 — Cached load / scan / folder-selection ordering
- Status: open; source-confirmed at acb9829, affects v0.1.51/v0.1.53. No OPPO reproduction. Old load may replace newer scan display; scan cancellation is not joined during switching. Root-cause confidence high from independent composable jobs/shared state.
- Fix pending: lifecycle-owned serialized state. Regression needed: delayed load followed by scan; A→B→A; rotation/background. See docs/PHASE1_AUDIT.md item 2.

### H-010 — Grant restoration/replacement recovery gaps
- Status: open; source-confirmed at acb9829, affects v0.1.51/v0.1.53. No OPPO reproduction. Stored grant is mistaken for accessible root; ignored release failure/process interruption can retain additional grants. Confidence high from source; actual OEM failure behavior unverified.
- Fix pending: explicit root validation/reconnect and reconcile grant ownership. Regression needed: unavailable/deleted root, revoked grant, failed release, process death between take/persist. See audit item 3.

### H-011 — Unbounded traversal and blocking-provider cancellation
- Status: open; source-confirmed at acb9829, affects v0.1.51/v0.1.53. No OPPO ANR/OOM report. Entire child cursors and queue are unbounded; query lacks CancellationSignal and row loop lacks cooperative cancellation. Confidence high from source.
- Fix pending: query cancellation/budgets with explicit incomplete error preserving cached rows. Regression needed: blocked query, huge cursor, cycles/deep/wide trees, cancellation after partial rows. Existing cancellation test only exercises a suspending callback.

### H-008 verification update
- PR #1 functional commit 9557723 merged as 79ee09d. Unit/debug CI 43612121975 passed. Android cursor/Room integration and handset evidence remain pending; complete-listing guard is source/build/JVM verified only.

### H-009 implementation update — 2026-11-14
- Status: implemented, PR CI pending; no OPPO reproduction/acceptance claimed. Retained lifecycle owner plus generation checks and serialized operations prevent old load/scan/finalizer from publishing into new state; cache restoration blocks scan start.
- A second source-confirmed switching race was found: picker-local forgetTree could run after A→B→A and delete reselected A. Cleanup now belongs to serialized transactional tree activation, not a detached picker coroutine.
- Regression scenarios implemented in LibrarySessionTest (six cases); Android recreation, Room cancellation/reconciliation and actual grant/provider checks remain pending. H-010 grant acquisition/release/process-death behavior is not resolved by this change.

H-009 regression verification: functional commit 17e46a8 merged via PR #2 as 10dab08; PR CI 43613459732 and main signed CI 43613651152 passed. Marked fixed at source/JVM/build level; actual Android recreation/Room/provider acceptance still pending. H-010/H-011 remain open.

### H-010 implementation update — 2026-11-14
Serialized FolderAccessManager/AndroidFolderAccessStore now probe actual root availability, persist selection before release, strip writes, clean abandoned grants and refuse activation after cleanup failure. Retained ViewModel owns picker/restoration with stale-completion guard. Nine fake-store regressions added; PR CI pending, OPPO grant/cursor behavior unverified. Next regression layer is Android cursor/Room instrumentation.

### H-011 implementation update — 2026-11-14
SAF root/child query CancellationSignal and per-row checks added, cursor lifecycle scoped to helper. Explicit row/directory/depth/width budgets throw before reconciliation. Out-of-tree/invalid rows now fail complete scan instead of producing misleading partial success. Added pure budget/cycle regression tests; CI and Android cursor verification pending. isChildDocument binder calls cannot be forcibly cancelled with this API; actual provider behavior remains device-dependent.

### H-012 — Numeric titles and multi-episode parser ambiguity
- Source-confirmed at 30ea20f; affects prior test releases. Leading year-like titles could become empty; multi-episode names could become movies or one episode. No OPPO report. Fix uses trailing nonleading year and explicitly leaves multi-episode fixtures unrecognized. Source regressions added; CI pending.

### Phase 1 verification follow-up — 2026-11-14
H-010 fixed at fake/JVM/source level (CI 43614796902); H-011 bounded scan/cursor helper fixed at JVM/build level (CI 43615085618). Android integration CI 43615935921 passed all 9 synthetic cases, including real cursor cancellation/close and launcher recreation. H-012 parser and plain UI/session state regressions passed CI 43616107190. Actual OPPO external-storage provider, permission recovery, media isolation and performance remain unverified until phone report.

### H-013 — Release backup verifier assumed one aapt2 display format
- First observed: publication workflow 43617409651 (#78), script backup assertion. No new APK was published. App source and merged debug-manifest Android test retain backup disabled; production representation is under verification.
- Root-cause confidence: format mismatch likely from narrow regex, exact attribute value pending corrected gate output. No signing/app identity change.
- Fix: recognize only known semantic false representations, log sanitized attribute value, reject true/unknown. Three full-script regression groups pass locally including forbidden network/debug-provider/stale-version checks; CI/publication pending.

### H-013 verified resolution and candidate availability — 2026-11-14
Publication run 43618442469 passed all gates and published v0.1.79. Actual release aapt2 text is `false`; the original typed-hex-only regex caused #78’s failure. Full-script regression fixtures and actual built-APK assertion now pass without weakening rejection of true/unknown backup. No backup-on defect existed in Player. Source/JVM/Android synthetic tests pass; real OPPO provider/phone acceptance remains pending.

### H-014 — Traversal and index mapping can resume on UI thread
- First observed: 2026-11-15 source audit at fb78659; affects v0.1.79 code path. No actual OPPO reproduction or measured timing. Potential large-library UI blocking and delayed mapping cancellation.
- Root-cause confidence high from coroutine boundaries: VM main scope invokes scanner and index callbacks; query/DAO suspend dispatch does not automatically move subsequent traversal/parsing/mapping off caller thread.
- Fix: whole scan and index entry-point IO dispatch, per-record cancellation checks. No source media write, schema, permission or signing change.
- Regression: Android main-caller 1,000-item save/activation/load then 100-item reconciliation; existing preservation/rollback/cancellation cases. CI pending; exact frame/memory/thermal outcomes require actual OPPO traces. Unreleased main; v0.1.79 remains current candidate.

H-014 verification: 57e4539 passed full main CI 43667022100, including 10 Android integration cases and signed-build/privacy/certificate checks. Fixed at source/build/functional-test level; unreleased, still present in the older v0.1.79 code path. Actual phone timing/impact is unmeasured.

### H-015 — User-reported broken Glass Lab dock
- v0.1.79 on OPPO CPH2531: user describes bottom navigation broken/floating in air. Exact pixel defect/renderer cause unverified (no screenshot). Source uses four fixed-width independent clear Glass surfaces merged in a container. Replace with coherent bounded single-surface dock, verify layouts/screenshots/fallback; no claim that library engine itself is proven defective.
### H-016 — Scan cancellation/rescan feedback is insufficient
- v0.1.79 user sees no clear effect after cancel. Very short scans can already be complete; current inline state is easy to miss. Keep safe completed-snapshot behavior and add explicit action feedback distinguishing cancelled versus already completed. No source deletion control is wanted.

### H-017 — New fixture generation assumed FFmpeg on hosted runner
- CI #85 / 43676678448: ubuntu-26.04 image has no ffmpeg, exit127 in fixture generation before any Gradle/test execution. Exact log confirms tooling absence, not a video playback result. Corrective workflow explicitly installs the distribution package when absent, then generates the owned synthetic test asset. No production dependency/source-media access change; full gate pending.

### H-018 — Media3 builder rejects NO_OP bandwidth meter
- Unpublished preview source 3c62a71/14b0b0b; Android15 first-frame test in CI43676928900 threw IllegalArgumentException at ExoPlayer.Builder.setBandwidthMeter line708. Exact1.11.1 source explicitly disallows the NO_OP singleton. Caught before signed publication; v0.1.79 unaffected.
- Fixed with a positive fixed local-track bandwidth estimate implementation, no connectivity/transfer monitoring. The generic default meter was rejected as a solution because its constructor registers NetworkTypeObserver. No network/media source expansion or permission added. Actual first-frame test remains the regression gate; 14 other Android cases passed.

### H-019 — Passing UI tests lost screenshots during connected-test teardown
- CI86 UI cases passed but post-test adb pull had no app external directory. Package-owned external output is removed with AGP connected-test uninstall; no retained screenshots existed to inspect. Test-only UiAutomation shell now captures owned synthetic screens in a shell-owned Download subfolder; CI pulls that path. No production filesystem permission, media write or test fixture in release. Image retrieval/inspection pending.

H-017/H-018 verification: explicit FFmpeg fixture setup and supported fixed local estimate passed full CI #87 / 43677324287. All15 Android cases include actual first-frame decode/seek, deny/ungranted playback, resume isolation and existing SAF/Room tests; JVM and signed manifest/certificate gates passed. H-019 image retention still awaiting CI88 artifact inspection; H-015 actual OPPO appearance remains pending.

H-019 image retention verified in CI88 Android job: all5 PNGs retrieved and visually inspected (owned synthetic catalogue/search/details/Glass on/off). Found narrow320dp single-column/clipped-filter and bright-dock contrast defects despite bounds tests passing. Catalogue cell threshold/filter allocation and deterministic dock text scrim corrected; source screenshot regression strengthened, new image inspection pending. H-015 remains physically unverified on OPPO.

### H-020 — Search UI test did not await asynchronous accessibility edit
- CI89 second movie is now visible in corrected two-column layout. The absence assertion ran before Compose/accessibility reflected ACTION_SET_TEXT, producing a stale-node failure; old one-column fixture could falsely pass because that movie was offscreen. Fix waits up to10s for actual nonmatching-title disappearance before checking search/details/play. No blanket sleep, retrying a failed workflow or dropping assertions; application search code unchanged. Follow-up CI pending.


### H-020 resolution — 2026-11-15
CI90 / 43678768709 passed the full Android15 integration suite including the corrected asynchronous catalogue-search case, after waiting up to10 seconds for the nonmatching visible title to disappear. Correct-title, details and play-identity assertions remain enabled. Root cause and test-only fix are confirmed; no application behavior change was needed. No OPPO UI result is inferred.


### H-021 — Room migration helper crashes before migration execution
- **Status: resolved and fully verified in CI #97.**
- First observed: CI #95 / 43734224640, Android 15 instrumentation, commit 76330c96. The new migration case failed in `androidx.room.migration.bundle.SchemaBundle.deserialize` with `AbstractMethodError` for `GeneratedSerializer.typeParametersSerializers()` while `MigrationTestHelper.createDatabase` read the v1 schema. It failed before SQL; 13/17 tests completed with this one failure.
- Root-cause confidence: high that this was a binary incompatibility between Room schema-serializer bytecode and Android-test kotlinx.serialization runtime; exact resolved runtime versions were not printed.
- Correction: remove the test-only Room helper dependency. Load the exact committed v1 JSON, use Room-generated CREATE/setup SQL and identity hash to build a version-1 database, then open it through production Room with `MIGRATION_1_2`. The production migration and DAO operations passed in CI #96.
- Regression evidence: CI #96 Android 15 XML contains 17 tests, zero failures/errors/skips, including `v1MediaSurvivesAndManualOverrideTableWorksAfterMigration`. This resolves the earlier harness failure; the overall workflow was still red at the separate schema-export gate. No APK release, device acceptance or performance result.

### H-022 — Room schema JSON side effect was absent after Gradle task cache restore
- **Status: resolved in CI #97 / run 43737778787.**
- First observed: CI #96 / run 43735914768, verify job 132880042081. Android 15 instrumentation succeeded with 17/17 tests. Verify logs show `:app:kspDebugKotlin FROM-CACHE`; the validator found only tracked `1.json` and no generated `2.json`. Unit/build/test-APK tasks passed; the schema gate prevented downstream signed APK and privacy/certificate checks from running.
- Cause: Room writes schema JSON as a KSP side effect outside the task outputs restored by the Gradle cache, so a cache hit did not put the file in the verify workspace.
- Correction: force `:app:kspDebugKotlin` execution with Gradle `--rerun-tasks` immediately before schema verification. CI #97 reran KSP, generated both snapshots, passed schema validation, and completed signed APK, production manifest privacy/identity and permanent-certificate gates. The Android 15 XML reports 17 tests with zero failures/errors/skips.
- Artifact review: v1 remains byte-identical at SHA-256 `820f2e814c9fcc3780be7c4bf6a1cbcb2498d52d1f92a0439b58aef11c9d6d04`; v2 is exactly generated at SHA-256 `6d2a1dd422834f5cadf3e6c4292aed393bf2a784adc3cef1108ba9975c0bd4fe`, with only the intended composite-key `manual_tmdb_matches` entity. The v2 file is committed in main.
- No app runtime behavior, database migration semantics, permission, network access, signing key or user release changed. VersionCode 97 is a CI-only signed artifact, not a Release. No OPPO acceptance or handset performance result.


### H-023 — Android app data can transfer despite allowBackup=false on some OEMs
- **Status:** Configured Android modes fixed and CI-verified; actual OEM backup/restore behavior remains untested.
- **Evidence:** Android's official Auto Backup documentation (https://developer.android.com/identity/data/autobackup) states that some Android 12+ manufacturers may allow D2D transfer even with `allowBackup=false`; when a backup mode has no rule section, all app data is enabled by default.
- **Correction in this commit:** preserve `allowBackup=false`; add legacy `fullBackupContent` exclusions and Android 12+ `dataExtractionRules` exclusions for all nine documented domains under both cloud-backup and device-transfer. Add XML/manifest tests and make the release APK checker require both attached resources.
- **Regression evidence:** seven focused Python tests passed locally. CI #99 / 43740997631 passed all 17 Android 15 integration tests, the full Python script suite, debug/release builds, schema validation, compiled manifest check (`allowBackup=false`, both rules referenced, no unexpected permissions/debug provider), APK signature and pinned permanent certificate. VersionCode99 APK SHA-256 `29f3945c4c8aa5e1501e90a3f05ad5de7685d7acc543821bb3fc74e57f9296fb` is CI-only; release publishing was skipped. No app runtime, permission, network, schema or signing identity changed.
- **Remaining limit:** Android 16 QPR2 cross-platform transfer needs real iOS bundle ID/Team ID/content version. There is no iOS Player target; that mode is not claimed as excluded and no placeholder IDs are used.
- **Next:** keep actual OEM transfer behavior unverified. Continue independent privacy work on explicit opt-in/destinations; do not add persistent credentials or HTTP until their design/tests pass.


## H-024 — Room v3 schema and offline metadata reconciliation
- **Status:** Resolved for schema/migration/cache behavior in CI; provider behavior and OPPO acceptance remain unverified.
- **Change:** Room v3 adds provider metadata keyed by TMDB ID/type and keeps local SAF document matches separate. The v2-to-v3 migration is explicit; cache visibility requires a user-confirmed binding; stale associations are removed after a successful rescan.
- **Evidence:** CI #102 / 44888106152 passed 20/20 Android 15 tests, including v1-to-v3 migration, unknown document rejection, unsafe image-path rejection and stale metadata cleanup. CI validated and uploaded v1/v2/v3; the exact generated v3 JSON is committed (SHA-256 a48bab777b7dcad1f02f0e61a1a936b95f91eca61265a1ede03d7cccbc4450db).
- **Remaining limit:** No live TMDB request or OPPO test is part of this persistence change.


## H-025 — No explicit way to remove a selected folder
- **Status:** Implementation on main; CI #104 manager/build/schema checks passed. Resume/UI coverage is pending CI #105.
- **User impact:** Changing the selected tree existed, but there was no clear action to remove the library and revoke its persisted access while leaving the original media in place.
- **Change:** Added a confirmation dialog in Library settings, removal of only Room data, resume checkpoints and saved SAF access, non-cancellable grant reconciliation and retry feedback. Source media are not written or deleted.
- **Next:** Verify unit/Android CI, then include “remove from Player without deleting originals; reconnect and rescan” in the final OPPO checklist.


## 2026-11-21 — bounded Android UI test wait
Issue: Android 15 PreviewUiAndroidTest intermittently stalled inside Instrumentation.waitForIdleSync during a modal transition; failure cascaded to next test, while identical-source retry passed. Fix: test helper polls accessibility state at 50–100 ms with explicit deadlines; callback checks are bounded. CI verification pending. This changes test synchronization, not production runtime behavior.


## 2026-11-21 — H-024: subtitle language suffix can erase simple title

**Symptoms:** `LocalSubtitleMatcherTest.capsSubtitleCandidates` failed when matching `Film.en.srt` to `Film.mp4`; complex-title language/forced tests and same-parent scanner integration passed. Reproduced by CI #128 / 44907892171 unit run.

**Root cause:** `Locale.getAvailableLocales()` includes the root locale; its empty ISO3 language value could enter the three-letter language suffix set. After correctly stripping `.en`, the next matcher iteration treated an empty suffix as a language token and erased the one-component title stem. Root cause confidence: high, based on the suffix loop and root-locale value semantics.

**Fix/regression:** Exclude blank ISO3 values and require a nonblank language token before stripping. Added a direct simple-title `Film.en.srt` regression plus the existing result-cap test with valid matching candidates. Verification pending CI; no device test. No source files/media are touched.


### R-012 — Immersive system bars vary by OEM
- **Status:** Android 15 implementation/tests pass in CI #138; actual OPPO behavior remains unverified.
- **Change:** Local playback exposes an explicit reversible system-bar toggle and restores bars on exit/composition disposal.
- **Verification:** CI #138 passed the synthetic enter/exit accessibility state test and all app gates.
- **Next:** Verify real Android 15 and OPPO window behavior with the single integrated APK; measure whether controls/system bars conflict with video surfaces.


### R-013 — Persistent player header obscures video
- **Status:** Product fix added; CI pending. Actual OPPO behavior remains unverified.
- **Evidence:** CI #151 / 44931701605 received Media3 isPlaying=true but the Compose header remained visible beyond the 2.5-second controller timeout.
- **Change:** Add a Compose-owned timeout aligned with PlayerView that explicitly hides both controller and title/track header while playing. Pause retains controls; tap/reveal and fullscreen action restart timeout.
- **Verification:** CI pending. Earlier CI #149 passed a 45-test suite before this repeatable active-playback report; #151 failed the auto-hide assertion. No APK.
- **Next:** verify active playback hide, tap restore, pause behavior and full test suite; later verify controller/subtitle layout on OPPO.


### R-014 — Auto-hide integration test tapped conditionally
- **Status:** Test correction committed; CI rerun pending.
- **Evidence:** CI #145 / 44920952692 had 42/43 Android 15 tests pass; the failing assertion was the final fullscreen exit-label reappearance check.
- **Root cause:** The test skipped its video tap if the label was already visible, then waited up to three seconds while the expected 2.5-second timeout hid it.
- **Fix:** Wait for title/header auto-hide after the fullscreen action, then tap the video and verify the exit label reappears. No product playback behavior changed.
- **Next:** Confirm CI green and continue integrated player/catalogue work. No interim APK.


### R-015 — Synthetic metadata details test isolation timeout
- **Status:** Under investigation; observed once in CI #146 / 44922204439.
- **Evidence:** `titleMetadataSearchIsManualAndMatchRequiresASecondConfirmation` timed out locating synthetic UI. Failure snapshot showed the active window in the connected one-video library rather than the synthetic test details sheet.
- **Possible causes:** transient Activity/Compose test setup race or test isolation failure; no product cause established yet.
- **Next:** rerun and compare active-window snapshot. Do not change metadata production code without reproducible evidence.


### R-016 — Subtitle viewport follows video resize frame
- **Status:** Addressed in source; CI pending; OPPO visual verification unperformed.
- **Evidence:** Pinned Media3 1.11.1 default `exo_player_view.xml` placed `SubtitleView` inside `AspectRatioFrameLayout` (`exo_content_frame`), so the subtitle viewport shared resize/crop geometry.
- **Fix:** App's Apache-2.0 layout adaptation moves SubtitleView to a sibling spanning PlayerView; accessible Fit/Fill/Zoom controls target the video frame. Automated test checks sibling hierarchy.
- **Next:** verify CI, screenshots and later OPPO cue position/legibility. Revalidate layout contract on Media3 upgrade.


### R-017 — Player integration test races fullscreen overlay semantics
- **Status:** Test timing expectation removed; CI #148 includes product subtitle/zoom changes, next corrected full-suite rerun pending.
- **Evidence:** CI #147 / 44923019327 passed 42/43. The final exit-label lookup failed after the player action changed fullscreen state; #146 showed a related visibility-timer race. The independent UI test verifies enter/exit accessible labels and reversible state.
- **Fix:** Keep the player integration focused on video auto-hide/reveal, action activation and state callback; do not perform a second accessibility-tree lookup across the controller timer.
- **Next:** Require green complete CI; do not infer a product defect from this synthetic tree race.


### R-018 — Synthetic player fixture may end before auto-hide assertion
- **Status:** Mitigation added; CI pending.
- **Evidence:** CI #148 / 44923450336 passed 44/45 tests; only the player header auto-hide assertion failed. The workflow fixture lasted ten seconds and test startup can vary under emulator load.
- **Mitigation:** Extend test-only fixture to 30 seconds so media cannot reach EOS during the controller timing assertion. Restored Media3 buffering/error placeholders in the custom layout.
- **Next:** Validate in full Android 15 CI; do not claim OPPO behavior from synthetic playback.


### R-018 — Synthetic auto-hide assertion ran before active playback
- **Status:** Test synchronization added; CI pending.
- **Evidence:** CI #150 / 44924204549 failed the auto-hide assertion; #149 passed the same 45-test app source. Extending the fixture to 30 seconds did not resolve the variability.
- **Root cause:** The test did not establish that Media3 had left idle/buffering before expecting the controller to auto-hide; PlayerView may intentionally keep controls visible in idle/non-playing states.
- **Fix:** Wait on the screen's optional internal isPlaying callback before measuring controller hide. The production callback defaults to no-op.
- **Next:** Validate all CI gates; if failure repeats after confirmed playback, investigate the actual PlayerView listener path.


### R-019 — Series detail sheet exposed release filenames as episode titles
- **Status:** Improved in source; CI pending.
- **Risk:** Raw codec/release suffixes made the episode list difficult to scan and provided no season hierarchy. The parser currently does not infer episode title text.
- **Fix:** Group existing parsed episode records by season and show zero-padded season/episode labels; retain original document identity for playback. No external metadata request is made.
- **Next:** Verify Android UI and accessibility in CI; later integrate episode titles only from confirmed metadata without guessing.


### R-013 — Persistent player header obscures video (interaction refinement)
- **Status:** Explicit timeout added; CI pending for interaction resets.
- **Change:** Reset the 2.5-second header/controller timer after seeks, track changes, resize mode changes and fullscreen action. Paused playback retains controls.
- **Verification:** CI #152's 46/46 suite passed before the interaction-reset change; CI #153 tests the explicit timer.
- **Next:** Verify all interaction paths, then continue integrated player work.


### R-020 — Format compatibility on OPPO remains unmeasured
- **Status:** Open; no claim of universal format support.
- **Evidence:** Media3 supports common containers but uses Android platform decoders by default; official docs state sample format support is device dependent. LibVLC is a viable broader candidate but adds native engine and LGPL obligations.
- **Next:** Create a local representative corpus including container, video codec/profile, audio codec, subtitle format and resolution; exercise the final signed app on OPPO and record decoder errors. Only run LibVLC comparison if material gaps appear. Preserve local-only SAF access and measure APK/memory/battery/thermal before adoption.


### R-022 — Flat default grid lacked cinematic collection hierarchy
- **Status:** Improved in source; CI pending.
- **Change:** Default catalogue displays separate Movies and TV Shows horizontal shelves. Search and explicit type filters keep the adaptive grid.
- **Constraints:** No change to local-only indexing, cached artwork, network behavior or selected-document identity.
- **Next:** Verify shelf scrolling/selection/search and synthetic screenshots across phone widths.


## H-026 — Synthetic Compose instrumentation can race MainActivity startup
- **Status:** Regression guard added; CI verification pending.
- **Evidence:** CI #155 had a metadata interaction failure whose active accessibility window contained the actual MainActivity library (including a persisted Aurora title) while the test expected synthetic content. A second failure in the same run clicked a season episode after scrolling only its header.
- **Root-cause confidence:** High that MainActivity lifecycle/startup can race synthetic `setContent`; the exact interleaving was intermittent. The episode row failure is consistent with independent list scrolling.
- **Fix:** UI tests now launch a debug-only, empty Compose host activity. The season test reveals the exact selected episode before click.
- **Regression coverage:** Existing metadata synthetic interaction and indexed season-episode playback tests. CI pending.


## H-027 — One fullscreen test retained the production activity reference
- **Status:** Corrected; full CI verification pending.
- **Evidence:** CI #157 failed Android-test Kotlin compilation at `LocalPlaybackAndroidTest.kt:136` with an unresolved `MainActivity` reference after the host refactor removed that import.
- **Root cause:** The test-host substitution did not match this line because its long one-line Intent expression differed from the other references.
- **Fix:** The fullscreen controller integration now launches the debug-only `PreviewTestActivity`.
- **Regression coverage:** CI #158 must compile and execute the Android 15 integration suite.


## H-028 — Debug test host edit removed synthetic SAF fixture provider
- **Status:** Restored; CI verification pending.
- **Evidence:** CI #158 Android test report: five failures were consistent with a provider returning null cursors or a missing playback file. Comparing manifests at `11faca5` and `62dbc67` confirmed that adding PreviewTestActivity replaced (rather than extended) the existing debug manifest and removed SyntheticSafProvider registration.
- **Root cause confidence:** Confirmed from the manifest diff and exact error behavior.
- **Fix:** Restore the non-exported synthetic provider declaration alongside the debug-only test activity. Both remain in `src/debug` and are absent from release.
- **Regression coverage:** All player fixture and SAF cursor tests in CI #160.

## H-029 — Season details test can swipe-dismiss its modal while seeking an off-screen episode
- **Status:** Changed to accessibility scrolling; CI verification pending.
- **Evidence:** CI #158 diagnostics showed the series details modal was no longer present after repeated shell swipe gestures, so the test searched the underlying library window.
- **Root-cause confidence:** Medium; gesture coordinates could hit the sheet's dismiss area rather than its nested lazy list.
- **Fix:** Scroll the smallest accessibility node that advertises ACTION_SCROLL_FORWARD; include scrollable state in failure snapshots.
- **Regression coverage:** Season/episode indexed-play test and metadata confirmation-sheet tests in CI #160.


## H-030 — Nested detail sheets do not expose an accessibility scroll action in CI
- **Status:** Test interaction correction pending verification.
- **Evidence:** CI #160 failed only the season-episode and metadata confirmation UI tests. Accessibility snapshots showed sheet content present but all exposed nodes reported `scrollable=false`; fixed-coordinate swipes in #158 dismissed the sheet.
- **Root-cause confidence:** High for missing scroll action in this test hierarchy; medium that a swipe initiated on bottom-most visible sheet text correctly targets nested scroll content.
- **Fix:** Test helper tries scroll actions, then swipes upward from the lowest visible text node, keeping the gesture inside sheet content.
- **Regression coverage:** Season 2 indexed playback and manual metadata confirmation flow in CI #162.


## H-031 — Nested detail-sheet scrolling was intercepted by the modal
- **Status:** Unified lazy content implemented; CI verification pending.
- **Evidence:** CI #160/#161 failed season-2 and metadata detail tests after the debug fixture provider was restored. CI #162 logs showed repeated sheet-content swipes ended at Pixel Launcher rather than revealing episode/search controls.
- **Root-cause confidence:** High. Source had a vertically scrollable Column containing another LazyColumn for episodes, causing the sheet and nested scroll containers to compete for vertical drags.
- **Fix:** One LazyColumn now owns all title, movie/show episode, and metadata content. Episode rows retain stable keys and lazy composition.
- **Regression coverage:** Season-2 indexed playback and manual metadata search/confirmation cases in CI #163.


## H-032 — Detail-sheet refactor retained stale block closures
- **Status:** Removed; CI verification pending.
- **Evidence:** Source review after `0f8b514` showed the old outer Column/Modal/if closing braces were carried into the new metadata item, duplicating the replacement structure.
- **Root-cause confidence:** Confirmed source-edit mistake. CI has not yet established whether the intermediate commit compiled.
- **Fix:** Remove the obsolete outer closures; retain the metadata Surface and new LazyColumn/Modal/selected-state closures.
- **Regression coverage:** Kotlin compilation and Android 15 UI tests in CI #164.

### H-033 — Detail sheet scroll gestures dismissed long content (2026-11-21)
- Evidence: CI #164 (#44938064931), Android 15 instrumentation 45/46 passed; the metadata manual-search flow failed while scrolling to the Search TMDB action. The failure snapshot showed the catalogue after the sheet had closed.
- Fix committed for verification: disable sheet swipe dismissal for this long-content details sheet and provide an accessible Close action; Android back and outside-tap continue to close it.
- Status: CI verification pending. Do not publish a release candidate until the full test, manifest/privacy and signing gates pass.

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

### H-033 resolution status — Detail list scroll and metadata confirmation
- CI #173 passes all Android 15 interaction tests after expanding the details sheet, keeping its LazyColumn scrollable, validating screen bounds for popup controls, and adding an explicit Close path. The metadata screenshot shows the manual Search TMDB action in the sheet. This verifies emulator UI interaction only; OPPO rendering and gestures remain pending.

### H-034 — Glass Lab has no visible return action and dock obscures demo copy (2026-11-21)
- Evidence: user reported the lab looked broken with a floating bottom bar; CI synthetic screenshot showed text beneath the dock.
- Fix committed for verification: add a labeled Back to library action, clarify preview-only tab behavior, add a calm bottom fade and scroll-tail clearance.
- Status: Android UI CI pending; OPPO appearance/performance not measured.

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
- Resolution: explicit return, footer fade/scroll room, and pressed material response are verified by CI #177/#178; Haze remains experimental and device appearance is still pending.

## 2026-11-21 — Completed local video reopen bug
- Issue: resume checkpoints were only saved periodically and on player disposal, so a video that naturally finished could reopen at EOF and appear stuck on its last frame.
- Change: Media3 STATE_ENDED now records position zero and false play intent and reveals the transport/header. Added an instrumentation test using the synthetic local video to verify the callback.
- Verification: CI run started; result pending. The change does not alter media files or any network behavior.
- Next: fix any CI failure, then continue subtitle controls and the production Glass/performance milestones.

## 2026-11-21 — Keep completed video reset durable
- Review found a follow-up timer could overwrite the initial zero checkpoint with duration. All save paths now share a STATE_ENDED normalizer and clear stored play intent.
- Added unit-style assertions and a synthetic playback integration assertion after the five-second timer fires. CI pending; source files remain read-only.

## 2026-11-21 — Resolve long-fixture timeout in completion test
- CI #180: 47 tests ran; the new playback test timed out waiting for the workflow-generated 30-second fixture to end after starting at zero. This is a harness timing defect.
- The test now starts from 29,000 ms and retains the five-second checkpoint follow-up assertion. CI pending.

## 2026-11-21 — Completed playback resumes from the beginning
- Failure history: CI #180/#181 used a 20-second wait from the start of a 30-second synthetic clip. CI #181 also surfaced Pixel Launcher ANR dialogs that caused unrelated UI test failures.
- Resolution: start the synthetic integration case at 29 seconds and verify the post-completion five-second checkpoint. CI #182 / 44949435610 passed all 48 Android 15 tests, plus unit/build and Room v1-v4 checks.
- Release-only signing/manifest gates were skipped for the normal push. Continue subtitle and production Glass work; phone acceptance remains pending.

## 2026-11-21 — Real catalogue navigation replaces demo-only tabs
- The production library now has functional Home/Movies/Shows/Search destinations; category tabs filter actual indexed titles and Search focuses local-only title search.
- Android 15 instrumentation checks content filtering, selected state and search focus. Verification pending. Existing Glass Lab remains a visual renderer preview; production Glass has not been enabled.

## 2026-11-21 — Correct false negative in navigation test
- CI #184: compilation and 48/49 tests passed; new tab filter behavior passed, but the test helper matched the non-clickable Movies shelf heading before the actual selected tab.
- Helper now searches every same-label node/ancestor rather than only the first. CI pending.


## 2026-11-21 — Navigation selection reporting and playback speed
- CI #185 failed one Android 15 assertion because Material3's selected navigation destination was not represented as `AccessibilityNodeInfo.isSelected`. The catalogue screen and movie filter passed. Production items now expose explicit selected/not-selected state descriptions; the updated synthetic assertion is pending CI.
- Added an accessible Media3 playback-speed menu (0.75×, 1×, 1.25×, 1.5×, 2×) and a synthetic selection test. Build/test verification pending; no APK or Reno10 performance result.


## 2026-11-21 — Navigation accessibility fix and playback-speed test verified
- CI #203 / 44952898549 passed the new explicit navigation selected-state test and playback-speed interaction; full suite 50/50, JVM unit tests/build/Room schemas all green.
- Prior selected-state failure was in the Android accessibility representation of Material3's selected item; adding an explicit `stateDescription` fixed the regression. No release-only gates or handset tests ran on this development push.


## 2026-11-21 — Production Glass dock integration (verification pending)
- CI #203 passed 50/50 Android 15 tests for the functional navigation and speed control baseline. Integrated the existing Haze dock as one bounded production surface, with a persisted private settings toggle and opaque fallback. Disabling effects detaches the Haze source.
- Added a synthetic test for disabling Glass and preserving the setting through activity recreation. Await CI and inspect safe-area screenshot; actual CPH2531 blur, frame time, heat and battery remain unverified.


## 2026-11-21 — Glass switch import failure
CI #205 failed compilation at LocalLibraryScreen.kt:517 because Compose `contentDescription` was unresolved. Added the semantics import. No tests or release gates ran; re-run pending.


### R-036 — Production Glass semantics and Manage-sheet reachability
- **Status:** Resolved in CI #220; the updated player screenshot capture is independently pending CI #221.
- **Evidence:** #206 had three failures in 51 tests: selected dock destination not discoverable, switch state not readable via `isChecked`, and setting displaced Forget-folder. In #207 the setting remained offscreen without an accessibility scroll action and Movies selected state remained unassociated with its label.
- **Verified fix:** Explicit selected semantics/content descriptions and localized switch state; keep Glass after existing Manage actions. CI #214 passed the base checks. CI #216/#217 initially showed only top actions after activity recreation; the settings sheet now starts full-height. CI #218 caught the sheet-entry race (only “Close sheet” in the accessibility tree), fixed by waiting for its visible heading. CI #219's snapshot showed the switch visible and checked; its only remaining failure was a needless swipe helper returning false. The direct node/bounds/state/persistence assertions passed in CI #220.
- **Next:** Confirm all UI regressions in full CI and inspect screenshots/player controls. No handset observation or Glass performance claim.

### R-040 — Glass Lab content overlapped floating dock
- **Status:** Resolved by viewport padding and regression in CI #222 Android tests.
- **Evidence:** CI #220 `glass-on.png` shows the lower generated artwork row continuing behind the floating four-tab dock. Tabs were clickable and in bounds; issue is visual/content occlusion, not dock reachability.
- **Fix under test:** Reduce the lab's vertical-scroll viewport by 114dp for the dock plus navigation inset; assert all three artwork labels can be scrolled fully above the dock while tab accessibility remains available.
- **Next:** Inspect the #223 updated screenshot after the next Android run. This is an isolated visual lab, not a claim of production Glass quality or OPPO blur behavior.

### R-041 — Subtitle cue decode and runtime delay API
- **Status:** Local cue callback is verified in CI #223 after explicit sidecar selection; delay UI remains deferred until stable API availability.
- **Evidence:** Existing test found a supported local SRT track and opened the Media3 selector, but did not assert cue delivery. Media3 1.11.1 stable is pinned. Official 1.12.0-beta01 release notes add the sideloaded cue offset API; current API reference marks `setTimeOffsetUs` unstable. Do not upgrade production playback to beta for this feature.
- **CI #222 evidence:** The only failure was the new cue assertion. The SRT track is configured and discoverable, but the synthetic player did not select it automatically. Explicitly select the supported text track in the test, seek back to the opening cue and then wait for cue callback; do not mislabel this as a decoder failure.
- **Fix under test:** Apply `TrackSelectionOverride` for the discovered sidecar, then assert delivery of the generated phrase in `Player.Listener.onCues`; retain the `SubtitleView` sibling-of-video-frame assertion for Fit/Zoom independence.
- **Next:** Recheck Media3 stable release notes before implementing adjustable subtitle delay/reset.

### R-042 — Player controls were difficult to restore after auto-hide
- **Status:** Verified in CI #226.
- **Evidence:** CI #223's only runtime failure was the overlay test's shell-injected center tap not restoring the header. CI #224 compiled main product sources but stopped compiling Android tests because the new accessibility action check called an `awaitNode` helper not yet defined in that test class. No decoder or playback failure was reported.
- **Fix under test:** The player surface is an accessible clickable control-restoration target instead of a pointer-only gesture layer. Verbose Back, scaling and Full screen labels are replaced in the narrow top row with original icon buttons and accessible descriptions/state; targets remain at least 48dp. Added a bounded recursive accessibility-node waiter to `LocalPlaybackAndroidTest`.
- **Verification:** CI #226 passed 53/53 Android 15 tests, including accessible surface restoration, resize/fullscreen actions, and full title display in the captured player header. Unit/build/schema/privacy workflow also passed. Screenshot's synthetic black video does not establish a real codec/first-frame result.
- **Next:** Keep regression coverage while the Compose track picker is integrated; validate on real device in final checklist.

### R-043 — Native track picker did not match the player presentation
- **Status:** Compose picker committed in `91aabf2`; CI #227 found and exposed a missing accessibility click action, now fixed in source pending CI #228.
- **Evidence:** The player transport was Compose-owned while track selection opened a platform dialog. CI #227's only failure showed the subtitle-off label existed in the accessibility tree but `isClickable` was false, so TalkBack could not activate that node. The test did not report Media3 compile or runtime playback failures.
- **Fix under test:** Add a bounded audio/subtitle selector that lists decoder-supported tracks using labels/languages/channel counts, offers Automatic restore for both types and Subtitles off, announces selected state, and applies Media3 track parameters. Put role, label, selected state and `onClick` into one explicit radio semantics node. The synthetic test selects Off, restores Automatic, selects generated local SRT and AAC. No network subtitle provider or source-file modification is added.
- **Next:** Verify accessibility action, state transitions, dismissal and full CI #228 against pinned Media3 1.11.1. Keep subtitle offset UI pending stable support.

### R-037 — Resolve localized descriptions outside Compose semantics lambdas
- **Status:** Compile failures corrected; CI #211 confirms app compilation.
- **Evidence:** CI #208 found `stringResource` inside the dock semantics lambda; CI #209 found the same in the offline poster semantics lambda. Neither run reached instrumentation.
- **Fix:** Resolve each localized label in a composable scope and assign its value from the semantics block.
- **Next:** Corrected source compiled in CI #211; continue semantics validation in CI #212.


### R-038 — Android 15 production accessibility semantics and modal scroll
- **Status:** Iterative correction pending CI #214. CI #212 stopped at compile for the separate missing `role` import; CI #213 compiled but surfaced 4 UI issues.
- **Evidence:** CI #210 ran 51 tests: 46 passed, 5 failed. CI #211 compiled and ran 51 tests: 47 passed, 4 failed. Explicit dock selected semantics now pass, as does series details. Compose clear semantics on the decorative title initial also removed merged card text; the Glass Switch Android node reported `isChecked=false` despite enabled preference. The metadata journey was blocked by missing accessible card title.
- **Correction under test:** Add a localized title/type/year description directly to each clickable card; make cards the test accessibility target. Clear and set Role.Switch/ToggleableState plus explicit accessibility click, keep localized On/Off, and verify Android checked fields and preference persistence. Accept exact text or contentDescription in tests; wait for selected semantics; directly activate the metadata confirm only after asserting safe visible bounds.
- **Next:** Verify all UI tests/screenshots in CI #214. Emulator results do not establish actual TalkBack behavior or OPPO quality.

### R-039 — Larger detail hero and backdrop caching
- **Status:** Implemented locally, security regression test added, CI pending.
- **Behavior:** Detail view now renders a wide title hero using a locally generated gradient immediately; when a cached provider backdrop exists it displays that file. Download is attempted only alongside poster fetch after the user explicitly confirms the match. Uses the existing approved CDN, route/type/dimension/size checks, and no-backup cache.
- **Next:** Ensure CI validates the two-image mock transport, disabled/offline cache reads, compiled app and synthetic screenshots. No provider live request was made.

## 2026-11-22 — Compose picker semantics compile correction
- CI #227 found missing click semantics on the track option; CI #228 found `role` could not be assigned in `clearAndSetSemantics` using the pinned Compose version.
- Corrective implementation now lets `selectable` own the RadioButton role and adds merged `semantics` with label, selection state and explicit click. Android CI result pending.
- No app storage, playback policy, network or permissions changed.
