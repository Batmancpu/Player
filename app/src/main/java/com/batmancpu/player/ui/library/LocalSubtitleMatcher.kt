package com.batmancpu.player.ui.library

import java.text.Normalizer
import java.util.Locale

/**
 * Matches only supported local subtitle documents from the same immediate SAF parent.
 * Document IDs remain opaque identities; filenames are compared only as display names.
 */
internal object LocalSubtitleMatcher {
    private val supportedExtensions = setOf("srt", "vtt", "ass", "ssa", "ttml")
    private val suffixFlags = setOf("forced", "sdh", "cc", "default")
    private val twoLetterLanguages = Locale.getISOLanguages().map(String::lowercase).toSet()
    private val threeLetterLanguages = Locale.getAvailableLocales().mapNotNull { locale ->
        runCatching { locale.isO3Language.lowercase() }.getOrNull()
    }.toSet()

    fun forVideo(
        video: DiscoveredLocalMedia,
        indexedMedia: List<DiscoveredLocalMedia>,
        maxResults: Int = 8,
    ): List<DiscoveredLocalMedia> {
        require(maxResults in 1..32)
        if (video.kind != LocalMediaKind.VIDEO || video.parentDocumentId.isBlank()) return emptyList()
        val videoStem = normalizedStem(video.displayName, setOf("mp4", "m4v", "mkv", "avi", "mov", "webm", "ts", "m2ts", "mpg", "mpeg", "wmv", "flv", "3gp", "3g2"))
            ?: return emptyList()
        return indexedMedia.asSequence()
            .filter { it.kind == LocalMediaKind.SUBTITLE && it.parentDocumentId == video.parentDocumentId }
            .mapNotNull { subtitle ->
                val subtitleStem = normalizedStem(subtitle.displayName, supportedExtensions) ?: return@mapNotNull null
                if (subtitleStem == videoStem || stripSubtitleSuffixes(subtitleStem) == videoStem) subtitle else null
            }
            .distinctBy { it.documentId }
            .sortedBy { it.displayName.lowercase(Locale.ROOT) }
            .take(maxResults)
            .toList()
    }

    private fun normalizedStem(name: String, extensions: Set<String>): String? {
        if (name.isBlank() || name.any { it == '/' || it == '\\' || it.isISOControl() }) return null
        val extension = name.substringAfterLast('.', "").lowercase(Locale.ROOT)
        if (extension !in extensions) return null
        val stem = name.dropLast(extension.length + 1).trim()
        if (stem.isBlank()) return null
        return Normalizer.normalize(stem, Normalizer.Form.NFKC).lowercase(Locale.ROOT)
    }

    private fun stripSubtitleSuffixes(stem: String): String {
        var result = stem
        repeat(3) {
            val suffix = result.substringAfterLast('.', "")
            val normalized = suffix.replace('_', '-')
            val language = normalized.substringBefore('-')
            val isLocale = language in twoLetterLanguages || language in threeLetterLanguages
            val region = normalized.substringAfter('-', "")
            val validRegion = region.isEmpty() || region.matches(Regex("[a-z]{2}|[0-9]{3}"))
            if (suffixFlags.contains(normalized) || (isLocale && validRegion)) {
                result = result.substringBeforeLast('.', "")
            }
        }
        return result
    }
}
