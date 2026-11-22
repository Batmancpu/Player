package com.mangoloads.player.ui.metadata

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mangoloads.player.R
import com.mangoloads.player.ui.theme.PlayerPalette
import com.mangoloads.player.ui.theme.PlayerSpacing

@Composable
internal fun MetadataSettingsScreen(
    model: MetadataSettingsViewModel,
    onBack: () -> Unit,
    onOpenCredits: () -> Unit = {},
) {
    val state by model.state.collectAsState()
    MetadataSettingsContent(state, model::save, model::disable, model::clear, onBack, onOpenCredits)
}

/** Consent only; publisher credentials are never entered or displayed here. */
@Composable
internal fun MetadataSettingsContent(
    state: MetadataSettingsUiState,
    onSave: (Boolean) -> Unit,
    onDisable: () -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onOpenCredits: () -> Unit = {},
) {
    var enabled by remember(state.settings.enabled) { mutableStateOf(state.settings.enabled) }
    val activity = LocalContext.current.activity()
    DisposableEffect(activity, state.settings.enabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                enabled = state.settings.enabled
            }
        }
        activity?.lifecycle?.addObserver(observer)
        onDispose { activity?.lifecycle?.removeObserver(observer) }
    }
    fun save() {
        if (state.loading) return
        onSave(enabled)
    }
    Surface(Modifier.fillMaxSize(), color = PlayerPalette.background) {
        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PlayerSpacing.screenHorizontal, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.metadata_back))
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.metadata_kicker), style = MaterialTheme.typography.labelSmall, color = PlayerPalette.accentMuted)
                Text(stringResource(R.string.metadata_title), style = MaterialTheme.typography.displayMedium)
                Text(stringResource(R.string.metadata_description), style = MaterialTheme.typography.bodyLarge, color = PlayerPalette.textSecondary)
            }
            TextButton(onClick = onOpenCredits) {
                Text(stringResource(R.string.metadata_open_credits))
            }
            if (state.loading && !state.settings.credentialConfigured) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.metadata_preparing), color = PlayerPalette.textSecondary)
                }
            }
            Surface(color = PlayerPalette.surface, shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.metadata_consent_title), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                        Switch(
                            checked = enabled,
                            enabled = !state.loading && !state.settings.storageError &&
                                (state.settings.appCredentialAvailable || state.settings.enabled),
                            onCheckedChange = { checked ->
                                enabled = checked
                                if (!checked) {
                                    onDisable()
                                }
                            },
                        )
                    }
                    Text(stringResource(R.string.metadata_consent_description), color = PlayerPalette.textSecondary)
                    Text(stringResource(R.string.metadata_local_boundary), style = MaterialTheme.typography.bodySmall, color = PlayerPalette.textTertiary)
                }
            }
            Text(
                stringResource(when {
                    state.settings.appCredentialAvailable -> R.string.metadata_app_credential_status
                    state.settings.credentialConfigured -> R.string.metadata_previous_credential_status
                    else -> R.string.metadata_app_credential_missing
                }),
                color = PlayerPalette.textSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
            if (state.settings.appCredentialAvailable) {
                Button(onClick = ::save, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(if (state.loading) R.string.metadata_saving else R.string.metadata_save))
                }
            } else if (state.settings.credentialConfigured || state.settings.storageError) {
                OutlinedButton(onClick = { enabled = false; onClear() }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.metadata_remove_previous_token))
                }
            }
            val message = when {
                state.settings.storageError -> R.string.metadata_storage_error
                state.settings.tokenRejected -> R.string.metadata_token_error
                state.notice == MetadataSettingsNotice.SAVED -> R.string.metadata_saved
                state.notice == MetadataSettingsNotice.DISABLED -> R.string.metadata_disabled
                state.notice == MetadataSettingsNotice.CLEARED -> R.string.metadata_cleared
                else -> null
            }
            if (message != null) {
                Text(
                    stringResource(message),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    color = if (state.settings.storageError || state.settings.tokenRejected) MaterialTheme.colorScheme.error else PlayerPalette.accent,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(stringResource(R.string.metadata_foundation_note), style = MaterialTheme.typography.bodySmall, color = PlayerPalette.textTertiary)
            Spacer(Modifier.height(12.dp))
        }
    }
}

private tailrec fun Context.activity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> if (baseContext === this) null else baseContext.activity()
    else -> null
}
