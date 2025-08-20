package com.app.seedlockapp.data.repository

import com.app.seedlockapp.data.local.DataStoreManager
import com.app.seedlockapp.data.model.Seed
import com.app.seedlockapp.domain.manager.KeystoreManager
import com.app.seedlockapp.domain.manager.ShamirSecretSharingManager
import com.app.seedlockapp.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.security.GeneralSecurityException
import java.util.UUID
import com.app.seedlockapp.domain.model.Share as DomainShare

/**
 * Repository yang berfungsi sebagai Single Source of Truth untuk semua data terkait seed.
 * Kelas ini mengoordinasikan operasi antara sumber data lokal ([DataStoreManager]),
 * manajemen kunci kriptografi ([KeystoreManager]), dan logika pembagian rahasia ([ShamirSecretSharingManager]).
 * Tujuannya adalah untuk mengabstraksi kompleksitas operasi data dari ViewModel.
 */
class SeedRepository(
    private val dataStoreManager: DataStoreManager,
    private val keystoreManager: KeystoreManager,
    private val shamirManager: ShamirSecretSharingManager
) {

    /**
     * Menyimpan seed phrase baru dengan aman.
     * Proses ini melibatkan tiga langkah utama:
     * 1. **Split**: Memecah `phrase` menjadi beberapa bagian (shares) menggunakan SSS.
     * 2. **Encrypt**: Mengenkripsi setiap `share` secara individual menggunakan kunci unik dari Android Keystore.
     * 3. **Save**: Menyimpan `shares` yang sudah dienkripsi ke DataStore.
     *
     * @param phrase Seed phrase yang akan disimpan.
     * @param alias Nama atau label untuk seed phrase ini.
     * @return [Result.success] jika semua proses berhasil, atau [Result.failure] dengan [Exception] yang relevan jika terjadi kegagalan.
     */
    suspend fun saveSeed(phrase: String, alias: String): Result<Unit> {
        val startTime = System.currentTimeMillis()
        return try {
            // 1. Split phrase menggunakan SSS
            val shares: List<DomainShare> = shamirManager.split(phrase)
                ?: return Result.failure(Exception("Gagal memecah seed phrase (SSS split returned null)."))
            val splitTime = System.currentTimeMillis()
            Timber.d("SSS Split Latency: ${splitTime - startTime} ms")

            // 2. Enkripsi setiap share
            val seedId = UUID.randomUUID().toString()
            val encryptedShares = mutableMapOf<Int, Pair<String, String>>()
            shares.forEach { share ->
                val keyAlias = "${Constants.KEY_ALIAS_PREFIX}${seedId}_share_${share.index}"
                val encrypted = keystoreManager.encrypt(keyAlias, share.value)
                    ?: return Result.failure(GeneralSecurityException("Gagal mengenkripsi bagian #${share.index}."))
                encryptedShares[share.index] = encrypted
            }
            val encryptTime = System.currentTimeMillis()
            Timber.d("Total Encryption Latency: ${encryptTime - splitTime} ms")

            // 3. Simpan ke DataStore
            dataStoreManager.saveShares(seedId, alias, encryptedShares)
                .onFailure { return Result.failure(it) } // Propagate failure from DataStore
            val saveTime = System.currentTimeMillis()
            Timber.d("DataStore Save Latency: ${saveTime - encryptTime} ms")

            Result.success(Unit)
        } catch (e: GeneralSecurityException) {
            Timber.e(e, "A security exception occurred during seed saving process.")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "An unexpected error occurred while saving the seed.")
            Result.failure(e)
        }
    }


    /**
     * Memuat dan merekonstruksi seed phrase dari penyimpanan.
     * Proses ini adalah kebalikan dari `saveSeed`:
     * 1. **Load**: Mengambil `shares` terenkripsi dari DataStore.
     * 2. **Decrypt**: Mendekripsi setiap `share` menggunakan kunci yang sesuai dari Keystore.
     * 3. **Reconstruct**: Menggabungkan `shares` yang sudah didekripsi untuk mendapatkan kembali seed phrase asli.
     *
     * @param seedId ID dari seed yang akan diambil.
     * @return [Result.success] dengan seed phrase asli jika berhasil, atau [Result.failure] jika gagal.
     */
    suspend fun getDecryptedSeed(seedId: String): Result<String> {
        val startTime = System.currentTimeMillis()
        return try {
            // 1. Load encrypted shares dari DataStore
            val encryptedSharesResult = dataStoreManager.loadShares(seedId)
            val encryptedShares = encryptedSharesResult.getOrElse {
                return Result.failure(it) // Gagal memuat, langsung kembalikan error.
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
                    // Jika satu share gagal didekripsi, tetap lanjutkan.
                    // Rekonstruksi mungkin masih berhasil jika jumlah share yang sukses memenuhi threshold.
                    Timber.w("Decryption failed for share $shareNumber, skipping.")
                }
            }
            val decryptTime = System.currentTimeMillis()
            Timber.d("Total Decryption Latency: ${decryptTime - loadTime} ms")

            // 3. Rekonstruksi menggunakan SSS
            if (decryptedShares.size < Constants.SSS_THRESHOLD) {
                return Result.failure(Exception("Data tidak cukup untuk merekonstruksi seed. Hanya ${decryptedShares.size} dari minimal ${Constants.SSS_THRESHOLD} bagian yang berhasil didekripsi."))
            }

            val reconstructed = shamirManager.reconstruct(decryptedShares)
            val reconstructTime = System.currentTimeMillis()
            Timber.d("SSS Reconstruct Latency: ${reconstructTime - decryptTime} ms")

            reconstructed?.let { Result.success(it) }
                ?: Result.failure(Exception("Gagal merekonstruksi seed phrase (SSS combine returned null)."))

        } catch (e: GeneralSecurityException) {
            Timber.e(e, "A security exception occurred during seed decryption process.")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "An unexpected error occurred while getting the decrypted seed.")
            Result.failure(e)
        }
    }


    /**
     * Mendapatkan daftar semua seed yang tersimpan (hanya ID dan alias) sebagai [Flow].
     * Data ini aman untuk ditampilkan di UI karena tidak mengandung informasi sensitif.
     *
     * @return [Flow] yang memancarkan daftar [Seed].
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
     *
     * @param seedId ID dari seed.
     * @return Alias sebagai [String] atau null jika tidak ditemukan.
     */
    suspend fun getAlias(seedId: String): String? {
        return dataStoreManager.getAliasForSeed(seedId)
    }


    /**
     * Menghapus seed dan semua data terkaitnya secara permanen.
     * Ini termasuk data dari DataStore dan kunci dari Android Keystore.
     *
     * @param seedId ID dari seed yang akan dihapus.
     * @return [Result.success] jika berhasil, [Result.failure] jika gagal.
     */
    suspend fun deleteSeed(seedId: String): Result<Unit> {
        return try {
            // Hapus data dari DataStore
            dataStoreManager.deleteSeed(seedId)
                .onFailure { return Result.failure(it) } // Propagate error

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