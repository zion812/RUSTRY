package com.rio.rustry.data.local.dao

import androidx.room.*
import com.rio.rustry.data.model.BreedingRecord
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for breeding record operations
 */
@Dao
interface BreedingDao {
    
    @Query("SELECT * FROM breeding_records WHERE id = :recordId")
    suspend fun getBreedingRecordById(recordId: String): BreedingRecord?
    
    @Query("SELECT * FROM breeding_records WHERE userId = :userId ORDER BY breedingDate DESC")
    fun getBreedingRecords(userId: String): Flow<List<BreedingRecord>>
    
    @Query("SELECT * FROM breeding_records WHERE maleParentId = :fowlId OR femaleParentId = :fowlId ORDER BY breedingDate DESC")
    fun getBreedingRecordsByParent(fowlId: String): Flow<List<BreedingRecord>>
    
    @Query("SELECT * FROM breeding_records WHERE status = :status ORDER BY breedingDate DESC")
    fun getBreedingRecordsByStatus(status: String): Flow<List<BreedingRecord>>
    
    @Query("SELECT * FROM breeding_records WHERE userId = :userId AND isSuccessful = 1 ORDER BY breedingDate DESC")
    fun getSuccessfulBreedings(userId: String): Flow<List<BreedingRecord>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreedingRecord(record: BreedingRecord)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreedingRecords(records: List<BreedingRecord>)
    
    @Update
    suspend fun updateBreedingRecord(record: BreedingRecord)
    
    @Query("DELETE FROM breeding_records WHERE id = :recordId")
    suspend fun deleteBreedingRecord(recordId: String)
    
    @Query("DELETE FROM breeding_records")
    suspend fun deleteAllBreedingRecords()
    
    @Query("SELECT * FROM breeding_records WHERE isSynced = 0")
    suspend fun getUnsyncedBreedingRecords(): List<BreedingRecord>
    
    @Query("SELECT COUNT(*) FROM breeding_records WHERE isSynced = 0")
    suspend fun getUnsyncedBreedingRecordsCount(): Int
    
    @Query("SELECT COUNT(*) FROM breeding_records WHERE userId = :userId")
    suspend fun getBreedingRecordCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM breeding_records WHERE userId = :userId AND isSuccessful = 1")
    suspend fun getSuccessfulBreedingCount(userId: String): Int
    
    @Query("SELECT AVG(CAST(hatchedCount AS REAL)) FROM breeding_records WHERE userId = :userId AND isSuccessful = 1")
    suspend fun getAverageOffspringCount(userId: String): Double?
    
    @Query("SELECT * FROM breeding_records WHERE breedingDate BETWEEN :startDate AND :endDate ORDER BY breedingDate DESC")
    fun getBreedingRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<BreedingRecord>>
}