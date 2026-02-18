# 倒计时逻辑重构指南

## 重构概述

已创建两个新的管理器类来分离职责：

### 1. UsageCountdownManager
**位置**: `app/src/main/java/com/example/focus/util/UsageCountdownManager.kt`

**职责**:
- 管理每个应用的倒计时状态（运行中/暂停）
- 正确处理暂停/恢复逻辑
- 基于实际使用时长计算剩余时间
- 自动保存使用时长到数据库

**核心方法**:
```kotlin
// 开始倒计时
fun startCountdown(
    packageName: String,
    limitMillis: Long,        // 总限制时间（毫秒）
    usedMillis: Long,         // 已使用时间（毫秒）
    scope: CoroutineScope,
    onUpdate: (remainingMillis: Long) -> Unit,  // 每秒回调
    onTimeUp: () -> Unit      // 时间用完回调
)

// 暂停倒计时（应用切换到后台）
fun pauseCountdown(packageName: String, scope: CoroutineScope)

// 恢复倒计时（应用回到前台）
fun resumeCountdown(packageName: String)

// 停止倒计时并清理
fun stopCountdown(packageName: String, scope: CoroutineScope)
```

**关键特性**:
- 暂停时自动保存已使用时长到数据库
- 恢复时从暂停点继续，不会重置时间
- 时间用完时自动触发回调

### 2. UsageNotificationManager
**位置**: `app/src/main/java/com/example/focus/util/UsageNotificationManager.kt`

**职责**:
- 创建和更新通知
- 管理通知ID
- 格式化显示内容（时间和次数）

**核心方法**:
```kotlin
// 显示或更新通知
fun showOrUpdateNotification(
    packageName: String,
    appName: String,
    remainingMillis: Long? = null,  // 剩余时间（毫秒）
    remainingCount: Int? = null     // 剩余次数
)

// 取消通知
fun cancelNotification(packageName: String)

// 取消所有通知
fun cancelAllNotifications()
```

## BlockAccessibilityService 重构要点

### 需要修改的部分

#### 1. 注入新的管理器
```kotlin
@Inject
lateinit var countdownManager: UsageCountdownManager

@Inject
lateinit var notificationManager: UsageNotificationManager
```

#### 2. 删除旧的代码
删除以下内容：
- `countdownRunnable` 变量
- `activeNotifications` 和 `nextNotificationId` 变量
- `startNotificationCountdown()` 方法
- `showUsageNotification()` 方法
- `cancelUsageNotification()` 方法
- `createNotificationChannel()` 方法（现在由 UsageNotificationManager 处理）

#### 3. 重写 trackAppSwitch 方法

**新的逻辑**:
```kotlin
private suspend fun trackAppSwitch(
    newPackageName: String,
    blockInfo: BlockInfo
) {
    // 1. 暂停上一个应用的倒计时
    currentForegroundPackage?.let { previousPackage ->
        if (previousPackage != newPackageName) {
            countdownManager.pauseCountdown(previousPackage, serviceScope)
        }
    }

    // 2. 记录新应用的打开事件
    if (newPackageName != currentForegroundPackage) {
        usageTrackingRepository.recordAppOpen(newPackageName)
    }

    // 3. 更新当前应用状态
    currentForegroundPackage = newPackageName
    currentAppStartTime = System.currentTimeMillis()

    // 4. 启动新应用的倒计时和通知
    blockInfo.usageInfo?.let { usageInfo ->
        val appName = getAppName(newPackageName)

        usageInfo.usageLimitMinutes?.let { limitMinutes ->
            val limitMillis = limitMinutes * 60 * 1000
            val usedMillis = usageInfo.usedMinutes * 60 * 1000

            countdownManager.startCountdown(
                packageName = newPackageName,
                limitMillis = limitMillis,
                usedMillis = usedMillis,
                scope = serviceScope,
                onUpdate = { remainingMillis ->
                    // 更新通知
                    notificationManager.showOrUpdateNotification(
                        packageName = newPackageName,
                        appName = appName,
                        remainingMillis = remainingMillis,
                        remainingCount = usageInfo.remainingCount
                    )
                },
                onTimeUp = {
                    // 时间用完，强制退出
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    handler.post {
                        showTimeUpDialog(newPackageName, appName)
                    }
                    notificationManager.cancelNotification(newPackageName)
                }
            )
        }
    }
}
```

#### 4. 修改 onAccessibilityEvent
```kotlin
// 在 else 分支中调用新的 trackAppSwitch
else {
    trackAppSwitch(packageName, blockInfo)
}
```

#### 5. 修改 onDestroy
```kotlin
override fun onDestroy() {
    // 清理所有倒计时
    countdownManager.cleanup(serviceScope)
    // 取消所有通知
    notificationManager.cancelAllNotifications()
    dismissBlockDialog()
    super.onDestroy()
}
```

## 倒计时逻辑说明

### 正确的工作流程

1. **应用打开时**:
   - 调用 `startCountdown()` 开始倒计时
   - 每秒触发 `onUpdate` 回调，更新通知显示
   - 倒计时基于：剩余时间 = 总限制 - (已使用 + 本次会话已用)

2. **应用切换到后台时**:
   - 调用 `pauseCountdown()` 暂停倒计时
   - 自动计算本次会话使用时长并保存到数据库
   - 更新内部的 `usedMillis` 状态

3. **应用重新回到前台时**:
   - 调用 `startCountdown()` 重新开始（会先停止旧的）
   - 使用更新后的 `usedMillis`（包含之前暂停时保存的时长）
   - 倒计时从正确的剩余时间继续

4. **时间用完时**:
   - 触发 `onTimeUp` 回调
   - 自动保存最终使用时长
   - 清理倒计时状态

### 关键改进

**之前的问题**:
- 倒计时使用固定的开始时间，暂停后无法正确恢复
- 倒计时与实际使用时长追踪脱节
- 代码职责混乱，Service 承担太多责任

**现在的解决方案**:
- 倒计时状态独立管理，暂停时保存进度
- 倒计时直接基于数据库中的实际使用时长
- 清晰的职责分离：Service 只负责监听事件，Manager 负责具体逻辑

## 测试建议

1. **测试暂停/恢复**:
   - 打开受限应用，观察倒计时
   - 切换到其他应用，倒计时应暂停
   - 再次打开受限应用，倒计时应从暂停点继续

2. **测试时间用完**:
   - 设置一个很短的时间限制（如 10 秒）
   - 打开应用并等待倒计时结束
   - 应用应被强制退出并显示对话框

3. **测试多应用切换**:
   - 在多个受限应用之间切换
   - 每个应用的倒计时应独立管理
   - 切换时应正确暂停/恢复各自的倒计时

## 下一步

需要手动修改 `BlockAccessibilityService.kt`，按照上述指南进行重构。主要步骤：

1. 删除旧的倒计时和通知相关代码
2. 重写 `trackAppSwitch` 方法
3. 修改 `onAccessibilityEvent` 调用
4. 修改 `onDestroy` 清理逻辑
5. 测试验证
