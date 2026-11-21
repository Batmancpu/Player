package com.batmancpu.player.ui.metadata

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.batmancpu.player.R
import com.batmancpu.player.ui.theme.PlayerPalette
import com.batmancpu.player.ui.theme.PlayerSpacing

@Composable
internal fun MetadataSettingsScreen(
    model: MetadataSettingsViewModel,
    onBack: () -> Unit,
    onOpenCredits: () -> Unit = {},
) {
    val state by model.state.collectAsState()
    MetadataSettingsContent(state, model::save, model::disable, model::clear, onBack, onOpenCredits)
}

/** Stateless persisted state; an unsaved token belongs only to this composition. */
@Composable
internal fun MetadataSettingsContent(
    state: MetadataSettingsUiState,
    onSave: (String?, Boolean) -> Unit,
    onDisable: () -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onOpenCredits: () -> Unit = {},
) {
    // Do not use rememberSaveable: a password must never enter the activity bundle.
    var token by remember { mutableStateOf("") }
    var enabled by remember(state.settings.enabled) { mutableStateOf(state.settings.enabled) }
    val focus = LocalFocusManager.current
    val activity = LocalContext.current.activity()
    DisposableEffect(activity, state.settings.enabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                token = ""
                enabled = state.settings.enabled
                focus.clearFocus()
            }
        }
        activity?.lifecycle?.addObserver(observer)
        onDispose { activity?.lifecycle?.removeObserver(observer) }
    }
    fun save() {
        if (state.loading) return
        val submitted = token.takeIf { it.isNotBlank() }
        token = ""
        focus.clearFocus()
        onSave(submitted, enabled)
    }
    Surface(Modifier.fillMaxSize(), color = PlayerPalette.background) {
        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PlayerSpacing.screenHorizontal, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            TextButton(onClick = { token = ""; focus.clearFocus(); onBack() }) {
                Text(stringResource(R.string.metadata_back))
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.metadata_kicker), style = MaterialTheme.typography.labelSmall, color = PlayerPalette.accentMuted)
                Text(stringResource(R.string.metadata_title), style = MaterialTheme.typography.displayMedium)
                Text(stringResource(R.string.metadata_description), style = MaterialTheme.typography.bodyLarge, color = PlayerPalette.textSecondary)
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
                            enabled = !state.loading && !state.settings.storageError,
                            onCheckedChange = { checked ->
                                enabled = checked
                                if (!checked) {
                                    token = ""
                                    focus.clearFocus()
                                    onDisable()
                                }
                            },
                        )
                    }
                    Text(stringResource(R.string.metadata_consent_description), color = PlayerPalette.textSecondary)
                    Text(stringResource(R.string.metadata_local_boundary), style = MaterialTheme.typography.bodySmall, color = PlayerPalette.textTertiary)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(if (state.settings.credentialConfigured) R.string.metadata_saved_token else R.string.metadata_missing_token),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(stringResource(R.string.metadata_token_description), color = PlayerPalette.textSecondary)
                OutlinedTextField(
                    value = token,
                    onValueChange = { if (it.length <= 2048) token = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.loading,
                    singleLine = true,
                    label = { Text(stringResource(if (state.settings.credentialConfigured) R.string.metadata_token_replace_label else R.string.metadata_token_label)) },
                    placeholder = { Text(stringResource(R.string.metadata_token_hint)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { save() }),
                    isError = state.settings.tokenRejected,
                    supportingText = if (state.settings.credentialConfigured) ({ Text(stringResource(R.string.metadata_token_retained)) }) else null,
                )
                Button(
                    onClick = ::save,
                    enabled = !state.loading && (token.isNotBlank() || state.settings.credentialConfigured),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(if (state.loading) R.string.metadata_saving else R.string.metadata_save)) }
                if (state.settings.credentialConfigured || state.settings.storageError) {
                    OutlinedButton(onClick = { token = ""; focus.clearFocus(); enabled = false; onClear() }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.metadata_remove))
                    }
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
            TextButton(onClick = { token = \"\"; focus.clearFocus(); onOpenCredits() }) {
                Text(stringResource(R.string.metadata_open_credits))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

private tailrec fun Context.activity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> if (baseContext === this) null else baseContext.activity()
    else -> null
}
