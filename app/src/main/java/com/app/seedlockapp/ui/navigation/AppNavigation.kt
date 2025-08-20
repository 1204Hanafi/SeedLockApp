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

/**
 * Mendefinisikan grafik navigasi (navigation graph) aplikasi menggunakan [NavHost].
 * Composable ini memetakan setiap rute dari [Screen] ke Composable layar yang sesuai.
 *
 * @param navController Instance [NavHostController] yang mengelola state navigasi.
 * @param startDestination Rute awal yang akan ditampilkan saat NavHost pertama kali dibuat.
 */
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
        // Definisi rute untuk ViewSeedScreen dengan argumen `seedId`.
        composable(
            route = "${Screen.ViewSeed.route}/{${Constants.NAV_ARG_SEED_ID}}",
            arguments = listOf(navArgument(Constants.NAV_ARG_SEED_ID) {
                type = NavType.StringType
                // Argumen ini tidak boleh null. Jika null, akan terjadi crash,
                // yang merupakan perilaku yang diinginkan untuk mencegah state ilegal.
            })
        ) { backStackEntry ->
            val seedId = backStackEntry.arguments?.getString(Constants.NAV_ARG_SEED_ID)
            // Menggunakan requireNotNull untuk memastikan seedId tidak null.
            requireNotNull(seedId) { "Parameter seedId tidak ditemukan di rute navigasi." }
            ViewSeedScreen(navController = navController, seedId = seedId)
        }
    }
}