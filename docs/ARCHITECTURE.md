## Status update — 2026-11-21

The Compose `Slider` transport around Media3's content-only `PlayerView` compiles; CI #216/#218 passed all nine local playback tests. Position updates at 4 Hz only while visible, and scrubbing suppresses auto-hide. The library Manage sheet starts expanded so its longer contents are reachable. CI #216/#217 exposed a hidden toggle on reopening; CI #218 exposed the test starting gestures before the sheet entrance completed. The test now waits for the visible heading before scrolling; pending verification. No target-device performance evidence.

The offline catalogue, local playback, production navigation dock and cached-title details are integrated. Artwork network access is restricted to the approved image CDN after the user separately confirms a TMDB match; detail-screen cache lookup remains disk-only and the offline gradient remains available. CI #214 passed 51/51 Android 15 tests plus unit/build/schema/privacy gates for `4aa127595ec164dbfdd9ba71d2a2e08169ca27eb`. The current unverified batch replaces native PlayerView chrome with Compose seek/play controls; see the transport details below. Synthetic emulator evidence does not establish OPPO performance or acceptance.

Phase 2 now includes default-off encrypted TMDB settings, a fixed-origin bounded HTTPS client, manual detail-sheet search/candidate selection/separate confirmation, and offline Room metadata cache. CI #115 / 44896086296 passed the Android 15 UI/client suite, unit tests, build, schema and privacy gates; no network call was made by CI. Artwork cache/display and OPPO network/device behavior remain unverified. The app's INTERNET capability is used only by this opt-in manual flow; scanning and playback remain local.

# Architecture

Status (2026-11-15): the one-tree offline SAF/Room library passed automated tests; user confirmed v0.1.79 behavior on OPPO. The newer catalogue/details/local Media3 preview in signed v0.1.91 remains untested on OPPO. Room v1/v2 snapshots and manual-match migration are committed; CI #99 / 43740997631 passed 17 Android 15 integration tests, schema/build/manifest/signing/certificate gates and all Python regressions. Explicit legacy Android and Android 12+ cloud/device-transfer exclusions cover all app-data domains; H-023 is resolved for configured Android modes. Its versionCode99 artifact is CI-only. No HTTP client or network permission is implemented. H-021/H-022/H-023 are resolved. Package identity and permanent signing remain unchanged.

## Product boundary
One Android application, one user-selected local folder, recursive read-only scan, TMDB metadata/artwork, offline local library. No media server, upload, streaming, account, cloud drive, ads, telemetry or analytics.

## Runtime layers
- Presentation: Kotlin + Jetpack Compose; screens for folder onboarding, library, movie details, series/seasons/episodes, local search, manual match, settings, credits and local playback. ViewModels expose immutable state. UI never calls ContentResolver, Room, TMDB or subtitle providers directly.
- Domain: LibraryFolder, MediaFile, ParsedMediaName, MediaMatch, Movie, Series, Season, Episode and use cases for selection, scan, parsing, matching, correction and refresh.
- Data: SAF folder source, TMDB client, reviewed subtitle-provider adapter, Room database, app-private artwork/subtitle cache, DataStore for non-secret preferences and Keystore-backed credential storage.
- Platform: Activity Result API, ACTION_OPEN_DOCUMENT_TREE, persisted read-only URI grant, ContentResolver and DocumentsContract. No JNI/NDK in v1.

## Data flow
1. User selects one local folder.
2. Validate provider authority and persist read grant.
3. Enumerate descendants of that tree only.
4. Store document identity as provider authority + document ID; keep URI locally for access.
5. Normalize filenames and parse title/year/type/season/episode.
6. Send only normalized title/year/type to TMDB.
7. Rank candidates; require confirmation for ambiguity; preserve manual overrides.
8. Fetch details and artwork path; download artwork directly from TMDB CDN.
9. Persist metadata and artwork locally; render from Room/cache.
10. Offline mode uses cached records and artwork only.

## Storage boundary
The selected SAF tree URI is the only scan root. Never use raw filesystem paths as stable identity. Use provider allowlisting and default-deny unknown authorities; verify OPPO local provider support on-device. Reject known cloud, WebDAV, SMB and network sources. A content URI alone does not prove local storage. Android 11+ has folder-picker restrictions; explain unsupported roots instead of requesting broad storage access.

## Offline metadata query contract

`TmdbSearchTerms` is a pure value model containing only a normalized title, optional valid year and typed movie/TV kind. It is created only from recognized parser output and defaults to disabled unless the caller supplies explicit user opt-in. Series lookups use the show title without season/episode numbers. Path/URI/file-suffix/control/oversized inputs are rejected. This type has no network side effects and no local file or SAF identity fields; persistent user consent and encrypted credential settings are implemented with Android Keystore and no-backup app-private storage; HTTP and `INTERNET` remain deliberately absent until the manual request/match/attribution flow is ready.

## Network boundary
Allow only HTTPS TMDB API and approved TMDB image CDN hosts. Permitted fields: normalized title, optional year, media type and language. Never send source URI, folder path/name, full filename, directory listing or video bytes. Validate returned image hosts. No custom proxy or backend.

The no-backend design means a private shared API credential cannot be kept secret inside a public APK. Use the optional user-managed Read Access Token protected with Android Keystore-backed encryption (implemented in MetadataSettingsStore); cached library remains available without it. Consent defaults off and changing the setting alone makes no request. Subtitle lookup is a separate provider decision: do not silently add a host, account, API key or SDK. Search only from normalized title/year/content identifiers, and download subtitle files directly to app-private storage.

## Identity and persistence
- Folder: tree URI, validated authority, display label, grant status and scan time.
- File: provider authority + document ID + folder ID; MIME, size and modified date.
- Content: TMDB ID + media type.
- Episode: parent series ID + season + episode; local file binding stored separately.
- Match override: file identity -> confirmed TMDB ID/type; never overwritten by a later guess.
- Artwork cache: bounded and local; never cache video files.
Use explicit Room migrations; never destructive-migrate user data in release builds.

## Room schema history

The exact Room v1 KSP snapshot remains committed at `app/schemas/com.batmancpu.player.ui.library.PlayerLibraryDatabase/1.json`. The generated v2 snapshot is preserved at `app/schemas/com.batmancpu.player.ui.library.PlayerLibraryDatabase/2.json`, SHA-256 `6d2a1dd422834f5cadf3e6c4292aed393bf2a784adc3cef1108ba9975c0bd4fe`. It declares database version 2, retains `indexed_media` unchanged and adds only `manual_tmdb_matches` keyed by `(treeUri, documentId)`. CI #97's validator confirmed both schemas, and Android instrumentation exercised the production migration/data preservation. The installed v0.1.91 release still uses v1. No destructive fallback is allowed.

Room schema JSON is test/build evidence only and contains no user library rows. Keep v1 byte-identical; both exact snapshots are committed. CI reruns KSP before schema validation so side-effect schema files are recreated after a cache hit.
## Scanning correctness
- Whitelist video MIME types/extensions; ignore hidden/system/unsupported files.
- Handle null size/date and provider errors.
- Bound recursion and request concurrency; cancellation must be prompt.
- Deduplicate by authority + document ID.
- Do not mark files missing after failed/incomplete scan. Reconcile missing files only after a completed scan of an accessible folder.
- Do not overwrite manual TMDB matching with later guesses.
- Match a series once; do not search TMDB separately for every episode.

## Local playback architecture

Playback is limited to local content URIs obtained from the one persisted SAF tree. The player opens the selected document through Android's content resolver; it must not copy or upload the video. Keep the playback engine behind a small interface so the engine can be changed without rewriting the catalogue or UI.

Evaluate AndroidX Media3 first against a representative local-file corpus. Its supported container list is not a guarantee that every embedded codec is decodable: actual support depends on the selected decoder and device. Evaluate LibVLC only if the Media3/device matrix leaves material format gaps; account for its native ABI/APK footprint, hardware-decoding behaviour, GPL/LGPL obligations and target-device stability before adoption. Do not promise literal support for every file format.

Playback owns an explicit immersive system-bar toggle; system bars are hidden only while a local target is active in immersive mode and restored on exit/disposal. The title/track Compose overlay follows Media3 controller visibility and also owns a bounded 2.5-second timeout while actively playing; the timeout explicitly hides both controller and header, pauses retain controls, and a reveal, seek, track change, resize-mode selection or fullscreen action restarts the timer.

The player must expose embedded audio and subtitle tracks. It must also support external subtitle files where the selected engine supports them, including common formats such as SRT, WebVTT, TTML and SSA/ASS. Subtitle search/download provider and credentials remain unselected until API terms, privacy, account requirements and availability are audited. Downloaded subtitle files default to app-private storage and are associated with the local media identity; never modify the original media folder by default.

Track selection and subtitle delay are functional requirements. The current player uses one compact Compose bottom sheet for decoder-supported audio and subtitle options, with Automatic restore and Subtitles off states; audio/subtitle details come from Media3 track metadata. The synthetic Android test checks accessible selection and state changes for a local SRT sidecar. External subtitle search/download remains a separately reviewed feature; fine-grained subtitle offset with reset stays deferred until Media3 provides a stable offset API. Keep video content unobstructed and do not add clutter.

**2026-11-22 implementation note:** The player track-picker replacement is currently uncommitted and awaits CI. `PlayerTrackSelectionSheet` reads supported [`Tracks.Group`](https://developer.android.com/reference/androidx/media3/common/Tracks.Group) values, displays labels/languages and channel count, and applies a selected Media3 override or the explicit default/disabled policy using the official [`TrackSelectionParameters.Builder`](https://developer.android.com/reference/kotlin/androidx/media3/common/TrackSelectionParameters.Builder). It does not inspect media bytes outside Media3 or write subtitle files.

**Media3 offset API check (2026-11-22):** The project pins Media3 1.11.1 stable. Android's release page lists 1.12.0-beta01 as the current beta; `SubtitleConfiguration.Builder.setTimeOffsetUs` is marked `@UnstableApi` and documented in that beta release line. Do not expose a nonfunctional delay control or move production playback to a beta solely for offset support. Recheck stable release notes/API before implementation; keep the offset and reset controls as explicit backlog requirements.

## Subtitle retrieval and privacy boundary

The local scanner may identify sidecar subtitle files in the selected tree and pair them locally with a movie/episode. Embedded tracks are read by the playback engine. Online subtitle discovery is a separate provider integration, not part of TMDB. The provider must be selected only after API terms, credentials, language coverage, rate limits and privacy are reviewed. Requests may include normalized title/year/season/episode identifiers only; never include the full filename, SAF URI, directory name or file contents. Save downloaded subtitle assets in Player's private storage by default.

## Glass architecture
Start with a plain Compose build. Upgrade toolchain and add Haze 2.1.0 in isolated commits. Register a bounded backdrop source and put Glass only on navigation/controls/sheets. Use reusable RegularGlassSurface, ClearGlassSurface, GlassIconButton, GlassPill and GlassSheet. Use glass containers only for small groups that visibly merge. Posters remain crisp. Provide opaque fallback for reduced transparency/unsupported rendering; avoid continuous pixel readback for adaptive contrast. Lens/refraction is optional and must pass separate device testing. No native backdrop backend in v1.

## Quality gates
Unit tests and FOSS/debug CI build are required at each milestone. Real-device validation is separate. CI does not prove OPPO GPU stability or 60-Hz smoothness.
## Binding premium design and privacy details (2026-11-14)
- `docs/DESIGN_SYSTEM_AND_PERFORMANCE.md`: official OPPO hardware, cinematic design tokens, per-screen functional Glass surfaces, true accessible slider semantics, 60-Hz/optional-120-Hz benchmarks and opaque/reduced-motion fallbacks.
- `docs/UPSTREAM_AND_PRIVACY.md`: vetted GitHub repository/source reuse/licence obligations, malicious upstream instruction protections and minimum external network/data-egress policy.
- Current app manifest retains allowBackup=false and explicit legacy plus Android 12+ cloud/device-transfer exclusion resources. The only added runtime capability is INTERNET for the explicit opt-in TMDB flow; no broad storage/media permission exists. CI validates the permission allowlist and configured backup modes. Scanning and playback stay local/offline-capable. Android 16 QPR2 cross-platform transfer is not configured: no iOS Player counterpart or genuine identifiers exist.


## Phase 1 lifecycle implementation — 2026-11-14
MainActivity retains LibraryViewModel using its ViewModelStore. The ViewModel holds application context and a pure LibrarySession whose immutable StateFlow is observed by FolderSetupScreen. Main-thread commands serialize cache activation/load, scan and save through a mutex; cancellation generations reject obsolete results/errors/finalizers. Restore finishes before scanning; reattaching the same folder preserves running work. Tree activation atomically retires other cached trees before reading current rows. Folder-picker grant/preferences orchestration is still screen-owned pending H-010; query cancellation/bounds and actual Room/lifecycle device tests remain separate gaps.

## Phase 1 grant and provider hardening — 2026-11-14
FolderAccessManager plus AndroidFolderAccessStore now own serialized restore/connect, durable selection, root directory probe, orphan-grant cleanup and removal of persisted write grants. Retained ViewModel owns picker completion and guards stale requests. SAF query helper binds CancellationSignal, scopes cursor lifetime, and checks cancellation per row; traversal budgets cap rows/directories/depth/width. Invalid/incomplete/out-of-tree results fail before Room reconciliation. Android 15 emulator tests exercise Room/cursor helper/manifest and launcher recreation; actual OPPO DocumentsProvider and frame performance remain unverified.

## Unreleased Phase 1 main-safe data entry points — 2026-11-15
SafLocalMediaSource.scan and OfflineLibraryIndex public entry points own IO dispatch for their entire operation, including permission preflight, traversal and filename/row mapping. Suspend DAO/query calls alone do not move subsequent work off their caller context. Mapping checks cancellation per record; Room reconciliation remains atomic, and activation mapping stays inside the Room transaction context. No database schema or source-storage write change. CI 43667022100 passed functional/build/signing gates for 57e4539; actual device timing remains unmeasured, no FPS inference.

## Offline catalogue/player preview — 2026-11-15
LocalCatalogue derives video-only movie/series identities from indexed files; ViewModel computes on Default and publishes tree-bound snapshots. LocalLibraryContent is a stateless searchable/adaptive card and detail-sheet view for UI verification. Playback requests must match the current indexed video and selected root; LocalPlaybackAccess checks persisted read access and actual descendant membership off-main before constructing a content URI. Media3 is configured with ContentDataSource only, fixed positive local-track bandwidth estimate (no network observer) and platform diagnostics disabled. Foreground lifecycle pauses/releases playback, audio focus/headphone-unplug handling stays local, and app-private hashed root/document resume positions checkpoint off-main. No service, HTTP media source or source-file writes. Media3 upstream ACCESS_NETWORK_STATE/WAKE_LOCK declarations are explicitly removed; release manifest gate remains deny-by-default. A synthetic H.264/AAC fixture/provider and screenshots live only in debug/test builds. Phase-specific format/fullscreen/track/subtitle hardening remains later work.


## Folder removal
The library settings action first confirms the user's intent, then deletes Player's app-private index/cache and resume checkpoints, then clears/reconciles persisted read-only SAF grants under FolderAccessManager's mutex. Grant reconciliation is non-cancellable after confirmation. It never invokes a DocumentsContract delete/write operation. On failure the setup screen exposes a retry state. CI #104 passed manager/build/schema gates; CI #105 covers resume cleanup and confirm/cancel UI.


## Confirmed artwork cache — 2026-11-21
Artwork uses a fixed unauthenticated TMDB image CDN transport, called only after explicit match confirmation. HTTPS/no redirects, w342 route, MIME/2 MiB/2048-pixel/timeout bounds, cancellation and AtomicFile cache under no-backup storage with 96 MiB LRU. UI state exposes only the local cached File; Coil Compose has no network module. Details read existing art offline. Consent/token rechecked around download/write. Synthetic tests inject fake transports; handset rendering/battery unmeasured.


## Catalogue artwork rendering — 2026-11-21
Library cards first map their indexed document IDs to a single user-confirmed offline metadata row. Only visible lazy cards read the corresponding app-private poster File; Coil decodes that local File with a dark text-contrast scrim. Unmatched/ambiguous/missing artwork retains a local deterministic gradient/glyph fallback. The card path cannot call the network. A poster-state key refreshes after a confirmed download or removal; cached files remain browsable offline.


### Subtitle viewport and video resizing

Player exposes Fit, Fill and Zoom resizing through Media3. The app overrides the supported `exo_player_view.xml` layout so `SubtitleView` is a sibling of `exo_content_frame`, rather than a child inside the aspect-ratio frame. This keeps subtitle size/placement tied to the player viewport while the video content is resized or cropped. The layout is a small Apache-2.0 adaptation of Media3's layout; its source and upstream URL are identified in the XML header. Android instrumentation asserts the caption and video frames remain siblings under Zoom. Media3 source references: [PlayerView layout contract](https://github.com/androidx/media/blob/release/libraries/ui/src/main/res/layout/exo_player_view.xml), [SubtitleView API](https://developer.android.com/reference/androidx/media3/ui/SubtitleView). Recheck the override whenever the pinned Media3 version changes.


**Playback engine selection:** Media3 remains the primary local engine. Its supported-container list does not guarantee device codec support. Do not add LibVLC until the target local-file corpus shows material gaps; compare pinned artifact size/ABI, hardware decode, subtitle overlay behavior, memory, thermal, battery and license obligations. See DEC-021.

## 2026-11-21 — Phase 4 completion reopen edge case
A completed local video now saves resume position 0 and play intent false when Media3 reports STATE_ENDED, and reveals the player controls. The CI-owned synthetic-video Android test asserts the completion checkpoint. CI pending; continue with subtitle controls and full player interaction coverage after green verification.

## 2026-11-21 — Stable reset state across lifecycle saves
The playback checkpoint helper now normalizes STATE_ENDED to zero/paused for timer, background and disposal paths; remembered play intent is cleared too. The new Android test spans the five-second periodic interval and checks that the result stays zero. CI pending.

## 2026-11-21 — Synthetic playback test timing
The previous CI attempt timed out because the synthetic fixture lasts 30 seconds. The regression test now begins at 29 seconds, then waits beyond the checkpoint timer. CI pending.

## 2026-11-21 — Player completion checkpoint CI evidence
CI #182 / 44949435610 passed the 48-case Android 15 instrumentation suite, unit tests, application build, and Room v1-v4 schema checks for the end-of-playback checkpoint fix. CI #180/#181 are retained in the failure history: the test originally waited 20 seconds for a 30-second fixture, and #181's emulator also displayed a Pixel Launcher ANR. Physical OPPO playback and performance remain unverified.

## 2026-11-21 — Local catalogue destination behavior
The Compose presentation layer exposes Home, Movies, Shows and Search over the local Room catalogue. Movies/Shows filter indexed titles, Search focuses local title search, and Home preserves the grouped shelves. The production library scaffold now uses one bounded Haze dock over a registered content source; the user can persistently disable it and use the opaque fallback, which also detaches the Haze source. It does not blur posters as independent surfaces or the video layer. CI #203 verified navigation before this Glass integration; CI #205 caught a missing semantics import before tests; the import is fixed and the full dock/toggle test rerun is pending. Physical Reno10 rendering/performance remains unverified.

## 2026-11-21 — Navigation test lookup adjustment
The CI #184 run built successfully and executed 49 Android tests; its only failure was the selected-state test choosing the Movies shelf heading before the tab. The filter itself passed. Test helper now searches all matches/ancestors; validation pending.
