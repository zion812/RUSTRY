// generated/phase3/app/src/test/java/com/rio/rustry/breeding/BreedingViewModelTest.kt

package com.rio.rustry.breeding

import com.rio.rustry.domain.repository.BreedingRepository
import com.rio.rustry.domain.usecase.ExportTreeUseCase
import com.rio.rustry.test.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BreedingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val breedingRepository = mockk<BreedingRepository>()
    private val exportTreeUseCase = mockk<ExportTreeUseCase>(relaxed = true)
    private lateinit var viewModel: BreedingViewModel

    @Before
    fun setup() {
        viewModel = BreedingViewModel(breedingRepository, exportTreeUseCase)
    }

    @Test
    fun `loadFamilyTree should emit success state when repository succeeds`() = runTest {
        // Given
        val fowlId = "fowl123"
        val expectedTree = FamilyTree(
            nodes = listOf(
                TreeNode("1", "Rooster", "Rhode Island Red", "male", 0, 0, 1),
                TreeNode("2", "Hen", "Rhode Island Red", "female", 1, 0, 1)
            ),
            connections = listOf(TreeConnection("1", "2", "parent"))
        )
        coEvery { breedingRepository.getFamilyTree(fowlId) } returns expectedTree

        // When
        viewModel.loadFamilyTree(fowlId)

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is BreedingUiState.FamilyTreeLoaded)
        assertEquals(expectedTree, (uiState as BreedingUiState.FamilyTreeLoaded).familyTree)
    }

    @Test
    fun `loadVaccinationSchedule should emit success state with events`() = runTest {
        // Given
        val fowlId = "fowl123"
        val expectedEvents = listOf(
            VaccinationEvent(
                id = "vac1",
                fowlId = fowlId,
                vaccineName = "Newcastle Disease",
                scheduledDate = System.currentTimeMillis(),
                status = VaccinationStatus.PENDING,
                notes = "First vaccination"
            )
        )
        coEvery { breedingRepository.getVaccinationEvents(fowlId) } returns expectedEvents

        // When
        viewModel.loadVaccinationSchedule(fowlId)

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is BreedingUiState.VaccinationScheduleLoaded)
        assertEquals(expectedEvents, (uiState as BreedingUiState.VaccinationScheduleLoaded).events)
    }

    @Test
    fun `addVaccinationEvent should call repository and reload schedule`() = runTest {
        // Given
        val fowlId = "fowl123"
        val event = VaccinationEvent(
            id = "vac2",
            fowlId = fowlId,
            vaccineName = "Marek's Disease",
            scheduledDate = System.currentTimeMillis(),
            status = VaccinationStatus.PENDING
        )
        coEvery { breedingRepository.addVaccinationEvent(fowlId, event) } returns Unit
        coEvery { breedingRepository.getVaccinationEvents(fowlId) } returns listOf(event)

        // When
        viewModel.addVaccinationEvent(fowlId, event)

        // Then
        coVerify { breedingRepository.addVaccinationEvent(fowlId, event) }
        coVerify { breedingRepository.getVaccinationEvents(fowlId) }
    }

    @Test
    fun `markVaccinationComplete should update event status`() = runTest {
        // Given
        val eventId = "vac1"
        val fowlId = "fowl123"
        val initialEvent = VaccinationEvent(
            id = eventId,
            fowlId = fowlId,
            vaccineName = "Test Vaccine",
            scheduledDate = System.currentTimeMillis(),
            status = VaccinationStatus.PENDING
        )
        
        // Set initial state
        coEvery { breedingRepository.getVaccinationEvents(fowlId) } returns listOf(initialEvent)
        viewModel.loadVaccinationSchedule(fowlId)
        
        coEvery { breedingRepository.markVaccinationComplete(eventId) } returns Unit

        // When
        viewModel.markVaccinationComplete(eventId)

        // Then
        coVerify { breedingRepository.markVaccinationComplete(eventId) }
        
        val uiState = viewModel.uiState.value
        assertTrue(uiState is BreedingUiState.VaccinationScheduleLoaded)
        val updatedEvents = (uiState as BreedingUiState.VaccinationScheduleLoaded).events
        val updatedEvent = updatedEvents.find { it.id == eventId }
        assertEquals(VaccinationStatus.COMPLETED, updatedEvent?.status)
    }

    @Test
    fun `exportTreeAsPng should call export use case`() = runTest {
        // Given
        val familyTree = FamilyTree(emptyList(), emptyList())
        coEvery { breedingRepository.getFamilyTree(any()) } returns familyTree
        viewModel.loadFamilyTree("fowl123")

        // When
        viewModel.exportTreeAsPng()

        // Then
        coVerify { exportTreeUseCase.exportAsPng(familyTree) }
    }
}