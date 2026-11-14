package com.batmancpu.player.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerThemeTest {

    @Test
    fun primaryTextMeetsHighContrastOnMainSurfaces() {
        assertTrue(
            "Primary text should meet a 7:1 contrast target on the base background",
            contrastRatio(PlayerPalette.textPrimary, PlayerPalette.background) >= 7f,
        )
        assertTrue(
            "Primary text should meet a 7:1 contrast target on elevated surfaces",
            contrastRatio(PlayerPalette.textPrimary, PlayerPalette.surfaceElevated) >= 7f,
        )
    }

    @Test
    fun secondaryAndTertiaryTextMeetAALargeTextContrastFloor() {
        assertTrue(
            "Secondary text should meet 4.5:1 on the base background",
            contrastRatio(PlayerPalette.textSecondary, PlayerPalette.background) >= 4.5f,
        )
        assertTrue(
            "Tertiary text should meet 4.5:1 on the main surface",
            contrastRatio(PlayerPalette.textTertiary, PlayerPalette.surface) >= 4.5f,
        )
    }

    @Test
    fun primarySurfacesRemainVisiblyDistinct() {
        assertNotEquals(PlayerPalette.background, PlayerPalette.surface)
        assertNotEquals(PlayerPalette.surface, PlayerPalette.surfaceElevated)
        assertNotEquals(PlayerPalette.surfaceElevated, PlayerPalette.surfacePressed)
    }

    private fun contrastRatio(foreground: Color, background: Color): Float {
        val first = foreground.luminance()
        val second = background.luminance()
        val lighter = maxOf(first, second)
        val darker = minOf(first, second)
        return (lighter + 0.05f) / (darker + 0.05f)
    }
}
