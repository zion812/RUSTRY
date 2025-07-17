package com.rio.rustry.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.data.model.User
import com.rio.rustry.data.model.UserType
import kotlinx.coroutines.tasks.await
import com.rio.rustry.di.FirebaseModule

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseModule.provideFirebaseAuth(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.Success(result.user!!)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun signUpWithEmail(
        email: String, 
        password: String, 
        name: String, 
        phone: String, 
        location: String, 
        userType: UserType
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!
            
            // Create user profile in Firestore
            val userProfile = User(
                id = user.uid,
                displayName = name,
                email = email,
                phoneNumber = phone,
                farmLocation = location,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            firestore.collection("users")
                .document(user.uid)
                .set(userProfile)
                .await()
            
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.Success(user)
            } else {
                Result.Error(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun getUserByEmail(email: String): Result<User> {
        return try {
            // Query Firestore for user by email
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                val user = document.toObject(User::class.java)
                if (user != null) {
                    Result.Success(user)
                } else {
                    Result.Error(Exception("User not found"))
                }
            } else {
                Result.Error(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
}