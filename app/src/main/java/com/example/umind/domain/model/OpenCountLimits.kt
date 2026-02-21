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
     * Get the limit for a specific package
     */
    fun getLimitFor(packageName: String): Int? {
        return when (type) {
            LimitType.TOTAL_ALL -> totalCount
            LimitType.PER_APP -> perAppCount
        }
    }

    /**
     * Check if this limit is valid
     */
    fun isValid(): Boolean {
        return when (type) {
            LimitType.TOTAL_ALL -> totalCount != null && totalCount > 0
            LimitType.PER_APP -> perAppCount != null && perAppCount > 0
        }
    }
}
