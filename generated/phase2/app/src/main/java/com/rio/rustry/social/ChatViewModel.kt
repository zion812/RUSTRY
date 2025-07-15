// generated/phase2/app/src/main/java/com/rio/rustry/social/ChatViewModel.kt

package com.rio.rustry.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.ChatMessage
import com.rio.rustry.domain.usecase.GetChatMessagesUseCase
import com.rio.rustry.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentChatId: String? = null

    fun loadChat(chatId: String) {
        if (currentChatId == chatId) return
        
        currentChatId = chatId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                getChatMessagesUseCase(chatId).collect { result ->
                    result.fold(
                        onSuccess = { messages ->
                            _uiState.update { 
                                it.copy(
                                    messages = messages,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to load messages"
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val chatId = currentChatId ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            
            try {
                sendMessageUseCase(chatId, text).fold(
                    onSuccess = {
                        _uiState.update { it.copy(isSending = false) }
                        // Message will be added via the real-time listener
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                isSending = false,
                                error = error.message ?: "Failed to send message"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSending = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun getCurrentUserId(): String {
        return sendMessageUseCase.getCurrentUserId()
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)