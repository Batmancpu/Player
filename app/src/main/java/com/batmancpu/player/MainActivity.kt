package com.batmancpu.player

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.batmancpu.player.ui.glass.GlassLabScreen
import com.batmancpu.player.ui.library.FolderSetupScreen
import com.batmancpu.player.ui.theme.PlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        window.isStatusBarContrastEnforced = false
        window.isNavigationBarContrastEnforced = false
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setContent {
            var showGlassLab by rememberSaveable { mutableStateOf(false) }
            BackHandler(enabled = showGlassLab) { showGlassLab = false }

            PlayerTheme {
                if (showGlassLab) {
                    GlassLabScreen()
                } else {
                    FolderSetupScreen(onOpenGlassLab = { showGlassLab = true })
                }
            }
        }
    }
}
