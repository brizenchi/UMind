package com.example.umind.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 使用会话实体
 * 记录每次应用使用的开始和结束时间
 */
@Entity(
    tableName = "usage_sessions",
    foreignKeys = [
        ForeignKey(
            entity = UsageRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recordId"]),
        Index(value = ["packageName"]),
        Index(value = ["startTime"])
    ]
)
data class UsageSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val recordId: String,              // 所属记录ID
    val packageName: String,           // 应用包名
    val startTime: Long,               // 会话开始时间戳
    val endTime: Long? = null,         // 会话结束时间戳（null 表示正在进行中）
    val durationMillis: Long = 0       // 会话时长（毫秒）
)
