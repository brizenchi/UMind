package com.example.umind.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils

/**
 * 无障碍服务工具类
 */
object AccessibilityUtil {

    /**
     * 检查无障碍服务是否已启用
     * 支持检查旧包名（com.example.focus）以兼容包名更改前启用的服务
     */
    fun isAccessibilityServiceEnabled(context: Context, serviceName: String): Boolean {
        val currentPackageName = context.packageName
        val expectedComponentName = "$currentPackageName/$serviceName"

        // 兼容旧包名（如果用户在包名更改前启用了服务）
        val oldPackageName = "com.example.focus"
        val oldComponentName = "$oldPackageName/$serviceName"

        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            // 检查新包名或旧包名
            if (componentName.equals(expectedComponentName, ignoreCase = true) ||
                componentName.equals(oldComponentName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * 打开无障碍服务设置页面
     */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}
