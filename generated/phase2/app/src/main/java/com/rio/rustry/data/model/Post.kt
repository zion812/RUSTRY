// generated/phase2/app/src/main/java/com/rio/rustry/data/model/Post.kt

package com.rio.rustry.data.model

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val content: String = "",
    val images: List<String> = emptyList(),
    val timestamp: Long = 0L,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val sharedFowlId: String? = null,
    val isLikedByCurrentUser: Boolean = false
)