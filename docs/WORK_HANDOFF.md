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

## Immediately verified state on 2026-11-14
- Head of completed source commit: d3ecc17c0036f4ef2f5db036c970d9c810b2f206. GitHub Actions #43 (43582079994) succeeded (tests and debug build). Previous key runs: #38 scanner SAF UI passed; #39 release publication workflow passed its normal push path; #40 filename parser passed; #41 Room/KSP deps passed; #42 offline Room index passed.
- The Android system SAF picker persists one read-only permission, with one-provider allowlist and recovery; SafLocalMediaSource uses immediate-child queries through that tree and DocumentsContract.isChildDocument containment checks. Device behavior still **unverified**.
- Basic manual scan/cancel/error states, a lazy list of discovered movie/TV names and sidecar counts are now wired to the launcher; simple filename parser and Room index persist records on complete scans and reload offline. Room transaction upserts and deletes missing records **only after a successful scan**; cancellation/failed provider query does not overwrite saved index.
- All are preliminary Phase 1 functionality. Filename corpus, nested folder/provider/instrumentation tests, app lifecycle testing, offline/scanning UX, Room migration validation and physical handset verification are incomplete. Do not mark Phase 1 accepted.
- GitHub Releases endpoint returned zero releases. Permanent signing secrets were missing at the last confirmed run; signed Release steps were skipped. No installation APK exists in Releases yet. Repo workflow supports manual workflow_dispatch publish_release=true: it refuses missing secrets, validates certificate fingerprint, creates a GitHub Release APK asset and SHA-256. That manual signing/publication path itself has **not** been exercised.
- Next coding tasks: tighten scanner tests for permission/cancellation/provider traversal; add representative parser fixture corpus (including multi-episode ambiguity), improve UI progress/lifecycle handling and Room schema-migration testing; document any handset findings. Keep small commits and CI gates.
- Next user-required action for a release: download and securely back up the NEW JKS and credentials created in the 2026-11-14 signing-reset chat, and enter four GitHub repository Actions secrets; do not share their values here. Then dispatch the manual signed release workflow and verify artifact certificate/hash. See docs/ANDROID_SIGNING.md and docs/PHASE1_PHONE_TEST.md.
- The user plans to initiate ChatGPT Work separately. That agent must read these records live and continue only from verified main/CI, not stale chat claims.

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
