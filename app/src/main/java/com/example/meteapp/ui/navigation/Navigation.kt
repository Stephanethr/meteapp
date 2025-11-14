package com.example.meteapp.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.meteapp.ui.detail.DetailScreen
import com.example.meteapp.ui.home.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{lat}/{lon}/{name}") {
        fun createRoute(lat: Double, lon: Double, name: String) = "detail/$lat/$lon/${Uri.encode(name)}"
    }
}

@Composable
fun MeteoNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(onOpenDetail = { lat, lon, name -> navController.navigate(Screen.Detail.createRoute(lat, lon, name)) })
        }
        composable(Screen.Detail.route) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull() ?: 0.0
            val nameEncoded = backStackEntry.arguments?.getString("name") ?: ""
            val name = Uri.decode(nameEncoded)
            DetailScreen(latitude = lat, longitude = lon, cityName = name, onBack = { navController.navigateUp() })
        }
    }
}
