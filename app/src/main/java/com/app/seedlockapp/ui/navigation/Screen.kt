package com.app.seedlockapp.ui.navigation

/**
 * Sealed class representing all screens in the application.
 * Used for type-safe navigation.
 */
sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object AddSeed : Screen("add_seed")
    object ViewSeed : Screen("view_seed")
}

