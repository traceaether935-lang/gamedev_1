package com.example.a2049.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a2049.ads.AdMobManager
import com.example.a2049.ui.components.BannerAd
import com.example.a2049.ui.components.FloatingFeedbackText
import com.example.a2049.ui.components.GameOverDialog
import com.example.a2049.ui.components.GameToolbar
import com.example.a2049.ui.components.HexBoard
import com.example.a2049.ui.components.PauseDialog
import com.example.a2049.ui.components.ScoreCard
import com.example.a2049.ui.components.TargetMilestoneCard
import com.example.a2049.ui.components.UpcomingPiecesTray
import com.example.a2049.ui.components.WinDialog
import com.example.a2049.ui.components.findCellAtOffset
import com.example.a2049.ui.model.ActiveTool
import com.example.a2049.ui.theme.AppThemeMode
import com.example.a2049.ui.viewmodel.HexagonMergeViewModel
import com.example.a2049.ui.viewmodel.HexagonMergeViewModelFactory
import com.example.a2049.util.SoundManager
import com.game.a2048.GameStatus
import com.game.a2048.hex.HexCell
import com.game.a2048.hex.HexGrid

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HexagonMergeScreen(
    gridRadius: Int = HexGrid.DEFAULT_RADIUS,
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val viewModel: HexagonMergeViewModel = viewModel(
        key = "hex_$gridRadius",
        factory = HexagonMergeViewModelFactory(context, gridRadius)
    )

    val uiState by viewModel.uiState.collectAsState()
    val feedbackEvent by viewModel.feedbackEvent.collectAsState()
    val upcomingPieces by viewModel.upcomingPieces.collectAsState()
    val swapFirstCell by viewModel.swapFirstCell.collectAsState()
    val undoUses by viewModel.undoUses.collectAsState()
    val hammerUses by viewModel.hammerUses.collectAsState()
    val switchUses by viewModel.switchUses.collectAsState()
    val activeTool by viewModel.activeTool.collectAsState()

    val hapticFeedback = LocalHapticFeedback.current
    val soundManager = remember { SoundManager() }

    var selectedPieceIndex by remember { mutableStateOf<Int?>(null) }
    var draggedPieceIndex by remember { mutableStateOf<Int?>(null) }
    var hoveredCell by remember { mutableStateOf<HexCell?>(null) }
    var boardRootOffset by remember { mutableStateOf(Offset.Zero) }
    var boardWidth by remember { mutableStateOf(0f) }
    var boardHeight by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.leaveGame()
            soundManager.release()
        }
    }

    fun handleNavigateHome() {
        viewModel.leaveGame()
        if (activity != null) {
            AdMobManager.onHomeNavigation(activity) {
                onNavigateBack()
            }
        } else {
            onNavigateBack()
        }
    }

    fun handleRestart() {
        if (activity != null) {
            AdMobManager.onGameRestart(activity) {
                viewModel.restart()
            }
        } else {
            viewModel.restart()
        }
    }

    BackHandler {
        if (!uiState.isPauseDialogShown && !uiState.isWinDialogShown &&
            uiState.gameState.status == GameStatus.PLAYING
        ) {
            viewModel.pause()
        } else {
            handleNavigateHome()
        }
    }

    val isInteractive = !uiState.isPauseDialogShown &&
            !uiState.isWinDialogShown &&
            uiState.gameState.status == GameStatus.PLAYING

    val activePieceIndex = draggedPieceIndex ?: selectedPieceIndex
    val activePiece = activePieceIndex?.let { upcomingPieces.getOrNull(it) }
    val activePieceSize = activePiece?.values?.size ?: 1

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hexagon Merge",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { handleNavigateHome() },
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
                        enabled = isInteractive,
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

            // Target Milestone Card directly above hex board
            TargetMilestoneCard(
                targetTile = uiState.gameState.targetGoal,
                highestTile = uiState.gameState.grid.values.maxOrNull() ?: 0,
                themeMode = uiState.userSettings.themeMode,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            val currentThemeMode = try {
                AppThemeMode.valueOf(uiState.userSettings.themeMode)
            } catch (_: Exception) {
                AppThemeMode.DEFAULT
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HexBoard(
                    grid = uiState.gameState.grid,
                    isInteractive = isInteractive,
                    highlightedCell = hoveredCell,
                    activePieceSize = activePieceSize,
                    radius = uiState.gameState.radius,
                    selectedSwapCell = swapFirstCell,
                    activePowerUp = null,
                    onBoardPositioned = { pos, w, h ->
                        boardRootOffset = pos
                        boardWidth = w
                        boardHeight = h
                    },
                    onCellClick = { cell ->
                        if (!isInteractive) return@HexBoard
                        when (activeTool) {
                            ActiveTool.HAMMER -> {
                                if ((uiState.gameState.grid[cell] ?: 0) != 0) {
                                    viewModel.useHammer(
                                        cell = cell,
                                        onHaptic = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onSound = { soundManager.playMoveSound() }
                                    )
                                }
                            }
                            ActiveTool.SWITCH -> {
                                val first = swapFirstCell
                                if (first == null) {
                                    if ((uiState.gameState.grid[cell] ?: 0) != 0) {
                                        viewModel.selectSwapCell(cell)
                                    }
                                } else if (first == cell) {
                                    viewModel.clearSwapCell()
                                } else {
                                    viewModel.useSwap(
                                        cell1 = first,
                                        cell2 = cell,
                                        onHaptic = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onSound = { soundManager.playMoveSound() }
                                    )
                                }
                            }
                            ActiveTool.NONE -> {
                                val pieceIdx = selectedPieceIndex
                                if (pieceIdx != null) {
                                    viewModel.placePiece(
                                        pieceIndex = pieceIdx,
                                        targetCell = cell,
                                        onHaptic = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onSound = { isMerge ->
                                            if (isMerge) soundManager.playMergeSound()
                                            else soundManager.playMoveSound()
                                        }
                                    )
                                    selectedPieceIndex = null
                                    hoveredCell = null
                                }
                            }
                        }
                    },
                    themeMode = currentThemeMode,
                    modifier = Modifier.fillMaxSize()
                )

                FloatingFeedbackText(
                    feedbackEvent = feedbackEvent,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Upcoming Pieces Tray
            UpcomingPiecesTray(
                upcomingPieces = upcomingPieces,
                selectedPieceIndex = selectedPieceIndex,
                onPieceSelected = { index ->
                    viewModel.cancelPowerUp()
                    selectedPieceIndex = if (selectedPieceIndex == index) null else index
                },
                onPieceDragStart = { index, _ ->
                    viewModel.cancelPowerUp()
                    draggedPieceIndex = index
                    selectedPieceIndex = index
                },
                onPieceDrag = { _, globalOffset ->
                    val localOffset = globalOffset - boardRootOffset
                    hoveredCell = findCellAtOffset(localOffset, boardWidth, boardHeight, uiState.gameState.radius)
                },
                onPieceDragEnd = { index ->
                    val cell = hoveredCell
                    if (cell != null && isInteractive) {
                        viewModel.placePiece(
                            pieceIndex = index,
                            targetCell = cell,
                            onHaptic = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onSound = { isMerge ->
                                if (isMerge) soundManager.playMergeSound()
                                else soundManager.playMoveSound()
                            }
                        )
                        selectedPieceIndex = null
                    }
                    draggedPieceIndex = null
                    hoveredCell = null
                },
                themeMode = currentThemeMode,
                modifier = Modifier
            )

            // GameToolbar
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
                    selectedPieceIndex = null
                    viewModel.onHammerToolClicked { onRewardEarned ->
                        if (activity != null) {
                            AdMobManager.showRewardedAd(activity, onRewardEarned)
                        } else {
                            onRewardEarned()
                        }
                    }
                },
                onSwitchClick = {
                    selectedPieceIndex = null
                    viewModel.onSwitchToolClicked { onRewardEarned ->
                        if (activity != null) {
                            AdMobManager.showRewardedAd(activity, onRewardEarned)
                        } else {
                            onRewardEarned()
                        }
                    }
                },
                isEnabled = isInteractive,
                modifier = Modifier.fillMaxWidth()
            )

            BannerAd(
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState.isWinDialogShown) {
            WinDialog(
                targetGoal = 2048,
                score = uiState.gameState.currentScore,
                winMessage = "Congratulations! You reached the 2048 Hex Tile!\nScore: ${uiState.gameState.currentScore}",
                onKeepPlaying = { viewModel.continueGame() },
                onNewGame = { handleRestart() },
                onDismissRequest = { viewModel.dismissWinDialog() }
            )
        }

        if (uiState.gameState.status == GameStatus.GAME_OVER) {
            GameOverDialog(
                score = uiState.gameState.currentScore,
                bestScore = uiState.gameState.bestScore,
                message = "No available hex merges left.",
                onRestart = { handleRestart() },
                onHome = { handleNavigateHome() }
            )
        }

        if (uiState.isPauseDialogShown) {
            PauseDialog(
                subtitle = "Hexagon Merge Mode\nGame is currently paused.",
                onResume = { viewModel.resume() },
                onRestart = { handleRestart() },
                onHome = { handleNavigateHome() }
            )
        }
    }
}
