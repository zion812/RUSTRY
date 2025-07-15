// generated/phase2/app/src/main/java/com/rio/rustry/social/SocialRepository.kt

package com.rio.rustry.social

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.rio.rustry.data.local.dao.ChatMessageDao
import com.rio.rustry.data.model.ChatMessage
import com.rio.rustry.data.model.Post
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val chatMessageDao: ChatMessageDao
) {

    fun getChatMessages(chatId: String): Flow<Result<List<ChatMessage>>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(Result.failure(Exception("User not authenticated")))
            close()
            return@callbackFlow
        }

        // Load cached messages first
        val cachedMessages = chatMessageDao.getMessagesForChat(chatId)
        if (cachedMessages.isNotEmpty()) {
            trySend(Result.success(cachedMessages))
        }

        val listener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                    }
                    
                    // Cache messages
                    chatMessageDao.insertMessages(messages)
                    
                    trySend(Result.success(messages))
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(chatId: String, text: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            
            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = currentUser.uid,
                text = text,
                timestamp = System.currentTimeMillis()
            )
            
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(message.id)
                .set(message)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCommunityPosts(): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PostsPagingSource(firestore, auth) }
        ).flow
    }

    suspend fun togglePostLike(postId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            
            firestore.runTransaction { transaction ->
                val postRef = firestore.collection("posts").document(postId)
                val postSnapshot = transaction.get(postRef)
                
                val post = postSnapshot.toObject(Post::class.java)
                    ?: throw Exception("Post not found")
                
                val currentLikes = post.likedBy.toMutableSet()
                val newLikeCount = if (currentUser.uid in currentLikes) {
                    currentLikes.remove(currentUser.uid)
                    post.likeCount - 1
                } else {
                    currentLikes.add(currentUser.uid)
                    post.likeCount + 1
                }
                
                transaction.update(postRef, mapOf(
                    "likedBy" to currentLikes.toList(),
                    "likeCount" to newLikeCount
                ))
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun shareProductToFeed(fowlId: String, content: String): Result<String> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            
            // Get fowl details
            val fowlDoc = firestore.collection("fowls").document(fowlId).get().await()
            val fowl = fowlDoc.toObject(com.rio.rustry.data.model.Fowl::class.java)
                ?: throw Exception("Fowl not found")
            
            val postId = UUID.randomUUID().toString()
            val post = Post(
                id = postId,
                authorId = currentUser.uid,
                authorName = currentUser.displayName ?: "Anonymous",
                authorAvatar = currentUser.photoUrl?.toString() ?: "",
                content = content,
                images = fowl.images,
                timestamp = System.currentTimeMillis(),
                likeCount = 0,
                commentCount = 0,
                likedBy = emptyList(),
                sharedFowlId = fowlId
            )
            
            firestore.collection("posts").document(postId).set(post).await()
            
            Result.success(postId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }
}