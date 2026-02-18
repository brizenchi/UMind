package com.example.umind.di

import android.content.Context
import android.content.pm.PackageManager
import androidx.room.Room
import com.example.umind.data.local.dao.*
import com.example.umind.data.local.database.FocusDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFocusDatabase(
        @ApplicationContext context: Context
    ): FocusDatabase {
        return Room.databaseBuilder(
            context,
            FocusDatabase::class.java,
            FocusDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFocusStrategyDao(database: FocusDatabase): FocusStrategyDao {
        return database.focusStrategyDao()
    }

    @Provides
    @Singleton
    fun provideFocusModeDao(database: FocusDatabase): FocusModeDao {
        return database.focusModeDao()
    }

    @Provides
    @Singleton
    fun provideUsageRecordDao(database: FocusDatabase): UsageRecordDao {
        return database.usageRecordDao()
    }

    @Provides
    @Singleton
    fun provideTemporaryUsageDao(database: FocusDatabase): TemporaryUsageDao {
        return database.temporaryUsageDao()
    }

    @Provides
    @Singleton
    fun provideUsageSessionDao(database: FocusDatabase): UsageSessionDao {
        return database.usageSessionDao()
    }

    @Provides
    @Singleton
    fun provideBlockEventDao(database: FocusDatabase): BlockEventDao {
        return database.blockEventDao()
    }

    @Provides
    @Singleton
    fun providePackageManager(@ApplicationContext context: Context): PackageManager {
        return context.packageManager
    }
}
