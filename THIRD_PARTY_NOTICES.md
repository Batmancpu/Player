# Third-party notices

This file records code and assets shipped with brrPlayer. Each imported component retains its upstream license, copyright, and modification notes. The repository is distributed under GPL-3.0 because the integrated brrMusic Glass glue and collapse motion are GPL-3.0; see `LICENSE` and the packaged `app/src/main/assets/licenses/GPL-3.0.txt`.

## brrMusic Glass integration and collapsing dock
- Source application: https://github.com/Batmancpu/brrMusic
- Pinned source commit reviewed: `654aecdb7b2777363db3585d1f59f3952aabb603`
- License: GPL-3.0. Adapted Glass integration, floating dock collapse/expand behavior, and selection-pill interaction are used as an Android movie-library navigation control.
- Scope: engine and generic navigation/collapse behavior only. No music-library screens, songs, mini-player, playback UI, or music branding were copied. Keyboard-app reference is limited to broad icon identity inspiration; no keyboard code/assets are included.
- Original reusable floating tab bar: `elyesmansour/compose-floating-tab-bar` v1.0.1, Apache-2.0; its upstream notice is retained in the source header. brrMusic's inline/expanded animation modifications are GPL-3.0.
- Selected pill motion port: `liquid_glass_easy` 4.3.4, MIT, Copyright (c) 2025 Ahmed Gamil. Notice packaged as `app/src/main/assets/licenses/MIT_LIQUID_GLASS_EASY.txt`.

## Backdrop renderer
- Project: https://github.com/Kyant0/backdrop
- Version: 2.0.0 (`io.github.kyant0:backdrop`)
- Copyright 2025 Kyant0; Apache License 2.0.
- Vendored Android Kotlin renderer in `app/src/main/java/com/mangoloads/player/ui/glass/backdrop/`. Per-file attribution is retained. Local changes: package rename, Android-only source consolidation, bounded resolution scaling, and one-pass recording/composition. Full Apache text is packaged as `APACHE_2_0.txt`.

## Other bundled components
- AndroidX/Jetpack, Kotlin/kotlinx.coroutines and Media3 1.11.1: Apache-2.0; full text in `APACHE_2_0.txt`.
- Coil Compose 3.6.3: Apache-2.0; used for local app-private artwork files only; no Coil network module.
- Lucide vectors: ISC/MIT-derived notices in `LUCIDE_ICONS_LICENSE.md`.
- Material `closed_caption` vector: Apache-2.0.

No Apple SF Symbols, SF fonts, Apple logos, or proprietary artwork are bundled. README concept artwork and launcher identity artwork are original generated assets.
