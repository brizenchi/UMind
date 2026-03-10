package com.example.umind.presentation.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umind.domain.model.FocusStrategy
import com.example.umind.domain.model.Result
import com.example.umind.domain.usecase.GetInstalledAppsUseCase
import com.example.umind.domain.usecase.GetFocusStrategiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Focus List Screen
 */
@HiltViewModel
class FocusListViewModel @Inject constructor(
    private val getFocusStrategiesUseCase: GetFocusStrategiesUseCase,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FocusListUiState>(FocusListUiState.Loading)
    val uiState: StateFlow<FocusListUiState> = _uiState.asStateFlow()
    private val appLabelMap = MutableStateFlow<Map<String, String>>(emptyMap())

    init {
        loadAppLabels()
        loadStrategies()
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

    private fun loadStrategies() {
        viewModelScope.launch {
            getFocusStrategiesUseCase()
                .combine(appLabelMap) { strategies, labels ->
                    strategies to labels
                }
                .catch { e ->
                    _uiState.value = FocusListUiState.Error(e.message ?: "加载失败")
                }
                .collect { (strategies, labels) ->
                    _uiState.value = if (strategies.isEmpty()) {
                        FocusListUiState.Empty
                    } else {
                        FocusListUiState.Success(
                            strategies = strategies,
                            appLabelMap = labels
                        )
                    }
                }
        }
    }
}

/**
 * UI State for Focus List Screen
 */
sealed class FocusListUiState {
    object Loading : FocusListUiState()
    object Empty : FocusListUiState()
    data class Success(
        val strategies: List<FocusStrategy>,
        val appLabelMap: Map<String, String>
    ) : FocusListUiState()
    data class Error(val message: String) : FocusListUiState()
}
