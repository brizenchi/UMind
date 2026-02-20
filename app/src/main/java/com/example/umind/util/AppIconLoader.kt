package com.example.umind.util

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.collection.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App Icon Loader with LRU cache
 * Loads app icons on demand and caches them in memory
 */
@Singleton
class AppIconLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // LRU cache for app icons (max 50 icons, ~5MB)
    private val iconCache = LruCache<String, Bitmap>(50)

    /**
     * Load app icon by package name
     * Returns cached icon if available, otherwise loads from PackageManager
     */
    suspend fun loadIcon(packageName: String): Bitmap? = withContext(Dispatchers.IO) {
        // Check cache first
        iconCache.get(packageName)?.let { return@withContext it }

        // Load from PackageManager
        try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            val bitmap = drawable.toBitmap(width = 96, height = 96)

            // Cache the icon
            iconCache.put(packageName, bitmap)

            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Preload icons for a list of package names
     */
    suspend fun preloadIcons(packageNames: List<String>) = withContext(Dispatchers.IO) {
        packageNames.forEach { packageName ->
            if (iconCache.get(packageName) == null) {
                loadIcon(packageName)
            }
        }
    }

    /**
     * Clear icon cache
     */
    fun clearCache() {
        iconCache.evictAll()
    }

    /**
     * Get cache size
     */
    fun getCacheSize(): Int = iconCache.size()
}
