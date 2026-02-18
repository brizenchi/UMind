package com.example.umind.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 阻止事件实体
 * 记录每次应用被阻止的事件
 */
@Entity(
    tableName = "block_events",
    indices = [
        Index(value = ["packageName"]),
        Index(value = ["timestamp"]),
        Index(value = ["blockSource"])
    ]
)
data class BlockEventEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val packageName: String,           // 应用包名
    val appName: String,               // 应用名称
    val timestamp: Long,               // 阻止时间戳
    val blockReason: String,           // 阻止原因（JSON 格式）
    val blockSource: String,           // 阻止来源（FOCUS_MODE, DAILY_MANAGEMENT）
    val strategyIds: String? = null    // 相关策略ID（JSON数组，仅日常管理）
)
