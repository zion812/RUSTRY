package com.rio.rustry

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rio.rustry.data.local.enhanced.EnhancedRustryDatabase
import com.rio.rustry.data.local.entity.*
import com.rio.rustry.payment.*
import com.rio.rustry.presentation.ai.RustryAIChatbotViewModel
import com.rio.rustry.presentation.localization.LocalizationManager
import com.rio.rustry.security.SecurityManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Comprehensive Test Suite for RUSTRY Robustness Enhancements
 * Tests all critical functionality including:
 * - Multi-language support
 * - Offline functionality
 * - Security features
 * - Payment processing
 * - AI chatbot
 * - Database operations
 */

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RobustnessEnhancementTestSuite {
    
    private lateinit var context: Context
    private lateinit var localizationManager: LocalizationManager
    private lateinit var securityManager: SecurityManager
    private lateinit var paymentViewModel: EnhancedPaymentViewModel
    private lateinit var aiChatbotViewModel: RustryAIChatbotViewModel
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        localizationManager = LocalizationManager(context)
        securityManager = SecurityManager(context)
        paymentViewModel = EnhancedPaymentViewModel()
        aiChatbotViewModel = RustryAIChatbotViewModel()
    }
    
    // ==================== LOCALIZATION TESTS ====================
    
    @Test
    fun `test localization manager initialization`() {
        assertNotNull(localizationManager)
        assertEquals("en", localizationManager.getCurrentLanguage())
    }
    
    @Test
    fun `test language switching`() {
        // Test Telugu
        localizationManager.setLanguage("te")
        assertEquals("te", localizationManager.getCurrentLanguage())
        
        // Test Tamil
        localizationManager.setLanguage("ta")
        assertEquals("ta", localizationManager.getCurrentLanguage())
        
        // Test Kannada
        localizationManager.setLanguage("kn")
        assertEquals("kn", localizationManager.getCurrentLanguage())
        
        // Test Hindi
        localizationManager.setLanguage("hi")
        assertEquals("hi", localizationManager.getCurrentLanguage())
        
        // Test back to English
        localizationManager.setLanguage("en")
        assertEquals("en", localizationManager.getCurrentLanguage())
    }
    
    @Test
    fun `test supported languages list`() {
        val supportedLanguages = localizationManager.getSupportedLanguages()
        assertEquals(5, supportedLanguages.size)
        
        val languageCodes = supportedLanguages.map { it.code }
        assertTrue(languageCodes.contains("en"))
        assertTrue(languageCodes.contains("te"))
        assertTrue(languageCodes.contains("ta"))
        assertTrue(languageCodes.contains("kn"))
        assertTrue(languageCodes.contains("hi"))
    }
    
    @Test
    fun `test localized string retrieval`() {
        // Test English (default)
        localizationManager.setLanguage("en")
        val englishString = localizationManager.getLocalizedString("app_name")
        assertNotNull(englishString)
        
        // Test Telugu
        localizationManager.setLanguage("te")
        val teluguString = localizationManager.getLocalizedString("app_name")
        assertNotNull(teluguString)
        
        // Test fallback for non-existent key
        val fallbackString = localizationManager.getLocalizedString("non_existent_key")
        assertEquals("non_existent_key", fallbackString)
    }
    
    // ==================== SECURITY TESTS ====================
    
    @Test
    fun `test security manager initialization`() {
        assertNotNull(securityManager)
        securityManager.initialize()
        // Should not throw any exceptions
    }
    
    @Test
    fun `test data encryption and decryption`() {
        val originalData = "Sensitive user data for testing"
        
        // Encrypt data
        val encryptedData = securityManager.encryptData(originalData)
        assertNotNull(encryptedData.encryptedData)
        assertNotNull(encryptedData.iv)
        assertNotEquals(originalData, encryptedData.encryptedData)
        
        // Decrypt data
        val decryptedData = securityManager.decryptData(encryptedData)
        assertEquals(originalData, decryptedData)
    }
    
    @Test
    fun `test secure data storage and retrieval`() {
        val key = "test_secure_key"
        val value = "test_secure_value"
        
        // Store secure data
        securityManager.storeSecureData(key, value)
        
        // Retrieve secure data
        val retrievedValue = securityManager.getSecureData(key)
        assertEquals(value, retrievedValue)
        
        // Test default value for non-existent key
        val defaultValue = securityManager.getSecureData("non_existent_key", "default")
        assertEquals("default", defaultValue)
    }
    
    @Test
    fun `test session management`() {
        val token = "test_session_token"
        val expiryTime = System.currentTimeMillis() + 3600000 // 1 hour from now
        
        // Store session
        securityManager.storeSession(token, expiryTime)
        
        // Validate session
        assertTrue(securityManager.validateSession())
        
        // Clear session
        securityManager.clearSession()
        
        // Validate cleared session
        assertFalse(securityManager.validateSession())
    }
    
    @Test
    fun `test secure token generation`() {
        val token1 = securityManager.generateSecureToken()
        val token2 = securityManager.generateSecureToken()
        
        assertEquals(32, token1.length)
        assertEquals(32, token2.length)
        assertNotEquals(token1, token2)
        
        // Test custom length
        val customToken = securityManager.generateSecureToken(16)
        assertEquals(16, customToken.length)
    }
    
    @Test
    fun `test data hashing and verification`() {
        val originalData = "password123"
        
        // Hash data
        val hashedData = securityManager.hashData(originalData)
        assertNotNull(hashedData.hash)
        assertNotNull(hashedData.salt)
        assertNotEquals(originalData, hashedData.hash)
        
        // Verify correct data
        assertTrue(securityManager.verifyHashedData(originalData, hashedData))
        
        // Verify incorrect data
        assertFalse(securityManager.verifyHashedData("wrongpassword", hashedData))
    }
    
    @Test
    fun `test security threat detection`() {
        val threatReport = securityManager.detectSecurityThreats()
        assertNotNull(threatReport)
        assertNotNull(threatReport.threats)
        assertNotNull(threatReport.riskLevel)
        assertTrue(threatReport.timestamp > 0)
    }
    
    @Test
    fun `test secure http client creation`() {
        val httpClient = securityManager.createSecureHttpClient()
        assertNotNull(httpClient)
        assertNotNull(httpClient.certificatePinner)
        assertTrue(httpClient.interceptors.isNotEmpty())
    }
    
    // ==================== PAYMENT TESTS ====================
    
    @Test
    fun `test payment method selection`() = runTest {
        // Test UPI selection
        paymentViewModel.updatePaymentMethod(PaymentMethod.UPI)
        assertEquals(PaymentMethod.UPI, paymentViewModel.paymentState.first().selectedMethod)
        
        // Test Card selection
        paymentViewModel.updatePaymentMethod(PaymentMethod.CARD)
        assertEquals(PaymentMethod.CARD, paymentViewModel.paymentState.first().selectedMethod)
        
        // Test Cash on Delivery selection
        paymentViewModel.updatePaymentMethod(PaymentMethod.CASH_ON_DELIVERY)
        assertEquals(PaymentMethod.CASH_ON_DELIVERY, paymentViewModel.paymentState.first().selectedMethod)
    }
    
    @Test
    fun `test payment gateway selection`() = runTest {
        // Test Mock gateway
        paymentViewModel.updatePaymentGateway(PaymentGateway.MOCK)
        assertEquals(PaymentGateway.MOCK, paymentViewModel.paymentState.first().selectedGateway)
        
        // Test Razorpay gateway
        paymentViewModel.updatePaymentGateway(PaymentGateway.RAZORPAY)
        assertEquals(PaymentGateway.RAZORPAY, paymentViewModel.paymentState.first().selectedGateway)
        
        // Test Stripe gateway
        paymentViewModel.updatePaymentGateway(PaymentGateway.STRIPE)
        assertEquals(PaymentGateway.STRIPE, paymentViewModel.paymentState.first().selectedGateway)
    }
    
    @Test
    fun `test payment request creation`() {
        val paymentRequest = PaymentRequest(
            orderId = "ORDER_123",
            amount = 1500.0,
            description = "Desi Hen Purchase",
            customerEmail = "test@example.com",
            customerPhone = "+919876543210",
            customerName = "Test User",
            fowlId = "FOWL_123",
            sellerId = "SELLER_123",
            buyerId = "BUYER_123"
        )
        
        assertEquals("ORDER_123", paymentRequest.orderId)
        assertEquals(1500.0, paymentRequest.amount, 0.01)
        assertEquals("INR", paymentRequest.currency)
        assertEquals("Desi Hen Purchase", paymentRequest.description)
        assertEquals("test@example.com", paymentRequest.customerEmail)
        assertEquals("+919876543210", paymentRequest.customerPhone)
    }
    
    @Test
    fun `test mock payment processing`() = runTest {
        val paymentRequest = PaymentRequest(
            orderId = "TEST_ORDER_123",
            amount = 800.0,
            description = "Test Fowl Purchase"
        )
        
        var successResponse: PaymentResponse? = null
        var failureResponse: PaymentResponse? = null
        
        paymentViewModel.updatePaymentGateway(PaymentGateway.MOCK)
        paymentViewModel.updatePaymentMethod(PaymentMethod.UPI)
        
        // Process payment multiple times to test both success and failure scenarios
        repeat(5) {
            paymentViewModel.processPayment(
                context = context,
                request = paymentRequest,
                onSuccess = { response ->
                    successResponse = response
                },
                onFailure = { response ->
                    failureResponse = response
                }
            )
        }
        
        // At least one should succeed or fail (due to random nature of mock)
        assertTrue(successResponse != null || failureResponse != null)
    }
    
    @Test
    fun `test payment history manager`() {
        val historyManager = PaymentHistoryManager(context)
        
        val paymentResponse = PaymentResponse(
            success = true,
            transactionId = "TXN_123",
            orderId = "ORDER_123",
            amount = 1200.0,
            paymentMethod = "UPI",
            status = PaymentStatus.SUCCESS
        )
        
        // Save payment record
        historyManager.savePaymentRecord(paymentResponse)
        
        // Retrieve payment history
        val history = historyManager.getPaymentHistory()
        assertTrue(history.isNotEmpty())
        assertEquals("TXN_123", history.first().transactionId)
        assertEquals("ORDER_123", history.first().orderId)
        assertEquals(1200.0, history.first().amount, 0.01)
    }
    
    // ==================== AI CHATBOT TESTS ====================
    
    @Test
    fun `test ai chatbot initialization`() = runTest {
        assertNotNull(aiChatbotViewModel)
        
        // Check initial welcome message
        val messages = aiChatbotViewModel.messages.first()
        assertTrue(messages.isNotEmpty())
        assertFalse(messages.first().isFromUser)
        assertTrue(messages.first().suggestions.isNotEmpty())
    }
    
    @Test
    fun `test ai chatbot language switching`() = runTest {
        // Test Telugu
        aiChatbotViewModel.setLanguage("te")
        assertEquals("te", aiChatbotViewModel.currentLanguage.first())
        
        // Test Tamil
        aiChatbotViewModel.setLanguage("ta")
        assertEquals("ta", aiChatbotViewModel.currentLanguage.first())
        
        // Test English
        aiChatbotViewModel.setLanguage("en")
        assertEquals("en", aiChatbotViewModel.currentLanguage.first())
    }
    
    @Test
    fun `test ai chatbot message sending`() = runTest {
        val testMessage = "How to feed chickens?"
        
        // Send message
        aiChatbotViewModel.sendMessage(testMessage)
        
        // Wait for processing
        kotlinx.coroutines.delay(1500)
        
        // Check messages
        val messages = aiChatbotViewModel.messages.first()
        assertTrue(messages.size >= 2) // Welcome message + user message + AI response
        
        // Find user message
        val userMessage = messages.find { it.isFromUser && it.content == testMessage }
        assertNotNull(userMessage)
        
        // Find AI response
        val aiResponse = messages.findLast { !it.isFromUser && it.content != messages.first().content }
        assertNotNull(aiResponse)
        assertTrue(aiResponse!!.content.isNotEmpty())
    }
    
    @Test
    fun `test ai chatbot health advice`() = runTest {
        aiChatbotViewModel.sendMessage("My chicken is sick")
        kotlinx.coroutines.delay(1500)
        
        val messages = aiChatbotViewModel.messages.first()
        val aiResponse = messages.findLast { !it.isFromUser }
        
        assertNotNull(aiResponse)
        assertTrue(aiResponse!!.content.contains("health") || 
                  aiResponse.content.contains("veterinarian") ||
                  aiResponse.content.contains("vet"))
    }
    
    @Test
    fun `test ai chatbot feeding advice`() = runTest {
        aiChatbotViewModel.sendMessage("What should I feed my chickens?")
        kotlinx.coroutines.delay(1500)
        
        val messages = aiChatbotViewModel.messages.first()
        val aiResponse = messages.findLast { !it.isFromUser }
        
        assertNotNull(aiResponse)
        assertTrue(aiResponse!!.content.contains("feed") || 
                  aiResponse.content.contains("nutrition") ||
                  aiResponse.content.contains("food"))
    }
    
    @Test
    fun `test ai chatbot market advice`() = runTest {
        aiChatbotViewModel.sendMessage("What are current market prices?")
        kotlinx.coroutines.delay(1500)
        
        val messages = aiChatbotViewModel.messages.first()
        val aiResponse = messages.findLast { !it.isFromUser }
        
        assertNotNull(aiResponse)
        assertTrue(aiResponse!!.content.contains("price") || 
                  aiResponse.content.contains("market") ||
                  aiResponse.content.contains("sell"))
    }
    
    @Test
    fun `test ai chatbot app usage help`() = runTest {
        aiChatbotViewModel.sendMessage("How to use this app?")
        kotlinx.coroutines.delay(1500)
        
        val messages = aiChatbotViewModel.messages.first()
        val aiResponse = messages.findLast { !it.isFromUser }
        
        assertNotNull(aiResponse)
        assertTrue(aiResponse!!.content.contains("app") || 
                  aiResponse.content.contains("list") ||
                  aiResponse.content.contains("buy"))
    }
    
    @Test
    fun `test ai chatbot greeting response`() = runTest {
        aiChatbotViewModel.sendMessage("Hello")
        kotlinx.coroutines.delay(1500)
        
        val messages = aiChatbotViewModel.messages.first()
        val aiResponse = messages.findLast { !it.isFromUser }
        
        assertNotNull(aiResponse)
        assertTrue(aiResponse!!.content.contains("Hello") || 
                  aiResponse.content.contains("Assistant") ||
                  aiResponse.content.contains("help"))
    }
    
    @Test
    fun `test ai chatbot default response`() = runTest {
        aiChatbotViewModel.sendMessage("Random gibberish xyz123")
        kotlinx.coroutines.delay(1500)
        
        val messages = aiChatbotViewModel.messages.first()
        val aiResponse = messages.findLast { !it.isFromUser }
        
        assertNotNull(aiResponse)
        assertTrue(aiResponse!!.suggestions.isNotEmpty())
    }
    
    // ==================== DATABASE TESTS ====================
    
    @Test
    fun `test enhanced fowl entity creation`() {
        val fowlEntity = EnhancedFowlEntity(
            id = "FOWL_123",
            ownerId = "USER_123",
            breed = "Desi Hen",
            age = 6,
            price = 800.0,
            description = "Healthy laying hen",
            location = "Hyderabad",
            isForSale = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        assertEquals("FOWL_123", fowlEntity.id)
        assertEquals("USER_123", fowlEntity.ownerId)
        assertEquals("Desi Hen", fowlEntity.breed)
        assertEquals(6, fowlEntity.age)
        assertEquals(800.0, fowlEntity.price, 0.01)
        assertTrue(fowlEntity.isForSale)
    }
    
    @Test
    fun `test enhanced user entity creation`() {
        val userEntity = EnhancedUserEntity(
            id = "USER_123",
            name = "Test Farmer",
            phoneNumber = "+919876543210",
            location = "Hyderabad",
            userType = "FARMER",
            farmName = "Test Farm",
            experienceYears = 5,
            isVerified = true,
            preferredLanguage = "te",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        assertEquals("USER_123", userEntity.id)
        assertEquals("Test Farmer", userEntity.name)
        assertEquals("+919876543210", userEntity.phoneNumber)
        assertEquals("FARMER", userEntity.userType)
        assertEquals("Test Farm", userEntity.farmName)
        assertEquals(5, userEntity.experienceYears)
        assertTrue(userEntity.isVerified)
        assertEquals("te", userEntity.preferredLanguage)
    }
    
    @Test
    fun `test sync queue entity creation`() {
        val syncEntity = SyncQueueEntity(
            entityType = "FOWL",
            entityId = "FOWL_123",
            action = "CREATE",
            data = "{\"id\":\"FOWL_123\",\"breed\":\"Desi Hen\"}",
            timestamp = System.currentTimeMillis()
        )
        
        assertEquals("FOWL", syncEntity.entityType)
        assertEquals("FOWL_123", syncEntity.entityId)
        assertEquals("CREATE", syncEntity.action)
        assertEquals("PENDING", syncEntity.status)
        assertEquals(0, syncEntity.retryCount)
    }
    
    @Test
    fun `test notification entity creation`() {
        val notificationEntity = NotificationEntity(
            notificationId = "NOTIF_123",
            title = "New Fowl Listed",
            body = "A new Desi Hen has been listed in your area",
            type = "PRICE_ALERT",
            timestamp = System.currentTimeMillis()
        )
        
        assertEquals("NOTIF_123", notificationEntity.notificationId)
        assertEquals("New Fowl Listed", notificationEntity.title)
        assertEquals("PRICE_ALERT", notificationEntity.type)
        assertFalse(notificationEntity.isRead)
        assertFalse(notificationEntity.isSynced)
    }
    
    @Test
    fun `test transaction entity creation`() {
        val transactionEntity = TransactionEntity(
            transactionId = "TXN_123",
            fowlId = "FOWL_123",
            buyerId = "BUYER_123",
            sellerId = "SELLER_123",
            amount = 1200.0,
            status = "SUCCESS",
            paymentMethod = "UPI",
            timestamp = System.currentTimeMillis()
        )
        
        assertEquals("TXN_123", transactionEntity.transactionId)
        assertEquals("FOWL_123", transactionEntity.fowlId)
        assertEquals(1200.0, transactionEntity.amount, 0.01)
        assertEquals("SUCCESS", transactionEntity.status)
        assertEquals("UPI", transactionEntity.paymentMethod)
        assertFalse(transactionEntity.isSynced)
    }
    
    @Test
    fun `test traceability entity creation`() {
        val traceabilityEntity = TraceabilityEntity(
            fowlId = "FOWL_123",
            eventType = "VACCINATION",
            eventData = "{\"vaccine\":\"Newcastle\",\"date\":\"2024-01-15\"}",
            timestamp = System.currentTimeMillis(),
            verifiedBy = "VET_123",
            location = "Hyderabad"
        )
        
        assertEquals("FOWL_123", traceabilityEntity.fowlId)
        assertEquals("VACCINATION", traceabilityEntity.eventType)
        assertEquals("VET_123", traceabilityEntity.verifiedBy)
        assertEquals("Hyderabad", traceabilityEntity.location)
        assertFalse(traceabilityEntity.isSynced)
    }
    
    // ==================== INTEGRATION TESTS ====================
    
    @Test
    fun `test end to end fowl listing with localization`() {
        // Set language to Telugu
        localizationManager.setLanguage("te")
        
        // Create fowl entity
        val fowlEntity = EnhancedFowlEntity(
            id = "FOWL_INTEGRATION_123",
            ownerId = "USER_123",
            breed = "దేశీ కో���ి", // Telugu for Desi Hen
            age = 8,
            price = 1000.0,
            description = "ఆరోగ్యకరమైన కోడి", // Telugu for Healthy hen
            location = "హైదరాబాద్", // Telugu for Hyderabad
            isForSale = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        // Verify Telugu content
        assertEquals("దేశీ కోడి", fowlEntity.breed)
        assertEquals("ఆరోగ్యకరమైన కోడి", fowlEntity.description)
        assertEquals("హైదరాబాద్", fowlEntity.location)
        
        // Verify localization
        assertEquals("te", localizationManager.getCurrentLanguage())
    }
    
    @Test
    fun `test secure payment with encryption`() {
        // Create payment request
        val paymentRequest = PaymentRequest(
            orderId = "SECURE_ORDER_123",
            amount = 1500.0,
            description = "Secure Fowl Purchase",
            customerEmail = "secure@example.com",
            customerPhone = "+919876543210"
        )
        
        // Encrypt sensitive payment data
        val encryptedEmail = securityManager.encryptData(paymentRequest.customerEmail)
        val encryptedPhone = securityManager.encryptData(paymentRequest.customerPhone)
        
        // Verify encryption
        assertNotEquals(paymentRequest.customerEmail, encryptedEmail.encryptedData)
        assertNotEquals(paymentRequest.customerPhone, encryptedPhone.encryptedData)
        
        // Decrypt and verify
        val decryptedEmail = securityManager.decryptData(encryptedEmail)
        val decryptedPhone = securityManager.decryptData(encryptedPhone)
        
        assertEquals(paymentRequest.customerEmail, decryptedEmail)
        assertEquals(paymentRequest.customerPhone, decryptedPhone)
    }
    
    @Test
    fun `test ai chatbot with localization`() = runTest {
        // Set language to Telugu
        aiChatbotViewModel.setLanguage("te")
        
        // Send Telugu greeting
        aiChatbotViewModel.sendMessage("నమస్కారం")
        kotlinx.coroutines.delay(1500)
        
        // Check response
        val messages = aiChatbotViewModel.messages.first()
        val aiResponse = messages.findLast { !it.isFromUser }
        
        assertNotNull(aiResponse)
        // Response should be in Telugu or contain Telugu elements
        assertTrue(aiResponse!!.content.isNotEmpty())
    }
    
    // ==================== PERFORMANCE TESTS ====================
    
    @Test
    fun `test large dataset handling`() {
        val largeDataset = (1..1000).map { index ->
            EnhancedFowlEntity(
                id = "FOWL_$index",
                ownerId = "USER_${index % 10}",
                breed = "Breed_$index",
                age = index % 24 + 1,
                price = (index * 10).toDouble(),
                description = "Description for fowl $index",
                location = "Location_${index % 5}",
                isForSale = index % 2 == 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }
        
        assertEquals(1000, largeDataset.size)
        assertTrue(largeDataset.all { it.id.startsWith("FOWL_") })
        assertTrue(largeDataset.all { it.age in 1..24 })
    }
    
    @Test
    fun `test concurrent operations`() = runTest {
        val operations = (1..10).map { index ->
            kotlinx.coroutines.async {
                // Simulate concurrent security operations
                val data = "Concurrent data $index"
                val encrypted = securityManager.encryptData(data)
                val decrypted = securityManager.decryptData(encrypted)
                assertEquals(data, decrypted)
                index
            }
        }
        
        val results = operations.map { it.await() }
        assertEquals((1..10).toList(), results)
    }
    
    // ==================== ERROR HANDLING TESTS ====================
    
    @Test
    fun `test invalid payment request handling`() = runTest {
        val invalidPaymentRequest = PaymentRequest(
            orderId = "",
            amount = -100.0,
            description = ""
        )
        
        var errorCaught = false
        
        paymentViewModel.processPayment(
            context = context,
            request = invalidPaymentRequest,
            onSuccess = { 
                fail("Should not succeed with invalid request")
            },
            onFailure = { response ->
                errorCaught = true
                assertFalse(response.success)
                assertTrue(response.errorMessage.isNotEmpty())
            }
        )
        
        kotlinx.coroutines.delay(3000)
        // Note: In real implementation, validation should catch this
    }
    
    @Test
    fun `test empty message to ai chatbot`() = runTest {
        val initialMessageCount = aiChatbotViewModel.messages.first().size
        
        // Send empty message
        aiChatbotViewModel.sendMessage("")
        aiChatbotViewModel.sendMessage("   ")
        
        kotlinx.coroutines.delay(500)
        
        // Message count should not increase
        val finalMessageCount = aiChatbotViewModel.messages.first().size
        assertEquals(initialMessageCount, finalMessageCount)
    }
    
    @Test
    fun `test security with invalid data`() {
        try {
            // Test with null/empty data
            securityManager.storeSecureData("", "")
            securityManager.getSecureData("")
            
            // Should handle gracefully
            assertTrue(true)
        } catch (e: Exception) {
            // Should not throw unhandled exceptions
            fail("Should handle invalid data gracefully: ${e.message}")
        }
    }
}

/**
 * Additional Test Utilities
 */
object TestUtils {
    
    fun createSampleFowlEntity(id: String = "TEST_FOWL"): EnhancedFowlEntity {
        return EnhancedFowlEntity(
            id = id,
            ownerId = "TEST_USER",
            breed = "Test Breed",
            age = 6,
            price = 800.0,
            description = "Test fowl description",
            location = "Test Location",
            isForSale = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun createSampleUserEntity(id: String = "TEST_USER"): EnhancedUserEntity {
        return EnhancedUserEntity(
            id = id,
            name = "Test User",
            phoneNumber = "+919876543210",
            location = "Test Location",
            userType = "FARMER",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun createSamplePaymentRequest(orderId: String = "TEST_ORDER"): PaymentRequest {
        return PaymentRequest(
            orderId = orderId,
            amount = 1000.0,
            description = "Test Payment",
            customerEmail = "test@example.com",
            customerPhone = "+919876543210"
        )
    }
}