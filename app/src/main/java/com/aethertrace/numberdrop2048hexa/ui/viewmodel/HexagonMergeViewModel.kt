package com.aethertrace.numberdrop2048hexa.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aethertrace.numberdrop2048hexa.ui.model.ActiveTool
import com.aethertrace.numberdrop2048hexa.ui.model.FeedbackEvent
import com.aethertrace.numberdrop2048hexa.ui.model.FeedbackWords
import com.game.a2048.GameStatus
import com.game.a2048.data.GameRepository
import com.game.a2048.data.SettingsRepository
import com.game.a2048.data.StatisticsRepository
import com.game.a2048.data.model.SavedGameState
import com.game.a2048.data.model.UserSettings
import com.game.a2048.hex.HexCell
import com.game.a2048.hex.HexDirection
import com.game.a2048.hex.HexEngine
import com.game.a2048.hex.HexGameState
import com.game.a2048.hex.HexGrid
import com.game.a2048.hex.HexPiece
import com.game.a2048.hex.PowerUpType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HexUiState(
    val gameState: HexGameState = HexGameState(),
    val userSettings: UserSettings = UserSettings(),
    val isWinDialogShown: Boolean = false,
    val isPauseDialogShown: Boolean = false,
    val isLoading: Boolean = true
)

class HexagonMergeViewModel(
    val gridRadius: Int = HexGrid.DEFAULT_RADIUS,
    private val gameRepository: GameRepository,
    private val statisticsRepository: StatisticsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val boardId: Int = HexGrid.getBoardIdForRadius(gridRadius)

    private val hexEngine = HexEngine(radius = gridRadius)

    private val _uiState = MutableStateFlow(
        HexUiState(gameState = hexEngine.state)
    )
    val uiState: StateFlow<HexUiState> = _uiState.asStateFlow()

    val gameState: StateFlow<HexGameState> = MutableStateFlow(hexEngine.state).apply {
        viewModelScope.launch {
            _uiState.collect { value = it.gameState }
        }
    }

    val upcomingPieces: StateFlow<List<HexPiece>> = MutableStateFlow(hexEngine.upcomingPieces).apply {
        viewModelScope.launch {
            _uiState.collect { value = it.gameState.upcomingPieces }
        }
    }

    private val _feedbackEvent = MutableStateFlow<FeedbackEvent?>(null)
    val feedbackEvent: StateFlow<FeedbackEvent?> = _feedbackEvent.asStateFlow()

    private val _swapFirstCell = MutableStateFlow<HexCell?>(null)
    val swapFirstCell: StateFlow<HexCell?> = _swapFirstCell.asStateFlow()

    private val _undoUses = MutableStateFlow(2)
    val undoUses: StateFlow<Int> = _undoUses.asStateFlow()

    private val _hammerUses = MutableStateFlow(2)
    val hammerUses: StateFlow<Int> = _hammerUses.asStateFlow()

    private val _switchUses = MutableStateFlow(2)
    val switchUses: StateFlow<Int> = _switchUses.asStateFlow()

    private val _activeTool = MutableStateFlow(ActiveTool.NONE)
    val activeTool: StateFlow<ActiveTool> = _activeTool.asStateFlow()

    private val _firstSelectedCell = MutableStateFlow<HexCell?>(null)
    val firstSelectedCell: StateFlow<HexCell?> = _firstSelectedCell.asStateFlow()

    private var eventIdCounter: Long = 0L

    init {
        viewModelScope.launch {
            launch {
                settingsRepository.userSettingsFlow.collect { settings ->
                    _uiState.update { it.copy(userSettings = settings) }
                }
            }

            launch {
                gameRepository.getBestScoreFlow(boardId).collect { bestScore ->
                    hexEngine.updateBestScore(bestScore)
                    updateUiState()
                }
            }

            val savedState = gameRepository.getSavedGameStateFlow(boardId).firstOrNull()
            val expectedCellCount = HexGrid.getCells(gridRadius).size
            if (savedState != null && savedState.gridValues.size == expectedCellCount) {
                val gridMap = HexGrid.getCells(gridRadius).zip(savedState.gridValues).toMap()
                val status = try {
                    GameStatus.valueOf(savedState.status)
                } catch (_: Exception) {
                    GameStatus.PLAYING
                }
                hexEngine.setGrid(
                    gridMap = gridMap,
                    score = savedState.currentScore,
                    moveCount = savedState.moveCount,
                    status = status,
                    isContinued = savedState.isContinued,
                    targetGoal = savedState.targetGoal
                )
                val isWinDialog = (status == GameStatus.WON && !savedState.isContinued)
                val isPauseDialog = (status == GameStatus.PAUSED)
                _uiState.update {
                    it.copy(
                        isWinDialogShown = isWinDialog,
                        isPauseDialogShown = isPauseDialog
                    )
                }
            } else {
                statisticsRepository.recordGameStarted(boardId)
                persistCurrentGameState()
            }

            updateUiState(isLoading = false)
        }
    }

    fun addUndoUses(count: Int = 2) {
        _undoUses.update { it + count }
    }

    fun addHammerUses(count: Int = 2) {
        _hammerUses.update { it + count }
    }

    fun addSwitchUses(count: Int = 2) {
        _switchUses.update { it + count }
    }

    fun onUndoToolClicked(onRequestRewardedAd: ((onRewardEarned: () -> Unit) -> Unit)? = null) {
        if (_undoUses.value > 0) {
            _swapFirstCell.value = null
            _firstSelectedCell.value = null
            if (hexEngine.undo()) {
                _undoUses.update { it - 1 }
                _activeTool.value = ActiveTool.NONE
                persistCurrentGameState()
                updateUiState()
            }
        } else {
            if (onRequestRewardedAd != null) {
                onRequestRewardedAd { addUndoUses(2) }
            } else {
                addUndoUses(2)
            }
        }
    }

    fun onHammerToolClicked(onRequestRewardedAd: ((onRewardEarned: () -> Unit) -> Unit)? = null) {
        if (_hammerUses.value > 0) {
            _swapFirstCell.value = null
            _firstSelectedCell.value = null
            _activeTool.update { current ->
                if (current == ActiveTool.HAMMER) ActiveTool.NONE else ActiveTool.HAMMER
            }
        } else {
            if (onRequestRewardedAd != null) {
                onRequestRewardedAd { addHammerUses(2) }
            } else {
                addHammerUses(2)
            }
        }
    }

    fun onSwitchToolClicked(onRequestRewardedAd: ((onRewardEarned: () -> Unit) -> Unit)? = null) {
        if (_switchUses.value > 0) {
            _swapFirstCell.value = null
            _firstSelectedCell.value = null
            _activeTool.update { current ->
                if (current == ActiveTool.SWITCH) ActiveTool.NONE else ActiveTool.SWITCH
            }
        } else {
            if (onRequestRewardedAd != null) {
                onRequestRewardedAd { addSwitchUses(2) }
            } else {
                addSwitchUses(2)
            }
        }
    }

    fun togglePowerUp(powerUp: PowerUpType) {
        _swapFirstCell.value = null
        _activeTool.value = ActiveTool.NONE
        _firstSelectedCell.value = null
        hexEngine.togglePowerUp(powerUp)
        updateUiState()
    }

    fun cancelPowerUp() {
        _swapFirstCell.value = null
        _activeTool.value = ActiveTool.NONE
        _firstSelectedCell.value = null
        hexEngine.cancelPowerUp()
        updateUiState()
    }

    fun selectSwapCell(cell: HexCell) {
        _swapFirstCell.value = cell
    }

    fun clearSwapCell() {
        _swapFirstCell.value = null
    }

    fun useHammer(
        cell: HexCell,
        onHaptic: (() -> Unit)? = null,
        onSound: ((isMerge: Boolean) -> Unit)? = null
    ) {
        val currentState = _uiState.value
        if (currentState.isPauseDialogShown || currentState.isWinDialogShown ||
            hexEngine.state.status != GameStatus.PLAYING
        ) {
            return
        }

        val oldTargetGoal = hexEngine.state.targetGoal
        val success = hexEngine.useHammer(cell)
        if (success) {
            if (_activeTool.value == ActiveTool.HAMMER) {
                if (_hammerUses.value > 0) _hammerUses.update { it - 1 }
                _activeTool.value = ActiveTool.NONE
            }
            if (currentState.userSettings.hapticsEnabled) {
                onHaptic?.invoke()
            }
            if (currentState.userSettings.soundEnabled) {
                onSound?.invoke(false)
            }

            val newTargetGoal = hexEngine.state.targetGoal
            if (newTargetGoal > oldTargetGoal) {
                _feedbackEvent.value = FeedbackEvent(
                    id = System.currentTimeMillis() * 1000 + (++eventIdCounter),
                    text = "TARGET $oldTargetGoal REACHED! 🎯",
                    tier = 4
                )
            } else {
                _feedbackEvent.value = FeedbackEvent(
                    id = System.currentTimeMillis() * 1000 + (++eventIdCounter),
                    text = "Tile Destroyed! 🔨",
                    tier = 3
                )
            }

            persistCurrentGameState()
            updateUiState()
        }
    }

    fun useSwap(
        cell1: HexCell,
        cell2: HexCell,
        onHaptic: (() -> Unit)? = null,
        onSound: ((isMerge: Boolean) -> Unit)? = null
    ) {
        val currentState = _uiState.value
        if (currentState.isPauseDialogShown || currentState.isWinDialogShown ||
            hexEngine.state.status != GameStatus.PLAYING
        ) {
            return
        }

        val oldTargetGoal = hexEngine.state.targetGoal
        val success = hexEngine.useSwap(cell1, cell2)
        if (success) {
            _swapFirstCell.value = null
            _firstSelectedCell.value = null
            if (_activeTool.value == ActiveTool.SWITCH) {
                if (_switchUses.value > 0) _switchUses.update { it - 1 }
                _activeTool.value = ActiveTool.NONE
            }
            if (currentState.userSettings.hapticsEnabled) {
                onHaptic?.invoke()
            }
            if (currentState.userSettings.soundEnabled) {
                onSound?.invoke(false)
            }

            val newTargetGoal = hexEngine.state.targetGoal
            if (newTargetGoal > oldTargetGoal) {
                _feedbackEvent.value = FeedbackEvent(
                    id = System.currentTimeMillis() * 1000 + (++eventIdCounter),
                    text = "TARGET $oldTargetGoal REACHED! 🎯",
                    tier = 4
                )
            } else {
                _feedbackEvent.value = FeedbackEvent(
                    id = System.currentTimeMillis() * 1000 + (++eventIdCounter),
                    text = "Tiles Swapped! 🔄",
                    tier = 3
                )
            }

            persistCurrentGameState()
            updateUiState()
        }
    }

    fun placePiece(
        pieceIndex: Int,
        targetCell: HexCell,
        onHaptic: (() -> Unit)? = null,
        onSound: ((isMerge: Boolean) -> Unit)? = null
    ) {
        val currentState = _uiState.value
        if (currentState.isPauseDialogShown || currentState.isWinDialogShown ||
            hexEngine.state.status != GameStatus.PLAYING
        ) {
            return
        }

        val oldTargetGoal = hexEngine.state.targetGoal
        val success = hexEngine.placePiece(pieceIndex, targetCell)
        if (success) {
            val lastResult = hexEngine.lastPieceResult
            val scoreGained = lastResult?.scoreGained ?: 0
            val maxChainCount = lastResult?.chainCount ?: 0
            val maxMergedValue = lastResult?.maxMergedValue ?: 0
            val mergeCount = lastResult?.mergeCount ?: 0

            if (currentState.userSettings.hapticsEnabled) {
                onHaptic?.invoke()
            }
            if (currentState.userSettings.soundEnabled) {
                onSound?.invoke(scoreGained > 0)
            }

            val newTargetGoal = hexEngine.state.targetGoal
            if (newTargetGoal > oldTargetGoal) {
                _feedbackEvent.value = FeedbackEvent(
                    id = System.currentTimeMillis() * 1000 + (++eventIdCounter),
                    text = "TARGET $oldTargetGoal REACHED! 🎯",
                    tier = 4
                )
            } else {
                val tier = when {
                    maxChainCount >= 2 -> 4
                    mergeCount >= 2 -> 4
                    maxMergedValue >= 512 -> 3
                    maxMergedValue >= 64 -> 2
                    maxMergedValue >= 16 -> 1
                    else -> null
                }

                if (tier != null) {
                    val text = if (maxChainCount >= 2) {
                        when (maxChainCount) {
                            2 -> "Combo x2!"
                            3 -> "Chain Reaction!"
                            else -> "Legendary Merge!"
                        }
                    } else {
                        FeedbackWords.getRandomWordForTier(tier)
                    }
                    _feedbackEvent.value = FeedbackEvent(
                        id = System.currentTimeMillis() * 1000 + (++eventIdCounter),
                        text = text,
                        tier = tier
                    )
                }
            }

            val newState = hexEngine.state
            if (newState.status == GameStatus.WON && !currentState.gameState.isContinued) {
                viewModelScope.launch { statisticsRepository.recordGameWon(boardId) }
                _uiState.update { it.copy(isWinDialogShown = true) }
            } else if (newState.status == GameStatus.GAME_OVER) {
                val highestTile = newState.grid.values.maxOrNull() ?: 0
                viewModelScope.launch {
                    statisticsRepository.recordGameEnd(
                        boardSize = boardId,
                        finalScore = newState.currentScore,
                        highestTile = highestTile,
                        moveCount = newState.moveCount
                    )
                    gameRepository.clearSavedGameState(boardId)
                }
            }

            persistCurrentGameState()
            updateUiState()
        }
    }

    @Deprecated(
        message = "Hexagon Merge is a piece placement game mode. Board-level swipe gestures are disabled.",
        level = DeprecationLevel.WARNING
    )
    fun onSwipe(
        direction: HexDirection,
        onHaptic: (() -> Unit)? = null,
        onSound: ((isMerge: Boolean) -> Unit)? = null
    ) {
        val currentState = _uiState.value
        if (currentState.isPauseDialogShown || currentState.isWinDialogShown ||
            hexEngine.state.status != GameStatus.PLAYING
        ) {
            return
        }

        val oldTargetGoal = hexEngine.state.targetGoal
        val result = hexEngine.move(direction)
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
            if (newState.status == GameStatus.WON && !currentState.gameState.isContinued) {
                viewModelScope.launch { statisticsRepository.recordGameWon(boardId) }
                _uiState.update { it.copy(isWinDialogShown = true) }
            } else if (newState.status == GameStatus.GAME_OVER) {
                val highestTile = newState.grid.values.maxOrNull() ?: 0
                viewModelScope.launch {
                    statisticsRepository.recordGameEnd(
                        boardSize = boardId,
                        finalScore = newState.currentScore,
                        highestTile = highestTile,
                        moveCount = newState.moveCount
                    )
                    gameRepository.clearSavedGameState(boardId)
                }
            }

            persistCurrentGameState()
            updateUiState()
        }
    }

    fun undo() {
        onUndoToolClicked(null)
    }

    fun restart() {
        viewModelScope.launch {
            _swapFirstCell.value = null
            _firstSelectedCell.value = null
            _activeTool.value = ActiveTool.NONE
            gameRepository.clearSavedGameState(boardId)
            statisticsRepository.recordGameStarted(boardId)
            hexEngine.restart()
            _uiState.update {
                it.copy(
                    gameState = hexEngine.state,
                    isWinDialogShown = false,
                    isPauseDialogShown = false
                )
            }
            persistCurrentGameState()
        }
    }

    fun pause() {
        hexEngine.pause()
        _uiState.update {
            it.copy(
                gameState = hexEngine.state,
                isPauseDialogShown = true
            )
        }
        persistCurrentGameState()
    }

    fun resume() {
        hexEngine.resume()
        _uiState.update {
            it.copy(
                gameState = hexEngine.state,
                isPauseDialogShown = false
            )
        }
        persistCurrentGameState()
    }

    fun continueGame() {
        hexEngine.continueGame()
        _uiState.update {
            it.copy(
                gameState = hexEngine.state,
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
                gameState = hexEngine.state,
                isLoading = isLoading
            )
        }
    }

    private fun persistCurrentGameState() {
        val currentState = hexEngine.state
        val savedState = SavedGameState(
            boardSize = boardId,
            gridValues = HexGrid.getCells(gridRadius).map { currentState.grid[it] ?: 0 },
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

class HexagonMergeViewModelFactory(
    private val context: Context,
    private val gridRadius: Int = HexGrid.DEFAULT_RADIUS
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HexagonMergeViewModel(
            gridRadius = gridRadius,
            gameRepository = GameRepository(context),
            statisticsRepository = StatisticsRepository(context),
            settingsRepository = SettingsRepository(context)
        ) as T
    }
}
