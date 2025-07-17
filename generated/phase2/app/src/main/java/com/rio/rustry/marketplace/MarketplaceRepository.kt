// generated/phase2/app/src/main/java/com/rio/rustry/marketplace/MarketplaceRepository.kt

package com.rio.rustry.marketplace

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rio.rustry.data.local.dao.FowlDao
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.data.model.AgeGroup
import com.rio.rustry.data.model.Breed
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fowlDao: FowlDao
) {

    suspend fun searchFowls(params: SearchParams): Flow<Result<List<Fowl>>> = flow {
        try {
            // Try to get cached results first
            val cachedResults = fowlDao.searchFowls(
                query = params.query,
                breeds = params.filters.breeds.map { it.name },
                ageGroups = params.filters.ageGroups.map { it.name },
                minPrice = params.filters.priceRange.start.toDouble(),
                maxPrice = params.filters.priceRange.endInclusive.toDouble()
            )
            
            if (cachedResults.isNotEmpty()) {
                emit(Result.Success(cachedResults))
            }

            // Fetch from Firestore
            var query = firestore.collection("fowls")
                .whereEqualTo("isActive", true)

            // Apply filters
            if (params.filters.breeds.isNotEmpty()) {
                query = query.whereIn("breed", params.filters.breeds.map { it.name })
            }
            
            if (params.filters.ageGroups.isNotEmpty()) {
                query = query.whereIn("ageGroup", params.filters.ageGroups.map { it.name })
            }

            query = query.whereGreaterThanOrEqualTo("price", params.filters.priceRange.start.toDouble())
                .whereLessThanOrEqualTo("price", params.filters.priceRange.endInclusive.toDouble())

            if (params.filters.traceableOnly) {
                query = query.whereEqualTo("isTraceable", true)
            }

            // Apply sorting
            query = when (params.sortOption) {
                SortOption.NEWEST -> query.orderBy("createdAt", Query.Direction.DESCENDING)
                SortOption.PRICE_LOW_HIGH -> query.orderBy("price", Query.Direction.ASCENDING)
                SortOption.PRICE_HIGH_LOW -> query.orderBy("price", Query.Direction.DESCENDING)
                SortOption.DISTANCE -> query.orderBy("createdAt", Query.Direction.DESCENDING) // TODO: Implement location-based sorting
            }

            // Apply text search if query is not empty
            if (params.query.isNotBlank()) {
                query = query.orderBy("breed")
                    .startAt(params.query)
                    .endAt(params.query + "\uf8ff")
            }

            val snapshot = query.limit(50).get().await()
            val fowls = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Fowl::class.java)?.copy(id = doc.id)
            }

            // Cache results
            fowlDao.insertFowls(fowls)
            
            emit(Result.Success(fowls))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    suspend fun getFowlDetail(fowlId: String): Flow<Result<FowlDetail>> = flow {
        try {
            // Try cache first
            val cachedFowl = fowlDao.getFowlById(fowlId)
            if (cachedFowl != null) {
                val lineage = getLineage(cachedFowl)
                emit(Result.Success(FowlDetail(cachedFowl, lineage)))
            }

            // Fetch from Firestore
            val doc = firestore.collection("fowls").document(fowlId).get().await()
            val fowl = doc.toObject(Fowl::class.java)?.copy(id = doc.id)
                ?: throw Exception("Fowl not found")

            val lineage = getLineage(fowl)
            
            // Cache the fowl
            fowlDao.insertFowl(fowl)
            
            emit(Result.Success(FowlDetail(fowl, lineage)))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    private suspend fun getLineage(fowl: Fowl): List<Fowl> {
        if (fowl.lineage.parentIds.isEmpty()) return emptyList()
        
        return try {
            val parentDocs = firestore.collection("fowls")
                .whereIn("id", fowl.lineage.parentIds)
                .get()
                .await()
            
            parentDocs.documents.mapNotNull { doc ->
                doc.toObject(Fowl::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun clearCache() {
        fowlDao.clearOldFowls()
    }
}