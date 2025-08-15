package com.app.seedlockapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.app.seedlockapp.domain.interactor.BiometricInteractor
import com.app.seedlockapp.domain.manager.KeystoreManager
import com.app.seedlockapp.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    val biometricInteractor: BiometricInteractor,
    private val sessionManager: SessionManager,
    val keystoreManager: KeystoreManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun onAuthSuccess() {
        sessionManager.startSession()
        _authState.value = AuthState.Success
    }

    fun onAuthError(errorCode: Int, errorMessage: String) {
        val fullErrorMessage = "Error ($errorCode): $errorMessage"
        _authState.value = AuthState.Error(fullErrorMessage)
    }
}