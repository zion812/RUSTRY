package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.*
import com.rio.rustry.data.repository.TransferRepository
import com.rio.rustry.data.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CertificateViewModel(
    private val transferRepository: TransferRepository = TransferRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CertificateUiState())
    val uiState: StateFlow<CertificateUiState> = _uiState.asStateFlow()
    
    fun loadCertificate(certificateId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val certificate = transferRepository.getCertificate(certificateId).getOrNull()
                if (certificate != null) {
                    _uiState.value = _uiState.value.copy(
                        certificate = certificate,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Certificate not found",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load certificate",
                    isLoading = false
                )
            }
        }
    }
    
    fun verifyCertificate(certificateId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifying = true)
            
            try {
                val result = transferRepository.verifyCertificate(certificateId)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isVerified = true,
                        isVerifying = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Certificate verification failed",
                        isVerifying = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Verification failed",
                    isVerifying = false
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class CertificateUiState(
    val certificate: DigitalCertificate? = null,
    val isLoading: Boolean = false,
    val isVerifying: Boolean = false,
    val isVerified: Boolean = false,
    val error: String? = null
)