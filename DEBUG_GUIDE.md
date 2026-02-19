# 调试指南

## 问题：打开被限制的应用时没有阻止

### 检查步骤

#### 1. 确认无障碍服务已启用
```bash
# 查看无障碍服务状态
adb shell settings get secure enabled_accessibility_services
```
应该包含 `com.example.umind/com.example.umind.BlockAccessibilityService`

#### 2. 确认弹窗权限已授予
```bash
# 检查弹窗权限
adb shell appops get com.example.umind SYSTEM_ALERT_WINDOW
```
应该返回 `allow`

#### 3. 查看实时日志
```bash
# 查看无障碍服务日志
adb logcat -s BlockAccessibilityService:D

# 或者查看所有相关日志
adb logcat | grep -E "BlockAccessibilityService|FocusRepository"
```

#### 4. 测试流程
1. 打开应用的"设置"页面，确认：
   - ✅ 无障碍服务：已启用
   - ✅ 弹窗权限：已启用

2. 创建一个专注策略：
   - 添加要阻止的应用（例如：微信、抖音等）
   - 设置时间范围（确保当前时间在范围内）
   - **重要**：启用策略开关

3. 尝试打开被阻止的应用

4. 观察日志输出，应该看到：
   ```
   BlockAccessibilityService: === Received event ===
   BlockAccessibilityService: Event type: 32 (TYPE_WINDOW_STATE_CHANGED)
   BlockAccessibilityService: Package: com.tencent.mm (或其他被阻止的应用)
   BlockAccessibilityService: Should block com.tencent.mm: true
   BlockAccessibilityService: !!! BLOCKING APP: com.tencent.mm !!!
   BlockAccessibilityService: Home action result: true
   BlockAccessibilityService: Showing block dialog for: com.tencent.mm
   ```

### 常见问题

#### 问题1：日志显示 "Should block xxx: false"
**原因**：策略没有正确配置或未激活
**解决**：
1. 确认策略已启用（开关打开）
2. 确认应用在目标应用列表中
3. 确认当前时间在专注时间范围内
4. 确认执行模式不是"仅监控"

#### 问题2：没有看到任何日志
**原因**：无障碍服务未运行
**解决**：
1. 重新启用无障碍服务
2. 重启应用
3. 检查系统是否限制了无障碍服务（MIUI等）

#### 问题3：日志显示 "Home action result: false"
**原因**：无障碍服务没有权限执行全局操作
**解决**：
1. 重新授予无障碍服务权限
2. 在MIUI等系统中，需要额外授予"后台弹出界面"权限

#### 问题4：弹窗没有显示
**原因**：弹窗权限未授予或被系统限制
**解决**：
1. 在设置页面点击"启用弹窗权限"
2. 在MIUI等系统中，需要在"权限管理"中手动授予"显示悬浮窗"权限

### MIUI 特殊设置

如果您使用的是小米手机（MIUI系统），需要额外设置：

1. **自启动权限**
   - 设置 → 应用设置 → 应用管理 → 专注模式 → 自启动：开启

2. **后台弹出界面**
   - 设置 → 应用设置 → 应用管理 → 专注模式 → 权限管理 → 后台弹出界面：允许

3. **显示悬浮窗**
   - 设置 → 应用设置 → 应用管理 → 专注模式 → 权限管理 → 显示悬浮窗：允许

4. **无障碍服务**
   - 设置 → 更多设置 → 无障碍 → 已安装的服务 → 专注模式：开启

5. **省电策略**
   - 设置 → 应用设置 → 应用管理 → 专注模式 → 省电策略：无限制

### 手动测试命令

```bash
# 1. 安装应用
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 2. 启动应用
adb shell am start -n com.example.umind/.MainActivity

# 3. 查看日志
adb logcat -c  # 清空日志
adb logcat -s BlockAccessibilityService:D

# 4. 测试打开被阻止的应用（例如微信）
adb shell am start -n com.tencent.mm/.ui.LauncherUI

# 5. 检查无障碍服务是否运行
adb shell dumpsys accessibility | grep -A 10 "com.example.umind"
```

### 预期行为

当一切正常工作时：
1. 用户点击被阻止的应用图标
2. 应用开始启动
3. 无障碍服务检测到窗口变化
4. 立即返回桌面（应用被阻止）
5. 显示全屏半透明弹窗，提示用户应用已被阻止
6. 3秒后弹窗自动消失，或用户点击"我知道了"关闭

### 如果还是不工作

请提供以下信息：
1. 手机型号和系统版本
2. 完整的 logcat 日志
3. 策略配置截图
4. 权限设置截图
