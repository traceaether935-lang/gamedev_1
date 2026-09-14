package com.aethertrace.numberdrop2048hexa.ui.viewmodel

import com.aethertrace.numberdrop2048hexa.ui.model.ActiveTool
import com.game.a2048.MissionCatalog
import com.game.a2048.data.FakeDataStore
import com.game.a2048.data.GameRepository
import com.game.a2048.data.SettingsRepository
import com.game.a2048.data.StatisticsRepository
import com.game.a2048.data.model.SavedGameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var fakeGameDataStore: FakeDataStore
    private lateinit var fakeStatsDataStore: FakeDataStore
    private lateinit var fakeSettingsDataStore: FakeDataStore

    private lateinit var gameRepository: GameRepository
    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeGameDataStore = FakeDataStore()
        fakeStatsDataStore = FakeDataStore()
        fakeSettingsDataStore = FakeDataStore()

        gameRepository = GameRepository(fakeGameDataStore)
        statisticsRepository = StatisticsRepository(fakeStatsDataStore)
        settingsRepository = SettingsRepository(fakeSettingsDataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun freeplayModeRestoresSavedStateWhenAvailable() = runTest {
        val boardSize = 4
        val savedState = SavedGameState(
            boardSize = boardSize,
            gridValues = listOf(
                2, 4, 8, 16,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
            ),
            currentScore = 150,
            moveCount = 12,
            status = "PLAYING",
            isContinued = false,
            targetGoal = 2048
        )
        gameRepository.saveGameState(boardSize, savedState)

        val viewModel = GameViewModel(
            rows = 4,
            cols = 4,
            targetGoal = 2048,
            initialMissionId = null,
            gameRepository = gameRepository,
            statisticsRepository = statisticsRepository,
            settingsRepository = settingsRepository
        )

        val uiState = viewModel.uiState.value
        assertEquals(150, uiState.gameState.currentScore)
        assertEquals(12, uiState.gameState.moveCount)
        assertNull(uiState.gameState.activeMission)
    }

    @Test
    fun missionModeDoesNotLoadSavedFreeplayStateAndStartsFresh() = runTest {
        val boardSize = 4
        val savedState = SavedGameState(
            boardSize = boardSize,
            gridValues = listOf(
                2, 4, 8, 16,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
            ),
            currentScore = 150,
            moveCount = 12,
            status = "PLAYING",
            isContinued = false,
            targetGoal = 2048
        )
        gameRepository.saveGameState(boardSize, savedState)

        val mission = MissionCatalog.getMissionById(1)
        assertNotNull(mission)

        val viewModel = GameViewModel(
            rows = 4,
            cols = 4,
            targetGoal = 2048,
            initialMissionId = 1,
            gameRepository = gameRepository,
            statisticsRepository = statisticsRepository,
            settingsRepository = settingsRepository
        )

        val uiState = viewModel.uiState.value
        assertEquals(0, uiState.gameState.currentScore)
        assertEquals(0, uiState.gameState.moveCount)
        assertEquals(mission, uiState.gameState.activeMission)

        // Verify Freeplay saved state in DataStore remains untouched and preserved
        val restoredFreeplayState = gameRepository.getSavedGameStateFlow(boardSize).first()
        assertNotNull(restoredFreeplayState)
        assertEquals(150, restoredFreeplayState?.currentScore)
    }

    @Test
    fun selectingMissionResetsToFreshBoardAndDoesNotOverwriteFreeplayState() = runTest {
        val boardSize = 4
        val savedState = SavedGameState(
            boardSize = boardSize,
            gridValues = listOf(
                2, 4, 8, 16,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
            ),
            currentScore = 500,
            moveCount = 30,
            status = "PLAYING",
            isContinued = false,
            targetGoal = 2048
        )
        gameRepository.saveGameState(boardSize, savedState)

        val viewModel = GameViewModel(
            rows = 4,
            cols = 4,
            targetGoal = 2048,
            initialMissionId = null,
            gameRepository = gameRepository,
            statisticsRepository = statisticsRepository,
            settingsRepository = settingsRepository
        )

        // Initially in Freeplay with loaded state
        assertEquals(500, viewModel.uiState.value.gameState.currentScore)

        val mission = MissionCatalog.getMissionById(2)
        assertNotNull(mission)
        viewModel.selectMission(mission)

        val updatedUiState = viewModel.uiState.value
        assertEquals(0, updatedUiState.gameState.currentScore)
        assertEquals(0, updatedUiState.gameState.moveCount)
        assertEquals(mission, updatedUiState.gameState.activeMission)

        // Ensure DataStore saved Freeplay state was not overwritten by mission select
        val freeplaySaveInStore = gameRepository.getSavedGameStateFlow(boardSize).first()
        assertNotNull(freeplaySaveInStore)
        assertEquals(500, freeplaySaveInStore?.currentScore)
    }

    @Test
    fun retryMissionDoesNotClearSavedFreeplayState() = runTest {
        val boardSize = 4
        val savedState = SavedGameState(
            boardSize = boardSize,
            gridValues = listOf(
                2, 4, 8, 16,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
            ),
            currentScore = 300,
            moveCount = 20,
            status = "PLAYING",
            isContinued = false,
            targetGoal = 2048
        )
        gameRepository.saveGameState(boardSize, savedState)

        val viewModel = GameViewModel(
            rows = 4,
            cols = 4,
            targetGoal = 2048,
            initialMissionId = 1,
            gameRepository = gameRepository,
            statisticsRepository = statisticsRepository,
            settingsRepository = settingsRepository
        )

        viewModel.retryMission()

        val freeplaySaveInStore = gameRepository.getSavedGameStateFlow(boardSize).first()
        assertNotNull(freeplaySaveInStore)
        assertEquals(300, freeplaySaveInStore?.currentScore)
    }

    @Test
    fun defaultToolUsesAndActiveToolState() = runTest {
        val viewModel = GameViewModel(
            rows = 4,
            cols = 4,
            targetGoal = 2048,
            initialMissionId = null,
            gameRepository = gameRepository,
            statisticsRepository = statisticsRepository,
            settingsRepository = settingsRepository
        )

        assertEquals(2, viewModel.undoUses.value)
        assertEquals(2, viewModel.hammerUses.value)
        assertEquals(2, viewModel.switchUses.value)
        assertEquals(ActiveTool.NONE, viewModel.activeTool.value)
        assertNull(viewModel.firstSelectedTileIndex.value)
    }

    @Test
    fun toggleHammerAndSwitchTools() = runTest {
        val viewModel = GameViewModel(
            rows = 4,
            cols = 4,
            targetGoal = 2048,
            initialMissionId = null,
            gameRepository = gameRepository,
            statisticsRepository = statisticsRepository,
            settingsRepository = settingsRepository
        )

        viewModel.onHammerToolClicked()
        assertEquals(ActiveTool.HAMMER, viewModel.activeTool.value)

        viewModel.onHammerToolClicked()
        assertEquals(ActiveTool.NONE, viewModel.activeTool.value)

        viewModel.onSwitchToolClicked()
        assertEquals(ActiveTool.SWITCH, viewModel.activeTool.value)

        viewModel.onSwitchToolClicked()
        assertEquals(ActiveTool.NONE, viewModel.activeTool.value)
    }

    @Test
    fun hammerToolClearsTileAndDeductsUse() = runTest {
        val savedState = SavedGameState(
            boardSize = 4,
            gridValues = listOf(
                8, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
            ),
            currentScore = 0,
            moveCount = 0,
            status = "PLAYING",
            isContinued = false,
            targetGoal = 2048
        )
        gameRepository.saveGameState(4, savedState)

        val viewModel = GameViewModel(
            rows = 4,
            cols = 4,
            targetGoal = 2048,
            initialMissionId = null,
            gameRepository = gameRepository,
            statisticsRepository = statisticsRepository,
            settingsRepository = settingsRepository
        )

        viewModel.onHammerToolClicked()
        assertEquals(ActiveTool.HAMMER, viewModel.activeTool.value)

        // Click non-zero tile at (0, 0)
        viewModel.onTileClick(0, 0)

        assertEquals(0, viewModel.uiState.value.gameState.grid[0][0])
        assertEquals(1, viewModel.hammerUses.value)
        assertEquals(ActiveTool.NONE, viewModel.activeTool.value)
    }

    @Test
    fun switchToolSwapsTilesAndDeductsUse() = runTest {
        val savedState = SavedGameState(
            boardSize = 4,
            gridValues = listOf(
                2, 8, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
            ),
            currentScore = 0,
            moveCount = 0,
            status = "PLAYING",
            isContinued = false,
            targetGoal = 2048
        )
        gameRepository.saveGameState(4, savedState)

        val viewModel = GameViewModel(
            rows = 4,
            cols = 4,
            targetGoal = 2048,
            initialMissionId = null,
            gameRepository = gameRepository,
            statisticsRepository = statisticsRepository,
            settingsRepository = settingsRepository
        )

        viewModel.onSwitchToolClicked()
        assertEquals(ActiveTool.SWITCH, viewModel.activeTool.value)

        // Select first tile at (0, 0)
        viewModel.onTileClick(0, 0)
        assertEquals(Pair(0, 0), viewModel.firstSelectedTileIndex.value)

        // Select second tile at (0, 1)
        viewModel.onTileClick(0, 1)

        assertEquals(8, viewModel.uiState.value.gameState.grid[0][0])
        assertEquals(2, viewModel.uiState.value.gameState.grid[0][1])
        assertEquals(1, viewModel.switchUses.value)
        assertEquals(ActiveTool.NONE, viewModel.activeTool.value)
        assertNull(viewModel.firstSelectedTileIndex.value)
    }

    @Test
    fun rewardedAdGrantsUsesWhenCountIsZero() = runTest {
        val viewModel = GameViewModel(
            rows = 4,
            cols = 4,
            targetGoal = 2048,
            initialMissionId = null,
            gameRepository = gameRepository,
            statisticsRepository = statisticsRepository,
            settingsRepository = settingsRepository
        )

        // Use up 2 undo uses
        viewModel.onUndoToolClicked()
        viewModel.onUndoToolClicked()
        // Wait, if no undo was possible, undo uses was not deducted, so let's set uses manually or perform valid moves
        // Or test addUndoUses / rewarded callback directly:
        viewModel.onUndoToolClicked(onRequestRewardedAd = { onReward ->
            onReward()
        })
        // Since undoUses was 2 > 0, it attempted undo. Let's consume uses or call addUndoUses
        viewModel.addUndoUses(2)
        assertEquals(4, viewModel.undoUses.value)

        var rewardedAdTriggered = false
        // Simulate hammerUses == 0
        viewModel.onHammerToolClicked() // toggle
        viewModel.onHammerToolClicked() // toggle off
        viewModel.addHammerUses(-2) // now 0
        assertEquals(0, viewModel.hammerUses.value)

        viewModel.onHammerToolClicked(onRequestRewardedAd = { onReward ->
            rewardedAdTriggered = true
            onReward()
        })

        Assert.assertTrue(rewardedAdTriggered)
        assertEquals(2, viewModel.hammerUses.value)
    }

    @Test
    fun setAdFreeUpdatesStateAndPersists() = runTest {
        val viewModel = GameViewModel(
            rows = 4,
            cols = 4,
            targetGoal = 2048,
            initialMissionId = null,
            gameRepository = gameRepository,
            statisticsRepository = statisticsRepository,
            settingsRepository = settingsRepository
        )

        Assert.assertFalse(viewModel.isAdFree.value)

        viewModel.setAdFree(true)

        Assert.assertTrue(viewModel.isAdFree.value)
        val settings = settingsRepository.userSettingsFlow.first()
        Assert.assertTrue(settings.isAdFree)
    }

    @Test
    fun consumablePacksGrant5UsesEach() = runTest {
        val viewModel = GameViewModel(
            rows = 4,
            cols = 4,
            targetGoal = 2048,
            initialMissionId = null,
            gameRepository = gameRepository,
            statisticsRepository = statisticsRepository,
            settingsRepository = settingsRepository
        )

        val initialHammer = viewModel.hammerUses.value
        val initialSwitch = viewModel.switchUses.value
        val initialUndo = viewModel.undoUses.value

        viewModel.onConsumablePurchased("sku_buy_hammer_pack")
        assertEquals(initialHammer + 5, viewModel.hammerUses.value)

        viewModel.onConsumablePurchased("sku_buy_switch_pack")
        assertEquals(initialSwitch + 5, viewModel.switchUses.value)

        viewModel.onConsumablePurchased("sku_buy_undo_pack")
        assertEquals(initialUndo + 5, viewModel.undoUses.value)
    }
}

