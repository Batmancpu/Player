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