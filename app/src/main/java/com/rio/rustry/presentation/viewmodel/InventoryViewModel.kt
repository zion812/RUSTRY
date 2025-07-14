package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.InventoryItem
import com.rio.rustry.data.model.InventorySummary
import com.rio.rustry.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {
    
    private val _inventoryItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryItems: StateFlow<List<InventoryItem>> = _inventoryItems.asStateFlow()
    
    private val _allInventoryItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _totalValue = MutableStateFlow(0.0)
    val totalValue: StateFlow<Double> = _totalValue.asStateFlow()
    
    private val _lowStockItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val lowStockItems: StateFlow<List<InventoryItem>> = _lowStockItems.asStateFlow()
    
    private val _inventorySummary = MutableStateFlow<InventorySummary?>(null)
    val inventorySummary: StateFlow<InventorySummary?> = _inventorySummary.asStateFlow()
    
    fun loadInventoryItems(farmId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                inventoryRepository.getInventoryItems(farmId).collect { items ->
                    _allInventoryItems.value = items
                    _inventoryItems.value = items.sortedBy { it.name }
                    calculateMetrics(items)
                }
            } catch (e: Exception) {
                _error.value = "Failed to load inventory items: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addInventoryItem(farmId: String, item: InventoryItem) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val itemWithFarmId = item.copy(
                    farmId = farmId,
                    id = UUID.randomUUID().toString(),
                    totalValue = item.currentQuantity * item.unitPrice,
                    reorderPoint = item.minimumQuantity * 1.2, // 20% above minimum
                    createdAt = Date(),
                    updatedAt = Date()
                )
                inventoryRepository.addInventoryItem(itemWithFarmId)
                loadInventoryItems(farmId) // Refresh the list
            } catch (e: Exception) {
                _error.value = "Failed to add inventory item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val updatedItem = item.copy(
                    updatedAt = Date(),
                    totalValue = item.currentQuantity * item.unitPrice
                )
                inventoryRepository.updateInventoryItem(updatedItem)
                loadInventoryItems(item.farmId) // Refresh the list
            } catch (e: Exception) {
                _error.value = "Failed to update inventory item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteInventoryItem(itemId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                inventoryRepository.deleteInventoryItem(itemId)
                // Remove from current list
                _inventoryItems.value = _inventoryItems.value.filter { it.id != itemId }
                _allInventoryItems.value = _allInventoryItems.value.filter { it.id != itemId }
                calculateMetrics(_allInventoryItems.value)
            } catch (e: Exception) {
                _error.value = "Failed to delete inventory item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateStock(itemId: String, newQuantity: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val item = _allInventoryItems.value.find { it.id == itemId }
                if (item != null) {
                    val updatedItem = item.copy(
                        currentQuantity = newQuantity,
                        totalValue = newQuantity * item.unitPrice,
                        lastRestocked = if (newQuantity > item.currentQuantity) Date() else item.lastRestocked,
                        lastUsed = if (newQuantity < item.currentQuantity) Date() else item.lastUsed,
                        updatedAt = Date()
                    )
                    inventoryRepository.updateInventoryItem(updatedItem)
                    
                    // Record transaction
                    inventoryRepository.recordTransaction(
                        itemId = itemId,
                        type = when {
                            newQuantity > item.currentQuantity -> "IN"
                            newQuantity < item.currentQuantity -> "OUT"
                            else -> "ADJUSTMENT"
                        },
                        quantity = kotlin.math.abs(newQuantity - item.currentQuantity),
                        reason = "Stock update"
                    )
                    
                    loadInventoryItems(item.farmId) // Refresh the list
                }
            } catch (e: Exception) {
                _error.value = "Failed to update stock: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun filterByCategory(category: String) {
        val allItems = _allInventoryItems.value
        val filteredItems = if (category == "All") {
            allItems
        } else {
            allItems.filter { it.category == category }
        }
        
        _inventoryItems.value = filteredItems.sortedBy { it.name }
        calculateMetrics(filteredItems)
    }
    
    fun searchItems(query: String) {
        val allItems = _allInventoryItems.value
        val filteredItems = if (query.isBlank()) {
            allItems
        } else {
            allItems.filter { 
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.supplier.contains(query, ignoreCase = true)
            }
        }
        
        _inventoryItems.value = filteredItems.sortedBy { it.name }
    }
    
    fun loadInventorySummary(farmId: String) {
        viewModelScope.launch {
            try {
                val summary = inventoryRepository.getInventorySummary(farmId)
                _inventorySummary.value = summary
            } catch (e: Exception) {
                _error.value = "Failed to load inventory summary: ${e.message}"
            }
        }
    }
    
    fun getExpiringSoonItems(farmId: String, daysAhead: Int = 30) {
        viewModelScope.launch {
            try {
                val items = inventoryRepository.getExpiringSoonItems(farmId, daysAhead)
                // Handle expiring items (could emit to a separate StateFlow)
            } catch (e: Exception) {
                _error.value = "Failed to load expiring items: ${e.message}"
            }
        }
    }
    
    private fun calculateMetrics(items: List<InventoryItem>) {
        val total = items.sumOf { it.currentQuantity * it.unitPrice }
        _totalValue.value = total
        
        val lowStock = items.filter { it.currentQuantity <= it.minimumQuantity }
        _lowStockItems.value = lowStock
    }
    
    fun clearError() {
        _error.value = null
    }
}