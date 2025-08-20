package com.app.seedlockapp.util

/**
 * Objek singleton untuk menyimpan nilai-nilai konstan yang digunakan di seluruh aplikasi.
 * Tujuannya adalah untuk memusatkan konfigurasi dan menghindari "magic numbers" atau "magic strings"
 * yang tersebar di dalam kode, sehingga lebih mudah untuk diubah dan dipelihara.
 */
object Constants {

    //region Konfigurasi Android Keystore
    /** Penyedia Keystore yang digunakan, standar untuk Android. */
    const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
    /** Algoritma enkripsi yang digunakan. AES/GCM adalah standar modern yang menyediakan
     * enkripsi terotentikasi (Authenticated Encryption), melindungi dari tampering. */
    const val KEY_ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding"
    /** Ukuran kunci AES dalam bit. 256 adalah standar keamanan yang kuat. */
    const val KEY_SIZE_BITS = 256
    /** Panjang tag otentikasi untuk mode GCM dalam bit. 128 adalah standar yang direkomendasikan. */
    const val AUTH_TAG_LENGTH_BITS = 128
    /** Awalan (prefix) yang digunakan untuk semua alias kunci di Keystore agar mudah diidentifikasi. */
    const val KEY_ALIAS_PREFIX = "seed_lock_key_"
    //endregion

    //region Konfigurasi Shamir's Secret Sharing
    /** Jumlah total bagian (shares) yang akan dibuat dari satu rahasia. */
    const val SSS_TOTAL_SHARES = 3
    /** Jumlah minimum bagian (shares) yang diperlukan untuk merekonstruksi rahasia. */
    const val SSS_THRESHOLD = 2
    //endregion

    //region Konfigurasi Session Manager
    /** Durasi timeout sesi dalam menit. Setelah waktu ini tanpa interaksi, sesi akan berakhir. */
    const val SESSION_TIMEOUT_MINUTES = 5L
    //endregion

    //region Kunci untuk Navigasi
    /** Kunci yang digunakan untuk mengirim argumen `seedId` antar layar melalui Jetpack Navigation. */
    const val NAV_ARG_SEED_ID = "seedId"
    //endregion

    //region Nama Database/Preferences
    /** Nama file preferensi yang digunakan oleh Jetpack DataStore untuk menyimpan data aplikasi. */
    const val DATASTORE_NAME = "seed_lock_prefs"
    //endregion
}