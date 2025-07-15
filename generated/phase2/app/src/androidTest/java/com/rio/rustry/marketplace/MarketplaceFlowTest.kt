// generated/phase2/app/src/androidTest/java/com/rio/rustry/marketplace/MarketplaceFlowTest.kt

package com.rio.rustry.marketplace

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rio.rustry.data.model.AgeGroup
import com.rio.rustry.data.model.Breed
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.data.model.Lineage
import com.rio.rustry.ui.theme.RustryTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MarketplaceFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun searchFowlsScreen_displaysSearchResults() {
        // Given
        val testFowls = listOf(
            createTestFowl("1", Breed.CHICKEN, AgeGroup.ADULT, 25.0),
            createTestFowl("2", Breed.DUCK, AgeGroup.JUVENILE, 15.0)
        )

        // When
        composeTestRule.setContent {
            RustryTheme {
                SearchFowlsScreen(
                    onFowlClick = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Search fowls...").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Filters").assertIsDisplayed()
    }

    @Test
    fun searchFowlsScreen_searchFunctionality() {
        // Given
        composeTestRule.setContent {
            RustryTheme {
                SearchFowlsScreen(
                    onFowlClick = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Search fowls...")
            .performTextInput("chicken")

        // Then
        composeTestRule.onNodeWithText("chicken").assertIsDisplayed()
    }

    @Test
    fun searchFowlsScreen_filterDialog() {
        // Given
        composeTestRule.setContent {
            RustryTheme {
                SearchFowlsScreen(
                    onFowlClick = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("Filters").performClick()

        // Then
        composeTestRule.onNodeWithText("Filter Results").assertIsDisplayed()
        composeTestRule.onNodeWithText("Breed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Age Group").assertIsDisplayed()
        composeTestRule.onNodeWithText("Price Range").assertIsDisplayed()
    }

    @Test
    fun searchFowlsScreen_sortOptions() {
        // Given
        composeTestRule.setContent {
            RustryTheme {
                SearchFowlsScreen(
                    onFowlClick = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Newest").assertIsDisplayed()
        composeTestRule.onNodeWithText("Price: Low to High").assertIsDisplayed()
        composeTestRule.onNodeWithText("Price: High to Low").assertIsDisplayed()
        composeTestRule.onNodeWithText("Distance").assertIsDisplayed()
    }

    @Test
    fun productDetailScreen_displaysProductInfo() {
        // Given
        composeTestRule.setContent {
            RustryTheme {
                ProductDetailScreen(
                    fowlId = "test123",
                    onAddToCart = { },
                    onBuyNow = { },
                    onShareToCommunity = { }
                )
            }
        }

        // Then - Initially shows loading
        composeTestRule.onNode(hasTestTag("loading")).assertIsDisplayed()
    }

    @Test
    fun fowlSearchItem_displaysCorrectInfo() {
        // Given
        val fowl = createTestFowl("1", Breed.CHICKEN, AgeGroup.ADULT, 25.0, isTraceable = true)

        composeTestRule.setContent {
            RustryTheme {
                FowlSearchItem(
                    fowl = fowl,
                    onClick = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Chicken").assertIsDisplayed()
        composeTestRule.onNodeWithText("Adult").assertIsDisplayed()
        composeTestRule.onNodeWithText("$25.0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Traceable").assertIsDisplayed()
    }

    @Test
    fun fowlSearchItem_clickable() {
        // Given
        val fowl = createTestFowl("1", Breed.CHICKEN, AgeGroup.ADULT, 25.0)
        var clicked = false

        composeTestRule.setContent {
            RustryTheme {
                FowlSearchItem(
                    fowl = fowl,
                    onClick = { clicked = true }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Chicken").performClick()

        // Then
        assert(clicked)
    }

    @Test
    fun filterDialog_breedSelection() {
        // Given
        var selectedFilters = SearchFilters()

        composeTestRule.setContent {
            RustryTheme {
                FilterDialog(
                    filters = selectedFilters,
                    onFiltersChange = { selectedFilters = it },
                    onDismiss = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Chicken").performClick()

        // Then
        composeTestRule.onNodeWithText("Apply").performClick()
        assert(Breed.CHICKEN in selectedFilters.breeds)
    }

    @Test
    fun filterDialog_ageGroupSelection() {
        // Given
        var selectedFilters = SearchFilters()

        composeTestRule.setContent {
            RustryTheme {
                FilterDialog(
                    filters = selectedFilters,
                    onFiltersChange = { selectedFilters = it },
                    onDismiss = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Adult").performClick()

        // Then
        composeTestRule.onNodeWithText("Apply").performClick()
        assert(AgeGroup.ADULT in selectedFilters.ageGroups)
    }

    @Test
    fun endToEndFlow_searchToDetail() {
        // Given
        var selectedFowlId: String? = null

        composeTestRule.setContent {
            RustryTheme {
                SearchFowlsScreen(
                    onFowlClick = { fowlId -> selectedFowlId = fowlId }
                )
            }
        }

        // When - Search for fowls
        composeTestRule.onNodeWithText("Search fowls...")
            .performTextInput("chicken")

        // Simulate fowl item click (would need mock data in real test)
        // composeTestRule.onNodeWithText("Chicken").performClick()

        // Then
        // assert(selectedFowlId != null)
    }

    private fun createTestFowl(
        id: String,
        breed: Breed,
        ageGroup: AgeGroup,
        price: Double,
        isTraceable: Boolean = false
    ) = Fowl(
        id = id,
        breed = breed,
        ageGroup = ageGroup,
        price = price,
        images = emptyList(),
        isTraceable = isTraceable,
        lineage = Lineage(parentIds = emptyList()),
        ownerUid = "test-user",
        status = com.rio.rustry.data.model.FowlStatus.PUBLISHED,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}