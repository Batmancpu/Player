# brrPlayer Glass engine review — 2026-11-22

## Current implementation state

The user clarified that the brrMusic repository is the only reference for the Liquid Glass engine and collapsing dock; the keyboard app is used only for launcher-icon identity inspiration. No keyboard-app code or artwork is copied. No music screens or music interaction patterns beyond the reusable glass/dock behavior are in scope.

A source audit pinned brrMusic at `654aecdb7b2777363db3585d1f59f3952aabb603`. Its Glass integration uses Kyant0/backdrop v2.0.0 (Apache-2.0) and GPL-3.0 integration code. The generic FloatingTabBar derives from `elyesmansour/compose-floating-tab-bar` v1.0.1 (Apache-2.0), with GPL-3.0 inline/expanded motion changes. The selected-pill motion port has an MIT notice for `liquid_glass_easy` 4.3.4 (Ahmed Gamil, 2025). Exact notices are in `THIRD_PARTY_NOTICES.md`, per-file source headers and packaged `app/src/main/assets/licenses/` documents. The app repository is therefore GPL-3.0; this copyleft obligation is deliberate and documented.

Current source imports the Backdrop renderer and reusable FloatingTabBar, removes the Haze dependencies, adapts menu/music-only callbacks out, and provides brrPlayer-owned composition settings. `PlayerGlassDock` maps the same collapse/expand dock to Home, Movies, Shows and Search. The library and Glass Lab record generated/local UI for the dock's source-backed surface. Android's animator-scale setting is observed for reduced-motion behavior. Blur work is limited to the dock/selection treatment and uses a 0.55 resolution scale; posters and the video surface are not blurred. Glass disable and API<31 paths stay available. Lens rendering is API 33+; API 31/32 retain source blur/rim/tint without refraction.

This work is still unverified until Android source and instrumentation CI passes. The 320x640 CI emulator and earlier Haze screenshots do not prove brrMusic engine output, CPH2531 appearance, constant 60/120 Hz, battery use, memory or thermal behavior. Do not claim any Reno10 frame result before release-like Macrobenchmark/Perfetto traces on the user's device.

## Design and performance principles

Apple's public HIG describes Liquid Glass as a distinct functional layer for navigation/controls and recommends sparing custom use, clear content/control hierarchy and adaptable contrast/transparency. brrPlayer translates those principles into an original Android implementation; it does not ship Apple code/assets. Compose guidance emphasizes separating composition/layout/drawing work and measuring real devices rather than inferring performance from emulator traces.

- [Apple HIG: Materials](https://developer.apple.com/design/human-interface-guidelines/materials)
- [Apple: Adopting Liquid Glass](https://developer.apple.com/documentation/technologyoverviews/adopting-liquid-glass)
- [Android: Compose performance](https://developer.android.com/develop/ui/compose/performance)
- [Kyant0/backdrop 2.0.0](https://github.com/Kyant0/backdrop/tree/2.0.0)
- [Batmancpu/brrMusic](https://github.com/Batmancpu/brrMusic)

The dock should remain legible over busy artwork with an opaque contrast backing, user-controlled Glass off state and reduced-motion behavior. Do not extend live blur to every poster, full-screen surfaces or video. Test click, accessibility, scroll-collapse, reverse-scroll expansion, Glass off, API fallbacks, nav insets, narrow screens and TalkBack. Profile the actual Reno10 at 60 Hz first, with enabled/disabled comparisons; investigate 120 Hz only if the phone actually exposes it for the app.

## Earlier experiments

Haze 2.1.0 was previously used for a dock/lab proof of concept. Its emulator screenshots and tests established only bounds/fallback behavior, not visual quality. The Haze dependencies and implementation are removed by the current Glass batch; historical CI notes in `DEVELOPMENT_LOG.md` remain as historical evidence, not current implementation claims.

## CI and privacy status — 2026-11-22
Glass compile fixes cleared CI #269 (`44999320588`): Android 15 instrumentation and normal unit/build/schema/privacy checks passed. CI #269 did not sign or publish an APK and its source predates the final no-network permission change. The latest `main` commit `180d193` removes INTERNET permission and release credential injection per the user's explicit offline-only direction; CI #270 is still verifying the merged manifest. Do not call this a phone-accepted or performance-verified engine. Next release candidate should be one signed batch APK only after #270 and release signature/privacy gates pass. The phone user has not inspected this Glass version yet; synthetic emulator screenshots do not measure CPH2531 appearance, frame time, thermal load or battery.
