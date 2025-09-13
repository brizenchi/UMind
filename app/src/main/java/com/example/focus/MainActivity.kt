package com.example.focus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.focus.ui.theme.FocusTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusTheme {
                val navController = rememberNavController()
                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route
                
                // 判断是否在二级页面
                val isSecondaryPage = currentRoute == "focus_edit" || currentRoute?.startsWith("focus_edit/") == true
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (isSecondaryPage) {
                            TopAppBar(
                                title = { Text("编辑专注策略") },
                                navigationIcon = {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        androidx.compose.material3.Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                                    }
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (!isSecondaryPage) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentRoute == "focus",
                                    onClick = { navController.navigate("focus") },
                                    icon = { androidx.compose.material3.Icon(Icons.Filled.Timer, contentDescription = null) },
                                    label = { Text("专注") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "stats",
                                    onClick = { navController.navigate("stats") },
                                    icon = { androidx.compose.material3.Icon(Icons.Filled.BarChart, contentDescription = null) },
                                    label = { Text("统计") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "me",
                                    onClick = { navController.navigate("me") },
                                    icon = { androidx.compose.material3.Icon(Icons.Filled.Person, contentDescription = null) },
                                    label = { Text("我的") }
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        if (currentRoute == "focus") {
                            FloatingActionButton(
                                onClick = { navController.navigate("focus_edit") }
                            ) {
                                androidx.compose.material3.Icon(Icons.Filled.Add, contentDescription = "添加专注策略")
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(navController = navController, startDestination = "focus", modifier = Modifier.padding(innerPadding)) {
                        composable("focus") { FocusListScreen(navController) }
                        composable("focus_edit") { FocusEditScreen(navController) }
                        composable("focus_edit/{strategyId}") { backStackEntry ->
                            val strategyId = backStackEntry.arguments?.getString("strategyId")
                            FocusEditScreen(navController, strategyId)
                        }
                        composable("stats") { StatsScreen() }
                        composable("me") { MeScreen() }
                    }
                }
            }
        }
    }
}

@Composable
fun FocusListScreen(navController: androidx.navigation.NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var strategies by remember { mutableStateOf(listOf<FocusStrategy>()) }
    
    LaunchedEffect(Unit) {
        strategies = FocusRepository.getFocusStrategies(context)
    }
    
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "专注策略",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (strategies.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "还没有专注策略",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = "点击右下角的 + 号创建第一个专注策略",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(strategies) { strategy ->
                    FocusStrategyCard(
                        strategy = strategy,
                        onToggleActive = { isActive ->
                            FocusRepository.setStrategyActive(context, strategy.id, isActive)
                            strategies = FocusRepository.getFocusStrategies(context)
                        },
                        onEdit = { navController.navigate("focus_edit/${strategy.id}") },
                        onDelete = {
                            FocusRepository.deleteFocusStrategy(context, strategy.id)
                            strategies = FocusRepository.getFocusStrategies(context)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FocusStrategyCard(
    strategy: FocusStrategy,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strategy.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "%02d:%02d - %02d:%02d".format(
                            strategy.startHour, strategy.startMinute,
                            strategy.endHour, strategy.endMinute
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${strategy.blockedPackages.size} 个应用被屏蔽",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = strategy.isActive,
                    onCheckedChange = onToggleActive
                )
            }
            
            if (strategy.isActive) {
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "● 当前激活",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FocusEditScreen(navController: androidx.navigation.NavController, strategyId: String? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val pm = context.packageManager
    data class AppItem(val packageName: String, val label: String, val icon: android.graphics.Bitmap?)
    
    // 加载现有策略（如果是编辑模式）
    val existingStrategy = strategyId?.let { id ->
        FocusRepository.getFocusStrategies(context).find { it.id == id }
    }
    
    var strategyName by remember { mutableStateOf(existingStrategy?.name ?: "") }
    var apps by remember { mutableStateOf(listOf<AppItem>()) }
    var selected by remember { 
        mutableStateOf(existingStrategy?.blockedPackages ?: emptySet())
    }
    var startHour by remember { mutableStateOf(existingStrategy?.startHour ?: 9) }
    var startMinute by remember { mutableStateOf(existingStrategy?.startMinute ?: 0) }
    var endHour by remember { mutableStateOf(existingStrategy?.endHour ?: 18) }
    var endMinute by remember { mutableStateOf(existingStrategy?.endMinute ?: 0) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var lastSaveTime by remember { mutableStateOf(0L) }
    val handler = remember { Handler(Looper.getMainLooper()) }
    
    // 自动保存函数
    fun autoSave() {
        if (strategyName.isNotBlank()) {
            val strategy = FocusStrategy(
                id = strategyId ?: java.util.UUID.randomUUID().toString(),
                name = strategyName,
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute,
                blockedPackages = selected,
                isActive = existingStrategy?.isActive ?: false
            )
            FocusRepository.saveFocusStrategy(context, strategy)
            lastSaveTime = System.currentTimeMillis()
        }
    }

    LaunchedEffect(Unit) {
        val allApps = mutableSetOf<AppItem>()
        
        // 方法1: 尝试通过 queryIntentActivities 获取启动器应用
        try {
            val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(launcherIntent, 0)
            }
            resolveInfos.forEach { resolveInfo ->
                val packageName = resolveInfo.activityInfo?.packageName
                if (packageName != null && packageName != context.packageName) {
                    val label = resolveInfo.loadLabel(pm)?.toString() ?: packageName
                    val drawable = try { pm.getApplicationIcon(packageName) } catch (_: Exception) { null }
                    val bitmap = drawable?.toBitmap(width = 96, height = 96)
                    allApps.add(AppItem(packageName, label, bitmap))
                }
            }
        } catch (e: Exception) {
            // 忽略错误，继续尝试其他方法
        }
        
        // 方法2: 尝试 getInstalledApplications (可能被拒绝但还是试试)
        try {
            val installedApps = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledApplications(PackageManager.GET_META_DATA)
            }
            installedApps.forEach { appInfo ->
                val packageName = appInfo.packageName
                if (!packageName.startsWith("android.") && 
                    !packageName.startsWith("com.android.") && 
                    !packageName.startsWith("com.google.") &&
                    !packageName.startsWith("com.miui.") &&
                    !packageName.startsWith("com.xiaomi.") &&
                    packageName != context.packageName) {
                    
                    val label = try { pm.getApplicationLabel(appInfo).toString() } catch (_: Exception) { packageName }
                    val drawable = try { pm.getApplicationIcon(packageName) } catch (_: Exception) { null }
                    val bitmap = drawable?.toBitmap(width = 96, height = 96)
                    allApps.add(AppItem(packageName, label, bitmap))
                }
            }
        } catch (e: Exception) {
            // MIUI 可能会拒绝这个调用
        }
        
        // 方法3: 使用反射尝试获取 (高风险但可能有效)
        try {
            val pmClass = pm.javaClass
            val method = pmClass.getDeclaredMethod("getInstalledApplications", Int::class.java)
            method.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val installedApps = method.invoke(pm, PackageManager.GET_META_DATA) as? List<android.content.pm.ApplicationInfo> ?: emptyList()
            
            installedApps.forEach { appInfo ->
                val packageName = appInfo.packageName
                if (!packageName.startsWith("android.") && 
                    !packageName.startsWith("com.android.") && 
                    !packageName.startsWith("com.google.") &&
                    !packageName.startsWith("com.miui.") &&
                    !packageName.startsWith("com.xiaomi.") &&
                    packageName != context.packageName) {
                    
                    val label = try { pm.getApplicationLabel(appInfo).toString() } catch (_: Exception) { packageName }
                    val drawable = try { pm.getApplicationIcon(packageName) } catch (_: Exception) { null }
                    val bitmap = drawable?.toBitmap(width = 96, height = 96)
                    allApps.add(AppItem(packageName, label, bitmap))
                }
            }
        } catch (e: Exception) {
            // 反射可能失败
        }
        
        // 如果所有方法都失败，添加常见应用作为后备
        if (allApps.isEmpty()) {
            val commonApps = listOf(
                "com.tencent.mm" to "微信",
                "com.tencent.mobileqq" to "QQ",
                "com.sina.weibo" to "微博",
                "com.ss.android.ugc.aweme" to "抖音",
                "com.taobao.taobao" to "淘宝",
                "com.jingdong.app.mall" to "京东",
                "com.eg.android.AlipayGphone" to "支付宝",
                "tv.danmaku.bili" to "哔哩哔哩",
                "com.tencent.qqmusic" to "QQ音乐",
                "com.netease.cloudmusic" to "网易云音乐"
            )
            
            commonApps.forEach { (pkg, name) ->
                val drawable = try { pm.getApplicationIcon(pkg) } catch (_: Exception) { null }
                val bitmap = drawable?.toBitmap(width = 96, height = 96)
                allApps.add(AppItem(pkg, name, bitmap))
            }
        }
        
        apps = allApps.sortedBy { it.label.lowercase() }
    }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 策略名称输入
        OutlinedTextField(
            value = strategyName,
            onValueChange = { 
                strategyName = it
                // 延迟自动保存，避免频繁保存
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({ autoSave() }, 1000)
            },
            label = { Text("策略名称") },
            placeholder = { Text("如：工作专注、学习时间") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // 显示保存状态
        if (lastSaveTime > 0 && strategyName.isNotBlank()) {
            Text(
                text = "✅ 已自动保存",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        // 美观的时间选择器
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // 开始时间
            TimePickerCard(
                title = "开始时间",
                hour = startHour,
                minute = startMinute,
                onClick = { showStartTimePicker = true },
                modifier = Modifier.weight(1f)
            )
            
            // 结束时间
            TimePickerCard(
                title = "结束时间", 
                hour = endHour,
                minute = endMinute,
                onClick = { showEndTimePicker = true },
                modifier = Modifier.weight(1f)
            )
        }
        
        // 开始时间选择对话框
        if (showStartTimePicker) {
            TimePickerDialog(
                title = "选择开始时间",
                hour = startHour,
                minute = startMinute,
                onTimeSelected = { h, m ->
                    startHour = h
                    startMinute = m
                    showStartTimePicker = false
                    autoSave()
                },
                onDismiss = { showStartTimePicker = false }
            )
        }
        
        // 结束时间选择对话框
        if (showEndTimePicker) {
            TimePickerDialog(
                title = "选择结束时间",
                hour = endHour,
                minute = endMinute,
                onTimeSelected = { h, m ->
                    endHour = h
                    endMinute = m
                    showEndTimePicker = false
                    autoSave()
                },
                onDismiss = { showEndTimePicker = false }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // 返回按钮（替代保存按钮）
//            Button(
//                onClick = { navController.popBackStack() }
//            ) { Text("返回") }
//
//            OutlinedButton(onClick = {
//                context.startActivity(BlockAccessibilityService.openAccessibilitySettingsIntent())
//            }) { Text("启用无障碍服务") }
            
            // 检查是否有系统弹窗权限
            val hasOverlayPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
            
            if (!hasOverlayPermission) {
                OutlinedButton(onClick = {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                }) { Text("启用弹窗权限") }
            }
        }
        
        Text(
            text = "选择要屏蔽的应用：",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
//
//        if (selected.isNotEmpty()) {
//            Text(
//                text = "已选择 ${selected.size} 个应用",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
//
//        Text(
//            text = "💡 提示：点击应用图标或勾选框来选择/取消选择应用",
//            style = MaterialTheme.typography.bodySmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(apps) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val pkg = item.packageName
                            selected = if (selected.contains(pkg)) {
                                selected - pkg
                            } else {
                                selected + pkg
                            }
                            autoSave()
                        }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (item.icon != null) {
                            Image(
                                bitmap = item.icon.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .let { it }
                            )
                        }
                        Text(item.label)
                    }
                    Checkbox(checked = selected.contains(item.packageName), onCheckedChange = {
                        val pkg = item.packageName
                        selected = if (it) {
                            selected + pkg
                        } else {
                            selected - pkg
                        }
                        autoSave()
                    })
                }
            }
        }
    }
}

@Composable
fun StatsScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val hasUsageAccess = try {
        val appOps = context.getSystemService(android.content.Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, 
                android.os.Process.myUid(), 
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        mode == android.app.AppOpsManager.MODE_ALLOWED
    } catch (e: Exception) {
        false
    }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!hasUsageAccess) {
            Text("需要开启“使用情况访问”以统计应用使用时长")
            Button(onClick = {
                context.startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }) { Text("去开启") }
        } else {
            Text("统计占位：后续展示使用时长、打开次数、行为记录")
        }
    }
}

@Composable
fun MeScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "使用说明",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "如何使用专注模式：",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text("1. 创建专注策略并选择要阻止的应用")
                Text("2. 设置专注时间范围")
                Text("3. 启用无障碍服务权限")
                Text("4. 启用弹窗权限（用于显示阻止提示）")
                Text("5. 激活专注策略开关")
                Text("6. 在设定时间内打开被阻止的应用时，会显示提示并跳转到桌面")
            }
        }
        
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
                Spacer(modifier = Modifier.padding(4.dp))
                
                // 检查无障碍服务权限
                val hasAccessibilityService = try {
                    val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                    enabledServices?.contains(context.packageName) == true
                } catch (e: Exception) {
                    false
                }
                
                // 检查弹窗权限
                val hasOverlayPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else {
                    true
                }
                
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
                
                if (!hasAccessibilityService) {
                    Spacer(modifier = Modifier.padding(4.dp))
                    Button(onClick = {
                        context.startActivity(BlockAccessibilityService.openAccessibilitySettingsIntent())
                    }) {
                        Text("启用无障碍服务")
                    }
                }
                
                if (!hasOverlayPermission) {
                    Spacer(modifier = Modifier.padding(4.dp))
                    Button(onClick = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    }) {
                        Text("启用弹窗权限")
                    }
                }
            }
        }
        
        // 调试信息卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "调试信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.padding(4.dp))
                
                val strategies = remember { mutableStateOf(listOf<FocusStrategy>()) }
                
                LaunchedEffect(Unit) {
                    strategies.value = FocusRepository.getFocusStrategies(context)
                }
                
                Text("策略总数: ${strategies.value.size}")
                val activeStrategy = strategies.value.find { it.isActive }
                if (activeStrategy != null) {
                    Text("激活策略: ${activeStrategy.name}")
                    Text("阻止应用数: ${activeStrategy.blockedPackages.size}")
                    Text("时间范围: ${activeStrategy.startHour}:${String.format("%02d", activeStrategy.startMinute)} - ${activeStrategy.endHour}:${String.format("%02d", activeStrategy.endMinute)}")
                    Text("阻止的应用:")
                    activeStrategy.blockedPackages.take(3).forEach { pkg ->
                        Text("  • $pkg", style = MaterialTheme.typography.bodySmall)
                    }
                    if (activeStrategy.blockedPackages.size > 3) {
                        Text("  ... 还有 ${activeStrategy.blockedPackages.size - 3} 个", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Text("无激活策略")
                }
                
                Spacer(modifier = Modifier.padding(4.dp))
                Button(onClick = {
                    strategies.value = FocusRepository.getFocusStrategies(context)
                }) {
                    Text("刷新状态")
                }
            }
        }
        
        Text("版本：1.0")
    }
}

@Composable
fun TimePickerCard(
    title: String,
    hour: Int,
    minute: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = "%02d:%02d".format(hour, minute),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TimePickerDialog(
    title: String,
    hour: Int,
    minute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(hour) }
    var selectedMinute by remember { mutableStateOf(minute) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = "%02d:%02d".format(selectedHour, selectedMinute),
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 小时选择
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("小时", style = MaterialTheme.typography.labelSmall)
                        LazyColumn(
                            modifier = Modifier
                                .height(120.dp)
                                .width(60.dp)
                                .border(
                                    1.dp, 
                                    MaterialTheme.colorScheme.outline, 
                                    RoundedCornerShape(8.dp)
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(24) { h ->
                                val isSelected = h == selectedHour
                                Text(
                                    text = "%02d".format(h),
                                    modifier = Modifier
                                        .clickable { selectedHour = h }
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                                            else Color.Transparent
                                        )
                                        .padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary 
                                           else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    
                    // 分钟选择
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("分钟", style = MaterialTheme.typography.labelSmall)
                        LazyColumn(
                            modifier = Modifier
                                .height(120.dp)
                                .width(60.dp)
                                .border(
                                    1.dp, 
                                    MaterialTheme.colorScheme.outline, 
                                    RoundedCornerShape(8.dp)
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items((0..59 step 5).toList()) { m ->
                                val isSelected = m == selectedMinute
                                Text(
                                    text = "%02d".format(m),
                                    modifier = Modifier
                                        .clickable { selectedMinute = m }
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                                            else Color.Transparent
                                        )
                                        .padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary 
                                           else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onTimeSelected(selectedHour, selectedMinute) }) {
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

private fun createTimeInMillis(hour: Int, minute: Int): Long {
    val cal = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
        set(java.util.Calendar.HOUR_OF_DAY, hour)
        set(java.util.Calendar.MINUTE, minute)
    }
    return cal.timeInMillis
}

@Preview(showBackground = true)
@Composable
fun PreviewFocus() {
    FocusTheme { Text("Preview") }
}