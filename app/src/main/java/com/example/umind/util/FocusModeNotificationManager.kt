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
import com.example.umind.domain.model.FocusMode
import com.example.umind.domain.model.FocusModeType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 管理专注模式通知
 * 在通知栏显示实时的计时时间
 */
@Singleton
class FocusModeNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var updateJob: Job? = null

    companion object {
        private const val TAG = "FocusModeNotificationMgr"
        private const val CHANNEL_ID = "focus_mode_timer_channel"
        private const val CHANNEL_NAME = "专注模式计时"
        private const val NOTIFICATION_ID = 2000
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
                description = "显示专注模式的实时计时"
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
     * 开始显示专注模式通知并实时更新
     */
    fun startNotification(focusMode: FocusMode, scope: CoroutineScope) {
        Log.d(TAG, "Starting focus mode notification")

        // 取消之前的更新任务
        updateJob?.cancel()

        // 启动新的更新任务
        updateJob = scope.launch {
            while (isActive && focusMode.shouldBeActive()) {
                updateNotification(focusMode)
                delay(1000) // 每秒更新一次
            }
        }
    }

    /**
     * 更新通知内容
     */
    private fun updateNotification(focusMode: FocusMode) {
        val currentTime = System.currentTimeMillis()

        // 计算时间
        val (hours, minutes, seconds, timeText) = when (focusMode.modeType) {
            FocusModeType.MANUAL -> {
                // 正计时
                val elapsedMillis = currentTime - focusMode.updatedAt
                val h = (elapsedMillis / 3600000).toInt()
                val m = ((elapsedMillis % 3600000) / 60000).toInt()
                val s = ((elapsedMillis % 60000) / 1000).toInt()
                val text = if (h > 0) {
                    String.format("%02d:%02d:%02d", h, m, s)
                } else {
                    String.format("%02d:%02d", m, s)
                }
                Tuple4(h, m, s, text)
            }
            FocusModeType.COUNTDOWN -> {
                // 倒计时
                focusMode.countdownEndTime?.let { endTime ->
                    val remainingMillis = (endTime - currentTime).coerceAtLeast(0)
                    val h = (remainingMillis / 3600000).toInt()
                    val m = ((remainingMillis % 3600000) / 60000).toInt()
                    val s = ((remainingMillis % 60000) / 1000).toInt()
                    val text = if (h > 0) {
                        String.format("%02d:%02d:%02d", h, m, s)
                    } else {
                        String.format("%02d:%02d", m, s)
                    }
                    Tuple4(h, m, s, text)
                } ?: Tuple4(0, 0, 0, "00:00")
            }
            else -> Tuple4(0, 0, 0, "00:00")
        }

        // 创建点击通知时的Intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 构建通知
        val title = when (focusMode.modeType) {
            FocusModeType.MANUAL -> "专注中"
            FocusModeType.COUNTDOWN -> "倒计时"
            else -> "专注模式"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // 使用闹钟图标
            .setContentTitle(title)
            .setContentText(timeText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(timeText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // 持久通知，不可滑动删除
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true) // 只在首次显示时提醒
            .setSound(null) // 不播放声音
            .setDefaults(0) // 不使用默认设置
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 停止通知并取消更新任务
     */
    fun stopNotification() {
        Log.d(TAG, "Stopping focus mode notification")
        updateJob?.cancel()
        updateJob = null
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * 检查通知权限
     */
    private fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
