package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rio.rustry.data.model.InventoryItem
import com.rio.rustry.data.model.InventorySummary
import com.rio.rustry.data.model.InventoryTransaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class InventoryRepository(
    private val firestore: FirebaseFirestore
) {
    private val inventoryCollection = firestore.collection("inventory_items")
    private val transactionsCollection = firestore.collection("inventory_transactions")
    
    fun getInventoryItems(farmId: String): Flow<List<InventoryItem>> = callbackFlow {
        val listener = inventoryCollection
            .whereEqualTo("farmId", farmId)
            .whereEqualTo("isActive", true)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(InventoryItem::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(items)
            }
        
        awaitClose { listener.remove() }
    }
    
    suspend fun addInventoryItem(item: InventoryItem) {
        val itemMap = mapOf(
            "farmId" to item.farmId,
            "name" to item.name,
            "category" to item.category,
            "description" to item.description,
            "currentQuantity" to item.currentQuantity,
            "minimumQuantity" to item.minimumQuantity,
            "maximumQuantity" to item.maximumQuantity,
            "unit" to item.unit,
            "unitPrice" to item.unitPrice,
            "totalValue" to item.totalValue,
            "supplier" to item.supplier,
            "supplierContact" to item.supplierContact,
            "batchNumber" to item.batchNumber,
            "manufacturingDate" to item.manufacturingDate,
            "expiryDate" to item.expiryDate,
            "location" to item.location,
            "barcode" to item.barcode,
            "isActive" to item.isActive,
            "lastRestocked" to item.lastRestocked,
            "lastUsed" to item.lastUsed,
            "usageRate" to item.usageRate,
            "reorderPoint" to item.reorderPoint,
            "notes" to item.notes,
            "imageUrls" to item.imageUrls,
            "createdAt" to item.createdAt,
            "updatedAt" to item.updatedAt
        )
        
        if (item.id.isNotEmpty()) {
            inventoryCollection.document(item.id).set(itemMap).await()
        } else {
            inventoryCollection.add(itemMap).await()
        }
    }
    
    suspend fun updateInventoryItem(item: InventoryItem) {
        val itemMap = mapOf(
            "farmId" to item.farmId,
            "name" to item.name,
            "category" to item.category,
            "description" to item.description,
            "currentQuantity" to item.currentQuantity,
            "minimumQuantity" to item.minimumQuantity,
            "maximumQuantity" to item.maximumQuantity,
            "unit" to item.unit,
            "unitPrice" to item.unitPrice,
            "totalValue" to item.totalValue,
            "supplier" to item.supplier,
            "supplierContact" to item.supplierContact,
            "batchNumber" to item.batchNumber,
            "manufacturingDate" to item.manufacturingDate,
            "expiryDate" to item.expiryDate,
            "location" to item.location,
            "barcode" to item.barcode,
            "isActive" to item.isActive,
            "lastRestocked" to item.lastRestocked,
            "lastUsed" to item.lastUsed,
            "usageRate" to item.usageRate,
            "reorderPoint" to item.reorderPoint,
            "notes" to item.notes,
            "imageUrls" to item.imageUrls,
            "updatedAt" to Date()
        )
        
        inventoryCollection.document(item.id).update(itemMap).await()
    }
    
    suspend fun deleteInventoryItem(itemId: String) {
        // Soft delete by setting isActive to false
        inventoryCollection.document(itemId).update("isActive", false).await()
    }
    
    suspend fun getInventoryItem(itemId: String): InventoryItem? {
        return try {
            val doc = inventoryCollection.document(itemId).get().await()
            doc.toObject(InventoryItem::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun recordTransaction(
        itemId: String,
        type: String,
        quantity: Double,
        reason: String,
        reference: String = "",
        unitPrice: Double = 0.0
    ) {
        val transaction = InventoryTransaction(
            id = UUID.randomUUID().toString(),
            inventoryItemId = itemId,
            type = type,
            quantity = quantity,
            unitPrice = unitPrice,
            totalAmount = quantity * unitPrice,
            reason = reason,
            reference = reference,
            performedBy = "", // Should be current user
            date = Date(),
            notes = "",
            createdAt = Date()
        )
        
        val transactionMap = mapOf(
            "inventoryItemId" to transaction.inventoryItemId,
            "type" to transaction.type,
            "quantity" to transaction.quantity,
            "unitPrice" to transaction.unitPrice,
            "totalAmount" to transaction.totalAmount,
            "reason" to transaction.reason,
            "reference" to transaction.reference,
            "performedBy" to transaction.performedBy,
            "date" to transaction.date,
            "notes" to transaction.notes,
            "createdAt" to transaction.createdAt
        )
        
        transactionsCollection.add(transactionMap).await()
    }
    
    suspend fun getInventorySummary(farmId: String): InventorySummary {
        return try {
            val snapshot = inventoryCollection
                .whereEqualTo("farmId", farmId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val items = snapshot.documents.mapNotNull { doc ->
                doc.toObject(InventoryItem::class.java)?.copy(id = doc.id)
            }
            
            if (items.isEmpty()) {
                return InventorySummary()
            }
            
            val totalItems = items.size
            val totalValue = items.sumOf { it.currentQuantity * it.unitPrice }
            val lowStockItems = items.count { it.currentQuantity <= it.minimumQuantity }
            val outOfStockItems = items.count { it.currentQuantity <= 0 }
            
            // Calculate expiring soon items (within 30 days)
            val thirtyDaysFromNow = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 30)
            }.time
            
            val expiringSoonItems = items.count { item ->
                item.expiryDate != null && item.expiryDate!!.before(thirtyDaysFromNow)
            }
            
            // Category breakdown
            val categoryBreakdown = items.groupBy { it.category }
                .mapValues { it.value.size }
            
            val valueByCategory = items.groupBy { it.category }
                .mapValues { it.value.sumOf { item -> item.currentQuantity * item.unitPrice } }
            
            // Top value items
            val topValueItems = items.sortedByDescending { it.currentQuantity * it.unitPrice }
                .take(5)
            
            // Get recent transactions
            val recentTransactions = getRecentTransactions(farmId, 10)
            
            InventorySummary(
                totalItems = totalItems,
                totalValue = totalValue,
                lowStockItems = lowStockItems,
                expiringSoonItems = expiringSoonItems,
                outOfStockItems = outOfStockItems,
                categoryBreakdown = categoryBreakdown,
                valueByCategory = valueByCategory,
                topValueItems = topValueItems,
                recentTransactions = recentTransactions
            )
        } catch (e: Exception) {
            InventorySummary()
        }
    }
    
    suspend fun getLowStockItems(farmId: String): List<InventoryItem> {
        return try {
            val snapshot = inventoryCollection
                .whereEqualTo("farmId", farmId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val items = snapshot.documents.mapNotNull { doc ->
                doc.toObject(InventoryItem::class.java)?.copy(id = doc.id)
            }
            
            items.filter { it.currentQuantity <= it.minimumQuantity }
                .sortedBy { it.currentQuantity }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getExpiringSoonItems(farmId: String, daysAhead: Int = 30): List<InventoryItem> {
        return try {
            val futureDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, daysAhead)
            }.time
            
            val snapshot = inventoryCollection
                .whereEqualTo("farmId", farmId)
                .whereEqualTo("isActive", true)
                .whereLessThan("expiryDate", futureDate)
                .orderBy("expiryDate", Query.Direction.ASCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(InventoryItem::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getItemsByCategory(farmId: String, category: String): List<InventoryItem> {
        return try {
            val snapshot = inventoryCollection
                .whereEqualTo("farmId", farmId)
                .whereEqualTo("category", category)
                .whereEqualTo("isActive", true)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(InventoryItem::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun getRecentTransactions(farmId: String, limit: Int): List<InventoryTransaction> {
        return try {
            // First get all inventory items for this farm
            val inventorySnapshot = inventoryCollection
                .whereEqualTo("farmId", farmId)
                .get()
                .await()
            
            val inventoryItemIds = inventorySnapshot.documents.map { it.id }
            
            if (inventoryItemIds.isEmpty()) {
                return emptyList()
            }
            
            // Then get transactions for these items
            val transactionSnapshot = transactionsCollection
                .whereIn("inventoryItemId", inventoryItemIds)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            transactionSnapshot.documents.mapNotNull { doc ->
                doc.toObject(InventoryTransaction::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}