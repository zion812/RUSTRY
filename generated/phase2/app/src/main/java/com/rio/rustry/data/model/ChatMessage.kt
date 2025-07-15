// generated/phase2/app/src/main/java/com/rio/rustry/data/model/ChatMessage.kt

package com.rio.rustry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)