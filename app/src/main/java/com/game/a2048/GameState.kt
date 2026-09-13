package com.game.a2048

data class GameState(
    val rows: Int = 4,
    val cols: Int = 4,
    val grid: List<List<Int>>,
    val currentScore: Int = 0,
    val bestScore: Int = 0,
    val moveCount: Int = 0,
    val targetGoal: Int = 32,
    val status: GameStatus = GameStatus.PLAYING,
    val canUndo: Boolean = false,
    val isContinued: Boolean = false,
    val hammerCount: Int = 3,
    val swapCount: Int = 3,
    val activePowerUp: PowerUpType? = null,
    val activeMission: Mission? = null,
    val isMissionCompleted: Boolean = false,
    val isMissionFailed: Boolean = false
) {
    val targetTile: Int get() = targetGoal
    operator fun get(row: Int, col: Int): Int = grid[row][col]
}

enum class PowerUpType {
    HAMMER, SWAP
}
