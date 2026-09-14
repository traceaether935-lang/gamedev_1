package com.example.a2049.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.a2049.auth.PlayGamesAuthManager
import com.example.a2049.billing.BillingManager
import com.example.a2049.ui.screens.ColumnDropScreen
import com.example.a2049.ui.screens.GameScreen
import com.example.a2049.ui.screens.HexagonMergeScreen
import com.example.a2049.ui.screens.HomeScreen
import com.example.a2049.ui.screens.SettingsScreen
import com.example.a2049.ui.screens.StatisticsScreen
import com.example.a2049.ui.screens.StoreScreen
import com.example.a2049.ui.viewmodel.GameViewModel
import com.example.a2049.ui.viewmodel.GameViewModelFactory
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute : NavKey

@Serializable
data class GameRoute(
    val rows: Int,
    val cols: Int,
    val targetGoal: Int = 2048,
    val missionId: Int? = null
) : NavKey

@Serializable
data class HexagonMergeRoute(
    val gridRadius: Int = 2
) : NavKey

@Serializable
object ColumnDropRoute : NavKey

@Serializable
object SettingsRoute : NavKey

@Serializable
object StatisticsRoute : NavKey

@Serializable
object StoreRoute : NavKey

@Composable
fun AppNavigation(
    playGamesAuthManager: PlayGamesAuthManager,
    billingManager: BillingManager?,
    modifier: Modifier = Modifier
) {
    val startDestination: NavKey = HomeRoute
    val backStack = rememberNavBackStack(startDestination)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreen(
                    playGamesAuthManager = playGamesAuthManager,
                    onPlayClicked = { rows, cols, targetGoal, missionId ->
                        backStack.add(GameRoute(rows, cols, targetGoal, missionId))
                    },
                    onHexagonModeClicked = { radius ->
                        backStack.add(HexagonMergeRoute(gridRadius = radius))
                    },
                    onColumnDropClicked = {
                        backStack.add(ColumnDropRoute)
                    },
                    onSettingsClicked = {
                        backStack.add(SettingsRoute)
                    },
                    onStatisticsClicked = {
                        backStack.add(StatisticsRoute)
                    },
                    onStoreClicked = {
                        backStack.add(StoreRoute)
                    }
                )
            }
            entry<GameRoute> { key ->
                GameScreen(
                    rows = key.rows,
                    cols = key.cols,
                    targetGoal = key.targetGoal,
                    missionId = key.missionId,
                    onNavigateBack = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    },
                    onNavigateToSettings = {
                        backStack.add(SettingsRoute)
                    }
                )
            }
            entry<HexagonMergeRoute> { key ->
                HexagonMergeScreen(
                    gridRadius = key.gridRadius,
                    onNavigateBack = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    },
                    onNavigateToSettings = {
                        backStack.add(SettingsRoute)
                    }
                )
            }
            entry<ColumnDropRoute> {
                ColumnDropScreen(
                    onNavigateBack = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    },
                    onNavigateToSettings = {
                        backStack.add(SettingsRoute)
                    }
                )
            }
            entry<SettingsRoute> {
                SettingsScreen(
                    playGamesAuthManager = playGamesAuthManager,
                    onNavigateBack = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }
                )
            }
            entry<StatisticsRoute> {
                StatisticsScreen(
                    onNavigateBack = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }
                )
            }
            entry<StoreRoute> {
                val context = LocalContext.current
                val gameViewModel: GameViewModel = viewModel(
                    factory = GameViewModelFactory(
                        context = context,
                        rows = 4,
                        cols = 4,
                        playGamesAuthManager = playGamesAuthManager,
                        billingManager = billingManager
                    )
                )
                StoreScreen(
                    billingManager = billingManager,
                    gameViewModel = gameViewModel,
                    onNavigateBack = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }
                )
            }
        }
    )
}
