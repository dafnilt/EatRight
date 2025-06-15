package com.proyek.eatright.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.proyek.eatright.data.model.Food
import com.proyek.eatright.ui.theme.DarkBlue
import com.proyek.eatright.ui.theme.DarkBlue2
import com.proyek.eatright.ui.theme.LightBlue
import com.proyek.eatright.ui.theme.LightBlue2
import com.proyek.eatright.viewmodel.AuthViewModel
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

    // Fungsi untuk melakukan pencarian
    val performSearch = {
        if (searchQuery.value.isNotEmpty()) {
            viewModel.searchFoods(searchQuery.value)
        }
    }

    Scaffold(

    ) { innerPadding ->
        // Konten sama seperti sebelumnya
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
                shape = RoundedCornerShape(25.dp),
                placeholder = { Text("Cari makanan...") },
                trailingIcon = {
                    IconButton(
                        onClick = performSearch
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Cari")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        performSearch()
                    }
                ),
                singleLine = true
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
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kolom informasi utama
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nama makanan
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue2 // Gunakan warna tema
                )

                // Deskripsi (jika ada)
                if (food.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = food.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                }

                // Tipe makanan
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = food.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .background(
                            color = LightBlue.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}