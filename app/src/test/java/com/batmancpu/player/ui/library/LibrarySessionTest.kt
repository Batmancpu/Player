package com.batmancpu.player.ui.library

import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibrarySessionTest {
    private fun media(id: String) = listOf(DiscoveredLocalMedia(id, "$id.mp4", LocalMediaKind.VIDEO))

    @Test(timeout = 10_000)
    fun finishesCacheRestorationBeforeAcceptingScan() = runBlocking {
        val cache = CompletableDeferred<List<DiscoveredLocalMedia>>()
        val scanned = media("new")
        var scans = 0
        val saved = mutableListOf<List<DiscoveredLocalMedia>>()
        val session = LibrarySession(this, { cache.await() }, { scans++; scanned }, { _, rows -> saved.add(rows) })
        session.select("A")
        session.startScan()
        assertEquals(0, scans)
        cache.complete(media("cached"))
        session.state.first { !it.isLoading }
        session.startScan()
        val done = session.state.first { !it.isScanning }
        assertEquals(scanned, done.media)
        assertEquals(listOf(scanned), saved)
    }

    @Test(timeout = 10_000)
    fun scanFailurePreservesPreviouslyLoadedLibrary() = runBlocking {
        val cached = media("cached")
        var saves = 0
        val session = LibrarySession(this, { cached }, { throw IOException("synthetic failure") }, { _, _ -> saves++ })
        session.select("A")
        session.state.first { !it.isLoading }
        session.startScan()
        val done = session.state.first { !it.isScanning }
        assertTrue(done.hasError)
        assertEquals(cached, done.media)
        assertEquals(0, saves)
    }

    @Test(timeout = 10_000)
    fun cancelledOldScanCannotSaveOrClearNewScanStatus() = runBlocking {
        val oldStarted = CompletableDeferred<Unit>()
        val oldRelease = CompletableDeferred<Unit>()
        val newStarted = CompletableDeferred<Unit>()
        val newRelease = CompletableDeferred<Unit>()
        val saved = mutableListOf<List<DiscoveredLocalMedia>>()
        var scans = 0
        val session = LibrarySession(this, { media("cached") }, {
            if (++scans == 1) {
                oldStarted.complete(Unit)
                withContext(NonCancellable) { oldRelease.await() }
                media("obsolete")
            } else {
                newStarted.complete(Unit)
                newRelease.await()
                media("new")
            }
        }, { _, rows -> saved.add(rows) })
        session.select("A")
        session.state.first { !it.isLoading }
        session.startScan()
        oldStarted.await()
        session.cancelScan()
        assertTrue(session.state.value.wasCancelled)
        assertEquals(media("cached"), session.state.value.media)
        session.startScan()
        oldRelease.complete(Unit)
        newStarted.await()
        assertTrue(session.state.value.isScanning)
        assertTrue(saved.isEmpty())
        newRelease.complete(Unit)
        session.state.first { !it.isScanning }
        assertEquals(listOf(media("new")), saved)
        assertEquals(media("new"), session.state.value.media)
        assertFalse(session.state.value.wasCancelled)
    }

    @Test(timeout = 10_000)
    fun emptyCacheDoesNotClaimCompletedScanButSuccessfulEmptyScanDoes() = runBlocking {
        val session = LibrarySession(this, { emptyList() }, { emptyList() }, { _, _ -> Unit })
        session.select("A")
        session.state.first { !it.isLoading }
        assertFalse(session.state.value.hasCompletedScan)
        session.startScan()
        session.state.first { !it.isScanning }
        assertTrue(session.state.value.hasCompletedScan)
    }

    @Test(timeout = 10_000)
    fun rapidAToBToARejectsOldResultsEvenThoughTreeIdentityMatchesAgain() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val loads = mutableListOf<String>()
        val session = LibrarySession(this, { tree ->
            loads.add(tree)
            if (loads.size == 1) {
                started.complete(Unit)
                withContext(NonCancellable) { release.await() }
                media("old-A")
            } else media("fresh-A")
        }, { error("no scan expected") }, { _, _ -> error("no save expected") })
        session.select("A")
        started.await()
        session.select("B")
        session.select("A")
        assertTrue(session.state.value.media.isEmpty())
        release.complete(Unit)
        val done = session.state.first { !it.isLoading }
        assertEquals("A", done.tree)
        assertEquals(media("fresh-A"), done.media)
        assertFalse(done.hasError)
    }

    @Test(timeout = 10_000)
    fun switchingFolderWaitsForOldSaveToUnwindAndNeverPublishesItsRows() = runBlocking {
        val saving = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val order = mutableListOf<String>()
        val session = LibrarySession(this, { tree ->
            order.add("load-$tree")
            media(tree)
        }, { media("scanned-A") }, { _, _ ->
            order.add("save-start")
            saving.complete(Unit)
            withContext(NonCancellable) { release.await() }
            order.add("save-end")
        })
        session.select("A")
        session.state.first { !it.isLoading }
        session.startScan()
        saving.await()
        session.select("B")
        assertEquals("B", session.state.value.tree)
        assertTrue(session.state.value.media.isEmpty())
        assertFalse("load-B" in order)
        release.complete(Unit)
        val done = session.state.first { !it.isLoading }
        assertEquals(listOf("load-A", "save-start", "save-end", "load-B"), order)
        assertEquals(media("B"), done.media)
    }

    @Test(timeout = 10_000)
    fun reattachingSameFolderKeepsRunningScanAndOwnerCancellationPreventsSave() = runBlocking {
        val owner = SupervisorJob()
        val scope = CoroutineScope(coroutineContext + owner)
        val started = CompletableDeferred<Unit>()
        val finished = CompletableDeferred<Unit>()
        var loads = 0
        var saves = 0
        val session = LibrarySession(scope, { loads++; media("cached") }, {
            started.complete(Unit)
            try {
                CompletableDeferred<Unit>().await()
                media("unreachable")
            } finally { finished.complete(Unit) }
        }, { _, _ -> saves++ })
        try {
            session.select("A")
            session.state.first { !it.isLoading }
            session.startScan()
            started.await()
            session.select("A")
            assertEquals(1, loads)
            assertTrue(session.state.value.isScanning)
            scope.cancel()
            finished.await()
            owner.join()
            assertEquals(0, saves)
        } finally { scope.cancel() }
    }
}
