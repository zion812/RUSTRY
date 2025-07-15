// generated/phase2/app/src/main/java/com/rio/rustry/social/PostsPagingSource.kt

package com.rio.rustry.social

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.rio.rustry.data.model.Post
import kotlinx.coroutines.tasks.await

class PostsPagingSource(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : PagingSource<QuerySnapshot, Post>() {

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Post> {
        return try {
            val currentUser = auth.currentUser
            
            var query = firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(params.loadSize.toLong())

            // For subsequent pages, start after the last document
            params.key?.let { lastSnapshot ->
                query = query.startAfter(lastSnapshot.documents.lastOrNull())
            }

            val snapshot = query.get().await()
            val posts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(
                    id = doc.id,
                    isLikedByCurrentUser = currentUser?.uid in (doc.get("likedBy") as? List<*> ?: emptyList<String>())
                )
            }

            LoadResult.Page(
                data = posts,
                prevKey = null, // Only paging forward
                nextKey = if (snapshot.documents.isNotEmpty()) snapshot else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Post>): QuerySnapshot? {
        return null
    }
}