package com.rio.rustry.presentation.transfers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.repository.TransfersRepository
import com.rio.rustry.domain.model.TransferRequest
import com.rio.rustry.domain.model.TransferVerification
import com.rio.rustry.domain.model.TransferStatus
import com.rio.rustry.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val transfersRepository: TransfersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransferUiState>(TransferUiState.Loading)
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    private val currentUserId = "current_user_id" // TODO: Get from auth service

    fun loadUserTransfers() {
        viewModelScope.launch {
            _uiState.value = TransferUiState.Loading
            try {
                transfersRepository.getUserTransfers(currentUserId).collect { transfers ->
                    _uiState.value = TransferUiState.TransfersLoaded(transfers)
                }
            } catch (e: Exception) {
                _uiState.value = TransferUiState.Error(e.message ?: "Failed to load transfers")
            }
        }
    }

    fun createTransfer(fowlId: String, buyerId: String, price: Double, notes: String) {
        viewModelScope.launch {
            try {
                val transferRequest = TransferRequest(
                    id = "",
                    fowlId = fowlId,
                    sellerId = currentUserId,
                    buyerId = buyerId,
                    price = price,
                    notes = notes,
                    status = TransferStatus.PENDING,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val result = transfersRepository.createTransferRequest(transferRequest)
                when (result) {
                    is Result.Success -> {
                        _uiState.value = TransferUiState.TransferCreated("Transfer created successfully")
                        loadUserTransfers() // Refresh the list
                    }
                    is Result.Error -> {
                        _uiState.value = TransferUiState.Error(result.exception.message ?: "Failed to create transfer")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = TransferUiState.Error(e.message ?: "Failed to create transfer")
            }
        }
    }

    fun submitVerification(transferId: String, verification: TransferVerification) {
        viewModelScope.launch {
            try {
                val result = transfersRepository.submitVerification(
                    transferId = transferId,
                    userId = currentUserId,
                    verification = verification.copy(verifierId = currentUserId)
                )

                when (result) {
                    is Result.Success -> {
                        _uiState.value = TransferUiState.VerificationSubmitted("Verification submitted successfully")
                        loadUserTransfers() // Refresh the list
                    }
                    is Result.Error -> {
                        _uiState.value = TransferUiState.Error(result.exception.message ?: "Failed to submit verification")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = TransferUiState.Error(e.message ?: "Failed to submit verification")
            }
        }
    }

    fun cancelTransfer(transferId: String) {
        viewModelScope.launch {
            try {
                val result = transfersRepository.cancelTransfer(transferId, currentUserId)
                when (result) {
                    is Result.Success -> {
                        _uiState.value = TransferUiState.TransferCancelled("Transfer cancelled successfully")
                        loadUserTransfers() // Refresh the list
                    }
                    is Result.Error -> {
                        _uiState.value = TransferUiState.Error(result.exception.message ?: "Failed to cancel transfer")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = TransferUiState.Error(e.message ?: "Failed to cancel transfer")
            }
        }
    }

    fun showVerificationDialog(transferId: String) {
        _uiState.value = TransferUiState.ShowingVerification(transferId)
    }

    fun hideVerificationDialog() {
        loadUserTransfers() // Return to transfers list
    }

    fun validateTransferDetails(
        fowlId: String,
        expectedColor: String,
        expectedAge: Int,
        expectedPrice: Double
    ) {
        viewModelScope.launch {
            try {
                val result = transfersRepository.validateTransferDetails(
                    fowlId, expectedColor, expectedAge, expectedPrice
                )
                when (result) {
                    is Result.Success -> {
                        _uiState.value = TransferUiState.ValidationResult(result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = TransferUiState.Error(result.exception.message ?: "Validation failed")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = TransferUiState.Error(e.message ?: "Validation failed")
            }
        }
    }

    fun reportTransferIssue(transferId: String, issue: String, description: String) {
        viewModelScope.launch {
            try {
                val result = transfersRepository.reportTransferIssue(
                    transferId = transferId,
                    reporterId = currentUserId,
                    issue = issue,
                    description = description
                )
                when (result) {
                    is Result.Success -> {
                        _uiState.value = TransferUiState.IssueReported("Issue reported successfully")
                        loadUserTransfers() // Refresh the list
                    }
                    is Result.Error -> {
                        _uiState.value = TransferUiState.Error(result.exception.message ?: "Failed to report issue")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = TransferUiState.Error(e.message ?: "Failed to report issue")
            }
        }
    }

    fun getTransferVerifications(transferId: String) {
        viewModelScope.launch {
            try {
                val result = transfersRepository.getTransferVerifications(transferId)
                when (result) {
                    is Result.Success -> {
                        _uiState.value = TransferUiState.VerificationsLoaded(result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = TransferUiState.Error(result.exception.message ?: "Failed to load verifications")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = TransferUiState.Error(e.message ?: "Failed to load verifications")
            }
        }
    }
}

sealed class TransferUiState {
    object Loading : TransferUiState()
    data class TransfersLoaded(val transfers: List<TransferRequest>) : TransferUiState()
    data class TransferCreated(val message: String) : TransferUiState()
    data class VerificationSubmitted(val message: String) : TransferUiState()
    data class TransferCancelled(val message: String) : TransferUiState()
    data class ShowingVerification(val transferId: String) : TransferUiState()
    data class ValidationResult(val isValid: Boolean) : TransferUiState()
    data class IssueReported(val message: String) : TransferUiState()
    data class VerificationsLoaded(val verifications: List<TransferVerification>) : TransferUiState()
    data class Error(val message: String) : TransferUiState()
}