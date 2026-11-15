## Current handoff — 2026-11-15

- The signed Release was built from main commit `36e6c684f8191037c183603139de2e0891b832c8`; its functional app source is `e4cc4b13acbd3ffa387a0dfc55f56d8fae34fcf2`. GitHub confirms `main` is the only branch and there are no open PRs. This environment has no usable local Git/Gradle/ADB checkout, so commits/builds use GitHub Actions.
- Latest signed Release is [v0.1.91](https://github.com/Batmancpu/Player/releases/tag/v0.1.91), versionCode 91, from commit `36e6c684f8191037c183603139de2e0891b832c8`. APK SHA-256: `7c9ff8287405be9b1f607259608d84a256e5aed1fcc4cea24e3f9b882dd05e17`. CI verifies the unchanged package and permanent certificate. The user reports v0.1.79 recognized four video titles/years, retained the library offline and switched folders; v0.1.91 behavior remains untested on the OPPO.
- Current main contains the larger offline catalogue/details and foreground local Media3 playback preview, resume checkpoints, scan feedback and a one-surface Glass Lab dock. No remote media source, INTERNET permission, broad storage permission, backup, original-media write or user-file delete action was added.
- [CI #91](https://github.com/Batmancpu/Player/actions/runs/43726886945) completed successfully and published the signed release. It passed 15 Android 15 integration tests, JVM/build, signed APK privacy/identity and permanent certificate checks; all five retained synthetic screenshots and test XML were inspected. CI90 had already verified the same functional source. Emulator results are not OPPO acceptance or performance measurements.
- **Latest main verification:** Room v1's exact generated schema is committed at `app/schemas/com.batmancpu.player.ui.library.PlayerLibraryDatabase/1.json` (SHA-256 `820f2e814c9fcc3780be7c4bf6a1cbcb2498d52d1f92a0439b58aef11c9d6d04`). CI #92 attempt 2 / 43728517965 passed all 15 Android 15 cases, JVM/build, signed APK privacy/identity and permanent-certificate checks. Attempt 1 was a transient resolution failure for the same pinned Compose compiler plugin that had passed CI #91. No release was published from this schema-only task.
- **Next action:** CI-check the committed snapshot, then define/test the offline normalized title/year/type metadata query with explicit default-off consent. Do not add HTTP, credentials, new permissions or TMDB destinations in that slice. The v0.1.91 phone checklist is in docs/OFFLINE_PREVIEW_TEST.md; Phase 1/device acceptance is still pending.
- Per D-026, after publishing the offline preview continue independent Phase 2 foundations without network calls: preserve the actual Room v1 schema, then define/test normalized title metadata queries and explicit default-off consent before any HTTP client. Online integration remains off until user-managed credentials and approved destinations are implemented.
- Keep work directly on `main`; no task branches/PRs or scheduled tasks. Preserve application ID, certificate, monotonically increasing versionCode, read-only single-folder SAF, `allowBackup=false`, and user media privacy.

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
Folder grant/root ownership moved to retained ViewModel plus serialized manager; nine fake-store tests. PR gate pending. Next H-011 cancellable/bounded provider queries, then persistence/parser/UI acceptance coverage and a signed Phase 1 Release candidate. User authorized sustained implementation and +5h/+10h scheduled continuations; inspect live main/open PRs before resuming, no routine permission requests, no fabricated phone acceptance.

## 2026-11-15 continuation — unreleased main maintenance
Both one-time continuations have now fired. Published candidate remains v0.1.79; no actual OPPO acceptance report. H-014 moves whole scan/index callbacks off UI thread and adds per-row mapping cancellation plus a 1,000→100 Android main-caller reconciliation regression. CI pending at authoring; this change is unreleased and must not be described as part of v0.1.79. Next: inspect latest main CI, record result, then actual phone report/Phase 1 defect triage. Main only; no duplicate release or gated Phase 2 production.

H-014 verification completed: 57e4539, CI 43667022100 passed all gates; publication skipped as intended. Documentation follow-up CI pending at authoring. Next actual OPPO report and direct-main Phase 1 bug triage, no duplicate release/gated Phase 2.

## Latest user steering — 2026-11-15 larger offline preview
User tested v0.1.79: folder/offline retention/switching work, media called audio (clarification pending), cancellation feedback unclear, Glass Lab dock broken. User requests substantial useful progress and FC Zenith/immediate Zenith reference, not additional micro-update waiting. Larger offline browsing/details/local-player preview and dock repair now prepared. No online metadata/network approval implied; no completed later phase/FPS claim. Media3 dependency gate must pass before integration; then Android decode/UI safety tests and signed candidate. Main only.

- 2026-11-15 — Latest user authorizes next planned phase implementation without routine APK-install pauses while absent. One requested continuation confirmed for today 13:30 Asia/Kolkata (08:00 UTC); inherit latest GitHub state, no task branches. Continue independent implementation/tests and record phone acceptance/performance as pending; preserve privacy/provider controls. Current preview source 3c62a71, CI #85 (43676678448) in progress. Next: finish preview gate, inspect screenshots, publish, then next roadmap work without inventing acceptance.
