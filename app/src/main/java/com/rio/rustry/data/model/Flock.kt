package com.rio.rustry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Flock data model for managing groups of fowls
 */
@Entity(tableName = "flocks")
@Serializable
data class Flock(
    @PrimaryKey
    val id: String = "",
    val farmId: String = "",
    val name: String = "",
    val breed: String = "",
    val quantity: Int = 0,
    val ageMonths: Int = 0,
    val healthStatus: String = "HEALTHY", // HEALTHY, SICK, QUARANTINE, DECEASED
    val location: String = "",
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val fowlIds: List<String> = emptyList(),
    
    // Management details
    val feedType: String = "",
    val feedingSchedule: String = "",
    val vaccinationStatus: String = "UP_TO_DATE", // UP_TO_DATE, OVERDUE, PARTIAL
    val lastVaccinationDate: Long? = null,
    val nextVaccinationDate: Long? = null,
    
    // Economic data
    val purchasePrice: Double = 0.0,
    val currentValue: Double = 0.0,
    val feedCost: Double = 0.0,
    val medicalCost: Double = 0.0,
    val totalInvestment: Double = 0.0,
    
    // Performance metrics
    val averageWeight: Double = 0.0,
    val mortalityRate: Double = 0.0,
    val productionRate: Double = 0.0, // For egg-laying breeds
    val growthRate: Double = 0.0,
    
    // Breeding information
    val isBreeding: Boolean = false,
    val breedingPairs: Int = 0,
    val expectedOffspring: Int = 0,
    val breedingCycle: String = "", // MATING, INCUBATION, HATCHING, REARING
    
    // Environmental conditions
    val temperature: Double = 0.0,
    val humidity: Double = 0.0,
    val lightingHours: Int = 0,
    val ventilationLevel: String = "GOOD", // POOR, FAIR, GOOD, EXCELLENT
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastHealthCheckDate: Long? = null,
    
    // Status and tracking
    val isActive: Boolean = true,
    val notes: String = "",
    val tags: List<String> = emptyList(),
    
    // Sync status
    val isSynced: Boolean = true,
    val needsSync: Boolean = false
)