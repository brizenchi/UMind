import Foundation

public struct FocusStrategyEntity: Codable, Hashable, Sendable {
    public var id: String
    public var name: String
    public var targetApps: Set<String>
    public var timeRestrictions: [TimeRestrictionData]
    public var usageLimits: UsageLimitsData?
    public var openCountLimits: OpenCountLimitsData?
    public var enforcementMode: EnforcementMode
    public var isActive: Bool
    public var createdAt: Milliseconds
    public var updatedAt: Milliseconds

    public init(
        id: String,
        name: String,
        targetApps: Set<String>,
        timeRestrictions: [TimeRestrictionData],
        usageLimits: UsageLimitsData?,
        openCountLimits: OpenCountLimitsData?,
        enforcementMode: EnforcementMode,
        isActive: Bool,
        createdAt: Milliseconds,
        updatedAt: Milliseconds
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

    public func toDomainModel() -> FocusStrategy {
        FocusStrategy(
            id: id,
            name: name,
            targetApps: targetApps,
            timeRestrictions: timeRestrictions.map { $0.toDomainModel() },
            usageLimits: usageLimits?.toDomainModel(),
            openCountLimits: openCountLimits?.toDomainModel(),
            enforcementMode: enforcementMode,
            isActive: isActive,
            createdAt: createdAt,
            updatedAt: updatedAt
        )
    }

    public static func fromDomainModel(strategy: FocusStrategy) -> FocusStrategyEntity {
        FocusStrategyEntity(
            id: strategy.id,
            name: strategy.name,
            targetApps: strategy.targetApps,
            timeRestrictions: strategy.timeRestrictions.map { TimeRestrictionData.fromDomainModel(restriction: $0) },
            usageLimits: strategy.usageLimits.map { UsageLimitsData.fromDomainModel(limits: $0) },
            openCountLimits: strategy.openCountLimits.map { OpenCountLimitsData.fromDomainModel(limits: $0) },
            enforcementMode: strategy.enforcementMode,
            isActive: strategy.isActive,
            createdAt: strategy.createdAt,
            updatedAt: strategy.updatedAt
        )
    }
}

public struct TimeRestrictionData: Codable, Hashable, Sendable {
    public var id: String
    public var daysOfWeek: [DayOfWeek]
    public var startHour: Int
    public var startMinute: Int
    public var endHour: Int
    public var endMinute: Int

    public init(
        id: String,
        daysOfWeek: [DayOfWeek],
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ) {
        self.id = id
        self.daysOfWeek = daysOfWeek
        self.startHour = startHour
        self.startMinute = startMinute
        self.endHour = endHour
        self.endMinute = endMinute
    }

    public func toDomainModel() -> TimeRestriction {
        TimeRestriction(
            id: id,
            daysOfWeek: Set(daysOfWeek),
            startTime: ClockTime(hour: startHour, minute: startMinute),
            endTime: ClockTime(hour: endHour, minute: endMinute)
        )
    }

    public static func fromDomainModel(restriction: TimeRestriction) -> TimeRestrictionData {
        TimeRestrictionData(
            id: restriction.id,
            daysOfWeek: restriction.daysOfWeek.sorted(),
            startHour: restriction.startTime.hour,
            startMinute: restriction.startTime.minute,
            endHour: restriction.endTime.hour,
            endMinute: restriction.endTime.minute
        )
    }
}

public struct UsageLimitsData: Codable, Hashable, Sendable {
    public var type: LimitType
    public var totalLimitMinutes: Int64?
    public var perAppLimitMinutes: Int64?
    public var individualLimitsMinutes: [String: Int64]

    public init(
        type: LimitType,
        totalLimitMinutes: Int64?,
        perAppLimitMinutes: Int64?,
        individualLimitsMinutes: [String: Int64]
    ) {
        self.type = type
        self.totalLimitMinutes = totalLimitMinutes
        self.perAppLimitMinutes = perAppLimitMinutes
        self.individualLimitsMinutes = individualLimitsMinutes
    }

    public func toDomainModel() -> UsageLimits {
        UsageLimits(
            type: type,
            totalLimitMillis: totalLimitMinutes.map { $0 * 60_000 },
            perAppLimitMillis: perAppLimitMinutes.map { $0 * 60_000 },
            individualLimitsMillis: individualLimitsMinutes.mapValues { $0 * 60_000 }
        )
    }

    public static func fromDomainModel(limits: UsageLimits) -> UsageLimitsData {
        UsageLimitsData(
            type: limits.type,
            totalLimitMinutes: limits.totalLimitMillis.map { $0 / 60_000 },
            perAppLimitMinutes: limits.perAppLimitMillis.map { $0 / 60_000 },
            individualLimitsMinutes: limits.individualLimitsMillis.mapValues { $0 / 60_000 }
        )
    }
}

public struct OpenCountLimitsData: Codable, Hashable, Sendable {
    public var type: LimitType
    public var totalCount: Int?
    public var perAppCount: Int?
    public var individualCounts: [String: Int]

    public init(type: LimitType, totalCount: Int?, perAppCount: Int?, individualCounts: [String: Int]) {
        self.type = type
        self.totalCount = totalCount
        self.perAppCount = perAppCount
        self.individualCounts = individualCounts
    }

    public func toDomainModel() -> OpenCountLimits {
        OpenCountLimits(
            type: type,
            totalCount: totalCount,
            perAppCount: perAppCount,
            individualCounts: individualCounts
        )
    }

    public static func fromDomainModel(limits: OpenCountLimits) -> OpenCountLimitsData {
        OpenCountLimitsData(
            type: limits.type,
            totalCount: limits.totalCount,
            perAppCount: limits.perAppCount,
            individualCounts: limits.individualCounts
        )
    }
}

public struct FocusModeEntity: Codable, Hashable, Sendable {
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

    public func toDomainModel() -> FocusMode {
        FocusMode(
            id: id,
            isEnabled: isEnabled,
            whitelistedApps: whitelistedApps,
            modeType: modeType,
            countdownEndTime: countdownEndTime,
            scheduledTimeRanges: scheduledTimeRanges,
            updatedAt: updatedAt
        )
    }
}

public struct TemporaryUsageEntity: Codable, Hashable, Sendable {
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
        actualDurationMillis: Milliseconds,
        startTime: Milliseconds,
        endTime: Milliseconds,
        isActive: Bool,
        createdAt: Milliseconds
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

    public func toDomainModel() -> TemporaryUsage {
        TemporaryUsage(
            id: id,
            packageName: packageName,
            appName: appName,
            reason: reason,
            requestedDurationMinutes: requestedDurationMinutes,
            actualDurationMillis: actualDurationMillis,
            startTime: startTime,
            endTime: endTime,
            isActive: isActive,
            createdAt: createdAt
        )
    }

    public static func fromDomainModel(temporaryUsage: TemporaryUsage) -> TemporaryUsageEntity {
        TemporaryUsageEntity(
            id: temporaryUsage.id,
            packageName: temporaryUsage.packageName,
            appName: temporaryUsage.appName,
            reason: temporaryUsage.reason,
            requestedDurationMinutes: temporaryUsage.requestedDurationMinutes,
            actualDurationMillis: temporaryUsage.actualDurationMillis,
            startTime: temporaryUsage.startTime,
            endTime: temporaryUsage.endTime,
            isActive: temporaryUsage.isActive,
            createdAt: temporaryUsage.createdAt
        )
    }
}

public struct UsageRecordEntity: Codable, Hashable, Sendable {
    public var id: Int64
    public var packageName: String
    public var date: Date
    public var usageDurationMillis: Milliseconds
    public var openCount: Int
    public var lastUpdated: Milliseconds

    public init(
        id: Int64 = 0,
        packageName: String,
        date: Date,
        usageDurationMillis: Milliseconds,
        openCount: Int,
        lastUpdated: Milliseconds = nowMillis()
    ) {
        self.id = id
        self.packageName = packageName
        self.date = Calendar.current.startOfDay(for: date)
        self.usageDurationMillis = usageDurationMillis
        self.openCount = openCount
        self.lastUpdated = lastUpdated
    }
}

public struct UsageSessionEntity: Codable, Hashable, Sendable {
    public var id: String
    public var recordId: String
    public var packageName: String
    public var startTime: Milliseconds
    public var endTime: Milliseconds?
    public var durationMillis: Milliseconds

    public init(
        id: String,
        recordId: String,
        packageName: String,
        startTime: Milliseconds,
        endTime: Milliseconds? = nil,
        durationMillis: Milliseconds = 0
    ) {
        self.id = id
        self.recordId = recordId
        self.packageName = packageName
        self.startTime = startTime
        self.endTime = endTime
        self.durationMillis = durationMillis
    }
}

public struct BlockEventEntity: Codable, Hashable, Sendable {
    public var id: String
    public var packageName: String
    public var appName: String
    public var timestamp: Milliseconds
    public var blockReason: String
    public var blockSource: String
    public var strategyIds: String?

    public init(
        id: String,
        packageName: String,
        appName: String,
        timestamp: Milliseconds,
        blockReason: String,
        blockSource: String,
        strategyIds: String? = nil
    ) {
        self.id = id
        self.packageName = packageName
        self.appName = appName
        self.timestamp = timestamp
        self.blockReason = blockReason
        self.blockSource = blockSource
        self.strategyIds = strategyIds
    }
}
