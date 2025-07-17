package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rio.rustry.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for marketplace operations
 */
interface MarketRepository {
    suspend fun createProductListing(listing: ProductListing): Result<String>
    suspend fun updateProductListing(listing: ProductListing): Result<Unit>
    suspend fun deleteProductListing(listingId: String): Result<Unit>
    suspend fun getProductListing(listingId: String): Result<ProductListing?>
    suspend fun getProductListings(): Flow<List<ProductListing>>
    suspend fun getProductListingsByOwner(ownerId: String): Flow<List<ProductListing>>
    suspend fun searchProductListings(query: String): Flow<List<ProductListing>>
    suspend fun getProductListingsByCategory(category: ProductCategory): Flow<List<ProductListing>>
    suspend fun getProductListingsByBreed(breed: String): Flow<List<ProductListing>>
    suspend fun getProductListingsByLocation(location: String, radius: Int): Flow<List<ProductListing>>
    suspend fun getProductListingsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<ProductListing>>
    suspend fun getProductListingsByAgeGroup(ageGroup: AgeGroup): Flow<List<ProductListing>>
    suspend fun getFilteredProductListings(filters: MarketFilters): Flow<List<ProductListing>>
    suspend fun getFeaturedProductListings(): Flow<List<ProductListing>>
    suspend fun getRecentProductListings(limit: Int = 20): Flow<List<ProductListing>>
    suspend fun getNearbyProductListings(userLocation: String, radius: Int): Flow<List<ProductListing>>
    suspend fun getVerifiedProductListings(): Flow<List<ProductListing>>
    suspend fun incrementViewCount(listingId: String): Result<Unit>
    suspend fun toggleLike(listingId: String, userId: String): Result<Unit>
    suspend fun addComment(listingId: String, comment: ProductComment): Result<String>
    suspend fun getComments(listingId: String): Flow<List<ProductComment>>
    suspend fun reportListing(listingId: String, reportReason: String, reportedBy: String): Result<Unit>
    suspend fun markAsSold(listingId: String): Result<Unit>
    suspend fun renewListing(listingId: String): Result<Unit>
}

/**
 * Market filters data class
 */
data class MarketFilters(
    val category: ProductCategory? = null,
    val breed: String? = null,
    val ageGroup: AgeGroup? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val location: String? = null,
    val radius: Int? = null,
    val isVerified: Boolean? = null,
    val isNearbyDelivery: Boolean? = null,
    val traceabilityType: TraceabilityType? = null,
    val gender: String? = null,
    val sortBy: SortOption = SortOption.RECENT
)

/**
 * Sort options for marketplace
 */
enum class SortOption {
    RECENT,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    DISTANCE,
    POPULARITY,
    RATING
}

/**
 * Product comment data class
 */
data class ProductComment(
    val id: String = "",
    val listingId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userType: UserType = UserType.GENERAL,
    val comment: String = "",
    val rating: Float = 0.0f,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val replies: List<ProductCommentReply> = emptyList()
)

/**
 * Product comment reply data class
 */
data class ProductCommentReply(
    val id: String = "",
    val commentId: String = "",
    val userId: String = "",
    val userName: String = "",
    val reply: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Implementation of MarketRepository using Firebase Firestore
 */
@Singleton
class MarketRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MarketRepository {

    companion object {
        private const val PRODUCT_LISTINGS_COLLECTION = "product_listings"
        private const val PRODUCT_COMMENTS_COLLECTION = "product_comments"
        private const val PRODUCT_REPORTS_COLLECTION = "product_reports"
        private const val USER_LIKES_COLLECTION = "user_likes"
    }

    override suspend fun createProductListing(listing: ProductListing): Result<String> {
        return try {
            val docRef = firestore.collection(PRODUCT_LISTINGS_COLLECTION).document()
            val listingWithId = listing.copy(id = docRef.id)
            docRef.set(listingWithId).await()
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateProductListing(listing: ProductListing): Result<Unit> {
        return try {
            firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .document(listing.id)
                .set(listing.copy(updatedAt = System.currentTimeMillis()))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteProductListing(listingId: String): Result<Unit> {
        return try {
            firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .document(listingId)
                .delete()
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getProductListing(listingId: String): Result<ProductListing?> {
        return try {
            val document = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .document(listingId)
                .get()
                .await()
            val listing = document.toObject(ProductListing::class.java)
            Result.Success(listing)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getProductListings(): Flow<List<ProductListing>> = flow {
        try {
            val snapshot = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isAvailable", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val listings = snapshot.toObjects(ProductListing::class.java)
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getProductListingsByOwner(ownerId: String): Flow<List<ProductListing>> = flow {
        try {
            val snapshot = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .whereEqualTo("ownerId", ownerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val listings = snapshot.toObjects(ProductListing::class.java)
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun searchProductListings(query: String): Flow<List<ProductListing>> = flow {
        try {
            // Simple text search - can be enhanced with Algolia or Elasticsearch
            val titleSnapshot = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isAvailable", true)
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()

            val breedSnapshot = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isAvailable", true)
                .whereEqualTo("breed", query)
                .get()
                .await()

            val titleListings = titleSnapshot.toObjects(ProductListing::class.java)
            val breedListings = breedSnapshot.toObjects(ProductListing::class.java)
            val allListings = (titleListings + breedListings).distinctBy { it.id }
            
            emit(allListings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getProductListingsByCategory(category: ProductCategory): Flow<List<ProductListing>> = flow {
        try {
            val snapshot = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isAvailable", true)
                .whereEqualTo("category", category.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val listings = snapshot.toObjects(ProductListing::class.java)
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getProductListingsByBreed(breed: String): Flow<List<ProductListing>> = flow {
        try {
            val snapshot = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isAvailable", true)
                .whereEqualTo("breed", breed)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val listings = snapshot.toObjects(ProductListing::class.java)
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getProductListingsByLocation(location: String, radius: Int): Flow<List<ProductListing>> = flow {
        try {
            // Simple location-based search - can be enhanced with geospatial queries
            val snapshot = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isAvailable", true)
                .whereEqualTo("location", location)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val listings = snapshot.toObjects(ProductListing::class.java)
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getProductListingsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<ProductListing>> = flow {
        try {
            val snapshot = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isAvailable", true)
                .whereGreaterThanOrEqualTo("price", minPrice)
                .whereLessThanOrEqualTo("price", maxPrice)
                .orderBy("price", Query.Direction.ASCENDING)
                .get()
                .await()
            val listings = snapshot.toObjects(ProductListing::class.java)
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getProductListingsByAgeGroup(ageGroup: AgeGroup): Flow<List<ProductListing>> = flow {
        try {
            val snapshot = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isAvailable", true)
                .whereEqualTo("ageGroup", ageGroup.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val listings = snapshot.toObjects(ProductListing::class.java)
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getFilteredProductListings(filters: MarketFilters): Flow<List<ProductListing>> = flow {
        try {
            var query = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isAvailable", true)

            // Apply filters
            filters.category?.let { 
                query = query.whereEqualTo("category", it.name)
            }
            filters.breed?.let { 
                query = query.whereEqualTo("breed", it)
            }
            filters.ageGroup?.let { 
                query = query.whereEqualTo("ageGroup", it.name)
            }
            filters.minPrice?.let { minPrice ->
                query = query.whereGreaterThanOrEqualTo("price", minPrice)
            }
            filters.maxPrice?.let { maxPrice ->
                query = query.whereLessThanOrEqualTo("price", maxPrice)
            }
            filters.location?.let { 
                query = query.whereEqualTo("location", it)
            }
            filters.isVerified?.let { 
                query = query.whereEqualTo("isOwnerVerified", it)
            }
            filters.isNearbyDelivery?.let { 
                query = query.whereEqualTo("isNearbyDelivery", it)
            }
            filters.traceabilityType?.let { 
                query = query.whereEqualTo("traceabilityType", it.name)
            }
            filters.gender?.let { 
                query = query.whereEqualTo("gender", it)
            }

            // Apply sorting
            query = when (filters.sortBy) {
                SortOption.RECENT -> query.orderBy("createdAt", Query.Direction.DESCENDING)
                SortOption.PRICE_LOW_TO_HIGH -> query.orderBy("price", Query.Direction.ASCENDING)
                SortOption.PRICE_HIGH_TO_LOW -> query.orderBy("price", Query.Direction.DESCENDING)
                SortOption.POPULARITY -> query.orderBy("viewCount", Query.Direction.DESCENDING)
                else -> query.orderBy("createdAt", Query.Direction.DESCENDING)
            }

            val snapshot = query.get().await()
            val listings = snapshot.toObjects(ProductListing::class.java)
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getFeaturedProductListings(): Flow<List<ProductListing>> = flow {
        try {
            val snapshot = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isAvailable", true)
                .whereEqualTo("isOwnerVerified", true)
                .orderBy("viewCount", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
            val listings = snapshot.toObjects(ProductListing::class.java)
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getRecentProductListings(limit: Int): Flow<List<ProductListing>> = flow {
        try {
            val snapshot = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isAvailable", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            val listings = snapshot.toObjects(ProductListing::class.java)
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getNearbyProductListings(userLocation: String, radius: Int): Flow<List<ProductListing>> = flow {
        try {
            // Simple nearby search - can be enhanced with geospatial queries
            val snapshot = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isAvailable", true)
                .whereEqualTo("isNearbyDelivery", true)
                .whereEqualTo("location", userLocation)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val listings = snapshot.toObjects(ProductListing::class.java)
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getVerifiedProductListings(): Flow<List<ProductListing>> = flow {
        try {
            val snapshot = firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .whereEqualTo("isActive", true)
                .whereEqualTo("isAvailable", true)
                .whereEqualTo("isOwnerVerified", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val listings = snapshot.toObjects(ProductListing::class.java)
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun incrementViewCount(listingId: String): Result<Unit> {
        return try {
            firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .document(listingId)
                .update("viewCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun toggleLike(listingId: String, userId: String): Result<Unit> {
        return try {
            val likeDocRef = firestore.collection(USER_LIKES_COLLECTION)
                .document("${userId}_${listingId}")
            
            val likeDoc = likeDocRef.get().await()
            
            if (likeDoc.exists()) {
                // Unlike
                likeDocRef.delete().await()
                firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                    .document(listingId)
                    .update("likeCount", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()
            } else {
                // Like
                likeDocRef.set(mapOf(
                    "userId" to userId,
                    "listingId" to listingId,
                    "createdAt" to System.currentTimeMillis()
                )).await()
                firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                    .document(listingId)
                    .update("likeCount", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun addComment(listingId: String, comment: ProductComment): Result<String> {
        return try {
            val docRef = firestore.collection(PRODUCT_COMMENTS_COLLECTION).document()
            val commentWithId = comment.copy(id = docRef.id, listingId = listingId)
            docRef.set(commentWithId).await()
            
            // Update comment count
            firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .document(listingId)
                .update("commentCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getComments(listingId: String): Flow<List<ProductComment>> = flow {
        try {
            val snapshot = firestore.collection(PRODUCT_COMMENTS_COLLECTION)
                .whereEqualTo("listingId", listingId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val comments = snapshot.toObjects(ProductComment::class.java)
            emit(comments)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun reportListing(listingId: String, reportReason: String, reportedBy: String): Result<Unit> {
        return try {
            val reportData = mapOf(
                "listingId" to listingId,
                "reportReason" to reportReason,
                "reportedBy" to reportedBy,
                "createdAt" to System.currentTimeMillis(),
                "status" to "pending"
            )
            
            firestore.collection(PRODUCT_REPORTS_COLLECTION)
                .add(reportData)
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun markAsSold(listingId: String): Result<Unit> {
        return try {
            firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .document(listingId)
                .update(
                    mapOf(
                        "isAvailable" to false,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun renewListing(listingId: String): Result<Unit> {
        return try {
            firestore.collection(PRODUCT_LISTINGS_COLLECTION)
                .document(listingId)
                .update(
                    mapOf(
                        "isAvailable" to true,
                        "updatedAt" to System.currentTimeMillis(),
                        "createdAt" to System.currentTimeMillis() // Refresh listing
                    )
                )
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}