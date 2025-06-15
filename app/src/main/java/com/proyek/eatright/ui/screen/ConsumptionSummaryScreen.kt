package com.proyek.eatright.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyek.eatright.data.model.DailyConsumption
import com.proyek.eatright.data.model.FoodDetail
import com.proyek.eatright.data.model.Serving
import com.proyek.eatright.ui.theme.LightBlue2
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

    // Load consumptions when date changes
    LaunchedEffect(selectedDate.value) {
        viewModel.loadUserConsumptions(selectedDate.value)
    }

    // Show snackbar when consumption state changes
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
                title = {
                    Text(
                        "Konsumsi Harian",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Date navigation header
            item {
                DateNavigationHeader(
                    selectedDate = selectedDate.value,
                    dateFormat = dateFormat,
                    onPreviousDay = {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = selectedDate.value
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                        selectedDate.value = calendar.timeInMillis
                    },
                    onNextDay = {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = selectedDate.value
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        selectedDate.value = calendar.timeInMillis
                    }
                )
            }

            // Loading indicator
            if (consumptionState is com.proyek.eatright.viewmodel.ConsumptionState.Loading) {
                item { LoadingIndicator() }
            }

            // Empty state
            if (userConsumptions.isEmpty() && consumptionState !is com.proyek.eatright.viewmodel.ConsumptionState.Loading) {
                item { EmptyState() }
            } else if (userConsumptions.isNotEmpty()) {
                // Calculate all nutrition data and thresholds
                val nutritionData = calculateNutritionData(
                    userConsumptions,
                    foodDetailsCache,
                    currentUser?.tinggiBadan ?: 0,
                    currentUser?.gender ?: "",
                    currentUser?.beratBadan ?: 0
                )

                // Display warnings in a dropdown
                item {
                    WarningsDropdown(nutritionData = nutritionData)
                }

                // Nutrition summary card
                item {
                    NutritionSummaryCard(nutritionData = nutritionData)
                }

                // Consumptions grouped by meal type
                displayConsumptionsByMealType(
                    userConsumptions = userConsumptions,
                    foodDetailsCache = foodDetailsCache,
                    onDeleteConsumption = { consumptionId ->
                        coroutineScope.launch {
                            viewModel.deleteConsumption(consumptionId)
                        }
                    },
                    item = { content -> item { content() } },
                    items = { items, content -> items(items) { content(it) } }
                )
            }
        }
    }
}

@Composable
fun DateNavigationHeader(
    selectedDate: Long,
    dateFormat: SimpleDateFormat,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousDay) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Previous Day"
            )
        }

        Text(
            text = dateFormat.format(Date(selectedDate)),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextDay) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Next Day"
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState() {
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

// Data class to hold all nutrition calculations and thresholds
data class NutritionData(
    val totals: NutritionTotals,
    val idealWeight: Double,
    val recommendedCalories: Int,
    val carbPercentage: Double,
    val proteinPercentage: Double,
    val fatPercentage: Double,
    val thresholds: NutritionThresholds
)

data class NutritionThresholds(
    val isCarbPercentageTooLow: Boolean,
    val isCarbPercentageTooHigh: Boolean,
    val isProteinTooLow: Boolean,
    val isProteinTooHigh: Boolean,
    val isFatTooLow: Boolean,
    val isFatTooHigh: Boolean,
    val isSugarTooHigh: Boolean,
    val isCaloriesTooLow: Boolean,
    val isCaloriesTooHigh: Boolean,
    val isFiberTooLow: Boolean,
    val isFiberTooHigh: Boolean
)

// Calculate all nutrition data including thresholds
fun calculateNutritionData(
    userConsumptions: List<DailyConsumption>,
    foodDetailsCache: Map<String, FoodDetail>,
    userHeight: Int,
    userGender: String,
    userWeight: Int
): NutritionData {
    val nutritionTotals = calculateNutritionTotals(userConsumptions, foodDetailsCache)
    val idealWeight = calculateIdealWeight(userHeight, userGender)
    val recommendedCalories = calculateRecommendedCalories(idealWeight, userWeight)

    // Calculate percentages
    val carbPercentage = if (nutritionTotals.totalCalories > 0) {
        (nutritionTotals.totalCarbohydrate * 4 / nutritionTotals.totalCalories.toDouble()) * 100
    } else 0.0

    val proteinPercentage = if (nutritionTotals.totalCalories > 0) {
        (nutritionTotals.totalProtein * 4 / nutritionTotals.totalCalories.toDouble()) * 100
    } else 0.0

    val fatPercentage = if (nutritionTotals.totalCalories > 0) {
        (nutritionTotals.totalFat * 9 / nutritionTotals.totalCalories.toDouble()) * 100
    } else 0.0

    // Calculate thresholds
    val thresholds = NutritionThresholds(
        isCarbPercentageTooLow = nutritionTotals.totalCarbohydrate < (recommendedCalories * 0.45 / 4),
        isCarbPercentageTooHigh = nutritionTotals.totalCarbohydrate > (recommendedCalories * 0.65 / 4),
        isProteinTooLow = nutritionTotals.totalProtein < (recommendedCalories * 0.10 / 4),
        isProteinTooHigh = nutritionTotals.totalProtein > (recommendedCalories * 0.35 / 4),
        isFatTooLow = nutritionTotals.totalFat < (recommendedCalories * 0.20 / 9),
        isFatTooHigh = nutritionTotals.totalFat > (recommendedCalories * 0.25 / 9),
        isSugarTooHigh = nutritionTotals.totalSugar > 50.0,
        isCaloriesTooLow = recommendedCalories > 0 && nutritionTotals.totalCalories < (recommendedCalories * 0.9),
        isCaloriesTooHigh = recommendedCalories > 0 && nutritionTotals.totalCalories > (recommendedCalories * 1.1),
        isFiberTooLow = nutritionTotals.totalFiber < 20.0,
        isFiberTooHigh = nutritionTotals.totalFiber > 35.0
    )

    return NutritionData(
        totals = nutritionTotals,
        idealWeight = idealWeight,
        recommendedCalories = recommendedCalories,
        carbPercentage = carbPercentage,
        proteinPercentage = proteinPercentage,
        fatPercentage = fatPercentage,
        thresholds = thresholds
    )
}

// Display warnings in a dropdown
@Composable
fun WarningsDropdown(nutritionData: NutritionData) {
    val thresholds = nutritionData.thresholds

    // Count total warnings
    val warningCount = countWarnings(thresholds)

    // Only show dropdown if there are warnings
    if (warningCount > 0) {
        var expanded by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            )
        ) {
            // Dropdown header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warnings",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "$warningCount Peringatan Nutrisi",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = if (expanded) 90f else 270f }
                )
            }

            // Expanded content with warnings
            if (expanded) {
                Divider(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                    thickness = 1.dp
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    // Calories Warning
                    if (thresholds.isCaloriesTooLow || thresholds.isCaloriesTooHigh) {
                        CompactWarningItem(
                            title = "Kalori",
                            message = if (thresholds.isCaloriesTooLow)
                                "Terlalu rendah (${nutritionData.totals.totalCalories.toInt()} kcal). Dianjurkan: ${nutritionData.recommendedCalories} kcal/hari."
                            else
                                "Terlalu tinggi (${nutritionData.totals.totalCalories.toInt()} kcal). Dianjurkan: ${nutritionData.recommendedCalories} kcal/hari."
                        )
                    }

                    // Carbohydrate Warning
                    if (thresholds.isCarbPercentageTooLow || thresholds.isCarbPercentageTooHigh ) {
                        val recommendedCarbMin = (nutritionData.recommendedCalories * 0.45 / 4).toInt()
                        val recommendedCarbMax = (nutritionData.recommendedCalories * 0.65 / 4).toInt()

                        CompactWarningItem(
                            title = "Karbohidrat",
                            message = if (thresholds.isCarbPercentageTooLow)
                                "Terlalu rendah (${String.format("%.1f", nutritionData.totals.totalCarbohydrate)}g). Dianjurkan: ${recommendedCarbMin}-${recommendedCarbMax}g/hari."
                            else
                                "Terlalu tinggi (${String.format("%.1f", nutritionData.totals.totalCarbohydrate)}g). Dianjurkan: ${recommendedCarbMin}-${recommendedCarbMax}g/hari."
                        )
                    }

                    // Protein Warning
                    if (thresholds.isProteinTooLow || thresholds.isProteinTooHigh) {
                        val recommendedProteinMin = (nutritionData.recommendedCalories * 0.10 / 4).toInt()
                        val recommendedProteinMax = (nutritionData.recommendedCalories * 0.35 / 4).toInt()

                        CompactWarningItem(
                            title = "Protein",
                            message = if (thresholds.isProteinTooLow)
                                "Terlalu rendah (${String.format("%.1f", nutritionData.totals.totalProtein)}g). Dianjurkan: ${recommendedProteinMin}-${recommendedProteinMax}g/hari."
                            else
                                "Terlalu tinggi (${String.format("%.1f", nutritionData.totals.totalProtein)}g). Dianjurkan: ${recommendedProteinMin}-${recommendedProteinMax}g/hari."
                        )
                    }

                    // Fat Warning
                    if (thresholds.isFatTooLow || thresholds.isFatTooHigh) {
                        val recommendedFatMin = (nutritionData.recommendedCalories * 0.20 / 9).toInt()
                        val recommendedFatMax = (nutritionData.recommendedCalories * 0.25 / 9).toInt()

                        CompactWarningItem(
                            title = "Lemak",
                            message = if (thresholds.isFatTooLow)
                                "Terlalu rendah (${String.format("%.1f", nutritionData.totals.totalFat)}g). Dianjurkan: ${recommendedFatMin}-${recommendedFatMax}g/hari."
                            else
                                "Terlalu tinggi (${String.format("%.1f", nutritionData.totals.totalFat)}g). Dianjurkan: ${recommendedFatMin}-${recommendedFatMax}g/hari."
                        )
                    }

                    // Sugar Warning
                    if (thresholds.isSugarTooHigh) {
                        CompactWarningItem(
                            title = "Gula",
                            message = "Terlalu tinggi (${String.format("%.1f", nutritionData.totals.totalSugar)}g). Dianjurkan: maks. 50g/hari."
                        )
                    }

                    // Fiber Warning
                    if (thresholds.isFiberTooLow || thresholds.isFiberTooHigh) {
                        CompactWarningItem(
                            title = "Serat",
                            message = if (thresholds.isFiberTooLow)
                                "Terlalu rendah (${String.format("%.1f", nutritionData.totals.totalFiber)}g). Dianjurkan: 20-35g/hari."
                            else
                                "Terlalu tinggi (${String.format("%.1f", nutritionData.totals.totalFiber)}g). Dianjurkan: 20-35g/hari."
                        )
                    }
                }
            }
        }
    }
}

// Helper to count total warnings
fun countWarnings(thresholds: NutritionThresholds): Int {
    var count = 0

    // Count each warning category once
    if (thresholds.isCaloriesTooLow || thresholds.isCaloriesTooHigh) count++
    if (thresholds.isCarbPercentageTooLow || thresholds.isCarbPercentageTooHigh ) count++
    if (thresholds.isProteinTooLow || thresholds.isProteinTooHigh) count++
    if (thresholds.isFatTooLow || thresholds.isFatTooHigh) count++
    if (thresholds.isSugarTooHigh) count++
    if (thresholds.isFiberTooLow || thresholds.isFiberTooHigh) count++

    return count
}

@Composable
fun CompactWarningItem(title: String, message: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )

        Spacer(modifier = Modifier.height(4.dp))

        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// This component is no longer used, replaced by CompactWarningItem within WarningsDropdown
// Keeping the function signature for backwards compatibility if needed
@Composable
fun WarningCard(title: String, message: String) {
    CompactWarningItem(title = title, message = message)
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
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isWarning) MaterialTheme.colorScheme.error else Color.Black
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
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black
        )
        Text(
            text = "${String.format("%.1f", value)} ${getNutrientUnit(label)}",
            style = MaterialTheme.typography.bodySmall,
            color = if (isWarning) MaterialTheme.colorScheme.error else Color.Black
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
    val baseCalories = (idealWeight * 30).toInt() // 30 calories per kg of ideal weight
    return if (isObese) baseCalories - 500 else baseCalories
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
fun NutritionSummaryCard(nutritionData: NutritionData) {
    val totals = nutritionData.totals
    val thresholds = nutritionData.thresholds

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // opsional, biar card kelihatan lebih "naik"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Ringkasan Nutrisi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Macro nutrients summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutrientSummary(
                    name = "Kalori",
                    value = "${totals.totalCalories.toInt()} kcal",
                    isWarning = thresholds.isCaloriesTooLow || thresholds.isCaloriesTooHigh
                )
                NutrientSummary(
                    name = "Karbohidrat",
                    value = "${String.format("%.1f", totals.totalCarbohydrate)}g",
                    isWarning = thresholds.isCarbPercentageTooLow || thresholds.isCarbPercentageTooHigh
                )
                NutrientSummary(
                    name = "Protein",
                    value = "${String.format("%.1f", totals.totalProtein)}g",
                    isWarning = thresholds.isProteinTooLow || thresholds.isProteinTooHigh
                )
                NutrientSummary(
                    name = "Lemak",
                    value = "${String.format("%.1f", totals.totalFat)}g",
                    isWarning = thresholds.isFatTooLow || thresholds.isFatTooHigh
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detailed nutrition breakdown
            Column {
                NutritionDetailRow("Lemak Jenuh", totals.totalSaturatedFat)
                NutritionDetailRow("Lemak Tak Jenuh Ganda", totals.totalPolyunsaturatedFat)
                NutritionDetailRow("Lemak Tak Jenuh Tunggal", totals.totalMonounsaturatedFat)
                NutritionDetailRow("Kolesterol", totals.totalCholesterol)
                NutritionDetailRow("Sodium", totals.totalSodium)
                NutritionDetailRow("Kalium", totals.totalPotassium)
                NutritionDetailRow("Serat", totals.totalFiber, isWarning = thresholds.isFiberTooLow || thresholds.isFiberTooHigh)
                NutritionDetailRow("Gula", totals.totalSugar, isWarning = thresholds.isSugarTooHigh)
                NutritionDetailRow("Vitamin A", totals.totalVitaminA)
                NutritionDetailRow("Vitamin C", totals.totalVitaminC)
                NutritionDetailRow("Kalsium", totals.totalCalcium)
                NutritionDetailRow("Zat Besi", totals.totalIron)
            }

            // Energy distribution
//            if (totals.totalCalories > 0) {
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Text(
//                    text = "Distribusi Energi",
//                    style = MaterialTheme.typography.titleSmall,
//                    fontWeight = FontWeight.Bold
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    MacroPercentage(
//                        name = "Karbohidrat",
//                        percentage = nutritionData.carbPercentage,
//                        isWarning = thresholds.isCarbPercentageTooLow || thresholds.isCarbPercentageTooHigh,
//                        recommendedRange = "45-65%"
//                    )
//                    MacroPercentage(
//                        name = "Protein",
//                        percentage = nutritionData.proteinPercentage,
//                        isWarning = thresholds.isProteinTooLow || thresholds.isProteinTooHigh,
//                        recommendedRange = "10-35%"
//                    )
//                    MacroPercentage(
//                        name = "Lemak",
//                        percentage = nutritionData.fatPercentage,
//                        isWarning = thresholds.isFatTooLow || thresholds.isFatTooHigh,
//                        recommendedRange = "20-25%"
//                    )
//                }
//
//                // Recommended calorie information
//                if (nutritionData.recommendedCalories > 0) {
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        text = "Kebutuhan Kalori Harian",
//                        style = MaterialTheme.typography.titleSmall,
//                        fontWeight = FontWeight.Bold
//                    )
//
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = "Rekomendasi: ${nutritionData.recommendedCalories} kcal",
//                            style = MaterialTheme.typography.bodyMedium
//                        )
//
//                        Text(
//                            text = "Konsumsi: ${totals.totalCalories.toInt()} kcal",
//                            style = MaterialTheme.typography.bodyMedium,
//                            fontWeight = FontWeight.Bold,
//                            color = if (thresholds.isCaloriesTooLow || thresholds.isCaloriesTooHigh)
//                                MaterialTheme.colorScheme.error
//                            else
//                                Color.Unspecified
//                        )
//                    }
//
//                    if (thresholds.isCaloriesTooLow || thresholds.isCaloriesTooHigh) {
//                        Text(
//                            text = if (thresholds.isCaloriesTooLow)
//                                "Konsumsi kalori Anda terlalu rendah"
//                            else
//                                "Konsumsi kalori Anda terlalu tinggi",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.error
//                        )
//                    }
//                }
//
//                // Sugar limit information
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Text(
//                    text = "Batas Konsumsi Gula",
//                    style = MaterialTheme.typography.titleSmall,
//                    fontWeight = FontWeight.Bold
//                )
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "Maksimal: 50g/hari",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//
//                    Text(
//                        text = "Konsumsi: ${String.format("%.1f", totals.totalSugar)}g",
//                        style = MaterialTheme.typography.bodyMedium,
//                        fontWeight = FontWeight.Bold,
//                        color = if (thresholds.isSugarTooHigh)
//                            MaterialTheme.colorScheme.error
//                        else
//                            Color.Unspecified
//                    )
//                }
//
//                if (thresholds.isSugarTooHigh) {
//                    Text(
//                        text = "Konsumsi gula Anda melebihi batas harian",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.error
//                    )
//                }
//            }
        }
    }
}

// Function to display consumption items grouped by meal type
fun displayConsumptionsByMealType(
    userConsumptions: List<DailyConsumption>,
    foodDetailsCache: Map<String, FoodDetail>,
    onDeleteConsumption: (String) -> Unit,
    item: (@Composable () -> Unit) -> Unit,
    items: (List<DailyConsumption>, @Composable (DailyConsumption) -> Unit) -> Unit
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
                consumption.servingIndex < foodDetail.servings.size
            ) {
                foodDetail.servings[consumption.servingIndex]
            } else null

            ConsumptionItem(
                consumption = consumption,
                foodDetail = foodDetail,
                serving = serving,
                onDelete = { onDeleteConsumption(consumption.id) }
            )
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

                if (serving != null) {
                    Text(
                        text = "${consumption.quantity}x ${serving.servingDescription}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

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

