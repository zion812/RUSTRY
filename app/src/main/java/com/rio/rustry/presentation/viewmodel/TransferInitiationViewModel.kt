package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.*
import com.rio.rustry.data.repository.TransferRepository
import com.rio.rustry.data.repository.FowlRepository
import com.rio.rustry.data.repository.AuthRepository
import com.rio.rustry.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferInitiationViewModel @Inject constructor(
    private val transferRepository: TransferRepository,
    private val fowlRepository: FowlRepository,
    private val authRepository: AuthRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TransferInitiationUiState())
    val uiState: StateFlow<TransferInitiationUiState> = _uiState.asStateFlow()
    
    private val _transferResult = MutableSharedFlow<TransferResult>()
    val transferResult: SharedFlow<TransferResult> = _transferResult.asSharedFlow()
    
    fun initializeTransfer(fowlId: String, paymentId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load fowl information
                val fowl = fowlRepository.getFowlById(fowlId).getOrNull()
                if (fowl == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Fowl not found",
                        isLoading = false
                    )
                    return@launch
                }
                
                // Load current user (seller)
                val currentFirebaseUser = authRepository.getCurrentUser()
                val currentUser = currentFirebaseUser?.let { 
                    authRepository.getUserProfile(it.uid).getOrNull() 
                }
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "User not authenticated",
                        isLoading = false
                    )
                    return@launch
                }
                
                // Load payment information if provided
                var payment: Payment? = null
                var buyer: User? = null
                
                if (paymentId != null) {
                    payment = paymentRepository.getPayment(paymentId).getOrNull()
                    if (payment != null) {
                        buyer = authRepository.getUserProfile(payment.buyerId).getOrNull()
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    fowl = fowl,
                    seller = currentUser,
                    payment = payment,
                    buyer = buyer,
                    transferPrice = payment?.amount ?: fowl.price,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to initialize transfer",
                    isLoading = false
                )
            }
        }
    }
    
    fun updateBuyerEmail(email: String) {
        _uiState.value = _uiState.value.copy(buyerEmail = email)
        
        // Auto-search for user by email
        if (email.contains("@") && email.length > 5) {
            searchUserByEmail(email)
        }
    }
    
    fun updateTransferType(type: TransferType) {
        _uiState.value = _uiState.value.copy(transferType = type)
    }
    
    fun updateTransferPrice(price: Double) {
        _uiState.value = _uiState.value.copy(transferPrice = price)
    }
    
    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }
    
    fun updateWitnessEmail(email: String) {
        _uiState.value = _uiState.value.copy(witnessEmail = email)
    }
    
    fun updateVerificationMethod(method: VerificationMethod) {
        _uiState.value = _uiState.value.copy(verificationMethod = method)
    }
    
    fun updateRequiresVerification(requires: Boolean) {
        _uiState.value = _uiState.value.copy(requiresVerification = requires)
    }
    
    private fun searchUserByEmail(email: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.getUserByEmail(email).getOrNull()
                _uiState.value = _uiState.value.copy(
                    buyer = user,
                    buyerSearchResult = if (user != null) "User found: ${user.name}" else "User not found"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    buyerSearchResult = "Error searching user"
                )
            }
        }
    }
    
    fun initiateTransfer() {
        viewModelScope.launch {
            val state = _uiState.value
            
            // Validation
            val validationErrors = validateTransfer(state)
            if (validationErrors.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    validationErrors = validationErrors
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                validationErrors = emptyList()
            )
            
            try {
                val fowl = state.fowl!!
                val seller = state.seller!!
                val buyer = state.buyer!!
                
                val transfer = OwnershipTransfer(
                    fowlId = fowl.id,
                    fowlTitle = "${fowl.breed} - ${fowl.description}",
                    fromUserId = seller.id,
                    fromUserName = seller.name,
                    fromUserEmail = seller.email,
                    toUserId = buyer.id,
                    toUserName = buyer.name,
                    toUserEmail = buyer.email,
                    paymentId = state.payment?.id ?: "",
                    transferType = state.transferType,
                    transferPrice = state.transferPrice,
                    notes = state.notes,
                    witnessName = state.witnessEmail, // For now, using email as witness identifier
                    verificationMethod = state.verificationMethod,
                    requiresVerification = state.requiresVerification,
                    status = TransferStatus.PENDING
                )
                
                val result = transferRepository.initiateTransfer(transfer)
                
                if (result.isSuccess) {
                    _transferResult.emit(TransferResult.Success(result.getOrNull()!!))
                } else {
                    _transferResult.emit(TransferResult.Error(result.exceptionOrNull()?.message ?: "Failed to initiate transfer"))
                }
                
            } catch (e: Exception) {
                _transferResult.emit(TransferResult.Error(e.message ?: "Unknown error occurred"))
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }
    
    private fun validateTransfer(state: TransferInitiationUiState): List<String> {
        val errors = mutableListOf<String>()
        
        if (state.fowl == null) {
            errors.add("Fowl information is required")
        }
        
        if (state.seller == null) {
            errors.add("Seller information is required")
        }
        
        if (state.buyer == null) {
            errors.add("Buyer information is required")
        }
        
        if (state.buyerEmail.isBlank()) {
            errors.add("Buyer email is required")
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.buyerEmail).matches()) {
            errors.add("Valid buyer email is required")
        }
        
        if (state.seller?.id == state.buyer?.id) {
            errors.add("Buyer and seller cannot be the same person")
        }
        
        if (state.transferPrice < 0) {
            errors.add("Transfer price cannot be negative")
        }
        
        if (state.transferType == TransferType.SALE && state.transferPrice <= 0) {
            errors.add("Sale price must be greater than zero")
        }
        
        if (state.witnessEmail.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(state.witnessEmail).matches()) {
            errors.add("Valid witness email is required")
        }
        
        return errors
    }
    
    fun clearValidationErrors() {
        _uiState.value = _uiState.value.copy(validationErrors = emptyList())
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class TransferInitiationUiState(
    val fowl: Fowl? = null,
    val seller: User? = null,
    val buyer: User? = null,
    val payment: Payment? = null,
    val buyerEmail: String = "",
    val buyerSearchResult: String = "",
    val transferType: TransferType = TransferType.SALE,
    val transferPrice: Double = 0.0,
    val notes: String = "",
    val witnessEmail: String = "",
    val verificationMethod: VerificationMethod = VerificationMethod.DUAL_CONFIRMATION,
    val requiresVerification: Boolean = true,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val validationErrors: List<String> = emptyList(),
    val error: String? = null
)

sealed class TransferResult {
    data class Success(val transferId: String) : TransferResult()
    data class Error(val message: String) : TransferResult()
}