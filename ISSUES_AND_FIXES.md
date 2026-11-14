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
