# Commit-sized implementation roadmap

This roadmap reflects the user's latest approved scope (2026-11-14). Build the functional catalogue and local player first; treat the current Glass Lab as an isolated experiment, not the product UI. Every implementation commit must update DEVELOPMENT_LOG.md. Work only on main, use small commits, and stop when a verification gate fails.

**Current verified position:** Android/Compose app skeleton; edge-to-edge launcher; shared dark cinematic theme, typography/spacing tokens and four-icon Lucide registry; three JVM theme tests; Haze 2.1.0 dependencies; shared source-backed PlayerGlass helper; isolated GlassLab currently launched from MainActivity. Code CI run 16 passed and uploaded APK artifact 14101590882. The latest docs-only run 17 also passed. No physical-device install/Glass validation has been recorded. No storage, metadata, database, catalogue or playback feature exists yet.

## Phase 0 — Repository and build foundation
- [x] Canonical handoff, operating rules, architecture, decisions, roadmap, device playbook, development log and issue register.
- [x] Android app module, SDK configuration, Compose launcher and dark starter theme.
- [x] Node 24-compatible CI workflow; unit tests, debug APK assembly and artifact upload.
- [x] Three JVM theme/contrast regression tests.
- [ ] Add Gradle wrapper and reproducible local build entry point; CI currently installs Gradle 9.5.1 directly.
- [ ] Add dependency/licence inventory and Compose UI smoke test.
**Gate:** clean main build, tests and APK artifact all pass.

## Phase 1 — Plain functional app shell and one-folder access (NEXT)
1. Replace the Glass Lab as the default launcher screen with a plain, content-first setup/library shell. Keep the Glass Lab isolated and reachable only as a development experiment; do not delete it or integrate it into the product navigation yet.
2. Add one-folder onboarding using ACTION_OPEN_DOCUMENT_TREE / ActivityResultContracts.OpenDocumentTree.
3. Validate the selected document provider against an explicit local-provider allowlist; default-deny unknown, cloud and network providers. Confirm the OPPO local provider on device before expanding the allowlist.
4. Persist the read-only tree grant and selected tree identity securely; recover gracefully after permission revocation/reselection. Do not request MANAGE_EXTERNAL_STORAGE.
5. Add a cancellable recursive scanner restricted to the chosen tree and descendants. Whitelist video MIME types/extensions, skip unsupported/system entries, deduplicate by provider authority + document ID, bound concurrency, and never modify source files.
6. Recognize sidecar subtitle files for later pairing, but never include subtitle contents in metadata requests.
7. Add tests for root-boundary enforcement, nested folders, cancellation, duplicates, inaccessible providers and permission loss.
**Gate:** the app remembers exactly one local folder, discovers only its descendants, and leaves source files untouched. No TMDB or player dependency is added in this phase.

## Phase 2 — Filename parsing and content identity
8. Normalize movie filenames into title/year while removing release-group, resolution, codec and container noise.
9. Parse series title, season and episode from common S01E02, S01.E02, 1x02 and Season 1/02 patterns.
10. Match a series once and bind episodes to the parent series; do not search TMDB separately for every episode.
11. Add test corpus for remakes, duplicate titles, punctuation, Unicode, anime, multi-episode names, year ambiguity and noisy release strings.
**Gate:** parser outputs are deterministic and ambiguous matches are flagged, never silently accepted.

## Phase 3 — Local database and offline persistence
12. Add Room entities, DAOs, repositories and explicit migrations for folder, media document identity, parsed title, movie/series/season/episode, metadata match and user override.
13. Use DataStore for non-secret preferences; keep credentials in Android Keystore-backed storage.
14. Implement incremental scan reconciliation. Do not mark files missing after an incomplete or failed scan.
15. Add persistent app-private artwork/subtitle cache boundaries; never cache video files.
**Gate:** indexed items and manual overrides survive process death, restart and offline use without destructive migrations.

## Phase 4 — TMDB metadata and high-quality artwork
16. Resolve the TMDB credential model before network implementation: user-provided credential or optional metadata. Never commit a shared key or add a proxy/backend merely to hide it.
17. Add a typed direct HTTPS TMDB client with timeout, bounded concurrency, rate-limit handling, cancellation, retries and log redaction.
18. Send only normalized title, optional year, media type and language. Never send full filename, local path, folder name, SAF URI or media bytes.
19. Rank candidates conservatively; handle remakes/languages and allow manual search, confirmation and persistent overrides.
20. Fetch detailed movie/series/season/episode metadata and TMDB poster/backdrop artwork; use a maintained image loader, stable aspect ratios, memory/disk caching and progressive placeholders.
21. Add required TMDB attribution and approved logo treatment in About/Credits.
**Gate:** test movies and series display correct titles, posters, backdrops and detail metadata; corrected matches survive rescans; cached catalogue works offline.

## Phase 5 — Real catalogue and detail screens
22. Build plain Compose navigation and the actual home/library experience before production Glass.
23. Display crisp poster grids and curated shelves with efficient image loading; avoid layout shifts while artwork loads.
24. Add movie detail pages and series → season → episode navigation with backdrops, synopsis and relevant metadata.
25. Add local-library search and unmatched/pending/offline/error/empty/loading states.
26. Add settings for selected folder, rescan, matching corrections, metadata credential, artwork cache and credits.
27. Add UI tests for navigation/state restoration, accessibility, long titles, no artwork, no internet and large libraries.
**Gate:** the user can open Player, browse a real locally indexed catalogue, view TMDB artwork/details and recover from offline/permission states. This is the first complete product milestone.

## Phase 6 — Local playback engine and basic player
28. Spike AndroidX Media3 against the local test corpus using SAF content URIs; test container parsing, codec/decoder availability, audio/video sync, seekability, multi-track discovery, hardware decoding and playback stability.
29. Compare LibVLC only if Media3 gaps are material; audit native ABIs/APK size, hardware/software decoding, battery/thermal impact, crash surface and GPL/LGPL obligations before selection.
30. Put the selected engine behind a small playback interface; keep catalogue identity and UI independent from engine-specific types.
31. Connect the catalogue's Play action to the local document URI. Add basic play/pause, seek, progress, full-screen/orientation handling, audio focus and remembered playback position.
32. Provide clear unsupported-format/decoder errors and never crash the whole app on an unsupported file.
33. Add JVM/instrumentation tests where possible, then perform the full CPH2531 format corpus test.
**Gate:** representative local files play reliably with a documented compatibility matrix. “Every format” is an aspiration, not a guarantee.

## Phase 7 — Audio, subtitles and sync controls
34. Expose embedded audio-track discovery and selection with language/codec/channel labels where available.
35. Expose embedded subtitle-track selection, visibility and language labels.
36. Allow the user to select local external subtitle files through SAF; support formats the chosen engine actually supports (candidate formats: SRT, WebVTT, TTML, SSA/ASS).
37. Select and integrate an online subtitle provider only after reviewing terms, API/auth, availability, language coverage, rate limits and privacy.
38. Search and download a subtitle from the player without interrupting playback; associate it with the local media item and store it in app-private storage by default.
39. Add subtitle delay adjustment in small increments, a larger adjustment step, visible offset feedback and reset-to-zero.
40. Keep all track/subtitle controls in a compact, uncluttered sheet/menu; video remains visible and controls disappear naturally.
**Gate:** embedded, local sidecar and downloaded subtitles can be selected; sync can be adjusted and reset; no source media is changed and no private local identifiers are sent.

## Phase 8 — Production Liquid Glass integration
41. Keep the current Haze Glass Lab as a test harness; do not use its generated artwork as the catalogue.
42. Validate the Glass Lab on CPH2531/Android 15 with full/disabled fallback before promoting any material to production.
43. Build reusable regular/clear Glass surfaces, icon buttons, compact pills and sheets around the verified Haze source-backed path.
44. Apply Glass only to functional chrome: navigation, playback controls, track/subtitle sheet and transient menus. Keep posters and video crisp.
45. Add reduced-transparency/reduced-motion/unsupported-device fallbacks; keep expensive surfaces few and grouped.
46. Verify readability over bright/dark artwork and video, gesture hit targets, animation performance, memory, thermal and 60-Hz stability.
**Gate:** every core journey works with Glass disabled; production Glass improves controls without blocking the product or causing jank.

## Phase 9 — Hardening and release
47. Validate TalkBack, text scaling, contrast, landscape, orientation, audio focus and lifecycle recovery.
48. Benchmark scanning and poster loading at 100/500/1000 files.
49. Test offline, slow network, API errors, permission revocation, provider failures, corrupt cache and unsupported codecs.
50. Inspect dependencies, licenses, manifest permissions and network hosts; verify no analytics, telemetry, media upload or hidden backend.
51. Document all third-party licenses, TMDB attribution and any playback-engine copyleft obligations.
52. Configure stable signing and monotonic version-code policy only after package identity is final.
53. Publish the first release only after clean CI, APK verification, documented format matrix and separate physical-device validation.
**Gate:** reproducible signed APK, passed tests, privacy audit and target-device acceptance.

Never mark an item complete from a plan or compilation alone. CI does not prove Glass rendering, codec compatibility, battery use or device stability.
