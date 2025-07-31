package com.app.seedlockapp.data.model

/**
 * Entity class for storing seed data in local storage.
 * This represents the structure used in EncryptedSharedPreferences.
 */
data class SeedEntity(
    val id: String,
    val name: String,
    val encryptedShares: List<String>,
    val createdAt: Long
)

