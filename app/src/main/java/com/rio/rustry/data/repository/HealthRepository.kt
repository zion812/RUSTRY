package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.data.model.*
import kotlinx.coroutines.tasks.await
import com.rio.rustry.di.FirebaseModule

class HealthRepository(
    private val firestore: FirebaseFirestore = FirebaseModule.provideFirebaseFirestore()
) {
    
    suspend fun addHealthRecord(record: HealthRecord): Result<String> {
        return try {
            val docRef = firestore.collection("health_records").add(record).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getHealthRecords(fowlId: String): Result<List<HealthRecord>> {
        return try {
            val snapshot = firestore.collection("health_records")
                .whereEqualTo("fowlId", fowlId)
                .orderBy("date")
                .get()
                .await()
            
            val records = snapshot.documents.mapNotNull { doc ->
                doc.toObject(HealthRecord::class.java)?.copy(id = doc.id)
            }
            
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getHealthSummary(fowlId: String): Result<HealthSummary> {
        return try {
            val document = firestore.collection("health_summaries")
                .document(fowlId)
                .get()
                .await()
            
            val summary = document.toObject(HealthSummary::class.java)
            if (summary != null) {
                Result.success(summary)
            } else {
                // Create default summary if none exists
                val defaultSummary = HealthSummary(fowlId = fowlId)
                Result.success(defaultSummary)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateHealthSummary(summary: HealthSummary): Result<Unit> {
        return try {
            firestore.collection("health_summaries")
                .document(summary.fowlId)
                .set(summary)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getHealthReminders(fowlId: String): Result<List<HealthReminder>> {
        return try {
            val snapshot = firestore.collection("health_reminders")
                .whereEqualTo("fowlId", fowlId)
                .whereEqualTo("isCompleted", false)
                .orderBy("dueDate")
                .get()
                .await()
            
            val reminders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(HealthReminder::class.java)?.copy(id = doc.id)
            }
            
            Result.success(reminders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addHealthReminder(reminder: HealthReminder): Result<String> {
        return try {
            val docRef = firestore.collection("health_reminders").add(reminder).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getHealthAlerts(fowlId: String): Result<List<HealthAlert>> {
        return try {
            val snapshot = firestore.collection("health_alerts")
                .whereEqualTo("fowlId", fowlId)
                .whereEqualTo("isRead", false)
                .orderBy("createdAt")
                .get()
                .await()
            
            val alerts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(HealthAlert::class.java)?.copy(id = doc.id)
            }
            
            Result.success(alerts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markAlertAsRead(alertId: String): Result<Unit> {
        return try {
            firestore.collection("health_alerts")
                .document(alertId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAIHealthTips(
        category: TipCategory? = null,
        priority: TipPriority? = null
    ): Result<List<AIHealthTip>> {
        return try {
            var query = firestore.collection("ai_health_tips")
                .orderBy("priority")
                .orderBy("createdAt")
            
            category?.let {
                query = query.whereEqualTo("category", it.name)
            }
            
            val snapshot = query.get().await()
            
            var tips = snapshot.documents.mapNotNull { doc ->
                doc.toObject(AIHealthTip::class.java)?.copy(id = doc.id)
            }
            
            // Filter by priority in memory if needed
            priority?.let { filterPriority ->
                tips = tips.filter { it.priority == filterPriority }
            }
            
            Result.success(tips)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun bookmarkTip(tipId: String, isBookmarked: Boolean): Result<Unit> {
        return try {
            firestore.collection("ai_health_tips")
                .document(tipId)
                .update("isBookmarked", isBookmarked)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getOverdueReminders(fowlId: String): Result<List<HealthReminder>> {
        return try {
            val currentTime = java.util.Date()
            val snapshot = firestore.collection("health_reminders")
                .whereEqualTo("fowlId", fowlId)
                .whereEqualTo("isCompleted", false)
                .whereLessThan("dueDate", currentTime)
                .orderBy("dueDate")
                .get()
                .await()
            
            val reminders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(HealthReminder::class.java)?.copy(id = doc.id)
            }
            
            Result.success(reminders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun completeHealthReminder(reminderId: String): Result<Unit> {
        return try {
            firestore.collection("health_reminders")
                .document(reminderId)
                .update("isCompleted", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}