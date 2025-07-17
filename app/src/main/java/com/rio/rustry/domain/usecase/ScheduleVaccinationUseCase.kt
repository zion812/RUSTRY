package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.repository.HealthRepository
import com.rio.rustry.domain.repository.VaccinationSchedule
import com.rio.rustry.domain.model.Result

class ScheduleVaccinationUseCase(
    private val healthRepository: HealthRepository
) {
    suspend operator fun invoke(schedule: VaccinationSchedule): Result<String> {
        return try {
            healthRepository.scheduleVaccination(schedule)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}