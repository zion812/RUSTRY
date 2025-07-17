package com.rio.rustry

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

/**
 * Firebase service for Plan B - Simplified Firebase integration
 * Enables all Firebase components: Auth, Firestore, Storage, Analytics, Crashlytics
 */
class FirebaseService(private val context: Context? = null) {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val analytics = context?.let { FirebaseAnalytics.getInstance(it) }
    private val crashlytics = FirebaseCrashlytics.getInstance()
    
    // Authentication
    fun getCurrentUser() = auth.currentUser
    
    fun signOut() {
        auth.signOut()
        analytics?.logEvent("user_logout", null)
    }
    
    // Firestore - Fowl Management
    suspend fun addFowl(fowl: FowlData): Result<String> {
        return try {
            val fowlId = UUID.randomUUID().toString()
            val fowlWithId = fowl.copy(
                id = fowlId,
                ownerId = getCurrentUser()?.uid ?: "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            firestore.collection("fowls")
                .document(fowlId)
                .set(fowlWithId)
                .await()
            
            analytics?.logEvent("fowl_added", null)
            Result.Success(fowlId)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.Error(e)
        }
    }
    
    suspend fun getFowls(): Result<List<FowlData>> {
        return try {
            val snapshot = firestore.collection("fowls")
                .whereEqualTo("ownerId", getCurrentUser()?.uid)
                .get()
                .await()
            
            val fowls = snapshot.documents.mapNotNull { doc ->
                doc.toObject(FowlData::class.java)
            }
            
            analytics?.logEvent("fowls_fetched", null)
            Result.Success(fowls)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.Error(e)
        }
    }
    
    suspend fun getMarketplaceFowls(): Result<List<FowlData>> {
        return try {
            val snapshot = firestore.collection("fowls")
                .whereEqualTo("isForSale", true)
                .limit(50)
                .get()
                .await()
            
            val fowls = snapshot.documents.mapNotNull { doc ->
                doc.toObject(FowlData::class.java)
            }
            
            analytics?.logEvent("marketplace_viewed", null)
            Result.Success(fowls)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.Error(e)
        }
    }
    
    // Storage - Image Upload
    suspend fun uploadFowlImage(fowlId: String, imageUri: Uri): Result<String> {
        return try {
            val imageRef = storage.reference
                .child("fowls/$fowlId/${UUID.randomUUID()}.jpg")
            
            val uploadTask = imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            
            analytics?.logEvent("image_uploaded", null)
            Result.Success(downloadUrl.toString())
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.Error(e)
        }
    }
    
    // User Profile Management
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val userId = getCurrentUser()?.uid ?: throw Exception("User not authenticated")
            
            firestore.collection("users")
                .document(userId)
                .set(profile)
                .await()
            
            analytics?.logEvent("profile_updated", null)
            Result.Success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.Error(e)
        }
    }
    
    suspend fun getUserProfile(): Result<UserProfile?> {
        return try {
            val userId = getCurrentUser()?.uid ?: throw Exception("User not authenticated")
            
            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val profile = snapshot.toObject(UserProfile::class.java)
            Result.Success(profile)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.Error(e)
        }
    }
    
    // Analytics Events
    fun logScreenView(screenName: String) {
        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }
    
    fun logFowlPurchase(fowlId: String, price: Double) {
        analytics?.logEvent(FirebaseAnalytics.Event.PURCHASE) {
            param(FirebaseAnalytics.Param.ITEM_ID, fowlId)
            param(FirebaseAnalytics.Param.VALUE, price)
            param(FirebaseAnalytics.Param.CURRENCY, "INR")
        }
    }
    
    // Test Firebase Connection
    suspend fun testFirebaseConnection(): Result<String> {
        return try {
            // Test Firestore
            firestore.collection("test")
                .document("connection")
                .set(mapOf("timestamp" to System.currentTimeMillis()))
                .await()
            
            // Test Analytics
            analytics?.logEvent("firebase_test", null)
            
            // Test Crashlytics
            crashlytics.log("Firebase connection test successful")
            
            Result.Success("All Firebase services connected successfully")
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.Error(e)
        }
    }
}

// Data Models for Plan B
data class FowlData(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val breed: String = "",
    val age: Int = 0,
    val price: Double = 0.0,
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val isForSale: Boolean = false,
    val location: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val location: String = "",
    val profileImageUrl: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

// Extension function for analytics
inline fun FirebaseAnalytics.logEvent(name: String, block: ParametersBuilder.() -> Unit) {
    val builder = ParametersBuilder()
    builder.block()
    logEvent(name, builder.bundle)
}

class ParametersBuilder {
    val bundle = android.os.Bundle()
    
    fun param(key: String, value: String) {
        bundle.putString(key, value)
    }
    
    fun param(key: String, value: Double) {
        bundle.putDouble(key, value)
    }
    
    fun param(key: String, value: Long) {
        bundle.putLong(key, value)
    }
}