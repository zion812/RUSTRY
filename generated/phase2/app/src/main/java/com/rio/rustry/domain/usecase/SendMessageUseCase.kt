// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/SendMessageUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.social.SocialRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    suspend operator fun invoke(chatId: String, text: String): Result<Unit> {
        return socialRepository.sendMessage(chatId, text)
    }
    
    fun getCurrentUserId(): String {
        return socialRepository.getCurrentUserId()
    }
}