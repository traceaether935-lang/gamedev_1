package com.game.a2048

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {

    @Test
    fun testInitialization4x4And5x5() {
        val engine4 = GameEngine(rows = 4, cols = 4)
        assertEquals(4, engine4.state.rows)
        assertEquals(4, engine4.state.cols)
        assertEquals(4, engine4.state.grid.size)
        assertEquals(4, engine4.state.grid[0].size)
        assertEquals(0, engine4.state.currentScore)
        assertEquals(0, engine4.state.moveCount)
        assertEquals(32, engine4.state.targetGoal)
        assertEquals(32, engine4.state.targetTile)
        assertEquals(GameStatus.PLAYING, engine4.state.status)
        assertFalse(engine4.state.canUndo)

        // Count non-zero tiles: should be 2
        val nonZeroTiles4 = engine4.state.grid.flatten().count { it != 0 }
        assertEquals(2, nonZeroTiles4)

        val engine5 = GameEngine(rows = 5, cols = 5)
        assertEquals(5, engine5.state.rows)
        assertEquals(5, engine5.state.cols)
        assertEquals(5, engine5.state.grid.size)
        assertEquals(5, engine5.state.grid[0].size)
        val nonZeroTiles5 = engine5.state.grid.flatten().count { it != 0 }
        assertEquals(2, nonZeroTiles5)
    }

    @Test
    fun testProgressiveTargetProgression() {
        val engine = GameEngine(rows = 4, cols = 4)
        assertEquals(32, engine.state.targetGoal)
        assertEquals(32, engine.state.targetTile)

        // Reaching 32 tile bumps target to 64
        val grid32 = listOf(
            listOf(16, 16, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid32)
        engine.move(Direction.LEFT)
        assertEquals(64, engine.state.targetGoal)
        assertEquals(64, engine.state.targetTile)

        // Reaching 64 tile bumps target to 128
        val grid64 = listOf(
            listOf(32, 32, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid64)
        engine.move(Direction.LEFT)
        assertEquals(128, engine.state.targetGoal)
        assertEquals(128, engine.state.targetTile)

        // Reaching 128 tile bumps target to 256
        val grid128 = listOf(
            listOf(64, 64, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid128)
        engine.move(Direction.LEFT)
        assertEquals(256, engine.state.targetGoal)
        assertEquals(256, engine.state.targetTile)

        // Reaching 256 tile bumps target to 512
        val grid256 = listOf(
            listOf(128, 128, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid256)
        engine.move(Direction.LEFT)
        assertEquals(512, engine.state.targetGoal)

        // Reaching 512 tile bumps target to 1024
        val grid512 = listOf(
            listOf(256, 256, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid512)
        engine.move(Direction.LEFT)
        assertEquals(1024, engine.state.targetGoal)

        // Reaching 1024 tile bumps target to 2048
        val grid1024 = listOf(
            listOf(512, 512, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid1024)
        engine.move(Direction.LEFT)
        assertEquals(2048, engine.state.targetGoal)

        // Reaching 2048 tile bumps target to 4096
        val grid2048 = listOf(
            listOf(1024, 1024, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid2048)
        engine.move(Direction.LEFT)
        assertEquals(4096, engine.state.targetGoal)
    }

    @Test
    fun testSingleMerge() {
        val engine = GameEngine(rows = 4, cols = 4)
        val grid = listOf(
            listOf(2, 2, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid)

        val result = engine.move(Direction.LEFT)

        assertTrue(result.hasChanged)
        assertEquals(4, result.scoreGained)
        assertEquals(4, result.maxMergedValue)
        assertEquals(1, result.mergeCount)
        assertEquals(4, engine.state.currentScore)
        assertEquals(4, engine.state.grid[0][0])
    }

    @Test
    fun testTripleTileMerge() {
        val engine = GameEngine(rows = 4, cols = 4)
        val grid = listOf(
            listOf(2, 2, 2, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid)

        val result = engine.move(Direction.LEFT)

        assertTrue(result.hasChanged)
        assertEquals(4, result.scoreGained)
        assertEquals(4, result.maxMergedValue)
        assertEquals(1, result.mergeCount)
        assertEquals(4, engine.state.grid[0][0])
        assertEquals(2, engine.state.grid[0][1])
    }

    @Test
    fun testFourEqualTilesMerge() {
        val engine = GameEngine(rows = 4, cols = 4)
        val grid = listOf(
            listOf(2, 2, 2, 2),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid)

        val result = engine.move(Direction.LEFT)

        assertTrue(result.hasChanged)
        assertEquals(8, result.scoreGained)
        assertEquals(4, result.maxMergedValue)
        assertEquals(2, result.mergeCount)
        assertEquals(4, engine.state.grid[0][0])
        assertEquals(4, engine.state.grid[0][1])
    }

    @Test
    fun testTwoDoubleMergesInOneRow() {
        val engine = GameEngine(rows = 4, cols = 4)
        val grid = listOf(
            listOf(2, 2, 4, 4),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid)

        val result = engine.move(Direction.LEFT)

        assertTrue(result.hasChanged)
        assertEquals(12, result.scoreGained)
        assertEquals(8, result.maxMergedValue)
        assertEquals(2, result.mergeCount)
        assertEquals(4, engine.state.grid[0][0])
        assertEquals(8, engine.state.grid[0][1])
    }

    @Test
    fun testMoveRight() {
        val engine = GameEngine(rows = 4, cols = 4)
        val grid = listOf(
            listOf(2, 0, 2, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid)

        val result = engine.move(Direction.RIGHT)

        assertTrue(result.hasChanged)
        assertEquals(4, result.scoreGained)
        assertEquals(4, result.maxMergedValue)
        assertEquals(1, result.mergeCount)
        assertEquals(4, engine.state.grid[0][3])
    }

    @Test
    fun testMoveUp() {
        val engine = GameEngine(rows = 4, cols = 4)
        val grid = listOf(
            listOf(2, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(2, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid)

        val result = engine.move(Direction.UP)

        assertTrue(result.hasChanged)
        assertEquals(4, result.scoreGained)
        assertEquals(4, result.maxMergedValue)
        assertEquals(1, result.mergeCount)
        assertEquals(4, engine.state.grid[0][0])
    }

    @Test
    fun testMoveDown() {
        val engine = GameEngine(rows = 4, cols = 4)
        val grid = listOf(
            listOf(2, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(2, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid)

        val result = engine.move(Direction.DOWN)

        assertTrue(result.hasChanged)
        assertEquals(4, result.scoreGained)
        assertEquals(4, result.maxMergedValue)
        assertEquals(1, result.mergeCount)
        assertEquals(4, engine.state.grid[3][0])
    }

    @Test
    fun testInvalidNoOpMove() {
        val engine = GameEngine(rows = 4, cols = 4)
        val grid = listOf(
            listOf(2, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid)

        val initialMoveCount = engine.state.moveCount
        val initialScore = engine.state.currentScore

        val result = engine.move(Direction.LEFT)

        assertFalse(result.hasChanged)
        assertEquals(0, result.scoreGained)
        assertEquals(0, result.maxMergedValue)
        assertEquals(0, result.mergeCount)
        assertEquals(initialMoveCount, engine.state.moveCount)
        assertEquals(initialScore, engine.state.currentScore)

        val nonZeroCount = engine.state.grid.flatten().count { it != 0 }
        assertEquals(1, nonZeroCount)
    }

    @Test
    fun testScoreCalculationAndBestScore() {
        val engine = GameEngine(rows = 4, cols = 4, initialBestScore = 50)
        assertEquals(50, engine.state.bestScore)

        val grid = listOf(
            listOf(2, 2, 4, 4),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid, score = 40)
        assertEquals(50, engine.state.bestScore)

        engine.move(Direction.LEFT) // Score gained = 12 -> total = 52
        assertEquals(52, engine.state.currentScore)
        assertEquals(52, engine.state.bestScore)
    }

    @Test
    fun testWinDetectionInMissionMode() {
        val mission = Mission(
            id = 23,
            title = "Grand Master",
            description = "Reach 2048 Tile",
            type = MissionType.TILE_REACH,
            targetValue = 2048
        )
        val engine = GameEngine(rows = 4, cols = 4, activeMission = mission)
        val grid = listOf(
            listOf(1024, 1024, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid, activeMission = mission)

        val result = engine.move(Direction.LEFT)

        assertTrue(result.hasChanged)
        assertEquals(GameStatus.WON, engine.state.status)
        assertEquals(GameStatus.WON, result.newState.status)
        assertTrue(engine.state.isMissionCompleted)
    }

    @Test
    fun testGameOverDetection() {
        val engine = GameEngine(rows = 4, cols = 4)
        val grid = listOf(
            listOf(2, 4, 8, 16),
            listOf(32, 64, 128, 256),
            listOf(8, 4, 2, 8),
            listOf(16, 32, 64, 0)
        )
        engine.setGrid(grid)

        val result = engine.move(Direction.RIGHT)

        assertTrue(result.hasChanged)
        assertEquals(GameStatus.GAME_OVER, engine.state.status)
    }

    @Test
    fun testUndoFunctionality() {
        val engine = GameEngine(rows = 4, cols = 4, initialBestScore = 100)
        val grid = listOf(
            listOf(2, 2, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid, score = 0, moveCount = 0)

        // Make move
        engine.move(Direction.LEFT)
        assertEquals(4, engine.state.currentScore)
        assertEquals(1, engine.state.moveCount)
        assertTrue(engine.state.canUndo)

        // Undo move
        val undoSuccess = engine.undo()
        assertTrue(undoSuccess)
        assertEquals(0, engine.state.currentScore)
        assertEquals(0, engine.state.moveCount)
        assertFalse(engine.state.canUndo)
        assertEquals(grid, engine.state.grid)
        assertEquals(100, engine.state.bestScore)

        val secondUndoSuccess = engine.undo()
        assertFalse(secondUndoSuccess)
    }

    @Test
    fun testRestartFunctionality() {
        val engine = GameEngine(rows = 4, cols = 4, initialBestScore = 20)
        val grid = listOf(
            listOf(2, 2, 4, 4),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid, score = 100)
        assertEquals(100, engine.state.bestScore)

        // Restart on 4x4
        engine.restart(4, 4)
        assertEquals(4, engine.state.rows)
        assertEquals(4, engine.state.cols)
        assertEquals(0, engine.state.currentScore)
        assertEquals(0, engine.state.moveCount)
        assertEquals(GameStatus.PLAYING, engine.state.status)
        assertFalse(engine.state.canUndo)
        assertFalse(engine.state.isContinued)
        assertEquals(100, engine.state.bestScore)
        assertEquals(2, engine.state.grid.flatten().count { it != 0 })

        // Restart on 5x5
        engine.restart(5, 5)
        assertEquals(5, engine.state.rows)
        assertEquals(5, engine.state.cols)
        assertEquals(5, engine.state.grid.size)
        assertEquals(100, engine.state.bestScore)
    }

    @Test
    fun testPauseAndResume() {
        val engine = GameEngine(rows = 4, cols = 4)
        assertEquals(GameStatus.PLAYING, engine.state.status)

        engine.pause()
        assertEquals(GameStatus.PAUSED, engine.state.status)

        val grid = listOf(
            listOf(2, 2, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid, status = GameStatus.PAUSED)
        val result = engine.move(Direction.LEFT)
        assertFalse(result.hasChanged)

        engine.resume()
        assertEquals(GameStatus.PLAYING, engine.state.status)
    }

    @Test
    fun testUseHammer() {
        val engine = GameEngine(rows = 4, cols = 4)
        val grid = listOf(
            listOf(2, 2, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid)

        val success = engine.useHammer(0, 0)
        assertTrue(success)
        assertEquals(0, engine.state.grid[0][0])
        assertEquals(2, engine.state.hammerCount)
        assertTrue(engine.state.canUndo)
    }

    @Test
    fun testUseSwap() {
        val engine = GameEngine(rows = 4, cols = 4)
        val grid = listOf(
            listOf(2, 4, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid)

        val success = engine.useSwap(0, 0, 0, 1)
        assertTrue(success)
        assertEquals(4, engine.state.grid[0][0])
        assertEquals(2, engine.state.grid[0][1])
        assertEquals(2, engine.state.swapCount)
        assertTrue(engine.state.canUndo)
    }

    // --- Mission System Tests ---

    @Test
    fun testTileReachMissionCompletion() {
        val mission = Mission(
            id = 1,
            title = "Quick Start",
            description = "Create a 64 Tile",
            type = MissionType.TILE_REACH,
            targetValue = 64
        )
        val engine = GameEngine(rows = 4, cols = 4, activeMission = mission)
        assertFalse(engine.state.isMissionCompleted)

        val grid = listOf(
            listOf(32, 32, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid, activeMission = mission)

        val result = engine.move(Direction.LEFT)
        assertTrue(result.hasChanged)
        assertTrue(engine.state.isMissionCompleted)
        assertFalse(engine.state.isMissionFailed)
    }

    @Test
    fun testMoveLimitTileMissionCompletion() {
        val mission = Mission(
            id = 2,
            title = "Speedrunner",
            description = "Create a 128 Tile in 30 moves or less",
            type = MissionType.MOVE_LIMIT_TILE,
            targetValue = 128,
            moveLimit = 30
        )
        val engine = GameEngine(rows = 4, cols = 4, activeMission = mission)

        val grid = listOf(
            listOf(64, 64, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid, moveCount = 10, activeMission = mission)

        val result = engine.move(Direction.LEFT)
        assertTrue(result.hasChanged)
        assertEquals(11, engine.state.moveCount)
        assertTrue(engine.state.isMissionCompleted)
        assertFalse(engine.state.isMissionFailed)
    }

    @Test
    fun testMoveLimitTileMissionFailure() {
        val mission = Mission(
            id = 2,
            title = "Speedrunner",
            description = "Create a 128 Tile in 2 moves or less",
            type = MissionType.MOVE_LIMIT_TILE,
            targetValue = 128,
            moveLimit = 2
        )
        val engine = GameEngine(rows = 4, cols = 4, activeMission = mission)

        val grid = listOf(
            listOf(0, 0, 2, 4),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid, moveCount = 1, activeMission = mission)

        // Move #2 (reaches move limit of 2 moves without reaching 128 tile)
        val result = engine.move(Direction.LEFT)
        assertTrue(result.hasChanged)
        assertEquals(2, engine.state.moveCount)
        assertFalse(engine.state.isMissionCompleted)
        assertTrue(engine.state.isMissionFailed)
    }

    @Test
    fun testScoreReachMissionCompletion() {
        val mission = Mission(
            id = 5,
            title = "Score Streak",
            description = "Score 3,000 Points",
            type = MissionType.SCORE_REACH,
            targetValue = 3000
        )
        val engine = GameEngine(rows = 4, cols = 4, activeMission = mission)

        val grid = listOf(
            listOf(512, 512, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid, score = 2500, activeMission = mission)

        val result = engine.move(Direction.LEFT) // gains 1024 score -> total score 3524
        assertTrue(result.hasChanged)
        assertTrue(engine.state.currentScore >= 3000)
        assertTrue(engine.state.isMissionCompleted)
        assertFalse(engine.state.isMissionFailed)
    }

    @Test
    fun testComboMergeMissionCompletion() {
        val mission = Mission(
            id = 3,
            title = "Double Trouble",
            description = "Perform a Double Merge in 1 move",
            type = MissionType.COMBO_MERGE,
            targetValue = 2
        )
        val engine = GameEngine(rows = 4, cols = 4, activeMission = mission)

        val grid = listOf(
            listOf(2, 2, 4, 4),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
        engine.setGrid(grid, activeMission = mission)

        val result = engine.move(Direction.LEFT) // mergeCount = 2
        assertTrue(result.hasChanged)
        assertEquals(2, result.mergeCount)
        assertTrue(engine.state.isMissionCompleted)
        assertFalse(engine.state.isMissionFailed)
    }
}
