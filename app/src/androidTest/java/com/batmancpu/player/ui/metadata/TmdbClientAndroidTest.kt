package com.batmancpu.player.ui.metadata

import com.batmancpu.player.ui.library.MetadataSearchConsent
import com.batmancpu.player.ui.library.ParsedMediaName
import com.batmancpu.player.ui.library.TmdbMediaType
import com.batmancpu.player.ui.library.TmdbSearchTerms
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI
import java.security.cert.Certificate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** All provider data/tokens are owned synthetic fixtures; these tests open no socket. */
class TmdbClientAndroidTest {
    private val token = "synthetic.read.token"
    private val movie = """{"id":42,"title":"Aurora","release_date":"2022-01-01","overview":"Synthetic description","poster_path":"/poster_A.jpg","backdrop_path":"/backdrop_A.jpg"}"""
    private fun terms() = TmdbSearchTerms.from(
        ParsedMediaName.Movie("Aurora", 2022), MetadataSearchConsent.ENABLED_BY_USER,
    )!!

    @Test fun disabledConsentMakesNoTransportCall() = runBlocking {
        val calls = AtomicInteger()
        val client = TmdbClient({ null }, TmdbTransport { _, _ -> calls.incrementAndGet(); error("Unexpected dispatch") })
        assertEquals(TmdbResult.Disabled, client.search(terms()))
        assertEquals(TmdbResult.Disabled, client.details(42, TmdbMediaType.MOVIE))
        assertEquals(0, calls.get())
    }

    @Test fun searchAndDetailsUseTypedOriginsAndBoundProviderData() = runBlocking {
        val destinations = mutableListOf<URI>()
        val client = TmdbClient({ token }, TmdbTransport { uri, supplied ->
            assertEquals(token, supplied)
            destinations += uri
            TmdbHttpResponse(200, (if (uri.path.contains("search")) """{"results":[$movie]}""" else movie).toByteArray())
        })
        val found = (client.search(terms()) as TmdbResult.Success).value.single()
        assertEquals(42L, found.id)
        assertEquals("Aurora", found.title)
        assertEquals(2022, found.year)
        assertEquals("/poster_A.jpg", found.posterPath)
        assertEquals(found, (client.details(42, TmdbMediaType.MOVIE) as TmdbResult.Success).value)
        assertEquals(listOf("/3/search/movie", "/3/movie/42"), destinations.map { it.path })
        assertTrue(destinations.all { it.scheme == "https" && it.host == "api.themoviedb.org" })
        assertTrue(destinations.none { it.toASCIIString().contains(token) })
    }

    @Test fun resultsAreBoundedAndUntrustedImageUrlsAreDropped() = runBlocking {
        val longOverview = "a".repeat(4_100)
        val malicious = movie.replace("Synthetic description", longOverview)
            .replace("/poster_A.jpg", "https://unapproved.invalid/image.jpg")
        val client = TmdbClient({ token }, TmdbTransport { _, _ ->
            TmdbHttpResponse(200, ("{\"results\":[" + List(25) { malicious }.joinToString(",") + "]}").toByteArray())
        })
        val found = (client.search(terms()) as TmdbResult.Success).value
        assertEquals(20, found.size)
        assertEquals(4_000, found.first().overview.length)
        assertNull(found.first().posterPath)
    }

    @Test fun revokedOrReplacedCredentialDropsAnInflightResponse() = runBlocking {
        var configured: String? = token
        val client = TmdbClient({ configured }, TmdbTransport { _, _ ->
            configured = null
            TmdbHttpResponse(200, """{"results":[$movie]}""".toByteArray())
        })
        assertEquals(TmdbResult.Disabled, client.search(terms()))
        configured = token
        val replaced = TmdbClient({ configured }, TmdbTransport { _, _ ->
            configured = "replacement.synthetic.token"
            TmdbHttpResponse(200, movie.toByteArray())
        })
        assertEquals(TmdbResult.Disabled, replaced.details(42, TmdbMediaType.MOVIE))
    }

    @Test fun serverFailuresRedirectsMalformedAndOversizeDataStayTyped() = runBlocking {
        suspend fun response(status: Int, body: ByteArray = ByteArray(0)) =
            TmdbClient({ token }, TmdbTransport { _, _ -> TmdbHttpResponse(status, body) }).search(terms())
        assertEquals(TmdbResult.Unauthorized, response(401))
        assertEquals(TmdbResult.Unauthorized, response(403))
        assertEquals(TmdbResult.RateLimited, response(429))
        assertEquals(TmdbResult.InvalidResponse, response(302))
        assertEquals(TmdbResult.Unavailable, response(500))
        assertEquals(TmdbResult.InvalidResponse, response(200, "not json".toByteArray()))
        assertEquals(TmdbResult.InvalidResponse, response(200, ByteArray(TmdbClient.MAX_JSON_BYTES + 1)))
    }

    @Test fun transportDisablesRedirectsUsesGenericHeadersAndClosesResponse() = runBlocking {
        val fixture = FixtureConnection("""{"results":[]}""".toByteArray())
        val transport = HttpsTmdbTransport { fixture }
        val response = transport.get(URI("https://api.themoviedb.org/3/search/movie?query=A"), token)
        assertEquals(200, response.statusCode)
        assertFalse(fixture.instanceFollowRedirects)
        assertFalse(fixture.useCaches)
        assertEquals(10_000, fixture.connectTimeout)
        assertEquals(15_000, fixture.readTimeout)
        assertEquals("Bearer $token", fixture.getRequestProperty("Authorization"))
        assertEquals("Player", fixture.getRequestProperty("User-Agent"))
        assertTrue(fixture.disconnected.await(5, TimeUnit.SECONDS))
        assertTrue(fixture.streamClosed)
    }

    @Test fun transportRefusesUnapprovedDestinationsBeforeConnection() = runBlocking {
        val calls = AtomicInteger()
        val transport = HttpsTmdbTransport { calls.incrementAndGet(); error("Unexpected connection") }
        for (uri in listOf(
            "http://api.themoviedb.org/3/movie/42", "https://api.themoviedb.org.evil.invalid/3/movie/42",
            "https://image.tmdb.org/t/p/w342/a.jpg", "https://api.themoviedb.org:443/3/movie/42",
            "https://user@api.themoviedb.org/3/movie/42", "https://api.themoviedb.org/3/movie/42#fragment",
            "https://api.themoviedb.org/3/movie/../42",
        )) {
            try { transport.get(URI(uri), token); fail("Unapproved destination accepted") }
            catch (_: IllegalArgumentException) { }
        }
        assertEquals(0, calls.get())
    }

    @Test(timeout = 10_000) fun cancellationDisconnectsBlockedReadAndPublishesNothing() = runBlocking {
        val enteredRead = CountDownLatch(1)
        val releaseRead = CountDownLatch(1)
        val fixture = FixtureConnection(ByteArray(0), object : InputStream() {
            override fun read(): Int {
                enteredRead.countDown()
                releaseRead.await(5, TimeUnit.SECONDS)
                return -1
            }
            override fun close() { releaseRead.countDown() }
        })
        var published = false
        val client = TmdbClient({ token }, HttpsTmdbTransport { fixture })
        val job = launch { client.search(terms()); published = true }
        // Keep the coroutine dispatcher free while waiting for the IO worker to enter read().
        repeat(100) { if (enteredRead.count == 0L) return@repeat; delay(10) }
        assertEquals(0L, enteredRead.count)
        job.cancelAndJoin()
        assertTrue(fixture.disconnected.await(2, TimeUnit.SECONDS))
        releaseRead.countDown()
        assertFalse(published)
    }

    private class FixtureConnection(
        private val bytes: ByteArray,
        private val overrideInput: InputStream? = null,
    ) : HttpsURLConnection(URI("https://api.themoviedb.org/3/search/movie").toURL()) {
        val disconnected = CountDownLatch(1)
        @Volatile var streamClosed = false
        override fun connect() = Unit
        override fun usingProxy() = false
        override fun disconnect() { disconnected.countDown(); overrideInput?.close() }
        override fun getResponseCode() = 200
        override fun getContentType() = "application/json; charset=utf-8"
        override fun getContentLengthLong() = -1L
        override fun getInputStream(): InputStream = overrideInput ?: object : ByteArrayInputStream(bytes) {
            override fun close() { streamClosed = true; super.close() }
        }
        override fun getCipherSuite() = "synthetic"
        override fun getLocalCertificates(): Array<Certificate>? = null
        override fun getServerCertificates(): Array<Certificate> = emptyArray()
    }
}
