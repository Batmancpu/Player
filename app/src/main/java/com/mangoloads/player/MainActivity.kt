package com.mangoloads.player

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModelProvider
import com.mangoloads.player.ui.glass.GlassLabScreen
import com.mangoloads.player.ui.library.LocalLibraryScreen
import com.mangoloads.player.ui.player.LocalPlayerScreen
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mangoloads.player.ui.library.LibraryViewModel
import com.mangoloads.player.ui.metadata.MetadataCreditsScreen
import com.mangoloads.player.ui.metadata.MetadataSettingsScreen
import com.mangoloads.player.ui.metadata.MetadataSettingsViewModel
import com.mangoloads.player.ui.theme.PlayerTheme

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

        val libraryModel = ViewModelProvider(this)[LibraryViewModel::class.java]
        val metadataModel = ViewModelProvider(this)[MetadataSettingsViewModel::class.java]
        setContent {
            var showGlassLab by rememberSaveable { mutableStateOf(false) }
            var immersivePlayback by rememberSaveable { mutableStateOf(false) }
            var showMetadataSettings by rememberSaveable { mutableStateOf(false) }
            var showMetadataCredits by rememberSaveable { mutableStateOf(false) }
            BackHandler(enabled = showMetadataCredits || showMetadataSettings || showGlassLab) {
                when {
                    showMetadataCredits -> showMetadataCredits = false
                    showMetadataSettings -> showMetadataSettings = false
                    showGlassLab -> showGlassLab = false
                }
            }

            val playback by libraryModel.playback.collectAsState()
            LaunchedEffect(playback.target?.uri) { immersivePlayback = false }
            DisposableEffect(playback.target?.uri, immersivePlayback) {
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                if (playback.target != null && immersivePlayback) {
                    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
                onDispose { controller.show(WindowInsetsCompat.Type.systemBars()) }
            }
            BackHandler(enabled = playback.target != null || playback.loading || playback.failed) { libraryModel.closePlayback() }
            PlayerTheme {
                val target = playback.target
                when {
                    target != null -> LocalPlayerScreen(
                        target = target,
                        lifecycle = lifecycle,
                        onCheckpoint = { position, play -> libraryModel.checkpoint(target, position, play) },
                        onClose = libraryModel::closePlayback,
                        isFullScreen = immersivePlayback,
                        onToggleFullScreen = { immersivePlayback = !immersivePlayback },
                    )
                    showMetadataCredits -> MetadataCreditsScreen(onBack = { showMetadataCredits = false })
                    showMetadataSettings -> MetadataSettingsScreen(
                        metadataModel,
                        onBack = { showMetadataSettings = false },
                        onOpenCredits = { showMetadataCredits = true },
                    )
                    showGlassLab -> GlassLabScreen(onBack = { showGlassLab = false })
                    else -> LocalLibraryScreen(
                        libraryModel,
                        onOpenGlassLab = { showGlassLab = true },
                        onOpenMetadataSettings = { showMetadataSettings = true },
                    )
                }
                if (playback.loading) AlertDialog(
                    onDismissRequest = libraryModel::closePlayback,
                    title = { Text(stringResource(R.string.player_opening)) },
                    text = { LinearProgressIndicator(Modifier.fillMaxWidth()) },
                    confirmButton = { TextButton(onClick = libraryModel::closePlayback) { Text(stringResource(android.R.string.cancel)) } },
                )
                if (playback.failed) AlertDialog(
                    onDismissRequest = libraryModel::closePlayback,
                    title = { Text(stringResource(R.string.player_unavailable)) },
                    text = { Text(stringResource(R.string.player_access_error)) },
                    confirmButton = { TextButton(onClick = libraryModel::closePlayback) { Text(stringResource(android.R.string.ok)) } },
                )
            }
        }
    }
}

