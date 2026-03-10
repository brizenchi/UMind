package com.example.umind.service

import android.util.Log
import com.example.umind.data.repository.FocusModeRepository
import com.example.umind.data.repository.UsageTrackingRepository
import com.example.umind.domain.model.*
import com.example.umind.domain.repository.FocusRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 阻止决策引擎 - 核心业务逻辑
 *
 * 职责：
 * 1. 判断应用是否应该被阻止
 * 2. 合并多个策略的限制规则
 * 3. 处理专注模式和日常管理的优先级
 *
 * 优先级规则：
 * 专注模式 > 日常管理
 *
 * 多策略合并规则：
 * - 时间范围：取并集（所有限制时间段的合并）
 * - 使用时长：取最小值（最严格）
 * - 打开次数：取最小值（最严格）
 * - 执行模式：取最严格（FORCE_THROUGH_APP > DIRECT_BLOCK > MONITOR_ONLY）
 */
@Singleton
class BlockingEngine @Inject constructor(
    private val focusModeRepository: FocusModeRepository,
    private val focusRepository: FocusRepository,
    private val usageTrackingRepository: UsageTrackingRepository
) {
    companion object {
        private const val TAG = "BlockingEngine"

        /**
         * 系统关键应用白名单 - 这些应用永远不会被阻止
         */
        private val SYSTEM_WHITELIST = setOf(
            // Android 系统
            "android",
            "com.android.systemui",

            // 启动器（Launcher）
            "com.android.launcher",
            "com.android.launcher2",
            "com.android.launcher3",
            "com.google.android.apps.nexuslauncher", // Pixel Launcher
            "com.google.android.launcher",
            "com.sec.android.app.launcher", // Samsung
            "com.miui.home", // MIUI
            "com.huawei.android.launcher", // Huawei
            "com.oppo.launcher", // OPPO
            "com.vivo.launcher", // Vivo

            // 系统设置
            "com.android.settings",
            "com.google.android.settings",

            // 电话和短信
            "com.android.phone",
            "com.android.dialer",
            "com.google.android.dialer",
            "com.android.mms",
            "com.google.android.apps.messaging",

            // 系统关键服务
            "com.android.vending", // Google Play Store
            "com.google.android.gms", // Google Play Services
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",

            // 输入法
            "com.android.inputmethod.latin",
            "com.google.android.inputmethod.latin",

            // 相机（可选，但建议保留）
            "com.android.camera",
            "com.android.camera2",
            "com.google.android.GoogleCamera",

            // 时钟和闹钟
            "com.android.deskclock",
            "com.google.android.deskclock"
        )
    }

    /**
     * 判断应用是否应该被阻止
     *
     * @param packageName 应用包名
     * @param openedFromUMind 是否从 UMind 内打开
     * @return BlockInfo 包含是否阻止、阻止原因、使用信息
     */
    suspend fun getBlockInfo(
        packageName: String,
        openedFromUMind: Boolean = false
    ): BlockInfo {
        Log.d(TAG, "=== getBlockInfo for $packageName ===")
        Log.d(TAG, "openedFromUMind: $openedFromUMind")

        // 首先检查是否是系统白名单应用
        if (isSystemWhitelistedApp(packageName)) {
            Log.d(TAG, "System whitelisted app, allowing: $packageName")
            return BlockInfo(
                shouldBlock = false,
                isUnderDailyManagement = false
            )
        }

        // 优先级 1: 检查专注模式
        val focusModeBlockInfo = checkFocusMode(packageName)
        if (focusModeBlockInfo != null) {
            Log.d(TAG, "Blocked by Focus Mode")
            return focusModeBlockInfo
        }

        // 优先级 2: 检查日常管理
        return checkDailyManagement(packageName, openedFromUMind)
    }

    /**
     * 检查是否是系统白名单应用
     */
    private fun isSystemWhitelistedApp(packageName: String): Boolean {
        return packageName in SYSTEM_WHITELIST ||
               packageName.startsWith("com.example.umind") // UMind 自己
    }

    /**
     * 检查专注模式
     *
     * @return BlockInfo if should block, null if should allow
     */
    private suspend fun checkFocusMode(packageName: String): BlockInfo? {
        val focusMode = focusModeRepository.getFocusModeOnce()

        if (!focusMode.shouldBeActive()) {
            Log.d(TAG, "Focus mode not active")
            return null
        }

        Log.d(TAG, "Focus mode is active")

        // 检查白名单
        if (focusMode.isAppAllowed(packageName)) {
            Log.d(TAG, "App is in whitelist, allowing")
            return null
        }

        Log.d(TAG, "App not in whitelist, blocking")
        return BlockInfo(
            shouldBlock = true,
            reasons = listOf(BlockReason.FocusModeActive),
            usageInfo = null,
            isUnderDailyManagement = false
        )
    }

    /**
     * 检查日常管理策略
     */
    private suspend fun checkDailyManagement(
        packageName: String,
        openedFromUMind: Boolean
    ): BlockInfo {
        // 获取所有激活的策略
        val allStrategiesResult = focusRepository.getFocusStrategies()
        if (allStrategiesResult !is Result.Success) {
            Log.e(TAG, "Failed to get strategies")
            return BlockInfo(
                shouldBlock = false,
                isUnderDailyManagement = false
            )
        }

        val activeStrategies = allStrategiesResult.data.filter { it.isActive }
        Log.d(TAG, "Found ${activeStrategies.size} active strategies")

        // 筛选包含该应用的策略
        val relevantStrategies = activeStrategies.filter { strategy ->
            packageName in strategy.targetApps
        }

        if (relevantStrategies.isEmpty()) {
            Log.d(TAG, "No relevant strategies for $packageName")
            return BlockInfo(
                shouldBlock = false,
                isUnderDailyManagement = false
            )
        }

        Log.d(TAG, "Found ${relevantStrategies.size} relevant strategies: ${relevantStrategies.map { it.name }}")

        // 合并所有策略
        val mergedRestriction = mergeStrategies(relevantStrategies)
        Log.d(TAG, "Merged enforcement mode: ${mergedRestriction.enforcementMode}")

        // 检查执行模式
        return when (mergedRestriction.enforcementMode) {
            EnforcementMode.MONITOR_ONLY -> {
                Log.d(TAG, "Monitor only mode, not blocking")
                // 仅监控，不阻止，但返回使用信息
                val usageInfo = calculateUsageInfo(mergedRestriction)
                BlockInfo(
                    shouldBlock = false,
                    reasons = emptyList(),
                    usageInfo = usageInfo,
                    isUnderDailyManagement = true
                )
            }

            EnforcementMode.DIRECT_BLOCK -> {
                Log.d(TAG, "Direct block mode, checking restrictions")
                checkAllRestrictions(mergedRestriction)
            }

            EnforcementMode.FORCE_THROUGH_APP -> {
                Log.d(TAG, "Force through app mode")
                if (!openedFromUMind) {
                    Log.d(TAG, "Not opened from UMind, blocking")
                    // 从外部打开，始终阻止
                    BlockInfo(
                        shouldBlock = true,
                        reasons = listOf(BlockReason.ForceThroughApp),
                        usageInfo = null,
                        isUnderDailyManagement = true
                    )
                } else {
                    Log.d(TAG, "Opened from UMind, checking restrictions")
                    // 从 UMind 内打开，检查限制
                    checkAllRestrictions(mergedRestriction)
                }
            }
        }
    }

    /**
     * 合并多个策略
     *
     * 合并规则：
     * 1. 时间范围：取并集
     * 2. 使用时长：取最小值（最严格）
     * 3. 打开次数：取最小值（最严格）
     * 4. 执行模式：取最严格
     */
    private fun mergeStrategies(
        strategies: List<FocusStrategy>
    ): MergedRestriction {
        Log.d(TAG, "=== Merging ${strategies.size} strategies ===")

        // 1. 合并时间限制（取并集）
        val mergedTimeRestrictions = strategies
            .flatMap { it.timeRestrictions }
        Log.d(TAG, "Merged time restrictions: ${mergedTimeRestrictions.size} time slots")

        // 2. 合并使用时长限制（取最小值）
        val mergedUsageLimit = strategies
            .mapNotNull { it.usageLimits?.effectiveLimit() }
            .minOrNull()
        Log.d(TAG, "Merged usage limit: $mergedUsageLimit")

        // 3. 合并打开次数限制（取最小值）
        val mergedOpenCountLimit = strategies
            .mapNotNull { it.openCountLimits?.effectiveCount() }
            .minOrNull()
        Log.d(TAG, "Merged open count limit: $mergedOpenCountLimit")

        // 4. 确定执行模式（取最严格）
        val enforcementMode = strategies
            .map { it.enforcementMode }
            .maxByOrNull { it.strictness }
            ?: EnforcementMode.MONITOR_ONLY
        Log.d(TAG, "Merged enforcement mode: $enforcementMode")

        // 5. 收集所有目标应用（用于 TOTAL_ALL 模式）
        val targetApps = strategies.flatMap { it.targetApps }.toSet()
        Log.d(TAG, "Target apps: ${targetApps.size} apps")

        return MergedRestriction(
            timeRestrictions = mergedTimeRestrictions,
            usageLimit = mergedUsageLimit,
            openCountLimit = mergedOpenCountLimit,
            targetApps = targetApps,
            enforcementMode = enforcementMode
        )
    }

    /**
     * 检查所有限制
     */
    private suspend fun checkAllRestrictions(
        merged: MergedRestriction
    ): BlockInfo {
        val today = LocalDate.now()
        val reasons = mutableListOf<BlockReason>()
        val usageScopeId = buildUsageScopeId(merged.targetApps)

        // 1. 检查时间范围限制
        val withinTimeRestriction = merged.timeRestrictions.any { it.isWithinRestriction() }
        if (withinTimeRestriction) {
            Log.d(TAG, "Within time restriction")
            val nextAvailableTime = merged.timeRestrictions
                .mapNotNull { it.endTime }
                .minOrNull()
                ?.toString()
            reasons.add(BlockReason.TimeRestriction(nextAvailableTime))
        }

        // 2. 检查使用时长限制
        val usageLimit = merged.usageLimit
        var usageLimitMinutes: Long? = null
        var usedMinutes: Long = 0
        var remainingMinutes: Long? = null

        if (usageLimit != null) {
            val limitMs = usageLimit.inWholeMilliseconds
            usageLimitMinutes = limitMs / 60000

            val usedMs = calculateTotalUsage(merged.targetApps, today)

            val remainingMs = limitMs - usedMs
            usedMinutes = usedMs / 60000
            remainingMinutes = if (remainingMs > 0) remainingMs / 60000 else 0

            Log.d(
                TAG,
                "Usage check (TOTAL_ALL): limit=${usageLimitMinutes}min, used=${usedMinutes}min, remaining=${remainingMinutes}min"
            )

            if (remainingMs <= 0) {
                Log.d(TAG, "Usage limit exceeded")
                reasons.add(BlockReason.UsageLimitExceeded(
                    limitMinutes = usageLimitMinutes,
                    usedMinutes = usedMinutes
                ))
            }
        }

        // 3. 检查打开次数限制
        val openCountLimit = merged.openCountLimit
        var openCount: Int = 0
        var remainingCount: Int? = null

        if (openCountLimit != null) {
            openCount = calculateTotalOpenCount(merged.targetApps, today)

            remainingCount = if (openCountLimit > openCount) openCountLimit - openCount else 0

            Log.d(
                TAG,
                "Open count check (TOTAL_ALL): limit=$openCountLimit, used=$openCount, remaining=$remainingCount"
            )

            if (openCount >= openCountLimit) {
                Log.d(TAG, "Open count limit exceeded")
                reasons.add(BlockReason.OpenCountLimitExceeded(
                    limitCount = openCountLimit,
                    usedCount = openCount
                ))
            }
        }

        // 创建使用信息
        val usageInfo = if (usageLimitMinutes != null || openCountLimit != null) {
            UsageInfo(
                usageScopeId = usageScopeId,
                usageLimitMinutes = usageLimitMinutes,
                usedMinutes = usedMinutes,
                remainingMinutes = remainingMinutes,
                openCountLimit = openCountLimit,
                openCount = openCount,
                remainingCount = remainingCount
            )
        } else {
            null
        }

        val shouldBlock = reasons.isNotEmpty()
        Log.d(TAG, "Final decision: shouldBlock=$shouldBlock, reasons=${reasons.size}")

        return BlockInfo(
            shouldBlock = shouldBlock,
            reasons = reasons,
            usageInfo = usageInfo,
            isUnderDailyManagement = true
        )
    }

    /**
     * 计算使用信息（用于 MONITOR_ONLY 模式）
     */
    private suspend fun calculateUsageInfo(
        merged: MergedRestriction
    ): UsageInfo? {
        val today = LocalDate.now()
        val usageScopeId = buildUsageScopeId(merged.targetApps)

        var usageLimitMinutes: Long? = null
        var usedMinutes: Long = 0
        var remainingMinutes: Long? = null
        var openCountLimit: Int? = null
        var openCount: Int = 0
        var remainingCount: Int? = null

        // 使用时长信息
        merged.usageLimit?.let { limit ->
            val limitMs = limit.inWholeMilliseconds
            usageLimitMinutes = limitMs / 60000

            val usedMs = calculateTotalUsage(merged.targetApps, today)

            val remainingMs = limitMs - usedMs
            usedMinutes = usedMs / 60000
            remainingMinutes = if (remainingMs > 0) remainingMs / 60000 else 0
        }

        // 打开次数信息
        merged.openCountLimit?.let { limit ->
            openCountLimit = limit

            openCount = calculateTotalOpenCount(merged.targetApps, today)

            remainingCount = if (limit > openCount) limit - openCount else 0
        }

        return if (usageLimitMinutes != null || openCountLimit != null) {
            UsageInfo(
                usageScopeId = usageScopeId,
                usageLimitMinutes = usageLimitMinutes,
                usedMinutes = usedMinutes,
                remainingMinutes = remainingMinutes,
                openCountLimit = openCountLimit,
                openCount = openCount,
                remainingCount = remainingCount
            )
        } else {
            null
        }
    }

    private suspend fun calculateTotalUsage(targetApps: Set<String>, date: LocalDate): Long {
        Log.d(TAG, "TOTAL_ALL mode: calculating total usage across ${targetApps.size} apps")
        var totalUsage = 0L
        targetApps.forEach { app ->
            val appUsage = usageTrackingRepository.getUsageDuration(app, date)
            totalUsage += appUsage
            Log.d(TAG, "  - $app: ${appUsage}ms")
        }
        Log.d(TAG, "  Total usage: ${totalUsage}ms")
        return totalUsage
    }

    private suspend fun calculateTotalOpenCount(targetApps: Set<String>, date: LocalDate): Int {
        Log.d(TAG, "TOTAL_ALL mode: calculating total open count across ${targetApps.size} apps")
        var totalCount = 0
        targetApps.forEach { app ->
            val appCount = usageTrackingRepository.getOpenCount(app, date)
            totalCount += appCount
            Log.d(TAG, "  - $app: $appCount times")
        }
        Log.d(TAG, "  Total count: $totalCount")
        return totalCount
    }

    private fun buildUsageScopeId(targetApps: Set<String>): String {
        if (targetApps.isEmpty()) return "scope:empty"
        val canonical = targetApps.sorted().joinToString("|")
        return "scope:${targetApps.size}:${canonical.hashCode()}"
    }
}

/**
 * 合并后的限制规则
 */
data class MergedRestriction(
    val timeRestrictions: List<TimeRestriction>,
    val usageLimit: kotlin.time.Duration?,
    val openCountLimit: Int?,
    val targetApps: Set<String>, // 目标应用列表（组内总量计算）
    val enforcementMode: EnforcementMode
)

/**
 * 执行模式的严格程度
 */
val EnforcementMode.strictness: Int
    get() = when (this) {
        EnforcementMode.MONITOR_ONLY -> 1
        EnforcementMode.DIRECT_BLOCK -> 2
        EnforcementMode.FORCE_THROUGH_APP -> 3
    }
