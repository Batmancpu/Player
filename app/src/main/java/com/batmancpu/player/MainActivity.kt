package com.batmancpu.player

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
import androidx.lifecycle.ViewModelProvider
import com.batmancpu.player.ui.glass.GlassLabScreen
import com.batmancpu.player.ui.library.LocalLibraryScreen
import com.batmancpu.player.ui.player.LocalPlayerScreen
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.batmancpu.player.ui.library.LibraryViewModel
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

        val libraryModel = ViewModelProvider(this)[LibraryViewModel::class.java]
        setContent {
            var showGlassLab by rememberSaveable { mutableStateOf(false) }
            BackHandler(enabled = showGlassLab) { showGlassLab = false }

            val playback by libraryModel.playback.collectAsState()
            BackHandler(enabled = playback.target != null || playback.loading || playback.failed) { libraryModel.closePlayback() }
            PlayerTheme {
                val target = playback.target
                when {
                    target != null -> LocalPlayerScreen(
                        target = target,
                        lifecycle = lifecycle,
                        onCheckpoint = { position, play -> libraryModel.checkpoint(target, position, play) },
                        onClose = libraryModel::closePlayback,
                    )
                    showGlassLab -> GlassLabScreen()
                    else -> LocalLibraryScreen(libraryModel, onOpenGlassLab = { showGlassLab = true })
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

