package com.proyek.eatright.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.proyek.eatright.data.model.Food
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.proyek.eatright.viewmodel.FoodSearchViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FoodSearchApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "search") {
        composable("search") {
            SearchScreen(
                onFoodClick = { foodId ->
                    navController.navigate("food_detail/$foodId")
                }
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
    }
}

// And update the SearchScreen to handle food clicks
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onFoodClick: (String) -> Unit,
    viewModel: FoodSearchViewModel = viewModel()
) {
    val searchQuery = viewModel.searchQuery.value
    val isLoading = viewModel.isLoading.value
    val searchResults = viewModel.searchResults.value

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pencarian Makanan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = { viewModel.searchFoods() },
                // Add the missing required parameters
                active = false, // Or use a state variable if you need to track active state
                onActiveChange = { }, // Handle active state changes if needed
                content = { } // Add content for search suggestions
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    CircularProgressIndicator()
                }
            } else {
                FoodList(
                    foods = searchResults,
                    onFoodClick = onFoodClick
                )
            }
        }
    }
}

@Composable
fun FoodList(
    foods: List<Food>,
    onFoodClick: (String) -> Unit
) {
    if (foods.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Tidak ada hasil pencarian")
        }
    } else {
        LazyColumn {
            items(foods) { food ->
                FoodItem(
                    food = food,
                    onClick = { onFoodClick(food.id) }
                )
                Divider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodItem(
    food: Food,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = food.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = food.type,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = food.description,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

//@Composable
//fun FoodSearchApp() {
//    val viewModel: FatSecretViewModel = viewModel()
//    val searchQuery = viewModel.searchQuery.value
//    val isLoading = viewModel.isLoading.value
//    val searchResults = viewModel.searchResults.value
//
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = MaterialTheme.colorScheme.background
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(
//                text = "Pencarian Makanan",
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//
//            SearchBar(
//                query = searchQuery,
//                onQueryChange = { viewModel.updateSearchQuery(it) },
//                onSearch = { viewModel.searchFoods() }
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            if (isLoading) {
//                Box(
//                    contentAlignment = Alignment.Center,
//                    modifier = Modifier.fillMaxWidth().padding(16.dp)
//                ) {
//                    CircularProgressIndicator()
//                }
//            } else {
//                FoodList(foods = searchResults)
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SearchBar(
//    query: String,
//    onQueryChange: (String) -> Unit,
//    onSearch: () -> Unit
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        OutlinedTextField(
//            value = query,
//            onValueChange = onQueryChange,
//            modifier = Modifier.weight(1f),
//            label = { Text("Cari makanan") },
//            singleLine = true
//        )
//
//        Spacer(modifier = Modifier.width(8.dp))
//
//        Button(onClick = onSearch) {
//            Text("Cari")
//        }
//    }
//}
//
//@Composable
//fun FoodList(foods: List<Food>) {
//    if (foods.isEmpty()) {
//        Box(
//            contentAlignment = Alignment.Center,
//            modifier = Modifier.fillMaxWidth().padding(16.dp)
//        ) {
//            Text("Tidak ada hasil pencarian")
//        }
//    } else {
//        LazyColumn {
//            items(foods) { food ->
//                FoodItem(food)
//                Divider()
//            }
//        }
//    }
//}
//
//@Composable
//fun FoodItem(food: Food) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        shape = RoundedCornerShape(8.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = food.name,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Text(
//                text = food.type,
//                fontSize = 14.sp,
//                color = Color.Gray
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = food.description,
//                fontSize = 14.sp,
//                maxLines = 3,
//                overflow = TextOverflow.Ellipsis
//            )
//        }
//    }
//}