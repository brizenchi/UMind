package com.example.umind.domain.model

/**
 * 打开次数限制
 * Open count limits
 */
data class OpenCountLimits(
    val type: LimitType,
    val totalCount: Int? = null, // 所有应用总次数（TOTAL_ALL 模式）
    val perAppCount: Int? = null // 每个应用次数（PER_APP 模式）
) {
    /**
     * 返回当前限制的有效次数。
     * 新逻辑下统一使用组内总量，优先读取 totalCount，兼容历史 perAppCount。
     */
    fun effectiveCount(): Int? = totalCount ?: perAppCount

    /**
     * 归一化为 TOTAL_ALL 结构，便于统一存储和计算。
     */
    fun normalizedToTotalAll(): OpenCountLimits? {
        val count = effectiveCount() ?: return null
        if (count <= 0) return null
        return OpenCountLimits(
            type = LimitType.TOTAL_ALL,
            totalCount = count
        )
    }

    /**
     * Get the limit for a specific package
     */
    fun getLimitFor(packageName: String): Int? {
        return effectiveCount()
    }

    /**
     * Check if this limit is valid
     */
    fun isValid(): Boolean {
        val count = effectiveCount()
        return count != null && count > 0
    }
}
