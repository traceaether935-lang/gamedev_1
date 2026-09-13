package com.game.a2048.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GameStatistics(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val highestScore: Int = 0,
    val highestTile: Int = 0,
    val totalMoves: Long = 0L
)
