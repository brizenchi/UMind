import Foundation

public func formatDuration(millis: Milliseconds) -> String {
    if millis == 0 { return "0m" }

    let totalMinutes = Int(millis / 60_000)
    let hours = totalMinutes / 60
    let minutes = totalMinutes % 60

    switch (hours, minutes) {
    case let (h, m) where h > 0 && m > 0:
        return "\(h)h \(m)m"
    case let (h, _) where h > 0:
        return "\(h)h"
    default:
        return "\(minutes)m"
    }
}

public func formatDurationShort(millis: Milliseconds) -> String {
    let hours = Double(millis) / 3_600_000.0
    if hours >= 1 {
        let rounded = (hours * 10.0).rounded() / 10.0
        return "\(rounded)h"
    }
    return "\(millis / 60_000)m"
}

public func getUsagePercentage(usage: Milliseconds, maxUsage: Milliseconds) -> Double {
    guard maxUsage > 0 else { return 0 }
    return min(max(Double(usage) / Double(maxUsage), 0), 1)
}

public enum AccessibilityUtil {
    public static func isAccessibilityServiceEnabled(context: String, serviceName: String) -> Bool {
        let expected = "\(context)/\(serviceName)"
        return enabledServices.contains(expected.lowercased())
    }

    public static func openAccessibilitySettings(context: String) {
        lastOpenedSettingsContext = context
    }

    private static var enabledServices: Set<String> = []
    private static var lastOpenedSettingsContext: String?
}

public enum BatteryOptimizationHelper {
    private static var ignoredContexts: Set<String> = []

    public static func isIgnoringBatteryOptimizations(context: String) -> Bool {
        ignoredContexts.contains(context)
    }

    public static func requestIgnoreBatteryOptimizations(context: String) {
        ignoredContexts.insert(context)
    }

    public static func openBatteryOptimizationSettings(context: String) {
        _ = context
    }

    public static func getBatteryOptimizationStatusText(context: String) -> String {
        isIgnoringBatteryOptimizations(context: context) ? "✓ 已关闭电池优化" : "✗ 需要关闭电池优化"
    }
}

public enum MiuiDeviceHelper {
    public struct SetupStep: Hashable, Sendable {
        public var title: String
        public var description: String
        public var action: String

        public init(title: String, description: String, action: String) {
            self.title = title
            self.description = description
            self.action = action
        }
    }

    private static var manufacturer = "apple"
    private static var miuiVersion: String?

    public static func isMiuiDevice() -> Bool {
        let lowered = manufacturer.lowercased()
        return lowered == "xiaomi" || lowered == "redmi" || hasSystemProperty(key: "ro.miui.ui.version.name")
    }

    public static func getMiuiVersion() -> String? {
        getSystemProperty(key: "ro.miui.ui.version.name")
    }

    public static func hasSystemProperty(key: String) -> Bool {
        getSystemProperty(key: key) != nil
    }

    public static func getSystemProperty(key: String) -> String? {
        if key == "ro.miui.ui.version.name" {
            return miuiVersion
        }
        return nil
    }

    public static func openMiuiAutoStartSettings(context: String) -> Bool {
        _ = context
        return isMiuiDevice()
    }

    public static func openMiuiBatterySaverSettings(context: String) -> Bool {
        _ = context
        return isMiuiDevice() || openMiuiBatterySaverSettingsAlternative(context: context)
    }

    public static func openMiuiBatterySaverSettingsAlternative(context: String) -> Bool {
        _ = context
        return isMiuiDevice()
    }

    public static func openMiuiBackgroundPopupSettings(context: String) -> Bool {
        _ = context
        return isMiuiDevice()
    }

    public static func openAppDetailsSettings(context: String) -> Bool {
        !context.isEmpty
    }

    public static func getMiuiSetupGuide() -> [SetupStep] {
        [
            SetupStep(title: "关闭电池优化", description: "防止系统杀死后台服务", action: "battery_optimization"),
            SetupStep(title: "允许自启动", description: "允许应用在后台自动启动", action: "autostart"),
            SetupStep(title: "设置省电策略为无限制", description: "确保应用可以持续运行", action: "battery_saver"),
            SetupStep(title: "允许后台弹出界面", description: "允许应用显示阻止弹窗", action: "background_popup"),
            SetupStep(title: "锁定最近任务", description: "防止应用被清理", action: "lock_recent")
        ]
    }
}

public struct TimeRestrictionAdapter {
    public init() {}

    public func serialize(src: TimeRestriction?) -> String {
        guard let src else { return "null" }

        let payload: [String: Any] = [
            "id": src.id,
            "startTime": src.startTime.description,
            "endTime": src.endTime.description,
            "daysOfWeek": src.daysOfWeek.sorted().map(\.rawValue)
        ]

        guard let data = try? JSONSerialization.data(withJSONObject: payload),
              let text = String(data: data, encoding: .utf8) else {
            return "null"
        }
        return text
    }

    public func deserialize(json: String?) -> TimeRestriction {
        guard let json,
              let data = json.data(using: .utf8),
              let object = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            return TimeRestriction(id: UUID().uuidString, daysOfWeek: [.monday], startTime: ClockTime(hour: 9, minute: 0), endTime: ClockTime(hour: 18, minute: 0))
        }

        let id = object["id"] as? String ?? UUID().uuidString
        let startRaw = object["startTime"] as? String ?? "09:00"
        let endRaw = object["endTime"] as? String ?? "18:00"
        let daysRaw = object["daysOfWeek"] as? [Int] ?? [1]

        let parse: (String) -> ClockTime = { value in
            let parts = value.split(separator: ":")
            let hour = Int(parts.first ?? "9") ?? 9
            let minute = Int(parts.dropFirst().first ?? "0") ?? 0
            return ClockTime(hour: hour, minute: minute)
        }

        let days = Set(daysRaw.compactMap(DayOfWeek.init(rawValue:)))

        return TimeRestriction(
            id: id,
            daysOfWeek: days,
            startTime: parse(startRaw),
            endTime: parse(endRaw)
        )
    }
}

public final class Converters {
    private let adapter = TimeRestrictionAdapter()

    public init() {}

    public func fromLocalDate(date: Date?) -> String? {
        guard let date else { return nil }
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withFullDate]
        return formatter.string(from: date)
    }

    public func toLocalDate(dateString: String?) -> Date? {
        guard let dateString else { return nil }
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withFullDate]
        return formatter.date(from: dateString)
    }

    public func fromStringSet(value: Set<String>?) -> String? {
        value?.joined(separator: ",")
    }

    public func toStringSet(value: String?) -> Set<String>? {
        guard let value else { return nil }
        return Set(value.split(separator: ",").map(String.init).filter { !$0.isEmpty })
    }

    public func fromTimeRestrictionList(value: [TimeRestriction]?) -> String? {
        guard let value else { return nil }
        let jsonList = value.map { adapter.serialize(src: $0) }
        guard let data = try? JSONSerialization.data(withJSONObject: jsonList),
              let text = String(data: data, encoding: .utf8) else {
            return nil
        }
        return text
    }

    public func toTimeRestrictionList(value: String?) -> [TimeRestriction]? {
        guard let value,
              let data = value.data(using: .utf8),
              let list = try? JSONSerialization.jsonObject(with: data) as? [String] else {
            return nil
        }
        return list.map { adapter.deserialize(json: $0) }
    }

    public func fromFocusModeType(value: FocusModeType?) -> String? {
        value?.rawValue
    }

    public func toFocusModeType(value: String?) -> FocusModeType? {
        guard let value else { return nil }
        return FocusModeType(rawValue: value)
    }
}

public final class FocusModeCountdownManager: @unchecked Sendable {
    private let focusModeRepository: FocusModeRepository
    private var countdownTask: Task<Void, Never>?

    public init(focusModeRepository: FocusModeRepository) {
        self.focusModeRepository = focusModeRepository
        createNotificationChannel()
        startMonitoring()
    }

    private func createNotificationChannel() {
        // iOS adaptation: channel concept not required
    }

    private func startMonitoring() {
        countdownTask?.cancel()
        countdownTask = Task {
            while !Task.isCancelled {
                await checkCountdown()
                try? await Task.sleep(nanoseconds: 10_000_000_000)
            }
        }
    }

    private func checkCountdown() async {
        let focusMode = await focusModeRepository.getFocusModeOnce()
        guard focusMode.isEnabled else { return }

        switch focusMode.modeType {
        case .countdown:
            if let remaining = focusMode.getRemainingMinutes(), remaining <= 0 {
                await focusModeRepository.stopFocusMode()
                showCountdownFinishedNotification()
            }
        case .scheduled:
            if !focusMode.shouldBeActive() {
                await focusModeRepository.stopFocusMode()
            }
        case .manual:
            break
        }
    }

    private func showCountdownFinishedNotification() {
        // iOS adaptation: push local notification through app host
    }

    public func stop() {
        countdownTask?.cancel()
        countdownTask = nil
    }
}

public final class UsageCountdownManager: @unchecked Sendable {
    private struct CountdownState {
        var packageName: String
        var limitMillis: Milliseconds
        var usedMillis: Milliseconds
        var sessionStartTime: Milliseconds
        var isRunning: Bool
        var onUpdate: ((Milliseconds) -> Void)?
        var onTimeUp: (() -> Void)?
    }

    private let usageTrackingRepository: UsageTrackingRepository
    private var countdownStates: [String: CountdownState] = [:]
    private var tasks: [String: Task<Void, Never>] = [:]
    private let maxActiveCountdowns = 5

    public init(usageTrackingRepository: UsageTrackingRepository) {
        self.usageTrackingRepository = usageTrackingRepository
    }

    public func startCountdown(
        packageName: String,
        limitMillis: Milliseconds,
        usedMillis: Milliseconds,
        scope: Any? = nil,
        onUpdate: @escaping (Milliseconds) -> Void,
        onTimeUp: @escaping () -> Void
    ) {
        _ = scope

        if countdownStates.count >= maxActiveCountdowns && countdownStates[packageName] == nil {
            if let oldestPaused = countdownStates.values.filter({ !$0.isRunning }).sorted(by: { $0.sessionStartTime < $1.sessionStartTime }).first {
                stopCountdown(packageName: oldestPaused.packageName, scope: scope)
            }
        }

        stopCountdown(packageName: packageName, scope: scope)

        var state = CountdownState(
            packageName: packageName,
            limitMillis: limitMillis,
            usedMillis: usedMillis,
            sessionStartTime: nowMillis(),
            isRunning: true,
            onUpdate: onUpdate,
            onTimeUp: onTimeUp
        )

        countdownStates[packageName] = state

        tasks[packageName] = Task { [weak self] in
            guard let self else { return }
            while !Task.isCancelled {
                guard let latest = self.countdownStates[packageName], latest.isRunning else {
                    break
                }

                let remaining = self.getRemainingTime(state: latest)
                state.onUpdate?(remaining)

                if remaining <= 0 {
                    state.isRunning = false
                    state.onTimeUp?()

                    let sessionDuration = nowMillis() - latest.sessionStartTime
                    if sessionDuration > 0 {
                        await self.usageTrackingRepository.recordUsage(packageName: packageName, durationMillis: sessionDuration)
                    }

                    self.countdownStates.removeValue(forKey: packageName)
                    self.tasks.removeValue(forKey: packageName)
                    break
                }

                try? await Task.sleep(nanoseconds: 1_000_000_000)
            }
        }
    }

    public func pauseCountdown(packageName: String, scope: Any? = nil) {
        _ = scope
        guard var state = countdownStates[packageName], state.isRunning else { return }

        let sessionDuration = nowMillis() - state.sessionStartTime
        state.usedMillis += sessionDuration
        state.isRunning = false
        countdownStates[packageName] = state

        if sessionDuration > 0 {
            Task {
                await usageTrackingRepository.recordUsage(packageName: packageName, durationMillis: sessionDuration)
            }
        }
    }

    public func resumeCountdown(packageName: String) {
        guard var state = countdownStates[packageName], !state.isRunning else { return }
        state.sessionStartTime = nowMillis()
        state.isRunning = true
        countdownStates[packageName] = state
    }

    public func stopCountdown(packageName: String, scope: Any? = nil) {
        _ = scope
        guard let state = countdownStates[packageName] else { return }

        if state.isRunning {
            let sessionDuration = nowMillis() - state.sessionStartTime
            if sessionDuration > 0 {
                Task {
                    await usageTrackingRepository.recordUsage(packageName: packageName, durationMillis: sessionDuration)
                }
            }
        }

        tasks[packageName]?.cancel()
        tasks.removeValue(forKey: packageName)
        countdownStates.removeValue(forKey: packageName)
    }

    private func getRemainingTime(state: CountdownState) -> Milliseconds {
        let currentSessionUsed = state.isRunning ? nowMillis() - state.sessionStartTime : 0
        let totalUsed = state.usedMillis + currentSessionUsed
        return max(0, state.limitMillis - totalUsed)
    }

    public func getRemainingTime(packageName: String) -> Milliseconds? {
        guard let state = countdownStates[packageName] else { return nil }
        return getRemainingTime(state: state)
    }

    public func getRemainingMillis(packageName: String) -> Milliseconds {
        getRemainingTime(packageName: packageName) ?? 0
    }

    public func isRunning(packageName: String) -> Bool {
        countdownStates[packageName]?.isRunning ?? false
    }

    public func hasCountdown(packageName: String) -> Bool {
        countdownStates[packageName] != nil
    }

    public func cleanup(scope: Any? = nil) {
        _ = scope
        let packages = Array(countdownStates.keys)
        for packageName in packages {
            stopCountdown(packageName: packageName)
        }
    }
}

public final class UsageNotificationManager: @unchecked Sendable {
    private struct CountdownInfo {
        var packageName: String
        var appName: String
        var targetEndTime: Milliseconds
        var remainingCount: Int?
    }

    private var updateJobs: [String: Task<Void, Never>] = [:]
    private var notificationIds: [String: Int] = [:]
    private var nextNotificationId = 3000
    private var countdownInfos: [String: CountdownInfo] = [:]
    private var notificationsEnabled = true

    public init() {
        createNotificationChannel()
    }

    private func createNotificationChannel() {}

    public func startCountdownNotification(
        packageName: String,
        appName: String,
        remainingMillis: Milliseconds,
        remainingCount: Int? = nil,
        scope: Any? = nil
    ) {
        _ = scope
        guard areNotificationsEnabled() else { return }

        updateJobs[packageName]?.cancel()
        let targetEndTime = nowMillis() + remainingMillis

        countdownInfos[packageName] = CountdownInfo(
            packageName: packageName,
            appName: appName,
            targetEndTime: targetEndTime,
            remainingCount: remainingCount
        )

        let notificationId = notificationIds[packageName] ?? {
            nextNotificationId += 1
            notificationIds[packageName] = nextNotificationId
            return nextNotificationId
        }()

        updateJobs[packageName] = Task {
            while !Task.isCancelled {
                let remaining = max(0, targetEndTime - nowMillis())
                updateNotification(
                    packageName: packageName,
                    appName: appName,
                    remainingMillis: remaining,
                    remainingCount: remainingCount,
                    notificationId: notificationId
                )

                if remaining <= 0 { break }
                try? await Task.sleep(nanoseconds: 1_000_000_000)
            }
        }
    }

    public func showOrUpdateNotification(
        packageName: String,
        appName: String,
        remainingMillis: Milliseconds? = nil,
        remainingCount: Int? = nil
    ) {
        guard areNotificationsEnabled() else { return }
        let _ = packageName
        let _ = appName
        _ = buildContentText(remainingMillis: remainingMillis, remainingCount: remainingCount)
    }

    private func updateNotification(
        packageName: String,
        appName: String,
        remainingMillis: Milliseconds,
        remainingCount: Int?,
        notificationId: Int
    ) {
        let _ = packageName
        let _ = appName
        let _ = remainingCount
        let _ = notificationId
        let _ = remainingMillis
    }

    public func pauseNotification(packageName: String) {
        updateJobs[packageName]?.cancel()
        updateJobs.removeValue(forKey: packageName)
    }

    public func cancelNotification(packageName: String) {
        updateJobs[packageName]?.cancel()
        updateJobs.removeValue(forKey: packageName)
        countdownInfos.removeValue(forKey: packageName)
        notificationIds.removeValue(forKey: packageName)
    }

    public func cancelAllNotifications() {
        for job in updateJobs.values { job.cancel() }
        updateJobs.removeAll()
        countdownInfos.removeAll()
        notificationIds.removeAll()
    }

    private func buildContentText(remainingMillis: Milliseconds?, remainingCount: Int?) -> String {
        var lines: [String] = []
        if let remainingMillis {
            let totalSeconds = max(0, Int(remainingMillis / 1000))
            let hours = totalSeconds / 3600
            let minutes = (totalSeconds % 3600) / 60
            let secs = totalSeconds % 60

            let timeText: String
            if hours > 0 {
                timeText = String(format: "%d小时%d分钟%d秒", hours, minutes, secs)
            } else if minutes > 0 {
                timeText = String(format: "%d分钟%d秒", minutes, secs)
            } else {
                timeText = String(format: "%d秒", secs)
            }
            lines.append("⏱️ 剩余: \(timeText)")
        }

        if let remainingCount {
            lines.append("🔢 剩余次数: \(remainingCount)次")
        }

        return lines.joined(separator: "\n")
    }

    private func areNotificationsEnabled() -> Bool {
        notificationsEnabled
    }
}

public final class FocusModeNotificationManager: @unchecked Sendable {
    private var updateTask: Task<Void, Never>?
    private var notificationsEnabled = true

    public init() {
        createNotificationChannel()
    }

    private func createNotificationChannel() {}

    public func startNotification(focusMode: FocusMode, scope: Any? = nil) {
        _ = scope
        updateTask?.cancel()
        updateTask = Task {
            while !Task.isCancelled && focusMode.shouldBeActive() {
                updateNotification(focusMode: focusMode)
                try? await Task.sleep(nanoseconds: 1_000_000_000)
            }
        }
    }

    private func updateNotification(focusMode: FocusMode) {
        let currentTime = nowMillis()
        let _: (Int64, Int64, Int64, String)

        switch focusMode.modeType {
        case .manual:
            let elapsed = currentTime - focusMode.updatedAt
            let h = elapsed / 3_600_000
            let m = (elapsed % 3_600_000) / 60_000
            let s = (elapsed % 60_000) / 1_000
            let text = h > 0 ? String(format: "%02lld:%02lld:%02lld", h, m, s) : String(format: "%02lld:%02lld", m, s)
            _ = (h, m, s, text)
        case .countdown:
            let remaining = max(0, (focusMode.countdownEndTime ?? currentTime) - currentTime)
            let h = remaining / 3_600_000
            let m = (remaining % 3_600_000) / 60_000
            let s = (remaining % 60_000) / 1_000
            let text = h > 0 ? String(format: "%02lld:%02lld:%02lld", h, m, s) : String(format: "%02lld:%02lld", m, s)
            _ = (h, m, s, text)
        case .scheduled:
            _ = (0, 0, 0, "00:00")
        }
    }

    public func stopNotification() {
        updateTask?.cancel()
        updateTask = nil
    }

    private func areNotificationsEnabled() -> Bool {
        notificationsEnabled
    }
}
