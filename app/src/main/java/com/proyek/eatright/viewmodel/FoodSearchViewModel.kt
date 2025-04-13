package com.proyek.eatright.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.eatright.data.api.FatSecretApiService
import com.proyek.eatright.data.model.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodSearchViewModel : ViewModel() {
    private val apiService = FatSecretApiService()

    private val _searchResults = MutableStateFlow<List<Food>>(emptyList())
    val searchResults: StateFlow<List<Food>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun searchFoods(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = apiService.searchFoods(query)
                _searchResults.value = response.foods
                _hasSearched.value = true
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}