// generated/phase1/app/src/main/java/com/rio/rustry/ui/theme/ConsentViewModel.kt
package com.rio.rustry.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.auth.GdprConsentManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConsentUiState(
    val isLoading: Boolean = false,
    val consentRecorded: Boolean = false,
    val deletionRequested: Boolean = false,
    val errorMessage: String = ""
)

@HiltViewModel
class ConsentViewModel @Inject constructor(
    private val gdprConsentManager: GdprConsentManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ConsentUiState())
    val uiState: StateFlow<ConsentUiState> = _uiState.asStateFlow()
    
    /**
     * Records user consent for GDPR compliance
     */
    fun recordConsent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = ""
            )
            
            gdprConsentManager.recordConsent()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        consentRecorded = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to record consent"
                    )
                }
        }
    }
    
    /**
     * Requests data deletion
     */
    fun requestDataDeletion() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = ""
            )
            
            gdprConsentManager.requestDeletion()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        deletionRequested = true,
                        errorMessage = "Data deletion request submitted successfully. You will be logged out."
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to request data deletion"
                    )
                }
        }
    }
}