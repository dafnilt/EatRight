package com.proyek.eatright.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
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
import com.proyek.eatright.viewmodel.AuthViewModel
import com.proyek.eatright.viewmodel.ConsumptionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionSummaryScreen(
    onBack: () -> Unit,
    viewModel: ConsumptionViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val userConsumptions by viewModel.userConsumptions.collectAsState()
    val foodDetailsCache by viewModel.foodDetailsCache.collectAsState()
    val consumptionState by viewModel.consumptionState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
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
        // Wrap everything in a LazyColumn for better scrolling
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                // Date display and navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
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
            }

            // Loading indicator
            if (consumptionState is com.proyek.eatright.viewmodel.ConsumptionState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Empty state
            if (userConsumptions.isEmpty() && consumptionState !is com.proyek.eatright.viewmodel.ConsumptionState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada konsumsi yang tercatat untuk hari ini",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else if (userConsumptions.isNotEmpty()) {
                // Calculate nutritional values
                val nutritionTotals = calculateNutritionTotals(userConsumptions, foodDetailsCache)

                val userHeight = currentUser?.tinggiBadan ?: 0 // Tinggi badan dalam cm
                val userGender = currentUser?.gender ?: ""
                val userWeight = currentUser?.beratBadan ?: 0 // Berat badan dalam kg

                val idealWeight = calculateIdealWeight(userHeight, userGender)
                val recommendedCalories = calculateRecommendedCalories(idealWeight, userWeight)

                // Carbohidrat threshold calculation
                val carbPercentage = if (nutritionTotals.totalCalories > 0) {
                    (nutritionTotals.totalCarbohydrate * 4 / nutritionTotals.totalCalories.toDouble()) * 100
                } else {
                    0.0
                }

                val isCarbPercentageTooLow = carbPercentage < 45.0
                val isCarbPercentageTooHigh = carbPercentage > 65.0
                val isCarbTotalTooLow = nutritionTotals.totalCarbohydrate < 130.0

                // Protein threshold calculation
                val proteinPercentage = if (nutritionTotals.totalCalories > 0) {
                    (nutritionTotals.totalProtein * 4 / nutritionTotals.totalCalories.toDouble()) * 100
                } else {
                    0.0
                }
                val isProteinTooLow = proteinPercentage < 10.0
                val isProteinTooHigh = proteinPercentage > 35.0

                // Fat threshold calculation
                val fatPercentage = if (nutritionTotals.totalCalories > 0) {
                    (nutritionTotals.totalFat * 9 / nutritionTotals.totalCalories.toDouble()) * 100
                } else {
                    0.0
                }
                val isFatTooLow = fatPercentage < 20.0
                val isFatTooHigh = fatPercentage > 25.0

                // Sugar threshold calculation
                val isSugarTooHigh = nutritionTotals.totalSugar > 50.0

                // Calories threshold calculation
                val isCaloriesTooLow = recommendedCalories > 0 && nutritionTotals.totalCalories < (recommendedCalories * 0.9)
                val isCaloriesTooHigh = recommendedCalories > 0 && nutritionTotals.totalCalories > (recommendedCalories * 1.1)

                // Serat threshold calculation
                val isFiberTooLow = nutritionTotals.totalFiber < 20.0
                val isFiberTooHigh = nutritionTotals.totalFiber > 35.0

                // Calories Warning Alert
                if (isCaloriesTooLow || isCaloriesTooHigh) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "Peringatan Kalori",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    if (isCaloriesTooLow) {
                                        Text(
                                            text = "Konsumsi kalori Anda terlalu rendah (${nutritionTotals.totalCalories.toInt()} kcal). " +
                                                    "Dianjurkan sekitar $recommendedCalories kcal per hari berdasarkan berat badan ideal Anda.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    } else if (isCaloriesTooHigh) {
                                        Text(
                                            text = "Konsumsi kalori Anda terlalu tinggi (${nutritionTotals.totalCalories.toInt()} kcal). " +
                                                    "Dianjurkan sekitar $recommendedCalories kcal per hari berdasarkan berat badan ideal Anda.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Carbohidrat Warning Alert
                if (isCarbPercentageTooLow || isCarbPercentageTooHigh || isCarbTotalTooLow) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "Peringatan Karbohidrat",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))


                                    if (isCarbTotalTooLow) {
                                        Text(
                                            text = "Konsumsi karbohidrat Anda kurang dari 130g/hari (${String.format(
                                                "%.1f",
                                                nutritionTotals.totalCarbohydrate
                                            )}g).",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }

                                    if (isCarbPercentageTooLow || isCarbPercentageTooHigh) {
                                        Text(
                                            text = when {
                                                isCarbPercentageTooLow -> "Persentase karbohidrat terlalu rendah (${
                                                    String.format(
                                                        "%.1f",
                                                        carbPercentage
                                                    )
                                                }%). Dianjurkan: 45-65% dari total energi."
                                                else -> "Persentase karbohidrat terlalu tinggi (${
                                                    String.format(
                                                        "%.1f",
                                                        carbPercentage
                                                    )
                                                }%). Dianjurkan: 45-65% dari total energi."
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Protein Warning Alert
                if (isProteinTooLow || isProteinTooHigh) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "Peringatan Protein",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    if (isProteinTooLow) {
                                        Text(
                                            text = "Konsumsi protein Anda terlalu rendah (${String.format(
                                                "%.1f",
                                                proteinPercentage
                                            )}%). Dianjurkan: 10-35% dari total kalori.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    } else if (isProteinTooHigh) {
                                        Text(
                                            text = "Konsumsi protein Anda terlalu tinggi (${String.format(
                                                "%.1f",
                                                proteinPercentage
                                            )}%). Dianjurkan: 10-35% dari total kalori.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Fat Warning Alert
                if (isFatTooLow || isFatTooHigh) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "Peringatan Lemak",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    if (isFatTooLow) {
                                        Text(
                                            text = "Konsumsi lemak Anda terlalu rendah (${String.format(
                                                "%.1f",
                                                fatPercentage
                                            )}%). Dianjurkan: 20-25% dari total kalori.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    } else if (isFatTooHigh) {
                                        Text(
                                            text = "Konsumsi lemak Anda terlalu tinggi (${String.format(
                                                "%.1f",
                                                fatPercentage
                                            )}%). Dianjurkan: 20-25% dari total kalori.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Sugar Warning Alert
                if (isSugarTooHigh) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "Peringatan Gula",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Konsumsi gula Anda terlalu tinggi (${String.format(
                                            "%.1f",
                                            nutritionTotals.totalSugar
                                        )}g). Dianjurkan: maksimal 50g per hari.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Serat Warning Alert
                if (isFiberTooLow || isFiberTooHigh) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "Peringatan Serat",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = when {
                                            isFiberTooLow -> "Konsumsi serat Anda terlalu rendah (${String.format("%.1f", nutritionTotals.totalFiber)}g). " +
                                                    "Dianjurkan 20-35g serat per hari."
                                            else -> "Konsumsi serat Anda terlalu tinggi (${String.format("%.1f", nutritionTotals.totalFiber)}g). " +
                                                    "Dianjurkan 20-35g serat per hari."
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Nutrition Summary Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
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
                                NutrientSummary(
                                    name = "Kalori",
                                    value = "${nutritionTotals.totalCalories.toInt()} kcal",
                                    isWarning = isCaloriesTooLow || isCaloriesTooHigh
                                )
                                NutrientSummary(
                                    name = "Karbohidrat",
                                    value = "${String.format("%.1f", nutritionTotals.totalCarbohydrate)}g",
                                    isWarning = isCarbPercentageTooLow || isCarbPercentageTooHigh || isCarbTotalTooLow
                                )
                                NutrientSummary(
                                    name = "Protein",
                                    value = "${String.format("%.1f", nutritionTotals.totalProtein)}g",
                                    isWarning = isProteinTooLow || isProteinTooHigh
                                )
                                NutrientSummary(
                                    name = "Lemak",
                                    value = "${String.format("%.1f", nutritionTotals.totalFat)}g",
                                    isWarning = isFatTooLow || isFatTooHigh
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Detailed Nutrition Breakdown
                            Column {
                                NutritionDetailRow("Lemak Jenuh", nutritionTotals.totalSaturatedFat)
                                NutritionDetailRow(
                                    "Lemak Tak Jenuh Ganda",
                                    nutritionTotals.totalPolyunsaturatedFat
                                )
                                NutritionDetailRow(
                                    "Lemak Tak Jenuh Tunggal",
                                    nutritionTotals.totalMonounsaturatedFat
                                )
                                NutritionDetailRow("Kolesterol", nutritionTotals.totalCholesterol)
                                NutritionDetailRow("Sodium", nutritionTotals.totalSodium)
                                NutritionDetailRow("Kalium", nutritionTotals.totalPotassium)
                                NutritionDetailRow("Serat", nutritionTotals.totalFiber, isWarning = isFiberTooLow || isFiberTooHigh)
                                NutritionDetailRow("Gula", nutritionTotals.totalSugar, isWarning = isSugarTooHigh)
                                NutritionDetailRow("Vitamin A", nutritionTotals.totalVitaminA)
                                NutritionDetailRow("Vitamin C", nutritionTotals.totalVitaminC)
                                NutritionDetailRow("Kalsium", nutritionTotals.totalCalcium)
                                NutritionDetailRow("Zat Besi", nutritionTotals.totalIron)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Energy distribution
                            if (nutritionTotals.totalCalories > 0) {
                                val carbEnergyPercentage =
                                    (nutritionTotals.totalCarbohydrate * 4 / nutritionTotals.totalCalories.toDouble()) * 100
                                val proteinEnergyPercentage =
                                    (nutritionTotals.totalProtein * 4 / nutritionTotals.totalCalories.toDouble()) * 100
                                val fatEnergyPercentage =
                                    (nutritionTotals.totalFat * 9 / nutritionTotals.totalCalories.toDouble()) * 100

                                Text(
                                    text = "Distribusi Energi",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MacroPercentage(

                                        name = "Karbohidrat",
                                        percentage = carbEnergyPercentage,
                                        isWarning = isCarbPercentageTooLow || isCarbPercentageTooHigh,
                                        recommendedRange = "45-65%"
                                    )
                                    MacroPercentage(
                                        name = "Protein",
                                        percentage = proteinEnergyPercentage,
                                        isWarning = isProteinTooLow || isProteinTooHigh,
                                        recommendedRange = "10-35%"
                                    )
                                    MacroPercentage(
                                        name = "Lemak",
                                        percentage = fatEnergyPercentage,
                                        isWarning = isFatTooLow || isFatTooHigh,
                                        recommendedRange = "20-25%"
                                    )
                                }

                                // Add recommended calorie information
                                if (recommendedCalories > 0) {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Kebutuhan Kalori Harian",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Rekomendasi: $recommendedCalories kcal",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        Text(
                                            text = "Konsumsi: ${nutritionTotals.totalCalories.toInt()} kcal",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCaloriesTooLow || isCaloriesTooHigh)
                                                MaterialTheme.colorScheme.error
                                            else
                                                Color.Unspecified
                                        )
                                    }

                                    if (isCaloriesTooLow || isCaloriesTooHigh) {
                                        Text(
                                            text = when {
                                                isCaloriesTooLow -> "Konsumsi kalori Anda terlalu rendah"
                                                else -> "Konsumsi kalori Anda terlalu tinggi"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                // Add sugar limit information
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Batas Konsumsi Gula",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Maksimal: 50g/hari",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "Konsumsi: ${String.format("%.1f", nutritionTotals.totalSugar)}g",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSugarTooHigh)
                                            MaterialTheme.colorScheme.error
                                        else
                                            Color.Unspecified
                                    )
                                }

                                if (isSugarTooHigh) {
                                    Text(
                                        text = "Konsumsi gula Anda melebihi batas harian",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // List of consumptions grouped by meal type
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
                            consumption.servingIndex < foodDetail.servings.size
                        ) {
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

// Helper function to calculate ideal weight based on the Broca formula
fun calculateIdealWeight(heightCm: Int, gender: String): Double {
    return if ((gender.equals("perempuan", ignoreCase = true) && heightCm < 150) ||
        (gender.equals("laki-laki", ignoreCase = true) && heightCm < 160)) {
        (heightCm - 100).toDouble()
    } else {
        0.9 * (heightCm - 100)
    }
}

// Helper function to calculate recommended calories
fun calculateRecommendedCalories(idealWeight: Double, currentWeight: Int): Int {
    val isObese = currentWeight > (idealWeight * 1.1)

    // For obese patients, reduce by 500 calories from normal recommendation
    val baseCalories = (idealWeight * 30).toInt() // 30 calories per kg of ideal weight

    return if (isObese) {
        baseCalories - 500
    } else {
        baseCalories
    }
}

// Model class untuk menyimpan total nutrisi
data class NutritionTotals(
    val totalCalories: Int = 0,
    val totalCarbohydrate: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalSaturatedFat: Double = 0.0,
    val totalPolyunsaturatedFat: Double = 0.0,
    val totalMonounsaturatedFat: Double = 0.0,
    val totalCholesterol: Double = 0.0,
    val totalSodium: Double = 0.0,
    val totalPotassium: Double = 0.0,
    val totalFiber: Double = 0.0,
    val totalSugar: Double = 0.0,
    val totalVitaminA: Double = 0.0,
    val totalVitaminC: Double = 0.0,
    val totalCalcium: Double = 0.0,
    val totalIron: Double = 0.0
)

// Function untuk menghitung total nutrisi
fun calculateNutritionTotals(
    consumptions: List<DailyConsumption>,
    foodDetailsCache: Map<String, FoodDetail>
): NutritionTotals {
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

    consumptions.forEach { consumption ->
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

    return NutritionTotals(
        totalCalories,
        totalCarbohydrate,
        totalProtein,
        totalFat,
        totalSaturatedFat,
        totalPolyunsaturatedFat,
        totalMonounsaturatedFat,
        totalCholesterol,
        totalSodium,
        totalPotassium,
        totalFiber,
        totalSugar,
        totalVitaminA,
        totalVitaminC,
        totalCalcium,
        totalIron
    )
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
fun NutrientSummary(
    name: String,
    value: String,
    isWarning: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isWarning) MaterialTheme.colorScheme.error else Color.Unspecified
        )
    }
}

@Composable
fun MacroPercentage(
    name: String,
    percentage: Double,
    isWarning: Boolean = false,
    recommendedRange: String = ""
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${String.format("%.1f", percentage)}%",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isWarning) MaterialTheme.colorScheme.error else Color.Unspecified
        )
        if (recommendedRange.isNotEmpty()) {
            Text(
                text = recommendedRange,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun NutritionDetailRow(label: String, value: Double, isWarning: Boolean = false) {
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
            fontWeight = FontWeight.Bold,
            color = if (isWarning) MaterialTheme.colorScheme.error else Color.Unspecified
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