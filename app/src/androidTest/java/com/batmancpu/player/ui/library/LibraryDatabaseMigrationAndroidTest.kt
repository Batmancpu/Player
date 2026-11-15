package com.batmancpu.player.ui.library

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LibraryDatabaseMigrationAndroidTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val databaseName = "player-library-v1-to-v2-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PlayerLibraryDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun v1MediaSurvivesAndManualOverrideTableWorksAfterMigration() = runBlocking {
        context.deleteDatabase(databaseName)
        try {
            val oldDatabase = helper.createDatabase(databaseName, 1)
            oldDatabase.execSQL(
                "INSERT INTO indexed_media " +
                    "(treeUri, documentId, displayName, mediaKind, parsedKind, title, year, season, episode, scanToken) " +
                    "VALUES ('synthetic-tree', 'movie-1', 'Example Movie.mkv', 'VIDEO', 'MOVIE', 'Example Movie', 2020, NULL, NULL, 'scan-v1')",
            )
            oldDatabase.close()

            val upgraded = Room.databaseBuilder(
                context,
                PlayerLibraryDatabase::class.java,
                databaseName,
            ).addMigrations(PlayerLibraryDatabase.MIGRATION_1_2).build()
            try {
                val indexed = upgraded.mediaDao().forTree("synthetic-tree").single()
                assertEquals("movie-1", indexed.documentId)
                assertEquals("Example Movie", indexed.title)
                assertNull(upgraded.manualTmdbMatchDao().forDocument("synthetic-tree", "movie-1"))

                val confirmed = ManualTmdbMatch("synthetic-tree", "movie-1", 123L, "MOVIE", 1_700_000_000_000L)
                upgraded.manualTmdbMatchDao().upsert(confirmed)
                assertEquals(confirmed, upgraded.manualTmdbMatchDao().forDocument("synthetic-tree", "movie-1"))
            } finally {
                upgraded.close()
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
    }
}
