package com.example.umind.presentation.stats

import kotlin.math.roundToInt

/**
 * Format milliseconds to readable duration string
 * e.g., 3661000 -> "1h 1m"
 */
fun formatDuration(millis: Long): String {
    if (millis == 0L) return "0m"

    val totalMinutes = (millis / 60000).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

/**
 * Format milliseconds to short duration string for charts
 * e.g., 3661000 -> "1.0h"
 */
fun formatDurationShort(millis: Long): String {
    val hours = millis / 3600000.0
    return when {
        hours >= 1.0 -> "${(hours * 10).roundToInt() / 10.0}h"
        else -> "${(millis / 60000).toInt()}m"
    }
}

/**
 * Get percentage for progress bar
 */
fun getUsagePercentage(usage: Long, maxUsage: Long): Float {
    if (maxUsage == 0L) return 0f
    return (usage.toFloat() / maxUsage.toFloat()).coerceIn(0f, 1f)
}
