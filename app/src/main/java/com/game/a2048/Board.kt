package com.game.a2048

data class Board(
    val rows: Int,
    val cols: Int,
    val grid: List<List<Int>>
) {
    init {
        require(rows > 0 && cols > 0) { "Board dimensions must be positive" }
        require(grid.size == rows && grid.all { it.size == cols }) { "Grid must be rows x cols" }
    }

    operator fun get(row: Int, col: Int): Int = grid[row][col]

    val emptyCells: List<Pair<Int, Int>>
        get() {
            val list = mutableListOf<Pair<Int, Int>>()
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (grid[r][c] == 0) {
                        list.add(r to c)
                    }
                }
            }
            return list
        }

    val isFull: Boolean get() = emptyCells.isEmpty()

    companion object {
        fun createEmpty(rows: Int, cols: Int): Board {
            return Board(rows, cols, List(rows) { List(cols) { 0 } })
        }
    }
}
