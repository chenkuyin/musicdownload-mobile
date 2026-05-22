package com.example.musicdownloader.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicdownloader.ui.screens.*

sealed class Screen(val route: String) {
    object Search : Screen("search")
    object Downloads : Screen("downloads")
    object Settings : Screen("settings")
}

@Composable
fun MusicNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Search.route
    ) {
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Downloads.route) {
            DownloadsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
