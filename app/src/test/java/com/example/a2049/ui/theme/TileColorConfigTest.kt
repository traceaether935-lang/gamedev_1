package com.example.a2049.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TileColorConfigTest {

    @Test
    fun testEmptyTileColor() {
        val colors = getTileColors(0, AppThemeMode.DARK)
        assertEquals(Color(0x33888888), colors.background)
        assertEquals(Color.Transparent, colors.text)
    }

    @Test
    fun testDarkThemeTileColors() {
        val colors2 = getTileColors(2, AppThemeMode.DARK)
        assertEquals(Color(0xFF2A2D34), colors2.background)

        val colors4 = getTileColors(4, AppThemeMode.DARK)
        assertEquals(Color(0xFF3A3F47), colors4.background)

        val colors8 = getTileColors(8, AppThemeMode.DARK)
        assertEquals(Color(0xFF4E5664), colors8.background)

        val colors16 = getTileColors(16, AppThemeMode.DARK)
        assertEquals(Color(0xFF626E80), colors16.background)

        val colors2048 = getTileColors(2048, AppThemeMode.DARK)
        assertEquals(Color(0xFFD81B60), colors2048.background)
        assertEquals(Color.White, colors2048.text)

        val colors131072 = getTileColors(131072, AppThemeMode.DARK)
        assertEquals(Color(0xFFBA68C8), colors131072.background)
    }

    @Test
    fun testAllThemeModesReturnColors() {
        AppThemeMode.entries.forEach { mode ->
            val tile2 = getTileColors(2, mode)
            assertNotEquals(Color.Unspecified, tile2.background)
        }
    }
}
