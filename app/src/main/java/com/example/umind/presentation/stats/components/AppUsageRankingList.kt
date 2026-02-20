package com.example.umind.presentation.stats.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.umind.domain.model.AppUsageStats
import com.example.umind.presentation.stats.formatDuration
import com.example.umind.presentation.stats.getUsagePercentage
import com.example.umind.ui.components.FocusCard

/**
 * App usage ranking list
 */
@Composable
fun AppUsageRankingList(
    appUsageStats: List<AppUsageStats>,
    modifier: Modifier = Modifier
) {
    if (appUsageStats.isEmpty()) {
        FocusCard(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无使用数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val maxUsage = appUsageStats.maxOfOrNull { it.usageDurationMillis } ?: 1L

    FocusCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "应用使用排行",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            appUsageStats.take(10).forEach { stats ->
                AppUsageItem(
                    appUsageStats = stats,
                    maxUsage = maxUsage
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun AppUsageItem(
    appUsageStats: AppUsageStats,
    maxUsage: Long
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = appUsageStats.appName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatDuration(appUsageStats.usageDurationMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { getUsagePercentage(appUsageStats.usageDurationMillis, maxUsage) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "打开 ${appUsageStats.openCount} 次",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
