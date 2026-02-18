package com.example.umind.domain.model

/**
 * Temporary usage record
 * Represents a temporary access grant for a blocked app
 */
data class TemporaryUsage(
    val id: String,
    val packageName: String,
    val appName: String,
    val reason: String,
    val requestedDurationMinutes: Int,
    val actualDurationMillis: Long = 0,
    val startTime: Long,
    val endTime: Long, // Calculated as startTime + requestedDurationMinutes
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if this temporary usage is still valid
     */
    fun isValid(): Boolean {
        val now = System.currentTimeMillis()
        return isActive && now < endTime
    }

    /**
     * Get remaining time in milliseconds
     */
    fun getRemainingTimeMillis(): Long {
        val now = System.currentTimeMillis()
        return if (isValid()) {
            endTime - now
        } else {
            0
        }
    }
}
