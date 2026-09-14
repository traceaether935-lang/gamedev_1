package com.game.a2048

import kotlin.math.abs
import kotlin.random.Random

class GameEngine(
    rows: Int = 4,
    cols: Int = 4,
    targetGoal: Int = 32,
    initialBestScore: Int = 0,
    activeMission: Mission? = null,
    private val random: Random = Random.Default
) {
    var state: GameState
        private set

    private var previousState: GameState? = null

    init {
        require(rows in 2..12 && cols in 2..12) { "Board dimensions must be between 2 and 12" }
        state = createNewGame(rows, cols, initialBestScore, targetGoal, activeMission)
    }

    private fun calculateProgressiveTarget(grid: List<List<Int>>, currentTarget: Int): Int {
        val maxTile = grid.flatten().maxOrNull() ?: 0
        var target = maxOf(32, currentTarget)
        while (maxTile >= target) {
            target *= 2
        }
        return target
    }

    private fun createNewGame(
        rows: Int,
        cols: Int,
        bestScore: Int,
        targetGoal: Int,
        activeMission: Mission? = null
    ): GameState {
        val emptyGrid = List(rows) { List(cols) { 0 } }
        val gridWithFirstTile = spawnRandomTileInGrid(emptyGrid)
        val initialGrid = spawnRandomTileInGrid(gridWithFirstTile)

        val finalTargetGoal = if (activeMission != null) {
            activeMission.targetValue
        } else {
            calculateProgressiveTarget(initialGrid, targetGoal)
        }

        val (isCompleted, isFailed) = evaluateMission(
            mission = activeMission,
            grid = initialGrid,
            currentScore = 0,
            moveCount = 0,
            lastMergeCount = 0,
            isCurrentlyCompleted = false,
            isCurrentlyFailed = false
        )

        return GameState(
            rows = rows,
            cols = cols,
            grid = initialGrid,
            currentScore = 0,
            bestScore = bestScore,
            moveCount = 0,
            targetGoal = finalTargetGoal,
            status = GameStatus.PLAYING,
            canUndo = false,
            isContinued = false,
            hammerCount = 3,
            swapCount = 3,
            activePowerUp = null,
            activeMission = activeMission,
            isMissionCompleted = isCompleted,
            isMissionFailed = isFailed
        )
    }

    fun restart(
        rows: Int = state.rows,
        cols: Int = state.cols,
        targetGoal: Int = 32,
        activeMission: Mission? = state.activeMission
    ) {
        val currentBestScore = state.bestScore
        previousState = null
        state = createNewGame(rows, cols, currentBestScore, targetGoal, activeMission)
    }

    fun selectMission(mission: Mission?) {
        val currentBestScore = state.bestScore
        previousState = null
        val target = mission?.targetValue ?: 32
        state = createNewGame(state.rows, state.cols, currentBestScore, target, mission)
    }

    fun pause() {
        if (state.status == GameStatus.PLAYING) {
            state = state.copy(status = GameStatus.PAUSED)
        }
    }

    fun resume() {
        if (state.status == GameStatus.PAUSED) {
            state = state.copy(status = GameStatus.PLAYING)
        }
    }

    fun continueGame() {
        if (state.status == GameStatus.WON || state.status == GameStatus.PLAYING) {
            state = state.copy(status = GameStatus.PLAYING, isContinued = true)
        }
    }

    fun setGameState(newState: GameState) {
        previousState = null
        state = newState
    }

    fun setTargetGoal(targetGoal: Int) {
        val finalTarget = if (state.activeMission == null) {
            calculateProgressiveTarget(state.grid, targetGoal)
        } else {
            targetGoal
        }
        val containsTargetOrHigher = state.grid.flatten().any { it >= finalTarget }
        val newStatus = if (state.activeMission != null && containsTargetOrHigher && state.status == GameStatus.PLAYING && !state.isContinued) {
            GameStatus.WON
        } else {
            state.status
        }
        state = state.copy(targetGoal = finalTarget, status = newStatus)
    }

    fun updateBestScore(bestScore: Int) {
        if (bestScore > state.bestScore) {
            state = state.copy(bestScore = bestScore)
        }
    }

    fun setGrid(
        grid: List<List<Int>>,
        score: Int = 0,
        moveCount: Int = 0,
        status: GameStatus = GameStatus.PLAYING,
        canUndo: Boolean = false,
        isContinued: Boolean = false,
        targetGoal: Int = state.targetGoal,
        hammerCount: Int = state.hammerCount,
        swapCount: Int = state.swapCount,
        activePowerUp: PowerUpType? = state.activePowerUp,
        activeMission: Mission? = state.activeMission,
        isMissionCompleted: Boolean = state.isMissionCompleted,
        isMissionFailed: Boolean = state.isMissionFailed
    ) {
        previousState = null
        val rows = grid.size
        val cols = if (rows > 0) grid[0].size else 0

        val finalTargetGoal = if (activeMission != null) {
            activeMission.targetValue
        } else {
            calculateProgressiveTarget(grid, targetGoal)
        }

        val (completed, failed) = evaluateMission(
            mission = activeMission,
            grid = grid,
            currentScore = score,
            moveCount = moveCount,
            lastMergeCount = 0,
            isCurrentlyCompleted = isMissionCompleted,
            isCurrentlyFailed = isMissionFailed
        )

        state = GameState(
            rows = rows,
            cols = cols,
            grid = grid,
            currentScore = score,
            bestScore = maxOf(state.bestScore, score),
            moveCount = moveCount,
            targetGoal = finalTargetGoal,
            status = status,
            canUndo = canUndo,
            isContinued = isContinued,
            hammerCount = hammerCount,
            swapCount = swapCount,
            activePowerUp = activePowerUp,
            activeMission = activeMission,
            isMissionCompleted = completed,
            isMissionFailed = failed
        )
    }

    fun togglePowerUp(powerUpType: PowerUpType?) {
        state = state.copy(activePowerUp = if (state.activePowerUp == powerUpType) null else powerUpType)
    }

    fun useHammer(row: Int, col: Int): Boolean {
        if (state.status != GameStatus.PLAYING) return false
        if (state.hammerCount <= 0) return false
        if (row !in 0 until state.rows || col !in 0 until state.cols) return false
        if (state.grid[row][col] == 0) return false

        previousState = state.copy(canUndo = false)

        val newGrid = state.grid.mapIndexed { r, rowList ->
            if (r == row) {
                rowList.mapIndexed { c, value ->
                    if (c == col) 0 else value
                }
            } else {
                rowList
            }
        }

        val finalTargetGoal = if (state.activeMission == null) {
            calculateProgressiveTarget(newGrid, state.targetGoal)
        } else {
            state.targetGoal
        }

        val (isCompleted, isFailed) = evaluateMission(
            mission = state.activeMission,
            grid = newGrid,
            currentScore = state.currentScore,
            moveCount = state.moveCount,
            lastMergeCount = 0,
            isCurrentlyCompleted = state.isMissionCompleted,
            isCurrentlyFailed = state.isMissionFailed
        )

        state = state.copy(
            grid = newGrid,
            targetGoal = finalTargetGoal,
            hammerCount = state.hammerCount - 1,
            activePowerUp = null,
            canUndo = true,
            isMissionCompleted = isCompleted,
            isMissionFailed = isFailed
        )
        return true
    }

    fun useSwap(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        if (state.status != GameStatus.PLAYING) return false
        if (state.swapCount <= 0) return false
        if (r1 !in 0 until state.rows || c1 !in 0 until state.cols) return false
        if (r2 !in 0 until state.rows || c2 !in 0 until state.cols) return false

        if (r1 != r2 || c1 != c2) {
            previousState = state.copy(canUndo = false)

            val v1 = state.grid[r1][c1]
            val v2 = state.grid[r2][c2]

            val newGrid = state.grid.mapIndexed { r, rowList ->
                rowList.mapIndexed { c, value ->
                    when {
                        r == r1 && c == c1 -> v2
                        r == r2 && c == c2 -> v1
                        else -> value
                    }
                }
            }

            val finalTargetGoal = if (state.activeMission == null) {
                calculateProgressiveTarget(newGrid, state.targetGoal)
            } else {
                state.targetGoal
            }

            val (isCompleted, isFailed) = evaluateMission(
                mission = state.activeMission,
                grid = newGrid,
                currentScore = state.currentScore,
                moveCount = state.moveCount,
                lastMergeCount = 0,
                isCurrentlyCompleted = state.isMissionCompleted,
                isCurrentlyFailed = state.isMissionFailed
            )

            state = state.copy(
                grid = newGrid,
                targetGoal = finalTargetGoal,
                swapCount = state.swapCount - 1,
                activePowerUp = null,
                canUndo = true,
                isMissionCompleted = isCompleted,
                isMissionFailed = isFailed
            )
            return true
        }
        return false
    }

    fun move(direction: Direction): MoveResult {
        if (state.status != GameStatus.PLAYING) {
            return MoveResult(hasChanged = false, scoreGained = 0, maxMergedValue = 0, mergeCount = 0, newState = state)
        }

        val transformResult = transformGrid(state.grid, direction)
        val hasChanged = transformResult.newGrid != state.grid

        if (!hasChanged) {
            return MoveResult(hasChanged = false, scoreGained = 0, maxMergedValue = 0, mergeCount = 0, newState = state)
        }

        previousState = state.copy(canUndo = false)

        val newScore = state.currentScore + transformResult.scoreGained
        val newBestScore = maxOf(state.bestScore, newScore)
        val newMoveCount = state.moveCount + 1

        val gridWithNewTile = spawnRandomTileInGrid(transformResult.newGrid)

        val finalTargetGoal = if (state.activeMission == null) {
            calculateProgressiveTarget(gridWithNewTile, state.targetGoal)
        } else {
            state.targetGoal
        }

        val containsTargetOrHigher = gridWithNewTile.any { row -> row.any { it >= finalTargetGoal } }
        val newStatus = when {
            state.activeMission != null && containsTargetOrHigher && !state.isContinued -> GameStatus.WON
            isGameOver(gridWithNewTile) -> GameStatus.GAME_OVER
            else -> GameStatus.PLAYING
        }

        val (isCompleted, isFailed) = evaluateMission(
            mission = state.activeMission,
            grid = gridWithNewTile,
            currentScore = newScore,
            moveCount = newMoveCount,
            lastMergeCount = transformResult.mergeCount,
            isCurrentlyCompleted = state.isMissionCompleted,
            isCurrentlyFailed = state.isMissionFailed
        )

        state = state.copy(
            grid = gridWithNewTile,
            currentScore = newScore,
            bestScore = newBestScore,
            moveCount = newMoveCount,
            targetGoal = finalTargetGoal,
            status = newStatus,
            canUndo = true,
            isMissionCompleted = isCompleted,
            isMissionFailed = isFailed
        )

        return MoveResult(
            hasChanged = true,
            scoreGained = transformResult.scoreGained,
            maxMergedValue = transformResult.maxMergedValue,
            mergeCount = transformResult.mergeCount,
            newState = state
        )
    }

    fun undo(): Boolean {
        val prev = previousState
        if (prev != null && state.canUndo) {
            val newBestScore = maxOf(prev.bestScore, state.bestScore)
            state = prev.copy(
                bestScore = newBestScore,
                canUndo = false
            )
            previousState = null
            return true
        }
        return false
    }

    private fun evaluateMission(
        mission: Mission?,
        grid: List<List<Int>>,
        currentScore: Int,
        moveCount: Int,
        lastMergeCount: Int,
        isCurrentlyCompleted: Boolean,
        isCurrentlyFailed: Boolean
    ): Pair<Boolean, Boolean> {
        if (mission == null) return Pair(false, false)
        if (isCurrentlyCompleted || isCurrentlyFailed) return Pair(isCurrentlyCompleted, isCurrentlyFailed)

        val maxTile = grid.flatten().maxOrNull() ?: 0
        var completed = false
        var failed = false

        when (mission.type) {
            MissionType.TILE_REACH -> {
                if (maxTile >= mission.targetValue) {
                    completed = true
                }
            }
            MissionType.MOVE_LIMIT_TILE -> {
                if (maxTile >= mission.targetValue && moveCount <= mission.moveLimit) {
                    completed = true
                } else if (moveCount >= mission.moveLimit && maxTile < mission.targetValue) {
                    failed = true
                }
            }
            MissionType.SCORE_REACH -> {
                if (currentScore >= mission.targetValue) {
                    completed = true
                }
            }
            MissionType.COMBO_MERGE -> {
                if (lastMergeCount >= mission.targetValue) {
                    completed = true
                }
            }
        }
        return Pair(completed, failed)
    }

    private fun spawnRandomTileInGrid(grid: List<List<Int>>): List<List<Int>> {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in grid.indices) {
            for (c in grid[r].indices) {
                if (grid[r][c] == 0) {
                    emptyCells.add(r to c)
                }
            }
        }
        if (emptyCells.isEmpty()) return grid

        val (r, c) = emptyCells[random.nextInt(emptyCells.size)]
        val tileValue = if (random.nextFloat() < 0.9f) 2 else 4

        return grid.mapIndexed { rowIdx, rowList ->
            if (rowIdx == r) {
                rowList.mapIndexed { colIdx, valCell ->
                    if (colIdx == c) tileValue else valCell
                }
            } else {
                rowList
            }
        }
    }

    private data class TransformResult(
        val newGrid: List<List<Int>>,
        val scoreGained: Int,
        val maxMergedValue: Int,
        val mergeCount: Int
    )

    private fun transformGrid(
        grid: List<List<Int>>,
        direction: Direction
    ): TransformResult {
        val rows = grid.size
        val cols = if (rows > 0) grid[0].size else 0
        var totalScoreGained = 0
        var overallMaxMergedValue = 0
        var totalMergeCount = 0
        val newGrid = List(rows) { MutableList(cols) { 0 } }

        when (direction) {
            Direction.LEFT -> {
                for (r in 0 until rows) {
                    val lineRes = slideAndMergeLine(grid[r], cols)
                    totalScoreGained += lineRes.scoreGained
                    totalMergeCount += lineRes.mergeCount
                    if (lineRes.maxMergedValue > overallMaxMergedValue) {
                        overallMaxMergedValue = lineRes.maxMergedValue
                    }
                    for (c in 0 until cols) {
                        newGrid[r][c] = lineRes.line[c]
                    }
                }
            }

            Direction.RIGHT -> {
                for (r in 0 until rows) {
                    val lineRes = slideAndMergeLine(grid[r].reversed(), cols)
                    totalScoreGained += lineRes.scoreGained
                    totalMergeCount += lineRes.mergeCount
                    if (lineRes.maxMergedValue > overallMaxMergedValue) {
                        overallMaxMergedValue = lineRes.maxMergedValue
                    }
                    val reversedLine = lineRes.line.reversed()
                    for (c in 0 until cols) {
                        newGrid[r][c] = reversedLine[c]
                    }
                }
            }

            Direction.UP -> {
                for (c in 0 until cols) {
                    val colList = List(rows) { r -> grid[r][c] }
                    val lineRes = slideAndMergeLine(colList, rows)
                    totalScoreGained += lineRes.scoreGained
                    totalMergeCount += lineRes.mergeCount
                    if (lineRes.maxMergedValue > overallMaxMergedValue) {
                        overallMaxMergedValue = lineRes.maxMergedValue
                    }
                    for (r in 0 until rows) {
                        newGrid[r][c] = lineRes.line[r]
                    }
                }
            }

            Direction.DOWN -> {
                for (c in 0 until cols) {
                    val colList = List(rows) { r -> grid[r][c] }
                    val lineRes = slideAndMergeLine(colList.reversed(), rows)
                    totalScoreGained += lineRes.scoreGained
                    totalMergeCount += lineRes.mergeCount
                    if (lineRes.maxMergedValue > overallMaxMergedValue) {
                        overallMaxMergedValue = lineRes.maxMergedValue
                    }
                    val reversedLine = lineRes.line.reversed()
                    for (r in 0 until rows) {
                        newGrid[r][c] = reversedLine[r]
                    }
                }
            }
        }

        return TransformResult(
            newGrid = newGrid,
            scoreGained = totalScoreGained,
            maxMergedValue = overallMaxMergedValue,
            mergeCount = totalMergeCount
        )
    }

    private data class LineResult(
        val line: List<Int>,
        val scoreGained: Int,
        val maxMergedValue: Int,
        val mergeCount: Int
    )

    private fun slideAndMergeLine(line: List<Int>, targetSize: Int): LineResult {
        val nonZeros = line.filter { it != 0 }
        val result = mutableListOf<Int>()
        var scoreGained = 0
        var maxMergedValue = 0
        var mergeCount = 0
        var i = 0
        while (i < nonZeros.size) {
            if (i + 1 < nonZeros.size && nonZeros[i] == nonZeros[i + 1]) {
                val mergedValue = nonZeros[i] * 2
                result.add(mergedValue)
                scoreGained += mergedValue
                mergeCount += 1
                if (mergedValue > maxMergedValue) {
                    maxMergedValue = mergedValue
                }
                i += 2
            } else {
                result.add(nonZeros[i])
                i += 1
            }
        }
        while (result.size < targetSize) {
            result.add(0)
        }
        return LineResult(result, scoreGained, maxMergedValue, mergeCount)
    }

    private fun isGameOver(grid: List<List<Int>>): Boolean {
        val rows = grid.size
        val cols = if (rows > 0) grid[0].size else 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (grid[r][c] == 0) return false
                if (c + 1 < cols && grid[r][c] == grid[r][c + 1]) return false
                if (r + 1 < rows && grid[r][c] == grid[r + 1][c]) return false
            }
        }
        return true
    }
}
