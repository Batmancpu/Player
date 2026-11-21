# Architecture and product decisions

Statuses: Accepted, Proposed, Open, Rejected. Change decisions only with evidence and a development-log entry.

## Accepted

### D-001 — Keep AfuseKt release repository untouched
Batmancpu/InfuseKt-release is a release-only fork. New source lives in Batmancpu/Player. The release fork contains no application source.

### D-002 — One selected local folder
One SAF folder tree is the only source. Recursive scanning is limited to its descendants.

### D-003 — No backend or media upload
No Player server, proxy, account, streaming, cloud integration or video upload. Metadata and artwork are fetched directly from TMDB.

### D-004 — Offline-first library
Persist indexed files, matches, metadata and artwork locally. Previously indexed content remains browsable offline.

### D-005 — Apple-inspired, content-first visual language
Apple TV cinematic media presentation; iOS 27-inspired Liquid Glass for functional controls only. Do not apply Glass indiscriminately to posters.

### D-006 — Do not transplant FC Zenith's Glass implementation wholesale
Use FC Zenith as a visual/engineering reference. Its implementation depends on Zenith-specific settings, composition locals, player/navigation state and vendored Backdrop internals.

### D-007 — Haze 2.1.0 is the first Glass proof-of-concept candidate
Proposed pending build/device validation. Its stable Glass API requires matching Kotlin 2.4.21 and Compose Multiplatform 1.12.2; upgrade in isolated commits. The library release is not proof of OPPO compatibility.

### D-008 — No custom native UI/rendering path in v1
Do not add custom C/C++, NDK, JNI, native shader libraries, experimental native window-backdrop or custom RenderEffect rendering for the UI. The user's CurrentLens history includes an OPPO CPH2531 Android 15 native SIGSEGV, although the exact root cause is unverified. This decision does not automatically prohibit a third-party native media decoder; that requires the separate playback-engine and licensing review in O-006.

### D-009 — Native Android, not KMP, for v1
Prefer one Android app module using Kotlin and Jetpack Compose. Reuse libraries and patterns, not unnecessary multiplatform complexity.

### D-010 — Playback deferred (original decision; superseded by D-017)
At the original project definition, the first release was a catalogue/scraper rather than a player. On 2026-11-14 the user explicitly expanded the product scope to include local playback, audio/subtitle track selection, on-demand external subtitle retrieval and subtitle-delay controls. D-017 is now authoritative; this entry is retained only to explain the historical roadmap.

### D-011 — Small commits and main-only workflow
One main branch, focused commits, CI gate after meaningful changes, corrective commits for failures, durable records.

### D-012 — CI is not device validation
CI build/test success and OPPO handset validation must be reported separately.

## Open decisions
- **O-001 TMDB credential UX:** BYO TMDB credential or optional metadata until supplied? A no-backend APK cannot hide a shared static secret.
- **O-002 Local provider support:** initially support Android external-storage DocumentsProvider; add OPPO/OEM authorities only after testing; unknown providers default-deny.
- **O-003 SDK floor:** choose minSdk/targetSdk after the project skeleton and SAF capability check; do not inherit Flux's values blindly.
- **O-004 Brand/package/signing:** Player is temporary. Do not change package identity or signing after release without a migration plan.
- **O-005 Glass engine:** compare Haze 2.1.0 against the FC Zenith Backdrop reference on CPH2531 before final adoption.
- **O-006 Playback engine:** compare current AndroidX Media3 with LibVLC for local SAF URIs, real container/codec coverage, embedded track selection, external subtitles, hardware decoding, smoothness on CPH2531, APK/ABI footprint and full licence obligations. Media3 is the first candidate, not a final selection. Reassess the current stable release at implementation time; 1.11.1 was the official stable release shown on 2026-11-14.
- **O-007 Subtitle search/download provider:** evaluate provider API terms, supported languages/releases, account/key requirements, rate limits, privacy, availability and file licensing before selecting one. Send only normalized media identifiers; never send local paths, full filenames, SAF URIs or video/subtitle contents.

## Rejected
- Implementing inside the release-only InfuseKt-release repository.
- Custom cloud/media server or proxy.
- Uploading media files/content to TMDB or another provider.
- MANAGE_EXTERNAL_STORAGE when one SAF tree grant is sufficient.
- Glass on every poster/list item.
- Starting with NDK/native rendering.
- Treating green CI as proof of device stability.
- Giant mixed-purpose commits or continuing while main is failing.

### D-013 — Do not bundle Apple SF Symbols glyph artwork in Android
**Status:** Accepted  
**Decision:** Do not ship SF Symbols glyph assets in the Android APK unless explicit licensing permission for this use is established. Use a coherent permissively licensed or original vector icon family, tuned for Apple-like optical sizing, stroke, spacing and motion.  
**Reason:** FC Zenith's icon library code is MIT, but its own README states the glyph artwork remains Apple-owned and directs users to Apple's SF Symbols license, which restricts use to Apple platforms. Design inspiration does not grant glyph redistribution rights.


### D-014 — Use selected Lucide vectors for Player's initial symbol system
**Status:** Accepted  
**Decision:** Use a small set of adapted Lucide outline vector assets behind a single PlayerGlyph registry. Preserve the upstream ISC/MIT license text. Add new icons only when product screens need them.  
**Reason:** Lucide is a consistent, permissively licensed line-icon family; selective vectors reduce the asset footprint and avoid bundling the full library or Apple-owned SF glyph artwork.


### D-015 — Use Haze source-backed rendering; keep experimental native window backdrop disabled
**Status:** Accepted  
**Decision:** The Glass lab and initial implementation use explicit HazeInput.Sources and hazeSource. Do not enable HazeFeatureFlags.isPlatformBackdropEnabled or use the experimental native Android window-backdrop path in v1.  
**Reason:** Native Android window-backdrop rendering is experimental, eligible only on Android 17/API 37.2+ and can draw a frame without source input when unavailable. The target device is Android 15. Haze source-backed rendering is the controllable cross-version path and must be benchmarked on the actual handset.

 
### D-016 — First Glass screen is a source-backed laboratory
**Status:** Accepted  
**Decision:** Implement an isolated Glass lab with a fixed local artwork/gradient source, one small four-control morphing cluster and explicit disabled/fallback path. Use Haze 2.1.0 stable API, HazeInput.Sources, hazeSource and Default/Balanced performance.  
**Reason:** Proves the visual engine on the target Android codebase without mixing it with media scanning, TMDB or main library UI. Avoids the experimental native Android window-backdrop path.

### D-017 — Local playback and subtitle controls are approved product scope
**Status:** Accepted (user instruction, 2026-11-14)  
**Decision:** Tapping a movie or episode in the catalogue opens a built-in player for the local media document. Aim for broad practical format compatibility, but never promise literal “every format”: container support, codecs, decoder availability and hardware capability all matter. Include basic playback, embedded audio/subtitle track selection, external subtitle loading, online subtitle search/download from a provider selected after review, and adjustable subtitle delay. Downloaded subtitles stay in app-private storage by default; never modify source media. The player UI must remain minimal and uncluttered. Audio/subtitle sheets and delay controls may use restrained iOS-inspired Liquid Glass after the functional player is stable.  
**Boundaries:** No media upload, remote playback, streaming service, server, account system or transmission of local file identifiers/content. Subtitle lookup may send only normalized media identifiers to a separately reviewed provider.  
**Reason:** The user explicitly expanded the original catalogue-only scope. This entry supersedes D-010 and the earlier playback-deferred roadmap.

### D-018 — Functional catalogue precedes production Glass integration
**Status:** Accepted  
**Decision:** The existing Glass Lab remains an isolated experiment, not the production home screen. Build a plain, stable catalogue flow first: one-folder setup, media discovery, filename matching, TMDB metadata/artwork, local persistence and library/detail screens. Add local playback and subtitle/audio features next. Integrate and tune Liquid Glass only on functional navigation/player controls after these foundations work; keep posters and video unobscured.  
**Reason:** The user explicitly prioritised a useful working product over further visual experiments. Haze dependencies and lab work are retained but must not dictate the functional architecture.


### D-019 — Stable self-managed Android release signing
**Status:** Accepted  
**Decision:** Use one long-lived RSA 2048 Android release keystore with a validity of 10,000 days and alias player-release. Generate it locally on the user's Android device; keep a separate offline backup. Never commit the private keystore or passwords. Store the base64 keystore and passwords only in GitHub Actions repository secrets. CI signs a release APK with the same key on every main-branch build and verifies its certificate; debug APKs are labelled CI-only. Use the monotonically increasing GitHub Actions run number as versionCode.  
**Identity constraint:** Keep applicationId=com.batmancpu.player unchanged after the first signed install. The visible app label/brand can be renamed without changing application identity.  
**Reason:** Android checks the signing certificate when installing an update; the stable certificate and a higher versionCode avoid the repeated uninstall/reinstall cycle.
### D-020 — Pre-release signing key reset explicitly approved by user (2026-11-14)
**Status:** Accepted. The user directly requested a completely NEW signing key because no signed Player release existed. This supersedes D-019's previously generated key identity and all older “never rotate” directions that referred to that old key. The new key was generated and validated in the current ChatGPT session, and private files were delivered through that conversation only. Current certificate SHA-256: `4D:2F:17:89:ED:27:D3:96:13:35:FE:36:45:64:55:BD:9A:31:0B:0D:CD:F4:1E:8C:CC:92:53:98:33:9E:80:BF`; alias `player-release`; JKS/RSA-3072. The repo CI certificate verification and `docs/ANDROID_SIGNING.md` now use this public fingerprint. No secret/password/Base64 was committed. DO NOT regenerate again after the first signed APK installation, because update continuity then matters.

### D-021 — Original premium design with verified OPPO frame deadlines (2026-11-14)
**Accepted:** Apple public Liquid Glass HIG is design inspiration for **original** Compose UI, not a licence for proprietary Apple code, SF icons, fonts or assets. Functional floating chrome only; crisp poster/video content. Existing Haze 2.1.0 source-backed prototype can be promoted after on-handset checks. Target 60-Hz (~16.67ms) frame smoothness on Reno10 5G CPH2531/Mali-G68 MC4, 120 Hz as stretch, never guarantee without Macrobenchmark/Perfetto evidence. `docs/DESIGN_SYSTEM_AND_PERFORMANCE.md` is binding.

### D-022 — Research public GitHub upstream and reuse licensed libraries safely (2026-11-14)
**Accepted:** Codex may browse official docs and GitHub when enabled. Prefer pinned maintained libraries to blind copy; audit licence/NOTICE, source provenance, transitive permissions/network, security, thermal and APK impacts. Do not copy unlicensed material; retain attribution and document exact adapted code. `docs/UPSTREAM_AND_PRIVACY.md` is binding and preserves the one-local-SAF-tree/no-telemetry rule.

### D-023 — Per-change AGENTS context, Codex short-lived PR exception (2026-11-14)
**Accepted:** Every code/test/docs/config/dependency/workflow change, no matter its size, must add/refresh a dated concise `AGENTS.md` Recent agent changes entry, with full description in `DEVELOPMENT_LOG.md`. Main remains one release branch; Codex Cloud may create short-lived isolated task PRs for safe test/review but not long-lived branches or force pushes.


### D-024 — Main-only development and merged-branch cleanup (2026-11-14)
**Accepted by latest user instruction:** work directly on main in small tested commits; do not create task branches/PRs. Maximum one additional backup branch only if genuinely needed, not routine task isolation. This supersedes D-023’s task-PR exception while retaining mandatory AGENTS/development logging. All eight previous task branches were fully merged with zero branch-only commits; delete those refs safely, preserving main/history/releases. Temporary cleanup automation must be removed after verification.

D-024 execution verified: cleanup CI 43642157896 deleted all eight fully merged task refs; only main remains. Temporary workflow removed after verification. No optional backup branch was needed.

### D-025 — Larger useful offline preview after actual phone report (2026-11-15)
Latest user requests substantial usable progress and permits further independent upgrades rather than idle gating. Proceed with searchable real local catalogue/details and a basic foreground local-player preview plus Glass Lab/feedback fixes. Keep six-phase acceptance records explicit: this does not certify missing physical tests, TMDB, all codecs, premium Glass or later completed phases. Phase 1 remains offline/read-only; no network permission or metadata calls. FC Zenith/immediate Zenith upstream is rendering reference; GPL integration code is not copied. Media3 Apache-2.0 is the initial bounded local-playback engine candidate, with content-only data source and no extra permissions.

### D-026 — Continue next-phase implementation while user is unavailable (2026-11-15)
**Accepted by the user's 2026-11-15 instructions:** continue useful implementation and tests into next-phase work without pausing for routine APK-install checks. This supersedes interpreting phase phone gates as a ban on later independent implementation. The separately scheduled automation was later canceled at the user's request; the current direct instruction to “Continue” resumes this work, but does not authorize new scheduled tasks. Preserve unresolved device acceptance as evidence gaps; never fabricate phase completion/FPS or enable online integrations without the documented opt-in/credential/destination design. First publish the larger offline preview, then next-phase storage/schema/metadata contracts and feature slices. No new task branches, shared embedded key, signing changes, original writes or background uploads.


### D-027 — Preserve Room schema snapshots before migrations (2026-11-15)
**Engineering choice:** use Room KSP `room.schemaLocation` supplied by a Gradle `CommandLineArgumentProvider`, validate the exported baseline in CI, review and commit that exact generated JSON, and preserve all future version snapshots. Do not manually fabricate Room schema JSON or add metadata columns until version1 is captured.
**Reason/evidence:** Room migration tooling depends on historical schema definitions. The provider exposes the schema directory as a Gradle input for incremental/cached correctness. Official guidance: https://developer.android.com/training/data-storage/room/migrating-db-versions.html . No app runtime permissions, network dependency or user-data behavior changes. CI #92 attempt 2 (43728517965) passed both test/build jobs; its generated schema artifact was reviewed and committed verbatim at `app/schemas/com.batmancpu.player.ui.library.PlayerLibraryDatabase/1.json` (SHA-256 `820f2e814c9fcc3780be7c4bf6a1cbcb2498d52d1f92a0439b58aef11c9d6d04`).


### D-028 — Make metadata queries explicit, normalized and network-free by default (2026-11-15)
**Accepted for the current foundation slice:** build query terms only from recognized parsed movie/show titles, with optional valid year and typed media kind; default consent is disabled. Do not include season/episode numbers for series search. Reject path/URI-like values, file suffixes, controls, empty values and oversized titles. Keep the data model free of folder/file identities and media contents.
**Scope boundary:** this is a pure in-memory contract, not persisted user consent and not an HTTP/API integration. User-managed encrypted credential storage, precise destination allowlisting, settings UI, INTERNET permission, TMDB attribution/logo and online request tests are later prerequisites; none is added yet.
**Official sources:** movie search https://developer.themoviedb.org/reference/search-movie ; TV search https://developer.themoviedb.org/reference/search-tv ; FAQ/attribution https://developer.themoviedb.org/docs/faq . The official FAQ requires the notice “This product uses the TMDB API but is not endorsed or certified by TMDB.” and an approved logo in About/Credits before API/data use. CI #94 / 43731972463 passed the seven JVM query-contract tests, 15 Android 15 integration tests and signing/privacy gates. Offline query verification is complete; credential and destination review is next.


### D-029 — Store manual matches separately with an explicit Room migration (2026-11-15)
**Engineering choice:** keep user-confirmed TMDB overrides in a separate table keyed by `(treeUri, documentId)`, storing only TMDB ID, media type and local confirmation time. Preserve the v1 `indexed_media` schema. Add an explicit v1→v2 migration and test that old library rows survive.
**Cleanup:** update overrides only inside Room transactions that follow successful scan reconciliation, selected-tree activation or explicit tree forgetting. Do not rely on a cascading foreign key: the current index uses `INSERT OR REPLACE`, which could otherwise erase overrides for unchanged files during rescans.
**Scope:** offline storage foundation only. No TMDB HTTP request, user matching UI, settings, new permission, source-media write or destructive migration. Official migration testing guide: https://developer.android.com/training/data-storage/room/migrating-db-versions.html .

**Test harness/export verification:** CI #95 showed Room's MigrationTestHelper crashing during schema deserialization before SQL. The replacement instrumentation test constructs a v1 SQLite database from the exact committed Room JSON and opens it with the production migration. CI #97 / 43737778787 passed 17 Android 15 tests, including preserved v1 row and v2 DAO operations; forced KSP execution produced both schemas and the validator passed. v1 remains byte-identical; generated v2 SHA-256 is `6d2a1dd422834f5cadf3e6c4292aed393bf2a784adc3cef1108ba9975c0bd4fe` and is preserved in the repository. No runtime networking or permissions were added.


### D-030 — Explicitly exclude app data from configured Android backup modes (2026-11-15)
**Accepted:** Keep `android:allowBackup="false"`; also attach `fullBackupContent` rules excluding all nine supported app-data domains on Android 11 and lower, and `dataExtractionRules` excluding those domains from both cloud backup and device-to-device transfer on Android 12+. Test the source XML, manifest wiring and packaged APK attributes. Do not add `include` rules.
**Reason:** Android's official Auto Backup documentation warns that some Android 12+ OEMs may honor `allowBackup=false` for cloud only, not device transfer, and an omitted data-extraction mode defaults to including app data. Reference: https://developer.android.com/identity/data/autobackup .
**Limit:** Android 16 QPR2/API 36.1 cross-platform transfer requires a real iOS counterpart and its bundle ID, Team ID and content version. Player has no such app; do not invent identifiers or claim this mode is excluded. Revisit if a real iOS target is approved. CI #99 / 43740997631 passed Android 15 17/17, source/XML regressions, compiled-manifest references, signed identity/privacy and permanent-certificate checks. The signed versionCode99 artifact is CI-only and was not released. Runtime backup behavior remains untested on an OEM handset; no credential/network work is authorized by this decision.


### D-031 — One integrated user test APK, not intermediate releases (2026-11-21)
**Accepted:** Continue the six engineering phases with automated CI, without routine user phone-install gates. Do not upload intermediate debug APK artifacts or sign a candidate for ordinary commits. When the complete integrated app is ready, create one signed GitHub Release APK and provide a full test checklist. A final verified candidate may then receive higher-version fixes from the user's report.
**Reason:** User wants to conserve testing effort and install only after the whole polished app is ready. CI may still compile internal debug/test APKs to execute Android instrumentation; these are not distributed as user test builds.
**Constraints:** Preserve the existing app ID, permanent certificate, strictly increasing versionCode, one-folder SAF/privacy behavior and all design/performance acceptance rules. Never state phone performance/acceptance without OPPO evidence.


### D-032 — Keep provider metadata separate from local document identity (Accepted)
Provider metadata is cached by TMDB ID/media type and joined to a SAF document only after an explicit user confirmation. Automatic matching never mutates an override. Full successful rescans clean stale document bindings and unreferenced provider rows transactionally. This design adds no network or credential permission by itself; it makes no metadata visible until the explicit match exists.


### D-033 — Forgetting a library never deletes source media (Accepted)
“Remove folder from Player” clears Player's app-private index and resume checkpoints and revokes its persisted SAF permission only after explicit confirmation. It must not delete, rename or modify source videos/subtitles. Retry is available if app-private cleanup or grant reconciliation fails. Grant cleanup continues if the UI lifecycle is cancelled.


### D-034 — Store TMDB credentials locally, keep request permission separate (2026-11-21)
**Engineering choice:** an optional user-owned TMDB API Read Access Token is AES-GCM encrypted using an Android Keystore key and stored only under app noBackupFilesDir. Persisted consent defaults off; a setting or saved token alone never initiates a request. Clear/disable revoke in-process use, and clear is non-cancellable once explicitly invoked. No token is included in Activity saved state or UI state.

**Evidence:** Android 15 instrumentation tests cover fresh default-off state/key absence, encryption/reopen, opt-in, cross-instance disable/clear, tampered ciphertext/key loss, malformed/oversized tokens, nonce uniqueness and concurrent revocation. CI #108 / 44893247270 passed 30/30 Android instrumentation plus unit/build/schema/privacy checks. The app still has no INTERNET permission or TMDB client/request, and no installable APK was produced.

**Next gate:** finish explicit one-title search/manual match and About/Credits approved logo plus exact notice; audit fixed API/CDN hosts, TLS, cancellation, response/size bounds and opt-out rechecks before adding INTERNET. Preserve offline playback/library when metadata is off.


### D-035 — Show official TMDB credit before using provider data (2026-11-21)
**Engineering choice:** provide a dedicated About/Credits page before the first TMDB request. Use an official approved TMDB asset without changing aspect ratio/color and include the exact non-endorsement notice. Keep Player's identity more prominent. External navigation is user-tapped only.

**Evidence:** official TMDB FAQ requires an approved logo, source notice and placement in About/Credits: https://developer.themoviedb.org/docs/faq. The official blue gradient logo linked from TMDB branding was retrieved; SVG SHA-256 d537fb228cf3ded904ef09b136fe3fec72548ebc1fea3fbbd1ad9e36364db38b, PNG conversion SHA-256 80ed9c889db93fdb384b3cc208d59e5697b2a5015a95ff7416e191c83248094b. UI tests added; CI #110 pending. No metadata fetch or INTERNET permission is enabled by this decision.


### D-035 implementation verification addendum — 2026-11-21
The first Credits UI build failed in CI #110 on a Kotlin string-escaping typo and wrong foundation import; both are corrected on main, with CI #111/#112 pending. No app permission/network behavior changed and no installable APK was created.
