package com.rio.rustry.domain.model

/**
 * User types for the Rustry application
 */
enum class UserType {
    GENERAL,    // Urban users - explore and buy products
    FARMER,     // Rural/Semi-urban - list products and community
    HIGH_LEVEL  // Enthusiasts & Breeders - advanced tracking and breeding
}

/**
 * User profile data model
 */
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val userType: UserType = UserType.GENERAL,
    val location: String = "",
    val isVerified: Boolean = false,
    val isCertified: Boolean = false,
    val profileImageUrl: String = "",
    val farmName: String = "", // For farmers and high-level users
    val brandName: String = "", // For high-level users
    val kycCompleted: Boolean = false,
    val averageTurnover: Double = 0.0,
    val numberOfBirds: Int = 0,
    val coins: Int = 0, // Virtual currency for platform monetization
    val totalCoinsEarned: Int = 0, // Lifetime coins earned
    val totalCoinsSpent: Int = 0, // Lifetime coins spent
    val registrationDate: Long = System.currentTimeMillis(),
    val verificationDate: Long? = null,
    val certificationDate: Long? = null
)

/**
 * Product categories for listing
 */
enum class ProductCategory {
    CHICKS,
    ADULTS,
    BREEDERS,
    EGGS,
    CUTTING,    // For consumption
    ADOPTION    // For breeding/pets
}

/**
 * Product traceability type
 */
enum class TraceabilityType {
    TRACEABLE,      // Full family tree and documentation
    NON_TRACEABLE   // Basic information only
}

/**
 * Age groups for poultry
 */
enum class AgeGroup {
    CHICK_0_5_WEEKS,        // 0-5 weeks
    YOUNG_5_WEEKS_5_MONTHS, // 5 weeks - 5 months
    ADULT_5_12_MONTHS,      // 5 months - 12 months
    BREEDER_12_PLUS         // 12+ months (breeding age)
}

/**
 * Product listing model
 */
data class ProductListing(
    val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerType: UserType = UserType.FARMER,
    val isOwnerVerified: Boolean = false,
    val isOwnerCertified: Boolean = false,
    
    // Basic product info
    val title: String = "",
    val description: String = "",
    val category: ProductCategory = ProductCategory.ADULTS,
    val breed: String = "",
    val ageGroup: AgeGroup = AgeGroup.ADULT_5_12_MONTHS,
    val gender: String = "", // Male, Female, Mixed
    val color: String = "",
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val price: Double = 0.0,
    val location: String = "",
    val isNearbyDelivery: Boolean = false,
    val deliveryRadius: Int = 0, // in km
    
    // Media
    val imageUrls: List<String> = emptyList(),
    val videoUrls: List<String> = emptyList(),
    val audioUrls: List<String> = emptyList(),
    
    // Traceability
    val traceabilityType: TraceabilityType = TraceabilityType.NON_TRACEABLE,
    val familyTreeId: String = "",
    val placeOfBirth: String = "",
    val dateOfBirth: Long? = null,
    val parentIds: List<String> = emptyList(),
    val bloodline: String = "",
    val pedigreeDocuments: List<String> = emptyList(),
    
    // Health & Vaccination
    val vaccinationRecords: List<VaccinationRecord> = emptyList(),
    val healthCertificates: List<String> = emptyList(),
    val lastHealthCheckDate: Long? = null,
    
    // Availability & Status
    val isAvailable: Boolean = true,
    val availableQuantity: Int = 1,
    val minimumResponseTime: Int = 24, // hours
    val deliveryOptions: List<String> = emptyList(), // "farmer_delivery", "self_pickup"
    val allowAuction: Boolean = false,
    val startingBidPrice: Double = 0.0,
    val auctionEndDate: Long? = null,
    
    // Engagement
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val bidCount: Int = 0,
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

/**
 * Vaccination record
 */
data class VaccinationRecord(
    val id: String = "",
    val vaccineName: String = "",
    val vaccinationDate: Long = System.currentTimeMillis(),
    val nextDueDate: Long? = null,
    val veterinarianName: String = "",
    val batchNumber: String = "",
    val certificateUrl: String = ""
)

/**
 * Family tree node
 */
data class FamilyTreeNode(
    val id: String = "",
    val birdId: String = "",
    val name: String = "",
    val breed: String = "",
    val gender: String = "",
    val dateOfBirth: Long? = null,
    val imageUrl: String = "",
    val parentIds: List<String> = emptyList(),
    val childrenIds: List<String> = emptyList(),
    val generation: Int = 0,
    val isAlive: Boolean = true,
    val deathDate: Long? = null,
    val achievements: List<String> = emptyList(),
    val specialTraits: List<String> = emptyList()
)

/**
 * Farm/Brand information
 */
data class FarmBrand(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val establishedDate: Long = System.currentTimeMillis(),
    val logoUrl: String = "",
    val coverImageUrl: String = "",
    val isVerified: Boolean = false,
    val isCertified: Boolean = false,
    val certificationDocuments: List<String> = emptyList(),
    val specializations: List<String> = emptyList(), // breeds they specialize in
    val totalBirds: Int = 0,
    val totalChicks: Int = 0,
    val totalAdults: Int = 0,
    val totalBreeders: Int = 0,
    val averageMonthlyTurnover: Double = 0.0,
    val rating: Float = 0.0f,
    val reviewCount: Int = 0,
    val socialMediaLinks: Map<String, String> = emptyMap()
)

/**
 * Transfer request model
 */
data class TransferRequest(
    val id: String = "",
    val fowlId: String = "",
    val sellerId: String = "",
    val buyerId: String = "",
    val price: Double = 0.0,
    val notes: String = "",
    val status: TransferStatus = TransferStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val transferType: String = "sale", // "sale", "gift", "breeding_loan"
    val verificationDate: Long? = null,
    val documentationProofs: List<String> = emptyList(),
    val familyTreeTransferred: Boolean = false,
    val rightsTransferred: List<String> = emptyList(),
    val transferLocation: String = "",
    val transferPhotos: List<String> = emptyList(),
    val verifiedByPlatform: Boolean = false
)

enum class TransferStatus {
    PENDING,
    PENDING_VERIFICATION,
    COMPLETED,
    CANCELLED,
    DISPUTED
}

/**
 * Social feed post
 */
data class SocialPost(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorType: UserType = UserType.GENERAL,
    val authorVerified: Boolean = false,
    val content: String = "",
    val mediaUrls: List<String> = emptyList(),
    val mediaType: String = "", // "image", "video", "audio"
    val hashtags: List<String> = emptyList(),
    val mentions: List<String> = emptyList(),
    val location: String = "",
    val productId: String = "", // if showcasing a product
    val isShowcase: Boolean = false,
    val isPromotion: Boolean = false,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val viewCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

/**
 * Order model
 */
data class Order(
    val id: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val productId: String = "",
    val quantity: Int = 1,
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "", // "online", "cod", "advance"
    val paymentStatus: String = "", // "pending", "paid", "partial"
    val advanceAmount: Double = 0.0,
    val deliveryMethod: String = "", // "farmer_delivery", "self_pickup"
    val deliveryAddress: String = "",
    val orderStatus: String = "", // "placed", "confirmed", "delivered", "completed"
    val orderDate: Long = System.currentTimeMillis(),
    val expectedDeliveryDate: Long? = null,
    val actualDeliveryDate: Long? = null,
    val trackingId: String = "",
    val notes: String = "",
    val rating: Float = 0.0f,
    val review: String = "",
    val reviewDate: Long? = null
)

/**
 * Cart item
 */
data class CartItem(
    val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val quantity: Int = 1,
    val selectedOptions: Map<String, String> = emptyMap(),
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * Notification model
 */
data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // "order", "health", "social", "system"
    val priority: String = "", // "low", "medium", "high", "urgent"
    val actionUrl: String = "",
    val imageUrl: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null
)