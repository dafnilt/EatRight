package com.proyek.eatright

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.proyek.eatright.ui.screen.ConsumptionSummaryScreen
import com.proyek.eatright.ui.screen.FoodDetailScreen
import com.proyek.eatright.ui.screen.LoginScreen
import com.proyek.eatright.ui.screen.RegisterScreen
import com.proyek.eatright.ui.screen.SplashScreen
import com.proyek.eatright.ui.screen.OnboardingScreen
import com.proyek.eatright.ui.screen.ProfileScreen
import com.proyek.eatright.viewmodel.AuthState
import com.proyek.eatright.viewmodel.AuthViewModel
import com.proyek.eatright.viewmodel.ConsumptionViewModel
import com.proyek.eatright.viewmodel.FoodSearchViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import com.proyek.eatright.ui.screen.DashboardScreen
import com.proyek.eatright.ui.screen.SearchScreen
import com.proyek.eatright.ui.theme.DarkBlue

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val foodSearchViewModel: FoodSearchViewModel = viewModel()
    val consumptionViewModel: ConsumptionViewModel = viewModel()

    val authState by authViewModel.authState.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Tentukan startDestination berdasarkan auth state
    val startDestination = when (authState) {
        is AuthState.Authenticated -> "main"
        is AuthState.Initial, is AuthState.Loading -> "splash"
        else -> "splash" // Untuk Unauthenticated dan Error, mulai dari splash
    }

    // Handle navigation based on auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> {
                // Hanya navigasi jika bukan sedang di halaman auth
                if (currentRoute !in listOf("login", "register", "onboarding", "splash")) {
                    navController.navigate("splash") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.Authenticated -> {
                // Navigasi ke main jika berhasil login/register dan tidak sedang di main
                if (currentRoute !in listOf("main", "search", "consumption_summary", "food_detail/{foodId}", "profile")) {
                    navController.navigate("main") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {
                // Tidak melakukan navigasi otomatis untuk state lain
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf("main", "consumption_summary", "search")) {
                BottomAppBar(
                    modifier = Modifier.height(80.dp),
                    containerColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                navController.navigate("main") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Dashboard",
                                tint = Color.Gray
                            )
                            Text("Dashboard", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(DarkBlue)
                                .clickable {
                                    navController.navigate("search")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                navController.navigate("consumption_summary") {
                                    popUpTo("consumption_summary") { inclusive = true }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Catatan Harian",
                                tint = Color.Gray
                            )
                            Text("Catatan Harian", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash", // Selalu mulai dari splash
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash screen akan menentukan kemana user diarahkan
            composable("splash") {
                SplashScreen(
                    authState = authState,
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    },
                    onNavigateToMain = {
                        navController.navigate("main") {
                            popUpTo("splash") { inclusive = true }
                        }
                    },
                    onNavigateToOnboarding = {
                        navController.navigate("onboarding") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            // Onboarding hanya untuk pengguna baru
            composable("onboarding") {
                OnboardingScreen(
                    navController = navController,
                    onboardingComplete = {
                        // Navigate to login after completing onboarding
                        navController.navigate("login") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }

            composable("login") {
                LoginScreen(
                    navController = navController,
                    viewModel = authViewModel
                )
            }

            composable("register") {
                RegisterScreen(
                    navController = navController,
                    viewModel = authViewModel
                )
            }

            composable("main") {
                DashboardScreen(
                    onFoodClick = { foodId ->
                        navController.navigate("food_detail/$foodId")
                    },
                    onConsumptionSummaryClick = {
                        navController.navigate("consumption_summary")
                    },
                    onProfileClick = {
                        navController.navigate("profile")
                    },
                    viewModel = foodSearchViewModel,
                    authViewModel = authViewModel,
                    consumptionViewModel = consumptionViewModel
                )
            }

            composable("search") {
                SearchScreen(
                    onFoodClick = { foodId ->
                        navController.navigate("food_detail/$foodId")
                    },
                    onConsumptionSummaryClick = {
                        navController.navigate("consumption_summary")
                    },
                    viewModel = foodSearchViewModel
                )
            }

            composable(
                route = "food_detail/{foodId}",
                arguments = listOf(navArgument("foodId") { type = NavType.StringType })
            ) { backStackEntry ->
                val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
                FoodDetailScreen(
                    foodId = foodId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("consumption_summary") {
                ConsumptionSummaryScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            // Profile Screen
            composable("profile") {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    authViewModel = authViewModel
                )
            }
        }
    }
}