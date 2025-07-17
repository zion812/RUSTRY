package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.data.local.dao.HealthDao
import com.rio.rustry.data.model.HealthRecord
import com.rio.rustry.domain.model.Result
import com.rio.rustry.domain.repository.HealthAlert
import com.rio.rustry.domain.repository.HealthRepository
import com.rio.rustry.domain.repository.HealthStatistics
import com.rio.rustry.domain.repository.HealthSummary
import com.rio.rustry.domain.repository.VaccinationSchedule
import com.rio.rustry.utils.ErrorHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HealthRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val localHealthDao: HealthDao,
    private val ioDispatcher: CoroutineDispatcher
) : HealthRepository {
    
    companion object {
        private const val HEALTH_RECORDS_COLLECTION = "health_records"
        private const val VACCINATION_SCHEDULES_COLLECTION = "vaccination_schedules"
    }
    
    override fun getHealthRecords(fowlId: String): Flow<Result<List<HealthRecord>>> = flow {
        try {
            emit(Result.Loading)
            
            // Get from local cache first
            localHealthDao.getHealthRecords(fowlId).collect { localRecords ->
                if (localRecords.isNotEmpty()) {
                    emit(Result.Success(localRecords))
                }
            }
            
            // Fetch from Firestore
            val snapshot = firestore.collection(HEALTH_RECORDS_COLLECTION)
                .whereEqualTo("fowlId", fowlId)
                .orderBy("recordDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val records = snapshot.toObjects(HealthRecord::class.java)
            
            // Update local cache
            withContext(ioDispatcher) {
                localHealthDao.insertHealthRecords(records)
            }
            
            emit(Result.Success(records))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(ioDispatcher)
    
    override suspend fun getHealthRecordById(recordId: String): Result<HealthRecord?> = withContext(ioDispatcher) {
        try {
            // Check local cache first
            val localRecord = localHealthDao.getHealthRecordById(recordId)
            if (localRecord != null) {
                return@withContext Result.Success(localRecord)
            }
            
            // Fetch from Firestore
            val document = firestore.collection(HEALTH_RECORDS_COLLECTION)
                .document(recordId)
                .get()
                .await()
            
            val record = document.toObject(HealthRecord::class.java)
            
            // Update local cache
            record?.let {
                localHealthDao.insertHealthRecord(it)
            }
            
            Result.Success(record)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun addHealthRecord(record: HealthRecord): Result<Unit> = withContext(ioDispatcher) {
        try {
            // Save to Firestore
            firestore.collection(HEALTH_RECORDS_COLLECTION)
                .document(record.id)
                .set(record)
                .await()
            
            // Save to local cache
            localHealthDao.insertHealthRecord(record)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun updateHealthRecord(record: HealthRecord): Result<Unit> = withContext(ioDispatcher) {
        try {
            val updatedRecord = record.copy(updatedAt = System.currentTimeMillis())
            
            // Update in Firestore
            firestore.collection(HEALTH_RECORDS_COLLECTION)
                .document(record.id)
                .set(updatedRecord)
                .await()
            
            // Update local cache
            localHealthDao.updateHealthRecord(updatedRecord)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun deleteHealthRecord(recordId: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            // Delete from Firestore
            firestore.collection(HEALTH_RECORDS_COLLECTION)
                .document(recordId)
                .delete()
                .await()
            
            // Delete from local cache
            localHealthDao.deleteHealthRecord(recordId)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun getHealthSummary(fowlId: String): Result<HealthSummary> = withContext(ioDispatcher) {
        try {
            val latestRecord = localHealthDao.getLatestHealthRecord(fowlId)
            
            val summary = HealthSummary(
                fowlId = fowlId,
                overallHealth = latestRecord?.overallHealth ?: "UNKNOWN",
                lastCheckup = latestRecord?.recordDate ?: 0L,
                vaccinationsUpToDate = true, // Would be calculated
                activeConditions = latestRecord?.conditions ?: emptyList(),
                recommendedActions = emptyList() // Would be calculated
            )
            
            Result.Success(summary)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun getVaccinationSchedule(fowlId: String): Result<List<VaccinationSchedule>> = withContext(ioDispatcher) {
        try {
            // Mock implementation - would fetch from Firestore
            val schedules = listOf(
                VaccinationSchedule(
                    id = "vacc_1",
                    fowlId = fowlId,
                    vaccineName = "Newcastle Disease",
                    scheduledDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000), // 7 days from now
                    isCompleted = false
                ),
                VaccinationSchedule(
                    id = "vacc_2",
                    fowlId = fowlId,
                    vaccineName = "Infectious Bronchitis",
                    scheduledDate = System.currentTimeMillis() + (14 * 24 * 60 * 60 * 1000), // 14 days from now
                    isCompleted = false
                )
            )
            
            Result.Success(schedules)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun getHealthHistory(fowlId: String): Result<List<HealthRecord>> = withContext(ioDispatcher) {
        try {
            val records = localHealthDao.getHealthRecords(fowlId).first()
            Result.Success(records)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun scheduleVaccination(schedule: VaccinationSchedule): Result<String> = withContext(ioDispatcher) {
        try {
            // Save to Firestore
            firestore.collection(VACCINATION_SCHEDULES_COLLECTION)
                .document(schedule.id)
                .set(schedule)
                .await()
            
            Result.Success(schedule.id)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override fun getHealthAlerts(userId: String): Flow<Result<List<HealthAlert>>> = flow {
        try {
            emit(Result.Loading)
            
            // Mock implementation - would fetch from Firestore
            val alerts = listOf(
                HealthAlert(
                    id = "alert_1",
                    fowlId = "fowl_1",
                    alertType = "VACCINATION_DUE",
                    message = "Newcastle Disease vaccination due in 2 days",
                    severity = "MEDIUM",
                    createdAt = System.currentTimeMillis(),
                    isRead = false
                ),
                HealthAlert(
                    id = "alert_2",
                    fowlId = "fowl_2",
                    alertType = "HEALTH_CONCERN",
                    message = "Abnormal temperature detected",
                    severity = "HIGH",
                    createdAt = System.currentTimeMillis() - (2 * 60 * 60 * 1000), // 2 hours ago
                    isRead = false
                )
            )
            
            emit(Result.Success(alerts))
        } catch (e: Exception) {
            emit(Result.Error(ErrorHandler.handleError(e)))
        }
    }.flowOn(ioDispatcher)
    
    override suspend fun getHealthStatistics(userId: String): Result<HealthStatistics> = withContext(ioDispatcher) {
        try {
            val totalFowls = 10 // Would be calculated from actual data
            val healthyFowls = 8
            val fowlsNeedingAttention = localHealthDao.getFowlsNeedingAttentionCount(userId)
            val overdueVaccinations = 2 // Would be calculated
            val overdueCheckups = 1 // Would be calculated
            
            val statistics = HealthStatistics(
                totalFowls = totalFowls,
                healthyFowls = healthyFowls,
                fowlsNeedingAttention = fowlsNeedingAttention,
                overdueVaccinations = overdueVaccinations,
                overdueCheckups = overdueCheckups,
                commonConditions = mapOf(
                    "Respiratory Issues" to 3,
                    "Digestive Problems" to 2,
                    "Parasites" to 1
                )
            )
            
            Result.Success(statistics)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
}