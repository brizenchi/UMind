# UMind 项目架构设计文档 v3.0

## 目录
1. [架构概览](#架构概览)
2. [系统分层设计](#系统分层设计)
3. [数据模型设计](#数据模型设计)
4. [核心业务逻辑](#核心业务逻辑)
5. [依赖注入](#依赖注入)
6. [性能优化](#性能优化)

---

## 一、架构概览

### 1.1 整体架构

UMind 采用 **Clean Architecture + MVVM** 架构模式，分为三层：

```
┌─────────────────────────────────────────────────┐
│            Presentation Layer (表现层)           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │ Screen   │  │ViewModel │  │ Compose  │      │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘      │
│       └─────────────┼─────────────┘             │
└─────────────────────┼──────────────────────────┘
                      │
┌─────────────────────┼──────────────────────────┐
│              ┌──────▼──────┐   Domain Layer     │
│              │  Use Cases  │   (领域层)         │
│              └──────┬──────┘                     │
│                     │                            │
│              ┌──────▼──────┐                     │
│              │ Repository  │ (Interface)         │
│              └──────┬──────┘                     │
└─────────────────────┼──────────────────────────┘
                      │
┌─────────────────────┼──────────────────────────┐
│              ┌──────▼──────┐    Data Layer      │
│              │ Repository  │    (数据层)        │
│              │     Impl    │                     │
│              └──────┬──────┘                     │
│  ┌───────────┼─────────────┼───────────┐        │
│  │           │             │           │        │
│  ▼           ▼             ▼           ▼        │
│ Room DB    DAO          Entity    Accessibility│
│                                      Service    │
└─────────────────────────────────────────────────┘
```

### 1.2 项目结构

```
app/src/main/java/com/example/umind/
├── UMindApplication.kt              # Hilt Application
├── MainActivity.kt                  # 主 Activity
│
├── domain/                          # 领域层
│   ├── model/                       # 领域模型
│   │   ├── Strategy.kt             # 策略模型
│   │   ├── FocusMode.kt            # 专注模式模型
│   │   ├── UsageRecord.kt          # 使用记录模型
│   │   └── BlockDecision.kt        # 阻止决策模型
│   │
│   ├── repository/                  # 仓库接口
│   │   ├── StrategyRepository.kt
│   │   ├── FocusModeRepository.kt
│   │   └── UsageRepository.kt
│   │
│   └── usecase/                     # 用例（业务逻辑）
│       ├── strategy/
│       │   ├── GetActiveStrategiesUseCase.kt
│       │   ├── SaveStrategyUseCase.kt
│       │   └── DeleteStrategyUseCase.kt
│       ├── focusmode/
│       │   ├── StartFocusModeUseCase.kt
│       │   └── StopFocusModeUseCase.kt
│       └── blocking/
│           └── ShouldBlockAppUseCase.kt
│
├── data/                            # 数据层
│   ├── local/                       # 本地数据源
│   │   ├── entity/                  # Room 实体
│   │   │   ├── StrategyEntity.kt
│   │   │   ├── TimeRestrictionEntity.kt
│   │   │   ├── UsageLimitsEntity.kt
│   │   │   ├── OpenCountLimitsEntity.kt
│   │   │   ├── FocusModeEntity.kt
│   │   │   ├── DailyUsageRecordEntity.kt
│   │   │   └── BlockEventEntity.kt
│   │   │
│   │   ├── dao/                     # 数据访问对象
│   │   │   ├── StrategyDao.kt
│   │   │   ├── FocusModeDao.kt
│   │   │   └── UsageDao.kt
│   │   │
│   │   └── database/                # 数据库
│   │       └── UMindDatabase.kt
│   │
│   └── repository/                  # 仓库实现
│       ├── StrategyRepositoryImpl.kt
│       ├── FocusModeRepositoryImpl.kt
│       └── UsageRepositoryImpl.kt
│
├── presentation/                    # 表现层
│   ├── dailymanagement/            # 日常管理模块
│   │   ├── DailyManagementScreen.kt
│   │   ├── DailyManagementViewModel.kt
│   │   ├── StrategyEditScreen.kt
│   │   └── StrategyEditViewModel.kt
│   │
│   ├── focusmode/                  # 专注模式模块
│   │   ├── FocusModeScreen.kt
│   │   └── FocusModeViewModel.kt
│   │
│   ├── stats/                      # 统计模块
│   │   ├── StatsScreen.kt
│   │   └── StatsViewModel.kt
│   │
│   └── settings/                   # 设置模块
│       ├── SettingsScreen.kt
│       └── SettingsViewModel.kt
│
├── service/                        # 服务层
│   ├── BlockAccessibilityService.kt    # 无障碍服务
│   ├── BlockingEngine.kt               # 阻止决策引擎
│   └── UsageTracker.kt                 # 使用追踪器
│
├── di/                             # 依赖注入模块
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── ServiceModule.kt
│
└── ui/                             # UI 组件和主题
    ├── components/                 # 可复用组件
    └── theme/                      # 主题定义
```

---

## 二、系统分层设计

### 2.1 Domain Layer (领域层)

#### 职责
- 定义业务逻辑和规则
- 不依赖任何框架
- 包含领域模型、仓库接口、用例

#### 领域模型

**Strategy (策略)**
```kotlin
data class Strategy(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val targetApps: List<String>,
    val timeRestrictions: List<TimeRestriction>,
    val usageLimits: UsageLimits?,
    val openCountLimits: OpenCountLimits?,
    val enforcementMode: EnforcementMode,
    val createdAt: Long,
    val updatedAt: Long
)

data class TimeRestriction(
    val id: String,
    val daysOfWeek: Set<DayOfWeek>,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isEnabled: Boolean
)

data class UsageLimits(
    val limitType: UsageLimitType,
    val totalLimit: Duration?,
    val perAppLimit: Duration?,
    val individualLimits: Map<String, Duration>?
)

data class OpenCountLimits(
    val limitType: OpenCountLimitType,
    val totalCount: Int?,
    val perAppCount: Int?,
    val individualCounts: Map<String, Int>?
)

enum class EnforcementMode {
    MONITOR_ONLY,
    DIRECT_BLOCK,
    FORCE_THROUGH_APP
}
```

**FocusMode (专注模式)**
```kotlin
data class FocusMode(
    val id: Int = 1,
    val modeType: FocusModeType,
    val isEnabled: Boolean,
    val startTime: Long?,
    val countdownEndTime: Long?,
    val countdownDuration: Long?,
    val updatedAt: Long,
    val whitelistApps: List<String>
) {
    fun shouldBeActive(): Boolean {
        if (!isEnabled) return false

        return when (modeType) {
            FocusModeType.MANUAL -> true
            FocusModeType.COUNTDOWN -> {
                val endTime = countdownEndTime ?: return false
                System.currentTimeMillis() < endTime
            }
        }
    }

    fun getElapsedTime(): Duration? {
        if (!isEnabled || startTime == null) return null
        return Duration.ofMillis(System.currentTimeMillis() - startTime)
    }

    fun getRemainingTime(): Duration? {
        if (!isEnabled || modeType != FocusModeType.COUNTDOWN) return null
        val endTime = countdownEndTime ?: return null
        val remaining = endTime - System.currentTimeMillis()
        return Duration.ofMillis(remaining.coerceAtLeast(0))
    }
}

enum class FocusModeType {
    MANUAL,
    COUNTDOWN
}
```

**BlockDecision (阻止决策)**
```kotlin
sealed class BlockDecision {
    object Allow : BlockDecision()
    data class Block(val reasons: List<BlockReason>) : BlockDecision()
    object BlockFocusMode : BlockDecision()
    object BlockForceThrough : BlockDecision()
}

enum class BlockReason {
    TIME_RESTRICTION,
    USAGE_LIMIT_EXCEEDED,
    OPEN_COUNT_EXCEEDED
}
```

#### 仓库接口

```kotlin
interface StrategyRepository {
    fun getStrategiesFlow(): Flow<List<Strategy>>
    suspend fun getActiveStrategies(): List<Strategy>
    suspend fun getStrategyById(id: String): Strategy?
    suspend fun saveStrategy(strategy: Strategy)
    suspend fun deleteStrategy(id: String)
    suspend fun setStrategyActive(id: String, isActive: Boolean)
}

interface FocusModeRepository {
    fun getFocusModeFlow(): Flow<FocusMode?>
    suspend fun getFocusMode(): FocusMode?
    suspend fun startManualMode(whitelistApps: List<String>)
    suspend fun startCountdownMode(duration: Long, whitelistApps: List<String>)
    suspend fun stopFocusMode()
    suspend fun updateWhitelist(whitelistApps: List<String>)
}

interface UsageRepository {
    suspend fun getTodayUsage(packageName: String): Duration
    suspend fun getTodayOpenCount(packageName: String): Int
    suspend fun recordAppOpen(packageName: String, timestamp: Long)
    suspend fun recordAppClose(packageName: String, timestamp: Long, duration: Long)
    suspend fun recordBlockEvent(event: BlockEvent)
    suspend fun getTodayUsageRecords(): List<DailyUsageRecord>
    suspend fun getTodayBlockEvents(): List<BlockEvent>
}
```

#### 用例

```kotlin
class ShouldBlockAppUseCase @Inject constructor(
    private val focusModeRepository: FocusModeRepository,
    private val strategyRepository: StrategyRepository,
    private val usageRepository: UsageRepository,
    private val blockingEngine: BlockingEngine
) {
    suspend operator fun invoke(
        packageName: String,
        openedFromUMind: Boolean
    ): BlockDecision {
        return blockingEngine.shouldBlockApp(
            packageName = packageName,
            openedFromUMind = openedFromUMind,
            currentTime = LocalDateTime.now()
        )
    }
}
```

### 2.2 Data Layer (数据层)

#### Room Database

```kotlin
@Database(
    entities = [
        StrategyEntity::class,
        TimeRestrictionEntity::class,
        UsageLimitsEntity::class,
        OpenCountLimitsEntity::class,
        FocusModeEntity::class,
        DailyUsageRecordEntity::class,
        UsageSessionEntity::class,
        BlockEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UMindDatabase : RoomDatabase() {
    abstract fun strategyDao(): StrategyDao
    abstract fun focusModeDao(): FocusModeDao
    abstract fun usageDao(): UsageDao

    companion object {
        const val DATABASE_NAME = "umind_database"
    }
}
```

#### DAO 示例

```kotlin
@Dao
interface StrategyDao {
    @Query("SELECT * FROM strategies ORDER BY createdAt DESC")
    fun getAllStrategiesFlow(): Flow<List<StrategyEntity>>

    @Query("SELECT * FROM strategies WHERE isActive = 1")
    suspend fun getActiveStrategies(): List<StrategyEntity>

    @Query("SELECT * FROM strategies WHERE id = :id")
    suspend fun getStrategyById(id: String): StrategyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrategy(strategy: StrategyEntity)

    @Delete
    suspend fun deleteStrategy(strategy: StrategyEntity)

    @Query("UPDATE strategies SET isActive = :isActive WHERE id = :id")
    suspend fun setStrategyActive(id: String, isActive: Boolean)

    // 获取策略的所有限制规则
    @Transaction
    @Query("SELECT * FROM strategies WHERE id = :strategyId")
    suspend fun getStrategyWithRestrictions(strategyId: String): StrategyWithRestrictions
}

data class StrategyWithRestrictions(
    @Embedded val strategy: StrategyEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "strategyId"
    )
    val timeRestrictions: List<TimeRestrictionEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "strategyId"
    )
    val usageLimits: UsageLimitsEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "strategyId"
    )
    val openCountLimits: OpenCountLimitsEntity?
)
```

#### Repository Implementation

```kotlin
@Singleton
class StrategyRepositoryImpl @Inject constructor(
    private val strategyDao: StrategyDao
) : StrategyRepository {

    override fun getStrategiesFlow(): Flow<List<Strategy>> {
        return strategyDao.getAllStrategiesFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getActiveStrategies(): List<Strategy> {
        return strategyDao.getActiveStrategies().map { it.toDomainModel() }
    }

    override suspend fun saveStrategy(strategy: Strategy) {
        val entity = StrategyEntity.fromDomainModel(strategy)
        strategyDao.insertStrategy(entity)

        // 保存关联的限制规则
        strategy.timeRestrictions.forEach { restriction ->
            val restrictionEntity = TimeRestrictionEntity.fromDomainModel(
                restriction,
                strategy.id
            )
            strategyDao.insertTimeRestriction(restrictionEntity)
        }

        strategy.usageLimits?.let { limits ->
            val limitsEntity = UsageLimitsEntity.fromDomainModel(limits, strategy.id)
            strategyDao.insertUsageLimits(limitsEntity)
        }

        strategy.openCountLimits?.let { limits ->
            val limitsEntity = OpenCountLimitsEntity.fromDomainModel(limits, strategy.id)
            strategyDao.insertOpenCountLimits(limitsEntity)
        }
    }
}
```

### 2.3 Presentation Layer (表现层)

#### ViewModel 示例

```kotlin
@HiltViewModel
class DailyManagementViewModel @Inject constructor(
    private val getActiveStrategiesUseCase: GetActiveStrategiesUseCase,
    private val saveStrategyUseCase: SaveStrategyUseCase,
    private val deleteStrategyUseCase: DeleteStrategyUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DailyManagementUiState>(
        DailyManagementUiState.Loading
    )
    val uiState: StateFlow<DailyManagementUiState> = _uiState.asStateFlow()

    init {
        loadStrategies()
    }

    private fun loadStrategies() {
        viewModelScope.launch {
            getActiveStrategiesUseCase()
                .catch { e ->
                    _uiState.value = DailyManagementUiState.Error(
                        e.message ?: "加载失败"
                    )
                }
                .collect { strategies ->
                    _uiState.value = if (strategies.isEmpty()) {
                        DailyManagementUiState.Empty
                    } else {
                        DailyManagementUiState.Success(strategies)
                    }
                }
        }
    }

    fun saveStrategy(strategy: Strategy) {
        viewModelScope.launch {
            try {
                saveStrategyUseCase(strategy)
            } catch (e: Exception) {
                _uiState.value = DailyManagementUiState.Error(
                    e.message ?: "保存失败"
                )
            }
        }
    }
}

sealed class DailyManagementUiState {
    object Loading : DailyManagementUiState()
    object Empty : DailyManagementUiState()
    data class Success(val strategies: List<Strategy>) : DailyManagementUiState()
    data class Error(val message: String) : DailyManagementUiState()
}
```

---

## 三、核心业务逻辑

### 3.1 阻止决策引擎 (BlockingEngine)

```kotlin
@Singleton
class BlockingEngine @Inject constructor(
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
        if (focusMode != null && focusMode.shouldBeActive()) {
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
        focusMode: FocusMode
    ): BlockDecision {
        // 检查白名单
        if (packageName in focusMode.whitelistApps) {
            return BlockDecision.Allow
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
            reasons.add(BlockReason.TIME_RESTRICTION)
        }

        // 检查使用时长
        val usageToday = usageRepository.getTodayUsage(packageName)
        if (hasExceededUsageLimit(usageToday, merged.usageLimits)) {
            reasons.add(BlockReason.USAGE_LIMIT_EXCEEDED)
        }

        // 检查打开次数
        val openCountToday = usageRepository.getTodayOpenCount(packageName)
        if (hasExceededOpenCountLimit(openCountToday, merged.openCountLimits)) {
            reasons.add(BlockReason.OPEN_COUNT_EXCEEDED)
        }

        return reasons
    }

    private fun mergeStrategies(
        strategies: List<Strategy>,
        packageName: String
    ): MergedRestriction {
        // 合并时间限制（取并集）
        val mergedTimeRestrictions = strategies
            .flatMap { it.timeRestrictions }
            .filter { it.isEnabled }

        // 合并使用时长限制（取最小值）
        val mergedUsageLimit = strategies
            .mapNotNull { it.usageLimits?.getLimitFor(packageName) }
            .minOrNull()

        // 合并打开次数限制（取最小值）
        val mergedOpenCountLimit = strategies
            .mapNotNull { it.openCountLimits?.getLimitFor(packageName) }
            .minOrNull()

        // 确定执行模式（取最严格）
        val enforcementMode = strategies
            .map { it.enforcementMode }
            .maxByOrNull { it.strictness } ?: EnforcementMode.MONITOR_ONLY

        return MergedRestriction(
            timeRestrictions = mergedTimeRestrictions,
            usageLimit = mergedUsageLimit,
            openCountLimit = mergedOpenCountLimit,
            enforcementMode = enforcementMode
        )
    }
}

data class MergedRestriction(
    val timeRestrictions: List<TimeRestriction>,
    val usageLimit: Duration?,
    val openCountLimit: Int?,
    val enforcementMode: EnforcementMode
)

val EnforcementMode.strictness: Int
    get() = when (this) {
        EnforcementMode.MONITOR_ONLY -> 1
        EnforcementMode.DIRECT_BLOCK -> 2
        EnforcementMode.FORCE_THROUGH_APP -> 3
    }
```

### 3.2 无障碍服务 (BlockAccessibilityService)

```kotlin
@AndroidEntryPoint
class BlockAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var blockingEngine: BlockingEngine

    @Inject
    lateinit var usageTracker: UsageTracker

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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
                openedFromUMind = false
            )

            handleBlockDecision(packageName, decision)
        }
    }

    private fun handleBlockDecision(packageName: String, decision: BlockDecision) {
        when (decision) {
            is BlockDecision.Allow -> {
                usageTracker.recordAppOpen(packageName)
            }

            is BlockDecision.Block -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
                showBlockDialog(packageName, decision.reasons)
            }

            is BlockDecision.BlockFocusMode -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
                showFocusModeBlockDialog(packageName)
            }

            is BlockDecision.BlockForceThrough -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
                showForceThroughDialog(packageName)
            }
        }
    }

    override fun onInterrupt() {
        // Handle interruption
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
```

---

## 四、依赖注入

### 4.1 Hilt 配置

```kotlin
@HiltAndroidApp
class UMindApplication : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity()

@AndroidEntryPoint
class BlockAccessibilityService : AccessibilityService()
```

### 4.2 Hilt 模块

**DatabaseModule**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideUMindDatabase(
        @ApplicationContext context: Context
    ): UMindDatabase {
        return Room.databaseBuilder(
            context,
            UMindDatabase::class.java,
            UMindDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideStrategyDao(database: UMindDatabase): StrategyDao {
        return database.strategyDao()
    }

    @Provides
    @Singleton
    fun provideFocusModeDao(database: UMindDatabase): FocusModeDao {
        return database.focusModeDao()
    }

    @Provides
    @Singleton
    fun provideUsageDao(database: UMindDatabase): UsageDao {
        return database.usageDao()
    }
}
```

**RepositoryModule**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStrategyRepository(
        impl: StrategyRepositoryImpl
    ): StrategyRepository

    @Binds
    @Singleton
    abstract fun bindFocusModeRepository(
        impl: FocusModeRepositoryImpl
    ): FocusModeRepository

    @Binds
    @Singleton
    abstract fun bindUsageRepository(
        impl: UsageRepositoryImpl
    ): UsageRepository
}
```

---

## 五、性能优化

### 5.1 数据库优化

- 使用索引加速查询
- 批量操作使用事务
- 使用 Flow 避免重复查询

```kotlin
@Entity(
    tableName = "strategies",
    indices = [
        Index(value = ["isActive"]),
        Index(value = ["createdAt"])
    ]
)
```

### 5.2 内存优化

- 使用 Bitmap 缓存应用图标
- 及时释放不用的资源
- 避免内存泄漏

### 5.3 UI 优化

- 使用 LazyColumn 实现列表虚拟化
- 避免过度重组
- 使用 remember 缓存计算结果

---

## 六、最佳实践

### ✅ 应该做的

1. 使用 Clean Architecture：清晰的分层架构
2. 依赖注入：使用 Hilt 管理依赖
3. 响应式编程：使用 Flow 处理数据流
4. 错误处理：统一的 Result 类型
5. 状态管理：明确的 UI 状态
6. 测试：编写单元测试和集成测试

### ❌ 不应该做的

1. 不要在 UI 层直接访问数据库
2. 不要在 ViewModel 中持有 Context
3. 不要在主线程执行耗时操作
4. 不要忽略错误处理
5. 不要过度设计：保持简单

---

**文档版本：** v3.0
**最后更新：** 2026-02-18
**维护者：** UMind Team
