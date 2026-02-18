package com.example.umind.domain.usecase

import com.example.umind.data.repository.UsageTrackingRepository
import com.example.umind.domain.model.UsageTrend
import java.time.LocalDate
import javax.inject.Inject

class GetUsageTrendUseCase @Inject constructor(
    private val usageTrackingRepository: UsageTrackingRepository
) {
    suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): List<UsageTrend> {
        val records = usageTrackingRepository.getUsageRecordsInRange(startDate, endDate)

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
        val result = mutableListOf<UsageTrend>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            result.add(
                trendMap[currentDate] ?: UsageTrend(
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
