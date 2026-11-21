## Status update — 2026-11-21

Phase 2 persistence groundwork adds a provider metadata table (Room v3) separate from SAF identities, with user-confirmed joins. CI #102 / 44888106152 passed Android 15 instrumentation 20/20, production v1-to-v3 migration, cache reconciliation and exact schema validation; generated v3 JSON is committed. No network transport or INTERNET permission exists in this slice; the actual OPPO candidate/performance remains unverified.

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

`TmdbSearchTerms` is a pure value model containing only a normalized title, optional valid year and typed movie/TV kind. It is created only from recognized parser output and defaults to disabled unless the caller supplies explicit user opt-in. Series lookups use the show title without season/episode numbers. Path/URI/file-suffix/control/oversized inputs are rejected. This type has no network side effects and no local file or SAF identity fields; persistent preferences, credential storage, HTTP and `INTERNET` are not yet implemented.

## Network boundary
Allow only HTTPS TMDB API and approved TMDB image CDN hosts. Permitted fields: normalized title, optional year, media type and language. Never send source URI, folder path/name, full filename, directory listing or video bytes. Validate returned image hosts. No custom proxy or backend.

The no-backend design means a private shared API credential cannot be kept secret inside a public APK. Use a user-managed credential protected with Android Keystore-backed encryption; cached library remains available without it. Subtitle lookup is a separate provider decision: do not silently add a host, account, API key or SDK. Search only from normalized title/year/content identifiers, and download subtitle files directly to app-private storage.

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

The player must expose embedded audio and subtitle tracks. It must also support external subtitle files where the selected engine supports them, including common formats such as SRT, WebVTT, TTML and SSA/ASS. Subtitle search/download provider and credentials remain unselected until API terms, privacy, account requirements and availability are audited. Downloaded subtitle files default to app-private storage and are associated with the local media identity; never modify the original media folder by default.

Track selection and subtitle delay are functional requirements. The initial playback engine milestone needs only the controls required to start, pause, seek and resume a local file, plus track discovery/selection. A later UI milestone adds a compact audio/subtitle sheet, external subtitle search/download, subtitle visibility and fine-grained subtitle offset with reset. Keep video content unobstructed and do not add clutter.

## Subtitle retrieval and privacy boundary

The local scanner may identify sidecar subtitle files in the selected tree and pair them locally with a movie/episode. Embedded tracks are read by the playback engine. Online subtitle discovery is a separate provider integration, not part of TMDB. The provider must be selected only after API terms, credentials, language coverage, rate limits and privacy are reviewed. Requests may include normalized title/year/season/episode identifiers only; never include the full filename, SAF URI, directory name or file contents. Save downloaded subtitle assets in Player's private storage by default.

## Glass architecture
Start with a plain Compose build. Upgrade toolchain and add Haze 2.1.0 in isolated commits. Register a bounded backdrop source and put Glass only on navigation/controls/sheets. Use reusable RegularGlassSurface, ClearGlassSurface, GlassIconButton, GlassPill and GlassSheet. Use glass containers only for small groups that visibly merge. Posters remain crisp. Provide opaque fallback for reduced transparency/unsupported rendering; avoid continuous pixel readback for adaptive contrast. Lens/refraction is optional and must pass separate device testing. No native backdrop backend in v1.

## Quality gates
Unit tests and FOSS/debug CI build are required at each milestone. Real-device validation is separate. CI does not prove OPPO GPU stability or 60-Hz smoothness.
## Binding premium design and privacy details (2026-11-14)
- `docs/DESIGN_SYSTEM_AND_PERFORMANCE.md`: official OPPO hardware, cinematic design tokens, per-screen functional Glass surfaces, true accessible slider semantics, 60-Hz/optional-120-Hz benchmarks and opaque/reduced-motion fallbacks.
- `docs/UPSTREAM_AND_PRIVACY.md`: vetted GitHub repository/source reuse/licence obligations, malicious upstream instruction protections and minimum external network/data-egress policy.
- Current `app/src/main/AndroidManifest.xml` retains `android:allowBackup="false"` and references explicit legacy plus Android 12+ cloud/device-transfer exclusion resources. CI #99 passed source/XML and compiled-manifest verification for the configured Android backup modes. Phase 1 requires no INTERNET permission. Android 16 QPR2 cross-platform transfer is not configured: there is no iOS Player counterpart or real identifiers.


## Phase 1 lifecycle implementation — 2026-11-14
MainActivity retains LibraryViewModel using its ViewModelStore. The ViewModel holds application context and a pure LibrarySession whose immutable StateFlow is observed by FolderSetupScreen. Main-thread commands serialize cache activation/load, scan and save through a mutex; cancellation generations reject obsolete results/errors/finalizers. Restore finishes before scanning; reattaching the same folder preserves running work. Tree activation atomically retires other cached trees before reading current rows. Folder-picker grant/preferences orchestration is still screen-owned pending H-010; query cancellation/bounds and actual Room/lifecycle device tests remain separate gaps.

## Phase 1 grant and provider hardening — 2026-11-14
FolderAccessManager plus AndroidFolderAccessStore now own serialized restore/connect, durable selection, root directory probe, orphan-grant cleanup and removal of persisted write grants. Retained ViewModel owns picker completion and guards stale requests. SAF query helper binds CancellationSignal, scopes cursor lifetime, and checks cancellation per row; traversal budgets cap rows/directories/depth/width. Invalid/incomplete/out-of-tree results fail before Room reconciliation. Android 15 emulator tests exercise Room/cursor helper/manifest and launcher recreation; actual OPPO DocumentsProvider and frame performance remain unverified.

## Unreleased Phase 1 main-safe data entry points — 2026-11-15
SafLocalMediaSource.scan and OfflineLibraryIndex public entry points own IO dispatch for their entire operation, including permission preflight, traversal and filename/row mapping. Suspend DAO/query calls alone do not move subsequent work off their caller context. Mapping checks cancellation per record; Room reconciliation remains atomic, and activation mapping stays inside the Room transaction context. No database schema or source-storage write change. CI 43667022100 passed functional/build/signing gates for 57e4539; actual device timing remains unmeasured, no FPS inference.

## Offline catalogue/player preview — 2026-11-15
LocalCatalogue derives video-only movie/series identities from indexed files; ViewModel computes on Default and publishes tree-bound snapshots. LocalLibraryContent is a stateless searchable/adaptive card and detail-sheet view for UI verification. Playback requests must match the current indexed video and selected root; LocalPlaybackAccess checks persisted read access and actual descendant membership off-main before constructing a content URI. Media3 is configured with ContentDataSource only, fixed positive local-track bandwidth estimate (no network observer) and platform diagnostics disabled. Foreground lifecycle pauses/releases playback, audio focus/headphone-unplug handling stays local, and app-private hashed root/document resume positions checkpoint off-main. No service, HTTP media source or source-file writes. Media3 upstream ACCESS_NETWORK_STATE/WAKE_LOCK declarations are explicitly removed; release manifest gate remains deny-by-default. A synthetic H.264/AAC fixture/provider and screenshots live only in debug/test builds. Phase-specific format/fullscreen/track/subtitle hardening remains later work.


## Folder removal
The library settings action first confirms the user's intent, then deletes Player's app-private index/cache for the selected tree and clears/reconciles persisted read-only SAF grants under FolderAccessManager's mutex. It never invokes a DocumentsContract delete/write operation. On failure the setup screen exposes a retry state; CI #104 is verifying the new flow.
