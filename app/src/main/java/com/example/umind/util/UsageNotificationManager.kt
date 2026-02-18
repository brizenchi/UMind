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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 管理使用情况通知
 * 职责：
 * 1. 创建和更新通知
 * 2. 管理通知ID
 * 3. 格式化显示内容
 */
@Singleton
class UsageNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val activeNotifications = mutableMapOf<String, Int>() // packageName to notificationId
    private var nextNotificationId = 1000

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
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "显示应用使用时间和次数限制"
                setShowBadge(true)
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    /**
     * 显示或更新使用情况通知
     * @param packageName 应用包名
     * @param appName 应用名称
     * @param remainingMillis 剩余时间（毫秒）
     * @param remainingCount 剩余次数
     */
    fun showOrUpdateNotification(
        packageName: String,
        appName: String,
        remainingMillis: Long? = null,
        remainingCount: Int? = null
    ) {
        Log.d(TAG, "=== showOrUpdateNotification for $packageName ===")
        Log.d(TAG, "remainingMillis: $remainingMillis, remainingCount: $remainingCount")

        // 检查通知权限
        if (!areNotificationsEnabled()) {
            Log.e(TAG, "Notifications are disabled")
            return
        }

        // 获取或创建通知ID
        val notificationId = activeNotifications.getOrPut(packageName) {
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
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // 持久通知
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true) // 只在首次显示时提醒，更新时不提醒
            .setSound(null) // 不播放声音
            .setDefaults(0) // 不使用默认设置（声音、震动等）
            .build()

        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notification shown/updated with ID: $notificationId")
    }

    /**
     * 取消通知
     */
    fun cancelNotification(packageName: String) {
        activeNotifications[packageName]?.let { notificationId ->
            notificationManager.cancel(notificationId)
            activeNotifications.remove(packageName)
            Log.d(TAG, "Notification cancelled for $packageName")
        }
    }

    /**
     * 取消所有通知
     */
    fun cancelAllNotifications() {
        activeNotifications.keys.toList().forEach { packageName ->
            cancelNotification(packageName)
        }
        Log.d(TAG, "All notifications cancelled")
    }

    /**
     * 构建通知内容文本
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
                append("⏱️ 剩余时间: $timeText")
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
