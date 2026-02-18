package com.example.umind.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Entity for tracking app usage duration
 */
@Entity(tableName = "usage_records")
data class UsageRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val date: LocalDate,
    val usageDurationMillis: Long, // Total usage duration in milliseconds
    val openCount: Int, // Number of times the app was opened
    val lastUpdated: Long = System.currentTimeMillis()
)
