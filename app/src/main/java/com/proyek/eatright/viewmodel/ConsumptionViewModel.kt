package com.proyek.eatright.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.proyek.eatright.data.api.FatSecretApiService
import com.proyek.eatright.data.model.DailyConsumption
import com.proyek.eatright.data.model.FoodDetail
import com.proyek.eatright.data.model.NutritionalDetails
import com.proyek.eatright.data.model.Serving
import com.proyek.eatright.data.repository.ConsumptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConsumptionViewModel : ViewModel() {
    private val repository by lazy { ConsumptionRepository() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val apiService by lazy { FatSecretApiService() }

    private val _consumptionState = MutableStateFlow<ConsumptionState>(ConsumptionState.Initial)
    val consumptionState: StateFlow<ConsumptionState> = _consumptionState.asStateFlow()

    private val _userConsumptions = MutableStateFlow<List<DailyConsumption>>(emptyList())
    val userConsumptions: StateFlow<List<DailyConsumption>> = _userConsumptions.asStateFlow()

    // Cache untuk FoodDetail yang telah diambil dari API
    private val _foodDetailsCache = MutableStateFlow<Map<String, FoodDetail>>(emptyMap())
    val foodDetailsCache: StateFlow<Map<String, FoodDetail>> = _foodDetailsCache.asStateFlow()

    // Function to add food to daily consumption
    @RequiresApi(Build.VERSION_CODES.O)
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

            try {
                // Ambil detail makanan dari API jika belum ada di cache
                if (!_foodDetailsCache.value.containsKey(foodId)) {
                    val foodDetail = apiService.getFoodDetails(foodId)
                    if (foodDetail != null) {
                        val newCache = _foodDetailsCache.value.toMutableMap()
                        newCache[foodId] = foodDetail
                        _foodDetailsCache.value = newCache
                    } else {
                        _consumptionState.value = ConsumptionState.Error("Tidak dapat mengambil detail makanan")
                        return@launch
                    }
                }

                // Ambil serving yang dipilih
                val foodDetail = _foodDetailsCache.value[foodId]
                val selectedServing = foodDetail?.servings?.getOrNull(servingIndex)

                if (selectedServing == null) {
                    _consumptionState.value = ConsumptionState.Error("Porsi makanan tidak valid")
                    return@launch
                }

                // Buat objek nutritional details dengan perhitungan berdasarkan quantity
                val nutritionalDetails = NutritionalDetails(
                    calories = selectedServing.calories * quantity,
                    carbohydrate = selectedServing.carbohydrate * quantity,
                    protein = selectedServing.protein * quantity,
                    fat = selectedServing.fat * quantity,
                    saturatedFat = selectedServing.saturatedFat * quantity,
                    polyunsaturatedFat = selectedServing.polyunsaturatedFat * quantity,
                    monounsaturatedFat = selectedServing.monounsaturatedFat * quantity,
                    cholesterol = selectedServing.cholesterol * quantity,
                    sodium = selectedServing.sodium * quantity,
                    potassium = selectedServing.potassium * quantity,
                    fiber = selectedServing.fiber * quantity,
                    sugar = selectedServing.sugar * quantity,
                    vitaminA = selectedServing.vitaminA * quantity,
                    vitaminC = selectedServing.vitaminC * quantity,
                    calcium = selectedServing.calcium * quantity,
                    iron = selectedServing.iron * quantity
                )

                // Buat objek konsumsi
                val consumption = DailyConsumption(
                    userId = currentUser.uid,
                    foodId = foodId,
                    servingIndex = servingIndex,
                    quantity = quantity,
                    timestamp = System.currentTimeMillis(),
                    mealType = mealType,
                    nutritionalDetails = nutritionalDetails
                )

                // Simpan ke repository
                repository.addConsumption(consumption).fold(
                    onSuccess = {
                        _consumptionState.value = ConsumptionState.Success("Makanan berhasil ditambahkan ke konsumsi harian")
                        loadUserConsumptions() // Refresh the list after adding
                    },
                    onFailure = { exception ->
                        _consumptionState.value = ConsumptionState.Error(exception.message ?: "Terjadi kesalahan saat menyimpan")
                    }
                )
            } catch (e: Exception) {
                _consumptionState.value = ConsumptionState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    // Function to load user's consumptions for today with food details
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadUserConsumptions(date: Long = System.currentTimeMillis()) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _consumptionState.value = ConsumptionState.Error("User tidak terautentikasi")
            return
        }

        viewModelScope.launch {
            _consumptionState.value = ConsumptionState.Loading

            try {
                // Load consumptions
                repository.getUserConsumptions(currentUser.uid, date).fold(
                    onSuccess = { consumptions ->
                        _userConsumptions.value = consumptions

                        // Load food details for all consumptions
                        val uniqueFoodIds = consumptions.map { it.foodId }.distinct()
                        val newCache = _foodDetailsCache.value.toMutableMap()

                        // Ambil food details yang belum ada di cache
                        for (foodId in uniqueFoodIds) {
                            if (!newCache.containsKey(foodId)) {
                                try {
                                    val foodDetail = apiService.getFoodDetails(foodId)
                                    if (foodDetail != null) {
                                        newCache[foodId] = foodDetail
                                    }
                                } catch (e: Exception) {
                                    Log.e("ConsumptionViewModel", "Error fetching food details for $foodId", e)
                                }
                            }
                        }

                        _foodDetailsCache.value = newCache
                        _consumptionState.value = ConsumptionState.Initial
                    },
                    onFailure = { exception ->
                        _consumptionState.value = ConsumptionState.Error(exception.message ?: "Terjadi kesalahan saat mengambil data")
                    }
                )
            } catch (e: Exception) {
                _consumptionState.value = ConsumptionState.Error("Terjadi kesalahan: ${e.message}")
            }
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
                    _consumptionState.value = ConsumptionState.Error(exception.message ?: "Terjadi kesalahan saat menghapus")
                }
            )
        }
    }

    // Helper function to get FoodDetail for a specific consumption
    fun getFoodDetailForConsumption(consumption: DailyConsumption): FoodDetail? {
        return _foodDetailsCache.value[consumption.foodId]
    }

    // Helper function to get Serving for a specific consumption
    fun getServingForConsumption(consumption: DailyConsumption): Serving? {
        val foodDetail = getFoodDetailForConsumption(consumption)
        return if (foodDetail != null &&
            consumption.servingIndex >= 0 &&
            consumption.servingIndex < foodDetail.servings.size) {
            foodDetail.servings[consumption.servingIndex]
        } else {
            null
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