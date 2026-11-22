package com.batmancpu.player.ui.metadata

import android.net.Uri
import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry
import com.batmancpu.player.ui.library.DiscoveredLocalMedia
import com.batmancpu.player.ui.library.LocalCatalogue
import com.batmancpu.player.ui.library.LocalMediaKind
import com.batmancpu.player.ui.library.OfflineLibraryIndex
import com.batmancpu.player.ui.library.OfflineMetadataCache
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Uses a fake transport and synthetic Room rows; never opens a socket or user media. */
class MetadataLookupControllerAndroidTest {
    @Test(timeout = 30_000)
    fun automaticLookupRequiresConsentAndOnlyExactUniqueMatchesAreCached() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val suffix = UUID.randomUUID().toString()
        val tree = "content://com.example.synthetic/tree/$suffix"
        val treeUri = Uri.parse(tree)
        val file = DiscoveredLocalMedia("synthetic-$suffix", "Aurora.2022.mp4", LocalMediaKind.VIDEO)
        val title = LocalCatalogue.build(listOf(file)).single()
        val settings = MetadataSettingsStore(context, "lookup_$suffix")
        val calls = AtomicInteger()
        val client = TmdbClient(settings::credentialIfEnabled, TmdbTransport { uri, token ->
            calls.incrementAndGet()
            assertEquals("synthetic.read.token", token)
            assertEquals("api.themoviedb.org", uri.host)
            assertEquals("/3/search/movie", uri.path)
            assertTrue(uri.rawQuery.contains("query=Aurora"))
            assertTrue(uri.rawQuery.contains("year=2022"))
            assertTrue(uri.rawQuery.contains("include_adult=false"))
            assertFalse(uri.toString().contains(".mp4"))
            assertFalse(uri.toString().contains(suffix))
            TmdbHttpResponse(
                200,
                """{"results":[{"id":42,"title":"Aurora","release_date":"2022-01-01","overview":"Synthetic provider description","poster_path":"/poster_A.jpg","backdrop_path":"/backdrop_A.jpg"}]}""".toByteArray(),
            )
        })
        val artworkCalls = AtomicInteger()
        val artwork = TmdbArtworkCache(context, settings, TmdbArtworkTransport { uri ->
            artworkCalls.incrementAndGet()
            assertEquals("image.tmdb.org", uri.host)
            TmdbArtworkResponse(200, Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR4nGNQaLjwHwAEpAJw/IFNkwAAAABJRU5ErkJggg==", Base64.DEFAULT), "image/png")
        }, testNamespace = suffix)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val controller = MetadataLookupController(
            context, scope, settings, client, artworkCache = artwork, currentTree = { tree },
            isCurrentTitle = { candidateTree, candidate -> candidateTree == tree && candidate.key == title.key },
        )
        try {
            OfflineLibraryIndex.saveSuccessfulScan(context, treeUri, listOf(file))
            settings.save("synthetic.read.token", enabled = false)
            controller.prepare(title)
            val prepared = withTimeout(5_000) { controller.state.first { !it.preparing } }
            assertTrue(prepared.settingsEnabled && prepared.credentialConfigured && prepared.searchable)
            assertEquals("Opening details must not search", 0, calls.get())
            assertEquals("Opening details must not fetch artwork", 0, artworkCalls.get())

            settings.save(token = null, enabled = true)
            controller.clear()
            controller.prepare(title)
            val automatic = withTimeout(5_000) {
                controller.state.first { it.cached?.title == "Aurora" && !it.searching && it.posterFile != null && it.backdropFile != null }
            }
            assertEquals("Explicit consent allows exact, unique metadata to bind without a second confirmation", 1, calls.get())
            assertEquals("Automatic lookup fetches one poster and one backdrop", 2, artworkCalls.get())
            assertEquals(1, automatic.matchedFiles)
            assertTrue(automatic.posterFile?.isFile == true)
            assertTrue(automatic.backdropFile?.isFile == true)
            assertEquals("Aurora", OfflineMetadataCache.load(context, tree)[file.documentId]?.title)
            assertNull("Untrusted image paths must not reach the transport", artwork.fetch("https://example.invalid/poster.jpg", TmdbImageSize.POSTER))
            assertEquals("Invalid routes are rejected locally", 2, artworkCalls.get())

            controller.remove(title)
            withTimeout(5_000) { controller.state.first { !it.preparing && it.cached == null } }
            assertTrue(OfflineMetadataCache.isLookupSuppressed(context, tree, listOf(file.documentId)))
            controller.prepare(title)
            withTimeout(5_000) { controller.state.first { !it.preparing && it.cached == null } }
            assertEquals("Explicit removal suppresses automatic lookup across detail reopens", 1, calls.get())

            settings.disable()
            assertEquals("Downloaded artwork remains readable offline after disabling TMDB", automatic.posterFile, artwork.cached("/poster_A.jpg", TmdbImageSize.POSTER))
            assertNull("Disabled settings prevent another artwork request", artwork.fetch("/poster_A.jpg", TmdbImageSize.POSTER))
            assertEquals("Offline cache reads do not call the network", 2, artworkCalls.get())
            assertEquals(automatic.backdropFile, artwork.cached("/backdrop_A.jpg", TmdbImageSize.BACKDROP))
        } finally {
            controller.clear()
            scope.cancel()
            OfflineLibraryIndex.forgetTree(context, treeUri)
            settings.clear()
            artwork.directory.deleteRecursively()
        }
    }
}
