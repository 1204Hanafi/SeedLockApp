package com.app.seedlockapp.util

/**
 * Objek untuk menyimpan nilai-nilai konstan yang digunakan di seluruh aplikasi.
 */
object Constants {

    // Konfigurasi Android Keystore
    const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
    const val KEY_ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding"
    const val KEY_SIZE_BITS = 256
    const val AUTH_TAG_LENGTH_BITS = 128
    const val KEY_ALIAS_PREFIX = "seed_lock_key_"

    // Konfigurasi Shamir's Secret Sharing
    const val SSS_TOTAL_SHARES = 3
    const val SSS_THRESHOLD = 2

    // Konfigurasi Session Manager
    const val SESSION_TIMEOUT_MINUTES = 5L

    // Kunci untuk Navigasi
    const val NAV_ARG_SEED_ID = "seedId"

    // Nama Database/Preferences
    const val DATASTORE_NAME = "seed_lock_prefs"
}