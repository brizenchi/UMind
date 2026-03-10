package com.example.umind.presentation.focus

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.umind.domain.model.FocusStrategy
import com.example.umind.domain.model.OpenCountLimits
import com.example.umind.domain.model.TimeRestriction
import com.example.umind.domain.model.UsageLimits
import com.example.umind.ui.components.FocusCard
import com.example.umind.ui.components.ImmersiveBackground
import com.example.umind.ui.components.ModernDialog
import com.example.umind.ui.components.ScreenHeader
import com.example.umind.ui.theme.ComponentSpacing
import com.example.umind.util.InternalLaunchTracker
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusDetailScreen(
    navController: NavController,
    viewModel: FocusDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val strategy = uiState.strategy
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = strategy?.name ?: "应用组详情")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    if (strategy != null && !uiState.isLoading) {
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                enabled = !uiState.isDeleting
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "更多操作"
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("编辑应用组") },
                                    onClick = {
                                        showMenu = false
                                        navController.navigate("focus_edit/${strategy.id}")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("删除应用组") },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        ImmersiveBackground(modifier = Modifier.padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.isNotFound || strategy == null -> {
                    NotFoundState(
                        errorMessage = uiState.error,
                        onBack = { navController.popBackStack() }
                    )
                }
                else -> {
                    FocusDetailContent(
                        strategy = strategy,
                        appLabelMap = uiState.appLabelMap,
                        loadedIcons = uiState.loadedIcons,
                        todayUsedDurationMillis = uiState.totalUsageDurationMillis,
                        todayOpenCount = uiState.totalOpenCount,
                        isUpdatingActive = uiState.isUpdatingActive,
                        isDeleting = uiState.isDeleting,
                        errorMessage = uiState.error,
                        onToggleActive = viewModel::toggleStrategyActive,
                        onLoadIcon = viewModel::getIconForApp,
                        onDismissError = viewModel::clearError
                    )
                }
            }
        }
    }

    if (showDeleteDialog && strategy != null) {
        ModernDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "删除应用组",
            message = "确定要删除 \"${strategy.name}\" 吗？此操作无法撤销。",
            confirmText = "删除",
            dismissText = "取消",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteStrategy {
                    navController.popBackStack()
                }
            },
            isDangerous = true
        )
    }
}

@Composable
private fun NotFoundState(
    errorMessage: String?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ComponentSpacing.pagePadding),
        verticalArrangement = Arrangement.spacedBy(ComponentSpacing.componentSpacing)
    ) {
        ScreenHeader(
            title = "应用组详情",
            subtitle = "未找到对应的应用组"
        )

        FocusCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
        ) {
            Column(
                modifier = Modifier.padding(ComponentSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)
            ) {
                Text(
                    text = errorMessage ?: "应用组不存在或已删除",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onBack) {
                    Text("返回列表")
                }
            }
        }
    }
}

@Composable
private fun FocusDetailContent(
    strategy: FocusStrategy,
    appLabelMap: Map<String, String>,
    loadedIcons: Map<String, Bitmap?>,
    todayUsedDurationMillis: Long,
    todayOpenCount: Int,
    isUpdatingActive: Boolean,
    isDeleting: Boolean,
    errorMessage: String?,
    onToggleActive: (Boolean) -> Unit,
    onLoadIcon: (String) -> Bitmap?,
    onDismissError: () -> Unit
) {
    val context = LocalContext.current
    val appItems = remember(strategy.targetApps, appLabelMap) {
        strategy.targetApps
            .map { packageName ->
                packageName to (appLabelMap[packageName] ?: packageName)
            }
            .sortedBy { (_, label) -> label }
    }
    val canToggle = !isUpdatingActive && !isDeleting

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(ComponentSpacing.pagePadding),
        verticalArrangement = Arrangement.spacedBy(ComponentSpacing.componentSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScreenHeader(
                title = "应用组详情",
                subtitle = "查看受限应用与已配置策略",
                modifier = Modifier.weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isUpdatingActive) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "启用",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = strategy.isActive,
                        onCheckedChange = onToggleActive,
                        enabled = canToggle
                    )
                }
            }
        }

        FocusCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(ComponentSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)
            ) {
                Text(
                    text = "今日使用统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                RestrictionRow(
                    label = "已使用时长",
                    value = formatDurationMinutes(todayUsedDurationMillis / 60000)
                )
                RestrictionRow(
                    label = "已打开次数",
                    value = "${todayOpenCount} 次"
                )
            }
        }

        if (errorMessage != null) {
            FocusCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ComponentSpacing.cardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDismissError) {
                        Text("关闭")
                    }
                }
            }
        }

        FocusCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(ComponentSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)
            ) {
                Text(
                    text = "策略规则",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                RestrictionRow(
                    label = "时间范围限制",
                    value = if (strategy.timeRestrictions.isEmpty()) "未设置" else "${strategy.timeRestrictions.size} 条"
                )
                if (strategy.timeRestrictions.isNotEmpty()) {
                    strategy.timeRestrictions.forEach { restriction ->
                        Text(
                            text = "• ${formatTimeRestriction(restriction)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                RestrictionRow(
                    label = "使用时长限制",
                    value = formatUsageLimits(strategy.usageLimits)
                )
                RestrictionRow(
                    label = "打开次数限制",
                    value = formatOpenCountLimits(strategy.openCountLimits)
                )
                RestrictionRow(
                    label = "执行方式",
                    value = strategy.enforcementMode.getDisplayName()
                )
            }
        }

        FocusCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(ComponentSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)
            ) {
                Text(
                    text = "受限应用",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (appItems.isEmpty()) {
                    Text(
                        text = "未选择应用",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    appItems.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { (packageName, label) ->
                                RestrictedAppGridItem(
                                    modifier = Modifier.weight(1f),
                                    appName = label,
                                    packageName = packageName,
                                    loadedIcon = loadedIcons[packageName],
                                    onLoadIcon = onLoadIcon,
                                    onClick = {
                                        openRestrictedApp(context, packageName)
                                    }
                                )
                            }

                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RestrictionRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RestrictedAppGridItem(
    modifier: Modifier = Modifier,
    appName: String,
    packageName: String,
    loadedIcon: Bitmap?,
    onLoadIcon: (String) -> Bitmap?,
    onClick: () -> Unit
) {
    var icon by remember(packageName, loadedIcon) { mutableStateOf(loadedIcon) }

    LaunchedEffect(packageName) {
        if (icon == null) {
            val cached = onLoadIcon(packageName)
            if (cached != null) {
                icon = cached
            }
        }
    }

    LaunchedEffect(loadedIcon) {
        if (loadedIcon != null) {
            icon = loadedIcon
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Image(
                    bitmap = icon!!.asImageBitmap(),
                    contentDescription = appName,
                    modifier = Modifier.size(56.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun openRestrictedApp(context: Context, packageName: String) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent == null) {
        Toast.makeText(context, "无法打开该应用", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        InternalLaunchTracker.markLaunchFromUMind(context, packageName)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
    } catch (e: Exception) {
        InternalLaunchTracker.clear(context)
        Toast.makeText(context, "启动应用失败", Toast.LENGTH_SHORT).show()
    }
}

private fun formatTimeRestriction(restriction: TimeRestriction): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return "${restriction.getDaysString()} ${restriction.startTime.format(formatter)} - ${restriction.endTime.format(formatter)}"
}

private fun formatUsageLimits(limits: UsageLimits?): String {
    if (limits == null) {
        return "未设置"
    }
    return "组内总时长 ${formatDurationMinutes(limits.effectiveLimit()?.inWholeMinutes)}"
}

private fun formatOpenCountLimits(limits: OpenCountLimits?): String {
    if (limits == null) {
        return "未设置"
    }
    return "组内总次数 ${limits.effectiveCount() ?: 0} 次"
}

private fun formatDurationMinutes(minutes: Long?): String {
    if (minutes == null || minutes <= 0) {
        return "0 分钟"
    }
    val hours = minutes / 60
    val remainMinutes = minutes % 60
    return when {
        hours > 0 && remainMinutes > 0 -> "${hours} 小时 ${remainMinutes} 分钟"
        hours > 0 -> "${hours} 小时"
        else -> "${remainMinutes} 分钟"
    }
}
