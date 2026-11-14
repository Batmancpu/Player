# Architecture

Status: partial implementation. The Android/Compose shell, theme, icon registry, Haze dependency/helper and isolated Glass Lab exist; the runtime media-library, storage, metadata, persistence and playback layers are not implemented.

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