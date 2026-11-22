package com.mangoloads.player.ui.library

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal data class LibrarySessionState(
    val tree: String? = null,
    val media: List<DiscoveredLocalMedia> = emptyList(),
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val hasError: Boolean = false,
    val hasCompletedScan: Boolean = false,
    val wasCancelled: Boolean = false,
)

/**
 * Main-thread session commands; injected operations never publish UI state directly.
 * A mutex waits for cancelled work to unwind before starting another operation.
 * Generation checks also reject late results/finalizers from obsolete operations.
 */
internal class LibrarySession(
    private val scope: CoroutineScope,
    private val load: suspend (String) -> List<DiscoveredLocalMedia>,
    private val scan: suspend (String) -> List<DiscoveredLocalMedia>,
    private val save: suspend (String, List<DiscoveredLocalMedia>) -> Unit,
) {
    private val mutableState = MutableStateFlow(LibrarySessionState())
    val state: StateFlow<LibrarySessionState> = mutableState.asStateFlow()
    private val operationMutex = Mutex()
    private var operation: Job? = null
    private var generation = 0L

    fun select(tree: String?) {
        if (tree == state.value.tree) return
        if (tree == null) {
            generation++
            operation?.cancel()
            mutableState.value = LibrarySessionState()
            return
        }
        launchOperation(LibrarySessionState(tree = tree, isLoading = true)) {
            load(tree)
        }
    }

    fun startScan() {
        val current = state.value
        val tree = current.tree ?: return
        // Finish cache restoration first so cancellation/failure retains that snapshot.
        if (current.isLoading || current.isScanning) return
        launchOperation(current.copy(isScanning = true, hasError = false, wasCancelled = false)) {
            val results = scan(tree)
            currentCoroutineContext().ensureActive()
            save(tree, results)
            results
        }
    }

    fun cancelScan() {
        if (!state.value.isScanning) return
        generation++
        operation?.cancel()
        mutableState.value = state.value.copy(isScanning = false, wasCancelled = true)
    }

    private fun launchOperation(
        initial: LibrarySessionState,
        action: suspend () -> List<DiscoveredLocalMedia>,
    ) {
        val ticket = ++generation
        operation?.cancel()
        mutableState.value = initial
        operation = scope.launch {
            try {
                operationMutex.withLock {
                    currentCoroutineContext().ensureActive()
                    val results = action()
                    currentCoroutineContext().ensureActive()
                    if (ticket == generation) {
                        mutableState.value = state.value.copy(
                            media = results,
                            hasCompletedScan = initial.isScanning || state.value.hasCompletedScan,
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (ticket == generation) {
                    mutableState.value = state.value.copy(hasError = true)
                }
            } finally {
                if (ticket == generation) {
                    mutableState.value = state.value.copy(isLoading = false, isScanning = false)
                }
            }
        }
    }
}
