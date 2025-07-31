package com.app.seedlockapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.app.seedlockapp.data.model.SeedEntity
import com.app.seedlockapp.domain.model.Seed
import com.app.seedlockapp.security.EncryptionManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Implementation of SeedRepository using regular SharedPreferences and EncryptionManager for secure local storage.
 * Provides secure local storage for seed phrase data.
 */
@Singleton
class SeedRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val encryptionManager: EncryptionManager
) : SeedRepository {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("seedlock_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val SEEDS_ENCRYPTED_DATA_KEY = "seeds_encrypted_data"
        private const val SEEDS_IV_KEY = "seeds_iv"
    }

    override suspend fun saveSeed(seed: Seed) = withContext(Dispatchers.IO) {
        try {
            val currentSeeds = getAllSeedsInternal().toMutableList()
            val existingSeedIndex = currentSeeds.indexOfFirst { it.id == seed.id }

            val seedEntity = SeedEntity(
                id = seed.id,
                name = seed.name,
                encryptedShares = seed.encryptedShares,
                createdAt = seed.createdAt
            )

            if (existingSeedIndex != -1) {
                currentSeeds[existingSeedIndex] = seedEntity
            } else {
                // As per user's request, only one seed is supported. If a new seed is added, replace the old one.
                // If multiple seeds were intended, this logic would need to be adjusted.
                currentSeeds.clear() // Clear existing to enforce single seed
                currentSeeds.add(seedEntity)
            }

            val json = gson.toJson(currentSeeds)
            val (encryptedData, iv) = encryptionManager.encrypt(json)

            sharedPreferences.edit {
                putString(SEEDS_ENCRYPTED_DATA_KEY, encryptedData.toBase64())
                    .putString(SEEDS_IV_KEY, iv.toBase64())
            }
            Timber.d("Seed saved: %s", seed.name)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save seed: %s", seed.name)
            throw e
        }
    }

    override suspend fun getSeed(id: String): Seed? = withContext(Dispatchers.IO) {
        try {
            val seeds = getAllSeedsInternal()
            val seedEntity = seeds.firstOrNull { it.id == id }
            return@withContext seedEntity?.let { entity ->
                Seed(
                    id = entity.id,
                    name = entity.name,
                    encryptedShares = entity.encryptedShares,
                    createdAt = entity.createdAt
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get seed: %s", id)
            throw e
        }
    }

    override suspend fun getAllSeeds(): List<Seed> = withContext(Dispatchers.IO) {
        try {
            val seeds = getAllSeedsInternal()
            return@withContext seeds.map { entity ->
                Seed(
                    id = entity.id,
                    name = entity.name,
                    encryptedShares = entity.encryptedShares,
                    createdAt = entity.createdAt
                )
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get all seeds")
            throw e
        }
    }

    override suspend fun deleteSeed(id: String) = withContext(Dispatchers.IO) {
        try {
            val currentSeeds = getAllSeedsInternal().toMutableList()
            val removed = currentSeeds.removeIf { it.id == id }
            if (removed) {
                val json = gson.toJson(currentSeeds)
                val (encryptedData, iv) = encryptionManager.encrypt(json)
                sharedPreferences.edit {
                    putString(SEEDS_ENCRYPTED_DATA_KEY, encryptedData.toBase64())
                        .putString(SEEDS_IV_KEY, iv.toBase64())
                }
                Timber.d("Seed deleted: %s", id)
            } else {
                Timber.w("Seed not found for deletion: %s", id)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete seed: %s", id)
            throw e
        }
    }

    private fun getAllSeedsInternal(): List<SeedEntity> {
        val encryptedDataString = sharedPreferences.getString(SEEDS_ENCRYPTED_DATA_KEY, null)
        val ivString = sharedPreferences.getString(SEEDS_IV_KEY, null)

        if (encryptedDataString.isNullOrEmpty() || ivString.isNullOrEmpty()) {
            return emptyList()
        }

        return try {
            val encryptedData = encryptedDataString.fromBase64()
            val iv = ivString.fromBase64()
            val decryptedJson = encryptionManager.decrypt(encryptedData, iv)
            val type = object : TypeToken<List<SeedEntity>>() {}.type
            gson.fromJson(decryptedJson, type)
        } catch (e: Exception) {
            Timber.e(e, "Failed to decrypt or parse seeds data.")
            emptyList()
        }
    }

    private fun ByteArray.toBase64(): String = android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)
    private fun String.fromBase64(): ByteArray = android.util.Base64.decode(this, android.util.Base64.NO_WRAP)
}


