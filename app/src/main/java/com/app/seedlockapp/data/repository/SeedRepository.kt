package com.app.seedlockapp.data.repository

import com.app.seedlockapp.domain.model.Seed

/**
 * Interface for managing seed phrase data.
 * 
 * This repository provides methods for CRUD operations on seed phrases,
 * abstracting the underlying storage mechanism from the domain layer.
 */
interface SeedRepository {
    
    /**
     * Saves a seed phrase to the repository.
     * 
     * @param seed The [Seed] object to save.
     */
    suspend fun saveSeed(seed: Seed)
    
    /**
     * Retrieves a seed phrase by its unique identifier.
     * 
     * @param id The unique identifier of the seed.
     * @return The [Seed] object if found, null otherwise.
     */
    suspend fun getSeed(id: String): Seed?
    
    /**
     * Retrieves all seed phrases from the repository.
     * 
     * @return A list of all [Seed] objects, sorted by creation date (newest first).
     */
    suspend fun getAllSeeds(): List<Seed>
    
    /**
     * Deletes a seed phrase from the repository.
     * 
     * @param id The unique identifier of the seed to delete.
     */
    suspend fun deleteSeed(id: String)
}

