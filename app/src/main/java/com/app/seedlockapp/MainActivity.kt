package com.app.seedlockapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.app.seedlockapp.session.SessionManager
import com.app.seedlockapp.ui.navigation.SeedLockNavigation
import com.app.seedlockapp.ui.theme.SeedLockAppTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Main activity for the SeedLock application.
 * Entry point that sets up navigation and theme.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Timber.d("MainActivity created")
        
        setContent {
            SeedLockAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    SeedLockNavigation(navController = navController)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("MainActivity resumed")
        sessionManager.invalidateSession()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        sessionManager.refreshInteraction()
    }
}

