package com.example.umind.util

import android.content.Context

/**
 * Tracks launches initiated from inside UMind so AccessibilityService can distinguish
 * internal open from external open.
 */
object InternalLaunchTracker {
    private const val PREF_NAME = "umind_internal_launch_tracker"
    private const val KEY_PACKAGE = "pending_package"
    private const val KEY_EXPIRE_AT = "pending_expire_at"
    private const val DEFAULT_TTL_MS = 15_000L

    fun markLaunchFromUMind(
        context: Context,
        packageName: String,
        ttlMillis: Long = DEFAULT_TTL_MS
    ) {
        val expireAt = System.currentTimeMillis() + ttlMillis
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PACKAGE, packageName)
            .putLong(KEY_EXPIRE_AT, expireAt)
            .apply()
    }

    fun consumeIfMatched(context: Context, packageName: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val pendingPackage = prefs.getString(KEY_PACKAGE, null) ?: return false
        val expireAt = prefs.getLong(KEY_EXPIRE_AT, 0L)
        val now = System.currentTimeMillis()

        if (expireAt <= now) {
            clear(context)
            return false
        }

        if (pendingPackage != packageName) {
            return false
        }

        clear(context)
        return true
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PACKAGE)
            .remove(KEY_EXPIRE_AT)
            .apply()
    }
}

