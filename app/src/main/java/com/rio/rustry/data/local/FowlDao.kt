package com.rio.rustry.data.local

import androidx.room.*
import androidx.paging.PagingSource
import com.rio.rustry.data.local.entity.FowlEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FowlDao {
    
    // Basic CRUD operations
    @Query("SELECT * FROM fowls WHERE id = :id")
    suspend fun getFowlById(id: String): FowlEntity?
    
    @Query("SELECT * FROM fowls WHERE id = :id")
    fun getFowlByIdFlow(id: String): Flow<FowlEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowl(fowl: FowlEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowls(fowls: List<FowlEntity>)
    
    @Update
    suspend fun updateFowl(fowl: FowlEntity)
    
    @Delete
    suspend fun deleteFowl(fowl: FowlEntity)
    
    @Query("DELETE FROM fowls WHERE id = :id")
    suspend fun deleteFowlById(id: String)
    
    // Query operations for marketplace
    @Query("SELECT * FROM fowls WHERE isAvailable = 1 ORDER BY createdAt DESC")
    fun getAvailableFowls(): PagingSource<Int, FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE isAvailable = 1 ORDER BY createdAt DESC")
    fun getAvailableFowlsFlow(): Flow<List<FowlEntity>>
    
    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getFowlsByOwner(ownerId: String): Flow<List<FowlEntity>>
    
    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getFowlsByOwnerPaging(ownerId: String): PagingSource<Int, FowlEntity>
    
    // Search and filter operations
    @Query("""
        SELECT * FROM fowls 
        WHERE isAvailable = 1 
        AND (breed LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun searchFowls(query: String): PagingSource<Int, FowlEntity>
    
    @Query("""
        SELECT * FROM fowls 
        WHERE isAvailable = 1 
        AND breed = :breed 
        ORDER BY createdAt DESC
    """)
    fun getFowlsByBreed(breed: String): PagingSource<Int, FowlEntity>
    
    @Query("""
        SELECT * FROM fowls 
        WHERE isAvailable = 1 
        AND location LIKE '%' || :location || '%'
        ORDER BY createdAt DESC
    """)
    fun getFowlsByLocation(location: String): PagingSource<Int, FowlEntity>
    
    @Query("""
        SELECT * FROM fowls 
        WHERE isAvailable = 1 
        AND price BETWEEN :minPrice AND :maxPrice
        ORDER BY price ASC
    """)
    fun getFowlsByPriceRange(minPrice: Double, maxPrice: Double): PagingSource<Int, FowlEntity>
    
    // Analytics and reporting
    @Query("SELECT COUNT(*) FROM fowls WHERE ownerId = :ownerId")
    suspend fun getFowlCountByOwner(ownerId: String): Int
    
    @Query("SELECT COUNT(*) FROM fowls WHERE isAvailable = 1")
    suspend fun getAvailableFowlCount(): Int
    
    @Query("SELECT AVG(price) FROM fowls WHERE isAvailable = 1 AND breed = :breed")
    suspend fun getAveragePriceByBreed(breed: String): Double?
    
    @Query("SELECT DISTINCT breed FROM fowls WHERE isAvailable = 1")
    suspend fun getAvailableBreeds(): List<String>
    
    @Query("SELECT DISTINCT location FROM fowls WHERE isAvailable = 1")
    suspend fun getAvailableLocations(): List<String>
    
    // Sync operations
    @Query("SELECT * FROM fowls WHERE lastSyncedAt < :timestamp")
    suspend fun getFowlsNeedingSync(timestamp: Long): List<FowlEntity>
    
    @Query("UPDATE fowls SET lastSyncedAt = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: String, timestamp: Long)
    
    @Query("DELETE FROM fowls WHERE lastSyncedAt < :cutoffTime AND ownerId != :currentUserId")
    suspend fun cleanupOldCachedFowls(cutoffTime: Long, currentUserId: String)
    
    // Traceability operations
    @Query("SELECT * FROM fowls WHERE isTraceable = 1 AND parentIds LIKE '%' || :parentId || '%'")
    suspend fun getFowlsByParent(parentId: String): List<FowlEntity>
    
    @Query("SELECT * FROM fowls WHERE id IN (:parentIds)")
    suspend fun getFowlParents(parentIds: List<String>): List<FowlEntity>
    
    // Batch operations for performance
    @Transaction
    suspend fun replaceFowls(fowls: List<FowlEntity>) {
        // Clear and replace - useful for full sync
        clearAllFowls()
        insertFowls(fowls)
    }
    
    @Query("DELETE FROM fowls")
    suspend fun clearAllFowls()
    
    @Query("DELETE FROM fowls WHERE ownerId = :ownerId")
    suspend fun clearFowlsByOwner(ownerId: String)
}