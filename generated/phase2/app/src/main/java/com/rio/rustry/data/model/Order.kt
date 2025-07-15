// generated/phase2/app/src/main/java/com/rio/rustry/data/model/Order.kt

package com.rio.rustry.data.model

import com.rio.rustry.payment.PaymentMethod

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val total: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentMethod: PaymentMethod = PaymentMethod.COD,
    val deliveryAddress: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

data class OrderItem(
    val fowlId: String = "",
    val fowlName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

enum class FowlStatus {
    PUBLISHED,
    DRAFT,
    SOLD,
    INACTIVE
}