// generated/phase2/app/src/main/java/com/rio/rustry/data/local/dao/FowlDao.kt

package com.rio.rustry.data.local.dao

import androidx.room.*
import com.rio.rustry.data.model.Fowl

@Dao
interface FowlDao {
    
    @Query("""
        SELECT * FROM fowls 
        WHERE (:query = '' OR breed LIKE '%' || :query || '%')
        AND (:breeds IS NULL OR breed IN (:breeds))
        AND (:ageGroups IS NULL OR ageGroup IN (:ageGroups))
        AND price BETWEEN :minPrice AND :maxPrice
        ORDER BY createdAt DESC
        LIMIT 50
    """)
    suspend fun searchFowls(
        query: String,
        breeds: List<String>?,
        ageGroups: List<String>?,
        minPrice: Double,
        maxPrice: Double
    ): List<Fowl>
    
    @Query("SELECT * FROM fowls WHERE id = :fowlId")
    suspend fun getFowlById(fowlId: String): Fowl?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowl(fowl: Fowl)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowls(fowls: List<Fowl>)
    
    @Query("DELETE FROM fowls WHERE createdAt < :cutoffTime")
    suspend fun clearOldFowls(cutoffTime: Long = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) // 7 days
}