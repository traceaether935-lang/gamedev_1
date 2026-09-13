package com.game.a2048.hex

object HexGrid {
    const val DEFAULT_RADIUS = 2

    val ALL_CELLS: List<HexCell> get() = getCells(DEFAULT_RADIUS)

    fun getCells(radius: Int): List<HexCell> = buildList {
        for (q in -radius..radius) {
            for (r in -radius..radius) {
                if (q + r in -radius..radius) {
                    add(HexCell(q, r))
                }
            }
        }
    }

    fun contains(cell: HexCell, radius: Int = DEFAULT_RADIUS): Boolean {
        return cell.q in -radius..radius &&
                cell.r in -radius..radius &&
                (cell.q + cell.r) in -radius..radius
    }

    fun getBoardIdForRadius(radius: Int): Int {
        return getCells(radius).size
    }

    fun getRadiusForBoardId(boardId: Int): Int {
        return when (boardId) {
            19 -> 2
            37 -> 3
            61 -> 4
            else -> 2
        }
    }
}
