package com.app.seedlockapp.di

import android.content.Context
import com.app.seedlockapp.data.local.DataStoreManager
import com.app.seedlockapp.data.repository.SeedRepository
import com.app.seedlockapp.domain.interactor.BiometricInteractor
import com.app.seedlockapp.domain.manager.KeystoreManager
import com.app.seedlockapp.domain.manager.SessionManager
import com.app.seedlockapp.domain.manager.ShamirSecretSharingManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt Module yang menyediakan dependensi-dependensi utama
 * yang digunakan di seluruh aplikasi dengan scope Singleton.
 * Ini memastikan hanya ada satu instance dari setiap manajer atau repository
 * selama aplikasi berjalan, yang efisien dan aman untuk state management.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** Menyediakan instance singleton dari [DataStoreManager]. */
    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    /** Menyediakan instance singleton dari [KeystoreManager]. */
    @Provides
    @Singleton
    fun provideKeystoreManager(): KeystoreManager {
        return KeystoreManager()
    }

    /** Menyediakan instance singleton dari [ShamirSecretSharingManager]. */
    @Provides
    @Singleton
    fun provideShamirSecretSharingManager(): ShamirSecretSharingManager {
        return ShamirSecretSharingManager()
    }

    /** Menyediakan instance singleton dari [BiometricInteractor]. */
    @Provides
    @Singleton
    fun provideBiometricInteractor(): BiometricInteractor {
        return BiometricInteractor()
    }

    /**
     * Menyediakan instance singleton dari [SeedRepository].
     * Dependensi-dependensinya (DataStore, Keystore, Shamir) akan
     * secara otomatis disediakan oleh Hilt dari provider lain di modul ini.
     */
    @Provides
    @Singleton
    fun provideSeedRepository(
        dataStoreManager: DataStoreManager,
        keystoreManager: KeystoreManager,
        shamirManager: ShamirSecretSharingManager
    ): SeedRepository {
        return SeedRepository(dataStoreManager, keystoreManager, shamirManager)
    }

    /** Menyediakan instance singleton dari [SessionManager]. */
    @Provides
    @Singleton
    fun provideSessionManager(): SessionManager {
        return SessionManager()
    }
}