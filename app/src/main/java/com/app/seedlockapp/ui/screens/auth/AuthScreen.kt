package com.app.seedlockapp.ui.screens.auth

import androidx.biometric.BiometricManager
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
    rememberCoroutineScope()

    DisposableEffect(lifecycleOwner, fragmentActivity) {
        fun triggerAuth() {
            if (fragmentActivity == null) return
            if (viewModel.authState.value !is AuthState.Idle) return

            if (!viewModel.biometricInteractor.isBiometricAvailable(fragmentActivity)) {
                viewModel.onAuthError(0, "Biometric authentication is not available.")
                return
            }

            val cipher = viewModel.keystoreManager.getAuthCipher()
            if (cipher == null) {
                viewModel.onAuthError(0, "Failed to create cryptographic object.")
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
            val biometricManager = BiometricManager.from(fragmentActivity)
            val canStrong = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

            when (canStrong) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    try {
                        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
                    } catch (iae: IllegalArgumentException) {
                        // Safety-net: jika ada kondisi edge yang menolak crypto-based, fallback non-crypto
                        Timber.e(iae, "Crypto-based auth not supported despite strong reported; fallback to non-crypto.")
                        biometricPrompt.authenticate(promptInfo) // non-crypto fallback
                    } catch (t: Throwable) {
                        Timber.e(t, "Unexpected error when starting crypto-based biometric auth")
                        viewModel.onAuthError(0, "Authentication error.")
                    }
                }
                else -> {
                    // Jika tidak ada BIOMETRIC_STRONG, cek weak biometric
                    val canWeak = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                    if (canWeak == BiometricManager.BIOMETRIC_SUCCESS) {
                        // Hanya weak biometric tersedia -> gunakan non-crypto authenticate (tanpa CryptoObject)
                        biometricPrompt.authenticate(promptInfo)
                    } else {
                        // Tidak ada biometric yang memadai -> beri error / fallback
                        viewModel.onAuthError(0, "Biometric strong not available on this device.")
                    }
                }
            }
    }

    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            Timber.d("AuthScreen resumed, triggering authentication")
            if (viewModel.authState.value is AuthState.Error) {
                viewModel.onAuthError(0,"")
            }
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