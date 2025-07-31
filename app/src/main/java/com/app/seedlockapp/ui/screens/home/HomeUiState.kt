package com.app.seedlockapp.ui.screens.home

import com.app.seedlockapp.domain.model.Seed

/**
 * UI state for the home screen.
 * 
 * @property seeds List of seeds to display
 * @property loading Whether data is currently being loaded
 * @property error Error message if any operation failed
 */
data class HomeUiState(
    val seeds: List<Seed> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

