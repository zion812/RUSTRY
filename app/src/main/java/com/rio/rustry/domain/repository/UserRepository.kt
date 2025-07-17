package com.rio.rustry.domain.repository

import com.rio.rustry.data.model.User
import com.rio.rustry.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for user operations
 */
interface UserRepository {
    
    /**
     * Get current user with real-time updates
     */
    fun getCurrentUser(): Flow<Result<User?>>
    
    /**
     * Login user with email and password
     */
    suspend fun login(email: String, password: String): Result<User>
    
    /**
     * Register new user
     */
    suspend fun register(email: String, password: String, displayName: String): Result<User>
    
    /**
     * Logout current user
     */
    suspend fun logout(): Result<Unit>
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(user: User): Result<Unit>
    
    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String): Result<User?>
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Flow<Boolean>
    
    /**
     * Reset password
     */
    suspend fun resetPassword(email: String): Result<Unit>
    
    /**
     * Update FCM token
     */
    suspend fun updateFCMToken(token: String): Result<Unit>
    
    /**
     * Get user profile
     */
    suspend fun getUserProfile(userId: String): Result<User?>
    
    /**
     * Update user profile
     */
    suspend fun updateUserProfile(user: User): Result<Unit>
    
    /**
     * Process payment
     */
    suspend fun processPayment(amount: Double, paymentMethod: String): Result<Boolean>
}