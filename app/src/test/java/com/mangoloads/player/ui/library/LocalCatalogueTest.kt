package com.mangoloads.player.ui.library

import org.junit.Assert.*
import org.junit.Test

class LocalCatalogueTest {
    private fun video(id: String, name: String) = DiscoveredLocalMedia(id, name, LocalMediaKind.VIDEO)
    @Test fun groupsSeriesOnceAndOrdersEpisodesWithoutLosingMovieIdentity() {
        val result = LocalCatalogue.build(listOf(
            video("e2", "Example.Show.S02E01.mkv"), video("e1", "Example.Show.S01E02.mp4"),
            video("m1", "Example.Movie.2020.mkv"),
            DiscoveredLocalMedia("sub", "Example.Movie.srt", LocalMediaKind.SUBTITLE),
        ))
        assertEquals(2, result.size)
        val show = result.single { it.isSeries }
        assertEquals(listOf("e1", "e2"), show.files.map { it.documentId })
        assertEquals(2020, result.single { !it.isSeries }.year)
    }
    @Test fun searchesTitleYearAndEpisodeTokensWithTypeFilter() {
        val titles = LocalCatalogue.build(listOf(video("m", "Example.Movie.2020.mp4"), video("e", "Example.Show.S01E02.mkv")))
        assertEquals("m", LocalCatalogue.search(titles, "example 2020", CatalogueFilter.ALL).single().files.single().documentId)
        assertEquals("e", LocalCatalogue.search(titles, "S01E02", CatalogueFilter.SERIES).single().files.single().documentId)
        assertTrue(LocalCatalogue.search(titles, "movie", CatalogueFilter.SERIES).isEmpty())
        assertTrue(LocalCatalogue.search(titles, "missing", CatalogueFilter.ALL).isEmpty())
    }
    @Test fun retainsUnicodeAndMultiEpisodeAmbiguity() {
        val titles = LocalCatalogue.build(listOf(video("u", "千と千尋の神隠し.2001.mp4"), video("x", "Example.S01E01E02.mkv")))
        assertEquals("u", LocalCatalogue.search(titles, "千と千尋", CatalogueFilter.MOVIES).single().files.single().documentId)
        assertEquals("Example.S01E01E02.mkv", titles.single { it.files.single().documentId == "x" }.title)
    }
    @Test fun thousandFilesKeepAllDocumentIdentities() {
        val input = (1..1000).map { video("id-$it", "Movie.$it.2020.mp4") }
        val titles = LocalCatalogue.build(input)
        assertEquals(input.map { it.documentId }.toSet(), titles.flatMap { it.files }.map { it.documentId }.toSet())
    }
}
