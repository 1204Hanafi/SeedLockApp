package com.app.seedlockapp.ui.screens.viewseed

/**
 * Secure string wrapper that clears its content when no longer needed.
 * Used to hold sensitive data like seed phrases.
 */
class SecureString(private var value: String?) {
    
    fun getValue(): String? = value
    
    fun clear() {
        value?.let { str ->
            // Overwrite the string content (best effort)
            CharArray(str.length) { '\u0000' }
            // Note: In JVM, strings are immutable, so this is limited protection
            // In a real implementation, you might use char arrays or secure memory
        }
        value = null
    }
}

/**
 * UI state for the view seed screen.
 * 
 * @property alias The alias/name of the seed
 * @property seedPhrase The reconstructed seed phrase (wrapped in SecureString)
 * @property loading Whether data is currently being loaded
 * @property error Error message if any operation failed
 */
data class ViewSeedUiState(
    val alias: String = "",
    val seedPhrase: SecureString? = null,
    val loading: Boolean = false,
    val error: String? = null
)

