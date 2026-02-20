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
import com.example.umind.ui.components.ModernDialog
import com.example.umind.ui.theme.ComponentSpacing
import com.example.umind.ui.theme.CornerRadius
import com.example.umind.ui.theme.Spacing

/**
 * 专注策略列表页面
 */
@Composable
fun FocusListScreen(
    navController: NavController,
    viewModel: FocusListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("focus_edit") }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加专注策略")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(ComponentSpacing.pagePadding),
            verticalArrangement = Arrangement.spacedBy(ComponentSpacing.componentSpacing)
        ) {
            Text(
                text = "专注策略",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
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
                                onToggleActive = { isActive ->
                                    viewModel.toggleStrategyActive(strategy.id, isActive)
                                },
                                onEdit = {
                                    navController.navigate("focus_edit/${strategy.id}")
                                },
                                onDelete = {
                                    viewModel.deleteStrategy(strategy.id)
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

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
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
                text = "还没有专注策略",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "点击右下角的 + 号创建第一个专注策略",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
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
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        onClick = onEdit,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
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
                        text = strategy.getRestrictionSummary(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${strategy.targetApps.size} 个应用被限制",
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
                Spacer(modifier = Modifier.height(ComponentSpacing.smallSpacing))
                Surface(
                    shape = RoundedCornerShape(CornerRadius.small),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "当前激活",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        ModernDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "删除策略",
            message = "确定要删除 \"${strategy.name}\" 吗？此操作无法撤销。",
            confirmText = "删除",
            dismissText = "取消",
            onConfirm = onDelete,
            isDangerous = true
        )
    }
}
