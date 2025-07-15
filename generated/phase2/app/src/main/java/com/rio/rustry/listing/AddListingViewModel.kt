// generated/phase2/app/src/main/java/com/rio/rustry/listing/AddListingViewModel.kt

package com.rio.rustry.listing

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.AgeGroup
import com.rio.rustry.data.model.Breed
import com.rio.rustry.domain.usecase.AddFowlListingUseCase
import com.rio.rustry.domain.usecase.GetFowlDetailUseCase
import com.rio.rustry.domain.usecase.UpdateFowlListingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddListingViewModel @Inject constructor(
    private val addFowlListingUseCase: AddFowlListingUseCase,
    private val updateFowlListingUseCase: UpdateFowlListingUseCase,
    private val getFowlDetailUseCase: GetFowlDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddListingUiState())
    val uiState: StateFlow<AddListingUiState> = _uiState.asStateFlow()

    fun loadFowl(fowlId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                getFowlDetailUseCase(fowlId).collect { result ->
                    result.fold(
                        onSuccess = { fowlDetail ->
                            val fowl = fowlDetail.fowl
                            _uiState.update { 
                                it.copy(
                                    fowlId = fowl.id,
                                    breed = fowl.breed,
                                    ageGroup = fowl.ageGroup,
                                    price = fowl.price.toString(),
                                    isTraceable = fowl.isTraceable,
                                    existingImages = fowl.images,
                                    isLoading = false
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to load fowl"
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun updateBreed(breed: Breed) {
        _uiState.update { 
            it.copy(
                breed = breed,
                breedError = null
            )
        }
    }

    fun updateAgeGroup(ageGroup: AgeGroup) {
        _uiState.update { 
            it.copy(
                ageGroup = ageGroup,
                ageGroupError = null
            )
        }
    }

    fun updatePrice(price: String) {
        _uiState.update { 
            it.copy(
                price = price,
                priceError = null
            )
        }
    }

    fun updateTraceability(isTraceable: Boolean) {
        _uiState.update { it.copy(isTraceable = isTraceable) }
    }

    fun addImages(uris: List<Uri>) {
        _uiState.update { 
            it.copy(
                images = it.images + uris,
                imagesError = null
            )
        }
    }

    fun removeImage(uri: Uri) {
        _uiState.update { 
            it.copy(images = it.images - uri)
        }
    }

    fun saveListing() {
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val currentState = _uiState.value
                val listingData = FowlListingData(
                    breed = currentState.breed!!,
                    ageGroup = currentState.ageGroup!!,
                    price = currentState.price.toDouble(),
                    images = currentState.images,
                    existingImages = currentState.existingImages,
                    isTraceable = currentState.isTraceable
                )

                val result = if (currentState.fowlId != null) {
                    updateFowlListingUseCase(currentState.fowlId, listingData)
                } else {
                    addFowlListingUseCase(listingData)
                }

                result.fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isSuccess = true
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to save listing"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        val currentState = _uiState.value
        var hasErrors = false

        val breedError = if (currentState.breed == null) {
            hasErrors = true
            "Please select a breed"
        } else null

        val ageGroupError = if (currentState.ageGroup == null) {
            hasErrors = true
            "Please select an age group"
        } else null

        val priceError = if (currentState.price.isBlank()) {
            hasErrors = true
            "Please enter a price"
        } else {
            try {
                val price = currentState.price.toDouble()
                if (price <= 0) {
                    hasErrors = true
                    "Price must be greater than 0"
                } else null
            } catch (e: NumberFormatException) {
                hasErrors = true
                "Please enter a valid price"
            }
        }

        val imagesError = if (currentState.images.isEmpty() && currentState.existingImages.isEmpty()) {
            hasErrors = true
            "Please add at least one image"
        } else null

        _uiState.update { 
            it.copy(
                breedError = breedError,
                ageGroupError = ageGroupError,
                priceError = priceError,
                imagesError = imagesError
            )
        }

        return !hasErrors
    }
}

data class AddListingUiState(
    val fowlId: String? = null,
    val breed: Breed? = null,
    val ageGroup: AgeGroup? = null,
    val price: String = "",
    val images: List<Uri> = emptyList(),
    val existingImages: List<String> = emptyList(),
    val isTraceable: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val breedError: String? = null,
    val ageGroupError: String? = null,
    val priceError: String? = null,
    val imagesError: String? = null
)

data class FowlListingData(
    val breed: Breed,
    val ageGroup: AgeGroup,
    val price: Double,
    val images: List<Uri>,
    val existingImages: List<String>,
    val isTraceable: Boolean
)