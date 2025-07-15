// generated/phase3/app/src/main/java/com/rio/rustry/domain/repository/TransferRepository.kt

package com.rio.rustry.domain.repository

import com.rio.rustry.data.model.Transfer

interface TransferRepository {
    suspend fun createTransfer(transfer: Transfer)
    suspend fun getTransfer(transferId: String): Transfer
    suspend fun getUserTransfers(): List<Transfer>
    suspend fun addProofData(transferId: String, proofType: String, proofData: String)
    suspend fun addSignedProof(transferId: String, signature: String)
    suspend fun triggerVerification(transferId: String): Boolean
    suspend fun updateTransferStatus(transferId: String, status: String)
}