# Player

> **Working title only.** The display name may change later; the release application ID and signing identity stay fixed. Repository: `Batmancpu/Player`.

A local-first Android media library inspired by the cinematic presentation of Apple TV and the refined Liquid Glass interface language of iOS 27.

## Product promise

Choose **one local folder**. Player scans only that folder and its descendants, identifies movies and television episodes, retrieves matching artwork and metadata directly from TMDB, caches everything locally, and presents the collection in a carefully crafted Apple-inspired interface.

- Media files remain on the device and are never uploaded to a Player server.
- No Player backend, media streaming server, account, subscription, advertisement, analytics, or telemetry.
- TMDB is contacted directly for metadata and artwork only. No video bytes, file paths, folder listings, or document URIs are sent.
- Previously indexed metadata and artwork remain available offline.
- The media library is read-only: never move, rename, edit, or delete the user's source files.
- Local playback is now in scope: tap a movie/episode to play its local file, with audio/subtitle track selection, external subtitle loading/download and subtitle-delay controls planned. Playback remains local-only.

## Current status

The latest signed preview is **[Player v0.1.91](https://github.com/Batmancpu/Player/releases/tag/v0.1.91)** (versionCode 91) and updates the existing app through Obtainium without uninstalling. APK SHA-256: `7c9ff8287405be9b1f607259608d84a256e5aed1fcc4cea24e3f9b882dd05e17`. CI #91 passed 15 Android 15 integration tests, JVM/build, signed APK privacy/identity and permanent-certificate checks.

The offline preview includes one read-only local SAF folder, Room persistence, searchable movie/show browsing, details and basic local Media3 playback with pause/seek/private resume, plus a single-surface Glass Lab dock. The user confirmed on v0.1.79 that four videos were correctly named/year-parsed, the folder persisted in airplane mode and folder switching worked. New preview behavior and remaining permission/cancellation scenarios still need OPPO testing; Phase 1 is not accepted and no FPS/thermal results are claimed. Phase 1 requires no network permission or online metadata. Follow the [short preview checklist](docs/OFFLINE_PREVIEW_TEST.md), [Phase 1 checklist](docs/PHASE1_PHONE_TEST.md), [six-phase roadmap](docs/ROADMAP.md) and [handoff](docs/WORK_HANDOFF.md). Never install a CI-only debug APK as an update. Development uses main only; see [AGENTS.md](AGENTS.md).

## Canonical project records

- [AI agent operating rules](AGENTS.md)
- [Full product and conversation handoff](PROJECT_CONTEXT.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Architecture decisions](docs/DECISIONS.md)
- [Step-by-step roadmap](docs/ROADMAP.md)
- [Device test and crash playbook](docs/DEVICE_TESTING.md)
- [Development log](DEVELOPMENT_LOG.md)
- [Known issues and fixes](ISSUES_AND_FIXES.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)
- [Android release signing and update continuity](docs/ANDROID_SIGNING.md)
- [Chronological project conversation/session log](CHAT_HISTORY.md)

## Design references

- [Apple Human Interface Guidelines: Materials](https://developer.apple.com/design/human-interface-guidelines/materials)
- [Haze 2.1.0 release and Glass API](https://chrisbanes.me/posts/haze-2.1.0/)
- [FC Zenith LiquidGlass.kt](https://github.com/Batmancpu/FCZenith/blob/main/app/src/main/java/com/music/zenith/ui/components/LiquidGlass.kt)
- [TMDB developer documentation](https://developer.themoviedb.org/docs/getting-started)

## Licence

The implementation licence and all third-party licence obligations must be settled and documented before code is copied or distributed. See [DECISIONS.md](docs/DECISIONS.md).

## First Obtainium test APK (2026-11-14)
The signed first test release is [Player v0.1.51](https://github.com/Batmancpu/Player/releases/tag/v0.1.51), from successful CI run #51. Add this repository URL in Obtainium and install release v0.1.51. After successful first install, publish a higher-version signed test update and verify installation over the prior version; do not count Phase 1 complete until device acceptance tests pass.

