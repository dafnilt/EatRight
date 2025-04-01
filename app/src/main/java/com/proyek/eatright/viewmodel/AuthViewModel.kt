package com.proyek.eatright.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.eatright.data.model.User
import com.proyek.eatright.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun register(user: User) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(user)
            result.fold(
                onSuccess = { firebaseUser ->
                    _authState.value = AuthState.Success("Registrasi berhasil")
                    // Ambil data user dari Firestore
                    getUserData(firebaseUser.uid)
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Registrasi gagal")
                }
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, password)
            result.fold(
                onSuccess = { firebaseUser ->
                    _authState.value = AuthState.Success("Login berhasil")
                    // Ambil data user dari Firestore
                    getUserData(firebaseUser.uid)
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Login gagal")
                }
            )
        }
    }

    private fun getUserData(userId: String) {
        viewModelScope.launch {
            val result = repository.getUserData(userId)
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Gagal mengambil data user")
                }
            )
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val message: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
