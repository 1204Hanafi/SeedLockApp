package com.app.seedlockapp.domain.interactor

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import timber.log.Timber

/**
 * Interactor (atau Use Case) yang menyederhanakan dan memusatkan logika
 * untuk berinteraksi dengan Android BiometricPrompt API.
 * Tujuannya adalah untuk mengabstraksi kompleksitas pembuatan prompt,
 * info, dan callback dari ViewModel, membuatnya lebih mudah diuji dan digunakan kembali.
 */
class BiometricInteractor {

    /**
     * Memeriksa apakah otentikasi biometrik (kuat atau lemah) tersedia dan dapat digunakan di perangkat.
     *
     * @param activity [FragmentActivity] yang diperlukan untuk mendapatkan [BiometricManager].
     * @return `true` jika biometrik tersedia, `false` jika tidak.
     */
    fun isBiometricAvailable(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (val authResult = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Timber.d("Biometric authentication is available.")
                true
            }
            else -> {
                Timber.w("Biometric authentication not available. Code: $authResult")
                false
            }
        }
    }

    /**
     * Membuat instance [BiometricPrompt] yang terikat pada activity dan callback yang diberikan.
     *
     * @param activity [FragmentActivity] sebagai host untuk dialog biometrik.
     * @param callback [BiometricPrompt.AuthenticationCallback] untuk menangani hasil otentikasi (sukses, error, gagal).
     * @return Instance [BiometricPrompt] yang siap digunakan.
     */
    fun createBiometricPrompt(
        activity: FragmentActivity,
        callback: BiometricPrompt.AuthenticationCallback
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        return BiometricPrompt(activity, executor, callback)
    }

    /**
     * Membuat konfigurasi [BiometricPrompt.PromptInfo] untuk dialog yang akan ditampilkan kepada pengguna.
     *
     * @param title Judul yang akan ditampilkan di dialog.
     * @param negativeButtonText Teks untuk tombol negatif (misalnya, "Batal").
     * @return Instance [BiometricPrompt.PromptInfo] yang dikonfigurasi.
     */
    fun createPromptInfo(title: String, negativeButtonText: String): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText(negativeButtonText)
            // Mengizinkan biometrik kuat (seperti sidik jari 3D) dan lemah (seperti face unlock 2D).
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
    }
}