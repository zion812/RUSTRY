// generated/phase1/app/src/main/java/com/rio/rustry/auth/EnhancedUserType.kt
package com.rio.rustry.auth

/**
 * Enhanced UserType enum with HIGH_LEVEL role for Phase 1
 * This extends the existing UserType to include the new role
 */
enum class EnhancedUserType {
    GENERAL,    // Buyer
    FARMER,     // Farmer/Seller  
    HIGH_LEVEL  // Admin/Manager
}

/**
 * Extension functions to convert between legacy and enhanced UserType
 */
fun com.rio.rustry.data.model.UserType.toEnhanced(): EnhancedUserType {
    return when (this) {
        com.rio.rustry.data.model.UserType.GENERAL -> EnhancedUserType.GENERAL
        com.rio.rustry.data.model.UserType.FARMER -> EnhancedUserType.FARMER
    }
}

fun EnhancedUserType.toLegacy(): com.rio.rustry.data.model.UserType {
    return when (this) {
        EnhancedUserType.GENERAL -> com.rio.rustry.data.model.UserType.GENERAL
        EnhancedUserType.FARMER -> com.rio.rustry.data.model.UserType.FARMER
        EnhancedUserType.HIGH_LEVEL -> com.rio.rustry.data.model.UserType.FARMER // Fallback for compatibility
    }
}