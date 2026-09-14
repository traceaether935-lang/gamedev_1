package com.example.a2049.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.a2049.ads.AdMobManager
import com.example.a2049.ui.model.ActiveTool
import com.example.a2049.ui.model.FeedbackEvent
import com.example.a2049.ui.model.FeedbackWords
import com.example.a2049.util.SoundManager
import com.game.a2048.GameStatus
import com.game.a2048.column.ColumnDropEngine
import com.game.a2048.column.ColumnDropState
import com.game.a2048.data.GameRepository
import com.game.a2048.data.SettingsRepository
import com.game.a2048.data.StatisticsRepository
import com.game.a2048.data.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ColumnDropUiState(
    val gameState: ColumnDropState = ColumnDropState(),
    val userSettings: UserSettings = UserSettings(),
    val isPauseDialogShown: Boolean = false,
    val isWinDialogShown: Boolean = false,
    val isGameOverDialogShown: Boolean = false,
    val isLoading: Boolean = true
)

class ColumnDropViewModel(
    private val context: Context,
    private val gameRepository: GameRepository = GameRepository(context),
    private val statisticsRepository: StatisticsRepository = StatisticsRepository(context),
    private val settingsRepository: SettingsRepository = SettingsRepository(context),
    private val adMobManager: AdMobManager? = null
) : ViewModel() {

    val boardId: Int = 508
    private val engine = ColumnDropEngine()
    private val soundManager = SoundManager()

    private val _uiState = MutableStateFlow(ColumnDropUiState(gameState = engine.state))
    val uiState: StateFlow<ColumnDropUiState> = _uiState.asStateFlow()

    private val _feedbackEvent = MutableStateFlow<FeedbackEvent?>(null)
    val feedbackEvent: StateFlow<FeedbackEvent?> = _feedbackEvent.asStateFlow()

    private val _selectedColumn = MutableStateFlow(2)
    val selectedColumn: StateFlow<Int> = _selectedColumn.asStateFlow()

    private var eventIdCounter = 0L

    init {
        viewModelScope.launch {
            settingsRepository.userSettingsFlow.collect { settings ->
                _uiState.update { it.copy(userSettings = settings) }
            }
        }

        viewModelScope.launch {
            gameRepository.getBestScoreFlow(boardId).collect { best ->
                engine.updateBestScore(best)
                updateState()
            }
        }

        viewModelScope.launch {
            statisticsRepository.recordGameStarted(boardId)
            updateState(isLoading = false)
        }
    }

    private fun updateState(isLoading: Boolean = false) {
        val st = engine.state
        val isWin = st.status == GameStatus.WON && !st.isContinued
        val isOver = st.status == GameStatus.GAME_OVER

        _uiState.update {
            it.copy(
                gameState = st,
                isWinDialogShown = isWin,
                isGameOverDialogShown = isOver,
                isLoading = isLoading
            )
        }
    }

    fun setAimingColumn(col: Int?) {
        engine.setAimingColumn(col)
        updateState()
    }

    fun setSelectedColumn(col: Int) {
        if (col in 0..4) {
            _selectedColumn.value = col
            engine.setAimingColumn(col)
            updateState()
        }
    }

    fun shootTile(col: Int) {
        val prevScore = engine.state.score
        val success = engine.shootTile(col)
        if (success) {
            val currentSettings = _uiState.value.userSettings
            if (currentSettings.soundEnabled) {
                soundManager.playMoveSound()
            }

            val diff = engine.state.score - prevScore
            if (diff > 0) {
                triggerFeedback(diff)
                if (currentSettings.soundEnabled) {
                    soundManager.playMergeSound()
                }
            }

            viewModelScope.launch {
                gameRepository.updateBestScore(boardId, engine.state.bestScore)
                val st = engine.state
                val highestTile = st.grid.flatten().maxOrNull() ?: 2
                if (st.status == GameStatus.GAME_OVER) {
                    statisticsRepository.recordGameEnd(boardId, st.score, highestTile, st.moveCount)
                } else if (st.status == GameStatus.WON) {
                    statisticsRepository.recordGameWon(boardId)
                    statisticsRepository.recordGameEnd(boardId, st.score, highestTile, st.moveCount)
                }
            }
        }
        updateState()
    }

    private fun triggerFeedback(scoreGained: Int) {
        val tier = when {
            scoreGained >= 256 -> 4
            scoreGained >= 128 -> 3
            scoreGained >= 64 -> 2
            else -> 1
        }
        val word = FeedbackWords.getRandomWordForTier(tier)
        _feedbackEvent.value = FeedbackEvent(
            id = ++eventIdCounter,
            text = "+$scoreGained $word",
            tier = tier
        )
    }

    fun toggleTool(tool: ActiveTool) {
        engine.toggleTool(tool)
        updateState()
    }

    fun cancelTool() {
        engine.cancelTool()
        updateState()
    }

    fun useHammer(col: Int, row: Int) {
        val success = engine.useHammer(col, row)
        if (success) {
            val currentSettings = _uiState.value.userSettings
            if (currentSettings.soundEnabled) {
                soundManager.playMoveSound()
            }
            viewModelScope.launch {
                gameRepository.updateBestScore(boardId, engine.state.bestScore)
            }
        }
        updateState()
    }

    fun selectTileForSwap(col: Int, row: Int) {
        val success = engine.selectTileForSwap(col, row)
        if (success) {
            val currentSettings = _uiState.value.userSettings
            if (currentSettings.soundEnabled) {
                soundManager.playMoveSound()
            }
            viewModelScope.launch {
                gameRepository.updateBestScore(boardId, engine.state.bestScore)
            }
        }
        updateState()
    }

    fun undo() {
        val success = engine.undo()
        if (success) {
            val currentSettings = _uiState.value.userSettings
            if (currentSettings.soundEnabled) {
                soundManager.playMoveSound()
            }
        }
        updateState()
    }

    fun restartGame() {
        engine.restart()
        _selectedColumn.value = 2
        viewModelScope.launch {
            statisticsRepository.recordGameStarted(boardId)
        }
        updateState()
    }

    fun continueGame() {
        engine.continueGame()
        updateState()
    }

    fun pauseGame() {
        _uiState.update { it.copy(isPauseDialogShown = true) }
    }

    fun resumeGame() {
        _uiState.update { it.copy(isPauseDialogShown = false) }
    }

    fun watchAdForUndo(onReward: () -> Unit = {}) {
        engine.addUndoUses(1)
        onReward()
        updateState()
    }

    fun watchAdForHammer(onReward: () -> Unit = {}) {
        engine.addHammerUses(1)
        onReward()
        updateState()
    }

    fun watchAdForSwitch(onReward: () -> Unit = {}) {
        engine.addSwitchUses(1)
        onReward()
        updateState()
    }

    override fun onCleared() {
        soundManager.release()
    }
}

class ColumnDropViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ColumnDropViewModel::class.java)) {
            return ColumnDropViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
