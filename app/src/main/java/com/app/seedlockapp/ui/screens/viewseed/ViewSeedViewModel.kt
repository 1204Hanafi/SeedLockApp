package com.app.seedlockapp.ui.screens.viewseed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.seedlockapp.data.repository.SeedRepository
import com.app.seedlockapp.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Kelas wrapper yang dirancang khusus untuk menangani String sensitif seperti seed phrase.
 * Tujuannya adalah untuk menyediakan metode [clear] agar referensi ke String
 * bisa dihapus dari memori sesegera mungkin setelah tidak lagi digunakan,
 * membantu Garbage Collector untuk membersihkannya dan mengurangi exposure time.
 * @param initialValue Nilai awal dari string sensitif.
 */
class SensitiveString(initialValue: String) {
    private var _value: String? = initialValue

    /** Mengembalikan nilai string. Hati-hati saat menggunakan ini. */
    fun getValue(): String? = _value

    /**
     * Menghapus referensi ke String sensitif. Ini adalah langkah penting
     * untuk keamanan memori. Panggil metode ini di `onDispose` atau `onCleared`.
     */
    fun clear() {
        _value = null
    }
}

/**
 * Merepresentasikan state untuk [ViewSeedScreen].
 *
 * @property loading `true` jika seed sedang dimuat dan didekripsi.
 * @property alias Nama dari seed yang ditampilkan.
 * @property seedPhrase Wrapper [SensitiveString] yang berisi seed phrase plaintext. Bisa `null` jika gagal.
 * @property error Pesan error jika terjadi kegagalan.
 */
data class ViewSeedUiState(
    val loading: Boolean = true,
    val alias: String = "",
    val seedPhrase: SensitiveString? = null,
    val error: String? = null
)

/**
 * ViewModel untuk [ViewSeedScreen].
 * Bertanggung jawab untuk memuat alias dan mendekripsi seed phrase
 * untuk seedId yang diberikan.
 */
@HiltViewModel
class ViewSeedViewModel @Inject constructor(
    private val seedRepository: SeedRepository,
    val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewSeedUiState())
    val uiState: StateFlow<ViewSeedUiState> = _uiState.asStateFlow()

    /**
     * Memuat dan mendekripsi seed.
     * @param seedId ID dari seed yang akan ditampilkan.
     */
    fun load(seedId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }

            // Secara paralel memuat alias dan seed, meskipun alias bisa menunggu.
            // Di sini kita muat alias terlebih dahulu untuk ditampilkan saat loading.
            val alias = seedRepository.getAlias(seedId) ?: "Tidak Diketahui"
            _uiState.update { it.copy(alias = alias) }

            seedRepository.getDecryptedSeed(seedId)
                .onSuccess { phrase ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            seedPhrase = SensitiveString(phrase),
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = error.message ?: "Terjadi kesalahan tidak diketahui saat mendekripsi seed."
                        )
                    }
                }
        }
    }

    /**
     * Membersihkan data sensitif dari memori.
     * Metode ini harus dipanggil ketika ViewModel dihancurkan.
     */
    override fun onCleared() {
        _uiState.value.seedPhrase?.clear()
        super.onCleared()
    }
}