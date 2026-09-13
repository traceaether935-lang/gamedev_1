package com.game.a2048.hex

data class HexCell(val q: Int, val r: Int) {
    fun neighbor(direction: HexDirection): HexCell {
        return when (direction) {
            HexDirection.EAST -> HexCell(q + 1, r)
            HexDirection.NORTH_EAST -> HexCell(q + 1, r - 1)
            HexDirection.NORTH_WEST -> HexCell(q, r - 1)
            HexDirection.WEST -> HexCell(q - 1, r)
            HexDirection.SOUTH_WEST -> HexCell(q - 1, r + 1)
            HexDirection.SOUTH_EAST -> HexCell(q, r + 1)
        }
    }

    fun neighbors(): List<HexCell> {
        return HexDirection.entries.map { neighbor(it) }
    }
}
