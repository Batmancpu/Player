package com.batmancpu.player.ui.glass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batmancpu.player.R
import com.batmancpu.player.ui.icons.PlayerGlyph
import com.batmancpu.player.ui.icons.PlayerGlyphIcon
import com.batmancpu.player.ui.theme.PlayerCorners
import com.batmancpu.player.ui.theme.PlayerPalette
import com.batmancpu.player.ui.theme.PlayerSpacing
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/**
 * Temporary visual laboratory for tuning Haze Glass on a real Android device.
 * All background artwork is generated locally; this screen makes no requests
 * and does not access the user's media folder.
 */
@Composable
internal fun GlassLabScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val hazeState = rememberHazeState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var glassEnabled by rememberSaveable { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PlayerPalette.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
        ) {
            GlassLabBackdrop(selectedTab = selectedTab)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = PlayerSpacing.screenHorizontal),
            ) {
                Spacer(Modifier.height(12.dp))

                TextButton(onClick = onBack) {
                    Text(stringResource(R.string.glass_lab_back))
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.glass_lab_kicker),
                            color = PlayerPalette.textTertiary,
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.6.sp,
                        )
                        Text(
                            text = stringResource(R.string.glass_lab_title),
                            color = PlayerPalette.textPrimary,
                            style = MaterialTheme.typography.headlineLarge,
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.glass_lab_toggle),
                            color = PlayerPalette.textSecondary,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Switch(
                            modifier = Modifier.semantics { contentDescription = "Glass effects" },
                            checked = glassEnabled,
                            onCheckedChange = { glassEnabled = it },
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.glass_lab_description),
                    color = PlayerPalette.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(188.dp)
                        .clip(PlayerCorners.panel)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF37436B),
                                    Color(0xFF6A3E70),
                                    Color(0xFFB06A59),
                                ),
                            ),
                        )
                        .padding(18.dp),
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.glass_lab_feature_label),
                            color = Color.White.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.2.sp,
                        )
                        Text(
                            text = stringResource(R.string.glass_lab_feature_title),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text = stringResource(R.string.glass_lab_artwork_heading),
                    color = PlayerPalette.textPrimary,
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(Modifier.height(13.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ArtworkTile(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.glass_lab_artwork_one),
                        subtitle = stringResource(R.string.glass_lab_artwork_one_subtitle),
                        colors = listOf(Color(0xFF263E57), Color(0xFF668B98), Color(0xFFCF9B80)),
                    )
                    ArtworkTile(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.glass_lab_artwork_two),
                        subtitle = stringResource(R.string.glass_lab_artwork_two_subtitle),
                        colors = listOf(Color(0xFF352B57), Color(0xFF7B4778), Color(0xFFD17D65)),
                    )
                    ArtworkTile(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.glass_lab_artwork_three),
                        subtitle = stringResource(R.string.glass_lab_artwork_three_subtitle),
                        colors = listOf(Color(0xFF1E3D36), Color(0xFF49796D), Color(0xFFB6A16E)),
                    )
                }

                Spacer(Modifier.height(24.dp))

                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "glass_lab_selection",
                ) { tab ->
                    Text(
                        text = stringResource(
                            when (tab) {
                                0 -> R.string.glass_lab_home_hint
                                1 -> R.string.glass_lab_movies_hint
                                2 -> R.string.glass_lab_shows_hint
                                else -> R.string.glass_lab_search_hint
                            },
                        ),
                        color = PlayerPalette.textTertiary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(Modifier.height(160.dp))
            }
        }

        // Give the floating control plane a calm lower edge and keep demo copy from
        // competing visually with its labels while content scrolls beneath it.
        Box(
            Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(176.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, PlayerPalette.background.copy(alpha = .98f)),
                    ),
                ),
        )

        GlassPreviewDock(
            hazeState = hazeState, enabled = glassEnabled, selected = selectedTab,
            onSelected = { selectedTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 18.dp).padding(bottom = 14.dp),
        )
    }
}

@Composable
private fun GlassLabBackdrop(selectedTab: Int) {
    val glowColors = when (selectedTab) {
        0 -> listOf(Color(0xFF28395A), Color(0xFF090B10))
        1 -> listOf(Color(0xFF4D3046), Color(0xFF090B10))
        2 -> listOf(Color(0xFF23493D), Color(0xFF090B10))
        else -> listOf(Color(0xFF4B3D63), Color(0xFF090B10))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(glowColors.first(), Color(0xFF11131A), PlayerPalette.background),
                ),
            ),
    )
}

@Composable
private fun ArtworkTile(
    modifier: Modifier,
    title: String,
    subtitle: String,
    colors: List<Color>,
) {
    Box(
        modifier = modifier
            .height(142.dp)
            .clip(PlayerCorners.artwork)
            .background(Brush.verticalGradient(colors)),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

