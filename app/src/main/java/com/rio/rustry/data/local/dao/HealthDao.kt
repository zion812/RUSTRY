package com.rio.rustry.data.local.dao

import androidx.room.*
import com.rio.rustry.data.model.HealthRecord
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for health record operations
 */
@Dao
interface HealthDao {
    
    @Query("SELECT * FROM health_records WHERE id = :recordId")
    suspend fun getHealthRecordById(recordId: String): HealthRecord?
    
    @Query("SELECT * FROM health_records WHERE fowlId = :fowlId ORDER BY recordDate DESC")
    fun getHealthRecords(fowlId: String): Flow<List<HealthRecord>>
    
    @Query("SELECT * FROM health_records WHERE userId = :userId ORDER BY recordDate DESC")
    fun getHealthRecordsByUser(userId: String): Flow<List<HealthRecord>>
    
    @Query("SELECT * FROM health_records WHERE recordType = :recordType ORDER BY recordDate DESC")
    fun getHealthRecordsByType(recordType: String): Flow<List<HealthRecord>>
    
    @Query("SELECT * FROM health_records WHERE fowlId = :fowlId AND recordType = 'VACCINATION' ORDER BY recordDate DESC")
    fun getVaccinationRecords(fowlId: String): Flow<List<HealthRecord>>
    
    @Query("SELECT * FROM health_records WHERE isEmergency = 1 ORDER BY recordDate DESC")
    fun getEmergencyRecords(): Flow<List<HealthRecord>>
    
    @Query("SELECT * FROM health_records WHERE followUpRequired = 1 AND followUpDate <= :currentDate ORDER BY followUpDate ASC")
    fun getRecordsNeedingFollowUp(currentDate: Long): Flow<List<HealthRecord>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(record: HealthRecord)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecords(records: List<HealthRecord>)
    
    @Update
    suspend fun updateHealthRecord(record: HealthRecord)
    
    @Query("DELETE FROM health_records WHERE id = :recordId")
    suspend fun deleteHealthRecord(recordId: String)
    
    @Query("DELETE FROM health_records WHERE fowlId = :fowlId")
    suspend fun deleteHealthRecordsByFowl(fowlId: String)
    
    @Query("DELETE FROM health_records")
    suspend fun deleteAllHealthRecords()
    
    @Query("SELECT * FROM health_records WHERE isSynced = 0")
    suspend fun getUnsyncedHealthRecords(): List<HealthRecord>
    
    @Query("SELECT COUNT(*) FROM health_records WHERE isSynced = 0")
    suspend fun getUnsyncedHealthRecordsCount(): Int
    
    @Query("SELECT COUNT(*) FROM health_records WHERE fowlId = :fowlId")
    suspend fun getHealthRecordCount(fowlId: String): Int
    
    @Query("SELECT * FROM health_records WHERE fowlId = :fowlId ORDER BY recordDate DESC LIMIT 1")
    suspend fun getLatestHealthRecord(fowlId: String): HealthRecord?
    
    @Query("SELECT COUNT(*) FROM health_records WHERE userId = :userId AND overallHealth = 'POOR' OR overallHealth = 'CRITICAL'")
    suspend fun getFowlsNeedingAttentionCount(userId: String): Int
    
    @Query("SELECT * FROM health_records WHERE recordDate BETWEEN :startDate AND :endDate ORDER BY recordDate DESC")
    fun getHealthRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<HealthRecord>>
}