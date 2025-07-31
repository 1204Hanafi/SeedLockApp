package com.app.seedlockapp.biometric

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import com.app.seedlockapp.domain.interactor.KeystoreInteractor
import com.app.seedlockapp.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricAuthManager @Inject constructor(
    private val sessionManager: SessionManager,
    private val keystoreInteractor: KeystoreInteractor,
    private val context: Context // Inject application context
) {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    fun authenticateForAppAccess(
        activity: FragmentActivity,
        onSuccess: suspend () -> Unit,
        onError: (String) -> Unit
    ) {
        executor = Executors.newSingleThreadExecutor()

        biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Timber.e("Biometric authentication error: %s (%d)", errString, errorCode)
                    onError(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Timber.d("Biometric authentication succeeded.")
                    sessionManager.authenticate()
                    CoroutineScope(Dispatchers.Main).launch {
                        onSuccess()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Timber.w("Biometric authentication failed.")
                    onError("Authentication failed")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autentikasi Diperlukan")
            .setSubtitle("Gunakan sidik jari atau kunci layar untuk melanjutkan")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun performOperationWithConditionalAuth(
        activity: FragmentActivity,
        operation: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (sessionManager.isAuthenticated.value) {

            operation()
        } else {

            authenticateForAppAccess(
                activity = activity,
                onSuccess = {
                    sessionManager.authenticate()
                    operation()
                },
                onError = onError
            )
        }
    }

}