# UMind - 专注管理应用 业务逻辑文档

## 一、应用整体架构

### 1.1 启动流程
1. 用户首次进入需要授权必要权限（无障碍服务、使用情况访问权限等）
2. 授权完成后进入主界面

### 1.2 主界面结构
应用分为三个 Tab：
- **Tab 1: 日常管理** - 创建和管理日常限制策略
- **Tab 2: 专注模式** - 快速进入专注状态
- **Tab 3: 统计** - 查看使用情况统计
- **Tab 4: 设置** - 全局设置和配置

### 1.3 核心系统概述

UMind 包含两个独立但互补的限制系统：

#### 系统 1：日常管理（Daily Management）
- **用途**：长期的、规律性的应用使用管理
- **特点**：多策略并行、精细化规则、灵活的执行模式
- **适用场景**：工作日限制娱乐应用、睡前限制所有应用、学习时段限制等

#### 系统 2：专注模式（Focus Mode）
- **用途**：临时的、即时的深度专注需求
- **特点**：一键启动、全局阻止、白名单机制
- **适用场景**：需要立即进入专注状态、番茄工作法、考试复习等

#### 优先级规则
```
专注模式 > 日常管理

当专注模式激活时：
  → 忽略所有日常管理规则
  → 只应用专注模式的限制（白名单机制）

当专注模式未激活时：
  → 应用日常管理规则
  → 多个策略冲突时，采用最严格原则
```

---

## 二、系统 1：日常管理（Daily Management）

### 2.1 核心概念

日常管理允许用户创建多个**策略（Strategy）**，每个策略是一组限制规则的组合。

**关键特性：**
- 多个策略可以同时激活
- 策略之间可以重叠
- 冲突时采用最严格原则

### 2.2 策略组成

每个策略包含以下要素：

```
策略 (Strategy)
├── 基本信息
│   ├── 策略名称 (name)
│   ├── 策略ID (id)
│   ├── 激活状态 (isActive)
│   └── 创建/更新时间
│
├── 目标应用 (targetApps)
│   └── 应用包名列表 (List<String>)
│
├── 限制规则 (Restrictions)
│   ├── 时间范围限制 (timeRestrictions)
│   ├── 使用时长限制 (usageLimits)
│   └── 打开次数限制 (openCountLimits)
│
└── 执行模式 (enforcementMode)
    ├── 仅监控 (MONITOR_ONLY)
    ├── 直接阻止 (DIRECT_BLOCK)
    └── 强制通过本应用 (FORCE_THROUGH_APP)
```

### 2.3 限制规则详解

#### 2.3.1 时间范围限制 (Time Range Restrictions)

**功能**：设置应用在特定时间段内不可使用

**数据结构**：
```kotlin
data class TimeRestriction(
    val id: String,                      // 时间段ID
    val daysOfWeek: Set<DayOfWeek>,     // 星期几（可多选）
    val startTime: LocalTime,            // 开始时间 (HH:mm)
    val endTime: LocalTime,              // 结束时间 (HH:mm)
    val isEnabled: Boolean = true        // 是否启用此时间段
)

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}
```

**配置示例**：
```
时间段1：周一至周五 9:00-18:00
时间段2：周六至周日 22:00-次日7:00
```

**判断逻辑**：
```kotlin
fun isWithinRestrictedTime(
    currentTime: LocalDateTime,
    restrictions: List<TimeRestriction>
): Boolean {
    val currentDay = currentTime.dayOfWeek
    val currentTimeOnly = currentTime.toLocalTime()

    return restrictions.any { restriction ->
        restriction.isEnabled &&
        currentDay in restriction.daysOfWeek &&
        isTimeBetween(currentTimeOnly, restriction.startTime, restriction.endTime)
    }
}
```

#### 2.3.2 使用时长限制 (Usage Duration Limits)

**功能**：限制应用的每日使用时长

**数据结构**：
```kotlin
data class UsageLimits(
    val limitType: UsageLimitType,                    // 限制类型
    val totalLimit: Duration? = null,                 // 所有应用总时长
    val perAppLimit: Duration? = null,                // 每个应用时长
    val individualLimits: Map<String, Duration>? = null  // 单独设置
)

enum class UsageLimitType {
    TOTAL_ALL,      // 所有应用总时长
    PER_APP,        // 每个应用相同时长
    INDIVIDUAL      // 每个应用单独设置
}
```

**三种模式**：

1. **所有应用总时长 (TOTAL_ALL)**
   ```kotlin
   UsageLimits(
       limitType = TOTAL_ALL,
       totalLimit = Duration.ofHours(2)  // 总计2小时
   )
   ```
   判断逻辑：`sum(所有目标应用今日使用时长) >= totalLimit`

2. **每个应用时长 (PER_APP)**
   ```kotlin
   UsageLimits(
       limitType = PER_APP,
       perAppLimit = Duration.ofMinutes(30)  // 每个30分钟
   )
   ```
   判断逻辑：`应用X今日使用时长 >= perAppLimit`

3. **单独设置时长 (INDIVIDUAL)**
   ```kotlin
   UsageLimits(
       limitType = INDIVIDUAL,
       individualLimits = mapOf(
           "com.tiktok" to Duration.ofMinutes(20),
           "com.weibo" to Duration.ofMinutes(30)
       )
   )
   ```
   判断逻辑：`应用X今日使用时长 >= individualLimits[应用X]`

**使用时长追踪变量**：
```kotlin
data class DailyUsageRecord(
    val date: LocalDate,                              // 日期
    val packageName: String,                          // 应用包名
    var totalDuration: Duration,                      // 今日总使用时长
    val sessions: MutableList<UsageSession>           // 使用会话列表
)

data class UsageSession(
    val startTime: Long,                              // 会话开始时间戳
    val endTime: Long?,                               // 会话结束时间戳
    val duration: Duration                            // 会话时长
)
```

#### 2.3.3 打开次数限制 (Open Count Limits)

**功能**：限制应用的每日打开次数

**数据结构**：
```kotlin
data class OpenCountLimits(
    val limitType: OpenCountLimitType,                // 限制类型
    val totalCount: Int? = null,                      // 所有应用总次数
    val perAppCount: Int? = null,                     // 每个应用次数
    val individualCounts: Map<String, Int>? = null    // 单独设置
)

enum class OpenCountLimitType {
    TOTAL_ALL,      // 所有应用总次数
    PER_APP,        // 每个应用相同次数
    INDIVIDUAL      // 每个应用单独设置
}
```

**三种模式**：

1. **所有应用总次数 (TOTAL_ALL)**
   ```kotlin
   OpenCountLimits(
       limitType = TOTAL_ALL,
       totalCount = 10  // 总计10次
   )
   ```
   判断逻辑：`sum(所有目标应用今日打开次数) >= totalCount`

2. **每个应用次数 (PER_APP)**
   ```kotlin
   OpenCountLimits(
       limitType = PER_APP,
       perAppCount = 5  // 每个5次
   )
   ```
   判断逻辑：`应用X今日打开次数 >= perAppCount`

3. **单独设置次数 (INDIVIDUAL)**
   ```kotlin
   OpenCountLimits(
       limitType = INDIVIDUAL,
       individualCounts = mapOf(
           "com.tiktok" to 3,
           "com.weibo" to 5
       )
   )
   ```
   判断逻辑：`应用X今日打开次数 >= individualCounts[应用X]`

**打开次数追踪变量**：
```kotlin
data class DailyOpenCountRecord(
    val date: LocalDate,                              // 日期
    val packageName: String,                          // 应用包名
    var openCount: Int,                               // 今日打开次数
    val openTimestamps: MutableList<Long>             // 打开时间戳列表
)
```

### 2.4 执行模式 (Enforcement Modes)

#### 2.4.1 仅监控模式 (MONITOR_ONLY)

**行为**：
- 追踪和记录应用使用情况
- **不阻止**用户使用应用
- 可以显示提醒通知

**适用场景**：
- 初期了解使用习惯
- 不想强制限制，只想有意识地监控

**实现逻辑**：
```kotlin
when (enforcementMode) {
    MONITOR_ONLY -> {
        // 记录使用数据
        recordUsage(packageName, timestamp)

        // 显示通知（可选）
        if (shouldShowNotification) {
            showUsageNotification(packageName, usageInfo)
        }

        // 允许打开
        return AllowOpen
    }
}
```

#### 2.4.2 直接阻止模式 (DIRECT_BLOCK)

**行为**：
- 通过无障碍服务直接阻止应用启动
- 在限制时间范围内，立即返回桌面或显示阻止页面
- 超出限制后，立即阻止

**适用场景**：
- 需要严格的自我管理
- 防止无意识地打开应用

**实现逻辑**：
```kotlin
when (enforcementMode) {
    DIRECT_BLOCK -> {
        if (isRestricted) {
            // 阻止打开
            performGlobalAction(GLOBAL_ACTION_HOME)
            showBlockDialog(packageName, restrictionReason)
            return BlockOpen
        } else {
            // 允许打开，但记录使用
            recordUsage(packageName, timestamp)
            return AllowOpen
        }
    }
}
```

#### 2.4.3 强制通过本应用模式 (FORCE_THROUGH_APP)

**行为**：
- 用户**不能直接**从桌面或其他地方打开被限制的应用
- **必须通过 UMind 应用内**的入口打开目标应用
- 在 UMind 内可以看到当前限制状态和剩余配额
- **即使通过 UMind 打开，仍然需要在允许的时间范围内**

**关键规则**：
```
外部打开（桌面、通知等）：
  → 始终阻止，无论是否在限制时间内
  → 引导用户到 UMind 应用

UMind 内打开：
  → 检查时间范围限制
  → 检查使用时长限制
  → 检查打开次数限制
  → 如果都满足，才允许打开
```

**适用场景**：
- 需要在使用前有意识地确认
- 想要更好的使用追踪和统计
- 需要在打开应用前看到提醒信息

**实现逻辑**：
```kotlin
when (enforcementMode) {
    FORCE_THROUGH_APP -> {
        if (openedFromUMind) {
            // 从 UMind 内打开，检查限制
            if (isRestricted) {
                showBlockDialog(packageName, restrictionReason)
                return BlockOpen
            } else {
                recordUsage(packageName, timestamp)
                launchApp(packageName)
                return AllowOpen
            }
        } else {
            // 从外部打开，始终阻止
            performGlobalAction(GLOBAL_ACTION_HOME)
            showForceThroughAppDialog(packageName)
            return BlockOpen
        }
    }
}
```

### 2.5 多策略冲突解决规则

当多个策略同时激活且对同一应用有限制时，采用以下规则：

#### 规则 1：最严格原则（时长和次数）

对于使用时长和打开次数，取最小值（最严格）。

**示例**：
```
策略A：抖音每天最多 30 分钟
策略B：抖音每天最多 60 分钟
→ 实际生效：30 分钟（更严格）
```

#### 规则 2：时间范围取并集

当多个策略对时间范围有限制时，取限制时间的并集（即所有限制时间段的合并）。

**示例**：
```
策略A：抖音在 9:00-12:00 限制
策略B：抖音在 11:00-14:00 限制
→ 实际限制时间：9:00-14:00（合并重叠时间段）
```

#### 规则 3：执行模式取最严格

执行模式的严格程度：`FORCE_THROUGH_APP > DIRECT_BLOCK > MONITOR_ONLY`

**示例**：
```
策略A：抖音 - 仅监控
策略B：抖音 - 直接阻止
→ 实际执行模式：直接阻止（更严格）
```

#### 规则 4：应用列表取并集

当多个策略限制不同应用时，限制的应用列表取并集。

**示例**：
```
策略A：限制 [抖音、微博]
策略B：限制 [抖音、游戏]
→ 实际限制：[抖音、微博、游戏]（并集）
```

### 2.6 日常管理判断流程

当用户尝试打开应用时，系统按以下流程判断：

```kotlin
fun shouldBlockApp(
    packageName: String,
    openedFromUMind: Boolean,
    currentTime: LocalDateTime
): BlockDecision {
    // 1. 获取所有激活的策略
    val activeStrategies = getAllActiveStrategies()

    // 2. 筛选包含该应用的策略
    val relevantStrategies = activeStrategies.filter {
        packageName in it.targetApps
    }

    if (relevantStrategies.isEmpty()) {
        return BlockDecision.Allow
    }

    // 3. 合并所有策略的限制
    val mergedRestriction = mergeStrategies(relevantStrategies, packageName)

    // 4. 检查执行模式
    when (mergedRestriction.enforcementMode) {
        MONITOR_ONLY -> {
            recordUsage(packageName)
            return BlockDecision.Allow
        }

        DIRECT_BLOCK -> {
            // 检查是否在限制范围内
            val isRestricted = checkRestrictions(
                packageName,
                mergedRestriction,
                currentTime
            )

            return if (isRestricted) {
                BlockDecision.Block(mergedRestriction.reasons)
            } else {
                recordUsage(packageName)
                BlockDecision.Allow
            }
        }

        FORCE_THROUGH_APP -> {
            if (!openedFromUMind) {
                // 从外部打开，始终阻止
                return BlockDecision.BlockForceThrough
            }

            // 从 UMind 内打开，检查限制
            val isRestricted = checkRestrictions(
                packageName,
                mergedRestriction,
                currentTime
            )

            return if (isRestricted) {
                BlockDecision.Block(mergedRestriction.reasons)
            } else {
                recordUsage(packageName)
                BlockDecision.Allow
            }
        }
    }
}

fun checkRestrictions(
    packageName: String,
    restriction: MergedRestriction,
    currentTime: LocalDateTime
): Boolean {
    // 检查时间范围限制
    if (isWithinRestrictedTime(currentTime, restriction.timeRestrictions)) {
        return true
    }

    // 检查使用时长限制
    if (hasExceededUsageLimit(packageName, restriction.usageLimits)) {
        return true
    }

    // 检查打开次数限制
    if (hasExceededOpenCountLimit(packageName, restriction.openCountLimits)) {
        return true
    }

    return false
}
```

---

## 三、系统 2：专注模式（Focus Mode）

### 3.1 核心概念

专注模式是一个**即时的、全局的**限制系统，用于快速进入深度专注状态。

**关键特性：**
- 一键启动/停止
- 全局阻止所有应用（白名单除外）
- 优先级高于日常管理
- 不受日常管理规则影响

### 3.2 专注模式类型

#### 3.2.1 手动专注 (MANUAL)

**功能**：正计时专注，手动开始和停止

**数据结构**：
```kotlin
data class FocusModeEntity(
    @PrimaryKey val id: Int = 1,
    val modeType: FocusModeType,              // MANUAL
    val isEnabled: Boolean,                    // 是否激活
    val startTime: Long?,                      // 开始时间戳
    val updatedAt: Long,                       // 最后更新时间
    val whitelistApps: String                  // 白名单应用（JSON）
)
```

**行为**：
- ��击"开始专注"后，立即激活
- 开始正计时（显示已专注时长）
- 阻止所有应用（白名单除外）
- 点击"停止专注"后，停止计时并解除限制

**计时逻辑**：
```kotlin
// 已专注时长 = 当前时间 - 开始时间
val elapsedTime = System.currentTimeMillis() - startTime
```

#### 3.2.2 计时专注 (COUNTDOWN)

**功能**：倒计时专注，设定专注时长

**数据结构**：
```kotlin
data class FocusModeEntity(
    @PrimaryKey val id: Int = 1,
    val modeType: FocusModeType,              // COUNTDOWN
    val isEnabled: Boolean,                    // 是否激活
    val startTime: Long?,                      // 开始时间戳
    val countdownEndTime: Long?,               // 结束时间戳
    val countdownDuration: Long?,              // 专注时长（毫秒）
    val updatedAt: Long,                       // 最后更新时间
    val whitelistApps: String                  // 白名单应用（JSON）
)
```

**行为**：
- 用户设定专注时长（小时 + 分钟）
- 点击"开始专注"后，立即激活
- 开始倒计时（显示剩余时长）
- 阻止所有应用（白名单除外）
- 倒计时结束后，自动停止并解除限制
- 也可以手动停止

**计时逻辑**：
```kotlin
// 剩余时长 = 结束时间 - 当前时间
val remainingTime = (countdownEndTime - System.currentTimeMillis()).coerceAtLeast(0)

// 自动停止检查
if (remainingTime == 0L && isEnabled) {
    stopFocusMode()
}
```

### 3.3 白名单机制

**功能**：允许某些应用在专注模式下仍然可以使用

**数据结构**：
```kotlin
data class WhitelistApp(
    val packageName: String,                   // 应用包名
    val appName: String,                       // 应用名称
    val addedAt: Long                          // 添加时间
)

// 存储在 FocusModeEntity 中
val whitelistApps: String  // JSON: ["com.android.phone", "com.android.mms"]
```

**常见白名单应用**：
- 电话
- 短信
- 闹钟
- 日历
- 笔记应用

**判断逻辑**：
```kotlin
fun isInWhitelist(packageName: String, whitelist: List<String>): Boolean {
    return packageName in whitelist
}
```

### 3.4 专注模式判断流程

```kotlin
fun shouldBlockAppInFocusMode(
    packageName: String,
    focusMode: FocusModeEntity?
): BlockDecision {
    // 1. 检查专注模式是否激活
    if (focusMode == null || !focusMode.isEnabled) {
        return BlockDecision.Allow  // 未激活，不阻止
    }

    // 2. 检查是否在白名单中
    val whitelist = parseWhitelistApps(focusMode.whitelistApps)
    if (isInWhitelist(packageName, whitelist)) {
        return BlockDecision.Allow  // 白名单应用，允许
    }

    // 3. 检查倒计时是否结束
    if (focusMode.modeType == FocusModeType.COUNTDOWN) {
        val currentTime = System.currentTimeMillis()
        val endTime = focusMode.countdownEndTime ?: return BlockDecision.Allow

        if (currentTime >= endTime) {
            // 倒计时已结束，自动停止专注模式
            stopFocusMode()
            return BlockDecision.Allow
        }
    }

    // 4. 阻止应用
    return BlockDecision.BlockFocusMode
}
```

### 3.5 专注模式状态管理

**状态变量**：
```kotlin
data class FocusModeState(
    val isActive: Boolean,                     // 是否激活
    val modeType: FocusModeType,               // 模式类型
    val startTime: Long?,                      // 开始时间
    val endTime: Long?,                        // 结束时间（仅倒计时）
    val elapsedTime: Duration?,                // 已专注时长（仅手动）
    val remainingTime: Duration?,              // 剩余时长（仅倒计时）
    val whitelistApps: List<WhitelistApp>      // 白名单应用
)
```

**状态转换**：
```
未激活 (isActive = false)
    ↓ 用户点击"开始专注"
激活中 (isActive = true)
    ↓ 用户点击"停止专注" 或 倒计时结束
未激活 (isActive = false)
```

---

## 四、优先级系统：专注模式 vs 日常管理

### 4.1 优先级规则

```kotlin
fun shouldBlockApp(
    packageName: String,
    openedFromUMind: Boolean,
    currentTime: LocalDateTime,
    focusMode: FocusModeEntity?,
    dailyStrategies: List<Strategy>
): BlockDecision {
    // 优先级 1：检查专注模式
    if (focusMode != null && focusMode.isEnabled) {
        val focusModeDecision = shouldBlockAppInFocusMode(packageName, focusMode)
        if (focusModeDecision != BlockDecision.Allow) {
            return focusModeDecision  // 专注模式阻止，直接返回
        }
        // 专注模式允许（白名单），直接返回，不检查日常管理
        return BlockDecision.Allow
    }

    // 优先级 2：检查日常管理
    return shouldBlockAppInDailyManagement(
        packageName,
        openedFromUMind,
        currentTime,
        dailyStrategies
    )
}
```

### 4.2 优先级示例

**场景 1：专注模式激活，应用不在白名单**
```
用户尝试打开抖音
→ 检查专注模式：激活中
→ 检查白名单：不在白名单
→ 结果：阻止（专注模式）
→ 不检查日常管理规则
```

**场景 2：专注模式激活，应用在白名单**
```
用户尝试打开电话
→ 检查专注模式：激活中
→ 检查白名单：在白名单中
→ 结果：允许
→ 不检查日常管理规则
```

**场景 3：专注模式未激活**
```
用户尝试打开抖音
→ 检查专注模式：未激活
→ 检查日常管理：
    策略A：工作模式，9:00-18:00 限制抖音
    策略B：娱乐限制，抖音每天最多30分钟
→ 合并策略，检查限制
→ 结果：根据日常管理规则决定
```

---

## 五、数据模型完整定义

### 5.1 日常管理数据模型

#### Strategy (策略)
```kotlin
@Entity(tableName = "strategies")
data class StrategyEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,                          // 策略名称
    val isActive: Boolean,                     // 是否激活
    val targetApps: String,                    // 目标应用（JSON数组）
    val enforcementMode: String,               // 执行模式
    val createdAt: Long,                       // 创建时间
    val updatedAt: Long                        // 更新时间
)
```

#### TimeRestriction (时间范围限制)
```kotlin
@Entity(
    tableName = "time_restrictions",
    foreignKeys = [ForeignKey(
        entity = StrategyEntity::class,
        parentColumns = ["id"],
        childColumns = ["strategyId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TimeRestrictionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val strategyId: String,                    // 所属策略ID
    val daysOfWeek: String,                    // 星期（JSON数组）
    val startHour: Int,                        // 开始小时
    val startMinute: Int,                      // 开始分钟
    val endHour: Int,                          // 结束小时
    val endMinute: Int,                        // 结束分钟
    val isEnabled: Boolean                     // 是否启用
)
```

#### UsageLimits (使用时长限制)
```kotlin
@Entity(
    tableName = "usage_limits",
    foreignKeys = [ForeignKey(
        entity = StrategyEntity::class,
        parentColumns = ["id"],
        childColumns = ["strategyId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class UsageLimitsEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val strategyId: String,                    // 所属策略ID
    val limitType: String,                     // TOTAL_ALL, PER_APP, INDIVIDUAL
    val totalLimitMillis: Long?,               // 总时长（毫秒）
    val perAppLimitMillis: Long?,              // 每个应用时长（毫秒）
    val individualLimits: String?              // 单独设置（JSON对象）
)
```

#### OpenCountLimits (打开次数限制)
```kotlin
@Entity(
    tableName = "open_count_limits",
    foreignKeys = [ForeignKey(
        entity = StrategyEntity::class,
        parentColumns = ["id"],
        childColumns = ["strategyId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class OpenCountLimitsEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val strategyId: String,                    // 所属策略ID
    val limitType: String,                     // TOTAL_ALL, PER_APP, INDIVIDUAL
    val totalCount: Int?,                      // 总次数
    val perAppCount: Int?,                     // 每个应用次数
    val individualCounts: String?              // 单独设置（JSON对象）
)
```

### 5.2 专注模式数据模型

```kotlin
@Entity(tableName = "focus_mode")
data class FocusModeEntity(
    @PrimaryKey val id: Int = 1,               // 单例，只有一条记录
    val modeType: String,                      // MANUAL, COUNTDOWN
    val isEnabled: Boolean,                    // 是否激活
    val startTime: Long?,                      // 开始时间戳
    val countdownEndTime: Long?,               // 结束时间戳（仅倒计时）
    val countdownDuration: Long?,              // 专注时长（毫秒，仅倒计时）
    val updatedAt: Long,                       // 最后更新时间
    val whitelistApps: String                  // 白名单应用（JSON数组）
)
```

### 5.3 使用记录数据模型

#### DailyUsageRecord (每日使用记录)
```kotlin
@Entity(
    tableName = "daily_usage_records",
    indices = [Index(value = ["date", "packageName"], unique = true)]
)
data class DailyUsageRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: String,                          // 日期 (yyyy-MM-dd)
    val packageName: String,                   // 应用包名
    val appName: String,                       // 应用名称
    val totalDurationMillis: Long,             // 总使用时长（毫秒）
    val openCount: Int,                        // 打开次数
    val blockedCount: Int,                     // 被阻止次数
    val lastUpdated: Long                      // 最后更新时间
)
```

#### UsageSession (使用会话)
```kotlin
@Entity(
    tableName = "usage_sessions",
    foreignKeys = [ForeignKey(
        entity = DailyUsageRecordEntity::class,
        parentColumns = ["id"],
        childColumns = ["recordId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class UsageSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val recordId: String,                      // 所属记录ID
    val startTime: Long,                       // 会话开始时间戳
    val endTime: Long?,                        // 会话结束时间戳
    val durationMillis: Long                   // 会话时长（毫秒）
)
```

#### BlockEvent (阻止事件)
```kotlin
@Entity(tableName = "block_events")
data class BlockEventEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val packageName: String,                   // 应用包名
    val appName: String,                       // 应用名称
    val timestamp: Long,                       // 阻止时间戳
    val blockReason: String,                   // 阻止原因
    val blockSource: String,                   // 阻止来源（FOCUS_MODE, DAILY_MANAGEMENT）
    val strategyIds: String?                   // 相关策略ID（JSON数组，仅日常管理）
)
```

---

## 六、核心业务逻辑实现

### 6.1 应用拦截主流程

```kotlin
class AppBlockingService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // 忽略系统应用和本应用
        if (isSystemApp(packageName) || packageName == this.packageName) {
            return
        }

        serviceScope.launch {
            val decision = blockingEngine.shouldBlockApp(
                packageName = packageName,
                openedFromUMind = false,  // 从无障碍服务检测到的都是外部打开
                currentTime = LocalDateTime.now()
            )

            when (decision) {
                is BlockDecision.Allow -> {
                    // 允许打开，记录使用
                    usageTracker.recordAppOpen(packageName)
                }

                is BlockDecision.Block -> {
                    // 阻止打开
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    showBlockDialog(packageName, decision.reasons)
                    blockEventRecorder.recordBlock(packageName, decision)
                }

                is BlockDecision.BlockFocusMode -> {
                    // 专注模式阻止
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    showFocusModeBlockDialog(packageName)
                    blockEventRecorder.recordBlock(packageName, decision)
                }

                is BlockDecision.BlockForceThrough -> {
                    // 强制通过应用模式阻止
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    showForceThroughDialog(packageName)
                    blockEventRecorder.recordBlock(packageName, decision)
                }
            }
        }
    }
}
```

### 6.2 阻止决策引擎

```kotlin
class BlockingEngine(
    private val focusModeRepository: FocusModeRepository,
    private val strategyRepository: StrategyRepository,
    private val usageRepository: UsageRepository
) {

    suspend fun shouldBlockApp(
        packageName: String,
        openedFromUMind: Boolean,
        currentTime: LocalDateTime
    ): BlockDecision {
        // 优先级 1：检查专注模式
        val focusMode = focusModeRepository.getFocusMode()
        if (focusMode != null && focusMode.isEnabled) {
            return checkFocusMode(packageName, focusMode)
        }

        // 优先级 2：检查日常管理
        val activeStrategies = strategyRepository.getActiveStrategies()
        return checkDailyManagement(
            packageName,
            openedFromUMind,
            currentTime,
            activeStrategies
        )
    }

    private fun checkFocusMode(
        packageName: String,
        focusMode: FocusModeEntity
    ): BlockDecision {
        // 检查白名单
        val whitelist = parseWhitelistApps(focusMode.whitelistApps)
        if (packageName in whitelist) {
            return BlockDecision.Allow
        }

        // 检查倒计时是否结束
        if (focusMode.modeType == "COUNTDOWN") {
            val endTime = focusMode.countdownEndTime ?: return BlockDecision.Allow
            if (System.currentTimeMillis() >= endTime) {
                // 自动停止
                focusModeRepository.stopFocusMode()
                return BlockDecision.Allow
            }
        }

        return BlockDecision.BlockFocusMode
    }

    private suspend fun checkDailyManagement(
        packageName: String,
        openedFromUMind: Boolean,
        currentTime: LocalDateTime,
        strategies: List<Strategy>
    ): BlockDecision {
        // 筛选相关策略
        val relevantStrategies = strategies.filter {
            packageName in it.targetApps
        }

        if (relevantStrategies.isEmpty()) {
            return BlockDecision.Allow
        }

        // 合并策略
        val merged = mergeStrategies(relevantStrategies, packageName)

        // 检查执行模式
        return when (merged.enforcementMode) {
            EnforcementMode.MONITOR_ONLY -> {
                BlockDecision.Allow
            }

            EnforcementMode.DIRECT_BLOCK -> {
                val reasons = checkAllRestrictions(packageName, merged, currentTime)
                if (reasons.isNotEmpty()) {
                    BlockDecision.Block(reasons)
                } else {
                    BlockDecision.Allow
                }
            }

            EnforcementMode.FORCE_THROUGH_APP -> {
                if (!openedFromUMind) {
                    BlockDecision.BlockForceThrough
                } else {
                    val reasons = checkAllRestrictions(packageName, merged, currentTime)
                    if (reasons.isNotEmpty()) {
                        BlockDecision.Block(reasons)
                    } else {
                        BlockDecision.Allow
                    }
                }
            }
        }
    }

    private suspend fun checkAllRestrictions(
        packageName: String,
        merged: MergedRestriction,
        currentTime: LocalDateTime
    ): List<BlockReason> {
        val reasons = mutableListOf<BlockReason>()

        // 检查时间范围
        if (isWithinRestrictedTime(currentTime, merged.timeRestrictions)) {
            reasons.add(BlockReason.TimeRestriction)
        }

        // 检查使用时长
        val usageToday = usageRepository.getTodayUsage(packageName)
        if (hasExceededUsageLimit(usageToday, merged.usageLimits)) {
            reasons.add(BlockReason.UsageLimitExceeded)
        }

        // 检查打开次数
        val openCountToday = usageRepository.getTodayOpenCount(packageName)
        if (hasExceededOpenCountLimit(openCountToday, merged.openCountLimits)) {
            reasons.add(BlockReason.OpenCountExceeded)
        }

        return reasons
    }
}

sealed class BlockDecision {
    object Allow : BlockDecision()
    data class Block(val reasons: List<BlockReason>) : BlockDecision()
    object BlockFocusMode : BlockDecision()
    object BlockForceThrough : BlockDecision()
}

enum class BlockReason {
    TimeRestriction,           // 时间范围限制
    UsageLimitExceeded,        // 使用时长超限
    OpenCountExceeded          // 打开次数超限
}
```

---

## 七、统计功能

### 7.1 统计维度

#### 今日统计
- 总使用时长
- 应用使用排行
- 打开次数统计
- 限制触发次数
- 专注模式使用时长

#### 历史统计
- 按日期查看
- 周统计、月统计
- 使用趋势图表

### 7.2 统计数据查询

```kotlin
interface UsageRepository {
    // 获取今日使用记录
    suspend fun getTodayUsageRecords(): List<DailyUsageRecord>

    // 获取指定日期使用记录
    suspend fun getUsageRecordsByDate(date: LocalDate): List<DailyUsageRecord>

    // 获取日期范围内的使用记录
    suspend fun getUsageRecordsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<DailyUsageRecord>

    // 获取今日阻止事件
    suspend fun getTodayBlockEvents(): List<BlockEvent>

    // 获取专注模式历史记录
    suspend fun getFocusModeHistory(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<FocusModeSession>
}
```

---

## 八、关键业务规则总结

### 8.1 优先级规则
1. **专注模式 > 日常管理**：专注模式激活时，忽略所有日常管理规则
2. **白名单优先**：专注模式下，白名单应用始终允许

### 8.2 日常管理规则
1. **多策略并存**：允许多个策略同时激活
2. **最严格原则**：时长和次数冲突时，取最小值
3. **时间取并集**：多个时间限制取限制时间的并集
4. **应用取并集**：限制的应用列表取并集
5. **执行模式取最严格**：FORCE_THROUGH_APP > DIRECT_BLOCK > MONITOR_ONLY

### 8.3 执行模式规则
1. **仅监控**：记录但不阻止
2. **直接阻止**：在限制范围内阻止
3. **强制通过应用**：
   - 外部打开：始终阻止
   - UMind 内打开：检查限制后决定

### 8.4 数据记录规则
1. 所有使用行为都需要记录
2. 所有阻止事件都需要记录
3. 每日数据在午夜自动重置
4. 历史数据永久保存（可配置保留期限）

---

**文档版本：** v3.0
**最后更新：** 2026-02-18
**维护者：** UMind Team
