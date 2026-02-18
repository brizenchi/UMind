package com.example.umind.data.local.dao

import androidx.room.*
import com.example.umind.data.local.entity.FocusStrategyEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Focus Strategies
 */
@Dao
interface FocusStrategyDao {
    @Query("SELECT * FROM focus_strategies ORDER BY createdAt DESC")
    fun getAllStrategiesFlow(): Flow<List<FocusStrategyEntity>>

    @Query("SELECT * FROM focus_strategies ORDER BY createdAt DESC")
    suspend fun getAllStrategies(): List<FocusStrategyEntity>

    @Query("SELECT * FROM focus_strategies WHERE id = :id")
    suspend fun getStrategyById(id: String): FocusStrategyEntity?

    @Query("SELECT * FROM focus_strategies WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveStrategy(): FocusStrategyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrategy(strategy: FocusStrategyEntity)

    @Update
    suspend fun updateStrategy(strategy: FocusStrategyEntity)

    @Query("DELETE FROM focus_strategies WHERE id = :id")
    suspend fun deleteStrategy(id: String)

    @Query("UPDATE focus_strategies SET isActive = 0")
    suspend fun deactivateAllStrategies()

    @Query("UPDATE focus_strategies SET isActive = :isActive WHERE id = :id")
    suspend fun setStrategyActive(id: String, isActive: Boolean)

    @Transaction
    suspend fun setStrategyActiveExclusive(id: String, isActive: Boolean) {
        if (isActive) {
            deactivateAllStrategies()
        }
        setStrategyActive(id, isActive)
    }
}
