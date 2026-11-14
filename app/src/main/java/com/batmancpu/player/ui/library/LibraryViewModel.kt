package com.batmancpu.player.ui.library

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class FolderUiState(
    val folder: LocalFolder? = null,
    val busy: Boolean = true,
    val error: Boolean = false,
    val unsupported: Boolean = false,
)

/** Application context only: never retain an Activity or its composition. */
internal class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val access = FolderAccessManager(AndroidFolderAccessStore(application))
    private val mutableFolder = MutableStateFlow(FolderUiState())
    val folder = mutableFolder.asStateFlow()
    private var folderGeneration = 0L
    val session = LibrarySession(
        scope = viewModelScope,
        load = { tree -> OfflineLibraryIndex.activateAndLoad(application, Uri.parse(tree)) },
        scan = { tree -> SafLocalMediaSource(application, Uri.parse(tree)).scan() },
        save = { tree, media ->
            OfflineLibraryIndex.saveSuccessfulScan(application, Uri.parse(tree), media)
        },
    )

    init { restoreFolder() }

    fun restoreFolder() = updateFolder { access.restore() }
    fun connectFolder(tree: String) = updateFolder { access.connect(tree) }

    private fun updateFolder(action: suspend () -> LocalFolder?) {
        val ticket = ++folderGeneration
        session.select(null)
        mutableFolder.value = FolderUiState()
        viewModelScope.launch {
            try {
                val selected = action()
                if (ticket == folderGeneration) {
                    session.select(selected?.tree)
                    mutableFolder.value = FolderUiState(folder = selected, busy = false)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                if (ticket == folderGeneration) {
                    mutableFolder.value = FolderUiState(
                        busy = false, error = true, unsupported = failure is UnsupportedFolderException,
                    )
                }
            }
        }
    }
}
