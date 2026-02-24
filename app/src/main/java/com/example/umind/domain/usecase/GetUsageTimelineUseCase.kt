package com.example.umind.domain.usecase

import android.content.pm.PackageManager
import com.example.umind.data.local.dao.UsageSessionDao
import com.example.umind.domain.model.UsageTimelineEntry
import com.example.umind.domain.model.TimelineEntryType
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetUsageTimelineUseCase @Inject constructor(
    private val usageSessionDao: UsageSessionDao,
    private val packageManager: PackageManager
) {
    suspend operator fun invoke(date: LocalDate): List<UsageTimelineEntry> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val sessions = usageSessionDao.getSessionsInRange(startOfDay, endOfDay)

        return sessions
            .filter { session ->
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
}
