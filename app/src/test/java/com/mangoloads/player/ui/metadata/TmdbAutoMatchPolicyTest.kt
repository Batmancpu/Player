package com.mangoloads.player.ui.metadata

import com.mangoloads.player.ui.library.TmdbMediaType
import com.mangoloads.player.ui.library.TmdbSearchTerms
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TmdbAutoMatchPolicyTest {
    private val movieTerms = TmdbSearchTerms.from(
        com.mangoloads.player.ui.library.ParsedMediaName.Movie("Amélie", 2001),
        com.mangoloads.player.ui.library.MetadataSearchConsent.ENABLED_BY_USER,
    )!!

    @Test fun acceptsUniqueExactTitleWithMatchingYearDespitePunctuationAndDiacritics() {
        val match = candidate(7, "Amelie", 2001)
        assertEquals(match, TmdbAutoMatchPolicy.uniqueExactMatch(movieTerms, listOf(match)))
    }

    @Test fun rejectsWrongYear() {
        assertNull(TmdbAutoMatchPolicy.uniqueExactMatch(movieTerms, listOf(candidate(7, "Amélie", 2011))))
    }

    @Test fun rejectsFuzzyTitle() {
        assertNull(TmdbAutoMatchPolicy.uniqueExactMatch(movieTerms, listOf(candidate(7, "Amélie from Montmartre", 2001))))
    }

    @Test fun rejectsWrongMediaType() {
        assertNull(TmdbAutoMatchPolicy.uniqueExactMatch(movieTerms, listOf(candidate(7, "Amélie", 2001, TmdbMediaType.TV))))
    }

    @Test fun rejectsAmbiguousExactCandidates() {
        assertNull(TmdbAutoMatchPolicy.uniqueExactMatch(movieTerms, listOf(
            candidate(7, "Amélie", 2001), candidate(8, "Amelie", 2001),
        )))
    }

    @Test fun duplicateRowsForSameProviderIdAreNotAmbiguous() {
        val match = candidate(7, "Amélie", 2001)
        assertEquals(match, TmdbAutoMatchPolicy.uniqueExactMatch(movieTerms, listOf(match, match.copy(overview = "duplicate row"))))
    }

    private fun candidate(id: Long, title: String, year: Int?, type: TmdbMediaType = TmdbMediaType.MOVIE) =
        TmdbCandidate(id, type, title, year, "", null, null)
}
