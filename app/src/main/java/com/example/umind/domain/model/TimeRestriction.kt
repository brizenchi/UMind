package com.example.umind.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * 时间范围限制
 * Time range restriction for specific days of the week
 */
data class TimeRestriction(
    val id: String,
    val daysOfWeek: Set<DayOfWeek>, // 周一至周日
    val startTime: LocalTime, // 开始时间
    val endTime: LocalTime // 结束时间
) {
    /**
     * Check if the current time is within this restriction
     */
    fun isWithinRestriction(): Boolean {
        val now = java.time.LocalDateTime.now()
        val currentDay = now.dayOfWeek
        val currentTime = now.toLocalTime()

        // Check if today is in the restricted days
        if (currentDay !in daysOfWeek) {
            return false
        }

        // Check if current time is within the restricted time range
        return if (startTime <= endTime) {
            // Normal range (e.g., 9:00 - 18:00)
            currentTime >= startTime && currentTime <= endTime
        } else {
            // Overnight range (e.g., 22:00 - 06:00)
            currentTime >= startTime || currentTime <= endTime
        }
    }

    /**
     * Format time range as string
     */
    fun getTimeRangeString(): String {
        return "${startTime} - ${endTime}"
    }

    /**
     * Format days of week as string
     */
    fun getDaysString(): String {
        val dayNames = mapOf(
            DayOfWeek.MONDAY to "周一",
            DayOfWeek.TUESDAY to "周二",
            DayOfWeek.WEDNESDAY to "周三",
            DayOfWeek.THURSDAY to "周四",
            DayOfWeek.FRIDAY to "周五",
            DayOfWeek.SATURDAY to "周六",
            DayOfWeek.SUNDAY to "周日"
        )

        return daysOfWeek.sortedBy { it.value }
            .joinToString(", ") { dayNames[it] ?: it.name }
    }
}
