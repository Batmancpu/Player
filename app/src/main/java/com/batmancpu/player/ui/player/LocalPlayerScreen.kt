package com.batmancpu.player.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.batmancpu.player.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
internal fun LocalPlayerScreen(
    target: LocalPlaybackTarget,
    lifecycle: Lifecycle,
    onCheckpoint: (Long, Boolean) -> Unit,
    onClose: () -> Unit,
    isFullScreen: Boolean,
    onToggleFullScreen: () -> Unit,
) {
    val context = LocalContext.current
    val checkpoint by rememberUpdatedState(onCheckpoint)
    var isPlaying by remember(target.uri) { mutableStateOf(false) }
    var error by remember(target.uri) { mutableStateOf(false) }
    var hasAudioTracks by remember(target.uri) { mutableStateOf(false) }
    var hasSubtitleTracks by remember(target.uri) { mutableStateOf(false) }
    var controlsVisible by remember(target.uri) { mutableStateOf(true) }
    val playerViewRef = remember(target.uri) { arrayOfNulls<PlayerView>(1) }
    val player = remember(target.uri) {
        createLocalPlayer(context).apply {
            setMediaItem(createLocalMediaItem(target))
            seekTo(target.positionMs)
            prepare()
        }
    }
    var savedPlayIntent by remember(target.uri) { mutableStateOf(target.playWhenReady) }
    DisposableEffect(player, lifecycle) {
        var firstResume = true
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && firstResume) {
                firstResume = false
                player.playWhenReady = savedPlayIntent
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                savedPlayIntent = player.playWhenReady
                checkpoint(player.currentPosition, savedPlayIntent)
                player.pause()
            }
        }
        val listener = object : Player.Listener {
            override fun onPlayerError(failure: PlaybackException) { error = true }
            override fun onIsPlayingChanged(value: Boolean) { isPlaying = value }
            override fun onTracksChanged(tracks: Tracks) {
                hasAudioTracks = hasSupportedTrackType(tracks, C.TRACK_TYPE_AUDIO)
                hasSubtitleTracks = hasSupportedTrackType(tracks, C.TRACK_TYPE_TEXT)
            }
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) savedPlayIntent = playWhenReady
            }
        }
        player.addListener(listener)
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            firstResume = false
            player.playWhenReady = savedPlayIntent
        }
        onDispose {
            checkpoint(player.currentPosition, savedPlayIntent)
            lifecycle.removeObserver(observer)
            player.removeListener(listener)
            playerViewRef[0] = null
            player.release()
        }
    }
    LaunchedEffect(player) {
        while (true) {
            delay(5_000)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                checkpoint(player.currentPosition, player.playWhenReady)
            }
        }
    }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { viewContext -> PlayerView(viewContext).apply {
                playerViewRef[0] = this
                this.player = player
                setShowNextButton(false)
                setShowPreviousButton(false)
                setShowSubtitleButton(false)
                setControllerShowTimeoutMs(2_500)
                setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
                    controlsVisible = visibility == View.VISIBLE
                })
            } },
            update = { it.keepScreenOn = isPlaying },
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
        )
        val audioTitle = stringResource(R.string.player_audio_tracks)
        val subtitleTitle = stringResource(R.string.player_subtitle_tracks)
        if (controlsVisible) Column(Modifier.align(Alignment.TopStart).fillMaxWidth().background(Color.Black.copy(alpha = .72f))
            .windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onClose) { Text(stringResource(R.string.player_back), color = Color.White) }
                Text(target.label, color = Color.White, maxLines = 1, modifier = Modifier.weight(1f))
                PlayerFullscreenButton(isFullScreen) {
                    playerViewRef[0]?.showController()
                    onToggleFullScreen()
                }
            }
            if (hasAudioTracks || hasSubtitleTracks) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (hasAudioTracks) TextButton(onClick = {
                        createTrackSelectionDialog(context, audioTitle, player, C.TRACK_TYPE_AUDIO).show()
                    }) { Text(audioTitle, color = Color.White) }
                    if (hasSubtitleTracks) TextButton(onClick = {
                        createTrackSelectionDialog(context, subtitleTitle, player, C.TRACK_TYPE_TEXT).show()
                    }) { Text(subtitleTitle, color = Color.White) }
                }
            }
        }
        if (error) {
            Surface(Modifier.align(Alignment.Center).padding(24.dp), shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.player_unavailable), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.player_error_detail))
                    Button(onClick = onClose) { Text(stringResource(R.string.player_back)) }
                }
            }
        }
    }
}


@Composable
internal fun PlayerFullscreenButton(isFullScreen: Boolean, onToggle: () -> Unit) {
    val label = stringResource(if (isFullScreen) R.string.player_exit_fullscreen else R.string.player_enter_fullscreen)
    TextButton(onClick = onToggle, modifier = Modifier.semantics { contentDescription = label }) { Text(label, color = Color.White) }
}
