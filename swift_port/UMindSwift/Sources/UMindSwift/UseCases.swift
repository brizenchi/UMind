import Foundation

public final class GetFocusStrategiesUseCase: @unchecked Sendable {
    private let repository: FocusRepository

    public init(repository: FocusRepository) {
        self.repository = repository
    }

    public func invoke() -> Flow<[FocusStrategy]> {
        repository.getFocusStrategiesFlow()
    }

    public func execute() async -> UMindResult<[FocusStrategy]> {
        await repository.getFocusStrategies()
    }
}

public final class SaveFocusStrategyUseCase: @unchecked Sendable {
    private let repository: FocusRepository

    public init(repository: FocusRepository) {
        self.repository = repository
    }

    public func invoke(strategy: FocusStrategy) async -> UMindResult<Void> {
        if strategy.name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return .error(ValidationError.emptyName, message: "请输入策略名称")
        }
        if strategy.targetApps.isEmpty {
            return .error(ValidationError.emptyTargets, message: "请至少选择一个要限制的应用")
        }
        return await repository.saveFocusStrategy(strategy: strategy)
    }

    private enum ValidationError: Error {
        case emptyName
        case emptyTargets
    }
}

public final class DeleteFocusStrategyUseCase: @unchecked Sendable {
    private let repository: FocusRepository

    public init(repository: FocusRepository) {
        self.repository = repository
    }

    public func invoke(id: String) async -> UMindResult<Void> {
        await repository.deleteFocusStrategy(id: id)
    }
}

public final class ToggleStrategyActiveUseCase: @unchecked Sendable {
    private let repository: FocusRepository

    public init(repository: FocusRepository) {
        self.repository = repository
    }

    public func invoke(id: String, isActive: Bool) async -> UMindResult<Void> {
        await repository.setStrategyActive(id: id, isActive: isActive)
    }
}

public final class GetInstalledAppsUseCase: @unchecked Sendable {
    private let repository: FocusRepository

    public init(repository: FocusRepository) {
        self.repository = repository
    }

    public func invoke() async -> UMindResult<[AppInfo]> {
        await repository.getInstalledApps()
    }
}

public final class GetDailyStatsUseCase: @unchecked Sendable {
    private let usageTrackingRepository: UsageTrackingRepository
    private let appNameResolver: @Sendable (String) -> String

    public init(
        usageTrackingRepository: UsageTrackingRepository,
        appNameResolver: @escaping @Sendable (String) -> String = { $0 }
    ) {
        self.usageTrackingRepository = usageTrackingRepository
        self.appNameResolver = appNameResolver
    }

    public func invoke(date: Date) async -> DailyStats {
        let records = await usageTrackingRepository.getUsageRecordsForDate(date: date)

        let appUsageStats = records.map { record in
            AppUsageStats(
                packageName: record.packageName,
                appName: appNameResolver(record.packageName),
                usageDurationMillis: record.usageDurationMillis,
                openCount: record.openCount,
                blockCount: 0
            )
        }.sorted(by: { $0.usageDurationMillis > $1.usageDurationMillis })

        let totalUsageDuration = records.reduce(Int64(0)) { $0 + $1.usageDurationMillis }
        let totalOpenCount = records.reduce(0) { $0 + $1.openCount }

        return DailyStats(
            date: Calendar.current.startOfDay(for: date),
            totalUsageDurationMillis: totalUsageDuration,
            totalOpenCount: totalOpenCount,
            totalBlockCount: 0,
            appUsageStats: appUsageStats
        )
    }
}

public final class GetUsageTrendUseCase: @unchecked Sendable {
    private let usageTrackingRepository: UsageTrackingRepository

    public init(usageTrackingRepository: UsageTrackingRepository) {
        self.usageTrackingRepository = usageTrackingRepository
    }

    public func invoke(startDate: Date, endDate: Date) async -> [UsageTrend] {
        let calendar = Calendar.current
        let start = calendar.startOfDay(for: startDate)
        let end = calendar.startOfDay(for: endDate)

        let records = await usageTrackingRepository.getUsageRecordsInRange(startDate: start, endDate: end)
        let grouped = Dictionary(grouping: records, by: { calendar.startOfDay(for: $0.date) })

        var result: [UsageTrend] = []
        var cursor = start

        while cursor <= end {
            if let dayRecords = grouped[cursor] {
                result.append(
                    UsageTrend(
                        date: cursor,
                        totalUsageDurationMillis: dayRecords.reduce(0) { $0 + $1.usageDurationMillis },
                        totalOpenCount: dayRecords.reduce(0) { $0 + $1.openCount }
                    )
                )
            } else {
                result.append(
                    UsageTrend(date: cursor, totalUsageDurationMillis: 0, totalOpenCount: 0)
                )
            }

            guard let next = calendar.date(byAdding: .day, value: 1, to: cursor) else { break }
            cursor = next
        }

        return result
    }
}
