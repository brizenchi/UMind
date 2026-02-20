import Foundation

public typealias Milliseconds = Int64

public struct ClockTime: Codable, Hashable, Comparable, Sendable {
    public var hour: Int
    public var minute: Int

    public init(hour: Int, minute: Int) {
        self.hour = max(0, min(23, hour))
        self.minute = max(0, min(59, minute))
    }

    public static func < (lhs: ClockTime, rhs: ClockTime) -> Bool {
        if lhs.hour != rhs.hour { return lhs.hour < rhs.hour }
        return lhs.minute < rhs.minute
    }

    public var description: String {
        String(format: "%02d:%02d", hour, minute)
    }
}

public enum DayOfWeek: Int, CaseIterable, Codable, Comparable, Sendable {
    case monday = 1
    case tuesday = 2
    case wednesday = 3
    case thursday = 4
    case friday = 5
    case saturday = 6
    case sunday = 7

    public static func < (lhs: DayOfWeek, rhs: DayOfWeek) -> Bool {
        lhs.rawValue < rhs.rawValue
    }

    public var zhName: String {
        switch self {
        case .monday: return "周一"
        case .tuesday: return "周二"
        case .wednesday: return "周三"
        case .thursday: return "周四"
        case .friday: return "周五"
        case .saturday: return "周六"
        case .sunday: return "周日"
        }
    }
}

public struct TimeRestriction: Codable, Hashable, Sendable {
    public var id: String
    public var daysOfWeek: Set<DayOfWeek>
    public var startTime: ClockTime
    public var endTime: ClockTime

    public init(
        id: String,
        daysOfWeek: Set<DayOfWeek>,
        startTime: ClockTime,
        endTime: ClockTime
    ) {
        self.id = id
        self.daysOfWeek = daysOfWeek
        self.startTime = startTime
        self.endTime = endTime
    }

    public func isWithinRestriction() -> Bool {
        let now = Date()
        let cal = Calendar.current
        let weekday = cal.component(.weekday, from: now)
        let mappedDay: DayOfWeek
        switch weekday {
        case 2: mappedDay = .monday
        case 3: mappedDay = .tuesday
        case 4: mappedDay = .wednesday
        case 5: mappedDay = .thursday
        case 6: mappedDay = .friday
        case 7: mappedDay = .saturday
        default: mappedDay = .sunday
        }

        guard daysOfWeek.contains(mappedDay) else { return false }

        let components = cal.dateComponents([.hour, .minute], from: now)
        let current = ClockTime(hour: components.hour ?? 0, minute: components.minute ?? 0)

        if startTime <= endTime {
            return current >= startTime && current <= endTime
        }
        return current >= startTime || current <= endTime
    }

    public func getTimeRangeString() -> String {
        "\(startTime.description) - \(endTime.description)"
    }

    public func getDaysString() -> String {
        daysOfWeek.sorted().map(\.zhName).joined(separator: ", ")
    }
}

public enum LimitType: String, Codable, Sendable {
    case totalAll = "TOTAL_ALL"
    case perApp = "PER_APP"
    case individual = "INDIVIDUAL"
}

public struct UsageLimits: Codable, Hashable, Sendable {
    public var type: LimitType
    public var totalLimitMillis: Milliseconds?
    public var perAppLimitMillis: Milliseconds?
    public var individualLimitsMillis: [String: Milliseconds]

    public init(
        type: LimitType,
        totalLimitMillis: Milliseconds? = nil,
        perAppLimitMillis: Milliseconds? = nil,
        individualLimitsMillis: [String: Milliseconds] = [:]
    ) {
        self.type = type
        self.totalLimitMillis = totalLimitMillis
        self.perAppLimitMillis = perAppLimitMillis
        self.individualLimitsMillis = individualLimitsMillis
    }

    public func getLimitFor(packageName: String) -> Milliseconds? {
        switch type {
        case .totalAll: return totalLimitMillis
        case .perApp: return perAppLimitMillis
        case .individual: return individualLimitsMillis[packageName]
        }
    }

    public func isValid() -> Bool {
        switch type {
        case .totalAll:
            return (totalLimitMillis ?? 0) > 0
        case .perApp:
            return (perAppLimitMillis ?? 0) > 0
        case .individual:
            return !individualLimitsMillis.isEmpty && individualLimitsMillis.values.allSatisfy { $0 > 0 }
        }
    }
}

public struct OpenCountLimits: Codable, Hashable, Sendable {
    public var type: LimitType
    public var totalCount: Int?
    public var perAppCount: Int?
    public var individualCounts: [String: Int]

    public init(
        type: LimitType,
        totalCount: Int? = nil,
        perAppCount: Int? = nil,
        individualCounts: [String: Int] = [:]
    ) {
        self.type = type
        self.totalCount = totalCount
        self.perAppCount = perAppCount
        self.individualCounts = individualCounts
    }

    public func getLimitFor(packageName: String) -> Int? {
        switch type {
        case .totalAll: return totalCount
        case .perApp: return perAppCount
        case .individual: return individualCounts[packageName]
        }
    }

    public func isValid() -> Bool {
        switch type {
        case .totalAll:
            return (totalCount ?? 0) > 0
        case .perApp:
            return (perAppCount ?? 0) > 0
        case .individual:
            return !individualCounts.isEmpty && individualCounts.values.allSatisfy { $0 > 0 }
        }
    }
}

public enum EnforcementMode: String, Codable, Sendable {
    case monitorOnly = "MONITOR_ONLY"
    case directBlock = "DIRECT_BLOCK"
    case forceThroughApp = "FORCE_THROUGH_APP"

    public var strictness: Int {
        switch self {
        case .monitorOnly: return 1
        case .directBlock: return 2
        case .forceThroughApp: return 3
        }
    }

    public func getDisplayName() -> String {
        switch self {
        case .monitorOnly: return "仅监控"
        case .directBlock: return "直接阻止"
        case .forceThroughApp: return "强制通过本应用"
        }
    }

    public func getDescription() -> String {
        switch self {
        case .monitorOnly: return "追踪和记录应用使用情况，不阻止使用"
        case .directBlock: return "通过系统监控直接阻止应用启动"
        case .forceThroughApp: return "必须通过 UMind 应用内打开目标应用"
        }
    }
}

public struct FocusStrategy: Codable, Hashable, Sendable {
    public var id: String
    public var name: String
    public var targetApps: Set<String>
    public var timeRestrictions: [TimeRestriction]
    public var usageLimits: UsageLimits?
    public var openCountLimits: OpenCountLimits?
    public var enforcementMode: EnforcementMode
    public var isActive: Bool
    public var createdAt: Milliseconds
    public var updatedAt: Milliseconds

    public init(
        id: String,
        name: String,
        targetApps: Set<String>,
        timeRestrictions: [TimeRestriction] = [],
        usageLimits: UsageLimits? = nil,
        openCountLimits: OpenCountLimits? = nil,
        enforcementMode: EnforcementMode = .directBlock,
        isActive: Bool = false,
        createdAt: Milliseconds = nowMillis(),
        updatedAt: Milliseconds = nowMillis()
    ) {
        self.id = id
        self.name = name
        self.targetApps = targetApps
        self.timeRestrictions = timeRestrictions
        self.usageLimits = usageLimits
        self.openCountLimits = openCountLimits
        self.enforcementMode = enforcementMode
        self.isActive = isActive
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }

    public func isWithinFocusTime() -> Bool {
        guard !timeRestrictions.isEmpty else { return false }
        return timeRestrictions.contains(where: { $0.isWithinRestriction() })
    }

    public func hasRestrictions() -> Bool {
        !timeRestrictions.isEmpty || usageLimits != nil || openCountLimits != nil
    }

    public func getRestrictionSummary() -> String {
        var parts: [String] = []
        if !timeRestrictions.isEmpty { parts.append("\(timeRestrictions.count)个时间段") }
        if usageLimits != nil { parts.append("时长限制") }
        if openCountLimits != nil { parts.append("次数限制") }
        return parts.isEmpty ? "无限制" : parts.joined(separator: " | ")
    }
}

public enum FocusModeType: String, Codable, Sendable {
    case manual = "MANUAL"
    case countdown = "COUNTDOWN"
    case scheduled = "SCHEDULED"
}

public struct FocusMode: Codable, Hashable, Sendable {
    public var id: String
    public var isEnabled: Bool
    public var whitelistedApps: Set<String>
    public var modeType: FocusModeType
    public var countdownEndTime: Milliseconds?
    public var scheduledTimeRanges: [TimeRestriction]
    public var updatedAt: Milliseconds

    public init(
        id: String = "focus_mode_singleton",
        isEnabled: Bool = false,
        whitelistedApps: Set<String> = [],
        modeType: FocusModeType = .manual,
        countdownEndTime: Milliseconds? = nil,
        scheduledTimeRanges: [TimeRestriction] = [],
        updatedAt: Milliseconds = nowMillis()
    ) {
        self.id = id
        self.isEnabled = isEnabled
        self.whitelistedApps = whitelistedApps
        self.modeType = modeType
        self.countdownEndTime = countdownEndTime
        self.scheduledTimeRanges = scheduledTimeRanges
        self.updatedAt = updatedAt
    }

    public func isAppAllowed(packageName: String) -> Bool {
        whitelistedApps.contains(packageName)
    }

    public func shouldBeActive() -> Bool {
        guard isEnabled else { return false }
        switch modeType {
        case .manual:
            return true
        case .countdown:
            guard let end = countdownEndTime else { return false }
            return nowMillis() < end
        case .scheduled:
            return scheduledTimeRanges.contains(where: { $0.isWithinRestriction() })
        }
    }

    public func getRemainingMinutes() -> Int? {
        guard modeType == .countdown, let end = countdownEndTime else { return nil }
        let remaining = end - nowMillis()
        return remaining > 0 ? Int(remaining / 60_000) : 0
    }

    public func getSummary() -> String {
        guard isEnabled else { return "未开启" }
        switch modeType {
        case .manual:
            return "已开启 · \(whitelistedApps.count)个允许应用"
        case .countdown:
            let remaining = getRemainingMinutes() ?? 0
            return remaining > 0 ? "倒计时 · 剩余\(remaining)分钟" : "倒计时已结束"
        case .scheduled:
            return "定时模式 · \(scheduledTimeRanges.count)个时间段"
        }
    }
}

public struct TemporaryUsage: Codable, Hashable, Sendable {
    public var id: String
    public var packageName: String
    public var appName: String
    public var reason: String
    public var requestedDurationMinutes: Int
    public var actualDurationMillis: Milliseconds
    public var startTime: Milliseconds
    public var endTime: Milliseconds
    public var isActive: Bool
    public var createdAt: Milliseconds

    public init(
        id: String,
        packageName: String,
        appName: String,
        reason: String,
        requestedDurationMinutes: Int,
        actualDurationMillis: Milliseconds = 0,
        startTime: Milliseconds,
        endTime: Milliseconds,
        isActive: Bool = true,
        createdAt: Milliseconds = nowMillis()
    ) {
        self.id = id
        self.packageName = packageName
        self.appName = appName
        self.reason = reason
        self.requestedDurationMinutes = requestedDurationMinutes
        self.actualDurationMillis = actualDurationMillis
        self.startTime = startTime
        self.endTime = endTime
        self.isActive = isActive
        self.createdAt = createdAt
    }

    public func isValid() -> Bool {
        isActive && nowMillis() < endTime
    }

    public func getRemainingTimeMillis() -> Milliseconds {
        isValid() ? max(0, endTime - nowMillis()) : 0
    }
}

public enum BlockReason: Hashable, Sendable {
    case timeRestriction(nextAvailableTime: String?)
    case usageLimitExceeded(limitMinutes: Int64, usedMinutes: Int64)
    case openCountLimitExceeded(limitCount: Int, usedCount: Int)
    case focusModeActive
    case forceThroughApp
}

public struct UsageInfo: Hashable, Sendable {
    public var usageLimitMinutes: Int64?
    public var usedMinutes: Int64
    public var remainingMinutes: Int64?
    public var openCountLimit: Int?
    public var openCount: Int
    public var remainingCount: Int?

    public init(
        usageLimitMinutes: Int64? = nil,
        usedMinutes: Int64 = 0,
        remainingMinutes: Int64? = nil,
        openCountLimit: Int? = nil,
        openCount: Int = 0,
        remainingCount: Int? = nil
    ) {
        self.usageLimitMinutes = usageLimitMinutes
        self.usedMinutes = usedMinutes
        self.remainingMinutes = remainingMinutes
        self.openCountLimit = openCountLimit
        self.openCount = openCount
        self.remainingCount = remainingCount
    }
}

public struct BlockInfo: Hashable, Sendable {
    public var shouldBlock: Bool
    public var reasons: [BlockReason]
    public var usageInfo: UsageInfo?

    public init(shouldBlock: Bool, reasons: [BlockReason] = [], usageInfo: UsageInfo? = nil) {
        self.shouldBlock = shouldBlock
        self.reasons = reasons
        self.usageInfo = usageInfo
    }
}

public struct AppInfo: Hashable, Sendable {
    public var packageName: String
    public var label: String
    public var iconData: Data?
    public var isSystemApp: Bool

    public init(packageName: String, label: String, iconData: Data? = nil, isSystemApp: Bool = false) {
        self.packageName = packageName
        self.label = label
        self.iconData = iconData
        self.isSystemApp = isSystemApp
    }
}

public struct DailyStats: Hashable, Sendable {
    public var date: Date
    public var totalUsageDurationMillis: Milliseconds
    public var totalOpenCount: Int
    public var totalBlockCount: Int
    public var appUsageStats: [AppUsageStats]

    public init(
        date: Date,
        totalUsageDurationMillis: Milliseconds,
        totalOpenCount: Int,
        totalBlockCount: Int,
        appUsageStats: [AppUsageStats]
    ) {
        self.date = date
        self.totalUsageDurationMillis = totalUsageDurationMillis
        self.totalOpenCount = totalOpenCount
        self.totalBlockCount = totalBlockCount
        self.appUsageStats = appUsageStats
    }
}

public struct AppUsageStats: Hashable, Sendable {
    public var packageName: String
    public var appName: String
    public var usageDurationMillis: Milliseconds
    public var openCount: Int
    public var blockCount: Int

    public init(
        packageName: String,
        appName: String,
        usageDurationMillis: Milliseconds,
        openCount: Int,
        blockCount: Int = 0
    ) {
        self.packageName = packageName
        self.appName = appName
        self.usageDurationMillis = usageDurationMillis
        self.openCount = openCount
        self.blockCount = blockCount
    }
}

public struct UsageTrend: Hashable, Sendable {
    public var date: Date
    public var totalUsageDurationMillis: Milliseconds
    public var totalOpenCount: Int

    public init(date: Date, totalUsageDurationMillis: Milliseconds, totalOpenCount: Int) {
        self.date = date
        self.totalUsageDurationMillis = totalUsageDurationMillis
        self.totalOpenCount = totalOpenCount
    }
}

public enum TimelineEntryType: String, Sendable {
    case appUsage = "APP_USAGE"
    case temporaryUsage = "TEMPORARY_USAGE"
    case blocked = "BLOCKED"
}

public struct UsageTimelineEntry: Hashable, Sendable {
    public var timestamp: Milliseconds
    public var packageName: String
    public var appName: String
    public var durationMillis: Milliseconds
    public var type: TimelineEntryType

    public init(
        timestamp: Milliseconds,
        packageName: String,
        appName: String,
        durationMillis: Milliseconds,
        type: TimelineEntryType
    ) {
        self.timestamp = timestamp
        self.packageName = packageName
        self.appName = appName
        self.durationMillis = durationMillis
        self.type = type
    }
}

public enum UMindResult<Value> {
    case success(Value)
    case error(Error, message: String?)
    case loading

    public var isSuccess: Bool {
        if case .success = self { return true }
        return false
    }

    public var isError: Bool {
        if case .error = self { return true }
        return false
    }

    public var isLoading: Bool {
        if case .loading = self { return true }
        return false
    }

    public func getOrNull() -> Value? {
        if case let .success(value) = self { return value }
        return nil
    }

    public func exceptionOrNull() -> Error? {
        if case let .error(error, _) = self { return error }
        return nil
    }
}

public func nowMillis() -> Milliseconds {
    Milliseconds(Date().timeIntervalSince1970 * 1000)
}
