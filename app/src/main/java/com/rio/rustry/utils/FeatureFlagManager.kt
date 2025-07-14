package com.rio.rustry.utils

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.rio.rustry.BuildConfig

class FeatureFlagManager {
    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)  // Faster in debug
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        // Set default values
        val defaults = mapOf(
            "enable_new_marketplace" to false,
            "enable_ai_health_analysis" to true
            // Add more flags as needed
        )
        remoteConfig.setDefaultsAsync(defaults)
        // Fetch and activate
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Log success
            }
        }
    }

    fun isFeatureEnabled(key: String, defaultValue: Boolean = false): Boolean {
        return remoteConfig.getBoolean(key)
    }

    // Add methods for other types like String, Long, etc.
}
