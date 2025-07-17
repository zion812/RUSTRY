// generated/phase3/app/src/main/java/com/rio/rustry/domain/usecase/VerifyTransferUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.analytics.AnalyticsService
import com.rio.rustry.domain.repository.TransferRepository
import com.rio.rustry.security.TransferSignatureService
import javax.inject.Inject

class VerifyTransferUseCase @Inject constructor(
    private val transferRepository: TransferRepository,
    private val signatureService: TransferSignatureService,
    private val analyticsService: AnalyticsService
) {
    suspend operator fun invoke(transferId: String): Boolean {
        val transfer = transferRepository.getTransfer(transferId)
        
        // Generate signed proof
        val proofData = mapOf(
            "transferId" to transferId,
            "fowlId" to transfer.fowlId,
            "fromUid" to transfer.fromUid,
            "toUid" to transfer.toUid,
            "timestamp" to transfer.timestamp.toString(),
            "proofUrls" to transfer.proofUrls.joinToString(",")
        )
        
        val signature = signatureService.signData(proofData)
        
        // Store signed proof
        transferRepository.addSignedProof(transferId, signature)
        
        // Trigger cloud function verification
        val verificationResult = transferRepository.triggerVerification(transferId)
        
        if (verificationResult) {
            analyticsService.logTransferVerified(
                transferId = transferId,
                verificationMethod = "ecdsa_signature"
            )
        }
        
        return verificationResult
    }
}