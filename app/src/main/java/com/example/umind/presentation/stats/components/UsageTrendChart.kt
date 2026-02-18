package com.example.umind.presentation.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.umind.domain.model.UsageTrend
import com.example.umind.presentation.stats.formatDurationShort
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Usage trend chart (7 days)
 */
@Composable
fun UsageTrendChart(
    trends: List<UsageTrend>,
    modifier: Modifier = Modifier
) {
    if (trends.isEmpty()) {
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "7天使用趋势",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val maxUsage = trends.maxOfOrNull { it.totalUsageDurationMillis } ?: 1L

            // Bar chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                trends.forEach { trend ->
                    TrendBar(
                        trend = trend,
                        maxUsage = maxUsage,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trends.forEach { trend ->
                    Text(
                        text = trend.date.format(DateTimeFormatter.ofPattern("E", Locale.CHINESE)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendBar(
    trend: UsageTrend,
    maxUsage: Long,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Duration label
        if (trend.totalUsageDurationMillis > 0) {
            Text(
                text = formatDurationShort(trend.totalUsageDurationMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Bar
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val barWidth = size.width
            val maxHeight = size.height
            val barHeight = if (maxUsage > 0) {
                (trend.totalUsageDurationMillis.toFloat() / maxUsage.toFloat() * maxHeight).coerceAtLeast(4.dp.toPx())
            } else {
                4.dp.toPx()
            }

            // Background bar
            drawRect(
                color = backgroundColor,
                topLeft = Offset(0f, 0f),
                size = Size(barWidth, maxHeight)
            )

            // Usage bar
            drawRect(
                color = barColor,
                topLeft = Offset(0f, maxHeight - barHeight),
                size = Size(barWidth, barHeight)
            )
        }
    }
}
