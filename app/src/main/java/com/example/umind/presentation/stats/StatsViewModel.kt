package com.example.umind.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umind.domain.model.DailyStats
import com.example.umind.domain.model.UsageTrend
import com.example.umind.domain.model.UsageTimelineEntry
import com.example.umind.domain.usecase.GetDailyStatsUseCase
import com.example.umind.domain.usecase.GetUsageTrendUseCase
import com.example.umind.domain.usecase.GetUsageTimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getDailyStatsUseCase: GetDailyStatsUseCase,
    private val getUsageTrendUseCase: GetUsageTrendUseCase,
    private val getUsageTimelineUseCase: GetUsageTimelineUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats(date: LocalDate = _selectedDate.value) {
        viewModelScope.launch {
            try {
                _uiState.value = StatsUiState.Loading

                // Load daily stats
                val dailyStats = getDailyStatsUseCase(date)

                // Load 7-day trend
                val endDate = date
                val startDate = date.minusDays(6)
                val trend = getUsageTrendUseCase(startDate, endDate)

                // Load timeline
                val timeline = getUsageTimelineUseCase(date)

                _uiState.value = StatsUiState.Success(
                    dailyStats = dailyStats,
                    weeklyTrend = trend,
                    timeline = timeline
                )
            } catch (e: Exception) {
                _uiState.value = StatsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        loadStats(date)
    }

    fun refresh() {
        loadStats()
    }
}

sealed class StatsUiState {
    object Loading : StatsUiState()
    data class Success(
        val dailyStats: DailyStats,
        val weeklyTrend: List<UsageTrend>,
        val timeline: List<UsageTimelineEntry>
    ) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}
