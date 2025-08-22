package com.app.seedlockapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.seedlockapp.data.model.EncryptedShare
import com.app.seedlockapp.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import timber.log.Timber
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATASTORE_NAME)

/**
 * Mengelola semua operasi penyimpanan dan pengambilan data dari Jetpack DataStore.
 * Kelas ini bertanggung jawab untuk berinteraksi langsung dengan file preferensi yang dienkripsi (jika dikonfigurasi),
 * menangani serialisasi dan deserialisasi data seed.
 *
 * @param context Konteks aplikasi, diperlukan untuk mengakses instance DataStore.
 */
class DataStoreManager(private val context: Context) {

    companion object {
        // Kunci untuk menyimpan daftar ID seed yang ada
        private val SEED_IDS_KEY = stringSetPreferencesKey("seed_ids")
    }

    /**
     * Menyimpan satu set share yang sudah dienkripsi beserta aliasnya untuk sebuah seed.
     * Operasi ini bersifat atomik per `edit`.
     *
     * @param seedId ID unik dari seed.
     * @param alias Nama yang diberikan pengguna untuk seed ini.
     * @param shares Peta yang berisi nomor share sebagai kunci dan [Pair] dari data terenkripsi dan IV sebagai nilai.
     * @return [Result] yang menandakan keberhasilan ([Result.success]) atau kegagalan ([Result.failure]) dengan [Throwable].
     */
    suspend fun saveShares(seedId: String, alias: String, shares: Map<Int, EncryptedShare>): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                // Kunci dinamis untuk alias dan setiap share.
                val aliasKey = stringPreferencesKey("seed_${seedId}_alias")
                preferences[aliasKey] = alias

                shares.forEach { (shareNumber, shareData) ->
                    // Serialisasi objek EncryptedShare menjadi String JSON
                    val jsonString = Json.encodeToString(shareData)
                    val shareKey = stringPreferencesKey("seed_${seedId}_share_$shareNumber")
                    preferences[shareKey] = jsonString
                }

                // Tambahkan ID seed ke daftar utama.
                val currentIds = preferences[SEED_IDS_KEY] ?: emptySet()
                preferences[SEED_IDS_KEY] = currentIds + seedId
            }
            Timber.d("Shares for seedId '$seedId' saved successfully.")
            Result.success(Unit)
        } catch (e: IOException) {
            Timber.e(e, "Failed to write to DataStore for seedId '$seedId'.")
            Result.failure(e)
        } catch (e: Exception) { // Menangkap semua error serialisasi juga
            Timber.e(e, "Failed to serialize or save shares for seedId '$seedId'.")
            Result.failure(e)
        }
    }

    /**
     * Mengambil semua ID seed yang tersimpan sebagai [Flow].
     * Flow ini akan otomatis memancarkan data baru setiap kali ada perubahan.
     */
    val seedIdsFlow: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            // Menangani error saat membaca dari DataStore, misalnya file korup.
            Timber.e(exception, "Error reading seed IDs from DataStore.")
            emit(preferencesOf()) // Pancarkan preferensi kosong sebagai fallback.
        }
        .map { preferences ->
            preferences[SEED_IDS_KEY] ?: emptySet()
        }

    /**
     * Mengambil alias untuk seedId tertentu.
     *
     * @param seedId ID dari seed yang aliasnya ingin diambil.
     * @return Alias sebagai [String], atau null jika tidak ditemukan atau terjadi error.
     */
    suspend fun getAliasForSeed(seedId: String): String? {
        return try {
            val aliasKey = stringPreferencesKey("seed_${seedId}_alias")
            context.dataStore.data.map { preferences ->
                preferences[aliasKey]
            }.first()
        } catch (e: IOException) {
            Timber.e(e, "Failed to read alias for seedId '$seedId' from DataStore.")
            null
        }
    }

    /**
     * Memuat semua share terenkripsi untuk sebuah seedId.
     *
     * @param seedId ID dari seed yang akan dimuat.
     * @return [Result] yang berisi [Map] dari share jika berhasil, atau [Throwable] jika gagal.
     */
    suspend fun loadShares(seedId: String): Result<Map<Int, EncryptedShare>> {
        return try {
            val preferences = context.dataStore.data.first()
            val shares = mutableMapOf<Int, EncryptedShare>()

            for (i in 1..Constants.SSS_TOTAL_SHARES) {
                val shareKey = stringPreferencesKey("seed_${seedId}_share_$i")
                val jsonString = preferences[shareKey]
                preferences[shareKey]?.let { jsonString ->
                    try {
                        // Deserialisasi String JSON menjadi objek EncryptedShare
                        val share = Json.decodeFromString<EncryptedShare>(jsonString)
                        shares[i] = share
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to parse share $i for seed $seedId")
                        // Jika satu share gagal di-parse, seluruh operasi dianggap gagal.
                        return Result.failure(e)
                    }
                }
            }

            if (shares.isEmpty()) {
                Result.failure(Exception("No shares found for seedId '$seedId'."))
            } else {
                Result.success(shares)
            }
        } catch (e: IOException) {
            Timber.e(e, "Failed to read shares for seedId '$seedId' from DataStore.")
            Result.failure(e)
        }
    }

    /**
     * Menghapus semua data yang terkait dengan sebuah seedId dari DataStore.
     *
     * @param seedId ID dari seed yang akan dihapus.
     * @return [Result] yang menandakan keberhasilan atau kegagalan.
     */
    suspend fun deleteSeed(seedId: String): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                // Hapus alias
                preferences.remove(stringPreferencesKey("seed_${seedId}_alias"))
                // Hapus shares
                for (i in 1..Constants.SSS_TOTAL_SHARES) {
                    preferences.remove(stringPreferencesKey("seed_${seedId}_share_$i"))
                }
                // Hapus ID dari daftar utama
                val currentIds = preferences[SEED_IDS_KEY] ?: emptySet()
                if (currentIds.contains(seedId)) {
                    preferences[SEED_IDS_KEY] = currentIds - seedId
                }
            }
            Timber.d("All data for seedId '$seedId' deleted.")
            Result.success(Unit)
        } catch (e: IOException) {
            Timber.e(e, "Failed to delete seed data for seedId '$seedId'.")
            Result.failure(e)
        }
    }
}