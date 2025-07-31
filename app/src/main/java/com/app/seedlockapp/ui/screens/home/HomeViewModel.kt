package com.app.seedlockapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.seedlockapp.data.repository.SeedRepository
import com.app.seedlockapp.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for home screen.
 * Manages the list of seeds and handles seed operations.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    val sessionManager: SessionManager,
    private val seedRepository: SeedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadSeeds()
    }

    /**
     * Loads all seeds from the repository.
     */
    private fun loadSeeds() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            try {
                val seeds = seedRepository.getAllSeeds()
                _uiState.value = _uiState.value.copy(
                    seeds = seeds,
                    loading = false,
                    error = null
                )
                Timber.d("Seeds loaded successfully: %d seeds", seeds.size)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message
                )
                Timber.e(e, "Failed to load seeds")
            }
        }
    }

    /**
     * Deletes a seed by its ID.
     * 
     * @param seedId The ID of the seed to delete
     */
    fun deleteSeed(seedId: String) {
        viewModelScope.launch {
            try {
                seedRepository.deleteSeed(seedId)
                loadSeeds() // Refresh the list
                Timber.d("Seed deleted: %s", seedId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
                Timber.e(e, "Failed to delete seed: %s", seedId)
            }
        }
    }
}

