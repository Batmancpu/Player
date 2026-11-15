package com.batmancpu.player.ui.glass

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.batmancpu.player.R
import com.batmancpu.player.ui.icons.PlayerGlyph
import com.batmancpu.player.ui.icons.PlayerGlyphIcon
import dev.chrisbanes.haze.HazeState

/** One bounded material surface; selection is a lightweight original spring pill. */
@Composable
internal fun GlassPreviewDock(
    hazeState: HazeState,
    enabled: Boolean,
    selected: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val glyphs = listOf(PlayerGlyph.HOME, PlayerGlyph.MOVIES, PlayerGlyph.SHOWS, PlayerGlyph.SEARCH)
    val labels = listOf(R.string.nav_home, R.string.nav_movies, R.string.nav_shows, R.string.nav_search)
    BoxWithConstraints(
        modifier.widthIn(max = 420.dp).fillMaxWidth().height(76.dp)
            .playerGlassSurface(hazeState, enabled, shape = RoundedCornerShape(30.dp))
            // Bound light-background luminance behind white labels in this always-dark interface.
            .background(Color.Black.copy(alpha = if (enabled) .62f else 0f), RoundedCornerShape(30.dp))
            .padding(6.dp),
    ) {
        val cell = maxWidth / 4
        val offset by animateDpAsState(cell * selected, spring(dampingRatio = .72f, stiffness = 420f), label = "dock_selection")
        Box(Modifier.offset(x = offset).width(cell).fillMaxHeight()
            .background(Color.White.copy(alpha = .10f), RoundedCornerShape(24.dp)))
        Row(Modifier.fillMaxSize().selectableGroup()) {
            glyphs.forEachIndexed { index, glyph ->
                val interaction = remember { MutableInteractionSource() }
                Column(
                    Modifier.weight(1f).fillMaxHeight().selectable(
                        selected = selected == index, role = Role.Tab, interactionSource = interaction,
                        indication = null, onClick = { onSelected(index) },
                    ).padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                ) {
                    PlayerGlyphIcon(glyph, null, size = 22.dp, tint = Color.White)
                    Text(stringResource(labels[index]), color = Color.White,
                        style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
