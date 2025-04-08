package com.proyek.eatright

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.proyek.eatright.ui.screen.ConsumptionSummaryScreen
import com.proyek.eatright.ui.screen.FoodDetailScreen
import com.proyek.eatright.ui.screen.LoginScreen
import com.proyek.eatright.ui.screen.RegisterScreen
import com.proyek.eatright.ui.screen.SearchScreen
import com.proyek.eatright.ui.screen.SplashScreen
import com.proyek.eatright.viewmodel.AuthState
import com.proyek.eatright.viewmodel.AuthViewModel
import com.proyek.eatright.viewmodel.ConsumptionViewModel
import com.proyek.eatright.viewmodel.FoodSearchViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val foodSearchViewModel: FoodSearchViewModel = viewModel()
    val consumptionViewModel: ConsumptionViewModel = viewModel()

    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash" // Start ke SplashScreen dulu
    ) {
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
            SearchScreen(
                onFoodClick = { foodId ->
                    navController.navigate("food_detail/$foodId")
                },
                onConsumptionSummaryClick = {
                    navController.navigate("consumption_summary")
                },
                viewModel = foodSearchViewModel,
                authViewModel = authViewModel
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
    }
}


