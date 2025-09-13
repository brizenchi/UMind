package com.example.focus

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.graphics.PixelFormat
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.content.Context
import android.util.Log
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat

class BlockAccessibilityService : AccessibilityService() {
    
    private var windowManager: WindowManager? = null
    private var blockView: View? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 50
        }
        
        Log.d("BlockAccessibilityService", "Service connected and configured")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 添加更详细的日志
        Log.d("BlockAccessibilityService", "Received event: ${event?.eventType}, package: ${event?.packageName}")
        
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Log.d("BlockAccessibilityService", "Ignoring event type: ${event?.eventType}")
            return
        }
        
        val packageName = event.packageName?.toString() ?: run {
            Log.d("BlockAccessibilityService", "No package name in event")
            return
        }
        
        // 忽略系统界面和自己的应用
        if (packageName == "com.android.systemui" || 
            packageName == "android" || 
            packageName == this.packageName) {
            Log.d("BlockAccessibilityService", "Ignoring system/self package: $packageName")
            return
        }

        Log.d("BlockAccessibilityService", "Window changed to: $packageName")
        
        // 检查是否应该被阻止
        val shouldBlock = FocusRepository.isAppBlockedNow(applicationContext, packageName)
        Log.d("BlockAccessibilityService", "Should block $packageName: $shouldBlock")
        
        if (shouldBlock) {
            Log.d("BlockAccessibilityService", "Blocking app: $packageName")
            showBlockDialog(packageName)
            // 延迟一点再跳转到桌面，让弹窗先显示
            handler.postDelayed({
                performGlobalAction(GLOBAL_ACTION_HOME)
            }, 100)
        }
    }
    
    private fun showBlockDialog(packageName: String) {
        try {
            // 移除之前的弹窗
            dismissBlockDialog()
            
            // 获取应用名称
            val pm = packageManager
            val appName = try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                packageName
            }
            
            // 创建弹窗布局
            val blockLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(60, 60, 60, 60)
                setBackgroundColor(ContextCompat.getColor(this@BlockAccessibilityService, android.R.color.white))
            }
            
            // 标题
            val titleText = TextView(this).apply {
                text = "专注模式已启用"
                textSize = 20f
                setTextColor(ContextCompat.getColor(this@BlockAccessibilityService, android.R.color.black))
                setPadding(0, 0, 0, 30)
            }
            blockLayout.addView(titleText)
            
            // 内容
            val contentText = TextView(this).apply {
                text = "应用「$appName」已被阻止\n\n现在是专注时间，请保持专注！"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@BlockAccessibilityService, android.R.color.black))
                setPadding(0, 0, 0, 30)
            }
            blockLayout.addView(contentText)
            
            // 确定按钮
            val okButton = Button(this).apply {
                text = "我知道了"
                setOnClickListener {
                    dismissBlockDialog()
                }
            }
            blockLayout.addView(okButton)
            
            // 设置窗口参数
            val layoutParams = WindowManager.LayoutParams().apply {
                type = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                }
                format = PixelFormat.TRANSLUCENT
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            
            // 显示弹窗
            windowManager?.addView(blockLayout, layoutParams)
            blockView = blockLayout
            
            // 3秒后自动关闭弹窗
            handler.postDelayed({
                dismissBlockDialog()
            }, 3000)
            
        } catch (e: Exception) {
            Log.e("BlockAccessibilityService", "Error showing block dialog", e)
        }
    }
    
    private fun dismissBlockDialog() {
        try {
            blockView?.let { view ->
                windowManager?.removeView(view)
                blockView = null
            }
        } catch (e: Exception) {
            Log.e("BlockAccessibilityService", "Error dismissing dialog", e)
        }
    }

    override fun onInterrupt() {
        dismissBlockDialog()
    }
    
    override fun onDestroy() {
        dismissBlockDialog()
        super.onDestroy()
    }

    companion object {
        fun openAccessibilitySettingsIntent(): Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }
}


