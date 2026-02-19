package com.example.umind.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Locale

/**
 * MIUI设备助手
 * 检测MIUI设备并提供特定的设置引导
 */
object MiuiDeviceHelper {

    private const val TAG = "MiuiDeviceHelper"

    /**
     * 检测是否是MIUI设备
     */
    fun isMiuiDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        val isMiui = manufacturer == "xiaomi" || manufacturer == "redmi" ||
                     hasSystemProperty("ro.miui.ui.version.name")
        Log.d(TAG, "Device check: manufacturer=$manufacturer, isMiui=$isMiui")
        return isMiui
    }

    /**
     * 获取MIUI版本
     */
    fun getMiuiVersion(): String? {
        return getSystemProperty("ro.miui.ui.version.name")
    }

    /**
     * 检查系统属性
     */
    private fun hasSystemProperty(key: String): Boolean {
        return getSystemProperty(key) != null
    }

    /**
     * 获取系统属性值
     */
    private fun getSystemProperty(key: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $key")
            process.inputStream.bufferedReader().use { it.readLine() }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting system property: $key", e)
            null
        }
    }

    /**
     * 打开MIUI自启动管理页面
     */
    fun openMiuiAutoStartSettings(context: Context): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "Opened MIUI autostart settings")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening MIUI autostart settings", e)
            false
        }
    }

    /**
     * 打开MIUI省电策略设置
     */
    fun openMiuiBatterySaverSettings(context: Context): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.miui.powerkeeper",
                    "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
                )
                putExtra("package_name", context.packageName)
                putExtra("package_label", context.applicationInfo.loadLabel(context.packageManager))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "Opened MIUI battery saver settings")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening MIUI battery saver settings", e)
            // 尝试备用方案
            openMiuiBatterySaverSettingsAlternative(context)
        }
    }

    /**
     * 打开MIUI省电策略设置（备用方案）
     */
    private fun openMiuiBatterySaverSettingsAlternative(context: Context): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.miui.powerkeeper",
                    "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "Opened MIUI battery saver settings (alternative)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening MIUI battery saver settings (alternative)", e)
            false
        }
    }

    /**
     * 打开MIUI后台弹出界面权限设置
     */
    fun openMiuiBackgroundPopupSettings(context: Context): Boolean {
        return try {
            val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"
                )
                putExtra("extra_pkgname", context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "Opened MIUI background popup settings")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening MIUI background popup settings", e)
            false
        }
    }

    /**
     * 打开应用详情页面（通用方案）
     */
    fun openAppDetailsSettings(context: Context): Boolean {
        return try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "Opened app details settings")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app details settings", e)
            false
        }
    }

    /**
     * 获取MIUI设备的设置指南
     */
    fun getMiuiSetupGuide(): List<SetupStep> {
        return listOf(
            SetupStep(
                title = "关闭电池优化",
                description = "防止系统杀死后台服务",
                action = "battery_optimization"
            ),
            SetupStep(
                title = "允许自启动",
                description = "允许应用在后台自动启动",
                action = "autostart"
            ),
            SetupStep(
                title = "设置省电策略为无限制",
                description = "确保应用可以持续运行",
                action = "battery_saver"
            ),
            SetupStep(
                title = "允许后台弹出界面",
                description = "允许应用显示阻止弹窗",
                action = "background_popup"
            ),
            SetupStep(
                title = "锁定最近任务",
                description = "防止应用被清理",
                action = "lock_recent"
            )
        )
    }

    data class SetupStep(
        val title: String,
        val description: String,
        val action: String
    )
}
