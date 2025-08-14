package com.app.seedlockapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.app.seedlockapp.domain.manager.SessionManager
import com.app.seedlockapp.ui.navigation.AppNavigation
import com.app.seedlockapp.ui.navigation.Screen
import com.app.seedlockapp.ui.theme.SeedLockAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeedLockAppTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val isAuthenticated by sessionManager.isAuthenticated.collectAsState()

                    // Logika untuk navigasi berdasarkan status autentikasi
                    LaunchedEffect(isAuthenticated) {
                        if (!isAuthenticated) {
                            // Jika sesi tidak aktif, paksa kembali ke layar auth
                            // dan bersihkan back stack.
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                            }
                        }
                    }

                    // Start destination ditentukan oleh status sesi awal
                    val startDestination = if (isAuthenticated) Screen.Home.route else Screen.Auth.route
                    AppNavigation(navController = navController, startDestination = startDestination)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Logout otomatis saat aplikasi ke background
        sessionManager.endSession()
    }
}