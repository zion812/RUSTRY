package com.rio.rustry.data.model

import java.util.*

/**
 * Inventory item domain model for farm inventory management
 */
data class InventoryItem(
    val id: String = "",
    val farmId: String = "",
    val name: String = "",
    val category: String = "", // Feed, Medicine, Equipment, Supplements, Other
    val description: String = "",
    val currentQuantity: Double = 0.0,
    val minimumQuantity: Double = 0.0,
    val maximumQuantity: Double = 0.0,
    val unit: String = "", // kg, g, L, ml, pieces, bags, bottles
    val unitPrice: Double = 0.0,
    val totalValue: Double = 0.0,
    val supplier: String = "",
    val supplierContact: String = "",
    val batchNumber: String = "",
    val manufacturingDate: Date? = null,
    val expiryDate: Date? = null,
    val location: String = "", // Storage location
    val barcode: String = "",
    val isActive: Boolean = true,
    val lastRestocked: Date? = null,
    val lastUsed: Date? = null,
    val usageRate: Double = 0.0, // Units per day
    val reorderPoint: Double = 0.0,
    val notes: String = "",
    val imageUrls: List<String> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Inventory transaction for tracking stock movements
 */
data class InventoryTransaction(
    val id: String = "",
    val inventoryItemId: String = "",
    val type: String = "", // IN, OUT, ADJUSTMENT, EXPIRED, DAMAGED
    val quantity: Double = 0.0,
    val unitPrice: Double = 0.0,
    val totalAmount: Double = 0.0,
    val reason: String = "",
    val reference: String = "", // Purchase order, sale order, etc.
    val performedBy: String = "",
    val date: Date = Date(),
    val notes: String = "",
    val createdAt: Date = Date()
)

/**
 * Inventory summary for analytics
 */
data class InventorySummary(
    val totalItems: Int = 0,
    val totalValue: Double = 0.0,
    val lowStockItems: Int = 0,
    val expiringSoonItems: Int = 0,
    val outOfStockItems: Int = 0,
    val categoryBreakdown: Map<String, Int> = emptyMap(),
    val valueByCategory: Map<String, Double> = emptyMap(),
    val topValueItems: List<InventoryItem> = emptyList(),
    val recentTransactions: List<InventoryTransaction> = emptyList(),
    val monthlyConsumption: Map<String, Double> = emptyMap()
)

/**
 * Stock alert for low stock notifications
 */
data class StockAlert(
    val id: String = "",
    val inventoryItemId: String = "",
    val itemName: String = "",
    val currentQuantity: Double = 0.0,
    val minimumQuantity: Double = 0.0,
    val alertType: String = "", // LOW_STOCK, OUT_OF_STOCK, EXPIRING_SOON, EXPIRED
    val severity: String = "", // LOW, MEDIUM, HIGH, CRITICAL
    val message: String = "",
    val isRead: Boolean = false,
    val createdAt: Date = Date()
)