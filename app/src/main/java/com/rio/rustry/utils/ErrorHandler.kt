package com.rio.rustry.utils

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Comprehensive error handling system with proper error categorization
 * 
 * Features:
 * - Error type classification
 * - User-friendly error messages
 * - Retry strategies
 * - Error reporting
 * - Recovery suggestions
 */
object ErrorHandler {
    
    private const val TAG = "ErrorHandler"
    
    /**
     * Handle and classify errors
     */
    fun handleError(throwable: Throwable): AppError {
        Log.e(TAG, "Handling error: ${throwable.javaClass.simpleName}", throwable)
        
        return when (throwable) {
            is CancellationException -> {
                // Don't treat cancellation as an error
                AppError.OperationCancelled
            }
            
            is FirebaseAuthException -> handleAuthError(throwable)
            is FirebaseFirestoreException -> handleFirestoreError(throwable)
            is StorageException -> handleStorageError(throwable)
            is FirebaseException -> handleFirebaseError(throwable)
            
            is UnknownHostException -> AppError.NetworkError.NoConnection
            is SocketTimeoutException -> AppError.NetworkError.Timeout
            is IOException -> AppError.NetworkError.ConnectionFailed
            
            is SecurityException -> AppError.SecurityError.PermissionDenied
            is IllegalArgumentException -> AppError.ValidationError.InvalidInput(throwable.message)
            is IllegalStateException -> AppError.ApplicationError.InvalidState(throwable.message)
            
            else -> AppError.UnknownError(throwable.message ?: "An unexpected error occurred")
        }
    }
    
    /**
     * Handle Firebase Auth errors
     */
    private fun handleAuthError(exception: FirebaseAuthException): AppError {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> AppError.AuthError.InvalidEmail
            "ERROR_WRONG_PASSWORD" -> AppError.AuthError.WrongPassword
            "ERROR_USER_NOT_FOUND" -> AppError.AuthError.UserNotFound
            "ERROR_USER_DISABLED" -> AppError.AuthError.UserDisabled
            "ERROR_TOO_MANY_REQUESTS" -> AppError.AuthError.TooManyRequests
            "ERROR_EMAIL_ALREADY_IN_USE" -> AppError.AuthError.EmailAlreadyInUse
            "ERROR_WEAK_PASSWORD" -> AppError.AuthError.WeakPassword
            "ERROR_NETWORK_REQUEST_FAILED" -> AppError.NetworkError.ConnectionFailed
            else -> AppError.AuthError.Unknown(exception.message)
        }
    }
    
    /**
     * Handle Firestore errors
     */
    private fun handleFirestoreError(exception: FirebaseFirestoreException): AppError {
        return when (exception.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                AppError.SecurityError.PermissionDenied
            FirebaseFirestoreException.Code.NOT_FOUND -> 
                AppError.DataError.NotFound
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> 
                AppError.DataError.AlreadyExists
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> 
                AppError.ServerError.QuotaExceeded
            FirebaseFirestoreException.Code.UNAVAILABLE -> 
                AppError.ServerError.ServiceUnavailable
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> 
                AppError.NetworkError.Timeout
            FirebaseFirestoreException.Code.CANCELLED -> 
                AppError.OperationCancelled
            else -> AppError.DataError.OperationFailed(exception.message)
        }
    }
    
    /**
     * Handle Storage errors
     */
    private fun handleStorageError(exception: StorageException): AppError {
        return when (exception.errorCode) {
            StorageException.ERROR_OBJECT_NOT_FOUND -> AppError.DataError.NotFound
            StorageException.ERROR_BUCKET_NOT_FOUND -> AppError.DataError.NotFound
            StorageException.ERROR_PROJECT_NOT_FOUND -> AppError.DataError.NotFound
            StorageException.ERROR_QUOTA_EXCEEDED -> AppError.ServerError.QuotaExceeded
            StorageException.ERROR_NOT_AUTHENTICATED -> AppError.AuthError.NotAuthenticated
            StorageException.ERROR_NOT_AUTHORIZED -> AppError.SecurityError.PermissionDenied
            StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> AppError.NetworkError.RetryLimitExceeded
            StorageException.ERROR_INVALID_CHECKSUM -> AppError.DataError.CorruptedData
            StorageException.ERROR_CANCELED -> AppError.OperationCancelled
            else -> AppError.DataError.OperationFailed(exception.message)
        }
    }
    
    /**
     * Handle general Firebase errors
     */
    private fun handleFirebaseError(exception: FirebaseException): AppError {
        return AppError.ServerError.FirebaseError(exception.message ?: "Firebase error occurred")
    }
    
    /**
     * Get user-friendly error message
     */
    fun getUserMessage(error: AppError): String {
        return when (error) {
            is AppError.NetworkError.NoConnection -> 
                "No internet connection. Please check your network settings."
            is AppError.NetworkError.Timeout -> 
                "Request timed out. Please try again."
            is AppError.NetworkError.ConnectionFailed -> 
                "Connection failed. Please check your internet connection."
            is AppError.NetworkError.RetryLimitExceeded -> 
                "Maximum retry attempts reached. Please try again later."
                
            is AppError.AuthError.InvalidEmail -> 
                "Please enter a valid email address."
            is AppError.AuthError.WrongPassword -> 
                "Incorrect password. Please try again."
            is AppError.AuthError.UserNotFound -> 
                "No account found with this email address."
            is AppError.AuthError.UserDisabled -> 
                "This account has been disabled. Please contact support."
            is AppError.AuthError.TooManyRequests -> 
                "Too many failed attempts. Please try again later."
            is AppError.AuthError.EmailAlreadyInUse -> 
                "An account with this email already exists."
            is AppError.AuthError.WeakPassword -> 
                "Password is too weak. Please choose a stronger password."
            is AppError.AuthError.NotAuthenticated -> 
                "Please sign in to continue."
            is AppError.AuthError.Unknown -> 
                error.message ?: "Authentication failed. Please try again."
                
            is AppError.DataError.NotFound -> 
                "The requested data was not found."
            is AppError.DataError.AlreadyExists -> 
                "This item already exists."
            is AppError.DataError.CorruptedData -> 
                "Data appears to be corrupted. Please try again."
            is AppError.DataError.OperationFailed -> 
                error.message ?: "Data operation failed. Please try again."
                
            is AppError.SecurityError.PermissionDenied -> 
                "You don't have permission to perform this action."
                
            is AppError.ServerError.ServiceUnavailable -> 
                "Service is temporarily unavailable. Please try again later."
            is AppError.ServerError.QuotaExceeded -> 
                "Service quota exceeded. Please try again later."
            is AppError.ServerError.FirebaseError -> 
                "Server error occurred. Please try again later."
                
            is AppError.ValidationError.InvalidInput -> 
                error.message ?: "Invalid input provided."
                
            is AppError.ApplicationError.InvalidState -> 
                "Application is in an invalid state. Please restart the app."
                
            is AppError.OperationCancelled -> 
                "Operation was cancelled."
                
            is AppError.UnknownError -> 
                "An unexpected error occurred. Please try again."
        }
    }
    
    /**
     * Check if error is recoverable
     */
    fun isRecoverable(error: AppError): Boolean {
        return when (error) {
            is AppError.NetworkError -> true
            is AppError.ServerError.ServiceUnavailable -> true
            is AppError.ServerError.QuotaExceeded -> true
            is AppError.AuthError.TooManyRequests -> true
            else -> false
        }
    }
}

/**
 * Comprehensive error types
 */
sealed class AppError : Throwable() {
    
    sealed class NetworkError : AppError() {
        object NoConnection : NetworkError()
        object Timeout : NetworkError()
        object ConnectionFailed : NetworkError()
        object RetryLimitExceeded : NetworkError()
    }
    
    sealed class AuthError : AppError() {
        object InvalidEmail : AuthError()
        object WrongPassword : AuthError()
        object UserNotFound : AuthError()
        object UserDisabled : AuthError()
        object TooManyRequests : AuthError()
        object EmailAlreadyInUse : AuthError()
        object WeakPassword : AuthError()
        object NotAuthenticated : AuthError()
        data class Unknown(override val message: String?) : AuthError()
    }
    
    sealed class DataError : AppError() {
        object NotFound : DataError()
        object AlreadyExists : DataError()
        object CorruptedData : DataError()
        data class OperationFailed(override val message: String?) : DataError()
    }
    
    sealed class SecurityError : AppError() {
        object PermissionDenied : SecurityError()
    }
    
    sealed class ServerError : AppError() {
        object ServiceUnavailable : ServerError()
        object QuotaExceeded : ServerError()
        data class FirebaseError(override val message: String) : ServerError()
    }
    
    sealed class ValidationError : AppError() {
        data class InvalidInput(override val message: String?) : ValidationError()
    }
    
    sealed class ApplicationError : AppError() {
        data class InvalidState(override val message: String?) : ApplicationError()
    }
    
    object OperationCancelled : AppError()
    data class UnknownError(override val message: String) : AppError()
}