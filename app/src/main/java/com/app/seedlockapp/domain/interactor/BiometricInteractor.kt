package com.app.seedlockapp.domain.interactor

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import timber.log.Timber
import javax.inject.Inject

/**
 * Handles biometric authentication operations.
 * Provides methods to check biometric availability and authenticate users.
 *
 * This class encapsulates the Android BiometricPrompt API and provides a clean interface
 * for biometric authentication within the application.
 */
class BiometricInteractor @Inject constructor() {

    /**
     * Checks if biometric authentication is available on the device.
     * 
     * @param activity The FragmentActivity context required for biometric operations.
     * @return True if biometric authentication is available and can be used, false otherwise.
     */
    fun isBiometricAvailable(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Timber.d("Biometric authentication is available.")
                true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Timber.w("No biometric features available on this device.")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Timber.w("Biometric features are currently unavailable.")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Timber.w("The user hasn't associated any biometric credentials with their account.")
                false
            }
            else -> {
                Timber.w("Unknown biometric status.")
                false
            }
        }
    }

    /**
     * Creates a BiometricPrompt instance with the provided callback.
     * 
     * @param activity The FragmentActivity context required for biometric operations.
     * @param callback The authentication callback that will handle authentication results.
     * @return BiometricPrompt instance ready for authentication.
     */
    fun createBiometricPrompt(
        activity: FragmentActivity,
        callback: BiometricPrompt.AuthenticationCallback
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        return BiometricPrompt(activity, executor, callback)
    }

    /**
     * Creates a BiometricPrompt.PromptInfo with default settings.
     * 
     * @param title The title for the biometric prompt dialog.
     * @param subtitle Optional subtitle for the prompt dialog.
     * @param negativeButtonText Text for the negative/cancel button.
     * @return BiometricPrompt.PromptInfo instance configured with the provided parameters.
     */
    fun createPromptInfo(
        title: String = "Verifikasi Identitas Anda",
        subtitle: String? = null,
        negativeButtonText: String = "Batal"
    ): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText(negativeButtonText)

        subtitle?.let { builder.setSubtitle(it) }

        return builder.build()
    }
}

