// generated/phase2/app/src/main/java/com/rio/rustry/listing/ListingRepository.kt

package com.rio.rustry.listing

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.data.model.FowlStatus
import com.rio.rustry.data.model.Lineage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListingRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {

    suspend fun addListing(listingData: FowlListingData): Result<String> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            
            // Upload images
            val imageUrls = uploadImages(listingData.images)
            
            // Create fowl document
            val fowlId = UUID.randomUUID().toString()
            val fowl = Fowl(
                id = fowlId,
                breed = listingData.breed,
                ageGroup = listingData.ageGroup,
                price = listingData.price,
                images = imageUrls + listingData.existingImages,
                isTraceable = listingData.isTraceable,
                lineage = Lineage(parentIds = emptyList()),
                ownerUid = currentUser.uid,
                status = FowlStatus.PUBLISHED,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            firestore.collection("fowls").document(fowlId).set(fowl).await()
            
            Result.Success(fowlId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateListing(fowlId: String, listingData: FowlListingData): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            
            // Upload new images
            val newImageUrls = uploadImages(listingData.images)
            
            // Update fowl document
            val updates = mapOf(
                "breed" to listingData.breed,
                "ageGroup" to listingData.ageGroup,
                "price" to listingData.price,
                "images" to newImageUrls + listingData.existingImages,
                "isTraceable" to listingData.isTraceable,
                "updatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection("fowls")
                .document(fowlId)
                .update(updates)
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deleteListing(fowlId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            
            // Soft delete - update status to inactive
            firestore.collection("fowls")
                .document(fowlId)
                .update(
                    mapOf(
                        "status" to FowlStatus.INACTIVE,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getMyListings(): Flow<Result<List<Fowl>>> = flow {
        try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            
            val snapshot = firestore.collection("fowls")
                .whereEqualTo("ownerUid", currentUser.uid)
                .whereNotEqualTo("status", FowlStatus.INACTIVE)
                .orderBy("status")
                .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val fowls = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Fowl::class.java)?.copy(id = doc.id)
            }
            
            emit(Result.Success(fowls))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    private suspend fun uploadImages(imageUris: List<Uri>): List<String> {
        return imageUris.map { uri ->
            val imageRef = storage.reference.child("fowl_images/${UUID.randomUUID()}")
            val uploadTask = imageRef.putFile(uri).await()
            imageRef.downloadUrl.await().toString()
        }
    }
}