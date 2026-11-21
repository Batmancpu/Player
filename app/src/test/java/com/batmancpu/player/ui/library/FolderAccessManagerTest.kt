package com.batmancpu.player.ui.library

import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderAccessManagerTest {
    private class Store : FolderAccessStore {
        var saved: String? = null
        val permissions = mutableMapOf<String, FolderGrant>()
        val events = mutableListOf<String>()
        var unavailable: String? = null
        var refuseRelease = false
        var acquisition: (suspend () -> Unit)? = null
        override fun isSupported(tree: String) = tree.startsWith("local:")
        override suspend fun selected() = saved
        override suspend fun saveSelected(tree: String) { events.add("save:$tree"); saved = tree }
        override suspend fun clearSelected() { events.add("clear"); saved = null }
        override suspend fun grants() = permissions.values.toList()
        override suspend fun takeRead(tree: String) {
            acquisition?.invoke()
            permissions[tree] = FolderGrant(tree, true, permissions[tree]?.write == true)
        }
        override suspend fun release(tree: String, read: Boolean, write: Boolean) {
            events.add("release:$tree")
            if (refuseRelease) throw SecurityException("synthetic refusal")
            val old = permissions.getValue(tree)
            val next = FolderGrant(tree, old.read && !read, old.write && !write)
            if (!next.read && !next.write) permissions.remove(tree) else permissions[tree] = next
        }
        override suspend fun probe(tree: String): String {
            if (tree == unavailable) throw IOException("synthetic unavailable root")
            return "Synthetic folder"
        }
    }

    @Test fun restoresOnlySelectedReadGrantAndRemovesWritesAndOrphans() = runBlocking {
        val store = Store().apply {
            saved = "local:A"
            permissions["local:A"] = FolderGrant("local:A", true, true)
            permissions["local:abandoned"] = FolderGrant("local:abandoned", true, false)
        }
        assertEquals("local:A", FolderAccessManager(store).restore()?.tree)
        assertEquals(listOf(FolderGrant("local:A", true, false)), store.grants())
    }

    @Test fun noSavedSelectionReleasesAbandonedAcquisitionAfterProcessInterruption() = runBlocking {
        val store = Store().apply { permissions["local:A"] = FolderGrant("local:A", true, false) }
        assertEquals(null, FolderAccessManager(store).restore())
        assertTrue(store.grants().isEmpty())
    }

    @Test fun grantRecordDoesNotMakeUnavailableRootUsable() {
        val store = Store().apply {
            saved = "local:A"; unavailable = "local:A"
            permissions["local:A"] = FolderGrant("local:A", true, false)
        }
        assertThrows(IOException::class.java) { runBlocking { FolderAccessManager(store).restore() } }
        assertEquals("local:A", store.saved) // allow explicit reconnection, preserve offline index
    }

    @Test fun missingReadGrantRequiresReconnection() {
        val store = Store().apply { saved = "local:A" }
        assertThrows(SecurityException::class.java) { runBlocking { FolderAccessManager(store).restore() } }
    }

    @Test fun failedNewRootKeepsPreviousSelectionAndReleasesNewGrant() {
        val store = Store().apply {
            saved = "local:A"; unavailable = "local:B"
            permissions["local:A"] = FolderGrant("local:A", true, false)
        }
        assertThrows(IOException::class.java) { runBlocking { FolderAccessManager(store).connect("local:B") } }
        assertEquals("local:A", store.saved)
        assertEquals(listOf(FolderGrant("local:A", true, false)), runBlocking { store.grants() })
    }

    @Test fun persistsNewSelectionBeforeReleasingOldGrant() = runBlocking {
        val store = Store().apply {
            saved = "local:A"
            permissions["local:A"] = FolderGrant("local:A", true, false)
        }
        assertEquals("local:B", FolderAccessManager(store).connect("local:B").tree)
        assertEquals(listOf("save:local:B", "release:local:A"), store.events)
        assertEquals(listOf(FolderGrant("local:B", true, false)), store.grants())
    }

    @Test fun refusesToActivateWhenGrantReleaseFails() {
        val store = Store().apply {
            saved = "local:A"; refuseRelease = true
            permissions["local:A"] = FolderGrant("local:A", true, false)
        }
        assertThrows(SecurityException::class.java) { runBlocking { FolderAccessManager(store).connect("local:B") } }
        assertEquals("local:B", store.saved) // next restoration retries cleanup before activation
    }

    @Test fun forgetClearsSelectionAndReleasesAllPersistedGrants() = runBlocking {
        val store = Store().apply {
            saved = "local:A"
            permissions["local:A"] = FolderGrant("local:A", true, true)
            permissions["local:orphan"] = FolderGrant("local:orphan", true, false)
        }
        FolderAccessManager(store).forget()
        assertEquals(null, store.saved)
        assertTrue(store.grants().isEmpty())
        assertEquals(listOf("clear", "release:local:A", "release:local:orphan"), store.events)
    }

    @Test fun forgetReportsGrantReleaseFailureAfterClearingSavedSelection() {
        val store = Store().apply {
            saved = "local:A"; refuseRelease = true
            permissions["local:A"] = FolderGrant("local:A", true, false)
        }
        assertThrows(SecurityException::class.java) { runBlocking { FolderAccessManager(store).forget() } }
        assertEquals(null, store.saved)
        assertEquals(listOf(FolderGrant("local:A", true, false)), runBlocking { store.grants() })
    }

    @Test fun rejectsUnknownProviderBeforeAcquiringPermission() {
        val store = Store()
        assertThrows(UnsupportedFolderException::class.java) { runBlocking { FolderAccessManager(store).connect("cloud:A") } }
        assertTrue(store.permissions.isEmpty())
    }

    @Test(timeout = 10_000) fun serializesRestoreWithAnInFlightConnection() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val store = Store().apply { acquisition = { entered.complete(Unit); release.await() } }
        val manager = FolderAccessManager(store)
        val connect = async { manager.connect("local:B") }
        entered.await()
        val restore = async { manager.restore() }
        release.complete(Unit)
        assertEquals(connect.await(), restore.await())
    }
}
