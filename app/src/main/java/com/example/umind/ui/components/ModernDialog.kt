package com.example.umind.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.umind.ui.theme.ComponentSpacing
import com.example.umind.ui.theme.CornerRadius

/**
 * UMind Design System - 现代对话框组件
 *
 * 使用示例:
 * ```
 * ModernDialog(
 *     onDismissRequest = { showDialog = false },
 *     title = "确认删除",
 *     message = "确定要删除这个专注策略吗？此操作无法撤销。",
 *     confirmText = "删除",
 *     dismissText = "取消",
 *     onConfirm = { /* 执行删除 */ }
 * )
 * ```
 */
@Composable
fun ModernDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "确认",
    dismissText: String = "取消",
    onConfirm: () -> Unit,
    isDangerous: Boolean = false
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComponentSpacing.pagePadding),
            shape = RoundedCornerShape(CornerRadius.extraLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(ComponentSpacing.pagePadding)
            ) {
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 内容
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 按钮组
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.smallSpacing)
                ) {
                    // 取消按钮
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(CornerRadius.medium)
                    ) {
                        Text(dismissText)
                    }

                    // 确认按钮
                    Button(
                        onClick = {
                            onConfirm()
                            onDismissRequest()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(CornerRadius.medium),
                        colors = if (isDangerous) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        }
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

/**
 * 简单的信息对话框（只有一个确认按钮）
 */
@Composable
fun InfoDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "知道了"
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComponentSpacing.pagePadding),
            shape = RoundedCornerShape(CornerRadius.extraLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(ComponentSpacing.pagePadding)
            ) {
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 内容
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 确认按钮
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CornerRadius.medium)
                ) {
                    Text(confirmText)
                }
            }
        }
    }
}

/**
 * 加载对话框
 */
@Composable
fun LoadingDialog(
    message: String = "加载中..."
) {
    Dialog(onDismissRequest = { }) {
        Card(
            modifier = Modifier
                .wrapContentSize()
                .padding(ComponentSpacing.pagePadding),
            shape = RoundedCornerShape(CornerRadius.large),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.pagePadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
