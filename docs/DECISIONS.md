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

### D-008 — Avoid native/JNI rendering in v1
No custom C/C++, NDK, JNI, native shader library or experimental native backdrop path. The user's CurrentLens history includes an OPPO CPH2531 Android 15 native SIGSEGV, although the exact root cause is unverified.

### D-009 — Native Android, not KMP, for v1
Prefer one Android app module using Kotlin and Jetpack Compose. Reuse libraries and patterns, not unnecessary multiplatform complexity.

### D-010 — Playback deferred
First release is a catalogue/scraper, not a player. Keep file records compatible with later Media3 playback but do not implement it in v1.

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
