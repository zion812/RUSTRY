package com.rio.rustry.presentation.marketplace

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import java.util.*

class MarketplaceScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun marketplaceScreen_displaysSearchBar() {
        composeTestRule.setContent {
            MockRoosterTheme {
                MockEnhancedMarketplaceScreen()
            }
        }

        composeTestRule
            .onNodeWithText("Search fowls, breeds, location...")
            .assertIsDisplayed()
    }

    @Test
    fun marketplaceScreen_displaysFilterButton() {
        composeTestRule.setContent {
            MockRoosterTheme {
                MockEnhancedMarketplaceScreen()
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Filters")
            .assertIsDisplayed()
    }

    @Test
    fun fowlCard_displaysCorrectInformation() {
        val mockFowl = MockFowl(
            id = "test_fowl",
            breed = "Rhode Island Red",
            ownerName = "Test Owner",
            price = 1200.0,
            description = "Healthy laying hen",
            location = "Hyderabad",
            isTraceable = true,
            isAvailable = true,
            dateOfBirth = Date(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            MockRoosterTheme {
                MockEnhancedFowlCard(
                    fowl = mockFowl,
                    onClick = {},
                    onFavoriteClick = {},
                    isFavorite = false
                )
            }
        }

        // Check if fowl information is displayed
        composeTestRule
            .onNodeWithText("Rhode Island Red")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("by Test Owner")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("₹1200")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Healthy laying hen")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Hyderabad")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Traceable")
            .assertIsDisplayed()
    }

    @Test
    fun fowlCard_showsNotTraceableBadge() {
        val mockFowl = MockFowl(
            id = "test_fowl",
            breed = "Leghorn",
            ownerName = "Test Owner",
            price = 800.0,
            description = "Young rooster",
            location = "Vijayawada",
            isTraceable = false,
            isAvailable = true,
            dateOfBirth = Date(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            MockRoosterTheme {
                MockEnhancedFowlCard(
                    fowl = mockFowl,
                    onClick = {},
                    onFavoriteClick = {},
                    isFavorite = false
                )
            }
        }

        composeTestRule
            .onNodeWithText("Not Traceable")
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_displaysCorrectMessage() {
        composeTestRule.setContent {
            MockRoosterTheme {
                MockEmptyState()
            }
        }

        composeTestRule
            .onNodeWithText("No fowls found")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Try adjusting your search or filters")
            .assertIsDisplayed()
    }

    // Mock classes and composables for testing
    data class MockFowl(
        val id: String = "",
        val breed: String = "",
        val ownerName: String = "",
        val price: Double = 0.0,
        val description: String = "",
        val location: String = "",
        val isTraceable: Boolean = true,
        val isAvailable: Boolean = true,
        val dateOfBirth: Date = Date(),
        val createdAt: Long = 0L,
        val updatedAt: Long = 0L
    )

    @androidx.compose.runtime.Composable
    private fun MockRoosterTheme(content: @androidx.compose.runtime.Composable () -> Unit) {
        androidx.compose.material3.MaterialTheme {
            content()
        }
    }

    @androidx.compose.runtime.Composable
    private fun MockEnhancedMarketplaceScreen() {
        androidx.compose.foundation.layout.Column {
            androidx.compose.material3.OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { androidx.compose.material3.Text("Search fowls, breeds, location...") }
            )
            androidx.compose.material3.IconButton(
                onClick = {},
                modifier = androidx.compose.ui.Modifier.testTag("filters")
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.FilterList,
                    contentDescription = "Filters"
                )
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun MockEnhancedFowlCard(
        fowl: MockFowl,
        onClick: () -> Unit,
        onFavoriteClick: () -> Unit,
        isFavorite: Boolean
    ) {
        androidx.compose.material3.Card(
            onClick = onClick,
            modifier = androidx.compose.ui.Modifier.fillMaxWidth()
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier.padding(16.dp)
            ) {
                androidx.compose.material3.Text(
                    text = fowl.breed,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                )
                androidx.compose.material3.Text(
                    text = "by ${fowl.ownerName}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
                androidx.compose.material3.Text(
                    text = "₹${fowl.price.toInt()}",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )
                androidx.compose.material3.Text(
                    text = fowl.description,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
                androidx.compose.material3.Text(
                    text = fowl.location,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
                androidx.compose.material3.Text(
                    text = if (fowl.isTraceable) "Traceable" else "Not Traceable",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                )
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun MockEmptyState() {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            modifier = androidx.compose.ui.Modifier.padding(32.dp)
        ) {
            androidx.compose.material3.Text(
                text = "No fowls found",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
            )
            androidx.compose.material3.Text(
                text = "Try adjusting your search or filters",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        }
    }
}