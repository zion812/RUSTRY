package com.rio.rustry.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.data.local.dao.UserDao
import com.rio.rustry.data.model.User
import com.rio.rustry.domain.model.Result
import com.rio.rustry.domain.repository.UserRepository
import com.rio.rustry.utils.ErrorHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * Implementation of UserRepository with Firebase Auth and local caching
 */
class UserRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val localUserDao: UserDao,
    private val ioDispatcher: CoroutineDispatcher
) : UserRepository {
    
    companion object {
        private const val TAG = "UserRepositoryImpl"
        private const val USERS_COLLECTION = "users"
    }
    
    override fun getCurrentUser(): Flow<Result<User?>> = flow {
        try {
            emit(Result.Loading)
            
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                emit(Result.Success(null))
                return@flow
            }
            
            // Try to get from local cache first
            val cachedUser = withContext(ioDispatcher) {
                localUserDao.getUserById(firebaseUser.uid)
            }
            
            if (cachedUser != null) {
                emit(Result.Success(cachedUser))
            }
            
            // Fetch from Firestore
            try {
                val userDoc = firestore.collection(USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .get()
                    .await()
                
                if (userDoc.exists()) {
                    val user = userDoc.toObject(User::class.java)?.copy(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: "",
                        phoneNumber = firebaseUser.phoneNumber ?: "",
                        isVerified = firebaseUser.isEmailVerified
                    )
                    
                    if (user != null) {
                        // Update local cache
                        withContext(ioDispatcher) {
                            localUserDao.insertUser(user)
                        }
                        emit(Result.Success(user))
                    }
                } else if (cachedUser == null) {
                    emit(Result.Success(null))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch user from Firestore", e)
                if (cachedUser == null) {
                    emit(Result.Error(ErrorHandler.handleError(e)))
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in getCurrentUser", e)
            emit(Result.Error(ErrorHandler.handleError(e)))
        }
    }.flowOn(ioDispatcher)
    
    override suspend fun login(email: String, password: String): Result<User> = withContext(ioDispatcher) {
        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return@withContext Result.Error(Exception("Login failed"))
            
            // Update last login time
            localUserDao.updateLastLogin(firebaseUser.uid, System.currentTimeMillis())
            
            // Get user data
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()
            
            val user = if (userDoc.exists()) {
                userDoc.toObject(User::class.java)?.copy(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    lastLoginAt = System.currentTimeMillis()
                ) ?: createUserFromFirebaseUser(firebaseUser)
            } else {
                createUserFromFirebaseUser(firebaseUser)
            }
            
            // Cache user locally
            localUserDao.insertUser(user)
            
            Result.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun register(email: String, password: String, displayName: String): Result<User> = withContext(ioDispatcher) {
        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return@withContext Result.Error(Exception("Registration failed"))
            
            // Update display name
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
            
            // Create user document
            val user = User(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )
            
            // Save to Firestore
            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .set(user)
                .await()
            
            // Cache locally
            localUserDao.insertUser(user)
            
            Result.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed", e)
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun logout(): Result<Unit> = withContext(ioDispatcher) {
        try {
            firebaseAuth.signOut()
            // Clear local user cache
            localUserDao.deleteAllUsers()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Logout failed", e)
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun updateProfile(user: User): Result<Unit> = withContext(ioDispatcher) {
        try {
            val updatedUser = user.copy(
                updatedAt = System.currentTimeMillis(),
                needsSync = true
            )
            
            // Update locally first
            localUserDao.updateUser(updatedUser)
            
            // Update in Firestore
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .set(updatedUser)
                .await()
            
            // Mark as synced
            localUserDao.updateUser(updatedUser.copy(needsSync = false, isSynced = true))
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Profile update failed", e)
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun getUserById(userId: String): Result<User?> = withContext(ioDispatcher) {
        try {
            // Check local cache first
            val cachedUser = localUserDao.getUserById(userId)
            if (cachedUser != null) {
                return@withContext Result.Success(cachedUser)
            }
            
            // Fetch from Firestore
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            if (userDoc.exists()) {
                val user = userDoc.toObject(User::class.java)
                if (user != null) {
                    localUserDao.insertUser(user)
                }
                Result.Success(user)
            } else {
                Result.Success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user by ID", e)
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override fun isAuthenticated(): Flow<Boolean> = flow {
        emit(firebaseAuth.currentUser != null)
    }.flowOn(ioDispatcher)
    
    override suspend fun resetPassword(email: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Password reset failed", e)
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun updateFCMToken(token: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            val currentUser = firebaseAuth.currentUser
                ?: return@withContext Result.Error(Exception("User not authenticated"))
            
            // Update locally
            localUserDao.updateFCMToken(currentUser.uid, token)
            
            // Update in Firestore
            firestore.collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .update(mapOf(
                    "fcmToken" to token,
                    "updatedAt" to System.currentTimeMillis()
                ))
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "FCM token update failed", e)
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun getUserProfile(userId: String): Result<User?> = withContext(ioDispatcher) {
        return@withContext getUserById(userId)
    }
    
    override suspend fun updateUserProfile(user: User): Result<Unit> = withContext(ioDispatcher) {
        return@withContext updateProfile(user)
    }
    
    override suspend fun processPayment(amount: Double, paymentMethod: String): Result<Boolean> = withContext(ioDispatcher) {
        try {
            // Mock payment processing - in real implementation this would integrate with payment gateway
            Log.d(TAG, "Processing payment: $amount via $paymentMethod")
            
            // Simulate payment processing delay
            kotlinx.coroutines.delay(2000)
            
            // Mock success (90% success rate)
            val success = (0..100).random() > 10
            
            if (success) {
                Log.d(TAG, "Payment successful")
                Result.Success(true)
            } else {
                Log.e(TAG, "Payment failed")
                Result.Success(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Payment processing error", e)
            Result.Error(ErrorHandler.handleError(e))
        }
    }
    
    private fun createUserFromFirebaseUser(firebaseUser: com.google.firebase.auth.FirebaseUser): User {
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            phoneNumber = firebaseUser.phoneNumber ?: "",
            isVerified = firebaseUser.isEmailVerified,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )
    }
}