package com.batmancpu.player.ui.library

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The metadata cache uses synthetic provider payloads and never opens a network connection. */
class OfflineMetadataCacheAndroidTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val tree = Uri.parse("content://com.android.externalstorage.documents/tree/metadata-cache-test")
    private val movie = CachedTmdbMetadata(
        tmdbId = 42L,
        mediaType = "MOVIE",
        title = "Synthetic Film",
        year = 2024,
        overview = "Synthetic provider description",
        posterPath = "/poster_42.jpg",
        backdropPath = "/backdrop_42.png",
        cachedAtEpochMs = 1_800_000_000_000L,
    )

    @After fun cleanup() = runBlocking { OfflineLibraryIndex.forgetTree(context, tree) }

    @Test fun providerMetadataIsInvisibleUntilExplicitMatchAndThenStaysOffline() = runBlocking {
        OfflineLibraryIndex.saveSuccessfulScan(context, tree, listOf(video("film")))
        val db = PlayerLibraryDatabase.get(context)
        db.cachedTmdbMetadataDao().upsert(movie)
        assertTrue(OfflineMetadataCache.load(context, tree.toString()).isEmpty())

        assertTrue(OfflineMetadataCache.bind(context, tree.toString(), listOf("film"), movie))
        assertEquals(mapOf("film" to movie), OfflineMetadataCache.load(context, tree.toString()))
    }

    @Test fun confirmationRejectsUnknownDocumentsAndUnsafeProviderImagePaths() = runBlocking {
        OfflineLibraryIndex.saveSuccessfulScan(context, tree, listOf(video("film")))
        assertFalse(OfflineMetadataCache.bind(context, tree.toString(), listOf("film", "other"), movie))
        val unsafe = movie.copy(posterPath = "https://unapproved.invalid/poster.jpg")
        assertFalse(OfflineMetadataCache.bind(context, tree.toString(), listOf("film"), unsafe))
        assertTrue(OfflineMetadataCache.load(context, tree.toString()).isEmpty())
    }

    @Test fun explicitRemovalPersistsNoRematchUntilAUserBindsAgain() = runBlocking {
        OfflineLibraryIndex.saveSuccessfulScan(context, tree, listOf(video("film")))
        assertTrue(OfflineMetadataCache.bind(context, tree.toString(), listOf("film"), movie))

        OfflineMetadataCache.suppressLookup(context, tree.toString(), listOf("film"))

        assertTrue(OfflineMetadataCache.load(context, tree.toString()).isEmpty())
        assertTrue(OfflineMetadataCache.isLookupSuppressed(context, tree.toString(), listOf("film")))
        assertFalse(
            "A late automatic response cannot overwrite an explicit removal",
            OfflineMetadataCache.bind(context, tree.toString(), listOf("film"), movie, respectLookupSuppression = true),
        )
        assertTrue(OfflineMetadataCache.isLookupSuppressed(context, tree.toString(), listOf("film")))
        assertTrue("A later explicit choice replaces the suppression", OfflineMetadataCache.bind(context, tree.toString(), listOf("film"), movie))
        assertFalse(OfflineMetadataCache.isLookupSuppressed(context, tree.toString(), listOf("film")))
        assertEquals(mapOf("film" to movie), OfflineMetadataCache.load(context, tree.toString()))
    }

    @Test fun successfulRescanRemovesStaleMatchAndUnreferencedMetadata() = runBlocking {
        OfflineLibraryIndex.saveSuccessfulScan(context, tree, listOf(video("keep"), video("remove")))
        assertTrue(OfflineMetadataCache.bind(context, tree.toString(), listOf("keep"), movie))
        val second = movie.copy(tmdbId = 43L, title = "Stale Match")
        assertTrue(OfflineMetadataCache.bind(context, tree.toString(), listOf("remove"), second))

        OfflineLibraryIndex.saveSuccessfulScan(context, tree, listOf(video("keep")))

        assertEquals(mapOf("keep" to movie), OfflineMetadataCache.load(context, tree.toString()))
        assertEquals(null, PlayerLibraryDatabase.get(context).cachedTmdbMetadataDao().forContent(43L, "MOVIE"))
    }

    private fun video(id: String) = DiscoveredLocalMedia(id, "$id.mp4", LocalMediaKind.VIDEO)
}
