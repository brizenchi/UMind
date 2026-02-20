package com.example.umind.presentation.focus

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umind.domain.model.AppInfo
import com.example.umind.domain.model.Result
import com.example.umind.domain.usecase.GetInstalledAppsUseCase
import com.example.umind.util.AppIconLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 应用选择页面的 ViewModel
 */
@HiltViewModel
class AppSelectionViewModel @Inject constructor(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val appIconLoader: AppIconLoader
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppSelectionUiState())
    val uiState: StateFlow<AppSelectionUiState> = _uiState.asStateFlow()

    private var allApps: List<AppInfo> = emptyList()

    // Cache for loaded icons
    private val loadedIcons = mutableMapOf<String, Bitmap?>()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = getInstalledAppsUseCase()) {
                is Result.Success -> {
                    allApps = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        filteredApps = allApps,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "加载应用列表失败"
                    )
                }
                Result.Loading -> {
                    // 已经在上面设置了 loading 状态
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterApps(query)
    }

    private fun filterApps(query: String) {
        val filtered = if (query.isBlank()) {
            allApps
        } else {
            allApps.filter { app ->
                app.label.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
            }
        }
        _uiState.value = _uiState.value.copy(filteredApps = filtered)
    }

    fun toggleAppSelection(packageName: String) {
        val currentSelected = _uiState.value.selectedPackages.toMutableSet()
        if (currentSelected.contains(packageName)) {
            currentSelected.remove(packageName)
        } else {
            currentSelected.add(packageName)
        }
        _uiState.value = _uiState.value.copy(selectedPackages = currentSelected)
    }

    fun setInitialSelection(packages: Set<String>) {
        _uiState.value = _uiState.value.copy(selectedPackages = packages)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Load icon for a specific app on-demand (non-suspend version for UI)
     * Returns cached icon immediately, or triggers async load and returns null
     */
    fun getIconForApp(packageName: String): Bitmap? {
        // Return cached icon if available
        loadedIcons[packageName]?.let { return it }

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
        loadedIcons[packageName]?.let { return it }

        // Load icon
        val icon = appIconLoader.loadIcon(packageName)
        loadedIcons[packageName] = icon

        // Update the app in the filtered list with the loaded icon
        val updatedApps = _uiState.value.filteredApps.map { app ->
            if (app.packageName == packageName) {
                app.copy(icon = icon)
            } else {
                app
            }
        }
        _uiState.value = _uiState.value.copy(filteredApps = updatedApps)

        return icon
    }
}

/**
 * 应用选择页面的 UI 状态
 */
data class AppSelectionUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filteredApps: List<AppInfo> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val error: String? = null
)
