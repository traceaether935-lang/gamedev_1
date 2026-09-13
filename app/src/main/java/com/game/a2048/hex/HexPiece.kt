package com.game.a2048.hex

import java.util.UUID
import kotlin.random.Random

data class HexPiece(
    val id: String = UUID.randomUUID().toString(),
    val values: List<Int>
) {
    companion object {
        private val SPAWN_VALUES = listOf(2, 4, 8, 16)

        fun random(): HexPiece {
            val rand = Random.nextFloat()
            val values = when {
                rand < 0.34f -> listOf(SPAWN_VALUES.random())
                rand < 0.67f -> listOf(SPAWN_VALUES.random(), SPAWN_VALUES.random())
                else -> listOf(SPAWN_VALUES.random(), SPAWN_VALUES.random(), SPAWN_VALUES.random())
            }
            return HexPiece(values = values)
        }

        fun createSingle(value: Int): HexPiece {
            return HexPiece(values = listOf(value))
        }

        fun createPair(v1: Int, v2: Int): HexPiece {
            return HexPiece(values = listOf(v1, v2))
        }

        fun createTriple(v1: Int, v2: Int, v3: Int): HexPiece {
            return HexPiece(values = listOf(v1, v2, v3))
        }
    }
}
