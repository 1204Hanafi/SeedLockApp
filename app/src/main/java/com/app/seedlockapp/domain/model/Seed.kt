package com.app.seedlockapp.domain.model

/**
 * Data class representing a seed phrase entry.
 * 
 * @property id Unique identifier for the seed
 * @property name User-defined alias/name for the seed
 * @property encryptedShares List of encrypted Shamir shares
 * @property createdAt Timestamp when the seed was created
 */
data class Seed(
    val id: String,
    val name: String,
    val encryptedShares: List<String>,
    val createdAt: Long = System.currentTimeMillis()
)

