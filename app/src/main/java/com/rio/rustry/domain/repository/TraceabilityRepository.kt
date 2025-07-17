package com.rio.rustry.domain.repository

import com.rio.rustry.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for traceability operations
 */
interface TraceabilityRepository {
    
    // Fowl operations
    suspend fun createFowl(fowl: Fowl): Result<String>
    suspend fun updateFowl(fowl: Fowl): Result<Unit>
    suspend fun getFowlById(fowlId: String): Result<Fowl?>
    suspend fun getFowlsByOwner(ownerUserId: String): Flow<List<Fowl>>
    suspend fun getFowlsByBreed(breed: String): Flow<List<Fowl>>
    suspend fun getFowlsByStatus(status: FowlStatus): Flow<List<Fowl>>
    suspend fun searchFowls(query: String): Flow<List<Fowl>>
    suspend fun deleteFowl(fowlId: String): Result<Unit>
    
    // Fowl records operations
    suspend fun addFowlRecord(record: FowlRecord): Result<String>
    suspend fun updateFowlRecord(record: FowlRecord): Result<Unit>
    suspend fun getFowlRecords(fowlId: String): Flow<List<FowlRecord>>
    suspend fun getFowlRecordsByType(fowlId: String, recordType: RecordType): Flow<List<FowlRecord>>
    suspend fun getFowlRecordById(recordId: String): Result<FowlRecord?>
    suspend fun deleteFowlRecord(recordId: String): Result<Unit>
    
    // Health records operations
    suspend fun addHealthRecord(healthRecord: HealthRecord): Result<String>
    suspend fun getHealthRecords(fowlId: String): Flow<List<HealthRecord>>
    suspend fun getHealthRecordById(recordId: String): Result<HealthRecord?>
    suspend fun updateHealthRecord(healthRecord: HealthRecord): Result<Unit>
    
    // Feeding records operations
    suspend fun addFeedingRecord(feedingRecord: FeedingRecord): Result<String>
    suspend fun getFeedingRecords(fowlId: String): Flow<List<FeedingRecord>>
    suspend fun getFeedingRecordById(recordId: String): Result<FeedingRecord?>
    
    // Production records operations
    suspend fun addProductionRecord(productionRecord: ProductionRecord): Result<String>
    suspend fun getProductionRecords(fowlId: String): Flow<List<ProductionRecord>>
    suspend fun getProductionRecordById(recordId: String): Result<ProductionRecord?>
    
    // Transfer log operations
    suspend fun createTransferLog(transferLog: TransferLog): Result<String>
    suspend fun updateTransferLog(transferLog: TransferLog): Result<Unit>
    suspend fun getTransferLogById(transferId: String): Result<TransferLog?>
    suspend fun getTransferLogsByFowl(fowlId: String): Flow<List<TransferLog>>
    suspend fun getTransferLogsByUser(userId: String): Flow<List<TransferLog>>
    suspend fun getTransferLogsByStatus(status: TransferLogStatus): Flow<List<TransferLog>>
    suspend fun getPendingTransfers(userId: String): Flow<List<TransferLog>>
    
    // Transfer verification operations
    suspend fun addTransferVerificationDocument(document: TransferVerificationDocument): Result<String>
    suspend fun getTransferVerificationDocuments(transferId: String): Flow<List<TransferVerificationDocument>>
    suspend fun verifyTransferDocument(documentId: String, verifiedBy: String): Result<Unit>
    
    // Transfer dispute operations
    suspend fun createTransferDispute(dispute: TransferDispute): Result<String>
    suspend fun updateTransferDispute(dispute: TransferDispute): Result<Unit>
    suspend fun getTransferDisputes(transferId: String): Flow<List<TransferDispute>>
    suspend fun getDisputeById(disputeId: String): Result<TransferDispute?>
    
    // Family tree operations
    suspend fun getFamilyTree(fowlId: String): Result<List<Fowl>>
    suspend fun getParents(fowlId: String): Result<Pair<Fowl?, Fowl?>>
    suspend fun getChildren(fowlId: String): Flow<List<Fowl>>
    suspend fun getSiblings(fowlId: String): Flow<List<Fowl>>
    suspend fun getAncestors(fowlId: String, generations: Int = 5): Result<List<Fowl>>
    suspend fun getDescendants(fowlId: String, generations: Int = 5): Result<List<Fowl>>
    
    // Breeding operations
    suspend fun addBreedingRecord(breedingRecord: BreedingRecord): Result<String>
    suspend fun getBreedingRecords(fowlId: String): Flow<List<BreedingRecord>>
    suspend fun getBreedingRecordById(recordId: String): Result<BreedingRecord?>
    suspend fun updateBreedingRecord(breedingRecord: BreedingRecord): Result<Unit>
    
    // Performance metrics operations
    suspend fun updatePerformanceMetrics(metrics: PerformanceMetrics): Result<Unit>
    suspend fun getPerformanceMetrics(fowlId: String): Result<PerformanceMetrics?>
    suspend fun getPerformanceMetricsByOwner(ownerUserId: String): Flow<List<PerformanceMetrics>>
    
    // Analytics and reporting
    suspend fun getFowlCountByOwner(ownerUserId: String): Result<Int>
    suspend fun getFowlCountByBreed(breed: String): Result<Int>
    suspend fun getTransferStatistics(userId: String): Result<Map<String, Int>>
    suspend fun getHealthStatistics(fowlId: String): Result<Map<String, Any>>
    suspend fun getProductionStatistics(fowlId: String): Result<Map<String, Double>>
    
    // Verification and compliance
    suspend fun verifyFowlOwnership(fowlId: String, userId: String): Result<Boolean>
    suspend fun getOwnershipHistory(fowlId: String): Flow<List<TransferLog>>
    suspend fun validateFowlData(fowl: Fowl): Result<List<String>>
    suspend fun generateTraceabilityReport(fowlId: String): Result<String>
    
    // Synchronization
    suspend fun syncFowlData(fowlId: String): Result<Unit>
    suspend fun syncUserFowls(userId: String): Result<Unit>
    suspend fun getLastSyncTime(userId: String): Result<Long?>
    suspend fun updateLastSyncTime(userId: String, timestamp: Long): Result<Unit>
}