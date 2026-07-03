package com.example.data.database

import androidx.room.*
import com.example.data.model.FavoriteAnimeEntity
import com.example.data.model.ProviderConfigEntity
import com.example.data.model.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {
    @Query("SELECT * FROM favorites ORDER BY lastWatchedTimestamp DESC")
    fun getFavorites(): Flow<List<FavoriteAnimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteAnimeEntity)

    @Query("DELETE FROM favorites WHERE animeId = :animeId")
    suspend fun deleteFavorite(animeId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE animeId = :animeId LIMIT 1)")
    fun isFavorite(animeId: String): Flow<Boolean>

    @Query("SELECT * FROM favorites WHERE animeId = :animeId LIMIT 1")
    suspend fun getFavoriteById(animeId: String): FavoriteAnimeEntity?

    @Query("SELECT * FROM watch_history ORDER BY timestamp DESC")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE animeId = :animeId LIMIT 1")
    fun getWatchHistoryById(animeId: String): Flow<WatchHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE animeId = :animeId")
    suspend fun deleteWatchHistory(animeId: String)

    @Query("SELECT * FROM provider_config ORDER BY priority ASC")
    fun getProviderConfigs(): Flow<List<ProviderConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviderConfigs(configs: List<ProviderConfigEntity>)

    @Update
    suspend fun updateProviderConfig(config: ProviderConfigEntity)
}

@Database(
    entities = [
        FavoriteAnimeEntity::class,
        WatchHistoryEntity::class,
        ProviderConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
}
