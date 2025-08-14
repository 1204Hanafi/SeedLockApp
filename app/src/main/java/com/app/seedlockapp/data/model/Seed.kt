package com.app.seedlockapp.data.model

/**
 * Merepresentasikan sebuah seed phrase yang disimpan.
 * @param id ID unik untuk seed, biasanya UUID.
 * @param name Alias atau nama yang diberikan pengguna untuk seed ini.
 */
data class Seed(
    val id: String,
    val name: String
)