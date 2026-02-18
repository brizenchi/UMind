package com.example.umind.domain.model

/**
 * Domain model for Focus Strategy
 * Represents a user-defined focus strategy with multiple restriction types
 */
data class FocusStrategy(
    val id: String,
    val name: String,
    val targetApps: Set<String>, // 目标应用包名列表
    val timeRestrictions: List<TimeRestriction> = emptyList(), // 时间范围限制
    val usageLimits: UsageLimits? = null, // 使用时长限制
    val openCountLimits: OpenCountLimits? = null, // 打开次数限制
    val enforcementMode: EnforcementMode = EnforcementMode.DIRECT_BLOCK, // 执行模式
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if the current time is within any time restriction
     * Returns true if current time falls within a restricted time period
     * Returns false if no time restrictions or current time is outside all restrictions
     */
    fun isWithinFocusTime(): Boolean {
        if (timeRestrictions.isEmpty()) {
            return false // No time restrictions means not restricted by time
        }
        return timeRestrictions.any { it.isWithinRestriction() }
    }

    /**
     * Check if strategy has any restrictions
     */
    fun hasRestrictions(): Boolean {
        return timeRestrictions.isNotEmpty() ||
            usageLimits != null ||
            openCountLimits != null
    }

    /**
     * Get summary of restrictions
     */
    fun getRestrictionSummary(): String {
        val parts = mutableListOf<String>()

        if (timeRestrictions.isNotEmpty()) {
            parts.add("${timeRestrictions.size}个时间段")
        }

        if (usageLimits != null) {
            parts.add("时长限制")
        }

        if (openCountLimits != null) {
            parts.add("次数限制")
        }

        return if (parts.isEmpty()) "无限制" else parts.joinToString(" | ")
    }
}
