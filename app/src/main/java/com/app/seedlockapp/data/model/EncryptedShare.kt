package com.app.seedlockapp.data.model

import java.security.spec.InvalidParameterSpecException

/**
 * Merepresentasikan satu bagian (share) dari seed phrase yang telah dienkripsi.
 * Model data ini digunakan untuk menyimpan hasil enkripsi dari satu `share` Shamir's Secret Sharing (SSS)
 * ke dalam DataStore, yang mencakup data terenkripsi dan Initialization Vector (IV) yang digunakan.
 *
 * @property encryptedData Data share yang sudah dienkripsi, direpresentasikan sebagai String Base64.
 * @property iv Initialization Vector yang digunakan selama proses enkripsi, juga dalam format String Base64.
 * IV ini esensial untuk proses dekripsi dengan mode GCM.
 */
data class EncryptedShare(
    val encryptedData: String,
    val iv: String
) {
    init {
        // Validasi dasar untuk memastikan properti tidak kosong saat objek dibuat.
        if (encryptedData.isBlank() || iv.isBlank()) {
            throw InvalidParameterSpecException("Encrypted data and IV cannot be blank.")
        }
    }
}