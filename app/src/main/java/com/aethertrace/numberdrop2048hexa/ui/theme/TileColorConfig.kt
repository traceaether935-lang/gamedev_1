package com.aethertrace.numberdrop2048hexa.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

data class TileColors(
    val background: Color,
    val text: Color = if (background.luminance() > 0.5f) Color(0xFF1E1E1E) else Color.White
)

fun getTileColors(value: Int, themeMode: AppThemeMode): TileColors {
    if (value == 0) return TileColors(background = Color(0x33888888), text = Color.Transparent)
    
    val palette = when (themeMode) {
        AppThemeMode.DEFAULT -> defaultTilePalette
        AppThemeMode.DARK -> darkTilePalette
        AppThemeMode.NEON -> neonTilePalette
        AppThemeMode.PASTEL -> pastelTilePalette
    }

    return palette[value] ?: TileColors(background = Color(0xFF3C3A32), text = Color.White) // Default for extremely high unmapped values
}

private val defaultTilePalette = mapOf(
    2 to TileColors(Color(0xFFEEE4DA)),
    4 to TileColors(Color(0xFFEDE0C8)),
    8 to TileColors(Color(0xFFF2B179), Color.White),
    16 to TileColors(Color(0xFFF59563), Color.White),
    32 to TileColors(Color(0xFFF67C5F), Color.White),
    64 to TileColors(Color(0xFFF65E3B), Color.White),
    128 to TileColors(Color(0xFFEDCF72), Color.White),
    256 to TileColors(Color(0xFFEDCC61), Color.White),
    512 to TileColors(Color(0xFFEDC850), Color.White),
    1024 to TileColors(Color(0xFFEDC53F), Color.White),
    2048 to TileColors(Color(0xFFEDC22E), Color.White),
    4096 to TileColors(Color(0xFF3E3933), Color.White),
    8192 to TileColors(Color(0xFF4C443C), Color.White),
    16384 to TileColors(Color(0xFF5A5045), Color.White),
    32768 to TileColors(Color(0xFF685D4E), Color.White),
    65536 to TileColors(Color(0xFF766957), Color.White),
    131072 to TileColors(Color(0xFF847660), Color.White)
)

private val darkTilePalette = mapOf(
    2 to TileColors(Color(0xFF2A2D34), Color(0xFFE0E0E0)),
    4 to TileColors(Color(0xFF3A3F47), Color(0xFFE0E0E0)),
    8 to TileColors(Color(0xFF4E5664), Color.White),
    16 to TileColors(Color(0xFF626E80), Color.White),
    32 to TileColors(Color(0xFF00838F), Color.White),
    64 to TileColors(Color(0xFF00ACC1), Color.White),
    128 to TileColors(Color(0xFF1E88E5), Color.White),
    256 to TileColors(Color(0xFF3949AB), Color.White),
    512 to TileColors(Color(0xFF5E35B1), Color.White),
    1024 to TileColors(Color(0xFF8E24AA), Color.White),
    2048 to TileColors(Color(0xFFD81B60), Color.White),
    4096 to TileColors(Color(0xFFFFB300), Color(0xFF121212)),
    8192 to TileColors(Color(0xFFFB8C00), Color.White),
    16384 to TileColors(Color(0xFFF4511E), Color.White),
    32768 to TileColors(Color(0xFF7CB342), Color.White),
    65536 to TileColors(Color(0xFF00897B), Color.White),
    131072 to TileColors(Color(0xFFBA68C8), Color.White)
)

private val neonTilePalette = mapOf(
    2 to TileColors(Color(0xFF0F0F0F), Color(0xFF00FF00)),
    4 to TileColors(Color(0xFF1A1A1A), Color(0xFF00FFCC)),
    8 to TileColors(Color(0xFF003300), Color(0xFF33FF33)),
    16 to TileColors(Color(0xFF004444), Color(0xFF33FFFF)),
    32 to TileColors(Color(0xFF330000), Color(0xFFFF3333)),
    64 to TileColors(Color(0xFF440022), Color(0xFFFF3399)),
    128 to TileColors(Color(0xFF444400), Color(0xFFFFFF33)),
    256 to TileColors(Color(0xFF555500), Color(0xFFFFFF66)),
    512 to TileColors(Color(0xFF666600), Color(0xFFFFFF99)),
    1024 to TileColors(Color(0xFF777700), Color(0xFFFFFFCC)),
    2048 to TileColors(Color(0xFF888800), Color(0xFFFFFFFF)),
    4096 to TileColors(Color(0xFF220044), Color(0xFFCC66FF)),
    8192 to TileColors(Color(0xFF330055), Color(0xFFDD88FF)),
    16384 to TileColors(Color(0xFF440066), Color(0xFFEEAAFF)),
    32768 to TileColors(Color(0xFF550077), Color(0xFFFFCCFF)),
    65536 to TileColors(Color(0xFF660088), Color(0xFFFFDDFF)),
    131072 to TileColors(Color(0xFF770099), Color(0xFFFFEEFF))
)

private val pastelTilePalette = mapOf(
    2 to TileColors(Color(0xFFFFB3BA)),
    4 to TileColors(Color(0xFFFFDFBA)),
    8 to TileColors(Color(0xFFFFFFBA)),
    16 to TileColors(Color(0xFFBaffC9)),
    32 to TileColors(Color(0xFFBAE1FF)),
    64 to TileColors(Color(0xFFD3B8FF)),
    128 to TileColors(Color(0xFFFFC4E1)),
    256 to TileColors(Color(0xFFFFD1C4)),
    512 to TileColors(Color(0xFFFFF2C4)),
    1024 to TileColors(Color(0xFFD4FFC4)),
    2048 to TileColors(Color(0xFFC4E8FF)),
    4096 to TileColors(Color(0xFFE4C4FF)),
    8192 to TileColors(Color(0xFFFFC4D4)),
    16384 to TileColors(Color(0xFFFFDFC4)),
    32768 to TileColors(Color(0xFFFFFFC4)),
    65536 to TileColors(Color(0xFFC4FFD4)),
    131072 to TileColors(Color(0xFFC4E4FF))
)
