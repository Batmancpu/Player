# Commit-sized implementation roadmap

Sequential plan. Each item is a focused commit, split further if its diff/failure surface grows. Update DEVELOPMENT_LOG.md in every implementation commit. A failed gate blocks subsequent work.

**Current position:** buildable Android shell, shared theme, icon registry, three passing theme tests and Haze 2.1.0 dependencies. First Glass laboratory is next.

## Phase 0 — Durable handoff
- [x] README, AGENTS.md, PROJECT_CONTEXT.md and initial DEVELOPMENT_LOG.md.
- [x] Architecture, decisions, roadmap, device tests and issue register.
- [x] First clean Android debug APK build and GitHub Actions artifact verified.
- [x] CI action runtime upgraded to Node 24-compatible releases.
- [ ] Gradle wrapper/reproducible local build entry point; currently CI installs Gradle 9.5.1 directly.
- [x] Three authored JVM theme contrast/palette tests; CI run 8 passed. Compose UI smoke tests remain pending.
- [ ] CI dependency/license inventory.

## Phase 1 — Buildable Android foundation
1. build: add Gradle wrapper (root settings are already committed); preserve pinned Gradle 9.5.1.
2. build: pin Android/Kotlin plugin versions and version catalog. **Done:** AGP 9.3.1 and Kotlin Compose plugin 2.4.21; AGP9 built-in Kotlin used.
3. build: add minimal Android app module and SDK configuration. **Done:** app module, SDK37/minSdk29/targetSdk37.
4. app: add manifest and minimal Compose launcher activity. **Done:** edge-to-edge dark starter shell.
5. ci: add Android build workflow and APK artifact. **Done:** run 2 and run 3 passed; run 3 uses Node24-compatible action majors.
6. test: add baseline JVM and Compose UI smoke tests. **Partial done:** three JVM theme contrast/palette tests pass CI; Compose UI smoke test remains pending.
7. ci: add dependency/licence inventory check and verify no analytics/crash SDK. **Pending.**
8. docs: record first verified APK build and artifact. **Done:** runs and artifact IDs recorded in DEVELOPMENT_LOG.md.
**Gate:** clean checkout builds and produces installable debug APK.

## Phase 2 — Design tokens and typography
9. ui: add cinematic dark color tokens. **Done:** semantic dark palette in PlayerTheme.
10. ui: add spacing, shape and elevation tokens. **Done:** shared spacing and corner geometry.
11. ui: add typography scale, text truncation and system font policy. **Partial:** system sans-serif typography scale is in place; truncation rules remain.
12. ui: add one symbol registry and semantic icon descriptions. **Done:** four-icon PlayerGlyph registry with localized content-description parameter; Lucide notice packaged.
13. ui: establish edge-to-edge and window inset handling. **Partial:** transparent system bars and edge-to-edge enabled; screen-specific safe-area/inset handling remains.
14. ui: add reusable artwork/loading placeholders with fixed aspect ratio.
15. test(ui): add dark/light contrast and layout previews. **Partial done:** three dark-theme palette/contrast tests pass; previews/light-mode layout validation remain pending.

## Phase 3 — Glass proof of concept
16. build: verify Kotlin 2.4.21 compatibility in isolation. **Done:** app compiles on AGP 9.3.1 / Kotlin 2.4.21.
17. build: align Compose with Haze 2.1 requirements in isolation. **Pending verification:** current native Jetpack Compose BOM 2026.09.00 must be checked against Haze's Android artifact; do not add a Compose Multiplatform plugin unless required.
18. deps: add Haze 2.1.0 core and Glass artifacts only. **Done:** dependency-only commit 12a6329; CI run 10 passed. Material 3 integration artifact also resolves.
19. ui(glass): add bounded backdrop source layer. **Done:** GlassLabScreen registers one explicit local source with hazeSource and uses HazeInput.Sources for controls.
20. ui(glass): implement RegularGlassSurface. **Partial done:** PlayerGlassSurface and GlassStyle.clear are implemented; regular-vs-clear comparison and lab tuning remain.
21. ui(glass): implement ClearGlassSurface.
22. ui(glass): add GlassIconButton and GlassPill.
23. ui(glass): prototype merging controls with hazeGlassContainer. **Done in lab:** connected four-control navigation group using playerGlassContainer.
24. ui(glass): add reduced-transparency and reduced-motion fallbacks.
25. test(ui): add Glass fallback/animation smoke tests.
26. perf(ui): measure Glass lab on CPH2531 and disable unstable effects.
**Gate:** usable without Glass; no unverified native backend.

## Phase 4 — Single-folder storage
27. domain(storage): add single-folder and media-file models.
28. platform(storage): add ACTION_OPEN_DOCUMENT_TREE picker.
29. platform(storage): persist read-only URI grant.
30. security(storage): validate local provider authority; default-deny unknown/cloud/network.
31. data(storage): recursively enumerate selected tree on IO dispatcher.
32. data(storage): whitelist video MIME types/extensions.
33. data(storage): add incremental document reconciliation.
34. test(storage): test tree boundaries, nested folders, permission loss, duplicates and inaccessible roots.
**Gate:** scanner sees only selected local folder and never modifies it.

## Phase 5 — Filename parsing and metadata
35. domain(metadata): add filename normalization.
36. domain(metadata): parse movie title and year.
37. domain(metadata): parse series/season/episode patterns.
38. test(metadata): cover remakes, punctuation, Unicode, multi-episode names and year ambiguity.
39. settings(metadata): add user-managed TMDB credential flow.
40. security(metadata): store credential with Keystore-backed encryption and redact logs.
41. data(metadata): add direct TMDB HTTPS client with typed DTOs/timeouts.
42. data(metadata): add rate limit, retry, cancellation and bounded concurrency.
43. domain(metadata): rank candidate matches and detect ambiguity.
44. data(metadata): persist confirmed TMDB ID and manual overrides.
45. ui(metadata): add manual identification search/confirmation.
46. data(metadata): retrieve movie/series details.
47. data(metadata): retrieve season/episode details without a search per episode.
48. test(metadata): prove no local path/URI/file bytes are sent.
**Gate:** known test media match correctly; ambiguity can be corrected; no local identifiers leave device.

## Phase 6 — Database and artwork cache
49. data(db): add Room entities and database with explicit migrations.
50. data(db): add media/match DAOs.
51. data(db): add repository interfaces and implementations.
52. data(scan): add incremental sync coordinator and progress state.
53. data(images): add poster/backdrop loader.
54. data(images): add bounded persistent artwork cache, never video cache.
55. test(data): verify offline persistence, restart and cache recovery.
**Gate:** library persists across process death and works offline.

## Phase 7 — Screens
56. ui: add cinematic application shell and floating Glass navigation.
57. ui: add onboarding and one-folder setup/recovery.
58. ui(library): add efficient movie poster grid.
59. ui(library): add featured/recent shelves.
60. ui(detail): add movie hero and metadata page.
61. ui(detail): add series/seasons/episodes pages.
62. ui(search): add local-library search.
63. ui: finish loading, empty, offline and error states.
64. ui(settings): add folder/cache/matching/credits controls and TMDB attribution.
65. test(ui): add navigation, state restoration, long-title and empty-state tests.
**Gate:** all primary user journeys work without placeholder screens.

## Phase 8 — Hardening and release
66. a11y: validate text scale, contrast, TalkBack, hit targets and motion.
67. privacy: add host/permission regression tests; verify no telemetry or upload.
68. perf: benchmark scanner and poster grid at 100/500/1000 files.
69. perf: tune Glass count/fallback using frame timing, memory and thermal tests on CPH2531.
70. test: validate offline, permission-loss and corrupt-cache recovery.
71. release: document third-party licences and TMDB attribution.
72. release: configure stable signing and monotonic version-code policy.
73. release: publish first FOSS APK with release notes and SHA-256.
**Gate:** signed APK, verified install path, passed tests and separately recorded device-validation status.

This is a plan, not a completion claim. Never merge unrelated items just to reduce the commit count.