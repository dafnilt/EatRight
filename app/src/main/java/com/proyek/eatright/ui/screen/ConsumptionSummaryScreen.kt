package com.proyek.eatright.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyek.eatright.data.model.DailyConsumption
import com.proyek.eatright.data.model.FoodDetail
import com.proyek.eatright.data.model.Serving
import com.proyek.eatright.viewmodel.ConsumptionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionSummaryScreen(
    onBack: () -> Unit,
    viewModel: ConsumptionViewModel = viewModel()
) {
    val userConsumptions by viewModel.userConsumptions.collectAsState()
    val foodDetailsCache by viewModel.foodDetailsCache.collectAsState()
    val consumptionState by viewModel.consumptionState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Selected date state
    val selectedDate = remember { mutableStateOf(System.currentTimeMillis()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()) }

    // Effect to load consumptions when the screen is displayed or date changes
    LaunchedEffect(selectedDate.value) {
        viewModel.loadUserConsumptions(selectedDate.value)
    }

    // Effect to show snackbar when consumption state changes
    LaunchedEffect(consumptionState) {
        when (consumptionState) {
            is com.proyek.eatright.viewmodel.ConsumptionState.Success -> {
                snackbarHostState.showSnackbar(
                    (consumptionState as com.proyek.eatright.viewmodel.ConsumptionState.Success).message
                )
                viewModel.resetState()
            }
            is com.proyek.eatright.viewmodel.ConsumptionState.Error -> {
                snackbarHostState.showSnackbar(
                    (consumptionState as com.proyek.eatright.viewmodel.ConsumptionState.Error).message
                )
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Konsumsi Harian") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Date display and navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = selectedDate.value
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                        selectedDate.value = calendar.timeInMillis
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous Day"
                    )
                }

                Text(
                    text = dateFormat.format(Date(selectedDate.value)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = selectedDate.value
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        selectedDate.value = calendar.timeInMillis
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next Day"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading indicator
            if (consumptionState is com.proyek.eatright.viewmodel.ConsumptionState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Consumption list
            if (userConsumptions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada konsumsi yang tercatat untuk hari ini",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                // Comprehensive Nutritional Totals
                var totalCalories = 0
                var totalCarbohydrate = 0.0
                var totalProtein = 0.0
                var totalFat = 0.0
                var totalSaturatedFat = 0.0
                var totalPolyunsaturatedFat = 0.0
                var totalMonounsaturatedFat = 0.0
                var totalCholesterol = 0.0
                var totalSodium = 0.0
                var totalPotassium = 0.0
                var totalFiber = 0.0
                var totalSugar = 0.0
                var totalVitaminA = 0.0
                var totalVitaminC = 0.0
                var totalCalcium = 0.0
                var totalIron = 0.0

                userConsumptions.forEach { consumption ->
                    val foodDetail = foodDetailsCache[consumption.foodId]
                    if (foodDetail != null &&
                        consumption.servingIndex >= 0 &&
                        consumption.servingIndex < foodDetail.servings.size) {

                        val serving = foodDetail.servings[consumption.servingIndex]
                        val quantity = consumption.quantity

                        totalCalories += serving.calories * quantity
                        totalCarbohydrate += serving.carbohydrate * quantity
                        totalProtein += serving.protein * quantity
                        totalFat += serving.fat * quantity
                        totalSaturatedFat += serving.saturatedFat * quantity
                        totalPolyunsaturatedFat += serving.polyunsaturatedFat * quantity
                        totalMonounsaturatedFat += serving.monounsaturatedFat * quantity
                        totalCholesterol += serving.cholesterol * quantity
                        totalSodium += serving.sodium * quantity
                        totalPotassium += serving.potassium * quantity
                        totalFiber += serving.fiber * quantity
                        totalSugar += serving.sugar * quantity
                        totalVitaminA += serving.vitaminA * quantity
                        totalVitaminC += serving.vitaminC * quantity
                        totalCalcium += serving.calcium * quantity
                        totalIron += serving.iron * quantity
                    }
                }

                // Nutrition Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Ringkasan Nutrisi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Macro Nutrients Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            NutrientSummary(name = "Kalori", value = "${totalCalories.toInt()} kcal")
                            NutrientSummary(name = "Karbohidrat", value = "${String.format("%.1f", totalCarbohydrate)}g")
                            NutrientSummary(name = "Protein", value = "${String.format("%.1f", totalProtein)}g")
                            NutrientSummary(name = "Lemak", value = "${String.format("%.1f", totalFat)}g")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Detailed Nutrition Breakdown
                        Column {
                            NutritionDetailRow("Lemak Jenuh", totalSaturatedFat)
                            NutritionDetailRow("Lemak Tak Jenuh Ganda", totalPolyunsaturatedFat)
                            NutritionDetailRow("Lemak Tak Jenuh Tunggal", totalMonounsaturatedFat)
                            NutritionDetailRow("Kolesterol", totalCholesterol)
                            NutritionDetailRow("Sodium", totalSodium)
                            NutritionDetailRow("Kalium", totalPotassium)
                            NutritionDetailRow("Serat", totalFiber)
                            NutritionDetailRow("Gula", totalSugar)
                            NutritionDetailRow("Vitamin A", totalVitaminA)
                            NutritionDetailRow("Vitamin C", totalVitaminC)
                            NutritionDetailRow("Kalsium", totalCalcium)
                            NutritionDetailRow("Zat Besi", totalIron)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of consumptions grouped by meal type
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val groupedConsumptions = userConsumptions.groupBy { it.mealType }

                    groupedConsumptions.forEach { (mealType, consumptions) ->
                        item {
                            Text(
                                text = mealType,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(consumptions) { consumption ->
                            val foodDetail = foodDetailsCache[consumption.foodId]
                            val serving = if (foodDetail != null &&
                                consumption.servingIndex >= 0 &&
                                consumption.servingIndex < foodDetail.servings.size) {
                                foodDetail.servings[consumption.servingIndex]
                            } else null

                            ConsumptionItem(
                                consumption = consumption,
                                foodDetail = foodDetail,
                                serving = serving,
                                onDelete = {
                                    coroutineScope.launch {
                                        viewModel.deleteConsumption(consumption.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConsumptionItem(
    consumption: DailyConsumption,
    foodDetail: FoodDetail?,
    serving: Serving?,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = foodDetail?.foodName ?: "Loading...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tampilkan informasi serving jika tersedia
                if (serving != null) {
                    Text(
                        text = "${consumption.quantity}x ${serving.servingDescription}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Kalkulasi total kalori berdasarkan serving dan quantity
                    val totalCalories = serving.calories * consumption.quantity
                    Text(
                        text = "$totalCalories kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                } else {
                    Text(
                        text = "Loading nutrisi...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun NutrientSummary(name: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NutritionDetailRow(label: String, value: Double) {
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
            text = "${String.format("%.1f", value)} ${getNutrientUnit(label)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// Helper function to get appropriate unit for each nutrient
fun getNutrientUnit(label: String): String {
    return when (label) {
        "Lemak Jenuh" -> "g"
        "Lemak Tak Jenuh Ganda" -> "g"
        "Lemak Tak Jenuh Tunggal" -> "g"
        "Kolesterol" -> "mg"
        "Sodium" -> "mg"
        "Kalium" -> "mg"
        "Serat" -> "g"
        "Gula" -> "g"
        "Vitamin A" -> "IU"
        "Vitamin C" -> "mg"
        "Kalsium" -> "mg"
        "Zat Besi" -> "mg"
        else -> ""
    }
}