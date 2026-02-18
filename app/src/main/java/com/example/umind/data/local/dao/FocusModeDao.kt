package com.example.umind.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.umind.data.local.entity.FocusModeEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Focus Mode
 */
@Dao
interface FocusModeDao {
    @Query("SELECT * FROM focus_mode WHERE id = 'focus_mode_singleton' LIMIT 1")
    fun getFocusMode(): Flow<FocusModeEntity?>

    @Query("SELECT * FROM focus_mode WHERE id = 'focus_mode_singleton' LIMIT 1")
    suspend fun getFocusModeOnce(): FocusModeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(focusMode: FocusModeEntity)

    @Update
    suspend fun update(focusMode: FocusModeEntity)

    @Query("UPDATE focus_mode SET isEnabled = :isEnabled, updatedAt = :updatedAt WHERE id = 'focus_mode_singleton'")
    suspend fun updateEnabled(isEnabled: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE focus_mode SET whitelistedApps = :whitelistedApps, updatedAt = :updatedAt WHERE id = 'focus_mode_singleton'")
    suspend fun updateWhitelist(whitelistedApps: Set<String>, updatedAt: Long = System.currentTimeMillis())
}
