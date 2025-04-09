package com.proyek.eatright.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyek.eatright.data.model.DailyConsumption
import com.proyek.eatright.data.model.Food
import com.proyek.eatright.ui.theme.DarkBlue
import com.proyek.eatright.ui.theme.DarkBlue2
import com.proyek.eatright.ui.theme.LightBlue
import com.proyek.eatright.ui.theme.LightBlue2
import com.proyek.eatright.viewmodel.AuthViewModel
import com.proyek.eatright.viewmodel.ConsumptionViewModel
import com.proyek.eatright.viewmodel.FoodSearchViewModel
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onFoodClick: (String) -> Unit,
    onConsumptionSummaryClick: () -> Unit,
    viewModel: FoodSearchViewModel,
    authViewModel: AuthViewModel,
    consumptionViewModel: ConsumptionViewModel
) {
    val searchQuery = remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasSearched = viewModel.hasSearched.collectAsState()
    val scrollState = rememberScrollState()

    // Collect consumption data
    val userConsumptions by consumptionViewModel.userConsumptions.collectAsState()

    // Calculate total nutritional values
    val totalCalories = userConsumptions.sumOf { it.nutritionalDetails.calories }
    val totalCarbs = userConsumptions.sumOf { it.nutritionalDetails.carbohydrate }
    val totalSugar = userConsumptions.sumOf { it.nutritionalDetails.sugar }
    val totalProtein = userConsumptions.sumOf { it.nutritionalDetails.protein }

    // Load user consumptions when screen is first displayed
    LaunchedEffect(Unit) {
        consumptionViewModel.loadUserConsumptions()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    // Empty or add icon if needed
                },
                actions = {
                    TopBarActions(authViewModel = authViewModel)
                    Spacer(modifier = Modifier.width(16.dp))
                }

            )
        },

        ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Greeting section
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Selamat datang!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    val userName = authViewModel.currentUser.value?.nama ?: "User"
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Health message
            Text(
                text = "Penting menjaga kesehatan setiap hari dengan menjaga pola makan yang sesuai.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))


            // Nutrition cards section with circular indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nutrisi Harian",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Format current date
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                val currentDate = dateFormat.format(Date())

                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Nutritional Cards with circular indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Calories Card with circular indicator
                CircularNutritionCard(
                    title = "Kalori",
                    value = totalCalories.toInt().toString(),
                    unit = "kkal",
                    color = LightBlue,
                    progress = 0.65f,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Carbs Card with circular indicator
                CircularNutritionCard(
                    title = "Karbohidrat",
                    value = String.format("%.1f", totalCarbs),
                    unit = "g",
                    color = LightBlue2,
                    progress = 0.45f,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Sugar Card with circular indicator
                CircularNutritionCard(
                    title = "Gula",
                    value = String.format("%.1f", totalSugar),
                    unit = "g",
                    color = DarkBlue.copy(alpha = 0.7f),
                    progress = 0.3f,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}

@Composable
fun TopBarActions(authViewModel: AuthViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
    ) {
        IconButton(
            onClick = { expanded = true }
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "User Menu",
                tint = DarkBlue
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Logout") },
                onClick = {
                    expanded = false
                    authViewModel.logout()
                    // Navigation will be triggered from AppNavigation
                }
            )
        }
    }
}

@Composable
fun CircularNutritionCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Circular progress indicator with centered value
            Box(
                modifier = Modifier
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f))
                )

                // Progress indicator
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(80.dp),
                    color = color,
                    strokeWidth = 8.dp
                )

                // Value text
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

