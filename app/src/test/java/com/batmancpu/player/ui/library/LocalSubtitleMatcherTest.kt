package com.batmancpu.player.ui.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalSubtitleMatcherTest {
    private fun video(id: String, name: String, parent: String = "opaque-parent-A") =
        DiscoveredLocalMedia(id, name, LocalMediaKind.VIDEO, parent)

    private fun subtitle(id: String, name: String, parent: String = "opaque-parent-A") =
        DiscoveredLocalMedia(id, name, LocalMediaKind.SUBTITLE, parent)

    @Test
    fun matchesExactAndLanguageForcedNamesOnlyInSameImmediateParent() {
        val film = video("v1", "The.Movie.2020.mkv")
        val files = listOf(
            subtitle("exact", "The.Movie.2020.srt"),
            subtitle("localized", "The.Movie.2020.en.forced.srt"),
            subtitle("region", "The.Movie.2020.pt-BR.vtt"),
            subtitle("wrong-parent", "The.Movie.2020.srt", "opaque-parent-B"),
            subtitle("near-name", "The.Movie.2020.extra.srt"),
            subtitle("unsupported", "The.Movie.2020.sub"),
            video("other-video", "The.Movie.2020.mp4"),
        )

        assertEquals(
            listOf("localized", "region", "exact"),
            LocalSubtitleMatcher.forVideo(film, files).map { it.documentId },
        )
    }

    @Test
    fun doesNotGuessParentFromDisplayNameOrDocumentId() {
        val noParent = video("folder/Film.mkv", "Film.mkv", "")
        val candidates = listOf(subtitle("folder/Film.srt", "Film.srt", ""))

        assertTrue(LocalSubtitleMatcher.forVideo(noParent, candidates).isEmpty())
    }

    @Test
    fun simpleMovieStemSurvivesLanguageSuffixRemoval() {
        val film = video("movie", "Film.mp4")
        val caption = subtitle("caption", "Film.en.srt")

        assertEquals(listOf("caption"), LocalSubtitleMatcher.forVideo(film, listOf(caption)).map { it.documentId })
    }

    @Test
    fun capsSubtitleCandidates() {
        val film = video("movie", "Film.mp4")
        val candidates = (1..12).map { subtitle("s$it", "Film.en.srt") }

        assertEquals(8, LocalSubtitleMatcher.forVideo(film, candidates, maxResults = 8).size)
    }
}
