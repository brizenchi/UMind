package com.example.umind.presentation.focusmode

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.umind.domain.model.AppInfo
import com.example.umind.domain.model.FocusModeType
import com.example.umind.ui.components.CountdownSelectorDialog
import com.example.umind.ui.components.AppSelectorDialog
import com.example.umind.ui.theme.ComponentSpacing
import com.example.umind.ui.theme.CornerRadius

/**
 * 番茄钟风格的专注模式页面 - Google Clock 风格
 */
@Composable
fun FocusModeScreen(
    viewModel: FocusModeViewModel = hiltViewModel()
) {
    val focusMode by viewModel.focusMode.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Pomo, 1: Stopwatch
    var showCountdownSelector by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // 顶部标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Focus",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = { showSettings = true }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Tab 导航
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Pomo") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Stopwatch") }
            )
        }

        // 内容区域
        when (selectedTab) {
            0 -> PomoTab(
                focusMode = focusMode,
                viewModel = viewModel,
                onShowCountdownSelector = { showCountdownSelector = true }
            )
            1 -> StopwatchTab(
                focusMode = focusMode,
                viewModel = viewModel
            )
        }
    }

    // 对话框
    if (showCountdownSelector) {
        CountdownSelectorDialog(
            onDismiss = { showCountdownSelector = false },
            onConfirm = { minutes ->
                viewModel.startCountdown(minutes)
                showCountdownSelector = false
            }
        )
    }

    if (showSettings) {
        FocusModeSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettings = false }
        )
    }
}

/**
 * Pomo Tab - 倒计时模式，带预设时长按钮
 */
@Composable
fun PomoTab(
    focusMode: com.example.umind.domain.model.FocusMode,
    viewModel: FocusModeViewModel,
    onShowCountdownSelector: () -> Unit
) {
    val isActive = focusMode.shouldBeActive() && focusMode.modeType == FocusModeType.COUNTDOWN

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isActive) {
            // 显示计时器
            Spacer(modifier = Modifier.weight(0.3f))
            PomodoroTimer(
                focusMode = focusMode,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(0.1f))

            // 停止按钮
            Button(
                onClick = { viewModel.stopFocusMode() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("停止", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.weight(0.2f))
        } else {
            // 显示预设时长按钮
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "选择专注时长",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 2x2 网格的预设时长按钮
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PresetDurationButton(
                        duration = "15:00",
                        minutes = 15,
                        onClick = { viewModel.startCountdown(15) },
                        modifier = Modifier.weight(1f)
                    )
                    PresetDurationButton(
                        duration = "25:00",
                        minutes = 25,
                        onClick = { viewModel.startCountdown(25) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PresetDurationButton(
                        duration = "40:00",
                        minutes = 40,
                        onClick = { viewModel.startCountdown(40) },
                        modifier = Modifier.weight(1f)
                    )
                    PresetDurationButton(
                        duration = "60:00",
                        minutes = 60,
                        onClick = { viewModel.startCountdown(60) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 自定义时长按钮
            OutlinedButton(
                onClick = onShowCountdownSelector,
                modifier = Modifier
                    .size(64.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "自定义时长",
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "自定义时长",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * 预设时长按钮
 */
@Composable
fun PresetDurationButton(
    duration: String,
    minutes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = duration,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Stopwatch Tab - 正计时模式
 */
@Composable
fun StopwatchTab(
    focusMode: com.example.umind.domain.model.FocusMode,
    viewModel: FocusModeViewModel
) {
    val isActive = focusMode.shouldBeActive() && focusMode.modeType == FocusModeType.MANUAL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.3f))

        // 计时器显示
        PomodoroTimer(
            focusMode = focusMode,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.weight(0.1f))

        // 控制按钮
        if (isActive) {
            Button(
                onClick = { viewModel.stopFocusMode() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("停止", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            Button(
                onClick = { viewModel.toggleFocusMode(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.weight(0.2f))
    }
}

/**
 * 番茄钟计时器 - 圆形进度显示
 */
@Composable
fun PomodoroTimer(
    focusMode: com.example.umind.domain.model.FocusMode,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // 每秒更新时间
    LaunchedEffect(focusMode.shouldBeActive()) {
        if (focusMode.shouldBeActive()) {
            while (true) {
                currentTime = System.currentTimeMillis()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    // 计算时间和进度
    val (hours, minutes, seconds, progress) = when {
        focusMode.shouldBeActive() -> {
            when (focusMode.modeType) {
                FocusModeType.MANUAL -> {
                    val elapsedMillis = currentTime - focusMode.updatedAt
                    val h = (elapsedMillis / 3600000).toInt()
                    val m = ((elapsedMillis % 3600000) / 60000).toInt()
                    val s = ((elapsedMillis % 60000) / 1000).toInt()
                    Tuple4(h, m, s, 0f) // 手动模式没有进度
                }
                FocusModeType.COUNTDOWN -> {
                    focusMode.countdownEndTime?.let { endTime ->
                        val remainingMillis = (endTime - currentTime).coerceAtLeast(0)
                        val totalMillis = (endTime - focusMode.updatedAt).coerceAtLeast(1)
                        val h = (remainingMillis / 3600000).toInt()
                        val m = ((remainingMillis % 3600000) / 60000).toInt()
                        val s = ((remainingMillis % 60000) / 1000).toInt()
                        val prog = 1f - (remainingMillis.toFloat() / totalMillis.toFloat())
                        Tuple4(h, m, s, prog)
                    } ?: Tuple4(0, 0, 0, 0f)
                }
                else -> Tuple4(0, 0, 0, 0f)
            }
        }
        else -> Tuple4(0, 0, 0, 0f)
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // 圆形进度指示器
        CircularProgressIndicator(
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            progress = progress,
            isActive = focusMode.shouldBeActive(),
            isCountdown = focusMode.modeType == FocusModeType.COUNTDOWN
        )
    }
}

/**
 * 圆形进度指示器 - 简化版
 */
@Composable
fun CircularProgressIndicator(
    hours: Int,
    minutes: Int,
    seconds: Int,
    progress: Float,
    isActive: Boolean,
    isCountdown: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (isCountdown) progress else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(240.dp)
    ) {
        // 背景圆环
        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 8.dp.toPx()

            // 背景圆
            drawArc(
                color = Color(0xFFE0E0E0),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            )

            // 进度圆弧（仅倒计时模式显示）
            if (isCountdown && isActive && animatedProgress > 0f) {
                drawArc(
                    color = Color(0xFF6750A4),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                )
            }
        }

        // 中心内容
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isActive) {
                // 时间显示 - 更大更清晰
                Text(
                    text = if (hours > 0) {
                        String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    } else {
                        String.format("%02d:%02d", minutes, seconds)
                    },
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                // 未激活状态 - 显示 00:00
                Text(
                    text = "00:00",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * 设置对话框（包含白名单管理）
 */
@Composable
fun FocusModeSettingsDialog(
    viewModel: FocusModeViewModel,
    onDismiss: () -> Unit
) {
    val whitelistedApps by viewModel.whitelistedApps.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val focusMode by viewModel.focusMode.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(CornerRadius.extraLarge)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "白名单应用",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }

                Text(
                    text = "专注模式下，只有白名单中的应用可以使用",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(ComponentSpacing.pagePadding))

                // 应用列表（直接显示所有应用，带复选框）
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)
                ) {
                    items(installedApps, key = { it.packageName }) { app ->
                        AppCheckboxItem(
                            app = app,
                            isChecked = focusMode.whitelistedApps.contains(app.packageName),
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    viewModel.addToWhitelist(app.packageName)
                                } else {
                                    viewModel.removeFromWhitelist(app.packageName)
                                }
                            },
                            onLoadIcon = { packageName ->
                                viewModel.getIconForApp(packageName)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 应用复选框项
 */
@Composable
fun AppCheckboxItem(
    app: AppInfo,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onLoadIcon: ((String) -> Bitmap?)? = null
) {
    // Track icon loading state
    var icon by remember(app.packageName) { mutableStateOf(app.icon) }

    // Load icon on-demand when item is displayed
    LaunchedEffect(app.packageName) {
        if (icon == null && onLoadIcon != null) {
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!isChecked) }
                .padding(ComponentSpacing.pagePadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Show icon if available, otherwise show placeholder
            if (icon != null) {
                androidx.compose.foundation.Image(
                    bitmap = icon!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            } else {
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

            Column(modifier = Modifier.weight(1f)) {
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

            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

/**
 * 白名单应用项
 */
@Composable
fun WhitelistAppItem(
    app: AppInfo,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.medium),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComponentSpacing.pagePadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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

// 辅助数据类
private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
