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
import androidx.room.withTransaction
import java.util.UUID

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
}

@Database(entities = [IndexedMedia::class], version = 1, exportSchema = false)
internal abstract class PlayerLibraryDatabase : RoomDatabase() {
    abstract fun mediaDao(): IndexedMediaDao

    companion object {
        @Volatile private var instance: PlayerLibraryDatabase? = null

        fun get(context: Context): PlayerLibraryDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlayerLibraryDatabase::class.java,
                    "player-local-library.db",
                ).build().also { instance = it }
            }
    }
}

/**
 * Only a fully completed scan is eligible for reconciliation. Failed/cancelled
 * SAF enumeration never reaches saveSuccessfulScan, so existing rows remain.
 * The upsert + missing-file deletion are atomic and read-only to the media source.
 */
internal object OfflineLibraryIndex {
    suspend fun load(context: Context, treeUri: Uri): List<DiscoveredLocalMedia> {
        return PlayerLibraryDatabase.get(context).mediaDao()
            .forTree(treeUri.toString())
            .map {
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
    ) {
        val token = UUID.randomUUID().toString()
        val root = treeUri.toString()
        val records = discovered.map { media ->
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
        }
    }

    suspend fun forgetTree(context: Context, treeUri: Uri) {
        PlayerLibraryDatabase.get(context).mediaDao().removeTree(treeUri.toString())
    }
}
