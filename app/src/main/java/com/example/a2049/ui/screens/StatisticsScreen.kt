package com.example.a2049.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a2049.ui.theme._2049Theme
import com.example.a2049.ui.viewmodel.StatisticsViewModel
import com.example.a2049.ui.viewmodel.StatisticsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModelFactory(context)
    )

    val selectedBoardSize by viewModel.selectedBoardSize.collectAsState()
    val stats4x4 by viewModel.stats4x4.collectAsState()
    val stats5x5 by viewModel.stats5x5.collectAsState()

    val currentStats = if (selectedBoardSize == 5) stats5x5 else stats4x4

    val winRate = if (currentStats.gamesPlayed > 0) {
        (currentStats.gamesWon * 100) / currentStats.gamesPlayed
    } else 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Statistics",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrimaryTabRow(
                selectedTabIndex = if (selectedBoardSize == 5) 1 else 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedBoardSize == 4,
                    onClick = { viewModel.selectBoardSize(4) },
                    text = { Text("4 × 4 Grid", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedBoardSize == 5,
                    onClick = { viewModel.selectBoardSize(5) },
                    text = { Text("5 × 5 Grid", fontWeight = FontWeight.Bold) }
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    StatCard(
                        title = "Games Played",
                        value = currentStats.gamesPlayed.toString(),
                        icon = Icons.Rounded.PlayArrow
                    )
                }
                item {
                    StatCard(
                        title = "Games Won",
                        value = currentStats.gamesWon.toString(),
                        icon = Icons.Rounded.EmojiEvents
                    )
                }
                item {
                    StatCard(
                        title = "Win Rate",
                        value = "$winRate%",
                        icon = Icons.Rounded.Leaderboard
                    )
                }
                item {
                    StatCard(
                        title = "Highest Score",
                        value = currentStats.highestScore.toString(),
                        icon = Icons.Rounded.MilitaryTech
                    )
                }
                item {
                    StatCard(
                        title = "Highest Tile",
                        value = currentStats.highestTile.toString(),
                        icon = Icons.Rounded.OpenInFull
                    )
                }
                item {
                    StatCard(
                        title = "Total Moves",
                        value = currentStats.totalMoves.toString(),
                        icon = Icons.Rounded.Swipe
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsScreenPreview() {
    _2049Theme {
        StatisticsScreen(onNavigateBack = {})
    }
}
