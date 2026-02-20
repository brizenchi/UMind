import Foundation

public enum DatabaseModule {
    public static func provideFocusDatabase() -> FocusDatabase {
        FocusDatabase()
    }

    public static func provideFocusStrategyDao(database: FocusDatabase) -> FocusStrategyDao {
        database.focusStrategyDao()
    }

    public static func provideFocusModeDao(database: FocusDatabase) -> FocusModeDao {
        database.focusModeDao()
    }

    public static func provideUsageRecordDao(database: FocusDatabase) -> UsageRecordDao {
        database.usageRecordDao()
    }

    public static func provideTemporaryUsageDao(database: FocusDatabase) -> TemporaryUsageDao {
        database.temporaryUsageDao()
    }

    public static func provideUsageSessionDao(database: FocusDatabase) -> UsageSessionDao {
        database.usageSessionDao()
    }

    public static func provideBlockEventDao(database: FocusDatabase) -> BlockEventDao {
        database.blockEventDao()
    }

    public static func providePackageManager() -> String {
        "ios.package.manager"
    }
}

public enum RepositoryModule {
    public static func bindFocusRepository(focusRepositoryImpl: FocusRepositoryImpl) -> FocusRepository {
        focusRepositoryImpl
    }
}
