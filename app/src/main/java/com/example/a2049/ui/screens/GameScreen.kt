package com.example.a2049.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import com.example.a2049.ui.components.FloatingFeedbackText
import com.example.a2049.ui.components.GameBoard
import com.example.a2049.ui.components.MissionCard
import com.example.a2049.ui.components.MissionCompleteDialog
import com.example.a2049.ui.components.MissionFailedDialog
import com.example.a2049.ui.components.ScoreCard
import com.example.a2049.ui.components.TargetMilestoneCard
import com.example.a2049.ui.theme._2049Theme
import com.example.a2049.ui.viewmodel.GameViewModel
import com.example.a2049.ui.viewmodel.GameViewModelFactory
import com.example.a2049.util.SoundManager
import com.game.a2048.GameStatus
import com.game.a2048.PowerUpType

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
    val viewModel: GameViewModel = viewModel(
        key = "GameViewModel_${rows}_${cols}_${targetGoal}_${missionId}",
        factory = GameViewModelFactory(context, rows, cols, targetGoal, missionId)
    )

    val uiState by viewModel.uiState.collectAsState()
    val feedbackEvent by viewModel.feedbackEvent.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current
    val soundManager = remember { SoundManager() }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.leaveGame()
            soundManager.release()
        }
    }

    BackHandler {
        if (!uiState.isPauseDialogShown && !uiState.isWinDialogShown &&
            !uiState.isMissionCompleteDialogShown && !uiState.isMissionFailedDialogShown &&
            uiState.gameState.status == GameStatus.PLAYING
        ) {
            viewModel.pause()
        } else {
            viewModel.leaveGame()
            onNavigateBack()
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
                            onNavigateBack()
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (uiState.gameState.activeMission != null) {
                MissionCard(gameState = uiState.gameState)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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

            // Power-up controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val hammerActive = uiState.gameState.activePowerUp == PowerUpType.HAMMER
                FilledTonalIconButton(
                    onClick = { viewModel.togglePowerUp(PowerUpType.HAMMER) },
                    enabled = isSwipeEnabled && uiState.gameState.hammerCount > 0,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (hammerActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Build,
                            contentDescription = "Hammer",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(text = "${uiState.gameState.hammerCount}", style = MaterialTheme.typography.labelSmall)
                    }
                }

                val swapActive = uiState.gameState.activePowerUp == PowerUpType.SWAP
                FilledTonalIconButton(
                    onClick = { viewModel.togglePowerUp(PowerUpType.SWAP) },
                    enabled = isSwipeEnabled && uiState.gameState.swapCount > 0,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (swapActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = "Swap",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(text = "${uiState.gameState.swapCount}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Target Badge in Red Box Area (space above game board)
            TargetMilestoneCard(
                targetTile = uiState.gameState.targetGoal,
                highestTile = uiState.gameState.grid.flatten().maxOrNull() ?: 0,
                themeMode = uiState.userSettings.themeMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                GameBoard(
                    grid = uiState.gameState.grid,
                    isSwipeEnabled = isSwipeEnabled && uiState.gameState.activePowerUp == null,
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = { viewModel.undo() },
                    enabled = uiState.gameState.canUndo && isSwipeEnabled,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Undo,
                        contentDescription = "Undo",
                        modifier = Modifier.size(28.dp)
                    )
                }

                FilledTonalIconButton(
                    onClick = { viewModel.pause() },
                    enabled = isSwipeEnabled,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(28.dp)
                    )
                }

                FilledTonalIconButton(
                    onClick = { viewModel.restart() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Restart",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Mission Complete Dialog
        if (uiState.isMissionCompleteDialogShown && uiState.gameState.activeMission != null) {
            MissionCompleteDialog(
                mission = uiState.gameState.activeMission!!,
                onNextMission = { viewModel.nextMission() },
                onContinueFreeplay = { viewModel.continueFreeplay() },
                onRetryMission = { viewModel.retryMission() }
            )
        }

        // Mission Failed Dialog
        if (uiState.isMissionFailedDialogShown && uiState.gameState.activeMission != null) {
            MissionFailedDialog(
                mission = uiState.gameState.activeMission!!,
                moveCount = uiState.gameState.moveCount,
                maxTile = uiState.gameState.grid.flatten().maxOrNull() ?: 0,
                onRetryMission = { viewModel.retryMission() },
                onChangeMission = {
                    viewModel.leaveGame()
                    onNavigateBack()
                }
            )
        }

        if (uiState.isWinDialogShown && !uiState.isMissionCompleteDialogShown) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissWinDialog() },
                title = { Text(text = "You Win! 🎉", fontWeight = FontWeight.Bold) },
                text = {
                    Text(text = "Congratulations! You created the ${uiState.gameState.targetGoal} tile!\nScore: ${uiState.gameState.currentScore}")
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.continueGame() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Keep Playing")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.restart() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("New Game")
                    }
                }
            )
        }

        if (uiState.gameState.status == GameStatus.GAME_OVER && !uiState.isMissionFailedDialogShown) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(text = "Game Over! 😔", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "No available moves left.\n\nFinal Score: ${uiState.gameState.currentScore}\nBest Score: ${uiState.gameState.bestScore}"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.restart() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Try Again")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            viewModel.leaveGame()
                            onNavigateBack()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Home")
                    }
                }
            )
        }

        if (uiState.isPauseDialogShown) {
            AlertDialog(
                onDismissRequest = { viewModel.resume() },
                title = { Text(text = "Game Paused ⏸️", fontWeight = FontWeight.Bold) },
                text = { Text(text = "Target Goal: ${uiState.gameState.targetGoal}\nGame is currently paused.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resume() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Resume")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.restart() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Restart")
                        }
                        TextButton(
                            onClick = {
                                viewModel.leaveGame()
                                onNavigateBack()
                            }
                        ) {
                            Text("Home")
                        }
                    }
                }
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
