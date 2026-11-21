package com.batmancpu.player.ui.metadata

import com.batmancpu.player.ui.library.MetadataSearchConsent
import com.batmancpu.player.ui.library.TmdbMediaType
import com.batmancpu.player.ui.library.TmdbSearchTerms
import java.net.URI
import java.net.URLEncoder

/** Deliberately bounded sizes; original-resolution downloads are not requested. */
internal enum class TmdbImageSize(internal val segment: String) {
    POSTER("w342"),
    BACKDROP("w780"),
}

/**
 * Builds destinations only; this class performs no HTTP or credential handling.
 * A future transport must recheck consent before dispatch, disable redirects and
 * attach API authorization only to the API origin, never to artwork downloads.
 */
internal object TmdbRequestPolicy {
    private const val API_ORIGIN = "https://api.themoviedb.org"
    private const val IMAGE_ORIGIN = "https://image.tmdb.org"
    private const val MAX_IMAGE_PATH_LENGTH = 200
    private val imagePath = Regex("/[A-Za-z0-9_-]+\\.(?:jpg|png)")

    fun search(
        terms: TmdbSearchTerms,
        consent: MetadataSearchConsent = MetadataSearchConsent.DISABLED,
    ): URI? {
        if (consent != MetadataSearchConsent.ENABLED_BY_USER) return null

        val (endpoint, yearParameter) = when (terms.mediaType) {
            TmdbMediaType.MOVIE -> "movie" to "year"
            TmdbMediaType.TV -> "tv" to "first_air_date_year"
        }
        val title = URLEncoder.encode(terms.normalizedTitle, "UTF-8").replace("+", "%20")
        val year = terms.year?.takeIf { it in 1000..9999 }
        val query = "query=$title&include_adult=false" +
            (year?.let { "&$yearParameter=$it" } ?: "")
        return URI.create("$API_ORIGIN/3/search/$endpoint?$query")
    }

    fun details(
        id: Long,
        mediaType: TmdbMediaType,
        consent: MetadataSearchConsent = MetadataSearchConsent.DISABLED,
    ): URI? {
        if (consent != MetadataSearchConsent.ENABLED_BY_USER || id <= 0L) return null
        val endpoint = when (mediaType) {
            TmdbMediaType.MOVIE -> "movie"
            TmdbMediaType.TV -> "tv"
        }
        return URI.create("$API_ORIGIN/3/$endpoint/$id")
    }

    fun artwork(
        path: String,
        size: TmdbImageSize,
        consent: MetadataSearchConsent = MetadataSearchConsent.DISABLED,
    ): URI? {
        if (consent != MetadataSearchConsent.ENABLED_BY_USER) return null
        // A returned TMDB file path is data, never an arbitrary URL or directory.
        if (path.length > MAX_IMAGE_PATH_LENGTH || !imagePath.matches(path)) return null
        return URI.create("$IMAGE_ORIGIN/t/p/${size.segment}$path")
    }
}
