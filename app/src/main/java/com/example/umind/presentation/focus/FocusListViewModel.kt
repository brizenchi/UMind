package com.example.umind.presentation.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umind.domain.model.FocusStrategy
import com.example.umind.domain.model.Result
import com.example.umind.domain.usecase.DeleteFocusStrategyUseCase
import com.example.umind.domain.usecase.GetFocusStrategiesUseCase
import com.example.umind.domain.usecase.ToggleStrategyActiveUseCase
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
    private val toggleStrategyActiveUseCase: ToggleStrategyActiveUseCase,
    private val deleteFocusStrategyUseCase: DeleteFocusStrategyUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FocusListUiState>(FocusListUiState.Loading)
    val uiState: StateFlow<FocusListUiState> = _uiState.asStateFlow()

    init {
        loadStrategies()
    }

    private fun loadStrategies() {
        viewModelScope.launch {
            getFocusStrategiesUseCase()
                .catch { e ->
                    _uiState.value = FocusListUiState.Error(e.message ?: "加载失败")
                }
                .collect { strategies ->
                    _uiState.value = if (strategies.isEmpty()) {
                        FocusListUiState.Empty
                    } else {
                        FocusListUiState.Success(strategies)
                    }
                }
        }
    }

    fun toggleStrategyActive(id: String, isActive: Boolean) {
        viewModelScope.launch {
            when (val result = toggleStrategyActiveUseCase(id, isActive)) {
                is Result.Error -> {
                    // Show error message
                    _uiState.value = FocusListUiState.Error(result.message ?: "操作失败")
                    // Reload to restore previous state
                    loadStrategies()
                }
                is Result.Success -> {
                    // Success - the Flow will automatically update
                }
                Result.Loading -> {}
            }
        }
    }

    fun deleteStrategy(id: String) {
        viewModelScope.launch {
            when (val result = deleteFocusStrategyUseCase(id)) {
                is Result.Error -> {
                    _uiState.value = FocusListUiState.Error(result.message ?: "删除失败")
                }
                is Result.Success -> {
                    // Success - the Flow will automatically update
                }
                Result.Loading -> {}
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
    data class Success(val strategies: List<FocusStrategy>) : FocusListUiState()
    data class Error(val message: String) : FocusListUiState()
}
