package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rustry.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()
    
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private val _voiceCommandsEnabled = MutableStateFlow(false)
    val voiceCommandsEnabled: StateFlow<Boolean> = _voiceCommandsEnabled.asStateFlow()
    
    private val _offlineModeEnabled = MutableStateFlow(true)
    val offlineModeEnabled: StateFlow<Boolean> = _offlineModeEnabled.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                userPreferencesRepository.getUserPreferences().collect { preferences ->
                    _isDarkMode.value = preferences.isDarkMode
                    _selectedLanguage.value = preferences.language
                    _notificationsEnabled.value = preferences.notificationsEnabled
                    _voiceCommandsEnabled.value = preferences.voiceCommandsEnabled
                    _offlineModeEnabled.value = preferences.offlineModeEnabled
                }
            } catch (e: Exception) {
                _error.value = "Failed to load settings: ${e.message}"
            }
        }
    }
    
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.setDarkMode(enabled)
                _isDarkMode.value = enabled
            } catch (e: Exception) {
                _error.value = "Failed to update dark mode setting: ${e.message}"
            }
        }
    }
    
    fun setLanguage(language: String) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.setLanguage(language)
                _selectedLanguage.value = language
            } catch (e: Exception) {
                _error.value = "Failed to update language setting: ${e.message}"
            }
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.setNotificationsEnabled(enabled)
                _notificationsEnabled.value = enabled
            } catch (e: Exception) {
                _error.value = "Failed to update notification setting: ${e.message}"
            }
        }
    }
    
    fun setVoiceCommandsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.setVoiceCommandsEnabled(enabled)
                _voiceCommandsEnabled.value = enabled
            } catch (e: Exception) {
                _error.value = "Failed to update voice commands setting: ${e.message}"
            }
        }
    }
    
    fun setOfflineModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.setOfflineModeEnabled(enabled)
                _offlineModeEnabled.value = enabled
            } catch (e: Exception) {
                _error.value = "Failed to update offline mode setting: ${e.message}"
            }
        }
    }
    
    fun syncData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Implement data sync logic
                userPreferencesRepository.syncData()
            } catch (e: Exception) {
                _error.value = "Failed to sync data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun exportData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Implement data export logic
                userPreferencesRepository.exportUserData()
            } catch (e: Exception) {
                _error.value = "Failed to export data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun checkForUpdates() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Implement update check logic
                userPreferencesRepository.checkForUpdates()
            } catch (e: Exception) {
                _error.value = "Failed to check for updates: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                userPreferencesRepository.signOut()
            } catch (e: Exception) {
                _error.value = "Failed to sign out: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}