package com.app.seedlockapp.domain.manager

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import com.app.seedlockapp.util.Constants
import timber.log.Timber
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.UnrecoverableKeyException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Mengelola semua operasi kriptografi menggunakan Android Keystore System.
 * Kelas ini bertanggung jawab untuk:
 * - Membuat dan mengambil kunci enkripsi yang disimpan dengan aman di hardware-backed storage (jika tersedia).
 * - Mengenkripsi dan mendekripsi data menggunakan algoritma AES/GCM/NoPadding.
 * - Mengelola siklus hidup kunci, termasuk penghapusan.
 * - Menyediakan Cipher untuk otentikasi biometrik.
 */
class KeystoreManager {
    private val keyStore: KeyStore = KeyStore.getInstance(Constants.ANDROID_KEYSTORE_PROVIDER).apply {
        load(null)
    }

    /**
     * Membuat atau mengambil kunci rahasia yang terikat dengan otentikasi biometrik.
     * Kunci ini hanya dapat digunakan setelah pengguna berhasil melakukan otentikasi.
     * Jika biometrik pengguna berubah (misalnya, sidik jari baru ditambahkan), kunci ini akan
     * menjadi tidak valid secara permanen ([KeyPermanentlyInvalidatedException]).
     *
     * @return [SecretKey] yang siap digunakan untuk operasi kripto setelah otentikasi.
     * @throws KeyStoreException jika terjadi masalah saat mengakses Keystore.
     */
    private fun getSecretKeyForAuth(): SecretKey {
        val alias = "biometric_auth_key"
        try {
            val existingKey = (keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.secretKey
            if (existingKey != null) {
                return existingKey
            }
        } catch (e: UnrecoverableKeyException) {
            // Ini bisa terjadi jika biometrik telah berubah. Hapus kunci lama.
            Timber.w(e, "Auth key is unrecoverable (likely due to biometric change). Deleting and regenerating.")
            deleteKey(alias)
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, Constants.ANDROID_KEYSTORE_PROVIDER)
        val parameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(Constants.KEY_SIZE_BITS)
            .setUserAuthenticationRequired(true) // MEWAJIBKAN OTENTIKASI PENGGUNA
            .setInvalidatedByBiometricEnrollment(true) // Kunci tidak valid jika biometrik berubah
            .build()

        keyGenerator.init(parameterSpec)
        return keyGenerator.generateKey()
    }


    /**
     * Mendapatkan instance [Cipher] yang diinisialisasi untuk enkripsi dan terikat dengan kunci otentikasi biometrik.
     * [Cipher] ini akan digunakan untuk membuat [android.hardware.biometrics.BiometricPrompt.CryptoObject].
     *
     * @return [Cipher] yang siap digunakan, atau `null` jika terjadi kegagalan (misalnya, Keystore tidak tersedia).
     */
    fun getAuthCipher(): Cipher? {
        return try {
            val secretKey = getSecretKeyForAuth()
            val cipher = Cipher.getInstance(Constants.KEY_ENCRYPTION_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            cipher
        } catch (e: Exception) {
            // Tangkap semua kemungkinan exception terkait kriptografi.
            Timber.e(e, "Failed to create and initialize the auth cipher.")
            null
        }
    }


    /**
     * Membuat dan menyimpan sebuah secret key baru di Android Keystore untuk enkripsi data per-share.
     * Kunci ini tidak memerlukan otentikasi pengguna untuk digunakan.
     *
     * @param alias Alias unik untuk kunci yang akan dibuat.
     * @return [SecretKey] yang baru dibuat.
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
            .setRandomizedEncryptionRequired(true) // Memastikan IV yang berbeda untuk setiap enkripsi
            .build()

        keyGenerator.init(parameterSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Mengambil [SecretKey] dari Keystore. Jika kunci dengan alias yang diberikan tidak ada,
     * maka kunci baru akan dibuat secara otomatis.
     *
     * @param alias Alias dari kunci yang akan diambil atau dibuat.
     * @return [SecretKey] yang ada atau yang baru dibuat.
     */
    private fun getSecretKey(alias: String): SecretKey {
        return (keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: generateSecretKey(alias)
    }

    /**
     * Menghapus kunci dari Keystore secara permanen.
     *
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
        } catch (e: KeyStoreException) {
            Timber.e(e, "Error deleting key with alias '$alias' from Keystore.")
        }
    }

    /**
     * Mengenkripsi data plaintext menggunakan kunci yang sesuai dengan alias yang diberikan.
     *
     * @param alias Alias kunci yang akan digunakan untuk enkripsi.
     * @param dataToEncrypt Data dalam bentuk [ByteArray] yang akan dienkripsi.
     * @return [Pair] dari data terenkripsi (Base64 String) dan IV (Base64 String), atau `null` jika enkripsi gagal.
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
            // Tangkap semua exception terkait enkripsi (misal: NoSuchAlgorithm, InvalidKey, etc.)
            Timber.e(e, "Encryption failed for alias: $alias")
            null
        }
    }


    /**
     * Mendekripsi data yang telah dienkripsi menggunakan kunci dan IV yang sesuai.
     *
     * @param alias Alias kunci yang digunakan saat enkripsi.
     * @param encryptedData Data terenkripsi dalam format Base64 String.
     * @param ivBase64 IV yang digunakan saat enkripsi dalam format Base64 String.
     * @return [ByteArray] dari data plaintext yang telah didekripsi, atau `null` jika dekripsi gagal
     * (misalnya, kunci salah, IV salah, atau data terkorupsi).
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
        } catch (e: KeyPermanentlyInvalidatedException) {
            // Error ini spesifik terjadi jika kunci biometrik tidak valid lagi.
            Timber.e(e, "Decryption failed because the key for alias '$alias' has been permanently invalidated.")
            // Pertimbangkan untuk menghapus kunci yang tidak valid di sini jika perlu.
            // deleteKey(alias)
            null
        }
        catch (e: Exception) {
            // Tangkap semua error dekripsi lainnya (misal: AEADBadTagException untuk data korup)
            Timber.e(e, "Decryption failed for alias: $alias")
            null
        }
    }
}