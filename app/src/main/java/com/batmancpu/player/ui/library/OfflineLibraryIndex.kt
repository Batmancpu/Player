package com.batmancpu.player.ui.library

import android.content.Context
import android.net.Uri
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.withTransaction
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** Folder tree ID and provider document ID form the stable local identity. */
@Entity(tableName = "indexed_media", primaryKeys = ["treeUri", "documentId"])
internal data class IndexedMedia(
    val treeUri: String,
    val documentId: String,
    val displayName: String,
    val mediaKind: String,
    val parsedKind: String,
    val title: String,
    val year: Int?,
    val season: Int?,
    val episode: Int?,
    val scanToken: String,
)

/** A user-confirmed TMDB match, private to one selected SAF tree/document. */
@Entity(tableName = "manual_tmdb_matches", primaryKeys = ["treeUri", "documentId"])
internal data class ManualTmdbMatch(
    val treeUri: String,
    val documentId: String,
    val tmdbId: Long,
    val mediaType: String,
    val confirmedAtEpochMs: Long,
)

@Dao
internal interface IndexedMediaDao {
    @Query("SELECT * FROM indexed_media WHERE treeUri = :tree ORDER BY title, season, episode")
    suspend fun forTree(tree: String): List<IndexedMedia>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(items: List<IndexedMedia>)

    @Query("DELETE FROM indexed_media WHERE treeUri = :tree AND scanToken != :token")
    suspend fun removeStale(tree: String, token: String)

    @Query("DELETE FROM indexed_media WHERE treeUri = :tree")
    suspend fun removeTree(tree: String)

    @Query("DELETE FROM indexed_media WHERE treeUri != :selectedTree")
    suspend fun removeOtherTrees(selectedTree: String)
}

@Dao
internal interface ManualTmdbMatchDao {
    @Query("SELECT * FROM manual_tmdb_matches WHERE treeUri = :tree AND documentId = :documentId")
    suspend fun forDocument(tree: String, documentId: String): ManualTmdbMatch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(match: ManualTmdbMatch)

    @Query("DELETE FROM manual_tmdb_matches WHERE treeUri = :tree AND documentId = :documentId")
    suspend fun remove(tree: String, documentId: String)

    @Query("DELETE FROM manual_tmdb_matches WHERE treeUri = :tree")
    suspend fun removeTree(tree: String)

    @Query("DELETE FROM manual_tmdb_matches WHERE treeUri != :selectedTree")
    suspend fun removeOtherTrees(selectedTree: String)

    @Query("DELETE FROM manual_tmdb_matches WHERE treeUri = :tree AND documentId NOT IN (SELECT documentId FROM indexed_media WHERE treeUri = :tree)")
    suspend fun removeOrphans(tree: String)
}

@Database(entities = [IndexedMedia::class, ManualTmdbMatch::class], version = 2, exportSchema = true)
internal abstract class PlayerLibraryDatabase : RoomDatabase() {
    abstract fun mediaDao(): IndexedMediaDao
    abstract fun manualTmdbMatchDao(): ManualTmdbMatchDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS manual_tmdb_matches (" +
                        "treeUri TEXT NOT NULL, documentId TEXT NOT NULL, tmdbId INTEGER NOT NULL, " +
                        "mediaType TEXT NOT NULL, confirmedAtEpochMs INTEGER NOT NULL, " +
                        "PRIMARY KEY(treeUri, documentId))",
                )
            }
        }

        @Volatile private var instance: PlayerLibraryDatabase? = null

        fun get(context: Context): PlayerLibraryDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlayerLibraryDatabase::class.java,
                    "player-local-library.db",
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}

/**
 * Only a fully completed scan is eligible for reconciliation. Failed/cancelled
 * SAF enumeration never reaches saveSuccessfulScan, so existing rows remain.
 * The upsert + missing-file deletion are atomic and read-only to the media source.
 */
internal object OfflineLibraryIndex {
    /** Serialized by LibrarySession after obsolete work has unwound. */
    suspend fun activateAndLoad(context: Context, treeUri: Uri): List<DiscoveredLocalMedia> =
        withContext(Dispatchers.IO) {
            val db = PlayerLibraryDatabase.get(context)
            db.withTransaction {
                db.mediaDao().removeOtherTrees(treeUri.toString())
                db.manualTmdbMatchDao().removeOtherTrees(treeUri.toString())
                toDiscovered(db.mediaDao().forTree(treeUri.toString()))
            }
        }

    suspend fun load(context: Context, treeUri: Uri): List<DiscoveredLocalMedia> =
        withContext(Dispatchers.IO) {
            toDiscovered(PlayerLibraryDatabase.get(context).mediaDao().forTree(treeUri.toString()))
        }

    private suspend fun toDiscovered(records: List<IndexedMedia>): List<DiscoveredLocalMedia> {
        val workContext = currentCoroutineContext()
        return records.map {
            workContext.ensureActive()
            DiscoveredLocalMedia(
                documentId = it.documentId,
                displayName = it.displayName,
                kind = LocalMediaKind.valueOf(it.mediaKind),
            )
        }
    }

    suspend fun saveSuccessfulScan(
        context: Context,
        treeUri: Uri,
        discovered: List<DiscoveredLocalMedia>,
    ) = withContext(Dispatchers.IO) {
        val workContext = currentCoroutineContext()
        val token = UUID.randomUUID().toString()
        val root = treeUri.toString()
        val records = discovered.map { media ->
            workContext.ensureActive()
            val parsed = if (media.kind == LocalMediaKind.VIDEO) {
                MediaNameParser.parse(media.displayName)
            } else {
                ParsedMediaName.Unrecognized(media.displayName)
            }
            IndexedMedia(
                treeUri = root,
                documentId = media.documentId,
                displayName = media.displayName,
                mediaKind = media.kind.name,
                parsedKind = when (parsed) {
                    is ParsedMediaName.Movie -> "MOVIE"
                    is ParsedMediaName.Episode -> "EPISODE"
                    is ParsedMediaName.Unrecognized -> "UNKNOWN"
                },
                title = when (parsed) {
                    is ParsedMediaName.Movie -> parsed.title
                    is ParsedMediaName.Episode -> parsed.series
                    is ParsedMediaName.Unrecognized -> media.displayName
                },
                year = (parsed as? ParsedMediaName.Movie)?.year,
                season = (parsed as? ParsedMediaName.Episode)?.season,
                episode = (parsed as? ParsedMediaName.Episode)?.episode,
                scanToken = token,
            )
        }
        val db = PlayerLibraryDatabase.get(context)
        db.withTransaction {
            db.mediaDao().upsert(records)
            db.mediaDao().removeStale(root, token)
            db.manualTmdbMatchDao().removeOrphans(root)
        }
    }

    suspend fun forgetTree(context: Context, treeUri: Uri): Unit = withContext(Dispatchers.IO) {
        val db = PlayerLibraryDatabase.get(context)
        db.withTransaction {
            db.manualTmdbMatchDao().removeTree(treeUri.toString())
            db.mediaDao().removeTree(treeUri.toString())
        }
    }
}

