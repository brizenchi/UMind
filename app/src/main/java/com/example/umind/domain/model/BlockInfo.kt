package com.example.umind.domain.model

/**
 * Information about why an app is blocked and remaining quotas
 */
data class BlockInfo(
    val shouldBlock: Boolean,
    val reasons: List<BlockReason> = emptyList(),
    val usageInfo: UsageInfo? = null
)

sealed class BlockReason {
    data class TimeRestriction(val nextAvailableTime: String?) : BlockReason()
    data class UsageLimitExceeded(val limitMinutes: Long, val usedMinutes: Long) : BlockReason()
    data class OpenCountLimitExceeded(val limitCount: Int, val usedCount: Int) : BlockReason()
    object FocusModeActive : BlockReason()
    object ForceThroughApp : BlockReason()
}

data class UsageInfo(
    val usageLimitMinutes: Long? = null,
    val usedMinutes: Long = 0,
    val remainingMinutes: Long? = null,
    val openCountLimit: Int? = null,
    val openCount: Int = 0,
    val remainingCount: Int? = null
)
