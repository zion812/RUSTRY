// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/TogglePostLikeUseCase.kt

package com.rio.rustry.domain.usecase

import com.rio.rustry.social.SocialRepository
import javax.inject.Inject

class TogglePostLikeUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    suspend operator fun invoke(postId: String): Result<Unit> {
        return socialRepository.togglePostLike(postId)
    }
}