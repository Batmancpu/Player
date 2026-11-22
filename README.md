# Player

<p align="center">
  <strong>Your library, on your terms.</strong><br>
  A private, local-first Android library and player for the films and shows you already own.
</p>

<p align="center">
  <a href="https://github.com/Batmancpu/Player/actions/workflows/ci.yml"><img alt="Android CI" src="https://github.com/Batmancpu/Player/actions/workflows/ci.yml/badge.svg?branch=main"></a>
  <img alt="Android" src="https://img.shields.io/badge/Android-29%2B-3DDC84?logo=android&logoColor=white">
  <img alt="Privacy-first" src="https://img.shields.io/badge/design-local%20first-77BDFB">
</p>

<p align="center">
  <img src="docs/images/player-visual-concept.webp" alt="Original visual-direction concept showing a cinematic local film library and separate video player controls on two generic phones" width="100%">
</p>

<p align="center"><sub>Original concept artwork for Player. It illustrates the visual direction and is not an in-app screenshot or a claim that every shown detail is implemented.</sub></p>

## What Player is

Player turns **one folder you choose** into a searchable movie and TV library, then plays those files locally. Its goal is a calm, cinematic catalogue with restrained glass controls, while keeping your media and library data on your device.

- **One selected folder.** Android’s Storage Access Framework grants read access to that folder and its descendants. Player does not request all-files access.
- **Offline library.** Filenames are organized into movies and episodes; the indexed library and confirmed details are stored locally so the collection remains useful offline.
- **Local playback.** Media3 plays the selected document directly. Device and codec support determines which formats can play. Sidecar subtitles are kept separate from the zoomable video frame.
- **Optional online metadata.** TMDB matching is user-controlled and manual. When enabled, a search sends only a normalized title, year and media type; no path, folder listing, SAF URI, video or subtitle content is sent.

<p align="center">
  <img src="docs/images/android15-catalogue-test.webp" alt="Player catalogue with synthetic movie titles on an Android 15 test emulator" width="320">
  &nbsp;&nbsp;
  <img src="docs/images/glass-lab-test.webp" alt="Experimental Glass Lab screen captured from the Android 15 test emulator" width="320">
</p>

<p align="center"><sub>Android 15 CI captures using synthetic test data. The Glass Lab is an experiment, not the finished library design; these images do not represent OPPO handset performance.</sub></p>

## Privacy by design

- Your video and subtitle files are never uploaded, renamed, moved, edited or deleted by Player.
- Access stays within the single folder tree you selected and is read-only.
- No Player account, media server, cloud library, ads, analytics or telemetry.
- App backup is disabled.
- Optional metadata and artwork use approved TMDB destinations only after you enable metadata features; the local library remains available without a connection.

See [storage and privacy rules](docs/UPSTREAM_AND_PRIVACY.md) and [architecture](docs/ARCHITECTURE.md).

## Current development status

The latest signed build is listed on the [Releases page](https://github.com/Batmancpu/Player/releases/latest). Existing signed installs keep the same package and permanent update certificate. These are development checkpoints; the integrated premium app is still in progress.

The user confirmed that v0.1.79 selected a folder, identified four local videos with titles and years, retained the library after an airplane-mode restart, and handled folder switching. Since then, main has gained title shelves, series season grouping, local Media3 playback, validated same-folder sidecar subtitles and a custom Compose transport. This release adds persisted caption-size and vertical-position controls on the separate subtitle viewport, accessible ranges/set-progress, Reset and a compact-screen-safe sheet. CI #250 passed all 55 Android 15 tests, unit/build, Room v1–v4 schema, privacy and permanent-signing checks. The Home feature hero uses cached artwork only, with local Play/Details actions. The user now wants no per-user TMDB setup and requests automatic matching/posters with only normalized title/year/type in application request fields. A publisher-configured app credential is required and still pending; it is extractable from a public APK, so it will never be called a secret. See the privacy handoff for the official licensing/auth limits. Emulator evidence is not Reno10 performance or acceptance. The latest signed release is [Player v0.1.250](https://github.com/Batmancpu/Player/releases/tag/v0.1.250), versionCode 250. Signed APKs are produced once per substantial completed batch, not per small edit.

**Glass is still being refined.** Haze 2.1.0 powers both the bounded production library navigation dock and a separate visual lab. The Android treatment is original and does not reproduce Apple's proprietary renderer. The lab is an experiment, not a promise of device-level blur or the final tactile finish. See the [engine review](docs/GLASS_ENGINE_REVIEW.md).

## Work plan

The integrated app is being completed in four visible batches. Development continues between checkpoints; there is no phone-install pause after each batch.

1. **README and visual identity** — original concept artwork and honest project overview. This batch is complete.
2. **Library and playback completeness** — folder recovery and scan edge cases, playback controls, subtitle workflows and persistent offline behavior.
3. **Production experience** — coherent movie/show navigation, polished title details, accessible controls, original restrained Glass and non-Glass fallbacks.
4. **Release readiness** — automated regression coverage, licensing/privacy review, signed update build, and release checklist. Measure frame time, memory and thermal behavior on the Reno10 when physical device data is available; do not infer those results from CI.

Follow the [six-phase roadmap](docs/ROADMAP.md) for engineering detail. A signed APK will be published to GitHub Releases for Obtainium after the requested build passes the release workflow's test, privacy and permanent-certificate checks. Use only a signed Release APK for installation; CI debug APKs are not for updating the installed app.

## Project records

- [Current handoff](docs/WORK_HANDOFF.md)
- [Product roadmap](docs/ROADMAP.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Design and device-performance requirements](docs/DESIGN_SYSTEM_AND_PERFORMANCE.md)
- [Storage, network and privacy rules](docs/UPSTREAM_AND_PRIVACY.md)
- [Glass engine review](docs/GLASS_ENGINE_REVIEW.md)
- [Phone-test checklist](docs/PHASE1_PHONE_TEST.md)
- [Development log](DEVELOPMENT_LOG.md)
- [Known issues and fixes](ISSUES_AND_FIXES.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)
- [AI-agent operating rules](AGENTS.md)

## Design references

- [Apple Human Interface Guidelines — Materials](https://developer.apple.com/design/human-interface-guidelines/materials)
- [Haze Glass documentation](https://chrisbanes.github.io/haze/2.1.0/)
- [TMDB API documentation](https://developer.themoviedb.org/docs/getting-started)

Player uses original Android code and generated concept artwork. It does not include Apple fonts, symbols, logos or proprietary assets.



**Glass Lab note:** The current lab previews a bounded selection dock over generated artwork and has a visible return to the library. Its tabs do not navigate the catalogue. The real library's separate dock is functional and has an effects-off fallback. Further visual tuning and Reno10 profiling remain in progress.
