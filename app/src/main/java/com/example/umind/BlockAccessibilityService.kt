package com.example.umind

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.graphics.PixelFormat
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.content.Context
import android.util.Log
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.umind.domain.repository.FocusRepository
import com.example.umind.data.repository.FocusModeRepository
import com.example.umind.data.repository.UsageTrackingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.umind.util.InternalLaunchTracker

@AndroidEntryPoint
class BlockAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var repository: FocusRepository

    @Inject
    lateinit var focusModeRepository: FocusModeRepository

    @Inject
    lateinit var usageTrackingRepository: UsageTrackingRepository

    @Inject
    lateinit var blockingEngine: com.example.umind.service.BlockingEngine

    @Inject
    lateinit var blockEventRepository: com.example.umind.data.repository.BlockEventRepository

    @Inject
    lateinit var countdownManager: com.example.umind.util.UsageCountdownManager

    @Inject
    lateinit var notificationManager: com.example.umind.util.UsageNotificationManager

    @Inject
    lateinit var focusModeNotificationManager: com.example.umind.util.FocusModeNotificationManager

    private var windowManager: WindowManager? = null
    private var blockView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var systemNotificationManager: NotificationManager? = null

    // Usage tracking state
    private var currentForegroundPackage: String? = null
    private var currentTrackedPackage: String? = null
    private var currentAppStartTime: Long = 0
    private var currentCountdownKey: String? = null
    private val lastOpenRecordAt = mutableMapOf<String, Long>()

    // Performance optimization: Cache block info to reduce database queries
    private val blockInfoCache = mutableMapOf<String, Pair<com.example.umind.domain.model.BlockInfo, Long>>()
    private val BLOCK_INFO_CACHE_MS = 2000L // 2 seconds cache

    // Dialog debounce: Prevent showing dialog too frequently for the same app
    private var lastBlockedPackage: String? = null
    private var lastBlockTime: Long = 0
    private val BLOCK_DIALOG_DEBOUNCE_MS = 3000L // 3 seconds debounce
    private val OPEN_COUNT_DEBOUNCE_MS = 1500L   // 1.5 seconds debounce
    private var lastWindowStatePackage: String? = null
    private var lastWindowStateTime: Long = 0
    private val WINDOW_STATE_DEBOUNCE_MS = 350L
    private val internalLaunchAllowUntil = mutableMapOf<String, Long>()
    private val INTERNAL_LAUNCH_GRACE_MS = 20_000L

    companion object {
        private const val CHANNEL_ID = "focus_usage_channel"
        private const val CHANNEL_NAME = "应用使用提醒"
        private const val FOREGROUND_CHANNEL_ID = "accessibility_service_channel"
        private const val FOREGROUND_CHANNEL_NAME = "无障碍服务"
        private const val FOREGROUND_NOTIFICATION_ID = 1

        /**
         * 临时系统UI - 这些不算作应用切换，应该完全忽略
         * 例如：通知栏、快捷设置面板等
         */
        private val TRANSIENT_SYSTEM_UI = setOf(
            "com.android.systemui",             // 通知栏、快捷设置等
            "com.android.permissioncontroller", // 系统权限弹窗
            "com.google.android.permissioncontroller",
            "android"                           // 部分系统临时弹窗
        )

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

        fun openAccessibilitySettingsIntent(): Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        systemNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 创建前台服务通知渠道
        createForegroundNotificationChannel()

        // 作为前台服务运行，防止被MIUI等系统杀死
        startForegroundService()

        // 测试通知 - 确认通知系统工作正常
        showTestNotification()

        // 监听专注模式状态，显示计时通知
        startFocusModeNotificationMonitor()

        serviceInfo = serviceInfo.apply {
            // 仅监听窗口状态变化，避免 CONTENT_CHANGED 带来的高频事件风暴
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 120
        }

        Log.d("BlockAccessibilityService", "Service connected and configured as foreground service")
    }

    private fun showTestNotification() {
        try {
            Log.d("BlockAccessibilityService", "=== Showing test notification ===")

            // 检查通知权限
            if (!areNotificationsEnabled()) {
                Log.e("BlockAccessibilityService", "!!! NOTIFICATIONS ARE DISABLED !!!")
                Log.e("BlockAccessibilityService", "Please enable notifications in app settings")
                return
            }

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("UMind 服务已启动")
                .setContentText("无障碍服务正在运行，通知系统正常")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            systemNotificationManager?.notify(999, notification)
            Log.d("BlockAccessibilityService", "Test notification shown with ID 999")
        } catch (e: Exception) {
            Log.e("BlockAccessibilityService", "Error showing test notification", e)
        }
    }

    /**
     * 监听专注模式状态，显示计时通知
     */
    private fun startFocusModeNotificationMonitor() {
        serviceScope.launch {
            focusModeRepository.getFocusMode().collect { focusMode ->
                Log.d("BlockAccessibilityService", "Focus mode changed: isEnabled=${focusMode.isEnabled}, type=${focusMode.modeType}")

                if (focusMode.shouldBeActive()) {
                    // 专注模式激活，显示通知
                    focusModeNotificationManager.startNotification(focusMode, serviceScope)
                } else {
                    // 专注模式未激活，停止通知
                    focusModeNotificationManager.stopNotification()
                }
            }
        }
    }

    /**
     * 创建前台服务通知渠道
     */
    private fun createForegroundNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                FOREGROUND_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // 低重要性，不会发出声音
            ).apply {
                description = "保持UMind无障碍服务持续运行"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            systemNotificationManager?.createNotificationChannel(channel)
            Log.d("BlockAccessibilityService", "Foreground notification channel created")
        }
    }

    /**
     * 启动前台服务
     * 这是防止MIUI等系统杀死服务的关键
     */
    private fun startForegroundService() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("UMind 正在运行")
                .setContentText("应用监控服务已启用，点击打开应用")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true) // 持续通知，不可滑动删除
                .setContentIntent(pendingIntent)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    FOREGROUND_NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(FOREGROUND_NOTIFICATION_ID, notification)
            }

            Log.d("BlockAccessibilityService", "Started as foreground service")
        } catch (e: Exception) {
            Log.e("BlockAccessibilityService", "Error starting foreground service", e)
        }
    }

    private fun areNotificationsEnabled(): Boolean {
        val notificationMgr = systemNotificationManager ?: return false

        // 检查通知是否被全局禁用
        val areEnabled = notificationMgr.areNotificationsEnabled()
        Log.d("BlockAccessibilityService", "Notifications enabled: $areEnabled")

        // 检查通知渠道是否被禁用 (Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationMgr.getNotificationChannel(CHANNEL_ID)
            if (channel != null) {
                val importance = channel.importance
                Log.d("BlockAccessibilityService", "Channel importance: $importance (NONE=0, MIN=1, LOW=2, DEFAULT=3, HIGH=4, MAX=5)")
                if (importance == NotificationManager.IMPORTANCE_NONE) {
                    Log.e("BlockAccessibilityService", "!!! Notification channel is disabled !!!")
                    return false
                }
            } else {
                Log.e("BlockAccessibilityService", "!!! Notification channel not found !!!")
            }
        }

        return areEnabled
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                return
            }

            val packageName = event.packageName?.toString() ?: run {
                return
            }

            val eventNow = System.currentTimeMillis()
            if (packageName == lastWindowStatePackage &&
                (eventNow - lastWindowStateTime) < WINDOW_STATE_DEBOUNCE_MS
            ) {
                return
            }
            lastWindowStatePackage = packageName
            lastWindowStateTime = eventNow

            // 忽略临时系统窗口（如通知栏、输入法、权限弹窗），这些不算作应用切换
            if (isTransientSystemWindow(packageName, event.className)) {
                return
            }

            // 处理应用切换时的倒计时暂停（在检查白名单之前）
            // 这样即使切换到桌面或系统应用，也会暂停上一个应用的倒计时
            if (packageName != currentForegroundPackage) {
                currentForegroundPackage?.let { previousPackage ->
                    if (previousPackage != packageName) {
                        internalLaunchAllowUntil.remove(previousPackage)
                    }
                }

                currentCountdownKey?.let { countdownKey ->
                    // 暂停当前应用组共享倒计时
                    if (countdownManager.isRunning(countdownKey)) {
                        Log.d("BlockAccessibilityService", "Pausing countdown for $countdownKey (switching to $packageName)")
                        countdownManager.pauseCountdown(countdownKey)
                    }
                    // 暂停通知更新（但保持通知可见）
                    notificationManager.pauseNotification(countdownKey)
                }

                // 切换到任意其他包时，立即结束当前受管应用 session，避免时长落库滞后
                endTrackedSessionIfNeeded(packageName)

                currentForegroundPackage?.let { previousPackage ->
                    // 记录使用时长
                    if (currentAppStartTime > 0) {
                        val usageDuration = System.currentTimeMillis() - currentAppStartTime
                        Log.d("BlockAccessibilityService", "App switch: $previousPackage used ${usageDuration}ms")
                    }
                }
            }

            // 忽略系统关键应用和 UMind 自己
            if (isSystemWhitelistedApp(packageName)) {
                Log.d("BlockAccessibilityService", "Ignoring whitelisted package: $packageName")
                // 更新当前前台应用为白名单应用（这样下次切换时能正确暂停）
                currentForegroundPackage = packageName
                currentAppStartTime = 0
                return
            }

            Log.d("BlockAccessibilityService", "Processing window change to: $packageName")

            // 使用新的 BlockingEngine 检查是否应该被阻止
            serviceScope.launch {
                try {
                    Log.d("BlockAccessibilityService", "=== Checking block info for: $packageName ===")

                    val consumedInternalLaunch = InternalLaunchTracker.consumeIfMatched(
                        this@BlockAccessibilityService,
                        packageName
                    )
                    if (consumedInternalLaunch) {
                        val nowTs = System.currentTimeMillis()
                        internalLaunchAllowUntil[packageName] = nowTs + INTERNAL_LAUNCH_GRACE_MS
                        Log.d("BlockAccessibilityService", "Internal launch detected for $packageName")
                    }
                    val nowTs = System.currentTimeMillis()
                    val openedFromUMind = consumedInternalLaunch ||
                        ((internalLaunchAllowUntil[packageName] ?: 0L) > nowTs)

                    // Check cache first to reduce database queries
                    val now = System.currentTimeMillis()
                    val cacheKey = "$packageName|${if (openedFromUMind) "INTERNAL" else "EXTERNAL"}"
                    val cachedInfo = blockInfoCache[cacheKey]
                    val blockInfo = if (cachedInfo != null && (now - cachedInfo.second) < BLOCK_INFO_CACHE_MS) {
                        Log.d("BlockAccessibilityService", "Using cached block info for $packageName")
                        cachedInfo.first
                    } else {
                        // 使用 BlockingEngine 获取阻止信息
                        val info = blockingEngine.getBlockInfo(
                            packageName = packageName,
                            openedFromUMind = openedFromUMind
                        )
                        // Cache the result
                        blockInfoCache[cacheKey] = Pair(info, now)
                        // Clean old cache entries (keep only last 10)
                        if (blockInfoCache.size > 10) {
                            val oldestKey = blockInfoCache.minByOrNull { it.value.second }?.key
                            oldestKey?.let { blockInfoCache.remove(it) }
                        }
                        info
                    }

                    Log.d("BlockAccessibilityService", "Block info: shouldBlock=${blockInfo.shouldBlock}, reasons=${blockInfo.reasons.size}")
                    blockInfo.usageInfo?.let { info ->
                        Log.d("BlockAccessibilityService", "Usage info: remainingMinutes=${info.remainingMinutes}, remainingCount=${info.remainingCount}")
                    }

                    if (blockInfo.shouldBlock) {
                        Log.d("BlockAccessibilityService", "!!! BLOCKING APP: $packageName !!!")
                        Log.d("BlockAccessibilityService", "Block reasons: ${blockInfo.reasons.joinToString { it.toString() }}")

                        // 防抖：检查是否在短时间内已经阻止过同一个应用
                        val now = System.currentTimeMillis()
                        if (lastBlockedPackage == packageName && (now - lastBlockTime) < BLOCK_DIALOG_DEBOUNCE_MS) {
                            Log.d("BlockAccessibilityService", "Debouncing: Already blocked $packageName recently, skipping dialog")
                            // 仍然返回桌面，但不显示弹窗
                            performGlobalAction(GLOBAL_ACTION_HOME)
                            return@launch
                        }

                        // 更新防抖记录
                        lastBlockedPackage = packageName
                        lastBlockTime = now

                        // 获取应用名称
                        val appName = try {
                            val pm = packageManager
                            val appInfo = pm.getApplicationInfo(packageName, 0)
                            pm.getApplicationLabel(appInfo).toString()
                        } catch (e: Exception) {
                            packageName
                        }

                        // 记录阻止事件
                        blockEventRepository.recordBlockEvent(
                            packageName = packageName,
                            appName = appName,
                            blockInfo = blockInfo
                        )

                        // 立即返回桌面，阻止应用打开
                        val homeSuccess = performGlobalAction(GLOBAL_ACTION_HOME)
                        Log.d("BlockAccessibilityService", "Home action result: $homeSuccess")

                        // 立即显示弹窗
                        handler.post {
                            showBlockDialog(packageName, blockInfo)
                        }

                        // 更新 currentForegroundPackage 以避免重复处理
                        currentForegroundPackage = packageName
                    } else {
                        Log.d("BlockAccessibilityService", "Not blocking $packageName, tracking app switch")
                        // 只有在不阻止的情况下才记录打开和使用
                        trackAppSwitch(packageName, blockInfo)
                    }
                } catch (e: Exception) {
                    Log.e("BlockAccessibilityService", "Error processing accessibility event for $packageName", e)
                }
            }
        } catch (e: Exception) {
            Log.e("BlockAccessibilityService", "Fatal error in onAccessibilityEvent", e)
        }
    }

    /**
     * Track app switch and record usage
     * 处理应用切换时的倒计时暂停/恢复逻辑
     */
    private suspend fun trackAppSwitch(
        newPackageName: String,
        blockInfo: com.example.umind.domain.model.BlockInfo
    ) {
        try {
            val now = System.currentTimeMillis()
            val previousCountdownKey = currentCountdownKey

            // 检查是否是同一个应用
            val isSameApp = currentForegroundPackage == newPackageName

            if (isSameApp) {
                Log.d("BlockAccessibilityService", "Same app $newPackageName, skipping countdown restart")
                return // 同一个应用，不需要重新启动倒计时
            }

            // 更新当前前台应用状态
            currentForegroundPackage = newPackageName
            currentAppStartTime = now

            // 非日常管理应用不进入统计，且清理任何旧通知
            if (!blockInfo.isUnderDailyManagement) {
                Log.d("BlockAccessibilityService", "Package $newPackageName is not under daily management, skip tracking")
                previousCountdownKey?.let { notificationManager.cancelNotification(it) }
                currentCountdownKey = null
                return
            }

            // 日常管理应用：启动 session
            withContext(Dispatchers.IO) {
                usageTrackingRepository.startSession(newPackageName)
            }
            currentTrackedPackage = newPackageName
            Log.d("BlockAccessibilityService", "Started managed session for $newPackageName")

            // 打开次数去重：短时间重复窗口抖动不重复计数
            val didRecordOpen = if (shouldRecordOpen(newPackageName, now)) {
                withContext(Dispatchers.IO) {
                    usageTrackingRepository.recordAppOpen(newPackageName)
                }
                Log.d("BlockAccessibilityService", "Recorded app open for $newPackageName")
                true
            } else {
                Log.d("BlockAccessibilityService", "Skip duplicate app open for $newPackageName")
                false
            }

        // 处理新应用的倒计时和通知（只在真正切换应用时执行）
        Log.d("BlockAccessibilityService", "=== Processing notifications for $newPackageName ===")
        Log.d("BlockAccessibilityService", "usageInfo: ${blockInfo.usageInfo}")
        blockInfo.usageInfo?.let { usageInfo ->
            Log.d("BlockAccessibilityService", "usageLimitMinutes: ${usageInfo.usageLimitMinutes}")
            Log.d("BlockAccessibilityService", "remainingMinutes: ${usageInfo.remainingMinutes}")
            Log.d("BlockAccessibilityService", "openCountLimit: ${usageInfo.openCountLimit}")
            Log.d("BlockAccessibilityService", "remainingCount: ${usageInfo.remainingCount}")

            // 计算实际显示的剩余次数（仅当本次真的记了一次打开事件时再减 1）
            val displayRemainingCount = usageInfo.remainingCount?.let { count ->
                if (didRecordOpen) (count - 1).coerceAtLeast(0) else count
            }
            Log.d("BlockAccessibilityService", "displayRemainingCount: $displayRemainingCount")

            // 获取应用名称
            val pm = packageManager
            val appName = try {
                val appInfo = pm.getApplicationInfo(newPackageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                newPackageName
            }
            val countdownKey = usageInfo.usageScopeId ?: newPackageName
            val countdownDisplayName = if (usageInfo.usageScopeId != null) "应用组" else appName
            currentCountdownKey = countdownKey
            if (previousCountdownKey != null && previousCountdownKey != countdownKey) {
                // 切换到新的计时作用域时，移除旧作用域通知，避免多个通知堆叠造成“分开计时”的感知
                notificationManager.cancelNotification(previousCountdownKey)
            }

            // 如果有时间限制，启动或恢复倒计时
            usageInfo.usageLimitMinutes?.let { limitMinutes ->
                val limitMillis = limitMinutes * 60 * 1000
                val usedMillis = usageInfo.usedMinutes * 60 * 1000

                Log.d("BlockAccessibilityService", "Time limit found: ${limitMinutes}min, used: ${usageInfo.usedMinutes}min")

                // 检查是否已经有暂停的倒计时状态
                val hasExistingCountdown = countdownManager.hasCountdown(countdownKey)
                Log.d("BlockAccessibilityService", "hasExistingCountdown: $hasExistingCountdown")

                if (hasExistingCountdown) {
                    // 已经有倒计时状态，恢复它
                    Log.d("BlockAccessibilityService", "=== RESUMING existing countdown for $countdownKey ===")

                    // 获取当前剩余时间
                    val remainingMillis = countdownManager.getRemainingMillis(countdownKey)
                    Log.d("BlockAccessibilityService", "Remaining time from manager: ${remainingMillis}ms (${remainingMillis/1000}s)")

                    // 恢复倒计时管理器
                    countdownManager.resumeCountdown(countdownKey)
                    Log.d("BlockAccessibilityService", "Countdown manager resumed")

                    // 启动实时倒计时通知
                    if (remainingMillis > 0) {
                        notificationManager.startCountdownNotification(
                            packageName = countdownKey,
                            appName = countdownDisplayName,
                            remainingMillis = remainingMillis,
                            remainingCount = displayRemainingCount,
                            scope = serviceScope
                        )
                        Log.d("BlockAccessibilityService", "Notification started with remaining time")
                    } else {
                        Log.w("BlockAccessibilityService", "Remaining time is 0, not starting notification")
                    }
                } else {
                    // 没有倒计时状态，创建新的
                    val remainingMillis = limitMillis - usedMillis
                    Log.d("BlockAccessibilityService", "=== STARTING NEW countdown for $countdownKey ===")
                    Log.d("BlockAccessibilityService", "limit=${limitMillis}ms, used=${usedMillis}ms, remaining=${remainingMillis}ms")

                    // 启动实时倒计时通知
                    notificationManager.startCountdownNotification(
                        packageName = countdownKey,
                        appName = countdownDisplayName,
                        remainingMillis = remainingMillis,
                        remainingCount = displayRemainingCount,
                        scope = serviceScope
                    )

                    countdownManager.startCountdown(
                        key = countdownKey,
                        limitMillis = limitMillis,
                        usedMillis = usedMillis,
                        onUpdate = { _ ->
                            // 不需要在这里更新通知，因为通知会自动实时更新
                        },
                        onTimeUp = {
                            // 时间用完，强制退出应用
                            Log.d("BlockAccessibilityService", "!!! TIME'S UP for $newPackageName !!!")
                            performGlobalAction(GLOBAL_ACTION_HOME)
                            handler.post {
                                showTimeUpDialog(newPackageName, appName)
                            }
                            notificationManager.cancelNotification(countdownKey)
                        }
                    )
                    Log.d("BlockAccessibilityService", "Countdown manager started")
                }
            } ?: run {
                // 没有时间限制，只显示次数信息
                Log.d("BlockAccessibilityService", "No time limit, checking count limit")

                if (usageInfo.remainingCount != null) {
                    Log.d("BlockAccessibilityService", "Showing count-only notification: remainingCount=${displayRemainingCount}")
                    notificationManager.showOrUpdateNotification(
                        packageName = countdownKey,
                        appName = countdownDisplayName,
                        remainingMillis = null,
                        remainingCount = displayRemainingCount
                    )
                } else {
                    Log.d("BlockAccessibilityService", "No count limit either, no notification to show")
                }
            }
            } ?: run {
                Log.d("BlockAccessibilityService", "No usageInfo, no notification to show")
                previousCountdownKey?.let { notificationManager.cancelNotification(it) }
                currentCountdownKey = null
            }
        } catch (e: Exception) {
            Log.e("BlockAccessibilityService", "Error in trackAppSwitch for $newPackageName", e)
        }
    }

    private fun showFocusModeBlockDialog(packageName: String) {
        try {
            Log.d("BlockAccessibilityService", "Showing focus mode block dialog for: $packageName")

            // 移除之前的弹窗
            dismissBlockDialog()

            // 获取应用名称
            val pm = packageManager
            val appName = try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                packageName
            }

            Log.d("BlockAccessibilityService", "App name: $appName")

            // 创建全屏半透明背景
            val blockLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(0xCC000000.toInt()) // 半透明黑色背景
                gravity = android.view.Gravity.CENTER
                setPadding(40, 40, 40, 40)
            }

            // 创建白色卡片容器
            val cardLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(80, 80, 80, 80)
                setBackgroundColor(ContextCompat.getColor(this@BlockAccessibilityService, android.R.color.white))
                elevation = 16f
            }

            // 标题
            val titleText = TextView(this).apply {
                text = "🧘 专注模式"
                textSize = 24f
                setTextColor(ContextCompat.getColor(this@BlockAccessibilityService, android.R.color.black))
                setPadding(0, 0, 0, 30)
                gravity = android.view.Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            cardLayout.addView(titleText)

            // 内容
            val contentText = TextView(this).apply {
                text = "应用「$appName」不在白名单中\n\n专注模式已开启，只有白名单中的应用可以使用\n\n请保持专注！"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@BlockAccessibilityService, android.R.color.black))
                setPadding(0, 0, 0, 40)
                gravity = android.view.Gravity.START
            }
            cardLayout.addView(contentText)

            // 确定按钮
            val okButton = Button(this).apply {
                text = "我知道了"
                textSize = 16f
                isFocusable = false
                isFocusableInTouchMode = false
                setOnClickListener {
                    Log.d("BlockAccessibilityService", "User dismissed focus mode dialog")
                    dismissBlockDialog()
                }
            }
            cardLayout.addView(okButton)

            // 添加整个布局的点击监听
            blockLayout.setOnClickListener {
                Log.d("BlockAccessibilityService", "Background clicked, dismissing dialog")
                dismissBlockDialog()
            }

            // 将卡片添加到全屏布局
            blockLayout.addView(cardLayout)

            // 设置窗口参数
            val layoutParams = WindowManager.LayoutParams().apply {
                type = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                }
                format = PixelFormat.TRANSLUCENT
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                gravity = android.view.Gravity.CENTER
            }

            // 显示弹窗
            windowManager?.addView(blockLayout, layoutParams)
            blockView = blockLayout

            Log.d("BlockAccessibilityService", "Focus mode dialog displayed successfully")

            // 3秒后自动关闭弹窗
            handler.postDelayed({
                Log.d("BlockAccessibilityService", "Auto-dismissing focus mode dialog")
                dismissBlockDialog()
            }, 3000)

        } catch (e: Exception) {
            Log.e("BlockAccessibilityService", "Error showing focus mode block dialog", e)
        }
    }

    private fun showBlockDialog(packageName: String, blockInfo: com.example.umind.domain.model.BlockInfo) {
        try {
            Log.d("BlockAccessibilityService", "Showing block dialog for: $packageName")

            // 移除之前的弹窗
            dismissBlockDialog()

            // 获取应用名称
            val pm = packageManager
            val appName = try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                packageName
            }

            Log.d("BlockAccessibilityService", "App name: $appName")

            // 创建全屏半透明背景
            val blockLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(0xCC000000.toInt()) // 半透明黑色背景
                gravity = android.view.Gravity.CENTER
                setPadding(40, 40, 40, 40)
            }

            // 创建白色卡片容器
            val cardLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(80, 80, 80, 80)
                setBackgroundColor(ContextCompat.getColor(this@BlockAccessibilityService, android.R.color.white))
                elevation = 16f
            }

            // 标题
            val titleText = TextView(this).apply {
                text = "⛔ 专注模式已启用"
                textSize = 24f
                setTextColor(ContextCompat.getColor(this@BlockAccessibilityService, android.R.color.black))
                setPadding(0, 0, 0, 30)
                gravity = android.view.Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            cardLayout.addView(titleText)

            // 内容 - 显示阻止原因
            val reasonsText = blockInfo.reasons.joinToString("\n") { reason ->
                when (reason) {
                    is com.example.umind.domain.model.BlockReason.TimeRestriction ->
                        "• 当前时间段内限制使用"
                    is com.example.umind.domain.model.BlockReason.UsageLimitExceeded ->
                        "• 使用时长已达上限\n  限制: ${reason.limitMinutes}分钟\n  已用: ${reason.usedMinutes}分钟"
                    is com.example.umind.domain.model.BlockReason.OpenCountLimitExceeded ->
                        "• 打开次数已达上限\n  限制: ${reason.limitCount}次\n  已用: ${reason.usedCount}次"
                    is com.example.umind.domain.model.BlockReason.FocusModeActive ->
                        "• 专注模式已开启\n  只有白名单中的应用可以使用"
                    is com.example.umind.domain.model.BlockReason.ForceThroughApp ->
                        "• 需要通过 UMind 应用内打开\n  请从 UMind 的受限应用列表中打开"
                }
            }

            // 添加调试信息
            val debugInfo = buildString {
                blockInfo.usageInfo?.let { info ->
                    append("\n\n[调试信息]\n")
                    info.openCountLimit?.let { append("次数限制: $it\n") }
                    append("当前次数: ${info.openCount}\n")
                    info.remainingCount?.let { append("剩余次数: $it\n") }
                }
            }

            val contentText = TextView(this).apply {
                text = "应用「$appName」已被阻止\n\n$reasonsText$debugInfo\n\n现在是专注时间，请保持专注！"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@BlockAccessibilityService, android.R.color.black))
                setPadding(0, 0, 0, 40)
                gravity = android.view.Gravity.START
            }
            cardLayout.addView(contentText)

            // 确定按钮
            val okButton = Button(this).apply {
                text = "我知道了"
                textSize = 16f
                // 确保按钮可以在不获取焦点的窗口中点击
                isFocusable = false
                isFocusableInTouchMode = false
                setOnClickListener {
                    Log.d("BlockAccessibilityService", "User dismissed dialog")
                    dismissBlockDialog()
                }
            }
            cardLayout.addView(okButton)

            // 添加整个布局的点击监听，作为备用关闭方式
            blockLayout.setOnClickListener {
                Log.d("BlockAccessibilityService", "Background clicked, dismissing dialog")
                dismissBlockDialog()
            }

            // 将卡片添加到全屏布局
            blockLayout.addView(cardLayout)

            // 设置窗口参数 - 使用全屏覆盖
            val layoutParams = WindowManager.LayoutParams().apply {
                type = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                }
                format = PixelFormat.TRANSLUCENT
                // 关键：添加FLAG_NOT_FOCUSABLE，防止弹窗启动focus app
                // 同时保持FLAG_NOT_TOUCH_MODAL，允许点击弹窗外的区域
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                // 居中显示
                gravity = android.view.Gravity.CENTER
            }

            // 显示弹窗
            windowManager?.addView(blockLayout, layoutParams)
            blockView = blockLayout

            Log.d("BlockAccessibilityService", "Dialog displayed successfully")

            // 不自动关闭弹窗，需要用户手动点击"我知道了"或点击背景关闭
            // 这样可以确保用户看到了阻止信息

        } catch (e: Exception) {
            Log.e("BlockAccessibilityService", "Error showing block dialog", e)
        }
    }

    private fun showTimeUpDialog(packageName: String, appName: String) {
        try {
            Log.d("BlockAccessibilityService", "Showing time up dialog for: $packageName")

            // 移除之前的弹窗
            dismissBlockDialog()

            // 创建全屏半透明背景
            val blockLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(0xCC000000.toInt())
                gravity = android.view.Gravity.CENTER
                setPadding(40, 40, 40, 40)
            }

            // 创建白色卡片容器
            val cardLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(80, 80, 80, 80)
                setBackgroundColor(ContextCompat.getColor(this@BlockAccessibilityService, android.R.color.white))
                elevation = 16f
            }

            // 标题
            val titleText = TextView(this).apply {
                text = "⏰ 使用时间已到"
                textSize = 24f
                setTextColor(ContextCompat.getColor(this@BlockAccessibilityService, android.R.color.black))
                setPadding(0, 0, 0, 30)
                gravity = android.view.Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            cardLayout.addView(titleText)

            // 内容
            val contentText = TextView(this).apply {
                text = "应用「$appName」的使用时间已用完\n\n请注意休息，保持专注！"
                textSize = 18f
                setTextColor(ContextCompat.getColor(this@BlockAccessibilityService, android.R.color.black))
                setPadding(0, 0, 0, 40)
                gravity = android.view.Gravity.CENTER
            }
            cardLayout.addView(contentText)

            // 确定按钮
            val okButton = Button(this).apply {
                text = "我知道了"
                textSize = 16f
                isFocusable = false
                isFocusableInTouchMode = false
                setOnClickListener {
                    Log.d("BlockAccessibilityService", "User dismissed time up dialog")
                    dismissBlockDialog()
                }
            }
            cardLayout.addView(okButton)

            // 添加整个布局的点击监听
            blockLayout.setOnClickListener {
                Log.d("BlockAccessibilityService", "Background clicked, dismissing time up dialog")
                dismissBlockDialog()
            }

            // 将卡片添加到全屏布局
            blockLayout.addView(cardLayout)

            // 设置窗口参数
            val layoutParams = WindowManager.LayoutParams().apply {
                type = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                }
                format = PixelFormat.TRANSLUCENT
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                gravity = android.view.Gravity.CENTER
            }

            // 显示弹窗
            windowManager?.addView(blockLayout, layoutParams)
            blockView = blockLayout

            Log.d("BlockAccessibilityService", "Time up dialog displayed successfully")

            // 3秒后自动关闭弹窗
            handler.postDelayed({
                Log.d("BlockAccessibilityService", "Auto-dismissing time up dialog")
                dismissBlockDialog()
            }, 3000)

        } catch (e: Exception) {
            Log.e("BlockAccessibilityService", "Error showing time up dialog", e)
        }
    }

    private fun dismissBlockDialog() {
        try {
            blockView?.let { view ->
                windowManager?.removeView(view)
                blockView = null
            }
        } catch (e: Exception) {
            Log.e("BlockAccessibilityService", "Error dismissing dialog", e)
        }
    }

    override fun onInterrupt() {
        dismissBlockDialog()
    }

    /**
     * 检查是否是系统白名单应用
     * 这些应用永远不会被阻止
     */
    private fun isSystemWhitelistedApp(packageName: String): Boolean {
        // 1. 检查是否是 UMind 自己
        if (packageName == this.packageName) {
            return true
        }

        // 2. 检查是否在系统白名单中
        if (packageName in SYSTEM_WHITELIST) {
            return true
        }

        // 3. 检查是否是系统应用（额外保护）
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            // 检查是否是系统应用或系统更新应用
            val isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            val isSystemUpdate = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            // 如果是系统应用且包名包含关键词，也加入白名单
            if (isSystemApp || isSystemUpdate) {
                val systemKeywords = listOf("launcher", "settings", "phone", "dialer", "mms", "messaging")
                if (systemKeywords.any { packageName.contains(it, ignoreCase = true) }) {
                    Log.d("BlockAccessibilityService", "System app with keyword detected: $packageName")
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e("BlockAccessibilityService", "Error checking system app: $packageName", e)
        }

        return false
    }

    /**
     * 判断是否是临时系统窗口（不应视为真实应用切换）
     */
    private fun isTransientSystemWindow(packageName: String, className: CharSequence?): Boolean {
        if (packageName in TRANSIENT_SYSTEM_UI) return true

        val normalizedPackage = packageName.lowercase()
        if (normalizedPackage.contains("inputmethod") || normalizedPackage.contains("keyboard")) {
            return true
        }

        val normalizedClass = className?.toString()?.lowercase().orEmpty()
        if (normalizedClass.contains("softinputwindow") || normalizedClass.contains("inputmethod")) {
            return true
        }

        return false
    }

    /**
     * 在应用切换时结束当前受管 session（如果目标包不是当前受管包）
     */
    private fun endTrackedSessionIfNeeded(nextPackageName: String) {
        val trackedPackage = currentTrackedPackage ?: return
        if (trackedPackage == nextPackageName) return

        currentTrackedPackage = null
        serviceScope.launch(Dispatchers.IO) {
            usageTrackingRepository.endSession(trackedPackage)
            Log.d("BlockAccessibilityService", "Ended managed session for $trackedPackage")
        }
    }

    /**
     * 开启次数去重，防止窗口抖动导致短时间重复计数
     */
    private fun shouldRecordOpen(packageName: String, now: Long): Boolean {
        val lastTs = lastOpenRecordAt[packageName]
        if (lastTs != null && (now - lastTs) < OPEN_COUNT_DEBOUNCE_MS) {
            return false
        }
        lastOpenRecordAt[packageName] = now
        if (lastOpenRecordAt.size > 128) {
            val expireBefore = now - 10 * 60 * 1000L
            val iterator = lastOpenRecordAt.entries.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().value < expireBefore) {
                    iterator.remove()
                }
            }
        }
        return true
    }

    override fun onDestroy() {
        // 结束当前应用的 session
        currentTrackedPackage?.let { packageName ->
            serviceScope.launch(Dispatchers.IO) {
                usageTrackingRepository.endSession(packageName)
            }
        }
        // 清理所有倒计时
        countdownManager.cleanup()
        // 取消所有通知
        notificationManager.cancelAllNotifications()
        // 停止专注模式通知
        focusModeNotificationManager.stopNotification()
        // 清理对话框
        dismissBlockDialog()
        super.onDestroy()
    }
}
