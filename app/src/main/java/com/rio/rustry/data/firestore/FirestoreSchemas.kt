package com.rio.rustry.data.firestore

/**
 * Firestore Collection Schemas for RUSTRY Enhanced Marketplace
 * 
 * This file documents the Firestore database structure and provides
 * data classes for type-safe operations.
 */

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

/**
 * Fowl Collection Schema
 * Collection: "fowls"
 * 
 * Optimized for marketplace queries with proper indexing
 */
data class FowlDocument(
    @DocumentId
    val id: String = "",
    
    // Owner information
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerPhoneNumber: String = "",
    val ownerLocation: String = "",
    val ownerVerified: Boolean = false,
    
    // Fowl basic information
    val breed: String = "",
    val name: String = "",
    val gender: String = "", // "male", "female", "unknown"
    val dateOfBirth: Date = Date(),
    val ageInDays: Int = 0, // Calculated field for easier querying
    val color: String = "",
    val weight: Double = 0.0, // in kg
    
    // Marketplace information
    val price: Double = 0.0,
    val currency: String = "INR",
    val isAvailable: Boolean = true,
    val isNegotiable: Boolean = false,
    val category: String = "", // "breeding", "meat", "eggs", "show"
    val condition: String = "", // "excellent", "good", "fair"
    
    // Location and delivery
    val location: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val deliveryAvailable: Boolean = false,
    val deliveryRadius: Int = 0, // in km
    val deliveryCharge: Double = 0.0,
    
    // Traceability and verification
    val isTraceable: Boolean = false,
    val traceabilityScore: Int = 0, // 0-100
    val parentIds: List<String> = emptyList(),
    val grandparentIds: List<String> = emptyList(),
    val bloodlineId: String = "",
    val generationNumber: Int = 0,
    
    // Health and records
    val healthStatus: String = "healthy", // "healthy", "sick", "recovering"
    val vaccinationStatus: String = "unknown", // "complete", "partial", "none", "unknown"
    val lastVaccinationDate: Date? = null,
    val healthCertificateUrl: String = "",
    val veterinarianContact: String = "",
    
    // Media and documentation
    val imageUrls: List<String> = emptyList(),
    val videoUrls: List<String> = emptyList(),
    val proofImageUrls: List<String> = emptyList(), // Certificates, records
    val thumbnailUrl: String = "",
    
    // Description and features
    val description: String = "",
    val specialFeatures: List<String> = emptyList(),
    val awards: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    
    // Performance metrics
    val eggProductionRate: Double = 0.0, // eggs per week
    val breedingHistory: List<String> = emptyList(),
    val offspringCount: Int = 0,
    val showParticipations: List<String> = emptyList(),
    
    // Marketplace metrics
    val viewCount: Int = 0,
    val favoriteCount: Int = 0,
    val inquiryCount: Int = 0,
    val shareCount: Int = 0,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    
    // Timestamps
    @ServerTimestamp
    val createdAt: Date = Date(),
    @ServerTimestamp
    val updatedAt: Date = Date(),
    val listedAt: Date? = null,
    val soldAt: Date? = null,
    
    // Status and flags
    val status: String = "active", // "active", "sold", "reserved", "inactive"
    val featured: Boolean = false,
    val urgent: Boolean = false,
    val verified: Boolean = false,
    val reportCount: Int = 0,
    val isBlocked: Boolean = false
)

/**
 * Fowl Record Collection Schema
 * Collection: "fowl_records"
 * 
 * For tracking fowl history and milestones
 */
data class FowlRecordDocument(
    @DocumentId
    val id: String = "",
    val fowlId: String = "",
    val recordType: String = "", // "birth", "vaccination", "health_check", "breeding", "milestone"
    val title: String = "",
    val description: String = "",
    val date: Date = Date(),
    val veterinarianId: String = "",
    val veterinarianName: String = "",
    val proofImageUrls: List<String> = emptyList(),
    val metadata: Map<String, Any> = emptyMap(),
    @ServerTimestamp
    val createdAt: Date = Date(),
    val createdBy: String = ""
)

/**
 * Transfer Log Collection Schema
 * Collection: "transfer_logs"
 * 
 * For tracking fowl ownership transfers
 */
data class TransferLogDocument(
    @DocumentId
    val id: String = "",
    val fowlId: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val toUserId: String = "",
    val toUserName: String = "",
    val transferType: String = "", // "sale", "gift", "breeding_loan", "return"
    val price: Double = 0.0,
    val currency: String = "INR",
    val paymentMethod: String = "",
    val paymentStatus: String = "", // "pending", "completed", "failed"
    val transferDate: Date = Date(),
    val verificationStatus: String = "", // "pending", "verified", "disputed"
    val giverConfirmation: Boolean = false,
    val receiverConfirmation: Boolean = false,
    val proofImageUrls: List<String> = emptyList(),
    val notes: String = "",
    @ServerTimestamp
    val createdAt: Date = Date(),
    @ServerTimestamp
    val updatedAt: Date = Date()
)

/**
 * User Favorites Collection Schema
 * Collection: "user_favorites"
 */
data class UserFavoriteDocument(
    @DocumentId
    val id: String = "", // userId_fowlId
    val userId: String = "",
    val fowlId: String = "",
    @ServerTimestamp
    val createdAt: Date = Date()
)

/**
 * Coin Transaction Collection Schema
 * Collection: "coin_transactions"
 * 
 * For monetization tracking
 */
data class CoinTransactionDocument(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val transactionType: String = "", // "purchase", "spend", "earn", "refund"
    val amount: Int = 0, // in coins
    val description: String = "",
    val relatedEntityId: String = "", // fowlId, listingId, etc.
    val relatedEntityType: String = "", // "fowl_listing", "verification", "transfer"
    val status: String = "", // "pending", "completed", "failed"
    val paymentMethod: String = "",
    val paymentReference: String = "",
    @ServerTimestamp
    val createdAt: Date = Date()
)

/**
 * Festival Campaign Collection Schema
 * Collection: "festival_campaigns"
 * 
 * For cultural integration features
 */
data class FestivalCampaignDocument(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val festival: String = "", // "sankranti", "diwali", "eid", etc.
    val description: String = "",
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val isActive: Boolean = false,
    val bannerImageUrl: String = "",
    val discountPercentage: Double = 0.0,
    val specialOffers: List<String> = emptyList(),
    val participatingFowlIds: List<String> = emptyList(),
    val targetLocations: List<String> = emptyList(),
    val preBookingEnabled: Boolean = false,
    val bulkOrderEnabled: Boolean = false,
    val minBulkQuantity: Int = 0,
    val bulkDiscountPercentage: Double = 0.0,
    @ServerTimestamp
    val createdAt: Date = Date(),
    @ServerTimestamp
    val updatedAt: Date = Date()
)