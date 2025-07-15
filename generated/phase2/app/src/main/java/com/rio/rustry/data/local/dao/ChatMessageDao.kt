// generated/phase2/app/src/main/java/com/rio/rustry/data/local/dao/ChatMessageDao.kt

package com.rio.rustry.data.local.dao

import androidx.room.*
import com.rio.rustry.data.model.ChatMessage

@Dao
interface ChatMessageDao {
    
    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    suspend fun getMessagesForChat(chatId: String): List<ChatMessage>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessage>)
    
    @Query("DELETE FROM chat_messages WHERE chatId = :chatId")
    suspend fun clearChatMessages(chatId: String)
    
    @Query("DELETE FROM chat_messages WHERE timestamp < :cutoffTime")
    suspend fun deleteOldMessages(cutoffTime: Long)
}