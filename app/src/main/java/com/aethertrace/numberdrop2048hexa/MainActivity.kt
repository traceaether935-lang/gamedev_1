package com.aethertrace.numberdrop2048hexa

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.aethertrace.numberdrop2048hexa.ads.AdMobManager
import com.aethertrace.numberdrop2048hexa.auth.PlayGamesAuthManager
import com.aethertrace.numberdrop2048hexa.billing.BillingManager
import com.aethertrace.numberdrop2048hexa.ui.navigation.AppNavigation
import com.aethertrace.numberdrop2048hexa.ui.theme._2049Theme
import com.aethertrace.numberdrop2048hexa.ui.theme.AppThemeMode
import com.game.a2048.data.SettingsRepository
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.games.PlayGamesSdk
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var playGamesAuthManager: PlayGamesAuthManager
    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        PlayGamesSdk.initialize(this)
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()

        MobileAds.initialize(this)

        playGamesAuthManager = PlayGamesAuthManager(this)

        val settingsRepository = SettingsRepository(this)

        billingManager = BillingManager(
            context = applicationContext,
            onRemoveAdsPurchased = {
                lifecycleScope.launch {
                    settingsRepository.setAdFree(true)
                    AdMobManager.isAdFree = true
                }
            },
            onConsumablePurchased = { productId ->
                lifecycleScope.launch {
                    when (productId) {
                        BillingManager.SKU_BUY_HAMMER_PACK -> settingsRepository.addHammerUses(5)
                        BillingManager.SKU_BUY_SWITCH_PACK -> settingsRepository.addSwitchUses(5)
                        BillingManager.SKU_BUY_UNDO_PACK -> settingsRepository.addUndoUses(5)
                    }
                }
            }
        )

        AdMobManager.loadRewardedAd(this)
        AdMobManager.loadInterstitialAd(this)

        setContent {
            val userSettings by settingsRepository.userSettingsFlow.collectAsState(initial = null)

            LaunchedEffect(userSettings?.isAdFree) {
                AdMobManager.isAdFree = userSettings?.isAdFree == true
            }

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
                    AppNavigation(
                        playGamesAuthManager = playGamesAuthManager,
                        billingManager = billingManager
                    )
                }
            }
        }
    }
}
