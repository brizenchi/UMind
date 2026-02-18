package com.example.umind.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.umind.BlockAccessibilityService
import com.example.umind.MainActivity
import com.example.umind.R
import com.example.umind.util.AccessibilityUtil

/**
 * 开机广播接收器
 * 在系统启动后检查无障碍服务状态，如果未启用则发送通知提醒用户
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 检查无障碍服务是否已启用
            val isEnabled = AccessibilityUtil.isAccessibilityServiceEnabled(
                context,
                BlockAccessibilityService::class.java.name
            )

            if (!isEnabled) {
                // 发送通知提醒用户启用无障碍服务
                sendNotification(context)
            }
        }
    }

    private fun sendNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "accessibility_service_channel"

        // 创建通知渠道（Android 8.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "无障碍服务提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "提醒用户启用无障碍服务"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 创建打开无障碍设置的Intent
        val settingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            settingsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 创建打开应用的Intent
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            1,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 构建通知
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("专注模式需要启用")
            .setContentText("请启用无障碍服务以使用应用限制功能")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("系统重启后，无障碍服务已被关闭。请点击此通知前往设置页面重新启用，以继续使用应用限制功能。")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_preferences,
                "去设置",
                pendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_view,
                "打开应用",
                appPendingIntent
            )
            .build()

        notificationManager.notify(1001, notification)
    }
}
