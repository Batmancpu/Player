# Player

> **Working title only.** The app and repository may be renamed later. The current repository is `Batmancpu/Player`.

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

Phase 1 is actively under construction. The app includes Android/Compose, a saved one-folder SAF read grant, a cancellable read-only recursive SAF scanner, a simple movies/TV list, a deterministic local filename parser and an app-private Room index that survives restarts and reconciles only completed scans. The code passed GitHub Actions run #43 on 2026-11-14; real-device behavior on OPPO CPH2531 is not yet validated. TMDB, artwork, cinematic screens, local playback and subtitle controls are still unimplemented.

A **manual signed Release publication** workflow is prepared, but GitHub Actions signing secrets have not yet been configured, so The FIRST signed test APK is now published as [v0.1.51](https://github.com/Batmancpu/Player/releases/tag/v0.1.51). Never use the CI-only debug artifact for update continuity. For the plan, see [six-phase roadmap](docs/ROADMAP.md), [Work agent handoff](docs/WORK_HANDOFF.md), [release signing](docs/ANDROID_SIGNING.md) and [Phase 1 phone test](docs/PHASE1_PHONE_TEST.md).

## Canonical project records

- [AI agent operating rules](AGENTS.md)
- [Full product and conversation handoff](PROJECT_CONTEXT.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Architecture decisions](docs/DECISIONS.md)
- [Step-by-step roadmap](docs/ROADMAP.md)
- [Device test and crash playbook](docs/DEVICE_TESTING.md)
- [Development log](DEVELOPMENT_LOG.md)
- [Known issues and fixes](ISSUES_AND_FIXES.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)\n- [Android release signing and update continuity](docs/ANDROID_SIGNING.md)\n- [Chronological project conversation/session log](CHAT_HISTORY.md)\n- [Android release signing and update continuity](docs/ANDROID_SIGNING.md)

## Design references

- [Apple Human Interface Guidelines: Materials](https://developer.apple.com/design/human-interface-guidelines/materials)
- [Haze 2.1.0 release and Glass API](https://chrisbanes.me/posts/haze-2.1.0/)
- [FC Zenith LiquidGlass.kt](https://github.com/Batmancpu/FCZenith/blob/main/app/src/main/java/com/music/zenith/ui/components/LiquidGlass.kt)
- [TMDB developer documentation](https://developer.themoviedb.org/docs/getting-started)

## Licence

The implementation licence and all third-party licence obligations must be settled and documented before code is copied or distributed. See [DECISIONS.md](docs/DECISIONS.md).

## First Obtainium test APK (2026-11-14)
The signed first test release is [Player v0.1.51](https://github.com/Batmancpu/Player/releases/tag/v0.1.51), from successful CI run #51. Add this repository URL in Obtainium and install release v0.1.51. After successful first install, publish a higher-version signed test update and verify installation over the prior version; do not count Phase 1 complete until device acceptance tests pass.
