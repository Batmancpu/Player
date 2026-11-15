# Player — chronological chat and session history

This file is the durable, accumulating record of important user requests, product decisions, implementation history and unresolved actions. It is written for future AI agents to continue work without depending on a lost chat context.

**Format:** decision-focused session summaries, not a verbatim export of every message. Preserve the meaning of user requests and reasons behind decisions. Never include private signing files, passwords, base64 secret values, API keys, tokens or other credentials.

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
