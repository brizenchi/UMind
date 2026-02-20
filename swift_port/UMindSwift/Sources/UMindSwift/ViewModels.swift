import Foundation

public enum FocusListUiState: Sendable {
    case loading
    case empty
    case success([FocusStrategy])
    case error(String)
}

public struct FocusEditUiState: Sendable {
    public var strategyId: String?
    public var name: String
    public var selectedPackages: Set<String>
    public var timeRestrictions: [TimeRestriction]
    public var usageLimits: UsageLimits?
    public var openCountLimits: OpenCountLimits?
    public var enforcementMode: EnforcementMode
    public var isActive: Bool
    public var isSaving: Bool
    public var hasUnsavedChanges: Bool
    public var error: String?

    public init(
        strategyId: String? = nil,
        name: String = "",
        selectedPackages: Set<String> = [],
        timeRestrictions: [TimeRestriction] = [],
        usageLimits: UsageLimits? = nil,
        openCountLimits: OpenCountLimits? = nil,
        enforcementMode: EnforcementMode = .directBlock,
        isActive: Bool = true,
        isSaving: Bool = false,
        hasUnsavedChanges: Bool = false,
        error: String? = nil
    ) {
        self.strategyId = strategyId
        self.name = name
        self.selectedPackages = selectedPackages
        self.timeRestrictions = timeRestrictions
        self.usageLimits = usageLimits
        self.openCountLimits = openCountLimits
        self.enforcementMode = enforcementMode
        self.isActive = isActive
        self.isSaving = isSaving
        self.hasUnsavedChanges = hasUnsavedChanges
        self.error = error
    }
}

public struct AppSelectionUiState: Sendable {
    public var isLoading: Bool
    public var searchQuery: String
    public var filteredApps: [AppInfo]
    public var selectedPackages: Set<String>
    public var error: String?

    public init(
        isLoading: Bool = false,
        searchQuery: String = "",
        filteredApps: [AppInfo] = [],
        selectedPackages: Set<String> = [],
        error: String? = nil
    ) {
        self.isLoading = isLoading
        self.searchQuery = searchQuery
        self.filteredApps = filteredApps
        self.selectedPackages = selectedPackages
        self.error = error
    }
}

public enum StatsUiState: Sendable {
    case loading
    case success(dailyStats: DailyStats, weeklyTrend: [UsageTrend])
    case error(String)
}

public struct SettingsUiState: Sendable {
    public var todayRecords: [UsageRecordEntity]
    public var debugMessage: String

    public init(todayRecords: [UsageRecordEntity] = [], debugMessage: String = "") {
        self.todayRecords = todayRecords
        self.debugMessage = debugMessage
    }
}

@MainActor
public final class FocusEditViewModel {
    private let getFocusStrategiesUseCase: GetFocusStrategiesUseCase
    private let saveFocusStrategyUseCase: SaveFocusStrategyUseCase
    private let strategyId: String?

    public private(set) var uiState = FocusEditUiState()

    public init(
        getFocusStrategiesUseCase: GetFocusStrategiesUseCase,
        saveFocusStrategyUseCase: SaveFocusStrategyUseCase,
        strategyId: String? = nil
    ) {
        self.getFocusStrategiesUseCase = getFocusStrategiesUseCase
        self.saveFocusStrategyUseCase = saveFocusStrategyUseCase
        self.strategyId = strategyId

        if let strategyId {
            Task { await self.loadStrategy(id: strategyId) }
        }
    }

    public func loadStrategy(id: String) async {
        let result = await getFocusStrategiesUseCase.execute()
        switch result {
        case let .success(strategies):
            if let strategy = strategies.first(where: { $0.id == id }) {
                uiState.strategyId = strategy.id
                uiState.name = strategy.name
                uiState.selectedPackages = strategy.targetApps
                uiState.timeRestrictions = strategy.timeRestrictions
                uiState.usageLimits = strategy.usageLimits
                uiState.openCountLimits = strategy.openCountLimits
                uiState.enforcementMode = strategy.enforcementMode
                uiState.isActive = strategy.isActive
            }
        case let .error(_, message):
            uiState.error = message ?? "加载策略失败"
        case .loading:
            break
        }
    }

    public func updateName(name: String) {
        uiState.name = name
        uiState.hasUnsavedChanges = true
    }

    public func updateSelectedPackages(packages: Set<String>) {
        uiState.selectedPackages = packages
        uiState.hasUnsavedChanges = true
    }

    public func addTimeRestriction(daysOfWeek: Set<DayOfWeek>, startTime: ClockTime, endTime: ClockTime) {
        uiState.timeRestrictions.append(
            TimeRestriction(id: UUID().uuidString, daysOfWeek: daysOfWeek, startTime: startTime, endTime: endTime)
        )
        uiState.hasUnsavedChanges = true
    }

    public func updateTimeRestriction(
        restrictionId: String,
        daysOfWeek: Set<DayOfWeek>,
        startTime: ClockTime,
        endTime: ClockTime
    ) {
        uiState.timeRestrictions = uiState.timeRestrictions.map { restriction in
            guard restriction.id == restrictionId else { return restriction }
            return TimeRestriction(id: restriction.id, daysOfWeek: daysOfWeek, startTime: startTime, endTime: endTime)
        }
        uiState.hasUnsavedChanges = true
    }

    public func removeTimeRestriction(restrictionId: String) {
        uiState.timeRestrictions.removeAll(where: { $0.id == restrictionId })
        uiState.hasUnsavedChanges = true
    }

    public func updateUsageLimits(limits: UsageLimits?) {
        uiState.usageLimits = limits
        uiState.hasUnsavedChanges = true
    }

    public func setUsageLimitsTotalAll(hours: Int, minutes: Int) {
        let millis = Milliseconds((hours * 60 + minutes) * 60_000)
        uiState.usageLimits = UsageLimits(type: .totalAll, totalLimitMillis: millis)
        uiState.hasUnsavedChanges = true
    }

    public func setUsageLimitsPerApp(hours: Int, minutes: Int) {
        let millis = Milliseconds((hours * 60 + minutes) * 60_000)
        uiState.usageLimits = UsageLimits(type: .perApp, perAppLimitMillis: millis)
        uiState.hasUnsavedChanges = true
    }

    public func setUsageLimitsIndividual(limits: [String: Milliseconds]) {
        uiState.usageLimits = UsageLimits(type: .individual, individualLimitsMillis: limits)
        uiState.hasUnsavedChanges = true
    }

    public func updateOpenCountLimits(limits: OpenCountLimits?) {
        uiState.openCountLimits = limits
        uiState.hasUnsavedChanges = true
    }

    public func setOpenCountLimitsTotalAll(count: Int) {
        uiState.openCountLimits = OpenCountLimits(type: .totalAll, totalCount: count)
        uiState.hasUnsavedChanges = true
    }

    public func setOpenCountLimitsPerApp(count: Int) {
        uiState.openCountLimits = OpenCountLimits(type: .perApp, perAppCount: count)
        uiState.hasUnsavedChanges = true
    }

    public func setOpenCountLimitsIndividual(counts: [String: Int]) {
        uiState.openCountLimits = OpenCountLimits(type: .individual, individualCounts: counts)
        uiState.hasUnsavedChanges = true
    }

    public func updateEnforcementMode(mode: EnforcementMode) {
        uiState.enforcementMode = mode
        uiState.hasUnsavedChanges = true
    }

    public func updateIsActive(isActive: Bool) {
        uiState.isActive = isActive
        uiState.hasUnsavedChanges = true
    }

    public func saveStrategy(onSuccess: @escaping () -> Void = {}) async {
        let strategy = FocusStrategy(
            id: uiState.strategyId ?? UUID().uuidString,
            name: uiState.name,
            targetApps: uiState.selectedPackages,
            timeRestrictions: uiState.timeRestrictions,
            usageLimits: uiState.usageLimits,
            openCountLimits: uiState.openCountLimits,
            enforcementMode: uiState.enforcementMode,
            isActive: uiState.isActive
        )

        uiState.isSaving = true
        let result = await saveFocusStrategyUseCase.invoke(strategy: strategy)
        switch result {
        case .success:
            uiState.isSaving = false
            uiState.hasUnsavedChanges = false
            onSuccess()
        case let .error(_, message):
            uiState.isSaving = false
            uiState.error = message ?? "保存失败"
        case .loading:
            break
        }
    }

    public func clearError() {
        uiState.error = nil
    }
}

@MainActor
public final class FocusListViewModel {
    private let getFocusStrategiesUseCase: GetFocusStrategiesUseCase
    private let toggleStrategyActiveUseCase: ToggleStrategyActiveUseCase
    private let deleteFocusStrategyUseCase: DeleteFocusStrategyUseCase

    public private(set) var uiState: FocusListUiState = .loading

    public init(
        getFocusStrategiesUseCase: GetFocusStrategiesUseCase,
        toggleStrategyActiveUseCase: ToggleStrategyActiveUseCase,
        deleteFocusStrategyUseCase: DeleteFocusStrategyUseCase
    ) {
        self.getFocusStrategiesUseCase = getFocusStrategiesUseCase
        self.toggleStrategyActiveUseCase = toggleStrategyActiveUseCase
        self.deleteFocusStrategyUseCase = deleteFocusStrategyUseCase

        Task { await self.loadStrategies() }
    }

    public func loadStrategies() async {
        let result = await getFocusStrategiesUseCase.execute()
        switch result {
        case let .success(strategies):
            uiState = strategies.isEmpty ? .empty : .success(strategies)
        case let .error(_, message):
            uiState = .error(message ?? "加载失败")
        case .loading:
            uiState = .loading
        }
    }

    public func toggleStrategyActive(id: String, isActive: Bool) async {
        let result = await toggleStrategyActiveUseCase.invoke(id: id, isActive: isActive)
        if case let .error(_, message) = result {
            uiState = .error(message ?? "操作失败")
            await loadStrategies()
        }
    }

    public func deleteStrategy(id: String) async {
        let result = await deleteFocusStrategyUseCase.invoke(id: id)
        if case let .error(_, message) = result {
            uiState = .error(message ?? "删除失败")
        }
    }
}

@MainActor
public final class AppSelectionViewModel {
    private let getInstalledAppsUseCase: GetInstalledAppsUseCase
    private var allApps: [AppInfo] = []

    public private(set) var uiState = AppSelectionUiState()

    public init(getInstalledAppsUseCase: GetInstalledAppsUseCase) {
        self.getInstalledAppsUseCase = getInstalledAppsUseCase
        Task { await self.loadApps() }
    }

    public func loadApps() async {
        uiState.isLoading = true
        let result = await getInstalledAppsUseCase.invoke()
        switch result {
        case let .success(apps):
            allApps = apps
            uiState.isLoading = false
            uiState.filteredApps = apps
            uiState.error = nil
        case let .error(_, message):
            uiState.isLoading = false
            uiState.error = message ?? "加载应用列表失败"
        case .loading:
            uiState.isLoading = true
        }
    }

    public func updateSearchQuery(query: String) {
        uiState.searchQuery = query
        filterApps(query: query)
    }

    public func filterApps(query: String) {
        if query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            uiState.filteredApps = allApps
            return
        }

        let filtered = allApps.filter { app in
            app.label.localizedCaseInsensitiveContains(query) ||
            app.packageName.localizedCaseInsensitiveContains(query)
        }
        uiState.filteredApps = filtered
    }

    public func toggleAppSelection(packageName: String) {
        if uiState.selectedPackages.contains(packageName) {
            uiState.selectedPackages.remove(packageName)
        } else {
            uiState.selectedPackages.insert(packageName)
        }
    }

    public func setInitialSelection(packages: Set<String>) {
        uiState.selectedPackages = packages
    }

    public func clearError() {
        uiState.error = nil
    }
}

@MainActor
public final class FocusModeViewModel {
    private let focusModeRepository: FocusModeRepository
    private let getInstalledAppsUseCase: GetInstalledAppsUseCase

    public private(set) var installedApps: [AppInfo] = []
    public private(set) var focusMode: FocusMode = FocusMode()
    public private(set) var whitelistedApps: [AppInfo] = []

    public init(
        focusModeRepository: FocusModeRepository,
        getInstalledAppsUseCase: GetInstalledAppsUseCase
    ) {
        self.focusModeRepository = focusModeRepository
        self.getInstalledAppsUseCase = getInstalledAppsUseCase

        Task {
            await self.loadInstalledApps()
            await self.observeFocusMode()
        }
    }

    private func observeFocusMode() async {
        for await mode in focusModeRepository.getFocusMode() {
            focusMode = mode
            whitelistedApps = installedApps.filter { mode.whitelistedApps.contains($0.packageName) }
        }
    }

    public func loadInstalledApps() async {
        switch await getInstalledAppsUseCase.invoke() {
        case let .success(apps):
            installedApps = apps
            whitelistedApps = apps.filter { focusMode.whitelistedApps.contains($0.packageName) }
        case .error, .loading:
            break
        }
    }

    public func toggleFocusMode(isEnabled: Bool) async {
        await focusModeRepository.toggleFocusMode(isEnabled: isEnabled)
    }

    public func startCountdown(durationMinutes: Int) async {
        await focusModeRepository.startCountdown(durationMinutes: durationMinutes)
    }

    public func setScheduledMode(timeRanges: [TimeRestriction]) async {
        await focusModeRepository.setScheduledMode(timeRanges: timeRanges)
    }

    public func updateScheduledTimeRanges(timeRanges: [TimeRestriction]) async {
        await focusModeRepository.updateScheduledTimeRanges(timeRanges: timeRanges)
    }

    public func stopFocusMode() async {
        await focusModeRepository.stopFocusMode()
    }

    public func addToWhitelist(packageName: String) async {
        await focusModeRepository.addToWhitelist(packageName: packageName)
    }

    public func removeFromWhitelist(packageName: String) async {
        await focusModeRepository.removeFromWhitelist(packageName: packageName)
    }
}

@MainActor
public final class StatsViewModel {
    private let getDailyStatsUseCase: GetDailyStatsUseCase
    private let getUsageTrendUseCase: GetUsageTrendUseCase

    public private(set) var uiState: StatsUiState = .loading
    public private(set) var selectedDate: Date = Calendar.current.startOfDay(for: Date())

    public init(
        getDailyStatsUseCase: GetDailyStatsUseCase,
        getUsageTrendUseCase: GetUsageTrendUseCase
    ) {
        self.getDailyStatsUseCase = getDailyStatsUseCase
        self.getUsageTrendUseCase = getUsageTrendUseCase

        Task { await self.loadStats(date: selectedDate) }
    }

    public func loadStats(date: Date? = nil) async {
        let targetDate = date ?? selectedDate
        uiState = .loading

        let dailyStats = await getDailyStatsUseCase.invoke(date: targetDate)

        let endDate = targetDate
        let startDate = Calendar.current.date(byAdding: .day, value: -6, to: targetDate) ?? targetDate
        let trend = await getUsageTrendUseCase.invoke(startDate: startDate, endDate: endDate)

        uiState = .success(dailyStats: dailyStats, weeklyTrend: trend)
    }

    public func selectDate(date: Date) async {
        selectedDate = Calendar.current.startOfDay(for: date)
        await loadStats(date: selectedDate)
    }

    public func refresh() async {
        await loadStats(date: selectedDate)
    }
}

@MainActor
public final class SettingsViewModel {
    private let usageTrackingRepository: UsageTrackingRepository

    public private(set) var uiState = SettingsUiState()

    public init(usageTrackingRepository: UsageTrackingRepository) {
        self.usageTrackingRepository = usageTrackingRepository
    }

    public func loadTodayRecords() async {
        let records = await usageTrackingRepository.getTodayRecords()
        uiState.todayRecords = records
        uiState.debugMessage = records.isEmpty ? "今日暂无使用记录" : ""
    }

    public func clearTodayRecords() async {
        await usageTrackingRepository.clearTodayRecords()
        uiState.todayRecords = []
        uiState.debugMessage = "今日记录已清除"
    }

    public func clearAllRecords() async {
        await usageTrackingRepository.clearAllRecords()
        uiState.todayRecords = []
        uiState.debugMessage = "所有记录已清除"
    }
}
