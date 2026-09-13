package com.example.a2049

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.a2049.ui.navigation.AppNavigation
import com.example.a2049.ui.theme._2049Theme
import com.example.a2049.ui.theme.AppThemeMode
import com.game.a2048.data.SettingsRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val settingsRepository = SettingsRepository(this)
        
        setContent {
            val userSettings by settingsRepository.userSettingsFlow.collectAsState(initial = null)
            
            val themeMode = try {
                AppThemeMode.valueOf(userSettings?.themeMode ?: "DEFAULT")
            } catch (e: Exception) {
                AppThemeMode.DEFAULT
            }
            
            _2049Theme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
