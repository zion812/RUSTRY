package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.model.Flock
import com.rio.rustry.domain.repository.FarmRepository
import javax.inject.Inject

class UpdateFlockUseCase @Inject constructor(
    private val farmRepository: FarmRepository
) {
    suspend operator fun invoke(flock: Flock) {
        // Validation
        if (flock.breed.isBlank()) throw IllegalArgumentException("Breed is required")
        if (flock.breed.length < 2) throw IllegalArgumentException("Breed must be at least 2 characters")
        if (flock.breed.length > 50) throw IllegalArgumentException("Breed must be less than 50 characters")
        if (!flock.breed.matches(Regex("^[a-zA-Z\\s\\-_'.]+$"))) throw IllegalArgumentException("Breed contains invalid characters")

        if (flock.quantity < 1) throw IllegalArgumentException("Quantity must be at least 1")
        if (flock.quantity > 100000) throw IllegalArgumentException("Quantity cannot exceed 100000")

        if (flock.ageMonths < 0) throw IllegalArgumentException("Age cannot be negative")
        if (flock.ageMonths > 120) throw IllegalArgumentException("Age cannot exceed 120 months")

        // Business logic
        val flockToUpdate = flock.copy(
            updatedAt = System.currentTimeMillis()
        )

        farmRepository.updateFlock(flockToUpdate)
    }
}