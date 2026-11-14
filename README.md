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
- The primary goal is the library and its presentation. Playback is outside the first milestone unless separately approved.

## Current status

This repository now contains a **buildable Android/Compose foundation** with a shared cinematic theme, a small licensed icon registry, and a verified GitHub Actions APK pipeline. Local-folder scanning, TMDB metadata, offline library persistence and Liquid Glass remain planned implementation work. Read [AGENTS.md](AGENTS.md) and [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md) before changing anything.

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

## Design references

- [Apple Human Interface Guidelines: Materials](https://developer.apple.com/design/human-interface-guidelines/materials)
- [Haze 2.1.0 release and Glass API](https://chrisbanes.me/posts/haze-2.1.0/)
- [FC Zenith LiquidGlass.kt](https://github.com/Batmancpu/FCZenith/blob/main/app/src/main/java/com/music/zenith/ui/components/LiquidGlass.kt)
- [TMDB developer documentation](https://developer.themoviedb.org/docs/getting-started)

## Licence

The implementation licence and all third-party licence obligations must be settled and documented before code is copied or distributed. See [DECISIONS.md](docs/DECISIONS.md).
