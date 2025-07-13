package com.rio.rustry.data.model

import java.util.Date

/**
 * Transfer and ownership-related data models for the Rooster Platform
 */

data class OwnershipTransfer(
    val id: String = "",
    val fowlId: String = "",
    val fowlTitle: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val fromUserEmail: String = "",
    val toUserId: String = "",
    val toUserName: String = "",
    val toUserEmail: String = "",
    val transferType: TransferType = TransferType.SALE,
    val status: TransferStatus = TransferStatus.PENDING,
    val transferPrice: Double = 0.0,
    val currency: String = "USD",
    val paymentId: String = "",
    val verificationMethod: VerificationMethod = VerificationMethod.EMAIL,
    val verificationCode: String = "",
    val requiresVerification: Boolean = true,
    val isVerified: Boolean = false,
    val verifiedAt: Date? = null,
    val transferDate: Date = Date(),
    val completedDate: Date? = null,
    val cancelledDate: Date? = null,
    val sellerConfirmedAt: Date? = null,
    val buyerConfirmedAt: Date? = null,
    val completedAt: Date? = null,
    val reason: String = "",
    val notes: String = "",
    val witnessName: String = "",
    val digitalCertificateId: String = "",
    val healthSummary: CertificateHealthSummary? = null,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class DigitalCertificate(
    val id: String = "",
    val fowlId: String = "",
    val ownerId: String = "",
    val transferId: String = "",
    val certificateType: CertificateType = CertificateType.OWNERSHIP,
    val certificateNumber: String = "",
    val issueDate: Date = Date(),
    val expiryDate: Date? = null,
    val validUntil: Date? = null,
    val isValid: Boolean = true,
    val issuedBy: String = "",
    val revokedAt: Date? = null,
    val revokedReason: String = "",
    val qrCode: String = "",
    val qrCodeData: String = "",
    val blockchainHash: String = "",
    val digitalSignature: String = "",
    val certificateVersion: String = "1.0",
    val fowlDetails: FowlCertificateDetails? = null,
    val healthSummary: CertificateHealthSummary? = null,
    val transferDetails: TransferCertificateDetails? = null,
    val lineage: List<LineageEntry> = emptyList(),
    val verificationUrl: String = "",
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class FowlCertificateDetails(
    val fowlId: String = "",
    val breed: String = "",
    val dateOfBirth: Date = Date(),
    val gender: String = "",
    val color: String = "",
    val weight: Double = 0.0,
    val isTraceable: Boolean = false,
    val parentIds: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val uniqueMarkers: List<String> = emptyList(), // Physical identifiers
    val microchipId: String = "",
    val dnaProfile: String = "",
    val registrationNumber: String = "",
    val currentOwnerName: String = "",
    val previousOwnerName: String = ""
)

data class TransferCertificateDetails(
    val transferId: String = "",
    val fromOwner: String = "",
    val toOwner: String = "",
    val transferDate: Date = Date(),
    val transferType: TransferType = TransferType.SALE,
    val transferPrice: Double = 0.0,
    val transferNotes: String = "",
    val transferReason: String = "",
    val currency: String = "USD",
    val verificationMethod: VerificationMethod = VerificationMethod.EMAIL,
    val witnessName: String = "",
    val witnessContact: String = "",
    val legalDocuments: List<String> = emptyList()
)

data class LineageEntry(
    val fowlId: String = "",
    val relationship: String = "", // "parent", "grandparent", etc.
    val breed: String = "",
    val dateOfBirth: Date = Date(),
    val gender: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val generation: Int = 0,
    val certificateId: String = "",
    val ownershipStartDate: Date = Date(),
    val ownershipEndDate: Date? = null,
    val transferReason: String = ""
)

data class TransferRequest(
    val id: String = "",
    val fowlId: String = "",
    val requesterId: String = "",
    val currentOwnerId: String = "",
    val transferType: TransferType = TransferType.SALE,
    val proposedPrice: Double = 0.0,
    val message: String = "",
    val status: TransferRequestStatus = TransferRequestStatus.PENDING,
    val expiryDate: Date = Date(),
    val createdAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null
)

data class VerificationCode(
    val id: String = "",
    val transferId: String = "",
    val code: String = "",
    val method: VerificationMethod = VerificationMethod.EMAIL,
    val recipient: String = "",
    val isUsed: Boolean = false,
    val expiryDate: Date = Date(),
    val createdAt: Long = System.currentTimeMillis(),
    val usedAt: Long? = null
)

// Enums
enum class TransferType(val displayName: String) {
    SALE("Sale"),
    GIFT("Gift"),
    INHERITANCE("Inheritance"),
    BREEDING_LOAN("Breeding Loan"),
    TEMPORARY_CUSTODY("Temporary Custody"),
    RESCUE("Rescue"),
    DONATION("Donation"),
    TRADE("Trade"),
    OTHER("Other")
}

enum class TransferStatus(val displayName: String) {
    PENDING("Pending"),
    SELLER_CONFIRMED("Seller Confirmed"),
    BUYER_CONFIRMED("Buyer Confirmed"),
    VERIFICATION_REQUIRED("Verification Required"),
    PAYMENT_PENDING("Payment Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    FAILED("Failed"),
    DISPUTED("Disputed"),
    EXPIRED("Expired")
}

enum class VerificationMethod(val displayName: String) {
    EMAIL("Email Verification"),
    SMS("SMS Verification"),
    PHONE_CALL("Phone Call"),
    IN_PERSON("In-Person Verification"),
    DIGITAL_SIGNATURE("Digital Signature"),
    DUAL_CONFIRMATION("Dual Confirmation"),
    BIOMETRIC("Biometric Verification"),
    GOVERNMENT_ID("Government ID"),
    WITNESS("Witness Verification")
}

enum class UserRole(val displayName: String) {
    BUYER("Buyer"),
    SELLER("Seller"),
    OWNER("Owner"),
    PREVIOUS_OWNER("Previous Owner"),
    WITNESS("Witness"),
    VETERINARIAN("Veterinarian"),
    ADMIN("Administrator")
}

enum class CertificateType(val displayName: String) {
    OWNERSHIP("Ownership Certificate"),
    HEALTH("Health Certificate"),
    BREEDING("Breeding Certificate"),
    TRANSFER("Transfer Certificate"),
    LINEAGE("Lineage Certificate"),
    VACCINATION("Vaccination Certificate"),
    EXPORT("Export Certificate"),
    IMPORT("Import Certificate")
}

enum class TransferRequestStatus(val displayName: String) {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    EXPIRED("Expired"),
    WITHDRAWN("Withdrawn")
}