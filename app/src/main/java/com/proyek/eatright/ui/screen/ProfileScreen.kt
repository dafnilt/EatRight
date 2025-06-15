package com.proyek.eatright.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyek.eatright.R
import com.proyek.eatright.data.model.User
import com.proyek.eatright.ui.theme.DarkBlue
import com.proyek.eatright.ui.theme.LightBlue
import com.proyek.eatright.viewmodel.AuthViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.util.Log
import com.proyek.eatright.viewmodel.UpdateProfileState
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
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Edit form states - akan di-reset setiap kali masuk edit mode
    var editedName by remember { mutableStateOf("") }
    var editedEmail by remember { mutableStateOf("") }
    var editedUsername by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }
    var editedBirthDate by remember { mutableStateOf("") }
    var editedGender by remember { mutableStateOf("") }
    var editedHeight by remember { mutableStateOf("") }
    var editedWeight by remember { mutableStateOf("") }
    var showGenderDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showEmailVerificationDialog by remember { mutableStateOf(false) }
    var pendingNewEmail by remember { mutableStateOf("") }

    // Date picker state
    val datePickerState = rememberDatePickerState()
    val updateProfileState by authViewModel.updateProfileState.collectAsState()



    // Initialize edit form when entering edit mode OR when user data changes
    LaunchedEffect(isEditing, currentUser) {
        if (isEditing) {
            currentUser?.let { user ->
                editedName = user.nama
                editedEmail = user.email
                editedUsername = user.username ?: ""
                editedPhone = user.noTelp
                editedBirthDate = user.tanggalLahir
                editedGender = user.gender
                editedHeight = if (user.tinggiBadan > 0) user.tinggiBadan.toString() else ""
                editedWeight = if (user.beratBadan > 0) user.beratBadan.toString() else ""

                // Set date picker state based on current birth date
                if (user.tanggalLahir.isNotEmpty()) {
                    try {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val date = sdf.parse(user.tanggalLahir)
                        date?.let {
                            datePickerState.selectedDateMillis = it.time
                        }
                    } catch (e: Exception) {
                        // If parsing fails, leave datePickerState as is
                    }
                }
            }
        }
    }

    // Handle date picker result
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            editedBirthDate = sdf.format(Date(millis))
        }
    }

    // Handle update profile state changes
    LaunchedEffect(updateProfileState) {
        when (updateProfileState) {
            is UpdateProfileState.EmailVerificationSent -> {
                pendingNewEmail = (updateProfileState as UpdateProfileState.EmailVerificationSent).newEmail
                showEmailVerificationDialog = true
                isLoading = false
            }
            is UpdateProfileState.Success -> {
                isLoading = false
                isEditing = false
            }
            is UpdateProfileState.EmailVerified -> {
                // Email berhasil diverifikasi dan diupdate
                isLoading = false
                if (showEmailVerificationDialog) {
                    showEmailVerificationDialog = false
                }
            }
            is UpdateProfileState.Loading -> {
                isLoading = true
            }
            is UpdateProfileState.Error -> {
                isLoading = false
            }
            else -> {}
        }
    }

// Check email verification status when screen resumes
    LaunchedEffect(Unit) {
        authViewModel.checkEmailVerification()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Profile" else "Profile",
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditing) {
                            isEditing = false
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        TextButton(
                            onClick = {
                                // Save changes
                                currentUser?.let { user ->
                                    isLoading = true

                                    // Debug logging
                                    Log.d("ProfileScreen", "Original user email: ${user.email}")
                                    Log.d("ProfileScreen", "New email: $editedEmail")
                                    Log.d("ProfileScreen", "Email changed: ${user.email != editedEmail}")

                                    val updatedUser = user.copy(
                                        nama = editedName,
                                        email = editedEmail,
                                        username = editedUsername.takeIf { it.isNotEmpty() },
                                        noTelp = editedPhone,
                                        tanggalLahir = editedBirthDate,
                                        gender = editedGender,
                                        tinggiBadan = editedHeight.toIntOrNull() ?: 0,
                                        beratBadan = editedWeight.toIntOrNull() ?: 0
                                    )

                                    Log.d("ProfileScreen", "Updated user email: ${updatedUser.email}")

                                    authViewModel.updateProfile(updatedUser) { success ->
                                        if (!success) {
                                            isLoading = false
                                        }
                                        Log.d("ProfileScreen", "Update result: $success")
                                        if (success) {
                                            isEditing = false
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = PrimaryPurple,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Simpan",
                                    color = PrimaryPurple,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .background(VeryLightPurple)
                    .padding(bottom = if (!isEditing) 80.dp else 0.dp) // Add padding for button
            ) {
                currentUser?.let { user ->
                    if (isEditing) {
                        EditProfileContent(
                            user = user,
                            editedName = editedName,
                            editedEmail = editedEmail,
                            editedUsername = editedUsername,
                            editedPhone = editedPhone,
                            editedBirthDate = editedBirthDate,
                            editedGender = editedGender,
                            editedHeight = editedHeight,
                            editedWeight = editedWeight,
                            showGenderDropdown = showGenderDropdown,
                            onNameChange = { editedName = it },
                            onEmailChange = { editedEmail = it },
                            onUsernameChange = { editedUsername = it },
                            onPhoneChange = { editedPhone = it },
                            onBirthDateClick = { showDatePicker = true },
                            onGenderChange = {
                                editedGender = it
                                showGenderDropdown = false
                            },
                            onHeightChange = { editedHeight = it },
                            onWeightChange = { editedWeight = it },
                            onGenderDropdownToggle = { showGenderDropdown = !showGenderDropdown }
                        )
                    } else {
                        ProfileHeader(user = user)
                        ProfileInfoSection(user = user)
                    }
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

            // Edit Profile Button - only show when not editing
            if (!isEditing && currentUser != null) {
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Email Verification Dialog
        if (showEmailVerificationDialog) {
            AlertDialog(
                onDismissRequest = {
                    showEmailVerificationDialog = false
                    authViewModel.resetUpdateProfileState()
                },
                title = {
                    Text(
                        text = "Verifikasi Email",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Email verifikasi telah dikirim ke:",
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pendingNewEmail,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryPurple
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Silakan cek email Anda dan klik link verifikasi. Email di profil akan diperbarui setelah verifikasi berhasil.",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showEmailVerificationDialog = false
                            authViewModel.resetUpdateProfileState()
                            isEditing = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            // Check if email has been verified
                            authViewModel.checkEmailVerification()
                        }
                    ) {
                        Text("Refresh")
                    }
                }
            )
        }

        // Date Picker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDateSelected = { selectedDate ->
                    selectedDate?.let {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        editedBirthDate = sdf.format(Date(it))
                    }
                    showDatePicker = false
                },
                onDismiss = {
                    showDatePicker = false
                },
                datePickerState = datePickerState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileContent(
    user: User,
    editedName: String,
    editedEmail: String,
    editedUsername: String,
    editedPhone: String,
    editedBirthDate: String,
    editedGender: String,
    editedHeight: String,
    editedWeight: String,
    showGenderDropdown: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onBirthDateClick: () -> Unit,
    onGenderChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onGenderDropdownToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Profile Picture (non-editable for now)
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(LightPurple)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(40.dp),
                tint = PrimaryPurple
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Name Field
        OutlinedTextField(
            value = editedName,
            onValueChange = onNameChange,
            label = { Text("Nama") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                focusedLabelColor = PrimaryPurple
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Field
        OutlinedTextField(
            value = editedEmail,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                focusedLabelColor = PrimaryPurple
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Username Field
        OutlinedTextField(
            value = editedUsername,
            onValueChange = onUsernameChange,
            label = { Text("Username (Opsional)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                focusedLabelColor = PrimaryPurple
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone Field
        OutlinedTextField(
            value = editedPhone,
            onValueChange = onPhoneChange,
            label = { Text("No. Telepon") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                focusedLabelColor = PrimaryPurple
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Birth Date Field with Date Picker
        OutlinedTextField(
            value = editedBirthDate,
            onValueChange = { }, // Read-only
            label = { Text("Tanggal Lahir") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBirthDateClick() },
            enabled = false,
            placeholder = { Text("Pilih tanggal lahir") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Pilih tanggal",
                    tint = PrimaryPurple,
                    modifier = Modifier.clickable { onBirthDateClick() }
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = PrimaryPurple.copy(alpha = 0.5f),
                disabledLabelColor = PrimaryPurple.copy(alpha = 0.7f),
                disabledTextColor = TextDark
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gender Dropdown
        ExposedDropdownMenuBox(
            expanded = showGenderDropdown,
            onExpandedChange = { onGenderDropdownToggle() }
        ) {
            OutlinedTextField(
                value = when (editedGender.lowercase()) {
                    "l", "laki-laki", "male" -> "Laki-laki"
                    "p", "perempuan", "female" -> "Perempuan"
                    else -> editedGender
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Jenis Kelamin") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGenderDropdown)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    focusedLabelColor = PrimaryPurple
                )
            )

            ExposedDropdownMenu(
                expanded = showGenderDropdown,
                onDismissRequest = { onGenderDropdownToggle() }
            ) {
                DropdownMenuItem(
                    text = { Text("Laki-laki") },
                    onClick = { onGenderChange("Laki-laki") }
                )
                DropdownMenuItem(
                    text = { Text("Perempuan") },
                    onClick = { onGenderChange("Perempuan") }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Height and Weight Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = editedHeight,
                onValueChange = { newValue ->
                    // Only allow numbers
                    val filtered = newValue.filter { it.isDigit() }
                    onHeightChange(filtered)
                },
                label = { Text("Tinggi (cm)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    focusedLabelColor = PrimaryPurple
                )
            )

            OutlinedTextField(
                value = editedWeight,
                onValueChange = { newValue ->
                    // Only allow numbers
                    val filtered = newValue.filter { it.isDigit() }
                    onWeightChange(filtered)
                },
                label = { Text("Berat (kg)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    focusedLabelColor = PrimaryPurple
                )
            )
        }

        // BMI Preview if both height and weight are filled
        val height = editedHeight.toIntOrNull()
        val weight = editedWeight.toIntOrNull()
        if (height != null && weight != null && height > 0 && weight > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            val bmi = calculateBMI(weight, height)
            val bmiCategory = getBMICategory(bmi)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LightPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Preview BMI",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    Text(
                        text = String.format("%.1f", bmi),
                        fontSize = 18.sp,
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    datePickerState: DatePickerState
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = PrimaryPurple,
                todayDateBorderColor = PrimaryPurple
            )
        )
    }
}

@Composable
private fun ProfileHeader(user: User) {
    Column(
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

        // Show username if available
        if (!user.username.isNullOrEmpty()) {
            Text(
                text = "@${user.username}",
                fontSize = 14.sp,
                color = TextGray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ProfileInfoSection(user: User) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        // Email Card
        ProfileInfoCard(
            icon = Icons.Default.Email,
            title = "Email",
            value = user.email.ifEmpty { "Belum diisi" }
        )

        // Username Card (only show if username exists)
        if (!user.username.isNullOrEmpty()) {
            ProfileInfoCard(
                icon = Icons.Default.AccountCircle,
                title = "Username",
                value = "@${user.username}"
            )
        }

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