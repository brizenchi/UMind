package com.example.umind.data.local.dao

import androidx.room.*
import com.example.umind.data.local.entity.TemporaryUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemporaryUsageDao {
    @Query("SELECT * FROM temporary_usage WHERE packageName = :packageName AND isActive = 1 AND endTime > :currentTime ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveTemporaryUsage(packageName: String, currentTime: Long): TemporaryUsageEntity?

    @Query("SELECT * FROM temporary_usage WHERE packageName = :packageName AND isActive = 1 AND endTime > :currentTime ORDER BY startTime DESC LIMIT 1")
    fun getActiveTemporaryUsageFlow(packageName: String, currentTime: Long): Flow<TemporaryUsageEntity?>

    @Query("SELECT * FROM temporary_usage ORDER BY createdAt DESC")
    suspend fun getAllTemporaryUsages(): List<TemporaryUsageEntity>

    @Query("SELECT * FROM temporary_usage WHERE packageName = :packageName ORDER BY createdAt DESC")
    suspend fun getTemporaryUsagesForPackage(packageName: String): List<TemporaryUsageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemporaryUsage(temporaryUsage: TemporaryUsageEntity)

    @Update
    suspend fun updateTemporaryUsage(temporaryUsage: TemporaryUsageEntity)

    @Query("UPDATE temporary_usage SET isActive = 0 WHERE id = :id")
    suspend fun deactivateTemporaryUsage(id: String)

    @Query("UPDATE temporary_usage SET isActive = 0 WHERE endTime < :currentTime")
    suspend fun deactivateExpiredUsages(currentTime: Long)

    @Query("DELETE FROM temporary_usage WHERE createdAt < :beforeTime")
    suspend fun deleteOldRecords(beforeTime: Long)
}
