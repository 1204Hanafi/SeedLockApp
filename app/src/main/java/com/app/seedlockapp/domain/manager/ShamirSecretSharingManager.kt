package com.app.seedlockapp.domain.manager

import com.app.seedlockapp.util.Constants
import com.mythosil.sss4j.Sss4j
import timber.log.Timber
import java.nio.charset.StandardCharsets
import com.app.seedlockapp.domain.model.Share as DomainShare
import com.mythosil.sss4j.Share as LibraryShare

/**
 * Mengelola semua operasi terkait Shamir's Secret Sharing (SSS).
 * Kelas ini bertindak sebagai wrapper (pembungkus) untuk pustaka `sss4j`,
 * mengabstraksi detail implementasi dan menyediakan antarmuka yang bersih
 * menggunakan model domain ([DomainShare]) aplikasi.
 */
class ShamirSecretSharingManager {

    /**
     * Memecah sebuah rahasia (seed phrase) menjadi beberapa bagian (shares) sesuai
     * dengan threshold dan total shares yang dikonfigurasi di [Constants].
     *
     * @param secret String rahasia yang akan dipecah.
     * @return Sebuah [List] dari [DomainShare] jika proses berhasil, atau `null` jika terjadi
     * kegagalan selama proses pemecahan di pustaka SSS.
     */
    fun split(secret: String): List<DomainShare>? {
        return try {
            if (secret.isBlank()) {
                Timber.w("Attempted to split a blank secret.")
                return null
            }
            val secretBytes = secret.toByteArray(StandardCharsets.UTF_8)

            // Panggil pustaka eksternal untuk melakukan pemecahan.
            val libraryShares: List<LibraryShare> = Sss4j.split(
                secretBytes,
                Constants.SSS_THRESHOLD,
                Constants.SSS_TOTAL_SHARES
            )

            // Konversi dari model pustaka ke model domain internal aplikasi.
            libraryShares.map { libShare ->
                DomainShare(libShare.index, libShare.value)
            }
        } catch (e: Exception) {
            // Tangkap semua kemungkinan exception dari pustaka eksternal.
            Timber.e(e, "Failed to split the secret using sss4j library.")
            null
        }
    }

    /**
     * Merekonstruksi rahasia asli dari daftar `shares` yang diberikan.
     *
     * @param shares Sebuah [List] dari [DomainShare] yang akan digabungkan. Jumlah
     * `shares` harus memenuhi atau melebihi threshold yang ditentukan.
     * @return [String] rahasia yang telah direkonstruksi jika berhasil, atau `null`
     * jika proses rekonstruksi gagal (misalnya, jumlah shares tidak cukup atau data korup).
     */
    fun reconstruct(shares: List<DomainShare>): String? {
        return try {
            if (shares.size < Constants.SSS_THRESHOLD) {
                Timber.e("Not enough shares to reconstruct. Required: ${Constants.SSS_THRESHOLD}, Provided: ${shares.size}")
                return null
            }

            // Konversi dari model domain kembali ke model pustaka.
            val libraryShares: List<LibraryShare> = shares.map { domainShare ->
                LibraryShare(domainShare.index, domainShare.value)
            }

            // Panggil pustaka untuk menggabungkan shares.
            val recoveredBytes = Sss4j.combine(libraryShares)
            String(recoveredBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            // Tangkap semua kemungkinan error dari proses penggabungan.
            Timber.e(e, "Failed to reconstruct secret from shares using sss4j library.")
            null
        }
    }
}