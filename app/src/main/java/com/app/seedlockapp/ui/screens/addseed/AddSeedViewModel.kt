package com.app.seedlockapp.ui.screens.addseed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.seedlockapp.data.repository.SeedRepository
import com.app.seedlockapp.domain.interactor.KeystoreInteractor
import com.app.seedlockapp.domain.interactor.SSSInteractor
import com.app.seedlockapp.domain.model.Seed
import com.app.seedlockapp.session.SessionManager
import com.app.seedlockapp.biometric.BiometricAuthManager
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddSeedViewModel @Inject constructor(
    val sessionManager: SessionManager,
    private val sssInteractor: SSSInteractor,
    private val keystoreInteractor: KeystoreInteractor,
    private val seedRepository: SeedRepository,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {

    private val _phrase = MutableStateFlow("")
    val phrase: StateFlow<String> = _phrase

    private val _alias = MutableStateFlow("")
    val alias: StateFlow<String> = _alias

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Updates the seed phrase.
     *
     * @param newPhrase The new seed phrase
     */
    fun onPhraseChanged(newPhrase: String) {
        _phrase.value = newPhrase
        Timber.d("Seed phrase updated")
    }

    /**
     * Updates the alias/name for the seed.
     *
     * @param newAlias The new alias
     */
    fun onAliasChanged(newAlias: String) {
        _alias.value = newAlias
        Timber.d("Seed alias updated: %s", newAlias)
    }

    /**
     * Saves the seed phrase using SSS and encryption.
     */
    fun saveSeed(activity: FragmentActivity) {
        viewModelScope.launch {
            _isLoading.value = true
            biometricAuthManager.performOperationWithConditionalAuth(
                activity = activity,
                operation = {
                    viewModelScope.launch {
                    performSeedSaving()
                    }
                },
                onError = { err ->
                    _isLoading.value = false
                    Timber.e("Biometric authentication failed: %s", err)
                }
            )
        }
    }

    private suspend fun performSeedSaving() {
        try {
            val currentPhrase = _phrase.value
            val currentAlias = _alias.value

            // Step 1: Split seed phrase using SSS
            val shares = sssInteractor.splitSecret(currentPhrase)
            Timber.d("Seed phrase split into %d shares", shares.size)

            // Step 2: Encrypt each share using Keystore
            val encryptedShares = mutableListOf<String>()
            for (share in shares) {
                val cipher = keystoreInteractor.getEncryptCipher()
                val (encryptedData, iv) = keystoreInteractor.encrypt(cipher, share.value)

                // Combine encrypted data and IV for storage
                val combinedData = iv + encryptedData
                val encodedShare = android.util.Base64.encodeToString(combinedData, android.util.Base64.DEFAULT)
                encryptedShares.add("${share.index}:$encodedShare")
            }
            Timber.d("All shares encrypted successfully")

            // Step 3: Save encrypted shares using Repository
            val seed = Seed(
                id = UUID.randomUUID().toString(),
                name = currentAlias,
                encryptedShares = encryptedShares,
                createdAt = System.currentTimeMillis()
            )

            seedRepository.saveSeed(seed)
            Timber.d("Seed saved successfully: %s", currentAlias)

            // Reset form
            _phrase.value = ""
            _alias.value = ""

        } catch (e: Exception) {
            Timber.e(e, "Failed to save seed")
        } finally {
            _isLoading.value = false
        }
    }
}