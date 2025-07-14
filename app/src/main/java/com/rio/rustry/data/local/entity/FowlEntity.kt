package com.rio.rustry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.rio.rustry.data.model.Fowl
import java.util.*

@Entity(tableName = "fowls")
@TypeConverters(Converters::class)
data class FowlEntity(
    @PrimaryKey
    val id: String,
    val ownerId: String,
    val ownerName: String,
    val breed: String,
    val dateOfBirth: Date,
    val isTraceable: Boolean,
    val parentIds: List<String>,
    val imageUrls: List<String>,
    val proofImageUrls: List<String>,
    val description: String,
    val location: String,
    val price: Double,
    val isAvailable: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val isCached: Boolean = true,
    val lastSyncTime: Long = System.currentTimeMillis()
) {
    fun toFowl(): Fowl {
        return Fowl(
            id = id,
            ownerId = ownerId,
            ownerName = ownerName,
            breed = breed,
            dateOfBirth = dateOfBirth,
            isTraceable = isTraceable,
            parentIds = parentIds,
            imageUrls = imageUrls,
            proofImageUrls = proofImageUrls,
            description = description,
            location = location,
            price = price,
            isAvailable = isAvailable,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromFowl(fowl: Fowl): FowlEntity {
            return FowlEntity(
                id = fowl.id,
                ownerId = fowl.ownerId,
                ownerName = fowl.ownerName,
                breed = fowl.breed,
                dateOfBirth = fowl.dateOfBirth,
                isTraceable = fowl.isTraceable,
                parentIds = fowl.parentIds,
                imageUrls = fowl.imageUrls,
                proofImageUrls = fowl.proofImageUrls,
                description = fowl.description,
                location = fowl.location,
                price = fowl.price,
                isAvailable = fowl.isAvailable,
                createdAt = fowl.createdAt,
                updatedAt = fowl.updatedAt
            )
        }
    }
}

@Entity(tableName = "user_favorites")
data class FavoriteEntity(
    @PrimaryKey
    val id: String, // userId_fowlId
    val userId: String,
    val fowlId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pending_changes")
data class PendingChangeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fowlId: String,
    val operation: String, // INSERT, UPDATE, DELETE
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(timestamp: Long): Date {
        return Date(timestamp)
    }
}