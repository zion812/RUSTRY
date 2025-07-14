package com.rio.rustry.presentation.marketplace

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.presentation.theme.RoosterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class MarketplaceScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun marketplaceScreen_displaysSearchBar() {
        composeTestRule.setContent {
            RoosterTheme {
                EnhancedMarketplaceScreen()
            }
        }

        composeTestRule
            .onNodeWithText("Search fowls, breeds, location...")
            .assertIsDisplayed()
    }

    @Test
    fun marketplaceScreen_displaysFilterButton() {
        composeTestRule.setContent {
            RoosterTheme {
                EnhancedMarketplaceScreen()
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Filters")
            .assertIsDisplayed()
    }

    @Test
    fun fowlCard_displaysCorrectInformation() {
        val mockFowl = Fowl(
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
            RoosterTheme {
                EnhancedFowlCard(
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
        val mockFowl = Fowl(
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
            RoosterTheme {
                EnhancedFowlCard(
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
            RoosterTheme {
                EmptyState()
            }
        }

        composeTestRule
            .onNodeWithText("No fowls found")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Try adjusting your search or filters")
            .assertIsDisplayed()
    }
}