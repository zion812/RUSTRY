package com.rio.rustry.domain.usecase

import com.rio.rustry.data.model.Fowl
import com.rio.rustry.domain.model.Result
import com.rio.rustry.domain.repository.FowlRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for updating an existing fowl with validation
 */
class UpdateFowlUseCase @Inject constructor(
    private val fowlRepository: FowlRepository,
    private val ioDispatcher: CoroutineDispatcher
) {
    
    suspend operator fun invoke(fowl: Fowl): Result<Unit> = withContext(ioDispatcher) {
        try {
            // Validate fowl data
            validateFowl(fowl)
            
            // Update timestamp and sync status
            val fowlToUpdate = fowl.copy(
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )
            
            // Update in repository
            fowlRepository.updateFowl(fowlToUpdate)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    private fun validateFowl(fowl: Fowl) {
        require(fowl.id.isNotBlank()) { "Fowl ID cannot be empty" }
        require(fowl.name.isNotBlank()) { "Fowl name cannot be empty" }
        require(fowl.breed.isNotBlank()) { "Fowl breed cannot be empty" }
        require(fowl.ownerId.isNotBlank()) { "Owner ID cannot be empty" }
        require(fowl.price >= 0) { "Price cannot be negative" }
        require(fowl.ageMonths >= 0) { "Age cannot be negative" }
    }
}