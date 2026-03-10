package com.example.umind.domain.usecase

import android.content.pm.PackageManager
import com.example.umind.data.repository.UsageTrackingRepository
import com.example.umind.domain.model.AppUsageStats
import com.example.umind.domain.model.DailyStats
import com.example.umind.domain.model.Result
import com.example.umind.domain.repository.FocusRepository
import java.time.LocalDate
import javax.inject.Inject

class GetDailyStatsUseCase @Inject constructor(
    private val usageTrackingRepository: UsageTrackingRepository,
    private val focusRepository: FocusRepository,
    private val packageManager: PackageManager
) {
    suspend operator fun invoke(date: LocalDate): DailyStats {
        val managedPackages = getManagedPackages()
        if (managedPackages.isEmpty()) {
            return DailyStats(
                date = date,
                totalUsageDurationMillis = 0,
                totalOpenCount = 0,
                totalBlockCount = 0,
                appUsageStats = emptyList()
            )
        }

        // Get records through repository method
        val records = usageTrackingRepository.getUsageRecordsForDate(date)
            .filter { it.packageName in managedPackages }

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

    private suspend fun getManagedPackages(): Set<String> {
        return when (val result = focusRepository.getFocusStrategies()) {
            is Result.Success -> result.data
                .flatMap { it.targetApps }
                .toSet()
            else -> emptySet()
        }
    }
}
