package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for Farm management
 */
class FarmViewModel : ViewModel() {
    
    private val _addFarmState = MutableStateFlow(AddFarmState())
    val addFarmState: StateFlow<AddFarmState> = _addFarmState.asStateFlow()
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun updateFarmName(name: String) {
        _addFarmState.value = _addFarmState.value.copy(farmName = name)
    }
    
    fun updateLocation(location: String) {
        _addFarmState.value = _addFarmState.value.copy(location = location)
    }
    
    fun updateSize(size: String) {
        _addFarmState.value = _addFarmState.value.copy(size = size)
    }
    
    fun updateOwnershipDetails(details: String) {
        _addFarmState.value = _addFarmState.value.copy(ownershipDetails = details)
    }
    
    fun updatePhotoUri(uri: String) {
        _addFarmState.value = _addFarmState.value.copy(photoUri = uri)
    }
    
    fun addFarm() {
        _addFarmState.value = _addFarmState.value.copy(isLoading = true)
        // Mock farm addition
        _uiState.value = _uiState.value.copy(successMessage = "Farm added successfully")
        _addFarmState.value = _addFarmState.value.copy(isLoading = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

data class AddFarmState(
    val farmName: String = "",
    val location: String = "",
    val size: String = "",
    val ownershipDetails: String = "",
    val photoUri: String = "",
    val isLoading: Boolean = false,
    val errors: Map<String, String> = emptyMap()
)

data class UiState(
    val error: String? = null,
    val successMessage: String? = null
)