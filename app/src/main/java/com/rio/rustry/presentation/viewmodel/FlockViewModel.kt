package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.rio.rustry.domain.model.Flock
import com.rio.rustry.domain.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Flock management operations
 * Handles flock creation, updates, deletion, and statistics
 */
@HiltViewModel
class FlockViewModel @Inject constructor(
    private val farmRepository: FarmRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FlockUiState())
    val uiState: StateFlow<FlockUiState> = _uiState.asStateFlow()
    
    private val _flocks = MutableStateFlow<List<Flock>>(emptyList())
    val flocks: StateFlow<List<Flock>> = _flocks.asStateFlow()
    
    private val _currentFlock = MutableStateFlow<Flock?>(null)
    val currentFlock: StateFlow<Flock?> = _currentFlock.asStateFlow()
    
    private val _flockStatistics = MutableStateFlow<FlockStatistics?>(null)
    val flockStatistics: StateFlow<FlockStatistics?> = _flockStatistics.asStateFlow()
    
    /**
     * Load flocks for a specific farm
     */
    fun loadFlocksByFarm(farmId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                farmRepository.getFlocksByFarm(farmId).collect { flockList ->
                    _flocks.value = flockList
                    updateFlockStatistics(flockList)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load flocks"
                )
            }
        }
    }
    
    /**
     * Add a new flock
     */
    fun addFlock(flock: Flock) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val flockWithTimestamp = flock.copy(
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                farmRepository.addFlock(flockWithTimestamp)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Flock added successfully"
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to add flock"
                )
            }
        }
    }
    
    /**
     * Update an existing flock
     */
    fun updateFlock(flock: Flock) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val updatedFlock = flock.copy(
                    updatedAt = System.currentTimeMillis(),
                    needsSync = true
                )
                
                farmRepository.updateFlock(updatedFlock)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Flock updated successfully"
                )
                
                // Update current flock if it's the one being edited
                if (_currentFlock.value?.id == flock.id) {
                    _currentFlock.value = updatedFlock
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update flock"
                )
            }
        }
    }
    
    /**
     * Delete a flock
     */
    fun deleteFlock(flockId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                farmRepository.deleteFlock(flockId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Flock deleted successfully"
                )
                
                // Clear current flock if it's the one being deleted
                if (_currentFlock.value?.id == flockId) {
                    _currentFlock.value = null
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to delete flock"
                )
            }
        }
    }
    
    /**
     * Get flock by ID
     */
    fun getFlockById(flockId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                farmRepository.getFlockById(flockId).collect { flock ->
                    _currentFlock.value = flock
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load flock"
                )
            }
        }
    }
    
    /**
     * Search flocks by breed
     */
    fun searchFlocksByBreed(breed: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                farmRepository.searchFlocksByBreed(breed).collect { flockList ->
                    _flocks.value = flockList
                    updateFlockStatistics(flockList)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to search flocks"
                )
            }
        }
    }
    
    /**
     * Filter flocks by health status
     */
    fun filterFlocksByHealthStatus(farmId: String, healthStatus: String) {
        viewModelScope.launch {
            try {
                farmRepository.getFlocksByFarm(farmId).collect { allFlocks ->
                    val filteredFlocks = if (healthStatus.isEmpty()) {
                        allFlocks
                    } else {
                        allFlocks.filter { it.healthStatus == healthStatus }
                    }
                    _flocks.value = filteredFlocks
                    updateFlockStatistics(filteredFlocks)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to filter flocks"
                )
            }
        }
    }
    
    /**
     * Filter flocks by production type
     */
    fun filterFlocksByProductionType(farmId: String, productionType: String) {
        viewModelScope.launch {
            try {
                farmRepository.getFlocksByFarm(farmId).collect { allFlocks ->
                    val filteredFlocks = if (productionType.isEmpty()) {
                        allFlocks
                    } else {
                        allFlocks.filter { it.productionType == productionType }
                    }
                    _flocks.value = filteredFlocks
                    updateFlockStatistics(filteredFlocks)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to filter flocks"
                )
            }
        }
    }
    
    /**
     * Upload flock photo
     */
    fun uploadFlockPhoto(flockId: String, photoUri: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isUploadingPhoto = true, error = null)
                
                val photoUrl = farmRepository.uploadFlockPhoto(flockId, photoUri)
                
                // Update flock with new photo URL
                _currentFlock.value?.let { flock ->
                    val updatedFlock = flock.copy(
                        photoUrls = flock.photoUrls + photoUrl,
                        updatedAt = System.currentTimeMillis(),
                        needsSync = true
                    )
                    farmRepository.updateFlock(updatedFlock)
                    _currentFlock.value = updatedFlock
                }
                
                _uiState.value = _uiState.value.copy(
                    isUploadingPhoto = false,
                    successMessage = "Photo uploaded successfully"
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploadingPhoto = false,
                    error = e.message ?: "Failed to upload photo"
                )
            }
        }
    }
    
    /**
     * Validate flock photo using AI
     */
    fun validateFlockPhoto(photoUri: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isValidatingPhoto = true, error = null)
                
                val validationResult = farmRepository.validatePhoto(photoUri)
                
                _uiState.value = _uiState.value.copy(
                    isValidatingPhoto = false,
                    photoValidationResult = validationResult
                )
                
                if (!validationResult.isValid) {
                    _uiState.value = _uiState.value.copy(
                        error = "Photo validation failed: ${validationResult.issues.joinToString(", ")}"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isValidatingPhoto = false,
                    error = e.message ?: "Failed to validate photo"
                )
            }
        }
    }
    
    /**
     * Sync offline changes
     */
    fun syncOfflineChanges() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSyncing = true, error = null)
                
                farmRepository.syncOfflineChanges()
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    successMessage = "Sync completed successfully"
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    error = e.message ?: "Sync failed"
                )
            }
        }
    }
    
    /**
     * Refresh flocks data
     */
    fun refreshFlocks(farmId: String) {
        loadFlocksByFarm(farmId)
    }
    
    /**
     * Sort flocks by criteria
     */
    fun sortFlocks(sortBy: FlockSortCriteria) {
        val currentFlocks = _flocks.value
        val sortedFlocks = when (sortBy) {
            FlockSortCriteria.BREED_ASC -> currentFlocks.sortedBy { it.breed }
            FlockSortCriteria.BREED_DESC -> currentFlocks.sortedByDescending { it.breed }
            FlockSortCriteria.QUANTITY_ASC -> currentFlocks.sortedBy { it.quantity }
            FlockSortCriteria.QUANTITY_DESC -> currentFlocks.sortedByDescending { it.quantity }
            FlockSortCriteria.AGE_ASC -> currentFlocks.sortedBy { it.ageMonths }
            FlockSortCriteria.AGE_DESC -> currentFlocks.sortedByDescending { it.ageMonths }
            FlockSortCriteria.DATE_CREATED_ASC -> currentFlocks.sortedBy { it.createdAt }
            FlockSortCriteria.DATE_CREATED_DESC -> currentFlocks.sortedByDescending { it.createdAt }
            FlockSortCriteria.HEALTH_STATUS -> currentFlocks.sortedBy { it.healthStatus }
        }
        _flocks.value = sortedFlocks
        updateFlockStatistics(sortedFlocks)
    }
    
    /**
     * Update flock statistics
     */
    private fun updateFlockStatistics(flocks: List<Flock>) {
        val statistics = FlockStatistics(
            totalFlocks = flocks.size,
            totalBirds = flocks.sumOf { it.quantity },
            healthyFlocks = flocks.count { it.healthStatus == "HEALTHY" },
            sickFlocks = flocks.count { it.healthStatus == "SICK" },
            quarantineFlocks = flocks.count { it.healthStatus == "QUARANTINE" },
            averageAge = if (flocks.isNotEmpty()) flocks.map { it.ageMonths }.average() else 0.0,
            breedDistribution = flocks.groupBy { it.breed }.mapValues { it.value.size },
            productionTypeDistribution = flocks.groupBy { it.productionType }.mapValues { it.value.size },
            healthStatusDistribution = flocks.groupBy { it.healthStatus }.mapValues { it.value.size }
        )
        _flockStatistics.value = statistics
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    /**
     * Clear photo validation result
     */
    fun clearPhotoValidationResult() {
        _uiState.value = _uiState.value.copy(photoValidationResult = null)
    }
    
    /**
     * Get current user ID from Firebase Auth
     */
    fun getCurrentUserId(): String {
        return firebaseAuth.currentUser?.uid ?: ""
    }
    
    /**
     * Check if user has flocks
     */
    fun hasFlocks(): Boolean {
        return _flocks.value.isNotEmpty()
    }
    
    /**
     * Get flocks count
     */
    fun getFlocksCount(): Int {
        return _flocks.value.size
    }
    
    /**
     * Get total birds count
     */
    fun getTotalBirdsCount(): Int {
        return _flocks.value.sumOf { it.quantity }
    }
    
    /**
     * Get healthy flocks count
     */
    fun getHealthyFlocksCount(): Int {
        return _flocks.value.count { it.healthStatus == "HEALTHY" }
    }
    
    /**
     * Get sick flocks count
     */
    fun getSickFlocksCount(): Int {
        return _flocks.value.count { it.healthStatus == "SICK" }
    }
    
    /**
     * Get flocks for sale
     */
    fun getFlocksForSale(): List<Flock> {
        return _flocks.value.filter { it.isForSale }
    }
    
    /**
     * Mark flock for sale
     */
    fun markFlockForSale(flockId: String, isForSale: Boolean, salePrice: Double = 0.0) {
        viewModelScope.launch {
            try {
                _currentFlock.value?.let { flock ->
                    if (flock.id == flockId) {
                        val updatedFlock = flock.copy(
                            isForSale = isForSale,
                            salePrice = salePrice,
                            updatedAt = System.currentTimeMillis(),
                            needsSync = true
                        )
                        farmRepository.updateFlock(updatedFlock)
                        _currentFlock.value = updatedFlock
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update flock sale status"
                )
            }
        }
    }
}

/**
 * UI State for Flock operations
 */
data class FlockUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val isValidatingPhoto: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val photoValidationResult: com.rio.rustry.domain.model.PhotoValidationResult? = null
)

/**
 * Flock statistics data class
 */
data class FlockStatistics(
    val totalFlocks: Int = 0,
    val totalBirds: Int = 0,
    val healthyFlocks: Int = 0,
    val sickFlocks: Int = 0,
    val quarantineFlocks: Int = 0,
    val averageAge: Double = 0.0,
    val breedDistribution: Map<String, Int> = emptyMap(),
    val productionTypeDistribution: Map<String, Int> = emptyMap(),
    val healthStatusDistribution: Map<String, Int> = emptyMap()
)

/**
 * Sort criteria for flocks
 */
enum class FlockSortCriteria {
    BREED_ASC,
    BREED_DESC,
    QUANTITY_ASC,
    QUANTITY_DESC,
    AGE_ASC,
    AGE_DESC,
    DATE_CREATED_ASC,
    DATE_CREATED_DESC,
    HEALTH_STATUS
}