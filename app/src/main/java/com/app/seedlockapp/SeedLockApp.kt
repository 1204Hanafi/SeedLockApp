package com.app.seedlockapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SeedLockApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inisialisasi Timber untuk logging di build debug
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}