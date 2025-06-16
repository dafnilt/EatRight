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
import com.proyek.eatright.ui.theme.Pink80
import com.proyek.eatright.ui.theme.Yellow
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
    onProfileClick: () -> Unit,
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

    // Collect current user data
    val currentUser by authViewModel.currentUser.collectAsState()

    // Calculate total nutritional values
    val totalCalories = userConsumptions.sumOf { it.nutritionalDetails.calories }
    val totalCarbs = userConsumptions.sumOf { it.nutritionalDetails.carbohydrate }
    val totalSugar = userConsumptions.sumOf { it.nutritionalDetails.sugar }
    val totalProtein = userConsumptions.sumOf { it.nutritionalDetails.protein }

    // Get user data from collected state
    val userHeight = currentUser?.tinggiBadan ?: 0 // Tinggi badan dalam cm
    val userGender = currentUser?.gender ?: ""
    val userWeight = currentUser?.beratBadan ?: 0 // Berat badan dalam kg

    // Calculate ideal weight and recommended calories
    val idealWeight = calculateIdealWeightInDashboard(userHeight, userGender)
    val recommendedCalories = calculateRecommendedCaloriesInDashboard(idealWeight, userWeight)

    // Calculate recommended nutrients based on calories
    val recommendedCarbsMin = (recommendedCalories * 0.45 / 4).toInt()
    val recommendedCarbsMax = (recommendedCalories * 0.65 / 4).toInt()
    val recommendedSugarMax = 50 // WHO recommendation: max 50g sugar per day

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
                    TopBarActionsInDashboard(
                        authViewModel = authViewModel,
                        onProfileClick = onProfileClick
                    )
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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Selamat datang!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )

                    Text(
                        text = currentUser?.nama ?: "User",
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

            Text(
                text = "Ayo capai kalori idealmu!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ideal Weight and Recommended Calories Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Ideal Weight Card
                HealthInfoCard(
                    title = "Berat Badan Ideal",
                    value = String.format("%.1f", idealWeight),
                    unit = "kg",
                    description = "",
                    modifier = Modifier.weight(1f),
                    cardColor = LightBlue.copy(alpha = 0.1f),
                    textColor = DarkBlue
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Recommended Calories Card
                HealthInfoCard(
                    title = "Kalori Harian",
                    value = recommendedCalories.toString(),
                    unit = "kkal",
                    description = "",
                    modifier = Modifier.weight(1f),
                    cardColor = DarkBlue2.copy(alpha = 0.1f),
                    textColor = DarkBlue2
                )
            }

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

            // Nutritional Cards with circular indicators and recommendations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Calories Card with circular indicator and recommendation
                CircularNutritionCardInDashboard(
                    title = "Kalori",
                    value = totalCalories.toInt().toString(),
                    unit = "kkal",
                    color = DarkBlue,
                    progress = if (recommendedCalories > 0) {
                        (totalCalories.toFloat() / recommendedCalories.toFloat()).coerceIn(0f, 1f)
                    } else 0f,
                    recommendation = "\nTarget:\n ${recommendedCalories} kkal/hari",
                    modifier = Modifier.weight(1f)
                )


                Spacer(modifier = Modifier.width(8.dp))

                // Carbs Card with circular indicator and recommendation
                CircularNutritionCardInDashboard(
                    title = "Karbohidrat",
                    value = String.format("%.1f", totalCarbs),
                    unit = "g",
                    color = Yellow,
                    progress = if (recommendedCarbsMax > 0) (totalCarbs / recommendedCarbsMax).toFloat().coerceIn(0f, 1f) else 0f,
                    recommendation = "\nTarget:\n${recommendedCarbsMin}-${recommendedCarbsMax}g/hari",
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Sugar Card with circular indicator and recommendation
                CircularNutritionCardInDashboard(
                    title = "Gula",
                    value = String.format("%.1f", totalSugar),
                    unit = "g",
                    color = Pink80,
                    progress = (totalSugar / recommendedSugarMax).toFloat().coerceIn(0f, 1f),
                    recommendation = "\nMaks.\n${recommendedSugarMax}g/hari",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TopBarActionsInDashboard(
    authViewModel: AuthViewModel,
    onProfileClick: () -> Unit
) {
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
                text = { Text("Profile") },
                onClick = {
                    expanded = false
                    onProfileClick()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            DropdownMenuItem(
                text = { Text("Logout") },
                onClick = {
                    expanded = false
                    authViewModel.logout()
                }
            )
        }
    }
}

@Composable
fun HealthInfoCard(
    title: String,
    value: String,
    unit: String,
    description: String,
    modifier: Modifier = Modifier,
    cardColor: Color = LightBlue.copy(alpha = 0.1f),
    textColor: Color = DarkBlue
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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Circular background untuk nilai
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(cardColor),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CircularNutritionCardInDashboard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    progress: Float,
    recommendation: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(180.dp), // Increased height to accommodate recommendation text
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            // Circular progress indicator with centered value
            Box(
                modifier = Modifier
                    .size(70.dp), // Slightly smaller to fit with recommendation
                contentAlignment = Alignment.Center
            ) {
                // Background circle
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f))
                )

                // Progress indicator - dinamis berdasarkan progress value
                CircularProgressIndicator(
                    progress = progress.coerceIn(0f, 1f), // Pastikan progress tidak lebih dari 1
                    modifier = Modifier.size(70.dp),
                    color = color,
                    strokeWidth = 6.dp,
                    trackColor = color.copy(alpha = 0.2f) // Tambahkan track color agar terlihat background lingkaran
                )

                // Value text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        fontSize = 12.sp
                    )
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black,
                        fontSize = 10.sp
                    )
                }
            }

            // Recommendation text with percentage
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Percentage text
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )

                Text(
                    text = recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )


            }
        }
    }
}

// Helper function to calculate ideal weight based on the Broca formula
fun calculateIdealWeightInDashboard(heightCm: Int, gender: String): Double {
    return if ((gender.equals("perempuan", ignoreCase = true) && heightCm < 150) ||
        (gender.equals("laki-laki", ignoreCase = true) && heightCm < 160)) {
        (heightCm - 100).toDouble()
    } else {
        0.9 * (heightCm - 100)
    }
}

// Helper function to calculate recommended calories
fun calculateRecommendedCaloriesInDashboard(idealWeight: Double, currentWeight: Int): Int {
    val isObese = currentWeight > (idealWeight * 1.1)

    // For obese patients, reduce by 500 calories from normal recommendation
    val baseCalories = (idealWeight * 30).toInt() // 30 calories per kg of ideal weight

    return if (isObese) {
        baseCalories - 500
    } else {
        baseCalories
    }
}