package com.app.seedlockapp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.app.seedlockapp.session.SessionManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Application class for SeedLock app.
 * Initializes Hilt dependency injection and Timber logging.
 */
@HiltAndroidApp
class SeedLockApp : Application() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Register lifecycle callbacks for session management
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                sessionManager.refreshInteraction()
                Timber.d("Activity resumed, refreshing session interaction")
            }

            override fun onActivityPaused(activity: Activity) {
                Timber.d("Activity paused")
            }

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })

        Timber.d("SeedLockApplication initialized")
    }
}