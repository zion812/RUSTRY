 package com.rio.rustry.data.mapper

import com.rio.rustry.data.local.entity.EnhancedFowlEntity
import com.rio.rustry.domain.model.Fowl
import com.rio.rustry.domain.model.FowlStatus
import com.rio.rustry.domain.model.HealthStatus

/**
 * Mapper functions to convert between domain and data models
 */
object FowlMapper {
    
    /**
     * Convert domain Fowl to EnhancedFowlEntity
     */
    fun toEntity(fowl: Fowl): EnhancedFowlEntity {
        return EnhancedFowlEntity(
            id = fowl.fowlId,
            ownerId = fowl.ownerUserId,
            name = fowl.name,
            breed = fowl.breed,
            age = calculateAgeFromBirth(fowl.dateOfBirth),
            price = 0.0, // Default price, should be set separately
            description = fowl.notes,
            imageUrls = fowl.imageUrls,
            isForSale = fowl.status == FowlStatus.ACTIVE, // Simplified logic
            location = fowl.placeOfBirth,
            latitude = null,
            longitude = null,
            healthStatus = fowl.healthStatus.name,
            vaccinationRecords = fowl.vaccinationRecords.map { it.id },
            parentMaleId = fowl.parentMaleId.takeIf { it.isNotEmpty() },
            parentFemaleId = fowl.parentFemaleId.takeIf { it.isNotEmpty() },
            birthDate = fowl.dateOfBirth,
            weight = fowl.weight,
            color = fowl.color,
            gender = fowl.gender,
            isVerified = fowl.healthCertificates.isNotEmpty(),
            verificationDocuments = fowl.healthCertificates,
            tags = fowl.specialTraits,
            createdAt = fowl.createdAt,
            updatedAt = fowl.updatedAt,
            isDeleted = fowl.status == FowlStatus.DECEASED,
            needsSync = true,
            lastSyncedAt = null,
            offlineChanges = emptyMap()
        )
    }
    
    /**
     * Convert EnhancedFowlEntity to domain Fowl
     */
    fun toDomain(entity: EnhancedFowlEntity): Fowl {
        return Fowl(
            fowlId = entity.id,
            ownerUserId = entity.ownerId,
            name = entity.name,
            breed = entity.breed,
            gender = entity.gender,
            color = entity.color,
            dateOfBirth = entity.birthDate,
            placeOfBirth = entity.location,
            weight = entity.weight ?: 0.0,
            height = 0.0, // Not stored in entity
            status = mapToFowlStatus(entity),
            healthStatus = mapToHealthStatus(entity.healthStatus),
            bloodline = "", // Not stored in entity
            specialTraits = entity.tags,
            achievements = emptyList(), // Not stored in entity
            imageUrls = entity.imageUrls,
            videoUrls = emptyList(), // Not stored in entity
            microchipId = "", // Not stored in entity
            ringNumber = "", // Not stored in entity
            parentMaleId = entity.parentMaleId ?: "",
            parentFemaleId = entity.parentFemaleId ?: "",
            childrenIds = emptyList(), // Would need separate query
            generation = 0, // Would need calculation
            isBreeder = false, // Would need separate logic
            breedingValue = 0.0, // Not stored in entity
            lastHealthCheckDate = null, // Not stored in entity
            vaccinationRecords = emptyList(), // Would need separate query
            healthCertificates = entity.verificationDocuments,
            pedigreeDocuments = emptyList(), // Not stored in entity
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            deathDate = if (entity.isDeleted) entity.updatedAt else null,
            deathCause = "", // Not stored in entity
            notes = entity.description
        )
    }
    
    /**
     * Convert list of entities to domain models
     */
    fun toDomainList(entities: List<EnhancedFowlEntity>): List<Fowl> {
        return entities.map { toDomain(it) }
    }
    
    /**
     * Convert list of domain models to entities
     */
    fun toEntityList(fowls: List<Fowl>): List<EnhancedFowlEntity> {
        return fowls.map { toEntity(it) }
    }
    
    private fun calculateAgeFromBirth(birthDate: Long?): Int {
        if (birthDate == null) return 0
        val currentTime = System.currentTimeMillis()
        val ageInMillis = currentTime - birthDate
        val ageInDays = ageInMillis / (1000 * 60 * 60 * 24)
        return (ageInDays / 30).toInt() // Approximate age in months
    }
    
    private fun mapToFowlStatus(entity: EnhancedFowlEntity): FowlStatus {
        return when {
            entity.isDeleted -> FowlStatus.DECEASED
            entity.isForSale -> FowlStatus.ACTIVE
            else -> FowlStatus.ACTIVE
        }
    }
    
    private fun mapToHealthStatus(healthStatus: String): HealthStatus {
        return try {
            HealthStatus.valueOf(healthStatus)
        } catch (e: IllegalArgumentException) {
            HealthStatus.UNKNOWN
        }
    }
}