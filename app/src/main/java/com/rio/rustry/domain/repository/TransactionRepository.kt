package com.rio.rustry.domain.repository

import com.rio.rustry.data.model.Transaction
import com.rio.rustry.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for transaction operations
 */
interface TransactionRepository {
    
    /**
     * Get all transactions for a user
     */
    fun getTransactionsByUser(userId: String): Flow<Result<List<Transaction>>>
    
    /**
     * Get transaction by ID
     */
    suspend fun getTransactionById(transactionId: String): Result<Transaction?>
    
    /**
     * Create a new transaction
     */
    suspend fun createTransaction(transaction: Transaction): Result<Unit>
    
    /**
     * Update transaction status
     */
    suspend fun updateTransactionStatus(transactionId: String, status: String): Result<Unit>
    
    /**
     * Get transaction history with pagination
     */
    fun getTransactionHistory(userId: String, page: Int = 0, pageSize: Int = 20): Flow<Result<List<Transaction>>>
    
    /**
     * Get pending transactions
     */
    fun getPendingTransactions(userId: String): Flow<Result<List<Transaction>>>
    
    /**
     * Process refund
     */
    suspend fun processRefund(transactionId: String, reason: String): Result<Unit>
    
    /**
     * Refund transaction
     */
    suspend fun refundTransaction(transactionId: String): Result<Boolean>
    
    /**
     * Get transaction statistics
     */
    suspend fun getTransactionStatistics(userId: String): Result<TransactionStatistics>
}

/**
 * Transaction statistics data class
 */
data class TransactionStatistics(
    val totalTransactions: Int = 0,
    val totalAmount: Double = 0.0,
    val successfulTransactions: Int = 0,
    val failedTransactions: Int = 0,
    val pendingTransactions: Int = 0,
    val averageTransactionAmount: Double = 0.0
)