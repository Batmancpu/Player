package com.mangoloads.player.ui.library

import java.io.IOException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal data class FolderGrant(val tree: String, val read: Boolean, val write: Boolean)
internal data class LocalFolder(val tree: String, val label: String)
internal class UnsupportedFolderException : IOException("Unsupported local folder provider")

internal interface FolderAccessStore {
    fun isSupported(tree: String): Boolean
    suspend fun selected(): String?
    suspend fun saveSelected(tree: String)
    suspend fun clearSelected()
    suspend fun grants(): List<FolderGrant>
    suspend fun takeRead(tree: String)
    suspend fun release(tree: String, read: Boolean, write: Boolean)
    suspend fun probe(tree: String): String
}

/** All grant mutations are serialized, durable selection precedes retiring old grants. */
internal class FolderAccessManager(private val store: FolderAccessStore) {
    private val mutex = Mutex()

    suspend fun restore(): LocalFolder? = mutex.withLock {
        val saved = store.selected()?.takeIf(store::isSupported)
        reconcile(saved)
        if (saved == null) return@withLock null
        requireRead(saved)
        LocalFolder(saved, store.probe(saved))
    }

    suspend fun connect(tree: String): LocalFolder = mutex.withLock {
        if (!store.isSupported(tree)) throw UnsupportedFolderException()
        try {
            store.takeRead(tree)
            requireRead(tree)
            val label = store.probe(tree)
            store.saveSelected(tree)
            reconcile(tree)
            LocalFolder(tree, label)
        } finally {
            // Recover abandoned acquisitions on cancellation/failure. On process death,
            // restore performs the same reconciliation using durable selection.
            withContext(NonCancellable) {
                reconcile(store.selected()?.takeIf(store::isSupported))
            }
        }
    }

    /** Forget only Player's index and SAF access; never touches source media. */
    suspend fun forget() = withContext(NonCancellable) {
        mutex.withLock {
            store.clearSelected()
            reconcile(null)
        }
    }

    private suspend fun requireRead(tree: String) {
        if (store.grants().none { it.tree == tree && it.read }) {
            throw SecurityException("Local folder read access is unavailable")
        }
    }

    private suspend fun reconcile(selected: String?) {
        for (grant in store.grants()) {
            val releaseRead = grant.tree != selected && grant.read
            if (releaseRead || grant.write) {
                store.release(grant.tree, read = releaseRead, write = grant.write)
            }
        }
        if (store.grants().any { it.tree != selected || it.write }) {
            throw SecurityException("Local folder grants could not be reconciled")
        }
    }
}
