package com.aethertrace.numberdrop2048hexa.ui.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aethertrace.numberdrop2048hexa.ui.theme._2049Theme
import com.game.a2048.GameState
import com.game.a2048.Mission
import com.game.a2048.MissionType
import kotlin.math.log2

@Composable
fun MissionCard(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val mission = gameState.activeMission ?: return

    val maxTile = gameState.grid.flatten().maxOrNull() ?: 0
    val moveCount = gameState.moveCount
    val score = gameState.currentScore

    // Calculate progress fraction
    val progress: Float = when (mission.type) {
        MissionType.TILE_REACH -> {
            if (maxTile <= 0) 0f
            else {
                val currentLog = log2(maxTile.toFloat())
                val targetLog = log2(mission.targetValue.toFloat())
                (currentLog / targetLog).coerceIn(0f, 1f)
            }
        }
        MissionType.MOVE_LIMIT_TILE -> {
            if (maxTile >= mission.targetValue) 1f
            else {
                val tileRatio = if (maxTile <= 0) 0f else (log2(maxTile.toFloat()) / log2(mission.targetValue.toFloat()))
                tileRatio.coerceIn(0f, 1f)
            }
        }
        MissionType.SCORE_REACH -> {
            (score.toFloat() / mission.targetValue.toFloat()).coerceIn(0f, 1f)
        }
        MissionType.COMBO_MERGE -> {
            if (gameState.isMissionCompleted) 1f else 0f
        }
    }

    // Metric text
    val metricText = when (mission.type) {
        MissionType.TILE_REACH -> "Max Tile: $maxTile / ${mission.targetValue}"
        MissionType.MOVE_LIMIT_TILE -> "Moves: $moveCount/${mission.moveLimit} • Max Tile: $maxTile/${mission.targetValue}"
        MissionType.SCORE_REACH -> "Score: $score / ${mission.targetValue}"
        MissionType.COMBO_MERGE -> "Target: ${mission.targetValue} merges in 1 move"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = mission.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = mission.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        gameState.isMissionCompleted -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        gameState.isMissionFailed -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (statusText, statusColor) = when {
                            gameState.isMissionCompleted -> Pair("DONE", Color(0xFF2E7D32))
                            gameState.isMissionFailed -> Pair("FAILED", MaterialTheme.colorScheme.onErrorContainer)
                            else -> Pair("TARGET", MaterialTheme.colorScheme.onPrimaryContainer)
                        }

                        Icon(
                            imageVector = when {
                                gameState.isMissionCompleted -> Icons.Rounded.CheckCircle
                                gameState.isMissionFailed -> Icons.Rounded.Error
                                else -> Icons.Rounded.Star
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (gameState.isMissionFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = metricText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(mission.rewardStars) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MissionCardPreview() {
    _2049Theme {
        val sampleMission = Mission(
            id = 2,
            title = "Speedrunner",
            description = "Create a 128 Tile in 30 moves or less",
            type = MissionType.MOVE_LIMIT_TILE,
            targetValue = 128,
            moveLimit = 30,
            rewardStars = 3
        )
        val state = GameState(
            rows = 4,
            cols = 4,
            grid = listOf(listOf(64, 2, 0, 0)),
            moveCount = 14,
            activeMission = sampleMission
        )
        MissionCard(gameState = state)
    }
}
