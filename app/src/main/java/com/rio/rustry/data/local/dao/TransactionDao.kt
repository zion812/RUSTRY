package com.rio.rustry.data.local.dao

import androidx.room.*
import com.rio.rustry.data.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for transaction data operations
 */
@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: String): Transaction?
    
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTransactionsByUser(userId: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsByUserPaginated(userId: String, limit: Int, offset: Int): List<Transaction>
    
    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY createdAt DESC")
    fun getTransactionsByStatus(status: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingTransactions(userId: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type ORDER BY createdAt DESC")
    fun getTransactionsByType(userId: String, type: String): Flow<List<Transaction>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)
    
    @Update
    suspend fun updateTransaction(transaction: Transaction)
    
    @Query("UPDATE transactions SET status = :status, updatedAt = :timestamp WHERE id = :transactionId")
    suspend fun updateTransactionStatus(transactionId: String, status: String, timestamp: Long)
    
    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransaction(transactionId: String)
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
    
    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<Transaction>
    
    @Query("SELECT COUNT(*) FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactionsCount(): Int
    
    @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId")
    suspend fun getTransactionCount(userId: String): Int
    
    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun getTotalTransactionAmount(userId: String): Double?
    
    @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId AND status = :status")
    suspend fun getTransactionCountByStatus(userId: String, status: String): Int
    
    @Query("SELECT AVG(amount) FROM transactions WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun getAverageTransactionAmount(userId: String): Double?
}