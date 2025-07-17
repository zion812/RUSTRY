package com.rio.rustry.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Enhanced Fowl Entity with offline support and traceability
 */
@Entity(tableName = "fowls")
data class EnhancedFowlEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "owner_id")
    val ownerId: String,
    
    @ColumnInfo(name = "name")
    val name: String = "",
    
    @ColumnInfo(name = "breed")
    val breed: String,
    
    @ColumnInfo(name = "age")
    val age: Int,
    
    @ColumnInfo(name = "price")
    val price: Double,
    
    @ColumnInfo(name = "description")
    val description: String = "",
    
    @ColumnInfo(name = "image_urls")
    val imageUrls: List<String> = emptyList(),
    
    @ColumnInfo(name = "is_for_sale")
    val isForSale: Boolean = false,
    
    @ColumnInfo(name = "location")
    val location: String = "",
    
    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double? = null,
    
    @ColumnInfo(name = "health_status")
    val healthStatus: String = "HEALTHY",
    
    @ColumnInfo(name = "vaccination_records")
    val vaccinationRecords: List<String> = emptyList(),
    
    @ColumnInfo(name = "parent_male_id")
    val parentMaleId: String? = null,
    
    @ColumnInfo(name = "parent_female_id")
    val parentFemaleId: String? = null,
    
    @ColumnInfo(name = "birth_date")
    val birthDate: Long? = null,
    
    @ColumnInfo(name = "weight")
    val weight: Double? = null,
    
    @ColumnInfo(name = "color")
    val color: String = "",
    
    @ColumnInfo(name = "gender")
    val gender: String = "UNKNOWN", // MALE, FEMALE, UNKNOWN
    
    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean = false,
    
    @ColumnInfo(name = "verification_documents")
    val verificationDocuments: List<String> = emptyList(),
    
    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList(),
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    
    @ColumnInfo(name = "needs_sync")
    val needsSync: Boolean = true,
    
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null,
    
    @ColumnInfo(name = "offline_changes")
    val offlineChanges: Map<String, String> = emptyMap()
)

/**
 * Enhanced User Entity with profile and preferences
 */
@Entity(tableName = "users")
data class EnhancedUserEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
    
    @ColumnInfo(name = "email")
    val email: String = "",
    
    @ColumnInfo(name = "profile_image_url")
    val profileImageUrl: String = "",
    
    @ColumnInfo(name = "location")
    val location: String = "",
    
    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double? = null,
    
    @ColumnInfo(name = "user_type")
    val userType: String = "FARMER", // FARMER, BUYER, ENTHUSIAST
    
    @ColumnInfo(name = "farm_name")
    val farmName: String = "",
    
    @ColumnInfo(name = "farm_size")
    val farmSize: String = "",
    
    @ColumnInfo(name = "experience_years")
    val experienceYears: Int = 0,
    
    @ColumnInfo(name = "specialization")
    val specialization: List<String> = emptyList(),
    
    @ColumnInfo(name = "rating")
    val rating: Double = 0.0,
    
    @ColumnInfo(name = "total_reviews")
    val totalReviews: Int = 0,
    
    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean = false,
    
    @ColumnInfo(name = "verification_level")
    val verificationLevel: String = "NONE", // NONE, PHONE, KYC, PREMIUM
    
    @ColumnInfo(name = "kyc_documents")
    val kycDocuments: List<String> = emptyList(),
    
    @ColumnInfo(name = "preferred_language")
    val preferredLanguage: String = "en",
    
    @ColumnInfo(name = "notification_preferences")
    val notificationPreferences: Map<String, String> = emptyMap(),
    
    @ColumnInfo(name = "is_online")
    val isOnline: Boolean = false,
    
    @ColumnInfo(name = "last_seen")
    val lastSeen: Long? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "needs_sync")
    val needsSync: Boolean = true,
    
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long? = null
)

/**
 * Sync Queue Entity for managing offline operations
 */
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "entity_type")
    val entityType: String, // FOWL, USER, TRANSACTION, etc.
    
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    
    @ColumnInfo(name = "action")
    val action: String, // CREATE, UPDATE, DELETE
    
    @ColumnInfo(name = "data")
    val data: String, // JSON data
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    
    @ColumnInfo(name = "status")
    val status: String = "PENDING" // PENDING, IN_PROGRESS, COMPLETED, FAILED
)

/**
 * Offline Action Entity for tracking user actions while offline
 */
@Entity(tableName = "offline_actions")
data class OfflineActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "action_type")
    val actionType: String, // LIKE, FAVORITE, SHARE, COMMENT, etc.
    
    @ColumnInfo(name = "target_id")
    val targetId: String, // ID of the target entity
    
    @ColumnInfo(name = "action_data")
    val actionData: String, // JSON data for the action
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
)

/**
 * Cached Image Entity for offline image support
 */
@Entity(tableName = "cached_images")
data class CachedImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "image_url")
    val imageUrl: String,
    
    @ColumnInfo(name = "local_path")
    val localPath: String,
    
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    
    @ColumnInfo(name = "entity_type")
    val entityType: String, // FOWL, USER, etc.
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long = 0
)

/**
 * Notification Entity for local notification storage
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "notification_id")
    val notificationId: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "body")
    val body: String,
    
    @ColumnInfo(name = "type")
    val type: String, // PRICE_ALERT, ORDER_UPDATE, GENERAL, etc.
    
    @ColumnInfo(name = "data")
    val data: String? = null, // JSON data
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
)

/**
 * Transaction Entity for payment and order tracking
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "transaction_id")
    val transactionId: String,
    
    @ColumnInfo(name = "fowl_id")
    val fowlId: String,
    
    @ColumnInfo(name = "buyer_id")
    val buyerId: String,
    
    @ColumnInfo(name = "seller_id")
    val sellerId: String,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "status")
    val status: String, // PENDING, COMPLETED, FAILED, REFUNDED
    
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String, // UPI, CARD, COD
    
    @ColumnInfo(name = "payment_gateway_response")
    val paymentGatewayResponse: String? = null,
    
    @ColumnInfo(name = "delivery_address")
    val deliveryAddress: String = "",
    
    @ColumnInfo(name = "delivery_status")
    val deliveryStatus: String = "PENDING", // PENDING, SHIPPED, DELIVERED
    
    @ColumnInfo(name = "notes")
    val notes: String = "",
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
)

/**
 * Traceability Entity for fowl history tracking
 */
@Entity(tableName = "traceability")
data class TraceabilityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "fowl_id")
    val fowlId: String,
    
    @ColumnInfo(name = "event_type")
    val eventType: String, // BIRTH, VACCINATION, TRANSFER, SALE, etc.
    
    @ColumnInfo(name = "event_data")
    val eventData: String, // JSON data for the event
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "verified_by")
    val verifiedBy: String? = null, // User ID who verified this event
    
    @ColumnInfo(name = "verification_proof")
    val verificationProof: List<String> = emptyList(), // Image URLs or document IDs
    
    @ColumnInfo(name = "location")
    val location: String = "",
    
    @ColumnInfo(name = "notes")
    val notes: String = "",
    
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
)

/**
 * User Favorites Entity for tracking favorite fowls
 */
@Entity(tableName = "user_favorites")
data class UserFavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "fowl_id")
    val fowlId: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
