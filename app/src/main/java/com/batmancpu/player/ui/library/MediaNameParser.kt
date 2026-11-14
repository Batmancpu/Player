package com.batmancpu.player.ui.library

/**
 * Deterministic local filename interpretation. This parser never accesses the network
 * and deliberately leaves ambiguous or unrecognizable files under their original names.
 */
internal sealed interface ParsedMediaName {
    data class Movie(val title: String, val year: Int?) : ParsedMediaName
    data class Episode(
        val series: String,
        val season: Int,
        val episode: Int,
    ) : ParsedMediaName
    data class Unrecognized(val originalName: String) : ParsedMediaName
}

internal object MediaNameParser {
    private val sxxExx = Regex("""(?i)\bS(\d{1,2})[ ._-]*E(\d{1,3})\b""")
    private val oneByTwo = Regex("""(?i)\b(\d{1,2})x(\d{1,3})\b""")
    private val seasonNumber = Regex("""(?i)\bseason[ ._-]*(\d{1,2})[ ._-]+(?:episode[ ._-]*)?(\d{1,3})\b""")
    private val yearPattern = Regex("""\b(19\d{2}|20\d{2})\b""")
    private val releaseNoise = Regex(
        """(?i)\b(?:2160p|1080p|720p|480p|4k|uhd|hdr10\+?|hdr|dv|bluray|brrip|bdrip|web[ ._-]?dl|webrip|dvdrip|remux|x264|x265|h264|h265|hevc|av1|aac|ac3|dts|atmos|proper|repack|extended|yify|rarbg)\b"""
    )
    private val spaces = Regex("""\s+""")
    private val supportedExtensions = setOf("mp4", "m4v", "mkv", "avi", "mov", "webm", "ts", "m2ts", "mpg", "mpeg", "wmv", "flv", "3gp", "3g2")

    fun parse(fileName: String): ParsedMediaName {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val stem = if (extension in supportedExtensions) fileName.dropLast(extension.length + 1) else fileName
        val cleanStem = stem.replace(Regex("""[._]+"""), " ")
            .replace(Regex("""[\[\]()]"""), " ")
            .replace(spaces, " ")
            .trim()

        val match = sxxExx.find(cleanStem) ?: oneByTwo.find(cleanStem) ?: seasonNumber.find(cleanStem)
        if (match != null) {
            val title = normalizeTitle(cleanStem.substring(0, match.range.first))
            val season = match.groupValues[1].toIntOrNull()
            val episode = match.groupValues[2].toIntOrNull()
            if (title.isNotBlank() && season != null && episode != null && episode > 0) {
                return ParsedMediaName.Episode(title, season, episode)
            }
            return ParsedMediaName.Unrecognized(fileName)
        }

        val year = yearPattern.find(cleanStem)
        val titlePart = if (year == null) cleanStem else cleanStem.substring(0, year.range.first)
        val title = normalizeTitle(titlePart)
        if (title.isBlank()) return ParsedMediaName.Unrecognized(fileName)
        return ParsedMediaName.Movie(title, year?.value?.toInt())
    }

    private fun normalizeTitle(raw: String): String {
        val beforeNoise = releaseNoise.find(raw)?.range?.first?.let(raw::substring) ?: raw
        return beforeNoise.replace(Regex("""[._\-]+"""), " ")
            .replace(spaces, " ")
            .trim(' ', '-', '.', '[', ']', '(', ')')
    }
}
