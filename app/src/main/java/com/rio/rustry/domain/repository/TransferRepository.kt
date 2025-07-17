package com.rio.rustry.domain.repository

import com.rio.rustry.data.model.Transfer
import com.rio.rustry.domain.model.Result

interface TransferRepository {
    suspend fun getTransfer(transferId: String): Transfer
    suspend fun getUserTransfers(): List<Transfer>
    suspend fun createTransfer(transfer: Transfer): Result<String>
    suspend fun updateTransferStatus(transferId: String, status: TransferStatus): Result<Unit>
    suspend fun addProofData(transferId: String, proofType: String, proofData: String): Result<Unit>
    suspend fun verifyTransfer(transferId: String): Result<Unit>
}

enum class TransferStatus {
    PENDING, VERIFIED, REJECTED, COMPLETED
}