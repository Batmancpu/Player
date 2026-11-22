package com.mangoloads.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Shared cinematic dark palette. Keep media artwork as the brightest visual layer;
 * controls and metadata use these tokens for predictable contrast.
 */
internal object PlayerPalette {
    val background = Color(0xFF08090B)
    val backgroundRaised = Color(0xFF0D0F13)
    val surface = Color(0xFF13161C)
    val surfaceElevated = Color(0xFF1B1F27)
    val surfacePressed = Color(0xFF252A34)

    val textPrimary = Color(0xFFF5F6F8)
    val textSecondary = Color(0xFFB6BBC5)
    val textTertiary = Color(0xFF7F8794)

    val accent = Color(0xFFE8EBF1)
    val accentMuted = Color(0xFFB8C0CE)
    val outline = Color(0x24FFFFFF)
    val outlineStrong = Color(0x42FFFFFF)
    val scrim = Color(0x99000000)
}

/** Consistent layout rhythm for shelves, grids, sheets and floating controls. */
internal object PlayerSpacing {
    val screenHorizontal = 24.dp
    val sectionVertical = 28.dp
    val sectionContent = 14.dp
    val row = 12.dp
    val grid = 10.dp
    val compact = 6.dp
    val touchTarget = 48.dp
}

/** Geometry tokens. Glass surfaces will use the same silhouette family. */
internal object PlayerCorners {
    val artwork = RoundedCornerShape(14.dp)
    val card = RoundedCornerShape(20.dp)
    val panel = RoundedCornerShape(26.dp)
    val sheet = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    val pill = RoundedCornerShape(percent = 50)
}

private val PlayerTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 42.sp,
        lineHeight = 46.sp,
        letterSpacing = (-1.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 39.sp,
        letterSpacing = (-0.9).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.65).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 25.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.4).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 19.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.05.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.15.sp,
    ),
)

private val PlayerShapes = Shapes(
    extraSmall = RoundedCornerShape(7.dp),
    small = RoundedCornerShape(11.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(30.dp),
)

@Composable
fun PlayerTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = PlayerPalette.accent,
        onPrimary = PlayerPalette.background,
        secondary = PlayerPalette.accentMuted,
        onSecondary = PlayerPalette.background,
        tertiary = PlayerPalette.textSecondary,
        onTertiary = PlayerPalette.background,
        background = PlayerPalette.background,
        onBackground = PlayerPalette.textPrimary,
        surface = PlayerPalette.surface,
        onSurface = PlayerPalette.textPrimary,
        surfaceVariant = PlayerPalette.surfaceElevated,
        onSurfaceVariant = PlayerPalette.textSecondary,
        outline = PlayerPalette.outlineStrong,
        outlineVariant = PlayerPalette.outline,
        scrim = PlayerPalette.scrim,
    )

    MaterialTheme(
        colorScheme = colors,
        typography = PlayerTypography,
        shapes = PlayerShapes,
        content = content,
    )
}
