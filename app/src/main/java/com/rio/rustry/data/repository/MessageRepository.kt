package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rio.rustry.data.model.Conversation
import com.rio.rustry.data.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    suspend fun sendMessage(
        senderId: String,
        receiverId: String,
        content: String,
        fowlId: String? = null
    ): Result<String> {
        return try {
            val conversationId = generateConversationId(senderId, receiverId)
            
            val message = Message(
                senderId = senderId,
                receiverId = receiverId,
                content = content,
                timestamp = System.currentTimeMillis(),
                fowlId = fowlId
            )
            
            // Add message to messages collection
            val messageRef = firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .add(message)
                .await()
            
            // Update conversation with last message
            val conversation = Conversation(
                id = conversationId,
                participants = listOf(senderId, receiverId).sorted(),
                lastMessage = message.copy(id = messageRef.id),
                lastMessageTime = message.timestamp,
                fowlId = fowlId
            )
            
            firestore.collection("conversations")
                .document(conversationId)
                .set(conversation)
                .await()
            
            Result.success(messageRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(messages)
            }
        
        awaitClose { listener.remove() }
    }
    
    suspend fun getConversations(userId: String): Result<List<Conversation>> {
        return try {
            val snapshot = firestore.collection("conversations")
                .whereArrayContains("participants", userId)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val conversations = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Conversation::class.java)?.copy(id = doc.id)
            }
            
            Result.success(conversations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markMessagesAsRead(conversationId: String, userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateConversationId(userId1: String, userId2: String): String {
        return listOf(userId1, userId2).sorted().joinToString("_")
    }
}