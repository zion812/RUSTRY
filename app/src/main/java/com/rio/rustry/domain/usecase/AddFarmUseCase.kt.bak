package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.model.Farm
import com.rio.rustry.domain.repository.FarmRepository
import javax.inject.Inject

class AddFarmUseCase @Inject constructor(
    private val farmRepository: FarmRepository
) {
    suspend operator fun invoke(farm: Farm): String {
        val errors = validate(farm)
        if (errors.isNotEmpty()) {
            throw IllegalArgumentException(errors.entries.joinToString("\n") { "${it.key.replaceFirstChar { it.titlecase() }}: ${it.value}" })
        }

        // Business logic
        val farmToAdd = farm.copy(
            updatedAt = System.currentTimeMillis(),
            createdAt = if (farm.createdAt == 0L) System.currentTimeMillis() else farm.createdAt
        )

        return farmRepository.addFarm(farmToAdd)
    }

    fun validate(farm: Farm): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (farm.name.isBlank()) {
            errors["name"] = "Farm name is required"
        } else if (farm.name.length < 2) {
            errors["name"] = "Farm name must be at least 2 characters"
        } else if (farm.name.length > 100) {
            errors["name"] = "Farm name must be less than 100 characters"
        } else if (!farm.name.matches(Regex("^[a-zA-Z0-9\\s\\-_'.]+$"))) {
            errors["name"] = "Farm name contains invalid characters"
        }

        if (farm.location.isBlank()) {
            errors["location"] = "Location is required"
        } else if (farm.location.length < 3) {
            errors["location"] = "Location must be at least 3 characters"
        } else if (farm.location.length > 200) {
            errors["location"] = "Location must be less than 200 characters"
        }

        if (farm.size < 0.1) {
            errors["size"] = "Farm size must be at least 0.1 acres"
        } else if (farm.size > 10000.0) {
            errors["size"] = "Farm size cannot exceed 10000 acres"
        }

        return errors
    }
}
