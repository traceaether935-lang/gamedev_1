package com.game.a2048.column

import com.game.a2048.GameStatus
import com.aethertrace.numberdrop2048hexa.ui.model.ActiveTool

data class ColumnDropState(
    val grid: List<List<Int>> = List(5) { emptyList() }, // 5 columns, max 8 rows each
    val currentTile: Int = 2,
    val nextTile: Int = 4,
    val score: Int = 0,
    val bestScore: Int = 0,
    val moveCount: Int = 0,
    val status: GameStatus = GameStatus.PLAYING,
    val hammerUses: Int = 3,
    val switchUses: Int = 3,
    val undoUses: Int = 3,
    val activeTool: ActiveTool = ActiveTool.NONE,
    val firstSelectedTile: Pair<Int, Int>? = null, // (col, row)
    val aimingColumn: Int? = null,
    val targetTile: Int = 32,
    val canUndo: Boolean = false,
    val isContinued: Boolean = false
)
