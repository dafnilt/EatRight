package com.proyek.eatright.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.proyek.eatright.data.model.DailyConsumption
import com.proyek.eatright.data.repository.ConsumptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class ConsumptionViewModel : ViewModel() {
    private val repository by lazy { ConsumptionRepository() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val _consumptionState = MutableStateFlow<ConsumptionState>(ConsumptionState.Initial)
    val consumptionState: StateFlow<ConsumptionState> = _consumptionState.asStateFlow()

    private val _userConsumptions = MutableStateFlow<List<DailyConsumption>>(emptyList())
    val userConsumptions: StateFlow<List<DailyConsumption>> = _userConsumptions.asStateFlow()

    // Function to add food to daily consumption
    fun addToConsumption(
        foodId: String,
        servingIndex: Int,
        quantity: Int,
        mealType: String
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _consumptionState.value = ConsumptionState.Error("User tidak terautentikasi")
            return
        }

        viewModelScope.launch {
            _consumptionState.value = ConsumptionState.Loading

            val consumption = DailyConsumption(
                userId = currentUser.uid,
                foodId = foodId,
                servingIndex = servingIndex,
                quantity = quantity,
                timestamp = System.currentTimeMillis(),
                mealType = mealType
            )

            repository.addConsumption(consumption).fold(
                onSuccess = {
                    _consumptionState.value = ConsumptionState.Success("Makanan berhasil ditambahkan ke konsumsi harian")
                    loadUserConsumptions() // Refresh the list after adding
                },
                onFailure = { exception ->
                    _consumptionState.value = ConsumptionState.Error(exception.message ?: "Terjadi kesalahan")
                }
            )
        }
    }

    // Function to load user's consumptions for today
    fun loadUserConsumptions(date: Long = System.currentTimeMillis()) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _consumptionState.value = ConsumptionState.Error("User tidak terautentikasi")
            return
        }

        viewModelScope.launch {
            _consumptionState.value = ConsumptionState.Loading

            repository.getUserConsumptions(currentUser.uid, date).fold(
                onSuccess = { consumptions ->
                    _userConsumptions.value = consumptions
                    _consumptionState.value = ConsumptionState.Initial
                },
                onFailure = { exception ->
                    _consumptionState.value = ConsumptionState.Error(exception.message ?: "Terjadi kesalahan")
                }
            )
        }
    }

    // Function to delete a consumption entry
    fun deleteConsumption(consumptionId: String) {
        viewModelScope.launch {
            _consumptionState.value = ConsumptionState.Loading

            repository.deleteConsumption(consumptionId).fold(
                onSuccess = {
                    _consumptionState.value = ConsumptionState.Success("Makanan berhasil dihapus dari konsumsi harian")
                    loadUserConsumptions() // Refresh the list after deleting
                },
                onFailure = { exception ->
                    _consumptionState.value = ConsumptionState.Error(exception.message ?: "Terjadi kesalahan")
                }
            )
        }
    }

    // Reset state (e.g., after showing success or error message)
    fun resetState() {
        _consumptionState.value = ConsumptionState.Initial
    }
}

// State handling for UI
sealed class ConsumptionState {
    object Initial : ConsumptionState()
    object Loading : ConsumptionState()
    data class Success(val message: String) : ConsumptionState()
    data class Error(val message: String) : ConsumptionState()
}