package com.batmancpu.player.ui.glass

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import com.batmancpu.player.R
import com.batmancpu.player.ui.icons.PlayerGlyph
import com.batmancpu.player.ui.icons.PlayerGlyphIcon
import dev.chrisbanes.haze.HazeState

/** One bounded material surface; selection is a lightweight original spring pill. */
@Composable
internal fun PlayerGlassDock(
    hazeState: HazeState,
    enabled: Boolean,
    selected: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val glyphs = listOf(PlayerGlyph.HOME, PlayerGlyph.MOVIES, PlayerGlyph.SHOWS, PlayerGlyph.SEARCH)
    val labels = listOf(R.string.nav_home, R.string.nav_movies, R.string.nav_shows, R.string.nav_search)
    val dockShape = RoundedCornerShape(30.dp)
    val dockInteraction = remember { MutableInteractionSource() }
    BoxWithConstraints(
        modifier.widthIn(max = 420.dp).fillMaxWidth().height(76.dp)
            .shadow(14.dp, dockShape, clip = false)
            .playerGlassSurface(
                hazeState = hazeState,
                enabled = enabled,
                interactionSource = dockInteraction,
                shape = dockShape,
            )
            // Bound light-background luminance behind white labels in this always-dark interface.
            .background(Color.Black.copy(alpha = if (enabled) .62f else 0f), dockShape)
            .border(
                1.dp,
                Brush.verticalGradient(listOf(Color.White.copy(alpha = .42f), Color.White.copy(alpha = .12f))),
                dockShape,
            )
            .padding(6.dp),
    ) {
        val cell = maxWidth / 4
        val offset by animateDpAsState(cell * selected, spring(dampingRatio = .72f, stiffness = 420f), label = "dock_selection")
        val selectionShape = RoundedCornerShape(24.dp)
        Box(Modifier.offset(x = offset).width(cell).fillMaxHeight()
            .background(
                Brush.verticalGradient(listOf(Color.White.copy(alpha = .20f), Color.White.copy(alpha = .07f))),
                selectionShape,
            )
            .border(1.dp, Color.White.copy(alpha = .22f), selectionShape))
        Row(Modifier.fillMaxSize().selectableGroup()) {
            glyphs.forEachIndexed { index, glyph ->
                val selectionDescription = stringResource(
                    if (selected == index) R.string.nav_item_selected else R.string.nav_item_not_selected,
                )
                Column(
                    Modifier.weight(1f).fillMaxHeight().selectable(
                        selected = selected == index, role = Role.Tab, interactionSource = dockInteraction,
                        indication = null, onClick = { onSelected(index) },
                    ).semantics { stateDescription = selectionDescription }.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
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
