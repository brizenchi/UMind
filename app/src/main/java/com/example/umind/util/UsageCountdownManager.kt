package com.example.umind.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 管理应用使用时长倒计时
 * 职责：
 * 1. 追踪每个应用的倒计时状态（运行中/暂停）
 * 2. 正确处理暂停/恢复逻辑
 * 3. 基于实际使用时长计算剩余时间
 * 4. 提供倒计时更新回调
 */
@Singleton
class UsageCountdownManager @Inject constructor() {
    private val handler = Handler(Looper.getMainLooper())
    private val countdownStates = mutableMapOf<String, CountdownState>()

    companion object {
        private const val TAG = "UsageCountdownManager"
        private const val UPDATE_INTERVAL_MS = 1000L // 每秒更新一次
        private const val MAX_ACTIVE_COUNTDOWNS = 5 // 最多同时运行的倒计时数量
    }

    /**
     * 倒计时状态
     */
    private data class CountdownState(
        val key: String,
        val limitMillis: Long,              // 总限制时间（毫秒）
        var usedMillis: Long,               // 已使用时间（毫秒）
        var sessionStartTime: Long = 0,     // 当前会话开始时间
        var isRunning: Boolean = false,     // 是否正在运行
        val createdAt: Long = System.currentTimeMillis(),
        var updateRunnable: Runnable? = null,
        var onUpdate: ((remainingMillis: Long) -> Unit)? = null,
        var onTimeUp: (() -> Unit)? = null
    )

    /**
     * 开始倒计时
     * @param key 倒计时唯一键（建议使用应用组作用域）
     * @param limitMillis 总限制时间（毫秒）
     * @param usedMillis 已使用时间（毫秒）
     * @param onUpdate 倒计时更新回调（剩余毫秒数）
     * @param onTimeUp 时间用完回调
     */
    fun startCountdown(
        key: String,
        limitMillis: Long,
        usedMillis: Long,
        onUpdate: (remainingMillis: Long) -> Unit,
        onTimeUp: () -> Unit
    ) {
        Log.d(TAG, "=== startCountdown for $key ===")
        Log.d(TAG, "limitMillis: $limitMillis, usedMillis: $usedMillis")

        // Prevent too many active countdowns to avoid performance issues
        if (countdownStates.size >= MAX_ACTIVE_COUNTDOWNS && !countdownStates.containsKey(key)) {
            Log.w(TAG, "Too many active countdowns (${countdownStates.size}), cleaning up oldest")
            // Remove the oldest non-running countdown
            val oldestPaused = countdownStates.entries
                .filter { !it.value.isRunning }
                .minByOrNull { it.value.createdAt }
            oldestPaused?.let {
                stopCountdown(it.key)
            }
        }

        // 停止之前的倒计时（如果有）
        stopCountdown(key)

        // 创建新的倒计时状态
        val state = CountdownState(
            key = key,
            limitMillis = limitMillis,
            usedMillis = usedMillis,
            sessionStartTime = System.currentTimeMillis(),
            isRunning = true,
            onUpdate = onUpdate,
            onTimeUp = onTimeUp
        )

        // 创建更新 Runnable
        lateinit var updateRunnable: Runnable
        updateRunnable = Runnable {
            if (!state.isRunning) {
                Log.d(TAG, "Countdown for $key is not running, stopping updates")
                return@Runnable
            }

            val remainingMillis = getRemainingTime(state)
            Log.d(TAG, "Countdown update for $key: remaining=${remainingMillis}ms")

            if (remainingMillis > 0) {
                // 还有剩余时间，通知更新
                state.onUpdate?.invoke(remainingMillis)
                // 继续下一次更新
                handler.postDelayed(updateRunnable, UPDATE_INTERVAL_MS)
            } else {
                // 时间用完
                Log.d(TAG, "!!! Time's up for $key !!!")
                state.isRunning = false
                state.onTimeUp?.invoke()

                // 移除状态
                countdownStates.remove(key)
            }
        }

        state.updateRunnable = updateRunnable
        countdownStates[key] = state

        // 立即执行第一次更新
        handler.post(updateRunnable)

        Log.d(TAG, "Countdown started for $key")
    }

    /**
     * 暂停倒计时（应用切换到后台时调用）
     */
    fun pauseCountdown(key: String) {
        val state = countdownStates[key] ?: return

        if (!state.isRunning) {
            Log.d(TAG, "Countdown for $key is already paused")
            return
        }

        Log.d(TAG, "=== pauseCountdown for $key ===")

        // 计算本次会话的使用时长
        val sessionDuration = System.currentTimeMillis() - state.sessionStartTime
        Log.d(TAG, "Session duration: ${sessionDuration}ms")

        // 更新已使用时间
        state.usedMillis += sessionDuration

        // 标记为暂停
        state.isRunning = false

        // 取消定时更新
        state.updateRunnable?.let { handler.removeCallbacks(it) }

        Log.d(TAG, "Countdown paused for $key, total used: ${state.usedMillis}ms")
    }

    /**
     * 恢复倒计时（应用重新回到前台时调用）
     */
    fun resumeCountdown(key: String) {
        val state = countdownStates[key] ?: return

        if (state.isRunning) {
            Log.d(TAG, "Countdown for $key is already running")
            return
        }

        Log.d(TAG, "=== resumeCountdown for $key ===")
        Log.d(TAG, "Previously used: ${state.usedMillis}ms")

        // 重新记录会话开始时间
        state.sessionStartTime = System.currentTimeMillis()
        state.isRunning = true

        // 重新启动定时更新
        state.updateRunnable?.let { handler.post(it) }

        Log.d(TAG, "Countdown resumed for $key")
    }

    /**
     * 停止倒计时并清理资源
     */
    fun stopCountdown(key: String) {
        val state = countdownStates[key] ?: return

        Log.d(TAG, "=== stopCountdown for $key ===")

        // 取消定时更新
        state.updateRunnable?.let { handler.removeCallbacks(it) }

        // 移除状态
        countdownStates.remove(key)

        Log.d(TAG, "Countdown stopped for $key")
    }

    /**
     * 获取剩余时间（毫秒）
     */
    private fun getRemainingTime(state: CountdownState): Long {
        val currentSessionUsed = if (state.isRunning) {
            System.currentTimeMillis() - state.sessionStartTime
        } else {
            0
        }
        val totalUsed = state.usedMillis + currentSessionUsed
        return (state.limitMillis - totalUsed).coerceAtLeast(0)
    }

    /**
     * 获取当前剩余时间（毫秒）- 公开方法
     */
    fun getRemainingTime(key: String): Long? {
        val state = countdownStates[key] ?: return null
        return getRemainingTime(state)
    }

    /**
     * 获取当前剩余时间（毫秒）- 别名方法
     */
    fun getRemainingMillis(key: String): Long {
        return getRemainingTime(key) ?: 0
    }

    /**
     * 检查倒计时是否正在运行
     */
    fun isRunning(key: String): Boolean {
        return countdownStates[key]?.isRunning ?: false
    }

    /**
     * 检查是否存在倒计时状态（包括暂停的）
     */
    fun hasCountdown(key: String): Boolean {
        return countdownStates.containsKey(key)
    }

    /**
     * 清理所有倒计时
     */
    fun cleanup() {
        Log.d(TAG, "=== cleanup all countdowns ===")
        countdownStates.keys.toList().forEach { key ->
            stopCountdown(key)
        }
    }
}
