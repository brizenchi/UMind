package com.example.umind.domain.usecase

import android.content.pm.PackageManager
import com.example.umind.data.local.dao.UsageSessionDao
import com.example.umind.domain.model.Result
import com.example.umind.domain.model.UsageTimelineEntry
import com.example.umind.domain.model.TimelineEntryType
import com.example.umind.domain.repository.FocusRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetUsageTimelineUseCase @Inject constructor(
    private val usageSessionDao: UsageSessionDao,
    private val focusRepository: FocusRepository,
    private val packageManager: PackageManager
) {
    suspend operator fun invoke(date: LocalDate): List<UsageTimelineEntry> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val managedPackages = getManagedPackages()
        if (managedPackages.isEmpty()) {
            return emptyList()
        }

        val sessions = usageSessionDao.getSessionsInRange(startOfDay, endOfDay)

        return sessions
            .filter { session ->
                session.packageName in managedPackages &&
                // 只显示已结束的 session（有 endTime）
                session.endTime != null && session.durationMillis > 0
            }
            .map { session ->
                val appName = try {
                    val appInfo = packageManager.getApplicationInfo(session.packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    session.packageName
                }

                UsageTimelineEntry(
                    timestamp = session.startTime,
                    packageName = session.packageName,
                    appName = appName,
                    durationMillis = session.durationMillis,
                    type = TimelineEntryType.APP_USAGE
                )
            }
            .sortedByDescending { it.timestamp }
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
