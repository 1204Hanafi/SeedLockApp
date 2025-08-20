package com.app.seedlockapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Kelas [Application] kustom untuk SeedLockApp.
 * Anotasi [HiltAndroidApp] mengaktifkan Hilt untuk dependency injection
 * di seluruh aplikasi. Kelas ini berfungsi sebagai titik masuk aplikasi
 * dan tempat untuk melakukan inisialisasi global.
 */
@HiltAndroidApp
class SeedLockApp : Application() {
    /**
     * Dipanggil saat aplikasi dibuat.
     * Metode ini digunakan untuk inisialisasi pustaka pihak ketiga
     * atau konfigurasi awal lainnya.
     */
    override fun onCreate() {
        super.onCreate()
        // Inisialisasi Timber untuk logging yang lebih baik hanya pada build tipe debug.
        // Pada build release, tidak ada log yang akan dicetak, sehingga lebih aman dan efisien.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}