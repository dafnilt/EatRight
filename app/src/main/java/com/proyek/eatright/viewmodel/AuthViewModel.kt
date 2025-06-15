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
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    // Use lazy initialization
    private val repository by lazy { AuthRepository() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _updateProfileState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Initial)
    val updateProfileState: StateFlow<UpdateProfileState> = _updateProfileState.asStateFlow()

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
                        // Tetap di halaman register dengan menampilkan error
                        // Jangan ubah ke Unauthenticated
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

    fun updateProfile(updatedUser: User, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _updateProfileState.value = UpdateProfileState.Loading

            // Debug logging
            android.util.Log.d("AuthViewModel", "Starting profile update...")
            android.util.Log.d("AuthViewModel", "Current user: ${_currentUser.value}")
            android.util.Log.d("AuthViewModel", "Updated user: $updatedUser")

            val currentUser = _currentUser.value
            val emailChanged = currentUser?.email != updatedUser.email

            try {
                repository.updateUserProfile(updatedUser).fold(
                    onSuccess = {
                        android.util.Log.d("AuthViewModel", "Profile update successful")

                        if (emailChanged) {
                            // Jika email berubah, update currentUser tapi tetap gunakan email lama
                            // karena email baru belum diverifikasi
                            val updatedUserWithOldEmail = updatedUser.copy(email = currentUser?.email ?: updatedUser.email)
                            _currentUser.value = updatedUserWithOldEmail
                            _updateProfileState.value = UpdateProfileState.EmailVerificationSent(updatedUser.email)
                        } else {
                            // Jika email tidak berubah, update seperti biasa
                            _currentUser.value = updatedUser
                            _updateProfileState.value = UpdateProfileState.Success
                        }
                        onComplete(true)
                    },
                    onFailure = { exception ->
                        android.util.Log.e("AuthViewModel", "Profile update failed: ${exception.message}")
                        exception.printStackTrace()
                        _updateProfileState.value = UpdateProfileState.Error(
                            getFirebaseErrorMessage(exception)
                        )
                        onComplete(false)
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Exception in updateProfile: ${e.message}")
                e.printStackTrace()
                _updateProfileState.value = UpdateProfileState.Error(
                    getFirebaseErrorMessage(e)
                )
                onComplete(false)
            }
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            val currentUser = _currentUser.value
            if (currentUser != null) {
                try {
                    // Cek apakah ada email yang sedang menunggu verifikasi
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        firebaseUser.reload().await()

                        // Jika email di Firebase Auth berbeda dengan yang ada di currentUser,
                        // berarti ada perubahan email yang sudah diverifikasi
                        if (firebaseUser.email != currentUser.email) {
                            repository.checkAndUpdateEmailAfterVerification(
                                currentUser.id,
                                firebaseUser.email ?: currentUser.email
                            ).fold(
                                onSuccess = {
                                    // Update currentUser dengan email yang baru
                                    _currentUser.value = currentUser.copy(email = firebaseUser.email ?: currentUser.email)
                                    _updateProfileState.value = UpdateProfileState.EmailVerified
                                },
                                onFailure = { exception ->
                                    android.util.Log.e("AuthViewModel", "Error updating email after verification: ${exception.message}")
                                }
                            )
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AuthViewModel", "Error checking email verification: ${e.message}")
                }
            }
        }
    }

    fun resetUpdateProfileState() {
        _updateProfileState.value = UpdateProfileState.Initial
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
            // Jangan reset ke Unauthenticated jika sedang di halaman register/login
            // Reset ke Initial state supaya tidak trigger navigation
            _authState.value = AuthState.Initial
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
                    "ERROR_REQUIRES_RECENT_LOGIN" -> "Untuk mengubah email, silakan login ulang terlebih dahulu"
                    else -> exception.message ?: "Terjadi kesalahan pada Firebase Auth"
                }
            }
            else -> {
                when {
                    exception.message?.contains("EMAIL_ALREADY_EXISTS", ignoreCase = true) == true ->
                        "Email sudah terdaftar"
                    exception.message?.contains("REQUIRES_RECENT_LOGIN", ignoreCase = true) == true ->
                        "Untuk mengubah email, silakan login ulang terlebih dahulu"
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

sealed class UpdateProfileState {
    object Initial : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    object EmailVerified : UpdateProfileState()
    data class EmailVerificationSent(val newEmail: String) : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}