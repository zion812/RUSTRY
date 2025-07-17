package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.HealthRecord
import com.rio.rustry.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class HealthViewModel(
    private val healthRepository: HealthRepository
) : ViewModel() {
    
    private val _healthRecords = MutableStateFlow<List<HealthRecord>>(emptyList())
    val healthRecords: StateFlow<List<HealthRecord>> = _healthRecords.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadHealthRecords(fowlId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                healthRepository.getHealthRecords(fowlId).collect { records ->
                    _healthRecords.value = records.sortedByDescending { it.recordDate }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load health records: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addHealthRecord(fowlId: String, record: HealthRecord) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val recordWithFowlId = record.copy(
                    fowlId = fowlId,
                    id = UUID.randomUUID().toString(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                healthRepository.addHealthRecord(recordWithFowlId)
                loadHealthRecords(fowlId) // Refresh the list
            } catch (e: Exception) {
                _error.value = "Failed to add health record: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateHealthRecord(record: HealthRecord) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val updatedRecord = record.copy(updatedAt = System.currentTimeMillis())
                healthRepository.updateHealthRecord(updatedRecord)
                loadHealthRecords(record.fowlId) // Refresh the list
            } catch (e: Exception) {
                _error.value = "Failed to update health record: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteHealthRecord(recordId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                healthRepository.deleteHealthRecord(recordId)
                // Remove from current list
                _healthRecords.value = _healthRecords.value.filter { it.id != recordId }
            } catch (e: Exception) {
                _error.value = "Failed to delete health record: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}