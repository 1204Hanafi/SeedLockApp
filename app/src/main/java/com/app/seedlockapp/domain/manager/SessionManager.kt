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

class SessionManager {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    private var sessionJob: Job? = null
    private val sessionScope = CoroutineScope(Dispatchers.Default)

    // Menggunakan konstanta dari Constants.kt
    private val sessionTimeoutMillis = TimeUnit.MINUTES.toMillis(Constants.SESSION_TIMEOUT_MINUTES)

    fun startSession() {
        _isAuthenticated.value = true
        Timber.d("Session started.")
        startTimeoutTimer()
    }

    fun endSession() {
        sessionJob?.cancel()
        _isAuthenticated.value = false
        Timber.d("Session ended.")
    }

    fun refreshInteraction() {
        if (_isAuthenticated.value) {
            Timber.d("User interaction detected, refreshing session timeout.")
            sessionJob?.cancel()
            startTimeoutTimer()
        }
    }

    private fun startTimeoutTimer() {
        sessionJob = sessionScope.launch {
            delay(sessionTimeoutMillis)
            Timber.w("Session timed out after ${Constants.SESSION_TIMEOUT_MINUTES} minutes of inactivity.")
            endSession()
        }
    }
}