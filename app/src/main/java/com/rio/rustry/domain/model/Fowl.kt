package com.rio.rustry.domain.model

/**
 * Core fowl/bird model for traceability system
 */
data class Fowl(
    val fowlId: String = "",
    val ownerUserId: String = "",
    val name: String = "",
    val breed: String = "",
    val gender: String = "", // Male, Female
    val color: String = "",
    val dateOfBirth: Long? = null,
    val placeOfBirth: String = "",
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val status: FowlStatus = FowlStatus.ACTIVE,
    val healthStatus: HealthStatus = HealthStatus.HEALTHY,
    val bloodline: String = "",
    val specialTraits: List<String> = emptyList(),
    val achievements: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val videoUrls: List<String> = emptyList(),
    val microchipId: String = "",
    val ringNumber: String = "",
    val parentMaleId: String = "",
    val parentFemaleId: String = "",
    val childrenIds: List<String> = emptyList(),
    val generation: Int = 0,
    val isBreeder: Boolean = false,
    val breedingValue: Double = 0.0,
    val lastHealthCheckDate: Long? = null,
    val vaccinationRecords: List<VaccinationRecord> = emptyList(),
    val healthCertificates: List<String> = emptyList(),
    val pedigreeDocuments: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deathDate: Long? = null,
    val deathCause: String = "",
    val notes: String = ""
)

/**
 * Fowl status enumeration
 */
enum class FowlStatus {
    ACTIVE,         // Currently owned and alive
    SOLD,           // Sold to another owner
    DECEASED,       // No longer alive
    MISSING,        // Lost or missing
    BREEDING,       // Currently in breeding program
    QUARANTINE,     // Under health quarantine
    RETIRED         // Retired from breeding
}

/**
 * Health status enumeration
 */
enum class HealthStatus {
    HEALTHY,        // Good health condition
    SICK,           // Currently ill
    RECOVERING,     // Recovering from illness
    INJURED,        // Has injury
    UNDER_TREATMENT,// Receiving medical treatment
    QUARANTINED,    // Isolated due to health concerns
    UNKNOWN         // Health status not determined
}

/**
 * Breeding record for fowl
 */
data class BreedingRecord(
    val id: String = "",
    val fowlId: String = "",
    val mateId: String = "",
    val breedingDate: Long = System.currentTimeMillis(),
    val expectedHatchDate: Long? = null,
    val actualHatchDate: Long? = null,
    val eggsLaid: Int = 0,
    val eggsHatched: Int = 0,
    val chickIds: List<String> = emptyList(),
    val breedingMethod: String = "", // "natural", "artificial"
    val success: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Performance metrics for fowl
 */
data class PerformanceMetrics(
    val fowlId: String = "",
    val eggProductionRate: Double = 0.0, // eggs per week
    val averageEggWeight: Double = 0.0,
    val fertilityRate: Double = 0.0,
    val hatchabilityRate: Double = 0.0,
    val growthRate: Double = 0.0,
    val feedConversionRatio: Double = 0.0,
    val mortalityRate: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)