package com.example.umind.presentation.focus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.umind.domain.model.FocusStrategy
import com.example.umind.ui.components.FocusCard
import com.example.umind.ui.components.ImmersiveBackground
import com.example.umind.ui.components.ScreenHeader
import com.example.umind.ui.components.StatusPill
import com.example.umind.ui.theme.ComponentSpacing
import com.example.umind.ui.theme.CornerRadius
import com.example.umind.ui.theme.Spacing

/**
 * 应用组列表页面
 */
@Composable
fun FocusListScreen(
    navController: NavController,
    viewModel: FocusListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("focus_edit") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加应用组")
            }
        }
    ) { paddingValues ->
        ImmersiveBackground(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ComponentSpacing.pagePadding),
                verticalArrangement = Arrangement.spacedBy(ComponentSpacing.componentSpacing)
            ) {
                ScreenHeader(
                    title = "应用组",
                    subtitle = "按类型管理受限应用，每个组可配置不同限制策略"
                )

                when (val state = uiState) {
                    is FocusListUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is FocusListUiState.Empty -> {
                        EmptyStateCard()
                    }

                    is FocusListUiState.Success -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)
                        ) {
                            items(state.strategies) { strategy ->
                                FocusStrategyCard(
                                    strategy = strategy,
                                    appLabelMap = state.appLabelMap,
                                    onClick = {
                                        navController.navigate("focus_detail/${strategy.id}")
                                    }
                                )
                            }
                        }
                    }

                    is FocusListUiState.Error -> {
                        ErrorCard(message = state.message)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    FocusCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)
        ) {
            Text(
                text = "📝",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = "还没有应用组",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "点击右下角的 + 号创建第一个应用组",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    FocusCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        containerColor = MaterialTheme.colorScheme.errorContainer,
        borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .padding(ComponentSpacing.cardPadding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)
        ) {
            Text(
                text = "错误",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun FocusStrategyCard(
    strategy: FocusStrategy,
    appLabelMap: Map<String, String>,
    onClick: () -> Unit
) {
    val appNames = remember(strategy.targetApps, appLabelMap) {
        strategy.targetApps
            .map { packageName -> appLabelMap[packageName] ?: packageName }
            .sorted()
    }
    val appPreview = remember(appNames) {
        when {
            appNames.isEmpty() -> "未选择应用"
            appNames.size <= 4 -> appNames.joinToString(" · ")
            else -> "${appNames.take(4).joinToString(" · ")} 等 ${appNames.size} 个"
        }
    }

    FocusCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(CornerRadius.large),
        containerColor = if (strategy.isActive) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        },
        borderColor = if (strategy.isActive) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.outlineVariant
        }
    ) {
        Column(modifier = Modifier.padding(ComponentSpacing.cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strategy.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.space4))
                    Text(
                        text = "受限应用：$appPreview",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (strategy.isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = "策略规则：${strategy.getRestrictionSummary()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (strategy.isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Text(
                    text = "查看详情",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (strategy.isActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            Spacer(modifier = Modifier.height(ComponentSpacing.smallSpacing))
            StatusPill(text = if (strategy.isActive) "当前激活" else "未激活")
        }
    }
}
