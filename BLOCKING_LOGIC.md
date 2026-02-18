# UMind 限制逻辑与阻止决策文档

## 📋 文档概述

本文档详细说明 UMind 应用的核心限制逻辑和阻止决策流程，确保所有开发者和维护者都能清楚理解应用如何决定是否阻止某个应用。

---

## 🎯 核心原则

### 1. 优先级系统

```
专注模式 (Focus Mode) > 日常管理 (Daily Management)
```

**规则：**
- 当专注模式激活时，**忽略所有日常管理规则**
- 专注模式下，只检查白名单
- 专注模式未激活时，才检查日常管理规则

### 2. 多策略合并规则

当多个日常管理策略同时激活时，采用以下合并规则：

| 限制类型 | 合并规则 | 说明 |
|---------|---------|------|
| 时间范围 | **取并集** | 所有限制时间段的合并 |
| 使用时长 | **取最小值** | 最严格的时长限制 |
| 打开次数 | **取最小值** | 最严格的次数限制 |
| 执行模式 | **取最严格** | FORCE_THROUGH_APP > DIRECT_BLOCK > MONITOR_ONLY |

---

## 🔍 阻止决策流程

### 完整流程图

```
用户尝试打开应用
    ↓
检查是否为系统应用或 UMind 自身
    ↓ 否
【优先级 1】检查专注模式
    ├─ 专注模式激活？
    │   ├─ 是 → 检查白名单
    │   │   ├─ 在白名单 → ✅ 允许打开
    │   │   └─ 不在白名单 → ❌ 阻止（专注模式）
    │   └─ 否 → 继续
    ↓
【优先级 2】检查日常管理
    ├─ 获取所有激活的策略
    ├─ 筛选包含该应用的策略
    ├─ 策略为空？
    │   └─ 是 → ✅ 允许打开
    ↓
合并所有相关策略
    ├─ 合并时间限制（并集）
    ├─ 合并使用时长（最小值）
    ├─ 合并打开次数（最小值）
    └─ 确定执行模式（最严格）
    ↓
根据执行模式判断
    ├─ MONITOR_ONLY → ✅ 允许打开（仅记录）
    ├─ DIRECT_BLOCK → 检查所有限制
    │   ├─ 在限制时间内？→ ❌ 阻止
    │   ├─ 使用时长超限？→ ❌ 阻止
    │   ├─ 打开次数超限？→ ❌ 阻止
    │   └─ 都不满足 → ✅ 允许打开
    └─ FORCE_THROUGH_APP
        ├─ 从外部打开？→ ❌ 阻止（必须通过 UMind）
        └─ 从 UMind 打开？→ 检查所有限制
            ├─ 在限制时间内？→ ❌ 阻止
            ├─ 使用时长超限？→ ❌ 阻止
            ├─ 打开次数超限？→ ❌ 阻止
            └─ 都不满足 → ✅ 允许打开
```

---

## 💻 代码实现

### 核心类：BlockingEngine

**位置：** `com.example.umind.service.BlockingEngine`

**职责：**
1. 判断应用是否应该被阻止
2. 合并多个策略的限制规则
3. 处理专注模式和日常管理的优先级

### 主要方法

#### 1. getBlockInfo()

```kotlin
suspend fun getBlockInfo(
    packageName: String,
    openedFromUMind: Boolean = false
): BlockInfo
```

**功能：** 获取应用的阻止信息

**返回：** `BlockInfo` 对象，包含：
- `shouldBlock: Boolean` - 是否应该阻止
- `reasons: List<BlockReason>` - 阻止原因列表
- `usageInfo: UsageInfo?` - 使用信息（剩余时长、剩余次数等）

**流程：**
1. 检查专注模式（优先级 1）
2. 检查日常管理（优先级 2）

#### 2. checkFocusMode()

```kotlin
private suspend fun checkFocusMode(packageName: String): BlockInfo?
```

**功能：** 检查专注模式

**返回：**
- `BlockInfo` - 如果应该阻止
- `null` - 如果应该允许

**逻辑：**
```kotlin
if (!focusMode.shouldBeActive()) {
    return null  // 专注模式未激活
}

if (focusMode.isAppAllowed(packageName)) {
    return null  // 在白名单中，允许
}

return BlockInfo(
    shouldBlock = true,
    reasons = listOf(BlockReason.FocusModeActive)
)  // 不在白名单，阻止
```

#### 3. checkDailyManagement()

```kotlin
private suspend fun checkDailyManagement(
    packageName: String,
    openedFromUMind: Boolean
): BlockInfo
```

**功能：** 检查日常管理策略

**流程：**
1. 获取所有激活的策略
2. 筛选包含该应用的策略
3. 合并所有策略
4. 根据执行模式判断

#### 4. mergeStrategies()

```kotlin
private fun mergeStrategies(
    strategies: List<FocusStrategy>,
    packageName: String
): MergedRestriction
```

**功能：** 合并多个策略

**合并规则：**

1. **时间限制（取并集）**
```kotlin
val mergedTimeRestrictions = strategies
    .flatMap { it.timeRestrictions }
    .filter { it.isEnabled }
```

2. **使用时长（取最小值）**
```kotlin
val usageLimits = strategies.mapNotNull { strategy ->
    strategy.usageLimits?.let { limits ->
        when (limits.type) {
            LimitType.TOTAL_ALL -> limits.totalLimit
            LimitType.PER_APP -> limits.perAppLimit
            LimitType.INDIVIDUAL -> limits.individualLimits[packageName]
        }
    }
}
val mergedUsageLimit = usageLimits.minOrNull()
```

3. **打开次数（取最小值）**
```kotlin
val openCountLimits = strategies.mapNotNull { strategy ->
    strategy.openCountLimits?.let { limits ->
        when (limits.type) {
            LimitType.TOTAL_ALL -> limits.totalCount
            LimitType.PER_APP -> limits.perAppCount
            LimitType.INDIVIDUAL -> limits.individualCounts[packageName]
        }
    }
}
val mergedOpenCountLimit = openCountLimits.minOrNull()
```

4. **执行模式（取最严格）**
```kotlin
val enforcementMode = strategies
    .map { it.enforcementMode }
    .maxByOrNull { it.strictness }
    ?: EnforcementMode.MONITOR_ONLY

// 严格程度：
// MONITOR_ONLY = 1
// DIRECT_BLOCK = 2
// FORCE_THROUGH_APP = 3
```

#### 5. checkAllRestrictions()

```kotlin
private suspend fun checkAllRestrictions(
    packageName: String,
    merged: MergedRestriction
): BlockInfo
```

**功能：** 检查所有限制条件

**检查顺序：**

1. **时间范围限制**
```kotlin
val withinTimeRestriction = merged.timeRestrictions.any {
    it.isWithinRestriction()
}
if (withinTimeRestriction) {
    reasons.add(BlockReason.TimeRestriction(nextAvailableTime))
}
```

2. **使用时长限制**
```kotlin
if (usageLimit != null) {
    val appUsage = usageTrackingRepository.getUsageDuration(packageName, today)
    val remainingMs = limitMs - usedMs

    if (remainingMs <= 0) {
        reasons.add(BlockReason.UsageLimitExceeded(...))
    }
}
```

3. **打开次数限制**
```kotlin
if (openCountLimit != null) {
    val openCount = usageTrackingRepository.getOpenCount(packageName, today)

    if (openCount >= openCountLimit) {
        reasons.add(BlockReason.OpenCountLimitExceeded(...))
    }
}
```

**返回：**
```kotlin
BlockInfo(
    shouldBlock = reasons.isNotEmpty(),
    reasons = reasons,
    usageInfo = usageInfo
)
```

---

## 🚫 阻止原因类型

### BlockReason 枚举

```kotlin
sealed class BlockReason {
    // 时间范围限制
    data class TimeRestriction(val nextAvailableTime: String?) : BlockReason()

    // 使用时长超限
    data class UsageLimitExceeded(
        val limitMinutes: Long,
        val usedMinutes: Long
    ) : BlockReason()

    // 打开次数超限
    data class OpenCountLimitExceeded(
        val limitCount: Int,
        val usedCount: Int
    ) : BlockReason()

    // 专注模式激活
    object FocusModeActive : BlockReason()

    // 需要通过 UMind 打开
    object ForceThroughApp : BlockReason()
}
```

---

## 📊 执行模式详解

### 1. MONITOR_ONLY（仅监控）

**行为：**
- ✅ 允许打开应用
- 📝 记录使用数据
- 📊 返回使用信息（剩余时长、剩余次数）
- 🔔 可以显示通知提醒

**适用场景：**
- 初期了解使用习惯
- 不想强制限制，只想有意识地监控

**代码逻辑：**
```kotlin
EnforcementMode.MONITOR_ONLY -> {
    val usageInfo = calculateUsageInfo(packageName, mergedRestriction)
    BlockInfo(
        shouldBlock = false,
        reasons = emptyList(),
        usageInfo = usageInfo
    )
}
```

### 2. DIRECT_BLOCK（直接阻止）

**行为：**
- ❌ 在限制范围内直接阻止
- 🏠 立即返回桌面
- 💬 显示阻止弹窗
- 📝 记录阻止事件

**适用场景：**
- 需要严格的自我管理
- 防止无意识地打开应用

**代码逻辑：**
```kotlin
EnforcementMode.DIRECT_BLOCK -> {
    checkAllRestrictions(packageName, mergedRestriction)
    // 如果 reasons.isNotEmpty()，则阻止
}
```

### 3. FORCE_THROUGH_APP（强制通过应用）

**行为：**
- ❌ 从外部打开：**始终阻止**
- ✅ 从 UMind 内打开：检查限制后决定
- 💬 显示"需要通过 UMind 打开"的提示

**适用场景：**
- 需要在使用前有意识地确认
- 想要更好的使用追踪和统计

**代码逻辑：**
```kotlin
EnforcementMode.FORCE_THROUGH_APP -> {
    if (!openedFromUMind) {
        // 从外部打开，始终阻止
        BlockInfo(
            shouldBlock = true,
            reasons = listOf(BlockReason.ForceThroughApp)
        )
    } else {
        // 从 UMind 内打开，检查限制
        checkAllRestrictions(packageName, mergedRestriction)
    }
}
```

---

## 📝 使用记录

### 记录时机

1. **打开次数记录**
   - 时机：应用切换时（不同应用）
   - 位置：`BlockAccessibilityService.trackAppSwitch()`
   - 方法：`usageTrackingRepository.recordAppOpen(packageName)`

2. **使用时长记录**
   - 时机：倒计时暂停时
   - 位置：`UsageCountdownManager.pauseCountdown()`
   - 方法：`usageTrackingRepository.recordUsage(packageName, duration)`

3. **阻止事件记录**
   - 时机：应用被阻止时
   - 位置：`BlockAccessibilityService.onAccessibilityEvent()`
   - 方法：`blockEventRepository.recordBlockEvent(...)`

### 数据模型

```kotlin
// 使用记录
data class UsageRecordEntity(
    val packageName: String,
    val date: LocalDate,
    val usageDurationMillis: Long,  // 使用时长
    val openCount: Int               // 打开次数
)

// 使用会话
data class UsageSessionEntity(
    val packageName: String,
    val startTime: Long,
    val endTime: Long?,
    val durationMillis: Long
)

// 阻止事件
data class BlockEventEntity(
    val packageName: String,
    val appName: String,
    val timestamp: Long,
    val blockReason: String,         // JSON 格式
    val blockSource: String          // FOCUS_MODE / DAILY_MANAGEMENT
)
```

---

## 🔧 调试与日志

### 关键日志点

1. **BlockingEngine.getBlockInfo()**
```
=== getBlockInfo for {packageName} ===
openedFromUMind: {true/false}
```

2. **专注模式检查**
```
Focus mode is active
App is in whitelist, allowing
或
App not in whitelist, blocking
```

3. **策略合并**
```
=== Merging {N} strategies ===
Merged time restrictions: {N} time slots
Merged usage limit: {duration}
Merged open count limit: {count}
Merged enforcement mode: {mode}
```

4. **限制检查**
```
Usage check: limit={X}min, used={Y}min, remaining={Z}min
Open count check: limit={X}, used={Y}, remaining={Z}
```

5. **最终决策**
```
Final decision: shouldBlock={true/false}, reasons={N}
```

### 日志级别

- `Log.d()` - 调试信息（正常流程）
- `Log.e()` - 错误信息（异常情况）

---

## ✅ 测试场景

### 1. 专注模式测试

| 场景 | 应用状态 | 预期结果 |
|------|---------|---------|
| 专注模式激活 + 应用在白名单 | 任意 | ✅ 允许 |
| 专注模式激活 + 应用不在白名单 | 任意 | ❌ 阻止（专注模式） |
| 专注模式未激活 | 任意 | 检查日常管理 |

### 2. 单策略测试

| 场景 | 限制条件 | 预期结果 |
|------|---------|---------|
| 时间范围内 | 9:00-18:00，当前 10:00 | ❌ 阻止（时间限制） |
| 时间范围外 | 9:00-18:00，当前 20:00 | ✅ 允许 |
| 使用时长超限 | 限制 30 分钟，已用 35 分钟 | ❌ 阻止（时长超限） |
| 使用时长未超限 | 限制 30 分钟，已用 20 分钟 | ✅ 允许 |
| 打开次数超限 | 限制 5 次，已打开 5 次 | ❌ 阻止（次数超限） |
| 打开次数未超限 | 限制 5 次，已打开 3 次 | ✅ 允许 |

### 3. 多策略合并测试

| 场景 | 策略 A | 策略 B | 合并结果 |
|------|--------|--------|---------|
| 时长限制 | 30 分钟 | 60 分钟 | 30 分钟（最小值） |
| 次数限制 | 5 次 | 10 次 | 5 次（最小值） |
| 时间范围 | 9:00-12:00 | 11:00-14:00 | 9:00-14:00（并集） |
| 执行模式 | MONITOR_ONLY | DIRECT_BLOCK | DIRECT_BLOCK（最严格） |

### 4. 执行模式测试

| 模式 | 限制条件 | 预期行为 |
|------|---------|---------|
| MONITOR_ONLY | 任意 | ✅ 允许 + 记录 |
| DIRECT_BLOCK | 在限制内 | ❌ 阻止 |
| DIRECT_BLOCK | 不在限制内 | ✅ 允许 |
| FORCE_THROUGH_APP | 从外部打开 | ❌ 阻止 |
| FORCE_THROUGH_APP | 从 UMind 打开 + 在限制内 | ❌ 阻止 |
| FORCE_THROUGH_APP | 从 UMind 打开 + 不在限制内 | ✅ 允许 |

---

## 🎓 最佳实践

### 1. 策略设计

- ✅ 使用清晰的策略名称（如"工作时间限制"、"睡前限制"）
- ✅ 避免过多重叠的策略（会增加复杂度）
- ✅ 优先使用 DIRECT_BLOCK 模式（最直观）
- ⚠️ 谨慎使用 FORCE_THROUGH_APP（需要额外的 UI 支持）

### 2. 限制设置

- ✅ 时间范围：设置明确的开始和结束时间
- ✅ 使用时长：根据实际需求设置合理的时长
- ✅ 打开次数：考虑应用的使用特点（如社交应用可能需要更多次数）

### 3. 调试技巧

- ✅ 查看 Logcat 中的 `BlockingEngine` 标签
- ✅ 检查策略是否正确激活
- ✅ 验证使用记录是否正确累加
- ✅ 测试多策略合并的结果

---

## 📚 相关文档

- [需求文档](function.md) - 完整的业务需求
- [架构文档](architecture-best-practices.md) - 系统架构设计
- [API 文档](API.md) - 接口定义

---

**文档版本：** v1.0
**最后更新：** 2026-02-18
**维护者：** UMind Team
