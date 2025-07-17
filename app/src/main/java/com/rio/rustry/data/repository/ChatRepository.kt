package com.rio.rustry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Chat models for P2P and group messaging
 */
data class ChatRoom(
    val id: String = "",
    val name: String = "",
    val type: ChatType = ChatType.DIRECT,
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val lastMessageSender: String = "",
    val unreadCount: Map<String, Int> = emptyMap(),
    val isActive: Boolean = true,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val groupImage: String = "",
    val groupDescription: String = "",
    val adminIds: List<String> = emptyList(),
    val isEncrypted: Boolean = true,
    val metadata: Map<String, String> = emptyMap()
)

data class ChatMessage(
    val id: String = "",
    val chatRoomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val messageType: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis(),
    val isEdited: Boolean = false,
    val editedAt: Long? = null,
    val replyToMessageId: String = "",
    val mediaUrls: List<String> = emptyList(),
    val isDelivered: Boolean = false,
    val isRead: Boolean = false,
    val readBy: Map<String, Long> = emptyMap(),
    val reactions: Map<String, String> = emptyMap(), // userId to emoji
    val mentions: List<String> = emptyList(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val metadata: Map<String, String> = emptyMap()
)

data class ChatParticipant(
    val userId: String = "",
    val userName: String = "",
    val userType: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val joinedAt: Long = System.currentTimeMillis(),
    val role: ParticipantRole = ParticipantRole.MEMBER,
    val isMuted: Boolean = false,
    val mutedUntil: Long? = null,
    val permissions: List<String> = emptyList()
)

enum class ChatType {
    DIRECT,     // 1-on-1 chat
    GROUP,      // Group chat
    CHANNEL,    // Broadcast channel
    SUPPORT     // Customer support chat
}

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    LOCATION,
    PRODUCT_SHARE,
    SYSTEM,
    VOICE_NOTE
}

enum class ParticipantRole {
    ADMIN,
    MODERATOR,
    MEMBER
}

/**
 * Repository interface for chat operations
 */
interface ChatRepository {
    // Chat Room operations
    suspend fun createChatRoom(chatRoom: ChatRoom): Result<String>
    suspend fun getChatRoom(chatRoomId: String): Result<ChatRoom?>
    suspend fun getUserChatRooms(userId: String): Flow<List<ChatRoom>>
    suspend fun updateChatRoom(chatRoom: ChatRoom): Result<Unit>
    suspend fun deleteChatRoom(chatRoomId: String): Result<Unit>
    suspend fun addParticipant(chatRoomId: String, participant: ChatParticipant): Result<Unit>
    suspend fun removeParticipant(chatRoomId: String, userId: String): Result<Unit>
    suspend fun updateParticipant(chatRoomId: String, participant: ChatParticipant): Result<Unit>
    suspend fun getParticipants(chatRoomId: String): Flow<List<ChatParticipant>>
    
    // Message operations
    suspend fun sendMessage(message: ChatMessage): Result<String>
    suspend fun getMessages(chatRoomId: String, limit: Int = 50): Flow<List<ChatMessage>>
    suspend fun getMessagesPaginated(chatRoomId: String, lastMessageId: String?, limit: Int = 20): Result<List<ChatMessage>>
    suspend fun updateMessage(message: ChatMessage): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
    suspend fun markMessageAsRead(messageId: String, userId: String): Result<Unit>
    suspend fun markChatAsRead(chatRoomId: String, userId: String): Result<Unit>
    suspend fun addReaction(messageId: String, userId: String, emoji: String): Result<Unit>
    suspend fun removeReaction(messageId: String, userId: String): Result<Unit>
    
    // Search and discovery
    suspend fun searchMessages(chatRoomId: String, query: String): Flow<List<ChatMessage>>
    suspend fun searchChatRooms(userId: String, query: String): Flow<List<ChatRoom>>
    suspend fun findOrCreateDirectChat(user1Id: String, user2Id: String): Result<String>
    
    // Notifications and presence
    suspend fun updateUserPresence(userId: String, isOnline: Boolean): Result<Unit>
    suspend fun getUserPresence(userId: String): Result<Boolean>
    suspend fun getUnreadCount(userId: String): Flow<Int>
    suspend fun getChatUnreadCount(chatRoomId: String, userId: String): Flow<Int>
    
    // Media and file handling
    suspend fun uploadChatMedia(chatRoomId: String, mediaData: ByteArray, mediaType: String): Result<String>
    suspend fun deleteChatMedia(mediaUrl: String): Result<Unit>
    
    // Moderation and security
    suspend fun reportMessage(messageId: String, reportedBy: String, reason: String): Result<Unit>
    suspend fun blockUser(userId: String, blockedUserId: String): Result<Unit>
    suspend fun unblockUser(userId: String, blockedUserId: String): Result<Unit>
    suspend fun getBlockedUsers(userId: String): Flow<List<String>>
    suspend fun muteChat(chatRoomId: String, userId: String, mutedUntil: Long?): Result<Unit>
    
    // Analytics and insights
    suspend fun getChatAnalytics(chatRoomId: String): Result<Map<String, Any>>
    suspend fun getUserChatStats(userId: String): Result<Map<String, Any>>
}

/**
 * Implementation of ChatRepository using Firebase Firestore
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    companion object {
        private const val CHAT_ROOMS_COLLECTION = "chat_rooms"
        private const val MESSAGES_COLLECTION = "messages"
        private const val PARTICIPANTS_COLLECTION = "participants"
        private const val USER_PRESENCE_COLLECTION = "user_presence"
        private const val BLOCKED_USERS_COLLECTION = "blocked_users"
        private const val MESSAGE_REPORTS_COLLECTION = "message_reports"
        private const val CHAT_ANALYTICS_COLLECTION = "chat_analytics"
    }

    override suspend fun createChatRoom(chatRoom: ChatRoom): Result<String> {
        return try {
            val docRef = firestore.collection(CHAT_ROOMS_COLLECTION).document()
            val chatRoomWithId = chatRoom.copy(id = docRef.id)
            docRef.set(chatRoomWithId).await()
            
            // Add participants
            chatRoom.participants.forEach { participantId ->
                val participant = ChatParticipant(
                    userId = participantId,
                    userName = chatRoom.participantNames[participantId] ?: "",
                    role = if (participantId == chatRoom.createdBy) ParticipantRole.ADMIN else ParticipantRole.MEMBER
                )
                addParticipant(docRef.id, participant)
            }
            
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getChatRoom(chatRoomId: String): Result<ChatRoom?> {
        return try {
            val document = firestore.collection(CHAT_ROOMS_COLLECTION)
                .document(chatRoomId)
                .get()
                .await()
            val chatRoom = document.toObject(ChatRoom::class.java)
            Result.Success(chatRoom)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getUserChatRooms(userId: String): Flow<List<ChatRoom>> = flow {
        try {
            val snapshot = firestore.collection(CHAT_ROOMS_COLLECTION)
                .whereArrayContains("participants", userId)
                .whereEqualTo("isActive", true)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .get()
                .await()
            val chatRooms = snapshot.toObjects(ChatRoom::class.java)
            emit(chatRooms)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun updateChatRoom(chatRoom: ChatRoom): Result<Unit> {
        return try {
            firestore.collection(CHAT_ROOMS_COLLECTION)
                .document(chatRoom.id)
                .set(chatRoom.copy(updatedAt = System.currentTimeMillis()))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteChatRoom(chatRoomId: String): Result<Unit> {
        return try {
            firestore.collection(CHAT_ROOMS_COLLECTION)
                .document(chatRoomId)
                .update("isActive", false)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun addParticipant(chatRoomId: String, participant: ChatParticipant): Result<Unit> {
        return try {
            firestore.collection(CHAT_ROOMS_COLLECTION)
                .document(chatRoomId)
                .collection(PARTICIPANTS_COLLECTION)
                .document(participant.userId)
                .set(participant)
                .await()
            
            // Update participants list in chat room
            firestore.collection(CHAT_ROOMS_COLLECTION)
                .document(chatRoomId)
                .update(
                    "participants", com.google.firebase.firestore.FieldValue.arrayUnion(participant.userId),
                    "participantNames.${participant.userId}", participant.userName
                )
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun removeParticipant(chatRoomId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection(CHAT_ROOMS_COLLECTION)
                .document(chatRoomId)
                .collection(PARTICIPANTS_COLLECTION)
                .document(userId)
                .delete()
                .await()
            
            // Update participants list in chat room
            firestore.collection(CHAT_ROOMS_COLLECTION)
                .document(chatRoomId)
                .update(
                    "participants", com.google.firebase.firestore.FieldValue.arrayRemove(userId)
                )
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateParticipant(chatRoomId: String, participant: ChatParticipant): Result<Unit> {
        return try {
            firestore.collection(CHAT_ROOMS_COLLECTION)
                .document(chatRoomId)
                .collection(PARTICIPANTS_COLLECTION)
                .document(participant.userId)
                .set(participant)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getParticipants(chatRoomId: String): Flow<List<ChatParticipant>> = flow {
        try {
            val snapshot = firestore.collection(CHAT_ROOMS_COLLECTION)
                .document(chatRoomId)
                .collection(PARTICIPANTS_COLLECTION)
                .get()
                .await()
            val participants = snapshot.toObjects(ChatParticipant::class.java)
            emit(participants)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun sendMessage(message: ChatMessage): Result<String> {
        return try {
            val docRef = firestore.collection(MESSAGES_COLLECTION).document()
            val messageWithId = message.copy(id = docRef.id)
            docRef.set(messageWithId).await()
            
            // Update chat room with last message
            firestore.collection(CHAT_ROOMS_COLLECTION)
                .document(message.chatRoomId)
                .update(
                    mapOf(
                        "lastMessage" to message.content,
                        "lastMessageTime" to message.timestamp,
                        "lastMessageSender" to message.senderId,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getMessages(chatRoomId: String, limit: Int): Flow<List<ChatMessage>> = flow {
        try {
            val snapshot = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("chatRoomId", chatRoomId)
                .whereEqualTo("isDeleted", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            val messages = snapshot.toObjects(ChatMessage::class.java).reversed()
            emit(messages)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getMessagesPaginated(chatRoomId: String, lastMessageId: String?, limit: Int): Result<List<ChatMessage>> {
        return try {
            var query = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("chatRoomId", chatRoomId)
                .whereEqualTo("isDeleted", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
            
            lastMessageId?.let { messageId ->
                val lastDoc = firestore.collection(MESSAGES_COLLECTION)
                    .document(messageId)
                    .get()
                    .await()
                query = query.startAfter(lastDoc)
            }
            
            val snapshot = query.get().await()
            val messages = snapshot.toObjects(ChatMessage::class.java).reversed()
            Result.Success(messages)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateMessage(message: ChatMessage): Result<Unit> {
        return try {
            firestore.collection(MESSAGES_COLLECTION)
                .document(message.id)
                .set(message.copy(isEdited = true, editedAt = System.currentTimeMillis()))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            firestore.collection(MESSAGES_COLLECTION)
                .document(messageId)
                .update(
                    mapOf(
                        "isDeleted" to true,
                        "deletedAt" to System.currentTimeMillis(),
                        "content" to "This message was deleted"
                    )
                )
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun markMessageAsRead(messageId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection(MESSAGES_COLLECTION)
                .document(messageId)
                .update(
                    mapOf(
                        "isRead" to true,
                        "readBy.$userId" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun markChatAsRead(chatRoomId: String, userId: String): Result<Unit> {
        return try {
            // Mark all unread messages as read
            val unreadMessages = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("chatRoomId", chatRoomId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val batch = firestore.batch()
            unreadMessages.documents.forEach { doc ->
                batch.update(doc.reference, "readBy.$userId", System.currentTimeMillis())
            }
            batch.commit().await()
            
            // Reset unread count for user
            firestore.collection(CHAT_ROOMS_COLLECTION)
                .document(chatRoomId)
                .update("unreadCount.$userId", 0)
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun addReaction(messageId: String, userId: String, emoji: String): Result<Unit> {
        return try {
            firestore.collection(MESSAGES_COLLECTION)
                .document(messageId)
                .update("reactions.$userId", emoji)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun removeReaction(messageId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection(MESSAGES_COLLECTION)
                .document(messageId)
                .update("reactions.$userId", com.google.firebase.firestore.FieldValue.delete())
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun searchMessages(chatRoomId: String, query: String): Flow<List<ChatMessage>> = flow {
        try {
            // Simple text search - can be enhanced with full-text search
            val snapshot = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("chatRoomId", chatRoomId)
                .whereEqualTo("isDeleted", false)
                .orderBy("content")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()
            val messages = snapshot.toObjects(ChatMessage::class.java)
            emit(messages)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun searchChatRooms(userId: String, query: String): Flow<List<ChatRoom>> = flow {
        try {
            val snapshot = firestore.collection(CHAT_ROOMS_COLLECTION)
                .whereArrayContains("participants", userId)
                .whereEqualTo("isActive", true)
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()
            val chatRooms = snapshot.toObjects(ChatRoom::class.java)
            emit(chatRooms)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun findOrCreateDirectChat(user1Id: String, user2Id: String): Result<String> {
        return try {
            // Check if direct chat already exists
            val existingChat = firestore.collection(CHAT_ROOMS_COLLECTION)
                .whereEqualTo("type", ChatType.DIRECT.name)
                .whereArrayContains("participants", user1Id)
                .get()
                .await()
            
            val directChat = existingChat.documents.find { doc ->
                val participants = doc.get("participants") as? List<*>
                participants?.contains(user2Id) == true
            }
            
            if (directChat != null) {
                Result.Success(directChat.id)
            } else {
                // Create new direct chat
                val chatRoom = ChatRoom(
                    name = "Direct Chat",
                    type = ChatType.DIRECT,
                    participants = listOf(user1Id, user2Id),
                    createdBy = user1Id
                )
                createChatRoom(chatRoom)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateUserPresence(userId: String, isOnline: Boolean): Result<Unit> {
        return try {
            firestore.collection(USER_PRESENCE_COLLECTION)
                .document(userId)
                .set(
                    mapOf(
                        "isOnline" to isOnline,
                        "lastSeen" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getUserPresence(userId: String): Result<Boolean> {
        return try {
            val document = firestore.collection(USER_PRESENCE_COLLECTION)
                .document(userId)
                .get()
                .await()
            val isOnline = document.getBoolean("isOnline") ?: false
            Result.Success(isOnline)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getUnreadCount(userId: String): Flow<Int> = flow {
        try {
            val chatRooms = firestore.collection(CHAT_ROOMS_COLLECTION)
                .whereArrayContains("participants", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            var totalUnread = 0
            chatRooms.documents.forEach { doc ->
                val unreadCount = doc.get("unreadCount.$userId") as? Long ?: 0L
                totalUnread += unreadCount.toInt()
            }
            
            emit(totalUnread)
        } catch (e: Exception) {
            emit(0)
        }
    }

    override suspend fun getChatUnreadCount(chatRoomId: String, userId: String): Flow<Int> = flow {
        try {
            val document = firestore.collection(CHAT_ROOMS_COLLECTION)
                .document(chatRoomId)
                .get()
                .await()
            val unreadCount = document.get("unreadCount.$userId") as? Long ?: 0L
            emit(unreadCount.toInt())
        } catch (e: Exception) {
            emit(0)
        }
    }

    override suspend fun uploadChatMedia(chatRoomId: String, mediaData: ByteArray, mediaType: String): Result<String> {
        return try {
            // In real implementation, upload to Firebase Storage
            // For now, return a mock URL
            val mediaUrl = "https://storage.googleapis.com/rustry-chat-media/${chatRoomId}/${System.currentTimeMillis()}.${mediaType}"
            Result.Success(mediaUrl)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteChatMedia(mediaUrl: String): Result<Unit> {
        return try {
            // In real implementation, delete from Firebase Storage
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun reportMessage(messageId: String, reportedBy: String, reason: String): Result<Unit> {
        return try {
            val reportData = mapOf(
                "messageId" to messageId,
                "reportedBy" to reportedBy,
                "reason" to reason,
                "createdAt" to System.currentTimeMillis(),
                "status" to "pending"
            )
            
            firestore.collection(MESSAGE_REPORTS_COLLECTION)
                .add(reportData)
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun blockUser(userId: String, blockedUserId: String): Result<Unit> {
        return try {
            firestore.collection(BLOCKED_USERS_COLLECTION)
                .document("${userId}_${blockedUserId}")
                .set(
                    mapOf(
                        "userId" to userId,
                        "blockedUserId" to blockedUserId,
                        "createdAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun unblockUser(userId: String, blockedUserId: String): Result<Unit> {
        return try {
            firestore.collection(BLOCKED_USERS_COLLECTION)
                .document("${userId}_${blockedUserId}")
                .delete()
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getBlockedUsers(userId: String): Flow<List<String>> = flow {
        try {
            val snapshot = firestore.collection(BLOCKED_USERS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val blockedUserIds = snapshot.documents.mapNotNull { 
                it.getString("blockedUserId") 
            }
            emit(blockedUserIds)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun muteChat(chatRoomId: String, userId: String, mutedUntil: Long?): Result<Unit> {
        return try {
            firestore.collection(CHAT_ROOMS_COLLECTION)
                .document(chatRoomId)
                .collection(PARTICIPANTS_COLLECTION)
                .document(userId)
                .update(
                    mapOf(
                        "isMuted" to (mutedUntil != null),
                        "mutedUntil" to mutedUntil
                    )
                )
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getChatAnalytics(chatRoomId: String): Result<Map<String, Any>> {
        return try {
            val messagesSnapshot = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("chatRoomId", chatRoomId)
                .get()
                .await()
            
            val analytics = mapOf(
                "totalMessages" to messagesSnapshot.size(),
                "activeParticipants" to messagesSnapshot.documents
                    .mapNotNull { it.getString("senderId") }
                    .distinct().size,
                "lastActivity" to (messagesSnapshot.documents
                    .maxOfOrNull { it.getLong("timestamp") ?: 0L } ?: 0L)
            )
            
            Result.Success(analytics)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getUserChatStats(userId: String): Result<Map<String, Any>> {
        return try {
            val userChats = firestore.collection(CHAT_ROOMS_COLLECTION)
                .whereArrayContains("participants", userId)
                .get()
                .await()
            
            val userMessages = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("senderId", userId)
                .get()
                .await()
            
            val stats = mapOf(
                "totalChats" to userChats.size(),
                "totalMessagesSent" to userMessages.size(),
                "activeChats" to userChats.documents.count { 
                    it.getBoolean("isActive") == true 
                }
            )
            
            Result.Success(stats)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}