package com.game.a2048

import com.game.a2048.hex.HexCell
import com.game.a2048.hex.HexDirection
import com.game.a2048.hex.HexEngine
import com.game.a2048.hex.HexGrid
import com.game.a2048.hex.HexPiece
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HexEngineTest {

    @Test
    fun testBoardInitialization() {
        val engine = HexEngine()
        assertEquals(19, engine.state.grid.size)

        val nonZeroCount = engine.state.grid.values.count { it != 0 }
        assertEquals(2, nonZeroCount)

        assertEquals(3, engine.upcomingPieces.size)
        assertEquals(0, engine.state.currentScore)
        assertEquals(0, engine.state.moveCount)
        assertEquals(GameStatus.PLAYING, engine.state.status)
        assertFalse(engine.state.canUndo)
        assertFalse(engine.state.isContinued)
        assertEquals(3, engine.state.hammerCount)
        assertEquals(3, engine.state.swapCount)
        assertEquals(32, engine.state.targetGoal)
    }

    @Test
    fun testGridRadiusInitializations() {
        val engineRadius2 = HexEngine(radius = 2)
        assertEquals(19, engineRadius2.state.grid.size)

        val engineRadius3 = HexEngine(radius = 3)
        assertEquals(37, engineRadius3.state.grid.size)

        val engineRadius4 = HexEngine(radius = 4)
        assertEquals(61, engineRadius4.state.grid.size)
    }

    @Test
    fun testPiecePlacement() {
        val engine = HexEngine()
        engine.setGrid(
            gridMap = emptyMap(),
            score = 0,
            upcomingPieces = listOf(HexPiece.createSingle(2), HexPiece.createSingle(4), HexPiece.createSingle(8))
        )

        assertEquals(3, engine.upcomingPieces.size)
        val targetCell = HexCell(0, 0)
        val success = engine.placePiece(0, targetCell)

        assertTrue(success)
        assertEquals(2, engine.state.grid[targetCell])
        assertEquals(1, engine.state.moveCount)
        assertTrue(engine.state.canUndo)
    }

    @Test
    fun testConnectedDoubleHexagonPiecePlacement() {
        val engine = HexEngine()
        val pairPiece = HexPiece.createPair(4, 8)
        engine.setGrid(
            gridMap = emptyMap(),
            upcomingPieces = listOf(pairPiece)
        )

        val targetCell = HexCell(0, 0)
        val success = engine.placePiece(0, targetCell)

        assertTrue(success)
        assertEquals(4, engine.state.grid[targetCell])
        val neighborCell = targetCell.neighbors().first { HexGrid.contains(it) }
        assertEquals(8, engine.state.grid[neighborCell])
        val nonZeroCount = engine.state.grid.values.count { it != 0 }
        assertEquals(2, nonZeroCount)
    }

    @Test
    fun testConnectedTripleHexagonPiecePlacement() {
        val engine = HexEngine()
        val triplePiece = HexPiece.createTriple(2, 4, 8)
        engine.setGrid(
            gridMap = emptyMap(),
            upcomingPieces = listOf(triplePiece)
        )

        val targetCell = HexCell(0, 0)
        val placementCells = engine.findPlacementCells(triplePiece, targetCell)
        assertNotNull(placementCells)
        assertEquals(3, placementCells!!.size)
        assertEquals(targetCell, placementCells[0])

        val success = engine.placePiece(0, targetCell)
        assertTrue(success)
        assertEquals(2, engine.state.grid[placementCells[0]])
        assertEquals(4, engine.state.grid[placementCells[1]])
        assertEquals(8, engine.state.grid[placementCells[2]])

        val nonZeroCount = engine.state.grid.values.count { it != 0 }
        assertEquals(3, nonZeroCount)
    }

    @Test
    fun testTriplePieceGroupMergeOnPlacement() {
        val engine = HexEngine()
        val tripleSameValuePiece = HexPiece.createTriple(2, 2, 2)
        engine.setGrid(
            gridMap = emptyMap(),
            upcomingPieces = listOf(tripleSameValuePiece)
        )

        val targetCell = HexCell(0, 0)
        val success = engine.placePiece(0, targetCell)
        assertTrue(success)

        // The 3 placed 2s should immediately merge into a single 4 tile
        val lastRes = engine.lastPieceResult
        assertNotNull(lastRes)
        assertEquals(4, lastRes!!.scoreGained)
        assertEquals(1, lastRes.mergeCount)
        assertEquals(4, engine.state.grid[targetCell])

        val nonZeroCount = engine.state.grid.values.count { it != 0 }
        assertEquals(1, nonZeroCount)
    }

    @Test
    fun testHexHammerUsageAndUndo() {
        val engine = HexEngine()
        val targetCell = HexCell(0, 0)
        engine.setGrid(
            gridMap = mapOf(targetCell to 16),
            hammerCount = 3
        )

        assertEquals(3, engine.state.hammerCount)
        assertEquals(16, engine.state.grid[targetCell])

        val success = engine.useHammer(targetCell)

        assertTrue(success)
        assertEquals(0, engine.state.grid[targetCell])
        assertEquals(2, engine.state.hammerCount)
        assertTrue(engine.state.canUndo)

        val undoSuccess = engine.undo()
        assertTrue(undoSuccess)
        assertEquals(16, engine.state.grid[targetCell])
        assertEquals(3, engine.state.hammerCount)
        assertFalse(engine.state.canUndo)
    }

    @Test
    fun testHexSwapUsageAndUndo() {
        val engine = HexEngine()
        val cell1 = HexCell(0, 0)
        val cell2 = HexCell(1, 0)
        engine.setGrid(
            gridMap = mapOf(
                cell1 to 4,
                cell2 to 32
            ),
            swapCount = 3
        )

        assertEquals(3, engine.state.swapCount)

        val success = engine.useSwap(cell1, cell2)

        assertTrue(success)
        assertEquals(32, engine.state.grid[cell1])
        assertEquals(4, engine.state.grid[cell2])
        assertEquals(2, engine.state.swapCount)
        assertTrue(engine.state.canUndo)

        val undoSuccess = engine.undo()
        assertTrue(undoSuccess)
        assertEquals(4, engine.state.grid[cell1])
        assertEquals(32, engine.state.grid[cell2])
        assertEquals(3, engine.state.swapCount)
        assertFalse(engine.state.canUndo)
    }

    @Test
    fun testProgressiveTargetMilestoneBumps() {
        val engine = HexEngine()
        engine.setGrid(
            gridMap = mapOf(
                HexCell(-1, 0) to 16,
                HexCell(0, 1) to 16
            ),
            upcomingPieces = listOf(HexPiece.createSingle(16)),
            targetGoal = 32
        )

        assertEquals(32, engine.state.targetGoal)

        val targetCell = HexCell(0, 0)
        val success = engine.placePiece(0, targetCell)

        assertTrue(success)
        assertEquals(32, engine.state.grid[targetCell])
        assertEquals(64, engine.state.targetGoal)
    }

    @Test
    fun testPrimaryThreeTileGroupMerge() {
        val engine = HexEngine()
        engine.setGrid(
            gridMap = mapOf(
                HexCell(-1, 0) to 2,
                HexCell(0, 1) to 2
            ),
            score = 0,
            upcomingPieces = listOf(HexPiece.createSingle(2), HexPiece.createSingle(4), HexPiece.createSingle(8))
        )

        val targetCell = HexCell(0, 0)
        val success = engine.placePiece(0, targetCell)

        assertTrue(success)
        assertEquals(4, engine.state.grid[targetCell])
        assertEquals(0, engine.state.grid[HexCell(-1, 0)])
        assertEquals(0, engine.state.grid[HexCell(0, 1)])
        assertEquals(4, engine.state.currentScore)
        val lastRes = engine.lastPieceResult
        assertTrue(lastRes != null)
        assertEquals(4, lastRes!!.scoreGained)
        assertEquals(1, lastRes.mergeCount)
        assertEquals(1, lastRes.chainCount)
    }

    @Test
    fun testChainReactionComboMerge() {
        val engine = HexEngine()
        engine.setGrid(
            gridMap = mapOf(
                HexCell(-1, 0) to 2,
                HexCell(0, 1) to 2,
                HexCell(1, 0) to 4
            ),
            score = 0,
            upcomingPieces = listOf(HexPiece.createSingle(2), HexPiece.createSingle(4), HexPiece.createSingle(8))
        )

        val targetCell = HexCell(0, 0)
        val success = engine.placePiece(0, targetCell)

        assertTrue(success)
        assertEquals(8, engine.state.grid[targetCell])
        assertEquals(0, engine.state.grid[HexCell(-1, 0)])
        assertEquals(0, engine.state.grid[HexCell(0, 1)])
        assertEquals(0, engine.state.grid[HexCell(1, 0)])
        assertEquals(12, engine.state.currentScore)

        val lastRes = engine.lastPieceResult
        assertTrue(lastRes != null)
        assertEquals(12, lastRes!!.scoreGained)
        assertEquals(2, lastRes.mergeCount)
        assertEquals(2, lastRes.chainCount)
        assertEquals(8, lastRes.maxMergedValue)
    }

    @Test
    fun testPieceTrayGenerationAndOneStepUndo() {
        val engine = HexEngine()
        val initialTray = listOf(HexPiece.createSingle(2), HexPiece.createSingle(4), HexPiece.createSingle(8))
        engine.setGrid(
            gridMap = mapOf(HexCell(0, 0) to 0),
            score = 10,
            upcomingPieces = initialTray
        )

        val targetCell = HexCell(0, 0)
        val success = engine.placePiece(0, targetCell)
        assertTrue(success)
        assertEquals(2, engine.state.grid[targetCell])
        assertTrue(engine.state.canUndo)

        val undoSuccess = engine.undo()
        assertTrue(undoSuccess)
        assertEquals(0, engine.state.grid[targetCell])
        assertEquals(10, engine.state.currentScore)
        assertEquals(initialTray, engine.upcomingPieces)
        assertFalse(engine.state.canUndo)
    }

    @Test
    fun testSlidingInAllSixDirections() {
        // Test EAST
        val engineEast = HexEngine()
        engineEast.setGrid(mapOf(HexCell(0, 0) to 2))
        val resEast = engineEast.move(HexDirection.EAST)
        assertTrue(resEast.hasChanged)
        assertEquals(2, engineEast.state.grid[HexCell(2, 0)])

        // Test WEST
        val engineWest = HexEngine()
        engineWest.setGrid(mapOf(HexCell(0, 0) to 2))
        val resWest = engineWest.move(HexDirection.WEST)
        assertTrue(resWest.hasChanged)
        assertEquals(2, engineWest.state.grid[HexCell(-2, 0)])

        // Test NORTH_EAST
        val engineNE = HexEngine()
        engineNE.setGrid(mapOf(HexCell(0, 0) to 2))
        val resNE = engineNE.move(HexDirection.NORTH_EAST)
        assertTrue(resNE.hasChanged)
        assertEquals(2, engineNE.state.grid[HexCell(2, -2)])

        // Test SOUTH_WEST
        val engineSW = HexEngine()
        engineSW.setGrid(mapOf(HexCell(0, 0) to 2))
        val resSW = engineSW.move(HexDirection.SOUTH_WEST)
        assertTrue(resSW.hasChanged)
        assertEquals(2, engineSW.state.grid[HexCell(-2, 2)])

        // Test NORTH_WEST
        val engineNW = HexEngine()
        engineNW.setGrid(mapOf(HexCell(0, 0) to 2))
        val resNW = engineNW.move(HexDirection.NORTH_WEST)
        assertTrue(resNW.hasChanged)
        assertEquals(2, engineNW.state.grid[HexCell(0, -2)])

        // Test SOUTH_EAST
        val engineSE = HexEngine()
        engineSE.setGrid(mapOf(HexCell(0, 0) to 2))
        val resSE = engineSE.move(HexDirection.SOUTH_EAST)
        assertTrue(resSE.hasChanged)
        assertEquals(2, engineSE.state.grid[HexCell(0, 2)])
    }

    @Test
    fun testHexMergingLogicAndScoreUpdates() {
        val engine = HexEngine()
        engine.setGrid(
            mapOf(
                HexCell(-1, 0) to 2,
                HexCell(0, 0) to 2
            )
        )

        val result = engine.move(HexDirection.EAST)

        assertTrue(result.hasChanged)
        assertEquals(4, result.scoreGained)
        assertEquals(4, result.maxMergedValue)
        assertEquals(1, result.mergeCount)
        assertEquals(4, engine.state.currentScore)
        assertEquals(4, engine.state.grid[HexCell(2, 0)])

        // Triple tile merge along EAST line
        val engineTriple = HexEngine()
        engineTriple.setGrid(
            mapOf(
                HexCell(-2, 0) to 2,
                HexCell(-1, 0) to 2,
                HexCell(0, 0) to 2
            )
        )

        val tripleResult = engineTriple.move(HexDirection.EAST)

        assertTrue(tripleResult.hasChanged)
        assertEquals(4, tripleResult.scoreGained)
        assertEquals(4, tripleResult.maxMergedValue)
        assertEquals(1, tripleResult.mergeCount)
        assertEquals(4, engineTriple.state.grid[HexCell(2, 0)])
        assertEquals(2, engineTriple.state.grid[HexCell(1, 0)])
    }

    @Test
    fun testHexGameOverDetection() {
        val engine = HexEngine()
        val fullDistinctGrid = HexGrid.ALL_CELLS.mapIndexed { index, cell ->
            cell to (2 shl index)
        }.toMap()

        engine.setGrid(fullDistinctGrid)

        val result = engine.move(HexDirection.EAST)
        assertFalse(result.hasChanged)

        engine.setGrid(fullDistinctGrid, status = GameStatus.PLAYING)
        for (dir in HexDirection.entries) {
            engine.move(dir)
        }
        assertEquals(GameStatus.GAME_OVER, engine.state.status)
    }

    @Test
    fun testUndoAndRestart() {
        val engine = HexEngine(initialBestScore = 100)
        engine.setGrid(
            mapOf(
                HexCell(-1, 0) to 2,
                HexCell(0, 0) to 2
            ),
            score = 0,
            moveCount = 0
        )

        val moveRes = engine.move(HexDirection.EAST)
        assertTrue(moveRes.hasChanged)
        assertEquals(4, engine.state.currentScore)
        assertEquals(1, engine.state.moveCount)
        assertTrue(engine.state.canUndo)

        val undoSuccess = engine.undo()
        assertTrue(undoSuccess)
        assertEquals(0, engine.state.currentScore)
        assertEquals(0, engine.state.moveCount)
        assertFalse(engine.state.canUndo)
        assertEquals(2, engine.state.grid[HexCell(-1, 0)])
        assertEquals(2, engine.state.grid[HexCell(0, 0)])
        assertEquals(100, engine.state.bestScore)

        engine.restart()
        assertEquals(0, engine.state.currentScore)
        assertEquals(0, engine.state.moveCount)
        assertEquals(GameStatus.PLAYING, engine.state.status)
        assertFalse(engine.state.canUndo)
        assertEquals(100, engine.state.bestScore)
        assertEquals(2, engine.state.grid.values.count { it != 0 })
    }
}
