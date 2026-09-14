package com.aethertrace.numberdrop2048hexa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.aethertrace.numberdrop2048hexa.ui.model.ActiveTool
import com.aethertrace.numberdrop2048hexa.ui.model.FeedbackEvent

@Composable
fun ColumnDropBoard(
    grid: List<List<Int>>, // 5 columns, max 8 rows each
    activeTool: ActiveTool,
    firstSelectedTile: Pair<Int, Int>?,
    selectedColumn: Int,
    aimingColumn: Int?,
    onColumnSelect: (col: Int) -> Unit,
    onColumnClick: (col: Int) -> Unit,
    onTileClick: (col: Int, row: Int) -> Unit,
    modifier: Modifier = Modifier,
    onAimingColumnChange: ((col: Int?) -> Unit)? = null,
    onShootColumn: ((col: Int) -> Unit)? = null,
    feedbackEvent: FeedbackEvent? = null
) {
    val numCols = 5
    val numRows = 8
    var boardWidth by remember { mutableStateOf(0f) }
    var currentAimCol by remember { mutableStateOf<Int?>(null) }

    val horizontalPadding = 4.dp
    val verticalPadding = 2.dp
    val columnSpacing = 3.dp
    val rowSpacing = 2.dp
    val colInnerPadding = 2.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .onSizeChanged { boardWidth = it.width.toFloat() }
            .pointerInput(activeTool, boardWidth) {
                if (activeTool == ActiveTool.NONE && boardWidth > 0f) {
                    val colWidth = boardWidth / numCols
                    detectDragGestures(
                        onDragStart = { offset ->
                            val col = (offset.x / colWidth).toInt().coerceIn(0, numCols - 1)
                            currentAimCol = col
                            onColumnSelect(col)
                            onAimingColumnChange?.invoke(col)
                        },
                        onDrag = { change, _ ->
                            val col = (change.position.x / colWidth).toInt().coerceIn(0, numCols - 1)
                            currentAimCol = col
                            onColumnSelect(col)
                            onAimingColumnChange?.invoke(col)
                        },
                        onDragEnd = {
                            currentAimCol?.let { col ->
                                onColumnSelect(col)
                                onShootColumn?.invoke(col)
                            }
                            currentAimCol = null
                            onAimingColumnChange?.invoke(null)
                        },
                        onDragCancel = {
                            currentAimCol = null
                            onAimingColumnChange?.invoke(null)
                        }
                    )
                }
            }
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth - (horizontalPadding * 2)
        val cellWidth = availableWidth / 6f
        val cellHeight = maxHeight / 8.5f

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(columnSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (c in 0 until numCols) {
                    val columnTiles = if (c < grid.size) grid[c] else emptyList()
                    val isSelected = selectedColumn == c || aimingColumn == c

                    val colBorder = if (isSelected) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    } else {
                        Modifier
                    }

                    Column(
                        modifier = Modifier
                            .width(cellWidth)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            )
                            .then(colBorder)
                            .pointerInput(activeTool) {
                                if (activeTool == ActiveTool.NONE) {
                                    detectTapGestures(
                                        onTap = {
                                            onColumnSelect(c)
                                            onColumnClick(c)
                                        },
                                        onPress = {
                                            onColumnSelect(c)
                                            onAimingColumnChange?.invoke(c)
                                            tryAwaitRelease()
                                            onAimingColumnChange?.invoke(null)
                                        }
                                    )
                                }
                            }
                            .padding(colInnerPadding),
                        verticalArrangement = Arrangement.spacedBy(rowSpacing),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (r in 0 until numRows) {
                            val tileValue = if (r < columnTiles.size) columnTiles[r] else 0
                            val isSelectedTile = firstSelectedTile == Pair(c, r)

                            val tileBorder = if (isSelectedTile) {
                                Modifier.border(2.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(8.dp))
                            } else {
                                Modifier
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(cellHeight)
                                    .then(tileBorder)
                                    .clickable(enabled = activeTool != ActiveTool.NONE && tileValue > 0) {
                                        onTileClick(c, r)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                TileView(
                                    value = tileValue,
                                    boardSize = maxOf(numCols, numRows),
                                    cellSize = min(cellWidth, cellHeight),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }

        feedbackEvent?.let { event ->
            FloatingFeedbackText(
                feedbackEvent = event,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
