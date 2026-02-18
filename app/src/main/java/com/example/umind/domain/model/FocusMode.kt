package com.example.umind.domain.model

/**
 * Focus Mode Type
 */
enum class FocusModeType {
    MANUAL,      // 手动开关
    COUNTDOWN,   // 倒计时模式
    SCHEDULED    // 定时模式
}

/**
 * Domain model for Focus Mode (Whitelist-based)
 * When enabled, only whitelisted apps are allowed, all others are blocked
 */
data class FocusMode(
    val id: String = "focus_mode_singleton", // Singleton instance
    val isEnabled: Boolean = false, // Whether focus mode is currently active
    val whitelistedApps: Set<String> = emptySet(), // Package names of allowed apps
    val modeType: FocusModeType = FocusModeType.MANUAL, // Mode type
    val countdownEndTime: Long? = null, // End time for countdown mode (timestamp)
    val scheduledTimeRanges: List<TimeRestriction> = emptyList(), // Time ranges for scheduled mode
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if an app is allowed in focus mode
     */
    fun isAppAllowed(packageName: String): Boolean {
        return whitelistedApps.contains(packageName)
    }

    /**
     * Check if focus mode should be active based on time settings
     */
    fun shouldBeActive(): Boolean {
        if (!isEnabled) return false

        return when (modeType) {
            FocusModeType.MANUAL -> true // Always active when manually enabled
            FocusModeType.COUNTDOWN -> {
                // Check if countdown has not expired
                countdownEndTime?.let { endTime ->
                    System.currentTimeMillis() < endTime
                } ?: false
            }
            FocusModeType.SCHEDULED -> {
                // Check if current time is within any scheduled range
                scheduledTimeRanges.any { it.isWithinRestriction() }
            }
        }
    }

    /**
     * Get remaining time in minutes for countdown mode
     */
    fun getRemainingMinutes(): Int? {
        if (modeType != FocusModeType.COUNTDOWN || countdownEndTime == null) {
            return null
        }
        val remaining = countdownEndTime - System.currentTimeMillis()
        return if (remaining > 0) {
            (remaining / 60000).toInt()
        } else {
            0
        }
    }

    /**
     * Get summary of focus mode status
     */
    fun getSummary(): String {
        if (!isEnabled) return "未开启"

        return when (modeType) {
            FocusModeType.MANUAL -> "已开启 · ${whitelistedApps.size}个允许应用"
            FocusModeType.COUNTDOWN -> {
                val remaining = getRemainingMinutes()
                if (remaining != null && remaining > 0) {
                    "倒计时 · 剩余${remaining}分钟"
                } else {
                    "倒计时已结束"
                }
            }
            FocusModeType.SCHEDULED -> {
                "定时模式 · ${scheduledTimeRanges.size}个时间段"
            }
        }
    }
}
