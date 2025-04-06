package com.proyek.eatright.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyek.eatright.data.model.FoodDetail
import com.proyek.eatright.data.model.Serving
import com.proyek.eatright.viewmodel.ConsumptionState
import com.proyek.eatright.viewmodel.ConsumptionViewModel
import com.proyek.eatright.viewmodel.FoodDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FoodDetailScreen(
    foodId: String,
    onBack: () -> Unit,
    viewModel: FoodDetailViewModel = viewModel(),
    consumptionViewModel: ConsumptionViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val isLoading = viewModel.isLoading.value
    val foodDetail = viewModel.foodDetail.value
    val selectedServingIndex = viewModel.selectedServingIndex.value
    val consumptionState by consumptionViewModel.consumptionState.collectAsState()

    // Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedQuantity by remember { mutableStateOf(1) }
    var selectedMealType by remember { mutableStateOf("Lainnya") }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(foodId) {
        viewModel.loadFoodDetails(foodId)
    }

    // Effect to show snackbar when consumption state changes
    LaunchedEffect(consumptionState) {
        when (consumptionState) {
            is ConsumptionState.Success -> {
                snackbarHostState.showSnackbar(
                    (consumptionState as ConsumptionState.Success).message
                )
                consumptionViewModel.resetState()
            }
            is ConsumptionState.Error -> {
                snackbarHostState.showSnackbar(
                    (consumptionState as ConsumptionState.Error).message
                )
                consumptionViewModel.resetState()
            }
            else -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detail Makanan") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (!isLoading && foodDetail != null && foodDetail.servings.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = { showAddDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah ke Konsumsi")
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
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

                // Add to consumption dialog
                if (showAddDialog && foodDetail != null && foodDetail.servings.isNotEmpty()) {
                    val selectedServing = foodDetail.servings[selectedServingIndex]

                    AddConsumptionDialog(
                        serving = selectedServing,
                        quantity = selectedQuantity,
                        onQuantityChange = { selectedQuantity = it },
                        mealType = selectedMealType,
                        onMealTypeChange = { selectedMealType = it },
                        onDismiss = { showAddDialog = false },
                        onConfirm = {
                            coroutineScope.launch {
                                consumptionViewModel.addToConsumption(
                                    foodId = foodId,
                                    servingIndex = selectedServingIndex,
                                    quantity = selectedQuantity,
                                    mealType = selectedMealType
                                )
                                showAddDialog = false
                            }
                        }
                    )
                }
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
                items(foodDetail.servings) { serving ->
                    val index = foodDetail.servings.indexOf(serving)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServingChip(
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.padding(end = 8.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Text(
            text = description,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun NutritionInfo(serving: Serving) {
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
            // Nutrition Header with calories and serving description
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
                        .fillMaxWidth(if (1f - carbPercentage.toFloat() > 0)
                            proteinPercentage.toFloat() / (1f - carbPercentage.toFloat()) else 0f)
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

@Composable
fun AddConsumptionDialog(
    serving: Serving,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    mealType: String,
    onMealTypeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val mealTypes = listOf("Sarapan", "Makan Siang", "Makan Malam", "Camilan", "Lainnya")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Tambahkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Tambah ke Konsumsi Harian") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Tambah ke Konsumsi Harian",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Porsi: ${serving.servingDescription}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Jumlah Porsi:",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quantity selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) onQuantityChange(quantity - 1) }
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Kurangi", modifier = Modifier.rotate(45f))
                    }

                    Text(
                        text = quantity.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.titleLarge
                    )

                    IconButton(
                        onClick = { onQuantityChange(quantity + 1) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Jenis Makan:",
                    style = MaterialTheme.typography.bodyLarge
                )

                // Meal type selector
                Column(Modifier.fillMaxWidth()) {
                    mealTypes.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (type == mealType),
                                onClick = { onMealTypeChange(type) }
                            )
                            Text(
                                text = type,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nutrient calculation based on quantity
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
                            text = "Total Nutrisi (${quantity}x porsi):",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroLegendItem(
                                color = Color(0xFF5DADE2),
                                name = "Karbohidrat",
                                value = serving.carbohydrate * quantity,
                                unit = "g"
                            )
                            MacroLegendItem(
                                color = Color(0xFF58D68D),
                                name = "Protein",
                                value = serving.protein * quantity,
                                unit = "g"
                            )
                            MacroLegendItem(
                                color = Color(0xFFF1948A),
                                name = "Lemak",
                                value = serving.fat * quantity,
                                unit = "g"
                            )
                        }

                        Text(
                            text = "Total Kalori: ${serving.calories * quantity} kcal",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    )
}