package com.game.a2048.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val themeMode: String = "DEFAULT",
    val isAdFree: Boolean = false,
    val hammerUses: Int = 2,
    val switchUses: Int = 2,
    val undoUses: Int = 2
)
