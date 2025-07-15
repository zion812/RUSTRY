// generated/phase2/app/src/main/java/com/rio/rustry/payment/CheckoutViewModel.kt

package com.rio.rustry.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.CartItem
import com.rio.rustry.domain.usecase.CheckoutUseCase
import com.rio.rustry.domain.usecase.GetCartUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val checkoutUseCase: CheckoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun loadCheckoutData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                getCartUseCase().collect { result ->
                    result.fold(
                        onSuccess = { items ->
                            val total = items.sumOf { it.price * it.quantity }
                            _uiState.update { 
                                it.copy(
                                    cartItems = items,
                                    total = total,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to load cart"
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

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun updateDeliveryAddress(address: String) {
        _uiState.update { it.copy(deliveryAddress = address) }
    }

    fun placeOrder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPayment = true) }
            
            try {
                val currentState = _uiState.value
                val orderData = CheckoutData(
                    items = currentState.cartItems,
                    paymentMethod = currentState.selectedPaymentMethod,
                    deliveryAddress = currentState.deliveryAddress,
                    total = currentState.total
                )

                val result = checkoutUseCase(orderData)
                
                _uiState.update { 
                    it.copy(
                        isProcessingPayment = false,
                        paymentResult = result
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isProcessingPayment = false,
                        paymentResult = Result.failure(e)
                    )
                }
            }
        }
    }
}

data class CheckoutUiState(
    val cartItems: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.GOOGLE_PAY,
    val deliveryAddress: String = "",
    val isLoading: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val error: String? = null,
    val paymentResult: Result<String>? = null
)

data class CheckoutData(
    val items: List<CartItem>,
    val paymentMethod: PaymentMethod,
    val deliveryAddress: String,
    val total: Double
)