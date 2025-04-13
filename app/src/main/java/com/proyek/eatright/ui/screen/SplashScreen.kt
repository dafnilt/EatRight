package com.proyek.eatright.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.proyek.eatright.viewmodel.AuthState

@Composable
fun SplashScreen(
    authState: AuthState,
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    // Navigasi saat state login berubah
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> onNavigateToMain()
            is AuthState.Unauthenticated -> onNavigateToLogin()
            else -> {} // Loading, Initial, Error → tetap di splash
        }
    }

    // Tampilan loading
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
