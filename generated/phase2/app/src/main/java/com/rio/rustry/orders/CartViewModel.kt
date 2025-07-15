// generated/phase2/app/src/main/java/com/rio/rustry/orders/CartViewModel.kt

package com.rio.rustry.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.CartItem
import com.rio.rustry.domain.usecase.AddToCartUseCase
import com.rio.rustry.domain.usecase.GetCartUseCase
import com.rio.rustry.domain.usecase.RemoveFromCartUseCase
import com.rio.rustry.domain.usecase.UpdateCartQuantityUseCase
import com.rio.rustry.payment.PaymentMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    fun loadCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                getCartUseCase().collect { result ->
                    result.fold(
                        onSuccess = { items ->
                            val total = items.sumOf { it.price * it.quantity }
                            _uiState.update { 
                                it.copy(
                                    items = items,
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

    fun updateQuantity(fowlId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                updateCartQuantityUseCase(fowlId, quantity).fold(
                    onSuccess = {
                        // Update local state
                        _uiState.update { state ->
                            val updatedItems = state.items.map { item ->
                                if (item.fowlId == fowlId) {
                                    item.copy(quantity = quantity)
                                } else {
                                    item
                                }
                            }
                            val newTotal = updatedItems.sumOf { it.price * it.quantity }
                            state.copy(items = updatedItems, total = newTotal)
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(error = error.message ?: "Failed to update quantity")
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

    fun removeFromCart(fowlId: String) {
        viewModelScope.launch {
            try {
                removeFromCartUseCase(fowlId).fold(
                    onSuccess = {
                        // Update local state
                        _uiState.update { state ->
                            val updatedItems = state.items.filter { it.fowlId != fowlId }
                            val newTotal = updatedItems.sumOf { it.price * it.quantity }
                            state.copy(items = updatedItems, total = newTotal)
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(error = error.message ?: "Failed to remove item")
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

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }
}

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.GOOGLE_PAY,
    val isLoading: Boolean = false,
    val error: String? = null
)