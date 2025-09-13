package com.example.focus

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

private val Context.dataStore by preferencesDataStore(name = "focus_prefs")

@Serializable
data class FocusStrategy(
    val id: String,
    val name: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val blockedPackages: Set<String>,
    val isActive: Boolean = false
)

object FocusRepository {

    private val KEY_FOCUS_STRATEGIES: Preferences.Key<String> = stringPreferencesKey("focus_strategies")
    private val KEY_BLOCKED_PACKAGES: Preferences.Key<Set<String>> = stringSetPreferencesKey("blocked_packages")
    private val KEY_BLOCK_START: Preferences.Key<Long> = longPreferencesKey("block_start")
    private val KEY_BLOCK_END: Preferences.Key<Long> = longPreferencesKey("block_end")
    
    private val json = Json { ignoreUnknownKeys = true }

    fun getBlockedPackages(context: Context): Set<String> = runBlocking {
        context.dataStore.data.first()[KEY_BLOCKED_PACKAGES] ?: emptySet()
    }

    fun setBlockedPackages(context: Context, packages: Set<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { prefs ->
                prefs[KEY_BLOCKED_PACKAGES] = packages
            }
        }
    }

    fun setBlockWindow(context: Context, startMillis: Long, endMillis: Long) = runBlocking {
        context.dataStore.edit { prefs ->
            prefs[KEY_BLOCK_START] = startMillis
            prefs[KEY_BLOCK_END] = endMillis
        }
    }

    fun getBlockWindow(context: Context): Pair<Long, Long>? = runBlocking {
        val prefs = context.dataStore.data.first()
        val s = prefs[KEY_BLOCK_START]
        val e = prefs[KEY_BLOCK_END]
        if (s == null || e == null) null else (s to e)
    }

    fun isBlockedNow(context: Context, packageName: String): Boolean {
        val packages = getBlockedPackages(context)
        if (!packages.contains(packageName)) return false

        val window = getBlockWindow(context) ?: return false
        val now = Calendar.getInstance().timeInMillis
        val (start, end) = window
        return if (end >= start) {
            now in start..end
        } else {
            // window crosses midnight
            now >= start || now <= end
        }
    }
    
    // 新的方法：基于激活的专注策略检查应用是否应该被阻止
    fun isAppBlockedNow(context: Context, packageName: String): Boolean {
        val strategies = getFocusStrategies(context)
        android.util.Log.d("FocusRepository", "Total strategies: ${strategies.size}")
        
        val activeStrategy = strategies.find { it.isActive }
        if (activeStrategy == null) {
            android.util.Log.d("FocusRepository", "No active strategy found")
            return false
        }
        
        android.util.Log.d("FocusRepository", "Active strategy: ${activeStrategy.name}, blocked packages: ${activeStrategy.blockedPackages}")
        
        // 检查应用是否在阻止列表中
        if (!activeStrategy.blockedPackages.contains(packageName)) {
            android.util.Log.d("FocusRepository", "Package $packageName not in blocked list")
            return false
        }
        
        // 检查当前时间是否在阻止时间范围内
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentTimeInMinutes = currentHour * 60 + currentMinute
        
        val startTimeInMinutes = activeStrategy.startHour * 60 + activeStrategy.startMinute
        val endTimeInMinutes = activeStrategy.endHour * 60 + activeStrategy.endMinute
        
        android.util.Log.d("FocusRepository", "Current time: $currentHour:$currentMinute ($currentTimeInMinutes min)")
        android.util.Log.d("FocusRepository", "Block window: ${activeStrategy.startHour}:${activeStrategy.startMinute} - ${activeStrategy.endHour}:${activeStrategy.endMinute} ($startTimeInMinutes - $endTimeInMinutes min)")
        
        val isInTimeRange = if (endTimeInMinutes >= startTimeInMinutes) {
            // 同一天内的时间范围
            currentTimeInMinutes in startTimeInMinutes..endTimeInMinutes
        } else {
            // 跨越午夜的时间范围
            currentTimeInMinutes >= startTimeInMinutes || currentTimeInMinutes <= endTimeInMinutes
        }
        
        android.util.Log.d("FocusRepository", "Is in time range: $isInTimeRange")
        return isInTimeRange
    }
    
    // 专注策略管理方法
    fun getFocusStrategies(context: Context): List<FocusStrategy> = runBlocking {
        val strategiesJson = context.dataStore.data.first()[KEY_FOCUS_STRATEGIES] ?: "[]"
        try {
            json.decodeFromString<List<FocusStrategy>>(strategiesJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveFocusStrategy(context: Context, strategy: FocusStrategy) {
        CoroutineScope(Dispatchers.IO).launch {
            val strategies = getFocusStrategies(context).toMutableList()
            val existingIndex = strategies.indexOfFirst { it.id == strategy.id }
            if (existingIndex >= 0) {
                strategies[existingIndex] = strategy
            } else {
                strategies.add(strategy)
            }
            
            context.dataStore.edit { prefs ->
                prefs[KEY_FOCUS_STRATEGIES] = json.encodeToString(strategies)
            }
        }
    }
    
    fun deleteFocusStrategy(context: Context, strategyId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val strategies = getFocusStrategies(context).filter { it.id != strategyId }
            context.dataStore.edit { prefs ->
                prefs[KEY_FOCUS_STRATEGIES] = json.encodeToString(strategies)
            }
        }
    }
    
    fun setStrategyActive(context: Context, strategyId: String, isActive: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val strategies = getFocusStrategies(context).map { strategy ->
                if (strategy.id == strategyId) {
                    strategy.copy(isActive = isActive)
                } else if (isActive) {
                    // 如果激活新策略，停用其他策略
                    strategy.copy(isActive = false)
                } else {
                    strategy
                }
            }
            
            context.dataStore.edit { prefs ->
                prefs[KEY_FOCUS_STRATEGIES] = json.encodeToString(strategies)
            }
            
            // 更新当前活跃策略的阻止设置
            val activeStrategy = strategies.find { it.isActive }
            if (activeStrategy != null) {
                setBlockedPackages(context, activeStrategy.blockedPackages)
                val startTime = createTimeInMillis(activeStrategy.startHour, activeStrategy.startMinute)
                val endTime = createTimeInMillis(activeStrategy.endHour, activeStrategy.endMinute)
                setBlockWindow(context, startTime, endTime)
            }
        }
    }
    
    private fun createTimeInMillis(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return cal.timeInMillis
    }
}


