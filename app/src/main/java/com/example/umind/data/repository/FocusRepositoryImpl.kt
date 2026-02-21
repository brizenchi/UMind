package com.example.umind.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import com.example.umind.data.local.dao.FocusStrategyDao
import com.example.umind.data.local.entity.FocusStrategyEntity
import com.example.umind.domain.model.AppInfo
import com.example.umind.domain.model.FocusStrategy
import com.example.umind.domain.model.Result
import com.example.umind.domain.repository.FocusRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of FocusRepository
 */
@Singleton
class FocusRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val focusStrategyDao: FocusStrategyDao,
    private val usageTrackingRepository: UsageTrackingRepository,
    private val temporaryUsageRepository: TemporaryUsageRepository
) : FocusRepository {

    // Cache for installed apps
    @Volatile
    private var cachedApps: List<AppInfo>? = null

    @Volatile
    private var lastLoadTime: Long = 0

    // Cache validity duration: 5 minutes
    private val CACHE_VALIDITY_MS = 5 * 60 * 1000L

    override fun getFocusStrategiesFlow(): Flow<List<FocusStrategy>> {
        return focusStrategyDao.getAllStrategiesFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getFocusStrategies(): Result<List<FocusStrategy>> {
        return try {
            val entities = focusStrategyDao.getAllStrategies()
            val strategies = entities.map { it.toDomainModel() }
            Result.Success(strategies)
        } catch (e: Exception) {
            Result.Error(e, "获取专注策略失败")
        }
    }

    override suspend fun getFocusStrategyById(id: String): Result<FocusStrategy?> {
        return try {
            val entity = focusStrategyDao.getStrategyById(id)
            Result.Success(entity?.toDomainModel())
        } catch (e: Exception) {
            Result.Error(e, "获取专注策略失败")
        }
    }

    override suspend fun getActiveStrategy(): Result<FocusStrategy?> {
        return try {
            val entity = focusStrategyDao.getActiveStrategy()
            Result.Success(entity?.toDomainModel())
        } catch (e: Exception) {
            Result.Error(e, "获取激活策略失败")
        }
    }

    override suspend fun saveFocusStrategy(strategy: FocusStrategy): Result<Unit> {
        return try {
            val entity = FocusStrategyEntity.fromDomainModel(
                strategy.copy(updatedAt = System.currentTimeMillis())
            )
            focusStrategyDao.insertStrategy(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "保存专注策略失败")
        }
    }

    override suspend fun deleteFocusStrategy(id: String): Result<Unit> {
        return try {
            focusStrategyDao.deleteStrategy(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "删除专注策略失败")
        }
    }

    override suspend fun setStrategyActive(id: String, isActive: Boolean): Result<Unit> {
        return try {
            focusStrategyDao.setStrategyActiveExclusive(id, isActive)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "设置策略状态失败")
        }
    }

    override suspend fun getInstalledApps(): Result<List<AppInfo>> {
        // Return cached apps if available and fresh
        val currentTime = System.currentTimeMillis()
        cachedApps?.let { apps ->
            if (currentTime - lastLoadTime < CACHE_VALIDITY_MS) {
                return Result.Success(apps)
            }
        }

        // Load apps if cache is empty or stale
        return loadInstalledApps().also { result ->
            if (result is Result.Success) {
                cachedApps = result.data
                lastLoadTime = currentTime
            }
        }
    }

    /**
     * Force reload apps from system, bypassing cache
     */
    suspend fun refreshInstalledApps(): Result<List<AppInfo>> {
        return loadInstalledApps().also { result ->
            if (result is Result.Success) {
                cachedApps = result.data
                lastLoadTime = System.currentTimeMillis()
            }
        }
    }

    /**
     * Preload apps into cache (call at app startup)
     */
    suspend fun preloadInstalledApps() {
        getInstalledApps()
    }

    /**
     * Load installed apps - optimized version
     * Only queries launcher apps (user-facing apps) without loading icons
     * Icons are loaded on-demand by AppIconLoader
     */
    private suspend fun loadInstalledApps(): Result<List<AppInfo>> {
        return try {
            val pm = context.packageManager
            // Use LinkedHashMap to deduplicate by packageName while preserving order
            val appsMap = linkedMapOf<String, AppInfo>()

            // Only use launcher apps query - faster and more relevant
            val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfos = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(launcherIntent, 0)
            }

            // Build app list without loading icons (much faster)
            // Use map to automatically deduplicate by packageName
            resolveInfos.forEach { resolveInfo ->
                val packageName = resolveInfo.activityInfo?.packageName
                if (packageName != null && packageName != context.packageName) {
                    // Only add if not already in map (avoid duplicates)
                    if (!appsMap.containsKey(packageName)) {
                        val label = resolveInfo.loadLabel(pm)?.toString() ?: packageName
                        // Don't load icon here - will be loaded on-demand by AppIconLoader
                        appsMap[packageName] = AppInfo(packageName, label, icon = null, isSystemApp = false)
                    }
                }
            }

            // If no apps found, add common apps as fallback
            if (appsMap.isEmpty()) {
                val commonApps = listOf(
                    "com.tencent.mm" to "微信",
                    "com.tencent.mobileqq" to "QQ",
                    "com.sina.weibo" to "微博",
                    "com.ss.android.ugc.aweme" to "抖音",
                    "com.taobao.taobao" to "淘宝",
                    "com.jingdong.app.mall" to "京东",
                    "com.eg.android.AlipayGphone" to "支付宝",
                    "tv.danmaku.bili" to "哔哩哔哩",
                    "com.tencent.qqmusic" to "QQ音乐",
                    "com.netease.cloudmusic" to "网易云音乐"
                )

                commonApps.forEach { (pkg, name) ->
                    appsMap[pkg] = AppInfo(pkg, name, icon = null, isSystemApp = false)
                }
            }

            Result.Success(appsMap.values.sortedBy { it.label.lowercase() })
        } catch (e: Exception) {
            Result.Error(e, "获取应用列表失败")
        }
    }

    override suspend fun shouldBlockPackage(packageName: String): Boolean {
        return try {
            // First check if there's an active temporary usage
            if (temporaryUsageRepository.hasActiveTemporaryUsage(packageName)) {
                return false // Don't block if temporary usage is active
            }

            val activeStrategy = focusStrategyDao.getActiveStrategy()?.toDomainModel()
            activeStrategy?.let { strategy ->
                // Check if package is in target apps
                if (!strategy.targetApps.contains(packageName)) {
                    return false
                }

                // Check enforcement mode
                if (strategy.enforcementMode == com.example.umind.domain.model.EnforcementMode.MONITOR_ONLY) {
                    return false // Monitor only, don't block
                }

                // Check time restrictions
                val withinTimeRestriction = strategy.isWithinFocusTime()

                // Check usage limits
                val exceedsUsageLimit = checkUsageLimits(strategy, packageName)

                // Check open count limits
                val exceedsOpenCountLimit = checkOpenCountLimits(strategy, packageName)

                // Block if within time restriction OR exceeds usage/open count limits
                withinTimeRestriction || exceedsUsageLimit || exceedsOpenCountLimit
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if package exceeds usage limits
     */
    private suspend fun checkUsageLimits(strategy: FocusStrategy, packageName: String): Boolean {
        val usageLimits = strategy.usageLimits ?: return false

        // For now, assume daily limits (can be extended to weekly/monthly later)
        val today = java.time.LocalDate.now()

        return when (usageLimits.type) {
            com.example.umind.domain.model.LimitType.TOTAL_ALL -> {
                // Check total usage for all target apps
                val totalUsage = strategy.targetApps.sumOf { pkg ->
                    usageTrackingRepository.getUsageDuration(pkg, today)
                }
                val limit = usageLimits.totalLimit ?: return false
                totalUsage >= limit.inWholeMilliseconds
            }
            com.example.umind.domain.model.LimitType.PER_APP -> {
                // Check usage for this specific app
                val appUsage = usageTrackingRepository.getUsageDuration(packageName, today)
                val limit = usageLimits.perAppLimit ?: return false
                appUsage >= limit.inWholeMilliseconds
            }
        }
    }

    /**
     * Check if package exceeds open count limits
     */
    private suspend fun checkOpenCountLimits(strategy: FocusStrategy, packageName: String): Boolean {
        val openCountLimits = strategy.openCountLimits ?: return false

        // For now, assume daily limits (can be extended to weekly/monthly later)
        val today = java.time.LocalDate.now()

        return when (openCountLimits.type) {
            com.example.umind.domain.model.LimitType.TOTAL_ALL -> {
                // Check total open count for all target apps
                val totalCount = strategy.targetApps.sumOf { pkg ->
                    usageTrackingRepository.getOpenCount(pkg, today)
                }
                val limit = openCountLimits.totalCount ?: return false
                totalCount >= limit
            }
            com.example.umind.domain.model.LimitType.PER_APP -> {
                // Check open count for this specific app
                val appCount = usageTrackingRepository.getOpenCount(packageName, today)
                val limit = openCountLimits.perAppCount ?: return false
                appCount >= limit
            }
            com.example.umind.domain.model.LimitType.PER_APP -> {
                // Check open count for this specific app
                val limit = openCountLimits.perAppCount ?: return false
                val appCount = usageTrackingRepository.getOpenCount(packageName, today)
                appCount >= limit
            }
        }
    }

    override suspend fun getBlockInfo(packageName: String): com.example.umind.domain.model.BlockInfo {
        return try {
            // Check if there's an active temporary usage
            if (temporaryUsageRepository.hasActiveTemporaryUsage(packageName)) {
                return com.example.umind.domain.model.BlockInfo(shouldBlock = false)
            }

            val activeStrategy = focusStrategyDao.getActiveStrategy()?.toDomainModel()
                ?: return com.example.umind.domain.model.BlockInfo(shouldBlock = false)

            // Check if package is in target apps
            if (!activeStrategy.targetApps.contains(packageName)) {
                return com.example.umind.domain.model.BlockInfo(shouldBlock = false)
            }

            // Check enforcement mode
            if (activeStrategy.enforcementMode == com.example.umind.domain.model.EnforcementMode.MONITOR_ONLY) {
                return com.example.umind.domain.model.BlockInfo(shouldBlock = false)
            }

            val today = java.time.LocalDate.now()
            val reasons = mutableListOf<com.example.umind.domain.model.BlockReason>()

            // Usage info tracking
            var usageLimitMinutes: Long? = null
            var usedMinutes: Long = 0
            var remainingMinutes: Long? = null
            var openCountLimit: Int? = null
            var openCount: Int = 0
            var remainingCount: Int? = null

            // Check time restrictions
            val withinTimeRestriction = activeStrategy.isWithinFocusTime()
            if (withinTimeRestriction) {
                // Find next available time
                val nextAvailableTime = activeStrategy.timeRestrictions
                    .mapNotNull { it.endTime }
                    .minOrNull()
                    ?.toString() // Convert LocalTime to String
                reasons.add(com.example.umind.domain.model.BlockReason.TimeRestriction(nextAvailableTime))
            }

            // Check usage limits
            val usageLimits = activeStrategy.usageLimits
            android.util.Log.d("FocusRepository", "=== Checking usage limits ===")
            android.util.Log.d("FocusRepository", "usageLimits: $usageLimits")
            if (usageLimits != null) {
                android.util.Log.d("FocusRepository", "Usage limit type: ${usageLimits.type}")
                when (usageLimits.type) {
                    com.example.umind.domain.model.LimitType.TOTAL_ALL -> {
                        val totalUsage = activeStrategy.targetApps.sumOf { pkg ->
                            usageTrackingRepository.getUsageDuration(pkg, today)
                        }
                        val limit = usageLimits.totalLimit
                        if (limit != null) {
                            val limitMs = limit.inWholeMilliseconds
                            val usedMs = totalUsage
                            val remainingMs = limitMs - usedMs

                            usageLimitMinutes = limitMs / 60000
                            usedMinutes = usedMs / 60000
                            remainingMinutes = if (remainingMs > 0) remainingMs / 60000 else 0

                            if (remainingMs <= 0) {
                                reasons.add(com.example.umind.domain.model.BlockReason.UsageLimitExceeded(
                                    limitMinutes = usageLimitMinutes!!,
                                    usedMinutes = usedMinutes
                                ))
                            }
                        }
                    }
                    com.example.umind.domain.model.LimitType.PER_APP -> {
                        val appUsage = usageTrackingRepository.getUsageDuration(packageName, today)
                        val limit = usageLimits.perAppLimit
                        if (limit != null) {
                            val limitMs = limit.inWholeMilliseconds
                            val usedMs = appUsage
                            val remainingMs = limitMs - usedMs

                            usageLimitMinutes = limitMs / 60000
                            usedMinutes = usedMs / 60000
                            remainingMinutes = if (remainingMs > 0) remainingMs / 60000 else 0

                            if (remainingMs <= 0) {
                                reasons.add(com.example.umind.domain.model.BlockReason.UsageLimitExceeded(
                                    limitMinutes = usageLimitMinutes!!,
                                    usedMinutes = usedMinutes
                                ))
                            }
                        }
                    }
                    com.example.umind.domain.model.LimitType.PER_APP -> {
                        val perAppLimit = usageLimits.perAppLimit
                        if (perAppLimit != null) {
                            val appUsage = usageTrackingRepository.getUsageDuration(packageName, today)
                            val limitMs = perAppLimit.inWholeMilliseconds
                            val usedMs = appUsage
                            val remainingMs = limitMs - usedMs

                            usageLimitMinutes = limitMs / 60000
                            usedMinutes = usedMs / 60000
                            remainingMinutes = if (remainingMs > 0) remainingMs / 60000 else 0

                            if (remainingMs <= 0) {
                                reasons.add(com.example.umind.domain.model.BlockReason.UsageLimitExceeded(
                                    limitMinutes = usageLimitMinutes!!,
                                    usedMinutes = usedMinutes
                                ))
                            }
                        }
                    }
                }
            }

            // Check open count limits
            val openCountLimits = activeStrategy.openCountLimits
            if (openCountLimits != null) {
                android.util.Log.d("FocusRepository", "=== Checking open count limits for $packageName ===")
                android.util.Log.d("FocusRepository", "Limit type: ${openCountLimits.type}")
                android.util.Log.d("FocusRepository", "Target apps: ${activeStrategy.targetApps}")
                when (openCountLimits.type) {
                    com.example.umind.domain.model.LimitType.TOTAL_ALL -> {
                        // Optimized: Batch query all apps at once instead of one by one
                        val allCounts = mutableMapOf<String, Int>()
                        activeStrategy.targetApps.forEach { pkg ->
                            allCounts[pkg] = usageTrackingRepository.getOpenCount(pkg, today)
                        }
                        val totalCount = allCounts.values.sum()

                        val limit = openCountLimits.totalCount
                        android.util.Log.d("FocusRepository", "Total open count: $totalCount, limit: $limit")
                        if (limit != null) {
                            openCountLimit = limit
                            openCount = totalCount
                            remainingCount = if (limit > totalCount) limit - totalCount else 0

                            android.util.Log.d("FocusRepository", "Calculated: openCountLimit=$openCountLimit, openCount=$openCount, remainingCount=$remainingCount")

                            if (totalCount >= limit) {
                                android.util.Log.d("FocusRepository", "!!! BLOCKING: Total count $totalCount >= limit $limit !!!")
                                reasons.add(com.example.umind.domain.model.BlockReason.OpenCountLimitExceeded(
                                    limitCount = limit,
                                    usedCount = totalCount
                                ))
                            } else {
                                android.util.Log.d("FocusRepository", "NOT BLOCKING: Total count $totalCount < limit $limit, remaining: $remainingCount")
                            }
                        }
                    }
                    com.example.umind.domain.model.LimitType.PER_APP -> {
                        val appCount = usageTrackingRepository.getOpenCount(packageName, today)
                        val limit = openCountLimits.perAppCount
                        android.util.Log.d("FocusRepository", "App $packageName open count: $appCount, limit: $limit")
                        if (limit != null) {
                            openCountLimit = limit
                            openCount = appCount
                            remainingCount = if (limit > appCount) limit - appCount else 0

                            if (appCount >= limit) {
                                android.util.Log.d("FocusRepository", "BLOCKING: App count $appCount >= limit $limit")
                                reasons.add(com.example.umind.domain.model.BlockReason.OpenCountLimitExceeded(
                                    limitCount = limit,
                                    usedCount = appCount
                                ))
                            } else {
                                android.util.Log.d("FocusRepository", "NOT BLOCKING: App count $appCount < limit $limit, remaining: $remainingCount")
                            }
                        }
                    }
                    com.example.umind.domain.model.LimitType.PER_APP -> {
                        val perAppLimit = openCountLimits.perAppCount
                        if (perAppLimit != null) {
                            val appCount = usageTrackingRepository.getOpenCount(packageName, today)
                            android.util.Log.d("FocusRepository", "App $packageName per-app open count: $appCount, limit: $perAppLimit")
                            openCountLimit = perAppLimit
                            openCount = appCount
                            remainingCount = if (perAppLimit > appCount) perAppLimit - appCount else 0

                            if (appCount >= perAppLimit) {
                                android.util.Log.d("FocusRepository", "BLOCKING: App count $appCount >= limit $perAppLimit")
                                reasons.add(com.example.umind.domain.model.BlockReason.OpenCountLimitExceeded(
                                    limitCount = perAppLimit,
                                    usedCount = appCount
                                ))
                            } else {
                                android.util.Log.d("FocusRepository", "NOT BLOCKING: App count $appCount < limit $perAppLimit, remaining: $remainingCount")
                            }
                        }
                    }
                }
            }

            // Create usage info if any limits are set
            android.util.Log.d("FocusRepository", "=== Creating usage info ===")
            android.util.Log.d("FocusRepository", "usageLimitMinutes: $usageLimitMinutes")
            android.util.Log.d("FocusRepository", "openCountLimit: $openCountLimit")

            val usageInfo = if (usageLimitMinutes != null || openCountLimit != null) {
                val info = com.example.umind.domain.model.UsageInfo(
                    usageLimitMinutes = usageLimitMinutes,
                    usedMinutes = usedMinutes,
                    remainingMinutes = remainingMinutes,
                    openCountLimit = openCountLimit,
                    openCount = openCount,
                    remainingCount = remainingCount
                )
                android.util.Log.d("FocusRepository", "Created usageInfo: $info")
                info
            } else {
                android.util.Log.d("FocusRepository", "No usage info created (no limits set)")
                null
            }

            // Return BlockInfo
            com.example.umind.domain.model.BlockInfo(
                shouldBlock = reasons.isNotEmpty(),
                reasons = reasons,
                usageInfo = usageInfo
            )
        } catch (e: Exception) {
            com.example.umind.domain.model.BlockInfo(shouldBlock = false)
        }
    }
}
