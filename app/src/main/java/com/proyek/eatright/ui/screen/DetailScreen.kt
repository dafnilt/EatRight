package com.proyek.eatright.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyek.eatright.data.model.FoodDetail
import com.proyek.eatright.data.model.Serving
import com.proyek.eatright.viewmodel.FoodDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FoodDetailScreen(
    foodId: String,
    onBack: () -> Unit,
    viewModel: FoodDetailViewModel = viewModel()
) {
    val isLoading = viewModel.isLoading.value
    val foodDetail = viewModel.foodDetail.value
    val selectedServingIndex = viewModel.selectedServingIndex.value

    LaunchedEffect(foodId) {
        viewModel.loadFoodDetails(foodId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                title = { Text("Detail Makanan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )

            if (isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            } else if (foodDetail == null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Detail tidak ditemukan")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadFoodDetails(foodId) }) {
                            Text("Coba Lagi")
                        }
                    }
                }
            } else {
                DetailContent(
                    foodDetail = foodDetail,
                    selectedServingIndex = selectedServingIndex,
                    onServingSelected = { index -> viewModel.selectServing(index) }
                )
            }
        }
    }
}

@Composable
fun DetailContent(
    foodDetail: FoodDetail,
    selectedServingIndex: Int,
    onServingSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Basic Food Info
        Text(
            text = foodDetail.foodName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = foodDetail.foodType,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Serving Selection
        if (foodDetail.servings.isNotEmpty()) {
            Text(
                text = "Pilih Porsi:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(foodDetail.servings.size) { index ->
                    val serving = foodDetail.servings[index]
                    ServingChip(
                        description = serving.servingDescription,
                        isSelected = index == selectedServingIndex,
                        onClick = { onServingSelected(index) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nutrition Info
            val selectedServing = foodDetail.servings[selectedServingIndex]
            Text(
                text = "Informasi Nutrisi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            NutritionInfo(serving = selectedServing)
        }
    }
}

@Composable
fun ServingChip(
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        onClick = onClick,
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
            )
        }
    }
}

@Composable
fun NutritionInfo(serving: Serving) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            NutritionHeader(
                calories = serving.calories,
                servingDescription = serving.servingDescription
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Macronutrients
            MacronutrientSection(
                carbs = serving.carbohydrate,
                protein = serving.protein,
                fat = serving.fat
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Detailed nutrition
            DetailedNutritionSection(serving = serving)
        }
    }
}

@Composable
fun NutritionHeader(calories: Int, servingDescription: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "$calories",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kalori",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = "Per $servingDescription",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.End,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun MacronutrientSection(carbs: Double, protein: Double, fat: Double) {
    // Calculate percentages for the progress bars
    val total = carbs + protein + fat
    val carbPercentage = if (total > 0) carbs / total else 0.0
    val proteinPercentage = if (total > 0) protein / total else 0.0
    val fatPercentage = if (total > 0) fat / total else 0.0

    Column {
        Text(
            text = "Makronutrien",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Stacked progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.LightGray)
        ) {
            Row {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(carbPercentage.toFloat())
                        .background(Color(0xFF5DADE2)) // Blue for carbs
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(proteinPercentage.toFloat() / (1f - carbPercentage.toFloat()))
                        .background(Color(0xFF58D68D)) // Green for protein
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .background(Color(0xFFF1948A)) // Red for fat
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MacroLegendItem(
                color = Color(0xFF5DADE2),
                name = "Karbohidrat",
                value = carbs,
                unit = "g"
            )
            MacroLegendItem(
                color = Color(0xFF58D68D),
                name = "Protein",
                value = protein,
                unit = "g"
            )
            MacroLegendItem(
                color = Color(0xFFF1948A),
                name = "Lemak",
                value = fat,
                unit = "g"
            )
        }
    }
}

@Composable
fun MacroLegendItem(color: Color, name: String, value: Double, unit: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${String.format("%.1f", value)}$unit",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DetailedNutritionSection(serving: Serving) {
    Column {
        Text(
            text = "Informasi Nutrisi Detail",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        NutritionRow("Lemak Jenuh", serving.saturatedFat, "g")
        NutritionRow("Lemak Tak Jenuh Ganda", serving.polyunsaturatedFat, "g")
        NutritionRow("Lemak Tak Jenuh Tunggal", serving.monounsaturatedFat, "g")
        NutritionRow("Kolesterol", serving.cholesterol, "mg")
        NutritionRow("Sodium", serving.sodium, "mg")
        NutritionRow("Kalium", serving.potassium, "mg")
        NutritionRow("Serat", serving.fiber, "g")
        NutritionRow("Gula", serving.sugar, "g")
        NutritionRow("Vitamin A", serving.vitaminA, "IU")
        NutritionRow("Vitamin C", serving.vitaminC, "mg")
        NutritionRow("Kalsium", serving.calcium, "mg")
        NutritionRow("Zat Besi", serving.iron, "mg")
    }
}

@Composable
fun NutritionRow(label: String, value: Double, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${String.format("%.1f", value)} $unit",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
