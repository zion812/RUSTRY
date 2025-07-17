package com.rio.rustry.domain.usecase

import com.rio.rustry.data.model.User
import com.rio.rustry.domain.model.Result
import com.rio.rustry.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case for user login with validation
 */
class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    
    suspend operator fun invoke(email: String, password: String): Result<User> {
        // Validate input
        if (email.isBlank()) {
            return Result.Error(IllegalArgumentException("Email cannot be empty"))
        }
        
        if (password.isBlank()) {
            return Result.Error(IllegalArgumentException("Password cannot be empty"))
        }
        
        if (!isValidEmail(email)) {
            return Result.Error(IllegalArgumentException("Invalid email format"))
        }
        
        if (password.length < 6) {
            return Result.Error(IllegalArgumentException("Password must be at least 6 characters"))
        }
        
        return userRepository.login(email.trim(), password)
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}