package com.rio.rustry.utils

import android.util.Log
import com.rio.rustry.BuildConfig

/**
 * Optimized logging utility for the Rooster Platform
 * 
 * Features:
 * - Conditional logging based on build type
 * - Performance optimized with inline functions
 * - Structured logging with consistent formatting
 * - Crash reporting integration
 * - Memory efficient string operations
 */
object Logger {
    
    private const val DEFAULT_TAG = "RoosterPlatform"
    
    /**
     * Debug logging - only enabled in debug builds
     */
    @JvmStatic
    fun d(tag: String = DEFAULT_TAG, message: () -> String) {
        if (BuildConfig.ENABLE_LOGGING) {
            Log.d(tag, message())
        }
    }
    
    /**
     * Info logging - enabled in debug builds
     */
    @JvmStatic
    fun i(tag: String = DEFAULT_TAG, message: () -> String) {
        if (BuildConfig.ENABLE_LOGGING) {
            Log.i(tag, message())
        }
    }
    
    /**
     * Warning logging - always enabled
     */
    @JvmStatic
    fun w(tag: String = DEFAULT_TAG, message: () -> String, throwable: Throwable? = null) {
        Log.w(tag, message(), throwable)
    }
    
    /**
     * Error logging - always enabled with crash reporting
     */
    @JvmStatic
    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        
        // Send to crash reporting in production
        if (!BuildConfig.DEBUG && throwable != null) {
            // Firebase Crashlytics integration
            try {
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .recordException(throwable)
            } catch (e: Exception) {
                // Fail silently if crashlytics is not available
            }
        }
    }
    
    /**
     * Error logging with lambda - for conditional message creation
     */
    @JvmStatic
    fun e(tag: String = DEFAULT_TAG, message: () -> String, throwable: Throwable? = null) {
        val msg = message()
        e(tag, msg, throwable)
    }
    
    /**
     * Performance logging for measuring execution time
     */
    @JvmStatic
    fun <T> measureTime(
        tag: String = DEFAULT_TAG,
        operation: String,
        block: () -> T
    ): T {
        return if (BuildConfig.ENABLE_LOGGING) {
            val startTime = System.currentTimeMillis()
            val result = block()
            val endTime = System.currentTimeMillis()
            Log.d(tag, "$operation took ${endTime - startTime}ms")
            result
        } else {
            block()
        }
    }
    
    /**
     * Network logging for API calls
     */
    @JvmStatic
    fun logNetworkCall(
        tag: String = "Network",
        method: String,
        url: String,
        responseCode: Int? = null,
        duration: Long? = null
    ) {
        if (BuildConfig.ENABLE_LOGGING) {
            val message = buildString {
                append("$method $url")
                responseCode?.let { append(" -> $it") }
                duration?.let { append(" (${it}ms)") }
            }
            Log.d(tag, message)
        }
    }
    
    /**
     * User action logging for analytics
     */
    @JvmStatic
    fun logUserAction(
        action: String,
        screen: String,
        additionalData: Map<String, Any>? = null
    ) {
        if (BuildConfig.ENABLE_ANALYTICS) {
            val message = buildString {
                append("User Action: $action on $screen")
                additionalData?.let { data ->
                    append(" | Data: ${data.entries.joinToString { "${it.key}=${it.value}" }}")
                }
            }
            
            if (BuildConfig.ENABLE_LOGGING) {
                Log.i("UserAction", message)
            }
            
            // Send to analytics in production
            // Note: This will be handled by the application class
            // FirebaseAnalytics integration would be done there
        }
    }
    
    /**
     * Database operation logging
     */
    @JvmStatic
    inline fun logDatabaseOperation(
        operation: String,
        table: String,
        duration: Long? = null
    ) {
        if (BuildConfig.ENABLE_LOGGING) {
            val message = buildString {
                append("DB: $operation on $table")
                duration?.let { append(" (${it}ms)") }
            }
            Log.d("Database", message)
        }
    }
    
    /**
     * Memory usage logging
     */
    @JvmStatic
    fun logMemoryUsage(tag: String = "Memory") {
        if (BuildConfig.ENABLE_LOGGING) {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            val usedMB = usedMemory / (1024 * 1024)
            val maxMB = maxMemory / (1024 * 1024)
            val percentage = (usedMemory * 100 / maxMemory)
            
            Log.d(tag, "Memory usage: ${usedMB}MB / ${maxMB}MB (${percentage}%)")
        }
    }
    
    /**
     * Compose recomposition logging
     */
    @JvmStatic
    fun logRecomposition(composableName: String, reason: String? = null) {
        if (BuildConfig.ENABLE_LOGGING) {
            val message = buildString {
                append("Recomposition: $composableName")
                reason?.let { append(" - $it") }
            }
            Log.d("Compose", message)
        }
    }
    
    /**
     * Firebase operation logging
     */
    @JvmStatic
    fun logFirebaseOperation(
        operation: String,
        collection: String? = null,
        documentId: String? = null,
        success: Boolean,
        duration: Long? = null,
        error: Throwable? = null
    ) {
        val tag = "Firebase"
        val message = buildString {
            append("$operation")
            collection?.let { append(" in $it") }
            documentId?.let { append("/$it") }
            append(" - ${if (success) "SUCCESS" else "FAILED"}")
            duration?.let { append(" (${it}ms)") }
        }
        
        if (success) {
            if (BuildConfig.ENABLE_LOGGING) {
                Log.d(tag, message)
            }
        } else {
            Log.e(tag, message, error)
        }
    }
}

/**
 * Extension functions for easier logging
 */
inline fun <T> T.logDebug(tag: String = "RoosterPlatform", crossinline message: (T) -> String): T {
    Logger.d(tag) { message(this) }
    return this
}

inline fun <T> T.logInfo(tag: String = "RoosterPlatform", crossinline message: (T) -> String): T {
    Logger.i(tag) { message(this) }
    return this
}

inline fun <T> T.logError(tag: String = "RoosterPlatform", crossinline message: (T) -> String, throwable: Throwable? = null): T {
    Logger.e(tag, { message(this) }, throwable)
    return this
}