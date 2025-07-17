package com.rio.rustry.data.local.dao

import androidx.room.*
import com.rio.rustry.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for user data operations
 */
@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<User?>
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber")
    suspend fun getUserByPhoneNumber(phoneNumber: String): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
    
    @Query("SELECT * FROM users WHERE isSynced = 0")
    suspend fun getUnsyncedUsers(): List<User>
    
    @Query("SELECT COUNT(*) FROM users WHERE isSynced = 0")
    suspend fun getUnsyncedUsersCount(): Int
    
    @Query("UPDATE users SET fcmToken = :token WHERE id = :userId")
    suspend fun updateFCMToken(userId: String, token: String)
    
    @Query("UPDATE users SET lastLoginAt = :timestamp WHERE id = :userId")
    suspend fun updateLastLogin(userId: String, timestamp: Long)
    
    @Query("UPDATE users SET isActive = :isActive WHERE id = :userId")
    suspend fun updateActiveStatus(userId: String, isActive: Boolean)
}