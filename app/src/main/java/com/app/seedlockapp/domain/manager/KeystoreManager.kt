package com.app.seedlockapp.domain.manager

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.app.seedlockapp.util.Constants
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Mengelola semua operasi kriptografi menggunakan Android Keystore System.
 * Bertanggung jawab untuk membuat, mengambil, mengenkripsi, dan mendekripsi data.
 */
class KeystoreManager {
    private val keyStore: KeyStore = KeyStore.getInstance(Constants.ANDROID_KEYSTORE_PROVIDER).apply {
        load(null)
    }

    /**
     * Membuat dan menyimpan sebuah secret key baru di Android Keystore.
     * @param alias Alias unik untuk kunci yang akan dibuat.
     * @return SecretKey yang baru dibuat.
     */
    private fun generateSecretKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, Constants.ANDROID_KEYSTORE_PROVIDER)
        val parameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(Constants.KEY_SIZE_BITS)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(parameterSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Mengambil SecretKey dari Keystore. Jika tidak ada, buat yang baru.
     * @param alias Alias dari kunci yang akan diambil.
     * @return SecretKey yang ada atau yang baru dibuat.
     */
    private fun getSecretKey(alias: String): SecretKey {
        return (keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: generateSecretKey(alias)
    }

    /**
     * Menghapus kunci dari Keystore.
     * @param alias Alias dari kunci yang akan dihapus.
     */
    fun deleteKey(alias: String) {
        try {
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
                Timber.d("Key with alias '$alias' deleted successfully.")
            } else {
                Timber.w("Key with alias '$alias' not found for deletion.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting key with alias '$alias'")
        }
    }

    /**
     * Mengenkripsi data plaintext.
     * @param alias Alias kunci yang akan digunakan untuk enkripsi.
     * @param dataToEncrypt Data dalam bentuk String yang akan dienkripsi.
     * @return Pair dari data terenkripsi (Base64) dan IV (Base64).
     */
    fun encrypt(alias: String, dataToEncrypt: ByteArray): Pair<String, String>? {
        return try {
            val secretKey = getSecretKey(alias)
            val cipher = Cipher.getInstance(Constants.KEY_ENCRYPTION_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val encryptedBytes = cipher.doFinal(dataToEncrypt)
            val ivBytes = cipher.iv

            val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
            val ivBase64 = Base64.encodeToString(ivBytes, Base64.DEFAULT)

            Pair(encryptedBase64, ivBase64)
        } catch (e: Exception) {
            Timber.e(e, "Encryption failed for alias: $alias")
            null
        }
    }

    /**
     * Mendekripsi data yang telah dienkripsi.
     * @param alias Alias kunci yang digunakan untuk enkripsi.
     * @param encryptedData Data terenkripsi dalam format Base64.
     * @param ivBase64 IV yang digunakan saat enkripsi dalam format Base64.
     * @return String plaintext yang telah didekripsi.
     */
    fun decrypt(alias: String, encryptedData: String, ivBase64: String): ByteArray? {
        return try {
            val secretKey = getSecretKey(alias)
            val cipher = Cipher.getInstance(Constants.KEY_ENCRYPTION_ALGORITHM)

            val ivBytes = Base64.decode(ivBase64, Base64.DEFAULT)
            val gcmParameterSpec = GCMParameterSpec(Constants.AUTH_TAG_LENGTH_BITS, ivBytes)

            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)

            val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
            cipher.doFinal(encryptedBytes)
        } catch (e: Exception) {
            Timber.e(e, "Decryption failed for alias: $alias")
            null
        }
    }
}