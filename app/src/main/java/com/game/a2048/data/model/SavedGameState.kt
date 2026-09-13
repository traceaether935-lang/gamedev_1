package com.game.a2048.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SavedGameState(
    val boardSize: Int,
    val gridValues: List<Int>,
    val currentScore: Int,
    val moveCount: Int,
    val status: String,
    val isContinued: Boolean,
    val targetGoal: Int = 2048,
    val undoGridValues: List<Int>? = null,
    val undoScore: Int? = null,
    val undoMoveCount: Int? = null
)
