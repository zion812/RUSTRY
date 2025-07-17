package com.rio.rustry.data.model

import java.util.Date

/**
 * Payment-related data models for the Rooster Platform
 */

data class Payment(
    val id: String = "",
    val transactionId: String = "",
    val fowlId: String = "",
    val fowlTitle: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val buyerEmail: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val sellerEmail: String = "",
    val amount: Double = 0.0,
    val platformFee: Double = 0.0,
    val netAmount: Double = 0.0,
    val currency: String = "USD",
    val method: PaymentMethodEnum = PaymentMethodEnum.GOOGLE_PAY,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val description: String = "",
    val fees: Double = 0.0,
    val googlePayToken: String = "",
    val paymentDate: Date? = null,
    val dueDate: Date? = null,
    val refundAmount: Double = 0.0,
    val refundDate: Date? = null,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PaymentTransaction(
    val id: String = "",
    val fowlId: String = "",
    val fowlTitle: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val counterpartyName: String = "",
    val type: TransactionType = TransactionType.PURCHASE,
    val amount: Double = 0.0,
    val currency: String = "USD",
    val status: TransactionStatus = TransactionStatus.PENDING,
    val paymentId: String = "",
    val description: String = "",
    val date: Date = Date(),
    val completedDate: Date? = null,
    val cancelledDate: Date? = null,
    val refundedDate: Date? = null,
    val notes: String = "",
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PaymentConfig(
    val merchantId: String = "",
    val merchantName: String = "Rooster Platform",
    val countryCode: String = "US",
    val currencyCode: String = "USD",
    val environment: PaymentEnvironment = PaymentEnvironment.TEST,
    val platformFeePercentage: Double = 5.0, // 5% platform fee
    val allowedPaymentMethods: List<PaymentMethodEnum> = listOf(PaymentMethodEnum.GOOGLE_PAY),
    val allowedCardNetworks: List<String> = listOf("VISA", "MASTERCARD"),
    val allowedCardAuthMethods: List<String> = listOf("PAN_ONLY", "CRYPTOGRAM_3DS"),
    val gatewayTokenizationSpecification: Map<String, String> = emptyMap(),
    val directTokenizationSpecification: Map<String, String> = emptyMap(),
    val shippingAddressRequired: Boolean = false,
    val billingAddressRequired: Boolean = true,
    val emailRequired: Boolean = true
)

data class PaymentIntent(
    val id: String = "",
    val amount: Double = 0.0,
    val currency: String = "USD",
    val description: String = "",
    val metadata: Map<String, String> = emptyMap(),
    val status: PaymentIntentStatus = PaymentIntentStatus.REQUIRES_PAYMENT_METHOD,
    val clientSecret: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class PaymentMethod(
    val id: String = "",
    val type: String = "",
    val card: CardDetails? = null,
    val billingDetails: BillingDetails? = null,
    val metadata: Map<String, String> = emptyMap()
)

data class CardDetails(
    val brand: String = "",
    val country: String = "",
    val expMonth: Int = 0,
    val expYear: Int = 0,
    val funding: String = "",
    val last4: String = ""
)

data class BillingDetails(
    val address: Address? = null,
    val email: String = "",
    val name: String = "",
    val phone: String = ""
)

data class Address(
    val city: String = "",
    val country: String = "",
    val line1: String = "",
    val line2: String = "",
    val postalCode: String = "",
    val state: String = ""
)

// Enums
enum class PaymentMethodEnum(val displayName: String) {
    GOOGLE_PAY("Google Pay"),
    UPI("UPI"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    BANK_TRANSFER("Bank Transfer"),
    CASH("Cash"),
    MOBILE_MONEY("Mobile Money"),
    CRYPTOCURRENCY("Cryptocurrency"),
    OTHER("Other")
}

enum class PaymentStatus(val displayName: String) {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded"),
    PARTIALLY_REFUNDED("Partially Refunded"),
    DISPUTED("Disputed"),
    EXPIRED("Expired")
}

enum class TransactionType(val displayName: String) {
    PURCHASE("Purchase"),
    SALE("Sale"),
    REFUND("Refund"),
    TRANSFER("Transfer"),
    FEE("Fee"),
    COMMISSION("Commission"),
    BONUS("Bonus"),
    PENALTY("Penalty"),
    OTHER("Other")
}

enum class TransactionStatus(val displayName: String) {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded"),
    DISPUTED("Disputed")
}

enum class PaymentEnvironment(val displayName: String) {
    TEST("Test"),
    PRODUCTION("Production")
}

enum class PaymentIntentStatus(val displayName: String) {
    REQUIRES_PAYMENT_METHOD("Requires Payment Method"),
    REQUIRES_CONFIRMATION("Requires Confirmation"),
    REQUIRES_ACTION("Requires Action"),
    PROCESSING("Processing"),
    REQUIRES_CAPTURE("Requires Capture"),
    CANCELLED("Cancelled"),
    SUCCEEDED("Succeeded")
}