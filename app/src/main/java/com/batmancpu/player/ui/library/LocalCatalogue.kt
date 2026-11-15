package com.batmancpu.player.ui.library

internal enum class CatalogueFilter { ALL, MOVIES, SERIES }
internal data class LocalTitle(
    val key: String,
    val title: String,
    val year: Int?,
    val isSeries: Boolean,
    val files: List<DiscoveredLocalMedia>,
)
internal data class CatalogueSnapshot(val tree: String? = null, val titles: List<LocalTitle> = emptyList())

/** Local filename interpretation only, never a TMDB identification claim. */
internal object LocalCatalogue {
    fun build(media: List<DiscoveredLocalMedia>): List<LocalTitle> {
        val movies = mutableListOf<LocalTitle>()
        val series = linkedMapOf<String, MutableList<Pair<DiscoveredLocalMedia, ParsedMediaName.Episode>>>()
        for (file in media) {
            if (file.kind != LocalMediaKind.VIDEO) continue
            when (val parsed = MediaNameParser.parse(file.displayName)) {
                is ParsedMediaName.Episode -> series.getOrPut(parsed.series.lowercase()) { mutableListOf() }.add(file to parsed)
                is ParsedMediaName.Movie -> movies.add(LocalTitle("file:${file.documentId}", parsed.title, parsed.year, false, listOf(file)))
                is ParsedMediaName.Unrecognized -> movies.add(LocalTitle("file:${file.documentId}", file.displayName, null, false, listOf(file)))
            }
        }
        return (movies + series.map { (key, episodes) ->
            val ordered = episodes.sortedWith(compareBy({ it.second.season }, { it.second.episode }, { it.first.displayName }))
            LocalTitle("series:$key", ordered.first().second.series, null, true, ordered.map { it.first })
        }).sortedWith(compareBy({ it.title.lowercase() }, { it.year ?: 0 }, { it.key }))
    }

    fun search(titles: List<LocalTitle>, query: String, filter: CatalogueFilter): List<LocalTitle> {
        val tokens = query.trim().split(Regex("\\s+")).filter(String::isNotBlank)
        return titles.filter { title ->
            val kindMatches = filter == CatalogueFilter.ALL ||
                (filter == CatalogueFilter.SERIES) == title.isSeries
            kindMatches && tokens.all { token ->
                title.title.contains(token, true) || title.year?.toString()?.contains(token) == true ||
                    title.files.any { it.displayName.contains(token, true) }
            }
        }
    }
}
