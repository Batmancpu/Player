## Current handoff — 2026-11-21 (latest)
- README/artwork is committed. The user wants a signed APK now, then continued work in four visible batches without installation waits.
- Details screen changed from nested vertical scrolling to one LazyColumn (`0f8b514`); its first edit carried stale outer closing braces. Corrected immediately in this commit. CI #163 may test the intermediate tree; CI #164 is required for the corrected source.
- Earlier provider regression is fixed; current automated failures were season-episode and metadata sheet scrolling. Release attempt #161 skipped signing after failed tests. Do not re-trigger release until the corrected detail list passes CI.
- Latest signed release v0.1.91; no OPPO acceptance or performance evidence. Preserve package identity/certificate, disabled backup, single read-only SAF tree and no source media writes.
-## Current handoff — 2026-11-21 (latest)
- README and original concept/synthetic CI artwork are committed on main.
- CI #156 passed 46/46 Android tests. #158/#159 caught a missing debug-only SyntheticSafProvider; restored. #160/#161 then caught 2 UI scroll failures; #162 reproduced them because the modal consumed vertical drags from nested scrollable content.
- Main now replaces the nested details Column/LazyColumn with one LazyColumn for title/year, movie/show rows, lazy episode groups and metadata card. CI #163 is the next required verification.
- User requested a signed checkpoint now and continuous progress in four visible batches without install pauses. The first release workflow #161 was correctly skipped after failing tests. After #163 passes, create a fresh permanent-key release run; continue player work without waiting for phone installation.
- Latest existing signed release remains v0.1.91. No Reno10 performance/appearance or final acceptance evidence. Preserve app ID/certificate, backup disabled, one read-only SAF tree and no source-media writes.
-## Current handoff — 2026-11-21 (latest)
- README/artwork checkpoint is committed; the exact source is current main. CI #156 passed 46/46 Android 15 tests. CI #158/#159 were broken by a dropped debug-only SyntheticSafProvider; restored.
- CI #160 / https://github.com/Batmancpu/Player/actions/runs/44936109342 compiled and passed 44/46 tests. Only season-detail and metadata-sheet scroll interactions failed because nested content had no accessibility scroll actions. Current main changes the helper to swipe upward from visible sheet content. CI #161 is queued for an older release-trigger commit; it must not count as release evidence if the suite fails. Current correction CI #162 is required.
- User requested a signed APK now, then continued work, and four substantial batches (<=5) without phone-install pauses. Next: inspect #161, verify #162, then queue a fresh signed release checkpoint after green; continue the larger player/navigation batch without waiting for installation.
- Existing signed release: v0.1.91. Do not publish from a failing CI source. No Reno10 performance or final acceptance data. Preserve app identity/certificate, backup disabled, one read-only SAF root and no media writes.
-## Current handoff — 2026-11-21 (latest)
- README visual checkpoint is on main with original concept artwork and labelled synthetic Android 15 CI captures.
- CI #158/#159 reproduced the missing debug-only SyntheticSafProvider and a modal swipe-dismiss test failure. Main now restores provider and uses accessibility scroll actions in CI #160. Inspect #160 result before any signed artifact is considered.
- User asked for an APK now and continuous work in four substantial batches (maximum five), no phone-install pause after each. A `release(test):` commit is queued as CI #161; its workflow must pass Android15 tests, release privacy/identity checks and permanent signing-certificate checks before it can publish a signed update. If successful, add its actual version/hash/URL to this handoff and README, and continue Batch 2 without waiting for phone installation.
- Expected workflow #161 uses run number for versionName/versionCode, but only write the actual resulting release version/hash after the run succeeds. Do not install or distribute CI-only debug APK.
- Latest existing release at time of this entry remains v0.1.91. No OPPO performance, appearance or final-phase acceptance evidence. Preserve application ID, signing certificate, disabled backup, one read-only SAF tree, no source-media writes.
-## Current handoff — 2026-11-21 (latest)
- Main contains README/assets checkpoint `aace44a0e863b8ac10c775f336c81e633087b597`; visual art is original generated concept art plus synthetic Android 15 CI captures, not Reno10 evidence.
- CI #156 passed 46/46 Android 15 tests; #157 caught a stale MainActivity reference, fixed; #158 then exposed a missing debug synthetic SAF provider declaration plus a swipe-dismiss issue in the series-detail test. Both are corrected in the current source. CI #159 at https://github.com/Batmancpu/Player/actions/runs/44935428332 is still running the intermediate source. CI #160 should validate the current correction.
- User has asked for a signed APK now, followed by continued development, and for up to four substantial published progress batches without manual pauses between them. Do not release from a failed test source. Next exact action: inspect #159 for its expected intermediate failures, verify #160, then publish the requested higher-version permanent-key APK through the existing release workflow if all privacy/signing/build gates pass.
- Latest release is v0.1.91. Preserve app ID, permanent certificate, increasing versionCode, backup disabled, one read-only SAF tree and no original-media writes. No OPPO UI/performance evidence is available; Glass remains lab-only.
-## Current handoff — 2026-11-21 (latest)
- README/visual batch added: original concept banner plus labelled synthetic catalogue and Glass Lab captures, and a corrected overview of actual implementation, privacy and limits. The imagery is not handset evidence.
- Latest source before README batch: `9eadae4057a4ed85c82cd00812ee58a0a114b75d`. CI #156 passed 46/46 Android 15 tests and all verify gates. CI #157 failed Android-test compile due a stale activity reference; corrected at `9eadae4057a4ed85c82cd00812ee58a0a114b75d`. CI #158 / https://github.com/Batmancpu/Player/actions/runs/44934756101 is in progress.
- User requested 4 substantial progress batches (<=5), continuous work without per-batch installation pauses, and asked for a signed APK now before later work continues. Next exact action: inspect #158, finish README batch and CI verification, then use the existing permanent signing/release workflow for a higher-version checkpoint. Do not ship if the source gate fails.
- Latest published signed release remains v0.1.91. Preserve app ID/signing certificate, disabled backup, read-only one-tree SAF access and no source media writes. No Reno10 performance data is available; Glass remains a lab preview.
-## Current handoff — 2026-11-21 (latest)
- Latest source: `62dbc678ce444de723383423a6963995ddba3d57`. CI #156 passed 46/46 Android 15 cases, plus schema/JVM/privacy/build gates. It produced no installable APK.
- CI #157 failed before Android tests at `LocalPlaybackAndroidTest.kt:136`, where one fullscreen test retained a deleted MainActivity import/reference after switching synthetic UI tests to the debug-only host. Corrected on main in this follow-up; verification pending.
- New CI run #158 is the next exact action: verify the Android test host compiles, run all instrumentation tests, inspect screenshots/results and confirm app privacy/build gates. Do not layer further changes until it is green.
- User wants one installable APK after integrated readiness; latest release remains signed v0.1.91. No new APK/Release, OPPO test or performance measure from these commits.
-## Current handoff — 2026-11-21 (latest)
- Main base for current test-host correction: `11faca5e43f55909b5911e4201f9707d1d4e31fa`; latest released signed APK remains v0.1.91. User requested one installable APK only when the integrated app is ready; no milestone APK.
- CI #156 is running for offline movie/TV shelves at `https://github.com/Batmancpu/Player/actions/runs/44933722641`. Before the next commit, inspect this run's final outcome.
- CI #155 exposed two UI test defects: a season 2 episode row was clicked without scrolling the exact row, and a metadata test intermittently observed MainActivity content during synthetic Compose setup. The current follow-up uses a debug-only synthetic host and exact episode reveal. CI has not yet verified this correction.
- Next exact action: inspect #156, then ensure the new host compiles/merges and the test suite passes; investigate any failing screenshot/log artifact before further product work.
- No release build or OPPO measurement/acceptance was performed for the current commits. Preserve application ID, permanent release certificate, increasing versionCode, single read-only SAF tree, no backup and no original-media writes.
-## Current handoff — 2026-11-21 (latest)
- Latest verified source: 412d4fc701a4f4000ec5eb92b5707cbfbf1b608a adds an immersive system-bar toggle for local playback. CI #138 / 44915530791 passed 42/42 Android 15 instrumentation tests plus JVM/unit/build, Room v1-v4 schema and privacy gates. Current pending source update synchronizes the title/track overlay with Media3 controller visibility and adds an auto-hide/tap integration test. CI #140 found an overload ambiguity; CI #141 compiled app source after the fix but test compilation found generic Activity; CI #144 / 44920009659 passed all 43 Android 15 tests, including auto-hide, tap reveal and accessible exit action. JVM/unit/build, Room v1-v4 schema and privacy gates passed. Signing/publication skipped; no APK. The exit action carries a localized accessibility description. CI #136 / 44913968127 passed the preceding dock correction; #134 was a Pixel Launcher ANR and #135 exposed a test-query defect. No user APK or release was produced. Latest Release remains v0.1.91.
- Implementation commit e43b779467f25d08f84c8f0a971c31ef16002a22 (local sidecar playback); CI #132 / 44911456180 passed.
- Latest signed Release remains v0.1.91 (versionCode 91). User requests one complete app before the next install APK; do not publish milestone APKs.
- Latest completed phone evidence is user-reported v0.1.79: four video titles/year parsed, library retained after airplane-mode restart, and folder switching worked. The newer v0.1.91 catalogue/player/Glass preview is not accepted or performance-tested on OPPO.
- The current source attaches only fresh, exact same-parent SRT/VTT/ASS/SSA/TTML local content URIs to Media3. CI passed 41 Android 15 instrumentation tests (zero failures), JVM/build, Room schema v1-v4 and privacy gates. The synthetic SRT decode/selector path passed. Workflow artifacts were test/schema only; no user-installable APK/Release was produced.
- Next exact action: continue complete player/subtitle/navigation/production Glass and accessibility implementation. Actual OPPO acceptance/performance remains pending; final signed APK only after integrated readiness. Then evaluate stable Media3 subtitle-offset support before choosing a delay/reset approach, and finish playback controls/resume, production Glass/navigation, accessibility/fallback and battery/performance review. Actual OPPO UI/performance remains unverified. Keep final APK deferred until the integrated app is ready. Only after one integrated build is ready should CI publish a signed APK for the broad phone checklist.
- Preserve app ID, release certificate, increasing versionCode, one read-only SAF root, disabled backup and no source-media writes. Do not claim phone/performance evidence without OPPO evidence.

## Current handoff — 2026-11-21
- User wants the whole integrated experience before receiving one phone-test APK. Do not publish milestone APKs/releases.
- Latest base: e6a64b0a7526ce3f2c1e6ddd0b7cf2eb0ae72a18. CI #123 / 44902997094 attempt 2 passed Android 15 tests plus JVM/build/schema/privacy checks; attempt 1's Compose tests timed out inside waitForIdleSync, the same code passed on retry. No signed Release.
- Poster cache only fetches after manual confirmation; local Coil files render in details and lazy catalogue cards. Cache remains readable offline after disabling TMDB; invalid image routes cannot reach transport. Zenith GPL findings are documented; no source/assets reused.
- Current change makes test helper waits bounded and updates obsolete library text. CI pending. Next: complete player controls/audio/subtitle journeys, original Glass design and accessibility, then CI and final integrated APK; phone performance remains unmeasured.
- Preserve app ID, certificate, read-only one-folder SAF boundary, no backup, no source writes/upload, no telemetry/backend/broad permissions. Main only.

---

## Current handoff — 2026-11-21
- User wants the whole integrated app before a single installable APK; do not create milestone Releases/APKs.
- Latest verified main before offline edge-case test: 96a340a260875eae945c611c7df3e6573aa3af26. CI #122 / 44902258363 passed Android 15 tests, screenshots, JVM/debug, Room schema and privacy. No user APK/Release.
- TMDB remains manual and default-off. Only a separate match confirmation fetches poster; cache is bounded/no-backup. Coil decodes local cache Files only. Lazy visible cards read only those Files; no requests on browse/scroll. Current follow-up tests offline cache after setting disable and invalid URL rejection; CI pending.
- Upstream reference research: FCZenith now redirects to Batmancpu/brrMusic; its LICENSE and rezapranata089-bit/Zenith LICENSE are GPL-3.0. No code/assets from them imported; visual reference only. See UPSTREAM_AND_PRIVACY.
- Next: verify current test run, continue offline details/catalogue, playback/audio/subtitle and original Haze-based Glass/accessibility/performance tasks. Preserve stable app ID/certificate, no-backup, read-only single SAF tree. Target phone acceptance/FPS evidence still pending. No APK until integrated experience ready.

---

## Current handoff — 2026-11-21
- User wants one APK only after the whole app is complete; do not provide installable milestone builds/releases.
- Base before current slice: dd7bffe86169a9f4831343b9ae213efdc8a00ed4. CI #121 / 44901117586 attempt 2 passed Android 15 integration, JVM/build, schema and privacy checks. Attempt 1 showed Pixel Launcher ANR in five existing UI tests; attempt 2 passed. Signing/release skipped. No user APK.
- Confirmed TMDB artwork now uses an unauthenticated, bounded fixed-host CDN transport after explicit match confirmation, no-backup LRU and Coil local-file decoding. Current follow-up shows already-cached poster files in visible catalogue cards; verification pending. It does not fetch from card scroll or startup.
- Next: verify grid changes on CI, then continue complete offline catalogue/details and local player, accessibility, Glass and audio/subtitle work. Keep main only, privacy constraints unchanged. No OPPO FPS or thermal claims. Final APK/testing waits for integrated app readiness.

---

## Current handoff — 2026-11-21
- Latest user instruction: finish the whole integrated app before providing an APK; no milestone APKs.
- Base main: b344b420ae693e830c2828c2fee5ac577bb86d34. CI #118 passed Android15 tests/build/schema/privacy. No candidate release; latest public v0.1.91.
- Current change adds bounded TMDB artwork cache after separate manual match confirmation, Coil local-file rendering, synthetic test. CI pending. Next inspect Actions and fix any build/test/privacy failure.
- Preserve app ID/certificate, allowBackup=false, one read-only local SAF tree, no source-media writes/telemetry/backend/broad permissions. Continue on main. Phone acceptance/performance still require evidence; do not publish APK until complete integrated catalogue/player/Glass experience is ready.

---

## Current handoff — 2026-11-21

CI #115 / 44896086296 passed after commit 5a467f2: Android 15 instrumentation, client tests, builds, Room schema and privacy gates. Main-only work; no signed/user-installable APK or Release was produced, per user's request to wait for one integrated build. Main now has opt-in manual TMDB search, separate candidate confirmation and offline Room metadata. INTERNET permission is scoped in source to this explicit flow; no scan/scroll/startup request. Next: finish bounded artwork caching/rendering, add visual/offline/error coverage, then complete remaining roadmap and hand to the user with one signed APK and phone checklist. Phase 1 acceptance, live TMDB behavior and performance on OPPO remain pending.

CI #103 / 44888950131 passed after the exact generated Room v3 schema commit (20/20 Android 15; build/schema gates). No signing/publication occurred.

- Live main contains commit 30b6a75 (Room v3 metadata cache) and this exact-schema follow-up. CI #102 / 44888106152 passed Android 15 instrumentation 20/20 and all unit/build/schema/regression gates. The v3 schema generated by CI and committed is SHA-256 a48bab777b7dcad1f02f0e61a1a936b95f91eca61265a1ede03d7cccbc4450db.
- Latest public installable APK remains signed v0.1.91. No Release, signing or installable candidate was produced by CI #102. User asked for one complete-app APK rather than per-step phone-test builds.
- User confirmed v0.1.79 selected-folder scan/title-year parsing, airplane-mode library persistence and folder switching. v0.1.91 appearance/playback and actual OPPO behavior/performance remain unverified; Phase 1 is not accepted.
- Room v3 keeps TMDB provider metadata separate from local SAF document IDs and only exposes it through explicit user-confirmed matches. The bounded HTTP client and detail-sheet search/confirmation flow are integrated; cache rendering is present, artwork is pending.
- Preserve com.batmancpu.player, the permanent signing certificate, one read-only selected SAF tree, backup-disabled policy, no source-media writes/uploads and no telemetry. Continue on main only.
- Current focused change: confirmed “Remove folder from Player” clears Player’s index/cache, resume points and persisted read-only SAF grant; original media remain untouched. CI #104 passed. CI #105 reported three synthetic accessibility label timeouts, but CI #106 / 44891844084 passed Android 15 22/22 and all other gates; the failure did not reproduce and no root cause is claimed. The bounded timeout diagnostic remains in tests.
- Next: implement the explicit opt-in manual TMDB lookup and match confirmation against the offline cache, review attribution/privacy and test cancellation/error handling, then continue playback/Glass integration. No installable APK is published per step; prepare one signed APK after integration is ready. OPPO acceptance and device performance remain unverified.

---

## Current handoff — 2026-11-15

- Live GitHub confirms `main` is the only branch; there are no open PRs. Implementation commit `20a3bceb2fac31cf381bcb265a4424e35cf3fe8f` passed CI #99 / 43740997631. Latest signed Release remains [v0.1.91](https://github.com/Batmancpu/Player/releases/tag/v0.1.91), versionCode 91, APK SHA-256 `7c9ff8287405be9b1f607259608d84a256e5aed1fcc4cea24e3f9b882dd05e17`. Preserve application ID and permanent certificate.
- User-confirmed v0.1.79 behavior: selected-folder scan found four correctly named/year-parsed videos, library persisted after airplane-mode restart, and switching folders worked. v0.1.91 catalogue/playback/Glass preview remains untested on the OPPO; cancellation/Glass feedback from v0.1.79 remains a product follow-up. Phase 1 is not phone-accepted; no OPPO FPS/performance claim exists.
- Main contains offline catalogue/details and local Media3 playback preview, Room v3 metadata cache, encrypted opt-in TMDB settings, bounded API client and explicit manual match flow. CI #115 passed; current integration CI pending. Keep phase acceptance and real handset/performance evidence pending.
- **Verified change:** legacy Android 11-and-lower exclusions and Android 12+ cloud/device-transfer exclusions now cover all app-data domains while retaining `allowBackup=false`. The focused Python suite passed 7/7. CI #99 / run 43740997631 passed: Android 15 instrumentation 17/17 with 0 failures/errors/skips; the full Python script suite; unit/debug/release builds; both Room schema validation; compiled release manifest showing `allowBackup=false` plus both backup-rule references and no unexpected permission/debug provider; APK signature; and the pinned permanent certificate. Signed versionCode 99 artifact SHA-256 `29f3945c4c8aa5e1501e90a3f05ad5de7685d7acc543821bb3fc74e57f9296fb` is CI-only. GitHub Release publication was skipped. H-023 is resolved for these Android modes; backup behavior was not tested on OPPO.
- Android 16 QPR2/API 36.1 cross-platform transfer to iOS requires genuine bundle ID, Team ID and content version. Player has no iOS target, so no fabricated IDs/rules are added and no iOS-transfer exclusion is claimed.
- **Next action:** continue independently on the approved roadmap, starting with credential-free metadata preference/consent and destination-policy tests; keep HTTP and credential storage gated until those privacy controls are explicit and verified. The v0.1.91 OPPO acceptance checklist remains a separate user evidence gate. No new Release is justified by this config-only slice.
- Continue on `main` only, no task branches/PRs or scheduled tasks. Preserve the one-tree read-only SAF boundary, no original-media writes/uploads, backup-disabled intent and no Phase 1 INTERNET. Actual Phase 1 acceptance and the v0.1.91 OPPO checklist remain pending.
 
- **Latest testing preference (2026-11-21):** user wants no installable APK for each phase/step. Continue coding, automated tests and CI without waiting for phone checks. Prepare and publish one signed APK for the complete integrated app when it is ready for their final phone test. Ordinary CI instrumentation binaries are internal and are not uploaded; signed release artifacts require an explicit candidate request.
## 2026-11-21 — TMDB transport foundation

- Main commit 4b74bc0 adds the fixed-origin client and synthetic transport/privacy/cancellation tests; follow-up 5a467f2 makes About/Credits visible and CI #115 passes. No user APK/Release was produced.
- Next: finish bounded artwork caching and render cached posters/backdrops with offline/reduced-motion fallbacks; then continue the remaining product/design/performance phases. Do not make an installable APK until the integrated candidate is ready.
- User requested one integrated app APK when the app is ready; do not publish per-step APKs. Latest signed Release remains v0.1.91. OPPO phone acceptance, Glass behavior and device performance remain unverified.

## 2026-11-21 — Credits test correction

CI #112 compiled and then failed one synthetic Android UI case because its search for “About and credits” did not scroll the settings screen to that offscreen action. The UI action is present; the test now scrolls to it before tapping. CI #113 verifies client groundwork and repeats the existing suite; the correction triggers the next run. No APK/Release. Continue manual title search → candidate → explicit confirm and offline cache presentation; keep phone testing until one integrated build is ready.

## 2026-11-21 — Credits discoverability correction

CI #113 repeated the Credits accessibility failure because the synthetic device did not expose the Compose scroll action. The About and credits button now appears below the settings introduction, visible immediately; the UI test taps it directly. CI #114 includes the earlier helper attempt; follow-up CI will verify this corrected layout. No APK/Release.

## Historical handoff snapshots (superseded)

# Historical Codex handoff — 2026-11-14
User confirmed signed Player v0.1.51 → v0.1.53 **in-place Obtainium update**. Before coding, read `AGENTS.md`, `docs/DESIGN_SYSTEM_AND_PERFORMANCE.md` and `docs/UPSTREAM_AND_PRIVACY.md`. Premium visual goal: original cinematic Android UI, carefully bounded Liquid Glass controls and active slider thumbs, **measured** 60-Hz target on OPPO Reno10 CPH2531 Dimensity 7050/Mali-G68 MC4, 120-Hz optional. Research public Apple HIG and maintained GitHub projects via Codex Cloud internet access, obey licences/privacy. One local read-only SAF folder; no network permission needed for Phase 1. **Every single small or large change updates AGENTS.md's recent ledger AND DEVELOPMENT_LOG.md.** Keep main release identity and no extra signing secrets. Phase 1 code/device functionality unfinished; don't jump to TMDB, production player/Glass or claim actual FPS before testing.

# Player — ChatGPT Work / AI agent operational handoff

**Read first:** AGENTS.md, PROJECT_CONTEXT.md, CHAT_HISTORY.md, docs/ROADMAP.md, docs/ARCHITECTURE.md, docs/DECISIONS.md, docs/ANDROID_SIGNING.md, docs/DEVICE_TESTING.md, DEVELOPMENT_LOG.md and ISSUES_AND_FIXES.md. The GitHub repository is the durable source of truth; never treat any stale chat as authoritative.

## User-approved execution contract (2026-11-14)
- Act as the hands-on software implementer, not as a speculative planner. Work on Batmancpu/Player/main only; small focused commits and an immediate GitHub Actions check after each change.
- The user develops from an OPPO Reno10 5G Android 15 handset and iPad, without a normal PC. Do not ask for Termux, local Android Studio, scripts, or manual patches when GitHub Actions/connected tools can do the work.
- Follow the **six phone-testable phases** in docs/ROADMAP.md. The user specifically merged local one-folder access, scanning, filename parsing and the Room-backed offline library into **Phase 1**. Each phase ends in a verified, **permanently signed GitHub Release APK** and a device acceptance checklist. Do not mark a phase accepted until the user actually tests it.
- Treat selected local folder read-only via Android SAF as a strict privacy boundary. Exactly ONE selected Android-local tree; no broad storage permissions, no cloud/network providers, no scanning siblings, no moving/writing source files. No media bytes ever uploaded. Use only needed normalized titles when TMDB/subtitle stages arrive.
- Maintain the project handoff docs at every session end with commits/build status/releases/next exact task. Keep keys, passwords, base64 secret values and credentials out of ALL repo files and logs.
- Never substitute a CI debug APK for a long-term signed release update. **New permanent key was explicitly generated and privately supplied in the current chat after user authorized a pre-release reset on 2026-11-14.** Earlier signing key identity is superseded. Do not generate another key after first signed installation. The user alone adds four encrypted GitHub Actions secrets and makes secure backups. docs/ANDROID_SIGNING.md has names and the NEW fingerprint.
- Store release APKs in GitHub **Releases** with changelog, public certificate/hash verification and increasing versionCode; Obtainium will track https://github.com/Batmancpu/Player, not CI artifact links.
- Do not presume ChatGPT Work can run asynchronously from this chat: this handoff is for an explicitly initiated Work session.

## Immediately verified state — Codex audit 2026-11-14
- Baseline main acb9829; CI 43588156049 succeeded. Signed v0.1.51 and v0.1.53 exist and user confirmed in-place Obtainium update. Signing setup is complete; never request new keys/secrets.
- SAF scanner/parser/Room prototype is implemented. Current slice rejects incomplete/error cursor listings before reconciliation, with JVM regression tests; PR CI 43612121975 passed, merged as 79ee09d (functional commit 9557723). Main signed CI 43612318514 passed tests/build/certificate pin for 79ee09d; signed CI artifact v61 exists, with no new Release. No new Release published; Phase 1/device acceptance pending.
- Read docs/PHASE1_AUDIT.md for prioritized defects/test gaps, source/research evidence and implementation order. Next: implement deterministic H-009 lifecycle ordering tests and serialize scan/folder ownership. Inspect newest main CI before editing.
- The older signing/release diagnostic sections below are historical, superseded by this status and the final user-confirmed update entry.

## Update discipline
At end of every Work session write: current SHA, relevant workflows/artifacts, whether signing succeeded, release link/tag/APK checksums if any, phase substeps/acceptance, device evidence vs unverified, blockers needing user action, and first next command/change. Append to CHAT_HISTORY.md and DEVELOPMENT_LOG.md; update PROJECT_CONTEXT.md and ISSUES_AND_FIXES.md as needed. Never claim that a future task has already run.

## Signing identity reset — newest handoff (2026-11-14)
- User explicitly rejected reuse of the prior key because no signed release exists and authorized a fresh key. New private JKS and base64/credential files generated and delivered in the current chat. They are not in GitHub. The previous fingerprint documented in historical sections is obsolete.
- NEW public SHA-256: `4D:2F:17:89:ED:27:D3:96:13:35:FE:36:45:64:55:BD:9A:31:0B:0D:CD:F4:1E:8C:CC:92:53:98:33:9E:80:BF`. Current `docs/ANDROID_SIGNING.md` is authoritative, and CI certificate pin now matches this identity (commit f119f07). Do not revert to an old key or commit private key material.
- User is currently entering the four GitHub Actions secrets, one at a time, via GitHub Settings. A password generator is not a substitute for the Base64 keystore contents. No signed release has been published or physically installed with the new certificate yet.

## Controlled first release trigger for Obtainium testing (2026-11-14)
- The user has entered all four GitHub Actions secrets and requested the first APK to install through Obtainium before Phase 1 acceptance.
- Because the GitHub connector does not expose workflow_dispatch, the CI workflow accepts a specifically named `main` push commit with subject prefix `release(test):` as a one-time intentional publication trigger. Ordinary source commits DO NOT publish releases. The manual workflow_dispatch publish toggle remains available.
- Signing, APK certificate check, tests, versionCode, GitHub Release asset and SHA-256 all remain mandatory. The release is explicitly a TEST BUILD and must not be represented as Phase 1 completed.
- Do not publish a second update build until the user has installed the first through Obtainium; then publish a higher versionCode with the same certificate so they can verify update-in-place.

## 2026-11-14 — Release-signing diagnostic correction
- GitHub Actions run 47 identified an empty PLAYER_RELEASE_KEY_PASSWORD even though the user said all four secrets had been added. The three other signing secret variables were populated. Run failed at early configuration; no APK published.
- Current JKS uses identical store/key passwords. The CI source now safely falls back to PLAYER_RELEASE_KEYSTORE_PASSWORD when optional key password is absent. Never expose either password in log/docs. Next gate: successful CI signing/certificate verification, then an explicit release(test): trigger commit to publish for Obtainium.

## Signing diagnostic gate after run 48 (2026-11-14)
- Workflows #47 and #48 failed at initial signing validation. Run 47: fourth key-pass secret absent; workflow modified to fall back to store pass. Run 48: keytool check failed despite that correction. The locally generated JKS, Base64 and credentials were verified to match by keytool in a private session workspace.
- CI now includes a non-secret local-JKS file hash checkpoint, followed by separate store-password and alias validation. Use the next CI's precise public diagnostic to ask the user to correct only the necessary GitHub secret. No signed APK has yet been published; never claim one.

## 2026-11-14 — Alias fix after run #49
- CI #49 identified a wrong GitHub alias secret. The JKS byte hash and store password both matched the private original. The alias is public and fixed, so workflow now passes 'player-release' directly rather than reading a potentially wrong GitHub alias secret.
- User should not be asked to re-enter any more secrets unless signed assembly reveals a new actual problem. Do not claim Release published until GitHub Releases reports an APK asset.

## 2026-11-14 — First test release trigger after proven signing
- CI #50 succeeded in actual signed APK assembly and apksigner verification. Its final duplicate fingerprint check failed only due to hardcoded apksigner output label: current tool prints "V2 Signer: certificate SHA-256 digest". The certificate digest in CI matched the permanent key. This is a known parsing defect, not evidence of wrong signing.
- The script now reads any "certificate SHA-256 digest:" line and compares it to the pinned exact certificate, then publishes only when CI passes and the main-branch commit subject starts with release(test):.
- This session's next push is specifically the user-requested FIRST signed Obtainium testing release. Verify real GitHub Release and APK asset before claiming publication.

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


## H-009 next-goal update — 2026-11-14
LibrarySession/LibraryViewModel now own load/scan/save across recreation, serialize obsolete work and reject stale publications. Selected-tree cache activation replaces picker-local cleanup. Six JVM regression cases added; PR gate pending at authoring, actual Room/device lifecycle behavior unverified. After green merge/signing, next coding goal is H-010 root/grant recovery, then H-011 provider cancellation/bounds.

H-009 verification: PR #2 commit 17e46a8 passed CI 43613459732, merged as 10dab08; main CI 43613651152 passed unit/debug, signed release and permanent certificate pin. No new Release. Next coding goal H-010 grant/root recovery with deterministic acquisition/revocation/process-interruption tests, then H-011.

## H-010 slice — 2026-11-14
Folder grant/root ownership moved to retained ViewModel plus serialized manager; nine fake-store tests. PR gate pending. Next H-011 cancellable/bounded provider queries, then persistence/parser/UI acceptance coverage and a signed Phase 1 Release candidate. User authorized sustained implementation (no future scheduled runs are currently requested); inspect live main/open PRs before resuming, no routine permission requests, no fabricated phone acceptance.

## 2026-11-15 continuation — unreleased main maintenance
Both one-time continuations have now fired. Published candidate remains v0.1.79; no actual OPPO acceptance report. H-014 moves whole scan/index callbacks off UI thread and adds per-row mapping cancellation plus a 1,000→100 Android main-caller reconciliation regression. CI pending at authoring; this change is unreleased and must not be described as part of v0.1.79. Next: inspect latest main CI, record result, then actual phone report/Phase 1 defect triage. Main only; no duplicate release or gated Phase 2 production.

H-014 verification completed: 57e4539, CI 43667022100 passed all gates; publication skipped as intended. Documentation follow-up CI pending at authoring. Next actual OPPO report and direct-main Phase 1 bug triage, no duplicate release/gated Phase 2.

## Latest user steering — 2026-11-15 larger offline preview
User tested v0.1.79: folder/offline retention/switching work, media called audio (clarification pending), cancellation feedback unclear, Glass Lab dock broken. User requests substantial useful progress and FC Zenith/immediate Zenith reference, not additional micro-update waiting. Larger offline browsing/details/local-player preview and dock repair now prepared. No online metadata/network approval implied; no completed later phase/FPS claim. Media3 dependency gate must pass before integration; then Android decode/UI safety tests and signed candidate. Main only.

- 2026-11-15 — Latest user authorizes next planned phase implementation without routine APK-install pauses while absent. One requested continuation confirmed for today 13:30 Asia/Kolkata (08:00 UTC); inherit latest GitHub state, no task branches. Continue independent implementation/tests and record phone acceptance/performance as pending; preserve privacy/provider controls. Current preview source 3c62a71, CI #85 (43676678448) in progress. Next: finish preview gate, inspect screenshots, publish, then next roadmap work without inventing acceptance.


## 2026-11-21 — immediate artwork follow-up
Follow-up commit adds a localized poster accessibility description referenced by the Compose view; CI #119 for artwork is still in progress. Verify both runs and update docs with their exact result before continuing.


## 2026-11-21 — artwork test fixture fix
A test-only follow-up replaces the malformed synthetic PNG with a valid CRC-checked PNG. Verify CI #119, #120 and this follow-up run before continuing.


## 2026-11-21 continuation — player track controls
CI #124 / 44904367611 passed for bounded Compose UI waits and catalogue copy on main. Current source adds Media3 audio/subtitle track actions in local playback using the official selector; audio/text actions appear only when supported tracks are present and subtitles can be turned off. The new commit is awaiting CI. No installable APK/Release is created at each commit per user direction; build one complete signed candidate only after the integrated app is ready. Next: verify CI, then sidecar subtitles and subtitle delay. OPPO playback, subtitle rendering, performance, battery and Glass remain unverified; do not claim acceptance/FPS.


## 2026-11-21 — selector integration correction
A live-tree audit found commit 9200aaeed66c08eb30f29b27d9d8713f8afe5c67 changed only documentation; its described selector source/resource changes had not landed. CI #125 Android integration success therefore applies to the prior player. A corrected commit now adds real Media3 audio/subtitle dialogs and test coverage for opening the selector. Verify exact commit blobs and full CI. No interim APK/Release. After green CI continue parent-safe local sidecar subtitles, delay, then overall Glass/accessibility/performance integration. OPPO evidence remains pending.


## 2026-11-21 — selector green; sidecar foundation
CI #126 / 44906299456 passed Android 15 integration (selector dialog open/dismiss), unit/build, Room schema and privacy. Embedded audio/subtitle selectors are real and tested against the synthetic audio fixture. New scanner foundation captures opaque parent document IDs and same-parent subtitle matching, with bounded deterministic suffix handling/tests; CI pending. It is not persistent yet, so offline-restored documents cannot use sidecars until Room v4 migration is complete. No installable APK. Next: verify CI then persist parent IDs (safe default migration), test v1→v4 preservation, and wire supported sidecars to the local content-only Media3 item. One complete APK later; no device acceptance/FPS claims.


CI #127 Android job passed, but its unit test step failed at `LocalSubtitleMatcherTest.capsSubtitleCandidates`. The synthetic names were not valid matches, so the expected eight was wrong; matcher logic is unchanged. A test-only follow-up now uses twelve same-stem subtitle entries to exercise the cap. Verify its full CI before Room v4 parent persistence. No installable APK.


H-024 found while fixing CI #127's cap test: a blank ISO3 value from Locale.ROOT could strip a simple base name after language suffix removal. The corrected candidate now filters/guards empty tokens and tests `Film.en.srt` directly; CI pending. Do not proceed to Room v4 until this full gate passes. No APK.


CI #129 / 44908615223 passed the language-suffix root-locale fix, Android 15 integration, JVM/build, Room v1-v3 and privacy gates. Current source adds Room v4 parent identity with default-empty 3→4 migration and offline index persistence; tests cover old v1 row preservation/default and parent round-trip. Full CI pending. Sidecar playback remains off until v4 and SAF URI validation are green. No APK/Release.


CI #130 / 44909463881 fully passed: Android 15 v1→v4 migration + offline parent-ID round-trip, unit/build, schema and privacy gates. Exact generated Room v4 JSON is committed from schema artifact 14709327372; historical v1-v3 remain intact. Next: wire validated same-parent SRT/VTT/SSA/TTML content URIs into Media3 and add synthetic provider end-to-end tests. No APK/Release; OPPO still unverified.


### Next integration — subtitle-safe video resize
Added Fit/Fill/Zoom controls and a documented Media3 layout adaptation with the caption view outside the aspect-ratio video frame. Android hierarchy and resize-cycle tests added; CI pending. No APK. Review synthetic artifacts after the run. Then complete subtitle delay when Media3 support reaches stable, continue production Glass/navigation/accessibility, and measure target-device behavior only when an OPPO trace is available.


### Player test note (2026-11-21)
CI #147 / 44923019327 passed 42/43; remaining failure was the final fullscreen label lookup racing controller visibility. The integration test now covers action activation/state change while the isolated Compose test covers reversible enter/exit accessible labels. CI #148 is testing the new separate subtitle viewport and Fit/Fill/Zoom behavior. CI result pending; no APK.


### CI #148 playback note
New subtitle/video sibling and Fit/Fill/Zoom tests passed. The 10-second synthetic playback fixture made the existing auto-hide assertion flaky; it now lasts 30 seconds, and the app layout retains Media3 buffering/error placeholders. CI verification pending.


### Current auto-hide test synchronization
CI #150 failed one player test; CI #149 passed the same 45-test source. The test now waits for Media3's active-playback callback before asserting control auto-hide, so slow idle/buffering setup is excluded. Caption/zoom tests passed. CI pending; no APK.


### Catalogue series detail update
Series sheets now group episodes under Season headings and use Sxx · Exx labels from the local parser. The exact indexed document ID is preserved for playback. Synthetic Android test covers ordering and selection; CI pending.


### Playback chrome regression (2026-11-21)
CI #151 confirmed the Compose player header remained visible after Media3 reported active playback. Source now includes a matching explicit 2.5-second Compose timeout that hides both PlayerView controls and header; paused playback keeps controls visible. CI pending. No APK.


### Verified catalogue update
CI #152 / 44931933805 passed all 46 Android 15 tests, including local season grouping, exact indexed episode playback, caption/video sibling layout and Fit/Fill/Zoom cycling. Full unit/schema/privacy job completion was pending at last check. Player timeout interaction resets are being tested in #153 and follow-up.


### Engine choice
DEC-021 records Media3 as primary and LibVLC as a measured fallback candidate. Media3's format list is not a guarantee of device codec support; target local corpus testing remains pending. Do not add another engine until failures, license obligations, package size, battery and thermal effects are measured.


### Default catalogue shelves
The unfiltered library uses horizontal Movies and TV Shows shelves; text search and explicit type filters retain the grid. Cards still resolve from the local index and app-private artwork only. CI pending.


### Current continuation — details sheet scroll fix (2026-11-21)
- Main is `4124d7d0c729acdfdd8d0fed448cf9788a346046`. CI #164 built successfully and ran 46 Android 15 tests: 45 passed, 1 failed (`titleMetadataSearchIsManualAndMatchRequiresASecondConfirmation`) because the detail sheet dismissed on an upward scroll before “Search TMDB” became reachable. The current patch disables swipe-to-dismiss only on the content details sheet and adds a labeled Close button. Back and outside-tap remain dismissal routes. Verification pending on the next CI run; publish the signed checkpoint only after the full workflow succeeds. Continue the remaining app batches after publication; actual Reno10 acceptance/performance remains unmeasured.

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


### Signed checkpoint requested; CI #173 green (2026-11-21)
- User changed the earlier no-interim-APK preference: publish one signed checkpoint now, then keep working through the remaining substantial batches without waiting for a phone install between them. Source SHA `7cbff28881332f7da8de418b8b348166885ff857` passed all 46 Android 15 instrumentation tests and the unit/schema/privacy/identity gate in CI #173. The details sheet now opens fully expanded, with a lazy episode/metadata list, visible Close action and manual metadata test. The CI screenshot confirms the Search TMDB action is on-screen.
- A `release(test):` commit is being pushed to main to trigger the existing signed build workflow. Verify the created tag/asset, versionCode, package ID and signing fingerprint before sharing the release URL. Last published signed release before this checkpoint: v0.1.91. Continue the Glass Lab/back-navigation and production player polish, then measured optimization; do not wait for interim handset installs. OPPO acceptance and frame-time/thermal evidence still require the actual phone at the final gate.
