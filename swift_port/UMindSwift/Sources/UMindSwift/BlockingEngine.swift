import Foundation

public struct MergedRestriction: Sendable {
    public var timeRestrictions: [TimeRestriction]
    public var usageLimit: Milliseconds?
    public var openCountLimit: Int?
    public var enforcementMode: EnforcementMode

    public init(
        timeRestrictions: [TimeRestriction],
        usageLimit: Milliseconds?,
        openCountLimit: Int?,
        enforcementMode: EnforcementMode
    ) {
        self.timeRestrictions = timeRestrictions
        self.usageLimit = usageLimit
        self.openCountLimit = openCountLimit
        self.enforcementMode = enforcementMode
    }
}

public final class BlockingEngine: @unchecked Sendable {
    private let focusModeRepository: FocusModeRepository
    private let focusRepository: FocusRepository
    private let usageTrackingRepository: UsageTrackingRepository

    private let systemWhitelist: Set<String> = [
        "android",
        "com.android.systemui",
        "com.android.launcher3",
        "com.android.settings",
        "com.android.phone",
        "com.android.vending",
        "com.google.android.gms",
        "com.example.umind"
    ]

    public init(
        focusModeRepository: FocusModeRepository,
        focusRepository: FocusRepository,
        usageTrackingRepository: UsageTrackingRepository
    ) {
        self.focusModeRepository = focusModeRepository
        self.focusRepository = focusRepository
        self.usageTrackingRepository = usageTrackingRepository
    }

    public func getBlockInfo(packageName: String, openedFromUMind: Bool = false) async -> BlockInfo {
        if isSystemWhitelistedApp(packageName: packageName) {
            return BlockInfo(shouldBlock: false)
        }

        if let focusModeBlockInfo = await checkFocusMode(packageName: packageName) {
            return focusModeBlockInfo
        }

        return await checkDailyManagement(packageName: packageName, openedFromUMind: openedFromUMind)
    }

    private func isSystemWhitelistedApp(packageName: String) -> Bool {
        systemWhitelist.contains(packageName) || packageName.hasPrefix("com.example.umind")
    }

    private func checkFocusMode(packageName: String) async -> BlockInfo? {
        let focusMode = await focusModeRepository.getFocusModeOnce()

        guard focusMode.shouldBeActive() else {
            return nil
        }

        if focusMode.isAppAllowed(packageName: packageName) {
            return nil
        }

        return BlockInfo(
            shouldBlock: true,
            reasons: [.focusModeActive],
            usageInfo: nil
        )
    }

    private func checkDailyManagement(packageName: String, openedFromUMind: Bool) async -> BlockInfo {
        guard case let .success(strategies) = await focusRepository.getFocusStrategies() else {
            return BlockInfo(shouldBlock: false)
        }

        let activeStrategies = strategies.filter(\.isActive)
        let relevantStrategies = activeStrategies.filter { $0.targetApps.contains(packageName) }

        guard !relevantStrategies.isEmpty else {
            return BlockInfo(shouldBlock: false)
        }

        let mergedRestriction = mergeStrategies(strategies: relevantStrategies, packageName: packageName)

        switch mergedRestriction.enforcementMode {
        case .monitorOnly:
            return BlockInfo(
                shouldBlock: false,
                reasons: [],
                usageInfo: await calculateUsageInfo(packageName: packageName, merged: mergedRestriction)
            )
        case .directBlock:
            return await checkAllRestrictions(packageName: packageName, merged: mergedRestriction)
        case .forceThroughApp:
            if !openedFromUMind {
                return BlockInfo(shouldBlock: true, reasons: [.forceThroughApp], usageInfo: nil)
            }
            return await checkAllRestrictions(packageName: packageName, merged: mergedRestriction)
        }
    }

    private func mergeStrategies(strategies: [FocusStrategy], packageName: String) -> MergedRestriction {
        let mergedTimeRestrictions = strategies.flatMap(\.timeRestrictions)

        let usageLimits: [Milliseconds] = strategies.compactMap { strategy in
            guard let limits = strategy.usageLimits else { return nil }
            switch limits.type {
            case .totalAll: return limits.totalLimitMillis
            case .perApp: return limits.perAppLimitMillis
            case .individual: return limits.individualLimitsMillis[packageName]
            }
        }

        let openCountLimits: [Int] = strategies.compactMap { strategy in
            guard let limits = strategy.openCountLimits else { return nil }
            switch limits.type {
            case .totalAll: return limits.totalCount
            case .perApp: return limits.perAppCount
            case .individual: return limits.individualCounts[packageName]
            }
        }

        let enforcementMode = strategies
            .map(\.enforcementMode)
            .max(by: { $0.strictness < $1.strictness }) ?? .monitorOnly

        return MergedRestriction(
            timeRestrictions: mergedTimeRestrictions,
            usageLimit: usageLimits.min(),
            openCountLimit: openCountLimits.min(),
            enforcementMode: enforcementMode
        )
    }

    private func checkAllRestrictions(packageName: String, merged: MergedRestriction) async -> BlockInfo {
        let today = Calendar.current.startOfDay(for: Date())
        var reasons: [BlockReason] = []

        let withinTimeRestriction = merged.timeRestrictions.contains(where: { $0.isWithinRestriction() })
        if withinTimeRestriction {
            let nextAvailableTime = merged.timeRestrictions.map { $0.endTime.description }.min()
            reasons.append(.timeRestriction(nextAvailableTime: nextAvailableTime))
        }

        var usageLimitMinutes: Int64?
        var usedMinutes: Int64 = 0
        var remainingMinutes: Int64?

        if let usageLimit = merged.usageLimit {
            let appUsage = await usageTrackingRepository.getUsageDuration(packageName: packageName, date: today)
            let remaining = usageLimit - appUsage

            usageLimitMinutes = usageLimit / 60_000
            usedMinutes = appUsage / 60_000
            remainingMinutes = max(0, remaining / 60_000)

            if remaining <= 0 {
                reasons.append(
                    .usageLimitExceeded(
                        limitMinutes: usageLimitMinutes ?? 0,
                        usedMinutes: usedMinutes
                    )
                )
            }
        }

        var openCount: Int = 0
        var remainingCount: Int?

        if let openCountLimit = merged.openCountLimit {
            openCount = await usageTrackingRepository.getOpenCount(packageName: packageName, date: today)
            remainingCount = max(0, openCountLimit - openCount)

            if openCount >= openCountLimit {
                reasons.append(.openCountLimitExceeded(limitCount: openCountLimit, usedCount: openCount))
            }
        }

        let usageInfo: UsageInfo?
        if usageLimitMinutes != nil || merged.openCountLimit != nil {
            usageInfo = UsageInfo(
                usageLimitMinutes: usageLimitMinutes,
                usedMinutes: usedMinutes,
                remainingMinutes: remainingMinutes,
                openCountLimit: merged.openCountLimit,
                openCount: openCount,
                remainingCount: remainingCount
            )
        } else {
            usageInfo = nil
        }

        return BlockInfo(
            shouldBlock: !reasons.isEmpty,
            reasons: reasons,
            usageInfo: usageInfo
        )
    }

    private func calculateUsageInfo(packageName: String, merged: MergedRestriction) async -> UsageInfo? {
        let today = Calendar.current.startOfDay(for: Date())

        var usageLimitMinutes: Int64?
        var usedMinutes: Int64 = 0
        var remainingMinutes: Int64?
        var openCountLimit: Int?
        var openCount: Int = 0
        var remainingCount: Int?

        if let usageLimit = merged.usageLimit {
            let appUsage = await usageTrackingRepository.getUsageDuration(packageName: packageName, date: today)
            usageLimitMinutes = usageLimit / 60_000
            usedMinutes = appUsage / 60_000
            remainingMinutes = max(0, (usageLimit - appUsage) / 60_000)
        }

        if let limit = merged.openCountLimit {
            openCount = await usageTrackingRepository.getOpenCount(packageName: packageName, date: today)
            openCountLimit = limit
            remainingCount = max(0, limit - openCount)
        }

        if usageLimitMinutes == nil && openCountLimit == nil {
            return nil
        }

        return UsageInfo(
            usageLimitMinutes: usageLimitMinutes,
            usedMinutes: usedMinutes,
            remainingMinutes: remainingMinutes,
            openCountLimit: openCountLimit,
            openCount: openCount,
            remainingCount: remainingCount
        )
    }
}
