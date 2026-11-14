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

No Apple SF Symbols glyphs, Apple logos or SF font files are bundled.


## Runtime libraries
AndroidX/Jetpack (Android Open Source Project and contributors), Kotlin/kotlinx.coroutines (JetBrains and contributors), and Haze 2.1.0 (Chris Banes and contributors) use Apache-2.0. Packaged license/attribution: `app/src/main/assets/licenses/APACHE_2_0.txt` and `DEPENDENCIES.md`. Haze license verified at tag 2.1.0: https://github.com/chrisbanes/haze/blob/2.1.0/LICENSE. Build tooling and AndroidX Test/JUnit are development/test dependencies, not runtime feature additions. Retain upstream per-file copyright and any applicable NOTICE when adapting code.
