package com.rio.rustry.domain.usecase

import com.rio.rustry.data.model.Transaction
import com.rio.rustry.domain.model.Result
import com.rio.rustry.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionHistoryUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(userId: String): Flow<Result<List<Transaction>>> {
        return transactionRepository.getTransactionHistory(userId)
    }
}