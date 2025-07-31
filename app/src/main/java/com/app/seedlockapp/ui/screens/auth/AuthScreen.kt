package com.app.seedlockapp.ui.screens.auth

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.seedlockapp.R
import com.app.seedlockapp.ui.navigation.Screen

@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity
    val state by viewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        if (fragmentActivity == null) {
            viewModel.onAuthError("Host is not a FragmentActivity")
            return@LaunchedEffect
        }

        if (!viewModel.biometricInteractor.isBiometricAvailable(fragmentActivity)) {
            viewModel.onAuthError("Biometric authentication is not available on this device.")
            return@LaunchedEffect
        }

        val promptInfo = viewModel.biometricInteractor.createPromptInfo(
            title = "Verifikasi Identitas Anda",
            negativeButtonText = "Batal"
        )

        val biometricPrompt = viewModel.biometricInteractor.createBiometricPrompt(
            fragmentActivity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.onAuthSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    viewModel.onAuthError(errString.toString())
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (state) {
            is AuthState.Idle -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(top = 120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.seed),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(80.dp))
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
                Text("Authentication failed: ${(state as AuthState.Error).message}")
                LaunchedEffect(Unit) {
                    fragmentActivity?.finish()
                }
            }
        }
    }
}

