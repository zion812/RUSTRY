package com.rio.rustry.data.model

import java.util.*

/**
 * Sale record domain model for tracking fowl sales
 */
data class SaleRecord(
    val id: String = "",
    val farmId: String = "",
    val fowlId: String = "",
    val fowlName: String = "",
    val buyerName: String = "",
    val buyerContact: String = "",
    val buyerEmail: String = "",
    val salePrice: Double = 0.0,
    val originalPrice: Double = 0.0,
    val discount: Double = 0.0,
    val saleDate: Date = Date(),
    val paymentMethod: String = "Cash", // Cash, Bank Transfer, UPI, Cheque, Other
    val paymentStatus: String = "Paid", // Paid, Pending, Partial, Overdue
    val deliveryMethod: String = "Pickup", // Pickup, Delivery, Shipping
    val deliveryAddress: String = "",
    val deliveryDate: Date? = null,
    val notes: String = "",
    val invoiceNumber: String = "",
    val taxAmount: Double = 0.0,
    val commission: Double = 0.0,
    val netAmount: Double = 0.0,
    val isVerified: Boolean = false,
    val verificationDocuments: List<String> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Sales summary for analytics
 */
data class SalesSummary(
    val totalSales: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageSalePrice: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val yearlyRevenue: Double = 0.0,
    val topBuyers: List<BuyerSummary> = emptyList(),
    val salesByMonth: Map<String, Double> = emptyMap(),
    val salesByPaymentMethod: Map<String, Int> = emptyMap(),
    val pendingPayments: Double = 0.0,
    val overduePayments: Double = 0.0
)

/**
 * Buyer summary for analytics
 */
data class BuyerSummary(
    val buyerName: String = "",
    val buyerContact: String = "",
    val totalPurchases: Int = 0,
    val totalAmount: Double = 0.0,
    val lastPurchaseDate: Date = Date(),
    val averageOrderValue: Double = 0.0
)