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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideKeystoreManager(): KeystoreManager {
        return KeystoreManager()
    }

    @Provides
    @Singleton
    fun provideShamirSecretSharingManager(): ShamirSecretSharingManager {
        return ShamirSecretSharingManager()
    }

    @Provides
    @Singleton
    fun provideBiometricInteractor(): BiometricInteractor {
        return BiometricInteractor()
    }

    @Provides
    @Singleton
    fun provideSeedRepository(
        dataStoreManager: DataStoreManager,
        keystoreManager: KeystoreManager,
        shamirManager: ShamirSecretSharingManager
    ): SeedRepository {
        return SeedRepository(dataStoreManager, keystoreManager, shamirManager)
    }

    @Provides
    @Singleton
    fun provideSessionManager(): SessionManager {
        return SessionManager()
    }
}