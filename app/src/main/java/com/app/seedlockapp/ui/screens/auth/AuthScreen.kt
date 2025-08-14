package com.app.seedlockapp.ui.screens.auth

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.app.seedlockapp.R
import com.app.seedlockapp.ui.navigation.Screen
import timber.log.Timber

@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity
    val state by viewModel.authState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, fragmentActivity) {
        fun triggerAuth() {
            if (fragmentActivity == null) return
            if (viewModel.authState.value !is AuthState.Idle) return

            if (!viewModel.biometricInteractor.isBiometricAvailable(fragmentActivity)) {
                viewModel.onAuthError(0, "Biometric authentication is not available.")
                return
            }
            // 2. Memanggil createPromptInfo
            val promptInfo = viewModel.biometricInteractor.createPromptInfo(
                title = "Verifikasi Identitas Anda",
                negativeButtonText = "Batal"
            )

            // 3. Memanggil createBiometricPrompt
            val biometricPrompt = viewModel.biometricInteractor.createBiometricPrompt(
                fragmentActivity,
            object : BiometricPrompt.AuthenticationCallback() {
                // Callback jika autentikasi berhasil
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.onAuthSuccess()
                }
                // Callback jika terjadi error atau dibatalkan pengguna
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Jika pengguna menekan "Batal", error code akan ter-trigger.
                    Timber.d("Authentication Error - Code: $errorCode, Message: $errString")
                    // Ini akan menutup aplikasi.
                    viewModel.onAuthError(errorCode,errString.toString())
                }
            }
        )
        // Tampilkan prompt ke pengguna
        biometricPrompt.authenticate(promptInfo)
    }

    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            Timber.d("AuthScreen resumed, triggering authentication")
            triggerAuth()
        }
    }

        // Tambahkan observer ke siklus hidup
        lifecycleOwner.lifecycle.addObserver(observer)

        // Pemicu pertama kali saat Composable dibuat
        triggerAuth()

        // Hapus observer saat Composable dihancurkan
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val currentState = state) {
            is AuthState.Idle -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(top = 120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Image(painter = painterResource(id = R.drawable.seed), contentDescription = null, modifier = Modifier.size(100.dp))
                    Spacer(Modifier.height(80.dp))
                    CircularProgressIndicator()
                }
            }
            is AuthState.Success -> {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            }
            is AuthState.Error -> {
                Text("Authentication failed: ${currentState.message}")
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    fragmentActivity?.finish()
                }
            }
        }
    }
}