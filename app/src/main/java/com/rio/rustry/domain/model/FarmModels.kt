package com.rio.rustry.domain.model

import java.util.Date

/**
 * Farm domain model for farm listing and management
 */
data class Farm(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val location: String = "",
    val size: Double = 0.0,
    val photoUrl: String = "",
    val ownershipDetails: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val needsSync: Boolean = true,
    val lastSyncedAt: Long? = null,
    val offlineChanges: Map<String, String> = emptyMap()
)

/**
 * Flock domain model for flock management
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
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val needsSync: Boolean = true,
    val lastSyncedAt: Long? = null,
    val offlineChanges: Map<String, String> = emptyMap()
)

/**
 * Health Record domain model for health tracking
 */
data class HealthRecord(
    val id: String = "",
    val flockId: String = "",
    val type: String = "", // VACCINATION, TREATMENT, CHECKUP
    val date: Long = System.currentTimeMillis(),
    val details: String = "",
    val photoUrl: String = "",
    val veterinarianId: String = "",
    val medicationUsed: String = "",
    val nextDueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val needsSync: Boolean = true,
    val lastSyncedAt: Long? = null,
    val offlineChanges: Map<String, String> = emptyMap()
)

/**
 * Sale domain model for sales tracking
 */
data class Sale(
    val id: String = "",
    val farmId: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val buyerContact: String = "",
    val date: Long = System.currentTimeMillis(),
    val amount: Double = 0.0,
    val items: String = "",
    val quantity: Int = 0,
    val paymentMethod: String = "", // CASH, UPI, BANK_TRANSFER
    val paymentStatus: String = "PENDING", // PENDING, COMPLETED, FAILED
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val needsSync: Boolean = true,
    val lastSyncedAt: Long? = null,
    val offlineChanges: Map<String, String> = emptyMap()
)

/**
 * Inventory Item domain model for inventory management
 */
data class InventoryItem(
    val id: String = "",
    val farmId: String = "",
    val type: String = "", // FEED, MEDICINE, EQUIPMENT, SUPPLIES
    val name: String = "",
    val quantity: Int = 0,
    val unit: String = "", // KG, LITERS, PIECES
    val restockThreshold: Int = 0,
    val supplier: String = "",
    val lastRestockDate: Long? = null,
    val expiryDate: Long? = null,
    val cost: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val needsSync: Boolean = true,
    val lastSyncedAt: Long? = null,
    val offlineChanges: Map<String, String> = emptyMap()
)

/**
 * Change Log domain model for tracking all changes
 */
data class ChangeLog(
    val id: String = "",
    val farmId: String = "",
    val userId: String = "",
    val entityType: String = "", // FARM, FLOCK, HEALTH_RECORD, SALE, INVENTORY
    val entityId: String = "",
    val action: String = "", // CREATE, UPDATE, DELETE
    val oldData: String = "",
    val newData: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String = "",
    val appVersion: String = "",
    val isVerified: Boolean = false,
    val verifiedBy: String = "",
    val verificationTimestamp: Long? = null,
    val needsSync: Boolean = true,
    val lastSyncedAt: Long? = null
)

/**
 * Notification domain model for FCM notifications
 */
data class FarmNotification(
    val id: String = "",
    val farmId: String = "",
    val userId: String = "",
    val type: String = "", // VACCINATION_DUE, RESTOCK_ALERT, HEALTH_REMINDER
    val title: String = "",
    val message: String = "",
    val data: Map<String, String> = emptyMap(),
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val scheduledFor: Long? = null,
    val priority: String = "NORMAL", // LOW, NORMAL, HIGH, URGENT
    val actionRequired: Boolean = false,
    val actionUrl: String = "",
    val expiresAt: Long? = null,
    val needsSync: Boolean = true,
    val lastSyncedAt: Long? = null
)

/**
 * Photo Validation Result from Qodo Gen AI
 */
data class PhotoValidationResult(
    val isValid: Boolean = false,
    val confidence: Double = 0.0,
    val detectedObjects: List<String> = emptyList(),
    val suggestedTags: List<String> = emptyList(),
    val qualityScore: Double = 0.0,
    val issues: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Farm Statistics for dashboard
 */
data class FarmStatistics(
    val farmId: String = "",
    val totalFlocks: Int = 0,
    val totalBirds: Int = 0,
    val healthyBirds: Int = 0,
    val sickBirds: Int = 0,
    val totalSales: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val lowStockItems: Int = 0,
    val overdueVaccinations: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)