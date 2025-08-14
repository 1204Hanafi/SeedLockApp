package com.app.seedlockapp.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth_screen")
    object Home : Screen("home_screen")
    object AddSeed : Screen("add_seed_screen")
    object ViewSeed : Screen("view_seed_screen")
}