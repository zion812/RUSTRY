package com.rio.rustry.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserPreferences(
    val isDarkMode: Boolean = false,
    val language: String = "English",
    val notificationsEnabled: Boolean = true,
    val voiceCommandsEnabled: Boolean = false,
    val offlineModeEnabled: Boolean = true,
    val autoSyncEnabled: Boolean = true,
    val highQualityImages: Boolean = true,
    val dataUsageOptimization: Boolean = false
)

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
    private val firebaseAuth: FirebaseAuth
) {
    
    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val VOICE_COMMANDS_ENABLED_KEY = booleanPreferencesKey("voice_commands_enabled")
        private val OFFLINE_MODE_ENABLED_KEY = booleanPreferencesKey("offline_mode_enabled")
        private val AUTO_SYNC_ENABLED_KEY = booleanPreferencesKey("auto_sync_enabled")
        private val HIGH_QUALITY_IMAGES_KEY = booleanPreferencesKey("high_quality_images")
        private val DATA_USAGE_OPTIMIZATION_KEY = booleanPreferencesKey("data_usage_optimization")
    }
    
    fun getUserPreferences(): Flow<UserPreferences> {
        return dataStore.data.map { preferences ->
            UserPreferences(
                isDarkMode = preferences[DARK_MODE_KEY] ?: false,
                language = preferences[LANGUAGE_KEY] ?: "English",
                notificationsEnabled = preferences[NOTIFICATIONS_ENABLED_KEY] ?: true,
                voiceCommandsEnabled = preferences[VOICE_COMMANDS_ENABLED_KEY] ?: false,
                offlineModeEnabled = preferences[OFFLINE_MODE_ENABLED_KEY] ?: true,
                autoSyncEnabled = preferences[AUTO_SYNC_ENABLED_KEY] ?: true,
                highQualityImages = preferences[HIGH_QUALITY_IMAGES_KEY] ?: true,
                dataUsageOptimization = preferences[DATA_USAGE_OPTIMIZATION_KEY] ?: false
            )
        }
    }
    
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }
    
    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
    
    suspend fun setVoiceCommandsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[VOICE_COMMANDS_ENABLED_KEY] = enabled
        }
    }
    
    suspend fun setOfflineModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[OFFLINE_MODE_ENABLED_KEY] = enabled
        }
    }
    
    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_SYNC_ENABLED_KEY] = enabled
        }
    }
    
    suspend fun setHighQualityImages(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HIGH_QUALITY_IMAGES_KEY] = enabled
        }
    }
    
    suspend fun setDataUsageOptimization(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DATA_USAGE_OPTIMIZATION_KEY] = enabled
        }
    }
    
    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    suspend fun syncData() {
        // Implement data synchronization logic
        // This could involve syncing local data with Firebase
        // For now, this is a placeholder
    }
    
    suspend fun exportUserData() {
        // Implement data export logic
        // This could generate a JSON file with user's farm data
        // For now, this is a placeholder
    }
    
    suspend fun checkForUpdates() {
        // Implement update checking logic
        // This could check for app updates from Play Store or Firebase
        // For now, this is a placeholder
    }
    
    suspend fun signOut() {
        // Sign out from Firebase
        firebaseAuth.signOut()
        // Clear user preferences
        clearAllPreferences()
    }
}