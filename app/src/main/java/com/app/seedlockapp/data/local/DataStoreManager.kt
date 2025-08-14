package com.app.seedlockapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.seedlockapp.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import timber.log.Timber

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATASTORE_NAME)

/**
 * Mengelola penyimpanan dan pengambilan data dari Jetpack DataStore.
 */
class DataStoreManager(private val context: Context) {

    companion object {
        // Kunci untuk menyimpan daftar ID seed yang ada
        private val SEED_IDS_KEY = stringSetPreferencesKey("seed_ids")
    }

    /**
     * Menyimpan satu set share yang sudah dienkripsi untuk sebuah seed.
     * @param seedId ID unik dari seed.
     * @param shares Map dari nomor share ke Pair<encryptedData, iv>.
     */
    suspend fun saveShares(seedId: String, alias: String, shares: Map<Int, Pair<String, String>>) {
        context.dataStore.edit { preferences ->
            // Simpan alias
            preferences[stringSetPreferencesKey("seed_${seedId}_alias")] = setOf(alias)

            // Simpan shares
            shares.forEach { (shareNumber, shareData) ->
                val (encrypted, iv) = shareData
                val json = JSONObject().apply {
                    put("data", encrypted)
                    put("iv", iv)
                }.toString()
                preferences[stringSetPreferencesKey("seed_${seedId}_share_$shareNumber")] = setOf(json)
            }

            // Tambahkan ID seed ke daftar utama
            val currentIds = preferences[SEED_IDS_KEY] ?: emptySet()
            preferences[SEED_IDS_KEY] = currentIds + seedId
            Timber.d("Shares for seedId '$seedId' saved successfully.")
        }
    }

    /**
     * Memuat semua ID seed yang tersimpan.
     */
    val seedIdsFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[SEED_IDS_KEY] ?: emptySet()
    }

    /**
     * Memuat alias untuk sebuah seedId.
     */
    suspend fun getAliasForSeed(seedId: String): String? {
        var alias: String? = null
        context.dataStore.edit { preferences ->
            alias = preferences[stringSetPreferencesKey("seed_${seedId}_alias")]?.firstOrNull()
        }
        return alias
    }

    /**
     * Memuat semua share terenkripsi untuk sebuah seedId.
     */
    suspend fun loadShares(seedId: String): Map<Int, Pair<String, String>>? {
        val shares = mutableMapOf<Int, Pair<String, String>>()
        var success = true
        context.dataStore.edit { preferences ->
            for (i in 1..Constants.SSS_TOTAL_SHARES) { // Asumsi 3 shares
                val shareKey = stringSetPreferencesKey("seed_${seedId}_share_$i")
                val jsonString = preferences[shareKey]?.firstOrNull()
                if (jsonString != null) {
                    try {
                        val json = JSONObject(jsonString)
                        val data = json.getString("data")
                        val iv = json.getString("iv")
                        shares[i] = Pair(data, iv)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to parse share $i for seed $seedId")
                        success = false
                        return@edit
                    }
                } else {
                    Timber.w("Share $i not found for seed $seedId")
                    // Ini bisa terjadi jika ada korupsi data, tapi kita tetap coba rekonstruksi
                }
            }
        }
        return if (success && shares.isNotEmpty()) shares else null
    }

    /**
     * Menghapus semua data yang terkait dengan sebuah seedId.
     */
    suspend fun deleteSeed(seedId: String) {
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
            Timber.d("All data for seedId '$seedId' deleted.")
        }
    }
}