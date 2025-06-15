package com.proyek.eatright.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
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

    init {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            viewModelScope.launch {
                try {
                    repository.getUserData(firebaseUser.uid).fold(
                        onSuccess = { user ->
                            _currentUser.value = user
                            _authState.value = AuthState.Authenticated
                        },
                        onFailure = { exception ->
                            _authState.value = AuthState.Error(exception.message ?: "Failed to load user data")
                            repository.logout()
                            _currentUser.value = null
                        }
                    )
                } catch (e: Exception) {
                    _authState.value = AuthState.Error("Error loading user data: ${e.message}")
                    repository.logout()
                    _currentUser.value = null
                }
            }
        } else {
            _authState.value = AuthState.Unauthenticated
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
                                _authState.value = AuthState.Error(
                                    getFirebaseErrorMessage(exception)
                                )
                            }
                        )
                    },
                    onFailure = { exception ->
                        _authState.value = AuthState.Error(
                            getFirebaseErrorMessage(exception)
                        )
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    getFirebaseErrorMessage(e)
                )
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
                        _authState.value = AuthState.Error(
                            getFirebaseErrorMessage(exception)
                        )
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    getFirebaseErrorMessage(e)
                )
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

    private fun getFirebaseErrorMessage(exception: Throwable): String {
        return when (exception) {
            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Format email tidak valid"
                    "ERROR_WRONG_PASSWORD" -> "Password salah"
                    "ERROR_USER_NOT_FOUND" -> "Email tidak terdaftar"
                    "ERROR_USER_DISABLED" -> "Akun telah dinonaktifkan"
                    "ERROR_TOO_MANY_REQUESTS" -> "Terlalu banyak percobaan. Coba lagi nanti"
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Email sudah terdaftar"
                    "ERROR_WEAK_PASSWORD" -> "Password terlalu lemah"
                    "ERROR_OPERATION_NOT_ALLOWED" -> "Operasi tidak diizinkan"
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Koneksi internet bermasalah"
                    "ERROR_INVALID_CREDENTIAL" -> "Email atau password salah"
                    else -> exception.message ?: "Terjadi kesalahan pada Firebase Auth"
                }
            }
            else -> {
                when {
                    exception.message?.contains("network", ignoreCase = true) == true ->
                        "Koneksi internet bermasalah"
                    exception.message?.contains("timeout", ignoreCase = true) == true ->
                        "Koneksi timeout. Periksa koneksi internet Anda"
                    else -> exception.message ?: "Terjadi kesalahan tidak diketahui"
                }
            }
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