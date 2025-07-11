package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.rio.rustry.data.model.Fowl
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.net.Uri

@Singleton
class FowlRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    
    suspend fun addFowl(fowl: Fowl): Result<String> {
        return try {
            val docRef = firestore.collection("fowls").add(fowl).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFowlsByOwner(ownerId: String): Result<List<Fowl>> {
        return try {
            // Simple query without composite index
            val snapshot = firestore.collection("fowls")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            
            val fowls = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Fowl::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.createdAt } // Sort in memory
            
            Result.success(fowls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllFowls(): Result<List<Fowl>> {
        return try {
            // Simplified query without composite index requirement
            val snapshot = firestore.collection("fowls")
                .whereEqualTo("isAvailable", true)
                .get()
                .await()
            
            val fowls = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Fowl::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.createdAt } // Sort in memory instead
            
            Result.success(fowls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFowlsByFilter(
        breed: String? = null,
        location: String? = null,
        isTraceable: Boolean? = null
    ): Result<List<Fowl>> {
        return try {
            // Start with basic query
            val snapshot = firestore.collection("fowls")
                .whereEqualTo("isAvailable", true)
                .get()
                .await()
            
            // Filter in memory to avoid composite index requirements
            var fowls = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Fowl::class.java)?.copy(id = doc.id)
            }
            
            // Apply filters in memory
            breed?.let { filterBreed ->
                fowls = fowls.filter { it.breed == filterBreed }
            }
            
            location?.let { filterLocation ->
                fowls = fowls.filter { it.location.contains(filterLocation, ignoreCase = true) }
            }
            
            isTraceable?.let { filterTraceable ->
                fowls = fowls.filter { it.isTraceable == filterTraceable }
            }
            
            // Sort by creation date
            fowls = fowls.sortedByDescending { it.createdAt }
            
            Result.success(fowls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFowlById(fowlId: String): Result<Fowl> {
        return try {
            val document = firestore.collection("fowls")
                .document(fowlId)
                .get()
                .await()
            
            val fowl = document.toObject(Fowl::class.java)?.copy(id = document.id)
            if (fowl != null) {
                Result.success(fowl)
            } else {
                Result.failure(Exception("Fowl not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateFowl(fowl: Fowl): Result<Unit> {
        return try {
            firestore.collection("fowls")
                .document(fowl.id)
                .set(fowl)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadImage(uri: Uri, path: String): Result<String> {
        return try {
            val ref = storage.reference.child("images/$path")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getNonTraceableCount(ownerId: String): Result<Int> {
        return try {
            // Simple query without ordering to avoid composite index
            val snapshot = firestore.collection("fowls")
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("isTraceable", false)
                .get()
                .await()
            
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}