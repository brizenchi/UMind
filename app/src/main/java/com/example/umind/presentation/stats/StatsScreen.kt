package com.example.umind.presentation.stats

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.umind.presentation.stats.components.AppUsageRankingList
import com.example.umind.presentation.stats.components.StatsOverviewCard
import com.example.umind.presentation.stats.components.UsageTrendChart
import com.example.umind.ui.components.FocusCard
import com.example.umind.ui.components.ImmersiveBackground
import com.example.umind.ui.theme.ComponentSpacing
import com.example.umind.ui.theme.CornerRadius

/**
 * 统计页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val hasUsageAccess = remember {
        try {
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
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Text(
                        text = "使用统计",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        ImmersiveBackground(modifier = Modifier.padding(paddingValues)) {
            if (!hasUsageAccess) {
                PermissionRequiredContent(
                    modifier = Modifier
                )
            } else {
                StatsContent(
                    uiState = uiState,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
private fun PermissionRequiredContent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(ComponentSpacing.pagePadding),
        verticalArrangement = Arrangement.spacedBy(ComponentSpacing.componentSpacing)
    ) {
        FocusCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CornerRadius.large),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ) {
            Column(
                modifier = Modifier.padding(ComponentSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)
            ) {
                Text(
                    text = "📊",
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = "需要权限",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "需要开启\"使用情况访问\"权限以统计应用使用时长",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(ComponentSpacing.smallSpacing))
                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CornerRadius.medium)
                ) {
                    Text("去开启")
                }
            }
        }
    }
}

@Composable
private fun StatsContent(
    uiState: StatsUiState,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is StatsUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is StatsUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "加载失败: ${uiState.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        is StatsUiState.Success -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(ComponentSpacing.pagePadding),
                verticalArrangement = Arrangement.spacedBy(ComponentSpacing.componentSpacing)
            ) {
                // Overview card
                StatsOverviewCard(
                    totalUsageDuration = uiState.dailyStats.totalUsageDurationMillis,
                    totalOpenCount = uiState.dailyStats.totalOpenCount,
                    totalBlockCount = uiState.dailyStats.totalBlockCount
                )

                // App usage ranking
                AppUsageRankingList(
                    appUsageStats = uiState.dailyStats.appUsageStats
                )

                // Weekly trend chart
                UsageTrendChart(
                    trends = uiState.weeklyTrend
                )

                // Bottom spacing
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
