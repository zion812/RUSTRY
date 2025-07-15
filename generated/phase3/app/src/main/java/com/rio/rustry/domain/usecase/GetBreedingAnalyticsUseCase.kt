// generated/phase3/app/src/main/java/com/rio/rustry/domain/usecase/GetBreedingAnalyticsUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.dashboard.AnalyticsPeriod
import com.rio.rustry.dashboard.BreedingAnalytics
import com.rio.rustry.domain.repository.BreedingRepository
import javax.inject.Inject

class GetBreedingAnalyticsUseCase @Inject constructor(
    private val breedingRepository: BreedingRepository
) {
    suspend operator fun invoke(period: AnalyticsPeriod): BreedingAnalytics {
        val endDate = System.currentTimeMillis()
        val startDate = endDate - (period.days * 24 * 60 * 60 * 1000L)
        
        return breedingRepository.getBreedingAnalytics(startDate, endDate, period)
    }
}