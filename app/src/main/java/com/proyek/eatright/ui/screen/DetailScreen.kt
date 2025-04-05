package com.proyek.eatright.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyek.eatright.data.model.FoodDetail
import com.proyek.eatright.data.model.Serving
import com.proyek.eatright.viewmodel.ConsumptionState
import com.proyek.eatright.viewmodel.ConsumptionViewModel
import com.proyek.eatright.viewmodel.FoodDetailViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    foodId: String,
    onBack: () -> Unit,
    foodDetailViewModel: FoodDetailViewModel = viewModel(),
    consumptionViewModel: ConsumptionViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val foodDetail by foodDetailViewModel.foodDetail.collectAsState()
    val isLoading by foodDetailViewModel.isLoading.collectAsState()
    val error by foodDetailViewModel.error.collectAsState()
    val consumptionState by consumptionViewModel.consumptionState.collectAsState()

    // Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedServing by remember { mutableStateOf<Pair<Int, Serving>?>(null) }
    var selectedQuantity by remember { mutableStateOf(1) }
    var selectedMealType by remember { mutableStateOf("Lainnya") }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    // Effect to fetch food details when screen is shown
    LaunchedEffect(foodId) {
        foodDetailViewModel.getFoodDetails(foodId)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Makanan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = "Error: ${error ?: "Unknown error"}",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                foodDetail != null -> {
                    val food = foodDetail!!

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Text(
                                text = food.foodName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Jenis: ${food.foodType}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Informasi Nutrisi",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(food.servings.withIndex().toList()) { (index, serving) ->
                            ServingCard(
                                serving = serving,
                                onClick = {
                                    selectedServing = Pair(index, serving)
                                    showAddDialog = true
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // FAB to add consumption
                    FloatingActionButton(
                        onClick = {
                            if (food.servings.isNotEmpty()) {
                                selectedServing = Pair(0, food.servings[0])
                                showAddDialog = true
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah ke Konsumsi")
                    }
                }
            }

            // Add to consumption dialog
            if (showAddDialog && selectedServing != null) {
                AddConsumptionDialog(
                    serving = selectedServing!!.second,
                    quantity = selectedQuantity,
                    onQuantityChange = { selectedQuantity = it },
                    mealType = selectedMealType,
                    onMealTypeChange = { selectedMealType = it },
                    onDismiss = { showAddDialog = false },
                    onConfirm = {
                        coroutineScope.launch {
                            consumptionViewModel.addToConsumption(
                                foodId = foodId,
                                servingIndex = selectedServing!!.first,
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

@Composable
fun ServingCard(
    serving: Serving,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = serving.servingDescription,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutrientInfo(name = "Kalori", value = "${serving.calories} kcal")
                NutrientInfo(name = "Protein", value = "${serving.protein}g")
                NutrientInfo(name = "Karbohidrat", value = "${serving.carbohydrate}g")
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutrientInfo(name = "Lemak", value = "${serving.fat}g")
                NutrientInfo(name = "Gula", value = "${serving.sugar}g")
                NutrientInfo(name = "Serat", value = "${serving.fiber}g")
            }
        }
    }
}

@Composable
fun NutrientInfo(name: String, value: String) {
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
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
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
                    Button(
                        onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("-")
                    }

                    Text(
                        text = quantity.toString(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Button(
                        onClick = { onQuantityChange(quantity + 1) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+")
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
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (type == mealType),
                                    onClick = { onMealTypeChange(type) }
                                )
                                .padding(vertical = 8.dp),
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
                            NutrientInfo(name = "Kalori", value = "${serving.calories * quantity} kcal")
                            NutrientInfo(name = "Protein", value = "${String.format("%.1f", serving.protein * quantity)}g")
                            NutrientInfo(name = "Karbo", value = "${String.format("%.1f", serving.carbohydrate * quantity)}g")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = onConfirm) {
                        Text("Tambahkan")
                    }
                }
            }
        }
    }
}