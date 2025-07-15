// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/GetChatMessagesUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.data.model.ChatMessage
import com.rio.rustry.social.SocialRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChatMessagesUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    operator fun invoke(chatId: String): Flow<Result<List<ChatMessage>>> {
        return socialRepository.getChatMessages(chatId)
    }
}