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
import org.junit.Assert.assertTrue
import org.junit.Test

/** Uses a fake transport and synthetic Room rows; never opens a socket or user media. */
class MetadataLookupControllerAndroidTest {
    @Test(timeout = 30_000)
    fun openingDetailsIsOfflineAndOnlyManualSearchCanBeConfirmed() = runBlocking {
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
            settings.save("synthetic.read.token", enabled = true)
            controller.prepare(title)
            val prepared = withTimeout(5_000) { controller.state.first { !it.preparing } }
            assertTrue(prepared.settingsEnabled && prepared.credentialConfigured && prepared.searchable)
            assertEquals("Opening details must not search", 0, calls.get())
            assertEquals("Opening details must not fetch artwork", 0, artworkCalls.get())

            controller.search(title)
            val found = withTimeout(5_000) { controller.state.first { !it.searching } }
            assertEquals(1, calls.get())
            assertEquals("Aurora", found.candidates.single().title)

            controller.confirm(title, found.candidates.single())
            val confirmed = withTimeout(5_000) { controller.state.first { !it.confirming && it.posterFile != null } }
            assertEquals("Aurora", confirmed.cached?.title)
            assertEquals(1, confirmed.matchedFiles)
            assertEquals(1, artworkCalls.get())
            assertTrue(confirmed.posterFile?.isFile == true)
            assertEquals("Aurora", OfflineMetadataCache.load(context, tree)[file.documentId]?.title)
        } finally {
            controller.clear()
            scope.cancel()
            OfflineLibraryIndex.forgetTree(context, treeUri)
            settings.clear()
            artwork.directory.deleteRecursively()
        }
    }
}
