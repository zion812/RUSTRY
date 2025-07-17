package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.model.Result
import com.rio.rustry.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case for user logout with cleanup
 */
class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    
    suspend operator fun invoke(): Result<Unit> {
        return userRepository.logout()
    }
}