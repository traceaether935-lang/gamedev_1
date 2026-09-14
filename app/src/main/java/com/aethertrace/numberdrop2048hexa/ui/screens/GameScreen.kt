package com.aethertrace.numberdrop2048hexa.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.MutableContextWrapper
import android.view.ContextThemeWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aethertrace.numberdrop2048hexa.ads.AdMobManager
import com.aethertrace.numberdrop2048hexa.ui.components.BannerAd
import com.aethertrace.numberdrop2048hexa.ui.components.FloatingFeedbackText
import com.aethertrace.numberdrop2048hexa.ui.components.GameBoard
import com.aethertrace.numberdrop2048hexa.ui.components.GameOverDialog
import com.aethertrace.numberdrop2048hexa.ui.components.GameToolbar
import com.aethertrace.numberdrop2048hexa.ui.components.MissionCard
import com.aethertrace.numberdrop2048hexa.ui.components.MissionCompleteDialog
import com.aethertrace.numberdrop2048hexa.ui.components.MissionFailedDialog
import com.aethertrace.numberdrop2048hexa.ui.components.PauseDialog
import com.aethertrace.numberdrop2048hexa.ui.components.ScoreCard
import com.aethertrace.numberdrop2048hexa.ui.components.TargetMilestoneCard
import com.aethertrace.numberdrop2048hexa.ui.components.WinDialog
import com.aethertrace.numberdrop2048hexa.ui.theme._2049Theme
import com.aethertrace.numberdrop2048hexa.ui.viewmodel.GameViewModel
import com.aethertrace.numberdrop2048hexa.ui.viewmodel.GameViewModelFactory
import com.aethertrace.numberdrop2048hexa.util.SoundManager
import com.game.a2048.GameStatus

private fun Context.findActivity(): Activity? {
    var currentContext: Context? = this
    val visited = mutableSetOf<Context>()
    while (currentContext != null && visited.add(currentContext)) {
        if (currentContext is Activity) return currentContext
        currentContext = when (currentContext) {
            is ContextThemeWrapper -> currentContext.baseContext
            is MutableContextWrapper -> currentContext.baseContext
            is ContextWrapper -> currentContext.baseContext
            else -> null
        }
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    rows: Int = 4,
    cols: Int = 4,
    targetGoal: Int = 32,
    missionId: Int? = null,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val viewModel: GameViewModel = viewModel(
        key = "GameViewModel_${rows}_${cols}_${targetGoal}_${missionId}",
        factory = GameViewModelFactory(context, rows, cols, targetGoal, missionId)
    )

    val uiState by viewModel.uiState.collectAsState()
    val feedbackEvent by viewModel.feedbackEvent.collectAsState()
    val undoUses by viewModel.undoUses.collectAsState()
    val hammerUses by viewModel.hammerUses.collectAsState()
    val switchUses by viewModel.switchUses.collectAsState()
    val activeTool by viewModel.activeTool.collectAsState()

    val hapticFeedback = LocalHapticFeedback.current
    val soundManager = remember { SoundManager() }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.leaveGame()
            soundManager.release()
        }
    }

    fun handleNavigateHome() {
        viewModel.leaveGame()
        AdMobManager.onHomeNavigation(context) {
            onNavigateBack()
        }
    }

    fun handleRestart() {
        AdMobManager.onGameRestart(context) {
            viewModel.restart()
        }
    }

    BackHandler {
        if (!uiState.isPauseDialogShown && !uiState.isWinDialogShown &&
            !uiState.isMissionCompleteDialogShown && !uiState.isMissionFailedDialogShown &&
            uiState.gameState.status == GameStatus.PLAYING
        ) {
            viewModel.pause()
        } else {
            handleNavigateHome()
        }
    }

    val isSwipeEnabled = !uiState.isPauseDialogShown &&
            !uiState.isWinDialogShown &&
            !uiState.isMissionCompleteDialogShown &&
            !uiState.isMissionFailedDialogShown &&
            uiState.gameState.status == GameStatus.PLAYING

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.gameState.activeMission != null)
                            "Mission ${uiState.gameState.activeMission?.id}"
                        else "$rows × $cols Mode",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.leaveGame()
                            AdMobManager.onBackNavigation(context) {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.pause() },
                        enabled = isSwipeEnabled,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Pause,
                            contentDescription = "Pause"
                        )
                    }
                    IconButton(
                        onClick = { handleRestart() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Restart"
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.gameState.activeMission != null) {
                MissionCard(gameState = uiState.gameState)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScoreCard(
                    title = "SCORE",
                    score = uiState.gameState.currentScore,
                    modifier = Modifier.weight(1f)
                )
                ScoreCard(
                    title = "BEST",
                    score = uiState.gameState.bestScore,
                    modifier = Modifier.weight(1f)
                )
            }

            // Target Badge in Red Box Area (space above game board)
            if (uiState.gameState.activeMission == null) {
                TargetMilestoneCard(
                    targetTile = uiState.gameState.targetGoal,
                    highestTile = uiState.gameState.grid.flatten().maxOrNull() ?: 0,
                    themeMode = uiState.userSettings.themeMode,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                GameBoard(
                    grid = uiState.gameState.grid,
                    isSwipeEnabled = isSwipeEnabled,
                    onSwipe = { direction ->
                        viewModel.onSwipe(
                            direction = direction,
                            onHaptic = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onSound = { isMerge ->
                                if (isMerge) soundManager.playMergeSound()
                                else soundManager.playMoveSound()
                            }
                        )
                    },
                    onTileClick = { r, c ->
                        viewModel.onTileClick(r, c)
                    },
                    modifier = Modifier.fillMaxSize()
                )

                FloatingFeedbackText(
                    feedbackEvent = feedbackEvent,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // In-game Tool System (GameToolbar) stacked above BannerAd
            GameToolbar(
                undoUses = undoUses,
                hammerUses = hammerUses,
                switchUses = switchUses,
                activeTool = activeTool,
                onUndoClick = {
                    viewModel.onUndoToolClicked { onRewardEarned ->
                        if (activity != null) {
                            AdMobManager.showRewardedAd(activity, onRewardEarned)
                        } else {
                            onRewardEarned()
                        }
                    }
                },
                onHammerClick = {
                    viewModel.onHammerToolClicked { onRewardEarned ->
                        if (activity != null) {
                            AdMobManager.showRewardedAd(activity, onRewardEarned)
                        } else {
                            onRewardEarned()
                        }
                    }
                },
                onSwitchClick = {
                    viewModel.onSwitchToolClicked { onRewardEarned ->
                        if (activity != null) {
                            AdMobManager.showRewardedAd(activity, onRewardEarned)
                        } else {
                            onRewardEarned()
                        }
                    }
                },
                isEnabled = isSwipeEnabled,
                modifier = Modifier.fillMaxWidth()
            )

            BannerAd(
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Mission Complete Dialog
        if (uiState.isMissionCompleteDialogShown && uiState.gameState.activeMission != null) {
            MissionCompleteDialog(
                mission = uiState.gameState.activeMission!!,
                onNextMission = { viewModel.nextMission() },
                onContinueFreeplay = { viewModel.continueFreeplay() },
                onRetryMission = { handleRestart() }
            )
        }

        // Mission Failed Dialog
        if (uiState.isMissionFailedDialogShown && uiState.gameState.activeMission != null) {
            MissionFailedDialog(
                mission = uiState.gameState.activeMission!!,
                moveCount = uiState.gameState.moveCount,
                maxTile = uiState.gameState.grid.flatten().maxOrNull() ?: 0,
                onRetryMission = { handleRestart() },
                onChangeMission = { handleNavigateHome() }
            )
        }

        if (uiState.isWinDialogShown && !uiState.isMissionCompleteDialogShown) {
            WinDialog(
                targetGoal = uiState.gameState.targetGoal,
                score = uiState.gameState.currentScore,
                onKeepPlaying = { viewModel.continueGame() },
                onNewGame = { handleRestart() },
                onHome = { handleNavigateHome() },
                onDismissRequest = { viewModel.dismissWinDialog() }
            )
        }

        if (uiState.gameState.status == GameStatus.GAME_OVER && !uiState.isMissionFailedDialogShown) {
            GameOverDialog(
                score = uiState.gameState.currentScore,
                bestScore = uiState.gameState.bestScore,
                onRestart = { handleRestart() },
                onHome = { handleNavigateHome() }
            )
        }

        if (uiState.isPauseDialogShown) {
            PauseDialog(
                subtitle = "Target Goal: ${uiState.gameState.targetGoal}\nGame is currently paused.",
                onResume = { viewModel.resume() },
                onRestart = { handleRestart() },
                onHome = { handleNavigateHome() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    _2049Theme {
        GameScreen(
            rows = 4,
            cols = 4,
            targetGoal = 32,
            onNavigateBack = {},
            onNavigateToSettings = {}
        )
    }
}
