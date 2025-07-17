package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rio.rustry.domain.model.*
import com.rio.rustry.domain.repository.TraceabilityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TraceabilityRepository using Firebase Firestore
 */
@Singleton
class TraceabilityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TraceabilityRepository {

    companion object {
        private const val FOWLS_COLLECTION = "fowls"
        private const val FOWL_RECORDS_COLLECTION = "fowl_records"
        private const val HEALTH_RECORDS_COLLECTION = "health_records"
        private const val FEEDING_RECORDS_COLLECTION = "feeding_records"
        private const val PRODUCTION_RECORDS_COLLECTION = "production_records"
        private const val TRANSFER_LOGS_COLLECTION = "transfer_logs"
        private const val TRANSFER_DOCUMENTS_COLLECTION = "transfer_documents"
        private const val TRANSFER_DISPUTES_COLLECTION = "transfer_disputes"
        private const val BREEDING_RECORDS_COLLECTION = "breeding_records"
        private const val PERFORMANCE_METRICS_COLLECTION = "performance_metrics"
        private const val SYNC_STATUS_COLLECTION = "sync_status"
    }

    // Fowl operations
    override suspend fun createFowl(fowl: Fowl): Result<String> {
        return try {
            val docRef = firestore.collection(FOWLS_COLLECTION).document()
            val fowlWithId = fowl.copy(fowlId = docRef.id)
            docRef.set(fowlWithId).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateFowl(fowl: Fowl): Result<Unit> {
        return try {
            firestore.collection(FOWLS_COLLECTION)
                .document(fowl.fowlId)
                .set(fowl.copy(updatedAt = System.currentTimeMillis()))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getFowlById(fowlId: String): Result<Fowl?> {
        return try {
            val document = firestore.collection(FOWLS_COLLECTION)
                .document(fowlId)
                .get()
                .await()
            val fowl = document.toObject(Fowl::class.java)
            Result.Success(fowl)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getFowlsByOwner(ownerUserId: String): Flow<List<Fowl>> = flow {
        try {
            val snapshot = firestore.collection(FOWLS_COLLECTION)
                .whereEqualTo("ownerUserId", ownerUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val fowls = snapshot.toObjects(Fowl::class.java)
            emit(fowls)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getFowlsByBreed(breed: String): Flow<List<Fowl>> = flow {
        try {
            val snapshot = firestore.collection(FOWLS_COLLECTION)
                .whereEqualTo("breed", breed)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val fowls = snapshot.toObjects(Fowl::class.java)
            emit(fowls)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getFowlsByStatus(status: FowlStatus): Flow<List<Fowl>> = flow {
        try {
            val snapshot = firestore.collection(FOWLS_COLLECTION)
                .whereEqualTo("status", status.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val fowls = snapshot.toObjects(Fowl::class.java)
            emit(fowls)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun searchFowls(query: String): Flow<List<Fowl>> = flow {
        try {
            // Simple search implementation - can be enhanced with full-text search
            val snapshot = firestore.collection(FOWLS_COLLECTION)
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()
            val fowls = snapshot.toObjects(Fowl::class.java)
            emit(fowls)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun deleteFowl(fowlId: String): Result<Unit> {
        return try {
            firestore.collection(FOWLS_COLLECTION)
                .document(fowlId)
                .delete()
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Fowl records operations
    override suspend fun addFowlRecord(record: FowlRecord): Result<String> {
        return try {
            val docRef = firestore.collection(FOWL_RECORDS_COLLECTION).document()
            val recordWithId = record.copy(recordId = docRef.id)
            docRef.set(recordWithId).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateFowlRecord(record: FowlRecord): Result<Unit> {
        return try {
            firestore.collection(FOWL_RECORDS_COLLECTION)
                .document(record.recordId)
                .set(record.copy(updatedAt = System.currentTimeMillis()))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getFowlRecords(fowlId: String): Flow<List<FowlRecord>> = flow {
        try {
            val snapshot = firestore.collection(FOWL_RECORDS_COLLECTION)
                .whereEqualTo("fowlId", fowlId)
                .orderBy("recordDate", Query.Direction.DESCENDING)
                .get()
                .await()
            val records = snapshot.toObjects(FowlRecord::class.java)
            emit(records)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getFowlRecordsByType(fowlId: String, recordType: RecordType): Flow<List<FowlRecord>> = flow {
        try {
            val snapshot = firestore.collection(FOWL_RECORDS_COLLECTION)
                .whereEqualTo("fowlId", fowlId)
                .whereEqualTo("recordType", recordType.name)
                .orderBy("recordDate", Query.Direction.DESCENDING)
                .get()
                .await()
            val records = snapshot.toObjects(FowlRecord::class.java)
            emit(records)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getFowlRecordById(recordId: String): Result<FowlRecord?> {
        return try {
            val document = firestore.collection(FOWL_RECORDS_COLLECTION)
                .document(recordId)
                .get()
                .await()
            val record = document.toObject(FowlRecord::class.java)
            Result.Success(record)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteFowlRecord(recordId: String): Result<Unit> {
        return try {
            firestore.collection(FOWL_RECORDS_COLLECTION)
                .document(recordId)
                .delete()
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Health records operations
    override suspend fun addHealthRecord(healthRecord: HealthRecord): Result<String> {
        return try {
            val docRef = firestore.collection(HEALTH_RECORDS_COLLECTION).document()
            val recordWithId = healthRecord.copy(recordId = docRef.id)
            docRef.set(recordWithId).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getHealthRecords(fowlId: String): Flow<List<HealthRecord>> = flow {
        try {
            val snapshot = firestore.collection(HEALTH_RECORDS_COLLECTION)
                .whereEqualTo("fowlId", fowlId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val records = snapshot.toObjects(HealthRecord::class.java)
            emit(records)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getHealthRecordById(recordId: String): Result<HealthRecord?> {
        return try {
            val document = firestore.collection(HEALTH_RECORDS_COLLECTION)
                .document(recordId)
                .get()
                .await()
            val record = document.toObject(HealthRecord::class.java)
            Result.Success(record)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateHealthRecord(healthRecord: HealthRecord): Result<Unit> {
        return try {
            firestore.collection(HEALTH_RECORDS_COLLECTION)
                .document(healthRecord.recordId)
                .set(healthRecord)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Feeding records operations
    override suspend fun addFeedingRecord(feedingRecord: FeedingRecord): Result<String> {
        return try {
            val docRef = firestore.collection(FEEDING_RECORDS_COLLECTION).document()
            val recordWithId = feedingRecord.copy(recordId = docRef.id)
            docRef.set(recordWithId).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getFeedingRecords(fowlId: String): Flow<List<FeedingRecord>> = flow {
        try {
            val snapshot = firestore.collection(FEEDING_RECORDS_COLLECTION)
                .whereEqualTo("fowlId", fowlId)
                .orderBy("feedingTime", Query.Direction.DESCENDING)
                .get()
                .await()
            val records = snapshot.toObjects(FeedingRecord::class.java)
            emit(records)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getFeedingRecordById(recordId: String): Result<FeedingRecord?> {
        return try {
            val document = firestore.collection(FEEDING_RECORDS_COLLECTION)
                .document(recordId)
                .get()
                .await()
            val record = document.toObject(FeedingRecord::class.java)
            Result.Success(record)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Production records operations
    override suspend fun addProductionRecord(productionRecord: ProductionRecord): Result<String> {
        return try {
            val docRef = firestore.collection(PRODUCTION_RECORDS_COLLECTION).document()
            val recordWithId = productionRecord.copy(recordId = docRef.id)
            docRef.set(recordWithId).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getProductionRecords(fowlId: String): Flow<List<ProductionRecord>> = flow {
        try {
            val snapshot = firestore.collection(PRODUCTION_RECORDS_COLLECTION)
                .whereEqualTo("fowlId", fowlId)
                .orderBy("productionDate", Query.Direction.DESCENDING)
                .get()
                .await()
            val records = snapshot.toObjects(ProductionRecord::class.java)
            emit(records)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getProductionRecordById(recordId: String): Result<ProductionRecord?> {
        return try {
            val document = firestore.collection(PRODUCTION_RECORDS_COLLECTION)
                .document(recordId)
                .get()
                .await()
            val record = document.toObject(ProductionRecord::class.java)
            Result.Success(record)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Transfer log operations
    override suspend fun createTransferLog(transferLog: TransferLog): Result<String> {
        return try {
            val docRef = firestore.collection(TRANSFER_LOGS_COLLECTION).document()
            val logWithId = transferLog.copy(transferId = docRef.id)
            docRef.set(logWithId).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateTransferLog(transferLog: TransferLog): Result<Unit> {
        return try {
            firestore.collection(TRANSFER_LOGS_COLLECTION)
                .document(transferLog.transferId)
                .set(transferLog.copy(updatedAt = System.currentTimeMillis()))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTransferLogById(transferId: String): Result<TransferLog?> {
        return try {
            val document = firestore.collection(TRANSFER_LOGS_COLLECTION)
                .document(transferId)
                .get()
                .await()
            val transferLog = document.toObject(TransferLog::class.java)
            Result.Success(transferLog)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTransferLogsByFowl(fowlId: String): Flow<List<TransferLog>> = flow {
        try {
            val snapshot = firestore.collection(TRANSFER_LOGS_COLLECTION)
                .whereEqualTo("fowlId", fowlId)
                .orderBy("transferDate", Query.Direction.DESCENDING)
                .get()
                .await()
            val logs = snapshot.toObjects(TransferLog::class.java)
            emit(logs)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getTransferLogsByUser(userId: String): Flow<List<TransferLog>> = flow {
        try {
            val fromSnapshot = firestore.collection(TRANSFER_LOGS_COLLECTION)
                .whereEqualTo("fromUserId", userId)
                .get()
                .await()
            
            val toSnapshot = firestore.collection(TRANSFER_LOGS_COLLECTION)
                .whereEqualTo("toUserId", userId)
                .get()
                .await()
            
            val fromLogs = fromSnapshot.toObjects(TransferLog::class.java)
            val toLogs = toSnapshot.toObjects(TransferLog::class.java)
            val allLogs = (fromLogs + toLogs).distinctBy { it.transferId }
                .sortedByDescending { it.transferDate }
            
            emit(allLogs)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getTransferLogsByStatus(status: TransferLogStatus): Flow<List<TransferLog>> = flow {
        try {
            val snapshot = firestore.collection(TRANSFER_LOGS_COLLECTION)
                .whereEqualTo("transferStatus", status.name)
                .orderBy("transferDate", Query.Direction.DESCENDING)
                .get()
                .await()
            val logs = snapshot.toObjects(TransferLog::class.java)
            emit(logs)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getPendingTransfers(userId: String): Flow<List<TransferLog>> = flow {
        try {
            val snapshot = firestore.collection(TRANSFER_LOGS_COLLECTION)
                .whereEqualTo("toUserId", userId)
                .whereEqualTo("transferStatus", TransferLogStatus.PENDING.name)
                .orderBy("transferDate", Query.Direction.DESCENDING)
                .get()
                .await()
            val logs = snapshot.toObjects(TransferLog::class.java)
            emit(logs)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Transfer verification operations
    override suspend fun addTransferVerificationDocument(document: TransferVerificationDocument): Result<String> {
        return try {
            val docRef = firestore.collection(TRANSFER_DOCUMENTS_COLLECTION).document()
            val docWithId = document.copy(documentId = docRef.id)
            docRef.set(docWithId).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTransferVerificationDocuments(transferId: String): Flow<List<TransferVerificationDocument>> = flow {
        try {
            val snapshot = firestore.collection(TRANSFER_DOCUMENTS_COLLECTION)
                .whereEqualTo("transferId", transferId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val documents = snapshot.toObjects(TransferVerificationDocument::class.java)
            emit(documents)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun verifyTransferDocument(documentId: String, verifiedBy: String): Result<Unit> {
        return try {
            firestore.collection(TRANSFER_DOCUMENTS_COLLECTION)
                .document(documentId)
                .update(
                    mapOf(
                        "isVerified" to true,
                        "verifiedBy" to verifiedBy,
                        "verificationDate" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Transfer dispute operations
    override suspend fun createTransferDispute(dispute: TransferDispute): Result<String> {
        return try {
            val docRef = firestore.collection(TRANSFER_DISPUTES_COLLECTION).document()
            val disputeWithId = dispute.copy(disputeId = docRef.id)
            docRef.set(disputeWithId).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateTransferDispute(dispute: TransferDispute): Result<Unit> {
        return try {
            firestore.collection(TRANSFER_DISPUTES_COLLECTION)
                .document(dispute.disputeId)
                .set(dispute.copy(updatedAt = System.currentTimeMillis()))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTransferDisputes(transferId: String): Flow<List<TransferDispute>> = flow {
        try {
            val snapshot = firestore.collection(TRANSFER_DISPUTES_COLLECTION)
                .whereEqualTo("transferId", transferId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val disputes = snapshot.toObjects(TransferDispute::class.java)
            emit(disputes)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getDisputeById(disputeId: String): Result<TransferDispute?> {
        return try {
            val document = firestore.collection(TRANSFER_DISPUTES_COLLECTION)
                .document(disputeId)
                .get()
                .await()
            val dispute = document.toObject(TransferDispute::class.java)
            Result.Success(dispute)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Family tree operations
    override suspend fun getFamilyTree(fowlId: String): Result<List<Fowl>> {
        return try {
            val fowl = getFowlById(fowlId).getOrNull()
            if (fowl == null) {
                return Result.Success(emptyList())
            }
            
            val familyMembers = mutableListOf<Fowl>()
            familyMembers.add(fowl)
            
            // Get parents
            val parents = getParents(fowlId).getOrNull()
            parents?.first?.let { familyMembers.add(it) }
            parents?.second?.let { familyMembers.add(it) }
            
            // Get children
            val childrenSnapshot = firestore.collection(FOWLS_COLLECTION)
                .whereArrayContains("parentIds", fowlId)
                .get()
                .await()
            val children = childrenSnapshot.toObjects(Fowl::class.java)
            familyMembers.addAll(children)
            
            Result.Success(familyMembers.distinctBy { it.fowlId })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getParents(fowlId: String): Result<Pair<Fowl?, Fowl?>> {
        return try {
            val fowl = getFowlById(fowlId).getOrNull()
            if (fowl == null) {
                return Result.Success(Pair(null, null))
            }
            
            val maleParent = if (fowl.parentMaleId.isNotEmpty()) {
                getFowlById(fowl.parentMaleId).getOrNull()
            } else null
            
            val femaleParent = if (fowl.parentFemaleId.isNotEmpty()) {
                getFowlById(fowl.parentFemaleId).getOrNull()
            } else null
            
            Result.Success(Pair(maleParent, femaleParent))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getChildren(fowlId: String): Flow<List<Fowl>> = flow {
        try {
            val snapshot = firestore.collection(FOWLS_COLLECTION)
                .whereIn("parentMaleId", listOf(fowlId))
                .get()
                .await()
            
            val snapshot2 = firestore.collection(FOWLS_COLLECTION)
                .whereIn("parentFemaleId", listOf(fowlId))
                .get()
                .await()
            
            val children1 = snapshot.toObjects(Fowl::class.java)
            val children2 = snapshot2.toObjects(Fowl::class.java)
            val allChildren = (children1 + children2).distinctBy { it.fowlId }
            
            emit(allChildren)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getSiblings(fowlId: String): Flow<List<Fowl>> = flow {
        try {
            val fowl = getFowlById(fowlId).getOrNull()
            if (fowl == null) {
                emit(emptyList())
                return@flow
            }
            
            val siblings = mutableListOf<Fowl>()
            
            if (fowl.parentMaleId.isNotEmpty()) {
                val snapshot = firestore.collection(FOWLS_COLLECTION)
                    .whereEqualTo("parentMaleId", fowl.parentMaleId)
                    .get()
                    .await()
                siblings.addAll(snapshot.toObjects(Fowl::class.java))
            }
            
            if (fowl.parentFemaleId.isNotEmpty()) {
                val snapshot = firestore.collection(FOWLS_COLLECTION)
                    .whereEqualTo("parentFemaleId", fowl.parentFemaleId)
                    .get()
                    .await()
                siblings.addAll(snapshot.toObjects(Fowl::class.java))
            }
            
            val uniqueSiblings = siblings.distinctBy { it.fowlId }
                .filter { it.fowlId != fowlId }
            
            emit(uniqueSiblings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getAncestors(fowlId: String, generations: Int): Result<List<Fowl>> {
        return try {
            val ancestors = mutableListOf<Fowl>()
            val visited = mutableSetOf<String>()
            
            suspend fun collectAncestors(currentFowlId: String, currentGeneration: Int) {
                if (currentGeneration >= generations || currentFowlId in visited) return
                visited.add(currentFowlId)
                
                val parents = getParents(currentFowlId).getOrNull()
                parents?.first?.let { parent ->
                    ancestors.add(parent)
                    collectAncestors(parent.fowlId, currentGeneration + 1)
                }
                parents?.second?.let { parent ->
                    ancestors.add(parent)
                    collectAncestors(parent.fowlId, currentGeneration + 1)
                }
            }
            
            collectAncestors(fowlId, 0)
            Result.Success(ancestors.distinctBy { it.fowlId })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getDescendants(fowlId: String, generations: Int): Result<List<Fowl>> {
        return try {
            val descendants = mutableListOf<Fowl>()
            val visited = mutableSetOf<String>()
            
            suspend fun collectDescendants(currentFowlId: String, currentGeneration: Int) {
                if (currentGeneration >= generations || currentFowlId in visited) return
                visited.add(currentFowlId)
                
                getChildren(currentFowlId).collect { children ->
                    descendants.addAll(children)
                    children.forEach { child ->
                        collectDescendants(child.fowlId, currentGeneration + 1)
                    }
                }
            }
            
            collectDescendants(fowlId, 0)
            Result.Success(descendants.distinctBy { it.fowlId })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Breeding operations
    override suspend fun addBreedingRecord(breedingRecord: BreedingRecord): Result<String> {
        return try {
            val docRef = firestore.collection(BREEDING_RECORDS_COLLECTION).document()
            val recordWithId = breedingRecord.copy(id = docRef.id)
            docRef.set(recordWithId).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getBreedingRecords(fowlId: String): Flow<List<BreedingRecord>> = flow {
        try {
            val snapshot = firestore.collection(BREEDING_RECORDS_COLLECTION)
                .whereEqualTo("fowlId", fowlId)
                .orderBy("breedingDate", Query.Direction.DESCENDING)
                .get()
                .await()
            val records = snapshot.toObjects(BreedingRecord::class.java)
            emit(records)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getBreedingRecordById(recordId: String): Result<BreedingRecord?> {
        return try {
            val document = firestore.collection(BREEDING_RECORDS_COLLECTION)
                .document(recordId)
                .get()
                .await()
            val record = document.toObject(BreedingRecord::class.java)
            Result.Success(record)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateBreedingRecord(breedingRecord: BreedingRecord): Result<Unit> {
        return try {
            firestore.collection(BREEDING_RECORDS_COLLECTION)
                .document(breedingRecord.id)
                .set(breedingRecord)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Performance metrics operations
    override suspend fun updatePerformanceMetrics(metrics: PerformanceMetrics): Result<Unit> {
        return try {
            firestore.collection(PERFORMANCE_METRICS_COLLECTION)
                .document(metrics.fowlId)
                .set(metrics.copy(lastUpdated = System.currentTimeMillis()))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getPerformanceMetrics(fowlId: String): Result<PerformanceMetrics?> {
        return try {
            val document = firestore.collection(PERFORMANCE_METRICS_COLLECTION)
                .document(fowlId)
                .get()
                .await()
            val metrics = document.toObject(PerformanceMetrics::class.java)
            Result.Success(metrics)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getPerformanceMetricsByOwner(ownerUserId: String): Flow<List<PerformanceMetrics>> = flow {
        try {
            // First get all fowls by owner
            val fowlsSnapshot = firestore.collection(FOWLS_COLLECTION)
                .whereEqualTo("ownerUserId", ownerUserId)
                .get()
                .await()
            val fowls = fowlsSnapshot.toObjects(Fowl::class.java)
            
            // Then get performance metrics for each fowl
            val metrics = mutableListOf<PerformanceMetrics>()
            fowls.forEach { fowl ->
                val metricsResult = getPerformanceMetrics(fowl.fowlId).getOrNull()
                metricsResult?.let { metrics.add(it) }
            }
            
            emit(metrics)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Analytics and reporting
    override suspend fun getFowlCountByOwner(ownerUserId: String): Result<Int> {
        return try {
            val snapshot = firestore.collection(FOWLS_COLLECTION)
                .whereEqualTo("ownerUserId", ownerUserId)
                .get()
                .await()
            Result.Success(snapshot.size())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getFowlCountByBreed(breed: String): Result<Int> {
        return try {
            val snapshot = firestore.collection(FOWLS_COLLECTION)
                .whereEqualTo("breed", breed)
                .get()
                .await()
            Result.Success(snapshot.size())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getTransferStatistics(userId: String): Result<Map<String, Int>> {
        return try {
            val fromSnapshot = firestore.collection(TRANSFER_LOGS_COLLECTION)
                .whereEqualTo("fromUserId", userId)
                .get()
                .await()
            
            val toSnapshot = firestore.collection(TRANSFER_LOGS_COLLECTION)
                .whereEqualTo("toUserId", userId)
                .get()
                .await()
            
            val stats = mapOf(
                "totalSent" to fromSnapshot.size(),
                "totalReceived" to toSnapshot.size(),
                "totalTransfers" to (fromSnapshot.size() + toSnapshot.size())
            )
            
            Result.Success(stats)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getHealthStatistics(fowlId: String): Result<Map<String, Any>> {
        return try {
            val healthRecordsSnapshot = firestore.collection(HEALTH_RECORDS_COLLECTION)
                .whereEqualTo("fowlId", fowlId)
                .get()
                .await()
            
            val vaccinationRecordsSnapshot = firestore.collection(FOWL_RECORDS_COLLECTION)
                .whereEqualTo("fowlId", fowlId)
                .whereEqualTo("recordType", RecordType.VACCINATION.name)
                .get()
                .await()
            
            val stats = mapOf(
                "totalHealthRecords" to healthRecordsSnapshot.size(),
                "totalVaccinations" to vaccinationRecordsSnapshot.size(),
                "lastHealthCheck" to (healthRecordsSnapshot.documents.maxOfOrNull { 
                    it.getLong("createdAt") ?: 0L 
                } ?: 0L)
            )
            
            Result.Success(stats)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getProductionStatistics(fowlId: String): Result<Map<String, Double>> {
        return try {
            val productionRecordsSnapshot = firestore.collection(PRODUCTION_RECORDS_COLLECTION)
                .whereEqualTo("fowlId", fowlId)
                .get()
                .await()
            
            val records = productionRecordsSnapshot.toObjects(ProductionRecord::class.java)
            
            val stats = mapOf(
                "totalEggs" to records.sumOf { it.eggsLaid.toDouble() },
                "averageEggWeight" to (records.map { it.averageEggWeight }.average().takeIf { !it.isNaN() } ?: 0.0),
                "totalRecords" to records.size.toDouble()
            )
            
            Result.Success(stats)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Verification and compliance
    override suspend fun verifyFowlOwnership(fowlId: String, userId: String): Result<Boolean> {
        return try {
            val fowl = getFowlById(fowlId).getOrNull()
            Result.Success(fowl?.ownerUserId == userId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getOwnershipHistory(fowlId: String): Flow<List<TransferLog>> {
        return getTransferLogsByFowl(fowlId)
    }

    override suspend fun validateFowlData(fowl: Fowl): Result<List<String>> {
        return try {
            val errors = mutableListOf<String>()
            
            if (fowl.name.isBlank()) errors.add("Fowl name is required")
            if (fowl.breed.isBlank()) errors.add("Breed is required")
            if (fowl.ownerUserId.isBlank()) errors.add("Owner ID is required")
            if (fowl.gender.isBlank()) errors.add("Gender is required")
            
            Result.Success(errors)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun generateTraceabilityReport(fowlId: String): Result<String> {
        return try {
            val fowl = getFowlById(fowlId).getOrNull()
            if (fowl == null) {
                return Result.Error(Exception("Fowl not found"))
            }
            
            // This would generate a comprehensive traceability report
            // For now, returning a simple report ID
            val reportId = "TRACE_${fowlId}_${System.currentTimeMillis()}"
            Result.Success(reportId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Synchronization
    override suspend fun syncFowlData(fowlId: String): Result<Unit> {
        return try {
            // Implementation for syncing fowl data
            // This could involve updating local cache, resolving conflicts, etc.
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun syncUserFowls(userId: String): Result<Unit> {
        return try {
            // Implementation for syncing all fowls for a user
            updateLastSyncTime(userId, System.currentTimeMillis())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getLastSyncTime(userId: String): Result<Long?> {
        return try {
            val document = firestore.collection(SYNC_STATUS_COLLECTION)
                .document(userId)
                .get()
                .await()
            val lastSync = document.getLong("lastSyncTime")
            Result.Success(lastSync)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateLastSyncTime(userId: String, timestamp: Long): Result<Unit> {
        return try {
            firestore.collection(SYNC_STATUS_COLLECTION)
                .document(userId)
                .set(mapOf("lastSyncTime" to timestamp))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}