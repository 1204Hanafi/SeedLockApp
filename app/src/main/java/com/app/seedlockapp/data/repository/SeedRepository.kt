package com.app.seedlockapp.data.repository

import com.app.seedlockapp.data.local.DataStoreManager
import com.app.seedlockapp.data.model.Seed
import com.app.seedlockapp.domain.manager.KeystoreManager
import com.app.seedlockapp.domain.manager.ShamirSecretSharingManager
import com.app.seedlockapp.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.UUID
import com.app.seedlockapp.domain.model.Share as DomainShare

/**
 * Repository sebagai Single Source of Truth untuk data seed.
 * Mengkoordinasikan operasi antara DataStore, Keystore, dan SSS.
 */
class SeedRepository(
    private val dataStoreManager: DataStoreManager,
    private val keystoreManager: KeystoreManager,
    private val shamirManager: ShamirSecretSharingManager
) {

    /**
     * Menyimpan seed phrase baru.
     * Proses: Split -> Encrypt -> Save
     */
    suspend fun saveSeed(phrase: String, alias: String): Result<Unit> {
        return try {
            val startTime = System.currentTimeMillis()
            // 1. Split phrase menggunakan SSS -> Menerima List<DomainShare>
            val shares: List<DomainShare>? = shamirManager.split(phrase)
            if (shares.isNullOrEmpty()) {
                Timber.e("SSS split failed")
                return Result.failure(Exception("Gagal memecah seed phrase."))
            }
            val splitTime = System.currentTimeMillis()
            Timber.d("SSS Split Latency: ${splitTime - startTime} ms")

            // 2. Enkripsi setiap share
            val seedId = UUID.randomUUID().toString()
            val encryptedShares = mutableMapOf<Int, Pair<String, String>>()

            // Iterasi melalui List<DomainShare>
            for (share in shares) {
                val keyAlias = "${Constants.KEY_ALIAS_PREFIX}${seedId}_share_${share.index}"
                // Enkripsi 'share.value'
                val encrypted = keystoreManager.encrypt(keyAlias, share.value)
                if (encrypted != null) {
                    encryptedShares[share.index] = encrypted
                } else {
                    Timber.e("Encryption failed for share ${share.index}")
                    return Result.failure(Exception("Gagal mengenkripsi bagian #${share.index}."))
                }
            }
            val encryptTime = System.currentTimeMillis()
            Timber.d("Total Encryption Latency: ${encryptTime - splitTime} ms")

            // 3. Simpan ke DataStore
            dataStoreManager.saveShares(seedId, alias, encryptedShares)
            val saveTime = System.currentTimeMillis()
            Timber.d("DataStore Save Latency: ${saveTime - encryptTime} ms")

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save seed")
            Result.failure(e)
        }
    }

    /**
     * Memuat dan merekonstruksi seed phrase.
     * Proses: Load -> Decrypt -> Reconstruct
     */
    suspend fun getDecryptedSeed(seedId: String): Result<String> {
        return try {
            val startTime = System.currentTimeMillis()
            // 1. Load encrypted shares dari DataStore
            val encryptedShares = dataStoreManager.loadShares(seedId)
            if (encryptedShares.isNullOrEmpty()) {
                return Result.failure(Exception("Tidak ada data untuk seed ini."))
            }
            val loadTime = System.currentTimeMillis()
            Timber.d("DataStore Load Latency: ${loadTime - startTime} ms")

            // 2. Dekripsi setiap share
            val decryptedShares = mutableListOf<DomainShare>()
            encryptedShares.forEach { (shareNumber, shareData) ->
                val keyAlias = "${Constants.KEY_ALIAS_PREFIX}${seedId}_share_$shareNumber"
                val (encrypted, iv) = shareData
                val decrypted: ByteArray? = keystoreManager.decrypt(keyAlias, encrypted, iv)
                if (decrypted != null) {
                    decryptedShares.add(DomainShare(index = shareNumber, value = decrypted))
                } else {
                    Timber.w("Decryption failed for share $shareNumber, skipping.")
                }
            }
            val decryptTime = System.currentTimeMillis()
            Timber.d("Total Decryption Latency: ${decryptTime - loadTime} ms")


            // 3. Rekonstruksi menggunakan SSS
            if (decryptedShares.size < Constants.SSS_THRESHOLD) { // Sesuai threshold
                return Result.failure(Exception("Data tidak cukup untuk merekonstruksi seed."))
            }
//
            val reconstructed = shamirManager.reconstruct(decryptedShares)
            val reconstructTime = System.currentTimeMillis()
            Timber.d("SSS Reconstruct Latency: ${reconstructTime - decryptTime} ms")

            if (reconstructed != null) {
                Result.success(reconstructed)
            } else {
                Result.failure(Exception("Gagal merekonstruksi seed phrase."))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get decrypted seed")
            Result.failure(e)
        }
    }

    /**
     * Mendapatkan daftar semua seed yang tersimpan (ID dan alias).
     */
    fun getAllSeeds(): Flow<List<Seed>> {
        return dataStoreManager.seedIdsFlow.map { ids ->
            ids.mapNotNull { id ->
                dataStoreManager.getAliasForSeed(id)?.let { alias ->
                    Seed(id, alias)
                }
            }
        }
    }

    /**
     * Mendapatkan alias untuk seedId tertentu.
     */
    suspend fun getAlias(seedId: String): String? {
        return dataStoreManager.getAliasForSeed(seedId)
    }

    /**
     * Menghapus seed dan semua data terkaitnya.
     */
    suspend fun deleteSeed(seedId: String): Result<Unit> {
        return try {
            // Hapus data dari DataStore
            dataStoreManager.deleteSeed(seedId)
            // Hapus kunci dari Keystore
            for (i in 1..Constants.SSS_TOTAL_SHARES) {
                keystoreManager.deleteKey("${Constants.KEY_ALIAS_PREFIX}${seedId}_share_$i")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete seed $seedId")
            Result.failure(e)
        }
    }
}