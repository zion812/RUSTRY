package com.rio.rustry.data.model

enum class UserType(val displayName: String) {
    FARMER("Farmer"),
    BUYER("Buyer"),
    VETERINARIAN("Veterinarian"),
    ADMIN("Admin"),
    GUEST("Guest")
}