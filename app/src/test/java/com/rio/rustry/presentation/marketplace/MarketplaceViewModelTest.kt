package com.rio.rustry.presentation.marketplace

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.data.model.FowlBreed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.util.*

@ExperimentalCoroutinesApi
class MarketplaceViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: MockFowlRepository
    private lateinit var viewModel: MarketplaceViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockFowlRepository()
        viewModel = MarketplaceViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMarketplaceFowls should update state with fowls on success`() = runTest {
        // Given
        val mockFowls = listOf(
            createMockFowl("1", "Rhode Island Red", 1200.0),
            createMockFowl("2", "Leghorn", 800.0)
        )
        mockRepository.setMockFowls(mockFowls)

        // When
        viewModel.loadMarketplaceFowls()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(mockFowls.size, uiState.fowls.size)
        assertEquals(mockFowls.size, uiState.filteredFowls.size)
        assertNull(uiState.error)
    }

    @Test
    fun `searchFowls should filter fowls by query`() = runTest {
        // Given
        val mockFowls = listOf(
            createMockFowl("1", "Rhode Island Red", 1200.0, description = "Healthy laying hen"),
            createMockFowl("2", "Leghorn", 800.0, description = "Young rooster"),
            createMockFowl("3", "Brahma", 1500.0, description = "Large breed chicken")
        )
        mockRepository.setMockFowls(mockFowls)

        // When
        viewModel.loadMarketplaceFowls()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.searchFowls("Rhode")

        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Rhode", uiState.searchQuery)
        assertEquals(1, uiState.filteredFowls.size)
        assertEquals("Rhode Island Red", uiState.filteredFowls.first().breed)
    }

    @Test
    fun `filterByBreed should filter fowls by selected breed`() = runTest {
        // Given
        val mockFowls = listOf(
            createMockFowl("1", "Rhode Island Red", 1200.0),
            createMockFowl("2", "Leghorn", 800.0),
            createMockFowl("3", "Rhode Island Red", 1300.0)
        )
        mockRepository.setMockFowls(mockFowls)

        // When
        viewModel.loadMarketplaceFowls()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.filterByBreed(FowlBreed.RHODE_ISLAND_RED)

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(FowlBreed.RHODE_ISLAND_RED, uiState.selectedBreed)
        assertEquals(2, uiState.filteredFowls.size)
        assertTrue(uiState.filteredFowls.all { it.breed == "Rhode Island Red" })
    }

    @Test
    fun `filterByPriceRange should filter fowls by price range`() = runTest {
        // Given
        val mockFowls = listOf(
            createMockFowl("1", "Rhode Island Red", 400.0),
            createMockFowl("2", "Leghorn", 800.0),
            createMockFowl("3", "Brahma", 1500.0)
        )
        mockRepository.setMockFowls(mockFowls)

        // When
        viewModel.loadMarketplaceFowls()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.filterByPriceRange(PriceRange.RANGE_500_1000)

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(PriceRange.RANGE_500_1000, uiState.selectedPriceRange)
        assertEquals(1, uiState.filteredFowls.size)
        assertEquals(800.0, uiState.filteredFowls.first().price, 0.01)
    }

    @Test
    fun `toggleVerifiedFilter should filter traceable fowls only`() = runTest {
        // Given
        val mockFowls = listOf(
            createMockFowl("1", "Rhode Island Red", 1200.0, isTraceable = true),
            createMockFowl("2", "Leghorn", 800.0, isTraceable = false),
            createMockFowl("3", "Brahma", 1500.0, isTraceable = true)
        )
        mockRepository.setMockFowls(mockFowls)

        // When
        viewModel.loadMarketplaceFowls()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.toggleVerifiedFilter()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.isVerifiedOnly)
        assertEquals(2, uiState.filteredFowls.size)
        assertTrue(uiState.filteredFowls.all { it.isTraceable })
    }

    @Test
    fun `toggleFavorite should add fowl to favorites`() = runTest {
        // Given
        val fowlId = "test_fowl_1"

        // When
        viewModel.toggleFavorite(fowlId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.favoriteFowlIds.contains(fowlId))
        assertTrue(mockRepository.getFavorites().contains(fowlId))
    }

    @Test
    fun `toggleFavorite should remove fowl from favorites when already favorited`() = runTest {
        // Given
        val fowlId = "test_fowl_1"
        
        // First add to favorites
        viewModel.toggleFavorite(fowlId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When - toggle again to remove
        viewModel.toggleFavorite(fowlId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.favoriteFowlIds.contains(fowlId))
        assertFalse(mockRepository.getFavorites().contains(fowlId))
    }

    @Test
    fun `loadMoreFowls should append new fowls to existing list`() = runTest {
        // Given
        val mockFowls = (1..25).map { 
            createMockFowl(it.toString(), "Breed $it", (500 + it * 100).toDouble())
        }
        mockRepository.setMockFowls(mockFowls)

        // When
        viewModel.loadMarketplaceFowls()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.loadMoreFowls()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.fowls.size > 20) // Should have loaded more than initial page
        assertEquals(1, uiState.currentPage)
        assertFalse(uiState.isLoadingMore)
    }

    @Test
    fun `clearFilters should reset all filters`() = runTest {
        // Given
        viewModel.searchFowls("test")
        viewModel.filterByBreed(FowlBreed.RHODE_ISLAND_RED)
        viewModel.filterByPriceRange(PriceRange.RANGE_500_1000)
        viewModel.toggleVerifiedFilter()

        // When
        viewModel.clearFilters()

        // Then
        val uiState = viewModel.uiState.value
        assertEquals("", uiState.searchQuery)
        assertNull(uiState.selectedBreed)
        assertNull(uiState.selectedPriceRange)
        assertFalse(uiState.isVerifiedOnly)
        assertFalse(uiState.isNearbyOnly)
    }

    private fun createMockFowl(
        id: String,
        breed: String,
        price: Double,
        description: String = "Test description",
        location: String = "Test Location",
        isTraceable: Boolean = false
    ): Fowl {
        return Fowl(
            id = id,
            breed = breed,
            ownerName = "Test Owner",
            price = price,
            description = description,
            location = location,
            isTraceable = isTraceable,
            isAvailable = true,
            dateOfBirth = Date(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}

// Mock repository for testing
class MockFowlRepository : FowlRepository() {
    private val mockFowls = mutableListOf<Fowl>()
    private val mockFavorites = mutableSetOf<String>()

    fun setMockFowls(fowls: List<Fowl>) {
        mockFowls.clear()
        mockFowls.addAll(fowls)
    }

    override suspend fun getMarketplaceFowls(page: Int, pageSize: Int): List<Fowl> {
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, mockFowls.size)
        return if (startIndex < mockFowls.size) {
            mockFowls.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }

    override suspend fun addToFavorites(fowlId: String) {
        mockFavorites.add(fowlId)
    }

    override suspend fun removeFromFavorites(fowlId: String) {
        mockFavorites.remove(fowlId)
    }

    fun getFavorites(): Set<String> = mockFavorites.toSet()
}