package com.example.umind.presentation.focusmode

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.umind.domain.model.AppInfo
import com.example.umind.domain.model.FocusModeType
import com.example.umind.domain.model.TimeRestriction
import com.example.umind.ui.components.TimePickerDialog
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Focus Mode Screen - Whitelist-based blocking with time controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(
    viewModel: FocusModeViewModel = hiltViewModel()
) {
    val focusMode by viewModel.focusMode.collectAsState()
    val whitelistedApps by viewModel.whitelistedApps.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()

    var showAppSelector by remember { mutableStateOf(false) }
    var showCountdownSelector by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Focus Mode Card
        item {
            FocusModeMainCard(
                focusMode = focusMode,
                onStartManual = { viewModel.toggleFocusMode(true) },
                onStartCountdown = { showCountdownSelector = true },
                onStop = { viewModel.stopFocusMode() }
            )
        }

        // Whitelist Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "允许的应用 (${whitelistedApps.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                FilledTonalButton(
                    onClick = { showAppSelector = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加")
                }
            }
        }

        if (whitelistedApps.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无允许的应用\n点击上方按钮添加",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(whitelistedApps, key = { it.packageName }) { app ->
                WhitelistAppItem(
                    app = app,
                    onRemove = { viewModel.removeFromWhitelist(app.packageName) }
                )
            }
        }
    }

    // Dialogs
    if (showAppSelector) {
        AppSelectorDialog(
            apps = installedApps.filter { !focusMode.whitelistedApps.contains(it.packageName) },
            onDismiss = { showAppSelector = false },
            onAppSelected = { app ->
                viewModel.addToWhitelist(app.packageName)
                showAppSelector = false
            }
        )
    }

    // Dialogs
    if (showCountdownSelector) {
        CountdownSelectorDialog(
            onDismiss = { showCountdownSelector = false },
            onConfirm = { minutes ->
                viewModel.startCountdown(minutes)
                showCountdownSelector = false
            }
        )
    }
}

/**
 * Main Focus Mode Card
 * Shows timer when active, mode selection when inactive
 */
@Composable
fun FocusModeMainCard(
    focusMode: com.example.umind.domain.model.FocusMode,
    onStartManual: () -> Unit,
    onStartCountdown: () -> Unit,
    onStop: () -> Unit
) {
    // Timer state that updates every second
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        // Always update time for scheduled ranges display
        while (true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (focusMode.shouldBeActive()) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "专注模式",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (focusMode.shouldBeActive()) {
                // Show large timer when active
                when (focusMode.modeType) {
                    FocusModeType.MANUAL -> {
                        // Elapsed time (counting up)
                        val elapsedMillis = currentTime - focusMode.updatedAt
                        val hours = (elapsedMillis / 3600000).toInt()
                        val minutes = ((elapsedMillis % 3600000) / 60000).toInt()
                        val seconds = ((elapsedMillis % 60000) / 1000).toInt()

                        Text(
                            text = "已专注",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 64.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "时分秒",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FocusModeType.COUNTDOWN -> {
                        // Remaining time (counting down)
                        focusMode.countdownEndTime?.let { endTime ->
                            val remainingMillis = (endTime - currentTime).coerceAtLeast(0)
                            val hours = (remainingMillis / 3600000).toInt()
                            val minutes = ((remainingMillis % 3600000) / 60000).toInt()
                            val seconds = ((remainingMillis % 60000) / 1000).toInt()

                            Text(
                                text = "剩余",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 64.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "时分秒",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FocusModeType.SCHEDULED -> {
                        // Scheduled mode removed - do nothing
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stop button
                FilledTonalButton(
                    onClick = onStop,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("停止专注", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                // Show mode selection buttons when not active
                Text(
                    text = "选择专注方式",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = onStartManual,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("开始专注", style = MaterialTheme.typography.titleMedium)
                    }

                    FilledTonalButton(
                        onClick = onStartCountdown,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("计时专注", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择专注时长") },
        text = {
            Column {
                // Preset durations
                Text(
                    text = "快速选择",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                presetDurations.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Custom time input
                Text(
                    text = "自定义时长",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                        singleLine = true
                    )
                    Text(":")
                    OutlinedTextField(
                        value = customMinutes.toString(),
                        onValueChange = {
                            customMinutes = it.toIntOrNull()?.coerceIn(0, 59) ?: 0
                            useCustomTime = true
                        },
                        label = { Text("分钟") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val totalMinutes = if (useCustomTime) {
                    customHours * 60 + customMinutes
                } else {
                    selectedMinutes
                }
                if (totalMinutes > 0) {
                    onConfirm(totalMinutes)
                }
            }) {
                Text("开始专注")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * Dialog for managing scheduled focus time ranges
 * Allows adding and deleting time ranges
 */
@Composable
fun ScheduledTimeRangeEditorDialog(
    initialRanges: List<TimeRestriction>,
    onDismiss: () -> Unit,
    onConfirm: (List<TimeRestriction>) -> Unit
) {
    var timeRanges by remember { mutableStateOf(initialRanges.toMutableList()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("管理专注时段") },
        text = {
            Column {
                if (timeRanges.isEmpty()) {
                    Text("暂无时间段，点击下方按钮添加")
                } else {
                    timeRanges.forEachIndexed { index, range ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${range.startTime} - ${range.endTime}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(onClick = {
                                    timeRanges = timeRanges.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                FilledTonalButton(
                    onClick = {
                        editingIndex = null
                        showTimePicker = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加时间段")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(timeRanges) }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    if (showTimePicker) {
        SimpleTimeRangePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { startHour, startMinute, endHour, endMinute ->
                val newRange = TimeRestriction(
                    id = java.util.UUID.randomUUID().toString(),
                    startTime = LocalTime.of(startHour, startMinute),
                    endTime = LocalTime.of(endHour, endMinute),
                    daysOfWeek = DayOfWeek.values().toSet()
                )
                timeRanges = timeRanges.toMutableList().apply { add(newRange) }
                showTimePicker = false
            }
        )
    }
}

@Composable
fun SimpleTimeRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int, Int) -> Unit
) {
    var startHour by remember { mutableStateOf(9) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(17) }
    var endMinute by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间范围") },
        text = {
            Column {
                Text("开始时间: ${String.format("%02d:%02d", startHour, startMinute)}")
                Row {
                    OutlinedTextField(
                        value = startHour.toString(),
                        onValueChange = { startHour = it.toIntOrNull()?.coerceIn(0, 23) ?: startHour },
                        label = { Text("时") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = startMinute.toString(),
                        onValueChange = { startMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: startMinute },
                        label = { Text("分") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("结束时间: ${String.format("%02d:%02d", endHour, endMinute)}")
                Row {
                    OutlinedTextField(
                        value = endHour.toString(),
                        onValueChange = { endHour = it.toIntOrNull()?.coerceIn(0, 23) ?: endHour },
                        label = { Text("时") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = endMinute.toString(),
                        onValueChange = { endMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: endMinute },
                        label = { Text("分") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(startHour, startMinute, endHour, endMinute) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun WhitelistAppItem(
    app: AppInfo,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "移除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AppSelectorDialog(
    apps: List<AppInfo>,
    onDismiss: () -> Unit,
    onAppSelected: (AppInfo) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择应用") },
        text = {
            LazyColumn {
                items(apps, key = { it.packageName }) { app ->
                    TextButton(
                        onClick = { onAppSelected(app) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = app.label,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
