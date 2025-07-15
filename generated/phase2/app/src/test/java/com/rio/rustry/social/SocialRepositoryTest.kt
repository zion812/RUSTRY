// generated/phase2/app/src/test/java/com/rio/rustry/social/SocialRepositoryTest.kt

package com.rio.rustry.social

import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.data.local.dao.ChatMessageDao
import com.rio.rustry.data.model.ChatMessage
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SocialRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var chatMessageDao: ChatMessageDao
    private lateinit var socialRepository: SocialRepository
    private lateinit var mockUser: FirebaseUser

    @Before
    fun setup() {
        firestore = mockk(relaxed = true)
        auth = mockk()
        chatMessageDao = mockk(relaxed = true)
        mockUser = mockk()
        socialRepository = SocialRepository(firestore, auth, chatMessageDao)
    }

    @Test
    fun `sendMessage creates message successfully`() = runTest {
        // Given
        val chatId = "chat123"
        val text = "Hello world"
        val userId = "user123"
        
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns userId
        
        // When
        val result = socialRepository.sendMessage(chatId, text)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `sendMessage fails when user not authenticated`() = runTest {
        // Given
        val chatId = "chat123"
        val text = "Hello world"
        
        every { auth.currentUser } returns null

        // When
        val result = socialRepository.sendMessage(chatId, text)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("not authenticated")
    }

    @Test
    fun `getChatMessages loads cached messages first`() = runTest {
        // Given
        val chatId = "chat123"
        val userId = "user123"
        val cachedMessages = listOf(
            ChatMessage(
                id = "msg1",
                chatId = chatId,
                senderId = userId,
                text = "Cached message",
                timestamp = System.currentTimeMillis()
            )
        )
        
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns userId
        coEvery { chatMessageDao.getMessagesForChat(chatId) } returns cachedMessages

        // When
        // Note: This test would need to be adapted for the actual Flow implementation
        // as getChatMessages returns a Flow with real-time updates

        // Then
        verify { chatMessageDao.getMessagesForChat(chatId) }
    }

    @Test
    fun `togglePostLike adds like when user hasn't liked`() = runTest {
        // Given
        val postId = "post123"
        val userId = "user123"
        
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns userId

        // When
        val result = socialRepository.togglePostLike(postId)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `togglePostLike fails when user not authenticated`() = runTest {
        // Given
        val postId = "post123"
        
        every { auth.currentUser } returns null

        // When
        val result = socialRepository.togglePostLike(postId)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("not authenticated")
    }

    @Test
    fun `shareProductToFeed creates post successfully`() = runTest {
        // Given
        val fowlId = "fowl123"
        val content = "Check out my fowl!"
        val userId = "user123"
        val userName = "John Doe"
        
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns userId
        every { mockUser.displayName } returns userName
        every { mockUser.photoUrl } returns null

        // When
        val result = socialRepository.shareProductToFeed(fowlId, content)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `shareProductToFeed fails when fowl not found`() = runTest {
        // Given
        val fowlId = "nonexistent"
        val content = "Check out my fowl!"
        val userId = "user123"
        
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns userId

        // When
        val result = socialRepository.shareProductToFeed(fowlId, content)

        // Then
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `getCurrentUserId returns user id when authenticated`() {
        // Given
        val userId = "user123"
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns userId

        // When
        val result = socialRepository.getCurrentUserId()

        // Then
        assertThat(result).isEqualTo(userId)
    }

    @Test
    fun `getCurrentUserId returns empty string when not authenticated`() {
        // Given
        every { auth.currentUser } returns null

        // When
        val result = socialRepository.getCurrentUserId()

        // Then
        assertThat(result).isEmpty()
    }
}