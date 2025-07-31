package com.app.seedlockapp.ui.screens.auth

/**
 * Sealed class representing different states of authentication.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

