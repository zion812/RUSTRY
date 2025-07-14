package com.rio.rustry.domain.payment

/**
 * Simple Payment Gateway Interface for RUSTRY
 */
interface PaymentGateway {
    suspend fun createOrder(amount: Long): String
    suspend fun processPayment(orderId: String): Boolean
}

/**
 * Payment status enum
 */
enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED
}