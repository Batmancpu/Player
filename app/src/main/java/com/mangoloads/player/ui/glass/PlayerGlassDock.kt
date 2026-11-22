package com.mangoloads.player.ui.glass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mangoloads.player.R
import com.mangoloads.player.ui.glass.floatingtabbar.FloatingTabBar
import com.mangoloads.player.ui.glass.floatingtabbar.FloatingTabBarDefaults
import com.mangoloads.player.ui.glass.floatingtabbar.FloatingTabBarInlineBehavior
import com.mangoloads.player.ui.glass.floatingtabbar.FloatingTabBarScrollConnection
import com.mangoloads.player.ui.icons.PlayerGlyph
import com.mangoloads.player.ui.icons.PlayerGlyphIcon

/** Movie-library adaptation of brrMusic's reusable collapsing Liquid Glass dock. */
@Composable
internal fun PlayerGlassDock(
    selected: Int,
    onSelected: (Int) -> Unit,
    scrollConnection: FloatingTabBarScrollConnection,
    modifier: Modifier = Modifier,
) {
    val labels = listOf(R.string.nav_home, R.string.nav_movies, R.string.nav_shows, R.string.nav_search)
    val labelTexts = listOf(
        stringResource(R.string.nav_home),
        stringResource(R.string.nav_movies),
        stringResource(R.string.nav_shows),
        stringResource(R.string.nav_search),
    )
    val icons = listOf(PlayerGlyph.HOME, PlayerGlyph.MOVIES, PlayerGlyph.SHOWS, PlayerGlyph.SEARCH)
    val shape = RoundedCornerShape(30.dp)
    FloatingTabBar(
        selectedTabKey = selected,
        scrollConnection = scrollConnection,
        modifier = modifier,
        tabBarContentModifier = {
            Modifier.liquidGlass(shape = shape, surfaceTint = Color(0xC611141C))
        },
        colors = FloatingTabBarDefaults.colors(
            backgroundColor = Color(0xFF171A22),
            indicatorColor = Color.White.copy(alpha = .20f),
            selectedContentColor = Color.White,
        ),
        shapes = FloatingTabBarDefaults.shapes(tabBarShape = shape, tabShape = shape),
        sizes = FloatingTabBarDefaults.sizes(
            tabBarContentPadding = androidx.compose.foundation.layout.PaddingValues(5.dp),
            tabInlineContentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            tabExpandedContentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 13.dp, vertical = 9.dp),
            tabSpacing = 4.dp,
        ),
    ) {
        labels.forEachIndexed { index, _ ->
            val label = labelTexts[index]
            tab(
                key = index,
                title = {
                    Text(
                        text = label,
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                },
                icon = { PlayerGlyphIcon(icons[index], label, size = 21.dp, tint = Color.White) },
                onClick = { onSelected(index) },
            )
        }
    }
}
