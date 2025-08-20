package com.app.seedlockapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.seedlockapp.data.model.Seed
import com.app.seedlockapp.data.repository.SeedRepository
import com.app.seedlockapp.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Merepresentasikan state untuk [HomeScreen].
 *
 * @property seeds Daftar seed yang akan ditampilkan.
 * @property isLoading `true` jika data sedang dimuat, `false` sebaliknya.
 * @property error Pesan error jika terjadi kegagalan saat memuat atau menghapus data.
 */
data class HomeUiState(
    val seeds: List<Seed> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel untuk [HomeScreen].
 * Bertanggung jawab untuk memuat daftar seed dari [SeedRepository],
 * menampilkannya ke UI, dan menangani permintaan penghapusan seed.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val seedRepository: SeedRepository,
    val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadSeeds()
    }

    /**
     * Memuat daftar seed dari repository.
     * Mengamati [kotlinx.coroutines.flow.Flow] dari `getAllSeeds` dan memperbarui [HomeUiState] setiap kali ada perubahan data.
     */
    private fun loadSeeds() {
        seedRepository.getAllSeeds()
            .onEach { seeds ->
                _uiState.update { it.copy(seeds = seeds, isLoading = false, error = null) }
            }
            .catch { error ->
                // Menangani error yang mungkin terjadi pada Flow
                Timber.e(error, "Failed to load seeds.")
                _uiState.update { it.copy(isLoading = false, error = "Gagal memuat daftar seed.") }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Menghapus seed dengan ID yang diberikan.
     * @param seedId ID dari seed yang akan dihapus.
     */
    fun deleteSeed(seedId: String) {
        viewModelScope.launch {
            seedRepository.deleteSeed(seedId)
                .onFailure { error ->
                    Timber.e(error, "Failed to delete seed with id: $seedId")
                    _uiState.update { it.copy(error = "Gagal menghapus seed.") }
                }
            // Jika berhasil, Flow dari `loadSeeds` akan otomatis memperbarui UI.
        }
    }

    /**
     * Menghapus pesan error dari state setelah ditampilkan.
     */
    fun errorShown() {
        _uiState.update { it.copy(error = null) }
    }
}