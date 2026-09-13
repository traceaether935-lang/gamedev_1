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
import com.example.a2049.ui.components.FloatingFeedbackText
import com.example.a2049.ui.components.HexBoard
import com.example.a2049.ui.components.ScoreCard
import com.example.a2049.ui.components.TargetMilestoneCard
import com.example.a2049.ui.components.UpcomingPiecesTray
import com.example.a2049.ui.components.findCellAtOffset
import com.example.a2049.ui.theme.AppThemeMode
import com.example.a2049.ui.viewmodel.HexagonMergeViewModel
import com.example.a2049.ui.viewmodel.HexagonMergeViewModelFactory
import com.example.a2049.util.SoundManager
import com.game.a2048.GameStatus
import com.game.a2048.hex.HexCell
import com.game.a2048.hex.HexGrid
import com.game.a2048.hex.PowerUpType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HexagonMergeScreen(
    gridRadius: Int = HexGrid.DEFAULT_RADIUS,
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: HexagonMergeViewModel = viewModel(
        key = "hex_$gridRadius",
        factory = HexagonMergeViewModelFactory(context, gridRadius)
    )

    val uiState by viewModel.uiState.collectAsState()
    val feedbackEvent by viewModel.feedbackEvent.collectAsState()
    val upcomingPieces by viewModel.upcomingPieces.collectAsState()
    val swapFirstCell by viewModel.swapFirstCell.collectAsState()
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

    BackHandler {
        if (!uiState.isPauseDialogShown && !uiState.isWinDialogShown &&
            uiState.gameState.status == GameStatus.PLAYING
        ) {
            viewModel.pause()
        } else {
            viewModel.leaveGame()
            onNavigateBack()
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

            // Power-up Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val hammerActive = uiState.gameState.activePowerUp == PowerUpType.HAMMER
                FilledTonalIconButton(
                    onClick = {
                        selectedPieceIndex = null
                        viewModel.togglePowerUp(PowerUpType.HAMMER)
                    },
                    enabled = isInteractive && uiState.gameState.hammerCount > 0,
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
                    onClick = {
                        selectedPieceIndex = null
                        viewModel.togglePowerUp(PowerUpType.SWAP)
                    },
                    enabled = isInteractive && uiState.gameState.swapCount > 0,
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

            // Target Milestone Card directly above hex board
            TargetMilestoneCard(
                targetTile = uiState.gameState.targetGoal,
                highestTile = uiState.gameState.grid.values.maxOrNull() ?: 0,
                themeMode = uiState.userSettings.themeMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            val currentThemeMode = try {
                AppThemeMode.valueOf(uiState.userSettings.themeMode)
            } catch (_: Exception) {
                AppThemeMode.DEFAULT
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                HexBoard(
                    grid = uiState.gameState.grid,
                    isInteractive = isInteractive,
                    highlightedCell = hoveredCell,
                    activePieceSize = activePieceSize,
                    radius = uiState.gameState.radius,
                    selectedSwapCell = swapFirstCell,
                    activePowerUp = uiState.gameState.activePowerUp,
                    onBoardPositioned = { pos, w, h ->
                        boardRootOffset = pos
                        boardWidth = w
                        boardHeight = h
                    },
                    onCellClick = { cell ->
                        if (!isInteractive) return@HexBoard
                        when (uiState.gameState.activePowerUp) {
                            PowerUpType.HAMMER -> {
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
                            PowerUpType.SWAP -> {
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
                            null -> {
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

            // Upcoming Pieces Tray at bottom
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
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = { viewModel.undo() },
                    enabled = uiState.gameState.canUndo && isInteractive,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Undo,
                        contentDescription = "Undo",
                        modifier = Modifier.size(26.dp)
                    )
                }

                FilledTonalIconButton(
                    onClick = { viewModel.pause() },
                    enabled = isInteractive,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(26.dp)
                    )
                }

                FilledTonalIconButton(
                    onClick = { viewModel.restart() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Restart",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        if (uiState.isWinDialogShown) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissWinDialog() },
                title = { Text(text = "You Win! 🎉", fontWeight = FontWeight.Bold) },
                text = {
                    Text(text = "Congratulations! You reached the 2048 Hex Tile!\nScore: ${uiState.gameState.currentScore}")
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

        if (uiState.gameState.status == GameStatus.GAME_OVER) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(text = "Game Over! 😔", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "No available hex merges left.\n\nFinal Score: ${uiState.gameState.currentScore}\nBest Score: ${uiState.gameState.bestScore}"
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
                text = { Text(text = "Hexagon Merge Mode\nGame is currently paused.") },
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
