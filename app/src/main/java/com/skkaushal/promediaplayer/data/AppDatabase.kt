package com.skkaushal.promediaplayer.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "history", indices = [Index(value=["uri"], unique=true)])
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String, val title: String, val position: Long,
    val duration: Long, val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites", indices = [Index(value=["uri"], unique=true)])
data class FavoriteEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val uri: String, val title: String)

@Entity(tableName = "playlists")
data class PlaylistEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String)

@Entity(tableName = "playlist_items")
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long, val uri: String, val title: String, val position: Int
)

@Dao
interface MediaDao {
    @Query("SELECT * FROM history ORDER BY updatedAt DESC") fun history(): Flow<List<HistoryEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveHistory(item: HistoryEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun favorite(item: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE uri=:uri") suspend fun unfavorite(uri: String)
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE uri=:uri)") suspend fun isFavorite(uri: String): Boolean
    @Insert suspend fun playlist(item: PlaylistEntity): Long
    @Insert suspend fun playlistItem(item: PlaylistItemEntity)
}

@Database(entities=[HistoryEntity::class,FavoriteEntity::class,PlaylistEntity::class,PlaylistItemEntity::class],version=1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun dao(): MediaDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "media.db").build().also { INSTANCE=it }
        }
    }
}
