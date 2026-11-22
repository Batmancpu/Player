/*
 * Glass rendering: Kyant0/backdrop v2.0.0, Apache-2.0, vendored with notices.
 * The integration and dock motion are adapted from Batmancpu/brrMusic
 * (GPL-3.0); see THIRD_PARTY_NOTICES.md and the repository LICENSE.
 */
package com.mangoloads.player.ui.glass

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mangoloads.player.ui.glass.backdrop.Backdrop
import com.mangoloads.player.ui.glass.backdrop.backdrops.LayerBackdrop
import com.mangoloads.player.ui.glass.backdrop.drawBackdrop
import com.mangoloads.player.ui.glass.backdrop.effects.blur
import com.mangoloads.player.ui.glass.backdrop.effects.colorControls
import com.mangoloads.player.ui.glass.backdrop.effects.lens
import com.mangoloads.player.ui.glass.backdrop.highlight.Highlight
import com.mangoloads.player.ui.glass.backdrop.highlight.HighlightElement
import com.mangoloads.player.ui.glass.backdrop.internal.ShapeProvider
import com.mangoloads.player.ui.glass.backdrop.shadow.Shadow

/** App-level Glass settings are intentionally owned by brrPlayer. */
val LocalLiquidGlassEnabled = staticCompositionLocalOf { false }
val LocalReduceDynamicBlur = staticCompositionLocalOf { false }
val LocalReduceMotion = staticCompositionLocalOf { false }
val LocalGlassIndicator = staticCompositionLocalOf { GlassIndicatorConfig() }

data class GlassIndicatorConfig(
    val blur: Float = .7f, val brightness: Float = 1f, val saturation: Float = 1f,
    val restLens: Boolean = true, val restRefractionWidth: Float = .55f,
    val restRefraction: Float = .35f, val depth: Float = .35f, val rim: Float = .8f,
    val iconLift: Float = .7f, val fill: Float = .9f, val squash: Float = 1f,
    val lift: Float = .75f,
    val innerRadius: Float = .45f, val innerShadow: Float = .25f,
    val liftRefraction: Float = .7f, val liftRefractionWidth: Float = .7f,
    val rainbow: Float = 0f, val shadow: Float = .35f,
)

val LocalAppBackdrop = staticCompositionLocalOf<Backdrop> { error("No app backdrop provided") }
internal val LocalMenuBackdrop = compositionLocalOf<Backdrop?> { null }
internal val LocalGlassExport = compositionLocalOf<LayerBackdrop?> { null }

fun isGlassSupported(sdkInt: Int = Build.VERSION.SDK_INT): Boolean = sdkInt >= Build.VERSION_CODES.S

private const val BACKDROP_SCALE = .55f
private const val BLUR_DP = 10f
private const val SURFACE_ALPHA = .30f
internal val GLASS_EDGE_WIDTH = .5.dp
internal val GLASS_EDGE_COLOR = Color.White.copy(alpha = .10f)

@Composable
fun glassContentColor(): Color = if (MaterialTheme.colorScheme.surface.luminance() > .5f) Color.Black else Color.White

@Composable
fun glassIndicatorColor(): Color = if (MaterialTheme.colorScheme.surface.luminance() > .5f) {
    Color.White.copy(alpha = .65f)
} else Color.White.copy(alpha = .30f)

@Composable
fun Modifier.lightweightLiquidGlass(shape: CornerBasedShape, fallbackColor: Color): Modifier {
    val enabled = LocalLiquidGlassEnabled.current && isGlassSupported()
    val tint = if (MaterialTheme.colorScheme.surface.luminance() > .5f) Color(0xFFFAFAFA) else Color(0xFF121212)
    val provider = ShapeProvider { shape }
    return clip(shape).background(if (enabled) tint.copy(alpha = SURFACE_ALPHA) else fallbackColor, shape)
        .then(if (enabled) HighlightElement(provider, highlight = { Highlight.Default.copy(alpha = .8f) }) else Modifier)
        .border(GLASS_EDGE_WIDTH, GLASS_EDGE_COLOR, shape)
}

@Composable
fun Modifier.liquidGlassRim(shape: CornerBasedShape): Modifier {
    if (!LocalLiquidGlassEnabled.current || !isGlassSupported()) return this
    val provider = ShapeProvider { shape }
    return then(HighlightElement(provider, highlight = { Highlight.Default.copy(alpha = .8f) }))
}

/** Source-backed, half-resolution Glass for small floating chrome only. */
@Composable
fun Modifier.liquidGlass(
    shape: CornerBasedShape,
    surfaceTint: Color? = null,
    refract: Boolean = true,
    source: Backdrop? = null,
): Modifier {
    if (!LocalLiquidGlassEnabled.current || !isGlassSupported()) return this
    if (LocalReduceDynamicBlur.current) {
        return background(MaterialTheme.colorScheme.surface, shape).border(GLASS_EDGE_WIDTH, GLASS_EDGE_COLOR, shape)
    }
    val backdrop = source ?: LocalAppBackdrop.current
    val exported = if (source == null) LocalGlassExport.current else null
    val density = LocalDensity.current
    val blurPx = with(density) { BLUR_DP.dp.toPx() } * BACKDROP_SCALE
    val lensDimension = with(density) { 24.dp.toPx() } * BACKDROP_SCALE
    val light = MaterialTheme.colorScheme.surface.luminance() > .5f
    val baseTint = if (light) Color(0xFFF2F2F7) else Color(0xFF121212)
    val tint = surfaceTint ?: baseTint.copy(alpha = if (light) .70f else SURFACE_ALPHA)
    val edge = if (light) Color.Black.copy(alpha = .12f) else GLASS_EDGE_COLOR
    val shadow = Shadow.Default.copy(color = Color.Black.copy(alpha = if (light) .18f else .1f))
    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            colorControls(saturation = 1.08f)
            blur(blurPx)
            if (refract && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                lens(refractionHeight = lensDimension, refractionAmount = lensDimension, depthEffect = .35f, chromaticAberration = false)
            }
        },
        highlight = { Highlight.Default.copy(alpha = .8f) },
        shadow = { shadow },
        onDrawSurface = { drawRect(color = tint, size = size) },
        exportedBackdrop = exported,
        backdropScale = BACKDROP_SCALE,
    ).border(GLASS_EDGE_WIDTH, edge, shape)
}
