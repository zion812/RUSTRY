// generated/phase3/app/src/main/java/com/rio/rustry/data/local/dao/BreedingEventDao.kt

package com.rio.rustry.data.local.dao

import androidx.room.*
import com.rio.rustry.data.model.BreedingEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface BreedingEventDao {
    
    @Query("SELECT * FROM breeding_events ORDER BY breedingDate DESC")
    fun getAllBreedingEvents(): Flow<List<BreedingEvent>>
    
    @Query("SELECT * FROM breeding_events WHERE id = :eventId")
    suspend fun getBreedingEventById(eventId: String): BreedingEvent?
    
    @Query("SELECT * FROM breeding_events WHERE sireId = :fowlId OR damId = :fowlId ORDER BY breedingDate DESC")
    suspend fun getBreedingEventsByParent(fowlId: String): List<BreedingEvent>
    
    @Query("SELECT * FROM breeding_events WHERE breedingDate BETWEEN :startDate AND :endDate ORDER BY breedingDate DESC")
    suspend fun getBreedingEventsByDateRange(startDate: Long, endDate: Long): List<BreedingEvent>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreedingEvent(event: BreedingEvent)
    
    @Update
    suspend fun updateBreedingEvent(event: BreedingEvent)
    
    @Delete
    suspend fun deleteBreedingEvent(event: BreedingEvent)
    
    @Query("DELETE FROM breeding_events WHERE id = :eventId")
    suspend fun deleteBreedingEventById(eventId: String)
}