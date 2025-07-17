package com.rio.rustry.domain.repository

import com.rio.rustry.data.model.HealthRecord
import com.rio.rustry.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for health operations
 */
interface HealthRepository {
    
    /**
     * Get all health records for a fowl
     */
    fun getHealthRecords(fowlId: String): Flow<Result<List<HealthRecord>>>
    
    /**
     * Get health history for a fowl
     */
    suspend fun getHealthHistory(fowlId: String): Result<List<HealthRecord>>
    
    /**
     * Get health record by ID
     */
    suspend fun getHealthRecordById(recordId: String): Result<HealthRecord?>
    
    /**
     * Add a new health record
     */
    suspend fun addHealthRecord(record: HealthRecord): Result<Unit>
    
    /**
     * Update health record
     */
    suspend fun updateHealthRecord(record: HealthRecord): Result<Unit>
    
    /**
     * Delete health record
     */
    suspend fun deleteHealthRecord(recordId: String): Result<Unit>
    
    /**
     * Get health summary for a fowl
     */
    suspend fun getHealthSummary(fowlId: String): Result<HealthSummary>
    
    /**
     * Get vaccination schedule
     */
    suspend fun getVaccinationSchedule(fowlId: String): Result<List<VaccinationSchedule>>
    
    /**
     * Schedule vaccination
     */
    suspend fun scheduleVaccination(schedule: VaccinationSchedule): Result<String>
    
    /**
     * Get health alerts
     */
    fun getHealthAlerts(userId: String): Flow<Result<List<HealthAlert>>>
    
    /**
     * Get health statistics
     */
    suspend fun getHealthStatistics(userId: String): Result<HealthStatistics>
}

/**
 * Health summary data class
 */
data class HealthSummary(
    val fowlId: String,
    val overallHealth: String, // EXCELLENT, GOOD, FAIR, POOR
    val lastCheckup: Long = 0L,
    val vaccinationsUpToDate: Boolean = false,
    val activeConditions: List<String> = emptyList(),
    val recommendedActions: List<String> = emptyList()
)

/**
 * Vaccination schedule data class
 */
data class VaccinationSchedule(
    val id: String,
    val fowlId: String,
    val vaccineName: String,
    val scheduledDate: Long,
    val isCompleted: Boolean = false,
    val completedDate: Long? = null,
    val notes: String = ""
)

/**
 * Health alert data class
 */
data class HealthAlert(
    val id: String,
    val fowlId: String,
    val alertType: String, // VACCINATION_DUE, CHECKUP_OVERDUE, HEALTH_CONCERN
    val message: String,
    val severity: String, // LOW, MEDIUM, HIGH, CRITICAL
    val createdAt: Long,
    val isRead: Boolean = false
)

/**
 * Health statistics data class
 */
data class HealthStatistics(
    val totalFowls: Int = 0,
    val healthyFowls: Int = 0,
    val fowlsNeedingAttention: Int = 0,
    val overdueVaccinations: Int = 0,
    val overdueCheckups: Int = 0,
    val commonConditions: Map<String, Int> = emptyMap()
)