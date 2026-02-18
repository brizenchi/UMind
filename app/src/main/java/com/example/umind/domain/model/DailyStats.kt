package com.example.umind.domain.model

import java.time.LocalDate

/**
 * 每日统计数据
 */
data class DailyStats(
    val date: LocalDate,
    val totalUsageDurationMillis: Long,
    val totalOpenCount: Int,
    val totalBlockCount: Int,
    val appUsageStats: List<AppUsageStats>
)

/**
 * 单个应用的使用统计
 */
data class AppUsageStats(
    val packageName: String,
    val appName: String,
    val usageDurationMillis: Long,
    val openCount: Int,
    val blockCount: Int = 0
)

/**
 * 使用趋势数据点
 */
data class UsageTrend(
    val date: LocalDate,
    val totalUsageDurationMillis: Long,
    val totalOpenCount: Int
)

/**
 * 时间线条目
 */
data class UsageTimelineEntry(
    val timestamp: Long,
    val packageName: String,
    val appName: String,
    val durationMillis: Long,
    val type: TimelineEntryType
)

enum class TimelineEntryType {
    APP_USAGE,
    TEMPORARY_USAGE,
    BLOCKED
}
