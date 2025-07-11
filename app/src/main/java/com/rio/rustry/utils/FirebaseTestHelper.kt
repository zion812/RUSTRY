package com.rio.rustry.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseTestHelper {
    
    fun checkFirebaseConfiguration(): String {
        return try {
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            val storage = FirebaseStorage.getInstance()
            
            val authApp = auth.app.name
            val firestoreApp = firestore.app.name
            val storageApp = storage.app.name
            
            """
            Firebase Configuration Status:
            ✅ Auth App: $authApp
            ✅ Firestore App: $firestoreApp  
            ✅ Storage App: $storageApp
            
            Current Auth User: ${auth.currentUser?.email ?: "None"}
            
            Note: If you see CONFIGURATION_NOT_FOUND error:
            1. Enable Email/Password in Firebase Console
            2. Download fresh google-services.json
            3. Ensure Firestore and Storage are enabled
            """.trimIndent()
            
        } catch (e: Exception) {
            "❌ Firebase Configuration Error: ${e.message}"
        }
    }
}