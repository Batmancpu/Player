package com.mangoloads.player.ui.library

import java.text.Normalizer

internal enum class TmdbMediaType { MOVIE, TV }

/** This contract has no network side effects; disabled is the safe default. */
internal enum class MetadataSearchConsent { DISABLED, ENABLED_BY_USER }

/**
 * Privacy-limited normalized search input for a future direct TMDB lookup.
 * It intentionally cannot carry a filename, document/tree URI, local identity,
 * episode numbers, or media bytes.
 */
internal data class TmdbSearchTerms private constructor(
    val normalizedTitle: String,
    val year: Int?,
    val mediaType: TmdbMediaType,
) {
    companion object {
        private val localFileExtensions = setOf(
            "mp4", "m4v", "mkv", "avi", "mov", "webm", "ts", "m2ts", "mpg",
            "mpeg", "wmv", "flv", "3gp", "3g2", "srt", "ass", "ssa", "vtt",
            "ttml", "sub", "idx", "nfo", "txt",
        )
        private val uriPrefixes = listOf("content:", "file:", "http:", "https:", "smb:", "webdav:")
        private const val MAX_TITLE_CODE_POINTS = 120

        /**
         * A query is produced only for recognized movies/series and an explicit
         * opt-in. The default keeps metadata lookup disabled until a later setting
         * persists the user's choice. Episode season/number are never included.
         */
        fun from(
            parsed: ParsedMediaName,
            consent: MetadataSearchConsent = MetadataSearchConsent.DISABLED,
        ): TmdbSearchTerms? {
            if (consent != MetadataSearchConsent.ENABLED_BY_USER) return null

            val (rawTitle, rawYear, mediaType) = when (parsed) {
                is ParsedMediaName.Movie -> Triple(parsed.title, parsed.year, TmdbMediaType.MOVIE)
                is ParsedMediaName.Episode -> Triple(parsed.series, null, TmdbMediaType.TV)
                is ParsedMediaName.Unrecognized -> return null
            }
            val title = normalizeTitle(rawTitle) ?: return null
            val safeYear = rawYear?.takeIf { it in 1000..9999 }
            return TmdbSearchTerms(title, safeYear, mediaType)
        }

        private fun normalizeTitle(raw: String): String? {
            // Reject controls before whitespace normalization so newlines cannot
            // silently become part of a transmitted search string.
            if (raw.any { Character.isISOControl(it.code) }) return null

            val title = compactWhitespace(Normalizer.normalize(raw, Normalizer.Form.NFC))
            if (title.isEmpty()) return null
            if (title.codePointCount(0, title.length) > MAX_TITLE_CODE_POINTS) return null
            if (title.contains('/') || title.contains('\\')) return null

            val lowered = title.lowercase()
            if (uriPrefixes.any { prefix -> lowered.startsWith(prefix) }) return null
            if (title.length >= 2 && title[0].isLetter() && title[1] == ':') return null
            val suffix = title.substringAfterLast('.', "").lowercase()
            if (suffix in localFileExtensions) return null
            if (title.none { it.isLetterOrDigit() }) return null
            return title
        }

        private fun compactWhitespace(value: String): String {
            val result = StringBuilder()
            var needsSpace = false
            var index = 0
            while (index < value.length) {
                val codePoint = value.codePointAt(index)
                if (Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint)) {
                    if (result.length > 0) needsSpace = true
                } else {
                    if (needsSpace) result.append(' ')
                    result.appendCodePoint(codePoint)
                    needsSpace = false
                }
                index += Character.charCount(codePoint)
            }
            return result.toString()
        }
    }
}
