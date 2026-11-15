package com.batmancpu.player.ui.library

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.platform.app.InstrumentationRegistry
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineLibraryAndroidTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val a = Uri.parse("content://com.android.externalstorage.documents/tree/synthetic-A")
    private val b = Uri.parse("content://com.android.externalstorage.documents/tree/synthetic-B")
    private fun media(id: String) = DiscoveredLocalMedia(id, "$id.mp4", LocalMediaKind.VIDEO)
    @After fun cleanup() = runBlocking {
        OfflineLibraryIndex.forgetTree(context, a); OfflineLibraryIndex.forgetTree(context, b)
    }

    @Test fun completeRescanReconcilesAndPersistsAcrossDatabaseReopen() = runBlocking {
        OfflineLibraryIndex.saveSuccessfulScan(context, a, listOf(media("old"), media("kept")))
        OfflineLibraryIndex.saveSuccessfulScan(context, a, listOf(media("kept"), media("new")))
        val reopened = Room.databaseBuilder(context, PlayerLibraryDatabase::class.java, "player-local-library.db").build()
        try {
            assertEquals(setOf("kept", "new"), reopened.mediaDao().forTree(a.toString()).map { it.documentId }.toSet())
        } finally { reopened.close() }
        OfflineLibraryIndex.saveSuccessfulScan(context, a, emptyList())
        assertTrue(OfflineLibraryIndex.load(context, a).isEmpty())
    }

    @Test fun activationRetiresOnlyOtherTreeAndNeverMixesLibraries() = runBlocking {
        OfflineLibraryIndex.saveSuccessfulScan(context, a, listOf(media("A")))
        OfflineLibraryIndex.saveSuccessfulScan(context, b, listOf(media("B")))
        val matches = PlayerLibraryDatabase.get(context).manualTmdbMatchDao()
        matches.upsert(ManualTmdbMatch(a.toString(), "A", 101L, "MOVIE", 1L))
        matches.upsert(ManualTmdbMatch(b.toString(), "B", 202L, "MOVIE", 2L))

        assertEquals(listOf(media("B")), OfflineLibraryIndex.activateAndLoad(context, b))
        assertTrue(OfflineLibraryIndex.load(context, a).isEmpty())
        assertNull(matches.forDocument(a.toString(), "A"))
        assertEquals(202L, matches.forDocument(b.toString(), "B")?.tmdbId)
    }

    @Test fun completedRescanKeepsOnlyManualMatchesForStillIndexedDocuments() = runBlocking {
        OfflineLibraryIndex.saveSuccessfulScan(context, a, listOf(media("keep"), media("remove")))
        val matches = PlayerLibraryDatabase.get(context).manualTmdbMatchDao()
        matches.upsert(ManualTmdbMatch(a.toString(), "keep", 123L, "MOVIE", 1L))
        matches.upsert(ManualTmdbMatch(a.toString(), "remove", 456L, "TV", 2L))

        OfflineLibraryIndex.saveSuccessfulScan(context, a, listOf(media("keep")))

        assertEquals(123L, matches.forDocument(a.toString(), "keep")?.tmdbId)
        assertNull(matches.forDocument(a.toString(), "remove"))
    }

    @Test fun incompleteProviderScanDoesNotReplaceIndexedRows() = runBlocking {
        val original = listOf(media("original"))
        OfflineLibraryIndex.saveSuccessfulScan(context, a, original)
        val scanner = MediaTreeScanner(
            listChildren = { TreeListing(emptyList(), isLoading = true) },
            isWithinSelectedTree = { _, _ -> true },
        )
        assertThrows(IOException::class.java) {
            runBlocking { OfflineLibraryIndex.saveSuccessfulScan(context, a, scanner.scan("root")) }
        }
        assertEquals(original, OfflineLibraryIndex.load(context, a))
    }

    @Test fun cancelledRoomTransactionRollsBackDeletion() = runBlocking {
        val original = listOf(media("original"))
        OfflineLibraryIndex.saveSuccessfulScan(context, a, original)
        assertThrows(CancellationException::class.java) {
            runBlocking {
                val db = PlayerLibraryDatabase.get(context)
                db.withTransaction {
                    db.mediaDao().removeTree(a.toString())
                    throw CancellationException("synthetic cancellation")
                }
            }
        }
        assertEquals(original, OfflineLibraryIndex.load(context, a))
    }

    @Test(timeout = 30_000) fun thousandItemReconciliationIsSafeToCallFromMain() = runBlocking {
        val expected = (1..1000).map { media("synthetic-$it") }
        val loaded = withContext(Dispatchers.Main) {
            OfflineLibraryIndex.saveSuccessfulScan(context, a, expected)
            OfflineLibraryIndex.activateAndLoad(context, a)
        }
        assertEquals(expected.map { it.documentId }.toSet(), loaded.map { it.documentId }.toSet())
        val remaining = expected.take(100)
        val rescanned = withContext(Dispatchers.Main) {
            OfflineLibraryIndex.saveSuccessfulScan(context, a, remaining)
            OfflineLibraryIndex.load(context, a)
        }
        assertEquals(remaining.map { it.documentId }.toSet(), rescanned.map { it.documentId }.toSet())
    }

    @Test fun mergedManifestRetainsOfflineReadOnlyPrivacyBoundary() {
        @Suppress("DEPRECATION")
        val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        assertTrue(info.requestedPermissions.orEmpty().all {
            it == context.packageName + ".DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
        })
        assertFalse(context.applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP != 0)
    }
}
