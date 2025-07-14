package com.rio.rustry

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rio.rustry.presentation.farm.FarmListingScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FowlSaleEndToEndTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        composeTestRule.activity.setContent {
            FarmListingScreen() // Assuming this is the entry point for listing
        }
    }

    @Test
    fun testFowlListingToSaleFlow() {
        // Simulate listing view
        composeTestRule.onNodeWithText("Fowl Listings").assertIsDisplayed()

        // Simulate selecting a fowl item (adjust tags/content descriptions as per actual UI)
        composeTestRule.onNodeWithTag("fowl_item_1").performClick()

        // Verify navigation to detail/sale screen
        composeTestRule.onNodeWithText("Record Sale").assertIsDisplayed()

        // Simulate filling sale details
        composeTestRule.onNodeWithTag("price_input").performTextInput("100.0")
        composeTestRule.onNodeWithTag("buyer_input").performTextInput("Test Buyer")

        // Simulate confirming sale
        composeTestRule.onNodeWithTag("confirm_sale_button").performClick()

        // Verify success state
        composeTestRule.onNodeWithText("Sale Recorded Successfully").assertIsDisplayed()
    }
}
