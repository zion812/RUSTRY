package com.rio.rustry.domain.usecase

import com.rio.rustry.data.model.Fowl
import com.rio.rustry.domain.model.Result
import com.rio.rustry.domain.repository.FowlRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Use case for getting fowls with proper error handling and loading states
 * 
 * Implements clean architecture principles with coordinated data loading
 */
class GetFowlsUseCase(
    private val fowlRepository: FowlRepository
) {
    
    /**
     * Execute use case with loading state management
     */
    operator fun invoke(): Flow<Result<List<Fowl>>> {
        return fowlRepository.getFowls()
            .onStart { emit(Result.Loading) }
            .catch { exception ->
                emit(Result.Error(exception))
            }
    }
    
    /**
     * Get fowls by owner with pagination
     */
    fun byOwner(ownerId: String, page: Int = 0): Flow<Result<List<Fowl>>> {
        return fowlRepository.getFowlsByOwner(ownerId, page)
            .onStart { emit(Result.Loading) }
            .catch { exception ->
                emit(Result.Error(exception))
            }
    }
    
    /**
     * Get available fowls for marketplace
     */
    fun available(page: Int = 0): Flow<Result<List<Fowl>>> {
        return fowlRepository.getAvailableFowls(page)
            .onStart { emit(Result.Loading) }
            .catch { exception ->
                emit(Result.Error(exception))
            }
    }
    
    /**
     * Get fowls by breed
     */
    fun byBreed(breed: String): Flow<Result<List<Fowl>>> {
        return fowlRepository.getFowlsByBreed(breed)
            .onStart { emit(Result.Loading) }
            .catch { exception ->
                emit(Result.Error(exception))
            }
    }
    
    /**
     * Get fowls by price range
     */
    fun byPriceRange(minPrice: Double, maxPrice: Double): Flow<Result<List<Fowl>>> {
        return fowlRepository.getFowlsByPriceRange(minPrice, maxPrice)
            .onStart { emit(Result.Loading) }
            .catch { exception ->
                emit(Result.Error(exception))
            }
    }
}