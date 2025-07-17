package com.rio.rustry

import kotlinx.coroutines.test.*
import java.util.*

/**
 * Test utilities for the Rooster Platform
 * 
 * Provides common test data, utilities, and helper functions
 * for unit and integration testing.
 */
object TestUtils {
    
    // Test Data Factory
    object TestData {
        
        fun createTestUser(
            id: String = "test_user_123",
            email: String = "test@example.com",
            name: String = "Test User",
            role: UserRole = UserRole.FARMER
        ) = User(
            id = id,
            email = email,
            name = name,
            role = role,
            phoneNumber = "+1234567890",
            location = "Test Location",
            isVerified = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        fun createTestFowl(
            id: String = "test_fowl_123",
            ownerId: String = "test_user_123",
            breed: String = "Rhode Island Red",
            isTraceable: Boolean = true
        ) = Fowl(
            id = id,
            ownerId = ownerId,
            ownerName = "Test Owner",
            breed = breed,
            dateOfBirth = Date(),
            gender = "Hen",
            isTraceable = isTraceable,
            price = 150.0,
            description = "Test fowl description",
            imageUrls = listOf("test_image_url"),
            location = "Test Farm",
            healthStatus = HealthStatus.GOOD,
            vaccinationStatus = VaccinationStatus.UP_TO_DATE,
            isForSale = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        fun createTestHealthRecord(
            id: String = "test_health_123",
            fowlId: String = "test_fowl_123",
            type: HealthEventType = HealthEventType.VACCINATION
        ) = HealthRecord(
            id = id,
            fowlId = fowlId,
            type = type,
            title = "Test Health Record",
            description = "Test health record description",
            veterinarianName = "Dr. Test",
            date = Date(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        fun createTestPaymentTransaction(
            id: String = "test_payment_123",
            fowlId: String = "test_fowl_123",
            buyerId: String = "test_buyer_123",
            sellerId: String = "test_seller_123"
        ) = PaymentTransaction(
            id = id,
            fowlId = fowlId,
            buyerId = buyerId,
            sellerId = sellerId,
            amount = 150.0,
            platformFee = 7.5,
            totalAmount = 157.5,
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.UPI,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        fun createTestOwnershipTransfer(
            id: String = "test_transfer_123",
            fowlId: String = "test_fowl_123",
            fromUserId: String = "test_seller_123",
            toUserId: String = "test_buyer_123"
        ) = OwnershipTransfer(
            id = id,
            fowlId = fowlId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            transferType = TransferType.SALE,
            transferPrice = 150.0,
            status = TransferStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        fun createTestDigitalCertificate(
            id: String = "test_cert_123",
            fowlId: String = "test_fowl_123",
            ownerId: String = "test_owner_123"
        ) = DigitalCertificate(
            id = id,
            fowlId = fowlId,
            ownerId = ownerId,
            certificateType = CertificateType.OWNERSHIP,
            certificateNumber = "CERT-TEST-123",
            issueDate = System.currentTimeMillis(),
            isValid = true,
            issuedBy = "Rooster Platform Test",
            certificateVersion = "1.0",
            currentOwnerName = "Test Owner",
            previousOwnerName = "Previous Owner"
        )
        
        fun createTestMessage(
            id: String = "test_message_123",
            senderId: String = "test_sender_123",
            receiverId: String = "test_receiver_123"
        ) = Message(
            id = id,
            senderId = senderId,
            receiverId = receiverId,
            content = "Test message content",
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        
        fun createTestAIHealthTip(
            id: String = "test_tip_123",
            category: TipCategory = TipCategory.GENERAL_CARE
        ) = AIHealthTip(
            id = id,
            title = "Test Health Tip",
            content = "Test health tip content",
            category = category,
            priority = TipPriority.MEDIUM,
            createdAt = System.currentTimeMillis()
        )
    }
    
    // Test Assertions
    object Assertions {
        
        fun assertUserEquals(expected: User, actual: User) {
            assert(expected.id == actual.id) { "User IDs don't match" }
            assert(expected.email == actual.email) { "User emails don't match" }
            assert(expected.name == actual.name) { "User names don't match" }
            assert(expected.role == actual.role) { "User roles don't match" }
        }
        
        fun assertFowlEquals(expected: Fowl, actual: Fowl) {
            assert(expected.id == actual.id) { "Fowl IDs don't match" }
            assert(expected.ownerId == actual.ownerId) { "Fowl owner IDs don't match" }
            assert(expected.breed == actual.breed) { "Fowl breeds don't match" }
            assert(expected.isTraceable == actual.isTraceable) { "Fowl traceability doesn't match" }
        }
        
        fun assertHealthRecordEquals(expected: HealthRecord, actual: HealthRecord) {
            assert(expected.id == actual.id) { "Health record IDs don't match" }
            assert(expected.fowlId == actual.fowlId) { "Health record fowl IDs don't match" }
            assert(expected.type == actual.type) { "Health record types don't match" }
            assert(expected.title == actual.title) { "Health record titles don't match" }
        }
        
        fun assertPaymentEquals(expected: PaymentTransaction, actual: PaymentTransaction) {
            assert(expected.id == actual.id) { "Payment IDs don't match" }
            assert(expected.fowlId == actual.fowlId) { "Payment fowl IDs don't match" }
            assert(expected.amount == actual.amount) { "Payment amounts don't match" }
            assert(expected.status == actual.status) { "Payment statuses don't match" }
        }
        
        fun assertTransferEquals(expected: OwnershipTransfer, actual: OwnershipTransfer) {
            assert(expected.id == actual.id) { "Transfer IDs don't match" }
            assert(expected.fowlId == actual.fowlId) { "Transfer fowl IDs don't match" }
            assert(expected.fromUserId == actual.fromUserId) { "Transfer from user IDs don't match" }
            assert(expected.toUserId == actual.toUserId) { "Transfer to user IDs don't match" }
        }
    }
    
    // Mock Data Generators
    object MockData {
        
        fun generateUsers(count: Int): List<User> {
            return (1..count).map { index ->
                TestData.createTestUser(
                    id = "user_$index",
                    email = "user$index@test.com",
                    name = "Test User $index"
                )
            }
        }
        
        fun generateFowls(count: Int, ownerId: String = "test_owner"): List<Fowl> {
            val breeds = listOf("Rhode Island Red", "Leghorn", "Plymouth Rock", "Orpington", "Sussex")
            return (1..count).map { index ->
                TestData.createTestFowl(
                    id = "fowl_$index",
                    ownerId = ownerId,
                    breed = breeds[index % breeds.size]
                )
            }
        }
        
        fun generateHealthRecords(count: Int, fowlId: String): List<HealthRecord> {
            val types = HealthEventType.values()
            return (1..count).map { index ->
                TestData.createTestHealthRecord(
                    id = "health_$index",
                    fowlId = fowlId,
                    type = types[index % types.size]
                )
            }
        }
        
        fun generatePayments(count: Int): List<PaymentTransaction> {
            return (1..count).map { index ->
                TestData.createTestPaymentTransaction(
                    id = "payment_$index",
                    fowlId = "fowl_$index",
                    buyerId = "buyer_$index",
                    sellerId = "seller_$index"
                )
            }
        }
        
        fun generateTransfers(count: Int): List<OwnershipTransfer> {
            return (1..count).map { index ->
                TestData.createTestOwnershipTransfer(
                    id = "transfer_$index",
                    fowlId = "fowl_$index",
                    fromUserId = "seller_$index",
                    toUserId = "buyer_$index"
                )
            }
        }
    }
    
    // Test Result Helpers
    object Results {
        
        fun <T> successResult(data: T): Result<T> = Result.Success(data)
        
        fun <T> failureResult(message: String): Result<T> = 
            Result.Error(Exception(message))
        
        fun <T> networkErrorResult(): Result<T> = 
            Result.Error(Exception("Network error"))
        
        fun <T> notFoundResult(): Result<T> = 
            Result.Error(Exception("Not found"))
        
        fun <T> unauthorizedResult(): Result<T> = 
            Result.Error(Exception("Unauthorized"))
    }
}

/**
 * Base test class with common setup
 */
abstract class BaseTest {
    
    protected fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.test.runTest {
            block()
        }
    }
}

// Mock enums and data classes for testing
enum class UserRole { FARMER, BUYER, VETERINARIAN, ADMIN }
enum class HealthStatus { GOOD, FAIR, POOR, SICK }
enum class VaccinationStatus { UP_TO_DATE, OVERDUE, NONE }
enum class HealthEventType { VACCINATION, CHECKUP, TREATMENT, ILLNESS }
enum class PaymentStatus { PENDING, PROCESSING, SUCCESS, FAILED, CANCELLED, REFUNDED, PARTIAL_REFUND }
enum class PaymentMethod { CARD, UPI, NET_BANKING, WALLET, EMI, CASH_ON_DELIVERY }
enum class TransferType { SALE, GIFT, INHERITANCE }
enum class TransferStatus { PENDING, COMPLETED, CANCELLED }
enum class CertificateType { OWNERSHIP, HEALTH, VACCINATION }
enum class TipCategory { GENERAL_CARE, FEEDING, HEALTH, BREEDING }
enum class TipPriority { LOW, MEDIUM, HIGH, URGENT }

data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val phoneNumber: String,
    val location: String,
    val isVerified: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class Fowl(
    val id: String,
    val ownerId: String,
    val ownerName: String,
    val breed: String,
    val dateOfBirth: Date,
    val gender: String,
    val isTraceable: Boolean,
    val price: Double,
    val description: String,
    val imageUrls: List<String>,
    val location: String,
    val healthStatus: HealthStatus,
    val vaccinationStatus: VaccinationStatus,
    val isForSale: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class HealthRecord(
    val id: String,
    val fowlId: String,
    val type: HealthEventType,
    val title: String,
    val description: String,
    val veterinarianName: String,
    val date: Date,
    val createdAt: Long,
    val updatedAt: Long
)

data class PaymentTransaction(
    val id: String,
    val fowlId: String,
    val buyerId: String,
    val sellerId: String,
    val amount: Double,
    val platformFee: Double,
    val totalAmount: Double,
    val status: PaymentStatus,
    val paymentMethod: PaymentMethod,
    val createdAt: Long,
    val updatedAt: Long
)

data class OwnershipTransfer(
    val id: String,
    val fowlId: String,
    val fromUserId: String,
    val toUserId: String,
    val transferType: TransferType,
    val transferPrice: Double,
    val status: TransferStatus,
    val createdAt: Long,
    val updatedAt: Long
)

data class DigitalCertificate(
    val id: String,
    val fowlId: String,
    val ownerId: String,
    val certificateType: CertificateType,
    val certificateNumber: String,
    val issueDate: Long,
    val isValid: Boolean,
    val issuedBy: String,
    val certificateVersion: String,
    val currentOwnerName: String,
    val previousOwnerName: String
)

data class Message(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean
)

data class AIHealthTip(
    val id: String,
    val title: String,
    val content: String,
    val category: TipCategory,
    val priority: TipPriority,
    val createdAt: Long
)