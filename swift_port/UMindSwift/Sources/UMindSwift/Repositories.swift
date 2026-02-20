import Foundation

public protocol FocusRepository: Sendable {
    func getFocusStrategiesFlow() -> Flow<[FocusStrategy]>
    func getFocusStrategies() async -> UMindResult<[FocusStrategy]>
    func getFocusStrategyById(id: String) async -> UMindResult<FocusStrategy?>
    func getActiveStrategy() async -> UMindResult<FocusStrategy?>
    func saveFocusStrategy(strategy: FocusStrategy) async -> UMindResult<Void>
    func deleteFocusStrategy(id: String) async -> UMindResult<Void>
    func setStrategyActive(id: String, isActive: Bool) async -> UMindResult<Void>
    func getInstalledApps() async -> UMindResult<[AppInfo]>
    func shouldBlockPackage(packageName: String) async -> Bool
    func getBlockInfo(packageName: String) async -> BlockInfo
}

public protocol InstalledAppProvider: Sendable {
    func fetchInstalledApps() async throws -> [AppInfo]
}

public struct StaticInstalledAppProvider: InstalledAppProvider {
    private let apps: [AppInfo]

    public init(apps: [AppInfo] = []) {
        self.apps = apps
    }

    public func fetchInstalledApps() async throws -> [AppInfo] {
        if !apps.isEmpty { return apps }
        return [
            AppInfo(packageName: "com.tencent.mm", label: "微信"),
            AppInfo(packageName: "com.ss.android.ugc.aweme", label: "抖音"),
            AppInfo(packageName: "tv.danmaku.bili", label: "哔哩哔哩"),
            AppInfo(packageName: "com.apple.mobilesafari", label: "Safari", isSystemApp: true)
        ].sorted(by: { $0.label.localizedCaseInsensitiveCompare($1.label) == .orderedAscending })
    }
}

public final class FocusRepositoryImpl: FocusRepository, @unchecked Sendable {
    private let focusStrategyDao: FocusStrategyDao
    private let usageTrackingRepository: UsageTrackingRepository
    private let temporaryUsageRepository: TemporaryUsageRepository
    private let appProvider: InstalledAppProvider

    private var cachedApps: [AppInfo]?
    private var lastLoadTime: Milliseconds = 0
    private let cacheValidityMs: Milliseconds = 5 * 60 * 1000
    private let lock = NSLock()

    public init(
        focusStrategyDao: FocusStrategyDao,
        usageTrackingRepository: UsageTrackingRepository,
        temporaryUsageRepository: TemporaryUsageRepository,
        appProvider: InstalledAppProvider = StaticInstalledAppProvider()
    ) {
        self.focusStrategyDao = focusStrategyDao
        self.usageTrackingRepository = usageTrackingRepository
        self.temporaryUsageRepository = temporaryUsageRepository
        self.appProvider = appProvider
    }

    public func getFocusStrategiesFlow() -> Flow<[FocusStrategy]> {
        let upstream = focusStrategyDao.getAllStrategiesFlow()
        return AsyncStream { continuation in
            let task = Task {
                for await entities in upstream {
                    continuation.yield(entities.map { $0.toDomainModel() })
                }
                continuation.finish()
            }
            continuation.onTermination = { _ in
                task.cancel()
            }
        }
    }

    public func getFocusStrategies() async -> UMindResult<[FocusStrategy]> {
        let entities = await focusStrategyDao.getAllStrategies()
        return .success(entities.map { $0.toDomainModel() })
    }

    public func getFocusStrategyById(id: String) async -> UMindResult<FocusStrategy?> {
        let entity = await focusStrategyDao.getStrategyById(id: id)
        return .success(entity?.toDomainModel())
    }

    public func getActiveStrategy() async -> UMindResult<FocusStrategy?> {
        let entity = await focusStrategyDao.getActiveStrategy()
        return .success(entity?.toDomainModel())
    }

    public func saveFocusStrategy(strategy: FocusStrategy) async -> UMindResult<Void> {
        let entity = FocusStrategyEntity.fromDomainModel(strategy: {
            var updated = strategy
            updated.updatedAt = nowMillis()
            return updated
        }())
        await focusStrategyDao.insertStrategy(strategy: entity)
        return .success(())
    }

    public func deleteFocusStrategy(id: String) async -> UMindResult<Void> {
        await focusStrategyDao.deleteStrategy(id: id)
        return .success(())
    }

    public func setStrategyActive(id: String, isActive: Bool) async -> UMindResult<Void> {
        await focusStrategyDao.setStrategyActiveExclusive(id: id, isActive: isActive)
        return .success(())
    }

    public func getInstalledApps() async -> UMindResult<[AppInfo]> {
        let now = nowMillis()

        lock.lock()
        if let cachedApps,
           now - lastLoadTime < cacheValidityMs {
            lock.unlock()
            return .success(cachedApps)
        }
        lock.unlock()

        return await loadInstalledApps()
    }

    public func refreshInstalledApps() async -> UMindResult<[AppInfo]> {
        await loadInstalledApps(forceRefresh: true)
    }

    public func preloadInstalledApps() async {
        _ = await getInstalledApps()
    }

    private func loadInstalledApps(forceRefresh: Bool = false) async -> UMindResult<[AppInfo]> {
        do {
            let apps = try await appProvider.fetchInstalledApps()
            lock.lock()
            cachedApps = apps
            lastLoadTime = nowMillis()
            lock.unlock()
            return .success(apps)
        } catch {
            return .error(error, message: "获取应用列表失败")
        }
    }

    public func shouldBlockPackage(packageName: String) async -> Bool {
        if await temporaryUsageRepository.hasActiveTemporaryUsage(packageName: packageName) {
            return false
        }

        guard case let .success(activeStrategy?) = await getActiveStrategy() else {
            return false
        }

        guard activeStrategy.targetApps.contains(packageName) else {
            return false
        }

        if activeStrategy.enforcementMode == .monitorOnly {
            return false
        }

        let withinTimeRestriction = activeStrategy.isWithinFocusTime()
        let exceedsUsageLimit = await checkUsageLimits(strategy: activeStrategy, packageName: packageName)
        let exceedsOpenCountLimit = await checkOpenCountLimits(strategy: activeStrategy, packageName: packageName)

        return withinTimeRestriction || exceedsUsageLimit || exceedsOpenCountLimit
    }

    private func checkUsageLimits(strategy: FocusStrategy, packageName: String) async -> Bool {
        guard let usageLimits = strategy.usageLimits else { return false }
        let today = Calendar.current.startOfDay(for: Date())

        switch usageLimits.type {
        case .totalAll:
            let totalUsage = await strategy.targetApps.reduce(into: Milliseconds(0)) { partialResult, pkg in
                partialResult += await usageTrackingRepository.getUsageDuration(packageName: pkg, date: today)
            }
            return totalUsage >= (usageLimits.totalLimitMillis ?? Int64.max)
        case .perApp:
            let appUsage = await usageTrackingRepository.getUsageDuration(packageName: packageName, date: today)
            return appUsage >= (usageLimits.perAppLimitMillis ?? Int64.max)
        case .individual:
            guard let individualLimit = usageLimits.individualLimitsMillis[packageName] else { return false }
            let appUsage = await usageTrackingRepository.getUsageDuration(packageName: packageName, date: today)
            return appUsage >= individualLimit
        }
    }

    private func checkOpenCountLimits(strategy: FocusStrategy, packageName: String) async -> Bool {
        guard let openCountLimits = strategy.openCountLimits else { return false }
        let today = Calendar.current.startOfDay(for: Date())

        switch openCountLimits.type {
        case .totalAll:
            let totalCount = await strategy.targetApps.reduce(into: 0) { partialResult, pkg in
                partialResult += await usageTrackingRepository.getOpenCount(packageName: pkg, date: today)
            }
            return totalCount >= (openCountLimits.totalCount ?? Int.max)
        case .perApp:
            let appCount = await usageTrackingRepository.getOpenCount(packageName: packageName, date: today)
            return appCount >= (openCountLimits.perAppCount ?? Int.max)
        case .individual:
            guard let individual = openCountLimits.individualCounts[packageName] else { return false }
            let appCount = await usageTrackingRepository.getOpenCount(packageName: packageName, date: today)
            return appCount >= individual
        }
    }

    public func getBlockInfo(packageName: String) async -> BlockInfo {
        if await temporaryUsageRepository.hasActiveTemporaryUsage(packageName: packageName) {
            return BlockInfo(shouldBlock: false)
        }

        guard case let .success(activeStrategy?) = await getActiveStrategy(),
              activeStrategy.targetApps.contains(packageName),
              activeStrategy.enforcementMode != .monitorOnly else {
            return BlockInfo(shouldBlock: false)
        }

        let today = Calendar.current.startOfDay(for: Date())
        var reasons: [BlockReason] = []

        var usageLimitMinutes: Int64?
        var usedMinutes: Int64 = 0
        var remainingMinutes: Int64?
        var openCountLimit: Int?
        var openCount: Int = 0
        var remainingCount: Int?

        if activeStrategy.isWithinFocusTime() {
            let nextAvailableTime = activeStrategy.timeRestrictions.map { $0.endTime.description }.min()
            reasons.append(.timeRestriction(nextAvailableTime: nextAvailableTime))
        }

        if let usageLimits = activeStrategy.usageLimits {
            switch usageLimits.type {
            case .totalAll:
                let totalUsage = await activeStrategy.targetApps.reduce(into: Milliseconds(0)) { partialResult, pkg in
                    partialResult += await usageTrackingRepository.getUsageDuration(packageName: pkg, date: today)
                }
                if let limit = usageLimits.totalLimitMillis {
                    usageLimitMinutes = limit / 60_000
                    usedMinutes = totalUsage / 60_000
                    remainingMinutes = max(0, (limit - totalUsage) / 60_000)
                    if totalUsage >= limit {
                        reasons.append(.usageLimitExceeded(limitMinutes: usageLimitMinutes ?? 0, usedMinutes: usedMinutes))
                    }
                }
            case .perApp:
                let appUsage = await usageTrackingRepository.getUsageDuration(packageName: packageName, date: today)
                if let limit = usageLimits.perAppLimitMillis {
                    usageLimitMinutes = limit / 60_000
                    usedMinutes = appUsage / 60_000
                    remainingMinutes = max(0, (limit - appUsage) / 60_000)
                    if appUsage >= limit {
                        reasons.append(.usageLimitExceeded(limitMinutes: usageLimitMinutes ?? 0, usedMinutes: usedMinutes))
                    }
                }
            case .individual:
                let appUsage = await usageTrackingRepository.getUsageDuration(packageName: packageName, date: today)
                if let limit = usageLimits.individualLimitsMillis[packageName] {
                    usageLimitMinutes = limit / 60_000
                    usedMinutes = appUsage / 60_000
                    remainingMinutes = max(0, (limit - appUsage) / 60_000)
                    if appUsage >= limit {
                        reasons.append(.usageLimitExceeded(limitMinutes: usageLimitMinutes ?? 0, usedMinutes: usedMinutes))
                    }
                }
            }
        }

        if let countLimits = activeStrategy.openCountLimits {
            switch countLimits.type {
            case .totalAll:
                let total = await activeStrategy.targetApps.reduce(into: 0) { partialResult, pkg in
                    partialResult += await usageTrackingRepository.getOpenCount(packageName: pkg, date: today)
                }
                if let limit = countLimits.totalCount {
                    openCountLimit = limit
                    openCount = total
                    remainingCount = max(0, limit - total)
                    if total >= limit {
                        reasons.append(.openCountLimitExceeded(limitCount: limit, usedCount: total))
                    }
                }
            case .perApp:
                let appCount = await usageTrackingRepository.getOpenCount(packageName: packageName, date: today)
                if let limit = countLimits.perAppCount {
                    openCountLimit = limit
                    openCount = appCount
                    remainingCount = max(0, limit - appCount)
                    if appCount >= limit {
                        reasons.append(.openCountLimitExceeded(limitCount: limit, usedCount: appCount))
                    }
                }
            case .individual:
                let appCount = await usageTrackingRepository.getOpenCount(packageName: packageName, date: today)
                if let limit = countLimits.individualCounts[packageName] {
                    openCountLimit = limit
                    openCount = appCount
                    remainingCount = max(0, limit - appCount)
                    if appCount >= limit {
                        reasons.append(.openCountLimitExceeded(limitCount: limit, usedCount: appCount))
                    }
                }
            }
        }

        let usageInfo: UsageInfo?
        if usageLimitMinutes != nil || openCountLimit != nil {
            usageInfo = UsageInfo(
                usageLimitMinutes: usageLimitMinutes,
                usedMinutes: usedMinutes,
                remainingMinutes: remainingMinutes,
                openCountLimit: openCountLimit,
                openCount: openCount,
                remainingCount: remainingCount
            )
        } else {
            usageInfo = nil
        }

        return BlockInfo(shouldBlock: !reasons.isEmpty, reasons: reasons, usageInfo: usageInfo)
    }
}

public final class UsageTrackingRepository: @unchecked Sendable {
    private let usageRecordDao: UsageRecordDao

    public init(usageRecordDao: UsageRecordDao) {
        self.usageRecordDao = usageRecordDao
    }

    public func recordUsage(packageName: String, durationMillis: Milliseconds) async {
        let today = Calendar.current.startOfDay(for: Date())
        if var existing = await usageRecordDao.getUsageRecord(packageName: packageName, date: today) {
            existing.usageDurationMillis += durationMillis
            existing.lastUpdated = nowMillis()
            await usageRecordDao.updateUsageRecord(record: existing)
        } else {
            let newRecord = UsageRecordEntity(
                packageName: packageName,
                date: today,
                usageDurationMillis: durationMillis,
                openCount: 0
            )
            await usageRecordDao.insertUsageRecord(record: newRecord)
        }
    }

    public func recordAppOpen(packageName: String) async {
        let today = Calendar.current.startOfDay(for: Date())
        if var existing = await usageRecordDao.getUsageRecord(packageName: packageName, date: today) {
            existing.openCount += 1
            existing.lastUpdated = nowMillis()
            await usageRecordDao.updateUsageRecord(record: existing)
        } else {
            let newRecord = UsageRecordEntity(
                packageName: packageName,
                date: today,
                usageDurationMillis: 0,
                openCount: 1
            )
            await usageRecordDao.insertUsageRecord(record: newRecord)
        }
    }

    public func getUsageDuration(packageName: String, date: Date) async -> Milliseconds {
        await usageRecordDao.getUsageRecord(packageName: packageName, date: date)?.usageDurationMillis ?? 0
    }

    public func getOpenCount(packageName: String, date: Date) async -> Int {
        await usageRecordDao.getUsageRecord(packageName: packageName, date: date)?.openCount ?? 0
    }

    public func getUsageDurationInRange(packageName: String, startDate: Date, endDate: Date) async -> Milliseconds {
        let records = await usageRecordDao.getUsageRecordsInRange(startDate: startDate, endDate: endDate)
        return records.filter { $0.packageName == packageName }.reduce(0) { $0 + $1.usageDurationMillis }
    }

    public func getOpenCountInRange(packageName: String, startDate: Date, endDate: Date) async -> Int {
        let records = await usageRecordDao.getUsageRecordsInRange(startDate: startDate, endDate: endDate)
        return records.filter { $0.packageName == packageName }.reduce(0) { $0 + $1.openCount }
    }

    public func getTotalUsageDurationInRange(startDate: Date, endDate: Date) async -> Milliseconds {
        let records = await usageRecordDao.getUsageRecordsInRange(startDate: startDate, endDate: endDate)
        return records.reduce(0) { $0 + $1.usageDurationMillis }
    }

    public func getTotalOpenCountInRange(startDate: Date, endDate: Date) async -> Int {
        let records = await usageRecordDao.getUsageRecordsInRange(startDate: startDate, endDate: endDate)
        return records.reduce(0) { $0 + $1.openCount }
    }

    public func cleanupOldRecords() async {
        let cutoffDate = Calendar.current.date(byAdding: .day, value: -90, to: Date()) ?? Date()
        await usageRecordDao.deleteOldRecords(beforeDate: cutoffDate)
    }

    public func getUsageRecordFlow(packageName: String, date: Date) -> Flow<UsageRecordEntity?> {
        usageRecordDao.getUsageRecordFlow(packageName: packageName, date: date)
    }

    public func clearTodayRecords() async {
        await usageRecordDao.deleteRecordsForDate(date: Date())
    }

    public func clearAllRecords() async {
        await usageRecordDao.deleteAllRecords()
    }

    public func getTodayRecords() async -> [UsageRecordEntity] {
        await usageRecordDao.getUsageRecordsForDate(date: Date())
    }

    public func getUsageRecordsForDateFlow(date: Date) -> Flow<[UsageRecordEntity]> {
        usageRecordDao.getUsageRecordsForDateFlow(date: date)
    }

    public func getTotalUsageDurationForDate(date: Date) async -> Milliseconds {
        await usageRecordDao.getTotalUsageDurationForDate(date: date) ?? 0
    }

    public func getTotalOpenCountForDate(date: Date) async -> Int {
        await usageRecordDao.getTotalOpenCountForDate(date: date) ?? 0
    }

    public func getUsageRecordsInRangeFlow(startDate: Date, endDate: Date) -> Flow<[UsageRecordEntity]> {
        usageRecordDao.getUsageRecordsInRangeFlow(startDate: startDate, endDate: endDate)
    }

    public func getUsageRecordsForDate(date: Date) async -> [UsageRecordEntity] {
        await usageRecordDao.getUsageRecordsForDate(date: date)
    }

    public func getUsageRecordsInRange(startDate: Date, endDate: Date) async -> [UsageRecordEntity] {
        await usageRecordDao.getUsageRecordsInRange(startDate: startDate, endDate: endDate)
    }
}

public final class TemporaryUsageRepository: @unchecked Sendable {
    private let temporaryUsageDao: TemporaryUsageDao

    public init(temporaryUsageDao: TemporaryUsageDao) {
        self.temporaryUsageDao = temporaryUsageDao
    }

    public func requestTemporaryUsage(
        packageName: String,
        appName: String,
        reason: String,
        durationMinutes: Int
    ) async -> TemporaryUsage {
        let now = nowMillis()
        let endTime = now + Milliseconds(durationMinutes * 60_000)

        let temporaryUsage = TemporaryUsage(
            id: UUID().uuidString,
            packageName: packageName,
            appName: appName,
            reason: reason,
            requestedDurationMinutes: durationMinutes,
            startTime: now,
            endTime: endTime,
            isActive: true,
            createdAt: now
        )

        await temporaryUsageDao.insertTemporaryUsage(
            temporaryUsage: TemporaryUsageEntity.fromDomainModel(temporaryUsage: temporaryUsage)
        )

        return temporaryUsage
    }

    public func getActiveTemporaryUsage(packageName: String) async -> TemporaryUsage? {
        let now = nowMillis()
        return await temporaryUsageDao
            .getActiveTemporaryUsage(packageName: packageName, currentTime: now)?
            .toDomainModel()
    }

    public func getActiveTemporaryUsageFlow(packageName: String) -> Flow<TemporaryUsage?> {
        let upstream = temporaryUsageDao.getActiveTemporaryUsageFlow(packageName: packageName, currentTime: nowMillis())
        return AsyncStream { continuation in
            let task = Task {
                for await entity in upstream {
                    continuation.yield(entity?.toDomainModel())
                }
                continuation.finish()
            }
            continuation.onTermination = { _ in
                task.cancel()
            }
        }
    }

    public func hasActiveTemporaryUsage(packageName: String) async -> Bool {
        await getActiveTemporaryUsage(packageName: packageName) != nil
    }

    public func deactivateTemporaryUsage(id: String) async {
        await temporaryUsageDao.deactivateTemporaryUsage(id: id)
    }

    public func deactivateExpiredUsages() async {
        await temporaryUsageDao.deactivateExpiredUsages(currentTime: nowMillis())
    }

    public func getAllTemporaryUsages() async -> [TemporaryUsage] {
        await temporaryUsageDao.getAllTemporaryUsages().map { $0.toDomainModel() }
    }

    public func getTemporaryUsagesForPackage(packageName: String) async -> [TemporaryUsage] {
        await temporaryUsageDao.getTemporaryUsagesForPackage(packageName: packageName).map { $0.toDomainModel() }
    }

    public func cleanupOldRecords() async {
        let cutoffTime = nowMillis() - (90 * 24 * 60 * 60 * 1000)
        await temporaryUsageDao.deleteOldRecords(beforeTime: cutoffTime)
    }
}

public final class BlockEventRepository: @unchecked Sendable {
    private let blockEventDao: BlockEventDao

    public init(blockEventDao: BlockEventDao) {
        self.blockEventDao = blockEventDao
    }

    public func recordBlockEvent(
        packageName: String,
        appName: String,
        blockInfo: BlockInfo,
        strategyIds: [String]? = nil
    ) async {
        let blockSource = determineBlockSource(reasons: blockInfo.reasons)
        let blockReasonJson = serializeBlockReasons(reasons: blockInfo.reasons)
        let strategyIdsJson: String?
        if let strategyIds {
            strategyIdsJson = (try? JSONSerialization.data(withJSONObject: strategyIds)).flatMap { String(data: $0, encoding: .utf8) }
        } else {
            strategyIdsJson = nil
        }

        let event = BlockEventEntity(
            id: UUID().uuidString,
            packageName: packageName,
            appName: appName,
            timestamp: nowMillis(),
            blockReason: blockReasonJson,
            blockSource: blockSource,
            strategyIds: strategyIdsJson
        )

        await blockEventDao.insertBlockEvent(event: event)
    }

    public func getTodayBlockEvents() async -> [BlockEventEntity] {
        let start = Calendar.current.startOfDay(for: Date())
        return await blockEventDao.getTodayBlockEvents(startOfDay: Milliseconds(start.timeIntervalSince1970 * 1000))
    }

    public func getTodayBlockEventsFlow() -> Flow<[BlockEventEntity]> {
        let start = Calendar.current.startOfDay(for: Date())
        return blockEventDao.getTodayBlockEventsFlow(startOfDay: Milliseconds(start.timeIntervalSince1970 * 1000))
    }

    public func getTodayBlockCount() async -> Int {
        let start = Calendar.current.startOfDay(for: Date())
        return await blockEventDao.getTodayBlockCount(startOfDay: Milliseconds(start.timeIntervalSince1970 * 1000))
    }

    public func getTodayBlockCountForPackage(packageName: String) async -> Int {
        let start = Calendar.current.startOfDay(for: Date())
        return await blockEventDao.getTodayBlockCountForPackage(
            packageName: packageName,
            startOfDay: Milliseconds(start.timeIntervalSince1970 * 1000)
        )
    }

    public func getBlockEventsInRange(startTime: Milliseconds, endTime: Milliseconds) async -> [BlockEventEntity] {
        await blockEventDao.getBlockEventsInRange(startTime: startTime, endTime: endTime)
    }

    public func cleanupOldBlockEvents() async {
        let cutoff = Calendar.current.date(byAdding: .day, value: -90, to: Calendar.current.startOfDay(for: Date())) ?? Date()
        await blockEventDao.deleteBlockEventsBefore(timestamp: Milliseconds(cutoff.timeIntervalSince1970 * 1000))
    }

    private func determineBlockSource(reasons: [BlockReason]) -> String {
        reasons.contains(.focusModeActive) ? "FOCUS_MODE" : "DAILY_MANAGEMENT"
    }

    private func serializeBlockReasons(reasons: [BlockReason]) -> String {
        var array: [[String: Any]] = []
        for reason in reasons {
            switch reason {
            case let .timeRestriction(nextAvailableTime):
                array.append(["type": "TIME_RESTRICTION", "nextAvailableTime": nextAvailableTime as Any])
            case let .usageLimitExceeded(limitMinutes, usedMinutes):
                array.append(["type": "USAGE_LIMIT_EXCEEDED", "limitMinutes": limitMinutes, "usedMinutes": usedMinutes])
            case let .openCountLimitExceeded(limitCount, usedCount):
                array.append(["type": "OPEN_COUNT_LIMIT_EXCEEDED", "limitCount": limitCount, "usedCount": usedCount])
            case .focusModeActive:
                array.append(["type": "FOCUS_MODE_ACTIVE"])
            case .forceThroughApp:
                array.append(["type": "FORCE_THROUGH_APP"])
            }
        }
        guard let data = try? JSONSerialization.data(withJSONObject: array, options: []),
              let text = String(data: data, encoding: .utf8) else {
            return "[]"
        }
        return text
    }
}

public final class FocusModeRepository: @unchecked Sendable {
    private let focusModeDao: FocusModeDao

    public init(focusModeDao: FocusModeDao) {
        self.focusModeDao = focusModeDao
    }

    public func getFocusMode() -> Flow<FocusMode> {
        let upstream = focusModeDao.getFocusMode()
        return AsyncStream { continuation in
            let task = Task {
                for await entity in upstream {
                    continuation.yield(entity?.toDomainModel() ?? FocusMode())
                }
                continuation.finish()
            }
            continuation.onTermination = { _ in
                task.cancel()
            }
        }
    }

    public func getFocusModeOnce() async -> FocusMode {
        await focusModeDao.getFocusModeOnce()?.toDomainModel() ?? FocusMode()
    }

    public func toggleFocusMode(isEnabled: Bool) async {
        if var existing = await focusModeDao.getFocusModeOnce() {
            existing.isEnabled = isEnabled
            existing.modeType = .manual
            existing.countdownEndTime = nil
            existing.updatedAt = nowMillis()
            await focusModeDao.update(focusMode: existing)
        } else {
            await focusModeDao.insert(focusMode: FocusModeEntity(isEnabled: isEnabled, modeType: .manual))
        }
    }

    public func startCountdown(durationMinutes: Int) async {
        let endTime = nowMillis() + Milliseconds(durationMinutes * 60_000)
        if var existing = await focusModeDao.getFocusModeOnce() {
            existing.isEnabled = true
            existing.modeType = .countdown
            existing.countdownEndTime = endTime
            existing.updatedAt = nowMillis()
            await focusModeDao.update(focusMode: existing)
        } else {
            await focusModeDao.insert(focusMode: FocusModeEntity(isEnabled: true, modeType: .countdown, countdownEndTime: endTime))
        }
    }

    public func setScheduledMode(timeRanges: [TimeRestriction]) async {
        if var existing = await focusModeDao.getFocusModeOnce() {
            existing.isEnabled = true
            existing.modeType = .scheduled
            existing.scheduledTimeRanges = timeRanges
            existing.countdownEndTime = nil
            existing.updatedAt = nowMillis()
            await focusModeDao.update(focusMode: existing)
        } else {
            await focusModeDao.insert(focusMode: FocusModeEntity(isEnabled: true, modeType: .scheduled, scheduledTimeRanges: timeRanges))
        }
    }

    public func updateScheduledTimeRanges(timeRanges: [TimeRestriction]) async {
        if var existing = await focusModeDao.getFocusModeOnce() {
            existing.scheduledTimeRanges = timeRanges
            existing.updatedAt = nowMillis()
            await focusModeDao.update(focusMode: existing)
        } else {
            await focusModeDao.insert(focusMode: FocusModeEntity(scheduledTimeRanges: timeRanges))
        }
    }

    public func stopFocusMode() async {
        if var existing = await focusModeDao.getFocusModeOnce() {
            existing.isEnabled = false
            existing.updatedAt = nowMillis()
            await focusModeDao.update(focusMode: existing)
        }
    }

    public func updateWhitelist(whitelistedApps: Set<String>) async {
        if var existing = await focusModeDao.getFocusModeOnce() {
            existing.whitelistedApps = whitelistedApps
            existing.updatedAt = nowMillis()
            await focusModeDao.update(focusMode: existing)
        } else {
            await focusModeDao.insert(focusMode: FocusModeEntity(whitelistedApps: whitelistedApps))
        }
    }

    public func addToWhitelist(packageName: String) async {
        let existing = await focusModeDao.getFocusModeOnce()
        let current = existing?.whitelistedApps ?? []
        await updateWhitelist(whitelistedApps: current.union([packageName]))
    }

    public func removeFromWhitelist(packageName: String) async {
        let existing = await focusModeDao.getFocusModeOnce()
        var current = existing?.whitelistedApps ?? []
        current.remove(packageName)
        await updateWhitelist(whitelistedApps: current)
    }
}
