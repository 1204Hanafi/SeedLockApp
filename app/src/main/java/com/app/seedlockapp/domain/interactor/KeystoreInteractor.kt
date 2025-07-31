package com.app.seedlockapp.domain.interactor

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.app.seedlockapp.session.SessionManager
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interactor for handling encryption and decryption using Android Keystore.
 * Provides secure storage for cryptographic keys and performs AES encryption/decryption.
 *
 * This class leverages the Android Keystore system to provide hardware-backed security
 * for cryptographic operations. Keys are stored securely in the device's Trusted Execution
 * Environment (TEE) when available.
 */
@Singleton
class KeystoreInteractor @Inject constructor(
    private val sessionManager: SessionManager
) {

    companion object {
        private const val AUTH_KEY_ALIAS = "seedlock_auth_key"
        private const val NO_AUTH_KEY_ALIAS = "seedlock_no_auth_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun createAuthenticatedKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(AUTH_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true) // Key requires user authentication
                .setInvalidatedByBiometricEnrollment(true) // Invalidate key if new biometrics are enrolled
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun createNonAuthenticatedKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(NO_AUTH_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun getAuthenticatedKey(): SecretKey {
        return (keyStore.getEntry(AUTH_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: createAuthenticatedKey()
    }

    private fun getNonAuthenticatedKey(): SecretKey {
        return (keyStore.getEntry(NO_AUTH_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: createNonAuthenticatedKey()
    }

    private fun getAppropriateKey(): SecretKey {
        return if (sessionManager.isAuthenticated.value) {
            getNonAuthenticatedKey()
        } else {
            getAuthenticatedKey()
        }
    }

    /**
     * Returns a Cipher instance initialized for encryption with the Keystore key.
     * This cipher can be used with BiometricPrompt.CryptoObject.
     */
    fun getEncryptCipher(): Cipher {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, getAppropriateKey())
        return cipher
    }

    /**
     * Returns a Cipher instance initialized for decryption with the Keystore key.
     * This cipher can be used with BiometricPrompt.CryptoObject.
     */
    fun getDecryptCipher(iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.DECRYPT_MODE, getAppropriateKey(), IvParameterSpec(iv))
        return cipher
    }

    /**
     * Encrypts data using the provided Cipher (which should be from getEncryptCipher).
     *
     * @param cipher The Cipher instance initialized for encryption.
     * @param bytes The data to encrypt as a ByteArray.
     * @return A Pair containing the encrypted data and the IV used for encryption.
     */
    fun encrypt(cipher: Cipher, bytes: ByteArray): Pair<ByteArray, ByteArray> {
        val encryptedBytes = cipher.doFinal(bytes)
        Timber.d("Data encrypted using Keystore.")
        return Pair(encryptedBytes, cipher.iv)
    }

    /**
     * Decrypts data using the provided Cipher (which should be from getDecryptCipher).
     *
     * @param cipher The Cipher instance initialized for decryption.
     * @param encryptedData The encrypted data as ByteArray.
     * @return The decrypted data as ByteArray.
     */
    fun decrypt(cipher: Cipher, encryptedData: ByteArray): ByteArray {
        val decryptedBytes = cipher.doFinal(encryptedData)
        Timber.d("Data decrypted using Keystore.")
        return decryptedBytes
    }
}