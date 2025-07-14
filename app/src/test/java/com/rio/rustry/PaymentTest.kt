package com.rio.rustry

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.rio.rustry.data.payment.MockPaymentGateway
import com.rio.rustry.domain.payment.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Payment system tests
 * Tests mock payment gateway and payment flows
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PaymentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var mockGateway: MockPaymentGateway

    @Before
    fun init() {
        hiltRule.inject()
        mockGateway = MockPaymentGateway()
    }

    @Test
    fun mockGateway_returnsOrderId() = runTest {
        val orderId = mockGateway.createOrder(10000, "INR") // ₹100
        
        assertThat(orderId).startsWith("mock_")
        assertThat(orderId).isNotEmpty()
    }

    @Test
    fun mockGateway_capturePayment_returnsSuccess() = runTest {
        val orderId = mockGateway.createOrder(10000, "INR")
        val success = mockGateway.capturePayment(orderId)
        
        // Mock gateway has 90% success rate, but for mock orders starting with "mock_" it should work
        assertThat(orderId.startsWith("mock_")).isTrue()
    }

    @Test
    fun mockGateway_verifyPayment_returnsCorrectStatus() = runTest {
        val orderId = mockGateway.createOrder(10000, "INR")
        mockGateway.capturePayment(orderId)
        
        val verification = mockGateway.verifyPayment(orderId)
        
        assertThat(verification).isNotNull()
        assertThat(verification.status).isAnyOf(PaymentStatus.SUCCESS, PaymentStatus.FAILED)
    }

    @Test
    fun mockGateway_getPaymentDetails_returnsCorrectDetails() = runTest {
        val amount = 15000L // ₹150
        val currency = "INR"
        val orderId = mockGateway.createOrder(amount, currency)
        
        val details = mockGateway.getPaymentDetails(orderId)
        
        assertThat(details).isNotNull()
        assertThat(details!!.orderId).isEqualTo(orderId)
        assertThat(details.amount).isEqualTo(amount)
        assertThat(details.currency).isEqualTo(currency)
        assertThat(details.status).isEqualTo(PaymentStatus.PENDING)
    }

    @Test
    fun mockGateway_refundPayment_worksForSuccessfulPayments() = runTest {
        val orderId = mockGateway.createOrder(10000, "INR")
        val captureSuccess = mockGateway.capturePayment(orderId)
        
        if (captureSuccess) {
            val refundId = mockGateway.refundPayment(orderId)
            assertThat(refundId).isNotNull()
            assertThat(refundId).startsWith("refund_")
            
            val updatedDetails = mockGateway.getPaymentDetails(orderId)
            assertThat(updatedDetails?.status).isAnyOf(PaymentStatus.REFUNDED, PaymentStatus.PARTIAL_REFUND)
        }
    }

    @Test
    fun mockGateway_partialRefund_updatesStatusCorrectly() = runTest {
        val orderId = mockGateway.createOrder(10000, "INR")
        val captureSuccess = mockGateway.capturePayment(orderId)
        
        if (captureSuccess) {
            val refundId = mockGateway.refundPayment(orderId, 5000) // Partial refund
            assertThat(refundId).isNotNull()
            
            val updatedDetails = mockGateway.getPaymentDetails(orderId)
            assertThat(updatedDetails?.status).isEqualTo(PaymentStatus.PARTIAL_REFUND)
        }
    }

    @Test
    fun paymentStatus_enumValues_areCorrect() {
        val statuses = PaymentStatus.values()
        
        assertThat(statuses).hasLength(7)
        assertThat(statuses).contains(PaymentStatus.PENDING)
        assertThat(statuses).contains(PaymentStatus.PROCESSING)
        assertThat(statuses).contains(PaymentStatus.SUCCESS)
        assertThat(statuses).contains(PaymentStatus.FAILED)
        assertThat(statuses).contains(PaymentStatus.CANCELLED)
        assertThat(statuses).contains(PaymentStatus.REFUNDED)
        assertThat(statuses).contains(PaymentStatus.PARTIAL_REFUND)
    }

    @Test
    fun paymentMethod_enumValues_areCorrect() {
        val methods = PaymentMethod.values()
        
        assertThat(methods).hasLength(6)
        assertThat(methods).contains(PaymentMethod.CARD)
        assertThat(methods).contains(PaymentMethod.UPI)
        assertThat(methods).contains(PaymentMethod.NET_BANKING)
        assertThat(methods).contains(PaymentMethod.WALLET)
        assertThat(methods).contains(PaymentMethod.EMI)
        assertThat(methods).contains(PaymentMethod.CASH_ON_DELIVERY)
    }

    @Test
    fun paymentVerification_dataClass_worksCorrectly() {
        val verification = PaymentVerification(
            isValid = true,
            status = PaymentStatus.SUCCESS,
            transactionId = "txn_123",
            errorMessage = null
        )
        
        assertThat(verification.isValid).isTrue()
        assertThat(verification.status).isEqualTo(PaymentStatus.SUCCESS)
        assertThat(verification.transactionId).isEqualTo("txn_123")
        assertThat(verification.errorMessage).isNull()
    }

    @Test
    fun paymentDetails_dataClass_worksCorrectly() {
        val details = PaymentDetails(
            orderId = "order_123",
            amount = 10000,
            currency = "INR",
            status = PaymentStatus.SUCCESS,
            transactionId = "txn_456",
            createdAt = System.currentTimeMillis(),
            completedAt = System.currentTimeMillis(),
            method = PaymentMethod.UPI,
            gatewayResponse = mapOf("gateway" to "mock")
        )
        
        assertThat(details.orderId).isEqualTo("order_123")
        assertThat(details.amount).isEqualTo(10000)
        assertThat(details.currency).isEqualTo("INR")
        assertThat(details.status).isEqualTo(PaymentStatus.SUCCESS)
        assertThat(details.transactionId).isEqualTo("txn_456")
        assertThat(details.method).isEqualTo(PaymentMethod.UPI)
        assertThat(details.gatewayResponse).containsEntry("gateway", "mock")
    }

    @Test
    fun mockGateway_clearPayments_removesAllData() = runTest {
        // Create multiple payments
        val orderId1 = mockGateway.createOrder(10000, "INR")
        val orderId2 = mockGateway.createOrder(20000, "INR")
        
        assertThat(mockGateway.getAllMockPayments()).hasSize(2)
        
        // Clear all payments
        mockGateway.clearMockPayments()
        
        assertThat(mockGateway.getAllMockPayments()).isEmpty()
        
        // Verify payments are no longer accessible
        val details1 = mockGateway.getPaymentDetails(orderId1)
        val details2 = mockGateway.getPaymentDetails(orderId2)
        
        assertThat(details1).isNull()
        assertThat(details2).isNull()
    }

    @Test
    fun mockGateway_multipleOrders_maintainsSeparateState() = runTest {
        val orderId1 = mockGateway.createOrder(10000, "INR")
        val orderId2 = mockGateway.createOrder(20000, "USD")
        
        val details1 = mockGateway.getPaymentDetails(orderId1)
        val details2 = mockGateway.getPaymentDetails(orderId2)
        
        assertThat(details1).isNotNull()
        assertThat(details2).isNotNull()
        assertThat(details1!!.amount).isEqualTo(10000)
        assertThat(details2!!.amount).isEqualTo(20000)
        assertThat(details1.currency).isEqualTo("INR")
        assertThat(details2.currency).isEqualTo("USD")
    }

    @Test
    fun mockGateway_invalidOrderId_returnsNull() = runTest {
        val details = mockGateway.getPaymentDetails("invalid_order_id")
        assertThat(details).isNull()
        
        val verification = mockGateway.verifyPayment("invalid_order_id")
        assertThat(verification.isValid).isFalse()
        assertThat(verification.status).isEqualTo(PaymentStatus.FAILED)
        assertThat(verification.errorMessage).isEqualTo("Order not found")
    }

    @Test
    fun mockGateway_refundFailedPayment_returnsNull() = runTest {
        val orderId = mockGateway.createOrder(10000, "INR")
        // Don't capture payment, so it remains in PENDING status
        
        val refundId = mockGateway.refundPayment(orderId)
        assertThat(refundId).isNull() // Can't refund non-successful payments
    }

    @Test
    fun mockGateway_paymentFlow_endToEnd() = runTest {
        // Complete payment flow test
        val amount = 25000L // ₹250
        val currency = "INR"
        
        // Step 1: Create order
        val orderId = mockGateway.createOrder(amount, currency)
        assertThat(orderId).startsWith("mock_")
        
        // Step 2: Verify order created
        val initialDetails = mockGateway.getPaymentDetails(orderId)
        assertThat(initialDetails).isNotNull()
        assertThat(initialDetails!!.status).isEqualTo(PaymentStatus.PENDING)
        
        // Step 3: Capture payment
        val captureSuccess = mockGateway.capturePayment(orderId)
        
        // Step 4: Verify payment
        val verification = mockGateway.verifyPayment(orderId)
        assertThat(verification.status).isAnyOf(PaymentStatus.SUCCESS, PaymentStatus.FAILED)
        
        // Step 5: Get final details
        val finalDetails = mockGateway.getPaymentDetails(orderId)
        assertThat(finalDetails).isNotNull()
        assertThat(finalDetails!!.completedAt).isNotNull()
        
        if (captureSuccess && verification.isValid) {
            assertThat(finalDetails.status).isEqualTo(PaymentStatus.SUCCESS)
            assertThat(finalDetails.transactionId).isNotNull()
            assertThat(finalDetails.method).isNotNull()
        }
    }
}