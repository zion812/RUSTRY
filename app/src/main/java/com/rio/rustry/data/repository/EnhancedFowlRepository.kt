package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.rio.rustry.data.local.dao.FowlDao
import com.rio.rustry.data.local.entity.FowlEntity
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.data.model.FowlBreed
import com.rio.rustry.presentation.marketplace.PriceRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnhancedFowlRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fowlDao: FowlDao
) {

    companion object {
        private const val FOWLS_COLLECTION = "fowls"
        private const val FAVORITES_COLLECTION = "user_favorites"
        private const val PAGE_SIZE = 20
    }

    /**
     * Get marketplace fowls with pagination and real-time updates
     */
    fun getMarketplaceFowls(
        page: Int = 0,
        pageSize: Int = PAGE_SIZE,
        forceRefresh: Boolean = false
    ): Flow<Result<List<Fowl>>> = flow {
        try {
            // First emit cached data if available and not forcing refresh
            if (!forceRefresh && page == 0) {
                val cachedFowls = fowlDao.getMarketplaceFowls().first()
                if (cachedFowls.isNotEmpty()) {
                    emit(Result.success(cachedFowls.map { it.toFowl() }))
                }
            }

            // Fetch from Firestore
            val query = firestore.collection(FOWLS_COLLECTION)
                .whereEqualTo("isAvailable", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            // Apply pagination
            val snapshot = if (page > 0) {
                val lastDocument = getLastDocumentForPage(page - 1, pageSize)
                if (lastDocument != null) {
                    query.startAfter(lastDocument).get().await()
                } else {
                    query.get().await()
                }
            } else {
                query.get().await()
            }

            val fowls = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Fowl::class.java)?.copy(id = doc.id)
            }

            // Cache the results if it's the first page
            if (page == 0) {
                fowlDao.clearMarketplaceFowls()
                fowlDao.insertFowls(fowls.map { FowlEntity.fromFowl(it) })
            }

            emit(Result.success(fowls))

        } catch (e: Exception) {
            // If network fails and we have cached data, return that
            if (page == 0) {
                val cachedFowls = fowlDao.getMarketplaceFowls().first()
                if (cachedFowls.isNotEmpty()) {
                    emit(Result.success(cachedFowls.map { it.toFowl() }))
                    return@flow
                }
            }
            emit(Result.failure(e))
        }
    }

    /**
     * Search fowls with optimized Firestore queries
     */
    fun searchFowls(
        query: String,
        breed: FowlBreed? = null,
        priceRange: PriceRange? = null,
        location: String? = null,
        isVerifiedOnly: Boolean = false,
        isNearbyOnly: Boolean = false
    ): Flow<Result<List<Fowl>>> = flow {
        try {
            var firestoreQuery = firestore.collection(FOWLS_COLLECTION)
                .whereEqualTo("isAvailable", true)

            // Apply breed filter
            breed?.let {
                firestoreQuery = firestoreQuery.whereEqualTo("breed", it.displayName)
            }

            // Apply price range filter
            priceRange?.let {
                firestoreQuery = firestoreQuery
                    .whereGreaterThanOrEqualTo("price", it.min)
                    .whereLessThanOrEqualTo("price", it.max)
            }

            // Apply verified filter
            if (isVerifiedOnly) {
                firestoreQuery = firestoreQuery.whereEqualTo("isTraceable", true)
            }

            // Apply location filter
            location?.let {
                firestoreQuery = firestoreQuery.whereEqualTo("location", it)
            }

            val snapshot = firestoreQuery
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100) // Limit search results
                .get()
                .await()

            var fowls = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Fowl::class.java)?.copy(id = doc.id)
            }

            // Apply text search filter (client-side for better performance)
            if (query.isNotBlank()) {
                fowls = fowls.filter { fowl ->
                    fowl.breed.contains(query, ignoreCase = true) ||
                    fowl.description.contains(query, ignoreCase = true) ||
                    fowl.ownerName.contains(query, ignoreCase = true) ||
                    fowl.location.contains(query, ignoreCase = true)
                }
            }

            // Apply nearby filter (mock implementation)
            if (isNearbyOnly) {
                fowls = fowls.filter { fowl ->
                    // In real app, this would use geolocation
                    fowl.location.contains("Hyderabad", ignoreCase = true) ||
                    fowl.location.contains("Secunderabad", ignoreCase = true)
                }
            }

            emit(Result.success(fowls))

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Get fowl details by ID with offline support
     */
    fun getFowlById(fowlId: String): Flow<Result<Fowl?>> = flow {
        try {
            // First check cache
            val cachedFowl = fowlDao.getFowlById(fowlId).first()
            if (cachedFowl != null) {
                emit(Result.success(cachedFowl.toFowl()))
            }

            // Fetch from Firestore
            val document = firestore.collection(FOWLS_COLLECTION)
                .document(fowlId)
                .get()
                .await()

            val fowl = document.toObject(Fowl::class.java)?.copy(id = document.id)
            
            // Update cache
            fowl?.let {
                fowlDao.insertFowl(FowlEntity.fromFowl(it))
            }

            emit(Result.success(fowl))

        } catch (e: Exception) {
            // Return cached data if available
            val cachedFowl = fowlDao.getFowlById(fowlId).first()
            if (cachedFowl != null) {
                emit(Result.success(cachedFowl.toFowl()))
            } else {
                emit(Result.failure(e))
            }
        }
    }

    /**
     * Add fowl to favorites
     */
    suspend fun addToFavorites(fowlId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection(FAVORITES_COLLECTION)
                .document("${userId}_$fowlId")
                .set(mapOf(
                    "userId" to userId,
                    "fowlId" to fowlId,
                    "createdAt" to System.currentTimeMillis()
                ))
                .await()

            // Update local cache
            fowlDao.addToFavorites(fowlId, userId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Remove fowl from favorites
     */
    suspend fun removeFromFavorites(fowlId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection(FAVORITES_COLLECTION)
                .document("${userId}_$fowlId")
                .delete()
                .await()

            // Update local cache
            fowlDao.removeFromFavorites(fowlId, userId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user's favorite fowls
     */
    fun getFavoriteFowls(userId: String): Flow<Result<List<Fowl>>> = flow {
        try {
            // First emit cached favorites
            val cachedFavorites = fowlDao.getFavoriteFowls(userId).first()
            if (cachedFavorites.isNotEmpty()) {
                emit(Result.success(cachedFavorites.map { it.toFowl() }))
            }

            // Fetch from Firestore
            val favoritesSnapshot = firestore.collection(FAVORITES_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val fowlIds = favoritesSnapshot.documents.mapNotNull { doc ->
                doc.getString("fowlId")
            }

            if (fowlIds.isNotEmpty()) {
                val fowlsSnapshot = firestore.collection(FOWLS_COLLECTION)
                    .whereIn("__name__", fowlIds)
                    .get()
                    .await()

                val fowls = fowlsSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Fowl::class.java)?.copy(id = doc.id)
                }

                // Update cache
                fowlDao.insertFowls(fowls.map { FowlEntity.fromFowl(it) })

                emit(Result.success(fowls))
            } else {
                emit(Result.success(emptyList()))
            }

        } catch (e: Exception) {
            // Return cached data if available
            val cachedFavorites = fowlDao.getFavoriteFowls(userId).first()
            if (cachedFavorites.isNotEmpty()) {
                emit(Result.success(cachedFavorites.map { it.toFowl() }))
            } else {
                emit(Result.failure(e))
            }
        }
    }

    /**
     * Get real-time updates for marketplace fowls
     */
    fun getMarketplaceFowlsRealTime(): Flow<List<Fowl>> = flow {
        firestore.collection(FOWLS_COLLECTION)
            .whereEqualTo("isAvailable", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                snapshot?.let { querySnapshot ->
                    val fowls = querySnapshot.documents.mapNotNull { doc ->
                        doc.toObject(Fowl::class.java)?.copy(id = doc.id)
                    }
                    
                    // Update cache in background
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        fowlDao.insertFowls(fowls.map { FowlEntity.fromFowl(it) })
                    }
                }
            }
    }

    /**
     * Clear cache
     */
    suspend fun clearCache() {
        fowlDao.clearAll()
    }

    /**
     * Sync offline changes when network is available
     */
    suspend fun syncOfflineChanges(): Result<Unit> {
        return try {
            // Get pending changes from local database
            val pendingChanges = fowlDao.getPendingChanges().first()
            
            // Sync each change
            pendingChanges.forEach { change ->
                when (change.operation) {
                    "INSERT" -> {
                        // Upload new fowl
                        val fowl = fowlDao.getFowlById(change.fowlId).first()?.toFowl()
                        fowl?.let {
                            firestore.collection(FOWLS_COLLECTION)
                                .document(it.id)
                                .set(it)
                                .await()
                        }
                    }
                    "UPDATE" -> {
                        // Update existing fowl
                        val fowl = fowlDao.getFowlById(change.fowlId).first()?.toFowl()
                        fowl?.let {
                            firestore.collection(FOWLS_COLLECTION)
                                .document(it.id)
                                .set(it)
                                .await()
                        }
                    }
                    "DELETE" -> {
                        // Delete fowl
                        firestore.collection(FOWLS_COLLECTION)
                            .document(change.fowlId)
                            .delete()
                            .await()
                    }
                }
                
                // Mark change as synced
                fowlDao.markChangeSynced(change.id)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getLastDocumentForPage(page: Int, pageSize: Int): QuerySnapshot? {
        return try {
            firestore.collection(FOWLS_COLLECTION)
                .whereEqualTo("isAvailable", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit((page * pageSize).toLong())
                .get()
                .await()
        } catch (e: Exception) {
            null
        }
    }
}