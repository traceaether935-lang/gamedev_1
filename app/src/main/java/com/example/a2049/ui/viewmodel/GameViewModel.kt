package com.example.a2049.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.a2049.ui.model.FeedbackEvent
import com.example.a2049.ui.model.FeedbackWords
import com.game.a2048.Direction
import com.game.a2048.GameEngine
import com.game.a2048.GameState
import com.game.a2048.GameStatus
import com.game.a2048.Mission
import com.game.a2048.MissionCatalog
import com.game.a2048.PowerUpType
import com.game.a2048.data.GameRepository
import com.game.a2048.data.SettingsRepository
import com.game.a2048.data.StatisticsRepository
import com.game.a2048.data.model.SavedGameState
import com.game.a2048.data.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val gameState: GameState = GameState(rows = 4, cols = 4, grid = emptyList()),
    val userSettings: UserSettings = UserSettings(),
    val isWinDialogShown: Boolean = false,
    val isPauseDialogShown: Boolean = false,
    val isMissionCompleteDialogShown: Boolean = false,
    val isMissionFailedDialogShown: Boolean = false,
    val isLoading: Boolean = true
)

class GameViewModel(
    val rows: Int = 4,
    val cols: Int = 4,
    val targetGoal: Int = 32,
    val initialMissionId: Int? = null,
    private val gameRepository: GameRepository,
    private val statisticsRepository: StatisticsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Helper to keep old scores for N x N boards, while supporting R x C sizes
    private val boardId = if (rows == cols) rows else (rows * 100 + cols)

    private val initialMission: Mission? = MissionCatalog.getMissionById(initialMissionId)

    private val gameEngine = GameEngine(
        rows = rows,
        cols = cols,
        targetGoal = targetGoal,
        activeMission = initialMission
    )

    private val _uiState = MutableStateFlow(
        GameUiState(
            gameState = gameEngine.state,
            isMissionCompleteDialogShown = gameEngine.state.isMissionCompleted,
            isMissionFailedDialogShown = gameEngine.state.isMissionFailed
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _feedbackEvent = MutableStateFlow<FeedbackEvent?>(null)
    val feedbackEvent: StateFlow<FeedbackEvent?> = _feedbackEvent.asStateFlow()

    private var eventIdCounter: Long = 0L
    private var swapFirstTile: Pair<Int, Int>? = null

    init {
        viewModelScope.launch {
            launch {
                settingsRepository.userSettingsFlow.collect { settings ->
                    _uiState.update { it.copy(userSettings = settings) }
                }
            }

            launch {
                gameRepository.getBestScoreFlow(boardId).collect { bestScore ->
                    gameEngine.updateBestScore(bestScore)
                    updateUiState()
                }
            }

            val savedState = gameRepository.getSavedGameStateFlow(boardId).firstOrNull()

            if (savedState != null && savedState.gridValues.size == rows * cols) {
                val grid = savedState.gridValues.chunked(cols)
                val status = try {
                    GameStatus.valueOf(savedState.status)
                } catch (_: Exception) {
                    GameStatus.PLAYING
                }
                gameEngine.setGrid(
                    grid = grid,
                    score = savedState.currentScore,
                    moveCount = savedState.moveCount,
                    status = status,
                    isContinued = savedState.isContinued,
                    targetGoal = savedState.targetGoal,
                    activeMission = initialMission ?: gameEngine.state.activeMission
                )
                val isWinDialog = (status == GameStatus.WON && !savedState.isContinued)
                val isPauseDialog = (status == GameStatus.PAUSED)
                _uiState.update {
                    it.copy(
                        isWinDialogShown = isWinDialog,
                        isPauseDialogShown = isPauseDialog,
                        isMissionCompleteDialogShown = gameEngine.state.isMissionCompleted,
                        isMissionFailedDialogShown = gameEngine.state.isMissionFailed
                    )
                }
            } else {
                statisticsRepository.recordGameStarted(boardId)
                persistCurrentGameState()
            }

            updateUiState(isLoading = false)
        }
    }

    fun selectMission(mission: Mission?) {
        swapFirstTile = null
        gameEngine.selectMission(mission)
        _uiState.update {
            it.copy(
                gameState = gameEngine.state,
                isWinDialogShown = false,
                isPauseDialogShown = false,
                isMissionCompleteDialogShown = gameEngine.state.isMissionCompleted,
                isMissionFailedDialogShown = gameEngine.state.isMissionFailed
            )
        }
        persistCurrentGameState()
    }

    fun retryMission() {
        swapFirstTile = null
        viewModelScope.launch {
            gameRepository.clearSavedGameState(boardId)
            statisticsRepository.recordGameStarted(boardId)
            gameEngine.restart(rows, cols, gameEngine.state.targetGoal, gameEngine.state.activeMission)
            _uiState.update {
                it.copy(
                    gameState = gameEngine.state,
                    isWinDialogShown = false,
                    isPauseDialogShown = false,
                    isMissionCompleteDialogShown = false,
                    isMissionFailedDialogShown = false
                )
            }
            persistCurrentGameState()
        }
    }

    fun nextMission() {
        val currentMissionId = gameEngine.state.activeMission?.id ?: return
        val nextMission = MissionCatalog.getNextMission(currentMissionId)
        if (nextMission != null) {
            selectMission(nextMission)
        }
    }

    fun continueFreeplay() {
        _uiState.update {
            it.copy(isMissionCompleteDialogShown = false)
        }
    }

    fun dismissMissionDialogs() {
        _uiState.update {
            it.copy(
                isMissionCompleteDialogShown = false,
                isMissionFailedDialogShown = false
            )
        }
    }

    fun setTargetGoal(newGoal: Int) {
        gameEngine.setTargetGoal(newGoal)
        val newState = gameEngine.state
        if (newState.status == GameStatus.WON && !_uiState.value.gameState.isContinued) {
            viewModelScope.launch { statisticsRepository.recordGameWon(boardId) }
            _uiState.update { it.copy(isWinDialogShown = true) }
        }
        persistCurrentGameState()
        updateUiState()
    }

    fun togglePowerUp(powerUpType: PowerUpType) {
        swapFirstTile = null // reset
        gameEngine.togglePowerUp(powerUpType)
        updateUiState()
    }

    fun onTileClick(row: Int, col: Int) {
        val activePowerUp = gameEngine.state.activePowerUp
        if (activePowerUp == PowerUpType.HAMMER) {
            val success = gameEngine.useHammer(row, col)
            if (success) {
                persistCurrentGameState()
                updateUiState()
            } else {
                togglePowerUp(PowerUpType.HAMMER)
            }
        } else if (activePowerUp == PowerUpType.SWAP) {
            if (swapFirstTile == null) {
                swapFirstTile = Pair(row, col)
            } else {
                val (r1, c1) = swapFirstTile!!
                val success = gameEngine.useSwap(r1, c1, row, col)
                swapFirstTile = null
                if (success) {
                    persistCurrentGameState()
                    updateUiState()
                } else {
                    togglePowerUp(PowerUpType.SWAP)
                }
            }
        }
    }

    fun onSwipe(
        direction: Direction,
        onHaptic: (() -> Unit)? = null,
        onSound: ((isMerge: Boolean) -> Unit)? = null
    ) {
        val currentState = _uiState.value
        if (currentState.isPauseDialogShown || currentState.isWinDialogShown ||
            currentState.isMissionCompleteDialogShown || currentState.isMissionFailedDialogShown ||
            gameEngine.state.status != GameStatus.PLAYING || gameEngine.state.activePowerUp != null
        ) {
            return
        }

        val oldTargetGoal = gameEngine.state.targetGoal
        val result = gameEngine.move(direction)
        if (result.hasChanged) {
            if (currentState.userSettings.hapticsEnabled) {
                onHaptic?.invoke()
            }
            if (currentState.userSettings.soundEnabled) {
                onSound?.invoke(result.scoreGained > 0)
            }

            val newTargetGoal = result.newState.targetGoal
            if (newTargetGoal > oldTargetGoal) {
                _feedbackEvent.value = FeedbackEvent(
                    id = System.currentTimeMillis() * 1000 + (++eventIdCounter),
                    text = "TARGET $oldTargetGoal REACHED! 🎯",
                    tier = 4
                )
            } else {
                // Evaluate dynamic rewarding feedback
                val tier = when {
                    result.mergeCount >= 2 -> 4
                    result.maxMergedValue >= 512 -> 3
                    result.maxMergedValue >= 64 -> 2
                    result.maxMergedValue >= 16 -> 1
                    else -> null
                }

                if (tier != null) {
                    val text = FeedbackWords.getRandomWordForTier(tier)
                    _feedbackEvent.value = FeedbackEvent(
                        id = System.currentTimeMillis() * 1000 + (++eventIdCounter),
                        text = text,
                        tier = tier
                    )
                }
            }

            val newState = result.newState

            // Check mission completion and failure dialog triggers
            val showMissionComplete = newState.isMissionCompleted && !currentState.isMissionCompleteDialogShown
            val showMissionFailed = newState.isMissionFailed && !currentState.isMissionFailedDialogShown

            if (newState.status == GameStatus.WON && !currentState.gameState.isContinued) {
                viewModelScope.launch { statisticsRepository.recordGameWon(boardId) }
                _uiState.update {
                    it.copy(
                        isWinDialogShown = true,
                        isMissionCompleteDialogShown = showMissionComplete,
                        isMissionFailedDialogShown = showMissionFailed
                    )
                }
            } else if (newState.status == GameStatus.GAME_OVER) {
                val highestTile = newState.grid.flatten().maxOrNull() ?: 0
                viewModelScope.launch {
                    statisticsRepository.recordGameEnd(
                        boardSize = boardId,
                        finalScore = newState.currentScore,
                        highestTile = highestTile,
                        moveCount = newState.moveCount
                    )
                    gameRepository.clearSavedGameState(boardId)
                }
                _uiState.update {
                    it.copy(
                        isMissionCompleteDialogShown = showMissionComplete,
                        isMissionFailedDialogShown = showMissionFailed
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isMissionCompleteDialogShown = showMissionComplete || it.isMissionCompleteDialogShown,
                        isMissionFailedDialogShown = showMissionFailed || it.isMissionFailedDialogShown
                    )
                }
            }

            persistCurrentGameState()
            updateUiState()
        }
    }

    fun undo() {
        swapFirstTile = null
        if (gameEngine.undo()) {
            persistCurrentGameState()
            updateUiState()
        }
    }

    fun restart() {
        swapFirstTile = null
        viewModelScope.launch {
            gameRepository.clearSavedGameState(boardId)
            statisticsRepository.recordGameStarted(boardId)
            gameEngine.restart(rows, cols, gameEngine.state.targetGoal)
            _uiState.update {
                it.copy(
                    gameState = gameEngine.state,
                    isWinDialogShown = false,
                    isPauseDialogShown = false,
                    isMissionCompleteDialogShown = gameEngine.state.isMissionCompleted,
                    isMissionFailedDialogShown = gameEngine.state.isMissionFailed
                )
            }
            persistCurrentGameState()
        }
    }

    fun pause() {
        gameEngine.pause()
        _uiState.update {
            it.copy(
                gameState = gameEngine.state,
                isPauseDialogShown = true
            )
        }
        persistCurrentGameState()
    }

    fun resume() {
        gameEngine.resume()
        _uiState.update {
            it.copy(
                gameState = gameEngine.state,
                isPauseDialogShown = false
            )
        }
        persistCurrentGameState()
    }

    fun continueGame() {
        gameEngine.continueGame()
        _uiState.update {
            it.copy(
                gameState = gameEngine.state,
                isWinDialogShown = false
            )
        }
        persistCurrentGameState()
    }

    fun leaveGame() {
        persistCurrentGameState()
    }

    fun dismissWinDialog() {
        _uiState.update { it.copy(isWinDialogShown = false) }
    }

    fun dismissPauseDialog() {
        resume()
    }

    private fun updateUiState(isLoading: Boolean = false) {
        _uiState.update {
            it.copy(
                gameState = gameEngine.state,
                isLoading = isLoading
            )
        }
    }

    private fun persistCurrentGameState() {
        val currentState = gameEngine.state
        val savedState = SavedGameState(
            boardSize = boardId,
            gridValues = currentState.grid.flatten(),
            currentScore = currentState.currentScore,
            moveCount = currentState.moveCount,
            status = currentState.status.name,
            isContinued = currentState.isContinued,
            targetGoal = currentState.targetGoal
        )
        viewModelScope.launch {
            gameRepository.saveGameState(boardId, savedState)
        }
    }
}

class GameViewModelFactory(
    private val context: Context,
    private val rows: Int,
    private val cols: Int,
    private val targetGoal: Int = 32,
    private val initialMissionId: Int? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GameViewModel(
            rows = rows,
            cols = cols,
            targetGoal = targetGoal,
            initialMissionId = initialMissionId,
            gameRepository = GameRepository(context),
            statisticsRepository = StatisticsRepository(context),
            settingsRepository = SettingsRepository(context)
        ) as T
    }
}
