package com.example.umind.data.local.dao

import androidx.room.*
import com.example.umind.data.local.entity.UsageSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface UsageSessionDao {
    /**
     * 插入使用会话
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UsageSessionEntity)

    /**
     * 更新使用会话
     */
    @Update
    suspend fun updateSession(session: UsageSessionEntity)

    /**
     * 获取指定记录的所有会话
     */
    @Query("SELECT * FROM usage_sessions WHERE recordId = :recordId ORDER BY startTime DESC")
    suspend fun getSessionsForRecord(recordId: String): List<UsageSessionEntity>

    /**
     * 获取指定应用的所有会话
     */
    @Query("SELECT * FROM usage_sessions WHERE packageName = :packageName ORDER BY startTime DESC")
    suspend fun getSessionsForPackage(packageName: String): List<UsageSessionEntity>

    /**
     * 获取正在进行中的会话（endTime 为 null）
     */
    @Query("SELECT * FROM usage_sessions WHERE packageName = :packageName AND endTime IS NULL LIMIT 1")
    suspend fun getActiveSession(packageName: String): UsageSessionEntity?

    /**
     * 获取指定时间范围内的会话
     */
    @Query("SELECT * FROM usage_sessions WHERE startTime >= :startTime AND startTime <= :endTime ORDER BY startTime DESC")
    suspend fun getSessionsInRange(startTime: Long, endTime: Long): List<UsageSessionEntity>

    /**
     * 删除指定应用的所有会话
     */
    @Query("DELETE FROM usage_sessions WHERE packageName = :packageName")
    suspend fun deleteSessionsForPackage(packageName: String)

    /**
     * 删除指定时间之前的会话
     */
    @Query("DELETE FROM usage_sessions WHERE startTime < :timestamp")
    suspend fun deleteSessionsBefore(timestamp: Long)
}
