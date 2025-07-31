package com.app.seedlockapp.session

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user session, including authentication status and timeout.
 * The session automatically expires after a period of inactivity.
 *
 * @property isAuthenticated A [StateFlow] indicating the current authentication status.
 */
@Singleton
class SessionManager @Inject constructor() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val handler = Handler(Looper.getMainLooper())
    companion object{
    private const val SESSION_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
    }

    private val logoutRunnable = Runnable { 
        _isAuthenticated.value = false
        Timber.d("Session expired due to inactivity.")
    }

    /**
     * Marks the session as authenticated and starts/resets the timeout timer.
     * This should be called after a successful biometric authentication.
     */
    fun authenticate() {
        _isAuthenticated.value = true
        Timber.d("Session authenticated.")
        startSessionTimeout()
    }

    /**
     * Invalidates the current session.
     * This should be called when the user explicitly logs out or when an authentication error occurs.
     */
    fun invalidateSession() {
        _isAuthenticated.value = false
        handler.removeCallbacks(logoutRunnable)
        Timber.d("Session invalidated manually.")
    }

    /**
     * Refreshes the session timeout timer. Should be called on any user interaction
     * (e.g., tap, scroll, navigation) to prevent the session from expiring.
     */
    fun refreshInteraction() {
        if (_isAuthenticated.value) {
            Timber.d("Refreshing session interaction.")
            startSessionTimeout()
        }
    }

    private fun startSessionTimeout() {
        handler.removeCallbacks(logoutRunnable)
        handler.postDelayed(logoutRunnable, SESSION_TIMEOUT_MS)
        Timber.d("Session timeout timer started/reset for %d ms.", SESSION_TIMEOUT_MS)
    }
}

