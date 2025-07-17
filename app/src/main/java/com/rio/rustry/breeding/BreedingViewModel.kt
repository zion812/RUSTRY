// generated/phase3/app/src/main/java/com/rio/rustry/breeding/BreedingViewModel.kt

package com.rio.rustry.breeding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.domain.usecase.ExportTreeUseCase
import com.rio.rustry.domain.repository.BreedingRepository
import com.rio.rustry.domain.model.FamilyTree
import com.rio.rustry.domain.model.Result
import com.rio.rustry.domain.model.FowlRecord
import com.rio.rustry.domain.model.BreederStatus
import com.rio.rustry.domain.model.LifecycleEvent
import com.rio.rustry.domain.model.VaccinationEvent
import com.rio.rustry.domain.model.VaccinationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BreedingViewModel(
    private val breedingRepository: BreedingRepository,
    private val exportTreeUseCase: ExportTreeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BreedingUiState>(BreedingUiState.Loading)
    val uiState: StateFlow<BreedingUiState> = _uiState.asStateFlow()

    fun loadFamilyTree(fowlId: String) {
        viewModelScope.launch {
            _uiState.value = BreedingUiState.Loading
            val result = breedingRepository.generateFamilyTree(fowlId)
            _uiState.value = when (result) {
                is Result.Success -> BreedingUiState.FamilyTreeLoaded(result.data)
                is Result.Error -> BreedingUiState.Error(result.exception.message ?: "Error")
                else -> BreedingUiState.Error("Unexpected result")
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

    // Lifecycle tracking methods
    fun addRecord(fowlId: String, record: FowlRecord) {
        viewModelScope.launch {
            try {
                breedingRepository.addFowlRecord(fowlId, record)
                _uiState.value = BreedingUiState.RecordAdded("Record added successfully")
            } catch (e: Exception) {
                _uiState.value = BreedingUiState.Error(e.message ?: "Failed to add record")
            }
        }
    }

    fun markBreederStatus(fowlId: String, status: BreederStatus) {
        viewModelScope.launch {
            try {
                breedingRepository.updateBreederStatus(fowlId, status)
                _uiState.value = BreedingUiState.BreederStatusUpdated("Breeder status updated successfully")
            } catch (e: Exception) {
                _uiState.value = BreedingUiState.Error(e.message ?: "Failed to update breeder status")
            }
        }
    }

    fun recordMortality(fowlId: String, cause: String, date: Long, notes: String? = null) {
        viewModelScope.launch {
            try {
                val mortalityEvent = LifecycleEvent(
                    id = "",
                    fowlId = fowlId,
                    eventType = "MORTALITY",
                    date = date,
                    description = cause,
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                )
                breedingRepository.recordLifecycleEvent(fowlId, mortalityEvent)
                _uiState.value = BreedingUiState.MortalityRecorded("Mortality recorded successfully")
            } catch (e: Exception) {
                _uiState.value = BreedingUiState.Error(e.message ?: "Failed to record mortality")
            }
        }
    }

    fun updateFowlStatus(fowlId: String, status: String, notes: String? = null) {
        viewModelScope.launch {
            try {
                val statusEvent = LifecycleEvent(
                    id = "",
                    fowlId = fowlId,
                    eventType = "STATUS_UPDATE",
                    date = System.currentTimeMillis(),
                    description = status,
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                )
                breedingRepository.recordLifecycleEvent(fowlId, statusEvent)
                _uiState.value = BreedingUiState.StatusUpdated("Status updated successfully")
            } catch (e: Exception) {
                _uiState.value = BreedingUiState.Error(e.message ?: "Failed to update status")
            }
        }
    }

    fun loadLifecycleHistory(fowlId: String) {
        viewModelScope.launch {
            try {
                val events = breedingRepository.getLifecycleEvents(fowlId)
                _uiState.value = BreedingUiState.LifecycleHistoryLoaded(events)
            } catch (e: Exception) {
                _uiState.value = BreedingUiState.Error(e.message ?: "Failed to load lifecycle history")
            }
        }
    }
}

sealed class BreedingUiState {
    object Loading : BreedingUiState()
    data class FamilyTreeLoaded(val familyTree: FamilyTree) : BreedingUiState()
    data class VaccinationScheduleLoaded(val events: List<VaccinationEvent>) : BreedingUiState()
    data class RecordAdded(val message: String) : BreedingUiState()
    data class BreederStatusUpdated(val message: String) : BreedingUiState()
    data class MortalityRecorded(val message: String) : BreedingUiState()
    data class StatusUpdated(val message: String) : BreedingUiState()
    data class LifecycleHistoryLoaded(val events: List<LifecycleEvent>) : BreedingUiState()
    data class Error(val message: String) : BreedingUiState()
}