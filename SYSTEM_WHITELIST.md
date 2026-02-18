# 系统应用白名单说明

## 🛡️ 问题描述

在专注模式或日常管理中，如果不正确处理系统应用，会导致严重的用户体验问题：

1. **UMind 自己被阻止** - 用户无法使用应用本身
2. **Launcher 被阻止** - 用户无法返回桌面，一直弹出退出提示
3. **系统设置被阻止** - 用户无法修改设置
4. **电话/短信被阻止** - 用户无法接打电话

## ✅ 解决方案

### 1. 系统应用白名单

在 `BlockAccessibilityService` 和 `BlockingEngine` 中添加了完整的系统应用白名单：

```kotlin
private val SYSTEM_WHITELIST = setOf(
    // Android 系统
    "android",
    "com.android.systemui",

    // 启动器（Launcher）- 各大厂商
    "com.android.launcher",
    "com.android.launcher2",
    "com.android.launcher3",
    "com.google.android.apps.nexuslauncher", // Pixel Launcher
    "com.google.android.launcher",
    "com.sec.android.app.launcher", // Samsung
    "com.miui.home", // MIUI
    "com.huawei.android.launcher", // Huawei
    "com.oppo.launcher", // OPPO
    "com.vivo.launcher", // Vivo

    // 系统设置
    "com.android.settings",
    "com.google.android.settings",

    // 电话和短信
    "com.android.phone",
    "com.android.dialer",
    "com.google.android.dialer",
    "com.android.mms",
    "com.google.android.apps.messaging",

    // 系统关键服务
    "com.android.vending", // Google Play Store
    "com.google.android.gms", // Google Play Services
    "com.android.packageinstaller",
    "com.google.android.packageinstaller",

    // 输入法
    "com.android.inputmethod.latin",
    "com.google.android.inputmethod.latin",

    // 相机
    "com.android.camera",
    "com.android.camera2",
    "com.google.android.GoogleCamera",

    // 时钟和闹钟
    "com.android.deskclock",
    "com.google.android.deskclock"
)
```

### 2. 三层保护机制

#### 第一层：UMind 自身保护
```kotlin
if (packageName == this.packageName) {
    return true
}
```

#### 第二层：白名单检查
```kotlin
if (packageName in SYSTEM_WHITELIST) {
    return true
}
```

#### 第三层：系统应用智能检测
```kotlin
val appInfo = packageManager.getApplicationInfo(packageName, 0)
val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

if (isSystemApp) {
    val systemKeywords = listOf("launcher", "settings", "phone", "dialer", "mms", "messaging")
    if (systemKeywords.any { packageName.contains(it, ignoreCase = true) }) {
        return true
    }
}
```

### 3. 在两个位置实现

#### BlockAccessibilityService
```kotlin
private fun isSystemWhitelistedApp(packageName: String): Boolean {
    // 1. 检查是否是 UMind 自己
    if (packageName == this.packageName) {
        return true
    }

    // 2. 检查是否在系统白名单中
    if (packageName in SYSTEM_WHITELIST) {
        return true
    }

    // 3. 检查是否是系统应用（额外保护）
    try {
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isSystemUpdate = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

        if (isSystemApp || isSystemUpdate) {
            val systemKeywords = listOf("launcher", "settings", "phone", "dialer", "mms", "messaging")
            if (systemKeywords.any { packageName.contains(it, ignoreCase = true) }) {
                return true
            }
        }
    } catch (e: Exception) {
        Log.e("BlockAccessibilityService", "Error checking system app: $packageName", e)
    }

    return false
}
```

#### BlockingEngine
```kotlin
private fun isSystemWhitelistedApp(packageName: String): Boolean {
    return packageName in SYSTEM_WHITELIST ||
           packageName.startsWith("com.example.umind") // UMind 自己
}
```

## 🎯 效果

### 永远不会被阻止的应用

1. ✅ **UMind 自己** - 用户可以随时使用应用
2. ✅ **所有 Launcher** - 用户可以正常返回桌面
3. ✅ **系统设置** - 用户可以修改设置
4. ✅ **电话/短信** - 紧急通讯不受影响
5. ✅ **输入法** - 用户可以正常输入
6. ✅ **相机** - 用户可以拍照
7. ✅ **闹钟** - 闹钟功能正常

### 专注模式下的行为

```
用户开启专注模式
  ↓
尝试打开 UMind
  → ✅ 允许（系统白名单）

尝试打开 Pixel Launcher
  → ✅ 允许（系统白名单）

尝试打开电话
  → ✅ 允许（系统白名单）

尝试打开抖音
  → ❌ 阻止（不在白名单）
```

## 📝 日志输出

### 系统应用被过滤
```
BlockAccessibilityService: Ignoring whitelisted package: com.google.android.apps.nexuslauncher
BlockingEngine: System whitelisted app, allowing: com.example.umind
```

### 普通应用被检查
```
BlockAccessibilityService: Processing window change to: com.tiktok
BlockingEngine: === getBlockInfo for com.tiktok ===
```

## 🔧 如何添加新的白名单应用

如果需要添加新的系统应用到白名单，在两个地方添加：

1. **BlockAccessibilityService.kt**
```kotlin
private val SYSTEM_WHITELIST = setOf(
    // ... 现有应用
    "com.new.system.app" // 新添加的应用
)
```

2. **BlockingEngine.kt**
```kotlin
private val SYSTEM_WHITELIST = setOf(
    // ... 现有应用
    "com.new.system.app" // 新添加的应用
)
```

## ⚠️ 注意事项

1. **不要过度添加** - 只添加真正的系统关键应用
2. **保持同步** - 两个文件中的白名单应该保持一致
3. **测试验证** - 添加后测试确保不影响正常功能

## 🎉 总结

通过三层保护机制和完整的系统应用白名单，确保：
- ✅ UMind 自己永远可用
- ✅ 系统关键功能不受影响
- ✅ 用户体验流畅
- ✅ 专注模式正常工作

---

**文档版本：** v1.0
**最后更新：** 2026-02-18
