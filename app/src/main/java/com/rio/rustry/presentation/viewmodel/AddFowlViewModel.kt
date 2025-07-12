package com.rio.rustry.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.data.repository.AuthRepository
import com.rio.rustry.data.repository.FowlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddFowlViewModel @Inject constructor(
    private val fowlRepository: FowlRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddFowlUiState())
    val uiState: StateFlow<AddFowlUiState> = _uiState.asStateFlow()
    
    init {
        loadNonTraceableCount()
    }
    
    private fun loadNonTraceableCount() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                fowlRepository.getNonTraceableCount(currentUser.uid).fold(
                    onSuccess = { count ->
                        _uiState.value = _uiState.value.copy(nonTraceableCount = count)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to load fowl count: ${error.message}"
                        )
                    }
                )
            }
        }
    }
    
    fun addFowl(
        breed: String,
        description: String,
        location: String,
        price: Double,
        isTraceable: Boolean,
        parentId: String?,
        dateOfBirth: Date,
        imageUri: Uri?,
        proofUri: Uri?
    ) {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    error = "User not authenticated"
                )
                return@launch
            }
            
            // Validate inputs
            if (breed.isBlank()) {
                _uiState.value = _uiState.value.copy(error = "Please select a breed")
                return@launch
            }
            
            if (location.isBlank()) {
                _uiState.value = _uiState.value.copy(error = "Please enter a location")
                return@launch
            }
            
            if (imageUri == null) {
                _uiState.value = _uiState.value.copy(error = "Please select a fowl photo")
                return@launch
            }
            
            // Check non-traceable limit
            if (!isTraceable && _uiState.value.nonTraceableCount >= 5) {
                _uiState.value = _uiState.value.copy(
                    error = "You have reached the limit of 5 non-traceable fowls. Please make this fowl traceable or remove some existing non-traceable fowls."
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Get user profile for owner name
                val userProfile = authRepository.getUserProfile(currentUser.uid).getOrNull()
                val ownerName = userProfile?.name ?: "Unknown"
                
                // Upload main image
                val imageUploadResult = fowlRepository.uploadImage(
                    imageUri,
                    "fowls/${currentUser.uid}/${UUID.randomUUID()}.jpg"
                )
                
                if (imageUploadResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to upload image: ${imageUploadResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }
                
                val imageUrl = imageUploadResult.getOrNull()!!
                val imageUrls = listOf(imageUrl)
                
                // Upload proof image if provided
                val proofUrls = if (isTraceable && proofUri != null) {
                    val proofUploadResult = fowlRepository.uploadImage(
                        proofUri,
                        "fowls/${currentUser.uid}/proof/${UUID.randomUUID()}.jpg"
                    )
                    
                    if (proofUploadResult.isSuccess) {
                        listOf(proofUploadResult.getOrNull()!!)
                    } else {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
                
                // Create fowl object
                val fowl = Fowl(
                    ownerId = currentUser.uid,
                    ownerName = ownerName,
                    breed = breed,
                    dateOfBirth = dateOfBirth,
                    isTraceable = isTraceable,
                    parentIds = if (isTraceable && !parentId.isNullOrBlank()) listOf(parentId) else emptyList(),
                    imageUrls = imageUrls,
                    proofImageUrls = proofUrls,
                    description = description,
                    location = location,
                    price = price,
                    isAvailable = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                // Save fowl to Firestore
                fowlRepository.addFowl(fowl).fold(
                    onSuccess = { fowlId ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                        
                        // Update non-traceable count if needed
                        if (!isTraceable) {
                            _uiState.value = _uiState.value.copy(
                                nonTraceableCount = _uiState.value.nonTraceableCount + 1
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to add fowl: ${error.message}"
                        )
                    }
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unexpected error: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}

data class AddFowlUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val nonTraceableCount: Int = 0
)