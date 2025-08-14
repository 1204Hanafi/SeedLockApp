package com.app.seedlockapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.seedlockapp.data.model.Seed
import com.app.seedlockapp.data.repository.SeedRepository
import com.app.seedlockapp.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val seeds: List<Seed> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val seedRepository: SeedRepository,
    val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSeeds()
    }

    private fun loadSeeds() {
        seedRepository.getAllSeeds()
            .onEach { seeds ->
                _uiState.value = HomeUiState(seeds = seeds, isLoading = false)
            }
            .launchIn(viewModelScope)
    }

    fun deleteSeed(seedId: String) {
        viewModelScope.launch {
            seedRepository.deleteSeed(seedId)
            // Flow akan otomatis memperbarui UI
        }
    }
}