package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.*
import com.rio.rustry.data.repository.TransferRepository
import com.rio.rustry.data.repository.FowlRepository
import com.rio.rustry.data.repository.AuthRepository
import com.rio.rustry.data.repository.HealthRepository
import com.rio.rustry.utils.CertificateGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferConfirmationViewModel @Inject constructor(
    private val transferRepository: TransferRepository,
    private val fowlRepository: FowlRepository,
    private val authRepository: AuthRepository,
    private val healthRepository: HealthRepository,
    private val certificateGenerator: CertificateGenerator
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TransferConfirmationUiState())
    val uiState: StateFlow<TransferConfirmationUiState> = _uiState.asStateFlow()
    
    private val _confirmationResult = MutableSharedFlow<ConfirmationResult>()
    val confirmationResult: SharedFlow<ConfirmationResult> = _confirmationResult.asSharedFlow()
    
    fun loadTransfer(transferId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val transfer = transferRepository.getTransfer(transferId).getOrNull()
                if (transfer == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Transfer not found",
                        isLoading = false
                    )
                    return@launch
                }
                
                // Load fowl information
                val fowl = fowlRepository.getFowlById(transfer.fowlId).getOrNull()
                
                // Load current user
                val currentFirebaseUser = authRepository.getCurrentUser()
                val currentUser = currentFirebaseUser?.let { 
                    authRepository.getUserProfile(it.uid).getOrNull() 
                }
                
                // Determine user role in this transfer
                val userRole = when (currentUser?.id) {
                    transfer.fromUserId -> UserRole.SELLER
                    transfer.toUserId -> UserRole.BUYER
                    else -> null
                }
                
                // Load health summary for the fowl
                val healthSummary = healthRepository.getHealthSummary(transfer.fowlId).getOrNull()
                    ?: HealthSummary(fowlId = transfer.fowlId)
                
                _uiState.value = _uiState.value.copy(
                    transfer = transfer,
                    fowl = fowl,
                    currentUser = currentUser,
                    userRole = userRole,
                    healthSummary = healthSummary,
                    canConfirm = canUserConfirm(transfer, currentUser, userRole),
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load transfer",
                    isLoading = false
                )
            }
        }
    }
    
    fun confirmTransfer(verificationCode: String = "", digitalSignature: String = "") {
        viewModelScope.launch {
            val state = _uiState.value
            val transfer = state.transfer ?: return@launch
            val currentUser = state.currentUser ?: return@launch
            val userRole = state.userRole ?: return@launch
            
            // Validate verification code if required
            if (transfer.requiresVerification && transfer.verificationCode.isNotEmpty()) {
                if (verificationCode != transfer.verificationCode) {
                    _uiState.value = _uiState.value.copy(
                        error = "Invalid verification code"
                    )
                    return@launch
                }
            }
            
            _uiState.value = _uiState.value.copy(isProcessing = true)
            
            try {
                val result = transferRepository.confirmTransfer(
                    transferId = transfer.id,
                    userId = currentUser.id,
                    userRole = userRole,
                    verificationData = digitalSignature
                )
                
                if (result.isSuccess) {
                    // Check if transfer is now completed
                    val updatedTransfer = transferRepository.getTransfer(transfer.id).getOrNull()
                    if (updatedTransfer?.status == TransferStatus.COMPLETED) {
                        // Generate digital certificate
                        generateCertificate(updatedTransfer)
                    }
                    
                    _confirmationResult.emit(ConfirmationResult.Success)
                } else {
                    _confirmationResult.emit(ConfirmationResult.Error(result.exceptionOrNull()?.message ?: "Failed to confirm transfer"))
                }
                
            } catch (e: Exception) {
                _confirmationResult.emit(ConfirmationResult.Error(e.message ?: "Unknown error occurred"))
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }
    
    fun cancelTransfer(reason: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val transfer = state.transfer ?: return@launch
            val currentUser = state.currentUser ?: return@launch
            
            _uiState.value = _uiState.value.copy(isProcessing = true)
            
            try {
                val result = transferRepository.cancelTransfer(
                    transferId = transfer.id,
                    userId = currentUser.id,
                    reason = reason
                )
                
                if (result.isSuccess) {
                    _confirmationResult.emit(ConfirmationResult.Cancelled)
                } else {
                    _confirmationResult.emit(ConfirmationResult.Error(result.exceptionOrNull()?.message ?: "Failed to cancel transfer"))
                }
                
            } catch (e: Exception) {
                _confirmationResult.emit(ConfirmationResult.Error(e.message ?: "Unknown error occurred"))
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }
    
    private suspend fun generateCertificate(transfer: OwnershipTransfer) {
        try {
            val fowl = _uiState.value.fowl ?: return
            val healthSummary = _uiState.value.healthSummary
            
            val certificateResult = transferRepository.generateDigitalCertificate(
                transfer = transfer,
                fowl = fowl,
                healthSummary = healthSummary
            )
            
            if (certificateResult.isSuccess) {
                val certificateId = certificateResult.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    certificateGenerated = true,
                    certificateId = certificateId
                )
            }
        } catch (e: Exception) {
            // Log error but don't fail the confirmation
        }
    }
    
    private fun canUserConfirm(transfer: OwnershipTransfer, user: User?, userRole: UserRole?): Boolean {
        if (user == null || userRole == null) return false
        
        return when (userRole) {
            UserRole.SELLER -> {
                transfer.fromUserId == user.id && 
                transfer.sellerConfirmedAt == null &&
                transfer.status in listOf(TransferStatus.PENDING, TransferStatus.BUYER_CONFIRMED)
            }
            UserRole.BUYER -> {
                transfer.toUserId == user.id && 
                transfer.buyerConfirmedAt == null &&
                transfer.status in listOf(TransferStatus.PENDING, TransferStatus.SELLER_CONFIRMED)
            }
            else -> false
        }
    }
    
    fun updateVerificationCode(code: String) {
        _uiState.value = _uiState.value.copy(verificationCode = code)
    }
    
    fun updateDigitalSignature(signature: String) {
        _uiState.value = _uiState.value.copy(digitalSignature = signature)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class TransferConfirmationUiState(
    val transfer: OwnershipTransfer? = null,
    val fowl: Fowl? = null,
    val currentUser: User? = null,
    val userRole: UserRole? = null,
    val healthSummary: HealthSummary = HealthSummary(),
    val canConfirm: Boolean = false,
    val verificationCode: String = "",
    val digitalSignature: String = "",
    val certificateGenerated: Boolean = false,
    val certificateId: String = "",
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null
)

sealed class ConfirmationResult {
    object Success : ConfirmationResult()
    object Cancelled : ConfirmationResult()
    data class Error(val message: String) : ConfirmationResult()
}