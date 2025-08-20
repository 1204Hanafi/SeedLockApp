package com.app.seedlockapp.domain.manager

import com.app.seedlockapp.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Mengelola state sesi otentikasi pengguna di seluruh aplikasi.
 * Sesi dianggap aktif setelah pengguna berhasil melakukan otentikasi biometrik.
 * Sesi akan berakhir secara otomatis setelah periode waktu inaktif tertentu atau
 * ketika aplikasi dipindahkan ke background.
 */
class SessionManager {
    private val _isAuthenticated = MutableStateFlow(false)
    /**
     * [kotlinx.coroutines.flow.StateFlow] yang memancarkan status otentikasi sesi saat ini.
     * `true` jika sesi aktif, `false` jika tidak. UI dapat mengamati flow ini
     * untuk bereaksi terhadap perubahan status sesi.
     */
    val isAuthenticated = _isAuthenticated.asStateFlow()

    private var sessionJob: Job? = null
    private val sessionScope = CoroutineScope(Dispatchers.Default)

    private val sessionTimeoutMillis = TimeUnit.MINUTES.toMillis(Constants.SESSION_TIMEOUT_MINUTES)

    /**
     * Memulai sesi otentikasi.
     * Mengatur status [isAuthenticated] menjadi `true` dan memulai timer timeout.
     */
    fun startSession() {
        if (_isAuthenticated.value) return // Hindari memulai ulang jika sudah aktif
        _isAuthenticated.value = true
        Timber.d("Session started.")
        startTimeoutTimer()
    }

    /**
     * Mengakhiri sesi otentikasi secara paksa.
     * Membatalkan timer timeout dan mengatur [isAuthenticated] menjadi `false`.
     */
    fun endSession() {
        sessionJob?.cancel()
        _isAuthenticated.value = false
        Timber.d("Session ended.")
    }

    /**
     * Memperbarui timer sesi. Dipanggil setiap kali ada interaksi pengguna
     * untuk mencegah sesi berakhir karena timeout.
     */
    fun refreshInteraction() {
        if (_isAuthenticated.value) {
            Timber.v("User interaction detected, refreshing session timeout.")
            sessionJob?.cancel()
            startTimeoutTimer()
        }
    }

    /**
     * Memulai coroutine yang akan mengakhiri sesi setelah durasi timeout tercapai.
     */
    private fun startTimeoutTimer() {
        sessionJob = sessionScope.launch {
            delay(sessionTimeoutMillis)
            Timber.w("Session timed out after ${Constants.SESSION_TIMEOUT_MINUTES} minutes of inactivity.")
            endSession()
        }
    }
}