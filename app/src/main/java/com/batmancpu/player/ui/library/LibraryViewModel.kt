package com.batmancpu.player.ui.library

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import com.batmancpu.player.ui.player.LocalPlaybackState
import com.batmancpu.player.ui.player.LocalPlaybackTarget
import com.batmancpu.player.ui.player.LocalPlaybackAccess
import com.batmancpu.player.ui.player.ResumePositions

internal data class FolderUiState(
    val folder: LocalFolder? = null,
    val busy: Boolean = true,
    val error: Boolean = false,
    val unsupported: Boolean = false,
    val forgetFailed: Boolean = false,
)

/** Application context only: never retain an Activity or its composition. */
internal class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val access = FolderAccessManager(AndroidFolderAccessStore(application))
    private val mutableFolder = MutableStateFlow(FolderUiState())
    val folder = mutableFolder.asStateFlow()
    private var folderGeneration = 0L
    private var pendingForgetTree: String? = null
    val session = LibrarySession(
        scope = viewModelScope,
        load = { tree -> OfflineLibraryIndex.activateAndLoad(application, Uri.parse(tree)) },
        scan = { tree -> SafLocalMediaSource(application, Uri.parse(tree)).scan() },
        save = { tree, media ->
            OfflineLibraryIndex.saveSuccessfulScan(application, Uri.parse(tree), media)
        },
    )

    val catalogue = session.state.map { it.tree to it.media }.distinctUntilChanged().map { (tree, media) ->
        withContext(Dispatchers.Default) { CatalogueSnapshot(tree, LocalCatalogue.build(media)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogueSnapshot())
    private val mutablePlayback = MutableStateFlow(LocalPlaybackState())
    val playback = mutablePlayback.asStateFlow()
    private var playbackGeneration = 0L

    fun play(media: DiscoveredLocalMedia) {
        val selected = folder.value.folder ?: return
        val canonical = session.state.value.media.firstOrNull { it.documentId == media.documentId && it.kind == LocalMediaKind.VIDEO }
            ?: return
        if (session.state.value.tree != selected.tree) return
        val ticket = ++playbackGeneration
        mutablePlayback.value = LocalPlaybackState(loading = true)
        viewModelScope.launch {
            try {
                val target = LocalPlaybackAccess.resolve(getApplication(), selected.tree, canonical.documentId, canonical.displayName)
                if (ticket == playbackGeneration && folder.value.folder?.tree == selected.tree) {
                    mutablePlayback.value = LocalPlaybackState(target = target)
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) {
                if (ticket == playbackGeneration) mutablePlayback.value = LocalPlaybackState(failed = true)
            }
        }
    }

    fun checkpoint(target: LocalPlaybackTarget, position: Long, playWhenReady: Boolean) {
        val current = mutablePlayback.value.target
        if (current?.uri == target.uri) mutablePlayback.value = LocalPlaybackState(
            target = current.copy(positionMs = position.coerceAtLeast(0), playWhenReady = playWhenReady),
        )
        viewModelScope.launch { ResumePositions.save(getApplication(), target, position) }
    }

    fun closePlayback() {
        playbackGeneration++
        mutablePlayback.value = LocalPlaybackState()
    }

    init { restoreFolder() }

    fun restoreFolder() = updateFolder { access.restore() }
    fun connectFolder(tree: String) = updateFolder { access.connect(tree) }

    /** Remove Player's cached index and read grant; original media stays untouched. */
    fun forgetFolder() {
        val tree = folder.value.folder?.tree ?: pendingForgetTree ?: return
        pendingForgetTree = tree
        val ticket = ++folderGeneration
        closePlayback()
        session.select(null)
        mutableFolder.value = FolderUiState()
        viewModelScope.launch {
            try {
                OfflineLibraryIndex.forgetTree(getApplication(), Uri.parse(tree))
                ResumePositions.clearAll(getApplication())
                access.forget()
                if (ticket == folderGeneration) {
                    pendingForgetTree = null
                    mutableFolder.value = FolderUiState(busy = false)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (ticket == folderGeneration) mutableFolder.value =
                    FolderUiState(busy = false, error = true, forgetFailed = true)
            }
        }
    }

    private fun updateFolder(action: suspend () -> LocalFolder?) {
        val ticket = ++folderGeneration
        closePlayback()
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
