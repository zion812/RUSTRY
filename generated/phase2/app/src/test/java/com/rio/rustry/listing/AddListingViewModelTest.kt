// generated/phase2/app/src/test/java/com/rio/rustry/listing/AddListingViewModelTest.kt

package com.rio.rustry.listing

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.rio.rustry.data.model.AgeGroup
import com.rio.rustry.data.model.Breed
import com.rio.rustry.domain.usecase.AddFowlListingUseCase
import com.rio.rustry.domain.usecase.GetFowlDetailUseCase
import com.rio.rustry.domain.usecase.UpdateFowlListingUseCase
import com.rio.rustry.marketplace.FowlDetail
import com.rio.rustry.test.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddListingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var addFowlListingUseCase: AddFowlListingUseCase
    private lateinit var updateFowlListingUseCase: UpdateFowlListingUseCase
    private lateinit var getFowlDetailUseCase: GetFowlDetailUseCase
    private lateinit var viewModel: AddListingViewModel

    @Before
    fun setup() {
        addFowlListingUseCase = mockk()
        updateFowlListingUseCase = mockk()
        getFowlDetailUseCase = mockk()
        viewModel = AddListingViewModel(
            addFowlListingUseCase,
            updateFowlListingUseCase,
            getFowlDetailUseCase
        )
    }

    @Test
    fun `initial state is correct`() {
        // Given & When
        val uiState = viewModel.uiState.value

        // Then
        assertThat(uiState.fowlId).isNull()
        assertThat(uiState.breed).isNull()
        assertThat(uiState.ageGroup).isNull()
        assertThat(uiState.price).isEmpty()
        assertThat(uiState.images).isEmpty()
        assertThat(uiState.isTraceable).isFalse()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isSuccess).isFalse()
    }

    @Test
    fun `updateBreed updates breed and clears error`() {
        // Given
        val breed = Breed.CHICKEN

        // When
        viewModel.updateBreed(breed)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.breed).isEqualTo(breed)
        assertThat(uiState.breedError).isNull()
    }

    @Test
    fun `updateAgeGroup updates age group and clears error`() {
        // Given
        val ageGroup = AgeGroup.ADULT

        // When
        viewModel.updateAgeGroup(ageGroup)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.ageGroup).isEqualTo(ageGroup)
        assertThat(uiState.ageGroupError).isNull()
    }

    @Test
    fun `updatePrice updates price and clears error`() {
        // Given
        val price = "25.50"

        // When
        viewModel.updatePrice(price)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.price).isEqualTo(price)
        assertThat(uiState.priceError).isNull()
    }

    @Test
    fun `updateTraceability updates traceability flag`() {
        // Given
        val isTraceable = true

        // When
        viewModel.updateTraceability(isTraceable)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isTraceable).isEqualTo(isTraceable)
    }

    @Test
    fun `addImages adds images and clears error`() {
        // Given
        val uri1 = mockk<Uri>()
        val uri2 = mockk<Uri>()
        val uris = listOf(uri1, uri2)

        // When
        viewModel.addImages(uris)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.images).containsExactly(uri1, uri2)
        assertThat(uiState.imagesError).isNull()
    }

    @Test
    fun `removeImage removes specific image`() {
        // Given
        val uri1 = mockk<Uri>()
        val uri2 = mockk<Uri>()
        viewModel.addImages(listOf(uri1, uri2))

        // When
        viewModel.removeImage(uri1)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.images).containsExactly(uri2)
    }

    @Test
    fun `saveListing fails validation when required fields are missing`() {
        // Given - empty form

        // When
        viewModel.saveListing()

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.breedError).isNotNull()
        assertThat(uiState.ageGroupError).isNotNull()
        assertThat(uiState.priceError).isNotNull()
        assertThat(uiState.imagesError).isNotNull()
        assertThat(uiState.isLoading).isFalse()
    }

    @Test
    fun `saveListing fails validation with invalid price`() {
        // Given
        viewModel.updateBreed(Breed.CHICKEN)
        viewModel.updateAgeGroup(AgeGroup.ADULT)
        viewModel.updatePrice("invalid")
        viewModel.addImages(listOf(mockk()))

        // When
        viewModel.saveListing()

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.priceError).isNotNull()
        assertThat(uiState.priceError).contains("valid price")
    }

    @Test
    fun `saveListing fails validation with zero price`() {
        // Given
        viewModel.updateBreed(Breed.CHICKEN)
        viewModel.updateAgeGroup(AgeGroup.ADULT)
        viewModel.updatePrice("0")
        viewModel.addImages(listOf(mockk()))

        // When
        viewModel.saveListing()

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.priceError).isNotNull()
        assertThat(uiState.priceError).contains("greater than 0")
    }

    @Test
    fun `saveListing succeeds with valid data`() = runTest {
        // Given
        viewModel.updateBreed(Breed.CHICKEN)
        viewModel.updateAgeGroup(AgeGroup.ADULT)
        viewModel.updatePrice("25.50")
        viewModel.addImages(listOf(mockk()))
        
        coEvery { addFowlListingUseCase(any()) } returns Result.Success("fowl123")

        // When
        viewModel.saveListing()

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isSuccess).isTrue()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).isNull()
    }

    @Test
    fun `saveListing handles failure from use case`() = runTest {
        // Given
        viewModel.updateBreed(Breed.CHICKEN)
        viewModel.updateAgeGroup(AgeGroup.ADULT)
        viewModel.updatePrice("25.50")
        viewModel.addImages(listOf(mockk()))
        
        val errorMessage = "Network error"
        coEvery { addFowlListingUseCase(any()) } returns Result.Error(Exception(errorMessage))

        // When
        viewModel.saveListing()

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isSuccess).isFalse()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).isEqualTo(errorMessage)
    }

    @Test
    fun `loadFowl loads existing fowl data for editing`() = runTest {
        // Given
        val fowlId = "fowl123"
        val fowlDetail = createTestFowlDetail()
        
        coEvery { getFowlDetailUseCase(fowlId) } returns flowOf(Result.Success(fowlDetail))

        // When
        viewModel.loadFowl(fowlId)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.fowlId).isEqualTo(fowlId)
        assertThat(uiState.breed).isEqualTo(fowlDetail.fowl.breed)
        assertThat(uiState.ageGroup).isEqualTo(fowlDetail.fowl.ageGroup)
        assertThat(uiState.price).isEqualTo(fowlDetail.fowl.price.toString())
        assertThat(uiState.isTraceable).isEqualTo(fowlDetail.fowl.isTraceable)
        assertThat(uiState.existingImages).isEqualTo(fowlDetail.fowl.images)
        assertThat(uiState.isLoading).isFalse()
    }

    private fun createTestFowlDetail() = FowlDetail(
        fowl = com.rio.rustry.data.model.Fowl(
            id = "fowl123",
            breed = Breed.CHICKEN,
            ageGroup = AgeGroup.ADULT,
            price = 25.0,
            images = listOf("image1.jpg", "image2.jpg"),
            isTraceable = true,
            lineage = com.rio.rustry.data.model.Lineage(parentIds = emptyList()),
            ownerUid = "user123",
            status = com.rio.rustry.data.model.FowlStatus.PUBLISHED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        lineage = emptyList()
    )
}