package com.rio.rustry.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object FirebaseConfigChecker {
    
    suspend fun checkConfiguration(): ConfigurationResult {
        return try {
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            val storage = FirebaseStorage.getInstance()
            
            // Test Auth configuration
            val authConfigured = try {
                // Try to get auth settings (this will fail if not configured)
                auth.app.options.projectId != null
            } catch (e: Exception) {
                false
            }
            
            // Test Firestore configuration
            val firestoreConfigured = try {
                // Try to access Firestore settings
                firestore.app.options.projectId != null
            } catch (e: Exception) {
                false
            }
            
            // Test Storage configuration
            val storageConfigured = try {
                // Try to access Storage settings
                storage.app.options.storageBucket != null
            } catch (e: Exception) {
                false
            }
            
            ConfigurationResult(
                authEnabled = authConfigured,
                firestoreEnabled = firestoreConfigured,
                storageEnabled = storageConfigured,
                projectId = auth.app.options.projectId ?: "Unknown",
                storageBucket = storage.app.options.storageBucket ?: "Unknown",
                error = null
            )
            
        } catch (e: Exception) {
            ConfigurationResult(
                authEnabled = false,
                firestoreEnabled = false,
                storageEnabled = false,
                projectId = "Error",
                storageBucket = "Error",
                error = e.message
            )
        }
    }
    
    suspend fun testAuthConnection(): AuthTestResult {
        return try {
            val auth = FirebaseAuth.getInstance()
            
            // Try to create a test user (we'll delete it immediately)
            val testEmail = "test-${System.currentTimeMillis()}@rooster-test.com"
            val testPassword = "test123456"
            
            val result = auth.createUserWithEmailAndPassword(testEmail, testPassword).await()
            val user = result.user
            
            // Delete the test user immediately
            user?.delete()?.await()
            
            AuthTestResult(
                success = true,
                message = "✅ Email/Password authentication is working correctly!",
                error = null
            )
            
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("CONFIGURATION_NOT_FOUND") == true -> 
                    "❌ Email/Password authentication is not enabled in Firebase Console"
                e.message?.contains("network") == true -> 
                    "⚠️ Network error - check internet connection"
                else -> 
                    "⚠️ Auth test failed: ${e.message}"
            }
            
            AuthTestResult(
                success = false,
                message = message,
                error = e.message
            )
        }
    }
}

data class ConfigurationResult(
    val authEnabled: Boolean,
    val firestoreEnabled: Boolean,
    val storageEnabled: Boolean,
    val projectId: String,
    val storageBucket: String,
    val error: String?
) {
    val allEnabled: Boolean get() = authEnabled && firestoreEnabled && storageEnabled
    
    fun getStatusMessage(): String {
        return """
Firebase Configuration Status:
=============================

🔐 Authentication: ${if (authEnabled) "✅ Enabled" else "❌ Disabled"}
📊 Firestore Database: ${if (firestoreEnabled) "✅ Enabled" else "❌ Disabled"}
📁 Storage: ${if (storageEnabled) "✅ Enabled" else "❌ Disabled"}

Project ID: $projectId
Storage Bucket: $storageBucket

Overall Status: ${if (allEnabled) "✅ ALL SERVICES READY" else "⚠️ SOME SERVICES MISSING"}

${error?.let { "Error: $it" } ?: ""}
        """.trimIndent()
    }
}

data class AuthTestResult(
    val success: Boolean,
    val message: String,
    val error: String?
)