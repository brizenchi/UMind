package com.example.umind.data.local.dao

import androidx.room.*
import com.example.umind.data.local.entity.BlockEventEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface BlockEventDao {
    /**
     * 插入阻止事件
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockEvent(event: BlockEventEntity)

    /**
     * 获取所有阻止事件
     */
    @Query("SELECT * FROM block_events ORDER BY timestamp DESC")
    fun getAllBlockEventsFlow(): Flow<List<BlockEventEntity>>

    /**
     * 获取指定应用的阻止事件
     */
    @Query("SELECT * FROM block_events WHERE packageName = :packageName ORDER BY timestamp DESC")
    suspend fun getBlockEventsForPackage(packageName: String): List<BlockEventEntity>

    /**
     * 获取指定时间范围内的阻止事件
     */
    @Query("SELECT * FROM block_events WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getBlockEventsInRange(startTime: Long, endTime: Long): List<BlockEventEntity>

    /**
     * 获取今日阻止事件
     */
    @Query("SELECT * FROM block_events WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    suspend fun getTodayBlockEvents(startOfDay: Long): List<BlockEventEntity>

    /**
     * 获取今日阻止事件 Flow
     */
    @Query("SELECT * FROM block_events WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getTodayBlockEventsFlow(startOfDay: Long): Flow<List<BlockEventEntity>>

    /**
     * 获取指定来源的阻止事件
     */
    @Query("SELECT * FROM block_events WHERE blockSource = :source ORDER BY timestamp DESC")
    suspend fun getBlockEventsBySource(source: String): List<BlockEventEntity>

    /**
     * 统计今日阻止次数
     */
    @Query("SELECT COUNT(*) FROM block_events WHERE timestamp >= :startOfDay")
    suspend fun getTodayBlockCount(startOfDay: Long): Int

    /**
     * 统计指定应用今日阻止次数
     */
    @Query("SELECT COUNT(*) FROM block_events WHERE packageName = :packageName AND timestamp >= :startOfDay")
    suspend fun getTodayBlockCountForPackage(packageName: String, startOfDay: Long): Int

    /**
     * 删除指定时间之前的阻止事件
     */
    @Query("DELETE FROM block_events WHERE timestamp < :timestamp")
    suspend fun deleteBlockEventsBefore(timestamp: Long)

    /**
     * 删除所有阻止事件
     */
    @Query("DELETE FROM block_events")
    suspend fun deleteAllBlockEvents()
}
