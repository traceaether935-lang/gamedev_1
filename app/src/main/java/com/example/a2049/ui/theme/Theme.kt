package com.example.a2049.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkThemeColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFB3C8E8),
    onSecondary = Color(0xFF1C314B),
    secondaryContainer = Color(0xFF334763),
    onSecondaryContainer = Color(0xFFD3E4FF),
    tertiary = Color(0xFF80CBC4),
    onTertiary = Color(0xFF003733),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E24),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF1E2028),
    onSurfaceVariant = Color(0xFFC4C6CF),
    surfaceContainerLow = Color(0xFF18181F),
    surfaceContainer = Color(0xFF22222B)
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val NeonColorScheme = darkColorScheme(
    primary = Color(0xFF00FF00),
    secondary = Color(0xFF00FFCC),
    tertiary = Color(0xFFFF3399),
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF1A1A1A)
)

private val PastelColorScheme = lightColorScheme(
    primary = Color(0xFFFFB3BA),
    secondary = Color(0xFFFFDFBA),
    tertiary = Color(0xFFBAE1FF),
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF)
)

@Composable
fun _2049Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    themeMode: AppThemeMode = AppThemeMode.DEFAULT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.DARK -> DarkThemeColorScheme
        AppThemeMode.NEON -> NeonColorScheme
        AppThemeMode.PASTEL -> PastelColorScheme
        AppThemeMode.DEFAULT -> when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    }

    CompositionLocalProvider(LocalAppThemeMode provides themeMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
