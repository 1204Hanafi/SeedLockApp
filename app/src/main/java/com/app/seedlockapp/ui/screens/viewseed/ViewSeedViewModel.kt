package com.app.seedlockapp.ui.screens.viewseed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.seedlockapp.data.repository.SeedRepository
import com.app.seedlockapp.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Kelas wrapper yang dirancang khusus untuk menangani String sensitif.
 * Tujuannya adalah untuk menyediakan metode clear() agar referensi ke String
 * bisa dihapus dari memori sesegera mungkin.
 */
class SensitiveString(initialValue: String) {
    private var _value: String? = initialValue

    fun getValue(): String? = _value

    /**
     * Menghapus referensi ke String sensitif. Ini adalah langkah penting
     * agar Garbage Collector bisa membersihkan data dari memori.
     */
    fun clear() {
        _value = null
    }
}

data class ViewSeedUiState(
    val loading: Boolean = true,
    val alias: String = "",
    val seedPhrase: SensitiveString? = null,
    val error: String? = null
)

@HiltViewModel
class ViewSeedViewModel @Inject constructor(
    private val seedRepository: SeedRepository,
    val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewSeedUiState())
    val uiState = _uiState.asStateFlow()

    fun load(seedId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }

            val alias = seedRepository.getAlias(seedId) ?: "N/A"

            seedRepository.getDecryptedSeed(seedId)
                .onSuccess { phrase ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            alias = alias,
                            seedPhrase = SensitiveString(phrase),
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            alias = alias,
                            error = error.message ?: "Terjadi kesalahan tidak diketahui"
                        )
                    }
                }
        }
    }
}