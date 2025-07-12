package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.*
import com.rio.rustry.data.repository.PaymentRepository
import com.rio.rustry.data.repository.FowlRepository
import com.rio.rustry.data.repository.AuthRepository
import com.rio.rustry.utils.GooglePayHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val fowlRepository: FowlRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()
    
    private val _paymentResult = MutableSharedFlow<PaymentResult>()
    val paymentResult: SharedFlow<PaymentResult> = _paymentResult.asSharedFlow()
    
    fun initializePayment(fowlId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val fowl = fowlRepository.getFowlById(fowlId).getOrNull()
                val currentFirebaseUser = authRepository.getCurrentUser()
                val currentUser = currentFirebaseUser?.let { 
                    authRepository.getUserProfile(it.uid).getOrNull() 
                }
                val config = paymentRepository.getPaymentConfig()
                
                if (fowl != null && currentUser != null) {
                    val platformFee = fowl.price * (config.platformFeePercentage / 100)
                    val totalAmount = fowl.price + platformFee
                    val netAmount = fowl.price - platformFee
                    
                    _uiState.value = _uiState.value.copy(
                        fowl = fowl,
                        buyer = currentUser,
                        platformFee = platformFee,
                        totalAmount = totalAmount,
                        netAmount = netAmount,
                        paymentConfig = config,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load payment information",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unknown error occurred",
                    isLoading = false
                )
            }
        }
    }
    
    fun processPayment(googlePayToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            
            try {
                val state = _uiState.value
                val fowl = state.fowl ?: return@launch
                val buyer = state.buyer ?: return@launch
                
                // Get seller information
                val seller = authRepository.getUserProfile(fowl.ownerId).getOrNull()
                    ?: throw Exception("Seller not found")
                
                val payment = Payment(
                    fowlId = fowl.id,
                    fowlTitle = "${fowl.breed} - ${fowl.description}",
                    buyerId = buyer.id,
                    buyerName = buyer.name,
                    buyerEmail = buyer.email,
                    sellerId = seller.id,
                    sellerName = seller.name,
                    sellerEmail = seller.email,
                    amount = state.totalAmount,
                    platformFee = state.platformFee,
                    netAmount = state.netAmount,
                    googlePayToken = googlePayToken,
                    status = PaymentStatus.PROCESSING
                )
                
                val result = paymentRepository.createPayment(payment)
                
                if (result.isSuccess) {
                    val paymentId = result.getOrNull()!!
                    
                    // Simulate payment processing (replace with actual payment gateway integration)
                    processWithPaymentGateway(paymentId, googlePayToken)
                } else {
                    _paymentResult.emit(PaymentResult.Error("Failed to create payment"))
                }
            } catch (e: Exception) {
                _paymentResult.emit(PaymentResult.Error(e.message ?: "Payment failed"))
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }
    
    private suspend fun processWithPaymentGateway(paymentId: String, token: String) {
        try {
            // TODO: Integrate with actual payment gateway
            // For now, simulate successful payment
            kotlinx.coroutines.delay(2000) // Simulate processing time
            
            val transactionId = "txn_${System.currentTimeMillis()}"
            
            paymentRepository.updatePaymentStatus(
                paymentId = paymentId,
                status = PaymentStatus.COMPLETED,
                transactionId = transactionId
            )
            
            _paymentResult.emit(PaymentResult.Success(paymentId, transactionId))
            
        } catch (e: Exception) {
            paymentRepository.updatePaymentStatus(
                paymentId = paymentId,
                status = PaymentStatus.FAILED,
                failureReason = e.message ?: "Payment processing failed"
            )
            
            _paymentResult.emit(PaymentResult.Error("Payment processing failed"))
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class PaymentUiState(
    val fowl: Fowl? = null,
    val buyer: User? = null,
    val platformFee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val netAmount: Double = 0.0,
    val paymentConfig: PaymentConfig = PaymentConfig(),
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null
)

sealed class PaymentResult {
    data class Success(val paymentId: String, val transactionId: String) : PaymentResult()
    data class Error(val message: String) : PaymentResult()
}