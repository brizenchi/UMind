# 通知权限修复

## 问题分析

用户报告无法看到任何通知，包括倒计时通知。通过分析日志发现：

1. **代码执行正常**：日志显示 `Notification.notify()` 被正确调用
2. **Toast被系统抑制**：日志显示 "Suppressing toast from package com.example.umind by user request"
3. **根本原因**：虽然在 AndroidManifest.xml 中声明了 `POST_NOTIFICATIONS` 权限，但在 Android 13+ (API 33+) 上，这个权限必须在运行时请求

## 修复内容

### 1. MainActivity.kt - 添加运行时权限请求

**修改位置**: `app/src/main/java/com/example/focus/MainActivity.kt`

**添加的功能**:
- 导入必要的权限相关类
- 添加 `notificationPermissionLauncher` 用于请求权限
- 在 `onCreate()` 中调用 `requestNotificationPermission()`
- 实现 `requestNotificationPermission()` 方法，在 Android 13+ 上请求通知权限

**关键代码**:
```kotlin
private val notificationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        Log.d("MainActivity", "Notification permission granted")
    } else {
        Log.d("MainActivity", "Notification permission denied")
    }
}

private fun requestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("MainActivity", "Notification permission already granted")
            }
            else -> {
                Log.d("MainActivity", "Requesting notification permission")
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
```

### 2. BlockAccessibilityService.kt - 添加权限检查

**修改位置**: `app/src/main/java/com/example/focus/BlockAccessibilityService.kt`

**添加的功能**:
- 新增 `areNotificationsEnabled()` 方法，检查通知是否被禁用
- 在 `showTestNotification()` 中添加权限检查
- 在 `showUsageNotification()` 中添加权限检查
- 移除被系统抑制的 Toast 调试消息

**关键代码**:
```kotlin
private fun areNotificationsEnabled(): Boolean {
    val notificationManager = notificationManager ?: return false

    // 检查通知是否被全局禁用
    val areEnabled = notificationManager.areNotificationsEnabled()
    Log.d("BlockAccessibilityService", "Notifications enabled: $areEnabled")

    // 检查通知渠道是否被禁用 (Android O+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
        if (channel != null) {
            val importance = channel.importance
            Log.d("BlockAccessibilityService", "Channel importance: $importance")
            if (importance == NotificationManager.IMPORTANCE_NONE) {
                Log.e("BlockAccessibilityService", "!!! Notification channel is disabled !!!")
                return false
            }
        }
    }

    return areEnabled
}
```

## 测试步骤

### 1. 首次安装测试

1. **卸载旧版本**（如果已安装）
2. **安装新版本**
3. **打开应用**
   - 应该会弹出通知权限请求对话框
   - 点击"允许"授予通知权限
4. **启用无障碍服务**
   - 进入设置 → 无障碍 → UMind
   - 启用服务
   - 应该会看到测试通知："UMind 服务已启动"
5. **配置限制规则**
   - 添加一个应用（如 Chrome）
   - 设置时长限制（如 15 分钟）
6. **打开受限应用**
   - 应该会看到倒计时通知
   - 通知应该每秒更新剩余时间

### 2. 权限被拒绝的情况

如果用户拒绝了通知权限：

1. **查看日志**：
   ```bash
   adb logcat | grep "BlockAccessibilityService\|MainActivity"
   ```

2. **应该看到**：
   ```
   MainActivity: Notification permission denied
   BlockAccessibilityService: !!! NOTIFICATIONS ARE DISABLED !!!
   ```

3. **手动授予权限**：
   - 进入 设置 → 应用 → UMind → 通知
   - 启用"允许通知"
   - 重启无障碍服务

### 3. 通知渠道被禁用的情况

如果通知渠道被用户禁用：

1. **查看日志**：
   ```
   BlockAccessibilityService: Channel importance: 0 (NONE=0, MIN=1, LOW=2, DEFAULT=3, HIGH=4, MAX=5)
   BlockAccessibilityService: !!! Notification channel is disabled !!!
   ```

2. **手动启用渠道**：
   - 进入 设置 → 应用 → UMind → 通知
   - 找到"应用使用提醒"渠道
   - 确保已启用且重要性设置为"高"

## 日志关键字

使用以下命令过滤相关日志：

```bash
# 查看通知相关日志
adb logcat | grep "BlockAccessibilityService\|MainActivity\|NotificationManager"

# 查看权限请求日志
adb logcat | grep "permission"

# 查看通知渠道日志
adb logcat | grep "Channel importance"
```

## 预期结果

修复后，用户应该能够：

1. ✅ 首次打开应用时看到通知权限请求
2. ✅ 授予权限后，启用无障碍服务时看到测试通知
3. ✅ 打开受限应用时看到倒计时通知
4. ✅ 倒计时通知每秒更新
5. ✅ 时间到达 0 时，应用自动退出并显示"时间已用完"对话框

## 注意事项

1. **Android 版本**：此修复主要针对 Android 13+ (API 33+)，低版本 Android 不需要运行时权限
2. **MIUI/ColorOS 等定制系统**：某些定制系统可能有额外的通知限制，需要在系统设置中手动启用
3. **省电模式**：某些省电模式可能会限制后台通知，需要将应用加入白名单
