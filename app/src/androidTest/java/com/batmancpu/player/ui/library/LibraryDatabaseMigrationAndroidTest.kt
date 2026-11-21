package com.batmancpu.player.ui.library

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Builds an actual v1 SQLite database from the committed Room schema, then
 * opens it with the production Room builder so Room executes and validates
 * the real v1-to-v4 migration chain. This deliberately avoids MigrationTestHelper:
 * Room 2.8.5's bundled schema serializers crash in the Android test runtime.
 */
class LibraryDatabaseMigrationAndroidTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val databaseName = "player-library-v1-to-v4-test.db"
    private val schemaAsset =
        "com.batmancpu.player.ui.library.PlayerLibraryDatabase/1.json"

    @Test
    fun v1MediaSurvivesAndProviderMetadataTableWorksAfterMigration() = runBlocking {
        context.deleteDatabase(databaseName)
        val file = context.getDatabasePath(databaseName)
        file.parentFile?.mkdirs()

        try {
            createV1Database(file)
            val upgraded = Room.databaseBuilder(
                context,
                PlayerLibraryDatabase::class.java,
                databaseName,
            ).addMigrations(
                PlayerLibraryDatabase.MIGRATION_1_2,
                PlayerLibraryDatabase.MIGRATION_2_3,
                PlayerLibraryDatabase.MIGRATION_3_4,
            ).build()
            try {
                val indexed = upgraded.mediaDao().forTree("synthetic-tree").single()
                assertEquals("movie-1", indexed.documentId)
                assertEquals("Example Movie", indexed.title)
                assertEquals("", indexed.parentDocumentId)
                assertNull(upgraded.manualTmdbMatchDao().forDocument("synthetic-tree", "movie-1"))

                val confirmed = ManualTmdbMatch(
                    "synthetic-tree",
                    "movie-1",
                    123L,
                    "MOVIE",
                    1_700_000_000_000L,
                )
                upgraded.manualTmdbMatchDao().upsert(confirmed)
                assertEquals(
                    confirmed,
                    upgraded.manualTmdbMatchDao().forDocument("synthetic-tree", "movie-1"),
                )

                val metadata = CachedTmdbMetadata(
                    123L, "MOVIE", "Example Movie", 2020, "Offline description",
                    "/poster.jpg", null, 1_700_000_000_000L,
                )
                upgraded.cachedTmdbMetadataDao().upsert(metadata)
                assertEquals(metadata, upgraded.cachedTmdbMetadataDao().forContent(123L, "MOVIE"))
            } finally {
                upgraded.close()
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
    }

    private fun createV1Database(file: File) {
        val schema = instrumentation.context.assets.open(schemaAsset).bufferedReader().use {
            JSONObject(it.readText())
        }.getJSONObject("database")
        assertEquals(1, schema.getInt("version"))

        val entity = schema.getJSONArray("entities").getJSONObject(0)
        val tableName = entity.getString("tableName")
        assertEquals("indexed_media", tableName)
        val createTable = entity.getString("createSql").replace("\${TABLE_NAME}", tableName)

        val old = SQLiteDatabase.openOrCreateDatabase(file.absolutePath, null)
        try {
            old.execSQL(createTable)
            val setupQueries = schema.getJSONArray("setupQueries")
            for (index in 0 until setupQueries.length()) {
                old.execSQL(setupQueries.getString(index))
            }
            val values = ContentValues().apply {
                put("treeUri", "synthetic-tree")
                put("documentId", "movie-1")
                put("displayName", "Example Movie.mkv")
                put("mediaKind", "VIDEO")
                put("parsedKind", "MOVIE")
                put("title", "Example Movie")
                put("year", 2020)
                putNull("season")
                putNull("episode")
                put("scanToken", "scan-v1")
            }
            assertTrue(old.insertOrThrow(tableName, null, values) >= 0L)
            old.version = schema.getInt("version")
        } finally {
            old.close()
        }
    }
}

