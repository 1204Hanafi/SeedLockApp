package com.app.seedlockapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.seedlockapp.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONException
import org.json.JSONObject
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
    suspend fun saveShares(seedId: String, alias: String, shares: Map<Int, Pair<String, String>>): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                // Kunci dinamis untuk alias dan setiap share.
                val aliasKey = stringSetPreferencesKey("seed_${seedId}_alias")
                preferences[aliasKey] = setOf(alias)

                shares.forEach { (shareNumber, shareData) ->
                    val (encrypted, iv) = shareData
                    val json = JSONObject().apply {
                        put("data", encrypted)
                        put("iv", iv)
                    }.toString()
                    val shareKey = stringSetPreferencesKey("seed_${seedId}_share_$shareNumber")
                    preferences[shareKey] = setOf(json)
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
        } catch (e: JSONException) {
            Timber.e(e, "Failed to create JSON for shares of seedId '$seedId'.")
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
            emit(emptyPreferences()) // Pancarkan set kosong sebagai fallback.
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
            var alias: String? = null
            context.dataStore.edit { preferences ->
                val aliasKey = stringSetPreferencesKey("seed_${seedId}_alias")
                alias = preferences[aliasKey]?.firstOrNull()
            }
            alias
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
    suspend fun loadShares(seedId: String): Result<Map<Int, Pair<String, String>>> {
        val shares = mutableMapOf<Int, Pair<String, String>>()
        return try {
            context.dataStore.edit { preferences ->
                for (i in 1..Constants.SSS_TOTAL_SHARES) {
                    val shareKey = stringSetPreferencesKey("seed_${seedId}_share_$i")
                    val jsonString = preferences[shareKey]?.firstOrNull()
                    if (jsonString != null) {
                        try {
                            val json = JSONObject(jsonString)
                            val data = json.getString("data")
                            val iv = json.getString("iv")
                            shares[i] = Pair(data, iv)
                        } catch (e: JSONException) {
                            // Jika satu share gagal di-parse, seluruh operasi dianggap gagal.
                            Timber.e(e, "Failed to parse share $i for seed $seedId")
                            throw e // Lemparkan lagi untuk ditangkap oleh blok catch luar.
                        }
                    } else {
                        Timber.w("Share $i not found for seed $seedId. This might be acceptable if threshold is met.")
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
        } catch (e: JSONException) {
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
                preferences.remove(stringSetPreferencesKey("seed_${seedId}_alias"))
                // Hapus shares
                for (i in 1..Constants.SSS_TOTAL_SHARES) {
                    preferences.remove(stringSetPreferencesKey("seed_${seedId}_share_$i"))
                }
                // Hapus ID dari daftar utama
                val currentIds = preferences[SEED_IDS_KEY] ?: emptySet()
                preferences[SEED_IDS_KEY] = currentIds - seedId
            }
            Timber.d("All data for seedId '$seedId' deleted.")
            Result.success(Unit)
        } catch (e: IOException) {
            Timber.e(e, "Failed to delete seed data for seedId '$seedId'.")
            Result.failure(e)
        }
    }
}