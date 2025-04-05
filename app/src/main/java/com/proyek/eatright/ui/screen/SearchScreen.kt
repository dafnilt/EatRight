package com.proyek.eatright.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyek.eatright.data.model.Food
import com.proyek.eatright.viewmodel.FoodSearchViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onFoodClick: (String) -> Unit,
    onConsumptionSummaryClick: () -> Unit,
    viewModel: FoodSearchViewModel
) {
    val searchQuery = remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasSearched = viewModel.hasSearched.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EatRight") },
                actions = {
                    IconButton(onClick = onConsumptionSummaryClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Konsumsi Harian"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari makanan...") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (searchQuery.value.isNotEmpty()) {
                                viewModel.searchFoods(searchQuery.value)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Cari")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search results
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (hasSearched.value && searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada hasil ditemukan untuk \"${searchQuery.value}\"")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(searchResults) { food ->
                        FoodItem(
                            food = food,
                            onClick = { onFoodClick(food.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FoodItem(
    food: Food,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = food.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (food.description.isNotEmpty()) {
                Text(
                    text = food.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = food.type,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}