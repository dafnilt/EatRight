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
import java.util.regex.Pattern

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
    var gender by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // Error states
    var namaError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var noTelpError by remember { mutableStateOf("") }
    var tanggalLahirError by remember { mutableStateOf("") }
    var tinggiBadanError by remember { mutableStateOf("") }
    var beratBadanError by remember { mutableStateOf("") }
    var genderError by remember { mutableStateOf("") }

    val genderOptions = listOf("Laki-laki", "Perempuan")

    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Function to clear all error states
    fun clearErrors() {
        namaError = ""
        emailError = ""
        usernameError = ""
        passwordError = ""
        noTelpError = ""
        tanggalLahirError = ""
        tinggiBadanError = ""
        beratBadanError = ""
        genderError = ""
    }

    // Validation functions
    fun validateNama(nama: String): String {
        return when {
            nama.isBlank() -> "Nama lengkap tidak boleh kosong"
            nama.length < 2 -> "Nama minimal 2 karakter"
            !nama.matches(Regex("^[a-zA-Z\\s]+$")) -> "Nama hanya boleh berisi huruf dan spasi"
            else -> ""
        }
    }

    fun validateEmail(email: String): String {
        val emailPattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        return when {
            email.isBlank() -> "Email tidak boleh kosong"
            !emailPattern.matcher(email).matches() -> "Format email tidak valid"
            else -> ""
        }
    }

    fun validateUsername(username: String): String {
        return when {
            username.isBlank() -> "Username tidak boleh kosong"
            username.length < 3 -> "Username minimal 3 karakter"
            username.length > 20 -> "Username maksimal 20 karakter"
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Username hanya boleh berisi huruf, angka, dan underscore"
            else -> ""
        }
    }

    fun validatePassword(password: String): String {
        return when {
            password.isBlank() -> "Password tidak boleh kosong"
            password.length < 8 -> "Password minimal 8 karakter"
            !password.any { it.isUpperCase() } -> "Password harus mengandung minimal 1 huruf besar"
            !password.any { it.isLowerCase() } -> "Password harus mengandung minimal 1 huruf kecil"
            !password.any { it.isDigit() } -> "Password harus mengandung minimal 1 angka"
            else -> ""
        }
    }

    fun validateNoTelp(noTelp: String): String {
        return when {
            noTelp.isBlank() -> "Nomor telepon tidak boleh kosong"
            !noTelp.matches(Regex("^[0-9+\\-\\s]+$")) -> "Nomor telepon hanya boleh berisi angka, +, -, dan spasi"
            else -> ""
        }
    }

    fun validateTanggalLahir(tanggalLahir: String): String {
        return when {
            tanggalLahir.isBlank() -> "Tanggal lahir tidak boleh kosong"
            !tanggalLahir.matches(Regex("^\\d{2}/\\d{2}/\\d{4}$")) -> "Format tanggal harus DD/MM/YYYY"
            else -> {
                try {
                    val parts = tanggalLahir.split("/")
                    val day = parts[0].toInt()
                    val month = parts[1].toInt()
                    when {
                        day < 1 || day > 31 -> "Hari tidak valid (1-31)"
                        month < 1 || month > 12 -> "Bulan tidak valid (1-12)"
                        else -> ""
                    }
                } catch (e: Exception) {
                    "Format tanggal tidak valid"
                }
            }
        }
    }

    fun validateTinggiBadan(tinggi: String): String {
        return when {
            tinggi.isBlank() -> "Tinggi badan tidak boleh kosong"
            else -> ""
        }
    }

    fun validateBeratBadan(berat: String): String {
        return when {
            berat.isBlank() -> "Berat badan tidak boleh kosong"
            else -> ""
        }
    }

    fun validateGender(gender: String): String {
        return when {
            gender.isBlank() -> "Gender tidak boleh kosong"
            !genderOptions.contains(gender) -> "Pilih gender yang valid"
            else -> ""
        }
    }

    fun validateAllFields(): Boolean {
        clearErrors()

        namaError = validateNama(nama)
        emailError = validateEmail(email)
        usernameError = validateUsername(username)
        passwordError = validatePassword(password)
        noTelpError = validateNoTelp(noTelp)
        tanggalLahirError = validateTanggalLahir(tanggalLahir)
        tinggiBadanError = validateTinggiBadan(tinggiBadan)
        beratBadanError = validateBeratBadan(beratBadan)
        genderError = validateGender(gender)

        return namaError.isEmpty() && emailError.isEmpty() && usernameError.isEmpty() &&
                passwordError.isEmpty() && noTelpError.isEmpty() && tanggalLahirError.isEmpty() &&
                tinggiBadanError.isEmpty() && beratBadanError.isEmpty() && genderError.isEmpty()
    }

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
                onValueChange = {
                    nama = it
                    namaError = validateNama(it)
                },
                label = { Text("Nama Lengkap") },
                singleLine = true,
                isError = namaError.isNotEmpty(),
                supportingText = if (namaError.isNotEmpty()) {
                    { Text(namaError, color = MaterialTheme.colorScheme.error) }
                } else null,
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
                    isError = genderError.isNotEmpty(),
                    supportingText = if (genderError.isNotEmpty()) {
                        { Text(genderError, color = MaterialTheme.colorScheme.error) }
                    } else null,
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
                                genderError = validateGender(option)
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
                onValueChange = {
                    email = it
                    emailError = validateEmail(it)
                },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = emailError.isNotEmpty(),
                supportingText = if (emailError.isNotEmpty()) {
                    { Text(emailError, color = MaterialTheme.colorScheme.error) }
                } else null,
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
                onValueChange = {
                    username = it
                    usernameError = validateUsername(it)
                },
                label = { Text("Username") },
                singleLine = true,
                isError = usernameError.isNotEmpty(),
                supportingText = if (usernameError.isNotEmpty()) {
                    { Text(usernameError, color = MaterialTheme.colorScheme.error) }
                } else null,
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
                onValueChange = {
                    password = it
                    passwordError = validatePassword(it)
                },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = passwordError.isNotEmpty(),
                supportingText = if (passwordError.isNotEmpty()) {
                    { Text(passwordError, color = MaterialTheme.colorScheme.error) }
                } else null,
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
                onValueChange = {
                    noTelp = it
                    noTelpError = validateNoTelp(it)
                },
                label = { Text("Nomor Telepon") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = noTelpError.isNotEmpty(),
                supportingText = if (noTelpError.isNotEmpty()) {
                    { Text(noTelpError, color = MaterialTheme.colorScheme.error) }
                } else null,
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
                onValueChange = {
                    tanggalLahir = it
                    tanggalLahirError = validateTanggalLahir(it)
                },
                label = { Text("Tanggal Lahir (DD/MM/YYYY)") },
                singleLine = true,
                isError = tanggalLahirError.isNotEmpty(),
                supportingText = if (tanggalLahirError.isNotEmpty()) {
                    { Text(tanggalLahirError, color = MaterialTheme.colorScheme.error) }
                } else null,
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
                    onValueChange = {
                        tinggiBadan = it
                        tinggiBadanError = validateTinggiBadan(it)
                    },
                    label = { Text("Tinggi (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = tinggiBadanError.isNotEmpty(),
                    supportingText = if (tinggiBadanError.isNotEmpty()) {
                        { Text(tinggiBadanError, color = MaterialTheme.colorScheme.error, fontSize = 10.sp) }
                    } else null,
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
                    onValueChange = {
                        beratBadan = it
                        beratBadanError = validateBeratBadan(it)
                    },
                    label = { Text("Berat (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = beratBadanError.isNotEmpty(),
                    supportingText = if (beratBadanError.isNotEmpty()) {
                        { Text(beratBadanError, color = MaterialTheme.colorScheme.error, fontSize = 10.sp) }
                    } else null,
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
                    if (validateAllFields()) {
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
                    color = Color(0xFF6E66FA),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}