package com.aethertrace.numberdrop2048hexa.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged

@Composable
fun LauncherDock(
    currentTile: Int,
    nextTile: Int,
    onShootCurrent: () -> Unit,
    modifier: Modifier = Modifier,
    onColumnSelect: (col: Int) -> Unit = {},
    onAimingColumnChange: (col: Int?) -> Unit = {},
    onShootColumn: (col: Int) -> Unit = {}
) {
    var dockWidth by remember { mutableStateOf(0f) }
    var currentAimCol by remember { mutableStateOf<Int?>(null) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { dockWidth = it.width.toFloat() }
            .pointerInput(dockWidth) {
                if (dockWidth > 0f) {
                    val colWidth = dockWidth / 5f
                    detectDragGestures(
                        onDragStart = { offset ->
                            val col = (offset.x / colWidth).toInt().coerceIn(0, 4)
                            currentAimCol = col
                            onColumnSelect(col)
                            onAimingColumnChange(col)
                        },
                        onDrag = { change, _ ->
                            val col = (change.position.x / colWidth).toInt().coerceIn(0, 4)
                            currentAimCol = col
                            onColumnSelect(col)
                            onAimingColumnChange(col)
                        },
                        onDragEnd = {
                            currentAimCol?.let { col ->
                                onShootColumn(col)
                            }
                            currentAimCol = null
                            onAimingColumnChange(null)
                        },
                        onDragCancel = {
                            currentAimCol = null
                            onAimingColumnChange(null)
                        }
                    )
                }
            },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "NEXT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TileView(
                        value = nextTile,
                        boardSize = 5,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Surface(
                onClick = onShootCurrent,
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .height(60.dp)
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TileView(
                            value = currentTile,
                            boardSize = 5,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "SHOOT TILE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Tap column to drop",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = "Shoot",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
