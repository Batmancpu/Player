package com.batmancpu.player.ui.metadata

import com.batmancpu.player.ui.library.TmdbSearchTerms
import java.text.Normalizer
import java.util.Locale

/**
 * Conservative automatic artwork-match policy. It deliberately has no fuzzy
 * score: punctuation/diacritics/case differences are normalized, while type,
 * year (when known), and title must agree. Multiple distinct exact candidates
 * remain for the user to choose from.
 */
internal object TmdbAutoMatchPolicy {
    fun uniqueExactMatch(terms: TmdbSearchTerms, candidates: List<TmdbCandidate>): TmdbCandidate? {
        val expectedTitle = comparable(terms.normalizedTitle)
        if (expectedTitle.isEmpty()) return null

        val exact = candidates.asSequence()
            .filter { it.mediaType == terms.mediaType }
            .filter { comparable(it.title) == expectedTitle }
            .filter { terms.year == null || it.year == terms.year }
            .distinctBy { it.id }
            .toList()
        return exact.singleOrNull()
    }

    private fun comparable(value: String): String {
        val decomposed = Normalizer.normalize(value, Normalizer.Form.NFKD).lowercase(Locale.ROOT)
        return buildString(decomposed.length) {
            var index = 0
            while (index < decomposed.length) {
                val codePoint = decomposed.codePointAt(index)
                // Retain only letters/numbers; punctuation and combining marks
                // are ignored so typographic variants compare consistently.
                if (Character.isLetterOrDigit(codePoint)) appendCodePoint(codePoint)
                index += Character.charCount(codePoint)
            }
        }
    }
}
