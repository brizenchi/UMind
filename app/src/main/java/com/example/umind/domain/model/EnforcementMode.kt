package com.example.umind.domain.model

/**
 * 执行模式
 * Enforcement mode for strategy
 */
enum class EnforcementMode(val strictness: Int) {
    MONITOR_ONLY(1),        // 仅监控 - 不阻止，只记录
    DIRECT_BLOCK(2),        // 直接阻止 - 立即阻止应用启动
    FORCE_THROUGH_APP(3);   // 强制通过本应用 - 必须通过 UMind 打开

    /**
     * Get display name in Chinese
     */
    fun getDisplayName(): String {
        return when (this) {
            MONITOR_ONLY -> "仅监控"
            DIRECT_BLOCK -> "直接阻止"
            FORCE_THROUGH_APP -> "强制通过本应用"
        }
    }

    /**
     * Get description
     */
    fun getDescription(): String {
        return when (this) {
            MONITOR_ONLY -> "追踪和记录应用使用情况，不阻止使用"
            DIRECT_BLOCK -> "通过无障碍服务直接阻止应用启动"
            FORCE_THROUGH_APP -> "必须通过 UMind 应用内打开目标应用"
        }
    }
}
