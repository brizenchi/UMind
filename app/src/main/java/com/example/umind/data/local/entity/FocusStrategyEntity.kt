package com.example.umind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.umind.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.time.DayOfWeek
import java.time.LocalTime
import kotlin.time.Duration

/**
 * Room entity for Focus Strategy
 */
@Entity(tableName = "focus_strategies")
data class FocusStrategyEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val targetApps: String, // JSON string of Set<String>
    val timeRestrictions: String, // JSON string of List<TimeRestrictionData>
    val usageLimits: String?, // JSON string of UsageLimitsData
    val openCountLimits: String?, // JSON string of OpenCountLimitsData
    val enforcementMode: String, // Enum name
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {
    /**
     * Convert entity to domain model
     */
    fun toDomainModel(): FocusStrategy {
        val apps = try {
            Json.decodeFromString<Set<String>>(targetApps)
        } catch (e: Exception) {
            emptySet()
        }

        val restrictions = try {
            Json.decodeFromString<List<TimeRestrictionData>>(timeRestrictions)
                .map { it.toDomainModel() }
        } catch (e: Exception) {
            emptyList()
        }

        val usage = try {
            usageLimits?.let { Json.decodeFromString<UsageLimitsData>(it).toDomainModel() }
        } catch (e: Exception) {
            null
        }

        val openCount = try {
            openCountLimits?.let { Json.decodeFromString<OpenCountLimitsData>(it).toDomainModel() }
        } catch (e: Exception) {
            null
        }

        val mode = try {
            EnforcementMode.valueOf(enforcementMode)
        } catch (e: Exception) {
            EnforcementMode.DIRECT_BLOCK
        }

        return FocusStrategy(
            id = id,
            name = name,
            targetApps = apps,
            timeRestrictions = restrictions,
            usageLimits = usage,
            openCountLimits = openCount,
            enforcementMode = mode,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        /**
         * Convert domain model to entity
         */
        fun fromDomainModel(strategy: FocusStrategy): FocusStrategyEntity {
            return FocusStrategyEntity(
                id = strategy.id,
                name = strategy.name,
                targetApps = Json.encodeToString(strategy.targetApps),
                timeRestrictions = Json.encodeToString(
                    strategy.timeRestrictions.map { TimeRestrictionData.fromDomainModel(it) }
                ),
                usageLimits = strategy.usageLimits?.let {
                    Json.encodeToString(UsageLimitsData.fromDomainModel(it))
                },
                openCountLimits = strategy.openCountLimits?.let {
                    Json.encodeToString(OpenCountLimitsData.fromDomainModel(it))
                },
                enforcementMode = strategy.enforcementMode.name,
                isActive = strategy.isActive,
                createdAt = strategy.createdAt,
                updatedAt = strategy.updatedAt
            )
        }
    }
}

// Serializable data classes for complex types
@kotlinx.serialization.Serializable
data class TimeRestrictionData(
    val id: String,
    val daysOfWeek: List<String>, // DayOfWeek names
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
) {
    fun toDomainModel(): TimeRestriction {
        return TimeRestriction(
            id = id,
            daysOfWeek = daysOfWeek.mapNotNull {
                try { DayOfWeek.valueOf(it) } catch (e: Exception) { null }
            }.toSet(),
            startTime = LocalTime.of(startHour, startMinute),
            endTime = LocalTime.of(endHour, endMinute)
        )
    }

    companion object {
        fun fromDomainModel(restriction: TimeRestriction): TimeRestrictionData {
            return TimeRestrictionData(
                id = restriction.id,
                daysOfWeek = restriction.daysOfWeek.map { it.name },
                startHour = restriction.startTime.hour,
                startMinute = restriction.startTime.minute,
                endHour = restriction.endTime.hour,
                endMinute = restriction.endTime.minute
            )
        }
    }
}

@kotlinx.serialization.Serializable
data class UsageLimitsData(
    val type: String, // LimitType name
    val totalLimitMinutes: Long?,
    val perAppLimitMinutes: Long?
) {
    fun toDomainModel(): UsageLimits {
        return UsageLimits(
            type = LimitType.valueOf(type),
            totalLimit = totalLimitMinutes?.let { Duration.parse("${it}m") },
            perAppLimit = perAppLimitMinutes?.let { Duration.parse("${it}m") }
        )
    }

    companion object {
        fun fromDomainModel(limits: UsageLimits): UsageLimitsData {
            return UsageLimitsData(
                type = limits.type.name,
                totalLimitMinutes = limits.totalLimit?.inWholeMinutes,
                perAppLimitMinutes = limits.perAppLimit?.inWholeMinutes
            )
        }
    }
}

@kotlinx.serialization.Serializable
data class OpenCountLimitsData(
    val type: String, // LimitType name
    val totalCount: Int?,
    val perAppCount: Int?
) {
    fun toDomainModel(): OpenCountLimits {
        return OpenCountLimits(
            type = LimitType.valueOf(type),
            totalCount = totalCount,
            perAppCount = perAppCount
        )
    }

    companion object {
        fun fromDomainModel(limits: OpenCountLimits): OpenCountLimitsData {
            return OpenCountLimitsData(
                type = limits.type.name,
                totalCount = limits.totalCount,
                perAppCount = limits.perAppCount
            )
        }
    }
}

