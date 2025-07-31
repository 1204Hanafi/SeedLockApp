package com.app.seedlockapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.seedlockapp.ui.screens.home.HomeScreen
import com.app.seedlockapp.ui.screens.addseed.AddSeedScreen
import com.app.seedlockapp.ui.screens.viewseed.ViewSeedScreen
import com.app.seedlockapp.ui.screens.auth.AuthScreen

/**
 * Main navigation component for the SeedLock application.
 * Defines all navigation routes and their corresponding screens.
 *
 * @param navController The navigation controller for handling navigation
 */
@Composable
fun SeedLockNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.AddSeed.route) {
            AddSeedScreen(navController = navController)
        }

        composable("${Screen.ViewSeed.route}/{seedId}") { backStackEntry ->
            val seedId = backStackEntry.arguments?.getString("seedId") ?: ""
            ViewSeedScreen(
                navController = navController,
                seedId = seedId
            )
        }
    }
}
