package com.example.umind.data.repository

import com.example.umind.data.local.dao.BlockEventDao
import com.example.umind.data.local.entity.BlockEventEntity
import com.example.umind.domain.model.BlockInfo
import com.example.umind.domain.model.BlockReason
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockEventRepository @Inject constructor(
    private val blockEventDao: BlockEventDao
) {
    /**
     * 记录阻止事件
     */
    suspend fun recordBlockEvent(
        packageName: String,
        appName: String,
        blockInfo: BlockInfo,
        strategyIds: List<String>? = null
    ) {
        val blockSource = determineBlockSource(blockInfo.reasons)
        val blockReasonJson = serializeBlockReasons(blockInfo.reasons)
        val strategyIdsJson = strategyIds?.let { JSONArray(it).toString() }

        val event = BlockEventEntity(
            packageName = packageName,
            appName = appName,
            timestamp = System.currentTimeMillis(),
            blockReason = blockReasonJson,
            blockSource = blockSource,
            strategyIds = strategyIdsJson
        )

        blockEventDao.insertBlockEvent(event)
        android.util.Log.d("BlockEventRepository", "Recorded block event for $packageName, source: $blockSource")
    }

    /**
     * 获取今日阻止事件
     */
    suspend fun getTodayBlockEvents(): List<BlockEventEntity> {
        val startOfDay = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return blockEventDao.getTodayBlockEvents(startOfDay)
    }

    /**
     * 获取今日阻止事件 Flow
     */
    fun getTodayBlockEventsFlow(): Flow<List<BlockEventEntity>> {
        val startOfDay = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return blockEventDao.getTodayBlockEventsFlow(startOfDay)
    }

    /**
     * 获取今日阻止次数
     */
    suspend fun getTodayBlockCount(): Int {
        val startOfDay = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return blockEventDao.getTodayBlockCount(startOfDay)
    }

    /**
     * 获取指定应用今日阻止次数
     */
    suspend fun getTodayBlockCountForPackage(packageName: String): Int {
        val startOfDay = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return blockEventDao.getTodayBlockCountForPackage(packageName, startOfDay)
    }

    /**
     * 获取指定时间范围内的阻止事件
     */
    suspend fun getBlockEventsInRange(startTime: Long, endTime: Long): List<BlockEventEntity> {
        return blockEventDao.getBlockEventsInRange(startTime, endTime)
    }

    /**
     * 清理旧的阻止事件（保留最近 90 天）
     */
    suspend fun cleanupOldBlockEvents() {
        val cutoffTime = LocalDate.now()
            .minusDays(90)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        blockEventDao.deleteBlockEventsBefore(cutoffTime)
    }

    /**
     * 确定阻止来源
     */
    private fun determineBlockSource(reasons: List<BlockReason>): String {
        return when {
            reasons.any { it is BlockReason.FocusModeActive } -> "FOCUS_MODE"
            else -> "DAILY_MANAGEMENT"
        }
    }

    /**
     * 序列化阻止原因为 JSON
     */
    private fun serializeBlockReasons(reasons: List<BlockReason>): String {
        val jsonArray = JSONArray()
        reasons.forEach { reason ->
            val jsonObject = JSONObject()
            when (reason) {
                is BlockReason.TimeRestriction -> {
                    jsonObject.put("type", "TIME_RESTRICTION")
                    jsonObject.put("nextAvailableTime", reason.nextAvailableTime)
                }
                is BlockReason.UsageLimitExceeded -> {
                    jsonObject.put("type", "USAGE_LIMIT_EXCEEDED")
                    jsonObject.put("limitMinutes", reason.limitMinutes)
                    jsonObject.put("usedMinutes", reason.usedMinutes)
                }
                is BlockReason.OpenCountLimitExceeded -> {
                    jsonObject.put("type", "OPEN_COUNT_LIMIT_EXCEEDED")
                    jsonObject.put("limitCount", reason.limitCount)
                    jsonObject.put("usedCount", reason.usedCount)
                }
                is BlockReason.FocusModeActive -> {
                    jsonObject.put("type", "FOCUS_MODE_ACTIVE")
                }
                is BlockReason.ForceThroughApp -> {
                    jsonObject.put("type", "FORCE_THROUGH_APP")
                }
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }
}
