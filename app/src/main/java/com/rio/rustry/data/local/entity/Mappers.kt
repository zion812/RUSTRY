package com.rio.rustry.data.local.entity

import com.rio.rustry.data.model.Fowl

fun EnhancedFowlEntity.toDomainModel(): Fowl {
    return Fowl(
        id = id,
        ownerId = ownerId,
        name = name,
        breed = breed,
        age = age,
        price = price,
        description = description,
        imageUrls = imageUrls,
        isForSale = isForSale,
        location = location,
        latitude = latitude,
        longitude = longitude,
        healthStatus = healthStatus,
        vaccinationRecords = vaccinationRecords,
        parentMaleId = parentMaleId,
        parentFemaleId = parentFemaleId,
        birthDate = birthDate,
        weight = weight,
        color = color,
        gender = gender,
        isVerified = isVerified,
        verificationDocuments = verificationDocuments,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Fowl.toEntity(): EnhancedFowlEntity {
    return EnhancedFowlEntity(
        id = id,
        ownerId = ownerId,
        name = name,
        breed = breed,
        age = age,
        price = price,
        description = description,
        imageUrls = imageUrls,
        isForSale = isForSale,
        location = location,
        latitude = latitude,
        longitude = longitude,
        healthStatus = healthStatus,
        vaccinationRecords = vaccinationRecords,
        parentMaleId = parentMaleId,
        parentFemaleId = parentFemaleId,
        birthDate = birthDate,
        weight = weight,
        color = color,
        gender = gender,
        isVerified = isVerified,
        verificationDocuments = verificationDocuments,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = false,
        needsSync = true,
        lastSyncedAt = null,
        offlineChanges = emptyMap()
    )
}
