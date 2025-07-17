package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.repository.HealthRepository
import com.rio.rustry.data.model.HealthRecord
import com.rio.rustry.domain.model.Result

class GetHealthHistoryUseCase(
    private val healthRepository: HealthRepository
) {
    suspend operator fun invoke(fowlId: String): Result<List<HealthRecord>> {
        return try {
            healthRepository.getHealthHistory(fowlId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}