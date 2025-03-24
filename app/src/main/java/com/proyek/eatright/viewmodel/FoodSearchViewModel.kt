package com.proyek.eatright.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.eatright.data.api.FatSecretApiService
import com.proyek.eatright.data.model.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

// ViewModel
class FoodSearchViewModel : ViewModel() {
    private val apiService = FatSecretApiService()

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _searchResults = mutableStateOf<List<Food>>(emptyList())
    val searchResults: State<List<Food>> = _searchResults

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun searchFoods() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val query = searchQuery.value
                if (query.isNotBlank()) {
                    val response = apiService.searchFoods(query)
                    _searchResults.value = response.foods
                }
            } catch (e: Exception) {
                Log.e("FoodSearchViewModel", "Error searching foods", e)
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
