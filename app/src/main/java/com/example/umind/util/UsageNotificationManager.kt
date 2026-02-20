package com.example.umind.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.umind.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 管理使用情况通知 - 实时倒计时版本
 * 参考 FocusModeNotificationManager 实现
 */
@Singleton
class UsageNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // 每个应用的更新任务
    private val updateJobs = mutableMapOf<String, Job>()
    // 每个应用的通知ID
    private val notificationIds = mutableMapOf<String, Int>()
    private var nextNotificationId = 3000

    // 存储倒计时信息
    private data class CountdownInfo(
        val packageName: String,
        val appName: String,
        val targetEndTime: Long, // 目标结束时间
        val remainingCount: Int? = null
    )
    private val countdownInfos = mutableMapOf<String, CountdownInfo>()

    companion object {
        private const val TAG = "UsageNotificationMgr"
        private const val CHANNEL_ID = "focus_usage_channel"
        private const val CHANNEL_NAME = "应用使用提醒"
    }

    init {
        createNotificationChannel()
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // 低重要性，不会发出声音
            ).apply {
                description = "显示应用使用时间和次数限制的实时倒计时"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    /**
     * 开始显示实时倒计时通知
     * 完全参考 FocusModeNotificationManager 的实现
     * @param packageName 应用包名
     * @param appName 应用名称
     * @param remainingMillis 剩余时间（毫秒）
     * @param remainingCount 剩余次数
     * @param scope 协程作用域
     */
    fun startCountdownNotification(
        packageName: String,
        appName: String,
        remainingMillis: Long,
        remainingCount: Int? = null,
        scope: CoroutineScope
    ) {
        Log.d(TAG, "=== Starting countdown notification for $packageName ===")
        Log.d(TAG, "remainingMillis: $remainingMillis, remainingCount: $remainingCount")

        // 检查通知权限
        if (!areNotificationsEnabled()) {
            Log.e(TAG, "!!! Notifications are disabled !!!")
            return
        }

        // 取消之前的更新任务
        updateJobs[packageName]?.cancel()
        Log.d(TAG, "Cancelled previous job for $packageName")

        // 计算目标结束时间
        val targetEndTime = System.currentTimeMillis() + remainingMillis

        // 保存倒计时信息
        countdownInfos[packageName] = CountdownInfo(
            packageName = packageName,
            appName = appName,
            targetEndTime = targetEndTime,
            remainingCount = remainingCount
        )

        // 获取或创建通知ID
        val notificationId = notificationIds.getOrPut(packageName) {
            nextNotificationId++
        }
        Log.d(TAG, "Using notification ID: $notificationId for $packageName")

        // 启动新的更新任务 - 完全按照 FocusModeNotificationManager 的方式
        updateJobs[packageName] = scope.launch {
            Log.d(TAG, "Coroutine started for $packageName")
            while (isActive) {
                val now = System.currentTimeMillis()
                val remaining = (targetEndTime - now).coerceAtLeast(0)

                Log.d(TAG, "Updating notification for $packageName, remaining: ${remaining}ms")

                // 先更新通知，再检查是否结束（和 FocusModeNotificationManager 一样）
                updateNotification(packageName, appName, remaining, remainingCount, notificationId)

                if (remaining <= 0) {
                    // 时间用完，停止更新
                    Log.d(TAG, "Time's up for $packageName, stopping notification updates")
                    break
                }

                // 每秒更新一次
                delay(1000)
            }
            Log.d(TAG, "Coroutine ended for $packageName")
        }
        Log.d(TAG, "=== Countdown notification started for $packageName ===")
    }

    /**
     * 显示或更新使用情况通知（旧版本，保持兼容）
     */
    fun showOrUpdateNotification(
        packageName: String,
        appName: String,
        remainingMillis: Long? = null,
        remainingCount: Int? = null
    ) {
        Log.d(TAG, "showOrUpdateNotification for $packageName (legacy method)")

        // 检查通知权限
        if (!areNotificationsEnabled()) {
            Log.e(TAG, "Notifications are disabled")
            return
        }

        // 获取或创建通知ID
        val notificationId = notificationIds.getOrPut(packageName) {
            nextNotificationId++
        }

        // 构建通知内容
        val contentText = buildContentText(remainingMillis, remainingCount)

        // 创建点击通知时的Intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$appName 使用提醒")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // 持久通知
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setSound(null)
            .setDefaults(0)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    /**
     * 更新通知内容 - 完全参考 FocusModeNotificationManager
     */
    private fun updateNotification(
        packageName: String,
        appName: String,
        remainingMillis: Long,
        remainingCount: Int?,
        notificationId: Int
    ) {
        Log.d(TAG, "updateNotification called for $packageName, remaining: ${remainingMillis}ms")

        // 计算时间 - 和 FocusModeNotificationManager 一样的格式
        val totalSeconds = (remainingMillis / 1000).coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val timeText = when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }

        Log.d(TAG, "Time text: $timeText")

        // 构建通知内容
        val contentText = buildString {
            append("⏱️ 剩余: $timeText")
            remainingCount?.let { count ->
                append("\n🔢 剩余次数: ${count}次")
            }
        }

        // 创建点击通知时的Intent - 和 FocusModeNotificationManager 一样
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 构建通知 - 完全按照 FocusModeNotificationManager 的方式
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$appName 使用提醒")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // 持久通知，不可滑动删除
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true) // 只在首次显示时提醒
            .setSound(null) // 不播放声音
            .setDefaults(0) // 不使用默认设置
            .build()

        Log.d(TAG, "Showing notification with ID: $notificationId")
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notification shown successfully")
    }

    /**
     * 暂停通知更新（但保持通知可见）
     * 当应用切换到后台时调用
     */
    fun pauseNotification(packageName: String) {
        Log.d(TAG, "Pausing notification updates for $packageName")
        // 取消更新任务，但不取消通知本身
        updateJobs[packageName]?.cancel()
        updateJobs.remove(packageName)
        Log.d(TAG, "Notification updates paused for $packageName")
    }

    /**
     * 取消通知
     */
    fun cancelNotification(packageName: String) {
        // 取消更新任务
        updateJobs[packageName]?.cancel()
        updateJobs.remove(packageName)

        // 移除倒计时信息
        countdownInfos.remove(packageName)

        // 取消通知
        notificationIds[packageName]?.let { notificationId ->
            notificationManager.cancel(notificationId)
            notificationIds.remove(packageName)
            Log.d(TAG, "Notification cancelled for $packageName")
        }
    }

    /**
     * 取消所有通知
     */
    fun cancelAllNotifications() {
        // 取消所有更新任务
        updateJobs.values.forEach { it.cancel() }
        updateJobs.clear()

        // 清除倒计时信息
        countdownInfos.clear()

        // 取消所有通知
        notificationIds.forEach { (_, notificationId) ->
            notificationManager.cancel(notificationId)
        }
        notificationIds.clear()

        Log.d(TAG, "All notifications cancelled")
    }

    /**
     * 构建通知内容文本（旧版本）
     */
    private fun buildContentText(remainingMillis: Long?, remainingCount: Int?): String {
        return buildString {
            remainingMillis?.let { millis ->
                val seconds = millis / 1000
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60

                val timeText = when {
                    hours > 0 -> "${hours}小时${minutes}分钟${secs}秒"
                    minutes > 0 -> "${minutes}分钟${secs}秒"
                    else -> "${secs}秒"
                }
                append("⏱️ 剩余: $timeText")
            }

            remainingCount?.let { count ->
                if (isNotEmpty()) append("\n")
                append("🔢 剩余次数: ${count}次")
            }
        }
    }

    /**
     * 检查通知权限
     */
    private fun areNotificationsEnabled(): Boolean {
        val areEnabled = notificationManager.areNotificationsEnabled()
        Log.d(TAG, "Notifications enabled: $areEnabled")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (channel != null) {
                val importance = channel.importance
                Log.d(TAG, "Channel importance: $importance")
                if (importance == NotificationManager.IMPORTANCE_NONE) {
                    Log.e(TAG, "Notification channel is disabled")
                    return false
                }
            }
        }

        return areEnabled
    }
}
