package com.rio.rustry.data.model

import java.util.Date

data class Fowl(
    val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val breed: String = "",
    val dateOfBirth: Date = Date(),
    val isTraceable: Boolean = false,
    val parentIds: List<String> = emptyList(), // Parent fowl IDs if traceable
    val imageUrls: List<String> = emptyList(),
    val proofImageUrls: List<String> = emptyList(), // Records/certificates
    val description: String = "",
    val location: String = "",
    val price: Double = 0.0,
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class FowlBreed(val displayName: String) {
    RHODE_ISLAND_RED("Rhode Island Red"),
    LEGHORN("Leghorn"),
    PLYMOUTH_ROCK("Plymouth Rock"),
    ORPINGTON("Orpington"),
    WYANDOTTE("Wyandotte"),
    SUSSEX("Sussex"),
    BRAHMA("Brahma"),
    COCHIN("Cochin"),
    SILKIE("Silkie"),
    BANTAM("Bantam"),
    OTHER("Other")
}