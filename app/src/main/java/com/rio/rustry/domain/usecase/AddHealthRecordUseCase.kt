package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.model.HealthRecord
import com.rio.rustry.domain.repository.FarmRepository
import javax.inject.Inject

class AddHealthRecordUseCase @Inject constructor(
    private val farmRepository: FarmRepository
) {
    suspend operator fun invoke(record: HealthRecord): String {
        // Validation
        val validTypes = listOf("VACCINATION", "TREATMENT", "CHECKUP", "MEDICATION", "SURGERY", "ROUTINE_CARE")
        if (record.type.isBlank()) throw IllegalArgumentException("Health record type is required")
        if (!validTypes.contains(record.type.uppercase())) throw IllegalArgumentException("Invalid health record type")

        val currentTime = System.currentTimeMillis()
        if (record.date > currentTime) throw IllegalArgumentException("Date cannot be in the future")
        if (record.date < (currentTime - (365L * 24 * 60 * 60 * 1000 * 10))) throw IllegalArgumentException("Date cannot be more than 10 years ago")

        // Add more validations if needed, e.g. for details

        // Business logic
        val recordToAdd = record.copy(
            updatedAt = System.currentTimeMillis(),
            createdAt = if (record.createdAt == 0L) System.currentTimeMillis() else record.createdAt
        )

        return farmRepository.addHealthRecord(recordToAdd)
    }
}