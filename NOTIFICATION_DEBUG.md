# 通知问题调试指南

## 问题描述
用户看不到任何倒计时通知。

## 已添加的调试功能

### 1. 测试通知
当无障碍服务启动时，会显示一个测试通知：
- 标题: "UMind 服务已启动"
- 内容: "无障碍服务正在运行，通知系统正常"

**如果看到这个通知**：说明通知系统基本正常
**如果看不到这个通知**：说明通知权限或系统设置有问题

### 2. Toast调试提示
在关键位置添加了Toast提示：
- "调试: usageInfo为null" - 如果没有使用限制信息
- "调试: 准备显示通知 - 剩余X分钟" - 准备显示通知时
- "调试: 通知已发送 (ID: X)" - 通知发送后
- "错误: 通知管理器为null" - 如果通知管理器异常

### 3. 详细日志
在以下位置添加了详细日志：
- `FocusRepository.getBlockInfo()` - 检查使用限制和创建usageInfo
- `BlockAccessibilityService.showUsageNotification()` - 显示通知的完整流程
- `BlockAccessibilityService.startNotificationCountdown()` - 倒计时启动

## 调试步骤

### 步骤1: 检查测试通知

1. **关闭无障碍服务**
   - 设置 -> 辅助功能 -> 无障碍 -> UMind -> 关闭

2. **重新开启无障碍服务**
   - 设置 -> 辅助功能 -> 无障碍 -> UMind -> 开启

3. **检查通知**
   - 下拉通知栏
   - 应该看到"UMind 服务已启动"的通知

**结果判断**:
- ✅ 看到测试通知 -> 通知系统正常，继续步骤2
- ❌ 看不到测试通知 -> 通知权限有问题，跳到"通知权限检查"

### 步骤2: 检查策略配置

1. **查看策略详情**
   - 打开UMind应用
   - 进入"专注"页面
   - 点击你创建的策略

2. **确认配置**
   - 是否选择了目标应用？
   - 是否设置了使用时长限制？
   - 限制类型是什么？（所有应用总计/每个应用/单独设置）
   - 限制时长是多少？
   - 策略是否已激活？

3. **运行日志**
   ```bash
   ./debug_logs.sh
   ```

4. **打开被限制的应用**

5. **查看Toast提示**
   - 应该看到"调试: 准备显示通知 - 剩余X分钟"
   - 应该看到"调试: 通知已发送 (ID: X)"

**结果判断**:
- ✅ 看到Toast提示 -> 代码正常执行，继续步骤3
- ❌ 看到"调试: usageInfo为null" -> 策略配置有问题，跳到"策略配置问题"
- ❌ 没有任何Toast -> 代码没有执行，跳到"代码执行问题"

### 步骤3: 检查日志输出

在日志中搜索以下关键词：

1. **检查策略加载**
   ```
   搜索: "Checking usage limits"
   期望: usageLimits: UsageLimits(type=PER_APP, ...)
   ```

2. **检查usageInfo创建**
   ```
   搜索: "Creating usage info"
   期望: usageLimitMinutes: 30 (或其他值)
   期望: Created usageInfo: UsageInfo(...)
   ```

3. **检查通知显示**
   ```
   搜索: "showUsageNotification called"
   期望: Usage info: remainingMinutes=30
   ```

4. **检查通知发送**
   ```
   搜索: "Notification.notify() called"
   期望: with ID: 1000 (或其他ID)
   ```

5. **检查倒计时启动**
   ```
   搜索: "Starting countdown"
   期望: Starting countdown: 1800 seconds for com.xxx.xxx
   ```

## 常见问题排查

### 问题1: 看不到测试通知

**可能原因**:
1. 通知权限未授予
2. 通知渠道被禁用
3. 系统勿扰模式开启
4. 通知被系统屏蔽

**解决方法**:

1. **检查通知权限**
   ```
   设置 -> 应用 -> UMind -> 通知 -> 确保开启
   ```

2. **检查通知渠道**
   ```
   设置 -> 应用 -> UMind -> 通知 -> 应用使用提醒
   确保重要性为"高"或"紧急"
   ```

3. **检查勿扰模式**
   ```
   设置 -> 声音和振动 -> 勿扰模式 -> 确保关闭
   或者将UMind添加到勿扰模式例外
   ```

4. **重启应用**
   ```
   强制停止UMind应用
   重新开启无障碍服务
   ```

### 问题2: Toast显示"usageInfo为null"

**可能原因**:
1. 策略没有设置使用时长限制
2. 策略没有激活
3. 应用不在目标应用列表中

**解决方法**:

1. **重新创建策略**
   - 删除现有策略
   - 创建新策略
   - 确保选择目标应用
   - 确保设置使用时长限制（例如：0小时2分钟）
   - 确保激活策略

2. **查看日志**
   ```
   搜索: "usageLimits:"
   应该看到: usageLimits: UsageLimits(type=PER_APP, perAppLimit=PT2M)
   ```

3. **使用调试工具**
   ```
   设置 -> 调试工具 -> 查看今日记录
   确认策略配置是否正确
   ```

### 问题3: 没有任何Toast提示

**可能原因**:
1. 无障碍服务没有运行
2. 应用不在目标应用列表中
3. 代码逻辑有问题

**解决方法**:

1. **检查无障碍服务**
   ```
   设置 -> 辅助功能 -> 无障碍 -> UMind
   确保开关是开启状态
   ```

2. **查看日志**
   ```
   搜索: "Received event"
   应该看到: Package: com.xxx.xxx (你打开的应用)
   ```

3. **检查应用是否在目标列表**
   ```
   搜索: "Check if package is in target apps"
   或查看策略配置中的目标应用列表
   ```

### 问题4: Toast显示但通知不显示

**可能原因**:
1. 通知渠道重要性太低
2. 通知被系统优化掉了
3. 通知ID冲突

**解决方法**:

1. **提高通知渠道重要性**
   ```
   设置 -> 应用 -> UMind -> 通知 -> 应用使用提醒
   将重要性设置为"紧急"
   开启"在锁定屏幕上显示"
   ```

2. **清除应用数据重试**
   ```
   设置 -> 应用 -> UMind -> 存储 -> 清除数据
   重新配置策略
   ```

3. **检查通知历史**
   ```
   设置 -> 通知 -> 通知历史记录
   查看是否有UMind的通知记录
   ```

## 日志分析示例

### 正常流程的日志

```
BlockAccessibilityService: Service connected and configured
BlockAccessibilityService: Notification channel created with HIGH importance
BlockAccessibilityService: Test notification shown with ID 999

[用户打开被限制的应用]

BlockAccessibilityService: === Received event ===
BlockAccessibilityService: Package: com.android.chrome
FocusRepository: === Checking usage limits ===
FocusRepository: usageLimits: UsageLimits(type=PER_APP, perAppLimit=PT2M)
FocusRepository: Usage limit type: PER_APP
FocusRepository: === Creating usage info ===
FocusRepository: usageLimitMinutes: 2
FocusRepository: Created usageInfo: UsageInfo(usageLimitMinutes=2, remainingMinutes=2, ...)
BlockAccessibilityService: Not blocking com.android.chrome
BlockAccessibilityService: === showUsageNotification called for: com.android.chrome ===
BlockAccessibilityService: Usage info: remainingMinutes=2, remainingCount=null
BlockAccessibilityService: App name: Chrome
BlockAccessibilityService: Notification content: ⏱️ 剩余时间: 2分钟
BlockAccessibilityService: Using notification ID: 1000
BlockAccessibilityService: Notification built, about to show...
BlockAccessibilityService: Notification.notify() called with ID: 1000
BlockAccessibilityService: Starting countdown for 2 minutes
BlockAccessibilityService: Starting countdown: 120 seconds for com.android.chrome
```

### 异常流程的日志

如果看到：
```
BlockAccessibilityService: No usage info, returning
```
说明usageInfo为null，策略配置有问题。

如果看到：
```
FocusRepository: No usage info created (no limits set)
```
说明策略没有设置使用限制。

## 快速测试命令

```bash
# 1. 清除日志
adb logcat -c

# 2. 开始监控日志
./debug_logs.sh

# 3. 在另一个终端，重启无障碍服务
adb shell settings put secure enabled_accessibility_services com.example.focus/.BlockAccessibilityService

# 4. 打开被限制的应用

# 5. 查看日志输出
```

## 下一步

如果按照以上步骤仍然无法解决问题，请提供：
1. 测试通知是否显示？
2. Toast提示显示了什么？
3. 日志中的关键输出（特别是"Creating usage info"和"showUsageNotification"部分）
4. 策略配置截图
