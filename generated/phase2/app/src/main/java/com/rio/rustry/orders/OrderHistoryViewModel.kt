// generated/phase2/app/src/main/java/com/rio/rustry/orders/OrderHistoryViewModel.kt

package com.rio.rustry.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.Order
import com.rio.rustry.data.model.OrderStatus
import com.rio.rustry.domain.usecase.LoadOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val loadOrdersUseCase: LoadOrdersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                loadOrdersUseCase().collect { result ->
                    result.fold(
                        onSuccess = { orders ->
                            _uiState.update { state ->
                                state.copy(
                                    orders = orders,
                                    filteredOrders = filterOrdersByTab(orders, state.selectedTab),
                                    isLoading = false,
                                    isRefreshing = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    error = error.message ?: "Failed to load orders"
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun refreshOrders() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadOrders()
    }

    fun selectTab(tab: OrderTab) {
        _uiState.update { state ->
            state.copy(
                selectedTab = tab,
                filteredOrders = filterOrdersByTab(state.orders, tab)
            )
        }
    }

    private fun filterOrdersByTab(orders: List<Order>, tab: OrderTab): List<Order> {
        return when (tab) {
            OrderTab.ACTIVE -> orders.filter { 
                it.status in listOf(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.SHIPPED)
            }
            OrderTab.COMPLETED -> orders.filter { 
                it.status == OrderStatus.DELIVERED
            }
            OrderTab.CANCELLED -> orders.filter { 
                it.status == OrderStatus.CANCELLED
            }
        }
    }
}

data class OrderHistoryUiState(
    val orders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val selectedTab: OrderTab = OrderTab.ACTIVE,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)