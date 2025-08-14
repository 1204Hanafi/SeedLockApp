package com.app.seedlockapp.domain.model

/**
 * Model Domain yang merepresentasikan satu bagian (share) mentah dari Shamir's Secret Sharing.
 * Kelas ini berfungsi sebagai lapisan abstraksi antara pustaka eksternal dan logika aplikasi.
 *
 * @property index Indeks dari share ini (misalnya, 1, 2, atau 3).
 * @property value Nilai data mentah dari share dalam bentuk ByteArray.
 */
data class Share(
    val index: Int,
    val value: ByteArray
) {
    // Override equals dan hashCode diperlukan saat membandingkan objek yang berisi ByteArray.
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