package com.example.umind.presentation.focus

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umind.data.local.entity.BlockEventEntity
import com.example.umind.data.local.entity.UsageRecordEntity
import com.example.umind.data.local.entity.UsageSessionEntity
import com.example.umind.data.repository.BlockEventRepository
import com.example.umind.data.repository.UsageTrackingRepository
import com.example.umind.domain.model.AppUsageStats
import com.example.umind.domain.model.FocusStrategy
import com.example.umind.domain.model.Result
import com.example.umind.domain.usecase.DeleteFocusStrategyUseCase
import com.example.umind.domain.usecase.GetFocusStrategiesUseCase
import com.example.umind.domain.usecase.GetInstalledAppsUseCase
import com.example.umind.domain.usecase.ToggleStrategyActiveUseCase
import com.example.umind.util.AppIconLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class FocusDetailViewModel @Inject constructor(
    private val getFocusStrategiesUseCase: GetFocusStrategiesUseCase,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val toggleStrategyActiveUseCase: ToggleStrategyActiveUseCase,
    private val deleteFocusStrategyUseCase: DeleteFocusStrategyUseCase,
    private val usageTrackingRepository: UsageTrackingRepository,
    private val blockEventRepository: BlockEventRepository,
    private val appIconLoader: AppIconLoader,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val strategyId: String = savedStateHandle.get<String>("strategyId").orEmpty()
    private val appLabelMap = MutableStateFlow<Map<String, String>>(emptyMap())
    private val selectedPeriod = MutableStateFlow(FocusStatsPeriod.DAY)

    private val _uiState = MutableStateFlow(FocusDetailUiState())
    val uiState: StateFlow<FocusDetailUiState> = _uiState.asStateFlow()

    init {
        if (strategyId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isNotFound = true,
                error = "应用组不存在"
            )
        } else {
            loadAppLabels()
            observeStrategy()
        }
    }

    private fun loadAppLabels() {
        viewModelScope.launch {
            when (val result = getInstalledAppsUseCase()) {
                is Result.Success -> {
                    appLabelMap.value = result.data.associate { app ->
                        app.packageName to app.label
                    }
                }
                is Result.Error, Result.Loading -> {}
            }
        }
    }

    private fun observeStrategy() {
        viewModelScope.launch {
            getFocusStrategiesUseCase()
                .combine(appLabelMap) { strategies, labels ->
                    strategies.find { it.id == strategyId } to labels
                }
                .combine(selectedPeriod) { (strategy, labels), period ->
                    Triple(strategy, labels, period)
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "加载详情失败"
                    )
                }
                .collectLatest { (strategy, labels, period) ->
                    if (strategy == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            strategy = null,
                            appLabelMap = labels,
                            selectedPeriod = period,
                            isNotFound = true,
                            totalUsageDurationMillis = 0L,
                            totalOpenCount = 0,
                            totalBlockCount = 0,
                            timelineEvents = emptyList(),
                            timelineTotalCount = 0,
                            appUsageStats = emptyList(),
                            trendPoints = emptyList()
                        )
                        return@collectLatest
                    }

                    try {
                        val stats = buildStats(strategy, labels, period)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            strategy = strategy,
                            appLabelMap = labels,
                            selectedPeriod = period,
                            isNotFound = false,
                            totalUsageDurationMillis = stats.totalUsageDurationMillis,
                            totalOpenCount = stats.totalOpenCount,
                            totalBlockCount = stats.totalBlockCount,
                            timelineEvents = stats.timelineEvents,
                            timelineTotalCount = stats.timelineTotalCount,
                            appUsageStats = stats.appUsageStats,
                            trendPoints = stats.trendPoints
                        )
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            strategy = strategy,
                            appLabelMap = labels,
                            selectedPeriod = period,
                            isNotFound = false,
                            error = e.message ?: "加载统计失败"
                        )
                    }
                }
        }
    }

    fun selectStatsPeriod(period: FocusStatsPeriod) {
        if (selectedPeriod.value != period) {
            selectedPeriod.value = period
            _uiState.value = _uiState.value.copy(isLoading = true)
        }
    }

    private suspend fun buildStats(
        strategy: FocusStrategy,
        labels: Map<String, String>,
        period: FocusStatsPeriod
    ): FocusStatsResult {
        val range = period.toRange(LocalDate.now())

        val usageRecords = usageTrackingRepository
            .getUsageRecordsInRange(range.startDate, range.endDate)
            .filter { it.packageName in strategy.targetApps }

        val sessions = usageTrackingRepository
            .getSessionsInRange(range.startMillis, range.endMillis)
            .filter { it.packageName in strategy.targetApps }

        val blockEvents = blockEventRepository
            .getBlockEventsInRange(range.startMillis, range.endMillis)
            .filter { event ->
                event.packageName in strategy.targetApps &&
                    event.blockSource == "DAILY_MANAGEMENT"
            }

        val usageByPackage = usageRecords.groupBy { it.packageName }
        val usageDurationByPackage = usageByPackage.mapValues { (_, records) ->
            records.sumOf { it.usageDurationMillis }
        }
        val openCountByPackage = usageByPackage.mapValues { (_, records) ->
            records.sumOf { it.openCount }
        }
        val blockCountByPackage = blockEvents.groupingBy { it.packageName }.eachCount()
        val fallbackAppNames = blockEvents
            .asReversed()
            .associate { it.packageName to it.appName }

        val appUsageStats = strategy.targetApps.mapNotNull { packageName ->
            val usageDuration = usageDurationByPackage[packageName] ?: 0L
            val openCount = openCountByPackage[packageName] ?: 0
            val blockCount = blockCountByPackage[packageName] ?: 0
            if (usageDuration == 0L && openCount == 0 && blockCount == 0) {
                return@mapNotNull null
            }
            AppUsageStats(
                packageName = packageName,
                appName = labels[packageName] ?: fallbackAppNames[packageName] ?: packageName,
                usageDurationMillis = usageDuration,
                openCount = openCount,
                blockCount = blockCount
            )
        }.sortedWith(
            compareByDescending<AppUsageStats> { it.usageDurationMillis }
                .thenByDescending { it.openCount }
                .thenBy { it.appName }
        )

        val timelineEvents = buildTimelineEvents(
            sessions = sessions,
            blockEvents = blockEvents,
            labels = labels,
            fallbackAppNames = fallbackAppNames
        )
        val timelineLimited = timelineEvents.take(300)

        val trendPoints = buildTrendPoints(
            usageRecords = usageRecords,
            blockEvents = blockEvents,
            range = range
        )

        return FocusStatsResult(
            totalUsageDurationMillis = usageRecords.sumOf { it.usageDurationMillis },
            totalOpenCount = usageRecords.sumOf { it.openCount },
            totalBlockCount = blockEvents.size,
            timelineEvents = timelineLimited,
            timelineTotalCount = timelineEvents.size,
            appUsageStats = appUsageStats,
            trendPoints = trendPoints
        )
    }

    private fun buildTimelineEvents(
        sessions: List<UsageSessionEntity>,
        blockEvents: List<BlockEventEntity>,
        labels: Map<String, String>,
        fallbackAppNames: Map<String, String>
    ): List<FocusTimelineEvent> {
        val sessionEvents = sessions.flatMap { session ->
            val appName = labels[session.packageName] ?: fallbackAppNames[session.packageName] ?: session.packageName
            buildList {
                add(
                    FocusTimelineEvent(
                        timestamp = session.startTime,
                        packageName = session.packageName,
                        appName = appName,
                        type = FocusTimelineEventType.APP_OPEN
                    )
                )
                session.endTime?.let { endTime ->
                    add(
                        FocusTimelineEvent(
                            timestamp = endTime,
                            packageName = session.packageName,
                            appName = appName,
                            type = FocusTimelineEventType.APP_CLOSE,
                            durationMillis = session.durationMillis
                        )
                    )
                }
            }
        }

        val blockTimelineEvents = blockEvents.map { event ->
            FocusTimelineEvent(
                timestamp = event.timestamp,
                packageName = event.packageName,
                appName = labels[event.packageName] ?: event.appName,
                type = FocusTimelineEventType.APP_BLOCKED
            )
        }

        return (sessionEvents + blockTimelineEvents)
            .sortedByDescending { it.timestamp }
    }

    private fun buildTrendPoints(
        usageRecords: List<UsageRecordEntity>,
        blockEvents: List<BlockEventEntity>,
        range: FocusStatsRange
    ): List<FocusTrendPoint> {
        val usageByDate = usageRecords.groupBy { it.date }
        val blockByDate = blockEvents.groupBy { event ->
            Instant.ofEpochMilli(event.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }

        val points = mutableListOf<FocusTrendPoint>()
        var currentDate = range.startDate
        while (!currentDate.isAfter(range.endDate)) {
            val dateUsage = usageByDate[currentDate].orEmpty()
            points.add(
                FocusTrendPoint(
                    date = currentDate,
                    totalUsageDurationMillis = dateUsage.sumOf { it.usageDurationMillis },
                    totalOpenCount = dateUsage.sumOf { it.openCount },
                    totalBlockCount = blockByDate[currentDate]?.size ?: 0
                )
            )
            currentDate = currentDate.plusDays(1)
        }

        return points
    }

    fun toggleStrategyActive(isActive: Boolean) {
        val strategy = _uiState.value.strategy ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdatingActive = true,
                error = null
            )
            when (val result = toggleStrategyActiveUseCase(strategy.id, isActive)) {
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdatingActive = false,
                        error = result.message ?: "更新状态失败"
                    )
                }
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isUpdatingActive = false)
                }
                Result.Loading -> {}
            }
        }
    }

    fun deleteStrategy(onSuccess: () -> Unit) {
        val targetId = _uiState.value.strategy?.id ?: strategyId
        if (targetId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "应用组不存在")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                error = null
            )
            when (val result = deleteFocusStrategyUseCase(targetId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isDeleting = false)
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = result.message ?: "删除失败"
                    )
                }
                Result.Loading -> {}
            }
        }
    }

    fun getIconForApp(packageName: String): Bitmap? {
        val cachedIcons = _uiState.value.loadedIcons
        if (cachedIcons.containsKey(packageName)) {
            return cachedIcons[packageName]
        }

        viewModelScope.launch {
            loadIconForApp(packageName)
        }

        return null
    }

    private suspend fun loadIconForApp(packageName: String): Bitmap? {
        val cachedIcons = _uiState.value.loadedIcons
        if (cachedIcons.containsKey(packageName)) {
            return cachedIcons[packageName]
        }

        val icon = appIconLoader.loadIcon(packageName)
        _uiState.value = _uiState.value.copy(
            loadedIcons = _uiState.value.loadedIcons + (packageName to icon)
        )
        return icon
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class FocusDetailUiState(
    val isLoading: Boolean = true,
    val strategy: FocusStrategy? = null,
    val appLabelMap: Map<String, String> = emptyMap(),
    val loadedIcons: Map<String, Bitmap?> = emptyMap(),
    val selectedPeriod: FocusStatsPeriod = FocusStatsPeriod.DAY,
    val totalUsageDurationMillis: Long = 0L,
    val totalOpenCount: Int = 0,
    val totalBlockCount: Int = 0,
    val timelineEvents: List<FocusTimelineEvent> = emptyList(),
    val timelineTotalCount: Int = 0,
    val appUsageStats: List<AppUsageStats> = emptyList(),
    val trendPoints: List<FocusTrendPoint> = emptyList(),
    val isNotFound: Boolean = false,
    val isUpdatingActive: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null
)

enum class FocusStatsPeriod(val label: String) {
    DAY("天"),
    WEEK("周"),
    MONTH("月")
}

data class FocusTimelineEvent(
    val timestamp: Long,
    val packageName: String,
    val appName: String,
    val type: FocusTimelineEventType,
    val durationMillis: Long = 0L
)

enum class FocusTimelineEventType {
    APP_OPEN,
    APP_CLOSE,
    APP_BLOCKED
}

data class FocusTrendPoint(
    val date: LocalDate,
    val totalUsageDurationMillis: Long,
    val totalOpenCount: Int,
    val totalBlockCount: Int
)

private data class FocusStatsResult(
    val totalUsageDurationMillis: Long,
    val totalOpenCount: Int,
    val totalBlockCount: Int,
    val timelineEvents: List<FocusTimelineEvent>,
    val timelineTotalCount: Int,
    val appUsageStats: List<AppUsageStats>,
    val trendPoints: List<FocusTrendPoint>
)

private data class FocusStatsRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val startMillis: Long,
    val endMillis: Long
)

private fun FocusStatsPeriod.toRange(anchorDate: LocalDate): FocusStatsRange {
    val zoneId = ZoneId.systemDefault()
    val startDate = when (this) {
        FocusStatsPeriod.DAY -> anchorDate
        FocusStatsPeriod.WEEK -> anchorDate.minusDays(6)
        FocusStatsPeriod.MONTH -> anchorDate.withDayOfMonth(1)
    }
    val endDate = anchorDate
    val startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endMillis = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
    return FocusStatsRange(
        startDate = startDate,
        endDate = endDate,
        startMillis = startMillis,
        endMillis = endMillis
    )
}
