package com.rio.rustry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rio.rustry.data.model.Fowl

@Entity(tableName = "fowls")
data class FowlEntity(
    @PrimaryKey
    val id: String,
    val ownerId: String,
    val ownerName: String,
    val breed: String,
    val dateOfBirth: Long, // Store as timestamp
    val price: Double,
    val description: String,
    val location: String,
    val imageUrls: String, // Store as comma-separated string
    val proofImageUrls: String, // Store as comma-separated string
    val parentIds: String, // Store as comma-separated string
    val isAvailable: Boolean,
    val isTraceable: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

// Extension functions for conversion
fun FowlEntity.toDomainModel(): Fowl {
    return Fowl(
        id = id,
        ownerId = ownerId,
        ownerName = ownerName,
        breed = breed,
        dateOfBirth = java.util.Date(dateOfBirth),
        price = price,
        description = description,
        location = location,
        imageUrls = if (imageUrls.isBlank()) emptyList() else imageUrls.split(","),
        proofImageUrls = if (proofImageUrls.isBlank()) emptyList() else proofImageUrls.split(","),
        parentIds = if (parentIds.isBlank()) emptyList() else parentIds.split(","),
        isAvailable = isAvailable,
        isTraceable = isTraceable,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Fowl.toEntity(): FowlEntity {
    return FowlEntity(
        id = id,
        ownerId = ownerId,
        ownerName = ownerName,
        breed = breed,
        dateOfBirth = dateOfBirth.time,
        price = price,
        description = description,
        location = location,
        imageUrls = imageUrls.joinToString(","),
        proofImageUrls = proofImageUrls.joinToString(","),
        parentIds = parentIds.joinToString(","),
        isAvailable = isAvailable,
        isTraceable = isTraceable,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}