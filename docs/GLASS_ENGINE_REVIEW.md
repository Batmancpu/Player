# Glass engine review — 2026-11-15

The user reports the floating dock in v0.1.79 looks broken on OPPO Reno10 CPH2531. This is a real visual defect report. A successful build never established visual quality, frame rate or thermal efficiency. The preview remains experimental; production library and video rendering do not apply Glass.

## What is currently used

Haze 2.1.0, Apache-2.0, source-backed Glass with `HazeInput.Sources`, Default/Balanced performance. This records designated Compose content, then processes a bounded control surface. It is not Apple's renderer and does not depend on Android 17 native window backdrop. Public guidance: https://chrisbanes.github.io/haze/2.1.0/performance/ . Regular/shared surfaces are the conservative starting point; the old preview instead merged four separate Clear tiles.

The new original preview uses one max420dp Regular dock, equal flexible tabs, an independent spring selection pill and an opaque effects-off fallback. The pill is an ordinary Compose highlight, not a claim of rubbery lens deformation. No custom shader, JNI, pixel-readback contrast loop or proprietary assets were added. CI88 retained catalogue/search/details/Glass-on/off PNGs were inspected. They exposed bright-dock white-label washout despite passing bounds tests; the next revision adds a deterministic dark contrast layer and lowers selection-pill opacity. CI90 retained and CI91 regenerated five synthetic screenshots; catalogue/search/details and Glass-on/off images were retrieved and inspected. On the 320×640 Android15 test emulator, two movie columns and all filters are visible; the dock is a single bounded floating surface with readable labels, and effects-off is a solid fallback. The async search-test gap is fixed and CI90/91 pass. This verifies emulator UI bounds only. The user has not reported new-build OPPO appearance; no speed/thermal efficiency or premium acceptance is claimed.

## Correct user reference: FCZenith and immediate Zenith upstream

- User fork: https://github.com/Batmancpu/FCZenith . Its immediate parent is https://github.com/rezapranata089-bit/Zenith . Bitcode/BitChord is not the requested visual reference.
- Reviewed immediate-parent commit `26c5ed8496b7beb5f248ee93f26fbe73e3df47dd`, including `app/src/main/java/com/music/zenith/ui/components/LiquidGlass.kt`; compared fork integration/backdrop files. The inspected glue says rendering is vendored Kyant0/backdrop, with separate GPL-3.0 Echo Music integration. The vendored renderer identifies Backdrop v2.0.0/Apache-2.0. App-level licence is GPL-3.0; do not treat all its glue as permissive renderer code.
- Pipeline includes captured backdrop, blur/lens, highlight/shadow and tint. Inspected defaults include 10dp blur, lens height/amount 0.5, maximum48dp and resolution fraction0.33. One-third width and height processes about one-ninth the pixels; this is an implementation property, not an OPPO benchmark or a proof of equal quality.
- Zenith's current upstream also has backdrop seam work. Its selection/refraction integration and settings are app-specific. A renderer name alone cannot reproduce its finish; geometry, source content, contrast, selection motion and compositing all matter.
- No Zenith/Echo application source was copied. A future Backdrop evaluation must independently audit the exact pinned renderer licence, retained notices, Compose ABI, Android fallback, dependency/permission graph and allocation/GPU cost before adoption. No wholesale renderer/toolchain switch is justified by unmeasured claims.

## Decision and measurements still needed

Haze 2.1.0 remains a viable lab renderer, with no established superiority or premium-quality acceptance. The v0.1.91 screenshots verify the emulator renders and toggles the dock, not physical-device output. Next compare the same control sizes/content/motion on the OPPO with Glass on/off and, if justified, a separately licensed Backdrop prototype. Follow `DESIGN_SYSTEM_AND_PERFORMANCE.md`: release-like Macrobenchmark/Perfetto on the actual OPPO at 60Hz, optional120Hz, p50/p95/p99 frame time/jank, scrolling/transitions/slider/playback, memory and sustained thermal behavior. Do not claim either renderer efficient on this phone without these results.

Apple's public material informs original controls and hierarchy, not copied assets: https://developer.apple.com/design/human-interface-guidelines/materials and https://developer.apple.com/documentation/technologyoverviews/adopting-liquid-glass . Content stays crisp; Glass belongs to restrained functional chrome with accessible fallbacks. Library feature work proceeds independently of renderer experiments.


## Upstream provenance and licence recheck — 2026-11-21
GitHub now redirects the requested Batmancpu/FCZenith URL to Batmancpu/brrMusic. That repository is a fork of the immediate reference rezapranata089-bit/Zenith; both declare GPL-3.0. Zenith's current GlassNavBar.kt documents GPL-3.0-derived Echo Music structure and vendored floating-tab code, while its renderer tree has separate provenance/licence requirements. These remain visual/design references only; no source code is imported into Player. Player continues using Haze 2.1.0 (Apache-2.0) only in the Glass Lab, not the production catalogue. A synthetic CI screenshot is not evidence of appearance or frame time on OPPO.


## Glass Lab usability repair — 2026-11-21 (CI pending)
The synthetic CI capture showed the preview dock covered demo copy at the lower edge, and the screen had only an implicit Android Back route to exit. Added an explicit “Back to library” action, clearly states that the four tabs only demonstrate selection motion, adds a restrained lower fade behind the dock, and increases scroll-tail spacing so the last demo text can move above the dock. The Haze surface remains bounded and opt-in in this lab only. A synthetic UI test now verifies the back action; CI and handset appearance are pending. This is a usability repair, not a claim that the renderer reproduces Apple's engine or meets OPPO frame-time targets.

## 2026-11-21 — Haze press response connected to dock controls
- Shared a single dock interaction source between selectable tabs and the bounded Haze material surface. This activates the already-configured Haze pressed lens/highlight/compression response during touch, while the original spring selection pill remains separate and system reduced-motion policy stays active. The effects-off opaque fallback is unchanged. CI and real-device appearance/performance are pending.
