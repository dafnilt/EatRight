package com.proyek.eatright.ui.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.proyek.eatright.viewmodel.AuthState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authState: AuthState,
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(authState) {
        delay(2000) // Splash delay

        when (authState) {
            is AuthState.Authenticated -> {
                onNavigateToMain()
            }
            is AuthState.Unauthenticated -> {
                // Cek apakah ini pertama kali user membuka app
                val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val isFirstTime = sharedPrefs.getBoolean("is_first_time", true)

                if (isFirstTime) {
                    // Tandai bahwa user sudah melihat onboarding
                    sharedPrefs.edit().putBoolean("is_first_time", false).apply()
                    onNavigateToOnboarding()
                } else {
                    // Langsung ke login jika sudah pernah onboarding
                    onNavigateToLogin()
                }
            }
            is AuthState.Initial -> {
                // Tunggu auth state berubah
            }
            is AuthState.Loading -> {
                // Tunggu loading selesai
            }
            is AuthState.Error -> {
                // Jika error, arahkan ke login
                onNavigateToLogin()
            }
        }
    }

    // UI Splash Screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo atau brand name
            Text(
                text = "EatRight",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF6E66FA)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    color = Color(0xFF6E66FA)
                )
            }
        }
    }
}
