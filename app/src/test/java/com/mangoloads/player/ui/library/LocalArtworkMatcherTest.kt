package com.mangoloads.player.ui.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalArtworkMatcherTest {
    @Test fun prefersExactAdjacentArtworkOverGenericCover() {
        val title = movie("Example Film", 2020, "Example.Film.2020.mkv")
        val candidates = listOf(
            image("poster.jpg"),
            image("Example.Film.2020-poster.png"),
            image("Example.Film.2020.webp"),
        )
        assertEquals("art-Example.Film.2020", LocalArtworkMatcher.choose(title, title.files.single(), candidates))
    }

    @Test fun doesNotApplyAnAmbiguousGenericPosterToMultipleMovies() {
        val title = LocalTitle(
            "pair", "Collection", null, false,
            listOf(video("One.mkv"), video("Two.mkv")),
        )
        assertNull(LocalArtworkMatcher.choose(title, title.files.first(), listOf(image("poster.jpg"), image("cover.png"))))
    }

    @Test fun seriesCanUseOneFolderPosterAndImageMustBeSupported() {
        val episode = video("Example.Show.S01E01.mkv")
        val title = LocalTitle("series:example", "Example Show", null, true, listOf(episode))
        assertEquals("art-poster", LocalArtworkMatcher.choose(title, episode, listOf(videoCandidate(episode), image("poster.jpg"))))
        assertNull(LocalArtworkMatcher.choose(title, episode, listOf(image("poster.svg"))))
    }

    @Test fun seriesDoesNotInheritGenericArtFromAnotherShowInSharedFolder() {
        val episode = video("Example.Show.S01E01.mkv")
        val otherShow = videoCandidate(video("Another.Show.S01E01.mkv"))
        val title = LocalTitle("series:example", "Example Show", null, true, listOf(episode))
        assertNull(LocalArtworkMatcher.choose(title, episode, listOf(otherShow, image("poster.jpg"))))
    }

    private fun movie(title: String, year: Int, name: String) = LocalTitle(
        key = "movie", title = title, year = year, isSeries = false, files = listOf(video(name)),
    )

    private fun video(name: String) = DiscoveredLocalMedia(name, name, LocalMediaKind.VIDEO, "parent")

    private fun image(name: String) = LocalArtworkCandidate(
        documentId = "art-${name.substringBeforeLast('.')}", displayName = name, mimeType = "image/${name.substringAfterLast('.')}"
            .replace("jpg", "jpeg"),
    )

    private fun videoCandidate(media: DiscoveredLocalMedia) = LocalArtworkCandidate(
        documentId = media.documentId,
        displayName = media.displayName,
        mimeType = "video/x-matroska",
    )
}
