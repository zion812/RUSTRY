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

class MarketplaceViewModel : ViewModel() {
    
    private val fowlRepository = FowlRepository(
        FirebaseFirestore.getInstance(),
        FirebaseStorage.getInstance()
    )
    
    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()
    
    private val _fowls = MutableStateFlow<List<Fowl>>(emptyList())
    val fowls: StateFlow<List<Fowl>> = _fowls.asStateFlow()
    
    private val _selectedFowl = MutableStateFlow<Fowl?>(null)
    val selectedFowl: StateFlow<Fowl?> = _selectedFowl.asStateFlow()
    
    init {
        loadFowls()
    }
    
    fun loadFowls() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            fowlRepository.getAllFowls().fold(
                onSuccess = { fowlList ->
                    _fowls.value = fowlList
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun filterFowls(breed: String? = null, location: String? = null, isTraceable: Boolean? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            fowlRepository.getFowlsByFilter(breed, location, isTraceable).fold(
                onSuccess = { fowlList ->
                    _fowls.value = fowlList
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun selectFowl(fowl: Fowl) {
        _selectedFowl.value = fowl
    }
    
    fun clearSelectedFowl() {
        _selectedFowl.value = null
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class MarketplaceUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)