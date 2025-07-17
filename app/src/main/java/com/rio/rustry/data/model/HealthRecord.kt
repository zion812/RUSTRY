package com.rio.rustry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Health record data model for tracking fowl health
 */
@Entity(tableName = "health_records")
@Serializable
data class HealthRecord(
    @PrimaryKey
    val id: String = "",
    val fowlId: String = "",
    val userId: String = "",
    val veterinarianId: String = "",
    
    // Record details
    val recordType: String = "", // CHECKUP, VACCINATION, TREATMENT, EMERGENCY, DEATH
    val recordDate: Long = 0L,
    val nextCheckupDate: Long? = null,
    
    // Health assessment
    val overallHealth: String = "", // EXCELLENT, GOOD, FAIR, POOR, CRITICAL
    val weight: Double = 0.0,
    val temperature: Double = 0.0,
    val heartRate: Int = 0,
    val respiratoryRate: Int = 0,
    
    // Physical examination
    val eyesCondition: String = "",
    val beakCondition: String = "",
    val combCondition: String = "",
    val feathersCondition: String = "",
    val legsCondition: String = "",
    val clawsCondition: String = "",
    
    // Symptoms and conditions
    val symptoms: List<String> = emptyList(),
    val diagnosis: String = "",
    val conditions: List<String> = emptyList(),
    val severity: String = "", // MILD, MODERATE, SEVERE, CRITICAL
    
    // Treatments and medications
    val treatments: List<Treatment> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val dosageInstructions: String = "",
    val treatmentDuration: Int = 0, // days
    
    // Vaccinations
    val vaccinations: List<Vaccination> = emptyList(),
    val nextVaccinationDate: Long? = null,
    val vaccinationSchedule: List<String> = emptyList(),
    
    // Laboratory tests
    val labTests: List<LabTest> = emptyList(),
    val testResults: Map<String, String> = emptyMap(),
    val abnormalFindings: List<String> = emptyList(),
    
    // Behavioral observations
    val behaviorChanges: List<String> = emptyList(),
    val appetiteLevel: String = "", // NORMAL, INCREASED, DECREASED, NONE
    val activityLevel: String = "", // NORMAL, INCREASED, DECREASED, LETHARGIC
    val socialBehavior: String = "", // NORMAL, AGGRESSIVE, WITHDRAWN, ISOLATED
    
    // Environmental factors
    val housingConditions: String = "",
    val feedQuality: String = "",
    val waterQuality: String = "",
    val stressFactors: List<String> = emptyList(),
    
    // Documentation
    val notes: String = "",
    val images: List<String> = emptyList(),
    val videos: List<String> = emptyList(),
    val documents: List<String> = emptyList(),
    
    // Follow-up
    val followUpRequired: Boolean = false,
    val followUpDate: Long? = null,
    val followUpInstructions: String = "",
    val recoveryExpected: Boolean = true,
    val recoveryTimeEstimate: Int = 0, // days
    
    // Costs
    val consultationFee: Double = 0.0,
    val medicationCost: Double = 0.0,
    val testCost: Double = 0.0,
    val totalCost: Double = 0.0,
    
    // Timestamps
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val completedAt: Long? = null,
    
    // Status
    val status: String = "", // ACTIVE, COMPLETED, CANCELLED
    val isEmergency: Boolean = false,
    val isContagious: Boolean = false,
    val quarantineRequired: Boolean = false,
    
    // Sync status
    val isSynced: Boolean = true,
    val needsSync: Boolean = false
)

/**
 * Treatment data class
 */
@Serializable
data class Treatment(
    val name: String,
    val type: String, // MEDICATION, SURGERY, THERAPY, ISOLATION
    val startDate: Long,
    val endDate: Long? = null,
    val frequency: String,
    val dosage: String,
    val instructions: String,
    val isCompleted: Boolean = false
)

/**
 * Medication data class
 */
@Serializable
data class Medication(
    val name: String,
    val type: String, // ANTIBIOTIC, ANTIVIRAL, ANTIFUNGAL, VITAMIN, SUPPLEMENT
    val dosage: String,
    val frequency: String,
    val duration: Int, // days
    val route: String, // ORAL, INJECTION, TOPICAL
    val startDate: Long,
    val endDate: Long,
    val sideEffects: List<String> = emptyList(),
    val isCompleted: Boolean = false
)

/**
 * Vaccination data class
 */
@Serializable
data class Vaccination(
    val name: String,
    val type: String, // LIVE, KILLED, RECOMBINANT
    val manufacturer: String,
    val batchNumber: String,
    val expiryDate: Long,
    val administrationDate: Long,
    val administrationRoute: String, // SUBCUTANEOUS, INTRAMUSCULAR, ORAL, NASAL
    val dose: String,
    val boosterRequired: Boolean = false,
    val boosterDate: Long? = null,
    val reactions: List<String> = emptyList()
)

/**
 * Lab test data class
 */
@Serializable
data class LabTest(
    val name: String,
    val type: String, // BLOOD, FECAL, SWAB, BIOPSY
    val sampleCollectionDate: Long,
    val testDate: Long,
    val results: Map<String, String> = emptyMap(),
    val normalRanges: Map<String, String> = emptyMap(),
    val interpretation: String,
    val isAbnormal: Boolean = false,
    val labName: String,
    val reportUrl: String = ""
)