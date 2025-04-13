package com.proyek.eatright.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.proyek.eatright.data.model.User
import com.proyek.eatright.viewmodel.AuthState
import com.proyek.eatright.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var noTelp by remember { mutableStateOf("") }
    var tanggalLahir by remember { mutableStateOf("") }
    var tinggiBadan by remember { mutableStateOf("") }
    var beratBadan by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") } // Menambahkan state untuk gender
    var expanded by remember { mutableStateOf(false) } // Untuk dropdown gender

    val genderOptions = listOf("Laki-laki", "Perempuan") // Opsi gender

    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigate("main") {
                    popUpTo("register") { inclusive = true }
                }
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (authState as AuthState.Error).message
                )
                viewModel.resetAuthState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Nama Lengkap field
            OutlinedTextField(
                value = nama,
                onValueChange = { nama = it },
                label = { Text("Nama Lengkap") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE6EEFF),
                    unfocusedContainerColor = Color(0xFFE6EEFF),
                    disabledContainerColor = Color(0xFFE6EEFF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Gender dropdown field
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(30.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE6EEFF),
                        unfocusedContainerColor = Color(0xFFE6EEFF),
                        disabledContainerColor = Color(0xFFE6EEFF),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                gender = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE6EEFF),
                    unfocusedContainerColor = Color(0xFFE6EEFF),
                    disabledContainerColor = Color(0xFFE6EEFF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE6EEFF),
                    unfocusedContainerColor = Color(0xFFE6EEFF),
                    disabledContainerColor = Color(0xFFE6EEFF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE6EEFF),
                    unfocusedContainerColor = Color(0xFFE6EEFF),
                    disabledContainerColor = Color(0xFFE6EEFF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nomor Telepon field
            OutlinedTextField(
                value = noTelp,
                onValueChange = { noTelp = it },
                label = { Text("Nomor Telepon") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE6EEFF),
                    unfocusedContainerColor = Color(0xFFE6EEFF),
                    disabledContainerColor = Color(0xFFE6EEFF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tanggal Lahir field
            OutlinedTextField(
                value = tanggalLahir,
                onValueChange = { tanggalLahir = it },
                label = { Text("Tanggal Lahir (DD/MM/YYYY)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE6EEFF),
                    unfocusedContainerColor = Color(0xFFE6EEFF),
                    disabledContainerColor = Color(0xFFE6EEFF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tinggi Badan and Berat Badan fields in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = tinggiBadan,
                    onValueChange = { tinggiBadan = it },
                    label = { Text("Tinggi (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(30.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE6EEFF),
                        unfocusedContainerColor = Color(0xFFE6EEFF),
                        disabledContainerColor = Color(0xFFE6EEFF),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = beratBadan,
                    onValueChange = { beratBadan = it },
                    label = { Text("Berat (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(30.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE6EEFF),
                        unfocusedContainerColor = Color(0xFFE6EEFF),
                        disabledContainerColor = Color(0xFFE6EEFF),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register button
            Button(
                onClick = {
                    if (validateFields()) {
                        val user = User(
                            nama = nama,
                            email = email,
                            username = username,
                            password = password,
                            noTelp = noTelp,
                            tanggalLahir = tanggalLahir,
                            tinggiBadan = tinggiBadan.toIntOrNull() ?: 0,
                            beratBadan = beratBadan.toIntOrNull() ?: 0,
                            gender = gender
                        )
                        viewModel.register(user)
                    } else {
                        // Show error message for missing fields
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6E66FA)
                )
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Register",
                        fontSize = 16.sp,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login text
            TextButton(
                onClick = { navController.navigate("login") }
            ) {
                Text(
                    "Already have an account? Login",
                    color = Color(0xFF6E66FA), // Warna biru-ungu seperti pada gambar
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun validateFields(): Boolean {
    // Implement validation logic here
    // For example, check that all required fields are not empty
    // and that email is valid, password meets minimum requirements, etc.
    return true // For now, just return true
}