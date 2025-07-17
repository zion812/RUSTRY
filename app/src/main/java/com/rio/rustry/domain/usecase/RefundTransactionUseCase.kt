package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.repository.TransactionRepository
import com.rio.rustry.domain.model.Result

class RefundTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: String): Result<Boolean> {
        return try {
            transactionRepository.refundTransaction(transactionId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}