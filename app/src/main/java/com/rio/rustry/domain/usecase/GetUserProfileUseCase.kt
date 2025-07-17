package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.repository.UserRepository
import com.rio.rustry.data.model.User
import com.rio.rustry.domain.model.Result

class GetUserProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User?> {
        return try {
            userRepository.getUserProfile(userId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}