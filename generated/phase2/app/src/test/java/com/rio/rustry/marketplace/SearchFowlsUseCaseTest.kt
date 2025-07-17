// generated/phase2/app/src/test/java/com/rio/rustry/marketplace/SearchFowlsUseCaseTest.kt

package com.rio.rustry.marketplace

import com.google.common.truth.Truth.assertThat
import com.rio.rustry.data.model.AgeGroup
import com.rio.rustry.data.model.Breed
import com.rio.rustry.data.model.Fowl
import com.rio.rustry.data.model.Lineage
import com.rio.rustry.domain.usecase.SearchFowlsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SearchFowlsUseCaseTest {

    private lateinit var marketplaceRepository: MarketplaceRepository
    private lateinit var searchFowlsUseCase: SearchFowlsUseCase

    @Before
    fun setup() {
        marketplaceRepository = mockk()
        searchFowlsUseCase = SearchFowlsUseCase(marketplaceRepository)
    }

    @Test
    fun `search fowls returns success with results`() = runTest {
        // Given
        val searchParams = SearchParams(
            query = "chicken",
            filters = SearchFilters(),
            sortOption = SortOption.NEWEST
        )
        val expectedFowls = listOf(
            createTestFowl("1", Breed.CHICKEN, AgeGroup.ADULT, 25.0),
            createTestFowl("2", Breed.CHICKEN, AgeGroup.JUVENILE, 15.0)
        )
        
        coEvery { marketplaceRepository.searchFowls(searchParams) } returns 
            flowOf(Result.Success(expectedFowls))

        // When
        val results = searchFowlsUseCase(searchParams).toList()

        // Then
        assertThat(results).hasSize(1)
        val result = results.first()
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedFowls)
    }

    @Test
    fun `search fowls returns failure when repository fails`() = runTest {
        // Given
        val searchParams = SearchParams(
            query = "chicken",
            filters = SearchFilters(),
            sortOption = SortOption.NEWEST
        )
        val exception = Exception("Network error")
        
        coEvery { marketplaceRepository.searchFowls(searchParams) } returns 
            flowOf(Result.Error(exception))

        // When
        val results = searchFowlsUseCase(searchParams).toList()

        // Then
        assertThat(results).hasSize(1)
        val result = results.first()
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `search fowls with filters returns filtered results`() = runTest {
        // Given
        val filters = SearchFilters(
            breeds = setOf(Breed.CHICKEN),
            ageGroups = setOf(AgeGroup.ADULT),
            priceRange = 20f..30f
        )
        val searchParams = SearchParams(
            query = "",
            filters = filters,
            sortOption = SortOption.PRICE_LOW_HIGH
        )
        val expectedFowls = listOf(
            createTestFowl("1", Breed.CHICKEN, AgeGroup.ADULT, 25.0)
        )
        
        coEvery { marketplaceRepository.searchFowls(searchParams) } returns 
            flowOf(Result.Success(expectedFowls))

        // When
        val results = searchFowlsUseCase(searchParams).toList()

        // Then
        assertThat(results).hasSize(1)
        val result = results.first()
        assertThat(result.isSuccess).isTrue()
        val fowls = result.getOrNull()!!
        assertThat(fowls).hasSize(1)
        assertThat(fowls.first().breed).isEqualTo(Breed.CHICKEN)
        assertThat(fowls.first().ageGroup).isEqualTo(AgeGroup.ADULT)
        assertThat(fowls.first().price).isEqualTo(25.0)
    }

    @Test
    fun `search fowls with empty query returns all results`() = runTest {
        // Given
        val searchParams = SearchParams(
            query = "",
            filters = SearchFilters(),
            sortOption = SortOption.NEWEST
        )
        val expectedFowls = listOf(
            createTestFowl("1", Breed.CHICKEN, AgeGroup.ADULT, 25.0),
            createTestFowl("2", Breed.DUCK, AgeGroup.JUVENILE, 15.0),
            createTestFowl("3", Breed.TURKEY, AgeGroup.YOUNG_ADULT, 35.0)
        )
        
        coEvery { marketplaceRepository.searchFowls(searchParams) } returns 
            flowOf(Result.Success(expectedFowls))

        // When
        val results = searchFowlsUseCase(searchParams).toList()

        // Then
        assertThat(results).hasSize(1)
        val result = results.first()
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).hasSize(3)
    }

    @Test
    fun `search fowls with traceable filter returns only traceable fowls`() = runTest {
        // Given
        val filters = SearchFilters(traceableOnly = true)
        val searchParams = SearchParams(
            query = "",
            filters = filters,
            sortOption = SortOption.NEWEST
        )
        val expectedFowls = listOf(
            createTestFowl("1", Breed.CHICKEN, AgeGroup.ADULT, 25.0, isTraceable = true)
        )
        
        coEvery { marketplaceRepository.searchFowls(searchParams) } returns 
            flowOf(Result.Success(expectedFowls))

        // When
        val results = searchFowlsUseCase(searchParams).toList()

        // Then
        assertThat(results).hasSize(1)
        val result = results.first()
        assertThat(result.isSuccess).isTrue()
        val fowls = result.getOrNull()!!
        assertThat(fowls).hasSize(1)
        assertThat(fowls.first().isTraceable).isTrue()
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