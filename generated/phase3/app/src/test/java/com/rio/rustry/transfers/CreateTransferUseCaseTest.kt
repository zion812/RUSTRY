// generated/phase3/app/src/test/java/com/rio/rustry/transfers/CreateTransferUseCaseTest.kt

package com.rio.rustry.transfers

import com.rio.rustry.analytics.AnalyticsService
import com.rio.rustry.data.model.Transfer
import com.rio.rustry.data.model.TransferStatus
import com.rio.rustry.domain.repository.TransferRepository
import com.rio.rustry.domain.usecase.CreateTransferUseCase
import com.rio.rustry.test.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreateTransferUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transferRepository = mockk<TransferRepository>(relaxed = true)
    private val analyticsService = mockk<AnalyticsService>(relaxed = true)
    private lateinit var useCase: CreateTransferUseCase

    @Before
    fun setup() {
        useCase = CreateTransferUseCase(transferRepository, analyticsService)
    }

    @Test
    fun `invoke should create transfer and log analytics`() = runTest {
        // Given
        val fowlId = "fowl123"
        val recipient = "test@example.com"
        val contactMethod = ContactMethod.EMAIL
        val transferSlot = slot<Transfer>()

        coEvery { transferRepository.createTransfer(capture(transferSlot)) } returns Unit

        // When
        val transferId = useCase(fowlId, recipient, contactMethod)

        // Then
        assertNotNull(transferId)
        assertTrue(transferId.isNotEmpty())

        val capturedTransfer = transferSlot.captured
        assertEquals(fowlId, capturedTransfer.fowlId)
        assertEquals(recipient, capturedTransfer.toUid)
        assertEquals(TransferStatus.PENDING, capturedTransfer.status)
        assertTrue(capturedTransfer.timestamp > 0)

        coVerify { transferRepository.createTransfer(any()) }
        coVerify { 
            analyticsService.logTransferInitiated(
                fowlId = fowlId,
                transferMethod = "email"
            )
        }
    }

    @Test
    fun `invoke should handle phone contact method`() = runTest {
        // Given
        val fowlId = "fowl456"
        val recipient = "+1234567890"
        val contactMethod = ContactMethod.PHONE

        // When
        useCase(fowlId, recipient, contactMethod)

        // Then
        coVerify { 
            analyticsService.logTransferInitiated(
                fowlId = fowlId,
                transferMethod = "phone"
            )
        }
    }

    @Test
    fun `invoke should propagate repository exceptions`() = runTest {
        // Given
        val fowlId = "fowl789"
        val recipient = "test@example.com"
        val contactMethod = ContactMethod.EMAIL
        val exception = Exception("Database error")

        coEvery { transferRepository.createTransfer(any()) } throws exception

        // When & Then
        try {
            useCase(fowlId, recipient, contactMethod)
            fail("Expected exception to be thrown")
        } catch (e: Exception) {
            assertEquals("Database error", e.message)
        }
    }
}