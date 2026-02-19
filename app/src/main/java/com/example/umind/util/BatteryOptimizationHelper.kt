package com.example.umind.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher

/**
 * 电池优化豁免助手
 * 用于请求系统忽略应用的电池优化，防止服务被杀死
 */
object BatteryOptimizationHelper {

    private const val TAG = "BatteryOptimizationHelper"

    /**
     * 检查应用是否已被豁免电池优化
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val packageName = context.packageName
            val isIgnoring = powerManager?.isIgnoringBatteryOptimizations(packageName) ?: false
            Log.d(TAG, "Battery optimization status: isIgnoring=$isIgnoring")
            return isIgnoring
        }
        return true // Android M 以下版本不需要此权限
    }

    /**
     * 请求电池优化豁免
     * 会打开系统设置页面让用户手动授权
     */
    fun requestIgnoreBatteryOptimizations(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
                Log.d(TAG, "Launched battery optimization request")
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting battery optimization exemption", e)
                // 如果直接请求失败，打开电池优化设置列表
                openBatteryOptimizationSettings(context)
            }
        }
    }

    /**
     * 打开电池优化设置页面
     * 作为备用方案，让用户手动在列表中找到应用并设置
     */
    fun openBatteryOptimizationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
            Log.d(TAG, "Opened battery optimization settings")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening battery optimization settings", e)
        }
    }

    /**
     * 获取电池优化状态的描述文本
     */
    fun getBatteryOptimizationStatusText(context: Context): String {
        return if (isIgnoringBatteryOptimizations(context)) {
            "✓ 已关闭电池优化"
        } else {
            "✗ 需要关闭电池优化"
        }
    }
}
