package com.example.a2049.ui.viewmodel

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

    private val _selectedBoardSize = MutableStateFlow(4)
    val selectedBoardSize: StateFlow<Int> = _selectedBoardSize.asStateFlow()

    val stats4x4: StateFlow<GameStatistics> = statisticsRepository.getStatisticsFlow(4)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GameStatistics()
        )

    val stats5x5: StateFlow<GameStatistics> = statisticsRepository.getStatisticsFlow(5)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GameStatistics()
        )

    fun selectBoardSize(boardSize: Int) {
        if (boardSize == 4 || boardSize == 5) {
            _selectedBoardSize.value = boardSize
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
