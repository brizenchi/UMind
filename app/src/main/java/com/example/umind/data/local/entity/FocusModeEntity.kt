package com.example.umind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.umind.data.local.converter.Converters
import com.example.umind.domain.model.FocusModeType
import com.example.umind.domain.model.TimeRestriction

/**
 * Room entity for Focus Mode
 * Stores the whitelist-based focus mode configuration
 */
@Entity(tableName = "focus_mode")
@TypeConverters(Converters::class)
data class FocusModeEntity(
    @PrimaryKey
    val id: String = "focus_mode_singleton",
    val isEnabled: Boolean = false,
    val whitelistedApps: Set<String> = emptySet(),
    val modeType: FocusModeType = FocusModeType.MANUAL,
    val countdownEndTime: Long? = null,
    val scheduledTimeRanges: List<TimeRestriction> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)
