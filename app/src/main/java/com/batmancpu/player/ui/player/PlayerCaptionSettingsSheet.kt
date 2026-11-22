package com.batmancpu.player.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import com.batmancpu.player.R
import com.batmancpu.player.ui.glass.PlayerUiSettingsStore
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayerCaptionSettingsSheet(
    textSize: Float,
    bottomPadding: Float,
    onTextSizeChange: (Float) -> Unit,
    onTextSizeChangeFinished: (Float) -> Unit,
    onBottomPaddingChange: (Float) -> Unit,
    onBottomPaddingChangeFinished: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    var currentTextSize by remember(textSize) { mutableFloatStateOf(textSize) }
    var currentBottomPadding by remember(bottomPadding) { mutableFloatStateOf(bottomPadding) }
    val textSizeLabel = stringResource(R.string.player_caption_size)
    val positionLabel = stringResource(R.string.player_caption_position)
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier.fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.player_caption_appearance), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.player_caption_appearance_description), style = MaterialTheme.typography.bodyMedium)

            Text(stringResource(R.string.player_caption_size_value, (textSize * 100).roundToInt()), style = MaterialTheme.typography.labelLarge)
            Slider(
                value = currentTextSize,
                onValueChange = {
                    currentTextSize = it
                    onTextSizeChange(it)
                },
                onValueChangeFinished = { onTextSizeChangeFinished(currentTextSize) },
                valueRange = CAPTION_TEXT_SIZE_RANGE,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = textSizeLabel
                    progressBarRangeInfo = ProgressBarRangeInfo(currentTextSize, CAPTION_TEXT_SIZE_RANGE)
                    setProgress { requested ->
                        val value = requested.coerceIn(CAPTION_TEXT_SIZE_RANGE.start, CAPTION_TEXT_SIZE_RANGE.endInclusive)
                        currentTextSize = value
                        onTextSizeChange(value)
                        onTextSizeChangeFinished(value)
                        true
                    }
                },
            )

            Text(stringResource(R.string.player_caption_position_value, (bottomPadding * 100).roundToInt()), style = MaterialTheme.typography.labelLarge)
            Slider(
                value = currentBottomPadding,
                onValueChange = {
                    currentBottomPadding = it
                    onBottomPaddingChange(it)
                },
                onValueChangeFinished = { onBottomPaddingChangeFinished(currentBottomPadding) },
                valueRange = CAPTION_BOTTOM_PADDING_RANGE,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = positionLabel
                    progressBarRangeInfo = ProgressBarRangeInfo(currentBottomPadding, CAPTION_BOTTOM_PADDING_RANGE)
                    setProgress { requested ->
                        val value = requested.coerceIn(CAPTION_BOTTOM_PADDING_RANGE.start, CAPTION_BOTTOM_PADDING_RANGE.endInclusive)
                        currentBottomPadding = value
                        onBottomPaddingChange(value)
                        onBottomPaddingChangeFinished(value)
                        true
                    }
                },
            )

            TextButton(onClick = {
                currentTextSize = DEFAULT_CAPTION_TEXT_SIZE
                onTextSizeChange(DEFAULT_CAPTION_TEXT_SIZE)
                onTextSizeChangeFinished(DEFAULT_CAPTION_TEXT_SIZE)
                currentBottomPadding = DEFAULT_CAPTION_BOTTOM_PADDING
                onBottomPaddingChange(DEFAULT_CAPTION_BOTTOM_PADDING)
                onBottomPaddingChangeFinished(DEFAULT_CAPTION_BOTTOM_PADDING)
            }) {
                Text(stringResource(R.string.player_caption_reset))
            }
        }
    }
}

internal const val DEFAULT_CAPTION_TEXT_SIZE = PlayerUiSettingsStore.DEFAULT_CAPTION_TEXT_SIZE
internal const val DEFAULT_CAPTION_BOTTOM_PADDING = PlayerUiSettingsStore.DEFAULT_CAPTION_BOTTOM_PADDING
internal val CAPTION_TEXT_SIZE_RANGE = PlayerUiSettingsStore.MIN_CAPTION_TEXT_SIZE..PlayerUiSettingsStore.MAX_CAPTION_TEXT_SIZE
internal val CAPTION_BOTTOM_PADDING_RANGE = PlayerUiSettingsStore.MIN_CAPTION_BOTTOM_PADDING..PlayerUiSettingsStore.MAX_CAPTION_BOTTOM_PADDING
