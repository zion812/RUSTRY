// generated/phase2/app/src/main/java/com/rio/rustry/domain/usecase/GetCommunityPostsUseCase.kt

package com.rio.rustry.domain.usecase

import androidx.paging.PagingData
import com.rio.rustry.data.model.Post
import com.rio.rustry.social.SocialRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommunityPostsUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    operator fun invoke(): Flow<PagingData<Post>> {
        return socialRepository.getCommunityPosts()
    }
}