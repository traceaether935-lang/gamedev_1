package com.game.a2048.column

import com.game.a2048.GameStatus
import com.aethertrace.numberdrop2048hexa.ui.model.ActiveTool
import kotlin.random.Random

class ColumnDropEngine(
    initialBestScore: Int = 0
) {
    private var _state = ColumnDropState(bestScore = initialBestScore)
    val state: ColumnDropState get() = _state

    private var undoState: ColumnDropState? = null

    init {
        restart()
    }

    private fun calculateProgressiveTarget(grid: List<List<Int>>, currentTarget: Int): Int {
        val maxTile = grid.flatten().maxOrNull() ?: 2
        var target = maxOf(32, currentTarget)
        while (maxTile >= target) {
            target *= 2
        }
        return target
    }

    private fun generateRandomTile(maxTile: Int): Int {
        val possibleValues = mutableListOf(2, 4)
        if (maxTile >= 16) possibleValues.add(8)
        if (maxTile >= 64) possibleValues.add(16)
        if (maxTile >= 256) possibleValues.add(32)
        if (maxTile >= 1024) possibleValues.add(64)
        if (maxTile >= 4096) possibleValues.add(128)
        return possibleValues.random()
    }

    fun restart() {
        val initialGrid = List(5) { emptyList<Int>() }
        val maxTile = 2
        val cur = generateRandomTile(maxTile)
        val nxt = generateRandomTile(maxTile)
        _state = ColumnDropState(
            grid = initialGrid,
            currentTile = cur,
            nextTile = nxt,
            score = 0,
            bestScore = _state.bestScore,
            moveCount = 0,
            status = GameStatus.PLAYING,
            hammerUses = 3,
            switchUses = 3,
            undoUses = 3,
            activeTool = ActiveTool.NONE,
            firstSelectedTile = null,
            aimingColumn = null,
            targetTile = 32,
            canUndo = false,
            isContinued = false
        )
        undoState = null
    }

    fun updateBestScore(bestScore: Int) {
        val newBest = maxOf(bestScore, _state.bestScore, _state.score)
        if (newBest != _state.bestScore) {
            _state = _state.copy(bestScore = newBest)
        }
    }

    fun setGrid(
        grid: List<List<Int>>,
        score: Int,
        currentTile: Int,
        nextTile: Int,
        moveCount: Int,
        status: GameStatus,
        hammerUses: Int,
        switchUses: Int,
        undoUses: Int,
        targetTile: Int,
        isContinued: Boolean
    ) {
        val normalizedGrid = List(5) { c ->
            val colList = if (c < grid.size) grid[c] else emptyList()
            colList.take(8)
        }
        val target = calculateProgressiveTarget(normalizedGrid, targetTile)
        val best = maxOf(_state.bestScore, score)
        val isOver = checkGameOver(normalizedGrid) && status == GameStatus.PLAYING
        val finalStatus = if (isOver) GameStatus.GAME_OVER else status

        _state = ColumnDropState(
            grid = normalizedGrid,
            currentTile = currentTile,
            nextTile = nextTile,
            score = score,
            bestScore = best,
            moveCount = moveCount,
            status = finalStatus,
            hammerUses = hammerUses,
            switchUses = switchUses,
            undoUses = undoUses,
            activeTool = ActiveTool.NONE,
            firstSelectedTile = null,
            aimingColumn = null,
            targetTile = target,
            canUndo = false,
            isContinued = isContinued
        )
        undoState = null
    }

    fun setAimingColumn(col: Int?) {
        _state = _state.copy(aimingColumn = col)
    }

    fun toggleTool(tool: ActiveTool) {
        if (_state.status != GameStatus.PLAYING) return
        val newTool = if (_state.activeTool == tool) ActiveTool.NONE else tool
        _state = _state.copy(activeTool = newTool, firstSelectedTile = null)
    }

    fun cancelTool() {
        if (_state.activeTool != ActiveTool.NONE || _state.firstSelectedTile != null) {
            _state = _state.copy(activeTool = ActiveTool.NONE, firstSelectedTile = null)
        }
    }

    fun shootTile(col: Int): Boolean {
        if (_state.status != GameStatus.PLAYING) return false
        if (col !in 0..4) return false

        val currentColumn = _state.grid[col]

        // 1. Ceiling Gravity & Collision: Scan from Row 0 downward.
        // Find the lowest occupied row k. If empty, target row is 0. If occupied at k, target row is k + 1.
        var lowestOccupiedRow = -1
        for (r in 0 until currentColumn.size) {
            if (r < 8 && currentColumn[r] != 0) {
                lowestOccupiedRow = r
            }
        }
        val targetRow = if (lowestOccupiedRow == -1) 0 else lowestOccupiedRow + 1

        if (targetRow > 7) {
            // Column full (max 8 rows, indices 0..7) -> Game Over
            _state = _state.copy(status = GameStatus.GAME_OVER, aimingColumn = null)
            return false
        }

        undoState = _state.copy(canUndo = true)

        val newGrid = _state.grid.map { it.toMutableList() }
        while (newGrid[col].size < targetRow) {
            newGrid[col].add(0)
        }
        newGrid[col].add(targetRow, _state.currentTile)
        newGrid[col].removeAll { it == 0 }

        val nextCur = _state.nextTile
        val maxTileBefore = newGrid.flatten().maxOrNull() ?: 2
        val nextNxt = generateRandomTile(maxTileBefore)

        val (finalGrid, scoreGained, _, _) = processCascade(newGrid)

        val newScore = _state.score + scoreGained
        val newBest = maxOf(_state.bestScore, newScore)
        val newMoveCount = _state.moveCount + 1

        val maxTileAfter = finalGrid.flatten().maxOrNull() ?: 2
        val newTarget = calculateProgressiveTarget(finalGrid, _state.targetTile)

        val isWin = maxTileAfter >= newTarget && _state.status == GameStatus.PLAYING && !_state.isContinued
        val statusAfterWin = if (isWin) GameStatus.WON else _state.status

        val isOver = checkGameOver(finalGrid)
        val finalStatus = if (isOver && statusAfterWin == GameStatus.PLAYING) GameStatus.GAME_OVER else statusAfterWin

        _state = _state.copy(
            grid = finalGrid,
            currentTile = nextCur,
            nextTile = nextNxt,
            score = newScore,
            bestScore = newBest,
            moveCount = newMoveCount,
            status = finalStatus,
            canUndo = true,
            activeTool = ActiveTool.NONE,
            firstSelectedTile = null,
            aimingColumn = null,
            targetTile = newTarget
        )

        return true
    }

    fun useHammer(col: Int, row: Int): Boolean {
        if (_state.status != GameStatus.PLAYING) return false
        if (_state.hammerUses <= 0) return false
        if (col !in 0..4) return false
        if (row !in 0 until _state.grid[col].size) return false
        if (_state.grid[col][row] == 0) return false

        undoState = _state.copy(canUndo = true)

        val newGrid = _state.grid.map { it.toMutableList() }
        newGrid[col].removeAt(row) // Upward collapse: shifting tiles below upward by 1 row

        val (finalGrid, scoreGained, _, _) = processCascade(newGrid)
        val newScore = _state.score + scoreGained
        val newBest = maxOf(_state.bestScore, newScore)
        val newTarget = calculateProgressiveTarget(finalGrid, _state.targetTile)
        val isOver = checkGameOver(finalGrid)
        val finalStatus = if (isOver && _state.status == GameStatus.PLAYING) GameStatus.GAME_OVER else _state.status

        _state = _state.copy(
            grid = finalGrid,
            score = newScore,
            bestScore = newBest,
            status = finalStatus,
            hammerUses = _state.hammerUses - 1,
            canUndo = true,
            activeTool = ActiveTool.NONE,
            firstSelectedTile = null,
            targetTile = newTarget
        )
        return true
    }

    fun selectTileForSwap(col: Int, row: Int): Boolean {
        if (_state.status != GameStatus.PLAYING) return false
        if (_state.switchUses <= 0) return false
        if (col !in 0..4) return false
        if (row !in 0 until _state.grid[col].size) return false

        val first = _state.firstSelectedTile
        if (first == null) {
            _state = _state.copy(firstSelectedTile = Pair(col, row))
            return true
        } else {
            if (first == Pair(col, row)) {
                _state = _state.copy(firstSelectedTile = null)
                return true
            }
            val (c1, r1) = first
            val c2 = col
            val r2 = row

            undoState = _state.copy(canUndo = true)

            val newGrid = _state.grid.map { it.toMutableList() }
            val temp = newGrid[c1][r1]
            newGrid[c1][r1] = newGrid[c2][r2]
            newGrid[c2][r2] = temp

            val (finalGrid, scoreGained, _, _) = processCascade(newGrid)
            val newScore = _state.score + scoreGained
            val newBest = maxOf(_state.bestScore, newScore)
            val newTarget = calculateProgressiveTarget(finalGrid, _state.targetTile)
            val isOver = checkGameOver(finalGrid)
            val finalStatus = if (isOver && _state.status == GameStatus.PLAYING) GameStatus.GAME_OVER else _state.status

            _state = _state.copy(
                grid = finalGrid,
                score = newScore,
                bestScore = newBest,
                status = finalStatus,
                switchUses = _state.switchUses - 1,
                canUndo = true,
                activeTool = ActiveTool.NONE,
                firstSelectedTile = null,
                targetTile = newTarget
            )
            return true
        }
    }

    fun undo(): Boolean {
        val saved = undoState ?: return false
        if (!_state.canUndo && _state.undoUses <= 0) return false

        _state = saved.copy(
            canUndo = false,
            undoUses = maxOf(0, _state.undoUses - 1),
            activeTool = ActiveTool.NONE,
            firstSelectedTile = null
        )
        undoState = null
        return true
    }

    fun continueGame() {
        if (_state.status == GameStatus.WON) {
            val nextTarget = _state.targetTile * 2
            _state = _state.copy(
                status = GameStatus.PLAYING,
                targetTile = nextTarget,
                isContinued = true
            )
        }
    }

    fun addUndoUses(count: Int = 1) {
        _state = _state.copy(undoUses = _state.undoUses + count)
    }

    fun addHammerUses(count: Int = 1) {
        _state = _state.copy(hammerUses = _state.hammerUses + count)
    }

    fun addSwitchUses(count: Int = 1) {
        _state = _state.copy(switchUses = _state.switchUses + count)
    }

    private data class CascadeResult(
        val grid: List<List<Int>>,
        val scoreGained: Int,
        val mergeCount: Int,
        val maxMergedValue: Int
    )

    private fun processCascade(initialGrid: List<MutableList<Int>>): CascadeResult {
        val grid = initialGrid.map { it.toMutableList() }
        var totalScoreGained = 0
        var totalMerges = 0
        var maxMerged = 0

        while (true) {
            var mergedThisPass = false

            // Vertical merges in columns
            outer@ for (c in 0 until 5) {
                val col = grid[c]
                for (r in 0 until col.size - 1) {
                    val v1 = col[r]
                    val v2 = col[r + 1]
                    if (v1 > 0 && v1 == v2) {
                        val mergedVal = v1 * 2
                        col[r] = mergedVal
                        col.removeAt(r + 1) // Upward collapse for removed tile
                        totalScoreGained += mergedVal
                        totalMerges++
                        if (mergedVal > maxMerged) maxMerged = mergedVal
                        mergedThisPass = true
                        break@outer
                    }
                }
            }

            if (mergedThisPass) continue

            // Horizontal merges across adjacent columns (col and col + 1 on the same row)
            outerH@ for (c in 0 until 4) {
                val col1 = grid[c]
                val col2 = grid[c + 1]
                val minSize = minOf(col1.size, col2.size)
                for (r in 0 until minSize) {
                    val v1 = col1[r]
                    val v2 = col2[r]
                    if (v1 > 0 && v1 == v2) {
                        val mergedVal = v1 * 2
                        col1[r] = mergedVal
                        col2.removeAt(r) // Upward collapse for removed tile in col2
                        totalScoreGained += mergedVal
                        totalMerges++
                        if (mergedVal > maxMerged) maxMerged = mergedVal
                        mergedThisPass = true
                        break@outerH
                    }
                }
            }

            if (!mergedThisPass) {
                break
            }
        }

        return CascadeResult(
            grid = grid.map { it.toList() },
            scoreGained = totalScoreGained,
            mergeCount = totalMerges,
            maxMergedValue = maxMerged
        )
    }

    private fun checkGameOver(grid: List<List<Int>>): Boolean {
        for (c in 0 until 5) {
            if (grid[c].size < 8) return false
        }
        for (c in 0 until 5) {
            val col = grid[c]
            for (r in 0 until col.size - 1) {
                if (col[r] == col[r + 1]) return false
            }
        }
        for (c in 0 until 4) {
            val col1 = grid[c]
            val col2 = grid[c + 1]
            for (r in 0 until minOf(col1.size, col2.size)) {
                if (col1[r] == col2[r]) return false
            }
        }
        return true
    }
}
