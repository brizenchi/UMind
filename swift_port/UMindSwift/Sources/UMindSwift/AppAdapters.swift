import Foundation

public final class MainActivity {
    private(set) var notificationPermissionGranted = false

    public init() {}

    public func onCreate(savedInstanceState: [String: Any]? = nil) {
        _ = savedInstanceState
        requestNotificationPermission()
        checkBatteryOptimization()
    }

    public func requestNotificationPermission() {
        notificationPermissionGranted = true
    }

    public func checkBatteryOptimization() {
        _ = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context: "umind")
    }

    public func onResume() {
        checkAccessibilityService()
    }

    public func checkAccessibilityService() {
        _ = AccessibilityUtil.isAccessibilityServiceEnabled(
            context: "com.example.umind",
            serviceName: "BlockAccessibilityService"
        )
    }
}

public func MainScreen() -> String {
    "MainScreen"
}

public final class FocusApplication {
    private let repository: FocusRepositoryImpl
    private let focusModeCountdownManager: FocusModeCountdownManager

    public init(
        repository: FocusRepositoryImpl,
        focusModeCountdownManager: FocusModeCountdownManager
    ) {
        self.repository = repository
        self.focusModeCountdownManager = focusModeCountdownManager
    }

    public func onCreate() {
        Task { await repository.preloadInstalledApps() }
        _ = focusModeCountdownManager
    }
}

public final class BootReceiver {
    public init() {}

    public func onReceive(context: String, intent: String) {
        if intent == "ACTION_BOOT_COMPLETED" {
            let isEnabled = AccessibilityUtil.isAccessibilityServiceEnabled(
                context: context,
                serviceName: "BlockAccessibilityService"
            )

            if !isEnabled {
                sendNotification(context: context)
            }
        }
    }

    public func sendNotification(context: String) {
        _ = context
    }
}

public final class BlockAccessibilityService {
    private let repository: FocusRepository
    private let focusModeRepository: FocusModeRepository
    private let usageTrackingRepository: UsageTrackingRepository
    private let blockingEngine: BlockingEngine
    private let blockEventRepository: BlockEventRepository
    private let countdownManager: UsageCountdownManager
    private let notificationManager: UsageNotificationManager
    private let focusModeNotificationManager: FocusModeNotificationManager

    private var currentForegroundPackage: String?
    private var currentAppStartTime: Milliseconds = 0
    private var isDialogVisible = false

    private let transientSystemUI: Set<String> = ["com.android.systemui"]
    private let systemWhitelist: Set<String> = [
        "android",
        "com.android.systemui",
        "com.android.launcher3",
        "com.android.settings",
        "com.example.umind"
    ]

    public init(
        repository: FocusRepository,
        focusModeRepository: FocusModeRepository,
        usageTrackingRepository: UsageTrackingRepository,
        blockingEngine: BlockingEngine,
        blockEventRepository: BlockEventRepository,
        countdownManager: UsageCountdownManager,
        notificationManager: UsageNotificationManager,
        focusModeNotificationManager: FocusModeNotificationManager
    ) {
        self.repository = repository
        self.focusModeRepository = focusModeRepository
        self.usageTrackingRepository = usageTrackingRepository
        self.blockingEngine = blockingEngine
        self.blockEventRepository = blockEventRepository
        self.countdownManager = countdownManager
        self.notificationManager = notificationManager
        self.focusModeNotificationManager = focusModeNotificationManager
    }

    public static func openAccessibilitySettingsIntent() -> String {
        "app-settings:accessibility"
    }

    public func onServiceConnected() {
        createForegroundNotificationChannel()
        startForegroundService()
        showTestNotification()
        startFocusModeNotificationMonitor()
    }

    private func showTestNotification() {
        if areNotificationsEnabled() {
            // test no-op
        }
    }

    private func startFocusModeNotificationMonitor() {
        Task {
            for await focusMode in focusModeRepository.getFocusMode() {
                if focusMode.shouldBeActive() {
                    focusModeNotificationManager.startNotification(focusMode: focusMode)
                } else {
                    focusModeNotificationManager.stopNotification()
                }
            }
        }
    }

    private func createForegroundNotificationChannel() {}

    private func startForegroundService() {}

    private func areNotificationsEnabled() -> Bool {
        true
    }

    public func onAccessibilityEvent(eventPackageName: String?) {
        guard let packageName = eventPackageName,
              !transientSystemUI.contains(packageName) else {
            return
        }

        if packageName != currentForegroundPackage {
            if let previousPackage = currentForegroundPackage,
               countdownManager.isRunning(packageName: previousPackage) {
                countdownManager.pauseCountdown(packageName: previousPackage)
                notificationManager.pauseNotification(packageName: previousPackage)
            }
        }

        if isSystemWhitelistedApp(packageName: packageName) {
            currentForegroundPackage = packageName
            currentAppStartTime = 0
            return
        }

        Task {
            let blockInfo = await blockingEngine.getBlockInfo(packageName: packageName, openedFromUMind: false)
            if blockInfo.shouldBlock {
                let appName = packageName
                await blockEventRepository.recordBlockEvent(
                    packageName: packageName,
                    appName: appName,
                    blockInfo: blockInfo
                )
                showBlockDialog(packageName: packageName, blockInfo: blockInfo)
                currentForegroundPackage = packageName
            } else {
                await trackAppSwitch(newPackageName: packageName, blockInfo: blockInfo)
            }
        }
    }

    private func trackAppSwitch(newPackageName: String, blockInfo: BlockInfo) async {
        let now = nowMillis()
        let isSameApp = currentForegroundPackage == newPackageName
        guard !isSameApp else { return }

        if let previousPackage = currentForegroundPackage,
           countdownManager.isRunning(packageName: previousPackage) {
            countdownManager.pauseCountdown(packageName: previousPackage)
        }

        await usageTrackingRepository.recordAppOpen(packageName: newPackageName)

        currentForegroundPackage = newPackageName
        currentAppStartTime = now

        guard let usageInfo = blockInfo.usageInfo else { return }

        let appName = newPackageName
        let displayRemainingCount = usageInfo.remainingCount.map { max(0, $0 - 1) }

        if let limitMinutes = usageInfo.usageLimitMinutes {
            let limitMillis = limitMinutes * 60_000
            let usedMillis = usageInfo.usedMinutes * 60_000

            if countdownManager.hasCountdown(packageName: newPackageName) {
                let remainingMillis = countdownManager.getRemainingMillis(packageName: newPackageName)
                countdownManager.resumeCountdown(packageName: newPackageName)
                if remainingMillis > 0 {
                    notificationManager.startCountdownNotification(
                        packageName: newPackageName,
                        appName: appName,
                        remainingMillis: remainingMillis,
                        remainingCount: displayRemainingCount
                    )
                }
            } else {
                let remainingMillis = max(0, limitMillis - usedMillis)
                notificationManager.startCountdownNotification(
                    packageName: newPackageName,
                    appName: appName,
                    remainingMillis: remainingMillis,
                    remainingCount: displayRemainingCount
                )
                countdownManager.startCountdown(
                    packageName: newPackageName,
                    limitMillis: limitMillis,
                    usedMillis: usedMillis,
                    onUpdate: { _ in },
                    onTimeUp: { [weak self] in
                        guard let self else { return }
                        self.showTimeUpDialog(packageName: newPackageName, appName: appName)
                        self.notificationManager.cancelNotification(packageName: newPackageName)
                    }
                )
            }
        } else if usageInfo.remainingCount != nil {
            notificationManager.showOrUpdateNotification(
                packageName: newPackageName,
                appName: appName,
                remainingMillis: nil,
                remainingCount: displayRemainingCount
            )
        }
    }

    private func showFocusModeBlockDialog(packageName: String) {
        _ = packageName
        isDialogVisible = true
    }

    private func showBlockDialog(packageName: String, blockInfo: BlockInfo) {
        _ = packageName

        if blockInfo.reasons.contains(.focusModeActive) {
            showFocusModeBlockDialog(packageName: packageName)
        } else {
            isDialogVisible = true
        }
    }

    private func showTimeUpDialog(packageName: String, appName: String) {
        _ = packageName
        _ = appName
        isDialogVisible = true
    }

    private func dismissBlockDialog() {
        isDialogVisible = false
    }

    public func onInterrupt() {
        dismissBlockDialog()
    }

    private func isSystemWhitelistedApp(packageName: String) -> Bool {
        if packageName == "com.example.umind" { return true }
        return systemWhitelist.contains(packageName)
    }

    public func onDestroy() {
        countdownManager.cleanup()
        notificationManager.cancelAllNotifications()
        focusModeNotificationManager.stopNotification()
        dismissBlockDialog()
    }
}
