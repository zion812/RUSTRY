// generated/phase2/app/src/main/java/com/rio/rustry/listing/MyListingsViewModel.kt

package com.rio.rustry.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.domain.usecase.DeleteFowlListingUseCase
import com.rio.rustry.domain.usecase.GetMyListingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyListingsViewModel @Inject constructor(
    private val getMyListingsUseCase: GetMyListingsUseCase,
    private val deleteFowlListingUseCase: DeleteFowlListingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyListingsUiState())
    val uiState: StateFlow<MyListingsUiState> = _uiState.asStateFlow()

    fun loadMyListings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                getMyListingsUseCase().collect { result ->
                    result.fold(
                        onSuccess = { fowls ->
                            _uiState.update { 
                                it.copy(
                                    fowls = fowls,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to load listings"
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

    fun deleteListing(fowlId: String) {
        viewModelScope.launch {
            try {
                deleteFowlListingUseCase(fowlId).fold(
                    onSuccess = {
                        // Remove from current list
                        _uiState.update { 
                            it.copy(fowls = it.fowls.filter { fowl -> fowl.id != fowlId })
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(error = error.message ?: "Failed to delete listing")
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Unknown error occurred")
                }
            }
        }
    }

    fun retry() {
        loadMyListings()
    }
}

data class MyListingsUiState(
    val fowls: List<Fowl> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)