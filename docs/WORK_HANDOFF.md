# Player — ChatGPT Work / AI agent operational handoff

**Read first:** AGENTS.md, PROJECT_CONTEXT.md, CHAT_HISTORY.md, docs/ROADMAP.md, docs/ARCHITECTURE.md, docs/DECISIONS.md, docs/ANDROID_SIGNING.md, docs/DEVICE_TESTING.md, DEVELOPMENT_LOG.md and ISSUES_AND_FIXES.md. The GitHub repository is the durable source of truth; never treat any stale chat as authoritative.

## User-approved execution contract (2026-11-14)
- Act as the hands-on software implementer, not as a speculative planner. Work on Batmancpu/Player/main only; small focused commits and an immediate GitHub Actions check after each change.
- The user develops from an OPPO Reno10 5G Android 15 handset and iPad, without a normal PC. Do not ask for Termux, local Android Studio, scripts, or manual patches when GitHub Actions/connected tools can do the work.
- Follow the **six phone-testable phases** in docs/ROADMAP.md. The user specifically merged local one-folder access, scanning, filename parsing and the Room-backed offline library into **Phase 1**. Each phase ends in a verified, **permanently signed GitHub Release APK** and a device acceptance checklist. Do not mark a phase accepted until the user actually tests it.
- Treat selected local folder read-only via Android SAF as a strict privacy boundary. Exactly ONE selected Android-local tree; no broad storage permissions, no cloud/network providers, no scanning siblings, no moving/writing source files. No media bytes ever uploaded. Use only needed normalized titles when TMDB/subtitle stages arrive.
- Maintain the project handoff docs at every session end with commits/build status/releases/next exact task. Keep keys, passwords, base64 secret values and credentials out of ALL repo files and logs.
- Never substitute a CI debug APK for a long-term signed release update. Release key exists and was privately supplied in previous chat; do not generate or replace it. The user alone adds four encrypted GitHub Actions secrets and makes the secure backups. docs/ANDROID_SIGNING.md has names and fingerprint.
- Store release APKs in GitHub **Releases** with changelog, public certificate/hash verification and increasing versionCode; Obtainium will track https://github.com/Batmancpu/Player, not CI artifact links.
- Do not presume ChatGPT Work can run asynchronously from this chat: this handoff is for an explicitly initiated Work session.

## Immediately verified state on 2026-11-14
- Head from preceding session: 95881e4de9b80663457de15a69394da504d7f9b4; GitHub Actions #36 (43580154860) succeeded.
- Pure MediaTreeScanner implementation and JVM tests exist, but no Android ContentResolver adapter/UI integration, deterministic parser, Room, TMDB, catalogue or player.
- Picker and persisted read-only grant are source-implemented but untested on CPH2531.
- GitHub Releases API returned zero releases. CI #36 signed-release job was **skipped** because repository signing secrets were absent. Thus no signed installable APK yet.
- **Exact next coding action:** implement SAF local-tree adapter that feeds MediaTreeScanner; record scan errors/permission failures, support cancellation; integrate a basic real files list into FolderSetupScreen. Verify CI. Then add parser and Room persistence in focused commits. Publish only after permanent signing and testing gates.
- **Exact next release action:** add a workflow/manual publish gate that consumes a verified signed Release APK, checks expected certificate fingerprint, publishes it as a GitHub Release, and never publishes a debug APK or an unsigned build. User must configure secrets before this can produce an APK.

## Update discipline
At end of every Work session write: current SHA, relevant workflows/artifacts, whether signing succeeded, release link/tag/APK checksums if any, phase substeps/acceptance, device evidence vs unverified, blockers needing user action, and first next command/change. Append to CHAT_HISTORY.md and DEVELOPMENT_LOG.md; update PROJECT_CONTEXT.md and ISSUES_AND_FIXES.md as needed. Never claim that a future task has already run.
