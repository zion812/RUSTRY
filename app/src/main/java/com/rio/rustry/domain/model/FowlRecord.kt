package com.rio.rustry.domain.model

/**
 * Individual record entry for fowl tracking and traceability
 */
data class FowlRecord(
    val recordId: String = "",
    val fowlId: String = "",
    val recordType: RecordType = RecordType.GENERAL,
    val title: String = "",
    val description: String = "",
    val recordDate: Long = System.currentTimeMillis(),
    val recordedBy: String = "", // User ID who recorded this
    val location: String = "",
    val weight: Double? = null,
    val height: Double? = null,
    val temperature: Double? = null,
    val mediaUrls: List<String> = emptyList(),
    val documentUrls: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val isVerified: Boolean = false,
    val verifiedBy: String = "",
    val verificationDate: Long? = null,
    val severity: RecordSeverity = RecordSeverity.LOW,
    val followUpRequired: Boolean = false,
    val followUpDate: Long? = null,
    val relatedRecordIds: List<String> = emptyList(),
    val cost: Double = 0.0,
    val currency: String = "USD",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Types of records that can be tracked
 */
enum class RecordType {
    GENERAL,            // General observation or note
    HEALTH,             // Health-related record
    VACCINATION,        // Vaccination record
    TREATMENT,          // Medical treatment
    FEEDING,            // Feeding record
    BREEDING,           // Breeding activity
    EGG_PRODUCTION,     // Egg laying record
    WEIGHT_CHECK,       // Weight measurement
    GROWTH,             // Growth milestone
    BEHAVIOR,           // Behavioral observation
    INJURY,             // Injury record
    DEATH,              // Death record
    TRANSFER,           // Ownership transfer
    SALE,               // Sale transaction
    PURCHASE,           // Purchase transaction
    BIRTH,              // Birth/hatching record
    QUARANTINE,         // Quarantine record
    CERTIFICATION,      // Certification or award
    COMPETITION,        // Competition participation
    MAINTENANCE,        // Facility/equipment maintenance
    ENVIRONMENTAL,      // Environmental conditions
    NUTRITION,          // Nutritional information
    GENETIC_TEST,       // Genetic testing results
    PERFORMANCE,        // Performance metrics
    INSPECTION,         // Official inspection
    AUDIT              // Audit record
}

/**
 * Severity levels for records
 */
enum class RecordSeverity {
    LOW,        // Routine, informational
    MEDIUM,     // Important, requires attention
    HIGH,       // Urgent, immediate action needed
    CRITICAL    // Emergency, critical situation
}

/**
 * Health record specific data
 */
data class HealthRecord(
    val recordId: String = "",
    val fowlId: String = "",
    val healthIssue: String = "",
    val symptoms: List<String> = emptyList(),
    val diagnosis: String = "",
    val treatment: String = "",
    val medication: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val veterinarianId: String = "",
    val veterinarianName: String = "",
    val clinicName: String = "",
    val prescriptionUrl: String = "",
    val followUpDate: Long? = null,
    val recoveryDate: Long? = null,
    val outcome: String = "",
    val cost: Double = 0.0,
    val insuranceCovered: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Feeding record specific data
 */
data class FeedingRecord(
    val recordId: String = "",
    val fowlId: String = "",
    val feedType: String = "",
    val feedBrand: String = "",
    val quantity: Double = 0.0,
    val unit: String = "kg",
    val feedingTime: Long = System.currentTimeMillis(),
    val nutritionalInfo: Map<String, Double> = emptyMap(),
    val supplements: List<String> = emptyList(),
    val waterConsumption: Double = 0.0,
    val feedCost: Double = 0.0,
    val feedQuality: String = "",
    val feedSource: String = "",
    val batchNumber: String = "",
    val expiryDate: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Production record for egg-laying fowl
 */
data class ProductionRecord(
    val recordId: String = "",
    val fowlId: String = "",
    val productionDate: Long = System.currentTimeMillis(),
    val eggsLaid: Int = 0,
    val averageEggWeight: Double = 0.0,
    val eggQuality: String = "",
    val eggSize: String = "",
    val shellThickness: Double = 0.0,
    val yolkColor: String = "",
    val abnormalEggs: Int = 0,
    val brokenEggs: Int = 0,
    val environmentalConditions: Map<String, String> = emptyMap(),
    val feedType: String = "",
    val stressFactors: List<String> = emptyList(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)