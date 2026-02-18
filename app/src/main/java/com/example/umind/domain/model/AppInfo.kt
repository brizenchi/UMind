package com.example.umind.domain.model

import android.graphics.Bitmap

/**
 * Domain model for App Information
 */
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Bitmap? = null,
    val isSystemApp: Boolean = false
)
