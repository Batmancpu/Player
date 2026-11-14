package com.batmancpu.player.ui.library

import java.io.IOException
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
            listChildren = { TreeListing(children[it].orEmpty()) },
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
                TreeListing(when (parent) {
                    "root" -> listOf(directory("outside"), directory("inside"))
                    "inside" -> listOf(file("safe-video", "ok.webm"))
                    else -> error("Unexpected traversal beyond selected root")
                })
            },
            isWithinSelectedTree = { _, candidate -> candidate != "outside" },
        )

        assertThrows(IOException::class.java) { runBlocking { scanner.scan("root") } }
        assertEquals(listOf("root"), inspected)
    }

    @Test
    fun propagatesProviderFailureInsteadOfReturningPartialResults() {
        val scanner = MediaTreeScanner(
            listChildren = {
                if (it == "root") TreeListing(listOf(directory("nested")))
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

    @Test
    fun rejectsLoadingListingEvenWhenItContainsValidMedia() {
        assertIncompleteListingFails(isLoading = true, hasError = false)
    }

    @Test
    fun rejectsProviderErrorAfterEarlierDirectoriesWereRead() {
        assertIncompleteListingFails(isLoading = false, hasError = true)
    }

    private fun assertIncompleteListingFails(isLoading: Boolean, hasError: Boolean) {
        val visited = mutableListOf<String>()
        val scanner = MediaTreeScanner(
            listChildren = { parent ->
                visited.add(parent)
                if (parent == "root") {
                    TreeListing(listOf(file("earlier", "Earlier.mp4"), directory("nested")))
                } else {
                    TreeListing(
                        listOf(file("partial", "Partial.mkv"), directory("never-visit")),
                        isLoading = isLoading,
                        hasError = hasError,
                    )
                }
            },
            isWithinSelectedTree = { _, _ -> true },
        )
        var reconciliationReached = false
        assertThrows(IOException::class.java) {
            runBlocking {
                scanner.scan("root")
                reconciliationReached = true
            }
        }
        assertEquals(listOf("root", "nested"), visited)
        assertTrue(!reconciliationReached)
    }

    @Test
    fun completedEmptyListingIsSuccessful() = runBlocking {
        val scanner = MediaTreeScanner(
            listChildren = { TreeListing(emptyList()) },
            isWithinSelectedTree = { _, _ -> true },
        )
        assertTrue(scanner.scan("root").isEmpty())
    }
    @Test fun boundsTotalRowsIncludingDuplicatesAndHiddenFiles() {
        val scanner = MediaTreeScanner(
            listChildren = { TreeListing(List(4) { file("same", ".hidden") }) },
            isWithinSelectedTree = { _, _ -> true },
            limits = ScanLimits(entries = 3),
        )
        assertThrows(IOException::class.java) { runBlocking { scanner.scan("root") } }
    }

    @Test fun boundsDepthWithoutRecursiveStackGrowth() {
        var id = 0
        val scanner = MediaTreeScanner(
            listChildren = { TreeListing(listOf(directory("dir-${++id}"))) },
            isWithinSelectedTree = { _, _ -> true },
            limits = ScanLimits(depth = 2),
        )
        assertThrows(IOException::class.java) { runBlocking { scanner.scan("root") } }
        assertEquals(3, id)
    }

    @Test fun boundsWideDirectoryQueues() {
        val scanner = MediaTreeScanner(
            listChildren = { TreeListing(listOf(directory("a"), directory("b"))) },
            isWithinSelectedTree = { _, _ -> true },
            limits = ScanLimits(directories = 2),
        )
        assertThrows(IOException::class.java) { runBlocking { scanner.scan("root") } }
    }

    @Test fun cyclesAreDeduplicatedAndDoNotConsumeDirectoryBudget() = runBlocking {
        val visited = mutableListOf<String>()
        val scanner = MediaTreeScanner(
            listChildren = { parent ->
                visited.add(parent)
                TreeListing(listOf(directory("root"), file("video", "Movie.mp4")))
            },
            isWithinSelectedTree = { _, _ -> true },
            limits = ScanLimits(directories = 1),
        )
        assertEquals(listOf("video"), scanner.scan("root").map { it.documentId })
        assertEquals(listOf("root"), visited)
    }

}

