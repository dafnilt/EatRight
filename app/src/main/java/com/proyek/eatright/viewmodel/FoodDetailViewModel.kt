package com.proyek.eatright.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.eatright.data.api.FatSecretApiService
import com.proyek.eatright.data.model.FoodDetail
import com.proyek.eatright.data.model.Serving
import kotlinx.coroutines.launch

class FoodDetailViewModel : ViewModel() {
    private val apiService = FatSecretApiService()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _foodDetail = mutableStateOf<FoodDetail?>(null)
    val foodDetail: State<FoodDetail?> = _foodDetail

    private val _selectedServingIndex = mutableStateOf(0)
    val selectedServingIndex: State<Int> = _selectedServingIndex

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadFoodDetails(foodId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val detail = apiService.getFoodDetails(foodId)
                _foodDetail.value = detail
            } catch (e: Exception) {
                Log.e("FoodDetailViewModel", "Error loading food detail", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectServing(index: Int) {
        if (index >= 0 && index < (_foodDetail.value?.servings?.size ?: 0)) {
            _selectedServingIndex.value = index
        }
    }

    fun getSelectedServing(): Serving? {
        val details = _foodDetail.value
        val index = _selectedServingIndex.value
        return if (details != null && index >= 0 && index < details.servings.size) {
            details.servings[index]
        } else {
            null
        }
    }
}