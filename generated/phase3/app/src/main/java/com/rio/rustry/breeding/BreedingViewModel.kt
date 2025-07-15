// generated/phase3/app/src/main/java/com/rio/rustry/breeding/BreedingViewModel.kt

package com.rio.rustry.breeding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.domain.usecase.ExportTreeUseCase
import com.rio.rustry.domain.repository.BreedingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BreedingViewModel @Inject constructor(
    private val breedingRepository: BreedingRepository,
    private val exportTreeUseCase: ExportTreeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BreedingUiState>(BreedingUiState.Loading)
    val uiState: StateFlow<BreedingUiState> = _uiState.asStateFlow()

    fun loadFamilyTree(fowlId: String) {
        viewModelScope.launch {
            _uiState.value = BreedingUiState.Loading
            try {
                val familyTree = breedingRepository.getFamilyTree(fowlId)
                _uiState.value = BreedingUiState.FamilyTreeLoaded(familyTree)
            } catch (e: Exception) {
                _uiState.value = BreedingUiState.Error(e.message ?: "Failed to load family tree")
            }
        }
    }

    fun loadVaccinationSchedule(fowlId: String) {
        viewModelScope.launch {
            _uiState.value = BreedingUiState.Loading
            try {
                val events = breedingRepository.getVaccinationEvents(fowlId)
                _uiState.value = BreedingUiState.VaccinationScheduleLoaded(events)
            } catch (e: Exception) {
                _uiState.value = BreedingUiState.Error(e.message ?: "Failed to load vaccination schedule")
            }
        }
    }

    fun addVaccinationEvent(fowlId: String, event: VaccinationEvent) {
        viewModelScope.launch {
            try {
                breedingRepository.addVaccinationEvent(fowlId, event)
                loadVaccinationSchedule(fowlId) // Reload
            } catch (e: Exception) {
                _uiState.value = BreedingUiState.Error(e.message ?: "Failed to add vaccination event")
            }
        }
    }

    fun updateVaccinationEvent(event: VaccinationEvent) {
        viewModelScope.launch {
            try {
                breedingRepository.updateVaccinationEvent(event)
                loadVaccinationSchedule(event.fowlId) // Reload
            } catch (e: Exception) {
                _uiState.value = BreedingUiState.Error(e.message ?: "Failed to update vaccination event")
            }
        }
    }

    fun deleteVaccinationEvent(eventId: String) {
        viewModelScope.launch {
            try {
                breedingRepository.deleteVaccinationEvent(eventId)
                // Reload current state
                val currentState = _uiState.value
                if (currentState is BreedingUiState.VaccinationScheduleLoaded) {
                    val updatedEvents = currentState.events.filter { it.id != eventId }
                    _uiState.value = BreedingUiState.VaccinationScheduleLoaded(updatedEvents)
                }
            } catch (e: Exception) {
                _uiState.value = BreedingUiState.Error(e.message ?: "Failed to delete vaccination event")
            }
        }
    }

    fun markVaccinationComplete(eventId: String) {
        viewModelScope.launch {
            try {
                breedingRepository.markVaccinationComplete(eventId)
                // Reload current state
                val currentState = _uiState.value
                if (currentState is BreedingUiState.VaccinationScheduleLoaded) {
                    val updatedEvents = currentState.events.map { event ->
                        if (event.id == eventId) {
                            event.copy(
                                status = VaccinationStatus.COMPLETED,
                                completedDate = System.currentTimeMillis()
                            )
                        } else event
                    }
                    _uiState.value = BreedingUiState.VaccinationScheduleLoaded(updatedEvents)
                }
            } catch (e: Exception) {
                _uiState.value = BreedingUiState.Error(e.message ?: "Failed to mark vaccination complete")
            }
        }
    }

    fun exportTreeAsPng() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is BreedingUiState.FamilyTreeLoaded) {
                    exportTreeUseCase.exportAsPng(currentState.familyTree)
                }
            } catch (e: Exception) {
                _uiState.value = BreedingUiState.Error(e.message ?: "Failed to export tree as PNG")
            }
        }
    }

    fun exportTreeAsPdf() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is BreedingUiState.FamilyTreeLoaded) {
                    exportTreeUseCase.exportAsPdf(currentState.familyTree)
                }
            } catch (e: Exception) {
                _uiState.value = BreedingUiState.Error(e.message ?: "Failed to export tree as PDF")
            }
        }
    }
}

sealed class BreedingUiState {
    object Loading : BreedingUiState()
    data class FamilyTreeLoaded(val familyTree: FamilyTree) : BreedingUiState()
    data class VaccinationScheduleLoaded(val events: List<VaccinationEvent>) : BreedingUiState()
    data class Error(val message: String) : BreedingUiState()
}