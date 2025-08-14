package com.app.seedlockapp.domain.interactor

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import timber.log.Timber

/**
 * Interactor untuk menyederhanakan penggunaan BiometricPrompt.
 */
class BiometricInteractor {

    /**
     * Memeriksa apakah autentikasi biometrik tersedia di perangkat.
     */
    fun isBiometricAvailable(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Timber.d("Biometric authentication is available.")
                true
            }
            else -> {
                Timber.w("Biometric authentication not available.")
                false
            }
        }
    }

    /**
     * Membuat instance BiometricPrompt.
     */
    fun createBiometricPrompt(
        activity: FragmentActivity,
        callback: BiometricPrompt.AuthenticationCallback
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        return BiometricPrompt(activity, executor, callback)
    }

    /**
     * Membuat konfigurasi untuk dialog BiometricPrompt.
     */
    fun createPromptInfo(title: String, negativeButtonText: String): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
    }
}