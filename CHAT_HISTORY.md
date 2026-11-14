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
- The helper-deletion commit temporarily left its Bash syntax-check step in the workflow; run 28 (`43574064538`) failed with file-not-found. The next workflow commit removed that stale step; run 29 (`43574233803`) passed tests, debug build and artifact upload. This was a transient workflow cleanup failure, not an application-code defect.
- The cumulative log is now part of the mandatory reading order and must be appended at each future session end. Keep all secrets out of it.
