package com.app.seedlockapp.domain.manager

import com.app.seedlockapp.util.Constants
import com.mythosil.sss4j.Sss4j
import timber.log.Timber
import java.nio.charset.StandardCharsets
import com.app.seedlockapp.domain.model.Share as DomainShare
import com.mythosil.sss4j.Share as LibraryShare

class ShamirSecretSharingManager {

    /**
     * Memecah seed phrase dan mengembalikannya sebagai List dari Model Domain kita.
     */
    fun split(secret: String): List<DomainShare>? {
        return try {
            val secretBytes = secret.toByteArray(StandardCharsets.UTF_8)

            val libraryShares: List<LibraryShare> = Sss4j.split(
                secretBytes,
                Constants.SSS_THRESHOLD,
                Constants.SSS_TOTAL_SHARES
            )

            // Konversi dari model pustaka ke model domain kita.
            libraryShares.map { libShare ->
                DomainShare(libShare.index, libShare.value)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to split secret")
            null
        }
    }

    /**
     * Menerima List dari Model Domain kita untuk merekonstruksi rahasia.
     */
    fun reconstruct(shares: List<DomainShare>): String? {
        return try {
            // Konversi dari model domain kita kembali ke model pustaka.
            val libraryShares: List<LibraryShare> = shares.map { domainShare ->
                LibraryShare(domainShare.index, domainShare.value)
            }

            val recoveredBytes = Sss4j.combine(libraryShares)
            String(recoveredBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            Timber.e(e, "Failed to reconstruct secret from shares")
            null
        }
    }
}