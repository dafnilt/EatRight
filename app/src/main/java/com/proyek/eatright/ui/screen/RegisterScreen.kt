package com.proyek.eatright.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.proyek.eatright.data.model.User
import com.proyek.eatright.viewmodel.AuthViewModel
import java.util.*

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val authState by viewModel.authState.collectAsState()

    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var noTelp by remember { mutableStateOf("") }
    var tanggalLahir by remember { mutableStateOf("") }
    var tinggiBadan by remember { mutableStateOf("") }
    var beratBadan by remember { mutableStateOf("") }

    var passwordsMatch by remember { mutableStateOf(true) }

    // Effect untuk menangani state
    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Success) {
            // Navigasi ke layar utama setelah berhasil register
            navController.navigate("main") {
                popUpTo("register") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Register",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Nama
        OutlinedTextField(
            value = nama,
            onValueChange = { nama = it },
            label = { Text("Nama Lengkap") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Username (optional)
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username (opsional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordsMatch = password == confirmPassword
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Confirm Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                passwordsMatch = password == confirmPassword
            },
            label = { Text("Konfirmasi Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = !passwordsMatch,
            modifier = Modifier.fillMaxWidth()
        )

        if (!passwordsMatch) {
            Text(
                text = "Password tidak cocok",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // No Telp
        OutlinedTextField(
            value = noTelp,
            onValueChange = { noTelp = it },
            label = { Text("Nomor Telepon") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tanggal Lahir
        OutlinedTextField(
            value = tanggalLahir,
            onValueChange = { tanggalLahir = it },
            label = { Text("Tanggal Lahir (DD/MM/YYYY)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tinggi Badan
        OutlinedTextField(
            value = tinggiBadan,
            onValueChange = { tinggiBadan = it },
            label = { Text("Tinggi Badan (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Berat Badan
        OutlinedTextField(
            value = beratBadan,
            onValueChange = { beratBadan = it },
            label = { Text("Berat Badan (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Register Button
        Button(
            onClick = {
                // Validasi input
                if (nama.isNotBlank() && email.isNotBlank() && password.isNotBlank() &&
                    confirmPassword.isNotBlank() && noTelp.isNotBlank() && tanggalLahir.isNotBlank() &&
                    tinggiBadan.isNotBlank() && beratBadan.isNotBlank() && passwordsMatch) {

                    val user = User(
                        nama = nama,
                        email = email,
                        username = if (username.isNotBlank()) username else null,
                        password = password,
                        noTelp = noTelp,
                        tanggalLahir = tanggalLahir,
                        tinggiBadan = tinggiBadan.toIntOrNull() ?: 0,
                        beratBadan = beratBadan.toIntOrNull() ?: 0
                    )
                    viewModel.register(user)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = nama.isNotBlank() && email.isNotBlank() && password.isNotBlank() &&
                    confirmPassword.isNotBlank() && noTelp.isNotBlank() && tanggalLahir.isNotBlank() &&
                    tinggiBadan.isNotBlank() && beratBadan.isNotBlank() && passwordsMatch &&
                    authState !is AuthViewModel.AuthState.Loading
        ) {
            if (authState is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                Text("Register")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login Link
        TextButton(
            onClick = { navController.navigate("login") }
        ) {
            Text("Sudah punya akun? Login disini")
        }

        // Error state
        if (authState is AuthViewModel.AuthState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (authState as AuthViewModel.AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}