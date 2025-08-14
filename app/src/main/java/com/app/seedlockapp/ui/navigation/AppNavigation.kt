package com.app.seedlockapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.seedlockapp.ui.screens.addseed.AddSeedScreen
import com.app.seedlockapp.ui.screens.auth.AuthScreen
import com.app.seedlockapp.ui.screens.home.HomeScreen
import com.app.seedlockapp.ui.screens.viewseed.ViewSeedScreen
import com.app.seedlockapp.util.Constants

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Auth.route) {
            AuthScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.AddSeed.route) {
            AddSeedScreen(navController = navController)
        }
        composable(
            route = "${Screen.ViewSeed.route}/{${Constants.NAV_ARG_SEED_ID}}",
            arguments = listOf(navArgument(Constants.NAV_ARG_SEED_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val seedId = backStackEntry.arguments?.getString(Constants.NAV_ARG_SEED_ID)
            requireNotNull(seedId) { "seedId parameter wasn't found. Please make sure it's set!" }
            ViewSeedScreen(navController = navController, seedId = seedId)
        }
    }
}