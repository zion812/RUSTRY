package com.rio.rustry.data.model

/**
 * Flock domain model
 */
data class Flock(
    val id: String = "",
    val farmId: String = "",
    val breed: String = "",
    val ageMonths: Int = 0,
    val quantity: Int = 0,
    val maleCount: Int = 0,
    val femaleCount: Int = 0,
    val notes: String = "",
    val photoUrls: List<String> = emptyList(),
    val acquisitionDate: Long = System.currentTimeMillis(),
    val healthStatus: String = "HEALTHY",
    val productionType: String = "MEAT",
    val housingType: String = "COOP",
    val feedType: String = "COMMERCIAL",
    val isForSale: Boolean = false,
    val salePrice: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)