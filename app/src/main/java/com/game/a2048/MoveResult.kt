package com.game.a2048

data class MoveResult(
    val hasChanged: Boolean,
    val scoreGained: Int,
    val maxMergedValue: Int = 0,
    val mergeCount: Int = 0,
    val newState: GameState
)
