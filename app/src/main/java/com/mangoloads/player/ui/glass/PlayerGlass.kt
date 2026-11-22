package com.mangoloads.player.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.InteractionSource
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.glass.GlassReducedMotionPolicy
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.GlassTransformPivot
import dev.chrisbanes.haze.glass.GlassTransformTarget
import dev.chrisbanes.haze.glass.hazeGlass
import dev.chrisbanes.haze.glass.hazeGlassContainer

/**
 * Shared Player glass treatment.
 *
 * The effect samples only the explicitly registered Haze source. It never asks
 * Android for the current window backdrop and does not depend on native blur.
 * When disabled, callers get a stable, readable frosted fallback.
 */
@Composable
internal fun Modifier.playerGlassSurface(
    hazeState: HazeState,
    enabled: Boolean,
    interactionSource: InteractionSource? = null,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
): Modifier {
    if (!enabled) {
        return clip(shape)
            .background(Color(0xFF20242D), shape)
            .border(0.75.dp, Color.White.copy(alpha = 0.16f), shape)
    }

    val style = remember(shape) {
        GlassStyle.regular.then {
            shape(shape)
            tint(Color.White.copy(alpha = 0.11f))
            specularIntensity(0.58f)
            edgeSoftness(1.dp)
            edgeShadow(Color.Black.copy(alpha = 0.13f))
            lightPosition(androidx.compose.ui.Alignment.TopCenter)
            pressed {
                lightingIntensity(1f)
                refractionMultiplier(1.08f)
                whitePointDelta(0.04f)
                scale(0.985f)
            }
        }
    }

    return clip(shape).hazeGlass(
        input = HazeInput.Sources(hazeState),
        style = style,
        performanceMode = HazePerformanceMode.Default,
        interactionSource = interactionSource,
        interactionTransformTarget = GlassTransformTarget.MaterialOnly,
        interactionTransformPivot = GlassTransformPivot.Pointer,
        interactionReducedMotionPolicy = GlassReducedMotionPolicy.System,
    )
}

/** Groups nearby Player glass surfaces into one morphing material shape. */
internal fun Modifier.playerGlassContainer(
    enabled: Boolean,
    mergeDistance: Dp = 18.dp,
): Modifier = if (enabled) {
    hazeGlassContainer(mergeDistance = mergeDistance)
} else {
    this
}

