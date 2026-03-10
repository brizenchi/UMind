package com.example.umind.presentation.focus

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umind.domain.model.*
import com.example.umind.domain.usecase.GetFocusStrategiesUseCase
import com.example.umind.domain.usecase.SaveFocusStrategyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * ViewModel for Focus Edit Screen
 */
@HiltViewModel
class FocusEditViewModel @Inject constructor(
    private val getFocusStrategiesUseCase: GetFocusStrategiesUseCase,
    private val saveFocusStrategyUseCase: SaveFocusStrategyUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val strategyId: String? = savedStateHandle.get<String>("strategyId")

    private val _uiState = MutableStateFlow(FocusEditUiState())
    val uiState: StateFlow<FocusEditUiState> = _uiState.asStateFlow()

    init {
        strategyId?.let { loadStrategy(it) }
    }

    private fun loadStrategy(id: String) {
        viewModelScope.launch {
            when (val result = getFocusStrategiesUseCase.execute()) {
                is Result.Success -> {
                    val strategy = result.data.find { it.id == id }
                    strategy?.let {
                        android.util.Log.d("FocusEditViewModel", "Loading strategy: ${it.name}")
                        android.util.Log.d("FocusEditViewModel", "Usage limits: ${it.usageLimits}")
                        android.util.Log.d("FocusEditViewModel", "Open count limits: ${it.openCountLimits}")

                        _uiState.value = _uiState.value.copy(
                            strategyId = it.id,
                            name = it.name,
                            selectedPackages = it.targetApps,
                            timeRestrictions = it.timeRestrictions,
                            usageLimits = it.usageLimits?.normalizedToTotalAll(),
                            openCountLimits = it.openCountLimits?.normalizedToTotalAll(),
                            enforcementMode = it.enforcementMode,
                            isActive = it.isActive
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: "加载策略失败"
                    )
                }
                Result.Loading -> {}
            }
        }
    }

    // Basic info
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, hasUnsavedChanges = true)
    }

    fun updateSelectedPackages(packages: Set<String>) {
        _uiState.value = _uiState.value.copy(
            selectedPackages = packages,
            hasUnsavedChanges = true
        )
    }

    // Time restrictions
    fun addTimeRestriction(
        daysOfWeek: Set<DayOfWeek>,
        startTime: LocalTime,
        endTime: LocalTime
    ) {
        val restriction = TimeRestriction(
            id = UUID.randomUUID().toString(),
            daysOfWeek = daysOfWeek,
            startTime = startTime,
            endTime = endTime
        )
        _uiState.value = _uiState.value.copy(
            timeRestrictions = _uiState.value.timeRestrictions + restriction,
            hasUnsavedChanges = true
        )
    }

    fun updateTimeRestriction(
        restrictionId: String,
        daysOfWeek: Set<DayOfWeek>,
        startTime: LocalTime,
        endTime: LocalTime
    ) {
        val updated = _uiState.value.timeRestrictions.map {
            if (it.id == restrictionId) {
                it.copy(daysOfWeek = daysOfWeek, startTime = startTime, endTime = endTime)
            } else {
                it
            }
        }
        _uiState.value = _uiState.value.copy(
            timeRestrictions = updated,
            hasUnsavedChanges = true
        )
    }

    fun removeTimeRestriction(restrictionId: String) {
        _uiState.value = _uiState.value.copy(
            timeRestrictions = _uiState.value.timeRestrictions.filter { it.id != restrictionId },
            hasUnsavedChanges = true
        )
    }

    // Usage limits
    fun updateUsageLimits(limits: UsageLimits?) {
        _uiState.value = _uiState.value.copy(
            usageLimits = limits,
            hasUnsavedChanges = true
        )
    }

    fun setUsageLimitsTotalAll(hours: Int, minutes: Int) {
        val duration = (hours * 60 + minutes).minutes
        _uiState.value = _uiState.value.copy(
            usageLimits = UsageLimits(
                type = LimitType.TOTAL_ALL,
                totalLimit = duration
            ),
            hasUnsavedChanges = true
        )
    }

    // Open count limits
    fun updateOpenCountLimits(limits: OpenCountLimits?) {
        _uiState.value = _uiState.value.copy(
            openCountLimits = limits,
            hasUnsavedChanges = true
        )
    }

    fun setOpenCountLimitsTotalAll(count: Int) {
        _uiState.value = _uiState.value.copy(
            openCountLimits = OpenCountLimits(
                type = LimitType.TOTAL_ALL,
                totalCount = count
            ),
            hasUnsavedChanges = true
        )
    }

    // Enforcement mode
    fun updateEnforcementMode(mode: EnforcementMode) {
        _uiState.value = _uiState.value.copy(
            enforcementMode = mode,
            hasUnsavedChanges = true
        )
    }

    // Active status
    fun updateIsActive(isActive: Boolean) {
        _uiState.value = _uiState.value.copy(
            isActive = isActive,
            hasUnsavedChanges = true
        )
    }

    fun saveStrategy(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value

            val strategy = FocusStrategy(
                id = state.strategyId ?: UUID.randomUUID().toString(),
                name = state.name,
                targetApps = state.selectedPackages,
                timeRestrictions = state.timeRestrictions,
                usageLimits = state.usageLimits?.normalizedToTotalAll(),
                openCountLimits = state.openCountLimits?.normalizedToTotalAll(),
                enforcementMode = state.enforcementMode,
                isActive = state.isActive
            )

            // 添加日志
            android.util.Log.d("FocusEditViewModel", "Saving strategy: ${strategy.name}")
            android.util.Log.d("FocusEditViewModel", "Usage limits: ${strategy.usageLimits}")
            android.util.Log.d("FocusEditViewModel", "Open count limits: ${strategy.openCountLimits}")

            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = saveFocusStrategyUseCase(strategy)) {
                is Result.Success -> {
                    android.util.Log.d("FocusEditViewModel", "Strategy saved successfully")
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        hasUnsavedChanges = false
                    )
                    onSuccess()
                }
                is Result.Error -> {
                    android.util.Log.e("FocusEditViewModel", "Failed to save strategy: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = result.message ?: "保存失败"
                    )
                }
                Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI State for Focus Edit Screen
 */
data class FocusEditUiState(
    val strategyId: String? = null,
    val name: String = "",
    val selectedPackages: Set<String> = emptySet(),
    val timeRestrictions: List<TimeRestriction> = emptyList(),
    val usageLimits: UsageLimits? = null,
    val openCountLimits: OpenCountLimits? = null,
    val enforcementMode: EnforcementMode = EnforcementMode.DIRECT_BLOCK,
    val isActive: Boolean = true, // 默认激活
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val error: String? = null
)
