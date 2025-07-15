// generated/phase3/app/src/main/java/com/rio/rustry/data/local/dao/VaccinationEventDao.kt

package com.rio.rustry.data.local.dao

import androidx.room.*
import com.rio.rustry.data.model.VaccinationEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccinationEventDao {
    
    @Query("SELECT * FROM vaccination_events WHERE fowlId = :fowlId ORDER BY scheduledDate ASC")
    suspend fun getVaccinationEventsByFowl(fowlId: String): List<VaccinationEvent>
    
    @Query("SELECT * FROM vaccination_events WHERE fowlId = :fowlId ORDER BY scheduledDate ASC")
    fun getVaccinationEventsByFowlFlow(fowlId: String): Flow<List<VaccinationEvent>>
    
    @Query("SELECT * FROM vaccination_events WHERE id = :eventId")
    suspend fun getVaccinationEventById(eventId: String): VaccinationEvent?
    
    @Query("SELECT * FROM vaccination_events WHERE status = :status ORDER BY scheduledDate ASC")
    suspend fun getVaccinationEventsByStatus(status: String): List<VaccinationEvent>
    
    @Query("SELECT * FROM vaccination_events WHERE scheduledDate <= :date AND status = 'PENDING'")
    suspend fun getOverdueVaccinations(date: Long): List<VaccinationEvent>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccinationEvent(event: VaccinationEvent)
    
    @Update
    suspend fun updateVaccinationEvent(event: VaccinationEvent)
    
    @Delete
    suspend fun deleteVaccinationEvent(event: VaccinationEvent)
    
    @Query("DELETE FROM vaccination_events WHERE id = :eventId")
    suspend fun deleteVaccinationEventById(eventId: String)
    
    @Query("UPDATE vaccination_events SET status = 'COMPLETED', completedDate = :completedDate WHERE id = :eventId")
    suspend fun markVaccinationComplete(eventId: String, completedDate: Long)
}