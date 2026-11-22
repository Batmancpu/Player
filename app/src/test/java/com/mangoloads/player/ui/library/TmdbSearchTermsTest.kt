package com.mangoloads.player.ui.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TmdbSearchTermsTest {
    @Test
    fun consentIsOffByDefault() {
        val parsed = MediaNameParser.parse("The.Matrix.1999.mkv")
        assertNull(TmdbSearchTerms.from(parsed))
    }

    @Test
    fun movieUsesOnlyNormalizedTitleYearAndType() {
        val terms = TmdbSearchTerms.from(
            MediaNameParser.parse("The.Matrix.1999.1080p.BluRay.x265.mkv"),
            MetadataSearchConsent.ENABLED_BY_USER,
        )

        assertNotNull(terms)
        assertEquals("The Matrix", terms?.normalizedTitle)
        assertEquals(1999, terms?.year)
        assertEquals(TmdbMediaType.MOVIE, terms?.mediaType)
    }

    @Test
    fun episodeUsesSeriesTitleOnlyOnce() {
        val terms = TmdbSearchTerms.from(
            MediaNameParser.parse("Breaking.Bad.S01E02.1080p.WEB-DL.mkv"),
            MetadataSearchConsent.ENABLED_BY_USER,
        )

        assertNotNull(terms)
        assertEquals("Breaking Bad", terms?.normalizedTitle)
        assertNull(terms?.year)
        assertEquals(TmdbMediaType.TV, terms?.mediaType)
    }

    @Test
    fun unrecognizedAndAmbiguousNamesStayLocal() {
        val parsed = MediaNameParser.parse("Show.S01E01-E02.mkv")
        assertNull(TmdbSearchTerms.from(parsed, MetadataSearchConsent.ENABLED_BY_USER))
    }

    @Test
    fun rejectsPathsUrisFileSuffixesControlsAndOversizedTerms() {
        val backslashPath = "C:" + '\\' + "Private Film"
        val unsafeTitles = listOf(
            "/storage/emulated/0/Movies/Private Film",
            backslashPath,
            "content://provider/private-film",
            "https://example.invalid/private-film",
            "C:Private Film",
            "Private Film.mkv",
            "Private subtitle.srt",
            "Private\nFilm",
            "A".repeat(121),
        )

        unsafeTitles.forEach { title ->
            assertNull(
                "Unsafe query must be rejected: $title",
                TmdbSearchTerms.from(
                    ParsedMediaName.Movie(title, 2020),
                    MetadataSearchConsent.ENABLED_BY_USER,
                ),
            )
        }
    }

    @Test
    fun normalizesWhitespaceAndUnicodeWithoutDiscardingTitleText() {
        val terms = TmdbSearchTerms.from(
            ParsedMediaName.Movie("  Cafe\u0301   物語  ", null),
            MetadataSearchConsent.ENABLED_BY_USER,
        )

        assertEquals("Café 物語", terms?.normalizedTitle)
    }

    @Test
    fun invalidYearIsOmittedInsteadOfSent() {
        val terms = TmdbSearchTerms.from(
            ParsedMediaName.Movie("A Film", 999),
            MetadataSearchConsent.ENABLED_BY_USER,
        )

        assertNotNull(terms)
        assertNull(terms?.year)
    }
}
