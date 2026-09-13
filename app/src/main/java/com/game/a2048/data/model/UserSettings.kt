package com.game.a2048.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val themeMode: String = "DEFAULT" // Using String for DataStore compatibility if needed, or enum.
)
