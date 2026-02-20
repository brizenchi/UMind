package com.example.umind.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.umind.data.repository.UsageTrackingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
class UsageCountdownManager @Inject constructor(
    private val usageTrackingRepository: UsageTrackingRepository
) {
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
        val packageName: String,
        val limitMillis: Long,              // 总限制时间（毫秒）
        var usedMillis: Long,               // 已使用时间（毫秒）- 从数据库获取
        var sessionStartTime: Long = 0,     // 当前会话开始时间
        var isRunning: Boolean = false,     // 是否正在运行
        var updateRunnable: Runnable? = null,
        var onUpdate: ((remainingMillis: Long) -> Unit)? = null,
        var onTimeUp: (() -> Unit)? = null
    )

    /**
     * 开始倒计时
     * @param packageName 应用包名
     * @param limitMillis 总限制时间（毫秒）
     * @param usedMillis 已使用时间（毫秒）
     * @param scope 协程作用域，用于异步保存使用时长
     * @param onUpdate 倒计时更新回调（剩余毫秒数）
     * @param onTimeUp 时间用完回调
     */
    fun startCountdown(
        packageName: String,
        limitMillis: Long,
        usedMillis: Long,
        scope: CoroutineScope,
        onUpdate: (remainingMillis: Long) -> Unit,
        onTimeUp: () -> Unit
    ) {
        Log.d(TAG, "=== startCountdown for $packageName ===")
        Log.d(TAG, "limitMillis: $limitMillis, usedMillis: $usedMillis")

        // Prevent too many active countdowns to avoid performance issues
        if (countdownStates.size >= MAX_ACTIVE_COUNTDOWNS && !countdownStates.containsKey(packageName)) {
            Log.w(TAG, "Too many active countdowns (${countdownStates.size}), cleaning up oldest")
            // Remove the oldest non-running countdown
            val oldestPaused = countdownStates.entries
                .filter { !it.value.isRunning }
                .minByOrNull { it.value.sessionStartTime }
            oldestPaused?.let {
                stopCountdown(it.key, scope)
            }
        }

        // 停止之前的倒计时（如果有）
        stopCountdown(packageName, scope)

        // 创建新的倒计时状态
        val state = CountdownState(
            packageName = packageName,
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
                Log.d(TAG, "Countdown for $packageName is not running, stopping updates")
                return@Runnable
            }

            val remainingMillis = getRemainingTime(state)
            Log.d(TAG, "Countdown update for $packageName: remaining=${remainingMillis}ms")

            if (remainingMillis > 0) {
                // 还有剩余时间，通知更新
                state.onUpdate?.invoke(remainingMillis)
                // 继续下一次更新
                handler.postDelayed(updateRunnable, UPDATE_INTERVAL_MS)
            } else {
                // 时间用完
                Log.d(TAG, "!!! Time's up for $packageName !!!")
                state.isRunning = false
                state.onTimeUp?.invoke()

                // 保存最终使用时长
                scope.launch {
                    val finalUsedMillis = state.usedMillis +
                        (System.currentTimeMillis() - state.sessionStartTime)
                    val sessionDuration = System.currentTimeMillis() - state.sessionStartTime
                    if (sessionDuration > 0) {
                        usageTrackingRepository.recordUsage(packageName, sessionDuration)
                        Log.d(TAG, "Recorded final usage for $packageName: ${sessionDuration}ms")
                    }
                }

                // 移除状态
                countdownStates.remove(packageName)
            }
        }

        state.updateRunnable = updateRunnable
        countdownStates[packageName] = state

        // 立即执行第一次更新
        handler.post(updateRunnable)

        Log.d(TAG, "Countdown started for $packageName")
    }

    /**
     * 暂停倒计时（应用切换到后台时调用）
     * 会保存当前会话的使用时长到数据库
     */
    fun pauseCountdown(packageName: String, scope: CoroutineScope) {
        val state = countdownStates[packageName] ?: return

        if (!state.isRunning) {
            Log.d(TAG, "Countdown for $packageName is already paused")
            return
        }

        Log.d(TAG, "=== pauseCountdown for $packageName ===")

        // 计算本次会话的使用时长
        val sessionDuration = System.currentTimeMillis() - state.sessionStartTime
        Log.d(TAG, "Session duration: ${sessionDuration}ms")

        // 更新已使用时间
        state.usedMillis += sessionDuration

        // 保存到数据库
        if (sessionDuration > 0) {
            scope.launch {
                usageTrackingRepository.recordUsage(packageName, sessionDuration)
                Log.d(TAG, "Recorded usage for $packageName: ${sessionDuration}ms, total used: ${state.usedMillis}ms")
            }
        }

        // 标记为暂停
        state.isRunning = false

        // 取消定时更新
        state.updateRunnable?.let { handler.removeCallbacks(it) }

        Log.d(TAG, "Countdown paused for $packageName, total used: ${state.usedMillis}ms")
    }

    /**
     * 恢复倒计时（应用重新回到前台时调用）
     */
    fun resumeCountdown(packageName: String) {
        val state = countdownStates[packageName] ?: return

        if (state.isRunning) {
            Log.d(TAG, "Countdown for $packageName is already running")
            return
        }

        Log.d(TAG, "=== resumeCountdown for $packageName ===")
        Log.d(TAG, "Previously used: ${state.usedMillis}ms")

        // 重新记录会话开始时间
        state.sessionStartTime = System.currentTimeMillis()
        state.isRunning = true

        // 重新启动定时更新
        state.updateRunnable?.let { handler.post(it) }

        Log.d(TAG, "Countdown resumed for $packageName")
    }

    /**
     * 停止倒计时并清理资源
     * 会保存当前会话的使用时长到数据库
     */
    fun stopCountdown(packageName: String, scope: CoroutineScope) {
        val state = countdownStates[packageName] ?: return

        Log.d(TAG, "=== stopCountdown for $packageName ===")

        // 如果正在运行，先保存使用时长
        if (state.isRunning) {
            val sessionDuration = System.currentTimeMillis() - state.sessionStartTime
            if (sessionDuration > 0) {
                scope.launch {
                    usageTrackingRepository.recordUsage(packageName, sessionDuration)
                    Log.d(TAG, "Recorded final usage for $packageName: ${sessionDuration}ms")
                }
            }
        }

        // 取消定时更新
        state.updateRunnable?.let { handler.removeCallbacks(it) }

        // 移除状态
        countdownStates.remove(packageName)

        Log.d(TAG, "Countdown stopped for $packageName")
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
    fun getRemainingTime(packageName: String): Long? {
        val state = countdownStates[packageName] ?: return null
        return getRemainingTime(state)
    }

    /**
     * 获取当前剩余时间（毫秒）- 别名方法
     */
    fun getRemainingMillis(packageName: String): Long {
        return getRemainingTime(packageName) ?: 0
    }

    /**
     * 检查倒计时是否正在运行
     */
    fun isRunning(packageName: String): Boolean {
        return countdownStates[packageName]?.isRunning ?: false
    }

    /**
     * 检查是否存在倒计时状态（包括暂停的）
     */
    fun hasCountdown(packageName: String): Boolean {
        return countdownStates.containsKey(packageName)
    }

    /**
     * 清理所有倒计时
     */
    fun cleanup(scope: CoroutineScope) {
        Log.d(TAG, "=== cleanup all countdowns ===")
        countdownStates.keys.toList().forEach { packageName ->
            stopCountdown(packageName, scope)
        }
    }
}
