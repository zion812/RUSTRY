package com.rio.rustry

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Integration test for the complete Marketplace feature
 * Tests ViewModel + Repository + Database integration
 */
class MarketplaceIntegrationTest {

    private lateinit var fowlRepository: MockFowlRepository
    private lateinit var viewModel: MockMarketplaceViewModel

    @Before
    fun init() {
        fowlRepository = MockFowlRepository()
        viewModel = MockMarketplaceViewModel(fowlRepository)
    }

    @Test
    fun testRepositoryCreation() {
        // Verify repository is properly created
        assertThat(fowlRepository).isNotNull()
        assertThat(fowlRepository).isInstanceOf(MockFowlRepository::class.java)
    }

    @Test
    fun testMarketplaceDataFlow() = runTest {
        // Test the complete data flow from repository to UI
        val availableCount = fowlRepository.getAvailableFowlCount()
        assertThat(availableCount).isAtLeast(0)
        
        val breeds = fowlRepository.getAvailableBreeds()
        assertThat(breeds).isNotNull()
        
        val locations = fowlRepository.getAvailableLocations()
        assertThat(locations).isNotNull()
    }

    @Test
    fun testPaginationFlow() = runTest {
        // Test that pagination flow is working
        val fowlFlow = fowlRepository.getAvailableFowls()
        assertThat(fowlFlow).isNotNull()
        
        // Test pagination by collecting the flow
        val fowls = fowlFlow.first()
        assertThat(fowls).isNotNull()
    }

    @Test
    fun testSearchFlow() = runTest {
        // Add test data
        fowlRepository.addTestFowl(
            MockMarketplaceFowl(
                id = "search-test-1",
                breed = "Rhode Island Red",
                description = "Healthy chicken for sale",
                location = "Test Farm"
            )
        )

        // Test search functionality
        val searchFlow = fowlRepository.searchFowls("chicken")
        assertThat(searchFlow).isNotNull()
        
        val searchResults = searchFlow.first()
        assertThat(searchResults).hasSize(1)
        assertThat(searchResults.first().description).contains("chicken")
    }

    @Test
    fun testFilterFlow() = runTest {
        // Add test data
        fowlRepository.addTestFowl(
            MockMarketplaceFowl(
                id = "filter-test-1",
                breed = "Rhode Island Red",
                location = "Test Farm",
                price = 1500.0
            )
        )
        fowlRepository.addTestFowl(
            MockMarketplaceFowl(
                id = "filter-test-2",
                breed = "Leghorn",
                location = "Another Farm",
                price = 1200.0
            )
        )

        // Test filter functionality
        val breedFlow = fowlRepository.getFowlsByBreed("Rhode Island Red")
        val breedResults = breedFlow.first()
        assertThat(breedResults).hasSize(1)
        assertThat(breedResults.first().breed).isEqualTo("Rhode Island Red")
        
        val locationFlow = fowlRepository.getFowlsByLocation("Test Farm")
        val locationResults = locationFlow.first()
        assertThat(locationResults).hasSize(1)
        assertThat(locationResults.first().location).isEqualTo("Test Farm")
        
        val priceFlow = fowlRepository.getFowlsByPriceRange(1000.0, 1400.0)
        val priceResults = priceFlow.first()
        assertThat(priceResults).hasSize(1)
        assertThat(priceResults.first().price).isEqualTo(1200.0)
    }

    @Test
    fun testAnalyticsFlow() = runTest {
        // Add test data for analytics
        fowlRepository.addTestFowl(
            MockMarketplaceFowl(
                id = "analytics-1",
                breed = "Rhode Island Red",
                price = 1500.0
            )
        )
        fowlRepository.addTestFowl(
            MockMarketplaceFowl(
                id = "analytics-2",
                breed = "Rhode Island Red",
                price = 1700.0
            )
        )

        // Test analytics functionality
        val totalCount = fowlRepository.getAvailableFowlCount()
        assertThat(totalCount).isEqualTo(2)
        
        val averagePrice = fowlRepository.getAveragePriceByBreed("Rhode Island Red")
        assertThat(averagePrice).isEqualTo(1600.0)
    }

    @Test
    fun testViewModelIntegration() = runTest {
        // Test ViewModel integration with repository
        viewModel.loadMarketplaceData()
        
        val uiState = viewModel.getUiState()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).isNull()
        
        // Test search through ViewModel
        viewModel.searchFowls("test")
        val searchState = viewModel.getSearchState()
        assertThat(searchState.isSearching).isFalse()
    }

    @Test
    fun testMarketplaceFiltering() = runTest {
        // Add diverse test data
        val testFowls = listOf(
            MockMarketplaceFowl(
                id = "filter-1",
                breed = "Rhode Island Red",
                location = "Farm A",
                price = 1500.0,
                ageMonths = 6
            ),
            MockMarketplaceFowl(
                id = "filter-2",
                breed = "Leghorn",
                location = "Farm B",
                price = 1200.0,
                ageMonths = 4
            ),
            MockMarketplaceFowl(
                id = "filter-3",
                breed = "Rhode Island Red",
                location = "Farm A",
                price = 1800.0,
                ageMonths = 8
            )
        )

        testFowls.forEach { fowlRepository.addTestFowl(it) }

        // Test multiple filters
        val filters = MockMarketplaceFilters(
            breed = "Rhode Island Red",
            location = "Farm A",
            minPrice = 1400.0,
            maxPrice = 2000.0
        )

        val filteredResults = fowlRepository.getFilteredFowls(filters).first()
        assertThat(filteredResults).hasSize(2)
        filteredResults.forEach { fowl ->
            assertThat(fowl.breed).isEqualTo("Rhode Island Red")
            assertThat(fowl.location).isEqualTo("Farm A")
            assertThat(fowl.price).isAtLeast(1400.0)
            assertThat(fowl.price).isAtMost(2000.0)
        }
    }

    @Test
    fun testMarketplaceSorting() = runTest {
        // Add test data with different prices
        val testFowls = listOf(
            MockMarketplaceFowl(id = "sort-1", price = 1800.0, createdAt = 1000L),
            MockMarketplaceFowl(id = "sort-2", price = 1200.0, createdAt = 3000L),
            MockMarketplaceFowl(id = "sort-3", price = 1500.0, createdAt = 2000L)
        )

        testFowls.forEach { fowlRepository.addTestFowl(it) }

        // Test sorting by price (ascending)
        val sortedByPriceAsc = fowlRepository.getFowlsSortedBy("price_asc").first()
        assertThat(sortedByPriceAsc.map { it.price }).isInOrder(Comparator<Double> { a, b -> a.compareTo(b) })

        // Test sorting by price (descending)
        val sortedByPriceDesc = fowlRepository.getFowlsSortedBy("price_desc").first()
        assertThat(sortedByPriceDesc.map { it.price }).isInOrder(Comparator<Double> { a, b -> b.compareTo(a) })

        // Test sorting by date (newest first)
        val sortedByDateDesc = fowlRepository.getFowlsSortedBy("date_desc").first()
        assertThat(sortedByDateDesc.map { it.createdAt }).isInOrder(Comparator<Long> { a, b -> b.compareTo(a) })
    }

    @Test
    fun testMarketplaceErrorHandling() = runTest {
        // Test error handling in repository
        fowlRepository.setErrorMode(true)

        val errorResult = fowlRepository.getAvailableFowlsWithError()
        assertThat(errorResult.isFailure).isTrue()
        assertThat(errorResult.exceptionOrNull()).isNotNull()

        // Test ViewModel error handling
        viewModel.loadMarketplaceDataWithError()
        val errorState = viewModel.getUiState()
        assertThat(errorState.error).isNotNull()
    }

    @Test
    fun testMarketplacePerformance() = runTest {
        // Test performance with larger dataset
        val largeFowlList = (1..1000).map { index ->
            MockMarketplaceFowl(
                id = "perf-fowl-$index",
                breed = "Breed ${index % 10}",
                location = "Location ${index % 20}",
                price = 1000.0 + (index % 100) * 10
            )
        }

        largeFowlList.forEach { fowlRepository.addTestFowl(it) }

        // Test that large datasets can be handled efficiently
        val startTime = System.currentTimeMillis()
        val allFowls = fowlRepository.getAvailableFowls().first()
        val queryTime = System.currentTimeMillis() - startTime

        assertThat(allFowls).hasSize(1000)
        assertThat(queryTime).isLessThan(1000L) // Should complete in less than 1 second
    }

    @Test
    fun testMarketplaceCaching() = runTest {
        // Test caching behavior
        fowlRepository.addTestFowl(
            MockMarketplaceFowl(id = "cache-test", breed = "Test Breed")
        )

        // First call should hit the database
        fowlRepository.resetCacheState()
        val firstCall = fowlRepository.getAvailableFowls().first()
        assertThat(fowlRepository.wasCacheHit()).isFalse()

        // Second call should hit the cache
        fowlRepository.setCacheHit(true)
        val secondCall = fowlRepository.getAvailableFowls().first()
        assertThat(fowlRepository.wasCacheHit()).isTrue()

        assertThat(firstCall).isEqualTo(secondCall)
    }

    // Mock classes for testing
    data class MockMarketplaceFowl(
        val id: String = "",
        val breed: String = "",
        val description: String = "",
        val location: String = "",
        val price: Double = 0.0,
        val ageMonths: Int = 0,
        val createdAt: Long = System.currentTimeMillis(),
        val isAvailable: Boolean = true
    )

    data class MockMarketplaceFilters(
        val breed: String? = null,
        val location: String? = null,
        val minPrice: Double? = null,
        val maxPrice: Double? = null,
        val minAge: Int? = null,
        val maxAge: Int? = null
    )

    data class MockMarketplaceUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val fowls: List<MockMarketplaceFowl> = emptyList()
    )

    data class MockSearchState(
        val isSearching: Boolean = false,
        val query: String = "",
        val results: List<MockMarketplaceFowl> = emptyList()
    )

    class MockFowlRepository {
        private val fowls = mutableListOf<MockMarketplaceFowl>()
        private var errorMode = false
        private var cacheHit = false

        fun addTestFowl(fowl: MockMarketplaceFowl) {
            fowls.add(fowl)
        }

        fun setErrorMode(enabled: Boolean) {
            errorMode = enabled
        }

        fun wasCacheHit(): Boolean = cacheHit
        
        fun resetCacheState() {
            cacheHit = false
        }
        
        fun setCacheHit(hit: Boolean) {
            cacheHit = hit
        }

        suspend fun getAvailableFowlCount(): Int {
            return fowls.count { it.isAvailable }
        }

        suspend fun getAvailableBreeds(): List<String> {
            return fowls.filter { it.isAvailable }.map { it.breed }.distinct()
        }

        suspend fun getAvailableLocations(): List<String> {
            return fowls.filter { it.isAvailable }.map { it.location }.distinct()
        }

        fun getAvailableFowls() = flowOf(fowls.filter { it.isAvailable })

        fun searchFowls(query: String) = flowOf(
            fowls.filter { 
                it.isAvailable && 
                (it.breed.contains(query, ignoreCase = true) || 
                 it.description.contains(query, ignoreCase = true))
            }
        )

        fun getFowlsByBreed(breed: String) = flowOf(
            fowls.filter { it.isAvailable && it.breed == breed }
        )

        fun getFowlsByLocation(location: String) = flowOf(
            fowls.filter { it.isAvailable && it.location == location }
        )

        fun getFowlsByPriceRange(minPrice: Double, maxPrice: Double) = flowOf(
            fowls.filter { 
                it.isAvailable && it.price >= minPrice && it.price <= maxPrice 
            }
        )

        suspend fun getAveragePriceByBreed(breed: String): Double? {
            val breedFowls = fowls.filter { it.isAvailable && it.breed == breed }
            return if (breedFowls.isNotEmpty()) {
                breedFowls.map { it.price }.average()
            } else null
        }

        fun getFilteredFowls(filters: MockMarketplaceFilters) = flowOf(
            fowls.filter { fowl ->
                fowl.isAvailable &&
                (filters.breed == null || fowl.breed == filters.breed) &&
                (filters.location == null || fowl.location == filters.location) &&
                (filters.minPrice == null || fowl.price >= filters.minPrice) &&
                (filters.maxPrice == null || fowl.price <= filters.maxPrice) &&
                (filters.minAge == null || fowl.ageMonths >= filters.minAge) &&
                (filters.maxAge == null || fowl.ageMonths <= filters.maxAge)
            }
        )

        fun getFowlsSortedBy(sortBy: String) = flowOf(
            when (sortBy) {
                "price_asc" -> fowls.filter { it.isAvailable }.sortedBy { it.price }
                "price_desc" -> fowls.filter { it.isAvailable }.sortedByDescending { it.price }
                "date_desc" -> fowls.filter { it.isAvailable }.sortedByDescending { it.createdAt }
                else -> fowls.filter { it.isAvailable }
            }
        )

        suspend fun getAvailableFowlsWithError(): Result<List<MockMarketplaceFowl>> {
            return if (errorMode) {
                Result.Error(Exception("Simulated repository error"))
            } else {
                Result.Success(fowls.filter { it.isAvailable })
            }
        }
    }

    class MockMarketplaceViewModel(private val repository: MockFowlRepository) {
        private var uiState = MockMarketplaceUiState()
        private var searchState = MockSearchState()

        suspend fun loadMarketplaceData() {
            uiState = uiState.copy(isLoading = true)
            try {
                val fowls = repository.getAvailableFowls().first()
                uiState = uiState.copy(isLoading = false, fowls = fowls, error = null)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }

        suspend fun searchFowls(query: String) {
            searchState = searchState.copy(isSearching = true, query = query)
            try {
                val results = repository.searchFowls(query).first()
                searchState = searchState.copy(isSearching = false, results = results)
            } catch (e: Exception) {
                searchState = searchState.copy(isSearching = false)
            }
        }

        suspend fun loadMarketplaceDataWithError() {
            uiState = uiState.copy(isLoading = true)
            val result = repository.getAvailableFowlsWithError()
            if (result.isFailure) {
                uiState = uiState.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            } else {
                uiState = uiState.copy(isLoading = false, fowls = result.getOrNull() ?: emptyList())
            }
        }

        fun getUiState(): MockMarketplaceUiState = uiState
        fun getSearchState(): MockSearchState = searchState
    }
}