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
