package com.example.umind.domain.model

import kotlin.time.Duration

/**
 * 使用时长限制类型
 * Usage limit type
 */
enum class LimitType {
    TOTAL_ALL,    // 所有应用总时长
    PER_APP       // 兼容历史数据，运行时会按 TOTAL_ALL 处理
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
     * 返回当前限制的有效时长。
     * 新逻辑下统一使用组内总量，优先读取 totalLimit，兼容历史 perAppLimit。
     */
    fun effectiveLimit(): Duration? = totalLimit ?: perAppLimit

    /**
     * 归一化为 TOTAL_ALL 结构，便于统一存储和计算。
     */
    fun normalizedToTotalAll(): UsageLimits? {
        val limit = effectiveLimit() ?: return null
        if (limit <= Duration.ZERO) return null
        return UsageLimits(
            type = LimitType.TOTAL_ALL,
            totalLimit = limit
        )
    }

    /**
     * Get the limit for a specific package
     */
    fun getLimitFor(packageName: String): Duration? {
        return effectiveLimit()
    }

    /**
     * Check if this limit is valid
     */
    fun isValid(): Boolean {
        val limit = effectiveLimit()
        return limit != null && limit > Duration.ZERO
    }
}
