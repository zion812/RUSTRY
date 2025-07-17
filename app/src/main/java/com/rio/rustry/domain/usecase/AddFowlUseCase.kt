package com.rio.rustry.domain.usecase

import com.rio.rustry.data.model.Fowl
import com.rio.rustry.domain.model.Result
import com.rio.rustry.domain.repository.FowlRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for adding a new fowl with validation and business logic
 */
class AddFowlUseCase @Inject constructor(
    private val fowlRepository: FowlRepository,
    private val ioDispatcher: CoroutineDispatcher
) {
    
    suspend operator fun invoke(fowl: Fowl): Result<Unit> = withContext(ioDispatcher) {
        try {
            // Validate fowl data
            validateFowl(fowl)
            
            // Add timestamps and generate ID if needed
            val fowlToAdd = fowl.copy(
                id = if (fowl.id.isEmpty()) generateFowlId() else fowl.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )
            
            // Add to repository
            fowlRepository.addFowl(fowlToAdd)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    private fun validateFowl(fowl: Fowl) {
        require(fowl.name.isNotBlank()) { "Fowl name cannot be empty" }
        require(fowl.breed.isNotBlank()) { "Fowl breed cannot be empty" }
        require(fowl.ownerId.isNotBlank()) { "Owner ID cannot be empty" }
        require(fowl.price >= 0) { "Price cannot be negative" }
        require(fowl.ageMonths >= 0) { "Age cannot be negative" }
    }
    
    private fun generateFowlId(): String {
        return "fowl_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}