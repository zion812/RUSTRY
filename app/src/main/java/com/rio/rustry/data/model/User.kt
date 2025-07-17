package com.rio.rustry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * User data model for authentication and profile management
 */
@Entity(tableName = "users")
@Serializable
data class User(
    @PrimaryKey
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val isVerified: Boolean = false,
    val fcmToken: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val lastLoginAt: Long = 0L,
    
    // Profile information
    val farmName: String = "",
    val farmLocation: String = "",
    val farmSize: String = "",
    val experienceYears: Int = 0,
    val specialization: List<String> = emptyList(),
    
    // Preferences
    val notificationsEnabled: Boolean = true,
    val marketingEmailsEnabled: Boolean = false,
    val language: String = "en",
    val currency: String = "INR",
    
    // Business information
    val businessType: String = "", // INDIVIDUAL, FARM, DEALER
    val gstNumber: String = "",
    val panNumber: String = "",
    val bankAccountDetails: BankAccountDetails? = null,
    
    // Statistics
    val totalFowls: Int = 0,
    val totalSales: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    
    // Status
    val isActive: Boolean = true,
    val isSuspended: Boolean = false,
    val suspensionReason: String = "",
    
    // Sync status
    val isSynced: Boolean = true,
    val needsSync: Boolean = false
)

/**
 * Bank account details for payments
 */
@Serializable
data class BankAccountDetails(
    val accountNumber: String = "",
    val ifscCode: String = "",
    val accountHolderName: String = "",
    val bankName: String = "",
    val branchName: String = "",
    val accountType: String = "", // SAVINGS, CURRENT
    val isVerified: Boolean = false
)