package com.example.umind.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.umind.R
import com.example.umind.data.repository.FocusModeRepository
import com.example.umind.domain.model.FocusModeType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for Focus Mode countdown timer
 */
@Singleton
class FocusModeCountdownManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val focusModeRepository: FocusModeRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var countdownJob: Job? = null
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val TAG = "FocusModeCountdown"
        private const val CHANNEL_ID = "focus_mode_countdown"
        private const val CHANNEL_NAME = "专注模式倒计时"
        private const val NOTIFICATION_ID = 1001
        private const val CHECK_INTERVAL = 10000L // Check every 10 seconds
    }

    init {
        createNotificationChannel()
        startMonitoring()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "专注模式倒计时提醒"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Start monitoring countdown
     */
    private fun startMonitoring() {
        countdownJob?.cancel()
        countdownJob = scope.launch {
            while (true) {
                try {
                    checkCountdown()
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking countdown", e)
                }
                delay(CHECK_INTERVAL)
            }
        }
    }

    private suspend fun checkCountdown() {
        val focusMode = focusModeRepository.getFocusModeOnce()

        if (!focusMode.isEnabled) {
            return
        }

        when (focusMode.modeType) {
            FocusModeType.COUNTDOWN -> {
                val remaining = focusMode.getRemainingMinutes()
                if (remaining != null) {
                    if (remaining <= 0) {
                        // Countdown finished
                        Log.d(TAG, "Countdown finished, stopping focus mode")
                        focusModeRepository.stopFocusMode()
                        showCountdownFinishedNotification()
                    } else if (remaining <= 5) {
                        // Show reminder when less than 5 minutes remaining
                        Log.d(TAG, "Countdown reminder: $remaining minutes remaining")
                    }
                }
            }
            FocusModeType.SCHEDULED -> {
                // Check if should be active based on time ranges
                if (!focusMode.shouldBeActive()) {
                    Log.d(TAG, "Scheduled time ended, stopping focus mode")
                    focusModeRepository.stopFocusMode()
                }
            }
            FocusModeType.MANUAL -> {
                // Manual mode, no automatic stop
            }
        }
    }

    private fun showCountdownFinishedNotification() {
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("专注模式已结束")
                .setContentText("倒计时已完成，专注模式已自动关闭")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Countdown finished notification shown")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    fun stop() {
        countdownJob?.cancel()
        countdownJob = null
    }
}
