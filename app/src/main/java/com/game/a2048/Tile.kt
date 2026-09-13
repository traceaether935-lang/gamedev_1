package com.game.a2048

data class Tile(
    val value: Int = 0,
    val id: Long = 0L
) {
    val isEmpty: Boolean get() = value == 0
}
