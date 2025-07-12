package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.model.*
import com.rio.rustry.data.repository.HealthRepository
import com.rio.rustry.data.repository.FowlRepository
import com.rio.rustry.data.service.AIHealthService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HealthDashboardViewModel(
    private val healthRepository: HealthRepository = HealthRepository(),
    private val fowlRepository: FowlRepository = FowlRepository(),
    private val aiHealthService: AIHealthService = AIHealthService()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HealthDashboardUiState())
    val uiState: StateFlow<HealthDashboardUiState> = _uiState.asStateFlow()
    
    fun loadHealthDashboard(fowlId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load fowl information
                val fowl = fowlRepository.getFowlById(fowlId).getOrNull()
                if (fowl == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Fowl not found",
                        isLoading = false
                    )
                    return@launch
                }
                
                // Load health data
                combine(
                    healthRepository.getHealthRecords(fowlId),
                    healthRepository.getHealthReminders(fowlId),
                    healthRepository.getHealthAlerts(fowlId),
                    healthRepository.getOverdueReminders(fowlId)
                ) { records, reminders, alerts, overdueReminders ->
                    
                    // Get health summary
                    val healthSummary = healthRepository.getHealthSummary(fowlId).getOrNull()
                        ?: HealthSummary(fowlId = fowlId)
                    
                    // Get AI recommendations
                    val aiTips = aiHealthService.getHealthTipsForFowl(fowl)
                    val vaccinationReminders = aiHealthService.getVaccinationReminders(fowl, records)
                    val healthTrends = aiHealthService.analyzeHealthTrends(records)
                    
                    val allAITips = (aiTips + vaccinationReminders + healthTrends)
                        .distinctBy { it.title }
                        .sortedByDescending { it.priority.ordinal }
                    
                    // Group records by type for quick stats
                    val recordsByType = records.groupBy { it.type }
                    
                    _uiState.value = _uiState.value.copy(
                        fowl = fowl,
                        healthSummary = healthSummary,
                        recentRecords = records.take(5),
                        upcomingReminders = reminders.take(5),
                        healthAlerts = alerts,
                        overdueReminders = overdueReminders,
                        aiRecommendations = allAITips.take(10),
                        vaccinationRecords = recordsByType[HealthEventType.VACCINATION] ?: emptyList(),
                        medicationRecords = recordsByType[HealthEventType.MEDICATION] ?: emptyList(),
                        checkupRecords = recordsByType[HealthEventType.CHECKUP] ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }.collect { }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load health dashboard",
                    isLoading = false
                )
            }
        }
    }
    
    fun markAlertAsRead(alertId: String) {
        viewModelScope.launch {
            healthRepository.markAlertAsRead(alertId)
        }
    }
    
    fun completeReminder(reminderId: String) {
        viewModelScope.launch {
            val result = healthRepository.completeHealthReminder(reminderId)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to complete reminder"
                )
            }
        }
    }
    
    fun refreshDashboard() {
        val fowlId = _uiState.value.fowl?.id
        if (fowlId != null) {
            loadHealthDashboard(fowlId)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HealthDashboardUiState(
    val fowl: Fowl? = null,
    val healthSummary: HealthSummary = HealthSummary(),
    val recentRecords: List<HealthRecord> = emptyList(),
    val upcomingReminders: List<HealthReminder> = emptyList(),
    val healthAlerts: List<HealthAlert> = emptyList(),
    val overdueReminders: List<HealthReminder> = emptyList(),
    val aiRecommendations: List<AIHealthTip> = emptyList(),
    val vaccinationRecords: List<HealthRecord> = emptyList(),
    val medicationRecords: List<HealthRecord> = emptyList(),
    val checkupRecords: List<HealthRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)