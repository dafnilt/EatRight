package com.proyek.eatright.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.proyek.eatright.data.model.User
import com.proyek.eatright.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    // Use lazy initialization
    private val repository by lazy { AuthRepository() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Initialize in a safe way
    init {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    _authState.value = AuthState.Loading
                    repository.getUserData(currentUser.uid).fold(
                        onSuccess = { user ->
                            _currentUser.value = user
                            _authState.value = AuthState.Authenticated
                        },
                        onFailure = {
                            _authState.value = AuthState.Error("Failed to load user data")
                        }
                    )
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error initializing: ${e.message}")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.login(email, password).fold(
                    onSuccess = { firebaseUser ->
                        repository.getUserData(firebaseUser.uid).fold(
                            onSuccess = { user ->
                                _currentUser.value = user
                                _authState.value = AuthState.Authenticated
                            },
                            onFailure = { exception ->
                                _authState.value = AuthState.Error(exception.message ?: "Failed to load user data")
                            }
                        )
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(exception.message ?: "Login failed")
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Login error: ${e.message}")
            }
        }
    }

    fun register(user: User) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.register(user).fold(
                    onSuccess = { firebaseUser ->
                        _currentUser.value = user.copy(id = firebaseUser.uid)
                        _authState.value = AuthState.Authenticated
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(exception.message ?: "Registration failed")
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Registration error: ${e.message}")
            }
        }
    }

    fun logout() {
        try {
            repository.logout()
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Logout error: ${e.message}")
        }
    }

    fun resetAuthState() {
        try {
            _authState.value = if (auth.currentUser != null)
                AuthState.Authenticated
            else
                AuthState.Unauthenticated
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Error resetting state: ${e.message}")
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}