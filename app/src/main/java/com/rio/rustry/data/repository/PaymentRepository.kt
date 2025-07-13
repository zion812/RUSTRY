package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.data.model.*
import kotlinx.coroutines.tasks.await
import com.rio.rustry.di.FirebaseModule

class PaymentRepository(
    private val firestore: FirebaseFirestore = FirebaseModule.provideFirebaseFirestore()
) {
    
    suspend fun createPayment(payment: Payment): Result<String> {
        return try {
            val docRef = firestore.collection("payments").add(payment).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPayment(paymentId: String): Result<Payment> {
        return try {
            val document = firestore.collection("payments")
                .document(paymentId)
                .get()
                .await()
            
            val payment = document.toObject(Payment::class.java)?.copy(id = document.id)
            if (payment != null) {
                Result.success(payment)
            } else {
                Result.failure(Exception("Payment not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePaymentStatus(paymentId: String, status: PaymentStatus): Result<Unit> {
        return try {
            firestore.collection("payments")
                .document(paymentId)
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserPayments(userId: String): Result<List<Payment>> {
        return try {
            // Get payments where user is either buyer or seller
            val buyerSnapshot = firestore.collection("payments")
                .whereEqualTo("buyerId", userId)
                .orderBy("createdAt")
                .get()
                .await()
            
            val sellerSnapshot = firestore.collection("payments")
                .whereEqualTo("sellerId", userId)
                .orderBy("createdAt")
                .get()
                .await()
            
            val buyerPayments = buyerSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Payment::class.java)?.copy(id = doc.id)
            }
            
            val sellerPayments = sellerSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Payment::class.java)?.copy(id = doc.id)
            }
            
            val allPayments = (buyerPayments + sellerPayments)
                .distinctBy { it.id }
                .sortedByDescending { it.createdAt }
            
            Result.success(allPayments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createTransaction(transaction: Transaction): Result<String> {
        return try {
            val docRef = firestore.collection("transactions").add(transaction).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTransaction(transactionId: String): Result<Transaction> {
        return try {
            val document = firestore.collection("transactions")
                .document(transactionId)
                .get()
                .await()
            
            val transaction = document.toObject(Transaction::class.java)?.copy(id = document.id)
            if (transaction != null) {
                Result.success(transaction)
            } else {
                Result.failure(Exception("Transaction not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserTransactions(userId: String): Result<List<Transaction>> {
        return try {
            // Get transactions where user is either buyer or seller
            val buyerSnapshot = firestore.collection("transactions")
                .whereEqualTo("buyerId", userId)
                .orderBy("createdAt")
                .get()
                .await()
            
            val sellerSnapshot = firestore.collection("transactions")
                .whereEqualTo("sellerId", userId)
                .orderBy("createdAt")
                .get()
                .await()
            
            val buyerTransactions = buyerSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Transaction::class.java)?.copy(id = doc.id)
            }
            
            val sellerTransactions = sellerSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Transaction::class.java)?.copy(id = doc.id)
            }
            
            val allTransactions = (buyerTransactions + sellerTransactions)
                .distinctBy { it.id }
                .sortedByDescending { it.createdAt }
            
            Result.success(allTransactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateTransactionStatus(
        transactionId: String, 
        status: TransactionStatus
    ): Result<Unit> {
        return try {
            firestore.collection("transactions")
                .document(transactionId)
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTransactionsByType(
        userId: String, 
        type: TransactionType
    ): Result<List<Transaction>> {
        return try {
            val snapshot = firestore.collection("transactions")
                .whereEqualTo("type", type)
                .orderBy("createdAt")
                .get()
                .await()
            
            val transactions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Transaction::class.java)?.copy(id = doc.id)
            }.filter { it.buyerId == userId || it.sellerId == userId }
            
            Result.success(transactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getPaymentConfig(): PaymentConfig {
        // Return default config for now
        return PaymentConfig()
    }
    
    suspend fun getPaymentConfigAsync(): Result<PaymentConfig> {
        return try {
            val document = firestore.collection("config")
                .document("payment")
                .get()
                .await()
            
            val config = document.toObject(PaymentConfig::class.java)
            if (config != null) {
                Result.success(config)
            } else {
                // Return default config
                Result.success(PaymentConfig())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun processRefund(paymentId: String, amount: Double): Result<Unit> {
        return try {
            firestore.collection("payments")
                .document(paymentId)
                .update(
                    mapOf(
                        "refundAmount" to amount,
                        "refundDate" to java.util.Date(),
                        "status" to if (amount > 0) PaymentStatus.PARTIALLY_REFUNDED else PaymentStatus.REFUNDED,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePaymentStatus(
        paymentId: String, 
        status: PaymentStatus, 
        transactionId: String? = null,
        failureReason: String? = null
    ): Result<Unit> {
        return try {
            val updateMap = mutableMapOf<String, Any>(
                "status" to status,
                "updatedAt" to System.currentTimeMillis()
            )
            
            transactionId?.let { updateMap["transactionId"] = it }
            failureReason?.let { updateMap["failureReason"] = it }
            
            firestore.collection("payments")
                .document(paymentId)
                .update(updateMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTransactionHistory(userId: String): Result<List<Transaction>> {
        return getUserTransactions(userId)
    }
}