package com.example.umind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.umind.domain.model.TemporaryUsage

/**
 * Entity for temporary usage records
 */
@Entity(tableName = "temporary_usage")
data class TemporaryUsageEntity(
    @PrimaryKey
    val id: String,
    val packageName: String,
    val appName: String,
    val reason: String,
    val requestedDurationMinutes: Int,
    val actualDurationMillis: Long,
    val startTime: Long,
    val endTime: Long,
    val isActive: Boolean,
    val createdAt: Long
) {
    fun toDomainModel(): TemporaryUsage {
        return TemporaryUsage(
            id = id,
            packageName = packageName,
            appName = appName,
            reason = reason,
            requestedDurationMinutes = requestedDurationMinutes,
            actualDurationMillis = actualDurationMillis,
            startTime = startTime,
            endTime = endTime,
            isActive = isActive,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomainModel(temporaryUsage: TemporaryUsage): TemporaryUsageEntity {
            return TemporaryUsageEntity(
                id = temporaryUsage.id,
                packageName = temporaryUsage.packageName,
                appName = temporaryUsage.appName,
                reason = temporaryUsage.reason,
                requestedDurationMinutes = temporaryUsage.requestedDurationMinutes,
                actualDurationMillis = temporaryUsage.actualDurationMillis,
                startTime = temporaryUsage.startTime,
                endTime = temporaryUsage.endTime,
                isActive = temporaryUsage.isActive,
                createdAt = temporaryUsage.createdAt
            )
        }
    }
}
