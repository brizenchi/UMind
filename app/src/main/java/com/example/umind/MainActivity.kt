package com.example.umind

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.umind.presentation.focus.AppSelectionScreen
import com.example.umind.presentation.focus.FocusEditScreen
import com.example.umind.presentation.focus.FocusListScreen
import com.example.umind.presentation.focusmode.FocusModeScreen
import com.example.umind.presentation.settings.SettingsScreen
import com.example.umind.presentation.stats.StatsScreen
import com.example.umind.ui.theme.FocusTheme
import com.example.umind.util.AccessibilityUtil
import com.example.umind.util.BatteryOptimizationHelper
import com.example.umind.util.MiuiDeviceHelper
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log

/**
 * 主Activity - 使用Hilt和Clean Architecture
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 通知权限请求
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 切换到正常主题（从启动屏幕主题切换）
        setTheme(R.style.Theme_UMind)

        enableEdgeToEdge()

        // 请求通知权限 (Android 13+)
        requestNotificationPermission()

        // 检查并请求电池优化豁免
        checkBatteryOptimization()

        // 如果是MIUI设备，记录日志提示用户
        if (MiuiDeviceHelper.isMiuiDevice()) {
            Log.d("MainActivity", "MIUI device detected: ${MiuiDeviceHelper.getMiuiVersion()}")
            Log.d("MainActivity", "Please ensure MIUI-specific permissions are granted in Settings")
        }

        setContent {
            FocusTheme {
                MainScreen()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("MainActivity", "Notification permission already granted")
                }
                else -> {
                    Log.d("MainActivity", "Requesting notification permission")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d("MainActivity", "Android version < 13, notification permission not required")
        }
    }

    /**
     * 检查电池优化状态
     * 如果未豁免，在首次启动时请求
     */
    private fun checkBatteryOptimization() {
        if (!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(this)) {
            Log.d("MainActivity", "Battery optimization is enabled, should request exemption")
            // 注意：这里不立即请求，而是在设置页面提供选项
            // 因为直接弹出可能会打扰用户体验
            // 如果需要立即请求，取消下面这行的注释：
            // BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(this)
        } else {
            Log.d("MainActivity", "Battery optimization is already disabled")
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次应用回到前台时检查无障碍服务状态
        checkAccessibilityService()
    }

    private fun checkAccessibilityService() {
        val isEnabled = AccessibilityUtil.isAccessibilityServiceEnabled(
            this,
            BlockAccessibilityService::class.java.name
        )
        // 这里可以根据需要显示提示，但为了不打扰用户，我们只在设置页面显示状态
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // 判断是否在二级页面
    val isSecondaryPage = currentRoute == "focus_edit" ||
                         currentRoute?.startsWith("focus_edit/") == true ||
                         currentRoute == "app_selection"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (isSecondaryPage) {
                TopAppBar(
                    title = {
                        Text(
                            when (currentRoute) {
                                "app_selection" -> "选择应用"
                                else -> "编辑专注策略"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!isSecondaryPage) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "daily_management",
                        onClick = { navController.navigate("daily_management") {
                            popUpTo("daily_management") { inclusive = true }
                        }},
                        icon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                        label = { Text("日常管理") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "focus_mode",
                        onClick = { navController.navigate("focus_mode") {
                            popUpTo("daily_management")
                        }},
                        icon = { Icon(Icons.Filled.LightMode, contentDescription = null) },
                        label = { Text("专注模式") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "stats",
                        onClick = { navController.navigate("stats") {
                            popUpTo("daily_management")
                        }},
                        icon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
                        label = { Text("统计") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "settings",
                        onClick = { navController.navigate("settings") {
                            popUpTo("daily_management")
                        }},
                        icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        label = { Text("我的") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "daily_management",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("daily_management") {
                FocusListScreen(navController = navController)
            }
            composable("focus_mode") {
                FocusModeScreen()
            }
            composable("focus_edit") {
                FocusEditScreen(navController = navController)
            }
            composable("focus_edit/{strategyId}") {
                FocusEditScreen(navController = navController)
            }
            composable("app_selection") { backStackEntry ->
                val previousBackStackEntry = navController.previousBackStackEntry
                AppSelectionScreen(
                    navController = navController,
                    initialSelectedPackages = emptySet(), // 从 savedStateHandle 获取
                    onAppsSelected = { selectedPackages ->
                        // 将选中的应用保存到 savedStateHandle
                        previousBackStackEntry?.savedStateHandle?.set(
                            "selected_packages",
                            selectedPackages
                        )
                    }
                )
            }
            composable("stats") {
                StatsScreen()
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}
