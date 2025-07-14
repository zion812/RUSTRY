package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TutorialViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()
    
    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()
    
    private val _voiceEnabled = MutableStateFlow(false)
    val voiceEnabled: StateFlow<Boolean> = _voiceEnabled.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadVoiceSettings()
    }
    
    private fun loadVoiceSettings() {
        viewModelScope.launch {
            try {
                userPreferencesRepository.getUserPreferences().collect { preferences ->
                    _voiceEnabled.value = preferences.voiceCommandsEnabled
                }
            } catch (e: Exception) {
                _error.value = "Failed to load voice settings: ${e.message}"
            }
        }
    }
    
    fun nextStep() {
        _currentStep.value = _currentStep.value + 1
    }
    
    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value = _currentStep.value - 1
        }
    }
    
    fun goToStep(step: Int) {
        _currentStep.value = step
    }
    
    fun performStepAction(stepIndex: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (stepIndex) {
                    1 -> {
                        // Farm Listing action
                        // This could navigate to farm setup or show a demo
                        simulateAction("Farm listing demo completed")
                    }
                    2 -> {
                        // Flock Management action
                        simulateAction("Flock management demo completed")
                    }
                    3 -> {
                        // Health Records action
                        simulateAction("Health records demo completed")
                    }
                    4 -> {
                        // Sales Tracking action
                        simulateAction("Sales tracking demo completed")
                    }
                    5 -> {
                        // Inventory Management action
                        simulateAction("Inventory management demo completed")
                    }
                    6 -> {
                        // Voice Commands action
                        toggleVoice()
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to perform action: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun toggleVoice() {
        viewModelScope.launch {
            try {
                val newValue = !_voiceEnabled.value
                userPreferencesRepository.setVoiceCommandsEnabled(newValue)
                _voiceEnabled.value = newValue
            } catch (e: Exception) {
                _error.value = "Failed to toggle voice commands: ${e.message}"
            }
        }
    }
    
    fun completeTutorial() {
        viewModelScope.launch {
            try {
                // Mark tutorial as completed in preferences
                // This could be stored in DataStore or Firebase
                _isCompleted.value = true
            } catch (e: Exception) {
                _error.value = "Failed to complete tutorial: ${e.message}"
            }
        }
    }
    
    fun skipTutorial() {
        _isCompleted.value = true
    }
    
    private suspend fun simulateAction(message: String) {
        // Simulate some action being performed
        kotlinx.coroutines.delay(1000)
        // In a real implementation, this would perform actual actions
        // like navigating to screens or showing demos
    }
    
    fun clearError() {
        _error.value = null
    }
}