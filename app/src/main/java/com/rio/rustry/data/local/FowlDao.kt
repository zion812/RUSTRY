package com.rio.rustry.data.local

import androidx.paging.PagingSource
import androidx.room.*
import com.rio.rustry.data.local.entity.EnhancedFowlEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Fowl operations
 */
@Dao
interface FowlDao {
    
    @Query("SELECT * FROM fowls WHERE id = :id AND is_deleted = 0")
    suspend fun getFowlById(id: String): EnhancedFowlEntity?
    
    @Query("SELECT * FROM fowls WHERE id = :id AND is_deleted = 0")
    fun getFowlByIdFlow(id: String): Flow<EnhancedFowlEntity?>
    
    @Query("SELECT * FROM fowls WHERE is_for_sale = 1 AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getMarketplaceFowls(): Flow<List<EnhancedFowlEntity>>
    
    @Query("SELECT * FROM fowls WHERE is_for_sale = 1 AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getAvailableFowls(): PagingSource<Int, EnhancedFowlEntity>
    
    @Query("SELECT * FROM fowls WHERE is_for_sale = 1 AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getAvailableFowlsFlow(): Flow<List<EnhancedFowlEntity>>
    
    @Query("SELECT * FROM fowls WHERE owner_id = :ownerId AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFowlsByOwner(ownerId: String): Flow<List<EnhancedFowlEntity>>
    
    @Query("SELECT * FROM fowls WHERE owner_id = :ownerId AND is_deleted = 0 ORDER BY updated_at DESC")
    fun getFowlsByOwnerPaging(ownerId: String): PagingSource<Int, EnhancedFowlEntity>
    
    @Query("SELECT * FROM fowls WHERE (breed LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') AND is_deleted = 0")
    fun searchFowls(query: String): PagingSource<Int, EnhancedFowlEntity>
    
    @Query("SELECT * FROM fowls WHERE breed = :breed AND is_deleted = 0")
    fun getFowlsByBreed(breed: String): PagingSource<Int, EnhancedFowlEntity>
    
    @Query("SELECT * FROM fowls WHERE location LIKE '%' || :location || '%' AND is_deleted = 0")
    fun getFowlsByLocation(location: String): PagingSource<Int, EnhancedFowlEntity>
    
    @Query("SELECT * FROM fowls WHERE price BETWEEN :minPrice AND :maxPrice AND is_deleted = 0")
    fun getFowlsByPriceRange(minPrice: Double, maxPrice: Double): PagingSource<Int, EnhancedFowlEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowl(fowl: EnhancedFowlEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowls(fowls: List<EnhancedFowlEntity>)
    
    @Update
    suspend fun updateFowl(fowl: EnhancedFowlEntity)
    
    @Delete
    suspend fun deleteFowl(fowl: EnhancedFowlEntity)
    
    @Query("DELETE FROM fowls WHERE id = :id")
    suspend fun deleteFowlById(id: String)
    
    @Query("SELECT COUNT(*) FROM fowls WHERE owner_id = :ownerId AND is_deleted = 0")
    suspend fun getFowlCountByOwner(ownerId: String): Int
    
    @Query("SELECT COUNT(*) FROM fowls WHERE is_for_sale = 1 AND is_deleted = 0")
    suspend fun getAvailableFowlCount(): Int
    
    @Query("SELECT AVG(price) FROM fowls WHERE breed = :breed AND is_deleted = 0")
    suspend fun getAveragePriceByBreed(breed: String): Double?
    
    @Query("SELECT DISTINCT breed FROM fowls WHERE is_deleted = 0")
    suspend fun getAvailableBreeds(): List<String>
    
    @Query("SELECT DISTINCT location FROM fowls WHERE is_deleted = 0")
    suspend fun getAvailableLocations(): List<String>
    
    @Query("SELECT * FROM fowls WHERE parent_male_id = :parentId OR parent_female_id = :parentId AND is_deleted = 0")
    suspend fun getFowlsByParent(parentId: String): List<EnhancedFowlEntity>
    
    @Query("SELECT * FROM fowls WHERE id IN (:parentIds) AND is_deleted = 0")
    suspend fun getFowlParents(parentIds: List<String>): List<EnhancedFowlEntity>
    
    @Query("SELECT * FROM fowls WHERE needs_sync = 1 AND updated_at > :timestamp")
    fun getFowlsNeedingSync(timestamp: Long): Flow<List<EnhancedFowlEntity>>
    
    @Query("UPDATE fowls SET last_synced_at = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: String, timestamp: Long)
    
    @Query("DELETE FROM fowls WHERE updated_at < :cutoffTime AND owner_id != :currentUserId")
    suspend fun cleanupOldCachedFowls(cutoffTime: Long, currentUserId: String)
    
    @Transaction
    suspend fun replaceFowls(fowls: List<EnhancedFowlEntity>) {
        clearAllFowls()
        insertFowls(fowls)
    }
    
    @Query("DELETE FROM fowls")
    suspend fun clearAllFowls()
    
    @Query("DELETE FROM fowls WHERE owner_id = :ownerId")
    suspend fun clearFowlsByOwner(ownerId: String)
    
    @Query("DELETE FROM fowls WHERE is_for_sale = 1")
    suspend fun clearMarketplaceFowls()
    
    // Favorites functionality
    @Query("INSERT INTO user_favorites (fowl_id, user_id, created_at) VALUES (:fowlId, :userId, :timestamp)")
    suspend fun addToFavorites(fowlId: String, userId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM user_favorites WHERE fowl_id = :fowlId AND user_id = :userId")
    suspend fun removeFromFavorites(fowlId: String, userId: String)
    
    @Query("SELECT f.* FROM fowls f INNER JOIN user_favorites uf ON f.id = uf.fowl_id WHERE uf.user_id = :userId AND f.is_deleted = 0")
    fun getFavoriteFowls(userId: String): Flow<List<EnhancedFowlEntity>>
    
    // Pending changes for sync
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING'")
    fun getPendingChanges(): Flow<List<SyncChange>>
    
    @Query("UPDATE sync_queue SET status = 'SYNCED' WHERE id = :changeId")
    suspend fun markChangeSynced(changeId: Long)
}

// Helper data class for sync operations
data class SyncChange(
    val id: Long,
    val fowlId: String,
    val operation: String, // INSERT, UPDATE, DELETE
    val timestamp: Long
)