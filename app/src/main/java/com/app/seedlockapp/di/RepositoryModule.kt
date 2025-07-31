package com.app.seedlockapp.di

import com.app.seedlockapp.data.repository.SeedRepository
import com.app.seedlockapp.data.repository.SeedRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSeedRepository(
        seedRepositoryImpl: SeedRepositoryImpl
    ): SeedRepository
}

