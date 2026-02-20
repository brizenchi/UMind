package com.example.umind.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.umind.domain.model.AppInfo
import com.example.umind.ui.theme.ComponentSpacing
import com.example.umind.ui.theme.CornerRadius

/**
 * 倒计时选择对话框 - 现代化版本
 */
@Composable
fun CountdownSelectorDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(30) }
    var useCustomTime by remember { mutableStateOf(false) }
    var customHours by remember { mutableStateOf(0) }
    var customMinutes by remember { mutableStateOf(30) }
    val presetDurations = listOf(15, 30, 45, 60, 90, 120)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComponentSpacing.pagePadding),
            shape = RoundedCornerShape(CornerRadius.extraLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(ComponentSpacing.pagePadding)
            ) {
                // 标题
                Text(
                    text = "选择专注时长",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // 快速选择
                Column(verticalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)) {
                    Text(
                        text = "快速选择",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    presetDurations.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)
                        ) {
                            row.forEach { minutes ->
                                FilterChip(
                                    selected = !useCustomTime && selectedMinutes == minutes,
                                    onClick = {
                                        useCustomTime = false
                                        selectedMinutes = minutes
                                    },
                                    label = { Text("${minutes}分钟") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // 填充空白
                            repeat(3 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 自定义时长
                Column(verticalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)) {
                    Text(
                        text = "自定义时长",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customHours.toString(),
                            onValueChange = {
                                customHours = it.toIntOrNull()?.coerceIn(0, 23) ?: 0
                                useCustomTime = true
                            },
                            label = { Text("小时") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(CornerRadius.medium)
                        )
                        Text(":", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(
                            value = customMinutes.toString(),
                            onValueChange = {
                                customMinutes = it.toIntOrNull()?.coerceIn(0, 59) ?: 0
                                useCustomTime = true
                            },
                            label = { Text("分钟") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(CornerRadius.medium)
                        )
                    }
                }

                // 按钮组
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(CornerRadius.medium)
                    ) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            val totalMinutes = if (useCustomTime) {
                                customHours * 60 + customMinutes
                            } else {
                                selectedMinutes
                            }
                            if (totalMinutes > 0) {
                                onConfirm(totalMinutes)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(CornerRadius.medium)
                    ) {
                        Text("开始专注")
                    }
                }
            }
        }
    }
}

/**
 * 应用选择对话框 - 现代化版本
 */
@Composable
fun AppSelectorDialog(
    apps: List<AppInfo>,
    onDismiss: () -> Unit,
    onAppSelected: (AppInfo) -> Unit,
    onLoadIcon: ((String) -> Bitmap?)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(ComponentSpacing.pagePadding),
            shape = RoundedCornerShape(CornerRadius.extraLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 标题
                Text(
                    text = "选择应用",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(ComponentSpacing.pagePadding))

                // 应用列表
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        AppSelectorItem(
                            app = app,
                            onClick = { onAppSelected(app) },
                            onLoadIcon = onLoadIcon
                        )
                    }
                }

                Spacer(modifier = Modifier.height(ComponentSpacing.pagePadding))

                // 取消按钮
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CornerRadius.medium)
                ) {
                    Text("取消")
                }
            }
        }
    }
}

@Composable
private fun AppSelectorItem(
    app: AppInfo,
    onClick: () -> Unit,
    onLoadIcon: ((String) -> Bitmap?)? = null
) {
    // Track icon loading state
    var icon by remember(app.packageName) { mutableStateOf(app.icon) }

    // Load icon on-demand when item is displayed
    LaunchedEffect(app.packageName) {
        if (icon == null && onLoadIcon != null) {
            // Trigger async load (returns cached or null)
            val loadedIcon = onLoadIcon(app.packageName)
            if (loadedIcon != null) {
                icon = loadedIcon
            }
        }
    }

    // Update icon when app changes
    LaunchedEffect(app.icon) {
        if (app.icon != null) {
            icon = app.icon
        }
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.medium),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(ComponentSpacing.pagePadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Show icon if available, otherwise show placeholder
            if (icon != null) {
                Image(
                    bitmap = icon!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                // Placeholder while loading
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.label.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
