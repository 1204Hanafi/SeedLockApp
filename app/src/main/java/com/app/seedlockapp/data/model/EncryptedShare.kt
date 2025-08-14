package com.app.seedlockapp.data.model

/**
 * Merepresentasikan satu bagian (share) dari seed phrase yang telah dienkripsi.
 * Model data ini digunakan untuk proses penyimpanan ke DataStore.
 *
 * @param encryptedData Data share yang sudah dienkripsi dalam bentuk Base64 String.
 * @param iv Initialization Vector yang digunakan untuk enkripsi, dalam bentuk Base64 String.
 */
data class EncryptedShare(
    val encryptedData: String,
    val iv: String
)