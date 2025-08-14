package com.app.seedlockapp.ui.screens.addseed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.seedlockapp.data.repository.SeedRepository
import com.app.seedlockapp.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AddSeedUiState {
    object Idle : AddSeedUiState()
    object Loading : AddSeedUiState()
    object Success : AddSeedUiState()
    data class Error(val message: String) : AddSeedUiState()
}

@HiltViewModel
class AddSeedViewModel @Inject constructor(
    private val seedRepository: SeedRepository,
    val sessionManager: SessionManager
) : ViewModel() {

    private val _phrase = MutableStateFlow("")
    private val _alias = MutableStateFlow("")

    private val _uiState = MutableStateFlow<AddSeedUiState>(AddSeedUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun onPhraseChanged(newPhrase: String) {
        _phrase.value = newPhrase
    }

    fun onAliasChanged(newAlias: String) {
        _alias.value = newAlias
    }

    fun saveSeed() {
        viewModelScope.launch {
            _uiState.value = AddSeedUiState.Loading
            // Tambahkan logika untuk menampilkan loading/error jika perlu
            seedRepository.saveSeed(_phrase.value, _alias.value)
                .onSuccess {
                    _uiState.value = AddSeedUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = AddSeedUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun resetUiState() {
        _uiState.value = AddSeedUiState.Idle
    }
}