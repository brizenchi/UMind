package com.example.umind.domain.repository

import com.example.umind.domain.model.AppInfo
import com.example.umind.domain.model.BlockInfo
import com.example.umind.domain.model.FocusStrategy
import com.example.umind.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Focus Strategy operations
 * This belongs to the domain layer and defines the contract
 */
interface FocusRepository {
    /**
     * Get all focus strategies as a Flow
     */
    fun getFocusStrategiesFlow(): Flow<List<FocusStrategy>>

    /**
     * Get all focus strategies
     */
    suspend fun getFocusStrategies(): Result<List<FocusStrategy>>

    /**
     * Get a specific focus strategy by ID
     */
    suspend fun getFocusStrategyById(id: String): Result<FocusStrategy?>

    /**
     * Get the currently active focus strategy
     */
    suspend fun getActiveStrategy(): Result<FocusStrategy?>

    /**
     * Save or update a focus strategy
     */
    suspend fun saveFocusStrategy(strategy: FocusStrategy): Result<Unit>

    /**
     * Delete a focus strategy
     */
    suspend fun deleteFocusStrategy(id: String): Result<Unit>

    /**
     * Set a strategy as active/inactive
     */
    suspend fun setStrategyActive(id: String, isActive: Boolean): Result<Unit>

    /**
     * Get all installed apps
     */
    suspend fun getInstalledApps(): Result<List<AppInfo>>

    /**
     * Check if a package should be blocked at the current time
     */
    suspend fun shouldBlockPackage(packageName: String): Boolean

    /**
     * Get detailed block information for a package
     */
    suspend fun getBlockInfo(packageName: String): BlockInfo
}
