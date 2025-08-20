package com.app.seedlockapp.ui.screens.addseed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.seedlockapp.data.repository.SeedRepository
import com.app.seedlockapp.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Representasi state untuk layar tambah seed.
 * Mengelola siklus hidup operasi penyimpanan seed, dari idle, loading, hingga hasil akhir.
 */
sealed class AddSeedUiState {
    /** State awal, tidak ada operasi yang sedang berlangsung. */
    object Idle : AddSeedUiState()
    /** Menandakan bahwa proses penyimpanan seed sedang berjalan di background. */
    object Loading : AddSeedUiState()
    /** Menandakan bahwa seed berhasil disimpan. */
    object Success : AddSeedUiState()
    /** Menandakan terjadi kegagalan saat menyimpan, berisi pesan error. */
    data class Error(val message: String) : AddSeedUiState()
}

/**
 * ViewModel untuk [AddSeedScreen].
 * Mengelola state input pengguna (phrase dan alias) dan menjalankan
 * logika untuk menyimpan seed baru melalui [SeedRepository].
 */
@HiltViewModel
class AddSeedViewModel @Inject constructor(
    private val seedRepository: SeedRepository,
    val sessionManager: SessionManager
) : ViewModel() {

    // State internal untuk phrase dan alias, tidak diekspos langsung untuk menjaga enkapsulasi.
    private val _phrase = MutableStateFlow("")
    private val _alias = MutableStateFlow("")

    private val _uiState = MutableStateFlow<AddSeedUiState>(AddSeedUiState.Idle)
    val uiState: StateFlow<AddSeedUiState> = _uiState.asStateFlow()

    /**
     * Memperbarui nilai seed phrase yang diinput oleh pengguna.
     * @param newPhrase Teks seed phrase yang baru.
     */
    fun onPhraseChanged(newPhrase: String) {
        _phrase.value = newPhrase
    }

    /**
     * Memperbarui nilai alias yang diinput oleh pengguna.
     * @param newAlias Teks alias yang baru.
     */
    fun onAliasChanged(newAlias: String) {
        _alias.value = newAlias
    }

    /**
     * Memulai proses penyimpanan seed.
     * Fungsi ini akan mengubah UI state menjadi [AddSeedUiState.Loading],
     * memanggil repository, dan memperbarui state dengan hasil [AddSeedUiState.Success] atau [AddSeedUiState.Error].
     */
    fun saveSeed() {
        // Validasi dasar sebelum melanjutkan
        if (_phrase.value.isBlank() || _alias.value.isBlank()) {
            _uiState.value = AddSeedUiState.Error("Seed phrase dan alias tidak boleh kosong.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddSeedUiState.Loading
            seedRepository.saveSeed(_phrase.value, _alias.value)
                .onSuccess {
                    _uiState.value = AddSeedUiState.Success
                }
                .onFailure { error ->
                    // Memberikan pesan error yang lebih ramah pengguna
                    val friendlyMessage = error.message ?: "Terjadi kesalahan yang tidak diketahui."
                    _uiState.value = AddSeedUiState.Error(friendlyMessage)
                }
        }
    }

    /**
     * Mereset UI state kembali ke [AddSeedUiState.Idle].
     * Dipanggil setelah operasi (sukses atau gagal) selesai ditangani oleh UI.
     */
    fun resetUiState() {
        _uiState.value = AddSeedUiState.Idle
    }
}