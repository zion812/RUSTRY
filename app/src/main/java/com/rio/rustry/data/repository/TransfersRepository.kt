package com.rio.rustry.data.repository

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.domain.model.TransferRequest
import com.rio.rustry.domain.model.TransferVerification
import com.rio.rustry.domain.model.TransferStatus
import com.rio.rustry.domain.model.Result
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransfersRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) {
    private val transfersCollection = firestore.collection("transfers")
    private val verificationsCollection = firestore.collection("transfer_verifications")

    suspend fun createTransferRequest(request: TransferRequest): Result<String> {
        return try {
            val docRef = transfersCollection.add(request).await()
            
            // Trigger Firebase Function for dual verification
            val data = hashMapOf(
                "transferId" to docRef.id,
                "sellerId" to request.sellerId,
                "buyerId" to request.buyerId,
                "fowlId" to request.fowlId
            )
            
            functions.getHttpsCallable("initiateTransferVerification")
                .call(data)
                .await()
            
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getTransferRequest(transferId: String): Result<TransferRequest> {
        return try {
            val document = transfersCollection.document(transferId).get().await()
            val transfer = document.toObject(TransferRequest::class.java)
            if (transfer != null) {
                Result.Success(transfer.copy(id = document.id))
            } else {
                Result.Error(Exception("Transfer not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getUserTransfers(userId: String): Flow<List<TransferRequest>> = flow {
        try {
            // Get transfers where user is either seller or buyer
            val sellerTransfers = transfersCollection
                .whereEqualTo("sellerId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(TransferRequest::class.java)?.copy(id = it.id) }

            val buyerTransfers = transfersCollection
                .whereEqualTo("buyerId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(TransferRequest::class.java)?.copy(id = it.id) }

            val allTransfers = (sellerTransfers + buyerTransfers).distinctBy { it.id }
            emit(allTransfers.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun submitVerification(
        transferId: String,
        userId: String,
        verification: TransferVerification
    ): Result<Unit> {
        return try {
            val verificationData = verification.copy(
                transferId = transferId,
                verifierId = userId,
                timestamp = System.currentTimeMillis()
            )
            
            verificationsCollection.add(verificationData).await()
            
            // Check if both parties have verified
            checkAndCompleteTransfer(transferId)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun checkAndCompleteTransfer(transferId: String) {
        try {
            val transfer = getTransferRequest(transferId)
            if (transfer is Result.Success) {
                val verifications = verificationsCollection
                    .whereEqualTo("transferId", transferId)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(TransferVerification::class.java) }

                val sellerVerified = verifications.any { it.verifierId == transfer.data.sellerId }
                val buyerVerified = verifications.any { it.verifierId == transfer.data.buyerId }

                if (sellerVerified && buyerVerified) {
                    // Both parties verified - complete transfer
                    updateTransferStatus(transferId, TransferStatus.COMPLETED)
                    
                    // Trigger Firebase Function to update fowl ownership
                    val data = hashMapOf(
                        "transferId" to transferId,
                        "fowlId" to transfer.data.fowlId,
                        "newOwnerId" to transfer.data.buyerId,
                        "previousOwnerId" to transfer.data.sellerId
                    )
                    
                    functions.getHttpsCallable("completeOwnershipTransfer")
                        .call(data)
                        .await()
                }
            }
        } catch (e: Exception) {
            // Log error but don't throw to avoid breaking the verification flow
        }
    }

    suspend fun updateTransferStatus(transferId: String, status: TransferStatus): Result<Unit> {
        return try {
            transfersCollection.document(transferId)
                .update("status", status.name)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun cancelTransfer(transferId: String, userId: String): Result<Unit> {
        return try {
            val transfer = getTransferRequest(transferId)
            if (transfer is Result.Success) {
                // Only seller or buyer can cancel
                if (transfer.data.sellerId == userId || transfer.data.buyerId == userId) {
                    updateTransferStatus(transferId, TransferStatus.CANCELLED)
                    Result.Success(Unit)
                } else {
                    Result.Error(Exception("Unauthorized to cancel this transfer"))
                }
            } else {
                Result.Error(Exception("Transfer not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getTransferVerifications(transferId: String): Result<List<TransferVerification>> {
        return try {
            val verifications = verificationsCollection
                .whereEqualTo("transferId", transferId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(TransferVerification::class.java) }
            
            Result.Success(verifications)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun validateTransferDetails(
        fowlId: String,
        expectedColor: String,
        expectedAge: Int,
        expectedPrice: Double
    ): Result<Boolean> {
        return try {
            // Call Firebase Function to validate fowl details
            val data = hashMapOf(
                "fowlId" to fowlId,
                "expectedColor" to expectedColor,
                "expectedAge" to expectedAge,
                "expectedPrice" to expectedPrice
            )
            
            val result = functions.getHttpsCallable("validateFowlDetails")
                .call(data)
                .await()
            
            val isValid = result.data as? Boolean ?: false
            Result.Success(isValid)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun reportTransferIssue(
        transferId: String,
        reporterId: String,
        issue: String,
        description: String
    ): Result<Unit> {
        return try {
            val reportData = hashMapOf(
                "transferId" to transferId,
                "reporterId" to reporterId,
                "issue" to issue,
                "description" to description,
                "timestamp" to System.currentTimeMillis(),
                "status" to "PENDING"
            )
            
            firestore.collection("transfer_reports")
                .add(reportData)
                .await()
            
            // Update transfer status to disputed
            updateTransferStatus(transferId, TransferStatus.DISPUTED)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}