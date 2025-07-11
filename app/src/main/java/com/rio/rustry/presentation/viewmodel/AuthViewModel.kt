package com.rio.rustry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rustry.data.model.User
import com.rio.rustry.data.model.UserType
import com.rio.rustry.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    
    private val authRepository = AuthRepository(
        FirebaseAuth.getInstance(),
        FirebaseFirestore.getInstance()
    )
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        val firebaseUser = authRepository.getCurrentUser()
        if (firebaseUser != null) {
            viewModelScope.launch {
                authRepository.getUserProfile(firebaseUser.uid).fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        _uiState.value = _uiState.value.copy(isAuthenticated = true)
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(isAuthenticated = false)
                    }
                )
            }
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.signInWithEmail(email, password).fold(
                onSuccess = { firebaseUser ->
                    authRepository.getUserProfile(firebaseUser.uid).fold(
                        onSuccess = { user ->
                            _currentUser.value = user
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isAuthenticated = true
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Profile loading failed: ${error.message}"
                            )
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = parseFirebaseError(error.message)
                    )
                }
            )
        }
    }
    
    fun signUp(
        email: String,
        password: String,
        name: String,
        phone: String,
        location: String,
        userType: UserType
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.signUpWithEmail(email, password, name, phone, location, userType).fold(
                onSuccess = { firebaseUser ->
                    authRepository.getUserProfile(firebaseUser.uid).fold(
                        onSuccess = { user ->
                            _currentUser.value = user
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isAuthenticated = true
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Profile creation failed: ${error.message}"
                            )
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = parseFirebaseError(error.message)
                    )
                }
            )
        }
    }
    
    private fun parseFirebaseError(errorMessage: String?): String {
        return when {
            errorMessage?.contains("CONFIGURATION_NOT_FOUND") == true -> 
                "❌ Firebase Configuration Error!\n\nTo fix this:\n1. Go to Firebase Console\n2. Enable Email/Password Authentication\n3. Enable Firestore Database\n4. Enable Firebase Storage\n\nThen restart the app."
            errorMessage?.contains("network") == true -> 
                "Network error. Please check your internet connection."
            errorMessage?.contains("invalid-email") == true -> 
                "Please enter a valid email address."
            errorMessage?.contains("weak-password") == true -> 
                "Password should be at least 6 characters long."
            errorMessage?.contains("email-already-in-use") == true -> 
                "This email is already registered. Try signing in instead."
            errorMessage?.contains("user-not-found") == true -> 
                "No account found with this email. Please sign up first."
            errorMessage?.contains("wrong-password") == true -> 
                "Incorrect password. Please try again."
            errorMessage?.contains("too-many-requests") == true -> 
                "Too many failed attempts. Please try again later."
            else -> errorMessage ?: "An unexpected error occurred. Please try again."
        }
    }
    
    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _uiState.value = AuthUiState()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)