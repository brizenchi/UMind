package com.example.umind.presentation.focusmode

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umind.data.repository.FocusModeRepository
import com.example.umind.domain.model.AppInfo
import com.example.umind.domain.model.FocusMode
import com.example.umind.domain.model.FocusModeType
import com.example.umind.domain.model.TimeRestriction
import com.example.umind.domain.usecase.GetInstalledAppsUseCase
import com.example.umind.util.AppIconLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Focus Mode Screen
 */
@HiltViewModel
class FocusModeViewModel @Inject constructor(
    private val focusModeRepository: FocusModeRepository,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val appIconLoader: AppIconLoader
) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    // Cache for loaded icons - use StateFlow to avoid triggering list updates
    private val _loadedIcons = MutableStateFlow<Map<String, Bitmap?>>(emptyMap())
    val loadedIcons: StateFlow<Map<String, Bitmap?>> = _loadedIcons.asStateFlow()

    val focusMode: StateFlow<FocusMode> = focusModeRepository.getFocusMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FocusMode()
        )

    // Combine focus mode with installed apps to get whitelist app details
    val whitelistedApps: StateFlow<List<AppInfo>> = combine(
        focusMode,
        installedApps
    ) { mode, apps ->
        apps.filter { mode.whitelistedApps.contains(it.packageName) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val result = getInstalledAppsUseCase()
            when (result) {
                is com.example.umind.domain.model.Result.Success -> {
                    _installedApps.value = result.data
                }
                is com.example.umind.domain.model.Result.Error -> {
                    // Handle error
                }
                is com.example.umind.domain.model.Result.Loading -> {
                    // Handle loading
                }
            }
        }
    }

    fun toggleFocusMode(isEnabled: Boolean) {
        viewModelScope.launch {
            focusModeRepository.toggleFocusMode(isEnabled)
        }
    }

    fun startCountdown(durationMinutes: Int) {
        viewModelScope.launch {
            focusModeRepository.startCountdown(durationMinutes)
        }
    }

    fun setScheduledMode(timeRanges: List<TimeRestriction>) {
        viewModelScope.launch {
            focusModeRepository.setScheduledMode(timeRanges)
        }
    }

    fun updateScheduledTimeRanges(timeRanges: List<TimeRestriction>) {
        viewModelScope.launch {
            focusModeRepository.updateScheduledTimeRanges(timeRanges)
        }
    }

    fun stopFocusMode() {
        viewModelScope.launch {
            focusModeRepository.stopFocusMode()
        }
    }

    fun addToWhitelist(packageName: String) {
        viewModelScope.launch {
            focusModeRepository.addToWhitelist(packageName)
        }
    }

    fun removeFromWhitelist(packageName: String) {
        viewModelScope.launch {
            focusModeRepository.removeFromWhitelist(packageName)
        }
    }

    /**
     * Load icon for a specific app on-demand (non-suspend version for callbacks)
     * Returns cached icon immediately, or null if not cached (will load asynchronously)
     */
    fun getIconForApp(packageName: String): Bitmap? {
        // Return cached icon if available
        _loadedIcons.value[packageName]?.let { return it }

        // Start loading asynchronously
        viewModelScope.launch {
            loadIconForApp(packageName)
        }

        return null
    }

    /**
     * Load icon for a specific app on-demand (suspend version)
     * Returns the loaded icon (or null if loading fails)
     */
    private suspend fun loadIconForApp(packageName: String): Bitmap? {
        // Return cached icon if available
        _loadedIcons.value[packageName]?.let { return it }

        // Load icon
        val icon = appIconLoader.loadIcon(packageName)

        // Update only the icons map, not the entire list
        _loadedIcons.value = _loadedIcons.value + (packageName to icon)

        return icon
    }
}
