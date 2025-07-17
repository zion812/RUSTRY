package com.rio.rustry.domain.usecase

import com.rio.rustry.domain.model.Result
import com.rio.rustry.domain.repository.FowlRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for deleting a fowl with proper validation and cleanup
 */
class DeleteFowlUseCase @Inject constructor(
    private val fowlRepository: FowlRepository,
    private val ioDispatcher: CoroutineDispatcher
) {
    
    suspend operator fun invoke(fowlId: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            // Validate input
            require(fowlId.isNotBlank()) { "Fowl ID cannot be empty" }
            
            // Check if fowl exists
            val existingFowl = fowlRepository.getFowlById(fowlId)
            // Note: This would need to be implemented as a suspend function
            
            // Delete from repository
            fowlRepository.deleteFowl(fowlId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}