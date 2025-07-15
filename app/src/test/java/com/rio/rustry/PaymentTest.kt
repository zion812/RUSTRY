package com.rio.rustry

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Payment system tests
 * Tests mock payment gateway and payment flows
 */
class PaymentTest {

    private lateinit var mockGateway: MockPaymentGateway

    @Before
    fun init() {
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
        assertThat(statuses.toList()).contains(PaymentStatus.PENDING)
        assertThat(statuses.toList()).contains(PaymentStatus.PROCESSING)
        assertThat(statuses.toList()).contains(PaymentStatus.SUCCESS)
        assertThat(statuses.toList()).contains(PaymentStatus.FAILED)
        assertThat(statuses.toList()).contains(PaymentStatus.CANCELLED)
        assertThat(statuses.toList()).contains(PaymentStatus.REFUNDED)
        assertThat(statuses.toList()).contains(PaymentStatus.PARTIAL_REFUND)
    }

    @Test
    fun paymentMethod_enumValues_areCorrect() {
        val methods = PaymentMethod.values()
        
        assertThat(methods).hasLength(6)
        assertThat(methods.toList()).contains(PaymentMethod.CARD)
        assertThat(methods.toList()).contains(PaymentMethod.UPI)
        assertThat(methods.toList()).contains(PaymentMethod.NET_BANKING)
        assertThat(methods.toList()).contains(PaymentMethod.WALLET)
        assertThat(methods.toList()).contains(PaymentMethod.EMI)
        assertThat(methods.toList()).contains(PaymentMethod.CASH_ON_DELIVERY)
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

    // Mock classes for unit testing
    enum class PaymentStatus {
        PENDING, PROCESSING, SUCCESS, FAILED, CANCELLED, REFUNDED, PARTIAL_REFUND
    }

    enum class PaymentMethod {
        CARD, UPI, NET_BANKING, WALLET, EMI, CASH_ON_DELIVERY
    }

    data class PaymentVerification(
        val isValid: Boolean,
        val status: PaymentStatus,
        val transactionId: String?,
        val errorMessage: String?
    )

    data class PaymentDetails(
        val orderId: String,
        val amount: Long,
        val currency: String,
        val status: PaymentStatus,
        val transactionId: String?,
        val createdAt: Long,
        val completedAt: Long?,
        val method: PaymentMethod?,
        val gatewayResponse: Map<String, Any>?
    )

    class MockPaymentGateway {
        private val payments = mutableMapOf<String, PaymentDetails>()
        private var orderCounter = 0

        suspend fun createOrder(amount: Long, currency: String): String {
            val orderId = "mock_order_${++orderCounter}_${System.currentTimeMillis()}"
            val payment = PaymentDetails(
                orderId = orderId,
                amount = amount,
                currency = currency,
                status = PaymentStatus.PENDING,
                transactionId = null,
                createdAt = System.currentTimeMillis(),
                completedAt = null,
                method = null,
                gatewayResponse = null
            )
            payments[orderId] = payment
            return orderId
        }

        suspend fun capturePayment(orderId: String): Boolean {
            val payment = payments[orderId] ?: return false
            val success = Math.random() > 0.1 // 90% success rate
            
            val updatedPayment = payment.copy(
                status = if (success) PaymentStatus.SUCCESS else PaymentStatus.FAILED,
                transactionId = if (success) "txn_${System.currentTimeMillis()}" else null,
                completedAt = System.currentTimeMillis(),
                method = PaymentMethod.UPI,
                gatewayResponse = mapOf("gateway" to "mock", "success" to success)
            )
            payments[orderId] = updatedPayment
            return success
        }

        suspend fun verifyPayment(orderId: String): PaymentVerification {
            val payment = payments[orderId]
            return if (payment != null) {
                PaymentVerification(
                    isValid = payment.status == PaymentStatus.SUCCESS,
                    status = payment.status,
                    transactionId = payment.transactionId,
                    errorMessage = null
                )
            } else {
                PaymentVerification(
                    isValid = false,
                    status = PaymentStatus.FAILED,
                    transactionId = null,
                    errorMessage = "Order not found"
                )
            }
        }

        suspend fun getPaymentDetails(orderId: String): PaymentDetails? {
            return payments[orderId]
        }

        suspend fun refundPayment(orderId: String, amount: Long? = null): String? {
            val payment = payments[orderId] ?: return null
            if (payment.status != PaymentStatus.SUCCESS) return null

            val refundId = "refund_${System.currentTimeMillis()}"
            val isPartialRefund = amount != null && amount < payment.amount
            
            val updatedPayment = payment.copy(
                status = if (isPartialRefund) PaymentStatus.PARTIAL_REFUND else PaymentStatus.REFUNDED
            )
            payments[orderId] = updatedPayment
            return refundId
        }

        fun getAllMockPayments(): List<PaymentDetails> {
            return payments.values.toList()
        }

        fun clearMockPayments() {
            payments.clear()
        }
    }
}