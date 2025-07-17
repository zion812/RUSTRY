package com.rio.rustry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Transaction data model for payment processing
 */
@Entity(tableName = "transactions")
@Serializable
data class Transaction(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val fowlId: String = "",
    val sellerId: String = "",
    val buyerId: String = "",
    
    // Transaction details
    val type: String = "", // PURCHASE, SALE, REFUND, COMMISSION
    val status: String = "", // PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED
    val amount: Double = 0.0,
    val currency: String = "INR",
    val paymentMethod: String = "", // UPI, CARD, BANK_TRANSFER, CASH, RAZORPAY
    
    // Payment gateway details
    val paymentGatewayId: String = "",
    val paymentGatewayOrderId: String = "",
    val paymentGatewayPaymentId: String = "",
    val paymentGatewaySignature: String = "",
    
    // Timestamps
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val completedAt: Long? = null,
    val failedAt: Long? = null,
    
    // Additional details
    val description: String = "",
    val notes: String = "",
    val receiptUrl: String = "",
    val invoiceNumber: String = "",
    
    // Fees and charges
    val platformFee: Double = 0.0,
    val paymentGatewayFee: Double = 0.0,
    val gst: Double = 0.0,
    val netAmount: Double = 0.0,
    
    // Refund details
    val refundAmount: Double = 0.0,
    val refundReason: String = "",
    val refundedAt: Long? = null,
    val refundTransactionId: String = "",
    
    // Error details
    val errorCode: String = "",
    val errorMessage: String = "",
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    
    // Sync status
    val isSynced: Boolean = true,
    val needsSync: Boolean = false
)