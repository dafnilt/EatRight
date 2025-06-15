package com.proyek.eatright.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyek.eatright.R
import com.proyek.eatright.data.model.User
import com.proyek.eatright.ui.theme.DarkBlue
import com.proyek.eatright.ui.theme.LightBlue
import com.proyek.eatright.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

private val PrimaryPurple = Color(0xFF6366F1)
private val LightPurple = Color(0xFFE0E7FF)
private val VeryLightPurple = Color(0xFFF8FAFF)
private val CardBackground = Color(0xFFF1F5F9)
private val TextDark = Color(0xFF1E293B)
private val TextGray = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .background(VeryLightPurple)
        ) {
            currentUser?.let { user ->
                ProfileHeader(user = user)
                ProfileInfoSection(user = user)

            } ?: run {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: User) { Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VeryLightPurple)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(LightPurple),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(40.dp),
                tint = PrimaryPurple
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.nama.ifEmpty { "" },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ProfileInfoSection(user: User) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {

        // Personal Info Cards
        ProfileInfoCard(
            icon = Icons.Default.Phone,
            title = "No. Telepon",
            value = user.noTelp.ifEmpty { "Belum diisi" }
        )

        ProfileInfoCard(
            icon = Icons.Default.DateRange,
            title = "Tanggal Lahir",
            value = user.tanggalLahir.ifEmpty { "Belum diisi" }
        )

        ProfileInfoCard(
            icon = Icons.Default.Person,
            title = "Jenis Kelamin",
            value = when (user.gender.lowercase()) {
                "l", "laki-laki", "male" -> "Laki-laki"
                "p", "perempuan", "female" -> "Perempuan"
                else -> user.gender.ifEmpty { "Belum diisi" }
            }
        )

        // Health Info Section
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Height Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tinggi Badan",
                        fontSize = 12.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (user.tinggiBadan > 0) "${user.tinggiBadan} cm" else "- cm",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }

            // Weight Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Berat Badan",
                        fontSize = 12.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (user.beratBadan > 0) "${user.beratBadan} kg" else "- kg",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }
        }

        // BMI Card if both height and weight are available
        if (user.tinggiBadan > 0 && user.beratBadan > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            val bmi = calculateBMI(user.beratBadan, user.tinggiBadan)
            val bmiCategory = getBMICategory(bmi)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Indeks Massa Tubuh (BMI)",
                        fontSize = 12.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = String.format("%.1f", bmi),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                    Text(
                        text = bmiCategory,
                        fontSize = 14.sp,
                        color = getBMICategoryColor(bmi),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileInfoCard(
    icon: ImageVector,
    title: String,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryPurple,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = TextGray
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
            }
        }
    }
}

// Helper functions for BMI calculation
private fun calculateBMI(weight: Int, height: Int): Double {
    val heightInMeters = height / 100.0
    return weight / (heightInMeters * heightInMeters)
}

private fun getBMICategory(bmi: Double): String {
    return when {
        bmi < 18.5 -> "Kurus"
        bmi < 25.0 -> "Normal"
        bmi < 30.0 -> "Gemuk"
        else -> "Obesitas"
    }
}

private fun getBMICategoryColor(bmi: Double): Color {
    return when {
        bmi < 18.5 -> Color.Blue
        bmi < 25.0 -> Color.Green
        bmi < 30.0 -> Color.Yellow
        else -> Color.Red
    }
}