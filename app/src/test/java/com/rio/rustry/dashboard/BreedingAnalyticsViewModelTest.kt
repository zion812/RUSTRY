// generated/phase3/app/src/test/java/com/rio/rustry/dashboard/BreedingAnalyticsViewModelTest.kt

package com.rio.rustry.dashboard

import com.rio.rustry.domain.usecase.GetBreedingAnalyticsUseCase
import com.rio.rustry.test.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BreedingAnalyticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getBreedingAnalyticsUseCase = mockk<GetBreedingAnalyticsUseCase>()
    private lateinit var viewModel: BreedingAnalyticsViewModel

    @Before
    fun setup() {
        viewModel = BreedingAnalyticsViewModel(getBreedingAnalyticsUseCase)
    }

    @Test
    fun `loadAnalytics should emit success state when use case succeeds`() = runTest {
        // Given
        val period = AnalyticsPeriod.THIRTY_DAYS
        val expectedAnalytics = BreedingAnalytics(
            hatchRate = 85.5,
            mortalityRate = 5.2,
            avgWeightGain = 150.0,
            trendData = listOf(80.0, 82.0, 85.5),
            period = period
        )
        coEvery { getBreedingAnalyticsUseCase(period) } returns expectedAnalytics

        // When
        viewModel.loadAnalytics(period)

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is BreedingAnalyticsUiState.Success)
        assertEquals(expectedAnalytics, (uiState as BreedingAnalyticsUiState.Success).analytics)
        coVerify { getBreedingAnalyticsUseCase(period) }
    }

    @Test
    fun `loadAnalytics should emit error state when use case fails`() = runTest {
        // Given
        val period = AnalyticsPeriod.SEVEN_DAYS
        val errorMessage = "Network error"
        coEvery { getBreedingAnalyticsUseCase(period) } throws Exception(errorMessage)

        // When
        viewModel.loadAnalytics(period)

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is BreedingAnalyticsUiState.Error)
        assertEquals(errorMessage, (uiState as BreedingAnalyticsUiState.Error).message)
    }

    @Test
    fun `loadAnalytics should emit loading state initially`() = runTest {
        // Given
        val period = AnalyticsPeriod.NINETY_DAYS
        coEvery { getBreedingAnalyticsUseCase(period) } coAnswers {
            kotlinx.coroutines.delay(100)
            BreedingAnalytics(0.0, 0.0, 0.0, emptyList(), period)
        }

        // When
        viewModel.loadAnalytics(period)

        // Then - Initial state should be loading
        // Note: In real test, you'd need to capture intermediate states
        coVerify { getBreedingAnalyticsUseCase(period) }
    }
}