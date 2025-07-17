// generated/phase1/app/src/main/java/com/rio/rustry/auth/GdprConsentManager.kt
package com.rio.rustry.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private val Context.gdprDataStore: DataStore<Preferences> by preferencesDataStore(name = "gdpr_preferences")

@Singleton
class GdprConsentManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) {
    private val consentKey = booleanPreferencesKey("gdpr_consent")
    
    companion object {
        const val PRIVACY_POLICY_URL = "https://rustry.app/privacy"
    }
    
    /**
     * Exposes GDPR consent status as a Flow
     */
    fun isConsented(): Flow<Boolean> {
        return context.gdprDataStore.data.map { preferences ->
            preferences[consentKey] ?: false
        }
    }
    
    /**
     * Records user consent in both Firestore and local cache
     */
    suspend fun recordConsent(): Result<Unit> {
        val currentUser = auth.currentUser
            ?: return Result.Error(Exception("User not authenticated"))
        
        return try {
            val now = Timestamp.now()
            
            // Update Firestore
            firestore.collection("users")
                .document(currentUser.uid)
                .update(
                    mapOf(
                        "gdprConsent" to true,
                        "gdprConsentTs" to now
                    )
                )
                .await()
            
            // Update local cache
            context.gdprDataStore.edit { preferences ->
                preferences[consentKey] = true
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(Exception("Failed to record consent: ${e.message}"))
        }
    }
    
    /**
     * Requests data deletion by setting flag and triggering Cloud Function
     */
    suspend fun requestDeletion(): Result<Unit> {
        val currentUser = auth.currentUser
            ?: return Result.Error(Exception("User not authenticated"))
        
        return try {
            val now = Timestamp.now()
            
            // Update Firestore with deletion request
            firestore.collection("users")
                .document(currentUser.uid)
                .update(
                    mapOf(
                        "dataDeletionRequested" to true,
                        "dataDeletionRequestedTs" to now
                    )
                )
                .await()
            
            // Trigger Cloud Function for data deletion
            val data = hashMapOf(
                "userId" to currentUser.uid,
                "requestedAt" to now.toDate().time
            )
            
            functions.getHttpsCallable("deleteUserData")
                .call(data)
                .await()
            
            // Clear local consent cache
            context.gdprDataStore.edit { preferences ->
                preferences[consentKey] = false
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(Exception("Failed to request deletion: ${e.message}"))
        }
    }
    
    /**
     * Checks if current user has given GDPR consent
     */
    suspend fun checkUserConsent(): Boolean {
        val currentUser = auth.currentUser ?: return false
        
        return try {
            val document = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()
            
            val gdprConsent = document.getBoolean("gdprConsent") ?: false
            val deletionRequested = document.getBoolean("dataDeletionRequested") ?: false
            
            // Update local cache
            context.gdprDataStore.edit { preferences ->
                preferences[consentKey] = gdprConsent && !deletionRequested
            }
            
            gdprConsent && !deletionRequested
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Clears local consent cache (useful on logout)
     */
    suspend fun clearConsentCache() {
        context.gdprDataStore.edit { preferences ->
            preferences.remove(consentKey)
        }
    }
}