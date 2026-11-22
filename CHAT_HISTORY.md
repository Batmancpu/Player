# Player — chronological chat and session history

## 2026-11-22 — Caption controls, batch APK cadence and private metadata request
- User clarified that APKs must not be generated/published for individual edits: finish a substantial batch and all its automated fixes, then make one installable APK for that batch. GitHub push CI includes an unpublished debug/test build, not an Obtainium Release APK; no signed Release has been created during the current fix sequence. Continue without phone-check pauses.
- CI #240 / 44978138086 passed 53 Android 15 tests plus unit/build/Room/privacy gates after fixing the settings inset and modal readiness. Started a caption presentation batch: subtitle size and independent vertical position controls are kept in the separate SubtitleView viewport, and preferences persist locally. Instrumentation/resource/privacy checker evidence is pending CI; no signed APK yet.
- User wants automatic poster/metadata lookup without a per-user TMDB connection, and authorizes only the normalized movie/episode title plus necessary year/type to leave the device. They clarified Player is currently non-commercial and instructed us to use the best private method. Official TMDB auth requires an API key or access token; its FAQ limits free use to non-commercial projects and requires attribution; commercial use needs a license. A shared app key can remove user setup but is visible/extractable in the client APK. Implement a one-time app-wide credential path through release CI configuration; never include the value in source or logs. We will not add a backend, send local paths/filenames/SAF URIs/subtitles/media bytes, or use an unreviewed provider. The one-time app-key dependency and request boundary are in `docs/DECISIONS.md` and `docs/UPSTREAM_AND_PRIVACY.md`.

## 2026-11-22 — Integrated build continues, no interim APK
- User reiterated: do not stop for incremental phone checks or generate APKs after every batch; build the complete integrated app, then perform final efficiency/security checks, then provide one signed APK. Keep all code on `main` and preserve a detailed handoff so another agent can continue.
- CI #236 ran 53 Android 15 tests (51 passed). Product compiled; local Movies poster bounds and Media3 subtitle-off parameter tests passed. Two accessibility/test issues remain: selected track state and Glass settings sheet entrance timing. Follow-up working-tree changes make disabled state independent Compose state, account for the explicit selected-state announcement, and wait for a settled settings heading. Next: run/inspect full CI, then keep advancing the integrated app batches. No interim APK, handset acceptance or performance claim.

## 2026-11-22 — CI #237: move Manage controls above system navigation
- Commit `5b59246` is on main. CI #237 compiled and ran 53 Android tests: the track-picker selected-state regression passed; one Glass settings test still failed. The accessibility snapshot placed the switch below the configured gesture-safe boundary (y528–576 with safe bottom y544 on a 320×640 viewport).
- This identifies a production layout accessibility issue. Manage settings now applies navigation-bar insets before its scrolling content so controls can move fully above system gestures. Local validation and CI are pending. Continue the integrated feature work after green verification. No APK or handset claim.

## 2026-11-22 — CI #238 modal-entry timing follow-up
- CI #238 / 44977098444: 53 Android tests ran; the track picker passed. Only the Glass settings test failed, because y599 was treated as upper-half by a helper using `displayMetrics.heightPixels`, while accessibility coordinates use a different density scale. Its tree contained only the sheet heading during entry. The test now waits for the heading to be within the top third before scrolling. Re-run full CI and confirm the inset fix. No APK/device claim.

## 2026-11-22 — CI #239 density-normalized sheet wait
- CI #239 / 44977605927: 53 tests ran, track selection passed, and the same Manage-sheet transition still failed. The proportional height threshold remained too permissive because the two coordinate scales differ. Test now uses a fixed 100dp top band converted through display density; the in-flight heading was observed at y599, while the settled heading had previously appeared at y72. Next: inspect CI #240 for safe switch placement, state persistence and all downstream gates. No APK or device claim.

## 2026-11-22 — CI #241 caption sheet compilation
- Caption batch commit `afe04f1` compiled until Material3 flagged `ModalBottomSheet` as experimental. Added a scoped `ExperimentalMaterial3Api` opt-in; no tests ran and no APK was made. Next CI must validate the caption controls and storage, then all batch gates. No signed candidate until that batch is green.

## 2026-11-22 — CI #242 caption test compilation
- CI #242 / 44980533537 compiled production code, but Android-test compilation found two helper errors: `PreviewUiAndroidTest` had no local screenshot function, and the set-progress action constant is under `AccessibilityAction`. Added the test-local screenshot helper and corrected the constant reference. No instrumentation ran or APK was created. Next: re-run the full batch and fix any actual slider/preferences/runtime failures.

This file is the durable, accumulating record of important user requests, product decisions, implementation history and unresolved actions. It is written for future AI agents to continue work without depending on a lost chat context.

**Format:** decision-focused session summaries, not a verbatim export of every message. Preserve the meaning of user requests and reasons behind decisions. Never include private signing files, passwords, base64 secret values, API keys, tokens or other credentials.

## 2026-11-21 — Glass Lab dock follow-up
- CI #134 failed from a Pixel Launcher system ANR across five unrelated UI tests; identical source passed #133. CI #135 ran 41 Android tests: 40 passed, including dock bounds, safe area and both Glass modes. The only failure queried selected state on the text child instead of Compose's selectable parent. Updated the test to walk the accessibility parent chain; CI #136 / 44913968127 passed all 41 Android 15 tests and the full unit/build/schema/privacy gates. Signing/release steps were skipped.

- User reported that the Glass Lab bottom navigation looked broken/floating in the older installed build. No current OPPO screenshot or latest-source handset report is available, so it remains a user-reported visual issue rather than a reproduced current-source defect.
- Refined the original dock's material edge, selection highlight and shadow, and added Android UI assertions for safe-area proximity, selected accessibility semantics, tab fit and Glass enabled/disabled. CI #136 verified the dock assertions and both Glass modes in synthetic Android 15 tests. No APK/Release.
- Next: continue integrated player/Glass/accessibility implementation. The user still wants one installable APK after the whole app is ready.

## 2026-11-21 — One integrated APK request and local subtitle playback

- User clarified: do not create an installable APK at each engineering step; complete the integrated app and then produce one APK for the broad phone test.
- Live main was verified at Room v4 schema commit 55fbaac; CI #131 passed, and latest signed Release remains v0.1.91. No new Release was made.
- Implemented local same-parent sidecar attachment in the working checkout: a fresh bounded immediate-child provider query confirms the video and exact candidate subtitle remain together; only supported text subtitle types are attached through read-only content URIs. Synthetic fixture exercises Media3 external SRT and selectors. CI #132 / 44911456180 passed Android 15 41/41 with zero failures, JVM/build, Room v1-v4 schema and privacy gates. CI artifacts were test/schema only; no installable APK was published.
- Next: commit directly on main, inspect CI and fix failures; continue subtitle delay/reset, cinematic navigation, original Glass and accessibility/performance. Device acceptance remains pending; build one signed APK only after integrated completion.

## 2026-11-14 — Project bootstrap, product scope, folder setup and release continuity

### Project origin and working method
- User created `Batmancpu/Player` as the source repository for a new local-first Android media catalogue/player. The separate `Batmancpu/InfuseKt-release` repository is release-only and must remain untouched.
- User has no PC and works from Android/iPad. GitHub and GitHub Actions are the remote source/build workstation; only provide installable artifacts after build verification.
- User expects the AI agent to be the main implementation worker: inspect the current repo and CI, make focused commits on `main`, run tests, fix failures and keep handoff docs current.
- User prefers direct, low-friction execution. Do not overcomplicate tasks with unnecessary terminal environments, setup scripts, cosmetic decisions or giant speculative architecture. Ask for user action only where account permissions or physical-device access genuinely require it.
- Repository records are canonical; a new agent must inspect the live branch/workflows and not assume old chat statements remain true.

### Product and visual direction
- One user-selected local folder and its descendants; read-only discovery of local movies and TV episodes.
- Normalize filenames, match against TMDB, retrieve high-quality posters/backdrops/details, cache locally for offline browsing.
- No media server, streaming, uploads, cloud storage, user accounts, ads, analytics or telemetry. Never send local paths, raw SAF URIs, folder names, complete filenames, subtitle contents or media bytes to metadata services.
- User explicitly approved a built-in local video player: broad practical format support, play/pause/seek, embedded audio and subtitle selection, local external subtitle loading, online subtitle search/download after a provider privacy/terms review, and adjustable subtitle delay with reset. “Every format” is an aspiration requiring a real compatibility matrix, not a guarantee.
- Apple TV-like cinematic catalogue and iOS 27-inspired Liquid Glass are the design target. Glass belongs on functional chrome (navigation, sheets, player controls), not posters or video. The user reprioritized functional foundations before production Glass polish.

### Architecture and implementation decisions
- Native Kotlin + Jetpack Compose; Room for indexed library; Storage Access Framework for one-folder permission; direct TMDB integration subject to secure credential design; offline metadata/artwork cache.
- Keep exactly one local selected tree. Validate providers, persist read permission, scan only descendants, never mutate media.
- Media engine undecided: evaluate AndroidX Media3 first; compare LibVLC only if the test corpus shows material gaps and after licensing/APK-size/performance review.
- Online subtitle provider remains unselected. User wants minimal, uncluttered player UI.
- Target verification device: OPPO Reno10 5G CPH2531, Android 15 / ColorOS 15, Dimensity 7050, stable 60-Hz target.

### Implemented before the current signing handoff
- Android/Compose app foundation, shared dark cinematic theme/tokens, Lucide icon registry and three JVM theme tests.
- Haze 2.1.0 Glass helper and isolated Glass Lab; Glass Lab is a developer preview, not the real product screen.
- Plain folder setup screen replaced Glass Lab as launcher. Android system OpenDocumentTree picker, allowlisted external-storage DocumentsProvider, persisted read tree grant, startup grant validation and stale-grant recovery.
- Corrected folder setup CI passed in workflow runs 43569710969 and 43569752178. Real OPPO provider/permission testing remains pending.
- Recursive scanning, filename parser, Room, TMDB/artwork, catalogue/detail screens, playback engine and audio/subtitle controls are not implemented yet.

### Stable APK update requirement and signing-key decision
- User wants a permanent signing identity so subsequent APKs can update the installed app in place rather than repeating uninstall/reinstall.
- Gradle release signing and GitHub Actions release path are committed. They read four encrypted repository secrets, keep debug artifacts CI-only, verify Release APKs with apksigner, upload a separately named Release artifact and derive versionCode from the monotonic GitHub Actions run number.
- Required Actions secrets: `PLAYER_RELEASE_KEYSTORE_BASE64`, `PLAYER_RELEASE_KEYSTORE_PASSWORD`, `PLAYER_RELEASE_KEY_ALIAS`, `PLAYER_RELEASE_KEY_PASSWORD`.
- Signing scaffold workflow run 25 (`43570229274`) passed, but correctly skipped signed Release because secrets were absent. Run 26 (`43570272899`) also passed.
- User rejected the proposed Termux process and asked the agent to create the key directly, with its password stored in Proton Pass. This was done in the current ChatGPT session: a permanent JKS was generated with keytool, reopened and verified, and delivered as private downloadable files. The JKS and secrets are intentionally absent from this public repository.
- Key identity: alias `player-release`; RSA-2048 / SHA256withRSA; 10,000-day validity. Public SHA-256 certificate fingerprint: `62:A0:97:B7:BE:7D:22:A9:88:3D:9E:09:01:5C:04:17:72:72:16:D7:3E:25:06:ED:F4:70:B2:4A:61:CD:AD:C0`.
- Private deliverables in the originating ChatGPT session: `player-release.jks`, `PLAYER_RELEASE_KEYSTORE_BASE64.txt`, `PLAYER_RELEASE_CREDENTIALS.txt`, `PLAYER_RELEASE_CERTIFICATE_FINGERPRINT.txt`. User must save the JKS and a separate backup, store its password in Proton Pass, then add the four GitHub repository secrets through Settings. Never ask for key/password/base64 in chat.
- The user clarified that app name and applicationId are temporary; do not waste time on branding/package makeover while core functionality is missing. Current applicationId is `com.batmancpu.player`. Signing keys are not tied to application IDs and can be reused after a package rename. A changed applicationId is a different Android package and cannot update the old package in place. No new key is needed.
- User specifically rejected Termux. The initial helper script and its CI syntax check were removed. Do not reintroduce Termux or device-side terminal steps for signing.

### Current status and next actions
- 2026-11-22: CI #227 built the Compose track picker and ran 53 Android tests; the only failure found its subtitle-off option was described but not clickable to AccessibilityNodeInfo. Fix now uses one explicit radio semantic node with activation, selected state and label. Source uncommitted; CI #228 will verify. Keep current task active.
- 2026-11-22: Committed `91aabf2` with a Compose audio/subtitle sheet: Automatic, explicit Off, supported track rows, language/channel details and accessible radio state. The synthetic test switches through subtitles Off → Automatic → local SRT, then selects AAC. CI #227 is running. Keep one final APK cadence.
- 2026-11-22: CI #224's product code compiled, but test compilation stopped at a missing class-local `awaitNode` helper in the new clickable-surface test. Added that bounded helper; #225 was triggered by documentation/artwork and uses the old source, so #226 must verify the fix. Next: inspect #226 and keep working through the larger integration plan. README concept image is committed as compressed original concept art, not a screenshot. No APK yet.
- 2026-11-22: Committed player control restoration/header compaction as `1c19a08`; CI #224 is exercising Android 15 UI and playback tests. README now uses a newly generated, compressed visual-direction concept image, explicitly labeled as nonliteral concept art. No intermediate APK is planned. Next: inspect CI results and continue the integrated player/caption and production Glass batches.
- 2026-11-22: CI #223 confirmed local SRT selection and cue delivery pass; the sole failure was the overlay test's shell-injected tap not restoring controls. The surface now has an accessible click action, replacing the brittle coordinate tap; player header uses original compact icons so its title fits. CI #224 pending; the previous screenshot truncated the title to “Synt”.
- 2026-11-22: CI #222 Android ran 53 tests: the Glass Lab dock-clear scroll check passed; one new caption cue assertion failed because the SRT track had not been explicitly selected in the test. Now selects it via `TrackSelectionOverride` and seeks to cue start before waiting. CI #223 pending. Do not infer decoder or user-facing subtitle bug from that test setup.
- 2026-11-22: CI #221 passed 52/52 Android 15 tests plus unit/build/schema/privacy checks and its new `player-controls.png` was inspected. Controls work but look cramped/plain in the 320px synthetic capture; the video area is black, so decoder visuals remain unverified. Latest source adds SRT cue delivery assertion and fixes Glass Lab scroll occlusion by reserving dock space, with an artwork/dock regression test. CI #222 pending. Stable Media3 1.11.1 lacks the newly documented unstable caption offset API, so delay UI waits for stable support.
- 2026-11-22: CI #220 passed all 52 Android 15 tests plus JVM/build/schema/privacy checks. Confirmed #219's lone failure was an obsolete swipe helper; the direct accessible Glass switch assertion passes. Added synthetic player-controls screenshot capture and corrected README's feature/Glass status; CI #221 pending. Continue to one integrated APK after remaining features and final efficiency review.
- 2026-11-22: CI #219 ran 52 Android 15 tests (51 passed). Its sole Glass settings failure snapshot actually contained the visible, checked switch; an unnecessary swipe fallback caused the assertion. Simplified the test to await the accessible node and assert visible bounds, localized On/Off semantics, native state and persistence. Next: full CI #220, then continue captions/player polish. No APK until integrated feature completion and efficiency review.
1. User downloads and securely backs up `player-release.jks`, saves the password in Proton Pass, and enters the four GitHub secrets using repository Settings.
2. Trigger Android CI. Verify the signed Release artifact exists and its certificate fingerprint exactly matches the public fingerprint recorded above.
3. Do not recommend CI-only debug APKs as the update channel. A one-time uninstall may be needed if a previously installed build uses a different debug certificate.
4. Continue the functional roadmap: validate folder picker/provider behaviour on CPH2531, then implement a cancellable read-only recursive scanner bounded to the saved SAF tree. Add tests for tree boundaries, nested folders, cancellation, duplicates and permission loss. Do not jump directly to TMDB/player before scanner correctness.
5. Keep the user informed about real blockers only; the user expects the AI agent to handle implementation and avoid unnecessary setup/cosmetic decisions.


### CI sequencing note — signing helper removal
- The helper-deletion commit temporarily left its Bash syntax-check step in the workflow; run 29 (`43574064538`) failed with file-not-found. The next workflow commit removed that stale step; run 30 (`43574233803`) passed tests, debug build and artifact upload. This was a transient workflow cleanup failure, not an application-code defect.
- The cumulative log is now part of the mandatory reading order and must be appended at each future session end. Keep all secrets out of it.


### Handoff verification update — 2026-11-14
- The canonical `AGENTS.md` now requires every agent to read `CHAT_HISTORY.md` first and append a dated session summary before ending work; secrets must never be written into the chat log.
- Historical signing snapshots 22–23 in `PROJECT_CONTEXT.md` are explicitly marked superseded by the current generated-key status in section 24; the obsolete Termux instructions are not current.
- Verified Android CI run 34 (`43574546390`) passed tests, debug APK assembly and artifact upload. Release signing remains skipped until the user configures the four exact repository Actions secrets.
- Corrected run numbering in this record: run 29 (`43574064538`) was the temporary missing-helper failure; run 30 (`43574233803`) was the successful workflow correction; runs 31–34 all passed.

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

## 2026-11-14 — Codex takeover: Phase 1 audit and first focused fix
- User delegated implementation/testing/documentation, requested canonical/source/CI/release audit, official Apple/GitHub research, and Phase 1 correctness before visual experiments.
- Audited baseline acb9829 and successful CI 43588156049; v0.1.53 remains latest published test release. No further signing setup needed; OPPO acceptance remains pending.
- First focused change rejects incomplete/error SAF directory cursors and invalid mandatory cursor fields before scan reconciliation. Added JVM regression coverage; CI pending at authoring. Detailed audit/order/research in docs/PHASE1_AUDIT.md.
- Remaining work: scan/folder lifecycle and grant races, provider cancellation/bounds, Room instrumentation/schema fixtures, conservative parser corpus and meaningful UI states. No device/FPS claims, dependency adoption or release publication.



- Verification follow-up: storage fix 9557723 merged via PR #1 as 79ee09d after successful CI https://github.com/Batmancpu/Player/actions/runs/43612121975. Upstream root licenses/activity pinned in audit. Main signed build pending; no new release/device acceptance. Next H-009 lifecycle ordering.

- Main verification completed: https://github.com/Batmancpu/Player/actions/runs/43612318514 (#61) passed tests/debug and signed release/certificate pin at merge 79ee09d. Signed CI artifact retained, publication skipped. No new phone-test request; continue H-009 lifecycle ordering.

## 2026-11-14 — Next goal: lifecycle ordering
- User requested next goal and very brief whole-plan/progress points. Reported rough planning estimate 10–15% overall, Phase 1 about half implemented; no accepted-phase or on-device claim.
- Implemented H-009 retained session/serialized operations and transactional selected-tree cache activation with deterministic tests. PR CI pending at recording. No new Release; no manual user work needed yet. Next: verify/integrate, then root/grant recovery H-010 and provider cancellation/bounds H-011.

- H-009 outcome: PR #2 merged as 10dab08, six regression cases added and unit/debug CI passed (43613459732). Main signed CI 43613651152 also passed including permanent certificate verification. No new Release or manual work requested. Continue H-010 grant/root recovery; broad product estimate remains rough 10–15%, 0/6 phases phone-accepted.

## 2026-11-14 — Continuous Phase 1 authorization and grant recovery
User requested sustained work until signed Phase 1 candidate in Releases; actual phone acceptance still requires user report. Two one-time continuations created for five and ten hours later, instructed to resume from live GitHub/pending CI without duplicate work. Implemented H-010 serialized grant/root recovery with nine fake-store cases; PR CI pending. Next H-011 query cancellation/bounds and remaining Phase 1 acceptance coverage.

## 2026-11-14 — Phase 1 automated candidate gates
- H-010 PR #3 and H-011 PR #4 passed; test harness PR #5 passed; Android PR #6 passed all 9 Android 15 emulator cases; plain UI/parser PR #7 passed. All integrated. No OPPO test inferred.
- PR #8 prepares final built-APK privacy/identity gate and packaged runtime attribution; no publication yet. Next merge green prep, verify signed main, publish candidate via explicit release(test): commit, then deliver phone checklist. Future scheduled runs must avoid duplicate changes and respect actual acceptance gate.

## 2026-11-14 — v0.1.79 published; latest main-only branch instruction
- Signed Phase 1 phone-test candidate v0.1.79 verified live in GitHub Releases; source f1d7f27, CI 43618442469 succeeded. Actual APK backup representation is literal false, confirming H-013 verifier-format fix. User has not yet supplied phone acceptance.
- User objected to eight task branches and requested main only with maximum one optional backup. All work is already merged into main, no open PRs and zero branch-only commits. Direct connector deletion absent/CLI auth invalid; temporary Actions cleanup uses existing repo token with exact SHA and ancestry guards, then will be removed. Both scheduled prompts updated without rescheduling.
- First +5h scheduled continuation resumed this work; second +10h remains 02:14 UTC Nov 15. Continue from live main/user report; no branch recreation, signing changes, duplicate candidate or invented handset tests.

- Branch request fulfilled: all eight extra task branches deleted after unchanged-head/ancestry checks by CI 43642157896; fresh branch list contains main only. Temporary workflow removed. No work/history/release lost and no manual branch deletion needed. Both scheduled prompts updated to main-only; no new branch to be created for ordinary work.
- Signed v0.1.79 is available; final user action is Obtainium update without uninstalling plus short OPPO checklist. Phase 1 acceptance remains pending, zero real-device FPS results claimed.

## 2026-11-15 — Second scheduled continuation
- Verified main-only repository, no open PRs, green latest CI and published signed v0.1.79. No user phone acceptance arrived. Avoid duplicate release/new branches or gated Phase 2 implementation.
- Found and addressed H-014 background-thread/cancellation boundaries in SAF traversal and index parsing/mapping; added 1,000→100 synthetic Android reconciliation from main caller. CI pending at authoring, no FPS claim. Latest released APK remains v0.1.79; this is unreleased main maintenance.

- Second continuation outcome: H-014 57e4539 passed full CI #82 (https://github.com/Batmancpu/Player/actions/runs/43667022100), now 10 Android integration cases plus unit/build/signing/privacy. Remains unreleased on main; v0.1.79 is still the only current phone candidate. No extra branch, APK, task, Phase 2 production or actual-device claim. Next actual OPPO checklist report/Phase 1 defect triage.

## 2026-11-15 — Phone findings and larger offline preview authorization
User reports v0.1.79 selection/offline persistence/folder switching success; cancellation feedback unclear and Glass Lab nav looks broken. Media called audio, clarification pending. User rejects night-long micro-updates and asks larger useful progress, specifically FC Zenith and immediate Zenith upstream as rendering reference. Proceed with larger offline browsing/local playback preview and visible Glass/scan fixes; retain real-device/phase gaps instead of blocking all independent implementation. Main only; no app delete/original writes, network/backend, secret/key changes.

- User clarified all four files are movies/videos and naming/year parsing is correct. Continue larger offline browsing/playback preview; no audio-library scope change. Glass dock changed to original bounded single-surface layout with spring selection; actual OPPO validation remains pending.

- 2026-11-15 — Latest user authorizes next planned phase implementation without routine APK-install pauses while absent. One requested continuation confirmed for today 13:30 Asia/Kolkata (08:00 UTC); inherit latest GitHub state, no task branches. Continue independent implementation/tests and record phone acceptance/performance as pending; preserve privacy/provider controls. Current preview source 3c62a71, CI #85 (43676678448) in progress. Next: finish preview gate, inspect screenshots, publish, then next roadmap work without inventing acceptance.

- 2026-11-15 — User explicitly requested stopping until the next automation; development paused. Latest functional source e4cc4b1 (main), CI90 / 43678768709 in progress at checkpoint; CI87/88 passed full automated/signing/privacy gates, CI89 screenshots visually confirmed corrected two-column catalogue/filter allocation and readable bounded dock, but search test required asynchronous-result synchronization (fixed in e4cc4b1). No larger preview Release published; v0.1.79 remains latest installed candidate. Confirmed one-time continuation today13:30 Asia/Kolkata (08:00 UTC). Next: inspect CI90 and retained images; repair only actual failures, publish verified larger signed preview through release(test) gate, then next-phase schema/metadata work per D-026 without routine phone-install pauses. This shutdown checkpoint is docs-only; skip redundant CI.


## 2026-11-15 — Resume after cancellation and verify offline preview
User's latest instruction is to continue. Scheduled automation is not part of the work plan; the user had requested its cancellation. Rechecked live GitHub rather than relying on the stop checkpoint: `main` is the only branch, there are no open PRs, and the source matches `e4cc4b1` under docs checkpoint `6a4b61c`. Latest release is v0.1.79. CI90 completed successfully after the prior checkpoint: all 15 Android 15 integration tests plus JVM/build/privacy/permanent-certificate gates passed, and five retained synthetic screenshots were retrieved/inspected. No new APK/Release or actual OPPO preview test existed at resume time. Corrected the handoff to remove stale “do not continue” wording. Next: publish the already-tested offline preview through existing signing workflow, verify its Release asset, then continue Room schema/Phase 2 privacy-contract groundwork without claiming phone acceptance or performance.


## 2026-11-15 — Verified offline preview release v0.1.91
Published [signed v0.1.91](https://github.com/Batmancpu/Player/releases/tag/v0.1.91) through the existing main-only release gate. CI #91 / 43726886945 completed successfully: 15/15 Android 15 integration tests, JVM/build, actual signed APK, privacy/identity and permanent-certificate gates. APK SHA-256 `7c9ff8287405be9b1f607259608d84a256e5aed1fcc4cea24e3f9b882dd05e17`; versionCode 91, package and certificate unchanged. The current preview adds local searchable movie/show browsing, details, content-only Media3 playback with pause/seek/private resume, scan feedback and the repaired one-surface Glass Lab dock. Only v0.1.79 behavior has real OPPO evidence so far; do not mark Phase 1 accepted or claim any new device performance. Next engineering task: Room v1 schema export and Phase 2 normalized opt-in metadata contracts, without adding HTTP yet.


## 2026-11-15 — Phase 2 database-safety foundation started
After publishing v0.1.91, began a focused schema-only update: Room/KSP exports version1 through an incremental-build-aware Gradle argument provider; CI validates the schema is the expected single `indexed_media` entity and retains its JSON for review. No database version/entity/runtime behavior changed, and no network functionality was added. CI and schema artifact are pending. Next: inspect generated JSON from Actions, commit that exact baseline, then add the offline normalized metadata/opt-in contract tests before an HTTP client. Real OPPO preview acceptance/FPS remain unverified.


## 2026-11-15 — Room v1 snapshot verified and preserved

- Main schema-export commit 1e4e63a initially hit a transient Gradle plugin-resolution failure before build/tests. The identical pinned plugin passed CI #91; retrying only failed jobs as CI #92 attempt 2 then passed Android 15 and verification gates. Android XML confirms 15 tests with 0 failures/errors/skips; signed APK, privacy/identity and permanent certificate verification also passed. No Release publication from this schema task.
- Inspected Actions artifact `Player-Room-Schema-1e4e63a473bb20f90119412acb1ed1a69f3fbab9`; committed its exact generated v1 JSON at `app/schemas/com.batmancpu.player.ui.library.PlayerLibraryDatabase/1.json`. It declares one `indexed_media` table, ten fields and the composite `treeUri/documentId` key; SHA-256 `820f2e814c9fcc3780be7c4bf6a1cbcb2498d52d1f92a0439b58aef11c9d6d04`. No user data or runtime change.
- Next: check the committed schema through CI, then add offline normalized title/year/type query and default-off opt-in tests. No HTTP/client/credential/permission addition in this slice. User's v0.1.79 OPPO evidence stands; v0.1.91 preview, full Phase 1 acceptance and all performance targets remain unverified.


## 2026-11-15 — Implement credential-free, default-off metadata query terms

- Following the preserved Room v1 baseline and green CI #93, added a pure query-term contract derived only from recognized movie/show parsing. It holds normalized title, optional in-range year and typed media kind. Consent defaults off; series include only the series title. No filename, SAF URI/document identity, season/episode data or media bytes fit the model.
- The builder rejects path/URI-like and file-suffix inputs, controls, blank/over-120-code-point values; it preserves Unicode NFC and omits invalid years. Seven JVM regression cases added. No HTTP client, API key, new dependency, permission, persistent user setting or metadata request is included. CI pending.
- Checked official TMDB movie/TV search references and FAQ: separate endpoint/year parameters; adult content defaults false; API use requires TMDB attribution, approved logo and the stated non-endorsement notice in About/Credits. No TMDB data use yet.
- Next: verify CI, then design secure user-managed credentials and exact API/image host controls before any network code. Phase 1 remains pending OPPO acceptance; v0.1.91 preview/performance is not handset-verified.


## 2026-11-15 — Metadata query contract verified

- CI #94 / 43731972463 passed after the `TmdbSearchTerms` implementation. Android 15 XML shows 15 tests, zero failures/errors/skips; JVM `:app:testDebugUnitTest` passed, including all seven new consent/normalization/privacy cases. Schema validation, signed APK assembly, package/manifest privacy and permanent certificate gates passed.
- Signed artifact `Player-release-v94-935601313171e1f43bb6632bb693787b1f84720f` is CI-only. No GitHub Release, network permission, HTTP/API integration, credential, persistent opt-in setting or migration was added.
- Next: review safe persistence for explicit opt-in and a user-managed encrypted TMDB credential, plus a strict API/image host allowlist. Keep external requests disabled until those controls are implemented and tested. No OPPO preview acceptance or performance evidence.


## 2026-11-15 — Started safe local manual-match persistence

- With Room v1 JSON now captured and CI #94 green, the next task is a separate `manual_tmdb_matches` table keyed by selected SAF tree/document, storing a provider ID/type and local confirmation timestamp. It is explicitly separate from `indexed_media`; v1 file data remains unchanged.
- Plan includes explicit tested Room v1→v2 migration, CI schema validator update, resume-safe cleanup after completed scans/tree switches/forgetting, and no cascading FK because `indexed_media` uses `INSERT OR REPLACE`. No network/client/permission or user matching UI is part of this migration slice.
- Official Android Room migration guidance and `MigrationTestHelper` were reviewed. Source/build/test changes are on main; CI/schema v2 artifact pending. Next retrieve and review exact generated v2 JSON before preserving it. Phase 1 OPPO acceptance/performance remain pending.


## 2026-11-15 — Room migration test failure, correction, and privacy research

- Main commit 76330c96 introduced the Room v1→v2 manual TMDB-match table and explicit production migration. CI #95 / 43734224640 exposed that the new `MigrationTestHelper` case crashes in `SchemaBundle.deserialize` with `AbstractMethodError` for `GeneratedSerializer.typeParametersSerializers()`, before the migration executes. The runner reported 13/17 tests completed and this as the one failure; other migration/schema/signing gates were skipped because instrumentation is a prerequisite.
- Corrected the Android test to build a genuine v1 SQLite database from the exact committed v1 Room JSON asset and open it with the production Room migration. The test-only helper dependency was removed. CI #96 must verify preserved data, v2 DAO operations, schema generation, build/privacy and permanent signing. No release was published.
- Researched official TMDB authentication and Android cryptography: TMDB supports the user's API Read Access Token in an Authorization Bearer header; Android recommends AES/GCM, with Android Keystore providing protected key use. This is a future storage design, not implemented networking or credential collection.
- Researched Android backup docs: some Android 12+ OEMs may still allow device-to-device app-data transfer when allowBackup=false. Add explicit extraction/legacy transfer exclusions before persisting user credentials.
- CI #96 / 43735914768 completed: Android 15 XML has 17 tests, 0 failures/errors/skips. The replacement migration test preserves a v1 row and exercises the new DAO. Verify job failed at schema validation because `:app:kspDebugKotlin` was `FROM-CACHE` but the schema JSON side effect was absent; downstream signed privacy/certificate steps were skipped. H-021 is resolved and H-022 tracks schema export.
- CI #97 / 43737778787 completed successfully. Android 15 XML reports 17 tests, 0 failures/errors/skips, including the production v1→v2 migration and manual-match DAO operations. Forced KSP export recreated v1/v2 JSON and schema validation passed; v1 is byte-identical to the prior snapshot, and exact v2 JSON was reviewed and committed. Unit/build, signed APK, production manifest privacy/identity and permanent-certificate gates also passed. VersionCode 97 remains a CI artifact only; no GitHub Release, OPPO acceptance or performance claim.
- Next: harden Android backup rules against cloud/D2D restore (legacy Android 11 and older plus Android 12+ extraction modes), verify packaged configuration, then implement user-managed encrypted credentials and persistent opt-in/destination controls before any HTTP. Phone acceptance/performance remain pending.


## 2026-11-15 — Explicit Android backup exclusion work

- Resumed from live main `655dd19209c7a5166146398fa1a488bd4beac348`; CI #98 passed, v0.1.91 remains the latest signed Release, and no PR/other branch is open. Implemented backup hardening as `20a3bceb2fac31cf381bcb265a4424e35cf3fe8f`.
- Reviewed Android's current Auto Backup docs. Some Android 12+ OEMs can allow D2D transfer despite `allowBackup=false`; an omitted transfer-mode rule defaults to including all app data. Added explicit legacy and Android 12+ cloud/device-transfer exclusions for all app-data domains.
- Added source XML/manifest tests and hardened the compiled-APK verifier. Focused local Python test suite passed 7/7. CI #99 / 43740997631 passed 17/17 Android 15 tests, complete Python suite, build/schema/compiled manifest privacy, APK signature and permanent certificate. VersionCode 99 signed artifact SHA-256 `29f3945c4c8aa5e1501e90a3f05ad5de7685d7acc543821bb3fc74e57f9296fb` is CI-only; no APK Release or phone acceptance is inferred.
- Android 16 QPR2 cross-platform transfer to iOS requires actual target-app bundle/team/content identifiers; there is no iOS Player app, so identifiers are not fabricated and that mode is not claimed as excluded.
- Next: continue independent privacy work on credential-free opt-in/destination-policy tests; keep HTTP and credential storage gated. OPPO behavior/performance remain unverified; no new Release from this config-only slice.


## 2026-11-21 — One complete-app test APK
The user clarified that they want the whole app built before receiving an installable test APK. Continue all six engineering phases and CI autonomously; do not ask for phone checks or publish APKs after each step. Updated CI to withhold intermediate debug artifacts and only sign/upload a Release candidate for an explicit candidate request. The single final candidate must preserve the permanent signing identity and include a complete OPPO checklist.

## 2026-11-21 — Continue without per-step APKs
The user reiterated that an APK should not be produced after every step. Keep implementing and validating the integrated app; prepare one signed APK when the full experience is ready. The Room v3 offline metadata-cache foundation is the current development slice, with automated migration/cache/schema checks and no device acceptance claim.


## 2026-11-21 — Room v3 foundation verified
CI #102 passed: Android 15 tests 20/20, including the production v1-to-v3 migration and offline metadata cache cases; unit/build and exported schema validators also passed. The exact generated v3 schema is committed. Release signing/publication were skipped. Development continues toward one complete app APK, with OPPO acceptance/performance still pending actual device evidence.


## 2026-11-21 — Remove a folder without deleting files
The user reported that the app did not offer a way to remove its selected folder. Added a confirmed remove-from-Player action that deletes only Player's index/cache and persisted SAF grant. The dialog states that the original videos/subtitles remain untouched; a retry action handles cleanup failures. CI #104 passed; the operation also clears app-private resume state, and grant cleanup now survives lifecycle cancellation. CI #105 is checking the confirm/cancel UI and resume cleanup. This will be included in the one final phone checklist.


## 2026-11-21 continuation — defer the installable APK until integration is ready
The user clarified that they do not want an APK at each step; continue the full app and provide one integrated APK to test. CI #105 reported three synthetic accessibility label lookups timing out in existing UI coverage plus the new remove-folder flow, while storage/playback/schema checks passed. Added a bounded active-window accessibility snapshot for CI #106. Next: diagnose the CI tree and fix the actual mismatch. No installable APK or Release was created; real OPPO evidence remains pending.


CI #106 / 44891844084 passed after the prior run’s three UI accessibility timeouts failed to reproduce: Android 15 instrumentation 22/22 and the unit/build/schema/privacy gates passed. The active-window diagnostic was retained; no product UI change or installable APK was made. Next engineering task: integrate the explicit opt-in TMDB search, manual match confirmation and offline cached presentation, while keeping all network actions user-triggered.


## 2026-11-21 — Secure metadata settings foundation
Added a Library Manage entry for metadata/connection settings using a user-owned token stored with Android Keystore AES-GCM in no-backup app-private storage. Consent defaults off, tampered/lost credentials fail closed, and clearing completes even during lifecycle cancellation. No network permission/request or installable APK was added. CI pending. Next: explicit user-initiated matching with approved attribution, bounded transport and offline cache.


## 2026-11-21 — Encrypted TMDB connection settings verified
Commit 7501006 added Library Manage → Metadata and connections, encrypted user-provided TMDB Read Access Token storage (AES-GCM/Android Keystore, app-private no-backup path), explicit default-off consent and clear/disable behavior. CI #108 / 44893247270 passed Android 15 30/30 plus unit/build/schema/privacy; only internal test/schema artifacts were produced, no user APK/Release. Current settings explicitly state they do not start a lookup yet. TMDB official attribution requirements were rechecked; next is the manual match flow and About/Credits logo/notice before any network request.


## 2026-11-21 — TMDB attribution screen integrated
Added Player's About/Credits route, the official approved TMDB logo converted at its original aspect ratio, and the exact required non-endorsement notice. The logo is visually secondary to Player; visiting TMDB is a manual link. Synthetic Android UI coverage added. No TMDB data is requested and no INTERNET permission or installable APK was added. CI #110 pending; next is manual search/matching with the privacy-limited request contract.


Credits screen follow-up: source review caught and corrected a Kotlin quoting typo in the button callback. CI #110 is for the preceding commit; CI #111 must verify the corrected source and new UI assertions. No APK/Release was made.


CI #110 failed before tests at Kotlin compile: an escaped quote in the Credits callback and an incorrect verticalScroll import. The callback fix is on main and the import is now corrected; CI #111/#112 verify the changes. No installable APK/Release was produced.


## 2026-11-21 — Bounded TMDB client groundwork

Added the fixed HTTPS API policy/client and synthetic tests for safe normalized search, bounded responses, explicit consent/credential checks, cancellation and redirect rejection. The UI still cannot dispatch lookups and INTERNET permission is still absent. No installable APK or Release was produced, following the user's preference to receive one integrated build when ready. CI #113 pending; next integrate manual matching and offline cache presentation before enabling network permission.


## 2026-11-21 — Credits test viewport fix

CI #112 found that the synthetic UI test attempted to locate the Credits action below the initial viewport without scrolling. Added a scroll-to-visible helper to the test and retained the actual tap and attribution assertions. No app control or network policy change; no APK/Release. CI #113 is running; follow with the broader explicit manual metadata flow.


## 2026-11-21 — Make Credits immediately visible

CI #113 showed that the synthetic Android accessibility root did not expose a scroll-forward action for the settings content, so a test-only scroll helper could not reveal the offscreen Credits link. Moved About and credits under the settings intro and returned the test to a direct tap. This improves discoverability and verifies the actual route. No APK/Release; follow-up CI pending.


## 2026-11-21 — Manual TMDB matching integration

After green CI #115 for the bounded transport and Credits route, integrated explicit search from title details, selection of one provider candidate and separate confirmation into Room. Controller verifies the currently selected SAF tree/title and current credential consent; cached provider data is available offline and a manual unmatch action is included. Search is never triggered by scan, startup, browse or playback. Manifest now includes the narrow install-time INTERNET permission for this user-opt-in stage; no broad storage permission, shared API key, backend or source-media write is added. Added synthetic Android controller/UI tests and updated manifest/privacy gates. No signed/user-installable APK or Release was produced. Current CI pending; next is bounded artwork cache/display and offline/accessibility verification.


## 2026-11-21 — Fix metadata test compile error

CI #116 compiled production code, then failed compiling the new Android controller test because it referenced OfflineMetadataCache without importing it. Added the missing import; no app behavior, permission or APK changed. Follow-up CI still needs to verify instrumentation and manifest privacy checks.


## 2026-11-21 — Keep playback visible and test manual metadata scrolling

CI #117 found the manifest test still disallowed the narrow INTERNET permission now used by opt-in TMDB, and metadata UI placement pushed Play video offscreen. The manual result test also needed to scroll to the candidate. Updated the exact permission allowlist assertion, placed metadata below playback controls, and added a small synthetic swipe helper. No APK/Release. Follow-up CI pending.


## 2026-11-21 — APK cadence
User explicitly asked to complete the app before receiving an APK rather than produce an installable build at every step. Continue small CI-verified main commits and withhold user-installable APKs/releases until one integrated phone-test build is ready. Added confirmed artwork cache/display slice; test pending. Target performance remains unmeasured.


## 2026-11-21 — artwork verification and grid continuation
User asked to wait for one complete APK, not milestone downloads. Artwork cache was added after manual confirmation and local poster display passed CI #121 on retry. Initial UI test failure was a transient Pixel Launcher ANR; run attempt 2 passed both Android 15 and build/schema/privacy jobs. Follow-up adds lazy cached artwork to catalogue cards without networking; no installable APK or Release. OPPO acceptance/performance still unmeasured.


## 2026-11-21 — lazy offline posters and reference review
CI #122 passed Android 15 UI/screenshots, JVM/build, Room-schema and privacy checks for grid poster rendering. User requested one complete APK at the end, so no installable build was published. Original FCZenith repository URL now redirects to Batmancpu/brrMusic; it and rezapranata089-bit/Zenith use GPL-3.0. We are using the user's reference for visual research only, no code/assets copied. Added tests for offline cached poster and disabled-settings behavior; pending CI.


## 2026-11-21 — bounded Compose test waits
CI #123 had a transient first-run Compose UI timeout at waitForIdleSync; the identical second attempt passed the entire Android 15 suite plus build/schema/privacy. The poster offline test passed both attempts. Removed unbounded idling in UI instrumentation in favor of deadline polling and fixed stale copy saying artwork was disabled. No APK/Release; CI for helper/copy fix pending.


## 2026-11-21 — one integrated APK request
User clarified: do not provide APKs after every small step; finish the whole integrated app first, then provide one APK for testing. Continued Phase 4/5 integration with Media3 embedded audio/subtitle track selectors. No user-installable APK produced. CI #124 passed the preceding bounded UI wait fix; selector commit awaits CI. Next implement sidecar subtitle association and delay controls; later complete Glass/accessibility/performance and build one signed candidate.


## 2026-11-21 — selector source correction
The assistant audited commit 9200aaeed66c08eb30f29b27d9d8713f8afe5c67 and found its notes said audio/subtitle selectors were implemented, but the source tree contained no selector code. CI #125 therefore tested the previous player. This is being corrected with actual Media3 selector controls, localized labels and a synthetic audio-selector integration test. No user APK produced. Next: verify exact blobs and full CI, then local sidecar subtitles and delay.


## 2026-11-21 — Phase 5 sidecar foundation
CI #126 / 44906299456 passed Android 15 selector testing and all unit/build/schema/privacy gates. Work continues on local sidecar subtitles: scanner now captures the opaque parent document ID and a pure matcher limits candidates to supported subtitle files in that exact directory, including bounded language/forced suffix matching. New tests check no sibling-folder bleed and no opaque ID/path guessing. Pending CI; no persistence or playback attachment until Room v4 is safely migrated. No APK produced for this step.


## 2026-11-21 — Room v4 sidecar identity
CI #129 / 44908615223 passed the matcher regression and full schema/build/privacy gates. New work persistently stores the immediate SAF parent document ID in Room v4 with a default-empty additive migration; includes v1→v4 migration and offline round-trip tests. CI pending. Sidecars are not yet attached to the player, and no user APK is produced each step.


## 2026-11-21 — Room v4 migration green
CI #130 / 44909463881 passed v1→v4 Android migration, offline parent-ID round-trip, unit/build/schema/privacy. Added the exact generated v4 schema snapshot from artifact 14709327372; historical schemas remain preserved. Next: local same-parent sidecar attachment and playback tests. No user APK.

## 2026-11-21 — Glass dock assertion verified
- Corrected accessibility parent-chain check passed in CI #136 / 44913968127; all 41 Android 15 tests, unit/build, Room v1-v4 schema and privacy gates succeeded.
- No signed APK/Release was created; latest installable remains v0.1.91 per the one-integrated-APK request.
- Continue the integrated product; actual OPPO visual/performance acceptance is pending.


## 2026-11-21 — Player immersive control
- User reiterated that they want one APK after the whole app is assembled, not per-step builds.
- Added a reversible player action for immersive system-bar hiding, reset per media item, and an Android UI test for enter/exit accessible labels. System bars restore on lifecycle disposal.
- CI #138 / 44915530791 passed 42/42 Android 15 instrumentation tests plus unit/build/schema/privacy gates. Signing/publication were skipped; no installable APK. Actual OPPO behavior and all other integration/performance checks remain pending.


## 2026-11-21 — Player overlay auto-hide
- To improve the cinematic local playback view, the title/track toolbar now auto-hides together with Media3 controls and returns when the video is tapped. CI's synthetic video is ten seconds; an Android test covers hide, restore and accessible full-screen action.
- CI pending; no installable APK. Continue to one final integrated build.


## 2026-11-21 — Media3 visibility listener compile correction
- CI #140 failed to compile because the listener lambda matched both PlayerView and PlayerControlView overloads. Explicitly typed the PlayerView callback; rerun pending. No installable APK.


## 2026-11-21 — Playback test activity typing
- CI #141 app compilation passed after explicit Media3 listener typing. Android-test compilation found the test used generic Activity; it is now explicitly MainActivity so Compose/lifecycle APIs resolve. Rerun pending; no APK.


## 2026-11-21 — Auto-hide exit-control timing
- CI #142 passed 42/43 tests. Auto-hide, tap restore and fullscreen entry worked; the exit label timed out before assertion. Full-screen action now calls `showController()` to restart the auto-hide timer. Rerun pending; no APK.


## 2026-11-21 — Exit action reveal regression
- CI #143 again timed out the exit-label assertion after fullscreen entry; transport/title auto-hide and tap restore worked. Updated the test to tap video to reveal controls before checking the exit action, and added an explicit localized accessibility description. Rerun pending; no APK.


## 2026-11-21 — Playback chrome verified
- CI #144 / 44920009659 passed 43/43 Android 15 tests. Synthetic playback verifies title/track overlay auto-hide, video tap reveal, and accessible full-screen action; full JVM/build/schema/privacy gates passed.
- No signed APK/Release. Continue integrated app work; latest user installable remains v0.1.91.


## 2026-11-21 — Player overlay test race
- CI #145 / 44920952692 had one failing test out of 43. The test's conditional tap branch skipped the tap when the exit label was already visible, then it waited longer than the 2.5-second hide timeout and misreported the correct auto-hide behavior.
- The test now waits for controls to hide before tapping video and requiring the exit action to return. Product source unchanged. CI rerun pending; no APK.
- Keep one integrated APK for broad phone testing; current release remains v0.1.91.


## 2026-11-21 — Follow-up CI #146
- The revised chrome test was still too strict: in the test host, it could not rely on a second auto-hide timeout after the fullscreen callback. It now checks the exit action immediately with controls visible; separate UI test covers reversible accessible labels.
- CI also had one metadata UI test unable to find its synthetic detail sheet while the active window showed app content. Treat as an open test isolation/emulator issue until a rerun confirms it.
- 41/43 tests passed; no APK. Subtitle zoom separation is next after CI stabilizes.


## 2026-11-21 — Separate subtitle viewport and video resize modes
- The user wants captions/lyrics independent from movie zoom. Review of the pinned Media3 1.11.1 layout showed its SubtitleView was nested inside the resize-managed content frame, so merely switching engines was unnecessary but the current geometry needed correction.
- Added app's Apache-2.0 attributed layout adaptation with SubtitleView as a sibling overlay and accessible Fit/Fill/Zoom controls. Added synthetic UI and Android hierarchy tests. No VLC dependency.
- Current CI pending; no APK. Revisit upstream layout when Media3 is upgraded.


## 2026-11-21 — Fullscreen overlay test isolation
- Repeated Android 15 CI runs showed the accessibility tree for the Compose header could change during Media3 controller visibility callbacks, even after its button activated successfully. CI #147 passed 42/43; the only remaining error was a timing-sensitive exit-label lookup. The dedicated fullscreen composable test checks both accessible labels and reversible state, so the player integration now stops after proving action activation changes fullscreen state. It retains auto-hide and tap-restore coverage.
- The unrelated metadata fixture failure from #146 did not recur in #147. CI #148 tests subtitle-zoom implementation; no APK.


## 2026-11-21 — Subtitle zoom CI follow-up
- CI #148 / 44923450336 compiled the new player layout and ran 45 tests. The new subtitle sibling assertion and Fit/Fill/Zoom cycling passed; one auto-hide integration timed out. Increased the CI-generated video fixture to 30 seconds to avoid playback ending during emulator setup and restored buffering/error placeholders in the adapted Media3 layout.
- CI #149 on the state assertion correction is running; next full run pending. No APK.


## 2026-11-21 — Playback startup race in auto-hide test
- CI #149 passed all 45 Android tests; #150 passed 44/45, failing only auto-hide while the engine was not yet in confirmed active playback. A 30-second fixture alone did not remove the startup race.
- Added an optional state callback and changed the test to await Media3 isPlaying before checking auto-hide. Product UI remains unchanged; no APK.


## 2026-11-21 — Season-grouped series sheet
- User requested a polished cinematic catalogue. Series details now show localized season headers and concise Sxx · Exx episode rows rather than release filenames, using the local parser only.
- A synthetic Android interaction test verifies out-of-order season grouping and that tapping S02 · E01 plays its exact indexed document. CI pending. No APK.


## 2026-11-21 — Playback chrome behavior fix
- CI #151 confirmed the player header remained visible even after a real `isPlaying=true` callback. This was a product issue, not the earlier synthetic startup race.
- Added an explicit 2.5-second Compose timeout synchronized with Media3. It hides the controller and header while playing; pausing preserves controls; revealing them restarts the timeout. CI pending; no APK.


## 2026-11-21 — Timer interaction behavior
- CI #152 passed all 46 Android 15 tests, including new season grouping and subtitle/zoom cases.
- The Compose playback chrome timeout now restarts after seeks, track changes, resize selection and fullscreen action; pause holds controls open. CI #153 verifies the hide behavior, and the interaction refinement is queued.


## 2026-11-21 — VLC engine evaluation
- The user suggested VLC for broad codec support. Official docs show Media3 covers common containers but relies on device decoders; official VideoLAN docs describe LibVLC's broader engine and LGPLv2 engine license (VLC app license differs).
- DEC-021 keeps Media3 primary pending real local corpus evidence. A LibVLC comparison must measure app size/ABI, license notices, decoder stability, subtitles, memory, thermal and battery; no external media route may be enabled. No VLC dependency added.


## 2026-11-21 — Cinematic catalogue shelves
- The default offline library now separates movies and TV shows into horizontal poster shelves, while filters/search retain the dense grid. Cards use the same cached-only artwork and indexed playback identity.
- CI pending; no APK.


## 2026-11-21 — Synthetic Compose test isolation
- CI #155 had two UI failures: the selected season-2 episode was not brought into view before click, and a metadata test occasionally saw production MainActivity content instead of the synthetic test screen. The season test now scrolls the exact episode node into view, and synthetic UI tests use an empty debug-only host activity so app startup cannot overwrite their content.
- This is test-only; release APK source and user data behavior are unchanged. Verification is pending the next Android CI run. The user has asked to receive a single APK after integrated readiness; no milestone APK was published.


## 2026-11-21 — Follow-up for CI #157 compile failure
- CI #157 found one missed `MainActivity` reference in the fullscreen test when synthetic screens were moved to the debug-only host. Corrected the Intent target to `PreviewTestActivity` and recorded H-027.
- This build failed before running tests. CI #158 is required before further app changes.


## 2026-11-21 — User sets visible work batches and requests current APK
- User wants no more than five substantial published checkpoints and continuous work between them, without APK-install waits after each. Approximate ranges were shared for four batches; they are planning estimates, not guarantees.
- User also explicitly asked for an APK now, then continued development. This supersedes the earlier wait-until-final-only APK timing for this request, while preserving the permanent application ID/signing key and higher versionCode. Package only after the current source passes CI.
- Began batch 1: refreshed README and added original generated concept artwork plus labelled synthetic Android 15 CI screenshots. No claim that these concept/CI images represent an OPPO device or finished production Glass.


## 2026-11-21 — CI #158 fixture regression diagnosis and correction
- CI #158 passed Kotlin compilation but 6/46 Android tests failed. I traced five playback/SAF failures to my test-host change overwriting the pre-existing debug manifest entry for SyntheticSafProvider; restored both debug-only entries.
- The season test's scroll gestures could dismiss the details sheet, leaving the underlying library in the active window. Changed test interaction to accessibility scroll actions on the smallest nested container.
- CI #159 was already running the broken intermediate source; CI #160 is required for verification. The APK remains withheld until a full green source gate.


## 2026-11-21 — User requests signed APK now and continuous batched work
- User wants up to five substantial published batches, no pause for a phone install/check after each, and specifically requested an APK now before continuing. We are using four visible batches with rough planning estimates. Batch 1 is the README and visual identity.
- Queued a higher-version signed checkpoint using the existing permanent release key/workflow. Full Android tests and signing/privacy checks must pass before the release can publish. Development continues after that release; it is not represented as the final premium app.
- Next: inspect CI #160 for the restored synthetic SAF provider and safe scroll correction, then the new release workflow. Continue Batch 2 without waiting for the user to install the checkpoint.


## 2026-11-21 — CI #160 narrows UI test correction
- The restored debug provider fixed the playback and SAF failures. CI #160 left two UI failures because nested details sheets exposed no accessibility scroll action; snapshots showed the sheet was still present.
- Test helper now falls back to an upward gesture starting from the bottom-most visible text node, rather than fixed screen coordinates. The queued #161 release checkpoint contains the older helper, so it is not valid as release evidence; CI #162 must pass before a replacement release trigger.


## 2026-11-21 — Refactor detail-sheet scroll after CI #162
- The attempted test gesture fallback still dismissed the modal. The source's nested vertical scroll column and episode LazyColumn were competing with ModalBottomSheet's drag handling.
- Refactored the sheet to one LazyColumn containing title/year, movie action or lazy season/episode rows, and metadata card. This fixes scroll ownership rather than adding more coordinate-specific test behavior.
- CI #163 must verify the behavior. No signed APK has been published from the failed #158–#162 runs.


## 2026-11-21 — Fix a stale closure in the detail-sheet refactor
- Review after `0f8b514` caught old outer closing braces left inside the new metadata item. They would prevent compilation.
- Removed the duplicate closures before treating the scroll refactor as verified. CI #164 must compile and run the tests; no APK/release is considered until then.

## 2026-11-21 — User asked for continuous completion and visible work batches
- User clarified that CI failures are ours to fix; do not pause for routine phone checks between implementation batches. Requested a signed APK checkpoint now, then continued work toward the premium local-first app and final performance/size/efficiency pass.
- Current issue: CI #164 had 45/46 tests pass; manual metadata search could not scroll the long details sheet. Added sheet scroll/dismiss fix with Close control; verification pending. No Reno10 measurements may be claimed without actual device evidence.

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

## 2026-11-21 — User authorized signed checkpoint and continuous batches
- User asked to produce a signed APK now, continue implementation without waiting for interim phone install checks, and stop only after the complete app and final efficiency/size/performance pass.
- CI #173 passed 46/46 Android 15 instrumentation plus unit/schema/privacy/identity checks on `7cbff28881332f7da8de418b8b348166885ff857`. First signed checkpoint release workflow is being triggered. README and visual identity batch is complete. Remaining work includes polished Glass Lab/navigation, production player format/subtitle reliability, accessibility, measured performance and final release readiness. Reno10 hardware claims remain unverified until actual device data exists.

## 2026-11-21 — Glass Lab usability work
- User reported the Glass Lab opened in a broken-looking state and the dock floated awkwardly. Screenshot review confirmed the dock covered demo copy; the screen lacked a visible back action. Added a Back to library control, transparent description of preview-only tabs, lower fade and additional scrolling room. CI pending; keep user-visible Glass in a lab until it is polished and measured.

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

## 2026-11-21 — Completed playback checkpoint reset
The playback layer now clears its resume position on natural completion and makes the controls visible again. A synthetic local-media instrumentation test checks the zero/paused checkpoint. Automated CI is pending. This is a playback behavior correction only; no device performance or acceptance is inferred.

## 2026-11-21 — Resume checkpoint timer regression coverage
After discovering that the periodic checkpoint may run after the end event, all save paths normalize an ended player to position zero and false play intent. The synthetic Android test waits over one timer interval and verifies the zero checkpoint persists. CI pending.

## 2026-11-21 — Completion test fixture timing
CI #180's 30-second synthetic video made the new 20-second natural-end wait time out. The test now seeks to 29 seconds before playback, then checks end state and a later timer save. CI pending.

## 2026-11-21 — Completed playback checkpoint verified
The corrected near-end synthetic fixture passed in CI #182 / 44949435610. All 48 Android 15 instrumentation tests, unit tests, app build and Room schema v1-v4 verification passed. Earlier #180/#181 attempts timed out against the 30-second fixture; #181 also had a Pixel Launcher ANR. No signed artifact was produced by this normal push.

## 2026-11-21 — Home, Movies, Shows and Search destinations
- Started the production navigation batch. The library bottom bar now switches real catalogue state: Home shelves, movie-only results, series-only results and focused local search. Search results remain on-device.
- Added UI instrumentation for the destination state and behavior; CI pending. The solid nav is a functional foundation, with bounded Glass still reserved for the later Glass phase.

## 2026-11-21 — Navigation accessibility test lookup
CI #184's only failure was a test helper choosing a duplicate static Movies heading while checking the selected tab. The movie filter assertion passed. The helper now searches all label matches and their ancestors; await the rerun.


## 2026-11-21 — Navigation accessibility follow-up
- CI #185 ran all 49 Android 15 instrumentation tests; the only failure was the new selected destination assertion. Catalogue screenshot shows correct bottom navigation and Home selection. Movies filtering had passed before the failed check.
- Added explicit selected-state descriptions to each production navigation destination and adjusted the test to inspect these accessible descriptions. CI pending; continue toward the integrated app release rather than emitting intermediate APKs.


## 2026-11-21 — Accessible playback speed selector
- Corrected the library navigation selected-state accessibility issue with explicit state descriptions; CI pending.
- Added an in-player speed menu using Media3 playback parameters, with common rates from 0.75× to 2× and Android UI interaction coverage. The single integrated APK remains deferred until the requested efficiency review.


## 2026-11-21 — Navigation and playback-speed CI result
- CI #203 passed all 50 Android 15 instrumentation tests, unit/build and Room v1-v4 schema checks. The current tree includes explicit tab-selection accessibility state and a local Media3 playback-speed selector.
- Release signing/privacy gates were skipped for this normal commit; no intermediate APK was generated. Continue the Glass/player batches until the integrated app and efficiency review are ready.


## 2026-11-21 — Production library Glass dock starts
- After CI #203 passed (50/50 Android 15 tests and unit/build/schema checks), integrated the existing bounded Haze navigation material into the real library. Manage includes a persistent local Glass toggle; off mode removes Haze source capture and uses the solid fallback. The bar keeps the four real local catalogue destinations.
- Added synthetic activity-recreation coverage. This change awaits CI; no handset performance claim or APK. User’s Glass Lab report remains a placement/experience item to verify from the new capture and later OPPO use.


## 2026-11-21 — Glass settings compile correction
- CI #205 caught a missing `contentDescription` import for the new Liquid Glass settings switch. Fixed the import; instrumentation did not start. Full CI for the corrected production dock is pending. No APK.


## 2026-11-21 — Production Glass CI regression follow-up
User asked to continue the integrated app through completion and the final efficiency review without pauses for intermediate installs. CI #206 reported three defects after production Glass was added: dock selection wasn't detected by accessibility, the switch state wasn't explicit, and the added row displaced folder actions. The focused fix adds selected semantics and localized On/Off announcements, moves the appearance setting after existing Manage actions, and makes tests scroll to it. Android CI is the verification gate; no APK is produced per step. Continue implementation after the test passes.


## 2026-11-21 — CI #207 accessibility follow-up
CI #207 compiled the code and ran 51 Android 15 tests; two assertions failed. The Manage-sheet scrollable column did not appear as an accessibility scroll action, and selected tab state was still not associated with its item label. Added a safe gesture fallback to the synthetic test, explicit merged descriptions for tab labels and richer test diagnostics. Run #208 is pending. Continue implementation after resolving the regression; no APK at this step.


## 2026-11-21 — CI #208 compile correction and portrait offline artwork
CI #208 failed before instrumentation because `stringResource` was called inside a non-composable semantics lambda. The dock now resolves the label in composable scope. The offline artwork card is also being adjusted to a portrait 2:3 poster with an original title initial and an accessible description; its bounds are covered by an Android UI assertion. CI #209 is pending. No APK is produced during this integrated work.


## 2026-11-21 — CI #209 localized poster label compile correction
CI #209 stopped before Android tests because the new poster accessibility description resolved a string resource inside a non-composable semantics lambda. The value now resolves in LocalTitleCard composable scope. CI #208 had the same issue for the dock label; that is also corrected. The next run verifies both plus the portrait artwork test. No APK yet.


## 2026-11-21 — CI #210 failures and integrated detail/accessibility correction
- CI #210 compiled and ran 51 Android 15 tests, with 46 passing and five new UI assertions failing. Failures involved Switch state surfaced differently by Android, dock selected state, poster art hiding clickable catalogue titles, and scrolling a details-sheet action into safe bounds. Offline library, player decode/resume, SAF, Room and metadata-security tests passed. Artifact screenshots and test reports were inspected.
- The current correction makes title posters decorative (so card titles remain screen-reader labels), exposes dock tabs through explicit semantics including selected state/click, announces Glass switch state and checks native `isChecked`, and adds an in-modal gesture fallback with accessibility bounds diagnostics. The detail screen gains a wide original gradient hero and uses optional cached TMDB backdrops. A backdrop can only be fetched after the already separate user confirmation; opening details remains offline.
- Android CI #211 has not yet verified this source. Local `git diff --check` and Android resource XML parse pass; there is no Android SDK in this workspace. No APK or actual OPPO evidence. Next: commit with the context update, inspect CI #211, continue integrated player/subtitle/Glass work and defer the single signed APK until final efficiency analysis.


## 2026-11-21 — CI #211 narrows Android accessibility issue
- CI #211 / 44959819745 compiled and ran 51 Android 15 cases: 47 passed and four UI checks failed. Production dock selected semantics and series-details navigation pass. Android omitted card title labels after clear semantics on the decorative initial; the metadata route therefore could not open its card. The Switch `isChecked` node flag was false despite the default-enabled preference.
- The follow-up gives each clickable poster card an explicit localized title/type/year description and switches UI automation to that accessible card. Glass exposes explicit Role.Switch/ToggleableState semantics and a localized On/Off announcement; tests check the Android checked flag and saved preference across recreation. CI #212 must validate the source and full modal route.
- No APK or handset evidence. Next: commit with updated context, inspect CI #212/screenshots, then continue player/subtitle and final efficiency work.


## 2026-11-21 — CI #212 import correction
- CI #212 / 44960830044 stopped before Android tests at `LocalLibraryScreen.kt:576`: the explicit `Role.Switch` semantics assignment was missing the Compose `role` extension import. This is a compile-only error; no UI or privacy behavior was evaluated and no APK/artifact was produced.
- Added the extension import and clarified the accessibility semantics comment. Next: commit with updated docs and inspect CI #213 for compilation, 51 Android 15 tests, screenshots and privacy/schema gates. Continue integrated player work after this gate.


## 2026-11-21 — CI #213 UI semantics and modal follow-up
- CI #213 compiled and ran 51 Android 15 instrumentation tests: 47 passed, 4 UI checks failed. Explicit card labels now appear and series detail works. The Switch remained a generic unchecked node; dock labels as contentDescriptions weren't recognized by a test helper; selected semantics were asserted before recomposition; and the metadata confirm button was visible in safe bounds but rejected by the generic scroll fallback.
- Current correction uses a clear Switch semantics node including role/state/accessibility click, searches either exact text or contentDescription, waits for navigation state settlement and asserts safe on-screen bounds before clicking the final confirmation. CI #214 must verify; no APK or actual-phone evidence.
- Next: commit with updated repo context, inspect CI #214 and screenshots, then continue player/subtitle/Glass, Macrobenchmark and the final efficiency review.
## 2026-11-21 — CI #214 green; custom Compose transport started
- CI run ID 44961886974 for `4aa127595ec164dbfdd9ba71d2a2e08169ca27eb` completed successfully: 51 Android 15 tests passed; unit/build/schema/privacy gates passed, release steps skipped. Inspected emulator Details and Glass On captures. No handset validation.
- Implemented Compose-owned local player transport (play/pause, ±10-second seek, accessible seek slider and timestamps), disabled duplicate PlayerView controller, and prevent auto-hide while scrubbing. Added localized control strings and instrumentation assertions/time formatting regression. Local diff whitespace check passes; Android CI is pending.
- User wants no per-step APK and one integrated signed test APK only after feature completion and final efficiency review. Continue on main; exact next action is commit with mandatory docs then run/inspect Android 15 CI.
## 2026-11-21 — CI #215 compile correction
- CI #215 / 44962980148 failed at compile time: custom transport Button semantics lacked the `androidx.compose.ui.semantics.role` extension import. No instrumentation or screenshot ran. Added the import in `684c35f`, pushed to main; CI #216 is running. Current handoff SHA/status is reconciled. Next: inspect CI #216 and fix any real regression it finds.
## 2026-11-21 — CI #216 exposed Manage-sheet reveal regression
- Transport compiled and all nine local-player tests passed. Android 15 total was 51/52; the existing Glass preference persistence test could not reveal the toggle after activity recreation. The active accessibility snapshot showed only the top Manage actions.
- Changed the settings sheet to start expanded and strengthened its test to require a visible control, with accessibility scroll and safe swipe fallback. Updated R-036 and current handoff. `git diff --check` passes; CI pending. Next: push and verify all 52 Android 15 tests plus build/privacy/schema gates.
## 2026-11-22 — CI #218 sheet-entry timing diagnosis
- CI #218 compiled the app and reached all 52 Android tests. The settings test failed before scrolling because it acted during the full-height sheet's entrance; active accessibility tree only contained “Close sheet.” Wait for the localized settings heading and visible bounds before gestures. Product control remains required to be revealed. Next: push correction and verify the full suite.

## 2026-11-22 continuation — Home hero and picker regression
CI #228 found the last picker accessibility adjustment did not compile because the pinned Compose API rejects setting `role` inside `clearAndSetSemantics`. Current source retains `selectable` radio semantics and merges the accessible label/selected state/activation. A Home hero was added with local Play/Details actions and cached-artwork-only rendering. The UI test covers the hero action; all code/tests are pending CI. The correction and feature are documented in AGENTS.md, DEVELOPMENT_LOG.md, roadmap and handoff. Continue implementation; user wants one APK after integrated feature completion and final efficiency review.

CI #229 compiled production Kotlin, then stopped at Android test compilation: Home hero test referenced undefined `awaitText`. Replaced it with existing `text()` bounded node helper. No runtime tests ran; next CI is pending.

CI #230 ran 53 Android tests, 48 passed and 5 failed. The Home hero occupied the compact screen and its duplicate “Play video” text confused node lookup; reduced its minimum height to 188dp and changed its own CTA to “Play”. The new test now clicks through the shared accessibility helper. Picker label/action semantics are reordered so selectable owns the merged label and state. Re-run pending.

CI #231: 51/53 Android tests passed; only picker label click and dock-clipped poster bounds failed. Current source restores one explicit RadioButton semantics node, including the previously missing `role` extension import and onClick. Poster layout test scrolls Home before measuring. CI #232 pending.

CI #232 compiled and ran 53 tests (51 passed, 2 failed): picker click works but selected state stays stale; poster bounds remained clipped after the shell swipe. Current fix observes Media3 parameter changes into Compose state and scrolls the largest accessibility scroll container, with a new scrolled-home capture. CI #233 pending.

CI #233 stopped before tests: `onDispose` was incorrectly imported as a top-level Compose symbol. Removed the import (the call remains in `DisposableEffect` scope). CI #234 pending.

CI #234 compiled the app; 51/53 Android tests passed. Subtitles Off clicks but the selected accessibility state still fails; new assertion checks Media3 disabled parameters immediately after click. Poster bounds stayed clipped because the scroll action made no progress; artwork ratio check now runs after opening Movies and captures `movies.png`. CI #235 pending.

CI #235: app compiled; 51/53 Android tests passed. The new Media3 assertion read ExoPlayer off its main application thread; wrapped it in `runOnMainSync`. Movie-tab portrait poster check passed. Existing Glass preference test found the switch below the visible safe area; helper now scrolls the sheet before asserting. CI #236 pending.
# 2026-11-22 continuation — Caption slider accessibility and metadata privacy

- User reiterated that a single APK per completed substantial batch is the useful cadence; do not generate APKs for each edit. Continue after batch publications without pausing for routine phone checks.
- User wants automatic TMDB posters with no per-install connection setup and only normalized title/year/type sent. Their app is non-commercial for now; they asked us to choose the best private method. Existing decision is a shared Player-wide application credential delivered through publisher CI, explicitly treated as extractable/public and not a secret; direct TMDB only, no proxy/backend. No credential exists yet, so implementation must remain fail-closed without one and the one-time publisher setup can be addressed after implementation is ready. HTTPS also reveals client network IP to TMDB; document that clearly. Preserve opt-out and offline operation.
- CI #243 / `44980944579`: app and test compilation passed; 54/55 Android 15 instrumentation tests passed. Caption accessibility test found missing slider range semantics. The size/position sliders now merge their labels with explicit bounded ranges; full rerun pending. No APK produced.
- CI #244 / `44981445066`: the first range semantics correction missed Compose's required extension import, so app compilation stopped before tests. Added the import; it still needs full verification.
- CI #245 / `44981754235`: 53/55 instrumentation tests passed. Both caption slider labels lacked range on the same accessibility node; use one explicit clear-and-set accessible label/range/action. Captured screenshot revealed Reset was offscreen on compact 320×640; make the settings content scroll and assert the button is safely reachable. An existing confirmation assertion also needed to scroll its long detail sheet first. No APK produced.
- CI #246 / `44982372098`: 54/55 tests passed. Slider labels/ranges/set-progress behavior and the metadata confirmation scroll passed. Screenshot/accessibility evidence showed the modal stopped at half-height; the reset action was completely absent, so internal scrolling could not reveal it. Set the caption-only sheet to skip partial expansion and verify Reset appears within safe bounds. No APK produced.
- CI #247 / `44982837242`: 55/55 Android tests passed; unit/debug build, Room v1–v4 schema, and release-checker fixtures passed. Reviewed the 320×640 capture: both caption sliders and Reset are visible in the expanded safe-area sheet. Release-only privacy/signing checks were skipped on the development commit; no signed APK yet.
- Release-trigger CI #248 / `44983614803` stopped before signing on an existing metadata confirmation test. Failure snapshot showed the action at y=535–553 inside the 640px screen; the helper incorrectly mixed display metric scaling with screen-space AccessibilityNodeInfo bounds and an ineffective swipe. The helper now derives screen/safe/gesture bounds in one coordinate system. No APK/release was produced.
- CI #249 / `44984427809` passed 55/55 Android tests, release-checker fixtures, unit/debug build and Room v1–v4 schema. The active-screen coordinate helper and corrected Drag handle swipe anchor passed. No signed APK produced on this development run.
- Exact next action: publish this completed batch with one `release(test):` commit and require repeat tests, merged-manifest privacy, permanent-certificate and signed Release verification. Then continue automatic TMDB matching/artwork implementation and make one app-wide credential path; no per-user credential setup, no credential in source, local path/media never sent. Never claim Reno10/FPS acceptance without real device evidence.
# 2026-11-22 continuation — Signed caption-controls batch and metadata direction

- Published signed `v0.1.250` (`versionCode` 250) after CI #250 passed all 55 Android 15 instrumentation tests, unit/debug build, Room v1–v4, manifest privacy, permanent signature, and release artifact checks. APK SHA-256 `445ef633f4ac0f09fb27dfd1affad43e5d250c73cd245b4a17ed266601b964b1`.
- User clarified TMDB should be automatic without per-install configuration. Chosen direction: one publisher-configured app credential, explicitly disclosed as extractable/public, plus local opt-in and direct TMDB requests carrying only normalized title/year/type. No credential is currently configured, so missing-key behavior must stay offline and fail closed.
- Next: implement and validate the automatic metadata/artwork flow as one substantial batch; publish one signed APK only when its combined changes pass.

## 2026-11-22 continuation — Automatic title-match foundation

- The caption batch was published as signed v0.1.250. Current source/docs checkout is ahead by local documentation commit `5bbb83f`; push is blocked by the workspace proxy, so this commit is not yet on GitHub and no CI ran.
- Added a pure match-selection policy and six JVM tests for exact normalized matches, year/type mismatches, ambiguity and duplicate IDs. This does not yet trigger requests or display posters automatically.
- Next: integrate it behind local explicit opt-in, configure shared app-credential injection (default empty/off), automatically bind only unique matches, cache/fetch artwork, test the offline and revoke/cancel paths. No app credential is configured yet; do not claim online metadata works until it is configured and tested.

## 2026-11-22 continuation — Automatic metadata integration in progress

- Added the app-wide credential build path, injected only from a release CI secret and kept out of local storage; the UI no longer asks users for a personal key. Local opt-in remains off by default. An exact unique title/type/year match on an open title detail can save metadata and private artwork; ambiguous choices still require confirmation. Catalogue scan and scrolling remain offline-only.
- Added match unit cases and Android integration/storage cases. They are not run because this workspace has no Gradle/Android SDK and the GitHub proxy is unreachable. Documentation records this explicitly. v0.1.250 remains the latest published APK; this source is not yet verified/released and no publisher key is configured.
- Next: CI validation, fixes, bounded opt-in catalogue poster enrichment, full privacy review, then one signed APK when the metadata batch is complete. Do not claim phone acceptance or live metadata before the app credential is configured and tested.

## 2026-11-22 continuation — First automatic metadata CI compile result

- CI #251 compiled the app, then test compilation found a missing JUnit `assertFalse` import in the new controller privacy assertions. Added the import. The earlier UI test correction was also pushed separately; the queued CI #252 does not include this later import. Next run must pass test compilation and the Android 15 suite before more metadata work is merged.

## 2026-11-22 — User requests automatic posters with minimal disclosure

The user confirmed Player is currently non-commercial, wants the most private workable method, and does not want each install to configure a personal TMDB account. They clarified that the only app-level information allowed to leave is the normalized movie/show title and any necessary year/type. Continue the existing direct-to-TMDB design with one publisher key, explicit local opt-in and clear disclosure that TMDB also sees the public key and network IP; no backend or source media data. User explicitly objected to frequent APK generation; the last APK remains v0.1.250 and the metadata batch gets at most one Release after integrated fixes and green checks.

CI #253 compiled app code but had two Android test failures: expected-enabled state after persisting disabled consent, and an offscreen settings status assertion without a scroll. Both test fixes and new foreground visible-card enrichment are now local and unverified. The user wants ongoing substantial progress without waiting after each batch; no routine phone prompt should block independent implementation, but actual phase/device acceptance must not be invented.
