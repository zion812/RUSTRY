package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.repository.BreedingRepository
import com.rio.rustry.domain.repository.BreedingAnalytics
import com.rio.rustry.domain.model.Result
import javax.inject.Inject

class GetBreedingAnalyticsUseCase @Inject constructor(
    private val breedingRepository: BreedingRepository
) {
    suspend operator fun invoke(userId: String): Result<BreedingAnalytics> {
        return try {
            breedingRepository.getBreedingAnalytics(userId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}