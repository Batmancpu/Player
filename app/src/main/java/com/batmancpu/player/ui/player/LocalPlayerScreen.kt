package com.batmancpu.player.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.batmancpu.player.R
import com.batmancpu.player.ui.icons.PlayerGlyph
import com.batmancpu.player.ui.icons.PlayerGlyphIcon
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
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
    onIsPlayingChanged: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val videoSurfaceDescription = stringResource(R.string.player_video_surface)
    val checkpoint by rememberUpdatedState(onCheckpoint)
    val playingChanged by rememberUpdatedState(onIsPlayingChanged)
    var isPlaying by remember(target.uri) { mutableStateOf(false) }
    var error by remember(target.uri) { mutableStateOf(false) }
    var hasAudioTracks by remember(target.uri) { mutableStateOf(false) }
    var hasSubtitleTracks by remember(target.uri) { mutableStateOf(false) }
    var availableTracks by remember(target.uri) { mutableStateOf(Tracks.EMPTY) }
    var trackPickerType by remember(target.uri) { mutableIntStateOf(C.TRACK_TYPE_UNKNOWN) }
    var playbackSpeed by remember(target.uri) { mutableFloatStateOf(1f) }
    var controlsVisible by remember(target.uri) { mutableStateOf(true) }
    var chromeInteractionEpoch by remember(target.uri) { mutableIntStateOf(0) }
    var resizeMode by remember(target.uri) { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var isScrubbing by remember(target.uri) { mutableStateOf(false) }
    val player = remember(target.uri) {
        createLocalPlayer(context).apply {
            setMediaItem(createLocalMediaItem(target))
            seekTo(target.positionMs)
            prepare()
        }
    }
    val videoSurfaceInteraction = remember(player) { MutableInteractionSource() }
    var savedPlayIntent by remember(target.uri) { mutableStateOf(target.playWhenReady) }
    fun saveCurrentCheckpoint(playIntent: Boolean = player.playWhenReady) {
        val (position, effectivePlayIntent) = playbackResumeCheckpoint(
            player.playbackState, player.currentPosition, playIntent,
        )
        if (player.playbackState == Player.STATE_ENDED) savedPlayIntent = false
        checkpoint(position, effectivePlayIntent)
    }
    DisposableEffect(player, lifecycle) {
        var firstResume = true
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && firstResume) {
                firstResume = false
                player.playWhenReady = savedPlayIntent
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                savedPlayIntent = player.playWhenReady
                saveCurrentCheckpoint(savedPlayIntent)
                player.pause()
            }
        }
        val listener = object : Player.Listener {
            override fun onPlayerError(failure: PlaybackException) { error = true }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    // A completed item should start from the beginning when opened again.
                    savedPlayIntent = false
                    checkpoint(0L, false)
                    controlsVisible = true
                    chromeInteractionEpoch++
                }
            }
            override fun onIsPlayingChanged(value: Boolean) {
                isPlaying = value
                playingChanged(value)
            }
            override fun onPositionDiscontinuity(
                oldPosition: androidx.media3.common.Player.PositionInfo,
                newPosition: androidx.media3.common.Player.PositionInfo,
                reason: Int,
            ) {
                chromeInteractionEpoch++
            }
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                playbackSpeed = playbackParameters.speed
            }
            override fun onTracksChanged(tracks: Tracks) {
                chromeInteractionEpoch++
                availableTracks = tracks
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
            saveCurrentCheckpoint(savedPlayIntent)
            lifecycle.removeObserver(observer)
            player.removeListener(listener)
            player.release()
        }
    }
    LaunchedEffect(player) {
        while (true) {
            delay(5_000)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                saveCurrentCheckpoint()
            }
        }
    }
    LaunchedEffect(player, controlsVisible, isPlaying, isScrubbing, chromeInteractionEpoch) {
        if (controlsVisible && isPlaying && !isScrubbing) {
            delay(PLAYER_CHROME_AUTO_HIDE_MS)
            controlsVisible = false
        }
    }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { viewContext -> PlayerView(viewContext).apply {
                this.player = player
                setUseController(false)
            } },
            update = {
                it.keepScreenOn = isPlaying
                it.setResizeMode(resizeMode)
            },
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
        )
        Box(
            Modifier.fillMaxSize()
                .clickable(
                    interactionSource = videoSurfaceInteraction,
                    indication = null,
                    onClickLabel = videoSurfaceDescription,
                ) {
                if (!controlsVisible) {
                    controlsVisible = true
                }
                chromeInteractionEpoch++
            }
            .semantics { contentDescription = videoSurfaceDescription },
        )
        val audioTitle = stringResource(R.string.player_audio_tracks)
        val subtitleTitle = stringResource(R.string.player_subtitle_tracks)
        if (controlsVisible) Column(Modifier.align(Alignment.TopStart).fillMaxWidth().background(Color.Black.copy(alpha = .72f))
            .windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerBackButton(onClick = onClose)
                Text(target.label, color = Color.White, maxLines = 1, modifier = Modifier.weight(1f))
                PlayerResizeButton(resizeMode) {
                    resizeMode = nextPlayerResizeMode(resizeMode)
                    chromeInteractionEpoch++
                }
                PlayerFullscreenButton(isFullScreen) {
                    controlsVisible = true
                    chromeInteractionEpoch++
                    onToggleFullScreen()
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasAudioTracks) TextButton(onClick = {
                        chromeInteractionEpoch++
                        trackPickerType = C.TRACK_TYPE_AUDIO
                    }) { Text(audioTitle, color = Color.White) }
                    if (hasSubtitleTracks) TextButton(onClick = {
                        chromeInteractionEpoch++
                        trackPickerType = C.TRACK_TYPE_TEXT
                    }) { Text(subtitleTitle, color = Color.White) }
                }
                Spacer(Modifier.weight(1f))
                PlayerSpeedButton(playbackSpeed) { speed ->
                    player.setPlaybackParameters(PlaybackParameters(speed))
                    chromeInteractionEpoch++
                }
            }
        }
        if (controlsVisible) {
            PlayerTransportControls(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                player = player,
                controlsVisible = controlsVisible,
                isPlaying = isPlaying,
                onScrubbingChanged = { isScrubbing = it },
                onPlayPause = {
                    if (player.isPlaying) player.pause() else player.play()
                    chromeInteractionEpoch++
                },
                onSeekBy = { delta ->
                    val end = player.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: Long.MAX_VALUE
                    player.seekTo((player.currentPosition + delta).coerceIn(0L, end))
                    chromeInteractionEpoch++
                },
            )
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
        if (trackPickerType != C.TRACK_TYPE_UNKNOWN) {
            PlayerTrackSelectionSheet(
                player = player,
                tracks = availableTracks,
                trackType = trackPickerType,
                onDismiss = { trackPickerType = C.TRACK_TYPE_UNKNOWN },
                onSelectionApplied = { availableTracks = player.currentTracks },
            )
        }
    }
}

@Composable
private fun PlayerTransportControls(
    modifier: Modifier,
    player: Player,
    controlsVisible: Boolean,
    isPlaying: Boolean,
    onScrubbingChanged: (Boolean) -> Unit,
    onPlayPause: () -> Unit,
    onSeekBy: (Long) -> Unit,
) {
    var positionMs by remember(player) { mutableLongStateOf(player.currentPosition.coerceAtLeast(0L)) }
    var durationMs by remember(player) {
        mutableLongStateOf(player.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: 0L)
    }
    var scrubPositionMs by remember(player) { mutableLongStateOf(positionMs) }
    var isScrubbing by remember(player) { mutableStateOf(false) }
    LaunchedEffect(player, controlsVisible, isScrubbing) {
        while (controlsVisible && !isScrubbing) {
            positionMs = player.currentPosition.coerceAtLeast(0L)
            durationMs = player.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: 0L
            delay(250)
        }
    }
    val playLabel = stringResource(if (isPlaying) R.string.player_pause else R.string.player_play)
    val seekBack = stringResource(R.string.player_seek_back)
    val seekForward = stringResource(R.string.player_seek_forward)
    val duration = durationMs.coerceAtLeast(0L)
    val safePosition = positionMs.coerceIn(0L, duration.coerceAtLeast(0L))
    val positionLabel = stringResource(R.string.player_position)
    val positionValue = stringResource(
        R.string.player_position_value,
        formatPlaybackTime(safePosition),
        formatPlaybackTime(duration),
    )
    val playbackStateLabel = stringResource(if (isPlaying) R.string.player_playing else R.string.player_paused)
    Column(modifier.background(Color.Black.copy(alpha = .76f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        .windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = 24.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(formatPlaybackTime(safePosition), color = Color.White, style = MaterialTheme.typography.labelMedium)
            Slider(
                value = safePosition.toFloat(),
                onValueChange = {
                    if (!isScrubbing) onScrubbingChanged(true)
                    isScrubbing = true
                    scrubPositionMs = it.toLong().coerceIn(0L, duration)
                },
                onValueChangeFinished = {
                    val finalPosition = scrubPositionMs.coerceIn(0L, duration)
                    player.seekTo(finalPosition)
                    positionMs = finalPosition
                    isScrubbing = false
                    onScrubbingChanged(false)
                },
                valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
                enabled = duration > 0L,
                modifier = Modifier.weight(1f).semantics {
                    contentDescription = positionLabel
                    stateDescription = positionValue
                },
            )
            Text(formatPlaybackTime(duration), color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = { onSeekBy(-10_000L) }, modifier = Modifier.semantics { contentDescription = seekBack }) { Text("−10", color = Color.White) }
            Button(onClick = onPlayPause, modifier = Modifier.semantics {
                contentDescription = playLabel
                role = Role.Button
                stateDescription = playbackStateLabel
            }) { Text(if (isPlaying) "Ⅱ" else "▶") }
            TextButton(onClick = { onSeekBy(10_000L) }, modifier = Modifier.semantics { contentDescription = seekForward }) { Text("+10", color = Color.White) }
        }
    }
}

internal fun formatPlaybackTime(positionMs: Long): String {
    val seconds = positionMs.coerceAtLeast(0L) / 1_000L
    val hours = seconds / 3_600L
    val minutes = (seconds % 3_600L) / 60L
    val remainder = seconds % 60L
    return if (hours > 0L) "%d:%02d:%02d".format(hours, minutes, remainder)
    else "%d:%02d".format(minutes, remainder)
}


@Composable
internal fun PlayerFullscreenButton(isFullScreen: Boolean, onToggle: () -> Unit) {
    val label = stringResource(if (isFullScreen) R.string.player_exit_fullscreen else R.string.player_enter_fullscreen)
    IconButton(onClick = onToggle, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics { contentDescription = label }) {
        PlayerGlyphIcon(if (isFullScreen) PlayerGlyph.FULLSCREEN_EXIT else PlayerGlyph.FULLSCREEN, null, tint = Color.White)
    }
}


internal fun nextPlayerResizeMode(current: Int): Int = when (current) {
    AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_FILL
    AspectRatioFrameLayout.RESIZE_MODE_FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
}

@Composable
internal fun PlayerResizeButton(resizeMode: Int, onNext: () -> Unit) {
    val label = stringResource(
        when (resizeMode) {
            AspectRatioFrameLayout.RESIZE_MODE_FILL -> R.string.player_resize_fill
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> R.string.player_resize_zoom
            else -> R.string.player_resize_fit
        },
    )
    val description = stringResource(R.string.player_resize_description, label)
    IconButton(onClick = onNext, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics { contentDescription = description }) {
        PlayerGlyphIcon(PlayerGlyph.ASPECT_RATIO, null, tint = Color.White)
    }
}

@Composable
private fun PlayerBackButton(onClick: () -> Unit) {
    val label = stringResource(R.string.player_back)
    IconButton(onClick = onClick, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics { contentDescription = label }) {
        PlayerGlyphIcon(PlayerGlyph.ARROW_BACK, null, tint = Color.White)
    }
}


private const val PLAYER_CHROME_AUTO_HIDE_MS = 2_500L

/** Completed playback is stored as a paused start point, even during later lifecycle saves. */
internal fun playbackResumeCheckpoint(
    playbackState: Int,
    positionMs: Long,
    playWhenReady: Boolean,
): Pair<Long, Boolean> =
    if (playbackState == Player.STATE_ENDED) 0L to false
    else positionMs.coerceAtLeast(0L) to playWhenReady


@Composable
internal fun PlayerSpeedButton(speed: Float, onSpeedSelected: (Float) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val currentSpeed = formatPlayerSpeed(speed)
    val description = stringResource(R.string.player_speed_accessibility, currentSpeed)
    Box {
        TextButton(onClick = { expanded = true }, modifier = Modifier.semantics { contentDescription = description }) {
            Text(currentSpeed, color = Color.White)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PLAYER_SPEED_OPTIONS.forEach { option ->
                DropdownMenuItem(
                    text = { Text(formatPlayerSpeed(option)) },
                    onClick = { expanded = false; onSpeedSelected(option) },
                )
            }
        }
    }
}

internal fun formatPlayerSpeed(speed: Float): String =
    if (speed % 1f == 0f) "${speed.toInt()}×" else "${speed}×"

private val PLAYER_SPEED_OPTIONS = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)
