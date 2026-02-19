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

    // 检查权限的函数
    fun checkPermissions() {
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 使用说明卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "如何使用专注模式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("1. 创建专注策略并选择要阻止的应用", style = MaterialTheme.typography.bodyMedium)
                Text("2. 设置专注时间范围", style = MaterialTheme.typography.bodyMedium)
                Text("3. 启用无障碍服务权限", style = MaterialTheme.typography.bodyMedium)
                Text("4. 启用弹窗权限（用于显示阻止提示）", style = MaterialTheme.typography.bodyMedium)
                Text("5. 激活专注策略开关", style = MaterialTheme.typography.bodyMedium)
                Text("6. 在设定时间内打开被阻止的应用时，会显示提示并跳转到桌面", style = MaterialTheme.typography.bodyMedium)
            }
        }

        // 重要提示卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚠️ 重要提示",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "由于Android系统安全机制，设备重启后无障碍服务会被自动关闭。重启后请重新启用无障碍服务，应用会在重启后发送通知提醒您。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 建议：在MIUI等系统中，请将本应用添加到自启动白名单，以便在重启后收到提醒通知。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // 权限检查卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "权限检查",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 无障碍服务状态
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("无障碍服务：")
                    Text(
                        text = if (hasAccessibilityService) "✅ 已启用" else "❌ 未启用",
                        color = if (hasAccessibilityService)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 弹窗权限状态
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("弹窗权限：")
                    Text(
                        text = if (hasOverlayPermission) "✅ 已启用" else "❌ 未启用",
                        color = if (hasOverlayPermission)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }

                // 启用无障碍服务按钮
                if (!hasAccessibilityService) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            context.startActivity(BlockAccessibilityService.openAccessibilitySettingsIntent())
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("启用无障碍服务")
                    }
                }

                // 启用弹窗权限按钮
                if (!hasOverlayPermission) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("启用弹窗权限")
                    }
                }
            }
        }

        // 电池优化和MIUI设置卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMiuiDevice && !hasBatteryOptimization)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isMiuiDevice) "🔋 MIUI设备优化设置" else "🔋 电池优化设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 电池优化状态
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("电池优化：")
                    Text(
                        text = if (hasBatteryOptimization) "✅ 已关闭" else "❌ 未关闭",
                        color = if (hasBatteryOptimization)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }

                if (!hasBatteryOptimization) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ 建议关闭电池优化，防止服务被系统杀死",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("关闭电池优化")
                    }
                }

                // MIUI特定设置
                if (isMiuiDevice) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "MIUI设备需要额外设置",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "为确保应用正常工作，请完成以下设置：",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // MIUI设置按钮
                    OutlinedButton(
                        onClick = {
                            MiuiDeviceHelper.openMiuiAutoStartSettings(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("1. 允许自启动")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            MiuiDeviceHelper.openMiuiBatterySaverSettings(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("2. 设置省电策略为无限制")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            MiuiDeviceHelper.openMiuiBackgroundPopupSettings(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("3. 允许后台弹出界面")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "💡 提示：在最近任务中长按UMind卡片，点击锁定图标防止被清理",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 关于卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "关于",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("版本", style = MaterialTheme.typography.bodyMedium)
                    Text("1.0.0", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // 调试工具卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔧 调试工具",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 显示今日使用记录
                if (uiState.todayRecords.isNotEmpty()) {
                    Text(
                        text = "今日使用记录:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    uiState.todayRecords.forEach { record ->
                        Text(
                            text = "• ${record.packageName}: 打开${record.openCount}次, 使用${record.usageDurationMillis / 60000}分钟",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else if (uiState.debugMessage.isNotEmpty()) {
                    Text(
                        text = uiState.debugMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.loadTodayRecords() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("查看今日记录")
                    }
                    Button(
                        onClick = { viewModel.clearTodayRecords() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("清除今日记录")
                    }
                }
            }
        }
    }
}
