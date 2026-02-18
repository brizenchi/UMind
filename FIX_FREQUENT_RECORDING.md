# 修复：防止频繁记录时间的问题

## 🐛 问题描述

用户发现"每过几秒就重新记录时间"，导致数据库频繁写入。

## 🔍 问题原因

在 `trackAppSwitch` 方法中，即使是同一个应用（比如应用内的窗口变化触发了 `TYPE_WINDOW_STATE_CHANGED` 事件），也会重新调用 `startCountdown`。

### 问题流程

1. 用户在应用内操作，触发窗口变化事件
2. `onAccessibilityEvent` 被触发
3. `trackAppSwitch` 被调用
4. `startCountdown` 被调用
5. `startCountdown` 内部先调用 `stopCountdown`
6. `stopCountdown` 保存当前会话的使用时长到数据库 ❌
7. 然后重新开始倒计时

这导致：
- 每次窗口变化都会保存一次时间
- 倒计时被频繁重启
- 数据库频繁写入

## ✅ 解决方案

在 `trackAppSwitch` 方法开始处添加检查：

```kotlin
// 检查是否是同一个应用
val isSameApp = currentForegroundPackage == newPackageName

// 只有在真正切换应用时才执行后续逻辑
if (!isSameApp) {
    // 暂停上一个应用的倒计时
    // 记录新应用的打开事件
    // 启动新应用的倒计时
} else {
    Log.d("BlockAccessibilityService", "Same app $newPackageName, skipping countdown restart")
    return // 同一个应用，直接返回
}
```

## 📝 修改内容

**文件**: `BlockAccessibilityService.kt`

**修改位置**: `trackAppSwitch` 方法（line 195-287）

**关键改动**:
1. 在方法开始处添加 `isSameApp` 检查
2. 只有当 `!isSameApp` 时才执行：
   - 暂停上一个应用的倒计时
   - 记录新应用的打开事件
   - 更新当前应用状态
   - 启动新应用的倒计时
3. 如果是同一个应用，直接 `return`，跳过所有逻辑

## 🎯 效果

修复后：
- ✅ 只有在真正切换应用时才会保存时间
- ✅ 同一应用内的窗口变化不会触发倒计时重启
- ✅ 减少数据库写入频率
- ✅ 倒计时更加稳定，不会被频繁重启

## 🧪 测试验证

### 测试步骤
1. 打开一个受限应用（如抖音）
2. 在应用内进行各种操作（滑动、点击、切换页面）
3. 观察日志

### 预期结果
- 只在首次打开应用时看到 "Starting countdown" 日志
- 应用内操作时看到 "Same app xxx, skipping countdown restart" 日志
- 倒计时持续运行，不会重启

### 实际测试
```
// 首次打开应用
D/BlockAccessibilityService: Recorded app open for com.ss.android.ugc.aweme
D/BlockAccessibilityService: Starting countdown: limit=60000ms, used=0ms

// 应用内操作（窗口变化）
D/BlockAccessibilityService: Same app com.ss.android.ugc.aweme, skipping countdown restart
D/BlockAccessibilityService: Same app com.ss.android.ugc.aweme, skipping countdown restart

// 切换到其他应用
D/BlockAccessibilityService: Pausing countdown for com.ss.android.ugc.aweme
D/BlockAccessibilityService: Recorded app open for com.android.launcher
```

## ✅ 编译状态

```
BUILD SUCCESSFUL in 7s
```

修复已完成，代码编译通过。

## 📚 相关文件

- `BlockAccessibilityService.kt` - 修复了 trackAppSwitch 方法
- `UsageCountdownManager.kt` - 倒计时管理器（无需修改）
- `UsageNotificationManager.kt` - 通知管理器（无需修改）
