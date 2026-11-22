package com.mangoloads.player.ui.library

import android.content.Context
import android.net.Uri
import androidx.room.Dao
import androidx.room.Database
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Embedded
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
    @ColumnInfo(defaultValue = "''") val parentDocumentId: String = "",
)

/** A validated TMDB binding, private to one selected SAF tree/document. */
@Entity(tableName = "manual_tmdb_matches", primaryKeys = ["treeUri", "documentId"])
internal data class ManualTmdbMatch(
    val treeUri: String,
    val documentId: String,
    val tmdbId: Long,
    val mediaType: String,
    val confirmedAtEpochMs: Long,
)

/** Provider metadata contains no source-file path or content; matches stay separate. */
@Entity(tableName = "cached_tmdb_metadata", primaryKeys = ["tmdbId", "mediaType"])
internal data class CachedTmdbMetadata(
    val tmdbId: Long,
    val mediaType: String,
    val title: String,
    val year: Int?,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val cachedAtEpochMs: Long,
)

internal data class DocumentTmdbMetadata(
    val documentId: String,
    @Embedded val metadata: CachedTmdbMetadata,
)

@Dao
internal interface CachedTmdbMetadataDao {
    @Query("SELECT * FROM cached_tmdb_metadata WHERE tmdbId = :id AND mediaType = :type")
    suspend fun forContent(id: Long, type: String): CachedTmdbMetadata?

    @Query("SELECT matches.documentId, metadata.* FROM manual_tmdb_matches AS matches " +
        "INNER JOIN cached_tmdb_metadata AS metadata ON metadata.tmdbId = matches.tmdbId AND metadata.mediaType = matches.mediaType " +
        "INNER JOIN indexed_media AS media ON media.treeUri = matches.treeUri AND media.documentId = matches.documentId " +
        "WHERE matches.treeUri = :tree AND media.mediaKind = 'VIDEO'")
    suspend fun forTree(tree: String): List<DocumentTmdbMetadata>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: CachedTmdbMetadata)

    @Query("DELETE FROM cached_tmdb_metadata WHERE NOT EXISTS " +
        "(SELECT 1 FROM manual_tmdb_matches AS matches WHERE matches.tmdbId = cached_tmdb_metadata.tmdbId " +
        "AND matches.mediaType = cached_tmdb_metadata.mediaType)")
    suspend fun removeOrphans()
}

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

    @Query("SELECT EXISTS(SELECT 1 FROM manual_tmdb_matches WHERE treeUri = :tree AND documentId IN (:documentIds) AND tmdbId = 0 AND mediaType = 'NONE')")
    suspend fun hasLookupSuppression(tree: String, documentIds: List<String>): Boolean

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

@Database(entities = [IndexedMedia::class, ManualTmdbMatch::class, CachedTmdbMetadata::class], version = 4, exportSchema = true)
internal abstract class PlayerLibraryDatabase : RoomDatabase() {
    abstract fun mediaDao(): IndexedMediaDao
    abstract fun manualTmdbMatchDao(): ManualTmdbMatchDao
    abstract fun cachedTmdbMetadataDao(): CachedTmdbMetadataDao

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

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS cached_tmdb_metadata (" +
                        "tmdbId INTEGER NOT NULL, mediaType TEXT NOT NULL, title TEXT NOT NULL, year INTEGER, " +
                        "overview TEXT NOT NULL, posterPath TEXT, backdropPath TEXT, cachedAtEpochMs INTEGER NOT NULL, " +
                        "PRIMARY KEY(tmdbId, mediaType))",
                )
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE indexed_media ADD COLUMN parentDocumentId TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile private var instance: PlayerLibraryDatabase? = null

        fun get(context: Context): PlayerLibraryDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlayerLibraryDatabase::class.java,
                    "player-local-library.db",
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { instance = it }
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
                db.cachedTmdbMetadataDao().removeOrphans()
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
                parentDocumentId = it.parentDocumentId,
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
                parentDocumentId = media.parentDocumentId,
            )
        }
        val db = PlayerLibraryDatabase.get(context)
        db.withTransaction {
            db.mediaDao().upsert(records)
            db.mediaDao().removeStale(root, token)
            db.manualTmdbMatchDao().removeOrphans(root)
            db.cachedTmdbMetadataDao().removeOrphans()
        }
    }

    suspend fun forgetTree(context: Context, treeUri: Uri): Unit = withContext(Dispatchers.IO) {
        val db = PlayerLibraryDatabase.get(context)
        db.withTransaction {
            db.manualTmdbMatchDao().removeTree(treeUri.toString())
            db.mediaDao().removeTree(treeUri.toString())
            db.cachedTmdbMetadataDao().removeOrphans()
        }
    }
}

/** Only indexed videos can receive a validated match; every binding is atomic. */
internal object OfflineMetadataCache {
    suspend fun load(context: Context, tree: String): Map<String, CachedTmdbMetadata> =
        withContext(Dispatchers.IO) {
            PlayerLibraryDatabase.get(context).cachedTmdbMetadataDao().forTree(tree)
                .associate { it.documentId to it.metadata }
        }

    suspend fun bind(
        context: Context,
        tree: String,
        documentIds: List<String>,
        metadata: CachedTmdbMetadata,
        respectLookupSuppression: Boolean = false,
    ): Boolean = withContext(Dispatchers.IO) {
        if (documentIds.isEmpty() || documentIds.size > 10_000 || !valid(metadata)) return@withContext false
        val ids = documentIds.toSet()
        val db = PlayerLibraryDatabase.get(context)
        db.withTransaction {
            val indexedVideos = db.mediaDao().forTree(tree)
                .filter { it.mediaKind == LocalMediaKind.VIDEO.name }.map { it.documentId }.toSet()
            // A stale search response, subtitle or other tree must not create an orphan/partial match.
            if (!indexedVideos.containsAll(ids)) return@withTransaction false
            if (respectLookupSuppression && ids.chunked(500).any { db.manualTmdbMatchDao().hasLookupSuppression(tree, it) }) {
                return@withTransaction false
            }
            db.cachedTmdbMetadataDao().upsert(metadata)
            val now = System.currentTimeMillis()
            for (id in ids) {
                currentCoroutineContext().ensureActive()
                db.manualTmdbMatchDao().upsert(ManualTmdbMatch(tree, id, metadata.tmdbId, metadata.mediaType, now))
            }
            db.cachedTmdbMetadataDao().removeOrphans()
            true
        }
    }

    suspend fun suppressLookup(context: Context, tree: String, documentIds: List<String>): Unit =
        withContext(Dispatchers.IO) {
            if (documentIds.isEmpty() || documentIds.size > 10_000) return@withContext
            val db = PlayerLibraryDatabase.get(context)
            db.withTransaction {
                val ids = documentIds.distinct()
                val indexedVideos = db.mediaDao().forTree(tree)
                    .filter { it.mediaKind == LocalMediaKind.VIDEO.name }.map { it.documentId }.toSet()
                if (!indexedVideos.containsAll(ids)) return@withTransaction
                val timestamp = System.currentTimeMillis()
                for (id in ids) {
                    currentCoroutineContext().ensureActive()
                    // Preserve an explicit user dismissal across restarts without
                    // adding another Room table/migration. The sentinel never joins
                    // cached metadata and a later explicit bind replaces it.
                    db.manualTmdbMatchDao().upsert(ManualTmdbMatch(tree, id, 0, "NONE", timestamp))
                }
                db.cachedTmdbMetadataDao().removeOrphans()
            }
        }

    suspend fun isLookupSuppressed(context: Context, tree: String, documentIds: List<String>): Boolean =
        withContext(Dispatchers.IO) {
            if (documentIds.isEmpty()) return@withContext false
            val dao = PlayerLibraryDatabase.get(context).manualTmdbMatchDao()
            documentIds.distinct().chunked(500).any { dao.hasLookupSuppression(tree, it) }
        }

    private fun valid(metadata: CachedTmdbMetadata): Boolean =
        metadata.tmdbId > 0 && metadata.mediaType in setOf("MOVIE", "TV") &&
            metadata.title.isNotBlank() && metadata.title.codePointCount(0, metadata.title.length) <= 200 &&
            metadata.title.none { Character.isISOControl(it.code) } &&
            (metadata.year == null || metadata.year in 1000..9999) && metadata.overview.length <= 4_000 &&
            metadata.cachedAtEpochMs >= 0 && validImagePath(metadata.posterPath) && validImagePath(metadata.backdropPath)

    private fun validImagePath(path: String?): Boolean = path == null ||
        (path.length <= 200 && Regex("/[A-Za-z0-9_-]+\\.(?:jpg|png)").matches(path))
}


