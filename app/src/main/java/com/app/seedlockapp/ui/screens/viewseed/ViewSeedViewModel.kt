package com.app.seedlockapp.ui.screens.viewseed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.seedlockapp.data.repository.SeedRepository
import com.app.seedlockapp.domain.interactor.KeystoreInteractor
import com.app.seedlockapp.domain.interactor.SSSInteractor
import com.app.seedlockapp.domain.model.Share
import com.app.seedlockapp.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for view seed screen.
 * Handles loading and displaying seed phrases securely.
 */
@HiltViewModel
class ViewSeedViewModel @Inject constructor(
    val sessionManager: SessionManager,
    private val seedRepository: SeedRepository,
    private val keystoreInteractor: KeystoreInteractor,
    private val sssInteractor: SSSInteractor
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewSeedUiState())
    val uiState: StateFlow<ViewSeedUiState> = _uiState

    /**
     * Loads and reconstructs a seed phrase by its ID.
     * 
     * @param seedId The ID of the seed to load
     */
    fun load(seedId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            
            try {
                // Step 1: Load seed from repository by ID
                val seed = seedRepository.getSeed(seedId)
                if (seed == null) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = "Seed not found"
                    )
                    return@launch
                }

                // Step 2: Decrypt each encrypted share using KeystoreInteractor
                val decryptedShares = mutableListOf<Share>()
                for (encryptedShareString in seed.encryptedShares) {
                    try {
                        val parts = encryptedShareString.split(":")
                        if (parts.size != 2) continue
                        
                        val shareIndex = parts[0].toInt()
                        val encodedData = parts[1]
                        
                        val combinedData = android.util.Base64.decode(encodedData, android.util.Base64.DEFAULT)
                        
                        // Extract IV and encrypted data
                        val ivSize = 16 // AES block size
                        val iv = combinedData.sliceArray(0 until ivSize)
                        val encryptedData = combinedData.sliceArray(ivSize until combinedData.size)
                        val cipher = keystoreInteractor.getDecryptCipher(iv)
                        
                        val decryptedData = keystoreInteractor.decrypt(cipher, encryptedData)
                        decryptedShares.add(Share(shareIndex, decryptedData))
                        
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to decrypt share")
                    }
                }

                if (decryptedShares.size < 2) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = "Insufficient shares for reconstruction"
                    )
                    return@launch
                }

                // Step 3: Reconstruct seed phrase using SSSInteractor
                val reconstructedPhrase = sssInteractor.reconstructSecret(decryptedShares)
                
                // Step 4: Wrap in SecureString and update UI state
                _uiState.value = _uiState.value.copy(
                    alias = seed.name,
                    seedPhrase = SecureString(reconstructedPhrase),
                    loading = false,
                    error = null
                )
                
                Timber.d("Seed loaded and reconstructed successfully: %s", seedId)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Unknown error occurred"
                )
                Timber.e(e, "Failed to load seed: %s", seedId)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clear sensitive data when ViewModel is destroyed
        _uiState.value.seedPhrase?.clear()
        Timber.d("ViewSeedViewModel cleared, sensitive data wiped")
    }
}

