package com.rio.rustry.domain.model

/**
 * Breeder status for fowl
 */
enum class BreederStatus {
    ACTIVE,         // Currently breeding
    RETIRED,        // No longer breeding
    CANDIDATE,      // Potential breeder
    PROVEN,         // Has successfully bred
    UNPROVEN,       // Has not yet bred successfully
    QUARANTINED,    // Temporarily isolated
    SOLD,           // No longer owned
    DECEASED        // Dead
}

/**
 * Lifecycle event for fowl tracking
 */
data class LifecycleEvent(
    val id: String = "",
    val fowlId: String = "",
    val eventType: String = "",
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val recordedBy: String = "",
    val location: String = "",
    val metadata: Map<String, String> = emptyMap(),
    val severity: String = "LOW", // LOW, MEDIUM, HIGH, CRITICAL
    val isVerified: Boolean = false,
    val verifiedBy: String = "",
    val verificationDate: Long? = null
)

/**
 * Vaccination event for fowl
 */
data class VaccinationEvent(
    val id: String = "",
    val fowlId: String = "",
    val vaccineName: String = "",
    val vaccineType: String = "",
    val scheduledDate: Long = System.currentTimeMillis(),
    val completedDate: Long? = null,
    val status: VaccinationStatus = VaccinationStatus.SCHEDULED,
    val veterinarianId: String = "",
    val veterinarianName: String = "",
    val batchNumber: String = "",
    val manufacturer: String = "",
    val dosage: String = "",
    val administrationMethod: String = "", // injection, oral, spray
    val siteOfAdministration: String = "",
    val nextDueDate: Long? = null,
    val reactions: List<String> = emptyList(),
    val certificateUrl: String = "",
    val cost: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Vaccination status
 */
enum class VaccinationStatus {
    SCHEDULED,      // Planned but not yet given
    COMPLETED,      // Successfully administered
    OVERDUE,        // Past due date
    CANCELLED,      // Cancelled for some reason
    POSTPONED,      // Delayed to a later date
    ADVERSE_REACTION // Had negative reaction
}

/**
 * Breeding pair information
 */
data class BreedingPair(
    val id: String = "",
    val maleId: String = "",
    val femaleId: String = "",
    val maleName: String = "",
    val femaleName: String = "",
    val pairingDate: Long = System.currentTimeMillis(),
    val status: BreedingPairStatus = BreedingPairStatus.ACTIVE,
    val expectedHatchDate: Long? = null,
    val actualHatchDate: Long? = null,
    val eggsLaid: Int = 0,
    val eggsHatched: Int = 0,
    val chicks: List<String> = emptyList(), // fowl IDs of offspring
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Breeding pair status
 */
enum class BreedingPairStatus {
    ACTIVE,         // Currently paired
    INACTIVE,       // Temporarily separated
    RETIRED,        // No longer breeding
    SUCCESSFUL,     // Successfully produced offspring
    UNSUCCESSFUL,   // Failed to produce offspring
    SEPARATED       // Permanently separated
}

/**
 * Breeding program information
 */
data class BreedingProgram(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val targetBreed: String = "",
    val goals: List<String> = emptyList(),
    val activePairs: List<String> = emptyList(), // BreedingPair IDs
    val totalOffspring: Int = 0,
    val successRate: Double = 0.0,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val status: String = "ACTIVE", // ACTIVE, PAUSED, COMPLETED, CANCELLED
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)