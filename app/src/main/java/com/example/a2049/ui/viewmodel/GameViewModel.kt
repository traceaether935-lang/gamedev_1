package com.example.a2049.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.a2049.auth.PlayGamesAuthManager
import com.example.a2049.billing.BillingManager
import com.example.a2049.ui.model.ActiveTool
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
    private val settingsRepository: SettingsRepository,
    playGamesAuthManager: PlayGamesAuthManager? = null,
    private val billingManager: BillingManager? = null
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

    private val _isAdFree = MutableStateFlow(false)
    val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

    private val _undoUses = MutableStateFlow(2)
    val undoUses: StateFlow<Int> = _undoUses.asStateFlow()

    private val _hammerUses = MutableStateFlow(2)
    val hammerUses: StateFlow<Int> = _hammerUses.asStateFlow()

    private val _switchUses = MutableStateFlow(2)
    val switchUses: StateFlow<Int> = _switchUses.asStateFlow()

    private val _activeTool = MutableStateFlow(ActiveTool.NONE)
    val activeTool: StateFlow<ActiveTool> = _activeTool.asStateFlow()

    private val _firstSelectedTileIndex = MutableStateFlow<Pair<Int, Int>?>(null)
    val firstSelectedTileIndex: StateFlow<Pair<Int, Int>?> = _firstSelectedTileIndex.asStateFlow()

    private var eventIdCounter: Long = 0L
    private var swapFirstTile: Pair<Int, Int>? = null

    init {
        playGamesAuthManager?.let { auth ->
            viewModelScope.launch {
                auth.isAuthenticated.collect { isAuthenticated ->
                    if (isAuthenticated) {
                        billingManager?.restorePurchases()
                    }
                }
            }
        }

        viewModelScope.launch {
            launch {
                settingsRepository.userSettingsFlow.collect { settings ->
                    _uiState.update { it.copy(userSettings = settings) }
                    _isAdFree.value = settings.isAdFree
                    _hammerUses.value = settings.hammerUses
                    _switchUses.value = settings.switchUses
                    _undoUses.value = settings.undoUses
                }
            }

            launch {
                gameRepository.getBestScoreFlow(boardId).collect { bestScore ->
                    gameEngine.updateBestScore(bestScore)
                    updateUiState()
                }
            }

            val isMissionMode = initialMissionId != null || initialMission != null

            if (isMissionMode) {
                gameEngine.restart(
                    rows = rows,
                    cols = cols,
                    targetGoal = initialMission?.targetValue ?: targetGoal,
                    activeMission = initialMission
                )
                _uiState.update {
                    it.copy(
                        gameState = gameEngine.state,
                        isWinDialogShown = false,
                        isPauseDialogShown = false,
                        isMissionCompleteDialogShown = gameEngine.state.isMissionCompleted,
                        isMissionFailedDialogShown = gameEngine.state.isMissionFailed
                    )
                }
            } else {
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
                        activeMission = null
                    )
                    val isWinDialog = (status == GameStatus.WON && !savedState.isContinued)
                    val isPauseDialog = (status == GameStatus.PAUSED)
                    _uiState.update {
                        it.copy(
                            gameState = gameEngine.state,
                            isWinDialogShown = isWinDialog,
                            isPauseDialogShown = isPauseDialog,
                            isMissionCompleteDialogShown = false,
                            isMissionFailedDialogShown = false
                        )
                    }
                } else {
                    statisticsRepository.recordGameStarted(boardId)
                    persistCurrentGameState()
                }
            }

            updateUiState(isLoading = false)
        }
    }

    fun setAdFree(adFree: Boolean) {
        _isAdFree.value = adFree
        viewModelScope.launch {
            settingsRepository.setAdFree(adFree)
        }
    }

    fun onConsumablePurchased(productId: String) {
        when (productId) {
            "sku_buy_hammer_pack" -> addHammerUses(5)
            "sku_buy_switch_pack" -> addSwitchUses(5)
            "sku_buy_undo_pack" -> addUndoUses(5)
        }
    }

    fun addUndoUses(count: Int = 2) {
        _undoUses.update { it + count }
        viewModelScope.launch {
            settingsRepository.setUndoUses(_undoUses.value)
        }
    }

    fun addHammerUses(count: Int = 2) {
        _hammerUses.update { it + count }
        viewModelScope.launch {
            settingsRepository.setHammerUses(_hammerUses.value)
        }
    }

    fun addSwitchUses(count: Int = 2) {
        _switchUses.update { it + count }
        viewModelScope.launch {
            settingsRepository.setSwitchUses(_switchUses.value)
        }
    }

    fun onUndoToolClicked(onRequestRewardedAd: ((onRewardEarned: () -> Unit) -> Unit)? = null) {
        if (_undoUses.value > 0) {
            val success = gameEngine.undo()
            if (success) {
                _undoUses.update { it - 1 }
                viewModelScope.launch {
                    settingsRepository.setUndoUses(_undoUses.value)
                }
                _activeTool.value = ActiveTool.NONE
                _firstSelectedTileIndex.value = null
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
            _activeTool.update { current ->
                if (current == ActiveTool.HAMMER) ActiveTool.NONE else ActiveTool.HAMMER
            }
            _firstSelectedTileIndex.value = null
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
            _activeTool.update { current ->
                if (current == ActiveTool.SWITCH) ActiveTool.NONE else ActiveTool.SWITCH
            }
            _firstSelectedTileIndex.value = null
        } else {
            if (onRequestRewardedAd != null) {
                onRequestRewardedAd { addSwitchUses(2) }
            } else {
                addSwitchUses(2)
            }
        }
    }

    fun selectMission(mission: Mission?) {
        swapFirstTile = null
        _activeTool.value = ActiveTool.NONE
        _firstSelectedTileIndex.value = null
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
        _activeTool.value = ActiveTool.NONE
        _firstSelectedTileIndex.value = null
        viewModelScope.launch {
            if (gameEngine.state.activeMission == null) {
                gameRepository.clearSavedGameState(boardId)
            }
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
        when (_activeTool.value) {
            ActiveTool.HAMMER -> {
                if (_hammerUses.value > 0 && gameEngine.state.grid.getOrNull(row)?.getOrNull(col) != 0) {
                    if (gameEngine.state.hammerCount <= 0) {
                        gameEngine.setGrid(
                            grid = gameEngine.state.grid,
                            score = gameEngine.state.currentScore,
                            moveCount = gameEngine.state.moveCount,
                            status = gameEngine.state.status,
                            canUndo = gameEngine.state.canUndo,
                            isContinued = gameEngine.state.isContinued,
                            targetGoal = gameEngine.state.targetGoal,
                            hammerCount = _hammerUses.value,
                            swapCount = _switchUses.value
                        )
                    }
                    val success = gameEngine.useHammer(row, col)
                    if (success) {
                        _hammerUses.update { it - 1 }
                        viewModelScope.launch {
                            settingsRepository.setHammerUses(_hammerUses.value)
                        }
                        _activeTool.value = ActiveTool.NONE
                        _firstSelectedTileIndex.value = null
                        persistCurrentGameState()
                        updateUiState()
                    }
                }
            }
            ActiveTool.SWITCH -> {
                if (_switchUses.value > 0) {
                    val first = _firstSelectedTileIndex.value
                    if (first == null) {
                        _firstSelectedTileIndex.value = Pair(row, col)
                    } else if (first.first == row && first.second == col) {
                        _firstSelectedTileIndex.value = null
                    } else {
                        if (gameEngine.state.swapCount <= 0) {
                            gameEngine.setGrid(
                                grid = gameEngine.state.grid,
                                score = gameEngine.state.currentScore,
                                moveCount = gameEngine.state.moveCount,
                                status = gameEngine.state.status,
                                canUndo = gameEngine.state.canUndo,
                                isContinued = gameEngine.state.isContinued,
                                targetGoal = gameEngine.state.targetGoal,
                                hammerCount = _hammerUses.value,
                                swapCount = _switchUses.value
                            )
                        }
                        val success = gameEngine.useSwap(first.first, first.second, row, col)
                        if (success) {
                            _switchUses.update { it - 1 }
                            viewModelScope.launch {
                                settingsRepository.setSwitchUses(_switchUses.value)
                            }
                            _activeTool.value = ActiveTool.NONE
                            _firstSelectedTileIndex.value = null
                            persistCurrentGameState()
                            updateUiState()
                        } else {
                            _firstSelectedTileIndex.value = null
                        }
                    }
                }
            }
            ActiveTool.NONE -> {
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
            gameEngine.state.status != GameStatus.PLAYING || gameEngine.state.activePowerUp != null ||
            _activeTool.value != ActiveTool.NONE
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
                    if (newState.activeMission == null) {
                        gameRepository.clearSavedGameState(boardId)
                    }
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
        onUndoToolClicked(null)
    }

    fun restart() {
        swapFirstTile = null
        _activeTool.value = ActiveTool.NONE
        _firstSelectedTileIndex.value = null
        viewModelScope.launch {
            if (gameEngine.state.activeMission == null) {
                gameRepository.clearSavedGameState(boardId)
            }
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
        if (currentState.activeMission != null) {
            return
        }
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
    private val initialMissionId: Int? = null,
    private val playGamesAuthManager: PlayGamesAuthManager? = null,
    private val billingManager: BillingManager? = null
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
            settingsRepository = SettingsRepository(context),
            playGamesAuthManager = playGamesAuthManager,
            billingManager = billingManager
        ) as T
    }
}
