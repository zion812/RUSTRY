package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.repository.UserRepository
import com.rio.rustry.data.model.User
import com.rio.rustry.domain.model.Result

class UpdateUserProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> {
        return try {
            userRepository.updateUserProfile(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}