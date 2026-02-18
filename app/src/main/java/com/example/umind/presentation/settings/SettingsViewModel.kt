package com.example.umind.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umind.data.local.entity.UsageRecordEntity
import com.example.umind.data.repository.UsageTrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val usageTrackingRepository: UsageTrackingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun loadTodayRecords() {
        viewModelScope.launch {
            try {
                val records = usageTrackingRepository.getTodayRecords()
                android.util.Log.d("SettingsViewModel", "Loaded ${records.size} records for today")
                _uiState.value = _uiState.value.copy(
                    todayRecords = records,
                    debugMessage = if (records.isEmpty()) "今日暂无使用记录" else ""
                )
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Error loading today records", e)
                _uiState.value = _uiState.value.copy(
                    debugMessage = "加载失败: ${e.message}"
                )
            }
        }
    }

    fun clearTodayRecords() {
        viewModelScope.launch {
            try {
                usageTrackingRepository.clearTodayRecords()
                android.util.Log.d("SettingsViewModel", "Cleared today records")
                _uiState.value = _uiState.value.copy(
                    todayRecords = emptyList(),
                    debugMessage = "今日记录已清除"
                )
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Error clearing today records", e)
                _uiState.value = _uiState.value.copy(
                    debugMessage = "清除失败: ${e.message}"
                )
            }
        }
    }
}

data class SettingsUiState(
    val todayRecords: List<UsageRecordEntity> = emptyList(),
    val debugMessage: String = ""
)
