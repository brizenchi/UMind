package com.example.umind.domain.model

import kotlin.time.Duration

/**
 * 使用时长限制类型
 * Usage limit type
 */
enum class LimitType {
    TOTAL_ALL,    // 所有应用总时长
    PER_APP       // 每个应用相同时长
}

/**
 * 使用时长限制
 * Usage duration limits
 */
data class UsageLimits(
    val type: LimitType,
    val totalLimit: Duration? = null, // 所有应用总时长（TOTAL_ALL 模式）
    val perAppLimit: Duration? = null // 每个应用时长（PER_APP 模式）
) {
    /**
     * Get the limit for a specific package
     */
    fun getLimitFor(packageName: String): Duration? {
        return when (type) {
            LimitType.TOTAL_ALL -> totalLimit
            LimitType.PER_APP -> perAppLimit
        }
    }

    /**
     * Check if this limit is valid
     */
    fun isValid(): Boolean {
        return when (type) {
            LimitType.TOTAL_ALL -> totalLimit != null && totalLimit > Duration.ZERO
            LimitType.PER_APP -> perAppLimit != null && perAppLimit > Duration.ZERO
        }
    }
}
