package com.app.seedlockapp.domain.model

/**
 * Data class representing a Shamir's Secret Sharing share.
 *
 * @property index The index of this share (1-based)
 * @property value The actual share value as byte array
 */
data class Share(
    val index: Int,
    val value: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Share

        if (index != other.index) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + value.contentHashCode()
        return result
    }
}

