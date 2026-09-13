package com.example.a2049.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.a2049.ui.screens.ColumnDropScreen
import com.example.a2049.ui.screens.GameScreen
import com.example.a2049.ui.screens.HexagonMergeScreen
import com.example.a2049.ui.screens.HomeScreen
import com.example.a2049.ui.screens.SettingsScreen
import com.example.a2049.ui.screens.StatisticsScreen
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

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(HomeRoute)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryProvider = entryProvider {
            entry<HomeRoute> {
                HomeScreen(
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
                ColumnDropScreen()
            }
            entry<SettingsRoute> {
                SettingsScreen(
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
        }
    )
}
