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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyek.eatright.data.api.FatSecretApiService
import com.proyek.eatright.data.model.DailyConsumption
import com.proyek.eatright.data.model.FoodDetail
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
    val consumptionState by viewModel.consumptionState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val apiService = remember { FatSecretApiService() }

    // State to hold fetched food details
    val foodDetailsMap = remember { mutableStateMapOf<String, FoodDetail?>() }

    // Selected date state
    val selectedDate = remember { mutableStateOf(System.currentTimeMillis()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()) }

    // Effect to load consumptions when the screen is displayed or date changes
    LaunchedEffect(selectedDate.value) {
        viewModel.loadUserConsumptions(selectedDate.value)
    }

    // Effect to fetch food details for each consumption item
    LaunchedEffect(userConsumptions) {
        userConsumptions.forEach { consumption ->
            if (!foodDetailsMap.containsKey(consumption.foodId)) {
                coroutineScope.launch {
                    try {
                        val foodDetail = apiService.getFoodDetails(consumption.foodId)
                        foodDetailsMap[consumption.foodId] = foodDetail
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            }
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
                            ConsumptionItem(
                                consumption = consumption,
                                foodDetail = foodDetailsMap[consumption.foodId],
                                onDelete = {
                                    coroutineScope.launch {
                                        viewModel.deleteConsumption(consumption.id)
                                    }
                                }
                            )
                        }
                    }

                    // Summary section
                    item {
                        Spacer(modifier = Modifier.height(16.dp))

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

                                // Calculate total nutrition
                                var totalCalories = 0
                                var totalProtein = 0.0
                                var totalCarbs = 0.0
                                var totalFat = 0.0

                                userConsumptions.forEach { consumption ->
                                    val foodDetail = foodDetailsMap[consumption.foodId]
                                    if (foodDetail != null && consumption.servingIndex < foodDetail.servings.size) {
                                        val serving = foodDetail.servings[consumption.servingIndex]
                                        totalCalories += serving.calories * consumption.quantity
                                        totalProtein += serving.protein * consumption.quantity
                                        totalCarbs += serving.carbohydrate * consumption.quantity
                                        totalFat += serving.fat * consumption.quantity
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    NutrientSummary(name = "Kalori", value = "$totalCalories kcal")
                                    NutrientSummary(name = "Protein", value = "${String.format("%.1f", totalProtein)}g")
                                    NutrientSummary(name = "Karbohidrat", value = "${String.format("%.1f", totalCarbs)}g")
                                    NutrientSummary(name = "Lemak", value = "${String.format("%.1f", totalFat)}g")
                                }
                            }
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
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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

                if (foodDetail != null && consumption.servingIndex < foodDetail.servings.size) {
                    val serving = foodDetail.servings[consumption.servingIndex]

                    Text(
                        text = "${consumption.quantity}x ${serving.servingDescription}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Calculate total nutrition based on quantity
                    val totalCalories = serving.calories * consumption.quantity
                    Text(
                        text = "Total: $totalCalories kcal",
                        style = MaterialTheme.typography.bodyMedium
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