package com.example.umind.presentation.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.umind.BlockAccessibilityService
import com.example.umind.util.AccessibilityUtil
import com.example.umind.util.BatteryOptimizationHelper
import com.example.umind.util.MiuiDeviceHelper

/**
 * 设置页面（我的）
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    // 使用 mutableState 以便可以更新
    var hasAccessibilityService by remember { mutableStateOf(false) }
    var hasOverlayPermission by remember { mutableStateOf(false) }
    var hasBatteryOptimization by remember { mutableStateOf(false) }
    val isMiuiDevice = remember { MiuiDeviceHelper.isMiuiDevice() }

    // Track last check time to avoid excessive permission checks
    var lastCheckTime by remember { mutableStateOf(0L) }
    val CHECK_INTERVAL_MS = 3000L // Check at most every 3 seconds

    // 检查权限的函数
    fun checkPermissions() {
        val now = System.currentTimeMillis()
        if (now - lastCheckTime < CHECK_INTERVAL_MS) {
            return // Skip if checked recently
        }
        lastCheckTime = now

        hasAccessibilityService = AccessibilityUtil.isAccessibilityServiceEnabled(
            context,
            BlockAccessibilityService::class.java.name
        )
        hasOverlayPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
        hasBatteryOptimization = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
    }

    // 初始检查
    LaunchedEffect(Unit) {
        checkPermissions()
    }

    // 监听生命周期，当页面恢复时重新检查权限
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "我的",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 权限状态卡片 - 简洁版
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "权限状态",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 无障碍服务
                PermissionItem(
                    title = "无障碍服务",
                    isEnabled = hasAccessibilityService,
                    onClick = {
                        context.startActivity(BlockAccessibilityService.openAccessibilitySettingsIntent())
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 弹窗权限
                PermissionItem(
                    title = "弹窗权限",
                    isEnabled = hasOverlayPermission,
                    onClick = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 电池优化
                PermissionItem(
                    title = "电池优化",
                    isEnabled = hasBatteryOptimization,
                    onClick = {
                        BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                    }
                )
            }
        }

        // MIUI特定设置 - 仅在MIUI设备上显示
        if (isMiuiDevice) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "MIUI 额外设置",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { MiuiDeviceHelper.openMiuiAutoStartSettings(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("允许自启动")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { MiuiDeviceHelper.openMiuiBatterySaverSettings(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("省电策略")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { MiuiDeviceHelper.openMiuiBackgroundPopupSettings(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("后台弹出")
                    }
                }
            }
        }

        // 关于
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "版本",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "1.0.0",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )

        if (isEnabled) {
            Text(
                text = "已启用",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            TextButton(onClick = onClick) {
                Text("去设置")
            }
        }
    }
}
