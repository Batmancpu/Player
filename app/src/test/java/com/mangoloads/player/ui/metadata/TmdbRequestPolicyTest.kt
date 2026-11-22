package com.mangoloads.player.ui.metadata

import com.mangoloads.player.ui.library.MetadataSearchConsent
import com.mangoloads.player.ui.library.ParsedMediaName
import com.mangoloads.player.ui.library.TmdbMediaType
import com.mangoloads.player.ui.library.TmdbSearchTerms
import java.net.URI
import java.net.URLDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class TmdbRequestPolicyTest {
    private val enabled = MetadataSearchConsent.ENABLED_BY_USER

    private fun movie(title: String = "Example Film", year: Int? = 2020): TmdbSearchTerms =
        requireNotNull(TmdbSearchTerms.from(ParsedMediaName.Movie(title, year), enabled))

    private fun parameters(uri: URI): Map<String, String> = uri.rawQuery.split('&').associate { part ->
        val pair = part.split('=', limit = 2)
        URLDecoder.decode(pair[0], "UTF-8") to URLDecoder.decode(pair[1], "UTF-8")
    }

    @Test
    fun disabledConsentBlocksPreviouslyCreatedSearchTermsAndArtwork() {
        val terms = movie()
        assertNull(TmdbRequestPolicy.search(terms))
        assertNull(TmdbRequestPolicy.search(terms, MetadataSearchConsent.DISABLED))
        assertNull(TmdbRequestPolicy.artwork("/poster.jpg", TmdbImageSize.POSTER))
        assertNull(TmdbRequestPolicy.artwork("/poster.jpg", TmdbImageSize.POSTER, MetadataSearchConsent.DISABLED))
    }

    @Test
    fun detailsUsePositiveIdsAtFixedMovieAndTvEndpointsWithoutQueries() {
        assertEquals(
            "https://api.themoviedb.org/3/movie/123",
            TmdbRequestPolicy.details(123, TmdbMediaType.MOVIE, enabled).toString(),
        )
        val tv = requireNotNull(TmdbRequestPolicy.details(Long.MAX_VALUE, TmdbMediaType.TV, enabled))
        assertEquals("https://api.themoviedb.org/3/tv/${Long.MAX_VALUE}", tv.toString())
        assertNull(tv.rawQuery)
        assertNull(tv.fragment)
        assertNull(tv.userInfo)
    }

    @Test
    fun detailsRejectNonpositiveIdsAndDisabledConsent() {
        assertNull(TmdbRequestPolicy.details(123, TmdbMediaType.MOVIE))
        assertNull(TmdbRequestPolicy.details(123, TmdbMediaType.TV, MetadataSearchConsent.DISABLED))
        for (id in listOf(0L, -1L, Long.MIN_VALUE)) {
            assertNull(TmdbRequestPolicy.details(id, TmdbMediaType.MOVIE, enabled))
        }
    }

    @Test
    fun movieSearchUsesOnlyFixedHttpsOriginAndPermittedParameters() {
        val uri = requireNotNull(TmdbRequestPolicy.search(movie(), enabled))

        assertEquals("https", uri.scheme)
        assertEquals("api.themoviedb.org", uri.host)
        assertEquals(-1, uri.port)
        assertNull(uri.userInfo)
        assertNull(uri.fragment)
        assertEquals("/3/search/movie", uri.path)
        assertEquals(mapOf("query" to "Example Film", "include_adult" to "false", "year" to "2020"), parameters(uri))
    }

    @Test
    fun episodeSearchUsesTvEndpointAndSeriesTitleWithoutEpisodeNumbers() {
        val parsed = ParsedMediaName.Episode("Example Show", 3, 7)
        val terms = requireNotNull(TmdbSearchTerms.from(parsed, enabled))
        val uri = requireNotNull(TmdbRequestPolicy.search(terms, enabled))

        assertEquals("https://api.themoviedb.org/3/search/tv", uri.toString().substringBefore('?'))
        assertEquals(mapOf("query" to "Example Show", "include_adult" to "false"), parameters(uri))
    }

    @Test
    fun missingAndInvalidYearsAreNotAddedToSearch() {
        for (year in listOf(null, 999, 10000)) {
            val uri = requireNotNull(TmdbRequestPolicy.search(movie(year = year), enabled))
            assertEquals(mapOf("query" to "Example Film", "include_adult" to "false"), parameters(uri))
        }
    }

    @Test
    fun unicodeAndQuerySyntaxRemainOneTitleValue() {
        val title = "Café 物語 & include_adult=true # + %"
        val uri = requireNotNull(TmdbRequestPolicy.search(movie(title, null), enabled))

        assertEquals(mapOf("query" to title, "include_adult" to "false"), parameters(uri))
        assertNull(uri.fragment)
        assertFalse(uri.rawQuery.contains(' '))
        assertEquals("api.themoviedb.org", uri.host)
    }

    @Test
    fun alreadyEncodedTextIsEncodedAgainInsteadOfBecomingUrlSyntax() {
        val title = "Example %26year%3D1999 %23fragment"
        val uri = requireNotNull(TmdbRequestPolicy.search(movie(title, null), enabled))

        assertEquals(mapOf("query" to title, "include_adult" to "false"), parameters(uri))
        assertNull(uri.fragment)
    }

    @Test
    fun artworkUsesFixedCdnAndBoundedPosterAndBackdropSizes() {
        assertEquals(
            "https://image.tmdb.org/t/p/w342/abc_123-XYZ.jpg",
            TmdbRequestPolicy.artwork("/abc_123-XYZ.jpg", TmdbImageSize.POSTER, enabled).toString(),
        )
        val backdrop = requireNotNull(TmdbRequestPolicy.artwork("/abc123.png", TmdbImageSize.BACKDROP, enabled))
        assertEquals("https://image.tmdb.org/t/p/w780/abc123.png", backdrop.toString())
        assertNull(backdrop.rawQuery)
        assertNull(backdrop.fragment)
        assertNull(backdrop.userInfo)
        assertEquals(-1, backdrop.port)
    }

    @Test
    fun artworkRejectsUrlsTraversalEncodedSeparatorsAndUnsupportedFiles() {
        val unsafe = listOf(
            "", "poster.jpg", "//image.tmdb.org/poster.jpg", "https://image.tmdb.org/poster.jpg",
            "https://evil.invalid/poster.jpg", "/../poster.jpg", "/a/../poster.jpg", "/nested/poster.jpg",
            "/%2e%2e%2fposter.jpg", "/%252e%252e%252fposter.jpg", "/poster%2fprivate.jpg",
            "/poster.jpg?token=private", "/poster.jpg#fragment", "/poster\\private.jpg",
            "/poster\n.jpg", "/poster\u0000.jpg", "/poster.svg", "/poster.webp", "/poster.JPG",
            "/poster name.jpg", "/物語.jpg", "/a.jpg/", "/a..jpg", "/" + "a".repeat(196) + ".jpg",
        )
        for (path in unsafe) {
            assertNull("Rejected artwork path: $path", TmdbRequestPolicy.artwork(path, TmdbImageSize.POSTER, enabled))
        }
    }
}
