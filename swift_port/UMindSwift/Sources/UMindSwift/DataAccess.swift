import Foundation

public typealias Flow<Value> = AsyncStream<Value>

public final class FlowRelay<Value>: @unchecked Sendable {
    private let lock = NSLock()
    private var continuations: [UUID: AsyncStream<Value>.Continuation] = [:]
    private var current: Value

    public init(_ initial: Value) {
        self.current = initial
    }

    public func stream() -> AsyncStream<Value> {
        AsyncStream { continuation in
            let id = UUID()
            lock.lock()
            continuations[id] = continuation
            let value = current
            lock.unlock()

            continuation.yield(value)
            continuation.onTermination = { [weak self] _ in
                guard let self else { return }
                self.lock.lock()
                self.continuations.removeValue(forKey: id)
                self.lock.unlock()
            }
        }
    }

    public func emit(_ value: Value) {
        lock.lock()
        current = value
        let active = Array(continuations.values)
        lock.unlock()

        for continuation in active {
            continuation.yield(value)
        }
    }

    public func value() -> Value {
        lock.lock()
        defer { lock.unlock() }
        return current
    }
}

public protocol FocusStrategyDao: Sendable {
    func getAllStrategiesFlow() -> Flow<[FocusStrategyEntity]>
    func getAllStrategies() async -> [FocusStrategyEntity]
    func getStrategyById(id: String) async -> FocusStrategyEntity?
    func getActiveStrategy() async -> FocusStrategyEntity?
    func insertStrategy(strategy: FocusStrategyEntity) async
    func updateStrategy(strategy: FocusStrategyEntity) async
    func deleteStrategy(id: String) async
    func deactivateAllStrategies() async
    func setStrategyActive(id: String, isActive: Bool) async
    func setStrategyActiveExclusive(id: String, isActive: Bool) async
}

public protocol FocusModeDao: Sendable {
    func getFocusMode() -> Flow<FocusModeEntity?>
    func getFocusModeOnce() async -> FocusModeEntity?
    func insert(focusMode: FocusModeEntity) async
    func update(focusMode: FocusModeEntity) async
    func updateEnabled(isEnabled: Bool, updatedAt: Milliseconds) async
    func updateWhitelist(whitelistedApps: Set<String>, updatedAt: Milliseconds) async
}

public protocol UsageRecordDao: Sendable {
    func getUsageRecord(packageName: String, date: Date) async -> UsageRecordEntity?
    func getUsageRecordFlow(packageName: String, date: Date) -> Flow<UsageRecordEntity?>
    func getUsageRecordsForDate(date: Date) async -> [UsageRecordEntity]
    func getUsageRecordsInRange(startDate: Date, endDate: Date) async -> [UsageRecordEntity]
    func insertUsageRecord(record: UsageRecordEntity) async
    func updateUsageRecord(record: UsageRecordEntity) async
    func deleteOldRecords(beforeDate: Date) async
    func deleteRecordsForPackage(packageName: String) async
    func getUsageRecordsForDateFlow(date: Date) -> Flow<[UsageRecordEntity]>
    func getTotalUsageDurationForDate(date: Date) async -> Milliseconds?
    func getTotalOpenCountForDate(date: Date) async -> Int?
    func getUsageRecordsInRangeFlow(startDate: Date, endDate: Date) -> Flow<[UsageRecordEntity]>
    func deleteAllRecords() async
    func deleteRecordsForDate(date: Date) async
}

public protocol TemporaryUsageDao: Sendable {
    func getActiveTemporaryUsage(packageName: String, currentTime: Milliseconds) async -> TemporaryUsageEntity?
    func getActiveTemporaryUsageFlow(packageName: String, currentTime: Milliseconds) -> Flow<TemporaryUsageEntity?>
    func getAllTemporaryUsages() async -> [TemporaryUsageEntity]
    func getTemporaryUsagesForPackage(packageName: String) async -> [TemporaryUsageEntity]
    func insertTemporaryUsage(temporaryUsage: TemporaryUsageEntity) async
    func updateTemporaryUsage(temporaryUsage: TemporaryUsageEntity) async
    func deactivateTemporaryUsage(id: String) async
    func deactivateExpiredUsages(currentTime: Milliseconds) async
    func deleteOldRecords(beforeTime: Milliseconds) async
}

public protocol UsageSessionDao: Sendable {
    func insertSession(session: UsageSessionEntity) async
    func updateSession(session: UsageSessionEntity) async
    func getSessionsForRecord(recordId: String) async -> [UsageSessionEntity]
    func getSessionsForPackage(packageName: String) async -> [UsageSessionEntity]
    func getActiveSession(packageName: String) async -> UsageSessionEntity?
    func getSessionsInRange(startTime: Milliseconds, endTime: Milliseconds) async -> [UsageSessionEntity]
    func deleteSessionsForPackage(packageName: String) async
    func deleteSessionsBefore(timestamp: Milliseconds) async
}

public protocol BlockEventDao: Sendable {
    func insertBlockEvent(event: BlockEventEntity) async
    func getAllBlockEventsFlow() -> Flow<[BlockEventEntity]>
    func getBlockEventsForPackage(packageName: String) async -> [BlockEventEntity]
    func getBlockEventsInRange(startTime: Milliseconds, endTime: Milliseconds) async -> [BlockEventEntity]
    func getTodayBlockEvents(startOfDay: Milliseconds) async -> [BlockEventEntity]
    func getTodayBlockEventsFlow(startOfDay: Milliseconds) -> Flow<[BlockEventEntity]>
    func getBlockEventsBySource(source: String) async -> [BlockEventEntity]
    func getTodayBlockCount(startOfDay: Milliseconds) async -> Int
    func getTodayBlockCountForPackage(packageName: String, startOfDay: Milliseconds) async -> Int
    func deleteBlockEventsBefore(timestamp: Milliseconds) async
    func deleteAllBlockEvents() async
}

public actor InMemoryFocusStrategyDao: FocusStrategyDao {
    private var items: [FocusStrategyEntity] = []
    private let relay = FlowRelay<[FocusStrategyEntity]>([])

    public init() {}

    public func getAllStrategiesFlow() -> Flow<[FocusStrategyEntity]> {
        relay.stream()
    }

    public func getAllStrategies() async -> [FocusStrategyEntity] {
        items.sorted(by: { $0.createdAt > $1.createdAt })
    }

    public func getStrategyById(id: String) async -> FocusStrategyEntity? {
        items.first(where: { $0.id == id })
    }

    public func getActiveStrategy() async -> FocusStrategyEntity? {
        items.first(where: { $0.isActive })
    }

    public func insertStrategy(strategy: FocusStrategyEntity) async {
        items.removeAll(where: { $0.id == strategy.id })
        items.append(strategy)
        relay.emit(await getAllStrategies())
    }

    public func updateStrategy(strategy: FocusStrategyEntity) async {
        await insertStrategy(strategy: strategy)
    }

    public func deleteStrategy(id: String) async {
        items.removeAll(where: { $0.id == id })
        relay.emit(await getAllStrategies())
    }

    public func deactivateAllStrategies() async {
        items = items.map { item in
            var copy = item
            copy.isActive = false
            return copy
        }
        relay.emit(await getAllStrategies())
    }

    public func setStrategyActive(id: String, isActive: Bool) async {
        items = items.map { item in
            guard item.id == id else { return item }
            var copy = item
            copy.isActive = isActive
            copy.updatedAt = nowMillis()
            return copy
        }
        relay.emit(await getAllStrategies())
    }

    public func setStrategyActiveExclusive(id: String, isActive: Bool) async {
        if isActive {
            await deactivateAllStrategies()
        }
        await setStrategyActive(id: id, isActive: isActive)
    }
}

public actor InMemoryFocusModeDao: FocusModeDao {
    private var item: FocusModeEntity?
    private let relay = FlowRelay<FocusModeEntity?>(nil)

    public init() {}

    public func getFocusMode() -> Flow<FocusModeEntity?> {
        relay.stream()
    }

    public func getFocusModeOnce() async -> FocusModeEntity? {
        item
    }

    public func insert(focusMode: FocusModeEntity) async {
        item = focusMode
        relay.emit(item)
    }

    public func update(focusMode: FocusModeEntity) async {
        item = focusMode
        relay.emit(item)
    }

    public func updateEnabled(isEnabled: Bool, updatedAt: Milliseconds) async {
        guard var existing = item else { return }
        existing.isEnabled = isEnabled
        existing.updatedAt = updatedAt
        item = existing
        relay.emit(item)
    }

    public func updateWhitelist(whitelistedApps: Set<String>, updatedAt: Milliseconds) async {
        guard var existing = item else { return }
        existing.whitelistedApps = whitelistedApps
        existing.updatedAt = updatedAt
        item = existing
        relay.emit(item)
    }
}

public actor InMemoryUsageRecordDao: UsageRecordDao {
    private var items: [UsageRecordEntity] = []
    private let byDateRelay = FlowRelay<[UsageRecordEntity]>([])

    public init() {}

    private func normalized(_ date: Date) -> Date {
        Calendar.current.startOfDay(for: date)
    }

    public func getUsageRecord(packageName: String, date: Date) async -> UsageRecordEntity? {
        let d = normalized(date)
        return items.first(where: { $0.packageName == packageName && $0.date == d })
    }

    public func getUsageRecordFlow(packageName: String, date: Date) -> Flow<UsageRecordEntity?> {
        let d = normalized(date)
        return AsyncStream { continuation in
            continuation.yield(items.first(where: { $0.packageName == packageName && $0.date == d }))
            continuation.onTermination = { _ in }
        }
    }

    public func getUsageRecordsForDate(date: Date) async -> [UsageRecordEntity] {
        let d = normalized(date)
        return items.filter { $0.date == d }
    }

    public func getUsageRecordsInRange(startDate: Date, endDate: Date) async -> [UsageRecordEntity] {
        let s = normalized(startDate)
        let e = normalized(endDate)
        return items.filter { $0.date >= s && $0.date <= e }
    }

    public func insertUsageRecord(record: UsageRecordEntity) async {
        if let index = items.firstIndex(where: { $0.packageName == record.packageName && $0.date == record.date }) {
            items[index] = record
        } else {
            items.append(record)
        }
        byDateRelay.emit(items)
    }

    public func updateUsageRecord(record: UsageRecordEntity) async {
        await insertUsageRecord(record: record)
    }

    public func deleteOldRecords(beforeDate: Date) async {
        let cutoff = normalized(beforeDate)
        items.removeAll(where: { $0.date < cutoff })
        byDateRelay.emit(items)
    }

    public func deleteRecordsForPackage(packageName: String) async {
        items.removeAll(where: { $0.packageName == packageName })
        byDateRelay.emit(items)
    }

    public func getUsageRecordsForDateFlow(date: Date) -> Flow<[UsageRecordEntity]> {
        let d = normalized(date)
        return AsyncStream { continuation in
            continuation.yield(items.filter { $0.date == d }.sorted(by: { $0.usageDurationMillis > $1.usageDurationMillis }))
            continuation.onTermination = { _ in }
        }
    }

    public func getTotalUsageDurationForDate(date: Date) async -> Milliseconds? {
        let records = await getUsageRecordsForDate(date: date)
        return records.isEmpty ? nil : records.reduce(0) { $0 + $1.usageDurationMillis }
    }

    public func getTotalOpenCountForDate(date: Date) async -> Int? {
        let records = await getUsageRecordsForDate(date: date)
        return records.isEmpty ? nil : records.reduce(0) { $0 + $1.openCount }
    }

    public func getUsageRecordsInRangeFlow(startDate: Date, endDate: Date) -> Flow<[UsageRecordEntity]> {
        let s = normalized(startDate)
        let e = normalized(endDate)
        return AsyncStream { continuation in
            continuation.yield(items.filter { $0.date >= s && $0.date <= e }.sorted(by: { $0.date < $1.date }))
            continuation.onTermination = { _ in }
        }
    }

    public func deleteAllRecords() async {
        items.removeAll()
        byDateRelay.emit(items)
    }

    public func deleteRecordsForDate(date: Date) async {
        let d = normalized(date)
        items.removeAll(where: { $0.date == d })
        byDateRelay.emit(items)
    }
}

public actor InMemoryTemporaryUsageDao: TemporaryUsageDao {
    private var items: [TemporaryUsageEntity] = []

    public init() {}

    public func getActiveTemporaryUsage(packageName: String, currentTime: Milliseconds) async -> TemporaryUsageEntity? {
        items
            .filter { $0.packageName == packageName && $0.isActive && $0.endTime > currentTime }
            .sorted(by: { $0.startTime > $1.startTime })
            .first
    }

    public func getActiveTemporaryUsageFlow(packageName: String, currentTime: Milliseconds) -> Flow<TemporaryUsageEntity?> {
        AsyncStream { continuation in
            continuation.yield(
                items
                    .filter { $0.packageName == packageName && $0.isActive && $0.endTime > currentTime }
                    .sorted(by: { $0.startTime > $1.startTime })
                    .first
            )
            continuation.onTermination = { _ in }
        }
    }

    public func getAllTemporaryUsages() async -> [TemporaryUsageEntity] {
        items.sorted(by: { $0.createdAt > $1.createdAt })
    }

    public func getTemporaryUsagesForPackage(packageName: String) async -> [TemporaryUsageEntity] {
        items.filter { $0.packageName == packageName }.sorted(by: { $0.createdAt > $1.createdAt })
    }

    public func insertTemporaryUsage(temporaryUsage: TemporaryUsageEntity) async {
        items.removeAll(where: { $0.id == temporaryUsage.id })
        items.append(temporaryUsage)
    }

    public func updateTemporaryUsage(temporaryUsage: TemporaryUsageEntity) async {
        await insertTemporaryUsage(temporaryUsage: temporaryUsage)
    }

    public func deactivateTemporaryUsage(id: String) async {
        items = items.map {
            guard $0.id == id else { return $0 }
            var copy = $0
            copy.isActive = false
            return copy
        }
    }

    public func deactivateExpiredUsages(currentTime: Milliseconds) async {
        items = items.map {
            guard $0.endTime < currentTime else { return $0 }
            var copy = $0
            copy.isActive = false
            return copy
        }
    }

    public func deleteOldRecords(beforeTime: Milliseconds) async {
        items.removeAll(where: { $0.createdAt < beforeTime })
    }
}

public actor InMemoryUsageSessionDao: UsageSessionDao {
    private var items: [UsageSessionEntity] = []

    public init() {}

    public func insertSession(session: UsageSessionEntity) async {
        items.removeAll(where: { $0.id == session.id })
        items.append(session)
    }

    public func updateSession(session: UsageSessionEntity) async {
        await insertSession(session: session)
    }

    public func getSessionsForRecord(recordId: String) async -> [UsageSessionEntity] {
        items.filter { $0.recordId == recordId }.sorted(by: { $0.startTime > $1.startTime })
    }

    public func getSessionsForPackage(packageName: String) async -> [UsageSessionEntity] {
        items.filter { $0.packageName == packageName }.sorted(by: { $0.startTime > $1.startTime })
    }

    public func getActiveSession(packageName: String) async -> UsageSessionEntity? {
        items.first(where: { $0.packageName == packageName && $0.endTime == nil })
    }

    public func getSessionsInRange(startTime: Milliseconds, endTime: Milliseconds) async -> [UsageSessionEntity] {
        items.filter { $0.startTime >= startTime && $0.startTime <= endTime }
            .sorted(by: { $0.startTime > $1.startTime })
    }

    public func deleteSessionsForPackage(packageName: String) async {
        items.removeAll(where: { $0.packageName == packageName })
    }

    public func deleteSessionsBefore(timestamp: Milliseconds) async {
        items.removeAll(where: { $0.startTime < timestamp })
    }
}

public actor InMemoryBlockEventDao: BlockEventDao {
    private var items: [BlockEventEntity] = []

    public init() {}

    public func insertBlockEvent(event: BlockEventEntity) async {
        items.removeAll(where: { $0.id == event.id })
        items.append(event)
    }

    public func getAllBlockEventsFlow() -> Flow<[BlockEventEntity]> {
        AsyncStream { continuation in
            continuation.yield(items.sorted(by: { $0.timestamp > $1.timestamp }))
            continuation.onTermination = { _ in }
        }
    }

    public func getBlockEventsForPackage(packageName: String) async -> [BlockEventEntity] {
        items.filter { $0.packageName == packageName }.sorted(by: { $0.timestamp > $1.timestamp })
    }

    public func getBlockEventsInRange(startTime: Milliseconds, endTime: Milliseconds) async -> [BlockEventEntity] {
        items.filter { $0.timestamp >= startTime && $0.timestamp <= endTime }
            .sorted(by: { $0.timestamp > $1.timestamp })
    }

    public func getTodayBlockEvents(startOfDay: Milliseconds) async -> [BlockEventEntity] {
        items.filter { $0.timestamp >= startOfDay }.sorted(by: { $0.timestamp > $1.timestamp })
    }

    public func getTodayBlockEventsFlow(startOfDay: Milliseconds) -> Flow<[BlockEventEntity]> {
        AsyncStream { continuation in
            continuation.yield(items.filter { $0.timestamp >= startOfDay }.sorted(by: { $0.timestamp > $1.timestamp }))
            continuation.onTermination = { _ in }
        }
    }

    public func getBlockEventsBySource(source: String) async -> [BlockEventEntity] {
        items.filter { $0.blockSource == source }.sorted(by: { $0.timestamp > $1.timestamp })
    }

    public func getTodayBlockCount(startOfDay: Milliseconds) async -> Int {
        items.filter { $0.timestamp >= startOfDay }.count
    }

    public func getTodayBlockCountForPackage(packageName: String, startOfDay: Milliseconds) async -> Int {
        items.filter { $0.packageName == packageName && $0.timestamp >= startOfDay }.count
    }

    public func deleteBlockEventsBefore(timestamp: Milliseconds) async {
        items.removeAll(where: { $0.timestamp < timestamp })
    }

    public func deleteAllBlockEvents() async {
        items.removeAll()
    }
}

public final class FocusDatabase: Sendable {
    public static let DATABASE_NAME = "focus_database"

    private let _focusStrategyDao: FocusStrategyDao
    private let _focusModeDao: FocusModeDao
    private let _usageRecordDao: UsageRecordDao
    private let _temporaryUsageDao: TemporaryUsageDao
    private let _usageSessionDao: UsageSessionDao
    private let _blockEventDao: BlockEventDao

    public init(
        focusStrategyDao: FocusStrategyDao = InMemoryFocusStrategyDao(),
        focusModeDao: FocusModeDao = InMemoryFocusModeDao(),
        usageRecordDao: UsageRecordDao = InMemoryUsageRecordDao(),
        temporaryUsageDao: TemporaryUsageDao = InMemoryTemporaryUsageDao(),
        usageSessionDao: UsageSessionDao = InMemoryUsageSessionDao(),
        blockEventDao: BlockEventDao = InMemoryBlockEventDao()
    ) {
        self._focusStrategyDao = focusStrategyDao
        self._focusModeDao = focusModeDao
        self._usageRecordDao = usageRecordDao
        self._temporaryUsageDao = temporaryUsageDao
        self._usageSessionDao = usageSessionDao
        self._blockEventDao = blockEventDao
    }

    public func focusStrategyDao() -> FocusStrategyDao { _focusStrategyDao }
    public func focusModeDao() -> FocusModeDao { _focusModeDao }
    public func usageRecordDao() -> UsageRecordDao { _usageRecordDao }
    public func temporaryUsageDao() -> TemporaryUsageDao { _temporaryUsageDao }
    public func usageSessionDao() -> UsageSessionDao { _usageSessionDao }
    public func blockEventDao() -> BlockEventDao { _blockEventDao }
}
