package com.app.seedlockapp.domain.model

/**
 * Model Domain yang merepresentasikan satu bagian (share) mentah dari hasil proses Shamir's Secret Sharing (SSS).
 * Kelas ini berfungsi sebagai lapisan abstraksi antara data mentah yang dihasilkan oleh pustaka SSS eksternal
 * dan logika bisnis aplikasi. Tujuannya adalah untuk menghindari ketergantungan langsung pada
 * model data dari pustaka pihak ketiga di seluruh aplikasi.
 *
 * @property index Indeks unik dari share ini (misalnya, 1, 2, atau 3). Indeks ini krusial
 * untuk proses rekonstruksi rahasia.
 * @property value Nilai data mentah dari share dalam bentuk [ByteArray]. Data ini adalah representasi
 * biner dari bagian rahasia dan siap untuk dienkripsi.
 */
data class Share(
    val index: Int,
    val value: ByteArray
) {
    // Override equals dan hashCode diperlukan saat membandingkan objek yang berisi ByteArray,
    // karena perbandingan default pada array hanya memeriksa referensi objek, bukan isinya.
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