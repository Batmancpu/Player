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

This repository contains a **buildable Android/Compose foundation** with a shared cinematic theme, licensed icon registry, three theme regression tests, and a GitHub Actions build pipeline. The current launcher is the plain one-folder setup screen; the Liquid Glass Lab remains a developer preview. Folder selection and persisted read-grant recovery compile and pass CI, but the recursive scanner, TMDB metadata, offline catalogue, and playback are not implemented yet. The release-signing workflow is prepared, but the private keystore and four GitHub Actions secrets are not yet configured, so no signed Release APK exists. See [Android signing](docs/ANDROID_SIGNING.md), [AGENTS.md](AGENTS.md), and [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md) before changing anything.

## Canonical project records

- [AI agent operating rules](AGENTS.md)
- [Full product and conversation handoff](PROJECT_CONTEXT.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Architecture decisions](docs/DECISIONS.md)
- [Step-by-step roadmap](docs/ROADMAP.md)
- [Device test and crash playbook](docs/DEVICE_TESTING.md)
- [Development log](DEVELOPMENT_LOG.md)
- [Known issues and fixes](ISSUES_AND_FIXES.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)\n- [Android release signing and update continuity](docs/ANDROID_SIGNING.md)

## Design references

- [Apple Human Interface Guidelines: Materials](https://developer.apple.com/design/human-interface-guidelines/materials)
- [Haze 2.1.0 release and Glass API](https://chrisbanes.me/posts/haze-2.1.0/)
- [FC Zenith LiquidGlass.kt](https://github.com/Batmancpu/FCZenith/blob/main/app/src/main/java/com/music/zenith/ui/components/LiquidGlass.kt)
- [TMDB developer documentation](https://developer.themoviedb.org/docs/getting-started)

## Licence

The implementation licence and all third-party licence obligations must be settled and documented before code is copied or distributed. See [DECISIONS.md](docs/DECISIONS.md).
