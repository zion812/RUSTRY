package com.rio.rustry.domain.repository

import com.rio.rustry.data.model.Fowl
import com.rio.rustry.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for fowl operations
 * 
 * Provides clean architecture separation between domain and data layers
 * with proper error handling and reactive data streams
 */
interface FowlRepository {
    
    /**
     * Get all fowls with real-time updates and offline support
     */
    fun getFowls(): Flow<Result<List<Fowl>>>
    
    /**
     * Get fowl by ID with caching
     */
    fun getFowlById(id: String): Flow<Result<Fowl?>>
    
    /**
     * Get fowls by owner with pagination
     */
    fun getFowlsByOwner(ownerId: String, page: Int = 0, pageSize: Int = 20): Flow<Result<List<Fowl>>>
    
    /**
     * Get available fowls for marketplace
     */
    fun getAvailableFowls(page: Int = 0, pageSize: Int = 20): Flow<Result<List<Fowl>>>
    
    /**
     * Search fowls with debouncing and caching
     */
    fun searchFowls(query: String): Flow<Result<List<Fowl>>>
    
    /**
     * Get fowls by breed with filtering
     */
    fun getFowlsByBreed(breed: String): Flow<Result<List<Fowl>>>
    
    /**
     * Get fowls by price range
     */
    fun getFowlsByPriceRange(minPrice: Double, maxPrice: Double): Flow<Result<List<Fowl>>>
    
    /**
     * Add fowl with validation and traceability
     */
    suspend fun addFowl(fowl: Fowl): Result<Unit>
    
    /**
     * Update fowl with optimistic updates
     */
    suspend fun updateFowl(fowl: Fowl): Result<Unit>
    
    /**
     * Delete fowl with cleanup
     */
    suspend fun deleteFowl(fowlId: String): Result<Unit>
    
    /**
     * Sync data with remote server
     */
    suspend fun syncData(): Result<Unit>
    
    /**
     * Check if data is synced
     */
    suspend fun isSynced(): Boolean
    
    /**
     * Get offline fowls count
     */
    suspend fun getOfflineFowlsCount(): Int
    
    /**
     * Force refresh from remote
     */
    suspend fun refreshFromRemote(): Result<Unit>
}