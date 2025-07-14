package com.rio.rustry.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.rio.rustry.data.local.FowlDao
import kotlinx.coroutines.flow.first
import com.rio.rustry.data.local.entity.toDomainModel
import com.rio.rustry.data.local.entity.toEntity
import com.rio.rustry.data.model.Fowl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Fowl data operations
 * 
 * Features:
 * - Offline-first architecture
 * - Automatic caching
 * - Pagination support
 * - Real-time updates via Flow
 * - Sync management
 */
@Singleton
class FowlRepository @Inject constructor(
    private val fowlDao: FowlDao
) {
    
    companion object {
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 5
    }
    
    // Basic CRUD operations
    suspend fun getFowlById(id: String): Fowl? {
        return fowlDao.getFowlById(id)?.toDomainModel()
    }
    
    fun getFowlByIdFlow(id: String): Flow<Fowl?> {
        return fowlDao.getFowlByIdFlow(id).map { it?.toDomainModel() }
    }
    
    suspend fun insertFowl(fowl: Fowl) {
        fowlDao.insertFowl(fowl.toEntity())
    }
    
    suspend fun insertFowls(fowls: List<Fowl>) {
        fowlDao.insertFowls(fowls.map { it.toEntity() })
    }
    
    suspend fun updateFowl(fowl: Fowl) {
        fowlDao.updateFowl(fowl.toEntity())
    }
    
    suspend fun deleteFowl(fowl: Fowl) {
        fowlDao.deleteFowl(fowl.toEntity())
    }
    
    suspend fun deleteFowlById(id: String) {
        fowlDao.deleteFowlById(id)
    }
    
    // Marketplace operations with pagination
    fun getAvailableFowls(): Flow<PagingData<Fowl>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { fowlDao.getAvailableFowls() }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }
    
    fun getAvailableFowlsFlow(): Flow<List<Fowl>> {
        return fowlDao.getAvailableFowlsFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getFowlsByOwner(ownerId: String): Flow<List<Fowl>> {
        return fowlDao.getFowlsByOwner(ownerId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getFowlsByOwnerPaging(ownerId: String): Flow<PagingData<Fowl>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { fowlDao.getFowlsByOwnerPaging(ownerId) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }
    
    // Search and filter operations
    fun searchFowls(query: String): Flow<PagingData<Fowl>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { fowlDao.searchFowls(query) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }
    
    fun getFowlsByBreed(breed: String): Flow<PagingData<Fowl>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { fowlDao.getFowlsByBreed(breed) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }
    
    fun getFowlsByLocation(location: String): Flow<PagingData<Fowl>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { fowlDao.getFowlsByLocation(location) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }
    
    fun getFowlsByPriceRange(minPrice: Double, maxPrice: Double): Flow<PagingData<Fowl>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { fowlDao.getFowlsByPriceRange(minPrice, maxPrice) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }
    
    // Analytics and reporting
    suspend fun getFowlCountByOwner(ownerId: String): Int {
        return fowlDao.getFowlCountByOwner(ownerId)
    }
    
    suspend fun getAvailableFowlCount(): Int {
        return fowlDao.getAvailableFowlCount()
    }
    
    suspend fun getAveragePriceByBreed(breed: String): Double? {
        return fowlDao.getAveragePriceByBreed(breed)
    }
    
    suspend fun getAvailableBreeds(): List<String> {
        return fowlDao.getAvailableBreeds()
    }
    
    suspend fun getAvailableLocations(): List<String> {
        return fowlDao.getAvailableLocations()
    }
    
    // Traceability operations
    suspend fun getFowlsByParent(parentId: String): List<Fowl> {
        return fowlDao.getFowlsByParent(parentId).map { it.toDomainModel() }
    }
    
    suspend fun getFowlParents(parentIds: List<String>): List<Fowl> {
        return fowlDao.getFowlParents(parentIds).map { it.toDomainModel() }
    }
    
    // Sync operations
    suspend fun getFowlsNeedingSync(timestamp: Long): List<Fowl> {
        return fowlDao.getFowlsNeedingSync(timestamp).first().map { it.toDomainModel() }
    }
    
    suspend fun updateSyncTimestamp(id: String, timestamp: Long = System.currentTimeMillis()) {
        fowlDao.updateSyncTimestamp(id, timestamp)
    }
    
    suspend fun cleanupOldCachedFowls(cutoffTime: Long, currentUserId: String) {
        fowlDao.cleanupOldCachedFowls(cutoffTime, currentUserId)
    }
    
    // Batch operations
    suspend fun replaceFowls(fowls: List<Fowl>) {
        fowlDao.replaceFowls(fowls.map { it.toEntity() })
    }
    
    suspend fun clearAllFowls() {
        fowlDao.clearAllFowls()
    }
    
    suspend fun clearFowlsByOwner(ownerId: String) {
        fowlDao.clearFowlsByOwner(ownerId)
    }
}