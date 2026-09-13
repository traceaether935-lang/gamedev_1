package com.game.a2048

enum class MissionType {
    TILE_REACH,
    MOVE_LIMIT_TILE,
    SCORE_REACH,
    COMBO_MERGE
}

enum class MissionCategory(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    NORMAL("Normal")
}

data class Mission(
    val id: Int,
    val title: String,
    val description: String,
    val type: MissionType,
    val targetValue: Int,
    val moveLimit: Int = 0,
    val rewardStars: Int = 3,
    val category: MissionCategory = MissionCategory.EASY
)
