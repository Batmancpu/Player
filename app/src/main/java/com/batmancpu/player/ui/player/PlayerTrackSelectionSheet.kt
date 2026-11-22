package com.batmancpu.player.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import com.batmancpu.player.R
import com.batmancpu.player.ui.theme.PlayerPalette
import java.util.Locale

/** Compact, accessible in-app picker for the decoder-supported audio or subtitle tracks. */
@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
internal fun PlayerTrackSelectionSheet(
    player: Player,
    tracks: Tracks,
    trackType: Int,
    onDismiss: () -> Unit,
    onSelectionApplied: () -> Unit = {},
) {
    var selectionRevision by remember(player) { mutableIntStateOf(0) }
    val title = stringResource(if (trackType == C.TRACK_TYPE_AUDIO) R.string.player_audio_tracks else R.string.player_subtitle_tracks)
    val isSubtitle = trackType == C.TRACK_TYPE_TEXT
    val parameters = remember(player, tracks, selectionRevision) { player.trackSelectionParameters }
    val hasOverride = tracks.groups.any { it.type == trackType && parameters.overrides.containsKey(it.mediaTrackGroup) }
    val isDisabled = trackType in parameters.disabledTrackTypes
    val options = tracks.groups.filter { it.type == trackType && it.isSupported }.flatMap { group ->
        (0 until group.length).filter(group::isTrackSupported).map { index ->
            TrackOption(
                index,
                group,
                formatTrackLabel(group.getTrackFormat(index), index),
                parameters.overrides[group.mediaTrackGroup]?.trackIndices?.contains(index) == true,
            )
        }
    }
    val automaticSelected = !isDisabled && !hasOverride
    val offSelected = isSubtitle && isDisabled

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PlayerPalette.surface,
        contentColor = PlayerPalette.textPrimary,
    ) {
        Column(
            Modifier.fillMaxWidth().selectableGroup().padding(horizontal = 20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))
            TrackOptionRow(
                label = stringResource(R.string.player_tracks_automatic),
                selected = automaticSelected,
                onActivate = {
                    applyPlayerTrackSelection(player, trackType, null, null)
                    selectionRevision++
                    onSelectionApplied()
                    onDismiss()
                },
            )
            if (isSubtitle) {
                TrackOptionRow(
                    label = stringResource(R.string.player_subtitles_off),
                    selected = offSelected,
                    onActivate = {
                        applyPlayerTrackSelection(player, trackType, null, null, disabled = true)
                        selectionRevision++
                        onSelectionApplied()
                        onDismiss()
                    },
                )
            }
            options.forEach { option ->
                TrackOptionRow(
                    label = option.label,
                    selected = option.selected,
                    onActivate = {
                        applyPlayerTrackSelection(player, trackType, option.group, option.trackIndex)
                        selectionRevision++
                        onSelectionApplied()
                        onDismiss()
                    },
                )
            }
            if (options.isEmpty() && !isSubtitle) {
                Text(stringResource(R.string.player_tracks_unavailable), color = PlayerPalette.textSecondary)
            }
        }
    }
}

@Composable
private fun TrackOptionRow(label: String, selected: Boolean, onActivate: () -> Unit) {
    val state = stringResource(if (selected) R.string.nav_item_selected else R.string.nav_item_not_selected)
    Row(
        Modifier.fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onActivate)
            .semantics(mergeDescendants = true) {
                contentDescription = label
                this.selected = selected
                stateDescription = state
                onClick(label) {
                    onActivate()
                    true
                }
            }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
    }
}

private data class TrackOption(
    val trackIndex: Int,
    val group: Tracks.Group,
    val label: String,
    val selected: Boolean,
)

private fun formatTrackLabel(format: Format, index: Int): String {
    val label = format.label?.takeIf(String::isNotBlank)
        ?: format.language?.takeIf { it.isNotBlank() && it != "und" }?.let {
            Locale.forLanguageTag(it).getDisplayName(Locale.getDefault()).takeIf(String::isNotBlank)
        }
        ?: "Track ${index + 1}"
    val details = buildList {
        if (format.channelCount > 0) add("${format.channelCount} ch")
        if (format.sampleMimeType?.startsWith("text/") == true) add("Subtitle")
    }
    return if (details.isEmpty()) label else "$label · ${details.joinToString(" · ")}"
}

@androidx.annotation.OptIn(UnstableApi::class)
internal fun applyPlayerTrackSelection(
    player: Player,
    trackType: Int,
    group: Tracks.Group?,
    trackIndex: Int?,
    disabled: Boolean = false,
) {
    val parameters = player.trackSelectionParameters.buildUpon()
        .setTrackTypeDisabled(trackType, disabled)
    if (group == null || trackIndex == null) {
        parameters.clearOverridesOfType(trackType)
    } else {
        parameters.setTrackTypeDisabled(trackType, false)
            .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, listOf(trackIndex)))
    }
    player.trackSelectionParameters = parameters.build()
}
