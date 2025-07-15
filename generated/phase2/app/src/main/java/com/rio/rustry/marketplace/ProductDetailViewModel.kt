// generated/phase2/app/src/main/java/com/rio/rustry/marketplace/ProductDetailViewModel.kt

package com.rio.rustry.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.domain.usecase.GetFowlDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getFowlDetailUseCase: GetFowlDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun loadFowlDetail(fowlId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                getFowlDetailUseCase(fowlId).collect { result ->
                    result.fold(
                        onSuccess = { fowlDetail ->
                            _uiState.update { 
                                it.copy(
                                    fowl = fowlDetail.fowl,
                                    lineage = fowlDetail.lineage,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to load fowl details"
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
}

data class ProductDetailUiState(
    val fowl: Fowl? = null,
    val lineage: List<Fowl> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class FowlDetail(
    val fowl: Fowl,
    val lineage: List<Fowl>
)