package com.rio.rustry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Breeding record data model for tracking breeding activities
 */
@Entity(tableName = "breeding_records")
@Serializable
data class BreedingRecord(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val maleParentId: String = "",
    val femaleParentId: String = "",
    
    // Breeding details
    val breedingDate: Long = 0L,
    val expectedHatchDate: Long = 0L,
    val actualHatchDate: Long? = null,
    val breedingMethod: String = "", // NATURAL, ARTIFICIAL_INSEMINATION
    val breedingLocation: String = "",
    
    // Results
    val isSuccessful: Boolean = false,
    val eggCount: Int = 0,
    val fertilizedEggCount: Int = 0,
    val hatchedCount: Int = 0,
    val survivedCount: Int = 0,
    val offspringIds: List<String> = emptyList(),
    
    // Health and conditions
    val parentHealthStatus: String = "", // EXCELLENT, GOOD, FAIR, POOR
    val breedingConditions: String = "", // OPTIMAL, GOOD, FAIR, POOR
    val temperature: Double = 0.0,
    val humidity: Double = 0.0,
    val weatherConditions: String = "",
    
    // Genetics and traits
    val expectedTraits: List<String> = emptyList(),
    val actualTraits: List<String> = emptyList(),
    val geneticMarkers: Map<String, String> = emptyMap(),
    val inbreedingCoefficient: Double = 0.0,
    
    // Documentation
    val notes: String = "",
    val images: List<String> = emptyList(),
    val videos: List<String> = emptyList(),
    val documents: List<String> = emptyList(),
    
    // Veterinary information
    val veterinarianId: String = "",
    val veterinarianNotes: String = "",
    val medicationsUsed: List<String> = emptyList(),
    val vaccinationsGiven: List<String> = emptyList(),
    
    // Economic data
    val cost: Double = 0.0,
    val expectedRevenue: Double = 0.0,
    val actualRevenue: Double = 0.0,
    val profitLoss: Double = 0.0,
    
    // Quality metrics
    val qualityScore: Double = 0.0,
    val performanceRating: String = "", // EXCELLENT, GOOD, AVERAGE, POOR
    val recommendations: List<String> = emptyList(),
    
    // Timestamps
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val completedAt: Long? = null,
    
    // Status and tracking
    val status: String = "", // PLANNED, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
    val isVerified: Boolean = false,
    val verifiedBy: String = "",
    val verificationDate: Long? = null,
    
    // Sync status
    val isSynced: Boolean = true,
    val needsSync: Boolean = false
)