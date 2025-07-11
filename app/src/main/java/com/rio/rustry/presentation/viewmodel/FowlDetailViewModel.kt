package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.data.repository.FowlRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FowlDetailViewModel : ViewModel() {
    
    private val fowlRepository = FowlRepository(
        FirebaseFirestore.getInstance(),
        FirebaseStorage.getInstance()
    )
    
    private val _uiState = MutableStateFlow(FowlDetailUiState())
    val uiState: StateFlow<FowlDetailUiState> = _uiState.asStateFlow()
    
    private val _fowl = MutableStateFlow<Fowl?>(null)
    val fowl: StateFlow<Fowl?> = _fowl.asStateFlow()
    
    fun loadFowl(fowlId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            fowlRepository.getFowlById(fowlId).fold(
                onSuccess = { fowl ->
                    _fowl.value = fowl
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load fowl details"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class FowlDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)