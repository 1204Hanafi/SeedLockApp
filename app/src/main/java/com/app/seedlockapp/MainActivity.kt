package com.app.seedlockapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.app.seedlockapp.domain.manager.SessionManager
import com.app.seedlockapp.ui.navigation.AppNavigation
import com.app.seedlockapp.ui.navigation.Screen
import com.app.seedlockapp.ui.theme.SeedLockAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity utama dan satu-satunya titik masuk (entry point) untuk UI aplikasi.
 * Bertanggung jawab untuk:
 * - Meng-host NavHost dari Jetpack Compose.
 * - Mengelola logika navigasi level atas berdasarkan status otentikasi.
 * - Menangani event siklus hidup aplikasi seperti `onPause` untuk keamanan.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeedLockAppTheme(
                darkTheme = false, // Tema gelap saat ini dinonaktifkan
                dynamicColor = false // Warna dinamis (Material You) dinonaktifkan
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    RootNavigation(navController, sessionManager)
                }
            }
        }
    }

    /**
     * Dipanggil ketika aplikasi berpindah ke background (misalnya, pengguna menekan tombol home).
     * Sesi otentikasi akan langsung diakhiri untuk alasan keamanan, memaksa
     * pengguna untuk melakukan otentikasi ulang saat kembali ke aplikasi.
     */
    override fun onPause() {
        super.onPause()
        sessionManager.endSession()
    }
}

/**
 * Composable yang bertanggung jawab untuk logika navigasi utama.
 * Memisahkan logika ini dari `onCreate` membuat kode lebih bersih dan mudah diuji.
 *
 * @param navController Controller navigasi untuk mengelola perpindahan layar.
 * @param sessionManager Manajer sesi untuk memeriksa status otentikasi.
 */
@Composable
private fun RootNavigation(navController: NavHostController, sessionManager: SessionManager) {
    val isAuthenticated by sessionManager.isAuthenticated.collectAsState()

    // LaunchedEffect digunakan untuk menangani navigasi sebagai "side-effect"
    // dari perubahan state `isAuthenticated`.
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            // Jika sesi berakhir (misalnya karena timeout atau onPause),
            // paksa navigasi kembali ke layar otentikasi dan hapus semua back stack
            // untuk mencegah pengguna kembali ke layar sebelumnya tanpa otentikasi.
            navController.navigate(Screen.Auth.route) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        }
    }

    // Tentukan layar awal berdasarkan status sesi saat aplikasi pertama kali dibuka.
    // Jika sesi sudah aktif (jarang terjadi, tapi sebagai safety net), langsung ke Home.
    val startDestination = if (isAuthenticated) Screen.Home.route else Screen.Auth.route
    AppNavigation(navController = navController, startDestination = startDestination)
}