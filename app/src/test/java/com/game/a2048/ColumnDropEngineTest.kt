package com.game.a2048

import com.game.a2048.column.ColumnDropEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ColumnDropEngineTest {

    @Test
    fun testInitialization() {
        val engine = ColumnDropEngine()
        assertEquals(5, engine.state.grid.size)
        assertTrue(engine.state.grid.all { it.isEmpty() })
        assertEquals(GameStatus.PLAYING, engine.state.status)
        assertEquals(3, engine.state.hammerUses)
        assertEquals(3, engine.state.switchUses)
        assertEquals(3, engine.state.undoUses)
    }

    @Test
    fun testShootTileAndCeilingCollision() {
        val engine = ColumnDropEngine()
        engine.setGrid(
            grid = List(5) { emptyList() },
            score = 0,
            currentTile = 2,
            nextTile = 4,
            moveCount = 0,
            status = GameStatus.PLAYING,
            hammerUses = 3,
            switchUses = 3,
            undoUses = 3,
            targetTile = 32,
            isContinued = false
        )

        // Shoot into empty column 2 -> goes to Row 0 (index 0)
        val success = engine.shootTile(2)
        assertTrue(success)
        assertEquals(1, engine.state.grid[2].size)
        assertEquals(2, engine.state.grid[2][0])

        // Shoot again into column 2 -> goes underneath at Row 1 (index 1)
        val success2 = engine.shootTile(2)
        assertTrue(success2)
        assertEquals(2, engine.state.grid[2].size)
        assertEquals(4, engine.state.grid[2][1])
    }

    @Test
    fun testColumnFullGameOverTrigger() {
        val engine = ColumnDropEngine()
        val fullColumn = listOf(2, 4, 8, 16, 32, 64, 128, 256) // 8 rows (0 to 7)
        engine.setGrid(
            grid = listOf(
                emptyList(),
                emptyList(),
                fullColumn,
                emptyList(),
                emptyList()
            ),
            score = 100,
            currentTile = 2,
            nextTile = 4,
            moveCount = 10,
            status = GameStatus.PLAYING,
            hammerUses = 3,
            switchUses = 3,
            undoUses = 3,
            targetTile = 2048,
            isContinued = false
        )

        // Shooting into full column 2 should trigger Game Over and return false
        val success = engine.shootTile(2)
        assertFalse(success)
        assertEquals(GameStatus.GAME_OVER, engine.state.status)
    }

    @Test
    fun testVerticalCascadeMergeAndUpwardCollapse() {
        val engine = ColumnDropEngine()
        engine.setGrid(
            grid = listOf(
                emptyList(),
                emptyList(),
                listOf(2),
                emptyList(),
                emptyList()
            ),
            score = 0,
            currentTile = 2,
            nextTile = 4,
            moveCount = 0,
            status = GameStatus.PLAYING,
            hammerUses = 3,
            switchUses = 3,
            undoUses = 3,
            targetTile = 32,
            isContinued = false
        )

        val success = engine.shootTile(2)
        assertTrue(success)
        assertEquals(4, engine.state.score)
        assertEquals(1, engine.state.grid[2].size)
        assertEquals(4, engine.state.grid[2][0])
    }

    @Test
    fun testHorizontalCascadeMergeAndUpwardCollapse() {
        val engine = ColumnDropEngine()
        engine.setGrid(
            grid = listOf(
                listOf(4),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList()
            ),
            score = 0,
            currentTile = 4,
            nextTile = 2,
            moveCount = 0,
            status = GameStatus.PLAYING,
            hammerUses = 3,
            switchUses = 3,
            undoUses = 3,
            targetTile = 32,
            isContinued = false
        )

        val success = engine.shootTile(1)
        assertTrue(success)
        assertEquals(8, engine.state.score)
        assertEquals(8, engine.state.grid[0][0])
        assertTrue(engine.state.grid[1].isEmpty())
    }

    @Test
    fun testHammerDeletionAndUpwardCollapse() {
        val engine = ColumnDropEngine()
        engine.setGrid(
            grid = listOf(
                emptyList(),
                emptyList(),
                listOf(2, 4, 8),
                emptyList(),
                emptyList()
            ),
            score = 0,
            currentTile = 2,
            nextTile = 4,
            moveCount = 0,
            status = GameStatus.PLAYING,
            hammerUses = 3,
            switchUses = 3,
            undoUses = 3,
            targetTile = 32,
            isContinued = false
        )

        // Remove index 0 (value 2), 4 and 8 should shift upward to indices 0 and 1
        val success = engine.useHammer(2, 0)
        assertTrue(success)
        assertEquals(2, engine.state.grid[2].size)
        assertEquals(4, engine.state.grid[2][0])
        assertEquals(8, engine.state.grid[2][1])
        assertEquals(2, engine.state.hammerUses)
    }

    @Test
    fun testSwapTool() {
        val engine = ColumnDropEngine()
        engine.setGrid(
            grid = listOf(
                listOf(2),
                listOf(4),
                emptyList(),
                emptyList(),
                emptyList()
            ),
            score = 0,
            currentTile = 2,
            nextTile = 4,
            moveCount = 0,
            status = GameStatus.PLAYING,
            hammerUses = 3,
            switchUses = 3,
            undoUses = 3,
            targetTile = 32,
            isContinued = false
        )

        assertTrue(engine.selectTileForSwap(0, 0))
        assertTrue(engine.selectTileForSwap(1, 0))
        assertEquals(4, engine.state.grid[0][0])
        assertEquals(2, engine.state.grid[1][0])
        assertEquals(2, engine.state.switchUses)
    }

    @Test
    fun testUndo() {
        val engine = ColumnDropEngine()
        engine.setGrid(
            grid = List(5) { emptyList() },
            score = 0,
            currentTile = 2,
            nextTile = 4,
            moveCount = 0,
            status = GameStatus.PLAYING,
            hammerUses = 3,
            switchUses = 3,
            undoUses = 3,
            targetTile = 32,
            isContinued = false
        )

        engine.shootTile(0)
        assertEquals(1, engine.state.grid[0].size)

        val undoSuccess = engine.undo()
        assertTrue(undoSuccess)
        assertTrue(engine.state.grid[0].isEmpty())
    }

    @Test
    fun testGameOverDetection() {
        val engine = ColumnDropEngine()
        val fullGrid = List(5) { c ->
            List(8) { r -> (c * 8 + r) * 2 + 1 }
        }
        engine.setGrid(
            grid = fullGrid,
            score = 100,
            currentTile = 2,
            nextTile = 4,
            moveCount = 40,
            status = GameStatus.PLAYING,
            hammerUses = 3,
            switchUses = 3,
            undoUses = 3,
            targetTile = 2048,
            isContinued = false
        )

        assertEquals(GameStatus.GAME_OVER, engine.state.status)
    }
}
