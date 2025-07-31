package com.app.seedlockapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.app.seedlockapp.domain.interactor.BiometricInteractor
import com.app.seedlockapp.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for authentication screen.
 * Manages authentication state and integrates with SessionManager.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    val biometricInteractor: BiometricInteractor
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    /**
     * Called when authentication is successful.
     * Updates session state and authentication state.
     */
    fun onAuthSuccess() {
        Timber.d("Authentication successful")
        sessionManager.authenticate()
        _authState.value = AuthState.Success
    }

    /**
     * Called when authentication fails.
     * 
     * @param message Error message describing the failure
     */
    fun onAuthError(message: String) {
        Timber.e("Authentication failed: %s", message)
        sessionManager.invalidateSession()
        _authState.value = AuthState.Error(message)
    }

    /**
     * Resets authentication state to idle.
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

