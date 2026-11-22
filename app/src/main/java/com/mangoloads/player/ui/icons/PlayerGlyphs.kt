package com.mangoloads.player.ui.icons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mangoloads.player.R

/** The deliberately small, consistent symbol vocabulary used by Player navigation. */
internal enum class PlayerGlyph(@DrawableRes val drawableRes: Int) {
    HOME(R.drawable.ic_player_home),
    MOVIES(R.drawable.ic_player_movies),
    SHOWS(R.drawable.ic_player_shows),
    SEARCH(R.drawable.ic_player_search),
    ARROW_BACK(R.drawable.ic_player_arrow_back),
    ASPECT_RATIO(R.drawable.ic_player_aspect_ratio),
    FULLSCREEN(R.drawable.ic_player_fullscreen),
    FULLSCREEN_EXIT(R.drawable.ic_player_fullscreen_exit),
    CAPTIONS(R.drawable.ic_player_captions),
}

@Composable
internal fun PlayerGlyphIcon(
    glyph: PlayerGlyph,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        painter = painterResource(glyph.drawableRes),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint,
    )
}
