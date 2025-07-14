package com.rio.rustry.presentation.localization

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.*

/**
 * Localization Manager for RUSTRY App
 * Supports English, Telugu, Tamil, Kannada, Hindi
 */
class LocalizationManager(private val context: Context) {
    
    companion object {
        const val ENGLISH = "en"
        const val TELUGU = "te"
        const val TAMIL = "ta"
        const val KANNADA = "kn"
        const val HINDI = "hi"
        
        private const val PREF_SELECTED_LANGUAGE = "selected_language"
    }
    
    private val sharedPreferences = context.getSharedPreferences("localization", Context.MODE_PRIVATE)
    
    fun setLanguage(languageCode: String) {
        sharedPreferences.edit()
            .putString(PREF_SELECTED_LANGUAGE, languageCode)
            .apply()
        
        updateLocale(languageCode)
    }
    
    fun getCurrentLanguage(): String {
        return sharedPreferences.getString(PREF_SELECTED_LANGUAGE, ENGLISH) ?: ENGLISH
    }
    
    fun getSupportedLanguages(): List<Language> {
        return listOf(
            Language(ENGLISH, "English", "🇺🇸"),
            Language(TELUGU, "తెలుగు", "🇮🇳"),
            Language(TAMIL, "தமிழ்", "🇮🇳"),
            Language(KANNADA, "ಕನ್ನಡ", "🇮🇳"),
            Language(HINDI, "हिंदी", "🇮🇳")
        )
    }
    
    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }
    
    fun getLocalizedString(key: String): String {
        val resourceId = context.resources.getIdentifier(key, "string", context.packageName)
        return if (resourceId != 0) {
            context.getString(resourceId)
        } else {
            key // Fallback to key if resource not found
        }
    }
}

data class Language(
    val code: String,
    val name: String,
    val flag: String
)

/**
 * Composable for providing localization context
 */
@Composable
fun LocalizationProvider(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    
    CompositionLocalProvider(
        LocalLocalizationManager provides localizationManager
    ) {
        content()
    }
}

val LocalLocalizationManager = compositionLocalOf<LocalizationManager> {
    error("LocalizationManager not provided")
}

/**
 * Hook for accessing localization in Composables
 */
@Composable
fun useLocalization(): LocalizationManager {
    return LocalLocalizationManager.current
}

/**
 * Composable for localized text
 */
@Composable
fun LocalizedText(
    key: String,
    fallback: String = key
): String {
    val localizationManager = useLocalization()
    return remember(key) {
        localizationManager.getLocalizedString(key).takeIf { it != key } ?: fallback
    }
}