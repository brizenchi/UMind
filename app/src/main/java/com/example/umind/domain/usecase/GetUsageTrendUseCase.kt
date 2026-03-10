package com.example.umind.domain.usecase

import com.example.umind.data.repository.UsageTrackingRepository
import com.example.umind.domain.model.Result
import com.example.umind.domain.model.UsageTrend
import com.example.umind.domain.repository.FocusRepository
import java.time.LocalDate
import javax.inject.Inject

class GetUsageTrendUseCase @Inject constructor(
    private val usageTrackingRepository: UsageTrackingRepository,
    private val focusRepository: FocusRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): List<UsageTrend> {
        val managedPackages = getManagedPackages()
        if (managedPackages.isEmpty()) {
            return buildZeroTrend(startDate, endDate)
        }

        val records = usageTrackingRepository.getUsageRecordsInRange(startDate, endDate)
            .filter { it.packageName in managedPackages }

        // Group by date and aggregate
        val trendMap = records.groupBy { it.date }
            .mapValues { (_, records) ->
                UsageTrend(
                    date = records.first().date,
                    totalUsageDurationMillis = records.sumOf { it.usageDurationMillis },
                    totalOpenCount = records.sumOf { it.openCount }
                )
            }

        // Fill in missing dates with zero values
        return buildZeroTrend(startDate, endDate).map { fallback ->
            trendMap[fallback.date] ?: fallback
        }
    }

    private suspend fun getManagedPackages(): Set<String> {
        return when (val result = focusRepository.getFocusStrategies()) {
            is Result.Success -> result.data
                .flatMap { it.targetApps }
                .toSet()
            else -> emptySet()
        }
    }

    private fun buildZeroTrend(startDate: LocalDate, endDate: LocalDate): List<UsageTrend> {
        val result = mutableListOf<UsageTrend>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            result.add(
                UsageTrend(
                    date = currentDate,
                    totalUsageDurationMillis = 0,
                    totalOpenCount = 0
                )
            )
            currentDate = currentDate.plusDays(1)
        }
        return result
    }
}
