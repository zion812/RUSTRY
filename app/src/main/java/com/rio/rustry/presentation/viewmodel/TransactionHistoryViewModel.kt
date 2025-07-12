package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.*
import com.rio.rustry.data.repository.PaymentRepository
import com.rio.rustry.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()
    
    private var allTransactions: List<Transaction> = emptyList()
    
    fun loadTransactionHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    paymentRepository.getTransactionHistory(currentUser?.uid ?: "").collect { transactions ->
                        allTransactions = transactions
                        
                        val totalSpent = transactions
                            .filter { it.amount < 0 }
                            .sumOf { kotlin.math.abs(it.amount) }
                        
                        val totalEarned = transactions
                            .filter { it.amount > 0 }
                            .sumOf { it.amount }
                        
                        _uiState.value = _uiState.value.copy(
                            transactions = transactions,
                            filteredTransactions = transactions,
                            totalSpent = totalSpent,
                            totalEarned = totalEarned,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "User not found",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load transaction history",
                    isLoading = false
                )
            }
        }
    }
    
    fun filterTransactions(type: TransactionType?) {
        val filtered = if (type == null) {
            allTransactions
        } else {
            allTransactions.filter { it.type == type }
        }
        
        _uiState.value = _uiState.value.copy(
            filteredTransactions = filtered,
            selectedFilter = type
        )
    }
    
    fun searchTransactions(query: String) {
        val filtered = if (query.isBlank()) {
            allTransactions
        } else {
            allTransactions.filter { transaction ->
                transaction.description.contains(query, ignoreCase = true) ||
                transaction.counterpartyName.contains(query, ignoreCase = true) ||
                transaction.fowlTitle.contains(query, ignoreCase = true)
            }
        }
        
        _uiState.value = _uiState.value.copy(
            filteredTransactions = filtered,
            searchQuery = query
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class TransactionHistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val totalSpent: Double = 0.0,
    val totalEarned: Double = 0.0,
    val selectedFilter: TransactionType? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)