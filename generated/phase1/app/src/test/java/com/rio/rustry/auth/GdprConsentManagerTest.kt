// generated/phase1/app/src/test/java/com/rio/rustry/auth/GdprConsentManagerTest.kt
package com.rio.rustry.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.HttpsCallableResult
import com.google.android.gms.tasks.Tasks
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class GdprConsentManagerTest {
    
    private lateinit var context: Context
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockFunctions: FirebaseFunctions
    private lateinit var mockUser: FirebaseUser
    private lateinit var mockDocument: DocumentSnapshot
    private lateinit var mockDocumentRef: DocumentReference
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockCallable: HttpsCallableReference
    private lateinit var mockCallableResult: HttpsCallableResult
    private lateinit var gdprConsentManager: GdprConsentManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockAuth = mockk()
        mockFirestore = mockk()
        mockFunctions = mockk()
        mockUser = mockk()
        mockDocument = mockk()
        mockDocumentRef = mockk()
        mockCollection = mockk()
        mockCallable = mockk()
        mockCallableResult = mockk()
        
        // Setup basic mocks
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "test-user-id"
        every { mockFirestore.collection("users") } returns mockCollection
        every { mockCollection.document("test-user-id") } returns mockDocumentRef
        every { mockFunctions.getHttpsCallable("deleteUserData") } returns mockCallable
        
        gdprConsentManager = GdprConsentManager(context, mockAuth, mockFirestore, mockFunctions)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `isConsented returns false by default`() = runTest {
        // When
        val isConsented = gdprConsentManager.isConsented().first()
        
        // Then
        assertThat(isConsented).isFalse()
    }
    
    @Test
    fun `recordConsent updates Firestore and cache successfully`() = runTest {
        // Given
        val updateMap = slot<Map<String, Any>>()
        every { mockDocumentRef.update(capture(updateMap)) } returns Tasks.forResult(null)
        
        // When
        val result = gdprConsentManager.recordConsent()
        
        // Then
        assertThat(result.isSuccess).isTrue()
        verify { mockDocumentRef.update(any<Map<String, Any>>()) }
        
        // Verify the update map contains correct fields
        val capturedMap = updateMap.captured
        assertThat(capturedMap["gdprConsent"]).isEqualTo(true)
        assertThat(capturedMap["gdprConsentTs"]).isInstanceOf(Timestamp::class.java)
    }
    
    @Test
    fun `recordConsent fails when user not authenticated`() = runTest {
        // Given
        every { mockAuth.currentUser } returns null
        
        // When
        val result = gdprConsentManager.recordConsent()
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("User not authenticated")
    }
    
    @Test
    fun `recordConsent fails when Firestore update fails`() = runTest {
        // Given
        val exception = Exception("Firestore error")
        every { mockDocumentRef.update(any<Map<String, Any>>()) } returns Tasks.forException(exception)
        
        // When
        val result = gdprConsentManager.recordConsent()
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Failed to record consent")
    }
    
    @Test
    fun `requestDeletion updates Firestore and calls Cloud Function successfully`() = runTest {
        // Given
        val updateMap = slot<Map<String, Any>>()
        val callData = slot<HashMap<String, Any>>()
        every { mockDocumentRef.update(capture(updateMap)) } returns Tasks.forResult(null)
        every { mockCallable.call(capture(callData)) } returns Tasks.forResult(mockCallableResult)
        
        // When
        val result = gdprConsentManager.requestDeletion()
        
        // Then
        assertThat(result.isSuccess).isTrue()
        verify { mockDocumentRef.update(any<Map<String, Any>>()) }
        verify { mockCallable.call(any<HashMap<String, Any>>()) }
        
        // Verify the update map contains correct fields
        val capturedUpdateMap = updateMap.captured
        assertThat(capturedUpdateMap["dataDeletionRequested"]).isEqualTo(true)
        assertThat(capturedUpdateMap["dataDeletionRequestedTs"]).isInstanceOf(Timestamp::class.java)
        
        // Verify the call data contains correct fields
        val capturedCallData = callData.captured
        assertThat(capturedCallData["userId"]).isEqualTo("test-user-id")
        assertThat(capturedCallData["requestedAt"]).isInstanceOf(Long::class.java)
    }
    
    @Test
    fun `requestDeletion fails when user not authenticated`() = runTest {
        // Given
        every { mockAuth.currentUser } returns null
        
        // When
        val result = gdprConsentManager.requestDeletion()
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("User not authenticated")
    }
    
    @Test
    fun `requestDeletion fails when Firestore update fails`() = runTest {
        // Given
        val exception = Exception("Firestore error")
        every { mockDocumentRef.update(any<Map<String, Any>>()) } returns Tasks.forException(exception)
        
        // When
        val result = gdprConsentManager.requestDeletion()
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("Failed to request deletion")
    }
    
    @Test
    fun `checkUserConsent returns true when user has consented and not requested deletion`() = runTest {
        // Given
        every { mockDocumentRef.get() } returns Tasks.forResult(mockDocument)
        every { mockDocument.getBoolean("gdprConsent") } returns true
        every { mockDocument.getBoolean("dataDeletionRequested") } returns false
        
        // When
        val hasConsent = gdprConsentManager.checkUserConsent()
        
        // Then
        assertThat(hasConsent).isTrue()
    }
    
    @Test
    fun `checkUserConsent returns false when user has not consented`() = runTest {
        // Given
        every { mockDocumentRef.get() } returns Tasks.forResult(mockDocument)
        every { mockDocument.getBoolean("gdprConsent") } returns false
        every { mockDocument.getBoolean("dataDeletionRequested") } returns false
        
        // When
        val hasConsent = gdprConsentManager.checkUserConsent()
        
        // Then
        assertThat(hasConsent).isFalse()
    }
    
    @Test
    fun `checkUserConsent returns false when user has requested deletion`() = runTest {
        // Given
        every { mockDocumentRef.get() } returns Tasks.forResult(mockDocument)
        every { mockDocument.getBoolean("gdprConsent") } returns true
        every { mockDocument.getBoolean("dataDeletionRequested") } returns true
        
        // When
        val hasConsent = gdprConsentManager.checkUserConsent()
        
        // Then
        assertThat(hasConsent).isFalse()
    }
    
    @Test
    fun `checkUserConsent returns false when Firestore fetch fails`() = runTest {
        // Given
        every { mockDocumentRef.get() } returns Tasks.forException(Exception("Network error"))
        
        // When
        val hasConsent = gdprConsentManager.checkUserConsent()
        
        // Then
        assertThat(hasConsent).isFalse()
    }
    
    @Test
    fun `clearConsentCache completes without error`() = runTest {
        // When & Then - Should complete without throwing
        gdprConsentManager.clearConsentCache()
    }
}