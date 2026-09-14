package com.aethertrace.numberdrop2048hexa.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.game.a2048.data.StatisticsRepository
import com.game.a2048.data.model.GameStatistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(
    statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _selectedMainTab = MutableStateFlow(0) // 0: Classic, 1: Hexagon Merge, 2: Column Drop
    val selectedMainTab: StateFlow<Int> = _selectedMainTab.asStateFlow()

    private val _selectedClassicGrid = MutableStateFlow(4) // 4, 5, 6, 7, 8, 9
    val selectedClassicGrid: StateFlow<Int> = _selectedClassicGrid.asStateFlow()

    val stats4x4: StateFlow<GameStatistics> = statisticsRepository.getStatisticsFlow(4)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = GameStatistics())

    val stats5x5: StateFlow<GameStatistics> = statisticsRepository.getStatisticsFlow(5)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = GameStatistics())

    val stats6x6: StateFlow<GameStatistics> = statisticsRepository.getStatisticsFlow(6)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = GameStatistics())

    val stats7x7: StateFlow<GameStatistics> = statisticsRepository.getStatisticsFlow(7)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = GameStatistics())

    val stats8x8: StateFlow<GameStatistics> = statisticsRepository.getStatisticsFlow(8)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = GameStatistics())

    val stats9x9: StateFlow<GameStatistics> = statisticsRepository.getStatisticsFlow(9)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = GameStatistics())

    val statsHexagon: StateFlow<GameStatistics> = statisticsRepository.getStatisticsFlow(19)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = GameStatistics())

    val statsColumnDrop: StateFlow<GameStatistics> = statisticsRepository.getStatisticsFlow(508)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = GameStatistics())

    fun selectMainTab(tabIndex: Int) {
        if (tabIndex in 0..2) {
            _selectedMainTab.value = tabIndex
        }
    }

    fun selectClassicGrid(gridSize: Int) {
        if (gridSize in 4..9) {
            _selectedClassicGrid.value = gridSize
        }
    }

    fun getStatsForGrid(size: Int): StateFlow<GameStatistics> {
        return when (size) {
            4 -> stats4x4
            5 -> stats5x5
            6 -> stats6x6
            7 -> stats7x7
            8 -> stats8x8
            9 -> stats9x9
            else -> stats4x4
        }
    }
}

class StatisticsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatisticsViewModel(
            statisticsRepository = StatisticsRepository(context)
        ) as T
    }
}
