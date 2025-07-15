// generated/phase1/app/src/main/java/com/rio/rustry/auth/RoleManager.kt
package com.rio.rustry.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.data.model.UserType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "role_preferences")

@Singleton
class RoleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val roleKey = stringPreferencesKey("user_role")
    
    /**
     * Exposes current user role as a Flow
     * Combines Firebase Auth state with cached role from DataStore
     */
    fun currentRole(): Flow<UserType> {
        val authStateFlow = auth.authStateFlow()
        val cachedRoleFlow = context.dataStore.data.map { preferences ->
            preferences[roleKey]?.let { UserType.valueOf(it) }
        }
        
        return combine(authStateFlow, cachedRoleFlow) { user, cachedRole ->
            when {
                user == null -> UserType.GENERAL // Default for unauthenticated users
                cachedRole != null -> cachedRole // Use cached role if available
                else -> {
                    // Fetch from Firestore and cache
                    fetchAndCacheRole(user.uid)
                }
            }
        }
    }
    
    /**
     * Fetches user role from Firestore and caches it locally
     */
    private suspend fun fetchAndCacheRole(userId: String): UserType {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val role = document.getString("role")?.let { UserType.valueOf(it) } 
                ?: UserType.GENERAL
            
            // Cache the role
            cacheRole(role)
            role
        } catch (e: Exception) {
            // Fallback to GENERAL if fetch fails
            UserType.GENERAL
        }
    }
    
    /**
     * Updates user role in both Firestore and local cache
     */
    suspend fun updateRole(userId: String, newRole: UserType) {
        try {
            // Update in Firestore
            firestore.collection("users")
                .document(userId)
                .update("role", newRole.name)
                .await()
            
            // Update local cache
            cacheRole(newRole)
        } catch (e: Exception) {
            throw Exception("Failed to update user role: ${e.message}")
        }
    }
    
    /**
     * Caches role in DataStore for offline access
     */
    private suspend fun cacheRole(role: UserType) {
        context.dataStore.edit { preferences ->
            preferences[roleKey] = role.name
        }
    }
    
    /**
     * Clears cached role (useful on logout)
     */
    suspend fun clearCachedRole() {
        context.dataStore.edit { preferences ->
            preferences.remove(roleKey)
        }
    }
}

/**
 * Extension function to convert FirebaseAuth to Flow
 */
private fun FirebaseAuth.authStateFlow(): Flow<com.google.firebase.auth.FirebaseUser?> {
    return kotlinx.coroutines.flow.callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        addAuthStateListener(listener)
        awaitClose { removeAuthStateListener(listener) }
    }
}