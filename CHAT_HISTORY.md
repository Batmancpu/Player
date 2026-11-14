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
