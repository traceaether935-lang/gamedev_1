package com.game.a2048.hex

import com.game.a2048.GameStatus

enum class PowerUpType {
    HAMMER,
    SWAP
}

data class HexGameState(
    val radius: Int = HexGrid.DEFAULT_RADIUS,
    val grid: Map<HexCell, Int> = HexGrid.getCells(HexGrid.DEFAULT_RADIUS).associateWith { 0 },
    val upcomingPieces: List<HexPiece> = emptyList(),
    val currentScore: Int = 0,
    val bestScore: Int = 0,
    val moveCount: Int = 0,
    val status: GameStatus = GameStatus.PLAYING,
    val canUndo: Boolean = false,
    val isContinued: Boolean = false,
    val hammerCount: Int = 3,
    val swapCount: Int = 3,
    val activePowerUp: PowerUpType? = null,
    val targetGoal: Int = 32
)
