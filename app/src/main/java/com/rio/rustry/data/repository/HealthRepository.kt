package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rio.rustry.data.model.HealthRecord
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class HealthRepository(
    private val firestore: FirebaseFirestore
) {
    private val healthRecordsCollection = firestore.collection("health_records")
    
    fun getHealthRecords(fowlId: String): Flow<List<HealthRecord>> = callbackFlow {
        val listener = healthRecordsCollection
            .whereEqualTo("fowlId", fowlId)
            .orderBy("recordDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val records = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(HealthRecord::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(records)
            }
        
        awaitClose { listener.remove() }
    }
    
    suspend fun addHealthRecord(record: HealthRecord) {
        val recordMap = mapOf(
            "fowlId" to record.fowlId,
            "recordType" to record.recordType,
            "notes" to record.notes,
            "recordDate" to record.recordDate,
            "veterinarianId" to record.veterinarianId,
            "status" to record.status,
            "medications" to record.medications,
            "dosageInstructions" to record.dosageInstructions,
            "totalCost" to record.totalCost,
            "images" to record.images,
            "nextCheckupDate" to record.nextCheckupDate,
            "createdAt" to record.createdAt,
            "updatedAt" to record.updatedAt
        )
        
        if (record.id.isNotEmpty()) {
            healthRecordsCollection.document(record.id).set(recordMap).await()
        } else {
            healthRecordsCollection.add(recordMap).await()
        }
    }
    
    suspend fun updateHealthRecord(record: HealthRecord) {
        val recordMap = mapOf(
            "fowlId" to record.fowlId,
            "recordType" to record.recordType,
            "notes" to record.notes,
            "recordDate" to record.recordDate,
            "veterinarianId" to record.veterinarianId,
            "status" to record.status,
            "medications" to record.medications,
            "dosageInstructions" to record.dosageInstructions,
            "totalCost" to record.totalCost,
            "images" to record.images,
            "nextCheckupDate" to record.nextCheckupDate,
            "updatedAt" to Date()
        )
        
        healthRecordsCollection.document(record.id).update(recordMap).await()
    }
    
    suspend fun deleteHealthRecord(recordId: String) {
        healthRecordsCollection.document(recordId).delete().await()
    }
    
    suspend fun getHealthRecord(recordId: String): HealthRecord? {
        return try {
            val doc = healthRecordsCollection.document(recordId).get().await()
            doc.toObject(HealthRecord::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getHealthRecordsByType(fowlId: String, type: String): List<HealthRecord> {
        return try {
            val snapshot = healthRecordsCollection
                .whereEqualTo("fowlId", fowlId)
                .whereEqualTo("recordType", type)
                .orderBy("recordDate", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(HealthRecord::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getUpcomingAppointments(fowlId: String): List<HealthRecord> {
        return try {
            val currentDate = Date()
            val snapshot = healthRecordsCollection
                .whereEqualTo("fowlId", fowlId)
                .whereGreaterThan("nextCheckupDate", currentDate)
                .orderBy("nextCheckupDate", Query.Direction.ASCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(HealthRecord::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}