// generated/phase3/app/src/androidTest/java/com/rio/rustry/transfers/TransferFlowTest.kt

package com.rio.rustry.transfers

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.data.model.Transfer
import com.rio.rustry.data.model.TransferStatus
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransferFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testInitiateTransferFlow() {
        // Given
        val mockViewModel = mockk<TransferViewModel>(relaxed = true)
        val fowls = listOf(
            Fowl(id = "1", name = "Rooster Red", breed = "Rhode Island Red", price = 50.0),
            Fowl(id = "2", name = "Hen White", breed = "Leghorn", price = 45.0)
        )
        val uiStateFlow = MutableStateFlow<TransferUiState>(TransferUiState.FowlsLoaded(fowls))
        every { mockViewModel.uiState } returns uiStateFlow

        // When
        composeTestRule.setContent {
            InitiateTransferScreen(
                onNavigateBack = {},
                onTransferInitiated = {},
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Initiate Transfer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select Fowl to Transfer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rooster Red").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hen White").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contact Method").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone").assertIsDisplayed()
    }

    @Test
    fun testSelectFowlAndEnterRecipient() {
        // Given
        val mockViewModel = mockk<TransferViewModel>(relaxed = true)
        val fowls = listOf(
            Fowl(id = "1", name = "Test Fowl", breed = "Test Breed", price = 50.0)
        )
        val uiStateFlow = MutableStateFlow<TransferUiState>(TransferUiState.FowlsLoaded(fowls))
        every { mockViewModel.uiState } returns uiStateFlow

        // When
        composeTestRule.setContent {
            InitiateTransferScreen(
                onNavigateBack = {},
                onTransferInitiated = {},
                viewModel = mockViewModel
            )
        }

        // Then - Select fowl
        composeTestRule.onNodeWithText("Test Fowl").performClick()
        
        // Enter recipient email
        composeTestRule.onNodeWithText("Recipient Email").performTextInput("test@example.com")
        
        // Initiate button should be enabled
        composeTestRule.onNodeWithText("Initiate Transfer").assertIsEnabled()
    }

    @Test
    fun testTransferHistoryDisplay() {
        // Given
        val mockViewModel = mockk<TransferViewModel>(relaxed = true)
        val transfers = listOf(
            Transfer(
                id = "transfer1",
                fromUid = "user1",
                toUid = "user2",
                fowlId = "fowl1",
                status = TransferStatus.PENDING,
                timestamp = System.currentTimeMillis()
            ),
            Transfer(
                id = "transfer2",
                fromUid = "user1",
                toUid = "user3",
                fowlId = "fowl2",
                status = TransferStatus.VERIFIED,
                timestamp = System.currentTimeMillis() - 86400000 // 1 day ago
            )
        )
        val uiStateFlow = MutableStateFlow<TransferUiState>(TransferUiState.TransfersLoaded(transfers))
        every { mockViewModel.uiState } returns uiStateFlow

        // When
        composeTestRule.setContent {
            TransferHistoryScreen(
                onTransferClick = {},
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Transfer History").assertIsDisplayed()
        composeTestRule.onNodeWithText("Transfer #transfer1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Transfer #transfer2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pending").assertIsDisplayed()
        composeTestRule.onNodeWithText("Verified").assertIsDisplayed()
    }

    @Test
    fun testFilterTransfers() {
        // Given
        val mockViewModel = mockk<TransferViewModel>(relaxed = true)
        val transfers = listOf(
            Transfer(
                id = "transfer1",
                status = TransferStatus.PENDING,
                timestamp = System.currentTimeMillis()
            )
        )
        val uiStateFlow = MutableStateFlow<TransferUiState>(TransferUiState.TransfersLoaded(transfers))
        every { mockViewModel.uiState } returns uiStateFlow

        // When
        composeTestRule.setContent {
            TransferHistoryScreen(
                onTransferClick = {},
                viewModel = mockViewModel
            )
        }

        // Then - Test filter chips
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Received").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pending").assertIsDisplayed()
        composeTestRule.onNodeWithText("Verified").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rejected").assertIsDisplayed()
        
        // Click on Pending filter
        composeTestRule.onAllNodesWithText("Pending")[0].performClick()
    }

    @Test
    fun testEmptyTransferHistory() {
        // Given
        val mockViewModel = mockk<TransferViewModel>(relaxed = true)
        val uiStateFlow = MutableStateFlow<TransferUiState>(TransferUiState.TransfersLoaded(emptyList()))
        every { mockViewModel.uiState } returns uiStateFlow

        // When
        composeTestRule.setContent {
            TransferHistoryScreen(
                onTransferClick = {},
                viewModel = mockViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("No transfers found").assertIsDisplayed()
    }
}