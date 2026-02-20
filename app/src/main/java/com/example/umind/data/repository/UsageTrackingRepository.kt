package com.example.umind.data.repository

import com.example.umind.data.local.dao.UsageRecordDao
import com.example.umind.data.local.entity.UsageRecordEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageTrackingRepository @Inject constructor(
    private val usageRecordDao: UsageRecordDao
) {
    /**
     * Record app usage duration
     */
    suspend fun recordUsage(packageName: String, durationMillis: Long) {
        val today = LocalDate.now()
        val existingRecord = usageRecordDao.getUsageRecord(packageName, today)

        if (existingRecord != null) {
            val updatedRecord = existingRecord.copy(
                usageDurationMillis = existingRecord.usageDurationMillis + durationMillis,
                lastUpdated = System.currentTimeMillis()
            )
            usageRecordDao.updateUsageRecord(updatedRecord)
        } else {
            val newRecord = UsageRecordEntity(
                packageName = packageName,
                date = today,
                usageDurationMillis = durationMillis,
                openCount = 0
            )
            usageRecordDao.insertUsageRecord(newRecord)
        }
    }

    /**
     * Record app open event
     */
    suspend fun recordAppOpen(packageName: String) {
        val today = LocalDate.now()
        val existingRecord = usageRecordDao.getUsageRecord(packageName, today)

        if (existingRecord != null) {
            val newCount = existingRecord.openCount + 1
            android.util.Log.d("UsageTrackingRepository", "=== Recording app open for $packageName ===")
            android.util.Log.d("UsageTrackingRepository", "Previous count: ${existingRecord.openCount}, New count: $newCount")
            val updatedRecord = existingRecord.copy(
                openCount = newCount,
                lastUpdated = System.currentTimeMillis()
            )
            usageRecordDao.updateUsageRecord(updatedRecord)
        } else {
            android.util.Log.d("UsageTrackingRepository", "=== Recording FIRST app open for $packageName ===")
            android.util.Log.d("UsageTrackingRepository", "Creating new record with openCount = 1")
            val newRecord = UsageRecordEntity(
                packageName = packageName,
                date = today,
                usageDurationMillis = 0,
                openCount = 1
            )
            usageRecordDao.insertUsageRecord(newRecord)
        }
    }

    /**
     * Get usage duration for a package on a specific date
     */
    suspend fun getUsageDuration(packageName: String, date: LocalDate): Long {
        return usageRecordDao.getUsageRecord(packageName, date)?.usageDurationMillis ?: 0
    }

    /**
     * Get open count for a package on a specific date
     */
    suspend fun getOpenCount(packageName: String, date: LocalDate): Int {
        val count = usageRecordDao.getUsageRecord(packageName, date)?.openCount ?: 0
        android.util.Log.d("UsageTrackingRepository", "getOpenCount for $packageName on $date: $count")
        return count
    }

    /**
     * Get usage duration for a package in a date range (for weekly/monthly limits)
     */
    suspend fun getUsageDurationInRange(packageName: String, startDate: LocalDate, endDate: LocalDate): Long {
        val records = usageRecordDao.getUsageRecordsInRange(startDate, endDate)
        return records.filter { it.packageName == packageName }.sumOf { it.usageDurationMillis }
    }

    /**
     * Get open count for a package in a date range (for weekly/monthly limits)
     */
    suspend fun getOpenCountInRange(packageName: String, startDate: LocalDate, endDate: LocalDate): Int {
        val records = usageRecordDao.getUsageRecordsInRange(startDate, endDate)
        return records.filter { it.packageName == packageName }.sumOf { it.openCount }
    }

    /**
     * Get total usage duration for all apps in a date range
     */
    suspend fun getTotalUsageDurationInRange(startDate: LocalDate, endDate: LocalDate): Long {
        val records = usageRecordDao.getUsageRecordsInRange(startDate, endDate)
        return records.sumOf { it.usageDurationMillis }
    }

    /**
     * Get total open count for all apps in a date range
     */
    suspend fun getTotalOpenCountInRange(startDate: LocalDate, endDate: LocalDate): Int {
        val records = usageRecordDao.getUsageRecordsInRange(startDate, endDate)
        return records.sumOf { it.openCount }
    }

    /**
     * Clean up old records (older than 90 days)
     */
    suspend fun cleanupOldRecords() {
        val cutoffDate = LocalDate.now().minus(90, ChronoUnit.DAYS)
        usageRecordDao.deleteOldRecords(cutoffDate)
    }

    /**
     * Get usage record flow for real-time updates
     */
    fun getUsageRecordFlow(packageName: String, date: LocalDate): Flow<UsageRecordEntity?> {
        return usageRecordDao.getUsageRecordFlow(packageName, date)
    }

    /**
     * Clear today's usage records (for debugging)
     */
    suspend fun clearTodayRecords() {
        val today = LocalDate.now()
        android.util.Log.d("UsageTrackingRepository", "Clearing records for today: $today")
        usageRecordDao.deleteRecordsForDate(today)
    }

    /**
     * Clear all usage records
     */
    suspend fun clearAllRecords() {
        android.util.Log.d("UsageTrackingRepository", "Clearing all usage records")
        usageRecordDao.deleteAllRecords()
    }

    /**
     * Get all usage records for today (for debugging)
     */
    suspend fun getTodayRecords(): List<UsageRecordEntity> {
        val today = LocalDate.now()
        return usageRecordDao.getUsageRecordsForDate(today)
    }

    /**
     * Get usage records for a specific date as Flow
     */
    fun getUsageRecordsForDateFlow(date: LocalDate): Flow<List<UsageRecordEntity>> {
        return usageRecordDao.getUsageRecordsForDateFlow(date)
    }

    /**
     * Get total usage duration for a specific date
     */
    suspend fun getTotalUsageDurationForDate(date: LocalDate): Long {
        return usageRecordDao.getTotalUsageDurationForDate(date) ?: 0
    }

    /**
     * Get total open count for a specific date
     */
    suspend fun getTotalOpenCountForDate(date: LocalDate): Int {
        return usageRecordDao.getTotalOpenCountForDate(date) ?: 0
    }

    /**
     * Get usage records in range as Flow
     */
    fun getUsageRecordsInRangeFlow(startDate: LocalDate, endDate: LocalDate): Flow<List<UsageRecordEntity>> {
        return usageRecordDao.getUsageRecordsInRangeFlow(startDate, endDate)
    }

    /**
     * Get usage records for a specific date
     */
    suspend fun getUsageRecordsForDate(date: LocalDate): List<UsageRecordEntity> {
        return usageRecordDao.getUsageRecordsForDate(date)
    }

    /**
     * Get usage records in a date range
     */
    suspend fun getUsageRecordsInRange(startDate: LocalDate, endDate: LocalDate): List<UsageRecordEntity> {
        return usageRecordDao.getUsageRecordsInRange(startDate, endDate)
    }
}
