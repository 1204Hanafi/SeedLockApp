package com.app.seedlockapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.app.seedlockapp.domain.interactor.BiometricInteractor
import com.app.seedlockapp.domain.manager.KeystoreManager
import com.app.seedlockapp.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Representasi state untuk layar otentikasi.
 * Menggambarkan semua kemungkinan kondisi UI selama proses otentikasi biometrik.
 */
sealed class AuthState {
    /** State awal, menunggu pemicu otentikasi. */
    object Idle : AuthState()
    /** State ketika otentikasi berhasil, sesi akan segera dimulai. */
    object Success : AuthState()
    /** State ketika terjadi kegagalan, berisi pesan error untuk ditampilkan. */
    data class Error(val message: String) : AuthState()
}

/**
 * ViewModel untuk [AuthScreen].
 * Bertanggung jawab untuk mengoordinasikan proses otentikasi biometrik,
 * mengelola state UI-nya, dan berinteraksi dengan [SessionManager] untuk
 * memulai sesi setelah otentikasi berhasil.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    val biometricInteractor: BiometricInteractor,
    private val sessionManager: SessionManager,
    val keystoreManager: KeystoreManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    /**
     * Dipanggil ketika otentikasi biometrik berhasil.
     * Memulai sesi pengguna dan mengubah state UI menjadi [AuthState.Success].
     */
    fun onAuthSuccess() {
        sessionManager.startSession()
        _authState.value = AuthState.Success
    }

    /**
     * Dipanggil ketika otentikasi biometrik gagal atau dibatalkan.
     * Mengubah state UI menjadi [AuthState.Error] dengan pesan yang relevan.
     *
     * @param errorCode Kode error yang diterima dari BiometricPrompt.
     * @param errorMessage Pesan error yang diterima.
     */
    fun onAuthError(errorCode: Int, errorMessage: String) {
        val fullErrorMessage = "Authentication Error (Code: $errorCode): $errorMessage"
        _authState.value = AuthState.Error(fullErrorMessage)
    }
}