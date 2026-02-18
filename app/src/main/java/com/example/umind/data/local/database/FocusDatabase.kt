package com.example.umind.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.umind.data.local.dao.*
import com.example.umind.data.local.entity.*
import com.example.umind.data.local.converter.Converters

/**
 * Room database for the Focus app
 */
@Database(
    entities = [
        FocusStrategyEntity::class,
        FocusModeEntity::class,
        UsageRecordEntity::class,
        TemporaryUsageEntity::class,
        UsageSessionEntity::class,
        BlockEventEntity::class
    ],
    version = 7, // Incremented for new entities
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FocusDatabase : RoomDatabase() {
    abstract fun focusStrategyDao(): FocusStrategyDao
    abstract fun focusModeDao(): FocusModeDao
    abstract fun usageRecordDao(): UsageRecordDao
    abstract fun temporaryUsageDao(): TemporaryUsageDao
    abstract fun usageSessionDao(): UsageSessionDao
    abstract fun blockEventDao(): BlockEventDao

    companion object {
        const val DATABASE_NAME = "focus_database"
    }
}
