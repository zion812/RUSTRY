// generated/phase1/app/src/main/java/com/rio/rustry/auth/EnhancedUser.kt
package com.rio.rustry.auth

import com.google.firebase.Timestamp
import com.rio.rustry.data.model.UserType

/**
 * Enhanced User model with GDPR compliance fields
 * Extends the existing User model with additional fields for Phase 1
 */
data class EnhancedUser(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val role: UserType = UserType.GENERAL, // Updated from userType to role
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val nonTraceableListingsCount: Int = 0,
    
    // GDPR Compliance Fields
    val gdprConsent: Boolean = false,
    val gdprConsentTs: Timestamp? = null,
    val dataDeletionRequested: Boolean = false,
    val dataDeletionRequestedTs: Timestamp? = null
) {
    /**
     * Converts to legacy User model for backward compatibility
     */
    fun toLegacyUser(): com.rio.rustry.data.model.User {
        return com.rio.rustry.data.model.User(
            id = id,
            name = name,
            email = email,
            phone = phone,
            location = location,
            userType = role, // Map role back to userType
            profileImageUrl = profileImageUrl,
            createdAt = createdAt,
            nonTraceableListingsCount = nonTraceableListingsCount
        )
    }
    
    /**
     * Checks if user has given valid GDPR consent
     */
    fun hasValidGdprConsent(): Boolean {
        return gdprConsent && gdprConsentTs != null && !dataDeletionRequested
    }
    
    /**
     * Checks if user has requested data deletion
     */
    fun hasPendingDeletion(): Boolean {
        return dataDeletionRequested
    }
}

/**
 * Extension function to convert legacy User to EnhancedUser
 */
fun com.rio.rustry.data.model.User.toEnhancedUser(
    gdprConsent: Boolean = false,
    gdprConsentTs: Timestamp? = null,
    dataDeletionRequested: Boolean = false,
    dataDeletionRequestedTs: Timestamp? = null
): EnhancedUser {
    return EnhancedUser(
        id = id,
        name = name,
        email = email,
        phone = phone,
        location = location,
        role = userType, // Map userType to role
        profileImageUrl = profileImageUrl,
        createdAt = createdAt,
        nonTraceableListingsCount = nonTraceableListingsCount,
        gdprConsent = gdprConsent,
        gdprConsentTs = gdprConsentTs,
        dataDeletionRequested = dataDeletionRequested,
        dataDeletionRequestedTs = dataDeletionRequestedTs
    )
}