package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.SaleRecord
import com.rio.rustry.data.model.SalesSummary
import com.rio.rustry.data.repository.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class SalesViewModel(
    private val salesRepository: SalesRepository
) : ViewModel() {
    
    private val _salesRecords = MutableStateFlow<List<SaleRecord>>(emptyList())
    val salesRecords: StateFlow<List<SaleRecord>> = _salesRecords.asStateFlow()
    
    private val _allSalesRecords = MutableStateFlow<List<SaleRecord>>(emptyList())
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _totalRevenue = MutableStateFlow(0.0)
    val totalRevenue: StateFlow<Double> = _totalRevenue.asStateFlow()
    
    private val _monthlyRevenue = MutableStateFlow(0.0)
    val monthlyRevenue: StateFlow<Double> = _monthlyRevenue.asStateFlow()
    
    private val _salesSummary = MutableStateFlow<SalesSummary?>(null)
    val salesSummary: StateFlow<SalesSummary?> = _salesSummary.asStateFlow()
    
    fun loadSalesRecords(farmId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                salesRepository.getSalesRecords(farmId).collect { records ->
                    _allSalesRecords.value = records
                    _salesRecords.value = records.sortedByDescending { it.saleDate }
                    calculateRevenue(records)
                }
            } catch (e: Exception) {
                _error.value = "Failed to load sales records: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addSaleRecord(farmId: String, record: SaleRecord) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val recordWithFarmId = record.copy(
                    farmId = farmId,
                    id = UUID.randomUUID().toString(),
                    netAmount = record.salePrice - record.taxAmount - record.commission,
                    createdAt = Date(),
                    updatedAt = Date()
                )
                salesRepository.addSaleRecord(recordWithFarmId)
                loadSalesRecords(farmId) // Refresh the list
            } catch (e: Exception) {
                _error.value = "Failed to add sale record: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateSaleRecord(record: SaleRecord) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val updatedRecord = record.copy(
                    updatedAt = Date(),
                    netAmount = record.salePrice - record.taxAmount - record.commission
                )
                salesRepository.updateSaleRecord(updatedRecord)
                loadSalesRecords(record.farmId) // Refresh the list
            } catch (e: Exception) {
                _error.value = "Failed to update sale record: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteSaleRecord(recordId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                salesRepository.deleteSaleRecord(recordId)
                // Remove from current list
                _salesRecords.value = _salesRecords.value.filter { it.id != recordId }
                _allSalesRecords.value = _allSalesRecords.value.filter { it.id != recordId }
                calculateRevenue(_allSalesRecords.value)
            } catch (e: Exception) {
                _error.value = "Failed to delete sale record: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun filterByPeriod(period: String) {
        val allRecords = _allSalesRecords.value
        val filteredRecords = when (period) {
            "This Month" -> {
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)
                
                allRecords.filter { record ->
                    calendar.time = record.saleDate
                    calendar.get(Calendar.MONTH) == currentMonth && 
                    calendar.get(Calendar.YEAR) == currentYear
                }
            }
            "Last 3 Months" -> {
                val threeMonthsAgo = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -3)
                }.time
                
                allRecords.filter { it.saleDate.after(threeMonthsAgo) }
            }
            "This Year" -> {
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                
                allRecords.filter { record ->
                    calendar.time = record.saleDate
                    calendar.get(Calendar.YEAR) == currentYear
                }
            }
            else -> allRecords // "All Time"
        }
        
        _salesRecords.value = filteredRecords.sortedByDescending { it.saleDate }
        calculateRevenue(filteredRecords)
    }
    
    fun loadSalesSummary(farmId: String) {
        viewModelScope.launch {
            try {
                val summary = salesRepository.getSalesSummary(farmId)
                _salesSummary.value = summary
            } catch (e: Exception) {
                _error.value = "Failed to load sales summary: ${e.message}"
            }
        }
    }
    
    private fun calculateRevenue(records: List<SaleRecord>) {
        val total = records.sumOf { it.salePrice }
        _totalRevenue.value = total
        
        // Calculate monthly revenue (current month)
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        val monthlyTotal = records.filter { record ->
            calendar.time = record.saleDate
            calendar.get(Calendar.MONTH) == currentMonth && 
            calendar.get(Calendar.YEAR) == currentYear
        }.sumOf { it.salePrice }
        
        _monthlyRevenue.value = monthlyTotal
    }
    
    fun clearError() {
        _error.value = null
    }
}