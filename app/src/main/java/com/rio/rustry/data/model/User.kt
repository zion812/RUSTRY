package com.rio.rustry.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val userType: UserType = UserType.GENERAL,
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val nonTraceableListingsCount: Int = 0
)

enum class UserType {
    GENERAL, // Buyer
    FARMER   // Farmer/Seller
}