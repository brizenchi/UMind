package com.example.umind.presentation.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.umind.domain.model.UsageTimelineEntry
import com.example.umind.ui.components.FocusCard
import com.example.umind.ui.theme.ComponentSpacing
import com.example.umind.ui.theme.CornerRadius
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun UsageTimelineCard(
    timelineEntries: List<UsageTimelineEntry>,
    modifier: Modifier = Modifier
) {
    FocusCard(
        modifier = modifier,
        shape = RoundedCornerShape(CornerRadius.large),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    ) {
        Column(
            modifier = Modifier.padding(ComponentSpacing.cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 今日时间轴",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${timelineEntries.size} 条记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(ComponentSpacing.smallSpacing))

            if (timelineEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⏰",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Text(
                            text = "暂无使用记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(timelineEntries) { entry ->
                        TimelineItem(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(
    entry: UsageTimelineEntry,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val startTime = Instant.ofEpochMilli(entry.timestamp)
        .atZone(ZoneId.systemDefault())
    val startTimeStr = startTime.format(timeFormatter)

    // 计算结束时间
    val endTime = startTime.plusSeconds((entry.durationMillis / 1000).toLong())
    val endTimeStr = endTime.format(timeFormatter)

    val durationMinutes = entry.durationMillis / 60000
    val durationText = when {
        durationMinutes < 1 -> "< 1分钟"
        durationMinutes < 60 -> "${durationMinutes}分钟"
        else -> {
            val hours = durationMinutes / 60
            val mins = durationMinutes % 60
            if (mins > 0) "${hours}h ${mins}m" else "${hours}小时"
        }
    }

    // 根据使用时长选择颜色
    val accentColor = when {
        durationMinutes < 5 -> MaterialTheme.colorScheme.tertiary
        durationMinutes < 30 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 左侧时间轴线
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            // 时间段显示
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = startTimeStr,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(accentColor.copy(alpha = 0.3f))
                )
                Text(
                    text = endTimeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 连接点
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(accentColor)
        )

        // 右侧内容卡片
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = accentColor.copy(alpha = 0.08f),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = entry.appName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = durationText,
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 时长指示器
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when {
                                durationMinutes < 5 -> "短"
                                durationMinutes < 30 -> "中"
                                else -> "长"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
