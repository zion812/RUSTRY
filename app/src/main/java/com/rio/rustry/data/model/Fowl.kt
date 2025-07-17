package com.rio.rustry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

/**
 * Fowl domain model
 */
@Entity(tableName = "fowls")
data class Fowl(
    @PrimaryKey
    val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val name: String = "",
    val breed: String = "",
    val age: Int = 0,
    val ageMonths: Int = 0, // Age in months for more precise tracking
    val price: Double = 0.0,
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val isForSale: Boolean = false,
    val location: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val healthStatus: String = "HEALTHY",
    val vaccinationRecords: List<String> = emptyList(),
    val parentMaleId: String? = null,
    val parentFemaleId: String? = null,
    val birthDate: Long? = null,
    val weight: Double? = null,
    val color: String = "",
    val gender: String = "UNKNOWN",
    val isVerified: Boolean = false,
    val verificationDocuments: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isTraceable: Boolean = false,
    // Additional fields for compatibility
    val dateOfBirth: Date = Date(),
    val parentIds: List<String> = emptyList(),
    val proofImageUrls: List<String> = emptyList(),
    val isAvailable: Boolean = true,
    val boostExpiry: Long? = null,
    
    // Sync status for offline support
    val isSynced: Boolean = true,
    val needsSync: Boolean = false
)