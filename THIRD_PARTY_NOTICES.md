# Third-party notices

This file records third-party code/assets shipped in the Player APK. Each included dependency or asset must retain its licence text and attribution.

## Lucide icon assets
- Source project: https://github.com/composablehorizons/compose-icons
- Source release/tag: 2.2.1
- Upstream module: `icons-lucide-android`
- Assets adapted: house, clapperboard, TV, search.
- Local files: `app/src/main/res/drawable/ic_player_*.xml`
- Adaptation: vector stroke width changed from 2.0 to 1.8 for the app's optical scale.
- License: ISC, with MIT terms for portions derived from Feather. The complete notice is packaged at `app/src/main/assets/licenses/LUCIDE_ICONS_LICENSE.md`.

## Material caption icon
- Source: Google Material Icons `closed_caption`, Apache License 2.0: https://developers.google.com/fonts/docs/material_icons
- Local adaptation: `app/src/main/res/drawable/ic_player_captions.xml`, rendered as a 24dp vector for the player caption-appearance control.
- The Apache-2.0 notice is already packaged in `app/src/main/assets/licenses/APACHE_2_0.txt`.

No Apple SF Symbols glyphs, Apple logos or SF font files are bundled.


## Runtime libraries
AndroidX/Jetpack (Android Open Source Project and contributors), Kotlin/kotlinx.coroutines (JetBrains and contributors), and Haze 2.1.0 (Chris Banes and contributors) use Apache-2.0. Packaged license/attribution: `app/src/main/assets/licenses/APACHE_2_0.txt` and `DEPENDENCIES.md`. Haze license verified at tag 2.1.0: https://github.com/chrisbanes/haze/blob/2.1.0/LICENSE. Build tooling and AndroidX Test/JUnit are development/test dependencies, not runtime feature additions. Retain upstream per-file copyright and any applicable NOTICE when adapting code.

## AndroidX Media3 local playback
Media3 ExoPlayer and UI 1.11.1, Android Open Source Project/contributors, Apache-2.0: https://github.com/androidx/media/tree/1.11.1 (LICENSE). Included through pinned Maven dependencies; packaged Apache-2.0 text and dependency attribution above apply. Uses platform decoders; no separate GPL/native codec bundle or Zenith application source is included.


## Coil Compose image rendering
- Dependency: `io.coil-kt.coil3:coil-compose:3.6.3`, Apache-2.0: https://github.com/coil-kt/coil/blob/3.6.3/LICENSE.txt
- Used only to decode bounded Player app-private cached artwork from local `File` values. No Coil network module is included; metadata API and artwork CDN transports remain separate, explicit and bounded.
- Apache-2.0 license text is packaged at `app/src/main/assets/licenses/APACHE_2_0.txt`.


## Coil Compose
Coil Compose 3.6.3 (Apache-2.0): https://github.com/coil-kt/coil/blob/3.6.3/LICENSE.txt. Used for local app-private artwork File decoding only; no network module. Apache text is already packaged in APACHE_2_0.txt.


### Media3 player layout adaptation

`app/src/main/res/layout/exo_player_view.xml` adapts the Media3 UI layout under the Apache License 2.0, moving `SubtitleView` outside the aspect-ratio content frame so captions do not inherit video resize/crop geometry. Original: https://github.com/androidx/media/blob/release/libraries/ui/src/main/res/layout/exo_player_view.xml
