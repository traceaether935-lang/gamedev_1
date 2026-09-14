package com.aethertrace.numberdrop2048hexa.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Grid4x4
import androidx.compose.material.icons.rounded.Hexagon
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ViewColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aethertrace.numberdrop2048hexa.auth.PlayGamesAuthManager
import com.aethertrace.numberdrop2048hexa.ui.components.ClassicSetupDialog
import com.aethertrace.numberdrop2048hexa.ui.components.HexSetupDialog
import com.aethertrace.numberdrop2048hexa.ui.components.MediumRectangleAd
import com.aethertrace.numberdrop2048hexa.ui.theme._2049Theme

@Composable
fun HomeScreen(
    playGamesAuthManager: PlayGamesAuthManager? = null,
    onPlayClicked: (rows: Int, cols: Int, targetGoal: Int, missionId: Int?) -> Unit,
    onHexagonModeClicked: (radius: Int) -> Unit,
    onColumnDropClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onStatisticsClicked: () -> Unit,
    modifier: Modifier = Modifier,
    onStoreClicked: (() -> Unit)? = null,
) {
    var showClassicSetupDialog by remember { mutableStateOf(false) }
    var showHexSetupDialog by remember { mutableStateOf(false) }

    val isAuthenticated by (playGamesAuthManager?.isAuthenticated?.collectAsState() ?: remember { mutableStateOf(false) })
    val playerName by (playGamesAuthManager?.playerName?.collectAsState() ?: remember { mutableStateOf(null) })

    if (showClassicSetupDialog) {
        ClassicSetupDialog(
            onDismissRequest = { showClassicSetupDialog = false },
            onStartGame = { rows, cols, targetGoal, missionId ->
                showClassicSetupDialog = false
                onPlayClicked(rows, cols, targetGoal, missionId)
            },
        )
    }

    if (showHexSetupDialog) {
        HexSetupDialog(
            onDismissRequest = { showHexSetupDialog = false },
            onStartGame = { radius ->
                showHexSetupDialog = false
                onHexagonModeClicked(radius)
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = {
                        if (!isAuthenticated) {
                            playGamesAuthManager?.signIn()
                        } else {
                            playGamesAuthManager?.checkAuthentication()
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAuthenticated) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isAuthenticated) {
                                if (!playerName.isNullOrBlank()) "🎮 $playerName" else "🎮 Play Games: Connected"
                            } else {
                                "🎮 Play Games: Offline"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isAuthenticated) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onStoreClicked != null) {
                        IconButton(
                            onClick = onStoreClicked,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ShoppingBag,
                                contentDescription = "Store",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = onStatisticsClicked,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = "Statistics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onSettingsClicked,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Game Title Banner ("2048 Puzzle")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFEDC22E),
                                    Color(0xFFF65E3B)
                                )
                            )
                        )
                        .padding(vertical = 28.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Number Drop",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2048 Hexa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick buttons / cards:
            // 🎮 Play Classic
            Button(
                onClick = { showClassicSetupDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Grid4x4,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Play Classic",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // ⬡ Hexagon Merge
            Button(
                onClick = { showHexSetupDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Hexagon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Hexagon Merge",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // 📊 Column Drop
            Button(
                onClick = onColumnDropClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.ViewColumn,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Column Drop",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }



            Spacer(modifier = Modifier.height(16.dp))

            MediumRectangleAd()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    _2049Theme {
        HomeScreen(
            onPlayClicked = { _, _, _, _ -> },
            onHexagonModeClicked = { _ -> },
            onColumnDropClicked = {},
            onSettingsClicked = {},
            onStatisticsClicked = {}
        )
    }
}
