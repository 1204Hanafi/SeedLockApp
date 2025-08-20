package com.app.seedlockapp.ui.navigation

/**
 * Sealed class yang mendefinisikan semua rute (route) navigasi yang ada di aplikasi.
 * Menggunakan sealed class memastikan type-safety dan mencegah kesalahan pengetikan nama rute.
 *
 * @property route String unik yang digunakan sebagai identitas untuk setiap layar di NavHost.
 */
sealed class Screen(val route: String) {
    /** Rute untuk layar otentikasi biometrik ([com.app.seedlockapp.ui.screens.auth.AuthScreen]). */
    object Auth : Screen("auth_screen")
    /** Rute untuk layar utama yang menampilkan daftar seed ([com.app.seedlockapp.ui.screens.home.HomeScreen]). */
    object Home : Screen("home_screen")
    /** Rute untuk layar penambahan seed baru ([com.app.seedlockapp.ui.screens.addseed.AddSeedScreen]). */
    object AddSeed : Screen("add_seed_screen")
    /** Rute untuk layar yang menampilkan detail seed phrase ([com.app.seedlockapp.ui.screens.viewseed.ViewSeedScreen]). */
    object ViewSeed : Screen("view_seed_screen")
}