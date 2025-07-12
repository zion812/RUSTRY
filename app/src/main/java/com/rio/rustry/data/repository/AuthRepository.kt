package com.rio.rustry.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.data.model.User
import com.rio.rustry.data.model.UserType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
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
                name = name,
                email = email,
                phone = phone,
                location = location,
                userType = userType
            )
            
            firestore.collection("users")
                .document(user.uid)
                .set(userProfile)
                .await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
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
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserByEmail(email: String): Result<User> {
        return try {
            if (networkUtils.isOnline()) {
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
                        // Cache the user data locally
                        userDao.insertUser(user.toEntity())
                        Result.success(user)
                    } else {
                        Result.failure(Exception("User not found"))
                    }
                } else {
                    Result.failure(Exception("User not found"))
                }
            } else {
                Result.failure(Exception("Cannot search users while offline"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
}