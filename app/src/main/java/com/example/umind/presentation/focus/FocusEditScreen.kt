package com.example.umind.presentation.focus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.umind.domain.model.*
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 策略名称
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("策略名称") },
            placeholder = { Text("如：工作专注、学习时间") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 选择要限制的应用
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                        text = "要限制的应用",
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
            text = "限制规则",
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
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
                        text = "激活后策略将立即生效",
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

@Composable
fun TimeRestrictionsSection(
    restrictions: List<TimeRestriction>,
    onAddRestriction: (Set<DayOfWeek>, LocalTime, LocalTime) -> Unit,
    onRemoveRestriction: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
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
            DayOfWeek.FRIDAY
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
        DayOfWeek.MONDAY to "一",
        DayOfWeek.TUESDAY to "二",
        DayOfWeek.WEDNESDAY to "三",
        DayOfWeek.THURSDAY to "四",
        DayOfWeek.FRIDAY to "五",
        DayOfWeek.SATURDAY to "六",
        DayOfWeek.SUNDAY to "日"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { (day, label) ->
            FilterChip(
                selected = day in selectedDays,
                onClick = {
                    onDaysChange(
                        if (day in selectedDays) {
                            selectedDays - day
                        } else {
                            selectedDays + day
                        }
                    )
                },
                label = { Text(label) }
            )
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
    var showIndividualDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                            LimitType.INDIVIDUAL -> {
                                Text(
                                    text = "单独设置",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${limits.individualLimits.size} 个应用已设置",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = {
                        if (limits.type == LimitType.INDIVIDUAL) {
                            showIndividualDialog = true
                        } else {
                            showDialog = true
                        }
                    },
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
                    LimitType.INDIVIDUAL -> {
                        showDialog = false
                        showIndividualDialog = true
                    }
                }
                if (type != LimitType.INDIVIDUAL) {
                    showDialog = false
                }
            }
        )
    }

    if (showIndividualDialog) {
        IndividualUsageLimitsDialog(
            selectedApps = selectedApps,
            currentLimits = limits?.individualLimits ?: emptyMap(),
            onDismiss = {
                showIndividualDialog = false
                if (limits == null) enabled = false
            },
            onConfirm = { individualLimits ->
                onUpdateLimits(
                    UsageLimits(
                        type = LimitType.INDIVIDUAL,
                        individualLimits = individualLimits
                    )
                )
                showIndividualDialog = false
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
                    RadioButton(
                        selected = selectedType == LimitType.INDIVIDUAL,
                        onClick = { selectedType = LimitType.INDIVIDUAL },
                        label = "单独设置每个应用"
                    )
                }

                if (selectedType != LimitType.INDIVIDUAL) {
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedType, hours, minutes) }
            ) {
                Text(if (selectedType == LimitType.INDIVIDUAL) "下一步" else "确定")
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
    var showIndividualDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                            LimitType.INDIVIDUAL -> {
                                Text(
                                    text = "单独设置",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${limits.individualCounts.size} 个应用已设置",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = {
                        if (limits.type == LimitType.INDIVIDUAL) {
                            showIndividualDialog = true
                        } else {
                            showDialog = true
                        }
                    },
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
                    LimitType.INDIVIDUAL -> {
                        showDialog = false
                        showIndividualDialog = true
                    }
                }
                if (type != LimitType.INDIVIDUAL) {
                    showDialog = false
                }
            }
        )
    }

    if (showIndividualDialog) {
        IndividualOpenCountLimitsDialog(
            selectedApps = selectedApps,
            currentLimits = limits?.individualCounts ?: emptyMap(),
            onDismiss = {
                showIndividualDialog = false
                if (limits == null) enabled = false
            },
            onConfirm = { individualCounts ->
                onUpdateLimits(
                    OpenCountLimits(
                        type = LimitType.INDIVIDUAL,
                        individualCounts = individualCounts
                    )
                )
                showIndividualDialog = false
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
                    RadioButton(
                        selected = selectedType == LimitType.INDIVIDUAL,
                        onClick = { selectedType = LimitType.INDIVIDUAL },
                        label = "单独设置每个应用"
                    )
                }

                if (selectedType != LimitType.INDIVIDUAL) {
                    Text("次数设置：", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = count.toString(),
                        onValueChange = { it.toIntOrNull()?.let { c -> count = c } },
                        label = { Text("次数") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedType, count) }
            ) {
                Text(if (selectedType == LimitType.INDIVIDUAL) "下一步" else "确定")
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeChange(enforcementMode) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (mode == enforcementMode) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
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

@Composable
fun IndividualUsageLimitsDialog(
    selectedApps: Set<String>,
    currentLimits: Map<String, kotlin.time.Duration>,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, kotlin.time.Duration>) -> Unit
) {
    var limits by remember {
        mutableStateOf(
            selectedApps.associateWith { pkg ->
                currentLimits[pkg] ?: 30.minutes
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("单独设置每个应用时长") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedApps.isEmpty()) {
                    Text(
                        text = "请先选择要限制的应用",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    selectedApps.forEach { packageName ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = packageName.substringAfterLast('.'),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val currentLimit = limits[packageName] ?: 30.minutes
                                var hours by remember {
                                    mutableStateOf((currentLimit.inWholeMinutes / 60).toInt())
                                }
                                var minutes by remember {
                                    mutableStateOf((currentLimit.inWholeMinutes % 60).toInt())
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = hours.toString(),
                                        onValueChange = {
                                            it.toIntOrNull()?.let { h ->
                                                hours = h
                                                limits = limits + (packageName to (h * 60 + minutes).minutes)
                                            }
                                        },
                                        label = { Text("小时") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = minutes.toString(),
                                        onValueChange = {
                                            it.toIntOrNull()?.let { m ->
                                                minutes = m
                                                limits = limits + (packageName to (hours * 60 + m).minutes)
                                            }
                                        },
                                        label = { Text("分钟") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(limits) },
                enabled = selectedApps.isNotEmpty()
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
fun IndividualOpenCountLimitsDialog(
    selectedApps: Set<String>,
    currentLimits: Map<String, Int>,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Int>) -> Unit
) {
    var limits by remember {
        mutableStateOf(
            selectedApps.associateWith { pkg ->
                currentLimits[pkg] ?: 5
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("单独设置每个应用次数") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedApps.isEmpty()) {
                    Text(
                        text = "请先选择要限制的应用",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    selectedApps.forEach { packageName ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = packageName.substringAfterLast('.'),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val currentLimit = limits[packageName] ?: 5
                                var count by remember { mutableStateOf(currentLimit) }

                                OutlinedTextField(
                                    value = count.toString(),
                                    onValueChange = {
                                        it.toIntOrNull()?.let { c ->
                                            count = c
                                            limits = limits + (packageName to c)
                                        }
                                    },
                                    label = { Text("每日打开次数") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(limits) },
                enabled = selectedApps.isNotEmpty()
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
