package com.aethertrace.numberdrop2048hexa.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aethertrace.numberdrop2048hexa.ads.AdMobManager
import com.aethertrace.numberdrop2048hexa.ui.components.BannerAd
import com.aethertrace.numberdrop2048hexa.ui.components.ColumnDropBoard
import com.aethertrace.numberdrop2048hexa.ui.components.GameOverDialog
import com.aethertrace.numberdrop2048hexa.ui.components.GameToolbar
import com.aethertrace.numberdrop2048hexa.ui.components.LauncherDock
import com.aethertrace.numberdrop2048hexa.ui.components.PauseDialog
import com.aethertrace.numberdrop2048hexa.ui.components.ScoreCard
import com.aethertrace.numberdrop2048hexa.ui.components.TargetMilestoneCard
import com.aethertrace.numberdrop2048hexa.ui.components.WinDialog
import com.aethertrace.numberdrop2048hexa.ui.model.ActiveTool
import com.aethertrace.numberdrop2048hexa.ui.viewmodel.ColumnDropViewModel
import com.aethertrace.numberdrop2048hexa.ui.viewmodel.ColumnDropViewModelFactory
import com.game.a2048.GameStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnDropScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ColumnDropViewModel = viewModel(
        factory = ColumnDropViewModelFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()
    val feedbackEvent by viewModel.feedbackEvent.collectAsState()
    val selectedColumn by viewModel.selectedColumn.collectAsState()

    val gameState = uiState.gameState

    fun handleNavigateHome() {
        AdMobManager.onHomeNavigation(context) {
            onNavigateBack()
        }
    }

    fun handleRestart() {
        AdMobManager.onGameRestart(context) {
            viewModel.resumeGame()
            viewModel.restartGame()
        }
    }

    BackHandler {
        if (!uiState.isPauseDialogShown && !uiState.isGameOverDialogShown && !uiState.isWinDialogShown &&
            gameState.status == GameStatus.PLAYING
        ) {
            viewModel.pauseGame()
        } else {
            handleNavigateHome()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Column Drop",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            AdMobManager.onBackNavigation(context) {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.pauseGame() }) {
                        Icon(
                            imageVector = Icons.Rounded.Pause,
                            contentDescription = "Pause"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScoreCard(
                        title = "SCORE",
                        score = gameState.score,
                        modifier = Modifier.weight(1f)
                    )
                    ScoreCard(
                        title = "BEST",
                        score = gameState.bestScore,
                        modifier = Modifier.weight(1f)
                    )
                }

                val highestTile = gameState.grid.flatten().maxOrNull() ?: 2
                TargetMilestoneCard(
                    targetTile = gameState.targetTile,
                    highestTile = highestTile,
                    themeMode = uiState.userSettings.themeMode,
                    modifier = Modifier.fillMaxWidth()
                )

                ColumnDropBoard(
                    grid = gameState.grid,
                    activeTool = gameState.activeTool,
                    firstSelectedTile = gameState.firstSelectedTile,
                    selectedColumn = selectedColumn,
                    aimingColumn = gameState.aimingColumn,
                    onColumnSelect = { col ->
                        viewModel.setSelectedColumn(col)
                    },
                    onColumnClick = { col ->
                        viewModel.setSelectedColumn(col)
                        viewModel.shootTile(col)
                    },
                    onShootColumn = { col ->
                        viewModel.setSelectedColumn(col)
                        viewModel.shootTile(col)
                    },
                    onTileClick = { col, row ->
                        if (gameState.activeTool == ActiveTool.HAMMER) {
                            viewModel.useHammer(col, row)
                        } else if (gameState.activeTool == ActiveTool.SWITCH) {
                            viewModel.selectTileForSwap(col, row)
                        }
                    },
                    onAimingColumnChange = { col ->
                        if (col != null) {
                            viewModel.setSelectedColumn(col)
                        }
                        viewModel.setAimingColumn(col)
                    },
                    feedbackEvent = feedbackEvent,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                LauncherDock(
                    currentTile = gameState.currentTile,
                    nextTile = gameState.nextTile,
                    onShootCurrent = {
                        viewModel.shootTile(selectedColumn)
                    },
                    onColumnSelect = { col ->
                        viewModel.setSelectedColumn(col)
                    },
                    onAimingColumnChange = { col ->
                        if (col != null) {
                            viewModel.setSelectedColumn(col)
                        }
                        viewModel.setAimingColumn(col)
                    },
                    onShootColumn = { col ->
                        viewModel.setSelectedColumn(col)
                        viewModel.shootTile(col)
                    }
                )

                GameToolbar(
                    undoUses = gameState.undoUses,
                    hammerUses = gameState.hammerUses,
                    switchUses = gameState.switchUses,
                    activeTool = gameState.activeTool,
                    onUndoClick = { viewModel.undo() },
                    onHammerClick = { viewModel.toggleTool(ActiveTool.HAMMER) },
                    onSwitchClick = { viewModel.toggleTool(ActiveTool.SWITCH) },
                    isEnabled = gameState.status == GameStatus.PLAYING
                )

                BannerAd()
            }

            if (uiState.isPauseDialogShown) {
                PauseDialog(
                    onResume = { viewModel.resumeGame() },
                    onRestart = { handleRestart() },
                    onHome = { handleNavigateHome() },
                    subtitle = "Target Goal: ${gameState.targetTile}"
                )
            }

            if (uiState.isGameOverDialogShown) {
                GameOverDialog(
                    score = gameState.score,
                    bestScore = gameState.bestScore,
                    onRestart = { handleRestart() },
                    onHome = { handleNavigateHome() },
                    message = "Columns are full! No more moves."
                )
            }

            if (uiState.isWinDialogShown) {
                WinDialog(
                    targetGoal = gameState.targetTile,
                    score = gameState.score,
                    onKeepPlaying = { viewModel.continueGame() },
                    onNewGame = { handleRestart() },
                    onHome = { handleNavigateHome() },
                    onDismissRequest = { viewModel.continueGame() }
                )
            }
        }
    }
}
