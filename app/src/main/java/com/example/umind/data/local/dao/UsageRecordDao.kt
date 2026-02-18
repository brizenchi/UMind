package com.example.umind.data.local.dao

import androidx.room.*
import com.example.umind.data.local.entity.UsageRecordEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface UsageRecordDao {
    @Query("SELECT * FROM usage_records WHERE packageName = :packageName AND date = :date")
    suspend fun getUsageRecord(packageName: String, date: LocalDate): UsageRecordEntity?

    @Query("SELECT * FROM usage_records WHERE packageName = :packageName AND date = :date")
    fun getUsageRecordFlow(packageName: String, date: LocalDate): Flow<UsageRecordEntity?>

    @Query("SELECT * FROM usage_records WHERE date = :date")
    suspend fun getUsageRecordsForDate(date: LocalDate): List<UsageRecordEntity>

    @Query("SELECT * FROM usage_records WHERE date >= :startDate AND date <= :endDate")
    suspend fun getUsageRecordsInRange(startDate: LocalDate, endDate: LocalDate): List<UsageRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageRecord(record: UsageRecordEntity)

    @Update
    suspend fun updateUsageRecord(record: UsageRecordEntity)

    @Query("DELETE FROM usage_records WHERE date < :beforeDate")
    suspend fun deleteOldRecords(beforeDate: LocalDate)

    @Query("DELETE FROM usage_records WHERE packageName = :packageName")
    suspend fun deleteRecordsForPackage(packageName: String)

    @Query("SELECT * FROM usage_records WHERE date = :date ORDER BY usageDurationMillis DESC")
    fun getUsageRecordsForDateFlow(date: LocalDate): Flow<List<UsageRecordEntity>>

    @Query("SELECT SUM(usageDurationMillis) FROM usage_records WHERE date = :date")
    suspend fun getTotalUsageDurationForDate(date: LocalDate): Long?

    @Query("SELECT SUM(openCount) FROM usage_records WHERE date = :date")
    suspend fun getTotalOpenCountForDate(date: LocalDate): Int?

    @Query("SELECT * FROM usage_records WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getUsageRecordsInRangeFlow(startDate: LocalDate, endDate: LocalDate): Flow<List<UsageRecordEntity>>
}
