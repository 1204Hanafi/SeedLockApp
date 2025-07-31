package com.app.seedlockapp.domain.interactor

import com.app.seedlockapp.domain.model.Share
import timber.log.Timber
import javax.inject.Inject
import com.mythosil.sss4j.Sss4j

/**
 * Interactor for Shamir\"s Secret Sharing operations.
 * Implements a 2-of-3 threshold scheme for splitting and reconstructing secrets
 * using SecretSharing4j library.
 *
 * @property THRESHOLD The minimum number of shares required to reconstruct the secret (2).
 * @property NUM_SHARES The total number of shares to generate (3).
 */
class SSSInteractor @Inject constructor() {

    companion object {
    private const val THRESHOLD = 2
    private const val NUM_SHARES = 3
    }

    /**
     * Splits a secret into multiple shares using Shamir\"s Secret Sharing.
     *
     * The secret is split into [NUM_SHARES] shares, where any [THRESHOLD] shares
     * can be used to reconstruct the original secret.
     *
     * @param secret The secret to split as a string.
     * @return List of [Share] objects containing the split secret.
     */
    fun splitSecret(secret: String): List<Share> {
        val secretBytes = secret.toByteArray(Charsets.UTF_8)
        val sssShares = Sss4j.split(secretBytes, THRESHOLD, NUM_SHARES)

        val shareList = mutableListOf<Share>()
        for (i in sssShares.indices) {
            // sss4j returns shares as byte arrays, we need to map them to our Share data class
            shareList.add(Share(sssShares[i].index, sssShares[i].value))
        }

        Timber.d("Secret split into %d shares with threshold %d using SecretSharing4j", NUM_SHARES, THRESHOLD)
        return shareList
    }

    /**
     * Reconstructs a secret from a subset of shares.
     *
     * Uses Lagrange interpolation to reconstruct the original secret from
     * the provided shares. At least [THRESHOLD] shares are required.
     *
     * @param shares List of [Share] objects (at least [THRESHOLD] shares required).
     * @return The reconstructed secret as a string.
     * @throws IllegalArgumentException if insufficient shares are provided.
     */
    fun reconstructSecret(shares: List<Share>): String {
        if (shares.size < THRESHOLD) {
            throw IllegalArgumentException("Insufficient shares for reconstruction. Need at least $THRESHOLD shares.")
        }

        // Convert our Share objects back to sss4j's Share objects
        val sss4jShares = shares.map { com.mythosil.sss4j.Share(it.index, it.value) }

        val reconstructedBytes = Sss4j.combine(sss4jShares)
        val reconstructedSecret = String(reconstructedBytes, Charsets.UTF_8)
        Timber.d("Secret reconstructed from %d shares using SecretSharing4j", shares.size)
        return reconstructedSecret
    }

    /**
     * Validates if the provided shares are sufficient for reconstruction.
     *
     * @param shares List of shares to validate.
     * @return True if shares are sufficient and valid, false otherwise.
     */
    fun validateShares(shares: List<Share>): Boolean {
        if (shares.size < THRESHOLD) {
            Timber.w("Insufficient shares: %d provided, %d required", shares.size, THRESHOLD)
            return false
        }

        // Basic validation: check if all shares have the same length
        val firstShareLength = shares.firstOrNull()?.value?.size ?: 0
        val allSameLength = shares.all { it.value.size == firstShareLength }

        if (!allSameLength) {
            Timber.w("Shares have inconsistent lengths")
            return false
        }

        Timber.d("Shares validation passed")
        return true
    }
}
