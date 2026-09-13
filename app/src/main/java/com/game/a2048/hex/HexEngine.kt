package com.game.a2048.hex

import com.game.a2048.GameStatus
import kotlin.random.Random

data class HexMoveResult(
    val hasChanged: Boolean = false,
    val scoreGained: Int = 0,
    val maxMergedValue: Int = 0,
    val mergeCount: Int = 0,
    val newState: HexGameState
)

data class HexPieceResult(
    val success: Boolean = false,
    val scoreGained: Int = 0,
    val maxMergedValue: Int = 0,
    val mergeCount: Int = 0,
    val chainCount: Int = 0,
    val newState: HexGameState
)

class HexEngine(
    initialBestScore: Int = 0,
    val radius: Int = HexGrid.DEFAULT_RADIUS
) {
    private var _state = HexGameState(radius = radius, grid = HexGrid.getCells(radius).associateWith { 0 }, bestScore = initialBestScore)
    val state: HexGameState get() = _state

    val upcomingPieces: List<HexPiece> get() = _state.upcomingPieces

    private var undoState: HexGameState? = null
    var lastPieceResult: HexPieceResult? = null
        private set

    init {
        restart()
    }

    private fun getCells(): List<HexCell> = HexGrid.getCells(radius)

    private fun calculateProgressiveTarget(grid: Map<HexCell, Int>, currentTarget: Int): Int {
        val maxTile = grid.values.maxOrNull() ?: 0
        var target = maxOf(32, currentTarget)
        while (maxTile >= target) {
            target *= 2
        }
        return target
    }

    fun restart() {
        val emptyGrid = getCells().associateWith { 0 }
        val tray = generateTray()
        _state = HexGameState(
            radius = radius,
            grid = emptyGrid,
            upcomingPieces = tray,
            currentScore = 0,
            bestScore = _state.bestScore,
            moveCount = 0,
            status = GameStatus.PLAYING,
            canUndo = false,
            isContinued = false,
            hammerCount = 3,
            swapCount = 3,
            activePowerUp = null,
            targetGoal = 32
        )
        undoState = null
        lastPieceResult = null
        spawnRandomTile()
        spawnRandomTile()
        val finalTarget = calculateProgressiveTarget(_state.grid, 32)
        _state = _state.copy(targetGoal = finalTarget)
    }

    fun setGrid(
        gridMap: Map<HexCell, Int>,
        score: Int = 0,
        bestScore: Int = _state.bestScore,
        moveCount: Int = 0,
        status: GameStatus = GameStatus.PLAYING,
        canUndo: Boolean = false,
        isContinued: Boolean = false,
        upcomingPieces: List<HexPiece>? = null,
        hammerCount: Int = _state.hammerCount,
        swapCount: Int = _state.swapCount,
        targetGoal: Int = _state.targetGoal
    ) {
        val fullGrid = getCells().associateWith { cell -> gridMap[cell] ?: 0 }
        val finalBestScore = maxOf(bestScore, score)
        val finalTray = upcomingPieces ?: generateTray()
        val finalTargetGoal = calculateProgressiveTarget(fullGrid, targetGoal)
        val isOver = checkGameOverForPiecePlacement(fullGrid, finalTray) && checkGameOver(fullGrid)
        val finalStatus = if (isOver && status == GameStatus.PLAYING) GameStatus.GAME_OVER else status
        _state = HexGameState(
            radius = radius,
            grid = fullGrid,
            upcomingPieces = finalTray,
            currentScore = score,
            bestScore = finalBestScore,
            moveCount = moveCount,
            status = finalStatus,
            canUndo = canUndo,
            isContinued = isContinued,
            hammerCount = hammerCount,
            swapCount = swapCount,
            activePowerUp = null,
            targetGoal = finalTargetGoal
        )
        undoState = null
        lastPieceResult = null
    }

    fun togglePowerUp(powerUp: PowerUpType) {
        if (_state.status != GameStatus.PLAYING) return
        val newActive = if (_state.activePowerUp == powerUp) {
            null
        } else {
            when (powerUp) {
                PowerUpType.HAMMER -> if (_state.hammerCount > 0) PowerUpType.HAMMER else null
                PowerUpType.SWAP -> if (_state.swapCount > 0) PowerUpType.SWAP else null
            }
        }
        _state = _state.copy(activePowerUp = newActive)
    }

    fun cancelPowerUp() {
        if (_state.activePowerUp != null) {
            _state = _state.copy(activePowerUp = null)
        }
    }

    fun useHammer(cell: HexCell): Boolean {
        if (_state.status != GameStatus.PLAYING) return false
        if (_state.hammerCount <= 0) return false
        if (!HexGrid.contains(cell, radius)) return false
        if ((_state.grid[cell] ?: 0) == 0) return false

        undoState = _state.copy(canUndo = true)

        val updatedGrid = _state.grid.toMutableMap()
        updatedGrid[cell] = 0

        val newTargetGoal = calculateProgressiveTarget(updatedGrid, _state.targetGoal)

        _state = _state.copy(
            grid = updatedGrid,
            hammerCount = _state.hammerCount - 1,
            activePowerUp = null,
            targetGoal = newTargetGoal,
            canUndo = true
        )
        lastPieceResult = null
        return true
    }

    fun useSwap(cell1: HexCell, cell2: HexCell): Boolean {
        if (_state.status != GameStatus.PLAYING) return false
        if (_state.swapCount <= 0) return false
        if (cell1 == cell2) return false
        if (!HexGrid.contains(cell1, radius) || !HexGrid.contains(cell2, radius)) return false
        val val1 = _state.grid[cell1] ?: 0
        val val2 = _state.grid[cell2] ?: 0
        if (val1 == 0 && val2 == 0) return false

        undoState = _state.copy(canUndo = true)

        val updatedGrid = _state.grid.toMutableMap()
        updatedGrid[cell1] = val2
        updatedGrid[cell2] = val1

        val newTargetGoal = calculateProgressiveTarget(updatedGrid, _state.targetGoal)

        _state = _state.copy(
            grid = updatedGrid,
            swapCount = _state.swapCount - 1,
            activePowerUp = null,
            targetGoal = newTargetGoal,
            canUndo = true
        )
        lastPieceResult = null
        return true
    }

    fun findPlacementCells(
        piece: HexPiece,
        targetCell: HexCell,
        grid: Map<HexCell, Int> = _state.grid
    ): List<HexCell>? {
        if (!HexGrid.contains(targetCell, radius)) return null
        if ((grid[targetCell] ?: 0) != 0) return null

        if (piece.values.size == 1) {
            return listOf(targetCell)
        }

        val cell2 = targetCell.neighbors().firstOrNull {
            HexGrid.contains(it, radius) && (grid[it] ?: 0) == 0
        } ?: return null

        if (piece.values.size == 2) {
            return listOf(targetCell, cell2)
        }

        val cell3 = targetCell.neighbors().firstOrNull { n ->
            HexGrid.contains(n, radius) && (grid[n] ?: 0) == 0 && n != cell2 && cell2.neighbors().contains(n)
        } ?: targetCell.neighbors().firstOrNull { n ->
            HexGrid.contains(n, radius) && (grid[n] ?: 0) == 0 && n != cell2
        } ?: cell2.neighbors().firstOrNull { n ->
            HexGrid.contains(n, radius) && (grid[n] ?: 0) == 0 && n != targetCell
        } ?: return null

        return listOf(targetCell, cell2, cell3)
    }

    fun placePiece(pieceIndex: Int, targetCell: HexCell): Boolean {
        if (_state.status != GameStatus.PLAYING) return false
        val pieces = _state.upcomingPieces
        if (pieceIndex !in pieces.indices) return false

        val piece = pieces[pieceIndex]
        val placementCells = findPlacementCells(piece, targetCell) ?: return false

        undoState = _state.copy(canUndo = true)

        val updatedGrid = _state.grid.toMutableMap()
        placementCells.forEachIndexed { idx, cell ->
            updatedGrid[cell] = piece.values[idx]
        }

        var scoreGained = 0
        var totalMerges = 0
        var maxChainCount = 0
        var maxMergedValue = 0

        val cellsToProcess = placementCells.toMutableList()

        for (cell in cellsToProcess) {
            if ((updatedGrid[cell] ?: 0) == 0) continue

            val valToMatch = updatedGrid[cell]!!
            val initialGroup = findConnectedGroup(cell, valToMatch, updatedGrid)

            // Initial merge rule: group size >= 3
            if (initialGroup.size >= 3) {
                for (gCell in initialGroup) {
                    if (gCell != cell) {
                        updatedGrid[gCell] = 0
                    }
                }
                var currentVal = valToMatch * 2
                updatedGrid[cell] = currentVal
                scoreGained += currentVal
                totalMerges++
                maxMergedValue = maxOf(maxMergedValue, currentVal)

                var chainCount = 1
                maxChainCount = maxOf(maxChainCount, chainCount)

                // Chain reaction (combo) loop: group size >= 2
                while (true) {
                    val chainGroup = findConnectedGroup(cell, currentVal, updatedGrid)
                    if (chainGroup.size >= 2) {
                        for (gCell in chainGroup) {
                            if (gCell != cell) {
                                updatedGrid[gCell] = 0
                            }
                        }
                        currentVal *= 2
                        updatedGrid[cell] = currentVal
                        scoreGained += currentVal
                        totalMerges++
                        maxMergedValue = maxOf(maxMergedValue, currentVal)
                        chainCount++
                        maxChainCount = maxOf(maxChainCount, chainCount)
                    } else {
                        break
                    }
                }
            }
        }

        val newScore = _state.currentScore + scoreGained
        val newBestScore = maxOf(_state.bestScore, newScore)
        val newMoveCount = _state.moveCount + 1

        val remainingPieces = pieces.toMutableList()
        remainingPieces.removeAt(pieceIndex)

        val nextUpcoming = if (remainingPieces.isEmpty()) {
            generateTray()
        } else {
            remainingPieces
        }

        var newStatus = _state.status
        val has2048 = updatedGrid.values.any { it >= 2048 }
        if (has2048 && newStatus == GameStatus.PLAYING && !_state.isContinued) {
            newStatus = GameStatus.WON
        }

        if (checkGameOverForPiecePlacement(updatedGrid, nextUpcoming)) {
            newStatus = GameStatus.GAME_OVER
        }

        val newTargetGoal = calculateProgressiveTarget(updatedGrid, _state.targetGoal)

        _state = _state.copy(
            grid = updatedGrid,
            upcomingPieces = nextUpcoming,
            currentScore = newScore,
            bestScore = newBestScore,
            moveCount = newMoveCount,
            status = newStatus,
            targetGoal = newTargetGoal,
            canUndo = true
        )

        lastPieceResult = HexPieceResult(
            success = true,
            scoreGained = scoreGained,
            maxMergedValue = maxMergedValue,
            mergeCount = totalMerges,
            chainCount = maxChainCount,
            newState = _state
        )

        return true
    }

    fun move(direction: HexDirection): HexMoveResult {
        if (_state.status != GameStatus.PLAYING) {
            return HexMoveResult(hasChanged = false, newState = _state)
        }

        val opDir = opposite(direction)
        val headCells = getCells().filter { cell ->
            !HexGrid.contains(cell.neighbor(direction), radius)
        }

        val currentGrid = _state.grid
        val newGrid = currentGrid.toMutableMap()

        var scoreGained = 0
        var maxMergedValue = 0
        var mergeCount = 0

        for (head in headCells) {
            val line = mutableListOf<HexCell>()
            var curr: HexCell? = head
            while (curr != null && HexGrid.contains(curr, radius)) {
                line.add(curr)
                curr = curr.neighbor(opDir)
            }

            val vals = line.map { newGrid[it] ?: 0 }.filter { it > 0 }
            val mergedList = mutableListOf<Int>()
            var i = 0
            while (i < vals.size) {
                if (i + 1 < vals.size && vals[i] == vals[i + 1]) {
                    val mergedVal = vals[i] * 2
                    mergedList.add(mergedVal)
                    scoreGained += mergedVal
                    maxMergedValue = maxOf(maxMergedValue, mergedVal)
                    mergeCount++
                    i += 2
                } else {
                    mergedList.add(vals[i])
                    i++
                }
            }

            for (j in line.indices) {
                val cell = line[j]
                if (j < mergedList.size) {
                    newGrid[cell] = mergedList[j]
                } else {
                    newGrid[cell] = 0
                }
            }
        }

        val hasChanged = newGrid != currentGrid

        if (hasChanged) {
            undoState = _state.copy(canUndo = true)

            val newScore = _state.currentScore + scoreGained
            val newBestScore = maxOf(_state.bestScore, newScore)
            val newMoveCount = _state.moveCount + 1

            _state = _state.copy(
                grid = newGrid,
                currentScore = newScore,
                bestScore = newBestScore,
                moveCount = newMoveCount,
                canUndo = true
            )

            spawnRandomTile()

            var newStatus = _state.status
            val has2048 = _state.grid.values.any { it >= 2048 }
            if (has2048 && newStatus == GameStatus.PLAYING && !_state.isContinued) {
                newStatus = GameStatus.WON
            }

            if (checkGameOver(_state.grid) && checkGameOverForPiecePlacement(_state.grid, _state.upcomingPieces)) {
                newStatus = GameStatus.GAME_OVER
            }

            val newTargetGoal = calculateProgressiveTarget(_state.grid, _state.targetGoal)

            _state = _state.copy(status = newStatus, targetGoal = newTargetGoal)

            return HexMoveResult(
                hasChanged = true,
                scoreGained = scoreGained,
                maxMergedValue = maxMergedValue,
                mergeCount = mergeCount,
                newState = _state
            )
        } else {
            if (checkGameOver(_state.grid) && checkGameOverForPiecePlacement(_state.grid, _state.upcomingPieces) && _state.status == GameStatus.PLAYING) {
                _state = _state.copy(status = GameStatus.GAME_OVER)
            }
            return HexMoveResult(hasChanged = false, newState = _state)
        }
    }

    fun undo(): Boolean {
        val previous = undoState ?: return false
        _state = previous.copy(canUndo = false)
        undoState = null
        lastPieceResult = null
        return true
    }

    fun pause() {
        if (_state.status == GameStatus.PLAYING) {
            _state = _state.copy(status = GameStatus.PAUSED)
        }
    }

    fun resume() {
        if (_state.status == GameStatus.PAUSED) {
            _state = _state.copy(status = GameStatus.PLAYING)
        }
    }

    fun continueGame() {
        if (_state.status == GameStatus.WON) {
            _state = _state.copy(status = GameStatus.PLAYING, isContinued = true)
        }
    }

    fun updateBestScore(bestScore: Int) {
        val newBest = maxOf(_state.bestScore, bestScore)
        _state = _state.copy(bestScore = newBest)
    }

    private fun generateTray(): List<HexPiece> {
        return listOf(HexPiece.random(), HexPiece.random(), HexPiece.random())
    }

    private fun findConnectedGroup(
        startCell: HexCell,
        valToMatch: Int,
        grid: Map<HexCell, Int>
    ): Set<HexCell> {
        if ((grid[startCell] ?: 0) != valToMatch || valToMatch == 0) return emptySet()
        val visited = mutableSetOf<HexCell>()
        val queue = ArrayDeque<HexCell>()
        queue.add(startCell)
        visited.add(startCell)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (neighbor in current.neighbors()) {
                if (HexGrid.contains(neighbor, radius) && neighbor !in visited && (grid[neighbor] ?: 0) == valToMatch) {
                    visited.add(neighbor)
                    queue.add(neighbor)
                }
            }
        }
        return visited
    }

    private fun checkGameOverForPiecePlacement(
        grid: Map<HexCell, Int>,
        upcomingPieces: List<HexPiece>
    ): Boolean {
        val emptyCells = getCells().filter { (grid[it] ?: 0) == 0 }
        if (emptyCells.isEmpty()) {
            return true
        }

        for (piece in upcomingPieces) {
            for (emptyCell in emptyCells) {
                if (findPlacementCells(piece, emptyCell, grid) != null) {
                    return false
                }
            }
        }

        return true
    }

    private fun spawnRandomTile() {
        val emptyCells = getCells().filter { (_state.grid[it] ?: 0) == 0 }
        if (emptyCells.isNotEmpty()) {
            val randomCell = emptyCells[Random.nextInt(emptyCells.size)]
            val spawnValue = if (Random.nextFloat() < 0.9f) 2 else 4
            val updatedGrid = _state.grid.toMutableMap()
            updatedGrid[randomCell] = spawnValue
            _state = _state.copy(grid = updatedGrid)
        }
    }

    private fun opposite(direction: HexDirection): HexDirection {
        return when (direction) {
            HexDirection.EAST -> HexDirection.WEST
            HexDirection.NORTH_EAST -> HexDirection.SOUTH_WEST
            HexDirection.NORTH_WEST -> HexDirection.SOUTH_EAST
            HexDirection.WEST -> HexDirection.EAST
            HexDirection.SOUTH_WEST -> HexDirection.NORTH_EAST
            HexDirection.SOUTH_EAST -> HexDirection.NORTH_WEST
        }
    }

    private fun checkGameOver(grid: Map<HexCell, Int>): Boolean {
        if (getCells().any { (grid[it] ?: 0) == 0 }) {
            return false
        }
        for (cell in getCells()) {
            val cellValue = grid[cell] ?: 0
            for (dir in HexDirection.entries) {
                val neighbor = cell.neighbor(dir)
                if (HexGrid.contains(neighbor, radius)) {
                    val neighborValue = grid[neighbor] ?: 0
                    if (cellValue == neighborValue) {
                        return false
                    }
                }
            }
        }
        return true
    }
}
