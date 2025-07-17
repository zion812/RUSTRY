package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.data.local.dao.TransactionDao
import com.rio.rustry.data.model.Transaction
import com.rio.rustry.domain.model.Result
import com.rio.rustry.domain.repository.TransactionRepository
import com.rio.rustry.domain.repository.TransactionStatistics
import com.rio.rustry.utils.ErrorHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TransactionRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val localTransactionDao: TransactionDao,
    private val ioDispatcher: CoroutineDispatcher
) : TransactionRepository {
    
    companion object {
        private const val TRANSACTIONS_COLLECTION = "transactions"
    }
    
    override fun getTransactionsByUser(userId: String): Flow<Result<List<Transaction>>> = flow {
        try {
            emit(Result.Loading)
            
            // Get from local cache first
            localTransactionDao.getTransactionsByUser(userId).collect { localTransactions ->
                if (localTransactions.isNotEmpty()) {
                    emit(Result.Success(localTransactions))
                }
            }
            
            // Fetch from Firestore
            val snapshot = firestore.collection(TRANSACTIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val transactions = snapshot.toObjects(Transaction::class.java)
            
            // Update local cache
            withContext(ioDispatcher) {
                localTransactionDao.insertTransactions(transactions)
            }
            
            emit(Result.Success(transactions))
        } catch (e: Exception) {
            emit(Result.Error(ErrorHandler.handleError(e)))
        }
    }.flowOn(ioDispatcher)
    
    override suspend fun getTransactionById(transactionId: String): Result<Transaction?> = withContext(ioDispatcher) {
        try {
            // Check local cache first
            val localTransaction = localTransactionDao.getTransactionById(transactionId)
            if (localTransaction != null) {
                return@withContext Result.Success(localTransaction)
            }
            
            // Fetch from Firestore
            val document = firestore.collection(TRANSACTIONS_COLLECTION)
                .document(transactionId)
                .get()
                .await()
            
            val transaction = document.toObject(Transaction::class.java)
            
            // Update local cache
            transaction?.let {
                localTransactionDao.insertTransaction(it)
            }
            
            Result.Success(transaction)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun createTransaction(transaction: Transaction): Result<Unit> = withContext(ioDispatcher) {
        try {
            // Save to Firestore
            firestore.collection(TRANSACTIONS_COLLECTION)
                .document(transaction.id)
                .set(transaction)
                .await()
            
            // Save to local cache
            localTransactionDao.insertTransaction(transaction)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun updateTransactionStatus(transactionId: String, status: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            val updateData = mapOf(
                "status" to status,
                "updatedAt" to System.currentTimeMillis()
            )
            
            // Update in Firestore
            firestore.collection(TRANSACTIONS_COLLECTION)
                .document(transactionId)
                .update(updateData)
                .await()
            
            // Update local cache
            localTransactionDao.updateTransactionStatus(transactionId, status, System.currentTimeMillis())
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override fun getTransactionHistory(userId: String, page: Int, pageSize: Int): Flow<Result<List<Transaction>>> = flow {
        try {
            emit(Result.Loading)
            
            val offset = page * pageSize
            val localTransactions = localTransactionDao.getTransactionsByUserPaginated(userId, pageSize, offset)
            
            if (localTransactions.isNotEmpty()) {
                emit(Result.Success(localTransactions))
            }
            
            // Fetch from Firestore with pagination
            val snapshot = firestore.collection(TRANSACTIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(pageSize.toLong())
                .get()
                .await()
            
            val transactions = snapshot.toObjects(Transaction::class.java)
            
            // Update local cache
            withContext(ioDispatcher) {
                localTransactionDao.insertTransactions(transactions)
            }
            
            emit(Result.Success(transactions))
        } catch (e: Exception) {
            emit(Result.Error(ErrorHandler.handleError(e)))
        }
    }.flowOn(ioDispatcher)
    
    override fun getPendingTransactions(userId: String): Flow<Result<List<Transaction>>> = flow {
        try {
            emit(Result.Loading)
            
            // Get from local cache first
            localTransactionDao.getPendingTransactions(userId).collect { localTransactions ->
                if (localTransactions.isNotEmpty()) {
                    emit(Result.Success(localTransactions))
                }
            }
            
            // Fetch from Firestore
            val snapshot = firestore.collection(TRANSACTIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "PENDING")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val transactions = snapshot.toObjects(Transaction::class.java)
            
            // Update local cache
            withContext(ioDispatcher) {
                localTransactionDao.insertTransactions(transactions)
            }
            
            emit(Result.Success(transactions))
        } catch (e: Exception) {
            emit(Result.Error(ErrorHandler.handleError(e)))
        }
    }.flowOn(ioDispatcher)
    
    override suspend fun processRefund(transactionId: String, reason: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            val updateData = mapOf(
                "status" to "REFUNDED",
                "refundReason" to reason,
                "refundedAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            
            // Update in Firestore
            firestore.collection(TRANSACTIONS_COLLECTION)
                .document(transactionId)
                .update(updateData)
                .await()
            
            // Update local cache
            localTransactionDao.updateTransactionStatus(transactionId, "REFUNDED", System.currentTimeMillis())
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun refundTransaction(transactionId: String): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val updateData = mapOf(
                "status" to "REFUNDED",
                "refundedAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            
            // Update in Firestore
            firestore.collection(TRANSACTIONS_COLLECTION)
                .document(transactionId)
                .update(updateData)
                .await()
            
            // Update local cache
            localTransactionDao.updateTransactionStatus(transactionId, "REFUNDED", System.currentTimeMillis())
            
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun getTransactionStatistics(userId: String): Result<TransactionStatistics> = withContext(ioDispatcher) {
        try {
            val totalCount = localTransactionDao.getTransactionCount(userId)
            val totalAmount = localTransactionDao.getTotalTransactionAmount(userId) ?: 0.0
            val successfulCount = localTransactionDao.getTransactionCountByStatus(userId, "COMPLETED")
            val failedCount = localTransactionDao.getTransactionCountByStatus(userId, "FAILED")
            val pendingCount = localTransactionDao.getTransactionCountByStatus(userId, "PENDING")
            val averageAmount = localTransactionDao.getAverageTransactionAmount(userId) ?: 0.0
            
            val statistics = TransactionStatistics(
                totalTransactions = totalCount,
                totalAmount = totalAmount,
                successfulTransactions = successfulCount,
                failedTransactions = failedCount,
                pendingTransactions = pendingCount,
                averageTransactionAmount = averageAmount
            )
            
            Result.Success(statistics)
        } catch (e: Exception) {
            Result.Error(ErrorHandler.handleError(e))
        }
    }
}