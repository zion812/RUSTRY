package com.rio.rustry.data.local.dao

import androidx.room.*
import com.rio.rustry.data.local.entity.FowlEntity
import com.rio.rustry.data.local.entity.FavoriteEntity
import com.rio.rustry.data.local.entity.PendingChangeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FowlDao {

    // Fowl operations
    @Query("SELECT * FROM fowls WHERE isAvailable = 1 ORDER BY createdAt DESC")
    fun getMarketplaceFowls(): Flow<List<FowlEntity>>

    @Query("SELECT * FROM fowls WHERE id = :fowlId")
    fun getFowlById(fowlId: String): Flow<FowlEntity?>

    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getFowlsByOwner(ownerId: String): Flow<List<FowlEntity>>

    @Query("SELECT * FROM fowls WHERE breed LIKE '%' || :breed || '%' AND isAvailable = 1")
    fun getFowlsByBreed(breed: String): Flow<List<FowlEntity>>

    @Query("SELECT * FROM fowls WHERE location LIKE '%' || :location || '%' AND isAvailable = 1")
    fun getFowlsByLocation(location: String): Flow<List<FowlEntity>>

    @Query("SELECT * FROM fowls WHERE price BETWEEN :minPrice AND :maxPrice AND isAvailable = 1")
    fun getFowlsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<FowlEntity>>

    @Query("SELECT * FROM fowls WHERE isTraceable = 1 AND isAvailable = 1")
    fun getTraceableFowls(): Flow<List<FowlEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowl(fowl: FowlEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowls(fowls: List<FowlEntity>)

    @Update
    suspend fun updateFowl(fowl: FowlEntity)

    @Delete
    suspend fun deleteFowl(fowl: FowlEntity)

    @Query("DELETE FROM fowls WHERE id = :fowlId")
    suspend fun deleteFowlById(fowlId: String)

    @Query("DELETE FROM fowls WHERE isAvailable = 1")
    suspend fun clearMarketplaceFowls()

    @Query("DELETE FROM fowls")
    suspend fun clearAllFowls()

    // Search operations
    @Query("""
        SELECT * FROM fowls 
        WHERE (breed LIKE '%' || :query || '%' 
               OR description LIKE '%' || :query || '%' 
               OR location LIKE '%' || :query || '%' 
               OR ownerName LIKE '%' || :query || '%')
        AND isAvailable = 1
        ORDER BY createdAt DESC
    """)
    fun searchFowls(query: String): Flow<List<FowlEntity>>

    // Favorites operations
    @Query("""
        SELECT f.* FROM fowls f 
        INNER JOIN user_favorites uf ON f.id = uf.fowlId 
        WHERE uf.userId = :userId 
        ORDER BY uf.createdAt DESC
    """)
    fun getFavoriteFowls(userId: String): Flow<List<FowlEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("INSERT INTO user_favorites (id, userId, fowlId) VALUES (:userId || '_' || :fowlId, :userId, :fowlId)")
    suspend fun addToFavorites(fowlId: String, userId: String)

    @Query("DELETE FROM user_favorites WHERE userId = :userId AND fowlId = :fowlId")
    suspend fun removeFromFavorites(fowlId: String, userId: String)

    @Query("SELECT COUNT(*) > 0 FROM user_favorites WHERE userId = :userId AND fowlId = :fowlId")
    suspend fun isFavorite(fowlId: String, userId: String): Boolean

    @Query("SELECT fowlId FROM user_favorites WHERE userId = :userId")
    fun getFavoriteIds(userId: String): Flow<List<String>>

    // Pending changes operations
    @Insert
    suspend fun insertPendingChange(change: PendingChangeEntity)

    @Query("SELECT * FROM pending_changes WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getPendingChanges(): Flow<List<PendingChangeEntity>>

    @Query("UPDATE pending_changes SET isSynced = 1 WHERE id = :changeId")
    suspend fun markChangeSynced(changeId: Long)

    @Query("DELETE FROM pending_changes WHERE isSynced = 1")
    suspend fun clearSyncedChanges()

    // Cache management
    @Query("SELECT COUNT(*) FROM fowls")
    suspend fun getFowlCount(): Int

    @Query("SELECT MAX(lastSyncTime) FROM fowls")
    suspend fun getLastSyncTime(): Long?

    @Query("DELETE FROM fowls WHERE lastSyncTime < :cutoffTime")
    suspend fun clearOldCache(cutoffTime: Long)

    @Query("UPDATE fowls SET lastSyncTime = :syncTime WHERE id = :fowlId")
    suspend fun updateSyncTime(fowlId: String, syncTime: Long)

    // Analytics and statistics
    @Query("SELECT COUNT(*) FROM fowls WHERE isAvailable = 1")
    suspend fun getAvailableFowlCount(): Int

    @Query("SELECT COUNT(*) FROM fowls WHERE isTraceable = 1 AND isAvailable = 1")
    suspend fun getTraceableFowlCount(): Int

    @Query("SELECT AVG(price) FROM fowls WHERE isAvailable = 1")
    suspend fun getAveragePrice(): Double?

    @Query("SELECT breed, COUNT(*) as count FROM fowls WHERE isAvailable = 1 GROUP BY breed ORDER BY count DESC")
    suspend fun getBreedStatistics(): List<BreedStatistic>

    @Query("SELECT location, COUNT(*) as count FROM fowls WHERE isAvailable = 1 GROUP BY location ORDER BY count DESC")
    suspend fun getLocationStatistics(): List<LocationStatistic>

    // Cleanup operations
    @Query("DELETE FROM fowls")
    suspend fun clearAll()

    @Transaction
    suspend fun refreshMarketplaceFowls(fowls: List<FowlEntity>) {
        clearMarketplaceFowls()
        insertFowls(fowls)
    }
}

// Data classes for statistics
data class BreedStatistic(
    val breed: String,
    val count: Int
)

data class LocationStatistic(
    val location: String,
    val count: Int
)