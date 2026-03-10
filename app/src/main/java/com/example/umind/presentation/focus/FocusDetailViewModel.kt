package com.example.umind.presentation.focus

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umind.domain.model.FocusStrategy
import com.example.umind.domain.model.Result
import com.example.umind.domain.usecase.DeleteFocusStrategyUseCase
import com.example.umind.domain.usecase.GetFocusStrategiesUseCase
import com.example.umind.domain.usecase.GetInstalledAppsUseCase
import com.example.umind.domain.usecase.ToggleStrategyActiveUseCase
import com.example.umind.util.AppIconLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FocusDetailViewModel @Inject constructor(
    private val getFocusStrategiesUseCase: GetFocusStrategiesUseCase,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val toggleStrategyActiveUseCase: ToggleStrategyActiveUseCase,
    private val deleteFocusStrategyUseCase: DeleteFocusStrategyUseCase,
    private val appIconLoader: AppIconLoader,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val strategyId: String = savedStateHandle.get<String>("strategyId").orEmpty()
    private val appLabelMap = MutableStateFlow<Map<String, String>>(emptyMap())

    private val _uiState = MutableStateFlow(FocusDetailUiState())
    val uiState: StateFlow<FocusDetailUiState> = _uiState.asStateFlow()

    init {
        if (strategyId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isNotFound = true,
                error = "应用组不存在"
            )
        } else {
            loadAppLabels()
            observeStrategy()
        }
    }

    private fun loadAppLabels() {
        viewModelScope.launch {
            when (val result = getInstalledAppsUseCase()) {
                is Result.Success -> {
                    appLabelMap.value = result.data.associate { app ->
                        app.packageName to app.label
                    }
                }
                is Result.Error, Result.Loading -> {}
            }
        }
    }

    private fun observeStrategy() {
        viewModelScope.launch {
            getFocusStrategiesUseCase()
                .combine(appLabelMap) { strategies, labels ->
                    strategies.find { it.id == strategyId } to labels
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "加载详情失败"
                    )
                }
                .collect { (strategy, labels) ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        strategy = strategy,
                        appLabelMap = labels,
                        isNotFound = strategy == null
                    )
                }
        }
    }

    fun toggleStrategyActive(isActive: Boolean) {
        val strategy = _uiState.value.strategy ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdatingActive = true,
                error = null
            )
            when (val result = toggleStrategyActiveUseCase(strategy.id, isActive)) {
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdatingActive = false,
                        error = result.message ?: "更新状态失败"
                    )
                }
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isUpdatingActive = false)
                }
                Result.Loading -> {}
            }
        }
    }

    fun deleteStrategy(onSuccess: () -> Unit) {
        val targetId = _uiState.value.strategy?.id ?: strategyId
        if (targetId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "应用组不存在")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                error = null
            )
            when (val result = deleteFocusStrategyUseCase(targetId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isDeleting = false)
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = result.message ?: "删除失败"
                    )
                }
                Result.Loading -> {}
            }
        }
    }

    fun getIconForApp(packageName: String): Bitmap? {
        val cachedIcons = _uiState.value.loadedIcons
        if (cachedIcons.containsKey(packageName)) {
            return cachedIcons[packageName]
        }

        viewModelScope.launch {
            loadIconForApp(packageName)
        }

        return null
    }

    private suspend fun loadIconForApp(packageName: String): Bitmap? {
        val cachedIcons = _uiState.value.loadedIcons
        if (cachedIcons.containsKey(packageName)) {
            return cachedIcons[packageName]
        }

        val icon = appIconLoader.loadIcon(packageName)
        _uiState.value = _uiState.value.copy(
            loadedIcons = _uiState.value.loadedIcons + (packageName to icon)
        )
        return icon
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class FocusDetailUiState(
    val isLoading: Boolean = true,
    val strategy: FocusStrategy? = null,
    val appLabelMap: Map<String, String> = emptyMap(),
    val loadedIcons: Map<String, Bitmap?> = emptyMap(),
    val isNotFound: Boolean = false,
    val isUpdatingActive: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null
)
