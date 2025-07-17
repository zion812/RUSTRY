package com.rio.rustry.domain.model

/**
 * Transfer log for tracking fowl ownership changes and maintaining traceability
 */
data class TransferLog(
    val transferId: String = "",
    val fowlId: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val fromUserName: String = "",
    val toUserName: String = "",
    val transferType: TransferType = TransferType.SALE,
    val transferReason: String = "",
    val transferDate: Long = System.currentTimeMillis(),
    val transferLocation: String = "",
    val transferMethod: String = "", // "direct", "platform", "auction"
    val price: Double = 0.0,
    val currency: String = "USD",
    val paymentMethod: String = "",
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val transferStatus: TransferLogStatus = TransferLogStatus.INITIATED,
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val verifiedBy: String = "",
    val verificationDate: Long? = null,
    val documentationProofs: List<String> = emptyList(),
    val transferPhotos: List<String> = emptyList(),
    val transferVideos: List<String> = emptyList(),
    val healthCertificateRequired: Boolean = false,
    val healthCertificateUrl: String = "",
    val vaccinationRecordsTransferred: Boolean = false,
    val pedigreeTransferred: Boolean = false,
    val breedingRightsTransferred: Boolean = false,
    val ownershipDocumentUrl: String = "",
    val contractUrl: String = "",
    val insuranceTransferred: Boolean = false,
    val microchipTransferred: Boolean = false,
    val ringTransferred: Boolean = false,
    val specialConditions: List<String> = emptyList(),
    val warrantyPeriod: Int = 0, // days
    val returnPolicy: String = "",
    val transportationDetails: TransportationDetails? = null,
    val witnessIds: List<String> = emptyList(),
    val platformFee: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val refundAmount: Double = 0.0,
    val refundReason: String = "",
    val refundDate: Long? = null,
    val disputeId: String = "",
    val disputeStatus: String = "",
    val notes: String = "",
    val internalNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val cancelledAt: Long? = null,
    val cancellationReason: String = ""
)

/**
 * Types of transfers
 */
enum class TransferType {
    SALE,           // Commercial sale
    GIFT,           // Gift transfer
    BREEDING_LOAN,  // Temporary breeding loan
    INHERITANCE,    // Inheritance transfer
    DONATION,       // Charitable donation
    EXCHANGE,       // Barter/exchange
    RETURN,         // Return to previous owner
    RESCUE,         // Rescue/rehabilitation
    BREEDING_SHARE, // Breeding partnership
    LEASE,          // Temporary lease
    AUCTION,        // Auction sale
    EMERGENCY       // Emergency transfer
}

/**
 * Payment status for transfers
 */
enum class PaymentStatus {
    PENDING,        // Payment not yet made
    PARTIAL,        // Partial payment received
    PAID,           // Full payment received
    REFUNDED,       // Payment refunded
    DISPUTED,       // Payment under dispute
    FAILED,         // Payment failed
    CANCELLED       // Payment cancelled
}

/**
 * Transfer log status
 */
enum class TransferLogStatus {
    INITIATED,      // Transfer request initiated
    PENDING,        // Waiting for acceptance
    ACCEPTED,       // Transfer accepted by recipient
    IN_PROGRESS,    // Transfer in progress
    COMPLETED,      // Transfer completed successfully
    CANCELLED,      // Transfer cancelled
    REJECTED,       // Transfer rejected
    DISPUTED,       // Transfer under dispute
    FAILED,         // Transfer failed
    EXPIRED         // Transfer request expired
}

/**
 * Verification status for transfers
 */
enum class VerificationStatus {
    PENDING,        // Awaiting verification
    VERIFIED,       // Verified by platform
    REJECTED,       // Verification rejected
    MANUAL_REVIEW,  // Requires manual review
    DISPUTED,       // Verification disputed
    EXPIRED,        // Verification expired
    NOT_REQUIRED    // Verification not required
}

/**
 * Transportation details for fowl transfer
 */
data class TransportationDetails(
    val transportMethod: String = "", // "self_pickup", "seller_delivery", "third_party"
    val transporterId: String = "",
    val transporterName: String = "",
    val transporterContact: String = "",
    val vehicleDetails: String = "",
    val pickupDate: Long? = null,
    val deliveryDate: Long? = null,
    val pickupLocation: String = "",
    val deliveryLocation: String = "",
    val distance: Double = 0.0,
    val transportCost: Double = 0.0,
    val insuranceCovered: Boolean = false,
    val specialRequirements: List<String> = emptyList(),
    val temperatureControlled: Boolean = false,
    val trackingId: String = "",
    val transportStatus: String = "",
    val estimatedDuration: Int = 0, // hours
    val actualDuration: Int = 0, // hours
    val fuelCost: Double = 0.0,
    val tollCost: Double = 0.0,
    val handlingInstructions: String = "",
    val emergencyContact: String = "",
    val transportPhotos: List<String> = emptyList(),
    val deliveryConfirmationUrl: String = "",
    val notes: String = ""
)

/**
 * Transfer verification for dual-party confirmation
 */
data class TransferVerification(
    val transferId: String = "",
    val verifierId: String = "",
    val verified: Boolean = false,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val verificationPhotos: List<String> = emptyList(),
    val fowlCondition: String = "",
    val colorVerified: Boolean = false,
    val ageVerified: Boolean = false,
    val priceVerified: Boolean = false,
    val healthVerified: Boolean = false,
    val documentationVerified: Boolean = false,
    val aiVerificationScore: Float = 0.0f,
    val aiVerificationDetails: String = "",
    val gpsLocation: String = "",
    val deviceInfo: String = ""
)

/**
 * Proof upload for transfer verification
 */
data class ProofUpload(
    val id: String = "",
    val transferId: String = "",
    val uploaderId: String = "",
    val imageUrl: String = "",
    val imageHash: String = "",
    val uploadTimestamp: Long = System.currentTimeMillis(),
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val aiAnalysisResult: String = "",
    val aiConfidenceScore: Float = 0.0f,
    val expectedDetails: Map<String, Any> = emptyMap(),
    val actualDetails: Map<String, Any> = emptyMap(),
    val gpsLocation: String = "",
    val deviceInfo: String = "",
    val notes: String = ""
)

/**
 * Transfer verification document
 */
data class TransferVerificationDocument(
    val documentId: String = "",
    val transferId: String = "",
    val documentType: String = "", // "health_certificate", "ownership_proof", "contract"
    val documentUrl: String = "",
    val documentName: String = "",
    val issuedBy: String = "",
    val issuedDate: Long = System.currentTimeMillis(),
    val expiryDate: Long? = null,
    val verificationCode: String = "",
    val isVerified: Boolean = false,
    val verifiedBy: String = "",
    val verificationDate: Long? = null,
    val documentHash: String = "",
    val blockchainTxId: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Transfer dispute record
 */
data class TransferDispute(
    val disputeId: String = "",
    val transferId: String = "",
    val raisedBy: String = "",
    val disputeType: String = "", // "quality", "health", "documentation", "payment"
    val disputeReason: String = "",
    val description: String = "",
    val evidenceUrls: List<String> = emptyList(),
    val status: String = "", // "open", "investigating", "resolved", "closed"
    val priority: String = "", // "low", "medium", "high", "urgent"
    val assignedTo: String = "",
    val resolution: String = "",
    val resolutionDate: Long? = null,
    val compensationAmount: Double = 0.0,
    val refundAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null
)