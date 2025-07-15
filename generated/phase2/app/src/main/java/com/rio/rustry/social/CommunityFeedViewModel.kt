// generated/phase2/app/src/main/java/com/rio/rustry/social/CommunityFeedViewModel.kt

package com.rio.rustry.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rio.rustry.data.model.Post
import com.rio.rustry.domain.usecase.GetCommunityPostsUseCase
import com.rio.rustry.domain.usecase.TogglePostLikeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityFeedViewModel @Inject constructor(
    private val getCommunityPostsUseCase: GetCommunityPostsUseCase,
    private val togglePostLikeUseCase: TogglePostLikeUseCase
) : ViewModel() {

    val posts: Flow<PagingData<Post>> = getCommunityPostsUseCase()
        .cachedIn(viewModelScope)

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            try {
                togglePostLikeUseCase(postId)
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }
}