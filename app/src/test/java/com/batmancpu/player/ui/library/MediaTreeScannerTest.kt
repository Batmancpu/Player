package com.batmancpu.player.ui.library

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaTreeScannerTest {
    private fun directory(id: String, name: String = id) =
        TreeEntry(id, name, "vnd.android.document/directory", true)

    private fun file(id: String, name: String, mime: String? = null) =
        TreeEntry(id, name, mime, false)

    @Test
    fun findsNestedVideosAndSubtitlesWithoutDuplicates() = runBlocking {
        val children = mapOf(
            "root" to listOf(
                file("film", "Movie.MP4", "application/octet-stream"),
                directory("series"),
                file("ignore", "poster.jpg", "image/jpeg"),
                directory("hidden", ".private"),
            ),
            "series" to listOf(
                file("episode", "Show.S01E01.mkv"),
                file("caption", "Show.S01E01.srt"),
                file("film", "Movie.MP4"),
            ),
        )
        val scanner = MediaTreeScanner(
            listChildren = { children[it].orEmpty() },
            isWithinSelectedTree = { _, _ -> true },
        )

        val results = scanner.scan("root")

        assertEquals(listOf("film", "episode", "caption"), results.map { it.documentId })
        assertEquals(
            listOf(LocalMediaKind.VIDEO, LocalMediaKind.VIDEO, LocalMediaKind.SUBTITLE),
            results.map { it.kind },
        )
    }

    @Test
    fun doesNotVisitOrCollectOutOfTreeDocuments() = runBlocking {
        val inspected = mutableListOf<String>()
        val scanner = MediaTreeScanner(
            listChildren = { parent ->
                inspected.add(parent)
                when (parent) {
                    "root" -> listOf(directory("outside"), directory("inside"))
                    "inside" -> listOf(file("safe-video", "ok.webm"))
                    else -> error("Unexpected traversal beyond selected root")
                }
            },
            isWithinSelectedTree = { _, candidate -> candidate != "outside" },
        )

        assertEquals(listOf("safe-video"), scanner.scan("root").map { it.documentId })
        assertEquals(listOf("root", "inside"), inspected)
    }

    @Test
    fun propagatesProviderFailureInsteadOfReturningPartialResults() {
        val scanner = MediaTreeScanner(
            listChildren = {
                if (it == "root") listOf(directory("nested"))
                else throw SecurityException("Permission revoked")
            },
            isWithinSelectedTree = { _, _ -> true },
        )
        assertThrows(SecurityException::class.java) {
            runBlocking { scanner.scan("root") }
        }
    }

    @Test
    fun cancelledScanStopsPromptly() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val scanner = MediaTreeScanner(
            listChildren = {
                started.complete(Unit)
                awaitCancellation()
            },
            isWithinSelectedTree = { _, _ -> true },
        )
        val task = async { scanner.scan("root") }
        started.await()
        task.cancel()
        task.join()
        assertTrue(task.isCancelled)
    }
}
