package com.rio.rustry.domain.model

/**
 * Application-specific error types for better error handling
 */
sealed class AppError : Throwable() {
    
    /**
     * Network-related errors
     */
    data class NetworkError(
        override val message: String,
        val code: Int? = null,
        override val cause: Throwable? = null
    ) : AppError()
    
    /**
     * Database-related errors
     */
    data class DatabaseError(
        override val message: String,
        val operation: String? = null,
        override val cause: Throwable? = null
    ) : AppError()
    
    /**
     * Authentication and authorization errors
     */
    data class AuthError(
        override val message: String,
        val errorCode: String? = null,
        override val cause: Throwable? = null
    ) : AppError()
    
    /**
     * Validation errors
     */
    data class ValidationError(
        override val message: String,
        val field: String? = null,
        val validationRule: String? = null
    ) : AppError()
    
    /**
     * Business logic errors
     */
    data class BusinessError(
        override val message: String,
        val errorCode: String? = null,
        val context: Map<String, Any>? = null
    ) : AppError()
    
    /**
     * File and storage errors
     */
    data class StorageError(
        override val message: String,
        val operation: String? = null,
        val filePath: String? = null,
        override val cause: Throwable? = null
    ) : AppError()
    
    /**
     * Permission-related errors
     */
    data class PermissionError(
        override val message: String,
        val permission: String? = null,
        val requiredLevel: String? = null
    ) : AppError()
    
    /**
     * Configuration and setup errors
     */
    data class ConfigurationError(
        override val message: String,
        val component: String? = null,
        override val cause: Throwable? = null
    ) : AppError()
    
    /**
     * External service errors
     */
    data class ExternalServiceError(
        override val message: String,
        val service: String? = null,
        val statusCode: Int? = null,
        override val cause: Throwable? = null
    ) : AppError()
    
    /**
     * Unknown or unexpected errors
     */
    data class UnknownError(
        override val message: String = "An unknown error occurred",
        override val cause: Throwable? = null
    ) : AppError()
    
    /**
     * Timeout errors
     */
    data class TimeoutError(
        override val message: String,
        val operation: String? = null,
        val timeoutDuration: Long? = null
    ) : AppError()
    
    /**
     * Rate limiting errors
     */
    data class RateLimitError(
        override val message: String,
        val retryAfter: Long? = null,
        val limit: Int? = null
    ) : AppError()
}

/**
 * Extension functions for error handling
 */
fun Throwable.toAppError(): AppError {
    return when (this) {
        is AppError -> this
        is java.net.UnknownHostException -> AppError.NetworkError("No internet connection", cause = this)
        is java.net.SocketTimeoutException -> AppError.TimeoutError("Request timed out")
        is java.io.IOException -> AppError.NetworkError("Network error: ${this.message}", cause = this)
        is IllegalArgumentException -> AppError.ValidationError("Invalid input: ${this.message}")
        is IllegalStateException -> AppError.BusinessError("Invalid state: ${this.message}")
        is SecurityException -> AppError.PermissionError("Permission denied: ${this.message}")
        else -> AppError.UnknownError("Unexpected error: ${this.message}", cause = this)
    }
}

/**
 * Error severity levels
 */
enum class ErrorSeverity {
    LOW,      // Minor issues that don't affect core functionality
    MEDIUM,   // Issues that affect some functionality but have workarounds
    HIGH,     // Issues that significantly impact functionality
    CRITICAL  // Issues that prevent core functionality from working
}

/**
 * Error context for better debugging and analytics
 */
data class ErrorContext(
    val userId: String? = null,
    val sessionId: String? = null,
    val feature: String? = null,
    val action: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceInfo: Map<String, String>? = null,
    val additionalData: Map<String, Any>? = null
)

/**
 * Enhanced error with context and severity
 */
data class EnhancedError(
    val error: AppError,
    val severity: ErrorSeverity = ErrorSeverity.MEDIUM,
    val context: ErrorContext? = null,
    val isRetryable: Boolean = false,
    val userMessage: String? = null,
    val technicalMessage: String? = null,
    val errorId: String = java.util.UUID.randomUUID().toString()
)