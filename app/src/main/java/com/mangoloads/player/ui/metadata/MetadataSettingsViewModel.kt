package com.mangoloads.player.ui.metadata

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal enum class MetadataSettingsNotice { NONE, SAVED, DISABLED, CLEARED }

/** UI state deliberately never carries a credential or an exception message. */
internal data class MetadataSettingsUiState(
    val settings: MetadataSettingsState = MetadataSettingsState(),
    val loading: Boolean = true,
    val notice: MetadataSettingsNotice = MetadataSettingsNotice.NONE,
)

internal class MetadataSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val store = MetadataSettingsStore(application)
    private val mutableState = MutableStateFlow(MetadataSettingsUiState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch { publish { store.load() } }
    }

    fun reload() = change(MetadataSettingsNotice.NONE) { store.load() }

    /** Only the publisher-configured app credential is exposed to the UI flow. */
    fun save(enabled: Boolean) = change(MetadataSettingsNotice.SAVED) {
        store.save(token = null, enabled = enabled)
    }

    fun disable() = change(MetadataSettingsNotice.DISABLED) { store.disable() }

    fun clear() = change(MetadataSettingsNotice.CLEARED) { store.clear() }

    private fun change(
        notice: MetadataSettingsNotice,
        operation: suspend () -> MetadataSettingsState,
    ) {
        // Buttons and input are disabled during a write. This also rejects duplicate
        // calls from accessibility/rapid taps instead of retaining queued secrets.
        if (mutableState.value.loading) return
        mutableState.value = mutableState.value.copy(loading = true, notice = MetadataSettingsNotice.NONE)
        viewModelScope.launch { publish(notice, operation) }
    }

    private suspend fun publish(
        notice: MetadataSettingsNotice = MetadataSettingsNotice.NONE,
        operation: suspend () -> MetadataSettingsState,
    ) {
        val settings = try {
            operation()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            MetadataSettingsState(storageError = true)
        }
        mutableState.value = MetadataSettingsUiState(
            settings = settings,
            loading = false,
            notice = notice.takeUnless { settings.storageError || settings.tokenRejected }
                ?: MetadataSettingsNotice.NONE,
        )
    }
}
