package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.model.InventoryItem
import com.rio.rustry.domain.repository.FarmRepository
import javax.inject.Inject

class ManageInventoryUseCase @Inject constructor(
    private val farmRepository: FarmRepository
) {
    suspend operator fun invoke(item: InventoryItem): String {
        // Validation
        val validTypes = listOf("FEED", "MEDICINE", "EQUIPMENT", "SUPPLIES", "BEDDING", "SUPPLEMENTS")
        if (item.type.isBlank()) throw IllegalArgumentException("Item type is required")
        if (!validTypes.contains(item.type.uppercase())) throw IllegalArgumentException("Invalid item type")

        if (item.name.isBlank()) throw IllegalArgumentException("Item name is required")

        if (item.quantity < 0) throw IllegalArgumentException("Quantity cannot be negative")
        if (item.quantity > 1000000) throw IllegalArgumentException("Quantity cannot exceed 1000000")

        if (item.restockThreshold < 0) throw IllegalArgumentException("Threshold cannot be negative")

        // Business logic
        val itemToAdd = item.copy(
            updatedAt = System.currentTimeMillis(),
            createdAt = if (item.createdAt == 0L) System.currentTimeMillis() else item.createdAt
        )

        return farmRepository.addInventoryItem(itemToAdd)
    }
}