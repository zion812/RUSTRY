package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.data.model.*
import kotlinx.coroutines.tasks.await
import com.rio.rustry.di.FirebaseModule

class TransferRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    
    suspend fun createTransfer(transfer: OwnershipTransfer): Result<String> {
        return try {
            val docRef = firestore.collection("ownership_transfers").add(transfer).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun getTransfer(transferId: String): Result<OwnershipTransfer> {
        return try {
            val document = firestore.collection("ownership_transfers")
                .document(transferId)
                .get()
                .await()
            
            val transfer = document.toObject(OwnershipTransfer::class.java)?.copy(id = document.id)
            if (transfer != null) {
                Result.Success(transfer)
            } else {
                Result.Error(Exception("Transfer not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun updateTransferStatus(
        transferId: String, 
        status: TransferStatus
    ): Result<Unit> {
        return try {
            firestore.collection("ownership_transfers")
                .document(transferId)
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun getUserTransfers(userId: String): Result<List<OwnershipTransfer>> {
        return try {
            // Get transfers where user is either sender or receiver
            val fromSnapshot = firestore.collection("ownership_transfers")
                .whereEqualTo("fromOwnerId", userId)
                .orderBy("createdAt")
                .get()
                .await()
            
            val toSnapshot = firestore.collection("ownership_transfers")
                .whereEqualTo("toOwnerId", userId)
                .orderBy("createdAt")
                .get()
                .await()
            
            val fromTransfers = fromSnapshot.documents.mapNotNull { doc ->
                doc.toObject(OwnershipTransfer::class.java)?.copy(id = doc.id)
            }
            
            val toTransfers = toSnapshot.documents.mapNotNull { doc ->
                doc.toObject(OwnershipTransfer::class.java)?.copy(id = doc.id)
            }
            
            val allTransfers = (fromTransfers + toTransfers)
                .distinctBy { it.id }
                .sortedByDescending { it.createdAt }
            
            Result.Success(allTransfers)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun getFowlTransfers(fowlId: String): Result<List<OwnershipTransfer>> {
        return try {
            val snapshot = firestore.collection("ownership_transfers")
                .whereEqualTo("fowlId", fowlId)
                .orderBy("createdAt")
                .get()
                .await()
            
            val transfers = snapshot.documents.mapNotNull { doc ->
                doc.toObject(OwnershipTransfer::class.java)?.copy(id = doc.id)
            }
            
            Result.Success(transfers)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun createDigitalCertificate(certificate: DigitalCertificate): Result<String> {
        return try {
            val docRef = firestore.collection("digital_certificates").add(certificate).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun getDigitalCertificate(certificateId: String): Result<DigitalCertificate> {
        return try {
            val document = firestore.collection("digital_certificates")
                .document(certificateId)
                .get()
                .await()
            
            val certificate = document.toObject(DigitalCertificate::class.java)?.copy(id = document.id)
            if (certificate != null) {
                Result.Success(certificate)
            } else {
                Result.Error(Exception("Certificate not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun getFowlCertificates(fowlId: String): Result<List<DigitalCertificate>> {
        return try {
            val snapshot = firestore.collection("digital_certificates")
                .whereEqualTo("fowlId", fowlId)
                .orderBy("issueDate")
                .get()
                .await()
            
            val certificates = snapshot.documents.mapNotNull { doc ->
                doc.toObject(DigitalCertificate::class.java)?.copy(id = doc.id)
            }
            
            Result.Success(certificates)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun createTransferRequest(request: TransferRequest): Result<String> {
        return try {
            val docRef = firestore.collection("transfer_requests").add(request).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun getTransferRequests(userId: String): Result<List<TransferRequest>> {
        return try {
            // Get requests where user is either requester or current owner
            val requesterSnapshot = firestore.collection("transfer_requests")
                .whereEqualTo("requesterId", userId)
                .orderBy("createdAt")
                .get()
                .await()
            
            val ownerSnapshot = firestore.collection("transfer_requests")
                .whereEqualTo("currentOwnerId", userId)
                .orderBy("createdAt")
                .get()
                .await()
            
            val requesterRequests = requesterSnapshot.documents.mapNotNull { doc ->
                doc.toObject(TransferRequest::class.java)?.copy(id = doc.id)
            }
            
            val ownerRequests = ownerSnapshot.documents.mapNotNull { doc ->
                doc.toObject(TransferRequest::class.java)?.copy(id = doc.id)
            }
            
            val allRequests = (requesterRequests + ownerRequests)
                .distinctBy { it.id }
                .sortedByDescending { it.createdAt }
            
            Result.Success(allRequests)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun updateTransferRequestStatus(
        requestId: String, 
        status: TransferRequestStatus
    ): Result<Unit> {
        return try {
            firestore.collection("transfer_requests")
                .document(requestId)
                .update(
                    mapOf(
                        "status" to status,
                        "respondedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun createVerificationCode(code: VerificationCode): Result<String> {
        return try {
            val docRef = firestore.collection("verification_codes").add(code).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun verifyCode(transferId: String, code: String): Result<Boolean> {
        return try {
            val snapshot = firestore.collection("verification_codes")
                .whereEqualTo("transferId", transferId)
                .whereEqualTo("code", code)
                .whereEqualTo("isUsed", false)
                .get()
                .await()
            
            if (snapshot.documents.isNotEmpty()) {
                val docId = snapshot.documents.first().id
                firestore.collection("verification_codes")
                    .document(docId)
                    .update(
                        mapOf(
                            "isUsed" to true,
                            "usedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
                Result.Success(true)
            } else {
                Result.Success(false)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun completeTransfer(transferId: String): Result<Unit> {
        return try {
            firestore.collection("ownership_transfers")
                .document(transferId)
                .update(
                    mapOf(
                        "status" to TransferStatus.COMPLETED,
                        "completedDate" to System.currentTimeMillis(),
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun getCertificate(certificateId: String): Result<DigitalCertificate> {
        return getDigitalCertificate(certificateId)
    }
    
    suspend fun verifyCertificate(certificateId: String): Result<Boolean> {
        return try {
            val certificate = getDigitalCertificate(certificateId).getOrNull()
            if (certificate != null && certificate.isValid) {
                Result.Success(true)
            } else {
                Result.Success(false)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun initiateTransfer(transfer: OwnershipTransfer): Result<String> {
        return createTransfer(transfer)
    }
    
    suspend fun confirmTransfer(transferId: String, userId: String): Result<Unit> {
        return try {
            val transfer = getTransfer(transferId).getOrNull()
            if (transfer != null) {
                val updateMap = mutableMapOf<String, Any>(
                    "updatedAt" to System.currentTimeMillis()
                )
                
                when (userId) {
                    transfer.fromUserId -> {
                        updateMap["sellerConfirmedAt"] = System.currentTimeMillis()
                        updateMap["status"] = TransferStatus.SELLER_CONFIRMED
                    }
                    transfer.toUserId -> {
                        updateMap["buyerConfirmedAt"] = System.currentTimeMillis()
                        updateMap["status"] = TransferStatus.BUYER_CONFIRMED
                    }
                }
                
                firestore.collection("ownership_transfers")
                    .document(transferId)
                    .update(updateMap)
                    .await()
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Transfer not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun cancelTransfer(transferId: String): Result<Unit> {
        return try {
            firestore.collection("ownership_transfers")
                .document(transferId)
                .update(
                    mapOf(
                        "status" to TransferStatus.CANCELLED,
                        "cancelledDate" to System.currentTimeMillis(),
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun generateDigitalCertificate(transferId: String): Result<String> {
        return try {
            val transfer = getTransfer(transferId).getOrNull()
            if (transfer != null) {
                val certificate = DigitalCertificate(
                    fowlId = transfer.fowlId,
                    ownerId = transfer.toUserId,
                    transferId = transferId,
                    certificateType = CertificateType.TRANSFER,
                    certificateNumber = "CERT-${System.currentTimeMillis()}",
                    issueDate = System.currentTimeMillis(),
                    isValid = true,
                    issuedBy = "Rooster Platform",
                    certificateVersion = "1.0"
                )
                createDigitalCertificate(certificate)
            } else {
                Result.Error(Exception("Transfer not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}