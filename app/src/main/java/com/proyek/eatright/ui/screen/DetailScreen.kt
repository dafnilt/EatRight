package com.proyek.eatright.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyek.eatright.data.model.FoodDetail
import com.proyek.eatright.data.model.Serving
import com.proyek.eatright.ui.theme.DarkBlue
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (!isLoading && foodDetail != null && foodDetail.servings.isNotEmpty()) {
                    FloatingActionButton(
                        containerColor = Color(0xFF6E66FA), // Warna yang sama dengan login button
                        contentColor = Color.White,
                        onClick = { showAddDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah ke Konsumsi")
                    }
                }
            },
            containerColor = Color.White
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
                        CircularProgressIndicator(color = Color(0xFF6E66FA))
                    }
                } else if (foodDetail == null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Detail tidak ditemukan")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadFoodDetails(foodId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF6E66FA)
                                )
                            ) {
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
            color = if (isSelected) Color(0xFF6E66FA) else Color.LightGray
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE6EEFF) else Color.White
        ),
        onClick = onClick
    ) {
        Text(
            text = description,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color(0xFF6E66FA) else Color.Black
        )
    }
}

@Composable
fun NutritionInfo(serving: Serving) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Macronutrients with circular visualization
            CircularMacronutrientSection(
                calories = serving.calories,
                carbs = serving.carbohydrate,
                protein = serving.protein,
                fat = serving.fat,
                servingDescription = serving.servingDescription
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Detailed nutrition in single column
            DetailedNutritionSection(serving = serving)
        }
    }
}

@Composable
fun CircularMacronutrientSection(
    calories: Int,
    carbs: Double,
    protein: Double,
    fat: Double,
    servingDescription: String
) {
    // Calculate total for percentages
    val total = carbs + protein + fat
    val carbPercentage = if (total > 0) carbs / total else 0.0
    val proteinPercentage = if (total > 0) protein / total else 0.0
    val fatPercentage = if (total > 0) fat / total else 0.0

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Text above the chart
        Text(
            text = "Per $servingDescription",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Circular chart with calories in center
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Draw circular arcs for each macronutrient
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 30f
                val radius = (size.minDimension - strokeWidth) / 2

                // Draw fat arc (red)
                drawArc(
                    color = Color(0xFFF1948A),
                    startAngle = 0f,
                    sweepAngle = (360f * fatPercentage).toFloat(),
                    useCenter = false,
                    topLeft = Offset(strokeWidth/2, strokeWidth/2),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                // Draw protein arc (green)
                drawArc(
                    color = Color(0xFF58D68D),
                    startAngle = (360f * fatPercentage).toFloat(),
                    sweepAngle = (360f * proteinPercentage).toFloat(),
                    useCenter = false,
                    topLeft = Offset(strokeWidth/2, strokeWidth/2),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                // Draw carbs arc (blue)
                drawArc(
                    color = Color(0xFF5DADE2),
                    startAngle = (360f * (fatPercentage + proteinPercentage)).toFloat(),
                    sweepAngle = (360f * carbPercentage).toFloat(),
                    useCenter = false,
                    topLeft = Offset(strokeWidth/2, strokeWidth/2),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
            }

            // Center calories text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(16.dp)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .padding(16.dp)
            ) {
                Text(
                    text = "$calories",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Kalori",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Macronutrient legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MacroLegendItem(
                color = Color(0xFF5DADE2),
                name = "Karbohidrat",
                value = carbs,
                unit = "g",
                percentage = carbPercentage * 100
            )
            MacroLegendItem(
                color = Color(0xFF58D68D),
                name = "Protein",
                value = protein,
                unit = "g",
                percentage = proteinPercentage * 100
            )
            MacroLegendItem(
                color = Color(0xFFF1948A),
                name = "Lemak",
                value = fat,
                unit = "g",
                percentage = fatPercentage * 100
            )
        }
    }
}

@Composable
fun MacroLegendItem(
    color: Color,
    name: String,
    value: Double,
    unit: String,
    percentage: Double
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${String.format("%.1f", value)}$unit",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "(${String.format("%.0f", percentage)}%)",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun DetailedNutritionSection(serving: Serving) {
    Column {
        Text(
            text = "Informasi Nutrisi Detail",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // All nutrients in a single column
            NutrientItem("Lemak Jenuh", serving.saturatedFat, "g")
            NutrientItem("Lemak T.J. Ganda", serving.polyunsaturatedFat, "g")
            NutrientItem("Lemak T.J. Tunggal", serving.monounsaturatedFat, "g")
            NutrientItem("Kolesterol", serving.cholesterol, "mg")
            NutrientItem("Sodium", serving.sodium, "mg")
            NutrientItem("Kalium", serving.potassium, "mg")
            NutrientItem("Serat", serving.fiber, "g")
            NutrientItem("Gula", serving.sugar, "g")
            NutrientItem("Vitamin A", serving.vitaminA, "IU")
            NutrientItem("Vitamin C", serving.vitaminC, "mg")
            NutrientItem("Kalsium", serving.calcium, "mg")
            NutrientItem("Zat Besi", serving.iron, "mg")
        }
    }
}

@Composable
fun NutrientItem(label: String, value: Double, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
        )
        Text(
            text = "${String.format("%.1f", value)} $unit",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)  // Menggunakan hampir seluruh lebar layar
                .padding(horizontal = 4.dp, vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tambah ke Konsumsi Hari Ini",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tutup",
                            modifier = Modifier.rotate(45f),
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Portion info row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Portion icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF6E66FA), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add, // Ganti dengan icon porsi yang sesuai jika ada
                            contentDescription = "Porsi",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Portion description
                    Column {
                        Text(
                            text = "Porsi makanan",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = serving.servingDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quantity selector section
                Text(
                    text = "Jumlah Porsi",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Redesigned quantity selector - horizontal buttons like in image
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Minus button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6E66FA))
                            .clickable(enabled = quantity > 1) {
                                if (quantity > 1) onQuantityChange(quantity - 1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "-",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Quantity display
                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        fontSize = 22.sp
                    )

                    // Plus button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6E66FA))
                            .clickable { onQuantityChange(quantity + 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Meal type section
                Text(
                    text = "Jenis Makan",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Meal type list - vertical layout
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val mealTypes = listOf("Sarapan", "Makan Siang", "Makan Malam", "Camilan")
                    mealTypes.forEach { type ->
                        MealTypeOption(
                            text = type,
                            selected = type == mealType,
                            onClick = { onMealTypeChange(type) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Confirm button
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6E66FA)
                    )
                ) {
                    Text(
                        "Tambahkan ke Konsumsi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MealTypeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF6E66FA) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = if (selected) Color.White else Color(0xFF6E66FA),
                unselectedColor = if (selected) Color.White.copy(alpha = 0.7f) else Color.Gray
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            color = if (selected) Color.White else Color(0xFF333333)
        )
    }
}

@Composable
fun ImprovedMacroSummaryItem(
    name: String,
    value: Double,
    unit: String,
    color: Color,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular background for icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Colored circle in center
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Value with unit
        Text(
            text = String.format("%.1f", value),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )

        // Unit and name
        Text(
            text = "$unit $name",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun MacroSummaryItem(
    name: String,
    value: Double,
    unit: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${String.format("%.1f", value)}$unit",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}