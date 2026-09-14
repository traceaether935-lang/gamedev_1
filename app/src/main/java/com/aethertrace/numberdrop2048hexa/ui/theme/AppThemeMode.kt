package com.aethertrace.numberdrop2048hexa.ui.theme

import androidx.compose.runtime.compositionLocalOf

enum class AppThemeMode {
    DEFAULT,
    DARK,
    NEON,
    PASTEL
}

val LocalAppThemeMode = compositionLocalOf { AppThemeMode.DEFAULT }
