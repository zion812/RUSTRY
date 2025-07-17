package com.rio.rustry.domain.usecase

import com.rio.rustry.data.model.User
import com.rio.rustry.domain.model.Result
import com.rio.rustry.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case for user registration with validation
 */
class RegisterUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    
    suspend operator fun invoke(email: String, password: String, displayName: String): Result<User> {
        // Validate input
        if (email.isBlank()) {
            return Result.Error(IllegalArgumentException("Email cannot be empty"))
        }
        
        if (password.isBlank()) {
            return Result.Error(IllegalArgumentException("Password cannot be empty"))
        }
        
        if (displayName.isBlank()) {
            return Result.Error(IllegalArgumentException("Display name cannot be empty"))
        }
        
        if (!isValidEmail(email)) {
            return Result.Error(IllegalArgumentException("Invalid email format"))
        }
        
        if (password.length < 6) {
            return Result.Error(IllegalArgumentException("Password must be at least 6 characters"))
        }
        
        if (displayName.length < 2) {
            return Result.Error(IllegalArgumentException("Display name must be at least 2 characters"))
        }
        
        if (!isStrongPassword(password)) {
            return Result.Error(IllegalArgumentException("Password must contain at least one uppercase letter, one lowercase letter, and one number"))
        }
        
        return userRepository.register(email.trim(), password, displayName.trim())
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isStrongPassword(password: String): Boolean {
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        return hasUppercase && hasLowercase && hasDigit
    }
}