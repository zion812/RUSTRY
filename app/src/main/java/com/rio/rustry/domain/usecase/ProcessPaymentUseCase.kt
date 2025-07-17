package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.repository.UserRepository
import com.rio.rustry.domain.model.Result

class ProcessPaymentUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(amount: Double, paymentMethod: String): Result<Boolean> {
        return try {
            userRepository.processPayment(amount, paymentMethod)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}