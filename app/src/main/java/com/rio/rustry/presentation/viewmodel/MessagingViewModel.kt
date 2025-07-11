package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.data.model.Message
import com.rio.rustry.data.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagingViewModel : ViewModel() {
    
    private val messageRepository = MessageRepository(
        FirebaseFirestore.getInstance()
    )
    
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(MessagingUiState())
    val uiState: StateFlow<MessagingUiState> = _uiState.asStateFlow()
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private var currentReceiverId: String = ""
    private var currentFowlId: String? = null
    private var conversationId: String = ""
    
    fun initializeConversation(receiverId: String, fowlId: String? = null) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated")
            return
        }
        
        currentReceiverId = receiverId
        currentFowlId = fowlId
        conversationId = generateConversationId(currentUser.uid, receiverId)
        
        _uiState.value = _uiState.value.copy(
            currentUserId = currentUser.uid,
            error = null
        )
        
        // Start listening to messages
        viewModelScope.launch {
            messageRepository.getMessages(conversationId).collect { messageList ->
                _messages.value = messageList
            }
        }
        
        // Mark messages as read
        viewModelScope.launch {
            messageRepository.markMessagesAsRead(conversationId, currentUser.uid)
        }
    }
    
    fun sendMessage(content: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated")
            return
        }
        
        if (content.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            messageRepository.sendMessage(
                senderId = currentUser.uid,
                receiverId = currentReceiverId,
                content = content,
                fowlId = currentFowlId
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to send message: ${error.message}"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun generateConversationId(userId1: String, userId2: String): String {
        return listOf(userId1, userId2).sorted().joinToString("_")
    }
}

data class MessagingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String = ""
)