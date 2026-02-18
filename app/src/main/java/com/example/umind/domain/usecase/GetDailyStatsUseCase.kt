package com.example.umind.domain.usecase

import android.content.pm.PackageManager
import com.example.umind.data.repository.UsageTrackingRepository
import com.example.umind.domain.model.AppUsageStats
import com.example.umind.domain.model.DailyStats
import java.time.LocalDate
import javax.inject.Inject

class GetDailyStatsUseCase @Inject constructor(
    private val usageTrackingRepository: UsageTrackingRepository,
    private val packageManager: PackageManager
) {
    suspend operator fun invoke(date: LocalDate): DailyStats {
        // Get records through repository method
        val records = usageTrackingRepository.getUsageRecordsForDate(date)

        val appUsageStats = records.map { record ->
            val appName = try {
                val appInfo = packageManager.getApplicationInfo(record.packageName, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                record.packageName
            }

            AppUsageStats(
                packageName = record.packageName,
                appName = appName,
                usageDurationMillis = record.usageDurationMillis,
                openCount = record.openCount,
                blockCount = 0 // TODO: Add block count tracking
            )
        }.sortedByDescending { it.usageDurationMillis }

        val totalUsageDuration = records.sumOf { it.usageDurationMillis }
        val totalOpenCount = records.sumOf { it.openCount }

        return DailyStats(
            date = date,
            totalUsageDurationMillis = totalUsageDuration,
            totalOpenCount = totalOpenCount,
            totalBlockCount = 0, // TODO: Add block count tracking
            appUsageStats = appUsageStats
        )
    }
}
