package com.example.umind.presentation.focus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.umind.domain.model.*
import com.example.umind.ui.components.FocusCard
import com.example.umind.ui.components.ImmersiveBackground
import com.example.umind.ui.components.ScreenHeader
import java.time.DayOfWeek
import java.time.LocalTime
import kotlin.time.Duration.Companion.minutes

/**
 * 专注策略编辑页面 - 完整版
 */
@Composable
fun FocusEditScreen(
    navController: NavController,
    viewModel: FocusEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 监听从应用选择页面返回的结果
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow<Set<String>?>("selected_packages", null)
            ?.collect { packages ->
                packages?.let {
                    viewModel.updateSelectedPackages(it)
                    savedStateHandle.remove<Set<String>>("selected_packages")
                }
            }
    }

    ImmersiveBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenHeader(
                title = "编辑应用组",
                subtitle = "设置规则后，系统将在目标时间自动拦截"
            )

        // 应用组名称
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("应用组名称") },
            placeholder = { Text("如：社媒、信息门户、娱乐") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 选择要限制的应用
        FocusCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            onClick = {
                // 在导航前，将当前已选中的应用保存到 savedStateHandle
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "current_selected_packages",
                    uiState.selectedPackages
                )
                navController.navigate("app_selection")
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "组内受限应用",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (uiState.selectedPackages.isEmpty()) {
                            "未选择"
                        } else {
                            "${uiState.selectedPackages.size} 个应用已选择"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "选择应用",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 规则标题
        Text(
            text = "组策略规则",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // 时间范围限制
        TimeRestrictionsSection(
            restrictions = uiState.timeRestrictions,
            onAddRestriction = { days, start, end ->
                viewModel.addTimeRestriction(days, start, end)
            },
            onRemoveRestriction = { id ->
                viewModel.removeTimeRestriction(id)
            }
        )

        // 使用时长限制
        UsageLimitsSection(
            limits = uiState.usageLimits,
            selectedApps = uiState.selectedPackages,
            onUpdateLimits = { viewModel.updateUsageLimits(it) },
            onSetTotalAll = { h, m -> viewModel.setUsageLimitsTotalAll(h, m) },
            onSetPerApp = { h, m -> viewModel.setUsageLimitsPerApp(h, m) }
        )

        // 打开次数限制
        OpenCountLimitsSection(
            limits = uiState.openCountLimits,
            selectedApps = uiState.selectedPackages,
            onUpdateLimits = { viewModel.updateOpenCountLimits(it) },
            onSetTotalAll = { count -> viewModel.setOpenCountLimitsTotalAll(count) },
            onSetPerApp = { count -> viewModel.setOpenCountLimitsPerApp(count) }
        )

        // 执行模式
        EnforcementModeSection(
            mode = uiState.enforcementMode,
            onModeChange = { viewModel.updateEnforcementMode(it) }
        )

        // 激活选项
        FocusCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f),
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "保存后立即激活",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "激活后应用组规则将立即生效",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isActive,
                    onCheckedChange = { viewModel.updateIsActive(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 底部按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }
            Button(
                onClick = {
                    viewModel.saveStrategy {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isSaving && uiState.name.isNotBlank()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("保存")
                }
            }
        }
        }
    }
}

@Composable
fun TimeRestrictionsSection(
    restrictions: List<TimeRestriction>,
    onAddRestriction: (Set<DayOfWeek>, LocalTime, LocalTime) -> Unit,
    onRemoveRestriction: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    FocusCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "⏰",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Text(
                        text = "时间范围限制",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 显示已添加的时间限制
            if (restrictions.isNotEmpty()) {
                restrictions.forEach { restriction ->
                    FocusCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
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
                                    text = restriction.getTimeRangeString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = restriction.getDaysString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onRemoveRestriction(restriction.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // 添加时间段按钮
            TextButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加时间段",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加时间段")
            }
        }
    }

    if (showAddDialog) {
        AddTimeRestrictionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { days, start, end ->
                onAddRestriction(days, start, end)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddTimeRestrictionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Set<DayOfWeek>, LocalTime, LocalTime) -> Unit
) {
    var selectedDays by remember {
        mutableStateOf(setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        ))
    }
    var startHour by remember { mutableStateOf(9) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(18) }
    var endMinute by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加时间限制") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 星期选择
                Text("选择星期：", style = MaterialTheme.typography.titleSmall)
                DayOfWeekSelector(
                    selectedDays = selectedDays,
                    onDaysChange = { selectedDays = it }
                )

                // 时间选择
                Text("开始时间：", style = MaterialTheme.typography.titleSmall)
                TimeSelector(
                    hour = startHour,
                    minute = startMinute,
                    onTimeChange = { h, m ->
                        startHour = h
                        startMinute = m
                    }
                )

                Text("结束时间：", style = MaterialTheme.typography.titleSmall)
                TimeSelector(
                    hour = endHour,
                    minute = endMinute,
                    onTimeChange = { h, m ->
                        endHour = h
                        endMinute = m
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        selectedDays,
                        LocalTime.of(startHour, startMinute),
                        LocalTime.of(endHour, endMinute)
                    )
                },
                enabled = selectedDays.isNotEmpty()
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
}

@Composable
fun DayOfWeekSelector(
    selectedDays: Set<DayOfWeek>,
    onDaysChange: (Set<DayOfWeek>) -> Unit
) {
    val days = listOf(
        DayOfWeek.MONDAY to "M",
        DayOfWeek.TUESDAY to "T",
        DayOfWeek.WEDNESDAY to "W",
        DayOfWeek.THURSDAY to "T",
        DayOfWeek.FRIDAY to "F",
        DayOfWeek.SATURDAY to "S",
        DayOfWeek.SUNDAY to "S"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { (day, label) ->
            val isSelected = day in selectedDays
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        onDaysChange(
                            if (day in selectedDays) {
                                selectedDays - day
                            } else {
                                selectedDays + day
                            }
                        )
                    },
                shape = CircleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                border = if (isSelected) {
                    null
                } else {
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun TimeSelector(
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hour
        OutlinedTextField(
            value = hour.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { h ->
                    if (h in 0..23) onTimeChange(h, minute)
                }
            },
            label = { Text("时") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        Text(":")

        // Minute
        OutlinedTextField(
            value = minute.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { m ->
                    if (m in 0..59) onTimeChange(hour, m)
                }
            },
            label = { Text("分") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }
}

@Composable
fun UsageLimitsSection(
    limits: UsageLimits?,
    selectedApps: Set<String>,
    onUpdateLimits: (UsageLimits?) -> Unit,
    onSetTotalAll: (Int, Int) -> Unit,
    onSetPerApp: (Int, Int) -> Unit
) {
    var enabled by remember(limits) { mutableStateOf(limits != null) }
    var showDialog by remember { mutableStateOf(false) }

    FocusCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题和开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "⏱️",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Text(
                        text = "使用时长限制",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        if (!it) {
                            onUpdateLimits(null)
                        } else {
                            showDialog = true
                        }
                    }
                )
            }

            // 显示当前限制
            if (enabled && limits != null) {
                FocusCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        when (limits.type) {
                            LimitType.TOTAL_ALL -> {
                                Text(
                                    text = "所有应用总时长",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                limits.totalLimit?.let {
                                    Text(
                                        text = "${it.inWholeHours}小时 ${it.inWholeMinutes % 60}分钟",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            LimitType.PER_APP -> {
                                Text(
                                    text = "每个应用时长",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                limits.perAppLimit?.let {
                                    Text(
                                        text = "${it.inWholeHours}小时 ${it.inWholeMinutes % 60}分钟",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                TextButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("修改设置")
                }
            }
        }
    }

    if (showDialog) {
        UsageLimitsDialog(
            currentLimits = limits,
            onDismiss = {
                showDialog = false
                if (limits == null) enabled = false
            },
            onConfirm = { type, hours, minutes ->
                when (type) {
                    LimitType.TOTAL_ALL -> onSetTotalAll(hours, minutes)
                    LimitType.PER_APP -> onSetPerApp(hours, minutes)
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun UsageLimitsDialog(
    currentLimits: UsageLimits?,
    onDismiss: () -> Unit,
    onConfirm: (LimitType, Int, Int) -> Unit
) {
    var selectedType by remember { mutableStateOf(currentLimits?.type ?: LimitType.TOTAL_ALL) }
    var hours by remember { mutableStateOf(0) }
    var minutes by remember { mutableStateOf(30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置使用时长限制") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 类型选择
                Text("限制类型：", style = MaterialTheme.typography.titleSmall)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RadioButton(
                        selected = selectedType == LimitType.TOTAL_ALL,
                        onClick = { selectedType = LimitType.TOTAL_ALL },
                        label = "所有应用总时长"
                    )
                    RadioButton(
                        selected = selectedType == LimitType.PER_APP,
                        onClick = { selectedType = LimitType.PER_APP },
                        label = "每个应用相同时长"
                    )
                }

                Text("时长设置：", style = MaterialTheme.typography.titleSmall)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hours.toString(),
                        onValueChange = { it.toIntOrNull()?.let { h -> hours = h } },
                        label = { Text("小时") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minutes.toString(),
                        onValueChange = { it.toIntOrNull()?.let { m -> minutes = m } },
                        label = { Text("分钟") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedType, hours, minutes) }
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
}

@Composable
fun RadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        androidx.compose.material3.RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(text = label)
    }
}

@Composable
fun OpenCountLimitsSection(
    limits: OpenCountLimits?,
    selectedApps: Set<String>,
    onUpdateLimits: (OpenCountLimits?) -> Unit,
    onSetTotalAll: (Int) -> Unit,
    onSetPerApp: (Int) -> Unit
) {
    var enabled by remember(limits) { mutableStateOf(limits != null) }
    var showDialog by remember { mutableStateOf(false) }

    FocusCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题和开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "🔄",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Text(
                        text = "打开次数限制",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        if (!it) {
                            onUpdateLimits(null)
                        } else {
                            showDialog = true
                        }
                    }
                )
            }

            // 显示当前限制
            if (enabled && limits != null) {
                FocusCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        when (limits.type) {
                            LimitType.TOTAL_ALL -> {
                                Text(
                                    text = "所有应用总次数",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                limits.totalCount?.let {
                                    Text(
                                        text = "$it 次",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            LimitType.PER_APP -> {
                                Text(
                                    text = "每个应用次数",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                limits.perAppCount?.let {
                                    Text(
                                        text = "$it 次",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                TextButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("修改设置")
                }
            }
        }
    }

    if (showDialog) {
        OpenCountLimitsDialog(
            currentLimits = limits,
            onDismiss = {
                showDialog = false
                if (limits == null) enabled = false
            },
            onConfirm = { type, count ->
                when (type) {
                    LimitType.TOTAL_ALL -> onSetTotalAll(count)
                    LimitType.PER_APP -> onSetPerApp(count)
                }
                showDialog = false

            }
        )
    }
}

@Composable
fun OpenCountLimitsDialog(
    currentLimits: OpenCountLimits?,
    onDismiss: () -> Unit,
    onConfirm: (LimitType, Int) -> Unit
) {
    var selectedType by remember { mutableStateOf(currentLimits?.type ?: LimitType.TOTAL_ALL) }
    var count by remember { mutableStateOf(10) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置打开次数限制") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 类型选择
                Text("限制类型：", style = MaterialTheme.typography.titleSmall)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RadioButton(
                        selected = selectedType == LimitType.TOTAL_ALL,
                        onClick = { selectedType = LimitType.TOTAL_ALL },
                        label = "所有应用总次数"
                    )
                    RadioButton(
                        selected = selectedType == LimitType.PER_APP,
                        onClick = { selectedType = LimitType.PER_APP },
                        label = "每个应用相同次数"
                    )
                }

                Text("次数设置：", style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = count.toString(),
                    onValueChange = { it.toIntOrNull()?.let { c -> count = c } },
                    label = { Text("次数") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedType, count) }
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
}

@Composable
fun EnforcementModeSection(
    mode: EnforcementMode,
    onModeChange: (EnforcementMode) -> Unit
) {
    FocusCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "⚙️",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                Text(
                    text = "执行模式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            // 模式选择
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EnforcementMode.values().forEach { enforcementMode ->
                    FocusCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeChange(enforcementMode) },
                        containerColor = if (mode == enforcementMode) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        },
                        borderColor = if (mode == enforcementMode) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = mode == enforcementMode,
                                onClick = { onModeChange(enforcementMode) }
                            )
                            Column {
                                Text(
                                    text = enforcementMode.getDisplayName(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = enforcementMode.getDescription(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
