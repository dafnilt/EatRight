package com.proyek.eatright.viewmodel

import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.proyek.eatright.data.api.FatSecretApiService
import com.proyek.eatright.data.model.FoodDetail
import com.proyek.eatright.data.model.Serving
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.proyek.eatright.data.model.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.asStateFlow

class FoodDetailViewModel : ViewModel() {
    private val apiService = FatSecretApiService()

    private val _foodDetail = MutableStateFlow<FoodDetail?>(null)
    val foodDetail: StateFlow<FoodDetail?> = _foodDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFoodDetails(foodId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val result = apiService.getFoodDetails(foodId)
                if (result != null) {
                    _foodDetail.value = result
                } else {
                    _error.value = "Tidak dapat memuat detail makanan"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

//class FoodDetailViewModel : ViewModel() {
//    private val apiService = FatSecretApiService()
//
//    private val _isLoading = mutableStateOf(false)
//    val isLoading: State<Boolean> = _isLoading
//
//    private val _foodDetail = mutableStateOf<FoodDetail?>(null)
//    val foodDetail: State<FoodDetail?> = _foodDetail
//
//    private val _selectedServingIndex = mutableStateOf(0)
//    val selectedServingIndex: State<Int> = _selectedServingIndex
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun loadFoodDetails(foodId: String) {
//        viewModelScope.launch {
//            try {
//                _isLoading.value = true
//                val detail = apiService.getFoodDetails(foodId)
//                _foodDetail.value = detail
//            } catch (e: Exception) {
//                Log.e("FoodDetailViewModel", "Error loading food detail", e)
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    fun selectServing(index: Int) {
//        if (index >= 0 && index < (_foodDetail.value?.servings?.size ?: 0)) {
//            _selectedServingIndex.value = index
//        }
//    }
//
//    fun getSelectedServing(): Serving? {
//        val details = _foodDetail.value
//        val index = _selectedServingIndex.value
//        return if (details != null && index >= 0 && index < details.servings.size) {
//            details.servings[index]
//        } else {
//            null
//        }
//    }
//}