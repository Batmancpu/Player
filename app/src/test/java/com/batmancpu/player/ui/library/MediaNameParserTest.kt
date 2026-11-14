package com.batmancpu.player.ui.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaNameParserTest {
    @Test fun parsesMovieYearWithoutReleaseNoise() {
        assertEquals(
            ParsedMediaName.Movie("The Matrix", 1999),
            MediaNameParser.parse("The.Matrix.1999.1080p.BluRay.x265.mkv"),
        )
    }

    @Test fun parsesSxxExx() {
        assertEquals(
            ParsedMediaName.Episode("Breaking Bad", 1, 2),
            MediaNameParser.parse("Breaking.Bad.S01E02.1080p.WEB-DL.mkv"),
        )
    }

    @Test fun parsesOneByTwoAndSeasonEpisode() {
        assertEquals(
            ParsedMediaName.Episode("Fringe", 2, 5),
            MediaNameParser.parse("Fringe.2x05.mkv"),
        )
        assertEquals(
            ParsedMediaName.Episode("Fringe", 2, 5),
            MediaNameParser.parse("Fringe Season 2 05.mp4"),
        )
    }

    @Test fun preservesUnicodeAndNoYear() {
        assertEquals(
            ParsedMediaName.Movie("千と千尋の神隠し", null),
            MediaNameParser.parse("千と千尋の神隠し.mkv"),
        )
    }

    @Test fun neverMakesUpAnEpisodeWithoutSeries() {
        assertTrue(MediaNameParser.parse("S01E03.mkv") is ParsedMediaName.Unrecognized)
    }
}
